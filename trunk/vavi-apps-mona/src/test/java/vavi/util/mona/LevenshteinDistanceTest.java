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

    @Test
    public void test00() throws Exception {
        Mona mona = new Mona();
        List<BbsBoard> boards = mona.getBoardsByCategory("ŽÀ‹µch");
        for (int i = 0; i < boards.size(); i++) {
            System.out.println(i + ": " + boards.get(i));
        }
        int index = new Scanner(System.in).nextInt();
        mona.setTargetBoardByName(boards.get(index).getName());
        List<BbsThread> threads = mona.getThreads();
        final String title = threads.get(0).getTitleAsPlainText();
        final LevenshteinDistance ld = new LevenshteinDistance();
        Collections.sort(threads, new Comparator<BbsThread>() {
            public int compare(BbsThread o1, BbsThread o2) {
                int d1 = ld.calculate(title, o1.getTitleAsPlainText()) - ld.calculate(title, o2.getTitleAsPlainText());
                return d1;
            }
        });
        System.out.println(title);
        for (BbsThread thread : threads) {
            System.out.println(ld.calculate(title, thread.getTitleAsPlainText()) + ": " + thread.getTitleAsPlainText());
        }
    }
}

/* */
