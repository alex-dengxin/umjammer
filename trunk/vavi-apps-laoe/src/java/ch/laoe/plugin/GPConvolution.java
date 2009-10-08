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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipSelection;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOConvolution;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChooser;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;


/**
 * plugin to perform convolution.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 24.06.01 first draft oli4 <br>
 *          17.09.01 layer-kernel added oli4
 */
public class GPConvolution extends GPluginFrame {
    public GPConvolution(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "convolution";
    }

    // GUI
    private JTable kernel;

    private GClipLayerChooser layerChooser;

    private JButton apply1, apply2;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        // GUI
        JTabbedPane tab = new JTabbedPane();

        // const tab
        JPanel p1 = new JPanel();
        UiCartesianLayout l1 = new UiCartesianLayout(p1, 10, 5);
        l1.setPreferredCellSize(new Dimension(25, 35));
        p1.setLayout(l1);

        String columnNames[] = {
            GLanguage.translate("kernel"), GLanguage.translate("value")
        };
        kernel = new JTable(new Object[maxRows][2], columnNames);
        for (int i = 0; i < maxRows; i++) {
            kernel.setValueAt(String.valueOf(i), i, 0);
            kernel.setValueAt(String.valueOf(0), i, 1);
        }
        JScrollPane scrollPane = new JScrollPane(kernel);
        kernel.setPreferredScrollableViewportSize(new Dimension(200, 70));
        l1.add(scrollPane, 0, 0, 10, 4);

        apply1 = new JButton(GLanguage.translate("apply"));
        l1.add(apply1, 3, 4, 4, 1);

        tab.add(GLanguage.translate("list"), p1);

        // const tab
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 10, 5);
        l2.setPreferredCellSize(new Dimension(25, 35));
        p2.setLayout(l2);

        layerChooser = new GClipLayerChooser(getMain(), "kernelLayer");
        l2.add(layerChooser, 0, 0, 10, 3);

        apply2 = new JButton(GLanguage.translate("apply"));
        l2.add(apply2, 3, 4, 4, 1);
        tab.add(GLanguage.translate("layer"), p2);

        frame.getContentPane().add(tab);
        pack();

        eventDispatcher = new EventDispatcher();
        apply1.addActionListener(eventDispatcher);
        apply2.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            GProgressViewer.start(getName());
            if (e.getSource() == apply1) {
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply1] clicked");
                onApply1();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
            } else if (e.getSource() == apply2) {
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply2] clicked");
                onApply2();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
            }
            GProgressViewer.finish();
        }
    }

    public void reload() {
        layerChooser.reload();
    }

    // rows
    private static final int maxRows = 50;

    private void onApply1() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        float k[] = new float[kernel.getRowCount()];

        // prepare arrays...
        for (int i = 0; i < kernel.getRowCount(); i++) {
            try {
                k[i] = Float.parseFloat((String) kernel.getValueAt(i, 1));
            } catch (Exception e) {
            }
        }
        ls.operateEachChannel(new AOConvolution(k));
    }

    private void onApply2() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ALayerSelection ps = layerChooser.getSelectedLayer().createLayerSelection();
        AClipSelection cs = new AClipSelection(new AClip());
        cs.addLayerSelection(ls);
        cs.addLayerSelection(ps);
        cs.operateLayer0WithLayer1(new AOConvolution());
    }

}
