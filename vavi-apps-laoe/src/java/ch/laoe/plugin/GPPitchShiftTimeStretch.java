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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipSelection;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOPitchShift;
import ch.laoe.operation.AOResample;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChooser;
import ch.laoe.ui.GComboBoxPowerOf2;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GControlTextAF;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * Class: GPPitchShiftTimeStretch @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to pitch shift and time stretch
 * 
 * @version 10.06.01 first draft oli4 23.06.01 add variable FFT-length oli4 02.12.01 introduce tabs and
 * divers factors oli4 06.05.2003 variable pitch shift added oli4
 * 
 */
public class GPPitchShiftTimeStretch extends GPluginFrame {
    public GPPitchShiftTimeStretch(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "pitchShiftTimeStretch";
    }

    // GUI
    private GComboBoxPowerOf2 pitchConstBufferLength, pitchVarBufferLength, timeBufferLength;

    private JRadioButton byFactor, byNewTime;

    private UiControlText pitchFactor, pitchConstTransition, pitchVarTransition, timeTransition, timeFactor, newTime;

    private GClipLayerChooser layerChooser;

    private JButton pitchConstApply, pitchVarApply, timeApply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JTabbedPane tab = new JTabbedPane();

        // pitch shift tab
        JTabbedPane tabPitch = new JTabbedPane();

        // constant pitch shift
        JPanel pPitchConst = new JPanel();
        UiCartesianLayout lPitchConst = new UiCartesianLayout(pPitchConst, 10, 6);
        lPitchConst.setPreferredCellSize(new Dimension(25, 35));
        pPitchConst.setLayout(lPitchConst);

        pPitchConst.add(new JLabel(GLanguage.translate("factor")), new Rectangle(0, 0, 4, 1));
        pitchFactor = new GControlTextAF(6, true, true);
        pitchFactor.setDataRange(.3, 3);
        pitchFactor.setData(1);
        pPitchConst.add(pitchFactor, new Rectangle(4, 0, 6, 1));

        pPitchConst.add(new JLabel(GLanguage.translate("bufferLength")), new Rectangle(0, 3, 4, 1));
        pitchConstBufferLength = new GComboBoxPowerOf2(7, 10);
        pitchConstBufferLength.setSelectedExponent(14);
        pPitchConst.add(pitchConstBufferLength, new Rectangle(4, 3, 6, 1));

        pPitchConst.add(new JLabel(GLanguage.translate("transition")), new Rectangle(0, 4, 4, 1));
        pitchConstTransition = new GControlTextA(6, true, true);
        pitchConstTransition.setDataRange(.01, .9);
        pitchConstTransition.setData(.4);
        pPitchConst.add(pitchConstTransition, new Rectangle(4, 4, 6, 1));

        pitchConstApply = new JButton(GLanguage.translate("apply"));
        pPitchConst.add(pitchConstApply, new Rectangle(3, 5, 4, 1));

        tabPitch.add(GLanguage.translate("constant"), pPitchConst);

        // variable pitch shift
        JPanel pPitchVar = new JPanel();
        UiCartesianLayout lPitchVar = new UiCartesianLayout(pPitchVar, 10, 6);
        lPitchVar.setPreferredCellSize(new Dimension(25, 35));
        pPitchVar.setLayout(lPitchVar);

        layerChooser = new GClipLayerChooser(getMain(), "pitchShiftCurve");
        pPitchVar.add(layerChooser, new Rectangle(0, 0, 10, 3));

        pPitchVar.add(new JLabel(GLanguage.translate("bufferLength")), new Rectangle(0, 3, 4, 1));
        pitchVarBufferLength = new GComboBoxPowerOf2(7, 10);
        pitchVarBufferLength.setSelectedExponent(14);
        pPitchVar.add(pitchVarBufferLength, new Rectangle(4, 3, 6, 1));

        pPitchVar.add(new JLabel(GLanguage.translate("transition")), new Rectangle(0, 4, 4, 1));
        pitchVarTransition = new GControlTextA(6, true, true);
        pitchVarTransition.setDataRange(0.01, .9);
        pitchVarTransition.setData(.4);
        pPitchVar.add(pitchVarTransition, new Rectangle(4, 4, 6, 1));

        pitchVarApply = new JButton(GLanguage.translate("apply"));
        pPitchVar.add(pitchVarApply, new Rectangle(3, 5, 4, 1));

        tabPitch.add(GLanguage.translate("f(time)"), pPitchVar);

