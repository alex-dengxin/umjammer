/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * This class processes <i>if</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * 
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class IfSpecialForm extends LispSpecialForm {

    /**
     * Evaluates "if" special form.
     * 
     * @param exp S expression
     * @param env lisp environment
     * @param interp the lisp interpreter
     * @return result of evaluation
     * @throws LispException If a lisp error occurs
     */
    public Object eval(LispCons exp, LispEnv env, LispInterpreter interp) throws LispException {

        LispEvaluator ev = interp.getEvaluator();
        Object test = exp.getCadr();
        LispCons tail = (LispCons) exp.getCddr();

        if (ev.eval(test, env, interp) != LispBoolean.falseValue) {
            return ev.eval(tail.getCar(), env, interp);
        } else if (tail.getCdr() != LispCons.emptyList) {
            return ev.eval(tail.getCadr(), env, interp);
        } else {
            return LispBoolean.falseValue;
        }
    }
}

/* */
