/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.microedition.lcdui;

/**
 * Text.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040920 nsano initial version <br>
 */
class Text {

    public static final int TRUNCATE = 1;
    public static final int INVERT = 2;
    public static final int NORMAL = 4;
    public static int FG_COLOR;
    public static int FG_H_COLOR;
    public static Image HYPERLINK_IMG;
    public static final int HYPERLINK = 8;
    public static final int PAINT_USE_CURSOR_INDEX = 16;
    public static final int PAINT_GET_CURSOR_INDEX = 32;

    /**
     * @param cs
     * @param textOffset
     * @param i
     * @param fnt
     */
    public static int getWidestLineWidth(char[] cs, int textOffset, int i, Font fnt) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param string
     * @param fnt
     * @param g
     * @param elWidth
     * @param i
     * @param textOffset
     * @param j
     * @param object
     */
    public static int paint(String string, Font fnt, Graphics g, int elWidth, int i, int textOffset, int j, Object object) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param string
     * @param fnt
     * @param width
     * @param textOffset
     */
    public static int getHeightForWidth(String string, Font fnt, int width, int textOffset) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param label
     * @param str
     * @param label_font
     * @param font
     * @param i
     * @param label_pad
     */
    public static int getTwoStringsWidth(String label, String str, Font label_font, Font font, int i, int label_pad) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param label
     * @param str
     * @param label_font
     * @param font
     * @param i
     * @param label_pad
     */
    public static int getTwoStringsHeight(String label, String str, Font label_font, Font font, int i, int label_pad) {
        // TODO Auto-generated method stub
        return 0;
    }
}

/* */
