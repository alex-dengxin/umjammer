/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import vavi.util.Debug;


/**
 * SRT Reader.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030219 nsano initial version <br>
 *          0.01 030306 nsano fix reading <br>
 */
public class SRTReader extends ClosedCaptionReader {

    /** */
    private static String encoding = "JISAutoDetect";

    /** */
    public SRTReader(File file) throws IOException {
        super(new InputStreamReader(new FileInputStream(file), encoding));
    }

    /** */
    private static final String sdf = "HH:mm:ss,SSS z";

    /** */
    public ClosedCaption[] readClosedCaptions() throws IOException {

        List<ClosedCaption> tmp = new ArrayList<ClosedCaption>();

//        long base = new Date(0).getTime();
//Debug.println("base: " + base + ": " + new Date(base));

        while (reader.ready()) {
            String l = null;
            ClosedCaption cc = new ClosedCaption();

            l = reader.readLine();
//System.err.println("Seq [" + l + "]");
            cc.setSequenceNo(Integer.parseInt(l));

            l = reader.readLine();
//System.err.println("Time[" + l + "]");
            long start;
            long end;
            StringTokenizer st = new StringTokenizer(l, "--> ");
            if (st.hasMoreTokens()) {
                try {
                    String startString = st.nextToken();
//Debug.println("start: " + startString);
                    start = new SimpleDateFormat(sdf).parse(startString + " GMT").getTime();
                } catch (ParseException e) {
Debug.println("no start: " + e);
                    continue;
                }
            } else {
Debug.println("no start");
                continue;
            }

            if (st.hasMoreTokens()) {
                try {
                    String endString = st.nextToken();
                    end = new SimpleDateFormat(sdf).parse(endString + " GMT").getTime();
                } catch (ParseException e) {
Debug.println("no end: " + e);
                    continue;
                }
            } else {
Debug.println("no end");
                continue;
            }
//Debug.println(l);
//Debug.println(new Date(start) + ", " + new Date(end));
//Debug.println("start: " + start + ", end: " + end);
            cc.setTimeFrom(start);
            cc.setTimeTo(end);

            StringBuilder sb = new StringBuilder();
            while (reader.ready()) {
                l = reader.readLine();
//System.err.println("Text[" + l + "]");
        		if ("".equals(l)) {
        		    break;
        		}
                sb.append(l);
                sb.append("\n");
            }
//Debug.println("cc: " + (tmp.size() + 1));
            String text = sb.toString().substring(0, sb.length() - 1);
            text = text.replaceAll("<[\\w/]\\w*>", "");
            cc.setText(text);

//Debug.println(StringUtil.paramString(cc));
            tmp.add(cc);
        }

        ClosedCaption[] ccs = new ClosedCaption[tmp.size()];
        tmp.toArray(ccs);
        return ccs;
    }

    /** */
    static {

        final Class<?> c = SRTReader.class;

        try {
            Properties props = new Properties();

            props.load(c.getResourceAsStream("SRT.properties"));

            String value = props.getProperty("reader.encoding");
            if (value != null) {
                encoding = value;
            }
        } catch (IOException e) {
Debug.println(e);
        }
    }

    /** */
    public static void main(String[] args) throws Exception {
        new SRTReader(new File(args[0])).readClosedCaptions();
    }
}

/* */
