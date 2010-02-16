/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.mona;


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
}

/* */
