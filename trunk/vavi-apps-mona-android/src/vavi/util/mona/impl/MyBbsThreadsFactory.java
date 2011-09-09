/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.mona.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import vavi.util.mona.BbsBoard;
import vavi.util.mona.BbsThread;
import vavi.util.mona.BbsThreadsFactory;


/**
 * BbsThread. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080831 nsano initial version <br>
 */
public class MyBbsThreadsFactory implements BbsThreadsFactory {

    /** */
    public List<BbsThread> readFrom(BbsBoard board) throws IOException {
        HttpURLConnection uc = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            uc = (HttpURLConnection) new URL(board.getUrl() + "subject.txt").openConnection();
            uc.connect();
            InputStream is = new BufferedInputStream(uc.getInputStream());
            byte[] buffer = new byte[1024];
            while (true) {
                int r = is.read(buffer);
                if (r < 0) {
                    break;
                }
                baos.write(buffer, 0, r);
            }
            String threadListText = new String(baos.toByteArray(), "MS932");

            List<BbsThread> threads = new ArrayList<BbsThread>();
            StringTokenizer rows = new StringTokenizer(threadListText, "\n");
            while (rows.hasMoreTokens()) {
                String row = rows.nextToken();
//System.err.println(row);
                StringTokenizer tmp = new StringTokenizer(row, "<>");
                threads.add(new BbsThread(board, tmp.nextToken(), tmp.nextToken()));
            }
            return threads;
        } finally {
            uc.disconnect();
        }
    }
}

/* */
