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

package ch.laoe.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelPlotter;
import ch.laoe.clip.AChannelSelection;
import ch.laoe.operation.AOSegmentGenerator;
import ch.laoe.operation.AOToolkit;


/**
 * Class: GEditableFreehand @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * reusable component to show, edit and apply freehand-curves on channels. these curves can be made fully graphical,
 * with the use of the mouse. once the curve edition terminated, the curve can be written to the actual channel.
 * 
 * mouse action table of freehand mode:
 * ---------------------------------------------------------
 *  press-drag-release click
 * ---------------------------------------------------------
 *  - draw - shift remove - ctrl draw "locked" -
 * ---------------------------------------------------------
 * 
 * @version 23.02.02 first draft, taken from FreeGenerator oli4 17.02.02 channel-wise copy/paste added
 * oli4
 */
public class GEditableFreehand {
    public GEditableFreehand() {
        points = new ArrayList<FPoint>(100);

        stroke = new BasicStroke();
        color = Color.yellow;

        actualOperation = IDLE;
        editable = true;

        clearXRange();
        clearYRange();

        penCursor = GToolkit.createCustomCursor(this, "penCursor");
        eraseCursor = GToolkit.createCustomCursor(this, "eraseCursor");
        actualCursor = penCursor;
    }

    public GEditableFreehand(GEditableFreehand fh) {
        this();
        for (int i = 0; i < fh.points.size(); i++) {
            points.add(new FPoint(fh.points.get(i)));
        }

    }

    // **************** graphic configuration *****************

    private Color color;

    public void setColor(Color c) {
        color = c;
    }

    // **************** graphic configuration *****************

    private boolean editable;

    public void setEditable(boolean b) {
        editable = b;
    }

    public boolean isEditable() {
        return editable;
    }

    // **************** range configuration *****************

    private float xMin, xMax, yMin, yMax;

    public void setXRange(float offset, float length) {
        xMin = offset;
        xMax = offset + length;
    }

    public void setYRange(float offset, float length) {
        yMin = offset;
        yMax = offset + length;
    }

    public void clearXRange() {
        xMin = -Float.MAX_VALUE;
        xMax = Float.MAX_VALUE;
    }

    public void clearYRange() {
        yMin = -Float.MAX_VALUE;
        yMax = Float.MAX_VALUE;
    }

    // ****************** point definition *******************

    private class FPoint {
        public float x, y;

        public FPoint() {
            this.x = 0;
            this.y = 0;
        }

        public FPoint(FPoint p) {
            this.x = p.x;
            this.y = p.y;
        }

        public FPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void toSampleDomain(Point p) {
            x = actualChannelPlotter.graphToSampleX(p.x);
            y = actualChannelPlotter.graphToSampleY(p.y);
        }

        public Point toGraphicDomain() {
            return new Point(actualChannelPlotter.sampleToGraphX(x), actualChannelPlotter.sampleToGraphY(y));
        }

        public int getGraphSquareDistance(FPoint p) {
            Point p1 = this.toGraphicDomain();
            Point p2 = p.toGraphicDomain();

            return (int) (Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
        }

        public float getSampleXDistance(FPoint p) {
            return Math.abs(x - p.x);
        }

        public void limitRange() {
            if (x > xMax)
                x = xMax;
            else if (x < xMin)
                x = xMin;

            if (y > yMax)
                y = yMax;
            else if (y < yMin)
                y = yMin;
        }

        public boolean xGreaterThan(FPoint p) {
            return x > p.x;
        }

        public boolean yGreaterThan(FPoint p) {
            return y > p.y;
        }

    }

    // ***************** objetcs ********************

    // the editable curve uses a vector of points
    private List<FPoint> points;

    private int actualIndex, previousIndex;

    // operation
    private int actualOperation;

    private static final int IDLE = 0;

    private static final int DRAW = 1;

    private static final int REMOVE = 2;

    // links to the model
    private AChannel actualChannel;

    private AChannelPlotter actualChannelPlotter;

    private Rectangle actualChannelRect;

    /**
     * set the layer-model
     */
    public void setChannel(AChannel ch) {
        actualChannel = ch;
        actualChannelPlotter = actualChannel.getChannelPlotter();
    }

    // cursors
    private Cursor penCursor, eraseCursor, actualCursor;

    /**
     * returns index of where inserting p in the points-curve
     */
    private int findIndex(FPoint p) {
        // all segment points...
        for (int i = 0; i < points.size(); i++) {
            // is right of ?
            if (points.get(i).xGreaterThan(p)) {
                return i;
            }
        }
        return points.size();
    }

    /**
     * insert or replace a point. p is normally inserted, except if there is a point with the same x value, in this case the old
     * point is replaced by p.
     */
    private void insertOrReplace(FPoint p, int index) {
        // append ?
        if (points.size() <= index) {
            points.add(p);
        }
        // same x value as existing point ?
        if (p.x == points.get(index).x) {
            points.remove(index);
            points.add(index, p);
        }
        // normal insertion ?
        else {
            points.add(index, p);
        }
    }

    /**
     * reset segment points
     */
    public void clear() {
        points.clear();
    }

    /**
     * returns the current number of points of thze edited segment-line
     */
    public int getNumberOfPoints() {
        return points.size();
    }

    // *************** results written into channel *****************

