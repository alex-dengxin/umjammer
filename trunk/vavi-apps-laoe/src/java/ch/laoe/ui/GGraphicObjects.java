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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelPlotter;
import ch.oli4.io.XmlInputStream;
import ch.oli4.io.XmlOutputStream;


/**
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * utility to draw pure graphic objects onto a channel.
 * these objects have no special relations to the samples, they
 * are just for graphical documentation.
 * 
 * mouse action table in pen-mode:
 * ---------------------------------------------------------
 *  press-drag-release click
 * ---------------------------------------------------------
 *  - draw - shift - append a line ctrl - -
 * ---------------------------------------------------------
 * 
 * mouse action table in rubber-mode:
 * ---------------------------------------------------------
 *  press-drag-release click
 * ---------------------------------------------------------
 *  - erase near objects - shift - - ctrl - -
 * ---------------------------------------------------------
 * 
 * mouse action table in text-mode:
 * ---------------------------------------------------------
 *  press-drag-release click
 * ---------------------------------------------------------
 *  - move text place a new text shift - - ctrl - -
 * ---------------------------------------------------------
 * 
 * @version 07.05.2003 first draft oli4 <br>
 */
public class GGraphicObjects {
    public GGraphicObjects() {
        initCursors();
        initOperation();
        initGeometry();
    }

    public GGraphicObjects(GGraphicObjects o) {
        this();
        Iterator<GrObject> it = o.grObjects.iterator();
        while (it.hasNext()) {
            grObjects.add(it.next());
        }
    }

    // ********************** XML import/export **********************

    private static final String nameSpace = "graphicObjects";

    private static void xmlAppendPointElement(XmlOutputStream os, GrPoint p) throws IOException {
        Map<String, String> m = new HashMap<String, String>();
        m.put("x", Float.toString(p.x));
        m.put("y", Float.toString(p.y));
        os.appendBeginEndTag(nameSpace + ".point", (HashMap) m);
    }

    public void toXmlElement(XmlOutputStream os) throws IOException {
        os.appendBeginTag(nameSpace, new HashMap());
        os.appendCR();
        Iterator it = grObjects.iterator();
        while (it.hasNext()) {
            GrObject o = (GrObject) it.next();
            if (o instanceof GrPoint) {
                os.appendTab(1);
                xmlAppendPointElement(os, (GrPoint) o);
                os.appendCR();
            } else if (o instanceof GrRectangle) {
                GrRectangle r = (GrRectangle) o;
                os.appendTab(1);
                os.appendBeginTag(nameSpace + ".rectangle", new HashMap());
                os.appendCR();
                os.appendTab(2);
                xmlAppendPointElement(os, r.p1);
                os.appendCR();
                os.appendTab(2);
                xmlAppendPointElement(os, r.p2);
                os.appendCR();
                os.appendTab(1);
                os.appendEndTag(nameSpace + ".rectangle");
                os.appendCR();
            } else if (o instanceof GrLine) {
                GrLine l = (GrLine) o;
                os.appendTab(1);
                os.appendBeginTag(nameSpace + ".line", new HashMap());
                os.appendCR();
                Iterator lit = l.points.iterator();
                while (lit.hasNext()) {
                    os.appendTab(2);
                    xmlAppendPointElement(os, (GrPoint) lit.next());
                    os.appendCR();
                }
                os.appendTab(1);
                os.appendEndTag(nameSpace + ".line");
                os.appendCR();
            } else if (o instanceof GrText) {
                GrText t = (GrText) o;
                os.appendTab(1);
                Map<String, String> m = new HashMap<String, String>();
                m.put("text", t.text);
                os.appendBeginTag(nameSpace + ".text", (HashMap) m);
                os.appendCR();
                os.appendTab(2);
                xmlAppendPointElement(os, t.point);
                os.appendCR();
                os.appendTab(1);
                os.appendEndTag(nameSpace + ".text");
                os.appendCR();
            }
        }
        os.appendEndTag(nameSpace);
        os.appendCR();
    }