        tab.add(GLanguage.translate("pitchShift"), tabPitch);

        // time stretch tab
        JPanel pTime = new JPanel();
        UiCartesianLayout lTime = new UiCartesianLayout(pTime, 10, 6);
        lTime.setPreferredCellSize(new Dimension(25, 35));
        pTime.setLayout(lTime);

        byFactor = new JRadioButton(GLanguage.translate("factor"));
        lTime.add(byFactor, 0, 0, 4, 1);
        byNewTime = new JRadioButton(GLanguage.translate("newTime"));
        lTime.add(byNewTime, 0, 1, 4, 1);
        ButtonGroup bg = new ButtonGroup();
        bg.add(byFactor);
        bg.add(byNewTime);
        byFactor.setSelected(true);

        timeFactor = new GControlTextA(6, true, true);
        timeFactor.setDataRange(.3, 3);
        timeFactor.setData(1);
        lTime.add(timeFactor, 4, 0, 6, 1);

        newTime = new GControlTextX(getMain(), 6, true, true);
        newTime.setDataRange(0, 1e9);
        newTime.setData(1000);
        lTime.add(newTime, 4, 1, 6, 1);

        lTime.add(new JLabel(GLanguage.translate("bufferLength")), 0, 3, 4, 1);
        timeBufferLength = new GComboBoxPowerOf2(7, 10);
        timeBufferLength.setSelectedExponent(14);
        lTime.add(timeBufferLength, 4, 3, 6, 1);

        pTime.add(new JLabel(GLanguage.translate("transition")), new Rectangle(0, 4, 4, 1));
        timeTransition = new GControlTextA(6, true, true);
        timeTransition.setDataRange(0.01, .9);
        timeTransition.setData(.4);
        pTime.add(timeTransition, new Rectangle(4, 4, 6, 1));

        timeApply = new JButton(GLanguage.translate("apply"));
        lTime.add(timeApply, 3, 5, 4, 1);

        tab.add(GLanguage.translate("timeStretch"), pTime);

        // frame
        frame.getContentPane().add(tab);
        pack();
        updateActiveComponents();

        eventDispatcher = new EventDispatcher();
        pitchConstApply.addActionListener(eventDispatcher);
        pitchVarApply.addActionListener(eventDispatcher);
        timeApply.addActionListener(eventDispatcher);
        byFactor.addActionListener(eventDispatcher);
        byNewTime.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == pitchConstApply) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [pitch const apply] clicked");
                onPitchConstApply();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == pitchVarApply) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [pitch var apply] clicked");
                onPitchVarApply();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == timeApply) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [time apply] clicked");
                onTimeApply();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else {
                updateActiveComponents();
            }
        }
    }

    public void reload() {
        layerChooser.reload();
    }

    private void updateActiveComponents() {
        if (byFactor.isSelected()) {
            timeFactor.setEnabled(true);
            newTime.setEnabled(false);
        } else if (byNewTime.isSelected()) {
            timeFactor.setEnabled(false);
            newTime.setEnabled(true);
        }

    }

    private void onPitchConstApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        float f = (float) pitchFactor.getData();
        float t = (float) pitchConstTransition.getData();

        // pitch shift ?
        ls.operateEachChannel(new AOPitchShift(f, pitchConstBufferLength.getSelectedValue(), t));
    }

    private void onPitchVarApply() {
        float t = (float) pitchVarTransition.getData();
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ALayerSelection ps = layerChooser.getSelectedLayer().createLayerSelection();
        AClipSelection cs = new AClipSelection(new AClip());
        cs.addLayerSelection(ls);
        cs.addLayerSelection(ps);

        cs.operateLayer0WithLayer1(new AOPitchShift(pitchVarBufferLength.getSelectedValue(), t));
    }

    private void onTimeApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        float f = 1;
        float t = (float) timeTransition.getData();

        if (byFactor.isSelected()) {
            f = (float) timeFactor.getData();
        } else if (byNewTime.isSelected()) {
            f = (float) newTime.getData() / ls.getMaxLength();
        }
        // time stretch
        GProgressViewer.entrySubProgress();
        GProgressViewer.setProgress(50);
        ls.operateEachChannel(new AOPitchShift(f, timeBufferLength.getSelectedValue(), t));
        GProgressViewer.setProgress(100);
        ls.operateEachChannel(new AOResample(1 / f, 3));
        pluginHandler.getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
        GProgressViewer.exitSubProgress();
    }
}
