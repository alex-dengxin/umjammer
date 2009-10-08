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

package ch.laoe.clip;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.laoe.ui.Debug;


/**
 * clip view.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 28.12.00 first draft oli4 <br>
 *          24.01.01 array-based oli4 <br>
 *          26.12.01 play/loop pointers always painted oli4 <br>
 *          29.12.01 global setting of autoscale individual-y oli4 <br>
 *          11.02.02 unit added oli4 <br>
 */
public class AClipPlotter extends APlotter {
    /**
     * constructor
     */
    public AClipPlotter(AModel m) {
        super(m);
    }

    public void setDefaultName() {
        name = "";
    }

    public AClip getClipModel() {
        return (AClip) model;
    }

    /**
     * set the sample range
     */
    public void setXRange(float offset, float length) {
        for (int i = 0; i < getClipModel().getNumberOfElements(); i++) {
            getClipModel().getLayer(i).getLayerPlotter().setXRange(offset, length);
        }
    }

    /**
     * set the amplitude range
     */
    public void setYRange(float offset, float length) {
        for (int i = 0; i < getClipModel().getNumberOfElements(); i++) {
            getClipModel().getLayer(i).getLayerPlotter().setYRange(offset, length);
        }
    }

    /**
     * translate the sample offset
     */
    public void translateXOffset(float offset) {
        for (int i = 0; i < getClipModel().getNumberOfElements(); i++) {
            getClipModel().getLayer(i).getLayerPlotter().translateXOffset(offset);
        }
    }

    /**
     * translate the amplitude offset
     */
    public void translateYOffset(float offset) {
        for (int i = 0; i < getClipModel().getNumberOfElements(); i++) {
            getClipModel().getLayer(i).getLayerPlotter().translateYOffset(offset);
        }
    }

    /**
     * zoom x
     */
    public void zoomX(float factor) {
        for (int i = 0; i < getClipModel().getNumberOfElements(); i++) {
            getClipModel().getLayer(i).getLayerPlotter().zoomX(factor);
        }
    }

    /**
     * zoom y
     */
    public void zoomY(float factor) {
        for (int i = 0; i < getClipModel().getNumberOfElements(); i++) {
            getClipModel().getLayer(i).getLayerPlotter().zoomY(factor);
        }
    }

    private static boolean autoScaleIndividualYEnable = false;

    public static void setAutoScaleIndividualYEnabled(boolean b) {
        autoScaleIndividualYEnable = b;
    }

    public void autoScale() {
        autoScaleX();
        autoScaleY();
    }

    public void autoScaleX() {
        float o = getAutoscaleXOffset();
        float l = getAutoscaleXLength();

        AClip c = getClipModel();
        for (int i = 0; i < c.getNumberOfLayers(); i++) {
            ALayerPlotter lp = c.getLayer(i).getLayerPlotter();
            if (lp.isVisible()) {
                lp.setXRange(o - l * .03f, l * 1.06f);
            }
        }
    }

    public void autoScaleY() {
        autoScaleY(0, getClipModel().getMaxSampleLength());
    }

    public void autoScaleY(int xOffset, int xLength) {
        float o = getAutoscaleYOffset(xOffset, xLength);
        float l = getAutoscaleYLength(xOffset, xLength);

        AClip c = getClipModel();
        for (int i = 0; i < c.getNumberOfLayers(); i++) {
            ALayerPlotter lp = c.getLayer(i).getLayerPlotter();
            if (lp.isVisible()) {
                if (autoScaleIndividualYEnable) {
                    lp.autoScaleY(xOffset, xLength);
                } else {
                    lp.setYRange(o - l * .03f, l * 1.06f);
                }
            }
        }
    }

    public float getAutoscaleXOffset() {
        float min = Float.MAX_VALUE;
        float f;
        AClip c = getClipModel();
        for (int i = 0; i < c.getNumberOfLayers(); i++) {
            ALayerPlotter lp = c.getLayer(i).getLayerPlotter();
            if (lp.isVisible()) {
                f = lp.getAutoscaleXOffset();
                if (f < min) {
                    min = f;
                }
            }
        }
        return min;
    }

