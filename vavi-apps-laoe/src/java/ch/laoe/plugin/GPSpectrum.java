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

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOSpectrum;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipEditor;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;


/**
 * Class: GPSpectrum @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * Spectrum analysis
 * 
 * @version 24.05.01 first draft oli4 19.09.01 undo-history save of spectrum-clip oli4
 * 
 */
public class GPSpectrum extends GPluginFrame {
    public GPSpectrum(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "spectrum";
    }

    // GUI
    private JComboBox window;

    private JButton createSpectrum, update;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 2);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("window")), 0, 0, 4, 1);
        String windowItem[] = {
            GLanguage.translate("rectangular"), GLanguage.translate("hamming"), GLanguage.translate("blackman"), GLanguage.translate("flattop")
        };
        window = new JComboBox(windowItem);
        cl.add(window, 4, 0, 6, 1);

        createSpectrum = new JButton(GLanguage.translate("new"));
        cl.add(createSpectrum, 1, 1, 4, 1);

        update = new JButton(GLanguage.translate("update"));
        cl.add(update, 5, 1, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        createSpectrum.addActionListener(eventDispatcher);
        update.addActionListener(eventDispatcher);

        updateGui();
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == update) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(100);
                Debug.println(1, "plugin " + getName() + " [update] clicked");
                onUpdate();
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == createSpectrum) {
                Debug.println(1, "plugin " + getName() + " [create spectrum] clicked");
                onCreateSpectrum();
            }

            updateGui();
        }
    }

    private GClipEditor spectrumClipEditor;

    private void updateGui() {
        if (spectrumClipEditor != null) {
            update.setEnabled(true);
        } else {
            update.setEnabled(false);
        }
    }

    private void onCreateSpectrum() {
        // create spectrum-clip and autoscale it...
        AClip c = new AClip(1, 1, AOSpectrum.getSpectrumLength());
        c.setName("<" + GLanguage.translate("spectrum") + ">");
        c.setSampleRate(getFocussedClip().getSampleRate());
        getMain().addClipFrame(c);

        spectrumClipEditor = getFocussedClipEditor();
    }

    private void onUpdate() {
        // window type
        int windowType;
        switch (window.getSelectedIndex()) {
        case 1:
            windowType = AOSpectrum.HAMMING_WINDOW;
            break;

        case 2:
            windowType = AOSpectrum.BLACKMAN_WINDOW;
            break;

        case 3:
            windowType = AOSpectrum.FLATTOP_WINDOW;
            break;

        default:
            windowType = AOSpectrum.RECTANGULAR_WINDOW;
        }

        // operate...
        AOSpectrum s = new AOSpectrum(windowType);
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(s);

        // copy result
        AClip c = spectrumClipEditor.getClip();
        AChannel ch = c.getLayer(0).getChannel(0);
        ch.sample = s.getSpectrum();

        // history
        ch.changeId();
        try {
            c.getHistory().store(loadIcon(), GLanguage.translate(getName()));
        } catch (NullPointerException npe) {
        }

        // refresh
        // c.getClipPlotter().autoScaleVisible();
        spectrumClipEditor.reload();
    }

}
