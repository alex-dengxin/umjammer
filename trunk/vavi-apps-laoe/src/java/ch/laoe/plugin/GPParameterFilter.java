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
import ch.laoe.operation.AOBandPass;
import ch.laoe.operation.AOHighPass;
import ch.laoe.operation.AOLowPass;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GControlTextF;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to perform delay-echo effect.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 09.11.00 first draft oli4
 */
public class GPParameterFilter extends GPluginFrame {
    public GPParameterFilter(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "parameterFilter";
    }

    // GUI
    private JComboBox filterType;

    private UiControlText dry, wet, freq, q;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 6);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("type")), 0, 0, 4, 1);
        String filterTypeItems[] = {
            GLanguage.translate("lowPass"), GLanguage.translate("highPass"), GLanguage.translate("bandPass")
        };
        filterType = new JComboBox(filterTypeItems);
        cl.add(filterType, 4, 0, 6, 1);

        cl.add(new JLabel(GLanguage.translate("dryFactor")), 0, 1, 4, 1);
        dry = new GControlTextA(7, true, true);
        dry.setDataRange(0, 1);
        dry.setData(0);
        cl.add(dry, 4, 1, 6, 1);

        cl.add(new JLabel(GLanguage.translate("wetFactor")), 0, 2, 4, 1);
        wet = new GControlTextA(7, true, true);
        wet.setDataRange(0, 1);
        wet.setData(1);
        cl.add(wet, 4, 2, 6, 1);

        cl.add(new JLabel(GLanguage.translate("frequency")), 0, 3, 4, 1);
        freq = new GControlTextF(getMain(), 7, true, true);
        freq.setDataRange(0, 1e6);
        freq.setData(0);
        cl.add(freq, 4, 3, 6, 1);

        cl.add(new JLabel(GLanguage.translate("quality")), 0, 4, 4, 1);
        q = new GControlTextA(7, true, true);
        q.setDataRange(0.5, 1e4);
        q.setData(1.4);
        cl.add(q, 4, 4, 6, 1);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 5, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
        filterType.addActionListener(eventDispatcher);

        onFilterTypeChange();
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
            } else if (e.getSource() == filterType) {
                Debug.println(1, "plugin " + getName() + " [filter type] clicked");
                onFilterTypeChange();
            }
        }
    }

    private void onFilterTypeChange() {
        switch (filterType.getSelectedIndex()) {
        case 0: // lowpass
            q.setEnabled(false);
            break;

        case 1: // highpass
            q.setEnabled(false);
            break;

        case 2: // bandpass
            q.setEnabled(true);
            break;

        }
    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        float f = 1.f / (float) freq.getData();

        switch (filterType.getSelectedIndex()) {
        case 0: // lowpass
            ls.operateEachChannel(new AOLowPass((float) dry.getData(), (float) wet.getData(), f));
            break;

        case 1: // highpass
            ls.operateEachChannel(new AOHighPass((float) dry.getData(), (float) wet.getData(), f));
            break;

        case 2: // bandpass
            ls.operateEachChannel(new AOBandPass((float) dry.getData(), (float) wet.getData(), f, (float) q.getData()));
            break;
        }
    }

}
