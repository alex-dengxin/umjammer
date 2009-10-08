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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelPlotter;
import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipPlotter;
import ch.laoe.clip.ALayer;
import ch.laoe.clip.ALayerPlotter;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextSF;
import ch.laoe.ui.GControlTextY;
import ch.laoe.ui.GLanguage;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlEvent;
import ch.oli4.ui.UiControlListener;
import ch.oli4.ui.UiControlText;


/**
 * plugin to zoom the clip-view.
 *  mouse action table:
 *   ------------------------------------------------------------
 * press/drag/release click double-click
 *  ------------------------------------------------------------
 *   no key translate xy - -
 * shift zoom in x zoom out x autoscale x shift&ctrl zoom in xy zoom out xy autoscale xy ctrl zoom in y zoom out y autoscale y
 * ------------------------------------------------------------
 * 
 * @target JDK 1.3
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @version 30.08.00 erster Entwurf oli4 <br>
 *          10.11.00 better zoom support of plotters is used here oli4 <br>
 *          30.01.01 extended zoom-functions controlled by mouse oli4 <br>
 *          24.03.01 individual layers / common clip zoom oli4 <br>
 *          14.04.01 cursors added oli4 <br>
 *          09.12.01 autoscale-modes and selectable x/y added oli4 <br>
 *          10.02.02 add different y autoscale-modes oli4 <br>
 */
public class GPZoom extends GPluginFrame {
    public GPZoom(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "zoom";
    }

    public JMenuItem createMenuItem() {
        return super.createMenuItem(KeyEvent.VK_1);
    }

    public void start() {
        super.start();
        pluginHandler.setFocussedPlugin(this);
    }

    public void reload() {
        super.reload();
        if (pluginHandler.getFocussedClipEditor() != null) {
            reloadZoomFrame();
        }
    }

    // **************** zoom functionality by mouse ****************

    private BasicStroke dashedStroke, normalStroke;

    private AChannelPlotter mouseChannelPlotter;

    private int mouseZoomX1, mouseZoomY1, mouseZoomX2, mouseZoomY2;

    private boolean shiftActive, ctrlActive;

