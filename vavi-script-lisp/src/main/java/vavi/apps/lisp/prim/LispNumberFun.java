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
 * This class processes the primitive of <i>number?</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispNumberFun extends LispPrimitive {

    /**
     * Processes <i>number?</i> expression.
     * <p>
     * <tt>
     * (number? <i>target</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @throws WrongArgumentCountException If argument count is wrong
     */
    public Object apply(List<Object> args) throws WrongArgumentCountException {

        checkArgs("number?", args, 1);

        if (args.get(0) instanceof Number) {
            return LispBoolean.trueValue;
        } else {
            return LispBoolean.falseValue;
        }
    }
}

/* */
