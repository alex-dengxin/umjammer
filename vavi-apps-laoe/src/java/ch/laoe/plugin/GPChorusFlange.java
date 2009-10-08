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
import ch.laoe.operation.AOChorusFlange;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GControlTextF;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;
import ch.oli4.ui.UiPersistance;


/**
 * plugin to perform chorus-flange effect.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 11.05.01 first draft oli4
 */
public class GPChorusFlange extends GPluginFrame {
    public GPChorusFlange(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "chorusFlange";
    }

    // GUI
    private JComboBox presets;

    private UiControlText dry, wet, feedback;

    private JCheckBox negFeedback;

    private JComboBox shape;

    private UiControlText base, modulation, period;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    // presets
    private String presetsName[];

    private float dryPresets[], wetPresets[], feedbackPresets[];

    private boolean negFeedbackPresets[];

    private int shapePresets[];

    private float basePresets[], modulationPresets[], periodPresets[];

    private void initGui() {
        // presets
        UiPersistance persist = new UiPersistance(getName() + ".properties");
        persist.restore();
        int n = persist.getInt("numberOfPresets");
        presetsName = new String[n];
        dryPresets = new float[n];
        wetPresets = new float[n];
        feedbackPresets = new float[n];
        negFeedbackPresets = new boolean[n];
        shapePresets = new int[n];
        basePresets = new float[n];
        modulationPresets = new float[n];
        periodPresets = new float[n];

        for (int i = 0; i < n; i++) {
            presetsName[i] = GLanguage.translate(persist.getString("name_" + i));
            dryPresets[i] = persist.getFloat("dry_" + i);
            wetPresets[i] = persist.getFloat("wet_" + i);
            feedbackPresets[i] = persist.getFloat("feedback_" + i);
            negFeedbackPresets[i] = persist.getBoolean("negFeedback_" + i);
            shapePresets[i] = persist.getInt("shape_" + i);
            basePresets[i] = persist.getFloat("base_" + i);
            modulationPresets[i] = persist.getFloat("modulation_" + i);
            periodPresets[i] = persist.getFloat("period_" + i);
        }

        // GUI
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 10);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("presets")), 0, 0, 4, 1);
        presets = new JComboBox(presetsName);
        cl.add(presets, 4, 0, 6, 1);

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

        cl.add(new JLabel(GLanguage.translate("feedback")), 0, 3, 4, 1);
        feedback = new GControlTextA(7, true, true);
        feedback.setDataRange(0, 1);
        feedback.setData(.5);
        cl.add(feedback, 4, 3, 6, 1);

        negFeedback = new JCheckBox(GLanguage.translate("negativeFeedback"));
        cl.add(negFeedback, 0, 4, 10, 1);

        cl.add(new JLabel(GLanguage.translate("shape")), 0, 5, 4, 1);
        String shapeItems[] = {
            GLanguage.translate("sinus"), GLanguage.translate("triangle"), GLanguage.translate("saw")
        };
        shape = new JComboBox(shapeItems);
        cl.add(shape, 4, 5, 6, 1);

        cl.add(new JLabel(GLanguage.translate("baseDelay")), 0, 6, 4, 1);
        base = new GControlTextX(getMain(), 7, true, true);
        base.setDataRange(0, 1e6);
        base.setData(1000);
        cl.add(base, 4, 6, 6, 1);

        cl.add(new JLabel(GLanguage.translate("modulatedDelay")), 0, 7, 4, 1);
        modulation = new GControlTextX(getMain(), 7, true, true);
        modulation.setDataRange(0, 1e6);
        modulation.setData(100);
        cl.add(modulation, 4, 7, 6, 1);

        cl.add(new JLabel(GLanguage.translate("modulationPeriod")), 0, 8, 4, 1);
        period = new GControlTextF(getMain(), 7, true, true);
        period.setDataRange(0, 1e6);
        period.setData(1000);
        cl.add(period, 4, 8, 6, 1);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 3, 9, 4, 1);

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
                onPresetChange();
            }
        }
    }

    private void onPresetChange() {
        float sr = getFocussedClip().getSampleRate();
        int i = presets.getSelectedIndex();

        dry.setData(dryPresets[i]);
        wet.setData(wetPresets[i]);
        feedback.setData(feedbackPresets[i]);
        negFeedback.setSelected(negFeedbackPresets[i]);
        shape.setSelectedIndex(shapePresets[i]);
        base.setData(sr * basePresets[i]);
        modulation.setData(sr * modulationPresets[i]);
        period.setData(sr / periodPresets[i]);
    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        int sh = 0;
        switch (shape.getSelectedIndex()) {
        case 0:
            sh = AOChorusFlange.SINUS;
            break;

        case 1:
            sh = AOChorusFlange.TRIANGLE;
            break;

        case 2:
            sh = AOChorusFlange.SAW;
            break;
        }

        ls.operateEachChannel(new AOChorusFlange((float) dry.getData(), (float) wet.getData(), (float) feedback.getData(), negFeedback.isSelected(), (int) base.getData(), (int) modulation.getData(), (int) period.getData(), sh));
    }

}
