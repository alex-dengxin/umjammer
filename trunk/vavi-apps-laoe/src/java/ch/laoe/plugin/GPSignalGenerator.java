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
import javax.swing.JTabbedPane;

import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOConstGenerator;
import ch.laoe.operation.AOGaussianGenerator;
import ch.laoe.operation.AONoiseGenerator;
import ch.laoe.operation.AOQuantizationGenerator;
import ch.laoe.operation.AORampGenerator;
import ch.laoe.operation.AORectangleGenerator;
import ch.laoe.operation.AOSinusGenerator;
import ch.laoe.operation.AOSinusSweepGenerator;
import ch.laoe.operation.AOTriangleGenerator;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GControlTextF;
import ch.laoe.ui.GControlTextSF;
import ch.laoe.ui.GControlTextY;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * Class: GPSignalGenerator @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to generate divers signals (rectangle, triangle, sinus...).
 * 
 * @version 28.04.01 first draft oli4 15.05.01 random generator added oli4 02.09.01 reorganized and
 * transients added oli4 14.09.01 ramp generator added oli4
 * 
 */
public class GPSignalGenerator extends GPluginFrame {
    public GPSignalGenerator(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "signalGenerator";
    }

    // GUI
    private JComboBox periodicShape;

    private UiControlText periodicAmplitude, periodicOffset, periodicStartPeriod, periodicEndPeriod, periodicDutyCycle;

    private JCheckBox periodicNormalize, periodicAdd, periodicSweep;

    private JButton periodicApply;

    private JComboBox noiseShape;

    private UiControlText noiseAmplitude, noiseOffset;

    private JCheckBox noiseNormalize, noiseAdd;

    private JButton noiseApply;

    private JComboBox transientShape;

    private UiControlText transientAmplitude, transientTime, transientDuration;

    private JCheckBox transientNormalize, transientAdd;

    private JButton transientApply;

    private JComboBox constantShape;

    private UiControlText constantStartAmplitude, constantEndAmplitude;

    private JCheckBox constantNormalize, constantAdd;

    private JButton constantApply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // constant tab
        JPanel pConstant = new JPanel();
        UiCartesianLayout lConstant = new UiCartesianLayout(pConstant, 10, 9);
        lConstant.setPreferredCellSize(new Dimension(30, 35));
        pConstant.setLayout(lConstant);

        lConstant.add(new JLabel(GLanguage.translate("shape")), 0, 0, 4, 1);
        String constantShapeItems[] = {
            GLanguage.translate("constant"), GLanguage.translate("ramp")
        };
        constantShape = new JComboBox(constantShapeItems);
        lConstant.add(constantShape, 4, 0, 6, 1);

        lConstant.add(new JLabel(GLanguage.translate("startAmplitude")), 0, 1, 4, 1);
        constantStartAmplitude = new GControlTextY(getMain(), 10, true, true);
        constantStartAmplitude.setDataRange(-1e9, 1e9);
        constantStartAmplitude.setData(1);
        lConstant.add(constantStartAmplitude, 4, 1, 6, 1);

        lConstant.add(new JLabel(GLanguage.translate("endAmplitude")), 0, 2, 4, 1);
        constantEndAmplitude = new GControlTextY(getMain(), 10, true, true);
        constantEndAmplitude.setDataRange(-1e9, 1e9);
        constantEndAmplitude.setData(1);
        lConstant.add(constantEndAmplitude, 4, 2, 6, 1);

        constantNormalize = new JCheckBox(GLanguage.translate("normalize"));
        lConstant.add(constantNormalize, 0, 3, 5, 1);

        constantAdd = new JCheckBox(GLanguage.translate("add"));
        lConstant.add(constantAdd, 5, 3, 5, 1);

        constantApply = new JButton(GLanguage.translate("apply"));
        lConstant.add(constantApply, 3, 8, 4, 1);
        tabbedPane.add(GLanguage.translate("constant"), pConstant);

        // periodic tab
        JPanel pPeriodic = new JPanel();
        UiCartesianLayout lPeriodic = new UiCartesianLayout(pPeriodic, 10, 9);
        lPeriodic.setPreferredCellSize(new Dimension(25, 35));
        pPeriodic.setLayout(lPeriodic);

        lPeriodic.add(new JLabel(GLanguage.translate("shape")), 0, 0, 4, 1);
        String periodicShapeItems[] = {
            GLanguage.translate("rectangle"), GLanguage.translate("triangle"), GLanguage.translate("sinus"), GLanguage.translate("quantization")
        };
        periodicShape = new JComboBox(periodicShapeItems);
        lPeriodic.add(periodicShape, 4, 0, 6, 1);

