/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import vavi.apps.lisp.BadArgumentTypeException;
import vavi.apps.lisp.BadEvalExpressionException;
import vavi.apps.lisp.BadLoadFileNameException;
import vavi.apps.lisp.IncompleteExpressionException;
import vavi.apps.lisp.InvalidLispExpressionException;
import vavi.apps.lisp.LispBoolean;
import vavi.apps.lisp.LispEnv;
import vavi.apps.lisp.LispEvaluator;
import vavi.apps.lisp.LispException;
import vavi.apps.lisp.LispInterpreter;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.LispReader;
import vavi.apps.lisp.LispSymbol;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>load</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 *          1.00 980123 nsano change filename to InputStream <br>
 *          1.01 980130 nsano add the encoding to the reader <br>
 */
public final class LispLoadFun extends LispPrimitive {

    /**
     * Processes <i>load</i> expression.
     * <p>
     * <tt>
     * (load <i>InputStream</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @param interp the lisp interpreter
     * @throws BadArgumentTypeException If a bad argument exists
     * @throws WrongArgumentCountException If the argument count is wrong
     * @throws BadLoadFileNameException If the file name is wrong
     * @throws InvalidLispExpressionException If expression is invalid
     * @throws IncompleteExpressionException If expression doesn't completed
     * @throws BadEvalExpressionException If bad eval expression exist
     * @throws LispException If a lisp error occurs
     */
    public Object apply(List<Object> args, LispInterpreter interp)
        throws BadArgumentTypeException,
               WrongArgumentCountException,
               InvalidLispExpressionException,
               IncompleteExpressionException,
               BadEvalExpressionException,
               LispException {

        checkArgs("load", args, 1);

        Object arg1 = args.get(0);
        InputStream in = null;

        if (arg1 instanceof InputStream) {
            in = (InputStream) arg1;
        } else if (arg1 instanceof LispSymbol) {
            Object val = interp.getEnv().fetch((LispSymbol) arg1);
            if (val instanceof InputStream) {
                in = (InputStream) val;
            } else {
                throw new BadArgumentTypeException("Wrong argument type: " + arg1 + "; expected InputStream");
            }
        } else {
            throw new BadArgumentTypeException("Wrong argument type: " + arg1 + "; expected InputStream");
        }

        LispReader reader = null;
        try {
            reader = new LispReader(new InputStreamReader(in, interp.getEncoding()), interp.getSymbols());
        } catch (IOException e) {
System.err.println("LispLoadFun::apply: " + e);
            return LispBoolean.falseValue;
        }
        LispEvaluator evaluator = interp.getEvaluator();
        LispEnv env = interp.getEnv();

        try {
            while (in.available() > 0) {
                Object readResult = reader.skipRead();

                if (readResult != null) {
                    evaluator.eval(readResult, env, interp);
                }
            }
        } catch (IOException e) {
System.err.println("LispLoadFun::apply: " + e);
            return LispBoolean.falseValue;
        }

        try {
            in.close();
        } catch (IOException e) {
System.err.println("LispLoadFun::apply: " + e);
            return LispBoolean.falseValue;
        }

        return LispBoolean.trueValue;
    }
}

/* */
