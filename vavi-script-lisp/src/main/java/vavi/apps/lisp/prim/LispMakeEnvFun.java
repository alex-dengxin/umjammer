/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.LispEnv;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>make-environment</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispMakeEnvFun extends LispPrimitive {

    /**
     * Processes <i>make-environment</i> expression.
     * <p>
     * <tt>
     * (make-environment)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @throws WrongArgumentCountException If argument count is wrong
     */
    public Object apply(List<Object> args) throws WrongArgumentCountException {

        checkArgs("make-environment", args, 0);

        return new LispEnv();
    }
}

/* */
