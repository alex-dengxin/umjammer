/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import vavi.util.Debug;
import vavi.util.event.GenericEvent;


/**
 * 時間でイベントを発生させるスケジューラです。
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030304 nsano initial version <br>
 */
public class V2Scheduler extends Scheduler {

    /** */
    public V2Scheduler(ClosedCaption[] ccs) {
        super(ccs);
    }

    /** カレントの CC の番号 */
    private int current;

    /** 開始した時刻 */
    private long started;

    /** */
    private Timer timer;

    /** */
    private String sec(long ms) {
        return
            (ms / (60 * 60 * 1000)) + ":" +
            (ms % (60 * 60 * 1000) / (60 * 1000)) + ":" +
            (ms % (60 * 1000) / 1000) + "." +
            (ms % 1000) + "(" +
            ms + ")";
    }

    /** */
    private class V2TimerTask extends TimerTask {
        public void run() {
            fireEventHappened(new GenericEvent(this, "show", ccs[current]));
            current++;
            if (current == ccs.length) {
                ClosedCaption last = ccs[ccs.length - 1];
                fireEventHappened(new GenericEvent(this, "exit", last));
Debug.println("end");
            } else {
                long next = started + (ccs[current].getTimeFrom() / ff);
                timer.schedule(new V2TimerTask(), new Date(next));
Debug.println("next: " + sec(ccs[current].getTimeFrom()));
            }
        }
    };

    /** */
    public void start() {
        current = 0;
        timer = new Timer();
        started = System.currentTimeMillis();
        long next = started + (ccs[current].getTimeFrom() / ff);
        timer.schedule(new V2TimerTask(), new Date(next));
Debug.println("started: " + sec(ccs[current].getTimeFrom()));
    }

    /** 一時停止した時刻 */
    private long paused;

    /** */
    public void pause() {
        paused = System.currentTimeMillis();
Debug.println("paused");
    }

    /** */
    public void restart() {
        long waited = System.currentTimeMillis() - paused;
        started += waited;
Debug.println("restart: " + sec(waited) + " paused");
    }

    /** */
    public void moveTo(long time) {
Debug.println("moveFrom: " + sec(System.currentTimeMillis() - started) + ": " + current);
        started = System.currentTimeMillis() - time;
        while (ccs[current].getTimeFrom() < time) {
            current++;
        }
Debug.println("moveTo: " + sec(time) + ": " + current);
    }
}

/* */
