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

import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOAutoVolume;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin auto volume.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 22.04.01 first draft oli4
 */
public class GPAutoVolume extends GPluginFrame {
    public GPAutoVolume(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "autoVolume";
    }

    // GUI
    private UiControlText attack, release;

    private JCheckBox backward;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 4);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("attack")), 0, 0, 4, 1);
        attack = new GControlTextX(getMain(), 7, true, true);
        attack.setDataRange(1, 1e9);
        attack.setData(1000);
        cl.add(attack, 4, 0, 6, 1);

        cl.add(new JLabel(GLanguage.translate("release")), 0, 1, 4, 1);
        release = new GControlTextX(getMain(), 7, true, true);
        release.setDataRange(0, 1e9);
        release.setData(1000);
        cl.add(release, 4, 1, 6, 1);

        backward = new JCheckBox(GLanguage.translate("backward"));
        cl.add(backward, 0, 2, 6, 1);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 3, 4, 1);

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
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            }
        }
    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        int sr = (int) getFocussedClip().getSampleRate();
        int a = (int) attack.getData();
        int r = (int) release.getData();
        boolean b = backward.isSelected();

        ls.operateEachChannel(new AOAutoVolume(sr, a, r, b));
    }

}
