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
import java.awt.Point;
import java.awt.Rectangle;

import ch.laoe.ui.Debug;
import ch.laoe.ui.GSimpleColorChooser;


/**
 * layer view.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @verison 28.12.00 first draft oli4 <br>
 *          24.01.01 array-based oli4 <br>
 *          24.06.01 new x/y-skala painting oli4 <br>
 */
public class ALayerPlotter extends APlotter {
    /**
     * constructor
     */
    public ALayerPlotter(AModel m) {
        super(m);
        setVisible(true);
        initColor();
    }

    public void setDefaultName() {
        name = "";
    }

    public ALayer getLayerModel() {
        return (ALayer) model;
    }

    // visibility
    private boolean isVisible;

    /**
     * set the whole layer visible/invisible in a layered representation.
     * this has no effect on the sample-data, it's only a graphical behaviour
     */
    public void setVisible(boolean b) {
        isVisible = b;
    }

    /**
     * get the actual visibility
     */
    public boolean isVisible() {
        return isVisible;
    }

    // color
    private Color color;

    private static int colorIndex;

    public void setColor(Color c) {
        color = c;
    }

    public Color getColor() {
        return color;
    }

    private void initColor() {
        setColor(colorSequence[colorIndex++ % colorSequence.length]);
    }

    private static Color colorSequence[] = GSimpleColorChooser.getDefaultColorList();

    /**
     * set the sample range
     */
    public void setXRange(float offset, float length) {
        for (int i = 0; i < getLayerModel().getNumberOfElements(); i++) {
            getLayerModel().getChannel(i).getChannelPlotter().setXRange(offset, length);
        }
    }

    /**
     * set the sample range
     */
    public void setYRange(float offset, float length) {
        for (int i = 0; i < getLayerModel().getNumberOfElements(); i++) {
            getLayerModel().getChannel(i).getChannelPlotter().setYRange(offset, length);
        }
    }

    /**
     * translate the sample offset
     */
    public void translateXOffset(float offset) {
        for (int i = 0; i < getLayerModel().getNumberOfElements(); i++) {
            getLayerModel().getChannel(i).getChannelPlotter().translateXOffset(offset);
        }
    }

    /**
     * translate the sample offset
     */
    public void translateYOffset(float offset) {
        for (int i = 0; i < getLayerModel().getNumberOfElements(); i++) {
            getLayerModel().getChannel(i).getChannelPlotter().translateYOffset(offset);
        }
    }

    /**
     * zoom x
     */
    public void zoomX(float factor) {
        for (int i = 0; i < getLayerModel().getNumberOfElements(); i++) {
            getLayerModel().getChannel(i).getChannelPlotter().zoomX(factor);
        }
    }

    /**
     * zoom y
     */
    public void zoomY(float factor) {
        for (int i = 0; i < getLayerModel().getNumberOfElements(); i++) {
            getLayerModel().getChannel(i).getChannelPlotter().zoomY(factor);
        }
    }

    public void autoScaleX() {
        float o = getAutoscaleXOffset();
        float l = getAutoscaleXLength();
        setXRange(o - l * .03f, l * 1.06f);
    }

    public void autoScaleY() {
        autoScaleY(0, getLayerModel().getMaxSampleLength());
    }

    public void autoScaleY(int xOffset, int xLength) {
        float o = getAutoscaleYOffset(xOffset, xLength);
        float l = getAutoscaleYLength(xOffset, xLength);
        setYRange(o - l * .03f, l * 1.06f);
    }

    public float getAutoscaleXOffset() {
        float min = Float.MAX_VALUE;
        float f;
        ALayer l = getLayerModel();
        for (int i = 0; i < l.getNumberOfChannels(); i++) {
            f = l.getChannel(i).getChannelPlotter().getAutoscaleXOffset();
            if (f < min) {
                min = f;
            }
        }
        return min;
    }

    public float getAutoscaleXLength() {
        float max = Float.MIN_VALUE;
        float f;
        ALayer l = getLayerModel();
        for (int i = 0; i < l.getNumberOfChannels(); i++) {
            f = l.getChannel(i).getChannelPlotter().getAutoscaleXLength();
            if (f > max) {
                max = f;
            }
        }
        return max;
    }

    public float getAutoscaleYOffset(int xOffset, int xLength) {
        float min = Float.MAX_VALUE;
        float f;
        ALayer l = getLayerModel();
        for (int i = 0; i < l.getNumberOfChannels(); i++) {
            f = l.getChannel(i).getChannelPlotter().getAutoscaleYOffset(xOffset, xLength);
            if (f < min) {
                min = f;
            }
        }
        return min;
    }

    public float getAutoscaleYLength(int xOffset, int xLength) {
        float max = Float.MIN_VALUE;
        float f;
        ALayer l = getLayerModel();
        for (int i = 0; i < l.getNumberOfChannels(); i++) {
            f = l.getChannel(i).getChannelPlotter().getAutoscaleYLength(xOffset, xLength);
            if (f > max) {
                max = f;
            }
        }
        return max;
    }

    // splitting layer into tracks geometry

    private int getSkalaSize(Rectangle layerRect, int maxNumberOfChannels) {
        int min = 3;
        int max = 8;
        return Math.max(Math.min(layerRect.height / maxNumberOfChannels / 20, max), min);
    }

