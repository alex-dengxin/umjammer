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
import javax.swing.JTable;

import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOMultiPitch;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GComboBoxPowerOf2;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;
import ch.oli4.ui.UiPersistance;


/**
 * plugin to perform multi-pitch effect.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 19.06.01 first draft oli4 <br>
 *          23.06.01 add variable FFT-length oli4 <br>
 */
public class GPMultiPitch extends GPluginFrame {
    public GPMultiPitch(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "multiPitch";
    }

    // GUI
    private JComboBox presets;

    private GComboBoxPowerOf2 fftLength;

    private UiControlText dry, wet;

    private JTable pitchPoints;

    private JButton apply;

    private String presetsName[];

    private float dryPresets[];

    private float wetPresets[];

    private double pitchPresets[][];

    private double amplitudePresets[][];

    private EventDispatcher eventDispatcher;

    private void initGui() {
        // presets
        UiPersistance persist = new UiPersistance(getName() + ".properties");
        persist.restore();
        int n = persist.getInt("numberOfPresets");
        int m = persist.getInt("numberOfPitches");
        presetsName = new String[n];
        dryPresets = new float[n];
        wetPresets = new float[n];
        pitchPresets = new double[n][];
        amplitudePresets = new double[n][];

        for (int i = 0; i < n; i++) {
            presetsName[i] = GLanguage.translate(persist.getString("name_" + i));
            dryPresets[i] = persist.getFloat("dry_" + i);
            wetPresets[i] = persist.getFloat("wet_" + i);
            pitchPresets[i] = new double[m];
            for (int j = 0; j < m; j++) {
                pitchPresets[i][j] = persist.getDouble("pitch_" + i + "_" + j);
            }
            amplitudePresets[i] = new double[m];
            for (int j = 0; j < m; j++) {
                amplitudePresets[i][j] = persist.getDouble("amplitude_" + i + "_" + j);
            }
        }

        // GUI
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 9);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("presets")), 0, 0, 4, 1);
        presets = new JComboBox(presetsName);
        cl.add(presets, 4, 0, 5, 1);

        cl.add(new JLabel(GLanguage.translate("fftLength")), 0, 1, 4, 1);
        fftLength = new GComboBoxPowerOf2(7, 16);
        fftLength.setSelectedExponent(14);
        cl.add(fftLength, 4, 1, 6, 1);

        cl.add(new JLabel(GLanguage.translate("dryFactor")), 0, 2, 4, 1);
        dry = new GControlTextA(7, true, true);
        dry.setDataRange(-1, 1);
        dry.setData(1);
        cl.add(dry, 4, 2, 6, 1);

        cl.add(new JLabel(GLanguage.translate("wetFactor")), 0, 3, 4, 1);
        wet = new GControlTextA(7, true, true);
        wet.setDataRange(-1, 1);
        wet.setData(1);
        cl.add(wet, 4, 3, 6, 1);

        String columnNames[] = {
            GLanguage.translate("pitch") + " [1]", GLanguage.translate("amplitude") + " [1]"
        };
        pitchPoints = new JTable(new Object[maxRows][2], columnNames);
        JScrollPane scrollPane = new JScrollPane(pitchPoints);
        pitchPoints.setPreferredScrollableViewportSize(new Dimension(200, 70));
        cl.add(scrollPane, 0, 4, 10, 4);
        onPresetChange();

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 8, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
        presets.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == apply) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply] clicked");
                onApply();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();

                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == presets) {
                Debug.println(1, "plugin " + getName() + " [presets] clicked");
                onPresetChange();
            }
        }
    }

    // rows
    private static final int maxRows = 20;

    private void loadPoints(double p[], double a[]) {
        for (int i = 0; i < p.length; i++) {
            pitchPoints.setValueAt(String.valueOf(p[i]), i, 0);
            pitchPoints.setValueAt(String.valueOf(a[i]), i, 1);
        }
    }

    private void onPresetChange() {
        int i = presets.getSelectedIndex();
        loadPoints(pitchPresets[i], amplitudePresets[i]);
        dry.setData(dryPresets[i]);
        wet.setData(wetPresets[i]);
    }

    private int getFftLength() {
        return fftLength.getSelectedValue();
    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        float p[] = new float[pitchPoints.getRowCount()];
        float a[] = new float[pitchPoints.getRowCount()];

        // prepare arrays...
        for (int i = 0; i < pitchPoints.getRowCount(); i++) {
            try {
                p[i] = Float.parseFloat((String) pitchPoints.getValueAt(i, 0));
                a[i] = Float.parseFloat((String) pitchPoints.getValueAt(i, 1));
            } catch (Exception e) {
            }
        }
        ls.operateEachChannel(new AOMultiPitch(p, a, (float) dry.getData(), (float) wet.getData(), getFftLength()));
    }

}
