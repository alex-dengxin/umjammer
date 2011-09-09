/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.mona;



/**
 * BbsThread. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080831 nsano initial version <br>
 */
public class BbsThread implements Comparable<BbsThread> {

    private BbsBoard board;

    public String getThreadUrl() {
        return board.getUrl() + "dat/" + number;
    }

    private String number;
    private String title;

    /** current responses */
    private int responses;
    /** */
    private long sinse;

    public long getSinse() {
        return sinse;
    }

    /** */
    private long lastModified;
    /** gunziped size */
    private int size;
    /** loaded data count */
    private int index;

    /** */
    public BbsThread(BbsBoard board, String number, String title) {
        this.board = board;
        this.number = number;
        this.title = title;
        int p = title.lastIndexOf("(");
        if (p > 0) {
            this.title = title.substring(0, p - 1);
//System.err.println("title: " + this.title);
            int q = title.indexOf(")", p + 1);
            if (q > 0) {
//System.err.println("responses: " + title.substring(p + 1, q));
                this.responses = Integer.parseInt(title.substring(p + 1, q));
            }
        }
//System.err.println("sinse: " + number.substring(0, number.indexOf(".dat")));
        this.sinse = Long.parseLong(number.substring(0, number.indexOf(".dat"))) * 1000;
    }

    /** */
    public String getTitleAsPlainText() {
        return HtmlUtil.toPlainText(title);
    }

    /** */
    public String toString() {
        return title + " (" + responses + ") " + number + ", " + getInfluence(); 
    }

    /** ê®Ç¢ */
    public float getInfluence() {
//System.err.println("1: " + responses);
//System.err.println("2: " + ((float) (System.currentTimeMillis() - sinse) / (1000 * 60 * 60 * 24)));
        return responses / ((float) (System.currentTimeMillis() - sinse) / (1000 * 60 * 60 * 24));
    }

    /* ê®Ç¢èá */
    public int compareTo(BbsThread y) {
        return (int) Math.ceil(y.getInfluence() - this.getInfluence());
    }

    /** */
    public int getIndex() {
        return index;
    }

    /** */
    public String getTitle() {
        return title;
    }

    /** */
    public int getResponses() {
        return responses;
    }

    /** */
    public long getLastModified() {
        return lastModified;
    }

    /** */
    public int getSize() {
        return size;
    }

    /** */
    public void setIndex(int index) {
        this.index = index;
    }

    /** */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    /** */
    public void setSize(int size) {
        this.size = size;
    }

    /** */
    public String getNumber() {
        return number;
    }
}

/* */
