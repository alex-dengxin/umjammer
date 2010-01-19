/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.BadArgumentTypeException;
import vavi.apps.lisp.LispBoolean;
import vavi.apps.lisp.LispFileInputStream;
import vavi.apps.lisp.LispFileOutputStream;
import vavi.apps.lisp.LispInterpreter;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>close</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispCloseFun extends LispPrimitive {

    /**
     * Processes <i>close</i> expression.
     * <p>
     * <tt>
     * (close <i>stream</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @param interp the lisp interpreter
     * @throws BadArgumentTypeException If a bad type argument exists
     * @throws WrongArgumentCountException If the argument count is wrong
     */
    public Object apply(List<Object> args, LispInterpreter interp) throws BadArgumentTypeException, WrongArgumentCountException {

        checkArgs("close", args, 1);

        Object arg = args.get(0);

        if (arg instanceof LispFileInputStream) {
            try {
                ((LispFileInputStream) arg).close();
            } catch (Exception e) {
                return LispBoolean.falseValue;
            }
        } else if (arg instanceof LispFileOutputStream) {
            try {
                ((LispFileOutputStream) arg).close();
            } catch (Exception e) {
                return LispBoolean.falseValue;
            }
        } else {
            throw new BadArgumentTypeException("Wrong argument type for close: " + arg + "; expected LispStream");
        }

        return LispBoolean.trueValue;
    }
}

/* */
