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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipSelection;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOMultiReverb;
import ch.laoe.operation.AOReverb;
import ch.laoe.operation.AOReverbAllPass;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChooser;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;
import ch.oli4.ui.UiPersistance;


/**
 * plugin to perform divers reverbations.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * 
 * 
 * @version 01.11.00 erster Entwurf oli4 <br>
 *          18.04.01 simple reverb only oli4 <br>
 *          20.06.01 3 different reverb types oli4 <br>
 *          12.08.01 variable reverb with parameter-layer oli4 <br>
 */
public class GPReverb extends GPluginFrame {
    public GPReverb(GPluginHandler ph) {
        super(ph);
        try {
            initGui();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getName() {
        return "reverb";
    }

    // GUI
    private JCheckBox allPass, negFeedback, backwardConst, backwardVar;

    private JComboBox room, reverbType;

    private UiControlText dry, wet, gain, delay, multiWidth, multiReverbs;

    private JTable shape;

    private JButton applyConst, applyVar;

    private GClipLayerChooser layerChooser;

    private EventDispatcher eventDispatcher;

    // presets
    private Preset presets[];

    private int presetIndex = 0;

    private class Preset {
        public String name;

        public float dry, wet, delay, gain;

        public double delayShape[], gainShape[];
    }

    private void loadPresets() {
        try {
            UiPersistance persist = new UiPersistance(getName() + ".properties");
            persist.restore();
            int n = persist.getInt("numberOfPresets");
            presets = new Preset[n];

            for (int i = 0; i < n; i++) {
                presets[i] = new Preset();
                presets[i].name = GLanguage.translate(persist.getString("name_" + i));
                presets[i].dry = persist.getFloat("dry_" + i);
                presets[i].wet = persist.getFloat("wet_" + i);
                presets[i].delay = persist.getFloat("delay_" + i);
                presets[i].gain = persist.getFloat("gain_" + i);
                presets[i].delayShape = new double[persist.getInt("shapeWidth_" + i)];
                for (int j = 0; j < presets[i].delayShape.length; j++) {
                    presets[i].delayShape[j] = persist.getDouble("delayShape_" + i + "_" + j);
                }
                presets[i].gainShape = new double[persist.getInt("shapeWidth_" + i)];
                for (int j = 0; j < presets[i].gainShape.length; j++) {
                    presets[i].gainShape[j] = persist.getDouble("gainShape_" + i + "_" + j);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGui() {
        // presets
        loadPresets();

        // GUI
        JPanel p = new JPanel();
        JTabbedPane tab = new JTabbedPane();

        // const tab...
        JPanel pConst = new JPanel();
        UiCartesianLayout clConst = new UiCartesianLayout(pConst, 10, 13);
        clConst.setPreferredCellSize(new Dimension(25, 35));
        pConst.setLayout(clConst);

        clConst.add(new JLabel(GLanguage.translate("room")), 0, 0, 4, 1);
        String roomItems[] = new String[presets.length];
        for (int i = 0; i < roomItems.length; i++) {
            roomItems[i] = presets[i].name;
        }
        room = new JComboBox(roomItems);
        clConst.add(room, 4, 0, 6, 1);

        String reverbTypeItems[] = {
            GLanguage.translate("comb"), GLanguage.translate("allpass"), GLanguage.translate("multi")
        };
        clConst.add(new JLabel(GLanguage.translate("reverbType")), 0, 1, 4, 1);
        reverbType = new JComboBox(reverbTypeItems);
        clConst.add(reverbType, 4, 1, 6, 1);

        clConst.add(new JLabel(GLanguage.translate("dryFactor")), 0, 2, 4, 1);
        dry = new GControlTextA(7, true, true);
        dry.setDataRange(-1, 1);
        dry.setData(1);
        clConst.add(dry, 4, 2, 6, 1);

        clConst.add(new JLabel(GLanguage.translate("wetFactor")), 0, 3, 4, 1);
        wet = new GControlTextA(7, true, true);
        wet.setDataRange(-1, 1);
        wet.setData(1);
        clConst.add(wet, 4, 3, 6, 1);

        clConst.add(new JLabel(GLanguage.translate("delay")), 0, 4, 4, 1);
        delay = new GControlTextX(getMain(), 7, true, true);
        delay.setDataRange(1, 1e9);
        delay.setData(1);
        clConst.add(delay, 4, 4, 6, 1);

        clConst.add(new JLabel(GLanguage.translate("gain")), 0, 5, 4, 1);
        gain = new GControlTextA(7, true, true);
        gain.setDataRange(-1, 1);
        gain.setData(1);
        clConst.add(gain, 4, 5, 6, 1);

        clConst.add(new JLabel(GLanguage.translate("shape")), 0, 6, 4, 1);
        String columnNames[] = {
            GLanguage.translate("delay"), GLanguage.translate("gain")
        };
        shape = new JTable(new Object[maxRows][2], columnNames);
        JScrollPane scrollPane = new JScrollPane(shape);
        shape.setPreferredScrollableViewportSize(new Dimension(200, 70));
        clConst.add(scrollPane, 0, 7, 10, 4);
        onRoomChange();

        negFeedback = new JCheckBox(GLanguage.translate("negativeFeedback"));
        clConst.add(negFeedback, 0, 11, 5, 1);

        backwardConst = new JCheckBox(GLanguage.translate("backward"));
        clConst.add(backwardConst, 5, 11, 5, 1);

        applyConst = new JButton(GLanguage.translate("apply"));
        clConst.add(applyConst, 3, 12, 4, 1);

        tab.add(GLanguage.translate("standard"), pConst);

        // variable tab...
        JPanel pVar = new JPanel();
        UiCartesianLayout clVar = new UiCartesianLayout(pVar, 10, 13);
        clVar.setPreferredCellSize(new Dimension(25, 35));
        pVar.setLayout(clVar);

        layerChooser = new GClipLayerChooser(getMain(), "room");
        clVar.add(layerChooser, 0, 0, 10, 3);

        backwardVar = new JCheckBox(GLanguage.translate("backward"));
        clVar.add(backwardVar, 4, 3, 6, 1);

        applyVar = new JButton(GLanguage.translate("apply"));
        clVar.add(applyVar, 3, 12, 4, 1);
        tab.add(GLanguage.translate("curve"), pVar);

        p.add(tab);
        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        applyConst.addActionListener(eventDispatcher);
        applyVar.addActionListener(eventDispatcher);
        reverbType.addActionListener(eventDispatcher);
        room.addActionListener(eventDispatcher);

        updateGui();
    }

    // rows
    private static final int maxRows = 50;

    private void loadReverbPoints(double d[], double a[]) {
        for (int i = 0; i < maxRows; i++) {
            if (i < d.length) {
                shape.setValueAt(String.valueOf(d[i]), i, 0);
                shape.setValueAt(String.valueOf(a[i]), i, 1);
            } else {
                shape.setValueAt("", i, 0);
                shape.setValueAt("", i, 1);
            }
        }
    }

    public void reload() {
        layerChooser.reload();
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == applyConst) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply const] clicked");
                onApplyConst();
                GProgressViewer.setProgress(100);
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
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == reverbType) {
                Debug.println(1, "plugin " + getName() + " [reverb type] clicked");
                updateGui();
            } else if (e.getSource() == room) {
                Debug.println(1, "plugin " + getName() + " [room changed] clicked");
                onRoomChange();
            }

        }
    }

    private void onRoomChange() {
        presetIndex = room.getSelectedIndex();
        dry.setData(presets[presetIndex].dry);
        wet.setData(presets[presetIndex].wet);
        delay.setData(presets[presetIndex].delay);
        gain.setData(presets[presetIndex].gain);
        loadReverbPoints(presets[presetIndex].delayShape, presets[presetIndex].gainShape);
    }

    private void updateGui() {
        shape.setEnabled(reverbType.getSelectedIndex() == 2);
        // System.out.println("shape enabled = "+shape.isEnabled());
    }

    private void onApplyMulti() {
        // find used length...
        int length = 0;
        for (int i = 0; i < shape.getRowCount(); i++) {
            try {
                Float.parseFloat((String) shape.getValueAt(i, 0));
                length++;
            } catch (Exception e) {
                break;
            }
        }

        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        // prepare arrays...
        float d[] = new float[length];
        float a[] = new float[length];

        for (int i = 0; i < length; i++) {
            try {
                d[i] = (Float.parseFloat((String) shape.getValueAt(i, 0)));
                a[i] = (Float.parseFloat((String) shape.getValueAt(i, 1)));
            } catch (Exception e) {
            }
        }
        ls.operateEachChannel(new AOMultiReverb((int) delay.getData(), (float) gain.getData(), d, a, (float) dry.getData(), (float) wet.getData(), negFeedback.isSelected(), backwardConst.isSelected()));
    }

    private void onApplyConst() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        // reverb type ?
        switch (reverbType.getSelectedIndex()) {
        case 0: // comb
            ls.operateEachChannel(new AOReverb((int) delay.getData(), (float) gain.getData(), (float) dry.getData(), (float) wet.getData(), negFeedback.isSelected(), backwardConst.isSelected()));
            break;

        case 1: // allpass
            ls.operateEachChannel(new AOReverbAllPass((int) delay.getData(), (float) gain.getData(), (float) dry.getData(), (float) wet.getData(), backwardConst.isSelected()));
            break;

        case 2: // multi
            onApplyMulti();
            break;
        }
    }

    private void onApplyVar() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ALayerSelection ps = layerChooser.getSelectedLayer().createLayerSelection();
        AClipSelection cs = new AClipSelection(new AClip());
        cs.addLayerSelection(ls);
        cs.addLayerSelection(ps);
        cs.operateLayer0WithLayer1(new AOMultiReverb(0, 1, backwardVar.isSelected()));
    }

}
