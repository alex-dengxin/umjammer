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

import java.awt.AlphaComposite;
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


/**
 * reusable component to show, edit and apply rectangle-shapes and
 * rectangle-brushed lines on channels. these curves
 * can be made fully graphical, with the use of the mouse.
 * 
 * mouse action table of freehand mode:
 * ---------------------------------------------------------
 *  press-drag-release click
 * ---------------------------------------------------------
 *  - draw - shift - - ctrl - -
 * ---------------------------------------------------------
 * 
 * @version olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 09.03.02 first draft oli4 <br>
 *          18.03.02 rectangle shapes and lines merged into one class oli4 <br>
 */
public class GEditableArea {
    public GEditableArea() {
        rectangles = new ArrayList<FRectangle>(100);

        stroke = new BasicStroke();
        color = Color.yellow;

        actualOperation = IDLE;
        editable = true;
        mode = RECTANGLE;

        clearXRange();
        clearYRange();

        penCursor = GToolkit.createCustomCursor(this, "brushCursor");
        eraseCursor = GToolkit.createCustomCursor(this, "eraseCursor");
        actualCursor = penCursor;
    }

    public GEditableArea(GEditableArea a) {
        this();
        color = a.color;
        editable = a.editable;
        brushSize = a.brushSize;
        mode = a.mode;
        xMin = a.xMin;
        xMax = a.xMax;
        yMin = a.yMin;
        yMax = a.yMax;
        currentX = a.currentX;
        currentY = a.currentY;

        for (int i = 0; i < a.rectangles.size(); i++) {
            rectangles.add(new FRectangle(a.rectangles.get(i)));
        }

        actualChannel = a.actualChannel;
    }

