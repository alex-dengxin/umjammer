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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOFade;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * Class: GPFade @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to different fade modes
 * 
 * @version 28.04.01 first draft oli4
 * 
 */
public class GPFade extends GPluginFrame {
    public GPFade(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "fade";
    }

    // GUI
    private JComboBox fadeType;

    private JComboBox order;

    private JCheckBox continueLow;

    private UiControlText lowFactor;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 5);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);
        cl.add(new JLabel(GLanguage.translate("mode")), 0, 0, 4, 1);
        String fadeTypeItem[] = {
            GLanguage.translate("fadeIn"), GLanguage.translate("fadeOut"), GLanguage.translate("fadeCross")
        };
        fadeType = new JComboBox(fadeTypeItem);
        fadeType.setSelectedIndex(0);
        cl.add(fadeType, 4, 0, 6, 1);

        cl.add(new JLabel(GLanguage.translate("order")), 0, 1, 4, 1);
        String orderItem[] = {
            GLanguage.translate("squareRoot"), GLanguage.translate("linear"), GLanguage.translate("square"), GLanguage.translate("cubic")
        };
        order = new JComboBox(orderItem);
        order.setSelectedIndex(2);
        cl.add(order, 4, 1, 6, 1);

        cl.add(new JLabel(GLanguage.translate("lowFactor")), 0, 2, 4, 1);
        lowFactor = new GControlTextA(7, true, true);
        lowFactor.setDataRange(0, 1);
        lowFactor.setData(0);
        cl.add(lowFactor, 4, 2, 6, 1);

        continueLow = new JCheckBox(GLanguage.translate("continueLow"));
        cl.add(continueLow, 0, 3, 5, 1);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 4, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        fadeType.addActionListener(eventDispatcher);
        apply.addActionListener(eventDispatcher);

        updateComponents();
    }

    private void updateComponents() {
        switch (fadeType.getSelectedIndex()) {
        case 0:
        case 1:
            continueLow.setEnabled(true);
            lowFactor.setEnabled(true);
            break;

        default:
            continueLow.setEnabled(false);
            lowFactor.setEnabled(false);
        }
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
            } else if (e.getSource() == fadeType) {
                Debug.println(1, "plugin " + getName() + " [fade type] clicked");
                updateComponents();
            }
        }
    }

    private void onApply() {
        // fade-type...
        int t = AOFade.IN;
        switch (fadeType.getSelectedIndex()) {
        case 0:
            t = AOFade.IN;
            break;

        case 1:
            t = AOFade.OUT;
            break;

        case 2:
            t = AOFade.CROSS;
            break;
        }

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
        boolean c = continueLow.isSelected();
        float lf = (float) lowFactor.getData();

        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOFade(t, o, lf, c));
    }

}
