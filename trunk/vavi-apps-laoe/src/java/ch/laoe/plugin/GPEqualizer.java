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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOEqualizer;
import ch.laoe.operation.AOToolkit;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiPersistance;


/**
 * Class: GPEqualizer @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * 13-band +-12dB equalizer plugin
 * 
 * @version 30.04.01 first draft oli4 01.12.01 change from 9 to 13 band oli4 03.05.2003 change to
 * choosable 5-9-13-25 band oli4
 * 
 */
public class GPEqualizer extends GPluginFrame {
    public GPEqualizer(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "equalizer";
    }

    // GUI
    private JSlider bands[];

    private JLabel labels[];

    float frequency[] = {
        20, 27, 36, 47, 63, 84, 112, 150, 200, 266, 355, 474, 631, 842, 1122, 1497, 1996, 2661, 3548, 4730, 6307, 8409, 11212, 14950, 20000
    };

    private JComboBox numberOfBands, predefined;

    private JComboBox order;

    private JButton reset, invert, apply;

    private EventDispatcher eventDispatcher;

    // presets
    private String presetsName[];

    private float bandsPresets[][];

    private void initGui() {
        // presets
        UiPersistance persist = new UiPersistance(getName() + ".properties");
        persist.restore();
        int n = persist.getInt("numberOfPresets");
        presetsName = new String[n];
        bandsPresets = new float[n][];

        for (int i = 0; i < n; i++) {
            presetsName[i] = GLanguage.translate(persist.getString("name_" + i));
            bandsPresets[i] = new float[frequency.length];
            for (int j = 0; j < frequency.length; j++) {
                try {
                    bandsPresets[i][j] = persist.getFloat("band_" + i + "_" + j);
                } catch (Exception e) {
                }
            }
        }

        // GUI
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 27, 6);
        cl.setPreferredCellSize(new Dimension(30, 35));
        cl.setCellGap(0);
        p.setLayout(cl);
        bands = new JSlider[frequency.length];
        cl.add(new JLabel("+18dB"), 0, 0, 2, 1);
        cl.add(new JLabel("-18dB"), 0, 3, 2, 1);
        labels = new JLabel[frequency.length];

        for (int i = 0; i < bands.length; i++) {
            bands[i] = new JSlider(JSlider.VERTICAL, -180, 180, 0);
            bands[i].setOrientation(JSlider.VERTICAL);
            bands[i].setMajorTickSpacing(180);
            bands[i].setMinorTickSpacing(20);
            bands[i].setPaintTicks(true);
            cl.add(bands[i], i + 2, 0, 1, 4);

            String s;
            if (frequency[i] < 1000) {
                s = "" + ((int) frequency[i]);
            } else if (frequency[i] < 10000) {
                s = "" + (((float) ((int) (frequency[i] + 50) / 100)) / 10) + "K";
            } else {
                s = "" + ((int) (frequency[i] + 500) / 1000) + "K";
            }
            labels[i] = new JLabel(s);
            cl.add(labels[i], i + 2, 4, 1, 1);
        }

        String numberOfBandsItems[] = {
            GLanguage.translate("5Bands"), GLanguage.translate("9Bands"), GLanguage.translate("13Bands"), GLanguage.translate("25Bands")
        };
        numberOfBands = new JComboBox(numberOfBandsItems);
        numberOfBands.setSelectedIndex(3);
        cl.add(numberOfBands, 1, 5, 3, 1);

        predefined = new JComboBox(presetsName);
        cl.add(predefined, 4, 5, 5, 1);

        String orderItem[] = {
            GLanguage.translate("1Order"), GLanguage.translate("2Order"), GLanguage.translate("3Order"), GLanguage.translate("4Order")
        };
        order = new JComboBox(orderItem);
        cl.add(order, 9, 5, 3, 1);

        reset = new JButton(GLanguage.translate("reset"));
        cl.add(reset, 15, 5, 3, 1);

