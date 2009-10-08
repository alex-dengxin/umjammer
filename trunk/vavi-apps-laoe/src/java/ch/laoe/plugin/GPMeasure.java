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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelPlotter;
import ch.laoe.clip.ALayer;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOMeasure;
import ch.laoe.operation.AOToolkit;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextSF;
import ch.laoe.ui.GControlTextY;
import ch.laoe.ui.GCookie;
import ch.laoe.ui.GLanguage;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to show measures.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 24.03.01 first draft oli4 <br>
 *          01.05.01 add cross lines oli4 <br>
 *          16.05.01 add multiple cross lines, delta measure, tabs, statistics oli4 <br>
 *          28.05.01 add locked y cursor oli4 <br>
 *          22.07.01 add locked x cursor oli4 <br>
 *          02.08.01 peak detector added oli4 <br>
 *          10.02.02 paint cursors in sample-domain (follows samples when zooming oli4
 * 
 */
public class GPMeasure extends GPluginFrame {
    public GPMeasure(GPluginHandler ph) {
        super(ph);
        initGui();
        updateCookie();
    }

    protected String getName() {
        return "measure";
    }

    public void start() {
        super.start();
        pluginHandler.setFocussedPlugin(this);
        updateCookie();
        updateDisplay();
    }

    public void reload() {
        super.reload();
        updateCookie();
        updateDisplay();
    }

    private GCookieMeasure cookie;

    private void updateCookie() {
        try {
            cookie = (GCookieMeasure) getFocussedClip().getCookies().getCookie(getName());
            if (cookie == null) {
                GCookie c = new GCookieMeasure();
                getFocussedClip().getCookies().setCookie(c, getName());
                cookie = (GCookieMeasure) c;
            }
        } catch (Exception e) {
        }
    }

    private class GCookieMeasure extends GCookie {
        public float ctrlX, ctrlY;

        public float shiftX, shiftY;

        public int ctrlChannelIndex, shiftChannelIndex;
    }

    public void mouseDragged(MouseEvent e) {
        calculateCursorPositions(e);
        updateDisplay();
    }

    public void mouseMoved(MouseEvent e) {
        calculateCursorPositions(e);
        updateDisplay();
    }

    private Stroke ctrlStroke, shiftStroke, defaultStroke;

    private static float ctrlXSample, shiftXSample;

    private boolean shiftPositivePeak, ctrlPositivePeak;

    private int ctrlChannelIndex, shiftChannelIndex;

    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
        // ctrl
        AChannelPlotter chpCtrl = getFocussedClip().getSelectedLayer().getChannel(cookie.ctrlChannelIndex).getChannelPlotter();
        int graphCtrlX = chpCtrl.sampleToGraphX(cookie.ctrlX);
        int graphCtrlY = chpCtrl.sampleToGraphY(cookie.ctrlY);

        g2d.setStroke(ctrlStroke);
        g2d.setColor(Color.black);
        g2d.drawLine(0, graphCtrlY, (int) rect.getWidth(), graphCtrlY);
        g2d.drawLine(graphCtrlX, 0, graphCtrlX, (int) rect.getHeight());

        // peak ?
        if (cursorMode.getSelectedIndex() == 2) {
            g2d.setStroke(defaultStroke);
            g2d.setColor(Color.red);
            if (ctrlPositivePeak) {
                g2d.drawLine(graphCtrlX, graphCtrlY, graphCtrlX - 4, graphCtrlY - 4);
                g2d.drawLine(graphCtrlX, graphCtrlY, graphCtrlX + 4, graphCtrlY - 4);
            } else {
                g2d.drawLine(graphCtrlX, graphCtrlY, graphCtrlX - 4, graphCtrlY + 4);
                g2d.drawLine(graphCtrlX, graphCtrlY, graphCtrlX + 4, graphCtrlY + 4);
            }
        }

        // shift
        AChannelPlotter chpShift = getFocussedClip().getSelectedLayer().getChannel(cookie.shiftChannelIndex).getChannelPlotter();
        int graphShiftX = chpShift.sampleToGraphX(cookie.shiftX);
        int graphShiftY = chpShift.sampleToGraphY(cookie.shiftY);

        g2d.setStroke(shiftStroke);
        g2d.setColor(Color.black);
        g2d.drawLine(0, graphShiftY, (int) rect.getWidth(), graphShiftY);
        g2d.drawLine(graphShiftX, 0, graphShiftX, (int) rect.getHeight());

        // peak ?
        if (cursorMode.getSelectedIndex() == 2) {
            g2d.setStroke(defaultStroke);
            g2d.setColor(Color.red);
            if (shiftPositivePeak) {
                g2d.drawLine(graphShiftX, graphShiftY, graphShiftX - 4, graphShiftY - 4);
                g2d.drawLine(graphShiftX, graphShiftY, graphShiftX + 4, graphShiftY - 4);
            } else {
                g2d.drawLine(graphShiftX, graphShiftY, graphShiftX - 4, graphShiftY + 4);
                g2d.drawLine(graphShiftX, graphShiftY, graphShiftX + 4, graphShiftY + 4);
            }
        }
    }

    private void calculateCursorPositions(MouseEvent e) {
        if (frame.isVisible() && (getFocussedClip() != null)) {
            // search pressed channel
            ALayer l = getFocussedClip().getSelectedLayer();
            int i = l.getLayerPlotter().getInsideChannelIndex(e.getPoint());

            // valid channel ?
            if (i >= 0) {
                AChannel ch = l.getChannel(i);
                AChannelPlotter cp = ch.getChannelPlotter();

                float x = cp.graphToSampleX(e.getPoint().x);
                float y = cp.graphToSampleY(e.getPoint().y);
                boolean positivePeak = true;
                // cursor mode ?
                switch (cursorMode.getSelectedIndex()) {
                case 1: // locked ?
                    x = l.getChannel(i).limitIndex((int) x);
                    y = l.getChannel(i).getSample((int) x);
                    break;

                case 2: // peak ?
                    // peak detect...
                    int xPeakRange = (int) (cp.graphToSampleX(e.getPoint().x + 15) - x);
                    int peakIndex;

                    // positive peaks ?
                    y = l.getChannel(i).getSample((int) x);
                    if (cp.graphToSampleY(e.getPoint().y) > y) {
                        peakIndex = AOToolkit.getNearestPositivePeakIndex(ch.sample, (int) x, xPeakRange);
                        positivePeak = true;
                    }
                    // negative peak ?
                    else {
                        peakIndex = AOToolkit.getNearestNegativePeakIndex(ch.sample, (int) x, xPeakRange);
                        positivePeak = false;
                    }

                    x = l.getChannel(i).limitIndex(peakIndex);
                    y = l.getChannel(i).getSample((int) x);
                    break;
                }

                if (isShiftKey(e)) {
                    shiftXSample = x;
                    cookie.shiftX = x;
                    cookie.shiftY = y;
                    shiftPositivePeak = positivePeak;
                    cookie.shiftChannelIndex = i;
                }
                if (isCtrlKey(e)) {
                    ctrlXSample = x;
                    cookie.ctrlX = x;
                    cookie.ctrlY = y;
                    ctrlPositivePeak = positivePeak;
                    cookie.ctrlChannelIndex = i;
                }
                repaintFocussedClipEditor();
            }
        }
    }

    private void updateDisplay() {
        try {
            // update shift...
            uiShiftX.setData(cookie.shiftX);
            uiShiftY.setData(cookie.shiftY);

            // update ctrl...
            uiCtrlX.setData(cookie.ctrlX);
            uiCtrlY.setData(cookie.ctrlY);

            // update delta...
            uiDeltaX.setData(uiCtrlX.getData() - uiShiftX.getData());
            uiDeltaY.setData(uiCtrlY.getData() - uiShiftY.getData());
        } catch (Exception e) {
        }
    }

    // ********************* frame **********************

    private JComboBox cursorMode;

    private UiControlText uiCtrlX, uiCtrlY;

    private UiControlText uiShiftX, uiShiftY;

    private UiControlText uiDeltaX, uiDeltaY;

    private UiControlText mean, rms, stdDev;

    private UiControlText min, max, clippedSamples;

    private JButton updateStat, updateExtrema;

    private EventDispatcher eventDispatcher;

    public void initGui() {
        // strokes
        float ctrlDash[] = {
            3.f, 5.f
        };
        ctrlStroke = new BasicStroke(1.f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.f, ctrlDash, 0.f);
        float shiftDash[] = {
            10.f, 3.f
        };
        shiftStroke = new BasicStroke(1.f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.f, shiftDash, 0.f);
        defaultStroke = new BasicStroke();

        JTabbedPane tabbedPane = new JTabbedPane();

        // cursor tab
        JPanel pCursor = new JPanel();
        UiCartesianLayout lCursor = new UiCartesianLayout(pCursor, 9, 4);
        lCursor.setPreferredCellSize(new Dimension(30, 35));
        pCursor.setLayout(lCursor);

        lCursor.add(new JLabel(GLanguage.translate("ctrl")), 0, 1, 4, 1);
        lCursor.add(new JLabel(GLanguage.translate("shift")), 0, 2, 4, 1);
        lCursor.add(new JLabel(GLanguage.translate("delta")), 0, 3, 4, 1);

        lCursor.add(new JLabel(GLanguage.translate("x")), 1, 0, 4, 1);

        uiCtrlX = new GControlTextSF(getMain(), 9, false, true);
        uiCtrlX.setEditable(false);
        uiCtrlX.setDataRange(-1e9, 1e9);
        uiCtrlX.setData(1);
        uiCtrlX.getFormat().setMaximumFractionDigits(3);
        lCursor.add(uiCtrlX, 1, 1, 4, 1);

        uiShiftX = new GControlTextSF(getMain(), 9, false, true);
        uiShiftX.setEditable(false);
        uiShiftX.setDataRange(-1e9, 1e9);
        uiShiftX.setData(1);
        uiShiftX.getFormat().setMaximumFractionDigits(3);
        lCursor.add(uiShiftX, 1, 2, 4, 1);

        uiDeltaX = new GControlTextSF(getMain(), 9, false, true);
        uiDeltaX.setEditable(false);
        uiDeltaX.setDataRange(-1e9, 1e9);
        uiDeltaX.setData(1);
        uiDeltaX.getFormat().setMaximumFractionDigits(3);
        lCursor.add(uiDeltaX, 1, 3, 4, 1);

        lCursor.add(new JLabel(GLanguage.translate("y")), 5, 0, 1, 1);

        String cursorModeItems[] = {
            GLanguage.translate("free"), GLanguage.translate("locked"), GLanguage.translate("peak")
        };

        cursorMode = new JComboBox(cursorModeItems);
        cursorMode.setSelectedIndex(1);
        lCursor.add(cursorMode, 6, 0, 3, 1);

        uiCtrlY = new GControlTextY(getMain(), 9, false, true);
        uiCtrlY.setEditable(false);
        uiCtrlY.setDataRange(-1e9, 1e9);
        uiCtrlY.setData(1);
        lCursor.add(uiCtrlY, 5, 1, 4, 1);

        uiShiftY = new GControlTextY(getMain(), 9, false, true);
        uiShiftY.setEditable(false);
        uiShiftY.setDataRange(-1e9, 1e9);
        uiShiftY.setData(1);
        lCursor.add(uiShiftY, 5, 2, 4, 1);

        uiDeltaY = new GControlTextY(getMain(), 9, false, true);
        uiDeltaY.setEditable(false);
        uiDeltaY.setDataRange(-1e9, 1e9);
        uiDeltaY.setData(1);
        lCursor.add(uiDeltaY, 5, 3, 4, 1);

        tabbedPane.add(GLanguage.translate("cursor"), pCursor);

        // statistics tab
        JPanel pStat = new JPanel();
        UiCartesianLayout lStat = new UiCartesianLayout(pStat, 9, 4);
        lStat.setPreferredCellSize(new Dimension(30, 35));
        pStat.setLayout(lStat);

        lStat.add(new JLabel(GLanguage.translate("mean")), 0, 0, 4, 1);

        mean = new GControlTextY(getMain(), 9, false, true);
        mean.setEditable(false);
        mean.setDataRange(-1e9, 1e9);
        mean.setData(1);
        mean.getFormat().setMaximumFractionDigits(3);
        lStat.add(mean, 4, 0, 5, 1);

        lStat.add(new JLabel(GLanguage.translate("rms")), 0, 1, 4, 1);

        rms = new GControlTextY(getMain(), 9, false, true);
        rms.setEditable(false);
        rms.setDataRange(-1e9, 1e9);
        rms.setData(1);
        rms.getFormat().setMaximumFractionDigits(3);
        lStat.add(rms, 4, 1, 5, 1);

        lStat.add(new JLabel(GLanguage.translate("standardDeviation")), 0, 2, 4, 1);

        stdDev = new GControlTextY(getMain(), 9, false, true);
        stdDev.setEditable(false);
        stdDev.setDataRange(-1e9, 1e9);
        stdDev.setData(1);
        stdDev.getFormat().setMaximumFractionDigits(3);
        lStat.add(stdDev, 4, 2, 5, 1);

        updateStat = new JButton(GLanguage.translate("update"));
        lStat.add(updateStat, 3, 3, 4, 1);

        tabbedPane.add(GLanguage.translate("statistics"), pStat);

        // extrema tab
        JPanel pExtrema = new JPanel();
        UiCartesianLayout lExtrema = new UiCartesianLayout(pExtrema, 9, 4);
        lExtrema.setPreferredCellSize(new Dimension(30, 35));
        pExtrema.setLayout(lExtrema);

        lExtrema.add(new JLabel(GLanguage.translate("min")), 0, 0, 4, 1);

        min = new GControlTextY(getMain(), 9, false, true);
        min.setEditable(false);
        min.setDataRange(-1e9, 1e9);
        min.setData(1);
        min.getFormat().setMaximumFractionDigits(3);
        lExtrema.add(min, 4, 0, 5, 1);

        lExtrema.add(new JLabel(GLanguage.translate("max")), 0, 1, 4, 1);

        max = new GControlTextY(getMain(), 9, false, true);
        max.setEditable(false);
        max.setDataRange(-1e9, 1e9);
        max.setData(1);
        max.getFormat().setMaximumFractionDigits(3);
        lExtrema.add(max, 4, 1, 5, 1);

        lExtrema.add(new JLabel(GLanguage.translate("clippedSamples")), 0, 2, 4, 1);

        clippedSamples = new UiControlText(9, false, true);
        clippedSamples.setEditable(false);
        clippedSamples.setDataRange(0, 1e9);
        clippedSamples.setData(0);
        lExtrema.add(clippedSamples, 4, 2, 5, 1);

        updateExtrema = new JButton(GLanguage.translate("update"));
        lExtrema.add(updateExtrema, 3, 3, 4, 1);

        tabbedPane.add(GLanguage.translate("extrema"), pExtrema);

        frame.getContentPane().add(tabbedPane);
        pack();

        eventDispatcher = new EventDispatcher();
        updateStat.addActionListener(eventDispatcher);
        updateExtrema.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Debug.println(1, "plugin " + getName() + " [update statistics] clicked");
            onUpdateStatistics();
        }
    }

    private void onUpdateStatistics() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        AOMeasure o = new AOMeasure(getFocussedClip().getSampleWidth());
        ls.operateEachChannel(o);

        mean.setData(o.getMean());
        rms.setData(o.getRms());
        stdDev.setData(o.getStandardDeviation());
        min.setData(o.getMin());
        max.setData(o.getMax());
        clippedSamples.setData(o.getNumberOfClippedSamples());
    }

    // get cursor values

    public static float getLowerCursor() {
        return Math.min(ctrlXSample, shiftXSample);
    }

    public static float getHigherCursor() {
        return Math.max(ctrlXSample, shiftXSample);
    }

}
