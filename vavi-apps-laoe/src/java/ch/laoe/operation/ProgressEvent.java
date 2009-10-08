/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package ch.laoe.operation;

import java.util.EventObject;


/**
 * ProgressEvent.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050905 nsano initial version <br>
 */
public class ProgressEvent extends EventObject {

    /** */
    private Object[] arguments;

    /**
     * Creates an progress event.
     * 
     * @param source the event source
     */
    public ProgressEvent(Object source) {
        this(source, new Object[0]);
    }

    /**
     * Creates an progress event.
     * 
     * @param source the event source
     * @param arguments the event arguments
     */
    public ProgressEvent(Object source, Object ... arguments) {
        super(source);

        this.arguments = arguments;
    }

    /** @return max index may be zelo */
    public Object[] getArguments() {
        return arguments;
    }
}

/* */
