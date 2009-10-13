/*
 * Copyright (c) 2004 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.microedition.util;

import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Graphics;


/**
 * StringUtil.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version 0.00 040208 nsano initial version <br>
 */
public final class StringUtil {

    /** */
    static int STR_TOP_ADJUST;

    /** */
    static int LINE_HEIGHT;

    /** */
    static Font font;

    /**
     * 文字列を描画します。\n で折り返します。
     *
     * @param g グラフィックオブジェクト
     * @param l 描画する文
     * @param x 開始 x 座標
     * @param y 開始 y 座標
     * @param w 折り返し幅 (pixel)
     * @param sl 開始行数
     * @param ml 最大描画行数
     */
    public static void drawSentence(Graphics g,
                                    String l,
                                    int x,
                                    int y,
                                    int w,
                                    int sl,
                                    int ml) {
        int sp = 0;
        int p;
        int p2;
        int pn;
        int line = 0;

        pn = l.indexOf("\n");

        while (l.length() > sp) {
            p = font.getLineBreak(l, sp, l.length() - sp, w);

            if ((pn >= sp) && (pn <= p)) {
                p = pn;
                p2 = pn + 1;
                pn = l.indexOf("\n", p2);
            } else {
                p2 = p;
            }

            if (line >= sl) {
                g.drawString(l.substring(sp, p), x, y + STR_TOP_ADJUST);
                y += LINE_HEIGHT;

                if (--ml <= 0) {
                    return;
                }
            }

            line++;
            sp = p2;
        }
    }

    /**
     * drawSentence で書くと何行になるかを調べます。
     *
     * @param l 描画する文
     * @param w 折り返し幅(pixel)
     * @return 行数
     */
    public static int getSentenceLine(String l, int w) {
        int sp = 0;
        int p;
        int p2;
        int pn;
        int line = 0;

        pn = l.indexOf("\n");

        while (l.length() > sp) {
            p = font.getLineBreak(l, sp, l.length() - sp, w);

            if ((pn >= sp) && (pn <= p)) {
                p = pn;
                p2 = pn + 1;
                pn = l.indexOf("\n", p2);
            } else {
                p2 = p;
            }

            line++;
            sp = p2;
        }

        return line;
    }

    /**
     *
     */
    public static String limitedString(String str, int width) {
        if (str.length() == 0) {
            return "";

            // SO 系では str.length() == 0 のときに
            // substring が Exceptionを吐く
        }

        int limitedPos = font.getLineBreak(str, 0, str.length(), width);

        if (limitedPos == str.length()) {
            return str;
        } else {
            return (str.substring(0, limitedPos) + "..");
        }
    }

    /**
     *
     *
     */
    public static String fillNumber(int num, int digit, String fill) {
        String formatStr = Integer.toString(num);

        if (num < 0) {
            return formatStr;
        }

        if (formatStr.length() > digit) {
            return formatStr.substring(formatStr.length() - digit);
        }

        for (int i = formatStr.length(); i < digit; i++) {
            formatStr = fill + formatStr;
        }

        return formatStr;
    }
}

/* */
