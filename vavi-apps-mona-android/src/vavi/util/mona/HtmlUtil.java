/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.mona;


/**
 * HtmlUtil. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/01/22 nsano initial version <br>
 */
final class HtmlUtil {

    private HtmlUtil() {
    }

    /** */
    public static String toPlainText(String text) {
//System.err.println("++++++++++++++++ text: " + text.length() + " ++++++++++++++++");
//System.err.println("TEXT: " + text);
        text = text.replace("<br>", "\n");
        int p;
        int q = 0;
        while ((p = text.indexOf("<a href=", q)) != -1) {
            q = text.indexOf(">", p + 8);
            if (q != -1) {
//System.err.println("Åö 1: " + text.substring(0, p) + "\nÅö 2: " + text.substring(q + 1));
                text = text.substring(0, p) + text.substring(q + 1);
                q++;
            }
        }
        text = text.replace("</a>", "");
        text = text.replace("&gt;", ">");
        text = text.replace("&lt;", "<");
        text = text.replace("&nbsp;", " ");
        return text;
    }
}

/* */
