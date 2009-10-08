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
import javax.swing.JPanel;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOHistogram;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipEditor;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;


/**
 * histogram analysis
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 08.06.02 first draft oli4
 */
public class GPHistogram extends GPluginFrame {
    public GPHistogram(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "histogram";
    }

    // GUI
    private JButton createHistogram, update;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 2);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        createHistogram = new JButton(GLanguage.translate("new"));
        cl.add(createHistogram, 1, 1, 4, 1);

        update = new JButton(GLanguage.translate("update"));
        cl.add(update, 5, 1, 4, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        createHistogram.addActionListener(eventDispatcher);
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
            } else if (e.getSource() == createHistogram) {
                Debug.println(1, "plugin " + getName() + " [create histogram] clicked");
                onCreateHistogram();
            }

            updateGui();
        }
    }

    private GClipEditor histogramClipEditor;

    private void updateGui() {
        if (histogramClipEditor != null) {
            update.setEnabled(true);
        } else {
            update.setEnabled(false);
        }
    }

    private void onCreateHistogram() {
        // create histogram-clip and autoscale it...
        AClip c = new AClip(1, 1, AOHistogram.getHistogramLength());
        c.setName("<" + GLanguage.translate("histogram") + ">");
        c.setSampleRate(getFocussedClip().getSampleRate());
        getMain().addClipFrame(c);

        histogramClipEditor = getFocussedClipEditor();
    }

    private void onUpdate() {
        // operate...
        AOHistogram h = new AOHistogram();
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(h);

        // copy result
        AClip c = histogramClipEditor.getClip();
        AChannel ch = c.getLayer(0).getChannel(0);
        ch.sample = h.getHistogram();

        // history
        ch.changeId();
        try {
            c.getHistory().store(loadIcon(), GLanguage.translate(getName()));
        } catch (NullPointerException npe) {
        }

        // refresh
        c.getClipPlotter().autoScale();
        histogramClipEditor.reload();
    }

}
