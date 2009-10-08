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

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipSelection;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOAmplify;
import ch.laoe.operation.AOHighPass;
import ch.laoe.operation.AOMeasure;
import ch.laoe.operation.AONormalize;
import ch.laoe.operation.AOSaturate;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChooser;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to browse the manual.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 24.10.00 erster Entwurf oli4 <br>
 *          10.11.00 normalize & saturate to 1..32bit oli4 <br>
 *          27.01.01 float-array based oli4 <br>
 *          06.05.01 add DC-removal oli4 <br>
 *          01.12.01 add invert & normalize modes oli4 <br>
 *          28.06.02 coupled normalisation added oli4 <br>
 */
public class GPAmplify extends GPluginFrame {
    public GPAmplify(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "amplify";
    }

    // GUI
    private JCheckBox invert, normalize, saturate, removeDc, coupled;

    private UiControlText amplification, bitWidth;

    private JComboBox normalizeMode;

    private JButton applyConst, applyVar;

    private GClipLayerChooser layerChooser;

    private JComboBox unit;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // const tab
        JPanel pConst = new JPanel();
        UiCartesianLayout clt = new UiCartesianLayout(pConst, 10, 6);
        clt.setPreferredCellSize(new Dimension(25, 35));
        pConst.setLayout(clt);
        clt.add(new JLabel(GLanguage.translate("amplification")), 0, 0, 5, 1);
        amplification = new GControlTextA(10, true, true);
        amplification.setDataRange(-10000, 10000);
        amplification.setData(1);
        // amplification.setUnit("%");
        clt.add(amplification, 5, 0, 5, 1);
        invert = new JCheckBox(GLanguage.translate("invert"));
        clt.add(invert, 0, 1, 5, 1);
        removeDc = new JCheckBox(GLanguage.translate("removeDc"));
        clt.add(removeDc, 5, 1, 5, 1);
        clt.add(new JLabel(GLanguage.translate("bitWidth")), 0, 2, 5, 1);
        bitWidth = new UiControlText(4, true, false);
        bitWidth.setDataRange(1, 32);
        bitWidth.setData(16);
        clt.add(bitWidth, 5, 2, 3, 1);
        normalize = new JCheckBox(GLanguage.translate("normalize"));
        clt.add(normalize, 0, 3, 5, 1);
        String modeItem[] = {
            GLanguage.translate("peak"), GLanguage.translate("RMS")
        };
        normalizeMode = new JComboBox(modeItem);
        normalizeMode.setSelectedIndex(0);
        clt.add(normalizeMode, 5, 3, 3, 1);
        saturate = new JCheckBox(GLanguage.translate("saturate"));
        clt.add(saturate, 0, 4, 5, 1);
        coupled = new JCheckBox(GLanguage.translate("coupled"));
        clt.add(coupled, 5, 4, 5, 1);
        applyConst = new JButton(GLanguage.translate("apply"));
        clt.add(applyConst, 3, 5, 4, 1);
        tabbedPane.add(GLanguage.translate("constant"), pConst);

        // var tab
        JPanel pVar = new JPanel();
        UiCartesianLayout clv = new UiCartesianLayout(pVar, 10, 6);
        clv.setPreferredCellSize(new Dimension(25, 35));
        pVar.setLayout(clv);
        layerChooser = new GClipLayerChooser(getMain(), "envelopeCurve");
        clv.add(layerChooser, 0, 0, 10, 3);
        applyVar = new JButton(GLanguage.translate("apply"));
        clv.add(applyVar, 3, 5, 4, 1);
        tabbedPane.add(GLanguage.translate("envelope"), pVar);

        frame.getContentPane().add(tabbedPane);
        pack();
        updateConstantComponents();

        eventDispatcher = new EventDispatcher();
        normalize.addActionListener(eventDispatcher);
        saturate.addActionListener(eventDispatcher);
        applyConst.addActionListener(eventDispatcher);
        applyVar.addActionListener(eventDispatcher);
    }

    private void updateConstantComponents() {
        if (normalize.isSelected() || saturate.isSelected()) {
            bitWidth.setEnabled(true);
        } else {
            bitWidth.setEnabled(false);
        }

        if (normalize.isSelected()) {
            normalizeMode.setEnabled(true);
            coupled.setEnabled(true);
        } else {
            normalizeMode.setEnabled(false);
            coupled.setEnabled(false);
        }
    }

    public void reload() {
        layerChooser.reload();
        bitWidth.setData(getFocussedClip().getSampleWidth());
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                // GUI...
                if (e.getSource() == normalize) {
                    Debug.println(1, "plugin " + getName() + " [normalize] clicked");
                    updateConstantComponents();
                    return;
                } else if (e.getSource() == saturate) {
                    Debug.println(1, "plugin " + getName() + " [saturate] clicked");
                    updateConstantComponents();
                    return;
                }

                // operations...
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                if (e.getSource() == applyConst) {
                    Debug.println(1, "plugin " + getName() + " [apply const] clicked");
                    onApplyConst();
                    reloadFocussedClipEditor();
                } else if (e.getSource() == applyVar) {
                    Debug.println(1, "plugin " + getName() + " [apply var] clicked");
                    onApplyVar();
                    reloadFocussedClipEditor();
                }
                GProgressViewer.setProgress(70);
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } catch (Exception exc) {
                exc.printStackTrace();
            }

        }
    }

    private void onApplyConst() {
        float bitWidthMaxValue = (1 << ((int) bitWidth.getData() - 1)) - 1;
        float a = (float) amplification.getData();
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        // remove DC ?
        if (removeDc.isSelected()) {
            ls.operateEachChannel(new AOHighPass(0.f, 1.f, 5.f / getFocussedClip().getSampleRate()));
        }

        // invert ?
        if (invert.isSelected()) {
            a = -a;
        }
        // normalize ?
        if (normalize.isSelected()) {
            if (coupled.isSelected()) {
//              float n;
                switch (normalizeMode.getSelectedIndex()) {
                case 0: // peak
                {
                    AOMeasure o = new AOMeasure((int) bitWidth.getData());
                    ls.operateEachChannel(o);
                    float p = o.getAbsoluteMax();
                    ls.operateEachChannel(new AOAmplify(bitWidthMaxValue / p * a));
                }
                    break;

                case 1: // RMS
                {
                    AOMeasure o = new AOMeasure((int) bitWidth.getData());
                    ls.operateEachChannel(o);
                    float p = o.getRms();
                    ls.operateEachChannel(new AOAmplify(bitWidthMaxValue / p * a));
                }
                    break;
                }
            } else {
                switch (normalizeMode.getSelectedIndex()) {
                case 0: // peak
                    ls.operateEachChannel(new AONormalize(AONormalize.PEAK, (bitWidthMaxValue * a)));
                    break;

                case 1: // RMS
                    ls.operateEachChannel(new AONormalize(AONormalize.RMS, (bitWidthMaxValue * a)));
                    break;
                }
            }
        } else {
            // normal amplification ?
            ls.operateEachChannel(new AOAmplify(a));
        }

        // saturation limit ?
        if (saturate.isSelected()) {
            ls.operateEachChannel(new AOSaturate(bitWidthMaxValue));
        }
    }

    private void onApplyVar() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ALayerSelection ps = layerChooser.getSelectedLayer().createLayerSelection();
        AClipSelection cs = new AClipSelection(new AClip());
        cs.addLayerSelection(ls);
        cs.addLayerSelection(ps);
        cs.operateLayer0WithLayer1(new AOAmplify());
    }

}
