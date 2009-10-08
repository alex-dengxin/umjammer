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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipSelection;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOResample;
import ch.laoe.operation.AOSweepResample;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChooser;
import ch.laoe.ui.GControlTextAF;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * GPResample @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to resample.
 * 
 * @version 26.10.00 erster Entwurf oli4 29.05.01 add sweep mode oli4
 * 
 */
public class GPResample extends GPluginFrame {
    public GPResample(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "resample";
    }

    // GUI
    private JComboBox cOrder, vOrder, swOrder;

    private JRadioButton bySampleRate, byFactor;

    private UiControlText sampleRate, factor, beginFactor, endFactor;

    private JButton applyConst, applySweep, applyVar;

    private GClipLayerChooser layerChooser;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // constant resampling
        JPanel pConst = new JPanel();
        UiCartesianLayout clt = new UiCartesianLayout(pConst, 10, 5);
        clt.setPreferredCellSize(new Dimension(25, 35));
        pConst.setLayout(clt);
        bySampleRate = new JRadioButton(GLanguage.translate("sampleRate"));
        clt.add(bySampleRate, 0, 0, 4, 1);
        byFactor = new JRadioButton(GLanguage.translate("factor"));
        clt.add(byFactor, 0, 1, 4, 1);
        ButtonGroup bg = new ButtonGroup();
        bg.add(bySampleRate);
        bg.add(byFactor);
        bySampleRate.setSelected(true);
        sampleRate = new UiControlText(7, true, false);
        sampleRate.setDataRange(100, 100000);
        clt.add(sampleRate, 4, 0, 5, 1);
        factor = new GControlTextAF(7, true, true);
        factor.setDataRange(.0001, 1000);
        factor.setData(1);
        clt.add(factor, 4, 1, 5, 1);
        String orderItem[] = {
            GLanguage.translate("0Order"), GLanguage.translate("1Order"), GLanguage.translate("2Order"), GLanguage.translate("3Order")
        };
        cOrder = new JComboBox(orderItem);
        cOrder.setSelectedIndex(3);
        clt.add(cOrder, 0, 2, 4, 1);
        applyConst = new JButton(GLanguage.translate("apply"));
        clt.add(applyConst, 3, 4, 4, 1);
        tabbedPane.add(GLanguage.translate("constant"), pConst);

        // sweep mode
        JPanel pSweep = new JPanel();
        UiCartesianLayout clsw = new UiCartesianLayout(pSweep, 10, 5);
        pSweep.setLayout(clsw);
        clsw.add(new JLabel(GLanguage.translate("beginFactor")), 0, 0, 4, 1);
        beginFactor = new GControlTextAF(7, true, true);
        beginFactor.setDataRange(.0001, 1000);
        beginFactor.setData(1);
        clsw.add(beginFactor, 4, 0, 5, 1);
        clsw.add(new JLabel(GLanguage.translate("endFactor")), 0, 1, 4, 1);
        endFactor = new GControlTextAF(7, true, true);
        endFactor.setDataRange(.0001, 1000);
        endFactor.setData(1);
        clsw.add(endFactor, 4, 1, 5, 1);
        swOrder = new JComboBox(orderItem);
        swOrder.setSelectedIndex(3);
        clsw.add(swOrder, 6, 3, 4, 1);
        applySweep = new JButton(GLanguage.translate("apply"));
        clsw.add(applySweep, 3, 4, 4, 1);
        tabbedPane.add(GLanguage.translate("sweep"), pSweep);

        // variable resampling
        JPanel pVar = new JPanel();
        UiCartesianLayout clv = new UiCartesianLayout(pVar, 10, 5);
        pVar.setLayout(clv);
        layerChooser = new GClipLayerChooser(getMain(), "resampleCurve");
        clv.add(layerChooser, 0, 0, 10, 3);
        vOrder = new JComboBox(orderItem);
        vOrder.setSelectedIndex(3);
        clv.add(vOrder, 6, 3, 4, 1);
        applyVar = new JButton(GLanguage.translate("apply"));
        clv.add(applyVar, 3, 4, 4, 1);
        tabbedPane.add(GLanguage.translate("f(time)"), pVar);

        updateActiveComponents();
        frame.getContentPane().add(tabbedPane);
        pack();

        eventDispatcher = new EventDispatcher();
        applyConst.addActionListener(eventDispatcher);
        applyVar.addActionListener(eventDispatcher);
        applySweep.addActionListener(eventDispatcher);
        bySampleRate.addActionListener(eventDispatcher);
        byFactor.addActionListener(eventDispatcher);
    }

    private void updateActiveComponents() {
        if (bySampleRate.isSelected()) {
            sampleRate.setEnabled(true);
            factor.setEnabled(false);
        } else if (byFactor.isSelected()) {
            sampleRate.setEnabled(false);
            factor.setEnabled(true);
        }

    }

    public void reload() {
        layerChooser.reload();
        sampleRate.setData(getFocussedClip().getSampleRate());
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == bySampleRate) {
                Debug.println(1, "plugin " + getName() + " [by samplerate] clicked");
                updateActiveComponents();
            } else if (e.getSource() == byFactor) {
                Debug.println(1, "plugin " + getName() + " [by factor] clicked");
                updateActiveComponents();
            } else if (e.getSource() == applyConst) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply const] clicked");
                onApplyConst();
                GProgressViewer.setProgress(100);
                pluginHandler.getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
                autoScaleFocussedClip();
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == applyVar) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply var] clicked");
                onApplyVar();
                GProgressViewer.setProgress(100);
                pluginHandler.getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
                autoScaleFocussedClip();
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == applySweep) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply sweep] clicked");
                onApplySweep();
                GProgressViewer.setProgress(100);
                pluginHandler.getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
                autoScaleFocussedClip();
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            }
        }
    }

    private void onApplyConst() {
        float resampleFactor = 1.f;

        // which mode ?
        if (bySampleRate.isSelected()) {
            resampleFactor = getFocussedClip().getSampleRate() / (float) sampleRate.getData();
            getFocussedClip().setSampleRate((float) sampleRate.getData());
        } else if (byFactor.isSelected()) {
            resampleFactor = (float) factor.getData();
        }

        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOResample(resampleFactor, cOrder.getSelectedIndex()));
    }

    private void onApplyVar() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ALayerSelection ps = layerChooser.getSelectedLayer().createLayerSelection();
        AClipSelection cs = new AClipSelection(new AClip());
        cs.addLayerSelection(ls);
        cs.addLayerSelection(ps);

        cs.operateLayer0WithLayer1(new AOResample(vOrder.getSelectedIndex()));
    }

    private void onApplySweep() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        ls.operateEachChannel(new AOSweepResample((float) beginFactor.getData(), (float) endFactor.getData(), swOrder.getSelectedIndex()));
    }
}
