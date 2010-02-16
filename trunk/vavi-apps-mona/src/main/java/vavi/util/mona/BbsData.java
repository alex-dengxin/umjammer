/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.mona;


/**
 * BbsData. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080901 nsano initial version <br>
 */
public class BbsData {

    /** 「レス」番号 */
    private int index;
    /** 名前 */
    private String name;
    /** メール */
    private String email;
    /** 日付 ID BE-ID */
//    private Date date;
    private String id;
//    private String beid;
    /** 本文 */
    private String text;
    /** スレッドタイトル */
    private String title;

    /** */
    public BbsData(int index, String name, String email, String id, String text, String title) {
        this.index = index;
        this.name = name;
        this.email = email;
        this.id = id;
        this.text = text;
        this.title = title;
//System.err.println(this);
    }

    public int getIndex() {
        return index;
    }

    public String getTextAsPlainText() {
        return HtmlUtil.toPlainText(text);
    }

    public String getEmailAsFormated() {
        if ("sage".equals(email)) {
            return "↓";
        } else {
            return email;
        }
    }

    /* */
    public String toString() {
        return name + ", " + email + ", " + id + ", " + text + ", " + title;
    }

    /** */
    public String toStringAsFormated() {
        return index + " :" + name + " [" + getEmailAsFormated() + "]" + id + "\n\n " + getTextAsPlainText();
    }

    /** */
    private String raw;

    /** */
    public String getRaw() {
        return raw;
    }

    /** */
    public void setRaw(String raw) {
        this.raw = raw;
    }
}

/* */
