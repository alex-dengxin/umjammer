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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipPlotter;
import ch.laoe.clip.ALayer;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOFftFilter;
import ch.laoe.operation.AOFlatSpectrum;
import ch.laoe.operation.AOStopSpectrumRange;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipEditor;
import ch.laoe.ui.GControlTextSF;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * Class: GPFftFilter @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * FFT-filter plugin
 * 
 * @version 19.05.01 first draft oli4 08.06.02 sharp-edged filter spectrums added oli4
 * 
 */
public class GPFftFilter extends GPluginFrame {
    public GPFftFilter(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "fftFilter";
    }

    // GUI
    private JButton createFilter, flat, lowPass, highPass, bandPass, bandStop, apply;

    private UiControlText fCut, fBegin, fEnd;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        JTabbedPane tab = new JTabbedPane();

        // general tab
        JPanel p1 = new JPanel();
        UiCartesianLayout l1 = new UiCartesianLayout(p1, 10, 3);
        l1.setPreferredCellSize(new Dimension(30, 35));
        p1.setLayout(l1);

        flat = new JButton(GLanguage.translate("flat"));
        l1.add(flat, 3, 2, 4, 1);

        tab.add(GLanguage.translate("general"), p1);

        // high low pass filter tab
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 10, 3);
        l2.setPreferredCellSize(new Dimension(30, 35));
        p2.setLayout(l2);

        l2.add(new JLabel(GLanguage.translate("fCut")), 0, 0, 5, 1);
        fCut = new GControlTextSF(getMain(), 9, true, true);
        fCut.setEditable(true);
        fCut.setDataRange(0, 1e9);
        fCut.setData(0);
        // fCut.getFormat().setMaximumFractionDigits(3);
        l2.add(fCut, 5, 0, 5, 1);

        lowPass = new JButton(GLanguage.translate("lowPass"));
        l2.add(lowPass, 1, 2, 4, 1);

        highPass = new JButton(GLanguage.translate("highPass"));
        l2.add(highPass, 5, 2, 4, 1);

        tab.add(GLanguage.translate("filter"), p2);

        // band pass/stop filter tab
        JPanel p3 = new JPanel();
        UiCartesianLayout l3 = new UiCartesianLayout(p3, 10, 3);
        l3.setPreferredCellSize(new Dimension(30, 35));
        p3.setLayout(l3);

        l3.add(new JLabel(GLanguage.translate("fBegin")), 0, 0, 5, 1);
        fBegin = new GControlTextSF(getMain(), 9, true, true);
        fBegin.setEditable(true);
        fBegin.setDataRange(0, 1e9);
        fBegin.setData(0);
        // fBegin.getFormat().setMaximumFractionDigits(3);
        l3.add(fBegin, 5, 0, 5, 1);

        l3.add(new JLabel(GLanguage.translate("fEnd")), 0, 1, 5, 1);
        fEnd = new GControlTextSF(getMain(), 9, true, true);
        fEnd.setEditable(true);
        fEnd.setDataRange(0, 1e9);
        fEnd.setData(0);
        // fEnd.getFormat().setMaximumFractionDigits(3);
        l3.add(fEnd, 5, 1, 5, 1);

        bandPass = new JButton(GLanguage.translate("bandPass"));
        l3.add(bandPass, 1, 2, 4, 1);

        bandStop = new JButton(GLanguage.translate("bandStop"));
        l3.add(bandStop, 5, 2, 4, 1);

        tab.add(GLanguage.translate("bandFilter"), p3);

        p.add(tab, BorderLayout.CENTER);

        // at bottom
        JPanel pBottom = new JPanel();

        createFilter = new JButton(GLanguage.translate("new"));
        pBottom.add(createFilter);

        apply = new JButton(GLanguage.translate("apply"));
        pBottom.add(apply);

        p.add(pBottom, BorderLayout.SOUTH);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        createFilter.addActionListener(eventDispatcher);
        flat.addActionListener(eventDispatcher);
        lowPass.addActionListener(eventDispatcher);
        highPass.addActionListener(eventDispatcher);
        bandPass.addActionListener(eventDispatcher);
        bandStop.addActionListener(eventDispatcher);
        apply.addActionListener(eventDispatcher);

        updateGui();
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
            } else if (e.getSource() == createFilter) {
                Debug.println(1, "plugin " + getName() + " [create filter] clicked");
                onCreateFilter();
            } else if (e.getSource() == flat) {
                Debug.println(1, "plugin " + getName() + " [flat] clicked");
                onFlat();
            } else if (e.getSource() == lowPass) {
                Debug.println(1, "plugin " + getName() + " [lowpass] clicked");
                onLowPass();
            } else if (e.getSource() == highPass) {
                Debug.println(1, "plugin " + getName() + " [highpass] clicked");
                onHighPass();
            } else if (e.getSource() == bandPass) {
                Debug.println(1, "plugin " + getName() + " [bandpass] clicked");
                onBandPass();
            } else if (e.getSource() == bandStop) {
                Debug.println(1, "plugin " + getName() + " [bandstop] clicked");
                onBandStop();
            }

            updateGui();
        }
    }

    private GClipEditor filterClipEditor;

    private void updateGui() {
        if (filterClipEditor != null) {
            flat.setEnabled(true);
            apply.setEnabled(true);
            lowPass.setEnabled(true);
            highPass.setEnabled(true);
            bandPass.setEnabled(true);
            bandStop.setEnabled(true);
        } else {
            flat.setEnabled(false);
            apply.setEnabled(false);
            lowPass.setEnabled(false);
            highPass.setEnabled(false);
            bandPass.setEnabled(false);
            bandStop.setEnabled(false);
        }
    }

    private void onCreateFilter() {
        // create filter-clip and autoscale it...
        AClip c = new AClip(1, 1, AOFftFilter.getFilterLength());
        c.setName("<" + GLanguage.translate("fftFilter") + ">");
        c.setSampleRate(getFocussedClip().getSampleRate());
        AClipPlotter cp = c.getClipPlotter();
        cp.autoScaleX();
        cp.setYRange(-2, 4);

        getMain().addClipFrame(c);
        filterClipEditor = getFocussedClipEditor();
    }

    private void onFlat() {
        // make flat
        AClip c = filterClipEditor.getClip();
        ALayerSelection ls = c.getSelectedLayer().createLayerSelection();
        ls.operateEachChannel(new AOFlatSpectrum());
        c.getSelectedLayer().setEmptyLayerSelection();

        // refresh
        filterClipEditor.reload();
    }

    private void onLowPass() {
        // make flat
        AClip c = filterClipEditor.getClip();
        ALayer l = c.getSelectedLayer();
        ALayerSelection ls = l.createLayerSelection();
        ls.operateEachChannel(new AOFlatSpectrum());

        int freqCut = (int) fCut.getData();

        // make low pass
        l.modifyLayerSelection(freqCut, AOFftFilter.getFilterLength() - freqCut);
        ls = l.getLayerSelection();
        ls.operateEachChannel(new AOStopSpectrumRange());

        // remove selection
        c.getSelectedLayer().setEmptyLayerSelection();

        // refresh
        filterClipEditor.reload();
    }

    private void onHighPass() {
        // make flat
        AClip c = filterClipEditor.getClip();
        ALayer l = c.getSelectedLayer();
        ALayerSelection ls = l.createLayerSelection();
        ls.operateEachChannel(new AOFlatSpectrum());

        int freqCut = (int) fCut.getData();

        // make high pass
        l.modifyLayerSelection(0, freqCut);
        ls = l.getLayerSelection();
        ls.operateEachChannel(new AOStopSpectrumRange());

        // remove selection
        c.getSelectedLayer().setEmptyLayerSelection();

        // refresh
        filterClipEditor.reload();
    }

    private void onBandPass() {
        // make flat
        AClip c = filterClipEditor.getClip();
        ALayer l = c.getSelectedLayer();
        ALayerSelection ls = l.createLayerSelection();
        ls.operateEachChannel(new AOFlatSpectrum());

        int freqBegin = (int) fBegin.getData();
        int freqEnd = (int) fEnd.getData();

        // make low pass
        l.modifyLayerSelection(freqEnd, AOFftFilter.getFilterLength() - freqEnd);
        ls = l.getLayerSelection();
        ls.operateEachChannel(new AOStopSpectrumRange());

        // make high pass
        l.modifyLayerSelection(0, freqBegin);
        ls = l.getLayerSelection();
        ls.operateEachChannel(new AOStopSpectrumRange());

        // remove selection
        c.getSelectedLayer().setEmptyLayerSelection();

        // refresh
        filterClipEditor.reload();
    }

    private void onBandStop() {
        // make flat
        AClip c = filterClipEditor.getClip();
        ALayer l = c.getSelectedLayer();
        ALayerSelection ls = l.createLayerSelection();
        ls.operateEachChannel(new AOFlatSpectrum());

        int freqBegin = (int) fBegin.getData();
        int freqEnd = (int) fEnd.getData();

        // make band stop
        l.modifyLayerSelection(freqBegin, Math.abs(freqEnd - freqBegin));
        ls = l.getLayerSelection();
        ls.operateEachChannel(new AOStopSpectrumRange());

        // remove selection
        c.getSelectedLayer().setEmptyLayerSelection();

        // refresh
        filterClipEditor.reload();
    }

    private void onApply() {
        float filter[] = filterClipEditor.getClip().getLayer(0).getChannel(0).sample;
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOFftFilter(filter));
    }

}
