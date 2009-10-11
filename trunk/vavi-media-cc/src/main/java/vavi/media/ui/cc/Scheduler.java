/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import vavi.util.Debug;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;
import vavi.util.event.GenericSupport;


/**
 * モデルの時間に従ってイベントを発生させるスケジューラです。
 *
 * 仕様として設定された EditorListener に対して
 * <ol>
 * <li> ClosedCaption#start を行った時刻に
 * カレントの ClosedCaption#getTimeFrom の値を足した時刻に、
 * fireEditorUpdated を名前 "show" 引数 カレントの ClosedCaption
 * で発行してください。</li>
 * <li> ClosedCaption をすべて出力し終えた際に、
 * fireEditorUpdated を名前 "exit" 引数 最後の ClosedCaption
 * で発行してください。</li>
 * </ol>
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030304 nsano initial version <br>
 *          0.01 030306 nsano 仕様決定 <br>
 */
public abstract class Scheduler {

    /** model */
    protected ClosedCaption[] ccs;

    /** */
    public Scheduler(ClosedCaption[] ccs) {
        this.ccs = ccs;
    }

    /** */
    public abstract void start();

    /** */
    public abstract void pause();

    /** */
    public abstract void restart();

    /** */
    public abstract void moveTo(long time);

    /** 再生する倍速 */
    protected int ff = 10;

    /** 再生する倍速を設定します。主にデバッグ用です。 */
    public void setSpeed(int ff) {
        this.ff = ff;
Debug.println("speed: x " + ff);
    }

    //----

    /** */
    private GenericSupport gs = new GenericSupport();

    /** */
    public void addGenericListener(GenericListener listener) {
        gs.addGenericListener(listener);
    }

    /** */
    public void removeGenericListener(GenericListener listener) {
        gs.removeGenericListener(listener);
    }

    /** */
    protected void fireEventHappened(GenericEvent event) {
        gs.fireEventHappened(event);
    }
}

/* */
