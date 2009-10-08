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
import ch.laoe.operation.AOReverse;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;


/**
 * Class: GPReverse @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to time-reverse samples.
 * 
 * @version 30.10.00 erster Entwurf oli4 01.12.01 change classname from GPMirror to GPReverse oli4
 * 
 */
public class GPReverse extends GPluginFrame {
    public GPReverse(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "reverse";
    }

    // GUI
    private String mirrorTypeItem[] = {
        GLanguage.translate("mirrorRightSide"), GLanguage.translate("mirrorLeftSide"), GLanguage.translate("reverse")
    };

    private JComboBox mirrorType;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 2);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);
        cl.add(new JLabel(GLanguage.translate("mode") + ":"), 0, 0, 4, 1);
        mirrorType = new JComboBox(mirrorTypeItem);
        mirrorType.setSelectedIndex(2);
        cl.add(mirrorType, 4, 0, 6, 1);
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
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply] clicked");
                onApply();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                pluginHandler.getFocussedClipEditor().reload();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            }

            autoCloseFrame();
        }
    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        // crop-type ?
        switch (mirrorType.getSelectedIndex()) {
        case 0:
            ls.operateEachChannel(new AOReverse(AOReverse.MIRROR_RIGHT_SIDE));
            break;

        case 1:
            ls.operateEachChannel(new AOReverse(AOReverse.MIRROR_LEFT_SIDE));
            pluginHandler.getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
            break;

        case 2:
            ls.operateEachChannel(new AOReverse(AOReverse.REVERSE));
            break;
        }
    }

}
