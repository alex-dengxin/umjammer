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

import java.awt.Color;
import java.awt.Graphics2D;

import ch.laoe.operation.AOSpline;
import ch.laoe.operation.AOToolkit;
import ch.laoe.ui.Debug;


/**
 * channel view.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 28.12.00 first draft oli4 <br>
 *          24.01.01 array-based oli4 <br>
 *          28.03.01 bugfix in selection-painting oli4 <br>
 *          24.06.01 new x/y-skala painting oli4 <br>
 *          04.07.01 add white range on skalas oli4 <br>
 *          08.07.01 don't paint skala if range=0, because of hard VM crash !!! oli4 <br>
 *          18.07.01 add different sample drawings, with and without points oli4 <br>
 *          26.07.01 3rd order interpolation on big zoom oli4 <br>
 *          30.07.01 draw selection-dimension oli4 <br>
 *          11.01.02 introduce intensity-points oli4 <br>
 *          27.04.03 "plots nothing on certain ranges" bug fixed oli4 <br>
 */
public class AChannelPlotterSampleCurve extends AChannelPlotter {
    /**
     * constructor
     */
    public AChannelPlotterSampleCurve(AModel m, AChannelPlotter p) {
        super(m, p);
        style = FILLED;
    }

    public float getAutoscaleXOffset() {
        return 0;
    }

    public float getAutoscaleXLength() {
        return getChannelModel().getSampleLength();
    }

    public float getAutoscaleYOffset(int xOffset, int xLength) {
        return -getChannelModel().getMaxSampleValue(xOffset, xLength);
    }

    public float getAutoscaleYLength(int xOffset, int xLength) {
        return 2 * getChannelModel().getMaxSampleValue(xOffset, xLength);
    }

    protected float getValidYOffset() {
        return -(1 << (((AClip) getChannelModel().getParent().getParent()).getSampleWidth() - 1));
    }

    protected float getValidYLength() {
        return (1 << ((AClip) getChannelModel().getParent().getParent()).getSampleWidth());
    }

    // representation style
    private int style;

    public static final int FILLED = 1;

    public static final int DRAWED = 2;

    /**
     * set the style of the sample curve
     */
    public void setStyle(int style) {
        this.style = style;
    }

    private AOSpline spline = AOToolkit.createSpline();

    float x[] = null;

    float y[] = null;

    public void paintSamples(Graphics2D g2d, Color color) {
        try {
            AChannel ch = getChannelModel();
            ALayer layer = (ALayer) ch.getParent();
            int width = rectangle.width;
//          int height = rectangle.height;
            float sample[] = ch.sample;
            switch (layer.getType()) {
            case ALayer.AUDIO_LAYER:
                style = FILLED;
                break;

            default:
                style = DRAWED;
                break;
            }

            int xMin;
            int xMax;

            float yTop;
            float yBottom;
            float oldYTop = 0;
            float oldYBottom = 0;
            boolean firstPointPlotted = true;

            // color, clip
            g2d.setColor(color);
            g2d.setClip(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

            // high zoom, little points ? paint cubic spline interpolation lines...
            int l = (int) getXLength() + 3;
            if ((width / getXLength()) > 2.5) // handgelenk-mal-pi factor... muss grösser 2 sein, um Spline-probleme zu vermeiden
            {
                // point-arrays size change ?
                if ((x == null) || (x.length != l)) {
                    x = new float[l];
                    y = new float[l];
                }
                // set points...
                for (int i = 0; i < l; i++) {
                    x[i] = sampleToGraphX((int) getXOffset() + i);
                    y[i] = sampleToGraphY(ch.getSample((int) getXOffset() + i));
                }

                // paint spline line...
                int oldX = (int) x[0] - 1;
                int oldY = (int) y[0];
                spline.load(x, y);
                for (int i = 0; i < width; i += 1) {
                    int newX = i;
                    int newY = (int) spline.getResult(i);

                    if (ch.isValidIndex((int) graphToSampleX(newX))) {
                        g2d.drawLine(oldX, oldY, newX, newY);
                        // System.out.println("i="+i+": "+oldX+" "+newX+" "+oldY+" "+newY);
                    }

                    oldX = newX;
                    oldY = newY;
                }
            }
            // wide angle, many points ? draw filling vertical lines or border min-max lines...
            else {
                // draw reduced sample, each pixel...
                for (int i = 0; i < width; i++) {
                    // x scaling, sample range represented by pixel i
                    xMin = (int) graphToSampleX(i);
                    xMax = (int) graphToSampleX(i + 1);

                    // x range ok ?
                    if ((xMin >= sample.length) || (xMax >= sample.length))
                        break;

                    if ((xMin < 0) || (xMax < 0))
                        continue;

                    // value
                    yTop = sample[xMin];
                    yBottom = sample[xMin];

                    // y peak detect in the sample range
                    for (int j = xMin; j <= xMax; j++) {
                        if (sample[j] < yBottom)
                            yBottom = sample[j];
                        if (sample[j] > yTop)
                            yTop = sample[j];
                    }

                    // y scaling
                    yBottom = sampleToGraphY(yBottom);
                    yTop = sampleToGraphY(yTop);

                    // init
                    if (firstPointPlotted) {
                        firstPointPlotted = false;
                        oldYBottom = yBottom;
                        oldYTop = yTop;
                    }
                    // draw sample
                    switch (style) {
                    case DRAWED:
                        g2d.drawLine(i - 1, (int) oldYBottom, i, (int) yBottom);
                        g2d.drawLine(i - 1, (int) oldYTop, i, (int) yTop);
                        break;

                    case FILLED:
                        g2d.drawLine(i, (int) yBottom, i, (int) yTop);
                        break;
                    }

                    // old
                    oldYBottom = yBottom;
                    oldYTop = yTop;
                }
            }

            // high zoom, little points ? paint points...
            if ((width / getXLength()) > 5) {
                // paint points...
                for (int i = 0; i < l; i++) {
                    if (ch.isValidIndex((int) graphToSampleX((int) x[i]))) {
                        g2d.fillRect((int) x[i] - 2, (int) y[i] - 2, 4, 4);
                    }
                }
            }
        } catch (Exception e) {
            Debug.printStackTrace(5, e);
        }
    }

}
