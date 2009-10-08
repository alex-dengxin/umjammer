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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.laoe.audio.Audio;
import ch.laoe.clip.AClip;
import ch.laoe.operation.AOToolkit;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiSignalLedBalken;


/**
 * VU-meter plugin
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @version 09.01.02 first draft oli4
 */
public class GPVuMeter extends GPluginFrame implements Runnable {
    public GPVuMeter(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "vuMeter";
    }

    // GUI
    private static final int numberOfBars = 2;

    private UiSignalLedBalken bar[];

    private EventDispatcher eventDispatcher;

    private Thread thread;

    private void initGui() {
//      JTabbedPane tabbedPane = new JTabbedPane();

        // const tab
        JPanel p = new JPanel();
        UiCartesianLayout l = new UiCartesianLayout(p, 3, 25);
        l.setPreferredCellSize(new Dimension(45, 10));
        p.setLayout(l);

        Color colors[] = {
            Color.green, Color.orange, Color.red
        };
        double rangeMin[] = {
            -60, -10, 0
        };
        double rangeMax[] = {
            -10, 0, 10
        };
        bar = new UiSignalLedBalken[numberOfBars];
        for (int i = 0; i < numberOfBars; i++) {
            bar[i] = new UiSignalLedBalken();
            bar[i].setDataRange(-60, 10);
            bar[i].setOrientation(UiSignalLedBalken.VERTICAL);
            bar[i].setNumberOfLeds(28);
            bar[i].setColoredRanges(colors, rangeMin, rangeMax);
            bar[i].setData(20);
            bar[i].setTransitionTime(0);
            bar[i].setPeakDetectionEnabled(true);
            l.add(bar[i], i + 1, 0, 1, 25);
        }

        l.add(new JLabel("+10"), 0, 0, 1, 2);
        l.add(new JLabel("0dB"), 0, 3, 1, 2);
        l.add(new JLabel("-10"), 0, 6, 1, 2);
        l.add(new JLabel("-60"), 0, 22, 1, 2);

        frame.getContentPane().add(p);
        pack();
        frame.setResizable(true);

        eventDispatcher = new EventDispatcher();

        thread = new Thread(this);
        thread.start();
    }

    public void reload() {
        int ch = getFocussedClip().getMaxNumberOfChannels();

        for (int i = 0; i < numberOfBars; i++) {
            if (i < ch) {
                bar[i].setVisible(true);
            } else {
                bar[i].setVisible(false);
            }
        }
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        }
    }

    // thread
    private static final int peakPeriod = 10;

    private int peakPeriodCounter = 0;

    public void run() {
        while (true) {
            try {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                }

                if (frame.isVisible()) {
                    AClip c = getFocussedClip();
                    Audio a = c.getAudio();
                    int ch = c.getMaxNumberOfChannels();

                    if (a.isActive()) {
                        for (int i = 0; i < ch; i++) {
                            bar[i].setData(AOToolkit.todB(a.getPeakLevel(i) / (1 << (c.getSampleWidth() - 1))));
                        }
                    } else {
                        for (int i = 0; i < ch; i++) {
                            bar[i].setData(AOToolkit.todB(0));
                        }
                    }

                    // peak
                    peakPeriodCounter = (peakPeriodCounter + 1) % peakPeriod;
                    if (peakPeriodCounter == 0) {
                        for (int i = 0; i < ch; i++) {
                            bar[i].clearPeakDetection();
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

}
