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

import ch.laoe.clip.ALayer;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOPan;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChannelChooser;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * variable pan plugin
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 03.06.01 first draft oli4 <br>
 *          17.12.01 add different modes, order and constant pan oli4
 */
public class GPPan extends GPluginFrame {
    public GPPan(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "pan";
    }

    // GUI
    private GClipLayerChannelChooser panChannel;

    private JTabbedPane tab;

    private JComboBox mode, shape;

    private UiControlText pan;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout l = new UiCartesianLayout(p, 10, 8);
        l.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(l);

        // const tab
        tab = new JTabbedPane();
        JPanel p1 = new JPanel();
        UiCartesianLayout l1 = new UiCartesianLayout(p1, 10, 4);
        l1.setPreferredCellSize(new Dimension(25, 35));
        p1.setLayout(l1);

        l1.add(new JLabel(GLanguage.translate("pan")), 0, 1, 5, 1);
        pan = new GControlTextA(7, true, true);
        pan.setDataRange(1, 2);
        pan.setData(1.5);
        pan.setEditable(true);
        l1.add(pan, 5, 1, 5, 1);

        tab.add(p1, GLanguage.translate("constant"));

        // variable tab
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 10, 4);
        l2.setPreferredCellSize(new Dimension(25, 35));
        p2.setLayout(l2);

        panChannel = new GClipLayerChannelChooser(getMain(), "panCurve");
        l2.add(panChannel, 0, 0, 10, 4);

        tab.add(p2, GLanguage.translate("f(time)"));
        l.add(tab, 0, 0, 10, 5);

        // common stuff...
        l.add(new JLabel(GLanguage.translate("mode")), 0, 5, 5, 1);
        String modeItem[] = {
            GLanguage.translate("half"), GLanguage.translate("full"), GLanguage.translate("mixEnds")
        };
        mode = new JComboBox(modeItem);
        mode.setSelectedIndex(0);
        l.add(mode, 5, 5, 5, 1);

        l.add(new JLabel(GLanguage.translate("shape")), 0, 6, 5, 1);
        String shapeItem[] = {
            GLanguage.translate("squareRoot"), GLanguage.translate("linear"), GLanguage.translate("square")
        };
        shape = new JComboBox(shapeItem);
        shape.setSelectedIndex(1);
        l.add(shape, 5, 6, 5, 1);

        apply = new JButton(GLanguage.translate("apply"));
        l.add(apply, 3, 7, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            GProgressViewer.start(getName());
            GProgressViewer.entrySubProgress();
            GProgressViewer.setProgress(70);
            if (e.getSource() == apply) {
                Debug.println(1, "plugin " + getName() + " [apply] clicked");
                onApply();
            }
            GProgressViewer.setProgress(100);
            updateHistory(GLanguage.translate(getName()));
            reloadFocussedClipEditor();
            autoCloseFrame();
            GProgressViewer.exitSubProgress();
            GProgressViewer.finish();
        }
    }

    private void onApply() {
        int m, sh;

        // mode...
        switch (mode.getSelectedIndex()) {
        case 1:
            m = AOPan.FULL_MODE;
            break;

        case 2:
            m = AOPan.MIX_ENDS_MODE;
            break;

        default:
            m = AOPan.HALF_MODE;
            break;
        }

        // shape...
        switch (shape.getSelectedIndex()) {
        case 0:
            sh = AOPan.SQUARE_ROOT_SHAPE;
            break;

        case 2:
            sh = AOPan.SQUARE_SHAPE;
            break;

        default:
            sh = AOPan.LINEAR_SHAPE;
            break;
        }

        // tab...
        ALayerSelection l = getFocussedClip().getSelectedLayer().getLayerSelection();
        ALayerSelection ls = new ALayerSelection(new ALayer());
        ls.addChannelSelection(l.getChannelSelection(0));
        ls.addChannelSelection(l.getChannelSelection(1));

        switch (tab.getSelectedIndex()) {
        case 0: // constant
            ls.operateChannel0WithChannel1(new AOPan(m, sh, (float) pan.getData()));
            break;

        case 1: // variable
            ls.addChannelSelection(panChannel.getSelectedChannel().getChannelSelection());
            ls.operateChannel0WithChannel1WithChannel2(new AOPan(m, sh));
            break;
        }
    }

    public void reload() {
        panChannel.reload();
    }

}
