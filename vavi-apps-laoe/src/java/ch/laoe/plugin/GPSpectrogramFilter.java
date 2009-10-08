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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.ALayer;
import ch.laoe.operation.AOToolkit;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GComboBoxPowerOf2;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GControlTextY;
import ch.laoe.ui.GCookie;
import ch.laoe.ui.GEditableArea;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to define and filter an area of multiple rectangular shapes.
 * 
 * @autor: olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 10.03.02 first draft oli4 <br>
 *          13.03.02 windowed overlap introduced oli4 <br>
 *          21.03.02 more selection-features like copy, paste, all, none oli4 <br>
 *          16.06.02 use channelstack as channel-chooser oli4 <br>
 */
public class GPSpectrogramFilter extends GPluginFrame {
    public GPSpectrogramFilter(GPluginHandler ph) {
        super(ph);
        initGui();
        updateCookie();
    }

    protected String getName() {
        return "spectrogramFilter";
    }

    public void start() {
        super.start();
        pluginHandler.setFocussedPlugin(this);
        updateCookie();
        reloadGui();
    }

    public void reload() {
        super.reload();
        updateCookie();
        reloadGui();
    }

    // cookie
    private GEditableArea area;

    private AChannel actualChannel;

    private void updateCookie() {
        try {
            ALayer l = getSelectedLayer();
            for (int i = 0; i < l.getNumberOfChannels(); i++) {
                if (l.getChannel(i).getCookies().getCookie(getName()) == null) {
                    AChannel ch = l.getChannel(i);
                    ch.getCookies().setCookie(new GCookieSpectrogramFilter(ch), getName());
                }
            }
        } catch (Exception e) {
        }
    }

    private void updateActualArea(MouseEvent e) {
        try {
            AChannel ch = getSelectedLayer().getChannel(e.getPoint());
            if (ch != null) {
                actualChannel = getSelectedLayer().getChannel(e.getPoint());
                area = ((GCookieSpectrogramFilter) actualChannel.getCookies().getCookie(getName())).area;
            }
        } catch (Exception exc) {
            Debug.printStackTrace(5, exc);
        }
    }

    private void updateActualArea() {
        try {
            actualChannel = getSelectedLayer().getSelectedChannel();
            area = ((GCookieSpectrogramFilter) actualChannel.getCookies().getCookie(getName())).area;
        } catch (Exception exc) {
            Debug.printStackTrace(5, exc);
        }
    }

    private class GCookieSpectrogramFilter extends GCookie {
        public GCookieSpectrogramFilter(AChannel ch) {
            area = new GEditableArea();
            area.setChannel(ch);
        }

        public GEditableArea area;
    }

    /**
     * mouse events
     */

    public void mousePressed(MouseEvent e) {
        try {
            updateActualArea(e);
            area.mousePressed(e);

            reloadGui();
            repaintFocussedClipEditor();
        } catch (Exception exc) {
            Debug.printStackTrace(5, exc);
        }
    }

    public void mouseReleased(MouseEvent e) {
        area.mouseReleased(e);

        reloadGui();
        repaintFocussedClipEditor();
    }

    public void mouseMoved(MouseEvent e) {
        try {
            updateActualArea(e);
            updateCoordinates(e);
            switch (drawMode.getSelectedIndex()) {
            case 0:
                area.setMode(GEditableArea.RECTANGLE);
                break;

            case 1:
                area.setMode(GEditableArea.LINE);
                break;
            }
            area.setBrushSize((int) brushSize.getData());
            area.mouseMoved(e);

            reloadGui();
            repaintFocussedClipEditor();
        } catch (Exception exc) {
        }
    }

    public void mouseDragged(MouseEvent e) {
        updateCoordinates(e);
        area.mouseDragged(e);

        reloadGui();
        repaintFocussedClipEditor();
    }

    public void mouseClicked(MouseEvent e) {
        area.mouseClicked(e);

        reloadGui();
        repaintFocussedClipEditor();
    }

    public void mouseEntered(MouseEvent e) {
        updateActualArea(e);
        area.mouseEntered(e);

        reloadGui();
        repaintFocussedClipEditor();
    }

    private void updateCoordinates(MouseEvent e) {
        xNumeric.setData(area.getCurrentX());
        yNumeric.setData(area.getCurrentY());
    }

