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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.NumberFormat;


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
 *          06.05.2003 paint numeric values in rulers oli4 <br>
 */
public abstract class AChannelPlotter extends APlotter {
    /**
     * constructor
     */
    public AChannelPlotter(AModel m) {
        super(m);
        xLength = 1;
        yLength = 1;
        rectangle = new Rectangle(1, 1);
    }

    /**
     * constructor
     */
    public AChannelPlotter(AModel m, AChannelPlotter p) {
        this(m);
        if (p != null) {
            xOffset = p.xOffset;
            xLength = p.xLength;
            yOffset = p.yOffset;
            yLength = p.yLength;
        }
    }

    public AChannel getChannelModel() {
        return (AChannel) model;
    }

    public void setDefaultName() {
        name = "";
    }

    // xy range
    private float xOffset, xLength;

    private float yOffset, yLength;

    // drawed rectangle
    protected Rectangle rectangle;

    /**
     * returns the actual x offset
     */
    public float getXOffset() {
        return xOffset;
    }

    /**
     * returns the actual x length
     */
    public float getXLength() {
        return xLength;
    }

    /**
     * returns the actual y offset
     */
    public float getYOffset() {
        return yOffset;
    }

    /**
     * returns the actual y length
     */
    public float getYLength() {
        return yLength;
    }

    public abstract float getAutoscaleXOffset();

    public abstract float getAutoscaleXLength();

    public abstract float getAutoscaleYOffset(int xOffset, int xLength);

    public abstract float getAutoscaleYLength(int xOffset, int xLength);

    protected abstract float getValidYOffset();

    protected abstract float getValidYLength();

    /**
     * set the sample range
     */
    public void setXRange(float offset, float length) {
        xOffset = offset;
        xLength = length;
        limitXRange();
    }

    /**
     * set the amplitude range
     */
    public void setYRange(float offset, float length) {
        yOffset = offset;
        yLength = length;
        // yOffset = sampleToGraphY(offset);
        // yLength = sampleToGraphY(length);
        limitYRange();
    }

    /**
     * translate the sample offset
     */
    public void translateXOffset(float offset) {
        xOffset += offset;
        limitXRange();
    }

    /**
     * translate the amplitude offset
     */
    public void translateYOffset(float offset) {
        yOffset += offset;
        // yOffset += sampleToGraphY(offset);
        limitYRange();
    }

    /**
     * limits x-ranges
     */
    private void limitXRange() {
        if (xOffset < Integer.MIN_VALUE / 2)
            xOffset = Integer.MIN_VALUE / 2;
        else if (xOffset > Integer.MAX_VALUE / 2)
            xOffset = Integer.MAX_VALUE / 2;

        if (xLength > Integer.MAX_VALUE)
            xLength = Integer.MAX_VALUE;
        else if (xLength < .1f)
            xLength = .1f;
    }

    /**
     * limits y-ranges
     */
    private void limitYRange() {
        if (yOffset > Integer.MAX_VALUE)
            yOffset = Integer.MAX_VALUE;
        else if (yOffset < Integer.MIN_VALUE)
            yOffset = Integer.MIN_VALUE;

        if (yLength > Integer.MAX_VALUE)
            yLength = Integer.MAX_VALUE;

        if (Math.abs(yLength) < 1e-12)
            yLength = 1e-12f;
    }

    /**
     * zoom x
     */
    public void zoomX(float factor) {
        xOffset += xLength * (1 - 1 / factor) / 2;
        xLength /= factor;
        limitXRange();
    }

    /**
     * zoom y
     */
    public void zoomY(float factor) {
        yOffset += yLength * (1 - 1 / factor) / 2;
        yLength /= factor;
        limitYRange();
    }

    public int sampleToGraphX(float x) {
        return (int) ((x - xOffset) * rectangle.width / xLength) + rectangle.x;
    }

    public int sampleToGraphY(float y) {
        return (int) ((yLength - y + yOffset) * rectangle.height / yLength) + rectangle.y;
        // return (int)((yLength - AOToolkit.todB(Math.abs(y)/128) + yOffset) * rectangle.height / yLength) + rectangle.y;
    }

    public int percentToGraphY(float y) {
        return (int) ((1.f - y) * rectangle.height) + rectangle.y;
    }

    public float graphToSampleX(int x) {
        return xOffset + ((x - rectangle.x) * xLength) / rectangle.width;
    }

    public float graphToSampleY(int y) {
        return yOffset + ((rectangle.height - y + rectangle.y) * yLength / rectangle.height);
        // return AOToolkit.fromdB(yOffset + ((rectangle.height - y + rectangle.y) * yLength / rectangle.height))*128;
    }

