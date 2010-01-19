/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.LispBoolean;
import vavi.apps.lisp.LispCons;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>null?</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispNullFun extends LispPrimitive {

    /**
     * Processes <i>null?</i> expression.
     * <p>
     * <tt>
     * (null? <i>target</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @throws WrongArgumentCountException If argument count is wrong
     */
    public Object apply(List<Object> args) throws WrongArgumentCountException {

        checkArgs("null?", args, 1);

        if (args.get(0) == LispCons.emptyList) {
            return LispBoolean.trueValue;
        } else {
            return LispBoolean.falseValue;
        }
    }
}

/* */
