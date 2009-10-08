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
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOInsert;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to insert silence.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 04.11.00 erster Entwurf oli4
 */
public class GPInsertSilence extends GPluginFrame {
    public GPInsertSilence(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "insertSilence";
    }

    // GUI
    private UiControlText insertionLength;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 2);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("sampleLength")), 0, 0, 4, 1);
        insertionLength = new GControlTextX(getMain(), 8, true, true);
        insertionLength.setDataRange(0, 1e9);
        insertionLength.setData(1);
        cl.add(insertionLength, 4, 0, 6, 1);
        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 1, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == apply) {
                GProgressViewer.start(getName());
                Debug.println(1, "plugin " + getName() + " [apply] clicked");
                onApply();
                GProgressViewer.finish();
            }

            autoCloseFrame();
        }
    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        // insert
        ls.operateEachChannel(new AOInsert((int) insertionLength.getData()));

        // reload clip
        updateHistory(GLanguage.translate(getName()));
        // autoScaleFocussedClip();
        reloadFocussedClipEditor();
        pluginHandler.getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
    }

}
