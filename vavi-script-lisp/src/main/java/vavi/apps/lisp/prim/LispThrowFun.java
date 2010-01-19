/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.BadArgumentTypeException;
import vavi.apps.lisp.LispInterpreter;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.LispSymbol;
import vavi.apps.lisp.LispThrowException;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>throw</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispThrowFun extends LispPrimitive {

    /**
     * 例外をスローします．
     * <p>
     * <tt>
     * (throw <i>symbol</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @param interp the lisp interpreter
     * @throws BadArgumentTypeException If bad argument are exist
     * @throws WrongArgumentCountException If the argument count is wrong
     * @throws LispThrowException When processed correctly
     */
    public Object apply(List<Object> args, LispInterpreter interp) throws BadArgumentTypeException, WrongArgumentCountException, LispThrowException {

        checkArgs("throw", args, 2);

        Object arg1 = args.get(0);
        Object value = args.get(1);

        LispSymbol tag;

        if (arg1 instanceof LispSymbol) {
            tag = (LispSymbol) arg1;
        } else {
            throw new BadArgumentTypeException("Wrong argument type for throw: " + arg1 + "; expected LispSymbol");
        }

        throw new LispThrowException(tag, value);
    }
}

/* */