    public void fromXmlElement(XmlInputStream is) throws IOException {
        GrObject o = null;
        boolean first = false;

        while (true) {
            int t = is.read();

            switch (t) {
            case XmlInputStream.BEGIN_END_TAG:
                if (is.getTagName().equals(nameSpace + ".point")) {
                    GrPoint p = new GrPoint();
                    p.x = Float.parseFloat(is.getAttribute("x"));
                    p.y = Float.parseFloat(is.getAttribute("y"));

                    if (first) {
                        o.setOrigin(p);
                        first = false;
                    } else {
                        o.setPoint(p);
                    }
                }
                break;

            case XmlInputStream.BEGIN_TAG:
                first = true;
                if (is.getTagName().equals(nameSpace + ".rectangle")) {
                    o = new GrRectangle();
                } else if (is.getTagName().equals(nameSpace + ".line")) {
                    o = new GrLine();
                } else if (is.getTagName().equals(nameSpace + ".text")) {
                    o = new GrText(is.getAttribute("text"));
                }
                grObjects.add(o);
                break;

            case XmlInputStream.END_TAG:
                if (is.getTagName().equals(nameSpace)) {
                    return;
                }
                break;

            case XmlInputStream.EOF:
                throw new IOException("unexpected EOF in middle of XML graphicObject structure");
            }
        }
    }

    // ***************** geometry *********************

    private java.util.List<GrObject> grObjects;

    private GrObject currentObject = null;

    private abstract class GrObject {
        public GrObject() {
            color = Color.DARK_GRAY;
            stroke = new BasicStroke();
        }

        public GrObject(GrObject o) {
            color = o.color;
            stroke = o.stroke;
        }

        public Color color;

        public Stroke stroke;

        public abstract void setOrigin(GrPoint o);

        public abstract void setPoint(GrPoint p);

        public abstract boolean isInside(GrRectangle r);

        public void paint(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.setStroke(stroke);
        }
    }

    private class GrPoint extends GrObject {
        public GrPoint() {
        }

        public GrPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public GrPoint(GrPoint p) {
            super(p);
            p.x = x;
            p.y = y;
        }

        public float x, y;

        public int getGraphicX() {
            return channelPlotter.sampleToGraphX(x);
        }

        public int getGraphicY() {
            return channelPlotter.sampleToGraphY(y);
        }

        public void setOrigin(GrPoint o) {
            x = o.x;
            y = o.y;
        }

        public void setPoint(GrPoint p) {
            setOrigin(p);
        }

        public boolean isInside(GrRectangle r) {
            int x1 = r.p1.getGraphicX();
            int y1 = r.p1.getGraphicY();
            int x2 = r.p2.getGraphicX();
            int y2 = r.p2.getGraphicY();
            if (x2 < x1) {
                int x = x2;
                x2 = x1;
                x1 = x;
            }
            if (y2 < y1) {
                int y = y2;
                y2 = y1;
                y1 = y;
            }
            int x3 = getGraphicX();
            int y3 = getGraphicY();
            return (x3 >= x1) && (x3 <= x2) && (y3 >= y1) && (y3 <= y2);
        }

        public void paint(Graphics2D g2d) {
            super.paint(g2d);
            g2d.drawLine(getGraphicX(), getGraphicY(), getGraphicX(), getGraphicY());
        }
    }

    private class GrRectangle extends GrObject {
        public GrRectangle() {
        }

        public GrRectangle(GrRectangle r) {
            super(r);
            p1 = new GrPoint(r.p1);
            p2 = new GrPoint(r.p2);
        }

        public GrPoint p1, p2;

        public void setOrigin(GrPoint o) {
            p1 = o;
            p2 = o;
        }

        public void setPoint(GrPoint p) {
            p2 = p;
        }

        public boolean isInside(GrRectangle r) {
            return p1.isInside(r) && p2.isInside(r);
        }