    /**
     * writes the samples in the channel
     */
    public void convertToSamples(int operation) {
        if (points.size() > 0) {
            // converts points to arrays
            float x[] = new float[points.size()];
            float y[] = new float[x.length];

            for (int i = 0; i < x.length; i++) {
                x[i] = points.get(i).x;
                y[i] = points.get(i).y;
            }

            // operate
            AChannelSelection chs = actualChannel.createChannelSelection();
            chs.operateChannel(new AOSegmentGenerator(x, y, AOSegmentGenerator.ORDER_1, operation, true));
        }
    }

    /**
     * writes the selected samples in the channel
     */
    public void convertToSelectedSamples(int operation) {
        if (points.size() > 0) {
            // converts points to arrays
            float x[] = new float[points.size()];
            float y[] = new float[x.length];

            for (int i = 0; i < x.length; i++) {
                x[i] = points.get(i).x;
                y[i] = points.get(i).y;
            }

            // operate
            AChannelSelection chs = actualChannel.getChannelSelection();
            chs.operateChannel(new AOSegmentGenerator(x, y, AOSegmentGenerator.ORDER_1, operation, false));
        }
    }

    /**
     * smooths the freehand curve
     */
    public void smooth() {
        float y[] = new float[points.size()];

        // read
        for (int i = 0; i < y.length; i++) {
            y[i] = points.get(i).y;
        }

        // smooth
        AOToolkit.smooth(y, 0, y.length, 3);

        // write back
        for (int i = 0; i < y.length; i++) {
            points.get(i).y = y[i];
        }
    }

    // *************** results without channel *****************

    private float x[], y[];

    /**
     * prepares the results, given through getSample() afterwards.
     */
    public void prepareResults() {
        // converts points into x/y arrays
        x = new float[points.size()];
        y = new float[points.size()];
        for (int i = 0; i < x.length; i++) {
            x[i] = points.get(i).x;
            y[i] = points.get(i).y;
        }
    }

    /**
     * returns the sample
     */
    public float getSample(int index) {
        return 0;// spline.getResult((float)index);
    }

    /**
     * mouse events
     */

    public void mousePressed(MouseEvent e) {
        if ((actualChannel != null) && editable) {
            actualChannelPlotter = actualChannel.getChannelPlotter();
            actualChannelRect = actualChannelPlotter.getRectangle();

            setXRange(actualChannelPlotter.getXOffset(), actualChannelPlotter.getXLength());
            setYRange(actualChannelPlotter.getYOffset(), actualChannelPlotter.getYLength());

            // operation
            actualOperation = IDLE;
            FPoint p = new FPoint();
            p.toSampleDomain(e.getPoint());
            p.limitRange();

            // remove ?
            if (GToolkit.isShiftKey(e)) {
                actualOperation = REMOVE;
            }
            // draw ?
            else {
                actualOperation = DRAW;
            }

            actualIndex = findIndex(p);
            previousIndex = actualIndex;

            // draw ?
            if (actualOperation == DRAW) {
                insertOrReplace(p, actualIndex);
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        Cursor c;
        // choose cursor...
        if (GToolkit.isShiftKey(e)) {
            c = eraseCursor;
        } else {
            c = penCursor;
        }

        // change detector...
        if (c != actualCursor) {
            actualCursor = c;
            ((Component) e.getSource()).setCursor(actualCursor);
        }
    }

    private int oldDraggedX = 0;

    public void mouseDragged(MouseEvent e) {
        if ((actualChannel != null) && editable) {
            if (e.getPoint().x != oldDraggedX) {
                oldDraggedX = e.getPoint().x;
                FPoint p = new FPoint();
                p.toSampleDomain(e.getPoint());
                p.limitRange();

                // draw ?
                if (actualOperation != IDLE) {
                    // lock point to curve mode ?
                    if (GToolkit.isCtrlKey(e)) {
                        p.y = actualChannel.sample[(int) p.x];
                    }

                    actualIndex = findIndex(p);

                    // remove old...
                    if (actualIndex > previousIndex) {
                        for (int i = previousIndex + 1; i < actualIndex; i++) {
                            if (points.size() > previousIndex + 1) {
                                points.remove(previousIndex + 1);
                            }
                        }
                    } else {
                        for (int i = actualIndex; i < previousIndex; i++) {
                            if (points.size() > actualIndex) {
                                points.remove(actualIndex);
                            }
                        }
                    }

                    // refind index
                    actualIndex = findIndex(p);

                    // add new...
                    if (actualOperation == DRAW) {
                        insertOrReplace(p, actualIndex);
                    }

                    previousIndex = actualIndex;
                }
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        try {
            actualChannelPlotter = actualChannel.getChannelPlotter();
            actualChannelRect = actualChannelPlotter.getRectangle();
        } catch (Exception exc) {
        }
        ((Component) e.getSource()).setCursor(actualCursor);
    }

    /**
     * graphics
     */

    private Stroke stroke;

    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
        if (actualChannel != null) {
            try {
                // draw segments
                g2d.setStroke(stroke);
                g2d.setColor(color);
                g2d.setClip(actualChannelRect);

                for (int i = 0; i < points.size() - 1; i++) {
                    Point a = points.get(i).toGraphicDomain();
                    Point b = points.get(i + 1).toGraphicDomain();
                    g2d.drawLine(a.x, a.y, b.x, b.y);
                }
            } catch (Exception e) {
            }
        }
    }

}
