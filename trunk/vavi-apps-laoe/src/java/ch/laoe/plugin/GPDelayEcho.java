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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipSelection;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AODelayEcho;
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
 * plugin to perform delay-echo effect.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 02.11.00 erster Entwurf oli4 <br>
 *          28.12.01 add multi-delay-echo (parameter-curve) oli4 <br>
 */
public class GPDelayEcho extends GPluginFrame {
    public GPDelayEcho(GPluginHandler ph) {
        super(ph);
        try {
            initGui();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getName() {
        return "delayEcho";
    }

    // GUI
    private JComboBox room;

    private UiControlText dry, wet;

    private UiControlText delay, gain;

    private JTable shape;

    private GClipLayerChooser layerChooser;

    private JButton apply1, apply2;

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

    private EventDispatcher eventDispatcher;

    private void initGui() {
        loadPresets();
        JTabbedPane tab = new JTabbedPane();

        // standard tab
        JPanel p1 = new JPanel();
        UiCartesianLayout l1 = new UiCartesianLayout(p1, 10, 11);
        l1.setPreferredCellSize(new Dimension(25, 35));
        p1.setLayout(l1);

        l1.add(new JLabel(GLanguage.translate("delayEchoType")), 0, 0, 4, 1);
        String roomItems[] = new String[presets.length];
        for (int i = 0; i < roomItems.length; i++) {
            roomItems[i] = presets[i].name;
        }
        room = new JComboBox(roomItems);
        l1.add(room, 4, 0, 6, 1);

        l1.add(new JLabel(GLanguage.translate("dryFactor")), 0, 1, 4, 1);
        dry = new GControlTextA(7, true, true);
        dry.setDataRange(-1, 1);
        dry.setData(1);
        l1.add(dry, 4, 1, 6, 1);

        l1.add(new JLabel(GLanguage.translate("wetFactor")), 0, 2, 4, 1);
        wet = new GControlTextA(7, true, true);
        wet.setDataRange(-1, 1);
        wet.setData(1);
        l1.add(wet, 4, 2, 6, 1);

        l1.add(new JLabel(GLanguage.translate("delay")), 0, 3, 4, 1);
        delay = new GControlTextX(getMain(), 7, true, true);
        delay.setDataRange(-1e9, 1e9);
        delay.setData(1100);
        l1.add(delay, 4, 3, 6, 1);

        l1.add(new JLabel(GLanguage.translate("gain")), 0, 4, 4, 1);
        gain = new GControlTextA(7, true, true);
        gain.setDataRange(-100, 100);
        gain.setData(.3);
        l1.add(gain, 4, 4, 6, 1);

        l1.add(new JLabel(GLanguage.translate("shape")), 0, 5, 4, 1);
        String columnNames[] = {
            GLanguage.translate("delay"), GLanguage.translate("gain")
        };
        shape = new JTable(new Object[maxRows][2], columnNames);
        JScrollPane scrollPane = new JScrollPane(shape);
        shape.setPreferredScrollableViewportSize(new Dimension(200, 70));
        l1.add(scrollPane, 0, 6, 10, 4);
        onRoomChange();

        apply1 = new JButton(GLanguage.translate("apply"));
        l1.add(apply1, 3, 10, 4, 1);

        tab.add(GLanguage.translate("standard"), p1);

        // curve tab
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 10, 11);
        l2.setPreferredCellSize(new Dimension(25, 35));
        p2.setLayout(l2);

        layerChooser = new GClipLayerChooser(getMain(), "room");
        l2.add(layerChooser, 0, 0, 10, 3);

        apply2 = new JButton(GLanguage.translate("apply"));
        l2.add(apply2, 3, 10, 4, 1);

        tab.add(GLanguage.translate("curve"), p2);

        frame.getContentPane().add(tab);
        pack();

        eventDispatcher = new EventDispatcher();
        apply1.addActionListener(eventDispatcher);
        apply2.addActionListener(eventDispatcher);
        room.addActionListener(eventDispatcher);
    }

    public void reload() {
        layerChooser.reload();
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == apply1) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply1] clicked");
                onApply1();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == apply2) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply2] clicked");
                onApply2();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == room) {
                onRoomChange();
            }

        }
    }

    // rows
    private static final int maxRows = 50;

    private void loadDelayEchoPoints(double d[], double a[]) {
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

    private void onRoomChange() {
        presetIndex = room.getSelectedIndex();
        dry.setData(presets[presetIndex].dry);
        wet.setData(presets[presetIndex].wet);
        delay.setData(presets[presetIndex].delay);
        gain.setData(presets[presetIndex].gain);
        loadDelayEchoPoints(presets[presetIndex].delayShape, presets[presetIndex].gainShape);
    }

    private void onApply1() {
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
        ls.operateEachChannel(new AODelayEcho(d, a, (float) delay.getData(), (float) gain.getData(), (float) dry.getData(), (float) wet.getData()));
    }

    private void onApply2() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ALayerSelection ps = layerChooser.getSelectedLayer().createLayerSelection();
        AClipSelection cs = new AClipSelection(new AClip());
        cs.addLayerSelection(ls);
        cs.addLayerSelection(ps);
        cs.operateLayer0WithLayer1(new AODelayEcho(0, 1));
    }

}
