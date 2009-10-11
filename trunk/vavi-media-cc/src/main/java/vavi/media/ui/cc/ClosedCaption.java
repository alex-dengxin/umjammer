/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;


/**
 * Closed Caption の基礎モデルクラスです。
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030218 nsano initial version <br>
 */
public class ClosedCaption {

    /** Closed Caption の順序、1 から始まります。 */
    private int sequenceNo;
    /** Closed Caption のテキスト、改行は \n を設定してください。 */
    private String text;
    /** Closed Caption の表示時刻 */
    private long timeFrom;
    /** Closed Caption の消去時刻、存在しない場合は -1 を設定してください。 */
    private long timeTo;

    /** */
    public void setSequenceNo(int sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    /** */
    public int getSequenceNo() {
        return sequenceNo;
    }

    /** */
    public void setText(String text) {
        this.text = text;
    }

    /** */
    public String getText() {
        return text;
    }
 
    /** */
    public void setTimeFrom(long timeFrom) {
        this.timeFrom = timeFrom;
    }

    /** */
    public long getTimeFrom() {
        return timeFrom;
    }

    /** */
    public void setTimeTo(long timeTo) {
        this.timeTo = timeTo;
    }

    /** */
    public long getTimeTo() {
        return timeTo;
    }
}

/* */