    /**
     * graphics
     */

    private Stroke segmentSroke;

    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
        try {
            ALayer l = getSelectedLayer();
            for (int i = 0; i < l.getNumberOfChannels(); i++) {
                ((GCookieSpectrogramFilter) l.getChannel(i).getCookies().getCookie(getName())).area.paintOntoClip(g2d, rect);
            }
        } catch (Exception exc) {
        }
    }

    /**
     * GUI
     */

    private JButton all, none, apply;

    private JButton copy, paste;

    private UiControlText xNumeric, yNumeric;

    private UiControlText amplification, brushSize, threshold;

    private GComboBoxPowerOf2 fftLength;

    private JCheckBox inversed;

    private JComboBox drawMode, filterMode;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        // panel
        JPanel p = new JPanel();
        UiCartesianLayout l = new UiCartesianLayout(p, 10, 9);
        l.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(l);

        // above tab
        l.add(new JLabel(GLanguage.translate("x")), 0, 0, 5, 1);
        xNumeric = new GControlTextX(getMain(), 7, false, true);
        xNumeric.setDataRange(-1e9, 1e9);
        xNumeric.setData(0);
        xNumeric.setEditable(false);
        l.add(xNumeric, 0, 1, 5, 1);

        l.add(new JLabel(GLanguage.translate("y")), 5, 0, 5, 1);
        yNumeric = new GControlTextY(getMain(), 7, false, true);
        yNumeric.setDataRange(-1e9, 1e9);
        yNumeric.setData(0);
        yNumeric.setEditable(false);
        l.add(yNumeric, 5, 1, 5, 1);

        JTabbedPane tab = new JTabbedPane();

        // selection tab
        JPanel p1 = new JPanel();
        UiCartesianLayout l1 = new UiCartesianLayout(p1, 10, 6);
        l1.setPreferredCellSize(new Dimension(25, 35));
        p1.setLayout(l1);

        l1.add(new JLabel(GLanguage.translate("drawMode")), 0, 0, 5, 1);
        String drawModeItems[] = {
            GLanguage.translate("rectangle"), GLanguage.translate("line")
        };
        drawMode = new JComboBox(drawModeItems);
        drawMode.setSelectedIndex(0);
        l1.add(drawMode, 5, 0, 5, 1);

        l1.add(new JLabel(GLanguage.translate("brushSize")), 0, 1, 5, 1);
        brushSize = new UiControlText(9, true, true);
        brushSize.setDataRange(2, 50);
        brushSize.setData(4);
        l1.add(brushSize, 5, 1, 5, 1);

        inversed = new JCheckBox(GLanguage.translate("inversed"));
        l1.add(inversed, 0, 2, 7, 1);

        all = new JButton(GLanguage.translate("all"));
        l1.add(all, 1, 3, 4, 1);

        none = new JButton(GLanguage.translate("none"));
        l1.add(none, 5, 3, 4, 1);

        copy = new JButton(GLanguage.translate("copy"));
        l1.add(copy, 1, 4, 4, 1);

        paste = new JButton(GLanguage.translate("paste"));
        l1.add(paste, 5, 4, 4, 1);

        tab.add(p1, GLanguage.translate("selection"));

        // filter tab
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 10, 6);
        l2.setPreferredCellSize(new Dimension(25, 35));
        p2.setLayout(l2);

        l2.add(new JLabel(GLanguage.translate("filterMode")), 0, 0, 5, 1);
        String filterModeItems[] = {
            GLanguage.translate("magnitude"), GLanguage.translate("aboveThreshold"), GLanguage.translate("belowThreshold")
        };
        filterMode = new JComboBox(filterModeItems);
        filterMode.setSelectedIndex(0);
        l2.add(filterMode, 5, 0, 5, 1);

        l2.add(new JLabel(GLanguage.translate("amplification")), 0, 1, 5, 1);
        amplification = new GControlTextA(9, true, true);
        amplification.setDataRange(0, 10);
        amplification.setData(0);
        l2.add(amplification, 5, 1, 5, 1);

        l2.add(new JLabel(GLanguage.translate("threshold")), 0, 2, 5, 1);
        threshold = new GControlTextY(getMain(), 9, true, true);
        threshold.setDataRange(0, 1e9);
        threshold.setData(50);
        l2.add(threshold, 5, 2, 5, 1);

        l2.add(new JLabel(GLanguage.translate("fftLength")), 0, 3, 5, 1);
        fftLength = new GComboBoxPowerOf2(6, 14);
        fftLength.setSelectedExponent(11);
        l2.add(fftLength, 5, 3, 5, 1);

        apply = new JButton(GLanguage.translate("apply"));
        l2.add(apply, 3, 4, 4, 1);

        tab.add(p2, GLanguage.translate("filter"));
        l.add(tab, 0, 2, 10, 7);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        all.addActionListener(eventDispatcher);
        none.addActionListener(eventDispatcher);
        copy.addActionListener(eventDispatcher);
        paste.addActionListener(eventDispatcher);
        apply.addActionListener(eventDispatcher);
        drawMode.addActionListener(eventDispatcher);
        filterMode.addActionListener(eventDispatcher);

        updateComponents();
    }

    private void reloadGui() {
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == all) {
                Debug.println(1, "plugin " + getName() + " [all] clicked");
                onSelectAll();
            } else if (e.getSource() == copy) {
                Debug.println(1, "plugin " + getName() + " [copy] clicked");
                onCopy();
            } else if (e.getSource() == paste) {
                Debug.println(1, "plugin " + getName() + " [paste] clicked");
                onPaste();
            } else if (e.getSource() == none) {
                Debug.println(1, "plugin " + getName() + " [none] clicked");
                onSelectNone();
            } else if (e.getSource() == apply) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply] clicked");
                onApply();
                GProgressViewer.setProgress(100);
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == drawMode) {
                Debug.println(1, "plugin " + getName() + " [draw mode] clicked");
                updateComponents();
            } else if (e.getSource() == filterMode) {
                Debug.println(1, "plugin " + getName() + " [filter mode] clicked");
                updateComponents();
            }
        }
    }

    private void updateComponents() {
        brushSize.setEnabled(drawMode.getSelectedIndex() == 1);
        threshold.setEnabled(filterMode.getSelectedIndex() != 0);
    }

    // private AChannel clipBoardChannel;
    private GEditableArea clipBoardArea;

    private void onCopy() {
        updateActualArea();
        // clipBoardChannel = actualChannel;
        clipBoardArea = new GEditableArea(area);
        repaintFocussedClipEditor();
    }

    private void onPaste() {
        if (clipBoardArea != null) {
            GEditableArea a = new GEditableArea(clipBoardArea);
            updateActualArea();
            a.setChannel(actualChannel);
            try {
                area = a;
                ((GCookieSpectrogramFilter) actualChannel.getCookies().getCookie(getName())).area = a;
            } catch (Exception exc) {
            }
            repaintFocussedClipEditor();
        }
    }

    private void onSelectAll() {
        // updateCookie();
        updateActualArea();
        area.full();
        repaintFocussedClipEditor();
    }

    private void onSelectNone() {
        // updateCookie();
        updateActualArea();
        area.clear();
        repaintFocussedClipEditor();
    }

    private void onApply() {
        GProgressViewer.entrySubProgress();
        ALayer l = getSelectedLayer();
        for (int i = 0; i < l.getNumberOfChannels(); i++) {
            AChannel ch = l.getChannel(i);
            GProgressViewer.setProgress((i + 1) * 100 / l.getNumberOfChannels());

            GEditableArea area = ((GCookieSpectrogramFilter) l.getChannel(i).getCookies().getCookie(getName())).area;
            int f = MAGNITUDE;
            switch (filterMode.getSelectedIndex()) {
            case 0:
                f = MAGNITUDE;
                break;

            case 1:
                f = MAGNITUDE_ABOVE_THRESHOLD;
                break;

            case 2:
                f = MAGNITUDE_BELOW_THRESHOLD;
                break;
            }
            filter(ch, area, inversed.isSelected(), f);

            reloadFocussedClipEditor();
        }
        GProgressViewer.exitSubProgress();
    }

    // ******************* filter operation *********************

    // filter mode
    private static final int MAGNITUDE = 1;

    private static final int MAGNITUDE_ABOVE_THRESHOLD = 2;

    private static final int MAGNITUDE_BELOW_THRESHOLD = 3;

    // spectrum-strip (column) processing

    /**
     * performs magnitude modification, returns true if modified
     */
    private boolean performMagnitude(GEditableArea area, boolean inversed, float re[], float im[], int x, float samplerate) {
        boolean changed = false;
        float a = (float) amplification.getData();

        for (int j = 0; j < re.length / 2; j++) {
            boolean selected = area.isInside(x, j * samplerate / re.length);
            if (selected ^ inversed) {
                re[j] *= a;
                im[j] *= a;
                changed = true;
            }
        }
        return changed;
    }

    /**
     * performs magnitude modification, but only if above thershold, returns true if modified
     */
    private boolean performMagnitudeAboveThreshold(GEditableArea area, boolean inversed, float re[], float im[], int x, float samplerate) {
        boolean changed = false;
        float a = (float) amplification.getData();
        float t = (float) threshold.getData();

        for (int j = 0; j < re.length / 2; j++) {
            boolean selected = area.isInside(x, j * samplerate / re.length);
            if (selected ^ inversed) {
                if (AOToolkit.cartesianToMagnitude(re[j], im[j]) > t) {
                    re[j] *= a;
                    im[j] *= a;
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * performs magnitude modification, but only if below thershold, returns true if modified
     */
    private boolean performMagnitudeBelowThreshold(GEditableArea area, boolean inversed, float re[], float im[], int x, float samplerate) {
        boolean changed = false;
        float a = (float) amplification.getData();
        float t = (float) threshold.getData();

        for (int j = 0; j < re.length / 2; j++) {
            boolean selected = area.isInside(x, j * samplerate / re.length);
            if (selected ^ inversed) {
                if (AOToolkit.cartesianToMagnitude(re[j], im[j]) < t) {
                    re[j] *= a;
                    im[j] *= a;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private void filter(AChannel ch, GEditableArea area, boolean inversed, int filterMode) {
        try {
            if (area.getNumberOfRectangles() > 0) {
                float samplerate = getFocussedClip().getSampleRate();
                float sample[] = ch.sample;
                int fftLength = this.fftLength.getSelectedValue();

                // short time FFT
                float re[] = new float[fftLength];
                float im[] = new float[fftLength];
                int start, end;

                // mark changed channels...
                ch.changeId();

                GProgressViewer.entrySubProgress();
                if (inversed) {
                    start = 0;
                    end = sample.length;
                } else {
                    start = (int) area.getXMin();
                    end = (int) area.getXMax();
                }
                start = Math.max(0, start);
                end = Math.min(end, sample.length);

                float tmp[] = new float[end - start];
                int overlap = 3;

                for (int i = start - fftLength; i < end; i += fftLength / overlap) {
                    GProgressViewer.setProgress((i + 1 - start) * 100 / (end - start));

                    // short time FFT
                    for (int j = 0; j < fftLength; j++) {
                        if ((i + j >= 0) && (i + j < sample.length)) {
                            re[j] = sample[i + j];
                        } else {
                            re[j] = 0;
                        }
                        im[j] = 0;
                    }
                    AOToolkit.applyBlackmanWindow(re, fftLength);
                    AOToolkit.complexFft(re, im);

                    // filter
                    boolean changed = false;

                    switch (filterMode) {
                    case MAGNITUDE:
                        changed = performMagnitude(area, inversed, re, im, i, samplerate);
                        break;

                    case MAGNITUDE_ABOVE_THRESHOLD:
                        changed = performMagnitudeAboveThreshold(area, inversed, re, im, i, samplerate);
                        break;

                    case MAGNITUDE_BELOW_THRESHOLD:
                        changed = performMagnitudeBelowThreshold(area, inversed, re, im, i, samplerate);
                        break;
                    }
Debug.println(5, "changed: " + changed);
                    // short time IFFT
                    AOToolkit.complexIfft(re, im);
                    for (int j = 0; j < fftLength; j++) {
                        int jj = i - start + j;
                        if ((jj >= 0) && (jj < tmp.length)) {
                            tmp[jj] += re[j];
                        }
                    }
                }

                for (int i = start; i < end; i++) {
                    sample[i] = tmp[i - start];
                }
                GProgressViewer.exitSubProgress();
            }
        } catch (Exception e) {
            Debug.printStackTrace(5, e);
        }
    }
}
