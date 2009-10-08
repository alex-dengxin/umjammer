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
import ch.laoe.operation.AOSpline;
import ch.laoe.operation.AOToolkit;


/**
 * Class: GEditableSegments @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * reusable component to show, edit and apply segment-curves on channels. segments of 0th order, 1st order polynom
 * and cubic spline interpolation. these curves can be made fully graphical, with the use of the mouse. once the curve edition
 * terminated, the curve can be written to the actual channel.
 * 
 * mouse action table of segment mode:
 * ---------------------------------------------------------
 *  press-drag-release click
 * ---------------------------------------------------------
 *  - move point add point shift - remove point ctrl move "locked" add point
 * ---------------------------------------------------------
 * 
 * @version 15.02.02 first draft, taken from FreeGenerator oli4
 */
public class GEditableSegments {
    public GEditableSegments() {
        segmentPoints = new ArrayList<FPoint>(25);
        spline = AOToolkit.createSpline();

        segmentSroke = new BasicStroke();
        color = Color.yellow;
        pointsVisible = true;
        highlightActivePoint = true;

        segmentMode = ORDER_1;
        actualOperation = IDLE;
        editable = true;

        setYDomain(SAMPLE_DOMAIN);

        clearXRange();
        clearYRange();

        penCursor = GToolkit.createCustomCursor(this, "penCursor");
        eraseCursor = GToolkit.createCustomCursor(this, "eraseCursor");
        actualCursor = penCursor;
    }

    /**
     * copy-constructor
     */
    public GEditableSegments(GEditableSegments es) {
        this();
        color = es.color;
        pointsVisible = es.pointsVisible;
        highlightActivePoint = es.highlightActivePoint;
        segmentMode = es.segmentMode;
        editable = es.editable;
        xMin = es.xMin;
        xMax = es.xMax;
        yMin = es.yMin;
        yMax = es.yMax;
        color = es.color;
        yDomain = es.yDomain;

        for (int i = 0; i < es.segmentPoints.size(); i++) {
            segmentPoints.add(new FPoint(es.segmentPoints.get(i)));
        }
    }

    // **************** graphic configuration *****************

    private Color color;

    public void setColor(Color c) {
        color = c;
    }

    private boolean pointsVisible;

    public void setPointsVisible(boolean b) {
        pointsVisible = b;
    }

    private boolean highlightActivePoint;

    public void setHighlightActivePointEnabled(boolean b) {
        highlightActivePoint = b;
    }

    // **************** editable *****************

    private boolean editable;

    public void setEditable(boolean b) {
        editable = b;
    }

    public boolean isEditable() {
        return editable;
    }

    // **************** domain *****************

    private int yDomain;

    public static final int SAMPLE_DOMAIN = 1;

    public static final int PERCENT_DOMAIN = 2;

    public void setYDomain(int d) {
        switch (d) {
        case SAMPLE_DOMAIN:
        case PERCENT_DOMAIN:
            yDomain = d;
            break;

        default:
            yDomain = SAMPLE_DOMAIN;

        }
    }

