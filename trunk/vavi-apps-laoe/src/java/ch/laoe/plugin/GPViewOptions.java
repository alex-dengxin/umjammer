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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.laoe.clip.AChannelPlotterSpectrogram;
import ch.laoe.clip.AClipPlotter;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GComboBoxPowerOf2;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GLanguage;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlEvent;
import ch.oli4.ui.UiControlListener;
import ch.oli4.ui.UiControlText;


/**
 * GPViewOptions.
 * 
 * plugin to modify view-options
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK: 1.4
 * @version 11.02.02 first draft as "GPUnit" oli4 <br>
 *          08.03.02 extend to view-options oli4 <r>
 */
public class GPViewOptions extends GPluginFrame {
    public GPViewOptions(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "viewOptions";
    }

    // ********************* GUI **********************

    private JComboBox xUnit, yUnit, windowType;

    private JCheckBox numericRuler;

    private GComboBoxPowerOf2 fftLength;

    private UiControlText colorGamma;

    private EventDispatcher eventDispatcher;

    public void initGui() {
        JTabbedPane tab = new JTabbedPane();

        // skala tab
        JPanel p1 = new JPanel();
        UiCartesianLayout l1 = new UiCartesianLayout(p1, 10, 5);
        l1.setPreferredCellSize(new Dimension(20, 35));
        p1.setLayout(l1);

        p1.add(new JLabel(GLanguage.translate("xUnit")), new Rectangle(0, 0, 4, 1));
        String xUnitItems[] = {
            "", "s", /* "ms", */"%", /* "Hz", */"fdHz"
        };
        xUnit = new JComboBox(xUnitItems);
        p1.add(xUnit, new Rectangle(4, 0, 4, 1));

        p1.add(new JLabel(GLanguage.translate("yUnit")), new Rectangle(0, 1, 4, 1));
        String yUnitItems[] = {
            "", "%"
        };
        yUnit = new JComboBox(yUnitItems);
        p1.add(yUnit, new Rectangle(4, 1, 4, 1));

        numericRuler = new JCheckBox(GLanguage.translate("numericRuler"));
        numericRuler.setSelected(AClipPlotter.isSkalaValuesVisible());
        p1.add(numericRuler, new Rectangle(0, 2, 8, 1));

        tab.add(p1, GLanguage.translate("ruler"));

        // spectrogram tab
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 10, 5);
        l2.setPreferredCellSize(new Dimension(20, 35));
        p2.setLayout(l2);

        l2.add(new JLabel(GLanguage.translate("fftLength")), 0, 0, 4, 1);
        fftLength = new GComboBoxPowerOf2(5, 9);
        fftLength.setSelectedExponent(9);
        l2.add(fftLength, 4, 0, 6, 1);

        l2.add(new JLabel(GLanguage.translate("window")), 0, 1, 4, 1);
        String windowTypeItem[] = {
            GLanguage.translate("rectangular"), GLanguage.translate("hamming"), GLanguage.translate("blackman"), GLanguage.translate("flattop")
        };
        windowType = new JComboBox(windowTypeItem);
        windowType.setSelectedIndex(1);
        l2.add(windowType, 4, 1, 6, 1);

        l2.add(new JLabel(GLanguage.translate("brightness")), 0, 2, 4, 1);
        colorGamma = new GControlTextA(9, true, true);
        colorGamma.setDataRange(0, 1);
        colorGamma.setData(.8);
        l2.add(colorGamma, 4, 2, 6, 1);

        tab.add(p2, GLanguage.translate("spectrogram"));

        frame.getContentPane().add(tab);
        pack();

        eventDispatcher = new EventDispatcher();
        xUnit.addActionListener(eventDispatcher);
        yUnit.addActionListener(eventDispatcher);
        numericRuler.addActionListener(eventDispatcher);
        fftLength.addActionListener(eventDispatcher);
        windowType.addActionListener(eventDispatcher);
        colorGamma.addControlListener(eventDispatcher);
    }

    public void reload() {
        fftLength.setSelectedValue(AChannelPlotterSpectrogram.getFftLength());
        // colorGamma.setData(AChannelPlotterSpectrogram.getColorGamma());
    }

    private class EventDispatcher implements ActionListener, UiControlListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == xUnit) {
                Debug.println(1, "plugin " + getName() + " [x unit] clicked");
                switch (xUnit.getSelectedIndex()) {
                case 0: // 1
                    AClipPlotter.setPlotterXUnit(AClipPlotter.UNIT_1);
                    break;

                case 1: // s
                    AClipPlotter.setPlotterXUnit(AClipPlotter.UNIT_S);
                    break;

                // case 2: //ms
                // AClipPlotter.setPlotterXUnit(AClipPlotter.UNIT_MS);
                // break;

                case 2: // %
                    AClipPlotter.setPlotterXUnit(AClipPlotter.UNIT_PERCENT);
                    break;

                // case 4: //Hz
                // AClipPlotter.setPlotterXUnit(AClipPlotter.UNIT_HZ);
                // break;

                case 3: // fdHz
                    AClipPlotter.setPlotterXUnit(AClipPlotter.UNIT_FDHZ);
                    break;

                }
                reloadFocussedClipEditor();
            } else if (e.getSource() == yUnit) {
                Debug.println(1, "plugin " + getName() + " [y unit] clicked");
                switch (yUnit.getSelectedIndex()) {
                case 0: // 1
                    AClipPlotter.setPlotterYUnit(AClipPlotter.UNIT_1);
                    break;

                case 1: // %
                    AClipPlotter.setPlotterYUnit(AClipPlotter.UNIT_PERCENT);
                    break;
                }
                reloadFocussedClipEditor();
            } else if (e.getSource() == fftLength) {
                Debug.println(1, "plugin " + getName() + " [fft length] clicked");
                AChannelPlotterSpectrogram.setFftLength(fftLength.getSelectedValue());
                reloadFocussedClipEditor();
            } else if (e.getSource() == numericRuler) {
                Debug.println(1, "plugin " + getName() + " [numeric ruler] clicked");
                AClipPlotter.setSkalaValuesVisible(numericRuler.isSelected());
                reloadFocussedClipEditor();
            } else if (e.getSource() == windowType) {
                Debug.println(1, "plugin " + getName() + " [window type] clicked");
                switch (windowType.getSelectedIndex()) {
                case 1:
                    AChannelPlotterSpectrogram.setWindowType(AChannelPlotterSpectrogram.HAMMING_WINDOW);
                    break;

                case 2:
                    AChannelPlotterSpectrogram.setWindowType(AChannelPlotterSpectrogram.BLACKMAN_WINDOW);
                    break;

                case 3:
                    AChannelPlotterSpectrogram.setWindowType(AChannelPlotterSpectrogram.FLATTOP_WINDOW);
                    break;

                default:
                    AChannelPlotterSpectrogram.setWindowType(AChannelPlotterSpectrogram.RECTANGULAR_WINDOW);
                }
                reloadFocussedClipEditor();
            }
        }

        public void onDataChanging(UiControlEvent e) {
        }

        public void onDataChanged(UiControlEvent e) {
            Debug.println(1, "plugin " + getName() + " [color gamma] changed");
            AChannelPlotterSpectrogram.setColorGamma((float) colorGamma.getData());
            reloadFocussedClipEditor();
        }

        public void onValidate(UiControlEvent e) {
        }

    }

}