        lPeriodic.add(new JLabel(GLanguage.translate("amplitude")), 0, 1, 4, 1);
        periodicAmplitude = new GControlTextY(getMain(), 10, true, true);
        periodicAmplitude.setDataRange(-1e9, 1e9);
        periodicAmplitude.setData(1);
        lPeriodic.add(periodicAmplitude, 4, 1, 6, 1);

        periodicNormalize = new JCheckBox(GLanguage.translate("normalize"));
        lPeriodic.add(periodicNormalize, 0, 2, 5, 1);

        periodicAdd = new JCheckBox(GLanguage.translate("add"));
        lPeriodic.add(periodicAdd, 5, 2, 5, 1);

        lPeriodic.add(new JLabel(GLanguage.translate("offset")), 0, 3, 4, 1);
        periodicOffset = new GControlTextY(getMain(), 10, true, true);
        periodicOffset.setDataRange(-1e6, 1e6);
        periodicOffset.setData(0);
        lPeriodic.add(periodicOffset, 4, 3, 6, 1);

        lPeriodic.add(new JLabel(GLanguage.translate("startPeriod")), 0, 4, 4, 1);
        periodicStartPeriod = new GControlTextF(getMain(), 10, true, true);
        periodicStartPeriod.setDataRange(1, 1e6);
        periodicStartPeriod.setData(1);
        lPeriodic.add(periodicStartPeriod, 4, 4, 6, 1);

        lPeriodic.add(new JLabel(GLanguage.translate("endPeriod")), 0, 5, 4, 1);
        periodicEndPeriod = new GControlTextF(getMain(), 10, true, true);
        periodicEndPeriod.setDataRange(1, 1e6);
        periodicEndPeriod.setData(1);
        lPeriodic.add(periodicEndPeriod, 4, 5, 6, 1);

        periodicSweep = new JCheckBox(GLanguage.translate("sweep"));
        lPeriodic.add(periodicSweep, 0, 6, 5, 1);

        lPeriodic.add(new JLabel(GLanguage.translate("dutyCycle")), 0, 7, 4, 1);
        periodicDutyCycle = new GControlTextA(10, true, true);
        periodicDutyCycle.setDataRange(-1e6, 1e6);
        periodicDutyCycle.setData(0.5);
        lPeriodic.add(periodicDutyCycle, 4, 7, 6, 1);

        periodicApply = new JButton(GLanguage.translate("apply"));
        lPeriodic.add(periodicApply, 3, 8, 4, 1);

        tabbedPane.add(GLanguage.translate("periodic"), pPeriodic);

        // noise tab
        JPanel pNoise = new JPanel();
        UiCartesianLayout lNoise = new UiCartesianLayout(pNoise, 10, 9);
        lNoise.setPreferredCellSize(new Dimension(25, 35));
        pNoise.setLayout(lNoise);

        lNoise.add(new JLabel(GLanguage.translate("shape")), 0, 0, 4, 1);
        String noiseShapeItems[] = {
            GLanguage.translate("whiteNoise"), GLanguage.translate("triangleNoise"), GLanguage.translate("gaussianNoise")
        };
        noiseShape = new JComboBox(noiseShapeItems);
        lNoise.add(noiseShape, 4, 0, 6, 1);

        lNoise.add(new JLabel(GLanguage.translate("amplitude")), 0, 1, 4, 1);
        noiseAmplitude = new GControlTextY(getMain(), 10, true, true);
        noiseAmplitude.setDataRange(-1e9, 1e9);
        noiseAmplitude.setData(1);
        lNoise.add(noiseAmplitude, 4, 1, 6, 1);

        noiseNormalize = new JCheckBox(GLanguage.translate("normalize"));
        lNoise.add(noiseNormalize, 0, 2, 5, 1);

        noiseAdd = new JCheckBox(GLanguage.translate("add"));
        lNoise.add(noiseAdd, 5, 2, 5, 1);

        lNoise.add(new JLabel(GLanguage.translate("offset")), 0, 3, 4, 1);
        noiseOffset = new GControlTextY(getMain(), 10, true, true);
        noiseOffset.setDataRange(-1e6, 1e6);
        noiseOffset.setData(0);
        lNoise.add(noiseOffset, 4, 3, 6, 1);