    public float graphToPercentY(float y) {
        return (rectangle.height - y + rectangle.y) / rectangle.height;
    }

    /**
     * set the pixel-rectangle, in which the track is painted
     */
    public void setRectangle(Rectangle r) {
        rectangle = r;
    }

    /**
     * get the pixel-rectangle
     */
    public Rectangle getRectangle() {
        return rectangle;
    }

    /**
     * returns true, if the point p is inside the sample-curve-rectangle
     */
    public boolean isInsideChannel(Point p) {
        return ((p.y >= rectangle.y) && (p.y <= (rectangle.y + rectangle.height)) && (p.x >= rectangle.x) && (p.y <= (rectangle.x + rectangle.width)));

    }

    /**
     * paint the frame around the sample-curve
     */
    public void paintFrame(Graphics2D g2d) {
        int width = rectangle.width;
        int height = rectangle.height;
        int x = rectangle.x;
        int y = rectangle.y;
        int y0 = sampleToGraphY(0);
        int yBottomLimit = sampleToGraphY(getValidYOffset());
        int yTopLimit = sampleToGraphY(getValidYOffset() + getValidYLength());

        g2d.setClip(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

        // x-axis
        g2d.setColor(new Color(0x7F3F3F3F, true));
        g2d.drawLine(x, y0, x + width - 1, y0);

        // sample-width limits
        g2d.setColor(new Color(0x7F999999, true));
        g2d.drawLine(x, yTopLimit, x + width - 1, yTopLimit);
        g2d.drawLine(x, yBottomLimit, x + width - 1, yBottomLimit);

        // frame
        g2d.setColor(Color.gray);
        g2d.drawRect(x, y, width - 2, height - 2);
        g2d.setColor(Color.white);
        g2d.drawRect(x + 1, y + 1, width - 2, height - 2);
    }

    /**
     * paint the sample-curve
     */
    public abstract void paintSamples(Graphics2D g2d, Color color);

    /**
     * paints the mask
     */
    public void paintMask(Graphics2D g2d, Color color) {
        getChannelModel().getMask().paintOntoClip(g2d, rectangle);
    }

    /**
     * paints the markers
     */
    public void paintMarker(Graphics2D g2d, Rectangle rect) {
        getChannelModel().getMarker().paintOntoClip(g2d, rect);
    }

    /**
     * highlight the actually selected part of the sample-curve
     */
    public void paintSelection(Graphics2D g2d, Color color) {
        AChannelSelection s = getChannelModel().getChannelSelection();
        AClipPlotter cp = ((AClip) model.getParent().getParent()).getClipPlotter();

        if (s.isSelected()) {
            int xLeft = sampleToGraphX(s.getOffset());
            int y = rectangle.y + 2;
            int xRight = sampleToGraphX(s.getOffset() + s.getLength());
            int h = rectangle.height - 5;

            // range
            if (xLeft < 0)
                xLeft = -50;
            else if (xLeft > rectangle.width)
                return;

            if (xRight > rectangle.width)
                xRight = rectangle.width + 50;
            else if (xRight < 0)
                return;

            // fill
            g2d.setColor(color);
            g2d.setClip(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .18f));
            g2d.fillRect(xLeft, y, xRight - xLeft, h);

            // border
            float dash[] = {
                4.f, 4.f
            };
            g2d.setStroke(new BasicStroke(1.f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.f, dash, 0.f));
            g2d.setColor(color.darker());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));
            g2d.drawRect(xLeft, y, xRight - xLeft, h);

