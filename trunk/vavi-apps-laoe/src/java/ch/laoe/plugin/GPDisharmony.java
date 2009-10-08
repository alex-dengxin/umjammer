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
import ch.laoe.operation.AODisharmony;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GComboBoxPowerOf2;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to disharmony
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 17.06.01 first draft oli4 <br>
 *          02.08.01 divers disharmony-types added oli4 <br>
 */
public class GPDisharmony extends GPluginFrame {
    public GPDisharmony(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "disharmony";
    }

    // GUI
    private UiControlText dry, wet;

    private UiControlText shift;

    private JComboBox type;

    private GComboBoxPowerOf2 bufferLength;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 6);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        String typeItem[] = {
            GLanguage.translate("shift"), GLanguage.translate("blur"), GLanguage.translate("unpitch"), GLanguage.translate("bassDephase"), GLanguage.translate("trebleDephase")
        };
        cl.add(new JLabel(GLanguage.translate("type")), 0, 0, 5, 1);
        type = new JComboBox(typeItem);
        type.setSelectedIndex(0);
        cl.add(type, 5, 0, 5, 1);

        cl.add(new JLabel(GLanguage.translate("dryFactor")), 0, 1, 5, 1);
        dry = new GControlTextA(6, true, true);
        dry.setDataRange(0, 1);
        dry.setData(0);
        cl.add(dry, 5, 1, 5, 1);

        cl.add(new JLabel(GLanguage.translate("wetFactor")), 0, 2, 5, 1);
        wet = new GControlTextA(6, true, true);
        wet.setDataRange(0, 1);
        wet.setData(1);
        cl.add(wet, 5, 2, 5, 1);

        cl.add(new JLabel(GLanguage.translate("disharmony")), 0, 3, 5, 1);
        shift = new GControlTextA(6, true, true);
        shift.setDataRange(-1, 1);
        shift.setData(.01);
        cl.add(shift, 5, 3, 5, 1);

        cl.add(new JLabel(GLanguage.translate("bufferLength")), 0, 4, 5, 1);
        bufferLength = new GComboBoxPowerOf2(7, 20);
        bufferLength.setSelectedExponent(10);
        cl.add(bufferLength, 5, 4, 5, 1);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 5, 4, 1);

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
                updateHistory(GLanguage.translate(getName() + " " + (String) type.getSelectedItem()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            }
        }
    }

    private void onApply() {
        int bl = bufferLength.getSelectedValue();
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AODisharmony(type.getSelectedIndex(), (float) dry.getData(), (float) wet.getData(), (float) shift.getData(), bl));
    }

}
