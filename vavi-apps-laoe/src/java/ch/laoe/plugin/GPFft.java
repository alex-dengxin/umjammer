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

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipSelection;
import ch.laoe.clip.ALayer;
import ch.laoe.operation.AOFft;
import ch.laoe.operation.AOIfft;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChooser;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;


/**
 * @autor: olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @target JDK 1.3plugin to perform total FFT/IFFT.
 * 
 * @version 02.08.01 first draft oli4
 */
public class GPFft extends GPluginFrame {
    public GPFft(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "fft";
    }

    // GUI
    private JComboBox operation;

    private GClipLayerChooser phaseLayer;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 6);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("operation")), 0, 0, 5, 1);
        String operationItems[] = {
            GLanguage.translate("fft"), GLanguage.translate("ifft")
        };
        operation = new JComboBox(operationItems);
        operation.setSelectedIndex(0);
        cl.add(operation, 5, 0, 5, 1);

        phaseLayer = new GClipLayerChooser(getMain(), "phaseLayer");
        cl.add(phaseLayer, 0, 2, 10, 3);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 5, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
        operation.addActionListener(eventDispatcher);

        updateComponents();
    }

    public void reload() {
        phaseLayer.reload();
    }

    private void updateComponents() {
        switch (operation.getSelectedIndex()) {
        case 0: // FFT
            phaseLayer.setEnabled(false);
            break;

        case 1: // IFFT
            phaseLayer.setEnabled(true);
            break;
        }
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
                updateHistory(GLanguage.translate(getName() + " " + (String) operation.getSelectedItem()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == operation) {
                Debug.println(1, "plugin " + getName() + " [operation] clicked");
                updateComponents();
            }
        }
    }

    private void onApply() {
//      ALayerSelection l1 = getFocussedClip().getSelectedLayer().getLayerSelection();
        switch (operation.getSelectedIndex()) {
        case 0: // FFT
        {
            ALayer mag = getFocussedClip().getSelectedLayer();
            ALayer phas = new ALayer(mag.getNumberOfChannels(), mag.getMaxSampleLength());
            phas.setName(GLanguage.translate("phaseOf") + " " + mag.getName());
            phas.setType(ALayer.PARAMETER_LAYER);

            getFocussedClip().add(phas);

            AClipSelection cs = new AClipSelection(new AClip());
            cs.addLayerSelection(mag.createLayerSelection());
            cs.addLayerSelection(phas.createLayerSelection());

            cs.operateLayer0WithLayer1(new AOFft());
        }
            break;

        case 1: // IFFT
        {
            ALayer mag = getFocussedClip().getSelectedLayer();
            ALayer phas = phaseLayer.getSelectedLayer();
            AClipSelection cs = new AClipSelection(new AClip());
            cs.addLayerSelection(mag.createLayerSelection());
            cs.addLayerSelection(phas.createLayerSelection());

            cs.operateLayer0WithLayer1(new AOIfft());
            // getFocussedClip().remove(phas);
        }
            break;
        }
    }

}