        invert = new JButton(GLanguage.translate("invert"));
        cl.add(invert, 18, 5, 3, 1);

        apply = new JButton(GLanguage.translate("apply"));
        cl.add(apply, 21, 5, 3, 1);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        for (int i = 0; i < bands.length; i++) {
            bands[i].addChangeListener(eventDispatcher);
        }
        numberOfBands.addActionListener(eventDispatcher);
        predefined.addActionListener(eventDispatcher);
        reset.addActionListener(eventDispatcher);
        invert.addActionListener(eventDispatcher);
        apply.addActionListener(eventDispatcher);
    }

    private int getNumberOfBands() {
        switch (numberOfBands.getSelectedIndex()) {
        case 0:
            return 5;

        case 1:
            return 9;

        case 2:
            return 13;

        case 3:
            return 25;
        }
        return 25;
    }

    private class EventDispatcher implements ActionListener, ChangeListener {
        public void stateChanged(ChangeEvent e) {
            smoothNeighbourBands();
        }

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
            } else if (e.getSource() == numberOfBands) {
                Debug.println(1, "plugin " + getName() + " [" + getNumberOfBands() + " bands] selected");
                reload();
            } else if (e.getSource() == predefined) {
                Debug.println(1, "plugin " + getName() + " [predefined] clicked");
                onPredefined();
            } else if (e.getSource() == reset) {
                Debug.println(1, "plugin " + getName() + " [reset] clicked");
                onReset();
            } else if (e.getSource() == invert) {
                Debug.println(1, "plugin " + getName() + " [invert] clicked");
                onInvert();
            }
            smoothNeighbourBands();
        }
    }

    private void interpolateBand(int leftBand, int concernedBand, int rightBand) {
        bands[concernedBand].setValue((int) (((float) (bands[leftBand].getValue() * (rightBand - concernedBand) + bands[rightBand].getValue() * (concernedBand - leftBand))) / (rightBand - leftBand)));
    }

    private void copyBand(int sourceBand, int destBand) {
        bands[destBand].setValue(bands[sourceBand].getValue());
    }

    private void smoothNeighbourBands() {
        // number of bands limitation...
        switch (getNumberOfBands()) {
        case 5:
            interpolateBand(2, 3, 7);
            interpolateBand(2, 4, 7);
            interpolateBand(2, 5, 7);
            interpolateBand(2, 6, 7);
            interpolateBand(7, 8, 12);
            interpolateBand(7, 9, 12);
            interpolateBand(7, 10, 12);
            interpolateBand(7, 11, 12);
            interpolateBand(12, 13, 17);
            interpolateBand(12, 14, 17);
            interpolateBand(12, 15, 17);
            interpolateBand(12, 16, 17);
            interpolateBand(17, 18, 22);
            interpolateBand(17, 19, 22);
            interpolateBand(17, 20, 22);
            interpolateBand(17, 21, 22);
            copyBand(2, 0);
            copyBand(2, 1);
            copyBand(22, 23);
            copyBand(22, 24);
            break;

        case 9:
            interpolateBand(0, 1, 3);
            interpolateBand(0, 2, 3);
            interpolateBand(3, 4, 6);
            interpolateBand(3, 5, 6);
            interpolateBand(6, 7, 9);
            interpolateBand(6, 8, 9);
            interpolateBand(9, 10, 12);
            interpolateBand(9, 11, 12);
            interpolateBand(12, 13, 15);
            interpolateBand(12, 14, 15);
            interpolateBand(15, 16, 18);
            interpolateBand(15, 17, 18);
            interpolateBand(18, 19, 21);
            interpolateBand(18, 20, 21);
            interpolateBand(21, 22, 24);
            interpolateBand(21, 23, 24);
            break;

        case 13:
            interpolateBand(0, 1, 2);
            interpolateBand(2, 3, 4);
            interpolateBand(4, 5, 6);
            interpolateBand(6, 7, 8);
            interpolateBand(8, 9, 10);
            interpolateBand(10, 11, 12);
            interpolateBand(12, 13, 14);
            interpolateBand(14, 15, 16);
            interpolateBand(16, 17, 18);
            interpolateBand(18, 19, 20);
            interpolateBand(20, 21, 22);
            interpolateBand(22, 23, 24);
            break;
        }
    }

    public void reload() {
        try {
            // number of bands limitation...
            switch (getNumberOfBands()) {
            case 5:
                for (int i = 0; i < bands.length; i++) {
                    bands[i].setVisible(i % 5 == 2);
                }
                break;

            case 9:
                for (int i = 0; i < bands.length; i++) {
                    bands[i].setVisible(i % 3 == 0);
                }
                break;

            case 13:
                for (int i = 0; i < bands.length; i++) {
                    bands[i].setVisible(i % 2 == 0);
                }
                break;

            case 25:
                for (int i = 0; i < bands.length; i++) {
                    bands[i].setVisible(true);
                }
                break;
            }

            // frequency band limitation...
            for (int i = 0; i < bands.length; i++) {
                if (frequency[i] > getFocussedClip().getSampleRate()) {
                    bands[i].setVisible(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // labels
            for (int i = 0; i < bands.length; i++) {
                labels[i].setVisible(bands[i].isVisible());
                // System.out.println("label["+i+"]'s visibility is "+bands[i].isVisible());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onApply() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();

        float freq[] = new float[frequency.length];
        float gain[] = new float[freq.length];

        float q = (float) (1.75);

        // frequency...
        for (int i = 0; i < freq.length; i++) {
            freq[i] = frequency[i] / getFocussedClip().getSampleRate();
        }

        // gain...
        for (int i = 0; i < gain.length; i++) {
            gain[i] = AOToolkit.fromdB((float) bands[i].getValue() / 10); // +-12dB really
        }

        /**
         * gain compensation: i have measured the unperfect transfer function of a flat equalizer spectrum, and here i try to
         * compensate this unperfection... (not guaranteed better quality)
         */
        try {
            gain[0] /= 1f;
            gain[1] /= 1f;
            gain[2] /= 1f;
            gain[3] /= 1f;
            gain[4] /= 1f;
            gain[5] /= 1f;
            gain[6] /= 1f;
            gain[7] /= 1f;
            gain[8] /= 1f;
            gain[9] /= 1f;
            gain[10] /= 1f;
            gain[11] /= 1f;
            gain[12] /= 1.02f;
            gain[13] /= 1.03f;
            gain[14] /= 1.03f;
            gain[15] /= 1.04f;
            gain[16] /= 1.05f;
            gain[17] /= 1.06f;
            gain[18] /= 1.08f;
            gain[19] /= 1.11f;
            gain[20] /= 1.15f;
            gain[21] /= 1.17f;
            gain[22] /= 1.15f;
            gain[23] /= .98f;
            gain[24] /= 1f;
        } catch (Exception e) {
        }

        GProgressViewer.entrySubProgress();
        int o = order.getSelectedIndex() + 1;
        for (int i = 0; i < o; i++) {
            GProgressViewer.setProgress((i + 1) * 100 / o);
            GProgressViewer.entrySubProgress("order", " " + i);
            ls.operateEachChannel(new AOEqualizer(freq, gain, q));
            GProgressViewer.exitSubProgress();
        }
        GProgressViewer.exitSubProgress();
    }

    private void onPredefined() {
        int i = predefined.getSelectedIndex();
        for (int j = 0; j < bands.length; j++) {
            bands[j].setValue((int) (bandsPresets[i][j] * 10));
        }
    }

    private void onReset() {
        for (int i = 0; i < bands.length; i++) {
            bands[i].setValue(0);
        }
    }

    private void onInvert() {
        for (int i = 0; i < bands.length; i++) {
            bands[i].setValue(-bands[i].getValue());
        }
    }

}