    public GEditableArea(AChannel ch) {
        this();
        setChannel(ch);
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

    private int brushSize;

    public void setBrushSize(int s) {
        brushSize = s;
    }

    public int getBrushSize() {
        return brushSize;
    }

    public static final int RECTANGLE = 1;

    public static final int LINE = 2;

    private int mode;

    public void setMode(int m) {
        mode = m;
    }

    public int getMode() {
        return mode;
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

    // ***************** current mouse position **************

    private float currentX, currentY;

    public float getCurrentX() {
        return currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    // ****************** point definition *******************

    private class FPoint {
        public float x, y;

        public FPoint() {
            this.x = 0;
            this.y = 0;
        }

        public FPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public FPoint(FPoint p) {
            this.x = p.x;
            this.y = p.y;
        }

        public void toSampleDomain() {
            x = actualChannelPlotter.graphToSampleX((int) x);
            y = actualChannelPlotter.graphToSampleY((int) y);
        }

        public Point toGraphicDomain() {
            return new Point(actualChannelPlotter.sampleToGraphX(x), actualChannelPlotter.sampleToGraphY(y));
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

    private class FRectangle {
        public FRectangle(int x1, int y1, int x2, int y2) {
            topLeft = new FPoint(Math.min(x1, x2), Math.min(y1, y2));
            bottomRight = new FPoint(Math.max(x1, x2), Math.max(y1, y2));
        }

        public FRectangle(FRectangle r) {
            topLeft = new FPoint(r.topLeft);
            bottomRight = new FPoint(r.bottomRight);
        }

        public void toSampleDomain() {
            topLeft.toSampleDomain();
            bottomRight.toSampleDomain();
        }

        public Rectangle toGraphicDomain() {
            Point tl = topLeft.toGraphicDomain();
            Point br = bottomRight.toGraphicDomain();
            return new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
        }

        FPoint topLeft, bottomRight;

        public boolean isInside(float x, float y) {
            return ((x > topLeft.x) && (y < topLeft.y) && (x < bottomRight.x) && (y > bottomRight.y));
        }

    }

    // ***************** objetcs ********************

    // the editable curve uses a vector of points
    private List<FRectangle> rectangles;

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
     * reset segment points
     */
    public void clear() {
        rectangles.clear();
    }

    /**
     * set full selection
     */
    public void full() {
        try {
            actualChannelPlotter = actualChannel.getChannelPlotter();
            actualChannelRect = actualChannelPlotter.getRectangle();
            FRectangle r = new FRectangle(actualChannelRect.x, actualChannelRect.y, actualChannelRect.x + actualChannelRect.width, actualChannelRect.y + actualChannelRect.height);
            r.toSampleDomain();
            rectangles.clear();
            rectangles.add(r);
        } catch (Exception e) {
        }
    }

    /**
     * returns the current number of rectangles
     */
    public int getNumberOfRectangles() {
        return rectangles.size();
    }

    public boolean isInside(float x, float y) {
        for (int i = 0; i < rectangles.size(); i++) {
            if (rectangles.get(i).isInside(x, y)) {
                return true;
            }
        }
        return false;
    }

    public float getXMin() {
        float m = Float.MAX_VALUE;
        for (int i = 0; i < rectangles.size(); i++) {
            float v = rectangles.get(i).topLeft.x;
            if (v < m) {
                m = v;
            }
        }
        return m;
    }

    public float getXMax() {
        float m = Float.MIN_VALUE;
        for (int i = 0; i < rectangles.size(); i++) {
            float v = rectangles.get(i).bottomRight.x;
            if (v > m) {
                m = v;
            }
        }
        return m;
    }

    public float getYMin() {
        float m = Float.MAX_VALUE;
        for (int i = 0; i < rectangles.size(); i++) {
            float v = rectangles.get(i).bottomRight.y;
            if (v < m) {
                m = v;
            }
        }
        return m;
    }

    public float getYMax() {
        float m = Float.MIN_VALUE;
        for (int i = 0; i < rectangles.size(); i++) {
            float v = rectangles.get(i).topLeft.y;
            if (v > m) {
                m = v;
            }
        }
        return m;
    }

    private int x1, x2, y1, y2;

    /**
     * mouse events
     */
    public void mousePressed(MouseEvent e) {
        if ((actualChannel != null) && editable) {
            actualChannelPlotter = actualChannel.getChannelPlotter();
            actualChannelRect = actualChannelPlotter.getRectangle();

            setXRange(actualChannelPlotter.getXOffset(), actualChannelPlotter.getXLength());
            setYRange(actualChannelPlotter.getYOffset(), actualChannelPlotter.getYLength());

            // operation...
            if (GToolkit.isShiftKey(e)) {
                actualOperation = REMOVE;
            } else {
                actualOperation = DRAW;
            }

            switch (mode) {
            case RECTANGLE:
                x1 = e.getPoint().x;
                y1 = e.getPoint().y;
                x2 = x1;
                y2 = y1;
                break;

            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        currentX = actualChannelPlotter.graphToSampleX(e.getPoint().x);
        currentY = actualChannelPlotter.graphToSampleY(e.getPoint().y);

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
            currentX = actualChannelPlotter.graphToSampleX(e.getPoint().x);
            currentY = actualChannelPlotter.graphToSampleY(e.getPoint().y);

            switch (mode) {
            case RECTANGLE:
                x2 = e.getPoint().x;
                y2 = e.getPoint().y;
                break;

            case LINE:
                if (actualOperation == DRAW) {
                    // operation
                    x1 = e.getPoint().x - brushSize / 2;
                    y1 = e.getPoint().y - brushSize / 2;
                    x2 = x1 + brushSize;
                    y2 = y1 + brushSize;

                    FRectangle r = new FRectangle(x1, y1, x2, y2);
                    r.toSampleDomain();
                    rectangles.add(r);
                }
                break;
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        if ((actualChannel != null) && editable) {
            switch (mode) {
            case RECTANGLE:
                if (actualOperation == DRAW) {
                    x2 = e.getPoint().x;
                    y2 = e.getPoint().y;

                    FRectangle r = new FRectangle(x1, y1, x2, y2);
                    r.toSampleDomain();
                    rectangles.add(r);
                }
                break;
            }

            actualOperation = IDLE;
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
                g2d.setClip(actualChannelRect);

                // draw existing rectangles
                g2d.setStroke(stroke);
                g2d.setColor(color);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .18f));

                // selection...
                for (int i = 0; i < rectangles.size(); i++) {
                    Rectangle r = rectangles.get(i).toGraphicDomain();
                    g2d.fillRect(r.x, r.y, r.width, r.height);
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));

                switch (mode) {
                case RECTANGLE:
                    // draw current elastic rectangle
                    if (actualOperation == DRAW) {
                        g2d.setStroke(stroke);
                        g2d.setColor(Color.white);
                        g2d.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
                    }
                    break;
                }

            } catch (Exception e) {
            }
        }
    }

}
