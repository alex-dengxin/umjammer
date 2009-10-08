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
import javax.swing.JTabbedPane;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelSelection;
import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;
import ch.laoe.operation.AOToolkit;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextSF;
import ch.laoe.ui.GControlTextY;
import ch.laoe.ui.GLanguage;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * Class: GPSelectExpander @author olivier gäumann, neuchâtel (switzerland) JDK: 1.4
 * 
 * plugin to expand or snap a selection.
 * 
 * @version 16.12.01 first draft oli4 <br>
 *          27.01.02 expand to all channels oli4 <br>
 *          16.06.02 use channelstack as channel-chooser oli4 <br>
 *          17.06.02 expand to all channel removed since copy/paste selections is now channel- wise oli4 <br>
 */
public class GPSelectExpander extends GPluginFrame {
    public GPSelectExpander(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "selectExpander";
    }

    // GUI
    private JComboBox mode;

    private UiControlText silenceThreshold, minimumWidth;

    private JButton apply1, apply2;

    private EventDispatcher eventDispatcher;

    private void initGui() {
//      JPanel p = new JPanel();
        JTabbedPane tab = new JTabbedPane();

        // x axis
        JPanel p1 = new JPanel();
        UiCartesianLayout l1 = new UiCartesianLayout(p1, 10, 4);
        l1.setPreferredCellSize(new Dimension(25, 35));
        p1.setLayout(l1);

        l1.add(new JLabel(GLanguage.translate("mode")), 0, 0, 5, 1);
        String modeItem[] = {
            GLanguage.translate("expandSilence"), GLanguage.translate("expandNoise"), GLanguage.translate("reduceSilence"), GLanguage.translate("reduceNoise")
        };
        mode = new JComboBox(modeItem);
        l1.add(mode, 5, 0, 5, 1);

        l1.add(new JLabel(GLanguage.translate("silenceThreshold")), 0, 1, 5, 1);
        silenceThreshold = new GControlTextY(getMain(), 10, true, true);
        silenceThreshold.setDataRange(0, 1e9);
        silenceThreshold.setData(33);
        l1.add(silenceThreshold, 5, 1, 5, 1);

        l1.add(new JLabel(GLanguage.translate("minimumWidth")), 0, 2, 5, 1);
        minimumWidth = new GControlTextSF(getMain(), 10, true, true);
        minimumWidth.setDataRange(0, 1e9);
        minimumWidth.setData(2);
        l1.add(minimumWidth, 5, 2, 5, 1);

        apply1 = new JButton(GLanguage.translate("apply"));
        l1.add(apply1, 3, 3, 4, 1);

        tab.add(p1, GLanguage.translate("xAxis"));

        // channel axis
        /*
         * JPanel p2 = new JPanel(); GCartesianLayout l2 = new GCartesianLayout(p2, 10, 4); l2.setPreferredCellSize(new
         * Dimension(25, 35)); p2.setLayout(l2);
         * 
         * apply2 = new JButton(GLanguage.translate("apply")); l2.add(apply2, 3, 3, 4, 1);
         * 
         * tab.add(p2, GLanguage.translate("channelAxis"));
         */
        frame.getContentPane().add(tab);
        pack();

        eventDispatcher = new EventDispatcher();
        apply1.addActionListener(eventDispatcher);
        // apply2.addActionListener(eventDispatcher);
        mode.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == apply1) {
                Debug.println(1, "plugin " + getName() + " [apply1] clicked");
                onApply1();
                updateHistory(GLanguage.translate(getName()));
                repaintFocussedClipEditor();
                autoCloseFrame();
            }
            /*
             * else if (e.getSource() == apply2) { Debug.println(1, "plugin "+getName()+" [apply2] clicked"); onApply2();
             * updateHistory(GLanguage.translate(getName())); repaintFocussedClipEditor(); autoCloseFrame(); }
             */
            else if (e.getSource() == mode) {
                Debug.println(1, "plugin " + getName() + " [mode] clicked");
                onModeChange();
            }

        }
    }

    public void reload() {
        super.reload();
    }

    private void onModeChange() {
    }

    private void onApply1() {
        AClip c = getFocussedClip();
        ALayer l = c.getSelectedLayer();
        AChannel ch = l.getSelectedChannel();
        AChannelSelection chs = ch.getChannelSelection();

        switch (mode.getSelectedIndex()) {
        case 0: // expand silence
            ch.setChannelSelection(AOToolkit.expandSilence(chs, (float) silenceThreshold.getData(), (int) minimumWidth.getData()));
            break;

        case 1: // expand noise
            ch.setChannelSelection(AOToolkit.expandNoise(chs, (float) silenceThreshold.getData(), (int) minimumWidth.getData()));
            break;

        case 2: // reduce silence
            ch.setChannelSelection(AOToolkit.reduceSilence(chs, (float) silenceThreshold.getData(), (int) minimumWidth.getData()));
            break;

        case 3: // reduce noise
            ch.setChannelSelection(AOToolkit.reduceNoise(chs, (float) silenceThreshold.getData(), (int) minimumWidth.getData()));
            break;
        }
    }

    /*
     * private void onApply2 () { //expand selection to all channels AClip c = getFocussedClip(); ALayer l = c.getSelectedLayer();
     * AChannel ch = l.getSelectedChannel(); AChannelSelection chs = ch.getChannelSelection();
     * 
     * for (int i=0; i<l.getNumberOfChannels(); i++) { AChannelSelection chs2 = new AChannelSelection(chs);
     * l.getChannel(i).setChannelSelection(chs2); chs2.setChannel(l.getChannel(i)); } }
     */

}
