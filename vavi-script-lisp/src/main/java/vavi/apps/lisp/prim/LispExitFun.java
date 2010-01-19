/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.ExitLispException;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>exit</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispExitFun extends LispPrimitive {

    /**
     * Processes <i>exit</i> expression.
     * <p>
     * <tt>
     * (exit)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @throws WrongArgumentCountException If the argument count is wrong
     * @throws ExitLispException exit code
     */
    public Object apply(List<Object> args) throws WrongArgumentCountException, ExitLispException {

        checkArgs("exit", args, 0);
        throw new ExitLispException();
    }
}

/* */
