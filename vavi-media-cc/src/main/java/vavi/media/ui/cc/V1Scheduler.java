/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.util.Timer;
import java.util.TimerTask;

import vavi.util.Debug;
import vavi.util.event.GenericEvent;


/**
 * 間隔でイベントを発生させるスケジューラです。(失敗)
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030304 nsano initial version <br>
 */
public class V1Scheduler extends Scheduler {

    public V1Scheduler(ClosedCaption[] ccs) {
        super(ccs);
    }

    /** */
    private int current;

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
    private class V1TimerTask extends TimerTask {
        public void run() {
            fireEventHappened(new GenericEvent(this, "show", ccs[current]));
            long now = ccs[current].getTimeFrom();
            current++;
            if (current == ccs.length) {
                fireEventHappened(new GenericEvent(this, "exit"));
Debug.println("end");
            } else {
                long next = ccs[current].getTimeFrom();
                timer.schedule(new V1TimerTask(), (next - now) / ff);
Debug.println("next: " + sec(next - now));
            }
        }
    };

    /** */
    public void start() {
        current = 0;
        timer = new Timer();
        timer.schedule(new V1TimerTask(), ccs[current].getTimeFrom() / ff);
Debug.println("started: " + sec(ccs[current].getTimeFrom()));
    }

    /** */
    public void pause() {
    }

    /** */
    public void restart() {
    }

    /** */
    public void moveTo(long time) {
    }
}

/* */
