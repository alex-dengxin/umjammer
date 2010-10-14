/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape.annotation;

import java.util.List;


/**
 * Parser. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/10/01 nsano initial version <br>
 */
public interface Parser<I, T> {
    
    /** */
    List<T> parse(Class<T> type, InputHandler<I> inputHandler, String ... args);

    /** */
    void foreach(Class<T> type, EachHandler<T> eachHandler, InputHandler<I> inputHandler, String ... args);
}

/* */
