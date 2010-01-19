/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import vavi.apps.lisp.BadArgumentTypeException;
import vavi.apps.lisp.ExitLispException;
import vavi.apps.lisp.IncompleteExpressionException;
import vavi.apps.lisp.InvalidLispExpressionException;
import vavi.apps.lisp.LispBoolean;
import vavi.apps.lisp.LispFileInputStream;
import vavi.apps.lisp.LispInterpreter;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.LispReader;
import vavi.apps.lisp.LispSymbol;
import vavi.apps.lisp.LispThrowException;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>read</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispReadFun extends LispPrimitive {

    /**
     * Processes <i>read</i> expression.
     * <p>
     * <tt>
     * (read <i>target</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @param interp the lisp interpreter
     * @throws WrongArgumentCountException If argument count is wrong
     * @throws BadArgumentTypeException If bad argument type exist
     * @throws IncompleteExpressionException If reading is not completed
     * @throws InvalidLispExpressionException If bad expression encountered
     * @throws LispThrowException When exception be thrown
     * @throws ExitLispException When exit code read
     */
    public Object apply(List<Object> args, LispInterpreter interp) throws BadArgumentTypeException, WrongArgumentCountException, IncompleteExpressionException, InvalidLispExpressionException, LispThrowException, ExitLispException {

        int numArgs = args.size();

        if (numArgs > 1) {
            throw new WrongArgumentCountException("read expects no more than 1 argument; got " + numArgs);
        }

        Map<String, LispSymbol> table = interp.getSymbols();
        LispReader reader;

        if (numArgs == 0) {
            reader = interp.getReader();
        } else {
            Object arg = args.get(0);

            if (arg instanceof LispFileInputStream) {
                reader = ((LispFileInputStream) arg).getReader();
            } else {
                throw new BadArgumentTypeException("Wrong argument type for read: " + arg + "; expected LispFileInputStream");
            }
        }

        Object result = null;

        try {
            result = reader.skipRead();
        } catch (IOException e) {
        }

        if (result == null) {
            throw new LispThrowException(LispSymbol.intern(table, "eof"), LispBoolean.falseValue);
        } else {
            return result;
        }
    }
}

/* */
