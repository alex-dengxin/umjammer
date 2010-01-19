/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.LispBoolean;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>=</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispEqlFun extends LispPrimitive {

    /**
     * Processes <i>=</i> expression.
     * <p>
     * <tt>
     * (= argment1 argment2)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @throws WrongArgumentCountException If the argument count is wrong
     */
    public Object apply(List<Object> args) throws WrongArgumentCountException {

        checkArgs("=", args, 2);

        Object arg1 = args.get(0);
        Object arg2 = args.get(1);

        if (arg1.equals(arg2)) {
            return LispBoolean.trueValue;
        } else {
            return LispBoolean.falseValue;
        }
    }
}

/* */
