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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOHarmonicsGenerator;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextF;
import ch.laoe.ui.GControlTextY;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin create sinus-harmonics.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 30.05.01 first draft oli4
 */
public class GPHarmonicsGenerator extends GPluginFrame {
    public GPHarmonicsGenerator(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "harmonicsGenerator";
    }

    // GUI
    private UiControlText offset, basePeriod;

    private JCheckBox add;

    private JTable amplitudes;

    private JButton reset, apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 10);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("offset")), 0, 0, 4, 1);
        offset = new GControlTextY(getMain(), 9, true, true);
        offset.setDataRange(-1e9, 1e9);
        offset.setData(0);
        cl.add(offset, 4, 0, 6, 1);

        cl.add(new JLabel(GLanguage.translate("basePeriod")), 0, 1, 4, 1);
        basePeriod = new GControlTextF(getMain(), 9, true, true);
        basePeriod.setDataRange(0, 1e6);
        basePeriod.setData(1000);
        cl.add(basePeriod, 4, 1, 6, 1);

        add = new JCheckBox(GLanguage.translate("add"));
        cl.add(add, 5, 2, 5, 1);

        String columnNames[] = {
            GLanguage.translate("harmonic"), GLanguage.translate("amplitude") + " [1]"
        };
        amplitudes = new JTable(new Object[maxRows][2], columnNames);
        for (int i = 0; i < maxRows; i++) {
            amplitudes.setValueAt(Integer.toString(i + 1), i, 0);
        }
        JScrollPane scrollPane = new JScrollPane(amplitudes);
        amplitudes.setPreferredScrollableViewportSize(new Dimension(200, 70));
        cl.add(scrollPane, 0, 3, 10, 6);

        reset = new JButton(GLanguage.translate("reset"));
        cl.add(reset, 1, 9, 4, 1);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 5, 9, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
        reset.addActionListener(eventDispatcher);
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
            }
            if (e.getSource() == reset) {
                Debug.println(1, "plugin " + getName() + " [reset] clicked");
                onReset();
            }
        }
    }

    // rows
    private static final int maxRows = 30;

    private void onApply() {
        float a[] = new float[maxRows];
        for (int i = 0; i < a.length; i++) {
            String s = (String) amplitudes.getValueAt(i, 1);
            if (s != null) {
                if (s.length() > 0) {
                    a[i] = (float) Double.parseDouble(s);
                }
            }
        }

        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOHarmonicsGenerator(a, (float) offset.getData(), (int) basePeriod.getData(), add.isSelected()));
    }

    private void onReset() {
        for (int i = 0; i < maxRows; i++) {
            amplitudes.setValueAt(null, i, 1);
        }
    }

}