    public float getAutoscaleXLength() {
        float max = Float.MIN_VALUE;
        float f;
        AClip c = getClipModel();
        for (int i = 0; i < c.getNumberOfLayers(); i++) {
            ALayerPlotter lp = c.getLayer(i).getLayerPlotter();
            if (lp.isVisible()) {
                f = lp.getAutoscaleXLength();
                if (f > max) {
                    max = f;
                }
            }
        }
        return max;
    }

    public float getAutoscaleYOffset(int xOffset, int xLength) {
        float min = Float.MAX_VALUE;
        float f;
        AClip c = getClipModel();
        for (int i = 0; i < c.getNumberOfLayers(); i++) {
            ALayerPlotter lp = c.getLayer(i).getLayerPlotter();
            if (lp.isVisible()) {
                f = lp.getAutoscaleYOffset(xOffset, xLength);
                if (f < min) {
                    min = f;
                }
            }
        }
        return min;
    }

    public float getAutoscaleYLength(int xOffset, int xLength) {
        float max = Float.MIN_VALUE;
        float f;
        AClip c = getClipModel();
        for (int i = 0; i < c.getNumberOfLayers(); i++) {
            ALayerPlotter lp = c.getLayer(i).getLayerPlotter();
            if (lp.isVisible()) {
                f = lp.getAutoscaleYLength(xOffset, xLength);
                if (f > max) {
                    max = f;
                }
            }
        }
        return max;
    }

    // ************ unit **************

    public static final int UNIT_1 = 0;

    public static final int UNIT_S = 1;

    public static final int UNIT_MS = 2;

    public static final int UNIT_PERCENT = 3;

    public static final int UNIT_HZ = 4;

    public static final int UNIT_FDHZ = 5;

    private static int plotterXUnit = UNIT_1;

    private static int plotterYUnit = UNIT_1;

    public static void setPlotterXUnit(int unit) {
        plotterXUnit = unit;
    }

    public static void setPlotterYUnit(int unit) {
        plotterYUnit = unit;
    }

    static String getPlotterXUnitName() {
        switch (plotterXUnit) {
        case UNIT_1:
            return "";

        case UNIT_S:
            return "s";

        case UNIT_MS:
            return "ms";

        case UNIT_PERCENT:
            return "%";

        case UNIT_HZ:
            return "1/s";

        case UNIT_FDHZ:
            return "Hz";
        }
        return "";
    }

    static String getPlotterYUnitName() {
        switch (plotterYUnit) {
        case UNIT_1:
            return "";

        case UNIT_PERCENT:
            return "%";
        }
        return "";
    }

    private static boolean skalaValuesVisible = true;

    /**
     * set the numeric values of the skala visibility
     * 
     * @param b
     */
    public static void setSkalaValuesVisible(boolean b) {
        skalaValuesVisible = b;
    }

    public static boolean isSkalaValuesVisible() {
        return skalaValuesVisible;
    }

    float toPlotterXUnit(float d) {
        switch (plotterXUnit) {
        case UNIT_1:
            return d;

        case UNIT_S:
            return d / getClipModel().getSampleRate();

        case UNIT_MS:
            return d / getClipModel().getSampleRate() * 1000;

        case UNIT_PERCENT:
            return d / getClipModel().getMaxSampleLength() * 100;

        case UNIT_HZ:
            return getClipModel().getSampleRate() / d;

        case UNIT_FDHZ:
            return d / (2 * getClipModel().getMaxSampleLength()) * getClipModel().getSampleRate();
        }
        return d;
    }

    float toPlotterYUnit(float d) {
        switch (plotterYUnit) {
        case UNIT_1:
            return d;

        case UNIT_PERCENT:
            return d / (1 << (getClipModel().getSampleWidth() - 1)) * 100;
        }
        return d;
    }

