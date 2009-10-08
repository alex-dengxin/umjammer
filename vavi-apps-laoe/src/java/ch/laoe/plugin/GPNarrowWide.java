/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.plugin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.laoe.clip.ALayer;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AONarrowWide;
import ch.laoe.operation.AONarrowWideSweep;
import ch.laoe.operation.AORemoveMono;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChannelChooser;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to narrow to mono or wide to stereo
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 03.06.01 first draft oli4 <br>
 *          02.12.2001 modify ch1/2 separatly oli4 <br>
 *          30.04.2002 sweep-mode introduced oli4 <br>
 *          11.07.2002 remove mono introduced oli4
 */
public class GPNarrowWide extends GPluginFrame {
    public GPNarrowWide(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "narrowWide";
    }

    // GUI
    private String modeItem[] = {
        GLanguage.translate("narrow"), GLanguage.translate("wide")
    };

    private JComboBox mode;

    private UiControlText value, beginValue, endValue, pan;

    private JCheckBox modifyCh1Const, modifyCh2Const, modifyCh1Var, modifyCh2Var, modifyCh1Sweep, modifyCh2Sweep;

    private JCheckBox continueBefore, continueAfter;

    private GClipLayerChannelChooser wideChannel;

    private JButton applyConst, applyVar, applySweep, applyMono;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // const tab
        JPanel pConst = new JPanel();
        UiCartesianLayout clConst = new UiCartesianLayout(pConst, 10, 6);
        clConst.setPreferredCellSize(new Dimension(25, 35));
        pConst.setLayout(clConst);

        clConst.add(new JLabel(GLanguage.translate("mode")), 0, 0, 4, 1);
        mode = new JComboBox(modeItem);
        clConst.add(mode, 4, 0, 6, 1);

        clConst.add(new JLabel(GLanguage.translate("value")), 0, 1, 4, 1);
        value = new GControlTextA(6, true, true);
        value.setDataRange(0, 1);
        value.setData(1);
        clConst.add(value, 4, 1, 6, 1);

        modifyCh1Const = new JCheckBox(GLanguage.translate("channel") + " 1");
        modifyCh1Const.setSelected(true);
        clConst.add(modifyCh1Const, 0, 2, 5, 1);

        modifyCh2Const = new JCheckBox(GLanguage.translate("channel") + " 2");
        modifyCh2Const.setSelected(true);
        clConst.add(modifyCh2Const, 5, 2, 5, 1);

        applyConst = new JButton(GLanguage.translate("apply"));
        clConst.add(applyConst, 3, 5, 4, 1);

        tabbedPane.add(GLanguage.translate("constant"), pConst);

        // sweep
        JPanel pSweep = new JPanel();
        UiCartesianLayout clSweep = new UiCartesianLayout(pSweep, 10, 6);
        clSweep.setPreferredCellSize(new Dimension(25, 35));
        pSweep.setLayout(clSweep);

        clSweep.add(new JLabel(GLanguage.translate("begin")), 0, 0, 4, 1);
        beginValue = new GControlTextA(6, true, true);
        beginValue.setDataRange(0, 2);
        beginValue.setData(1);
        clSweep.add(beginValue, 4, 0, 6, 1);

        clSweep.add(new JLabel(GLanguage.translate("end")), 0, 1, 4, 1);
        endValue = new GControlTextA(6, true, true);
        endValue.setDataRange(0, 2);
        endValue.setData(1);
        clSweep.add(endValue, 4, 1, 6, 1);

        continueBefore = new JCheckBox(GLanguage.translate("continueBefore"));
        clSweep.add(continueBefore, 0, 2, 5, 1);

        continueAfter = new JCheckBox(GLanguage.translate("continueAfter"));
        clSweep.add(continueAfter, 5, 2, 5, 1);

        modifyCh1Sweep = new JCheckBox(GLanguage.translate("channel") + " 1");
        modifyCh1Sweep.setSelected(true);
        clSweep.add(modifyCh1Sweep, 0, 3, 5, 1);

        modifyCh2Sweep = new JCheckBox(GLanguage.translate("channel") + " 2");
        modifyCh2Sweep.setSelected(true);
        clSweep.add(modifyCh2Sweep, 5, 3, 5, 1);

        applySweep = new JButton(GLanguage.translate("apply"));
        clSweep.add(applySweep, 3, 5, 4, 1);

        tabbedPane.add(GLanguage.translate("sweep"), pSweep);

        // variable
        JPanel pVar = new JPanel();
        UiCartesianLayout clVar = new UiCartesianLayout(pVar, 10, 6);
        clVar.setPreferredCellSize(new Dimension(25, 35));
        pVar.setLayout(clVar);

