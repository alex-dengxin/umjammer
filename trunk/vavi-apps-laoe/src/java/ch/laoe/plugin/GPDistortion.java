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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipSelection;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AODistort;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChooser;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GControlTextY;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * Class: GPDistortion @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to different crop modes
 * 
 * @version 22.07.01 first draft oli4
 * 
 */
public class GPDistortion extends GPluginFrame {
    public GPDistortion(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "distortion";
    }

    // GUI
    private String distortionTypeItem[] = {
        GLanguage.translate("clamping"), GLanguage.translate("noiseGate")
    };

    private JComboBox distortionType;

    private UiControlText threshold;

    private UiControlText clamping;

    private GClipLayerChooser layerChooser;

    private JButton applyConst, applyVar;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JTabbedPane tab = new JTabbedPane();

        // const tab...
        JPanel pConst = new JPanel();
        UiCartesianLayout lConst = new UiCartesianLayout(pConst, 10, 4);
        lConst.setPreferredCellSize(new Dimension(25, 35));
        pConst.setLayout(lConst);

        lConst.add(new JLabel(GLanguage.translate("mode")), 0, 0, 4, 1);
        distortionType = new JComboBox(distortionTypeItem);
        lConst.add(distortionType, 4, 0, 6, 1);

        lConst.add(new JLabel(GLanguage.translate("threshold")), 0, 1, 4, 1);
        threshold = new GControlTextY(getMain(), 6, true, true);
        threshold.setDataRange(0, 1000000);
        threshold.setData(33);
        lConst.add(threshold, 4, 1, 6, 1);

        lConst.add(new JLabel(GLanguage.translate("clamping")), 0, 2, 4, 1);
        clamping = new GControlTextX(getMain(), 6, true, true);
        clamping.setDataRange(0, 1000000);
        clamping.setData(50);
        lConst.add(clamping, 4, 2, 6, 1);

        applyConst = new JButton(GLanguage.translate("apply"));
        lConst.add(applyConst, 3, 3, 4, 1);

        tab.add(GLanguage.translate("constant"), pConst);

        // var tab...
        JPanel pVar = new JPanel();
        UiCartesianLayout lVar = new UiCartesianLayout(pVar, 10, 4);
        lVar.setPreferredCellSize(new Dimension(25, 35));
        pVar.setLayout(lVar);

        layerChooser = new GClipLayerChooser(getMain(), "transferFunction");
        lVar.add(layerChooser, 0, 0, 10, 3);

        applyVar = new JButton(GLanguage.translate("apply"));
        lVar.add(applyVar, 3, 3, 4, 1);

        tab.add(GLanguage.translate("f(amplitude)"), pVar);

        frame.getContentPane().add(tab);
        pack();

        eventDispatcher = new EventDispatcher();
        applyConst.addActionListener(eventDispatcher);
        applyVar.addActionListener(eventDispatcher);
    }

    public void start() {
        super.start();
    }

    public void reload() {
        layerChooser.reload();
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
            }
            GProgressViewer.setProgress(100);
            reloadFocussedClipEditor();
            updateHistory(GLanguage.translate(getName()));
            autoCloseFrame();
            GProgressViewer.exitSubProgress();
            GProgressViewer.finish();
        }
    }

    private void onApplyConst() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        // distortion-type ?
        switch (distortionType.getSelectedIndex()) {
        case 0: // clamping
            ls.operateEachChannel(new AODistort((float) threshold.getData(), (float) clamping.getData(), AODistort.CLAMPING_TYPE));
            break;

        case 1: // noise gate
            ls.operateEachChannel(new AODistort((float) threshold.getData(), (float) clamping.getData(), AODistort.NOISE_GATING_TYPE));
            break;
        }
    }

    private void onApplyVar() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ALayerSelection ps = layerChooser.getSelectedLayer().createLayerSelection();
        AClipSelection cs = new AClipSelection(new AClip());
        cs.addLayerSelection(ls);
        cs.addLayerSelection(ps);
        cs.operateLayer0WithLayer1(new AODistort());
    }

}
