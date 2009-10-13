/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.mona;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import vavi.util.bdj.Sjis;

import junit.framework.TestCase;


/**
 * MonaAppTest. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080901 nsano initial version <br>
 */
public class MonaAppTest extends TestCase {

    public void $test00() throws Exception {
        URLConnection uc = new URL("http://menu.2ch.net/bbsmenu.html").openConnection();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = uc.getInputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int r = is.read(buffer);
            if (r < 0) {
                break;
            }
            baos.write(buffer, 0, r);
        }
        baos.flush();
//System.setProperty("file.encoding", "ISO8859_1");
//System.err.println(System.getProperty("file.encoding"));
//byte[] ba = baos.toByteArray();
//for (int i = 0; i < ba.length; i++) {
// System.err.print(Integer.toHexString(ba[i] & 0xff) + " ");
// if (i % 16 == 15) {
//  System.err.println();
// }
//}
        String boardListHtml = Sjis.toUnicode(baos.toByteArray());
//        String boardListHtml = baos.toString("MS932");
System.err.println(boardListHtml);
    }

    BbsBoardsFactory bbsBoardsFactory = new MyBbsBoardsFactory();
    BbsThreadsFactory bbsThreadsFactory = new MyBbsThreadsFactory();
    BbsDatumFactory bbsDatumFactory = new MyBbsDatumFactory();

    public void test01() throws Exception {
        String boardUrl = "http://menu.2ch.net/bbsmenu.html";

        List boards = bbsBoardsFactory.readFrom(boardUrl);
        List liveBoards = new ArrayList();

        String categoryLive = "ŽÀ‹µch";

        for (int i = 0; i < boards.size(); i++) {
            BbsBoard board = (BbsBoard) boards.get(i);
//System.err.println(board.category + ", " + board.name + ", " + board.url);
            if (board.category.equals(categoryLive)) {
                liveBoards.add(board);
            }
        }
        for (int i = 0; i < liveBoards.size(); i++) {
            BbsBoard board = (BbsBoard) liveBoards.get(i);
System.err.println(board.category + ", " + board.name + ", " + board.url);
        }

        String boardName = "”Ô‘gch(ƒtƒW)";

        BbsBoard targetBoard = null;
        for (int i = 0; i < liveBoards.size(); i++) {
            BbsBoard board = (BbsBoard) liveBoards.get(i);
            if (board.name.equals(boardName)) {
                targetBoard = board;
            }
        }

        List threads = bbsThreadsFactory.readFrom(targetBoard);
        for (int i = 0; i < threads.size(); i++) {
            BbsThread thread = (BbsThread) threads.get(i);
System.err.println(thread.getNumber() + ", " + thread.getTitle() + ", " + thread.getResponses());
        }

        BbsThread targetThread = (BbsThread) threads.get(0);

System.err.println("\n--- " + targetThread.getTitleAsPlainText() + " ---\n");
        int lastIndex = 0;
        do {
            List datum = bbsDatumFactory.readFrom(targetThread);
//System.err.println("lastIndex: " + lastIndex);
            for (int i = 0; i < datum.size(); i++) {
                BbsData data = (BbsData) datum.get(i);
                if (data.getIndex() <= lastIndex) {
//System.err.println("SKIP: " + data.index);
                } else {
//try {
System.err.println(data.toStringAsFormated() + "\n");
//} catch (Error t) {
// System.err.println("FATAL: " + t + "\n" + data.raw);
// throw t;
//}
                    lastIndex = data.getIndex();
                }
            }
            Thread.sleep(3000);
        } while (targetThread.getIndex() < 1002);
    }

    public void $test02() throws Exception {
//System.err.println(System.getProperty("file.encoding"));
    }
}

/* */
