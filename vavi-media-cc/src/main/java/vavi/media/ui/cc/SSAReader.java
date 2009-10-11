/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import vavi.util.CSVTokenizer;
import vavi.util.Debug;


/**
 * SSA.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030219 nsano initial version <br>
 *          0.01 030303 nsano complete <br>
 *          0.02 030306 nsano fix line break <br>
 */
public class SSAReader extends ClosedCaptionReader {

    /** */
    public SSAReader(File file) throws IOException {
        super(new FileReader(file));
    }

    private static final int MODE_SCRIPT_INFO = 3;
    private static final int MODE_V4_STYLES = 2;
    private static final int MODE_EVENTS = 1;
    private static final int MODE_UNKNOWN = 0;

    /** */
    public ClosedCaption[] readClosedCaptions() throws IOException {

        List<ClosedCaption> tmp = new ArrayList<ClosedCaption>();

        int mode = MODE_UNKNOWN;

        while (reader.ready()) {
            String l = reader.readLine();
//System.err.println(l);
            if (l.trim().startsWith("[") && l.trim().endsWith("]")) {
                String m = l.trim().substring(1, l.trim().length() - 1);
                if ("Script Info".equals(m)) {
                    mode = MODE_SCRIPT_INFO;
                } else if ("V4 Styles".equals(m)) {
                    mode = MODE_V4_STYLES;
                } else if ("Events".equals(m)) {
                    mode = MODE_EVENTS;
                } else {
                    mode = MODE_UNKNOWN;
                }
Debug.println("mode change: " + mode);
            } else if (l.trim().equals("")) {
Debug.println("no contents");
            } else if (l.startsWith(";")) {
Debug.println("comment: " + l.substring(l.indexOf(';') + 1));
            } else {
                switch (mode) {
                case MODE_SCRIPT_INFO:
Debug.println("mode: " + mode + ": " + l);
                    break;
                case MODE_V4_STYLES:
Debug.println("mode: " + mode + ": " + l);
                    break;
                case MODE_EVENTS:
                    if (l.startsWith("Format: ")) {
Debug.println("mode: " + mode + ": " + l);
                    } else if (l.startsWith("Dialogue: ")) {
                        String v = l.substring(l.indexOf(' ') + 1);
                        CSVTokenizer st = new CSVTokenizer(v);
                        @SuppressWarnings("unused")
                        String dummy = st.nextToken();
//Debug.println("mode: " + mode + ": marked: " + dummy);
                        String start = st.nextToken() + "0 GMT";
//Debug.println("mode: " + mode + ": start: " + start);
                        String end = st.nextToken() + "0 GMT";
//Debug.println("mode: " + mode + ": end: " + end);
                        dummy = st.nextToken();
//Debug.println("mode: " + mode + ": style: " + dummy);
                        dummy = st.nextToken();
//Debug.println("mode: " + mode + ": name: " + dummy);
                        dummy = st.nextToken();
//Debug.println("mode: " + mode + ": margin L: " + dummy);
                        dummy = st.nextToken();
//Debug.println("mode: " + mode + ": margin R: " + dummy);
                        dummy = st.nextToken();
//Debug.println("mode: " + mode + ": margin V: " + dummy);
                        dummy = st.nextToken();
//Debug.println("mode: " + mode + ": effect: " + dummy);
                        String text = st.nextToken().replaceAll("\\\\[Nn]", "\n");
//Debug.println("mode: " + mode + ": text: " + text);

                        DateFormat sdf = new SimpleDateFormat("H:mm:ss.SSS z");
                        long s;
                        long e;

                        try {
                            s = sdf.parse(start).getTime();
                            e = sdf.parse(end).getTime();
                        } catch (ParseException p) {
Debug.println(p);
                            continue;
                        }

                        ClosedCaption cc = new ClosedCaption();
                        cc.setSequenceNo(tmp.size());
                        cc.setText(text);
                        cc.setTimeFrom(s);
                        cc.setTimeTo(e);
                        tmp.add(cc);
                    } else {
Debug.println("mode: " + mode + ": " + l);
                    }
                    break;
                default:
Debug.println("mode: " + mode + ": " + l);
                    break;
                }
            }
        }

        ClosedCaption[] ccs = new ClosedCaption[tmp.size()];
        tmp.toArray(ccs);
        return ccs;
    }

    /** */
    public static void main(String[] args) throws Exception {
        new SSAReader(new File(args[0])).readClosedCaptions();
    }
}

/* */
