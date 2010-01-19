/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.ArrayList;
import java.util.List;

import vavi.apps.lisp.BadArgumentTypeException;
import vavi.apps.lisp.LispException;
import vavi.apps.lisp.LispFunction;
import vavi.apps.lisp.LispInterpreter;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.LispSymbol;
import vavi.apps.lisp.LispThrowException;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>catch</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispCatchFun extends LispPrimitive {

    /** The stack for exceptions */
    private static List<Object> vec = new ArrayList<Object>();

    /**
     * Processes <i>catch</i> expression.
     * <p>
     * <tt>
     * (catch <i>argment1</i> <i>argment2</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @throws BadArgumentTypeException If a bad type argument exists
     * @throws LispException If a lisp error occurs
     * @throws WrongArgumentCountException If the argument count is wrong
     */
    public Object apply(List<Object> args, LispInterpreter interp) throws BadArgumentTypeException, LispException, WrongArgumentCountException {

        checkArgs("catch", args, 2);

        Object arg1 = args.get(0);
        Object arg2 = args.get(1);

        LispSymbol tag;
        LispFunction func;

        if (arg1 instanceof LispSymbol) {
            tag = (LispSymbol) arg1;
        } else {
            throw new BadArgumentTypeException("Wrong argument type for catch: " + arg1 + "; expected LispSymbol");
        }

        if (arg2 instanceof LispFunction) {
            func = (LispFunction) arg2;
        } else {
            throw new BadArgumentTypeException("Wrong argument type for catch: " + arg2 + "; expected LispFunction");
        }

        try {
            return func.apply(vec, interp);
        } catch (LispThrowException e) {
            if (e.getTag() == tag) {
                return e.getValue();
            } else {
                throw e;
            }
        }
    }
}

/* */
