/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.BadNumericArgumentException;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>/</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispDivideFun extends LispPrimitive {

    /**
     * Processes <i>/</i> expression.
     * <p>
     * <tt>
     * (/ <i>number1</i> <i>argment2</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @throws WrongArgumentCountException If the argument count is wrong
     * @throws BadNumericArgumentException If a not number argument exists
     */
    public Object apply(List<Object> args) throws WrongArgumentCountException, BadNumericArgumentException {

        checkArgs("/", args, 2);

        Object arg1 = args.get(0);
        Object arg2 = args.get(1);

        if (arg1 instanceof Integer) {
            if (arg2 instanceof Integer) {
                return new Integer(((Integer) arg1).intValue() / ((Integer) arg2).intValue());
            } else if (arg2 instanceof Number) {
                return new Double(((Number) arg1).doubleValue() / ((Number) arg2).doubleValue());
            } else {
                throw new BadNumericArgumentException("Invalid numeric argument: " + arg2);
            }
        } else if (arg2 instanceof Number) {
            if (arg1 instanceof Number) {
                return new Double(((Number) arg1).doubleValue() / ((Number) arg2).doubleValue());
            } else {
                throw new BadNumericArgumentException("Invalid numeric argument: " + arg1);
            }
        } else {
            throw new BadNumericArgumentException("Invalid numeric argument: " + arg2);
        }
    }
}

/* */
