/*
 * "http://blogs.dion.ne.jp/anis7742/archives/7484691.html"
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

import vavi.util.mona.BbsBoard;
import vavi.util.mona.BbsBoardsFactory;


/**
 * MyBbsBoardsFactory.
 * 
 * @author ‚ ‚É‚·
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080829 nsano initial version <br>
 */
public class MyBbsBoardsFactory implements BbsBoardsFactory {

    private static final int State_Category = 0;
    private static final int State_Url = 1;
    private static final int State_BoardName = 2;

    /** */
    public List<BbsBoard> readFrom(String url) throws IOException {
        HttpURLConnection uc = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            uc = (HttpURLConnection) new URL(url).openConnection();
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
            baos.flush();
            String boardListHtml = new String(baos.toByteArray(), "MS932");
//System.err.println(boardListHtml);
            List<BbsBoard> boards = new ArrayList<BbsBoard>();
            int state = State_Category;
            String category = null, boardUrl = null, boardName = null;
loop:
            for (int i = 0; i < boardListHtml.length();) {
                switch (state) {
                case State_Category:
                    int tmp = boardListHtml.indexOf("<B>", i) + 3;
                    int tmp2 = boardListHtml.indexOf("</B>", tmp);
                    if (tmp != -1 && tmp2 != -1) {
                        category = boardListHtml.substring(tmp, tmp2);
                        i = tmp2 + 4;
                        state++;
                    }
                    break;
                case State_Url:
                    int tmp1 = boardListHtml.indexOf("HREF=", i) + 5;
                    int tmp12 = boardListHtml.indexOf(">", tmp1);
                    if (tmp1 != -1 && tmp12 != -1) {
                        boardUrl = boardListHtml.substring(tmp1, tmp12);
                        if (boardUrl.indexOf(" ") > 0) {
                            boardUrl = boardUrl.substring(0, boardUrl.indexOf(" "));
                        }
                        i = tmp12 + 1;
                        state++;
                    }
                    break;
                case State_BoardName:
                    int tmp3 = boardListHtml.indexOf("</A>", i);
                    if (tmp3 != -1) {
                        boardName = boardListHtml.substring(i, tmp3);
                        i = tmp3 + 4;
                        boards.add(new BbsBoard(category, boardUrl, boardName));
                        int hrefIndex = boardListHtml.indexOf("HREF=", i);
                        int bTagIndex = boardListHtml.indexOf("<B>", i);
                        if (hrefIndex == -1) {
                            break loop;
                        } else {
                            if (hrefIndex > bTagIndex && bTagIndex != -1) {
                                state = State_Category;
                            } else {
                                state = State_Url;
                            }
                        }
    
                    }
                    break;
                }
            }
    
            return boards;
        } finally {
            uc.disconnect();
        }
    }
}

/* */
