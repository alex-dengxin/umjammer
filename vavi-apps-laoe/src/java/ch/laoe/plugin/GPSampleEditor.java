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
import javax.swing.JScrollPane;
import javax.swing.JTable;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelPlotter;
import ch.laoe.clip.AChannelSelection;
import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlEvent;
import ch.oli4.ui.UiControlListener;
import ch.oli4.ui.UiControlText;


/***
 * plugin to edit sigle sample in numerical form.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @verision 17.11.00 erster Entwurf oli4
 *           30.11.01 add offset-settings oli4
 *           16.06.02 use channelstack as channel-chooser oli4
 */
public class GPSampleEditor extends GPluginFrame {
    public GPSampleEditor(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "sampleEditor";
    }

    // GUI
    private JComboBox offsetSetting;

    private UiControlText sampleOffset;

    private JTable sampleTable;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 10);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        String settingItem[] = {
            GLanguage.translate("begin"), GLanguage.translate("end"), GLanguage.translate("zoomBegin"), GLanguage.translate("loopBegin"), GLanguage.translate("measureBegin"), GLanguage.translate("selectionBegin")
        };
        offsetSetting = new JComboBox(settingItem);
        offsetSetting.setSelectedIndex(0);
        cl.add(new JLabel(GLanguage.translate("offsetSetting")), 0, 0, 4, 1);
        cl.add(offsetSetting, 4, 0, 6, 1);

        sampleOffset = new GControlTextX(getMain(), 9, true, true);
        sampleOffset.setDataRange(0, 1e9);
        sampleOffset.setData(0);
        cl.add(new JLabel(GLanguage.translate("sampleOffset")), 0, 1, 4, 1);
        cl.add(sampleOffset, 4, 1, 6, 1);
        // cl.add(new JLabel(GLanguage.translate("samples")+":"), 0, 0, 5, 1);

        String columnNames[] = {
            GLanguage.translate("sample") + " [1]", GLanguage.translate("value") + " [1]"
        };
        sampleTable = new JTable(new Object[maxRows][2], columnNames);
        JScrollPane scrollPane = new JScrollPane(sampleTable);
        sampleTable.setPreferredScrollableViewportSize(new Dimension(200, 70));
        cl.add(scrollPane, 0, 2, 10, 7);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 9, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
        offsetSetting.addActionListener(eventDispatcher);
        sampleOffset.addControlListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener, UiControlListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == apply) {
                GProgressViewer.start(getName());
                Debug.println(1, "plugin " + getName() + " [apply] clicked");
                onApply();
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.finish();
            } else if (e.getSource() == offsetSetting) {
                Debug.println(1, "plugin " + getName() + " [offset setting] clicked");
                AClip c = getFocussedClip();
                ALayer l = c.getSelectedLayer();
                AChannel ch = l.getSelectedChannel();
                AChannelPlotter p = ch.getChannelPlotter();
                AChannelSelection s = ch.getChannelSelection();

                switch (offsetSetting.getSelectedIndex()) {
                case 0: // begin
                    sampleOffset.setData(0);
                    break;

                case 1: // end
                    sampleOffset.setData(ch.getSampleLength() - 1 - maxRows);
                    break;

                case 2: // zoom begin
                    sampleOffset.setData((int) p.getXOffset());
                    break;

                case 3: // loop begin
                    sampleOffset.setData(c.getAudio().getLoopStartPointer());
                    break;

                case 4: // measure begin
                    sampleOffset.setData((int) GPMeasure.getLowerCursor());
                    break;

                case 5: // selection begin
                    sampleOffset.setData(s.getOffset());
                    break;
                }
                sampleToTable();
            }
        }

        public void onDataChanging(UiControlEvent e) {
        }

        public void onDataChanged(UiControlEvent e) {
            Debug.println(1, "plugin " + getName() + " [sample offset] changed");
            sampleToTable();
        }

        public void onValidate(UiControlEvent e) {
            Debug.println(1, "plugin " + getName() + " [sample offset] changed");
            sampleToTable();
        }
    }

    // rows
    private static final int maxRows = 20;

    private void onApply() {
        tableToSample();
    }

    public void reload() {
        super.reload();
        sampleToTable();
    }

    private void sampleToTable() {
        AClip c = getFocussedClip();
        ALayer l = c.getSelectedLayer();
        AChannel ch = l.getSelectedChannel();

        // index range check...
        int index = (int) sampleOffset.getData();
        if (index < 0) {
            index = 0;
        } else if (index > ch.getSampleLength() - 1 - maxRows) {
            index = ch.getSampleLength() - 1 - maxRows;
        }
        sampleOffset.setData(index);

        // write samples to selection
        for (int j = 0; j < maxRows; j++) {
            sampleTable.setValueAt(String.valueOf(index + j), j, 0);
            sampleTable.setValueAt(String.valueOf(ch.sample[index + j]), j, 1);
        }
    }

    private void tableToSample() {
        AClip c = getFocussedClip();
        ALayer l = c.getSelectedLayer();
        AChannel ch = l.getSelectedChannel();

        // write samples to selection
        for (int j = 0; j < maxRows; j++) {
            ch.sample[(int) Double.parseDouble((String) sampleTable.getValueAt(j, 0))] = (float) Double.parseDouble((String) sampleTable.getValueAt(j, 1));
        }
    }

}
