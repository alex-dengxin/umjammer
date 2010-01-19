/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * This class processes <i>define</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class DefineSpecialForm extends LispSpecialForm {

    /**
     * Evaluates "define" special form.
     * 
     * @param exp S expression
     * @param env lisp environment
     * @param interp the lisp interpreter
     * @return result of evaluation
     * @throws LispException If a lisp error occurs
     */
    public Object eval(LispCons exp, LispEnv env, LispInterpreter interp) throws LispException {
        LispEvaluator ev = interp.getEvaluator();

        if (exp.getCadr() instanceof LispCons) {

            // Syntactic sugar:
            //
            // (define (foo x) x) => (define foo (lambda (x) x))
            //

            LispCons callExp = (LispCons) exp.getCadr();

            if (defineSymbol == null) {
                defineSymbol = LispSymbol.intern(interp.getSymbols(), "define");
                lambdaSymbol = LispSymbol.intern(interp.getSymbols(), "lambda");
            }

            LispCons newExp = new LispCons(defineSymbol, new LispCons(callExp.getCar(), new LispCons(new LispCons(lambdaSymbol, new LispCons(callExp.getCdr(), exp.getCddr())), LispCons.emptyList)));

            return ev.eval(newExp, env, interp);

        } else {
            env.define((LispSymbol) exp.getCadr(), ev.eval(exp.getCaddr(), env, interp));

            return exp.getCadr();
        }
    }

    /** the defined symbol */
    private static LispSymbol defineSymbol = null;

    /** the lambda symbol */
    private static LispSymbol lambdaSymbol = null;
}

/* */
