/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.BadArgumentListException;
import vavi.apps.lisp.LispBoolean;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>&lt;=</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispLEFun extends LispPrimitive {

    /**
     * Processes <i>&lt;=</i> expression.
     * <p>
     * <tt>
     * (&lt;= <i>number1</i> <i>number2</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @throws WrongArgumentCountException If the argument count is wrong
     * @throws BadArgumentListException If a bad argument exists
     */
    public Object apply(List<Object> args) throws WrongArgumentCountException, BadArgumentListException {

        checkArgs("<=", args, 2);

        Object arg1 = args.get(0);
        Object arg2 = args.get(1);

        if (arg1 instanceof Integer && arg2 instanceof Integer) {
            boolean result = ((Integer) arg1).intValue() <= ((Integer) arg2).intValue();
            return LispBoolean.select(result);
        } else if (arg1 instanceof Number && arg2 instanceof Number) {
            boolean result = ((Number) arg1).doubleValue() <= ((Number) arg2).doubleValue();
            return LispBoolean.select(result);
        } else {
            throw new BadArgumentListException("Both arguments to '<=' must be numbers: " + arg1 + ", " + arg2);
        }
    }
}

/* */