    /**
     * returns a rectangle which defines the shape of a channel in the layer representation
     */
    private Rectangle createChannelRect(Rectangle layerRect, int channelIndex, int maxNumberOfChannels) {
        int lW = layerRect.width;
        int lH = layerRect.height;
        int skalaSize = getSkalaSize(layerRect, maxNumberOfChannels);
        int chH = lH / maxNumberOfChannels - skalaSize;
        int chW = lW - skalaSize;
        return new Rectangle(skalaSize, channelIndex * (chH + skalaSize), chW, chH);
    }

    /**
     * returns a rectangle which defines the shape between two channels
     */
    private Rectangle createYSkalaRect(Rectangle layerRect, int channelIndex, int maxNumberOfChannels) {
//      int lW = layerRect.width;
        int lH = layerRect.height;
        int skalaSize = getSkalaSize(layerRect, maxNumberOfChannels);
        int chH = lH / maxNumberOfChannels - skalaSize;
//      int chW = lW - skalaSize;

        return new Rectangle(0, channelIndex * (chH + skalaSize), skalaSize, chH);
    }

    /**
     * returns a rectangle which defines the shape of the x-skala of a channel
     */
    private Rectangle createXSkalaRect(Rectangle layerRect, int channelIndex, int maxNumberOfChannels) {
        int lW = layerRect.width;
        int lH = layerRect.height;
        int skalaSize = getSkalaSize(layerRect, maxNumberOfChannels);
        int chH = lH / maxNumberOfChannels - skalaSize;
        int chW = lW - skalaSize;

        return new Rectangle(skalaSize, channelIndex * (chH + skalaSize) + chH, chW - skalaSize, skalaSize);
    }

    /**
     * paints a whole layer: channels and its frames
     */
    public void paintLayer(Graphics2D g2d, Rectangle layerRect, int maxSampleLength, int maxNumberOfChannels, boolean skalaEnable, boolean volumeSensitive) {
        // visible ?
        if (isVisible || !volumeSensitive) {
            ALayer l = getLayerModel();

            for (int i = 0; i < maxNumberOfChannels; i++) {
                Rectangle channelRect = createChannelRect(layerRect, i, maxNumberOfChannels);

                // samples
                if (i < l.getNumberOfChannels()) {
                    l.getChannel(i).getChannelPlotter().setRectangle(channelRect);
                    l.getChannel(i).getChannelPlotter().paintSamples(g2d, color);

                    // frames
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));
                    l.getChannel(i).getChannelPlotter().paintFrame(g2d);
                }

                // skala
                if (skalaEnable && (i < l.getNumberOfChannels())) {
                    Rectangle xr = createXSkalaRect(layerRect, i, maxNumberOfChannels);
                    Rectangle yr = createYSkalaRect(layerRect, i, maxNumberOfChannels);
                    l.getChannel(i).getChannelPlotter().paintXSkala(g2d, xr);
                    l.getChannel(i).getChannelPlotter().paintYSkala(g2d, yr);
                }
            }
        }
    }

    /**
     * paints all selections int the channels of this layer.
     */
    public void paintAllSelections(Graphics2D g2d, Rectangle layerRect, Color color, int maxSampleLength, int numberOfChannels) {
        // visible ?
        if (isVisible) {
            ALayer l = getLayerModel();

            for (int i = 0; i < numberOfChannels; i++) {
                Rectangle subRect = createChannelRect(layerRect, i, numberOfChannels);

                // selections
                if (i < l.getNumberOfChannels()) {
                    l.getChannel(i).getChannelPlotter().setRectangle(subRect);
                    l.getChannel(i).getChannelPlotter().paintSelection(g2d, color);
                }
            }
        }
    }

    /**
     * paint the volume mask of the channels.
     */
    public void paintMasks(Graphics2D g2d, Rectangle layerRect, Color color, int maxSampleLength, int numberOfChannels) {
        // visible ?
        if (isVisible) {
            ALayer l = getLayerModel();

            for (int i = 0; i < numberOfChannels; i++) {
                Rectangle subRect = createChannelRect(layerRect, i, numberOfChannels);

                // masks
                if (i < l.getNumberOfChannels()) {
                    l.getChannel(i).getChannelPlotter().setRectangle(subRect);
                    l.getChannel(i).getChannelPlotter().paintMask(g2d, color);
                }
            }
        }
    }

    /**
     * paint the markers of the channels.
     */
    public void paintMarkers(Graphics2D g2d, Rectangle layerRect, int maxSampleLength, int numberOfChannels) {
        // visible ?
        if (isVisible) {
            ALayer l = getLayerModel();

            for (int i = 0; i < numberOfChannels; i++) {
                Rectangle subRect = createChannelRect(layerRect, i, numberOfChannels);
Debug.println(0, "subRect: " + subRect);

                // markers
                if (i < l.getNumberOfChannels()) {
                    Rectangle xr = createXSkalaRect(layerRect, i, numberOfChannels);
                    l.getChannel(i).getChannelPlotter().paintMarker(g2d, xr);
                }
            }
        }
    }

    // mouse handling

    /**
     * returns the channel index, in which the point p is residing. If no channel is concerned, it returns -1
     */
    public int getInsideChannelIndex(Point p) {
        try {
            ALayer l = getLayerModel();

            // which track...
            for (int i = 0; i < l.getNumberOfChannels(); i++) {
                if (l.getChannel(i).getChannelPlotter().isInsideChannel(p))
                    return i;
            }
        } catch (NullPointerException npe) {
        }
        // no track...
        return -1;
    }

}