    private boolean mouseDown;

    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
        if (mouseDown) {
            int xMin = Math.min(mouseZoomX1, mouseZoomX2);
            int xDelta = Math.abs(mouseZoomX2 - mouseZoomX1);
            int yMin = Math.min(mouseZoomY1, mouseZoomY2);
            int yDelta = Math.abs(mouseZoomY2 - mouseZoomY1);

            if (shiftActive && ctrlActive) {
                // draw free rectangle
                g2d.setColor(Color.white);
                g2d.setStroke(normalStroke);
                g2d.drawRect(xMin, yMin, xDelta, yDelta);
                g2d.setColor(Color.black);
                g2d.setStroke(dashedStroke);
                g2d.drawRect(xMin, yMin, xDelta, yDelta);
            } else if (shiftActive) {
                // draw x-variable rectangle
                g2d.setColor(Color.white);
                g2d.setStroke(normalStroke);
                g2d.drawRect(xMin, 0, xDelta, rect.height - 1);
                g2d.setColor(Color.black);
                g2d.setStroke(dashedStroke);
                g2d.drawRect(xMin, 0, xDelta, rect.height - 1);
            } else if (ctrlActive) {
                // draw y-variable rectangle
                g2d.setColor(Color.white);
                g2d.setStroke(normalStroke);
                g2d.drawRect(0, yMin, rect.width - 1, yDelta);
                g2d.setColor(Color.black);
                g2d.setStroke(dashedStroke);
                g2d.drawRect(0, yMin, rect.width - 1, yDelta);
            } else {
                // draw translation line
                g2d.setColor(Color.white);
                g2d.setStroke(normalStroke);
                g2d.drawLine(mouseZoomX1, mouseZoomY1, mouseZoomX2, mouseZoomY2);
                g2d.setColor(Color.black);
                g2d.setStroke(dashedStroke);
                g2d.drawLine(mouseZoomX1, mouseZoomY1, mouseZoomX2, mouseZoomY2);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        Debug.println(5, "mouse pressed: start zoom functionality");
        mouseDown = true;
        // store start point
        mouseZoomX1 = e.getPoint().x;
        mouseZoomY1 = e.getPoint().y;
        mouseZoomX2 = mouseZoomX1;
        mouseZoomY2 = mouseZoomY1;
        // store keys
        shiftActive = isShiftKey(e);
        ctrlActive = isCtrlKey(e);
        // search pressed channel
        ALayer l = getFocussedClip().getSelectedLayer();
        int i = l.getLayerPlotter().getInsideChannelIndex(e.getPoint());
        mouseChannelPlotter = l.getChannel(i).getChannelPlotter();
    }

    public void mouseReleased(MouseEvent e) {
        mouseDown = false;

        // range
        float sx1 = mouseChannelPlotter.graphToSampleX(mouseZoomX1);
        float sx2 = mouseChannelPlotter.graphToSampleX(mouseZoomX2);
        float sxMin = Math.min(sx1, sx2);
        float sxMax = Math.max(sx1, sx2);
        int deltaX = mouseZoomX2 - mouseZoomX1;

        float sy1 = mouseChannelPlotter.graphToSampleY(mouseZoomY1);
        float sy2 = mouseChannelPlotter.graphToSampleY(mouseZoomY2);
        float syMin = Math.min(sy1, sy2);
        float syMax = Math.max(sy1, sy2);
        int deltaY = mouseZoomY2 - mouseZoomY1;
        // System.out.println("xmin="+sxMin+" xmax="+sxMax+" ymin="+syMin+" ymax="+syMax);

        // zoom
        AClipPlotter cp = getFocussedClip().getClipPlotter();
        ALayerPlotter lp = getFocussedClip().getSelectedLayer().getLayerPlotter();

        // no key pressed ?
        if (!shiftActive && !ctrlActive) {
            // translate offset
            Debug.println(5, "mouse released: move xy offset");
            // individual ?
            if (individualY.isSelected()) {
                lp.translateYOffset(sy1 - sy2);
            }
            // common ?
            else {
                cp.translateYOffset(sy1 - sy2);
            }
            cp.translateXOffset(sx1 - sx2);
        }
        // minimum one key pressed ?
        else {
            if (shiftActive) {
                if (Math.abs(deltaX) > 0) {
                    // change x range
                    Debug.println(5, "mouse released: zoom x into rectangle");
                    cp.setXRange(sxMin, sxMax - sxMin);
                }
            }
            if (ctrlActive) {
                if (Math.abs(deltaY) > 0) {
                    // change y range
                    Debug.println(5, "mouse released: zoom y into rectangle");
                    // individual ?
                    if (individualY.isSelected()) {
                        lp.setYRange(syMin, syMax - syMin);
                    }
                    // common ?
                    else {
                        cp.setYRange(syMin, syMax - syMin);
                    }
                }
            }
        }
        reloadZoomFrame();
        reloadFocussedClipEditor();
    }

    public void mouseClicked(MouseEvent e) {
        Debug.println(5, "zoom out on click");
        AClipPlotter cp = getFocussedClip().getClipPlotter();
        ALayerPlotter lp = getFocussedClip().getSelectedLayer().getLayerPlotter();

        // click ?
        if (e.getClickCount() == 1) {
            if (isShiftKey(e)) {
                // zoom out x
                Debug.println(5, "mouse clicked: zoom out x");
                cp.zoomX(.5f);
            }
            if (isCtrlKey(e)) {
                // zoom out y
                Debug.println(5, "mouse clicked: zoom out y");
                // individual ?
                if (individualY.isSelected()) {
                    lp.zoomY(.5f);
                }
                // common ?
                else {
                    cp.zoomY(.5f);
                }
            }
        }
        // double-click ?
        else if (e.getClickCount() == 2) {
            if (isShiftKey(e)) {
                // autoscale x
                Debug.println(5, "mouse double-clicked: autoscale visible x");
                cp.autoScaleX();
            }
            if (isCtrlKey(e)) {
                // autoscale y
                Debug.println(5, "mouse double-clicked: autoscale visible y");
                // individual ?
                if (individualY.isSelected()) {
                    lp.autoScaleY();
                }
                // common ?
                else {
                    cp.autoScaleY();
                }
            }
        }
        reloadZoomFrame();
        reloadFocussedClipEditor();
    }

    public void mouseEntered(MouseEvent e) {
        ((Component) e.getSource()).setCursor(actualCursor);
    }

    public void mouseDragged(MouseEvent e) {
        // store actual end point
        mouseZoomX2 = e.getPoint().x;
        mouseZoomY2 = e.getPoint().y;
        // paint zoom rectangle
        repaintFocussedClipEditor();
    }

    public void mouseMoved(MouseEvent e) {
        Cursor c;
        // choose cursor...
        if (isShiftKey(e)) {
            if (isCtrlKey(e)) {
                c = xyCursor;
            } else {
                c = xCursor;
            }
        } else {
            if (isCtrlKey(e)) {
                c = yCursor;
            } else {
                c = moveCursor;
            }
        }

        // change detector...
        if (c != actualCursor) {
            actualCursor = c;
            ((Component) e.getSource()).setCursor(actualCursor);
        }
    }

    // ********************* frame **********************

    private UiControlText scaleX, scaleY, offsetX, offsetY;

    private JButton autoScale;

    private JCheckBox individualY, autoX, autoY;

    private JComboBox xAutoMode, yAutoMode;

    private EventDispatcher eventDispatcher;

    private Cursor moveCursor, xCursor, yCursor, xyCursor, actualCursor;

    public void initGui() {
        moveCursor = createCustomCursor("zoomMoveCursor");
        xCursor = createCustomCursor("zoomXCursor");
        yCursor = createCustomCursor("zoomYCursor");
        xyCursor = createCustomCursor("zoomXYCursor");
        actualCursor = null;

        float dash[] = {
            4.f, 4.f
        };
        dashedStroke = new BasicStroke(1.f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.f, dash, 0.f);
        normalStroke = new BasicStroke();

        JPanel p = new JPanel();
        frame.getContentPane().add(p);
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 5);
        cl.setPreferredCellSize(new Dimension(35, 35));
        p.setLayout(cl);

        autoX = new JCheckBox(GLanguage.translate("x"));
        autoX.setSelected(true);
        cl.add(autoX, 2, 0, 4, 1);

        autoY = new JCheckBox(GLanguage.translate("y"));
        autoY.setSelected(true);
        cl.add(autoY, 6, 0, 4, 1);

        cl.add(new JLabel(GLanguage.translate("offset")), 0, 1, 2, 1);
        offsetX = new GControlTextSF(getMain(), 9, true, true);
        offsetX.setDataRange(-1e9, 1e9);
        offsetX.setData(1);
        cl.add(offsetX, 2, 1, 4, 1);

        offsetY = new GControlTextY(getMain(), 9, true, true);
        offsetY.setDataRange(-1e9, 1e9);
        offsetY.setData(1);
        cl.add(offsetY, 6, 1, 4, 1);

        cl.add(new JLabel(GLanguage.translate("range")), 0, 2, 2, 1);
        scaleX = new GControlTextSF(getMain(), 9, true, true);
        scaleX.setDataRange(0, 1e9);
        scaleX.setData(1);
        cl.add(scaleX, 2, 2, 4, 1);

        scaleY = new GControlTextY(getMain(), 9, true, true);
        scaleY.setDataRange(0, 1e9);
        scaleY.setData(1);
        cl.add(scaleY, 6, 2, 4, 1);

        cl.add(new JLabel(GLanguage.translate("mode")), 0, 3, 2, 1);

        String xAutoModeItems[] = {
            GLanguage.translate("wholeClip"), GLanguage.translate("loopPoints"), GLanguage.translate("measurePoints"), GLanguage.translate("selections")
        };

        xAutoMode = new JComboBox(xAutoModeItems);
        xAutoMode.setSelectedIndex(0);
        cl.add(xAutoMode, 2, 3, 4, 1);

        String yAutoModeItems[] = {
            GLanguage.translate("wholeClip"), GLanguage.translate("zoomedRange"), GLanguage.translate("sampleWidth"), GLanguage.translate("selections")
        };

        yAutoMode = new JComboBox(yAutoModeItems);
        yAutoMode.setSelectedIndex(1);
        cl.add(yAutoMode, 6, 3, 4, 1);

        individualY = new JCheckBox(GLanguage.translate("yIndividual"));
        cl.add(individualY, 0, 4, 4, 1);

        autoScale = new JButton(GLanguage.translate("autoscale"));
        cl.add(autoScale, 4, 4, 4, 1);

        pack();

        eventDispatcher = new EventDispatcher();
        scaleX.addControlListener(eventDispatcher);
        scaleY.addControlListener(eventDispatcher);
        offsetX.addControlListener(eventDispatcher);
        offsetY.addControlListener(eventDispatcher);
        individualY.addActionListener(eventDispatcher);
        autoScale.addActionListener(eventDispatcher);
    }

