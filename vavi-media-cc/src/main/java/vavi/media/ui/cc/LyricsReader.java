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
 * タグ付き歌詞のローダです。
 * 
 * 仕様として getTimeTo は今のところ常に -1 を返します。
 * 
 * <pre>
 *  [mm:ss:ms] message
 *  TODO    ms は 3 桁で 1/1000 sec 単位
 *                2 桁で 1/100  sec 単位
 * 			      無しは秒単位
 * 			'@' 対応
 * 			改行のみ行対応
 * </pre>
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030218 nsano initial version <br>
 */
public class LyricsReader extends ClosedCaptionReader {

    public LyricsReader(File file) throws IOException {
        super(new FileReader(file));
    }

    public ClosedCaption[] readClosedCaptions() throws IOException {

        List<ClosedCaption> tmp = new ArrayList<ClosedCaption>();

        while (reader.ready()) {
            long time = 0;
            String text = null;
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            StringTokenizer st = new StringTokenizer(line, "[]");
            if (st.hasMoreTokens()) {
                String times = st.nextToken();
                StringTokenizer st2 = new StringTokenizer(times, ":");
                long min = Long.parseLong(st2.nextToken());
                long sec = Long.parseLong(st2.nextToken());
                long msec = Long.parseLong(st2.nextToken());
                time = min * 60 * 1000 + sec * 1000 + msec;
            }
            else {
Debug.println("no time section");
                continue;
            }
            if (st.hasMoreTokens()) {
                text = st.nextToken();
            }
            else {
Debug.println("no text");
                text = "";
            }

Debug.println(time + ": " + text);
            ClosedCaption cc = new ClosedCaption();
            cc.setSequenceNo(tmp.size() + 1);
            cc.setText(text);
            cc.setTimeFrom(time);
            cc.setTimeTo(-1);
            tmp.add(cc);
        }

        ClosedCaption[] ccs = new ClosedCaption[tmp.size()];
        tmp.toArray(ccs);
        return ccs;
    }
}

/* */
