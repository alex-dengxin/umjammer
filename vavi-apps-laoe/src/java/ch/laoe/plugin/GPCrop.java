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

import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOAutoCropSilence;
import ch.laoe.operation.AOCropSelection;
import ch.laoe.operation.AOCropSilence;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GControlTextY;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to different crop modes
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 28.10.00 erster Entwurf oli4

 */
public class GPCrop extends GPluginFrame {
    public GPCrop(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "crop";
    }

    // GUI
    private String cropTypeItem[] = {
        GLanguage.translate("borderSilence"), GLanguage.translate("everySilence"), GLanguage.translate("selection")
    };

    private JComboBox cropType;

    private UiControlText silenceLimit;

    private UiControlText silenceTMin;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 4);
        cl.setPreferredCellSize(new Dimension(30, 35));
        p.setLayout(cl);
        cl.add(new JLabel(GLanguage.translate("mode")), 0, 0, 4, 1);
        cropType = new JComboBox(cropTypeItem);
        cl.add(cropType, 4, 0, 6, 1);
        cl.add(new JLabel(GLanguage.translate("silenceThreshold")), 0, 1, 4, 1);
        silenceLimit = new GControlTextY(getMain(), 6, true, true);
        silenceLimit.setDataRange(0, 1000000);
        silenceLimit.setData(33);
        cl.add(silenceLimit, 4, 1, 6, 1);
        cl.add(new JLabel(GLanguage.translate("silenceMinTime")), 0, 2, 4, 1);
        silenceTMin = new GControlTextX(getMain(), 6, true, true);
        silenceTMin.setDataRange(0, 1000000);
        silenceTMin.setData(50);
        cl.add(silenceTMin, 4, 2, 6, 1);
        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 3, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
        cropType.addActionListener(eventDispatcher);
    }

    public void start() {
        super.start();
        updateActiveComponents();
    }

    private void updateActiveComponents() {
        switch (cropType.getSelectedIndex()) {
        case 0:
            silenceLimit.setEnabled(true);
            silenceTMin.setEnabled(false);
            break;

        case 1:
            silenceLimit.setEnabled(true);
            silenceTMin.setEnabled(true);
            break;

        case 2:
            silenceLimit.setEnabled(false);
            silenceTMin.setEnabled(false);
            break;
        }
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == cropType) {
                Debug.println(1, "plugin " + getName() + " [cropType] clicked");
                updateActiveComponents();
            } else if (e.getSource() == apply) {
                GProgressViewer.start(getName());
                Debug.println(1, "plugin " + getName() + " [apply] clicked");
                onApply();
                reloadFocussedClipEditor();
                GProgressViewer.finish();
            }
        }
    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        // crop-type ?
        switch (cropType.getSelectedIndex()) {
        case 0:
            ls.operateEachChannel(new AOCropSilence((float) silenceLimit.getData()));
            break;

        case 1:
            ls.operateEachChannel(new AOAutoCropSilence((float) silenceLimit.getData(), (int) (silenceTMin.getData())));
            break;

        case 2:
            ls.operateEachChannel(new AOCropSelection());
            break;
        }

        // reload clip
        updateHistory(GLanguage.translate(getName()));
        pluginHandler.getFocussedClipEditor().reload();
        pluginHandler.getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
        autoCloseFrame();
    }

}
