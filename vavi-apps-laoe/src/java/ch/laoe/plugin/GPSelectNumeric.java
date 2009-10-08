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

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelSelection;
import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextSF;
import ch.laoe.ui.GLanguage;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * Class: GPSelectNumeric @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to select in numerical form.
 * 
 * @version 12.11.00 first draft oli4 17.11.00 create selection from measure-range oli4 22.06.01 remove
 * selection from measure-range oli4 16.06.02 use channelstack as channel-chooser oli4
 * 
 */
public class GPSelectNumeric extends GPluginFrame {
    public GPSelectNumeric(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "selectNumeric";
    }

    // GUI
    private JComboBox mode;

    private UiControlText offset, length;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 12, 4);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("selectMode")), 0, 0, 4, 1);
        String modeItem[] = {
            GLanguage.translate("free"), GLanguage.translate("loopPoints"), GLanguage.translate("measurePoints"), GLanguage.translate("begin"), GLanguage.translate("end")
        };
        mode = new JComboBox(modeItem);
        cl.add(mode, 4, 0, 8, 1);

        cl.add(new JLabel(GLanguage.translate("offset")), 0, 1, 4, 1);
        offset = new GControlTextSF(getMain(), 10, true, true);
        offset.setDataRange(-1e9, 1e9);
        offset.setData(0);
        cl.add(offset, 4, 1, 8, 1);
        cl.add(new JLabel(GLanguage.translate("length")), 0, 2, 4, 1);
        length = new GControlTextSF(getMain(), 10, true, true);
        length.setDataRange(-1e9, 1e9);
        length.setData(0);
        cl.add(length, 4, 2, 8, 1);
        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 4, 3, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
        mode.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == apply) {
                Debug.println(1, "plugin " + getName() + " [apply] clicked");
                onApply();
                updateHistory(GLanguage.translate(getName()));
                repaintFocussedClipEditor();
                autoCloseFrame();
            } else if (e.getSource() == mode) {
                Debug.println(1, "plugin " + getName() + " [mode] clicked");
                onModeChange();
            }

        }
    }

    public void reload() {
        super.reload();
    }

    private void onModeChange() {
        switch (mode.getSelectedIndex()) {
        case 0: // free
            offset.setEnabled(true);
            length.setEnabled(true);
            break;

        case 1: // loop points
            offset.setEnabled(false);
            length.setEnabled(false);
            break;

        case 2: // measure cursors
            offset.setEnabled(false);
            length.setEnabled(false);
            break;

        case 3: // begin
        case 4: // end
            offset.setEnabled(false);
            length.setEnabled(false);
            break;
        }
    }

    private void onApply() {
        AClip c = getFocussedClip();
        ALayer l = c.getSelectedLayer();
        AChannel ch = l.getSelectedChannel();

        switch (mode.getSelectedIndex()) {
        case 0: // free
            ch.setChannelSelection(new AChannelSelection(ch, (int) offset.getData(), (int) length.getData()));
            break;

        case 1: // loop points
            ch.setChannelSelection(new AChannelSelection(ch, c.getAudio().getLoopStartPointer(), c.getAudio().getLoopEndPointer() - c.getAudio().getLoopStartPointer()));
            break;

        case 2: // measure cursors
            ch.setChannelSelection(new AChannelSelection(ch, (int) GPMeasure.getLowerCursor(), (int) (GPMeasure.getHigherCursor() - GPMeasure.getLowerCursor())));
            break;

        case 3: // begin
            ch.setChannelSelection(new AChannelSelection(ch, 0, 1));
            break;

        case 4: // end
            ch.setChannelSelection(new AChannelSelection(ch, ch.getSampleLength() - 1, 1));
            break;
        }
    }

}
