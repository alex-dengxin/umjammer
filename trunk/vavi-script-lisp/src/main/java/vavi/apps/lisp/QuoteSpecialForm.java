/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * This class processes <i>quote</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class QuoteSpecialForm extends LispSpecialForm {

    /**
     * Evaluates quote special form.
     * 
     * @param exp lisp expression
     * @param env lisp environment
     * @param interp the lisp interpreter
     */
    public Object eval(LispCons exp, LispEnv env, LispInterpreter interp) {
        return exp.getCadr();
    }
}

/* */