        noiseApply = new JButton(GLanguage.translate("apply"));
        lNoise.add(noiseApply, 3, 8, 4, 1);

        tabbedPane.add(GLanguage.translate("noise"), pNoise);

        // transient tab
        JPanel pTransient = new JPanel();
        UiCartesianLayout lTransient = new UiCartesianLayout(pTransient, 10, 9);
        lTransient.setPreferredCellSize(new Dimension(25, 35));
        pTransient.setLayout(lTransient);

        lTransient.add(new JLabel(GLanguage.translate("shape")), 0, 0, 4, 1);
        String transientShapeItems[] = {
            GLanguage.translate("gaussianDistribution")
        };
        transientShape = new JComboBox(transientShapeItems);
        lTransient.add(transientShape, 4, 0, 6, 1);

        lTransient.add(new JLabel(GLanguage.translate("amplitude")), 0, 1, 4, 1);
        transientAmplitude = new GControlTextY(getMain(), 10, true, true);
        transientAmplitude.setDataRange(-1e9, 1e9);
        transientAmplitude.setData(1);
        lTransient.add(transientAmplitude, 4, 1, 6, 1);

        transientNormalize = new JCheckBox(GLanguage.translate("normalize"));
        lTransient.add(transientNormalize, 0, 2, 5, 1);

        transientAdd = new JCheckBox(GLanguage.translate("add"));
        lTransient.add(transientAdd, 5, 2, 5, 1);

        lTransient.add(new JLabel(GLanguage.translate("time")), 0, 3, 4, 1);
        transientTime = new GControlTextSF(getMain(), 10, true, true);
        transientTime.setDataRange(-1e6, 1e6);
        transientTime.setData(0);
        lTransient.add(transientTime, 4, 3, 6, 1);

        lTransient.add(new JLabel(GLanguage.translate("duration")), 0, 4, 4, 1);
        transientDuration = new GControlTextSF(getMain(), 10, true, true);
        transientDuration.setDataRange(1, 1e6);
        transientDuration.setData(1);
        lTransient.add(transientDuration, 4, 4, 6, 1);

        transientApply = new JButton(GLanguage.translate("apply"));
        lTransient.add(transientApply, 3, 8, 4, 1);

        tabbedPane.add(GLanguage.translate("transient"), pTransient);

        frame.getContentPane().add(tabbedPane);
        pack();

        eventDispatcher = new EventDispatcher();
        constantApply.addActionListener(eventDispatcher);
        periodicApply.addActionListener(eventDispatcher);
        noiseApply.addActionListener(eventDispatcher);
        transientApply.addActionListener(eventDispatcher);
        constantShape.addActionListener(eventDispatcher);
        periodicShape.addActionListener(eventDispatcher);
        noiseShape.addActionListener(eventDispatcher);
        transientShape.addActionListener(eventDispatcher);
        constantNormalize.addActionListener(eventDispatcher);
        periodicNormalize.addActionListener(eventDispatcher);
        noiseNormalize.addActionListener(eventDispatcher);
        transientNormalize.addActionListener(eventDispatcher);
        periodicSweep.addActionListener(eventDispatcher);

