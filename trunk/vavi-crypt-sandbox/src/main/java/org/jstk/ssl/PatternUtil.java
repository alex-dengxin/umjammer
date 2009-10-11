/*
 * @(#) $Id: PatternUtil.java,v 1.1.1.1 2003/10/05 18:39:22 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import org.jstk.JSTKOptions;
import java.util.HashMap;
import java.util.Map;


public class PatternUtil {
    private String pattern = null;

    private byte[] patbuf = null;

    private int patidx = 0;

    private boolean matched = true;

    public PatternUtil(String pattern) {
        this.pattern = pattern;
        if (pattern != null)
            patbuf = pattern.getBytes();
    }

    public boolean needToFill() {
        return (pattern != null);
    }

    public boolean needToMatch() {
        return (pattern != null);
    }

    public void fillPattern(JSTKBuffer jbuf) {
        if (pattern == null)
            return;
        // Same PatternUtil could be applied on multiple buffers. PatternUtil remembers
        // state in patidx variable.
        jbuf.clear();
        int patlen = patbuf.length - patidx;
        int bufoff = 0;
        while (bufoff + patlen <= jbuf.length()) {
            jbuf.putBytes(patbuf, patidx, patlen);
            bufoff += patlen;
            patidx = 0;
            patlen = patbuf.length - patidx;
        }
        if (bufoff < jbuf.length()) { // Some work is left.
            patlen = jbuf.length() - bufoff;
            jbuf.putBytes(patbuf, patidx, patlen);
            patidx = patlen;
        }
    }

    public boolean matchPattern(JSTKBuffer jbuf) {
        if (pattern == null)
            return true;
        byte[] buf = jbuf.getBytes();
        int patlen = patbuf.length - patidx;
        int bufoff = 0;
        while (bufoff + patlen < buf.length) {
            if (!equals(patbuf, patidx, buf, bufoff, patlen)) {
                return false;
            }
            bufoff += patlen;
            patidx = 0;
            patlen = patbuf.length - patidx;
        }
        if (bufoff < buf.length) { // Some work is left.
            patlen = buf.length - bufoff;
            if (!equals(patbuf, patidx, buf, bufoff, patlen)) {
                return false;
            }
            patidx = patlen;
        }
        return true;
    }

    public boolean getMatched() {
        return matched;
    }

    private boolean equals(byte[] srcbuf, int srcoff, byte[] dstbuf, int dstoff, int len) {
        for (int i = 0; i < len; i++) {
            if (srcbuf[srcoff + i] != dstbuf[dstoff + i]) {
                matched = false;
                return matched;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        Map<String, String> defaults = new HashMap<String, String>();
        defaults.put("pattern1", "Test Pattern");
        defaults.put("pattern2", "Test Pattern");
        defaults.put("size1", "1024");
        defaults.put("size2", "512");
        opts.setDefaults(defaults);

        String pattern1 = opts.get("pattern1");
        String pattern2 = opts.get("pattern2");
        int size1 = Integer.parseInt(opts.get("size1"));
        int size2 = Integer.parseInt(opts.get("size2"));

        System.out.println("pattern1: " + pattern1 + ", pattern2: " + pattern2);
        System.out.println("size1: " + size1 + ", size2: " + size2);

        PatternUtil pu1 = new PatternUtil(pattern1);
        JSTKBuffer buf1 = JSTKBuffer.getInstance(size1, opts);
        JSTKBuffer buf2 = JSTKBuffer.getInstance(size2, opts);

        pu1.fillPattern(buf1);
        pu1.fillPattern(buf2);

        PatternUtil pu2 = new PatternUtil(pattern2);
        if (!pu2.matchPattern(buf1)) {
            System.out.println("buf1: Match Failed.");
            System.exit(1);
        }
        if (!pu2.matchPattern(buf2)) {
            System.out.println("buf2: Match Failed.");
            System.exit(1);
        }
        System.out.println("buf1, buf2: Match Succeeded.");
        System.exit(0);
    }
}
