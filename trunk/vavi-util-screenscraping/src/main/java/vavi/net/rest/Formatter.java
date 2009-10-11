/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.rest;


/**
 * Formatter. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070224 nsano initial version <br>
 */
public interface Formatter {

    /**
     * @param format {@link Formatted#value()}
     * @param value field value 
     */
    String format(String format, Object value);
}

/* */
