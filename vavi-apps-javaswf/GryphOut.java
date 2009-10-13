/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;

import com.anotherbigidea.flash.movie.Shape;


/**
 * GryphOut.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070618 nsano initial version <br>
 */
public class GryphOut {
    private final FontRenderContext frc;

    private Font font = java.awt.Font.decode(null);

    public GryphOut() {
        this.frc = createFontRenderContext();
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void draw(String text, float x, float y, PathDrawer drawer) {
        GlyphVector gv = font.createGlyphVector(frc, text.toCharArray());
        for (int i = 0; i < text.length(); i++) {
            java.awt.Shape s = gv.getGlyphOutline(i, x, y);
            // FlatteningPathIterator で平坦化。
            // SEG_QUADTO や SEG_CUBICTO を無くして
            // SEG_MOVETO, SEG_LINETO, SEG_CLOSE だけにする。
            PathIterator pi = s.getPathIterator(null);
            pi = new FlatteningPathIterator(pi, 0.1);
            drawPath(pi, drawer);
        }
    }

    private static FontRenderContext createFontRenderContext() {
        // FontRenderContext frc = new FontRenderContext( null, true, false );
        BufferedImage bi = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        // g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        // RenderingHints.VALUE_ANTIALIAS_ON);
        // g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        // RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontRenderContext frc = g.getFontRenderContext();
        return frc;
    }

    /**
     * PathIterator オブジェクトの構成点を出力する。
     */
    private static void drawPath(PathIterator pi, PathDrawer drawer) {

        while (!pi.isDone()) {
            double coords[] = new double[6];
            int type = pi.currentSegment(coords);
            switch (type) {
            case PathIterator.SEG_CLOSE:
                // System.err.println("CLOSE :");
                break;
            case PathIterator.SEG_CUBICTO:
                // System.err.println("CUBICTO :");
                break;
            case PathIterator.SEG_LINETO:
                // System.err.println("LINETO :");
                drawer.line(coords[0], coords[1]);
                break;
            case PathIterator.SEG_MOVETO:
                // System.err.println("MOVETO :");
                drawer.move(coords[0], coords[1]);
                break;
            case PathIterator.SEG_QUADTO:
                // System.err.println("QUADTO :");
                drawer.quad(coords[0], coords[1], coords[2], coords[3]);
                break;
            }
            // System.err.print(coords[0]); System.err.print(",");
            // System.err.print(coords[1]); System.err.print(",");
            // System.err.print(coords[2]); System.err.print(",");
            // System.err.print(coords[3]); System.err.print(",");
            // System.err.print(coords[4]); System.err.print(",");
            // System.err.print(coords[5]); System.err.println();
            pi.next();
        }
    }

    public static interface PathDrawer {
        void move(double x, double y);

        void line(double x, double y);

        void quad(double x, double y, double cx, double cy);
    }

    public static class SwfPathDrawer implements PathDrawer {

        private final Shape shape;

        public SwfPathDrawer(Shape shape) {
            this.shape = shape;
        }

        public void move(double x, double y) {
            shape.move(x, y);
        }

        public void line(double x, double y) {
            shape.line(x, y);
        }

        public void quad(double x, double y, double cx, double cy) {
            // これはダメ:ギザギザになってしまう
            // quad をどうやったら curve の引数に変換できるんだろうか？
            shape.curve(x, y, cx, cy);
        }
    }
}

/* */
