/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;

import java.util.ArrayList;
import java.util.List;


/**
 * This class processes <i>apply</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class ApplySpecialForm extends LispSpecialForm {

    /**
     * Evaluates "apply" special form.
     * 
     * @param exp S expression
     * @param env lisp environment
     * @param interp the lisp interpreter
     * @return result of evaluation
     * @throws LispException If a lisp exception occurs
     * @throws BadApplyArgumentsException If can not apply arguments
     */
    public Object eval(LispCons exp, LispEnv env, LispInterpreter interp) throws BadApplyArgumentsException, LispException {

        LispEvaluator ev = interp.getEvaluator();
        Object argList = ev.eval(exp.getCaddr(), env, interp);

        if (!(argList instanceof LispCons)) {
            throw new BadApplyArgumentsException("Invalid argument list for apply: " + argList);
        }

        List<Object> vec = new ArrayList<Object>();

        while (argList != LispCons.emptyList) {
            vec.add(((LispCons) argList).getCar());
            argList = ((LispCons) argList).getCdr();
        }

        Object func = ev.eval(exp.getCadr(), env, interp);

        if (!(func instanceof LispFunction)) {
            System.err.println("Invalid function for apply: " + func);
            throw new BadApplyArgumentsException("Invalid function for apply: " + func);
        }

        return ((LispFunction) func).apply(vec, interp);
    }
}

/* */
