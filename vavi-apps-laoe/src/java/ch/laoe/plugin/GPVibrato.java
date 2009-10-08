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
import ch.laoe.operation.AOVibrato;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextF;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;
import ch.oli4.ui.UiPersistance;


/**
 * GPVibrato
 * 
 * plugin to perform vibrato effect.
 * 
 * @target JDK 1.3
 * @author olivier gäumann, neuchâtel (switzerland)
 * @version 25.12.01 first draft oli4
 */
public class GPVibrato extends GPluginFrame {
    public GPVibrato(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "vibrato";
    }

    // GUI
    private JComboBox presets;

    private JComboBox shape;

    private UiControlText modulation, period;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    // presets
    private String presetsName[];

    private int shapePresets[];

    private float modulationPresets[], periodPresets[];

    private void initGui() {
        // presets
        UiPersistance persist = new UiPersistance(getName() + ".properties");
        persist.restore();
        int n = persist.getInt("numberOfPresets");
        presetsName = new String[n];
        shapePresets = new int[n];
        modulationPresets = new float[n];
        periodPresets = new float[n];

        for (int i = 0; i < n; i++) {
            presetsName[i] = GLanguage.translate(persist.getString("name_" + i));
            shapePresets[i] = persist.getInt("shape_" + i);
            modulationPresets[i] = persist.getFloat("modulation_" + i);
            periodPresets[i] = persist.getFloat("period_" + i);
        }

        // GUI
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 5);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("presets")), 0, 0, 4, 1);
        presets = new JComboBox(presetsName);
        cl.add(presets, 4, 0, 6, 1);

        cl.add(new JLabel(GLanguage.translate("shape")), 0, 1, 4, 1);
        String shapeItems[] = {
            GLanguage.translate("sinus"), GLanguage.translate("triangle"), GLanguage.translate("saw")
        };
        shape = new JComboBox(shapeItems);
        cl.add(shape, 4, 1, 6, 1);

        cl.add(new JLabel(GLanguage.translate("modulatedDelay")), 0, 2, 4, 1);
        modulation = new GControlTextX(getMain(), 7, true, true);
        modulation.setDataRange(0, 1e6);
        modulation.setData(100);
        cl.add(modulation, 4, 2, 6, 1);

        cl.add(new JLabel(GLanguage.translate("modulationPeriod")), 0, 3, 4, 1);
        period = new GControlTextF(getMain(), 7, true, true);
        period.setDataRange(0, 1e6);
        period.setData(1000);
        cl.add(period, 4, 3, 6, 1);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 4, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
        presets.addActionListener(eventDispatcher);
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
            } else if (e.getSource() == presets) {
                Debug.println(1, "plugin " + getName() + " [presets] clicked");
                onPresetChange();
            }
        }
    }

    private void onPresetChange() {
        float sr = getFocussedClip().getSampleRate();
        int i = presets.getSelectedIndex();

        shape.setSelectedIndex(shapePresets[i]);
        modulation.setData(sr * modulationPresets[i]);
        period.setData(sr / periodPresets[i]);
    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        int sh = 0;
        switch (shape.getSelectedIndex()) {
        case 0:
            sh = AOVibrato.SINUS;
            break;

        case 1:
            sh = AOVibrato.TRIANGLE;
            break;

        case 2:
            sh = AOVibrato.SAW;
            break;
        }

        ls.operateEachChannel(new AOVibrato((int) modulation.getData(), (int) period.getData(), sh));
    }

}
