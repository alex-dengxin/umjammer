/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.BadArgumentTypeException;
import vavi.apps.lisp.LispBoolean;
import vavi.apps.lisp.LispFileOutputStream;
import vavi.apps.lisp.LispInterpreter;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.LispPrinter;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>print</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispPrintFun extends LispPrimitive {

    /**
     * Processes <i>print</i> expression.
     * <p>
     * <tt>
     * (print <i>target</i>) <br>
     * (print <i>target</i> LispFileOutputStream)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @param interp the lisp interpreter
     * @return true: success
     * @throws WrongArgumentCountException If argument count is wrong
     * @throws BadArgumentTypeException If bad argument type exists
     */
    public Object apply(List<Object> args, LispInterpreter interp) throws BadArgumentTypeException, WrongArgumentCountException {

        int numArgs = args.size();

        if (numArgs > 2) {
            throw new WrongArgumentCountException("print expects no more than 2 arguments; got " + numArgs);
        }

        if (numArgs == 1) {
            LispPrinter.println(args.get(0));
        } else {
            Object toPrint = args.get(0);
            Object stream = args.get(1);

            if (stream instanceof LispFileOutputStream) {
                LispPrinter.println((LispFileOutputStream) stream, toPrint);
            } else {
                throw new BadArgumentTypeException("Wrong argument type for print: " + stream + "; expected LispFileOutputStream");
            }
        }

        return LispBoolean.trueValue;
    }
}

/* */