    float fromPlotterXUnit(float d) {
        switch (plotterXUnit) {
        case UNIT_1:
            return d;

        case UNIT_S:
            return d * getClipModel().getSampleRate();

        case UNIT_MS:
            return d * getClipModel().getSampleRate() / 1000;

        case UNIT_PERCENT:
            return d * getClipModel().getMaxSampleLength() / 100;

        case UNIT_HZ:
            return getClipModel().getSampleRate() / d;

        case UNIT_FDHZ:
            return d * (2 * getClipModel().getMaxSampleLength()) / getClipModel().getSampleRate();
        }
        return d;
    }

    float fromPlotterYUnit(float d) {
        switch (plotterYUnit) {
        case UNIT_1:
            return d;

        case UNIT_PERCENT:
            return d * (1 << (getClipModel().getSampleWidth() - 1)) / 100;
        }
        return d;
    }

    // paint scales
    protected int maxNumberOfChannels;

    protected int maxSampleLength;

    /**
     * paint all layers in layered mode, mixed considering volume-transparency, adapted for clip-editor window.
     */
    public void paintFullClip(Graphics2D g2d, Rectangle rect) {
        if ((rect.width > 0) && (rect.height > 0)) {
            maxNumberOfChannels = getClipModel().getMaxNumberOfChannels();
            maxSampleLength = getClipModel().getMaxSampleLength();
            ALayer selectedLayer = getClipModel().getSelectedLayer();

            // paint all non-selected layers
            int selectedLayerIndex = 0;
            for (int i = 0; i < getClipModel().getNumberOfLayers(); i++) {
                ALayer l = getClipModel().getLayer(i);
                if (l != selectedLayer) {
                    l.getLayerPlotter().paintLayer(g2d, rect, maxSampleLength, maxNumberOfChannels, false, true);
                } else {
                    selectedLayerIndex = i;
                }
            }
Debug.println(0, "selectedLayerIndex: " + selectedLayerIndex);
            // paint selected layer on the top
            selectedLayer.getLayerPlotter().paintLayer(g2d, rect, maxSampleLength, maxNumberOfChannels, true, true);
        }
    }

    /**
     * paints all the selections of the top layer only
     */
    public void paintDetailsOfSelectedLayer(Graphics2D g2d, Rectangle rect) {
        if (getClipModel().getNumberOfLayers() > 0) {
            // paint top selection
            getClipModel().getSelectedLayer().getLayerPlotter().paintAllSelections(g2d, rect, Color.yellow, maxSampleLength, maxNumberOfChannels);

            // paint top mask
            getClipModel().getSelectedLayer().getLayerPlotter().paintMasks(g2d, rect, Color.yellow, maxSampleLength, maxNumberOfChannels);

            // paint top markers
            getClipModel().getSelectedLayer().getLayerPlotter().paintMarkers(g2d, rect, maxSampleLength, maxNumberOfChannels);

            // play- and looppointers
            getClipModel().getAudio().getPlotter().paintPlayPointer(g2d, rect, Color.black);
            getClipModel().getAudio().getPlotter().paintLoopPointer(g2d, rect, Color.red);

        }
    }

    /**
     * paints only one layer, without volume-transparency, without selection, adapted for thumbnail-representation of a layer
     */
    public void paintLayerThumbnail(Graphics2D g2d, Rectangle rect, int layerIndex) {
        ALayer l = getClipModel().getLayer(layerIndex);

        l.getLayerPlotter().paintLayer(g2d, rect, l.getMaxSampleLength(), l.getNumberOfChannels(), false, false);
    }

    /**
     * paints only one channel, without volume-transparency, without selection, adapted for thumbnail-representation of a channel
     */
    public void paintChannelThumbnail(Graphics2D g2d, Rectangle rect, int layerIndex, int channelIndex) {
        ALayer l = getClipModel().getLayer(layerIndex);
        AChannel ch = l.getChannel(channelIndex);

        // samples
        ch.getChannelPlotter().setRectangle(rect);
        ch.getChannelPlotter().paintSamples(g2d, l.getLayerPlotter().getColor());

        // frames
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));
        ch.getChannelPlotter().paintFrame(g2d);

    }

}
