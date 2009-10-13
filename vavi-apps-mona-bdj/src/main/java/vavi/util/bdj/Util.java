/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.bdj;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Util. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080902 nsano initial version <br>
 */
public class Util {

    /** */
    public static String toPlainText(String text) {
//System.err.println("++++++++++++++++ text: " + text.length() + " ++++++++++++++++");
//System.err.println("TEXT: " + text);
        text = replace(text, "<br>", "\n");
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
        text = replace(text, "</a>", "");
        text = replace(text, "&gt;", ">");
        text = replace(text, "&lt;", "<");
        text = replace(text, "&nbsp;", " ");
        return text;
    }

    /** */
    public static String replace(String string, String target, String replacement) {
        int p;
        int q = 0;
        while ((p = string.indexOf(target, q)) != -1) {
//System.err.println("Åö 1: " + text.substring(0, p) + "\nÅö 2: " + text.substring(q + 1));
            string = string.substring(0, p) + replacement + string.substring(p + target.length());
            q = p + replacement.length();
        }
        return string;
    }

    private static final Map platforms = new HashMap();

    public static final String BDJ_PS3 = "PS3"; 
    public static final String BDJ_POWER_DVD = "PowerDVD"; 
    public static final String BDJ_DMR_BW800 = "Panasonic DMR-BW800"; 
    public static final String BDJ_UNKNOWN = "unknown"; 

    static {
        platforms.put("/VP/", "PS3");
    }

    public static String getPlatform() {
        String value = System.getProperty("bluray.vfs.root");
        if (platforms.containsKey(value)) {
            return (String) platforms.get(value);
        } else {
            return BDJ_UNKNOWN;
        }
    }

    public static List getStackTraceString(Throwable t) {
        List lines = new ArrayList();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            t.printStackTrace(ps);
            String stackTraces = baos.toString();
            StringTokenizer st = new StringTokenizer(stackTraces, "\n");
            while (st.hasMoreTokens()) {
                lines.add(st.nextToken());
            }
        } catch (Exception e) {
e.printStackTrace(System.err);
lines.add("X:" + e);
        }
        return lines;
    }
}

/* */