        public void paint(Graphics2D g2d) {
            super.paint(g2d);
            int x1 = p1.getGraphicX();
            int y1 = p1.getGraphicY();
            int x2 = p2.getGraphicX();
            int y2 = p2.getGraphicY();
            g2d.drawLine(x1, y1, x1, y2);
            g2d.drawLine(x1, y1, x2, y1);
            g2d.drawLine(x2, y2, x1, y2);
            g2d.drawLine(x2, y2, x2, y1);
        }
    }

    private class GrLine extends GrObject {
        public GrLine() {
            points = new ArrayList<GrPoint>();
        }

        public GrLine(GrLine l) {
            super(l);
            points = new ArrayList<GrPoint>();
            Iterator it = l.points.iterator();
            while (it.hasNext()) {
                points.add(new GrPoint((GrPoint) it.next()));
            }
        }

        private java.util.List<GrPoint> points;

        public void setOrigin(GrPoint o) {
            points.add(o);
        }

        public void setPoint(GrPoint p) {
            points.add(p);
        }

        public boolean isInside(GrRectangle r) {
            Iterator it = points.iterator();
            while (it.hasNext()) {
                if (!((GrPoint) it.next()).isInside(r)) {
                    return false;
                }
            }
            return true;
        }

        public void paint(Graphics2D g2d) {
            super.paint(g2d);
            Iterator it = points.iterator();
            GrPoint previousP = null;
            while (it.hasNext()) {
                GrPoint p = (GrPoint) it.next();

                if (previousP != null) {
                    g2d.drawLine(previousP.getGraphicX(), previousP.getGraphicY(), p.getGraphicX(), p.getGraphicY());
                }
                previousP = p;
            }
        }
    }

    private class GrText extends GrObject {
        public GrText(String text) {
            this.text = text;
        }

        public GrText(GrText t) {
            super(t);
            text = t.text;
            point = new GrPoint(t.point);
        }

        private String text;

        private GrPoint point;

        public void setOrigin(GrPoint o) {
            point = o;
        }

        public void setPoint(GrPoint p) {
            point = p;
        }

        public boolean isInside(GrRectangle r) {
            return point.isInside(r);
        }

        public void paint(Graphics2D g2d) {
            super.paint(g2d);
            g2d.setFont(new Font("Courrier", Font.PLAIN, 12));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(text, point.getGraphicX() - fm.stringWidth(text) / 2, point.getGraphicY() + fm.getAscent() - fm.getHeight() / 2);
        }
    }

    private void initGeometry() {
        grObjects = new ArrayList<GrObject>();
        currentObject = null;
    }

    // ***************** graphics *********************

    private Cursor penCursor, eraseCursor, textCursor, actualCursor;

    private void initCursors() {
        penCursor = GToolkit.createCustomCursor(this, "penCursor");
        eraseCursor = GToolkit.createCustomCursor(this, "eraseCursor");
        textCursor = new Cursor(Cursor.TEXT_CURSOR);
        actualCursor = null;
    }

    private Color currentColor = Color.DARK_GRAY;

    private Stroke currentStroke = new BasicStroke();

    public void setCurrentColor(Color c) {
        currentColor = c;
    }

    public void setCurrentStroke(Stroke s) {
        currentStroke = s;
    }

    // ***************** channel *********************

    private AChannel channel;

    private AChannelPlotter channelPlotter;

    /**
     * set the layer-model
     */
    public void setChannel(AChannel ch) {
        channel = ch;
        channelPlotter = ch.getChannelPlotter();
    }