    public int getYDomain() {
        return yDomain;
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

    // **************** operations *****************

    /**
     * transforms segment-points to complementary segment-points (1-x). this is useful when using percent-y-domain.
     */
    public void setComplementary() {
        for (int i = 0; i < segmentPoints.size(); i++) {
            FPoint p = segmentPoints.get(i);
            p.y = 1 - p.y;
        }
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

            switch (yDomain) {
            case PERCENT_DOMAIN:
                y = actualChannelPlotter.graphToPercentY(p.y);
                break;

            default:
                y = actualChannelPlotter.graphToSampleY(p.y);
                break;
            }
        }

        public Point toGraphicDomain() {
            int xx, yy;

            xx = actualChannelPlotter.sampleToGraphX(x);

            switch (yDomain) {
            case PERCENT_DOMAIN:
                yy = actualChannelPlotter.percentToGraphY(y);
                break;

            default:
                yy = actualChannelPlotter.sampleToGraphY(y);
                break;
            }
            return new Point(xx, yy);
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

    }

    // ***************** objetcs ********************

    // the editable segments uses a vector of points
    private List<FPoint> segmentPoints;

    private int activePointIndex;

    private FPoint selectedPoint, leftPoint, rightPoint;

    private AOSpline spline;

    public float getActivePointX() {
        try {
            return segmentPoints.get(activePointIndex).x;
        } catch (Exception e) {
            return 0;
        }
    }

    public float getActivePointY() {
        try {
            return segmentPoints.get(activePointIndex).y;
        } catch (Exception e) {
            return 0;
        }
    }

    // operation
    private int actualOperation;

    private static final int IDLE = 0;

    private static final int DRAW = 1;

    private static final int REMOVE = 2;

    // segment mode
    private int segmentMode;

    public static final int SINGLE_POINTS = AOSegmentGenerator.SINGLE_POINTS;

    public static final int ORDER_0 = AOSegmentGenerator.ORDER_0;

    public static final int ORDER_1 = AOSegmentGenerator.ORDER_1;

    public static final int SPLINE = AOSegmentGenerator.SPLINE;

    public void setSegmentMode(int sm) {
        segmentMode = sm;
    }

    public int getSegmentMode() {
        return segmentMode;
    }

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
     * returns nearest segment point index relative to p or -1 if not near enough
     */
    private int searchNearest(FPoint p) {
        final int catchNearLimit = 400;
        float nearestValue = Float.MAX_VALUE;
        int nearestIndex = -1;

        // all segment points...
        for (int i = 0; i < segmentPoints.size(); i++) {
            // square of distance
            float n = p.getGraphSquareDistance(segmentPoints.get(i));

            // is nearer ?
            if ((n < nearestValue) && (n < catchNearLimit)) {
                nearestValue = n;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    /**
     * returns the index where the point p would be inserted
     */
    private int searchInsertIndex(FPoint p) {
        // all segment points...
        for (int i = 0; i < segmentPoints.size(); i++) {
            // is here ?
            if (p.x < segmentPoints.get(i).x) {
                return i;
            }
        }
        return segmentPoints.size();
    }

    /**
     * reset segment points
     */
    public void clear() {
        segmentPoints.clear();
    }

    /**
     * returns the current number of points of the edited segment-line
     */
    public int getNumberOfPoints() {
        return segmentPoints.size();
    }

    /**
     * adds a point with the given coordinates in sample-domain
     */
    public void addPoint(float x, float y) {
        segmentPoints.add(new FPoint(x, y));
    }

    /**
     * get the x-coordinate of point at index index
     */
    public float getPointX(int index) {
        return segmentPoints.get(index).x;
    }

    /**
     * get the y-coordinate of point at index index
     */
    public float getPointY(int index) {
        return segmentPoints.get(index).y;
    }

    // *************** results written into channel *****************

    /**
     * writes the samples in the channel
     */
    public void convertToSamples(int operation) {
        if (segmentPoints.size() > 0) {
            // converts points to arrays
            float x[] = new float[segmentPoints.size()];
            float y[] = new float[x.length];

            for (int i = 0; i < x.length; i++) {
                x[i] = segmentPoints.get(i).x;
                y[i] = segmentPoints.get(i).y;
            }

            // operate
            AChannelSelection chs = actualChannel.createChannelSelection();
            chs.operateChannel(new AOSegmentGenerator(x, y, segmentMode, operation, true));
        }
    }

    /**
     * writes the selected samples in the channel
     */
    public void convertToSelectedSamples(int operation) {
        if (segmentPoints.size() > 0) {
            // converts points to arrays
            float x[] = new float[segmentPoints.size()];
            float y[] = new float[x.length];

            for (int i = 0; i < x.length; i++) {
                x[i] = segmentPoints.get(i).x;
                y[i] = segmentPoints.get(i).y;
            }

            // operate
            AChannelSelection chs = actualChannel.getChannelSelection();
            chs.operateChannel(new AOSegmentGenerator(x, y, segmentMode, operation, false));
        }
    }

    // *************** results without channel *****************

    private float x[], y[];

    /**
     * prepares the results, given through getSample() afterwards.
     */
    public void prepareResults() {
        // converts points into x/y arrays
        x = new float[segmentPoints.size()];
        y = new float[segmentPoints.size()];
        for (int i = 0; i < x.length; i++) {
            x[i] = segmentPoints.get(i).x;
            y[i] = segmentPoints.get(i).y;
        }

        AOSpline spline = AOToolkit.createSpline();
        spline.load(x, y);
    }

    /**
     * returns the sample
     */
    public float getSample(int index) {
        switch (segmentMode) {
        case SINGLE_POINTS:
            for (int i = 0; i < x.length; i++) {
                if (index == (int) x[i]) {
                    return y[i];
                }
            }
            return 0;

        case ORDER_0:
            return AOToolkit.interpolate0(x, y, index);

        case ORDER_1:
            return AOToolkit.interpolate1(x, y, index);

        case SPLINE:
            return spline.getResult(index);
        }
        return 0;
    }

    /**
     * mouse events
     */

    public void mousePressed(MouseEvent e) {
        if ((actualChannel != null) && editable) {
            // actualChannelRect = actualChannelPlotter.getRectangle();

            setXRange(actualChannelPlotter.getXOffset(), actualChannelPlotter.getXLength());
            /*
             * setYRange( actualChannelPlotter.getYOffset(), actualChannelPlotter.getYLength() );
             */
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

            // draw ?
            if (actualOperation == DRAW) {
                int i = searchNearest(p);

                // is there a point selected ?
                if (i >= 0) {
                    actualOperation = DRAW;
                    selectedPoint = segmentPoints.get(i);

                    // left
                    if (i > 0) {
                        leftPoint = segmentPoints.get(i - 1);
                    } else {
                        leftPoint = null;
                    }

                    // right
                    if (i < segmentPoints.size() - 1) {
                        rightPoint = segmentPoints.get(i + 1);
                    } else {
                        rightPoint = null;
                    }
                }
                // no point selected ?
                else {
                    actualOperation = IDLE;
                }
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        if ((actualChannel != null) && editable) {
            // actualChannelRect = actualChannelPlotter.getRectangle();
            FPoint p = new FPoint();
            p.toSampleDomain(e.getPoint());
            p.limitRange();
            activePointIndex = searchNearest(p);
        }

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

    public void mouseDragged(MouseEvent e) {
        if ((actualChannel != null) && editable) {
            FPoint p = new FPoint();
            p.toSampleDomain(e.getPoint());
            p.limitRange();
            // System.out.println("mouse dragged "+p.x+" "+p.y);
            // System.out.println("mouse dragged min="+yMin+" max="+yMax);

            // draw ?
            if (actualOperation == DRAW) {
                // neighbour range limitation
                if ((leftPoint != null) && (p.x < leftPoint.x + 1))
                    selectedPoint.x = leftPoint.x + 1;
                else if ((rightPoint != null) && (p.x > rightPoint.x - 1))
                    selectedPoint.x = rightPoint.x - 1;
                else
                    selectedPoint.x = p.x;

                // lock point to curve mode ?
                if (GToolkit.isCtrlKey(e)) {
                    selectedPoint.y = actualChannel.sample[(int) p.x];
                }
                // free mode ?
                else {
                    selectedPoint.y = p.y;
                }
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        if ((actualChannel != null) && editable) {
            FPoint p = new FPoint();
            p.toSampleDomain(e.getPoint());
            p.limitRange();

            // which operation ?
            switch (actualOperation) {
            case IDLE:
            case DRAW:
                segmentPoints.add(searchInsertIndex(p), p);
                break;

            case REMOVE:
                int i = searchNearest(p);
                // near enough ?
                if (i >= 0) {
                    segmentPoints.remove(i);
                }
                break;
            }
        }
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

    private Stroke segmentSroke;

    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
        if (actualChannel != null) {
            try {
                // draw segments
                g2d.setStroke(segmentSroke);
                g2d.setColor(color);
                actualChannelPlotter = actualChannel.getChannelPlotter();
                actualChannelRect = actualChannelPlotter.getRectangle();
                g2d.setClip(actualChannelRect.x - 5, actualChannelRect.y - 5, actualChannelRect.width + 10, actualChannelRect.height + 10);

                switch (segmentMode) {
                case SINGLE_POINTS:
                    int y0 = actualChannelPlotter.sampleToGraphY(0);
                    for (int i = 0; i < segmentPoints.size(); i++) {
                        Point a = segmentPoints.get(i).toGraphicDomain();
                        g2d.drawLine(a.x, a.y, a.x, y0);
                    }
                    break;

                case ORDER_0:
                    for (int i = 0; i < segmentPoints.size() - 1; i++) {
                        Point a = segmentPoints.get(i).toGraphicDomain();
                        Point b = segmentPoints.get(i + 1).toGraphicDomain();
                        g2d.drawLine(a.x, a.y, b.x, a.y);
                        g2d.drawLine(b.x, a.y, b.x, b.y);
                    }
                    break;

                case ORDER_1:
                    for (int i = 0; i < segmentPoints.size() - 1; i++) {
                        Point a = segmentPoints.get(i).toGraphicDomain();
                        Point b = segmentPoints.get(i + 1).toGraphicDomain();
                        g2d.drawLine(a.x, a.y, b.x, b.y);
                    }
                    break;

                case SPLINE: {
                    int l = segmentPoints.size();
                    if (l > 3) {
                        float x[] = new float[l];
                        float y[] = new float[l];
                        for (int i = 0; i < x.length; i++) {
                            Point p = segmentPoints.get(i).toGraphicDomain();
                            x[i] = p.x;
                            y[i] = p.y;
                        }
                        spline.load(x, y);

                        int xLeft = Math.max(rect.x, (int) x[0]);
                        int xRight = Math.min(rect.x + rect.width, (int) x[x.length - 1]);

                        int oldX = xLeft;
                        int oldY = (int) spline.getResult(xLeft);
                        for (int i = xLeft + 1; i < xRight; i += 3) {
                            int newX = i;
                            int newY = (int) spline.getResult(i);
                            g2d.drawLine(oldX, oldY, newX, newY);
                            oldX = newX;
                            oldY = newY;
                        }
                    }
                }
                    break;
                }

                // draw points
                if (pointsVisible) {
                    for (int i = 0; i < segmentPoints.size(); i++) {
                        Point a = segmentPoints.get(i).toGraphicDomain();

                        if ((i == activePointIndex) && highlightActivePoint) {
                            g2d.setColor(Color.red);
                        } else {
                            g2d.setColor(color);
                        }
                        g2d.fillRect(a.x - 2, a.y - 2, 5, 5);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

}
