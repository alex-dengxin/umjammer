/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package ch.laoe.operation;

import java.util.EventListener;


/**
 * ProgressListener. 
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050905 nsano initial version <br>
 */
public interface ProgressListener extends EventListener {

    /**
     * @param ev
     */
    void entrySubProgress(ProgressEvent ev);

    /**
     * @param ev
     */
    void setProgress(ProgressEvent ev);

    /**
     * @param ev
     */
    void exitSubProgress(ProgressEvent ev);
}

/* */
