/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.mona;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import vavi.util.LevenshteinDistance;
import vavi.util.mona.BbsBoard;
import vavi.util.mona.BbsThread;
import vavi.util.mona.Mona;


/**
 * LevenshteinDistanceTest. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/05/11 nsano initial version <br>
 */
public class LevenshteinDistanceTest {

    /** input threads no from console */
    @Test
    public void test00() throws Exception {
        Mona mona = new Mona();
        List<BbsBoard> boards = mona.getBoardsByCategory("実況ch");
        for (int i = 0; i < boards.size(); i++) {
            System.out.println(i + ": " + boards.get(i));
        }
        int index = new Scanner(System.in).nextInt();
        BbsBoard board = mona.getBoardByName(boards.get(index).getName());
        List<BbsThread> threads = board.getThreads();
        final String title = threads.get(0).getTitleAsPlainText();
        final LevenshteinDistance ld = new LevenshteinDistance();
        Collections.sort(threads, new Comparator<BbsThread>() {
            public int compare(BbsThread o1, BbsThread o2) {
                int d = ld.calculate(title, o1.getTitleAsPlainText()) - ld.calculate(title, o2.getTitleAsPlainText());
                if (d != 0) {
                    return d;
                }
                return (int) (o2.getSinse() - o1.getSinse());
            }
        });
        System.out.println(title);
        for (BbsThread thread : threads) {
            System.out.println(ld.calculate(title, thread.getTitleAsPlainText()) + ": " + thread.getTitleAsPlainText()  + "\t\t" + thread.getInfluence());
        }
    }

    /** input threads no from console */
//    @Test
    public void test01() throws Exception {
        String[] names = {
            "番組ch(NHK)",
            "番組ch(教育)",
            "番組ch(NTV)",
            "番組ch(TBS)",
            "番組ch(フジ)",
            "番組ch(朝日)",
            "番組ch(TX)"
        };
        Mona mona = new Mona();
        for (String name : names) {
            BbsBoard board = mona.getBoardByName(name);
            List<BbsThread> threads = board.getThreads();
            BbsThread thread = threads.get(0);
            System.out.println(thread.getInfluence() + ": " + thread.getTitleAsPlainText());
        }
    }
}

/* */