    private void reloadZoomFrame() {
        // read data on first channel...
        AChannelPlotter chp = getFocussedClip().getSelectedLayer().getChannel(0).getChannelPlotter();
        // reload all UiControlText entries...
        scaleX.setData(chp.getXLength());
        scaleY.setData(chp.getYLength());
        offsetX.setData(chp.getXOffset());
        offsetY.setData(chp.getYOffset());
    }

    private class EventDispatcher implements UiControlListener, ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == individualY) {
                Debug.println(1, "plugin " + getName() + " [individual y] clicked");
                AClipPlotter.setAutoScaleIndividualYEnabled(individualY.isSelected());
            } else if (e.getSource() == autoScale) {
                Debug.println(1, "plugin " + getName() + " [autoscale] clicked");
                AClip c = getFocussedClip();
                AClipPlotter cp = c.getClipPlotter();
                ALayer l = c.getSelectedLayer();
//              ALayerPlotter lp = l.getLayerPlotter();
                AChannel ch = l.getChannel(0);
                AChannelPlotter chp = ch.getChannelPlotter();

                // autoscale x ?
                if (autoX.isSelected()) {
                    float offset, length;

                    // mode...
                    switch (xAutoMode.getSelectedIndex()) {
                    case 0: // clip ?
                        cp.autoScaleX();
                        break;

                    case 1: // loop points ?
                        offset = c.getAudio().getLoopStartPointer();
                        length = c.getAudio().getLoopEndPointer() - c.getAudio().getLoopStartPointer();
                        cp.setXRange(offset - .03f * length, length * 1.06f);
                        break;

                    case 2: // measure points ?
                        offset = GPMeasure.getLowerCursor();
                        length = GPMeasure.getHigherCursor() - GPMeasure.getLowerCursor();
                        cp.setXRange(offset - .03f * length, length * 1.06f);
                        break;

                    case 3: // selections ?
                        offset = l.getLayerSelection().getLowestSelectedIndex();
                        length = l.getLayerSelection().getHighestSelectedIndex() - l.getLayerSelection().getLowestSelectedIndex();
                        cp.setXRange(offset - .03f * length, length * 1.06f);
                        break;
                    }

                }

                // autoscale y ?
                if (autoY.isSelected()) {
                    int xOffset, xLength;
                    float maxValue;
                    // mode...
                    switch (yAutoMode.getSelectedIndex()) {
                    case 0: // whole clip ?
                        cp.autoScaleY();
                        break;

                    case 1: // zoomed range ?
                        xOffset = (int) chp.getXOffset();
                        xLength = (int) chp.getXLength();
                        cp.autoScaleY(xOffset, xLength);
                        break;

                    case 2: // samplewidth ?
                        maxValue = 1 << (c.getSampleWidth() - 1);
                        cp.setYRange(-maxValue * 1.03f, 2 * maxValue * 1.03f);
                        break;

                    case 3: // selections ?
                        xOffset = l.getLayerSelection().getLowestSelectedIndex();
                        xLength = l.getLayerSelection().getHighestSelectedIndex() - l.getLayerSelection().getLowestSelectedIndex();
                        cp.autoScaleY(xOffset, xLength);
                        break;
                    }

                }
                reloadZoomFrame();
                reloadFocussedClipEditor();
            }
        }

        public void onDataChanging(UiControlEvent e) {
        }

        public void onDataChanged(UiControlEvent e) {
            AClip c = getFocussedClip();
            AClipPlotter cp = c.getClipPlotter();
            ALayerPlotter lp = c.getSelectedLayer().getLayerPlotter();

            if ((e.getSource() == scaleX) || (e.getSource() == offsetX)) {
                Debug.println(1, "plugin " + getName() + " [scale x] changed");
                // individual ?
                if (individualY.isSelected()) {
                    lp.setXRange((float) offsetX.getData(), (float) scaleX.getData());
                }
                // common ?
                else {
                    cp.setXRange((float) offsetX.getData(), (float) scaleX.getData());
                }
            } else if ((e.getSource() == scaleY) || (e.getSource() == offsetY)) {
                Debug.println(1, "plugin " + getName() + " [scale y] changed");
                // individual ?
                if (individualY.isSelected()) {
                    lp.setYRange((float) offsetY.getData(), (float) scaleY.getData());
                }
                // common ?
                else {
                    cp.setYRange((float) offsetY.getData(), (float) scaleY.getData());
                }
            }
            reloadZoomFrame();
            reloadFocussedClipEditor();
        }

        public void onValidate(UiControlEvent e) {
        }
    }
}
