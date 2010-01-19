/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;

import java.util.List;


/**
 * Lisp のプリミティブであることを示すインターフェースです．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public interface LispFunction {

    /**
     * プリミティブを評価します．
     * 
     * @param args arguments list
     * @param interp the lisp interpreter
     * @throws LispException If a lisp error occurs
     */
    Object apply(List<Object> args, LispInterpreter interp) throws LispException;
}

/* */