    // ***************** paint *********************

    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
        if (channel != null) {
            try {
                channelPlotter = channel.getChannelPlotter();
                g2d.setClip(channelPlotter.getRectangle());

                Iterator it = grObjects.iterator();
                while (it.hasNext()) {
                    GrObject o = (GrObject) it.next();
                    if (o != null) {
                        o.paint(g2d);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int operation;

    public static final int NO = 0;

    public static final int DRAW_LINE = 1;

    public static final int DRAW_RECTANGLE = 2;

    public static final int DRAW_TEXT = 3;

    public static final int ERASE = 4;

    private void initOperation() {
        operation = NO;
    }

    public void setOperation(int o) {
        this.operation = o;
    }

    private String currentText = "";

    public void setCurrentText(String text) {
        this.currentText = text;
    }

    /**
     * mouse events
     */

    public void mousePressed(MouseEvent e) {
        if (channel != null) {
            channelPlotter = channel.getChannelPlotter();

            if (channelPlotter.isInsideChannel(e.getPoint())) {
                float x = channelPlotter.graphToSampleX(e.getX());
                float y = channelPlotter.graphToSampleY(e.getY());

                switch (operation) {
                case DRAW_LINE:
                    currentObject = new GrLine();
                    currentObject.setOrigin(new GrPoint(x, y));
                    currentObject.color = currentColor;
                    currentObject.stroke = currentStroke;
                    break;

                case DRAW_RECTANGLE:
                    currentObject = new GrRectangle();
                    currentObject.setOrigin(new GrPoint(x, y));
                    currentObject.color = currentColor;
                    currentObject.stroke = currentStroke;
                    break;

                case DRAW_TEXT:
                    currentObject = new GrText(currentText);
                    currentObject.setOrigin(new GrPoint(x, y));
                    currentObject.color = currentColor;
                    currentObject.stroke = currentStroke;
                    break;

                case ERASE:
                    currentObject = new GrRectangle();
                    float dash[] = {
                        4.f, 4.f
                    };
                    currentObject.setOrigin(new GrPoint(x, y));
                    currentObject.color = Color.WHITE;
                    currentObject.stroke = new BasicStroke(1.f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.f, dash, 0.f);
                    break;
                }
                grObjects.add(currentObject);
            } else {
                currentObject = null;
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        Cursor c = null;

        switch (operation) {
        case DRAW_LINE:
            c = penCursor;
            break;

        case DRAW_RECTANGLE:
            c = penCursor;
            break;

        case DRAW_TEXT:
            c = textCursor;
            break;

        case ERASE:
            c = eraseCursor;
            break;
        }

        // change detector...
        if (c != actualCursor) {
            actualCursor = c;
            ((Component) e.getSource()).setCursor(actualCursor);
        }
    }

    private int oldDraggedX = 0;

    private int oldDraggedY = 0;

    public void mouseDragged(MouseEvent e) {
        if ((channel != null) && (currentObject != null)) {
            if (channelPlotter.isInsideChannel(e.getPoint())) {
                int x = e.getX();
                int y = e.getY();

                if ((x != oldDraggedX) || (y != oldDraggedY)) {
                    oldDraggedX = x;
                    oldDraggedY = y;

                    switch (operation) {
                    case DRAW_LINE:
                        break;

                    case DRAW_RECTANGLE:
                        break;

                    case DRAW_TEXT:
                        break;

                    case ERASE:
                        break;
                    }

                    if (GToolkit.isShiftKey(e)) {
                    } else if (GToolkit.isCtrlKey(e)) {
                    } else {
                        currentObject.setPoint(new GrPoint(channelPlotter.graphToSampleX(e.getX()), channelPlotter.graphToSampleY(e.getY())));
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (channel != null) {
//            int x = e.getX();
//            int y = e.getY();

            switch (operation) {
            case DRAW_LINE:
                break;

            case DRAW_RECTANGLE:
                break;

            case DRAW_TEXT:
                break;

            case ERASE:
                int s = grObjects.size();
                for (int i = s - 1; i >= 0; i--) {
                    GrObject o = grObjects.get(i);
                    if (o.isInside((GrRectangle) currentObject)) {
                        grObjects.remove(o);
                    }
                }
                grObjects.remove(currentObject);
                break;
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        try {
            channelPlotter = channel.getChannelPlotter();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        ((Component) e.getSource()).setCursor(actualCursor);
    }

}
