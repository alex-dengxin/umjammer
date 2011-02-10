/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape.annotation;

import java.io.IOException;


/**
 * InputHandler. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/10/01 nsano initial version <br>
 */
public interface InputHandler<T> {

    /**
     * TODO こういう所だよなぁ、Java のメソッドが First Class Object だったらなぁと思う場面
     */
    T getInput(String ... args) throws IOException;
}

/* */