            // selection intensity
            g2d.setStroke(new BasicStroke());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));
            int oldPx = 0;
            int oldPy = 0;
            for (int i = 0; i < s.getIntensityPoints().size(); i++) {
                AChannelSelection.Point sp = s.getIntensityPoints().get(i);
                int px = sampleToGraphX(s.getOffset() + (s.getLength() * sp.x));
                int py = (int) ((1 - sp.y) * h) + y;
                if (i > 0) {
                    g2d.setColor(color);
                    g2d.drawLine(oldPx, oldPy, px, py);
                }
                oldPx = px;
                oldPy = py;
            }
            for (int i = 0; i < s.getIntensityPoints().size(); i++) {
                AChannelSelection.Point sp = s.getIntensityPoints().get(i);
                int px = sampleToGraphX(s.getOffset() + (s.getLength() * sp.x));
                int py = (int) ((1 - sp.y) * h) + y;
                if (s.getActiveIntensityPointIndex() == i) {
                    g2d.setColor(Color.red);
                } else {
                    g2d.setColor(color);
                }
                g2d.fillRect(px - 2, py - 2, 4, 4);
            }

            // text
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(4);
            nf.setGroupingUsed(false);
            g2d.setColor(Color.white);
            g2d.setFont(new Font("Courrier", Font.PLAIN, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String topStr = "" + nf.format(cp.toPlotterXUnit(s.getOffset())) + AClipPlotter.getPlotterXUnitName();
            String bottomStr = "" + nf.format(cp.toPlotterXUnit(s.getLength())) + AClipPlotter.getPlotterXUnitName();
            int mx = (xRight + xLeft) / 2;
            int my = y + (h / 2);
            g2d.drawString(topStr, mx - fm.stringWidth(topStr) / 2, my - fm.getHeight() / 2);
            g2d.drawString(bottomStr, mx - fm.stringWidth(bottomStr) / 2, my + fm.getHeight() / 2);
        }
    }

    private float getNearestDecade(float value) {
        float decade = 1;
        for (int i = -12; i < 12; i++) {
            decade = (float) Math.pow(10, i);
            if (value < (3 * decade)) {
                return decade;
            }
        }
        return decade;
    }

    /**
     * paints a text, given the center coordinates
     * 
     * @param g2d
     * @param text text to paint
     * @param x coordinate of middle of text
     * @param y coordinate of middle of text
     */
    private void paintText(Graphics2D g2d, String text, int size, int x, int y) {
        g2d.setFont(new Font("Courrier", Font.PLAIN, size));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, x - fm.stringWidth(text) / 2, y + fm.getAscent() - fm.getHeight() / 2);
    }

    /**
     * same as paintText, but paints formatted numeric value
     * 
     * @param g2d
     * @param value
     * @param size
     * @param x
     * @param y
     */
    private static String floatToString(float value) {
        // multiplicator...
        String multi = "";
        if (Math.abs(value) >= 1e9) {
            value /= 1e9;
            multi = "G";
        } else if (Math.abs(value) >= 1e6) {
            value /= 1e6;
            multi = "M";
        } else if (Math.abs(value) >= 1e3) {
            value /= 1e3;
            multi = "k";
        } else if (Math.abs(value) >= 1e0) {
            multi = "";
        } else if (Math.abs(value) >= 1e-3) {
            value /= 1e-3;
            multi = "m";
        } else if (Math.abs(value) >= 1e-6) {
            value /= 1e-6;
            multi = "u";
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);
        return nf.format(value) + multi;
    }

    /**
     * highlight the actually selected part of the sample-curve
     */
    public void paintXSkala(Graphics2D g2d, Rectangle rect) {
        if (getXLength() < .1)
            return;

        // graphic settings
        g2d.setClip(rect.x, rect.y - 100, rect.width, rect.height + 100);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));
        // sample range
        AChannel ch = getChannelModel();
        int rangeBegin = Math.max(0, sampleToGraphX(0));
        int rangeEnd = Math.min(rect.x + rect.width, sampleToGraphX(ch.getSampleLength()));
        g2d.setColor(Color.white);
        g2d.fillRect(rangeBegin, rect.y, rangeEnd - rangeBegin, rect.height);

        // ticks geometry
        AClipPlotter cp = ((AClip) model.getParent().getParent()).getClipPlotter();
