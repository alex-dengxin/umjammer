/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.mona;

import java.util.Collections;
import java.util.List;

import vavi.util.mona.impl.MyBbsThreadsFactory;


/**
 * BbsBoard.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080829 nsano initial version <br>
 */
public class BbsBoard {

    String category;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** */
    public BbsBoard(String category, String url, String name) {
        this.category = category;
        this.url = url;
        this.name = name;
//System.err.println(this);
    }

    /* */
    public String toString() {
        return category + ", " + name + ", " + url;
    }

    /** */
    private BbsThreadsFactory bbsThreadsFactory = new MyBbsThreadsFactory();

    /** */
    public List<BbsThread> getThreads() throws Exception {
        List<BbsThread> threads = bbsThreadsFactory.readFrom(this);
        Collections.sort(threads);
//for (int i = 0; i < threads.size(); i++) {
// BbsThread thread = (BbsThread) threads.get(i);
// System.err.println(thread);
//}

        return threads;
    }
}

/* */