        updateActiveComponents();
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == constantApply) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply constant] clicked");
                onConstantApply();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == periodicApply) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply periodic] clicked");
                onPeriodicApply();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == noiseApply) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply noise] clicked");
                onNoiseApply();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == transientApply) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply transient] clicked");
                onTransientApply();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            }

            updateActiveComponents();
        }
    }

    private void updateActiveComponents() {
        updateConstantComponents();
        updatePeriodicComponents();
        updateNoiseComponents();
        updateTransientComponents();
    }

    private void updatePeriodicComponents() {
        periodicAmplitude.setEnabled(!periodicNormalize.isSelected());
        periodicOffset.setEnabled(true);
        periodicNormalize.setEnabled(true);

        switch (periodicShape.getSelectedIndex()) {
        case 0: // rectangle
        case 1: // triangle
            periodicEndPeriod.setEnabled(false);
            periodicSweep.setEnabled(false);
            periodicDutyCycle.setEnabled(true);
            break;

        case 2: // sinus
            periodicSweep.setEnabled(true);
            periodicEndPeriod.setEnabled(periodicSweep.isSelected());
            periodicDutyCycle.setEnabled(true);
            break;

        case 3: // quantization
            periodicNormalize.setEnabled(false);
            periodicAmplitude.setEnabled(false);
            periodicOffset.setEnabled(false);
            periodicSweep.setEnabled(false);
            periodicEndPeriod.setEnabled(periodicSweep.isSelected());
            periodicDutyCycle.setEnabled(false);
            break;
        }
    }

    private void updateConstantComponents() {
        constantStartAmplitude.setEnabled(!constantNormalize.isSelected());

        switch (constantShape.getSelectedIndex()) {
        case 0: // constant
            constantEndAmplitude.setEnabled(false);
            break;

        case 1: // ramp
            constantEndAmplitude.setEnabled(true);
            break;
        }
    }

    private void updateNoiseComponents() {
        noiseAmplitude.setEnabled(!noiseNormalize.isSelected());
    }

    private void updateTransientComponents() {
        transientAmplitude.setEnabled(!transientNormalize.isSelected());
    }

    private void onConstantApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        float a;
        if (constantNormalize.isSelected()) {
            a = (float) Math.pow(2, getFocussedClip().getSampleWidth() - 1);
        } else {
            a = (float) constantStartAmplitude.getData();
        }

        switch (constantShape.getSelectedIndex()) {
        case 0: // constant
            ls.operateEachChannel(new AOConstGenerator(a, constantAdd.isSelected()));
            break;

        case 1: // ramp
            ls.operateEachChannel(new AORampGenerator((float) constantStartAmplitude.getData(), (float) constantEndAmplitude.getData(), constantAdd.isSelected(), constantNormalize.isSelected()));
            break;
        }

    }

    private void onPeriodicApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        float a;
        if (periodicNormalize.isSelected()) {
            a = (float) Math.pow(2, getFocussedClip().getSampleWidth() - 1);
        } else {
            a = (float) periodicAmplitude.getData();
        }

        switch (periodicShape.getSelectedIndex()) {
        case 0: // rectangle
            ls.operateEachChannel(new AORectangleGenerator(a, (float) periodicOffset.getData(), (int) periodicStartPeriod.getData(), (float) periodicDutyCycle.getData(), periodicAdd.isSelected()));
            break;

        case 1: // triangle
            ls.operateEachChannel(new AOTriangleGenerator(a, (float) periodicOffset.getData(), (int) periodicStartPeriod.getData(), (float) periodicDutyCycle.getData(), periodicAdd.isSelected()));
            break;

        case 2: // sinus
            if (periodicSweep.isSelected()) {
                ls.operateEachChannel(new AOSinusSweepGenerator(a, (float) periodicOffset.getData(), (int) periodicStartPeriod.getData(), (int) periodicEndPeriod.getData(), periodicAdd.isSelected()));
            } else {
                ls.operateEachChannel(new AOSinusGenerator(a, (float) periodicOffset.getData(), (int) periodicStartPeriod.getData(), periodicAdd.isSelected()));
            }
            break;

        case 3: // quantization
            ls.operateEachChannel(new AOQuantizationGenerator(0, (int) periodicStartPeriod.getData(), periodicAdd.isSelected()));
            break;
        }

    }

    private void onNoiseApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        float a;
        if (noiseNormalize.isSelected()) {
            a = (float) Math.pow(2, getFocussedClip().getSampleWidth() - 1);
        } else {
            a = (float) noiseAmplitude.getData();
        }

        switch (noiseShape.getSelectedIndex()) {
        case 0: // white noise
            ls.operateEachChannel(new AONoiseGenerator(a, (float) noiseOffset.getData(), AONoiseGenerator.WHITE, noiseAdd.isSelected()));
            break;

        case 1: // triangle noise
            ls.operateEachChannel(new AONoiseGenerator(a, (float) noiseOffset.getData(), AONoiseGenerator.TRIANGLE, noiseAdd.isSelected()));
            break;

        case 2: // gaussian noise
            ls.operateEachChannel(new AONoiseGenerator(a, (float) noiseOffset.getData(), AONoiseGenerator.GAUSSIAN, noiseAdd.isSelected()));
            break;
        }
    }

    private void onTransientApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        float a;
        if (transientNormalize.isSelected()) {
            a = (float) Math.pow(2, getFocussedClip().getSampleWidth() - 1);
        } else {
            a = (float) transientAmplitude.getData();
        }

        switch (transientShape.getSelectedIndex()) {
        case 0: // gaussian distribution
            ls.operateEachChannel(new AOGaussianGenerator(a, (float) transientTime.getData(), (float) transientDuration.getData(), transientAdd.isSelected()));
            break;

        }
    }

}