//      int yMiddle = rect.y + rect.height / 2;
        int majorDeltaX = 100; // unit = pixels
        float sLeft = cp.toPlotterXUnit(graphToSampleX(rect.x));
        float sRight = cp.toPlotterXUnit(graphToSampleX(rect.x + rect.width));
        float majorDeltaS = cp.toPlotterXUnit(graphToSampleX(rect.x + majorDeltaX)) - sLeft;
        float majorDecadedDeltaS = getNearestDecade(majorDeltaS);
        float majorDecadedSLeft = ((int) (sLeft / majorDecadedDeltaS)) * majorDecadedDeltaS;
        float majorReducedFactor = majorDecadedDeltaS / majorDeltaS;
        float minorDecadedDeltaS;

        // adaptable 10/5-minor ticks
        if (majorReducedFactor > .5)
            minorDecadedDeltaS = majorDecadedDeltaS / 10;
        else
            minorDecadedDeltaS = majorDecadedDeltaS / 5;

        // minor
        int minorLineLength = (int) (rect.height * .4);
        g2d.setColor(Color.gray);
        int n = (int) ((sRight - sLeft) / minorDecadedDeltaS + 20);
        for (int i = 0; i < n; i++) {
            float s = majorDecadedSLeft + i * minorDecadedDeltaS;
            if (s < sRight) {
                int x = sampleToGraphX(cp.fromPlotterXUnit(s));
                g2d.drawLine(x, rect.y, x, rect.y + minorLineLength);
            } else {
                break;
            }
        }

        // major
        int majorLineLength = (rect.height * 1);
        n = (int) ((sRight - sLeft) / majorDecadedDeltaS + 20);
        for (int i = 0; i < n; i++) {
            float s = majorDecadedSLeft + i * majorDecadedDeltaS;
            if (s < sRight) {
                int x = sampleToGraphX(cp.fromPlotterXUnit(s));
                g2d.setColor(Color.GRAY);
                g2d.drawLine(x, rect.y, x, rect.y + majorLineLength);

                if (AClipPlotter.isSkalaValuesVisible()) {
                    g2d.setColor(Color.BLACK);
                    paintText(g2d, floatToString(s) + AClipPlotter.getPlotterXUnitName(), 10, x, rect.y - 6);
                }
            } else {
                break;
            }
        }
    }

    /**
     * highlight the actually selected part of the sample-curve
     */
    public void paintYSkala(Graphics2D g2d, Rectangle rect) {
        if (getYLength() < .1)
            return;

        // graphic settings
        g2d.setClip(rect.x, rect.y, rect.width + 100, rect.height);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));

        // sample range
//      AChannel ch = getChannelModel();
        int rangeTop = Math.max(0, sampleToGraphY(getValidYOffset() + getValidYLength()));
        int rangeBottom = Math.min(rect.y + rect.height, sampleToGraphY(getValidYOffset()));
        g2d.setColor(Color.white);
        g2d.fillRect(rect.x, rangeTop, rect.width, rangeBottom - rangeTop);

        // ticks geometry
        AClipPlotter cp = ((AClip) model.getParent().getParent()).getClipPlotter();
//      int xMiddle = rect.x + rect.width / 2;
        int majorDeltaY = 100; // unit = pixels
        float sTop = cp.toPlotterYUnit(graphToSampleY(rect.y));
        float sBottom = cp.toPlotterYUnit(graphToSampleY(rect.y + rect.height));
        float majorDeltaS = sTop - cp.toPlotterYUnit(graphToSampleY(rect.y + majorDeltaY));
        float majorDecadedDeltaS = getNearestDecade(majorDeltaS);
        float majorDecadedSTop = (float) Math.ceil(sTop / majorDecadedDeltaS) * majorDecadedDeltaS;
        float majorReducedFactor = majorDecadedDeltaS / majorDeltaS;
        float minorDecadedDeltaS;

        // System.out.println("majorDecadedDeltaS"+majorDecadedDeltaS);
        // System.out.println("majorDecadedSTop"+majorDecadedSTop);

        // adaptable 10/5-minor ticks
        if (majorReducedFactor > .5)
            minorDecadedDeltaS = majorDecadedDeltaS / 10;
        else
            minorDecadedDeltaS = majorDecadedDeltaS / 5;

        // minor
        int minorLineLength = (int) (rect.width * .6);
        int n = (int) ((sTop - sBottom) / minorDecadedDeltaS + 20);
        g2d.setColor(Color.gray);
        for (int i = 0; i < n; i++) {
            float s = majorDecadedSTop - i * minorDecadedDeltaS;
            if (s > sBottom) {
                int y = sampleToGraphY(cp.fromPlotterYUnit(s));
                g2d.drawLine(rect.x + minorLineLength, y, rect.x + rect.width, y);
            } else {
                break;
            }
        }

        // major
        int majorLineLength = (int) (rect.width * .3);
        n = (int) ((sTop - sBottom) / majorDecadedDeltaS + 20);
        for (int i = 0; i < n; i++) {
            float s = majorDecadedSTop - i * majorDecadedDeltaS;
            if (s > sBottom) {
                int y = sampleToGraphY(cp.fromPlotterYUnit(s));
                if (y > 0) {
                    g2d.setColor(Color.gray);
                    g2d.drawLine(rect.x + majorLineLength, y, rect.x + rect.width, y);

                    if (AClipPlotter.isSkalaValuesVisible()) {
                        g2d.setColor(Color.BLACK);
                        paintText(g2d, floatToString(s) + AClipPlotter.getPlotterYUnitName(), 10, rect.x + 20, y);
                    }
                }
            } else {
                break;
            }
        }
    }

}
