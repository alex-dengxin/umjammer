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
import ch.laoe.operation.AOFlipBytes;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;


/**
 * plugin to flip bytes.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 17.11.00 erster Entwurf oli4
 */
public class GPFlipBytes extends GPluginFrame {
    public GPFlipBytes(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "flipBytes";
    }

    // GUI
    private JComboBox newByte0, newByte1, newByte2, newByte3;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 6);
        cl.setPreferredCellSize(new Dimension(20, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("byteMapping")), 0, 0, 10, 1);
        String flipItems[] = {
            GLanguage.translate("byte0"), GLanguage.translate("byte1"), GLanguage.translate("byte2"), GLanguage.translate("byte3"), GLanguage.translate("empty")
        };
        newByte0 = new JComboBox(flipItems);
        newByte0.setSelectedIndex(0);
        newByte1 = new JComboBox(flipItems);
        newByte1.setSelectedIndex(1);
        newByte2 = new JComboBox(flipItems);
        newByte2.setSelectedIndex(2);
        newByte3 = new JComboBox(flipItems);
        newByte3.setSelectedIndex(3);
        cl.add(newByte0, 4, 1, 6, 1);
        cl.add(newByte1, 4, 2, 6, 1);
        cl.add(newByte2, 4, 3, 6, 1);
        cl.add(newByte3, 4, 4, 6, 1);
        cl.add(new JLabel(GLanguage.translate("byte0") + ":"), 0, 1, 4, 1);
        cl.add(new JLabel(GLanguage.translate("byte1") + ":"), 0, 2, 4, 1);
        cl.add(new JLabel(GLanguage.translate("byte2") + ":"), 0, 3, 4, 1);
        cl.add(new JLabel(GLanguage.translate("byte3") + ":"), 0, 4, 4, 1);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 2, 5, 6, 1);

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
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.finish();
            }
        }
    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOFlipBytes(newByte0.getSelectedIndex(), newByte1.getSelectedIndex(), newByte2.getSelectedIndex(), newByte3.getSelectedIndex()));
    }

}
