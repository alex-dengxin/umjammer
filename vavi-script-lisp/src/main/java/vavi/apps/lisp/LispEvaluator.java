/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Original by SunSoft Java Series CD-ROM "Java by Example"
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;

import java.util.ArrayList;
import java.util.List;


/**
 * Lisp Evaluator of the interpreter in this packege.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 *          0.01 97xxxx nsano add class path <br>
 */
public final class LispEvaluator {

    /**
     * Creates a lisp evaluator.
     * 
     * @param classPath class path string
     */
    LispEvaluator(String classPath) {
        this.classPath = classPath;
    }

    /**
     * Evaluates.
     * 
     * @param exp lisp expression
     * @param env lisp environment
     * @param interp the lisp interpreter
     * @throws BadEvalExpressionException If the expression is bad.
     * @throws BadBuiltInException If a builtin is bad.
     * @throws BuiltInNotFoundException If a builtin does not found.
     * @throws LispException If a lisp error occurs.
     */
    public Object eval(Object exp, LispEnv env, LispInterpreter interp) throws BadEvalExpressionException, BadBuiltInException, BuiltInNotFoundException, LispException {

        if (exp instanceof LispSymbol) {
            if ((((LispSymbol) exp).getName()).length() == 0) {
                // System.err.println("Maybe comment");
                throw new CommentLispException();
            } else {
                return getSymbolValue((LispSymbol) exp, env);
            }
        }

        if (!(exp instanceof LispCons)) {
            return exp;
        }

        if (exp == LispCons.emptyList) {
            return exp;
        }

        LispCons consExp = (LispCons) exp;

        Object expCar = eval(consExp.getCar(), env, interp);

        if (expCar instanceof LispSpecialForm) {
            return ((LispSpecialForm) expCar).eval(consExp, env, interp);
        }

        if (expCar instanceof LispFunction) {
            List<Object> args = evalArgs((LispCons) consExp.getCdr(), env, interp);

            return ((LispFunction) expCar).apply(args, interp);
        }

        if (exp instanceof LispPrintable) {
            throw new BadEvalExpressionException("Badly formed expression: " + ((LispPrintable) exp).toLispString());
        } else {
            throw new BadEvalExpressionException("Badly formed expression: " + exp);
        }
    }

    /**
     * Evaluates sequence,
     * 
     * @param seq the sequence
     * @param env lisp environment
     * @param interp the lisp interpreter
     * @throws LispException If a lisp error occurs.
     */
    public Object evalSequence(LispCons seq, LispEnv env, LispInterpreter interp) throws LispException {

        while (seq.getCdr() != LispCons.emptyList) {
            eval(seq.getCar(), env, interp);
            seq = (LispCons) seq.getCdr();
        }

        return eval(seq.getCar(), env, interp);
    }

    /**
     * Evaluates arguments.
     * 
     * @param exp lisp expression
     * @param env lisp environment
     * @param interp the lisp interpreter
     */
    private List<Object> evalArgs(LispCons exp, LispEnv env, LispInterpreter interp) throws BadEvalExpressionException, LispException {

        List<Object> args = new ArrayList<Object>();

        while (exp != LispCons.emptyList) {
            args.add(eval(exp.getCar(), env, interp));
            exp = (LispCons) exp.getCdr();
        }

        return args;
    }

    /**
     * Gets the value of the symbol.
     * 
     * @param sym the lisp symbol
     * @param env lisp environment
     */
    private Object getSymbolValue(LispSymbol sym, LispEnv env) throws UnboundSymbolException {

        Object symVal;

        try {
            symVal = env.fetch(sym);
        } catch (UnboundSymbolException e) {

            Class<?> builtIn = null;
            String className = classPath + "." + sym;

            try {
                builtIn = Class.forName(className);
            } catch (Exception locException) {
                throw new UnboundSymbolException("Unbound symbol: " + sym);
            }

            try {
                symVal = builtIn.newInstance();
                env.define(sym, symVal);
            } catch (Exception constructionException) {
                throw new UnboundSymbolException("Unbound symbol: " + sym);
            }

            return symVal;
        }

        if (symVal instanceof LispBuiltIn) {

            Class<?> builtIn = null;
            String className = ((LispBuiltIn) symVal).getName();

            try {
                builtIn = Class.forName(className);
            } catch (Exception locException) {
                throw new UnboundSymbolException("Unbound symbol: " + sym);
            }

            try {
                symVal = builtIn.newInstance();
                env.store(sym, symVal);
            } catch (Exception constructionException) {
                throw new UnboundSymbolException("Unbound symbol: " + sym);
            }
        }

        return symVal;
    }

    /** The class path string */
    private String classPath;
}

/* */
