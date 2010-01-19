/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.LispEnv;
import vavi.apps.lisp.LispEvaluator;
import vavi.apps.lisp.LispException;
import vavi.apps.lisp.LispInterpreter;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>eval</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispEvalFun extends LispPrimitive {

    /**
     * Processes <i>eval</i> expression.
     * <p>
     * <tt>
     * (eval argment1 argment2)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @param interp the lisp interpreter
     * @throws WrongArgumentCountException If the argument count is wrong
     * @throws LispException If a lisp error occurs
     */
    public Object apply(List<Object> args, LispInterpreter interp) throws WrongArgumentCountException, LispException {

        checkArgs("eval", args, 2);

        LispEvaluator ev = interp.getEvaluator();

        return ev.eval(args.get(0), (LispEnv) args.get(1), interp);
    }
}

/* */