        wideChannel = new GClipLayerChannelChooser(getMain(), "narrowWideCurve");
        clVar.add(wideChannel, 0, 0, 10, 4);

        modifyCh1Var = new JCheckBox(GLanguage.translate("channel") + " 1");
        modifyCh1Var.setSelected(true);
        clVar.add(modifyCh1Var, 0, 4, 5, 1);

        modifyCh2Var = new JCheckBox(GLanguage.translate("channel") + " 2");
        modifyCh2Var.setSelected(true);
        clVar.add(modifyCh2Var, 5, 4, 5, 1);

        applyVar = new JButton(GLanguage.translate("apply"));
        clVar.add(applyVar, 3, 5, 4, 1);

        tabbedPane.add(GLanguage.translate("f(time)"), pVar);

        // remove mono
        JPanel pMono = new JPanel();
        UiCartesianLayout clMono = new UiCartesianLayout(pMono, 10, 6);
        clMono.setPreferredCellSize(new Dimension(25, 35));
        pMono.setLayout(clMono);

        clMono.add(new JLabel(GLanguage.translate("pan")), 0, 1, 4, 1);
        pan = new GControlTextA(6, true, true);
        pan.setDataRange(1, 2);
        pan.setData(1.5);
        clMono.add(pan, 4, 1, 6, 1);

        applyMono = new JButton(GLanguage.translate("apply"));
        clMono.add(applyMono, 3, 5, 4, 1);

        tabbedPane.add(GLanguage.translate("removeMono"), pMono);

        frame.getContentPane().add(tabbedPane);
        pack();

        eventDispatcher = new EventDispatcher();
        applyConst.addActionListener(eventDispatcher);
        applyVar.addActionListener(eventDispatcher);
        applySweep.addActionListener(eventDispatcher);
        applyMono.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            GProgressViewer.start(getName());
            GProgressViewer.entrySubProgress();
            GProgressViewer.setProgress(70);
            if (e.getSource() == applyConst) {
                Debug.println(1, "plugin " + getName() + " [apply const] clicked");
                onApplyConst();
            } else if (e.getSource() == applyVar) {
                Debug.println(1, "plugin " + getName() + " [apply var] clicked");
                onApplyVar();
            } else if (e.getSource() == applySweep) {
                Debug.println(1, "plugin " + getName() + " [apply sweep] clicked");
                onApplySweep();
            } else if (e.getSource() == applyMono) {
                Debug.println(1, "plugin " + getName() + " [apply remove mono] clicked");
                onApplyRemoveMono();
            }
            GProgressViewer.setProgress(100);
            updateHistory(GLanguage.translate(getName()));
            reloadFocussedClipEditor();
            autoCloseFrame();
            GProgressViewer.exitSubProgress();
            GProgressViewer.finish();
        }
    }

    private void onApplyConst() {
        // mode...
        float f = 1;
        switch (mode.getSelectedIndex()) {
        case 0: // narrow ?
            f = (float) (1 - value.getData());
            break;

        case 1: // wide ?
            f = (float) (1 + value.getData());
            break;
        }

        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateChannel0WithChannel1(new AONarrowWide(modifyCh1Const.isSelected(), modifyCh2Const.isSelected(), f));
    }

    private void onApplySweep() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateChannel0WithChannel1(new AONarrowWideSweep(modifyCh1Const.isSelected(), modifyCh2Const.isSelected(), (float) beginValue.getData(), (float) endValue.getData(), continueBefore.isSelected(), continueAfter.isSelected()));
    }

    private void onApplyVar() {
        ALayerSelection l = getFocussedClip().getSelectedLayer().getLayerSelection();
        ALayerSelection ls = new ALayerSelection(new ALayer());
        ls.addChannelSelection(l.getChannelSelection(0));
        ls.addChannelSelection(l.getChannelSelection(1));
        ls.addChannelSelection(wideChannel.getSelectedChannel().getChannelSelection());
        ls.operateChannel0WithChannel1WithChannel2(new AONarrowWide(modifyCh1Var.isSelected(), modifyCh2Var.isSelected()));
    }

    private void onApplyRemoveMono() {
        float p = (float) pan.getData();
        ALayer l = getFocussedClip().getSelectedLayer();
        ALayerSelection ls = l.getLayerSelection();
        ls.operateChannel0WithChannel1(new AORemoveMono(p));
        l.remove(1);
    }

    public void reload() {
        wideChannel.reload();
    }

}
