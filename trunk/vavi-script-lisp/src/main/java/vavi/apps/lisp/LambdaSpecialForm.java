/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * This class processes <i>lambda</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LambdaSpecialForm extends LispSpecialForm {

    /**
     * Evaluates lambda special form.
     * 
     * @param exp S expression
     * @param env lisp environment
     * @param interp the lisp interpreter
     * @return result of evaluation
     * @throws LispException If a lisp error occurs
     */
    public Object eval(LispCons exp, LispEnv env, LispInterpreter interp) throws LispException {
        return new LispProcedure(exp, env);
    }
}

/* */
