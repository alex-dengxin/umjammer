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
import ch.laoe.operation.AOMultiNotch;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;
import ch.oli4.ui.UiPersistance;


/**
 * plugin to perform multi-notch filter.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 09.06.01 first draft oli4
 */
public class GPMultiNotch extends GPluginFrame {
    public GPMultiNotch(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "multiNotch";
    }

    // GUI
    private JComboBox presets;

    private UiControlText q;

    private JTable frequency;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    // presets
    private String presetsName[];

    private float qPresets[];

    private float frequencyPresets[][];

    private void initGui() {
        // presets
        UiPersistance persist = new UiPersistance(getName() + ".properties");
        persist.restore();
        int n = persist.getInt("numberOfPresets");
        int m = persist.getInt("numberOfFrequencies");
        presetsName = new String[n];
        qPresets = new float[n];
        frequencyPresets = new float[n][];

        for (int i = 0; i < n; i++) {
            presetsName[i] = GLanguage.translate(persist.getString("name_" + i));
            qPresets[i] = persist.getFloat("q_" + i);
            frequencyPresets[i] = new float[m];
            for (int j = 0; j < m; j++) {
                frequencyPresets[i][j] = persist.getFloat("frequency_" + i + "_" + j);
            }
        }

        // GUI
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 8);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("presets")), 0, 0, 4, 1);
        presets = new JComboBox(presetsName);
        cl.add(presets, 4, 0, 5, 1);

        cl.add(new JLabel(GLanguage.translate("quality")), 0, 1, 4, 1);
        q = new GControlTextA(7, true, true);
        q.setDataRange(1, 100);
        q.setData(1);
        cl.add(q, 4, 1, 6, 1);

        String columnNames[] = {
            GLanguage.translate("frequency") + "[Hz]"
        };
        frequency = new JTable(new Object[maxRows][1], columnNames);
        JScrollPane scrollPane = new JScrollPane(frequency);
        frequency.setPreferredScrollableViewportSize(new Dimension(200, 70));
        cl.add(scrollPane, 0, 2, 10, 5);
        onPresets();

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 7, 4, 1);

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
                onPresets();
            }

        }
    }

    // rows
    private static final int maxRows = 20;

    private void loadFrequencies(float f[]) {
        for (int i = 0; i < f.length; i++) {
            frequency.setValueAt(String.valueOf(f[i]), i, 0);
        }
    }

    private void onPresets() {
        int i = presets.getSelectedIndex();
        q.setData(qPresets[i]);
        loadFrequencies(frequencyPresets[i]);

    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        float f[] = new float[frequency.getRowCount()];

        // prepare arrays...
        for (int i = 0; i < frequency.getRowCount(); i++) {
            try {
                f[i] = Float.parseFloat((String) frequency.getValueAt(i, 0)) / getFocussedClip().getSampleRate();
            } catch (Exception e) {
            }
        }
        ls.operateEachChannel(new AOMultiNotch(f, (float) q.getData()));
    }

}
