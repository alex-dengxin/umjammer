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

import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOCanon;
import ch.laoe.operation.AOFade;
import ch.laoe.operation.AOLoopable;
import ch.laoe.operation.AOLoopableKeepSize;
import ch.laoe.operation.AOLoopableMultiplicate;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to build a loopable file
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 14.05.01 first draft oli4 <br>
 *          21.06.01 add loopcount oli4 <br>
 *          15.09.01 completely redefined oli4 <br>
 *          01.02.02 add multiplicate and keep-size oli4 <br>
 */
public class GPLoopable extends GPluginFrame {
    public GPLoopable(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "loopable";
    }

    // GUI
    private JComboBox order;

    private UiControlText voices, borderWidth, numberOfTimes;

    private JButton apply1, apply2, apply3, apply4;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JTabbedPane tab = new JTabbedPane();

        // crossfade tab
        JPanel p1 = new JPanel();
        UiCartesianLayout l1 = new UiCartesianLayout(p1, 10, 3);
        l1.setPreferredCellSize(new Dimension(25, 35));
        p1.setLayout(l1);

        l1.add(new JLabel(GLanguage.translate("order")), 0, 0, 4, 1);
        String orderItem[] = {
            GLanguage.translate("squareRoot"), GLanguage.translate("linear"), GLanguage.translate("square"), GLanguage.translate("cubic")
        };
        order = new JComboBox(orderItem);
        order.setSelectedIndex(2);
        l1.add(order, 4, 0, 6, 1);

        apply1 = new JButton(GLanguage.translate("apply"));
        l1.add(apply1, 3, 2, 4, 1);

        tab.add(GLanguage.translate("crossFade"), p1);

        // canon tab
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 10, 3);
        l2.setPreferredCellSize(new Dimension(25, 35));
        p2.setLayout(l2);

        l2.add(new JLabel(GLanguage.translate("voices")), 0, 0, 5, 1);
        voices = new UiControlText(10, true, true);
        voices.setDataRange(2, 5);
        voices.setData(2);
        l2.add(voices, 5, 0, 5, 1);

        apply2 = new JButton(GLanguage.translate("apply"));
        l2.add(apply2, 3, 2, 4, 1);

        tab.add(GLanguage.translate("canon"), p2);

        // multiplicate tab
        JPanel p3 = new JPanel();
        UiCartesianLayout l3 = new UiCartesianLayout(p3, 10, 3);
        l3.setPreferredCellSize(new Dimension(25, 35));
        p3.setLayout(l3);

        l3.add(new JLabel(GLanguage.translate("numberOfTimes")), 0, 0, 5, 1);
        numberOfTimes = new UiControlText(10, true, true);
        numberOfTimes.setDataRange(2, 100);
        numberOfTimes.setData(2);
        l3.add(numberOfTimes, 5, 0, 5, 1);

        apply3 = new JButton(GLanguage.translate("apply"));
        l3.add(apply3, 3, 2, 4, 1);

        tab.add(GLanguage.translate("multiplicate"), p3);

        // keep size
        JPanel p4 = new JPanel();
        UiCartesianLayout l4 = new UiCartesianLayout(p4, 10, 3);
        l4.setPreferredCellSize(new Dimension(25, 35));
        p4.setLayout(l4);

        l4.add(new JLabel(GLanguage.translate("borderWidth")), 0, 0, 4, 1);
        borderWidth = new GControlTextX(getMain(), 10, true, true);
        borderWidth.setDataRange(1, 100000);
        borderWidth.setData(1000);
        l4.add(borderWidth, 4, 0, 6, 1);

        apply4 = new JButton(GLanguage.translate("apply"));
        l4.add(apply4, 3, 2, 4, 1);

        tab.add(GLanguage.translate("keepSize"), p4);

        frame.getContentPane().add(tab);
        pack();

        eventDispatcher = new EventDispatcher();
        apply1.addActionListener(eventDispatcher);
        apply2.addActionListener(eventDispatcher);
        apply3.addActionListener(eventDispatcher);
        apply4.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            GProgressViewer.start(getName());
            GProgressViewer.entrySubProgress();
            GProgressViewer.setProgress(70);
            if (e.getSource() == apply1) {
                Debug.println(1, "plugin " + getName() + " [apply1] clicked");
                onApply1();
            } else if (e.getSource() == apply2) {
                Debug.println(1, "plugin " + getName() + " [apply2] clicked");
                onApply2();
            } else if (e.getSource() == apply3) {
                Debug.println(1, "plugin " + getName() + " [apply3] clicked");
                onApply3();
            } else if (e.getSource() == apply4) {
                Debug.println(1, "plugin " + getName() + " [apply4] clicked");
                onApply4();
            }
            GProgressViewer.setProgress(100);
            reloadFocussedClipEditor();
            updateHistory(GLanguage.translate(getName()));
            autoCloseFrame();
            GProgressViewer.exitSubProgress();
            GProgressViewer.finish();
        }
    }

    private void onApply1() {
        // order...
        int o = AOFade.SQUARE;
        switch (order.getSelectedIndex()) {
        case 0:
            o = AOFade.SQUARE_ROOT;
            break;

        case 1:
            o = AOFade.LINEAR;
            break;

        case 2:
            o = AOFade.SQUARE;
            break;

        case 3:
            o = AOFade.CUBIC;
            break;
        }
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOLoopable(o));
        getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
    }

    private void onApply2() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOCanon((int) voices.getData()));
        getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
    }

    private void onApply3() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOLoopableMultiplicate((int) numberOfTimes.getData()));
        // getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
    }

    private void onApply4() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOLoopableKeepSize((int) borderWidth.getData()));
        // getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
    }

}
