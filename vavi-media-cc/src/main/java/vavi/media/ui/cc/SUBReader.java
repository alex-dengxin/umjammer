/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import vavi.util.Debug;


/**
 * SUB Reader.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030219 nsano initial version <br>
 */
public class SUBReader extends ClosedCaptionReader {

    /** */
    public SUBReader(File file) throws IOException {
        super(new FileReader(file));
    }

    /** */
    public ClosedCaption[] readClosedCaptions() throws IOException {

        List<ClosedCaption> tmp = new ArrayList<ClosedCaption>();

        while (reader.ready()) {
            String l = reader.readLine();

            ClosedCaption cc = new ClosedCaption();

            // TODO text ’†‚Ì { ‚ª”²‚¯‚Ä‚µ‚Ü‚¤
            StringTokenizer st = new StringTokenizer(l, "{}");

            long start;
            long end;

            if (st.hasMoreTokens()) {
                start = Long.parseLong(st.nextToken()) * 40;
            } else {
Debug.println("no start");
                continue;
            }

            if (st.hasMoreTokens()) {
                end = Long.parseLong(st.nextToken()) * 40;
            } else {
Debug.println("no end");
                continue;
            }

            String text;

            if (st.hasMoreTokens()) {
                text = st.nextToken();
                text = text.replace('|', '\n');
            } else {
Debug.println("no end");
                continue;
            }

            cc.setSequenceNo(tmp.size() + 1);
            cc.setText(text);
            cc.setTimeFrom(start);
            cc.setTimeTo(end);

            tmp.add(cc);
//Debug.println(StringUtil.paramString(cc));
        }

        ClosedCaption[] ccs = new ClosedCaption[tmp.size()];
        tmp.toArray(ccs);
        return ccs;
    }

    /** */
    public static void main(String[] args) throws Exception {
        new SUBReader(new File(args[0])).readClosedCaptions();
    }
}

/* */
