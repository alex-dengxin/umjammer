/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is the base class of lisp procedure.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispProcedure implements LispFunction, LispPrintable {

    /**
     * Creates a lisp procedure.
     * 
     * @param exp the S expression
     * @param env the lisp environment
     */
    public LispProcedure(LispCons exp, LispEnv env) throws BadArgumentListException,
        BadProcedureBodyException {

        procEnv = env;
        Object args = exp.getCadr();

        if (args instanceof LispSymbol) {
            procArgs = new ArrayList<Object>();
            procArgs.add(args);
            procHasListArgs = true;
            procRegularArgCount = 0;
        } else {
            procArgs = new ArrayList<Object>();

            while (args instanceof LispCons && args != LispCons.emptyList) {

                procArgs.add(((LispCons) args).getCar());
                args = ((LispCons) args).getCdr();
            }

            procRegularArgCount = procArgs.size();

            if (args == LispCons.emptyList) {
                procHasListArgs = false;
            } else if (args instanceof LispSymbol) {
                procHasListArgs = true;
                procArgs.add(args);
            } else {
                throw new BadArgumentListException("Invalid argument list: " + LispPrinter.toLispString(exp.getCadr()));
            }
        }

        Object body = exp.getCddr();

        if (body instanceof LispCons) {
            procBody = (LispCons) body;
        } else {
            throw new BadProcedureBodyException("Invalid procedure body: " + LispPrinter.toLispString(exp.getCddr()));
        }
    }

    /**
     * Processes this procedure.
     * 
     * @param args arguments
     * @param interp the lisp interpreter
     * @throws BadArgumentListException If arguments are bad.
     * @throws LispException If a lisp error occurs.
     */
    public Object apply(List<Object> args, LispInterpreter interp) throws BadArgumentListException, LispException {

        if (args.size() < procRegularArgCount) {
            handleBadArgumentCount(args.size(), procRegularArgCount);
        }

        if (procHasListArgs) {
            LispCons restArgs = LispCons.emptyList;

            if (args.size() > procRegularArgCount) {
                for (int i = args.size() - 1; i >= procRegularArgCount; i--) {
                    restArgs = new LispCons(args.get(i), restArgs);
                }

                args.add(procRegularArgCount, restArgs);
            } else {
                args.add(restArgs);
            }
        } else if (args.size() > procRegularArgCount) {
            handleBadArgumentCount(args.size(), procRegularArgCount);
        }

        return interp.getEvaluator().evalSequence(procBody, procEnv.extend(procArgs, args), interp);
    }

    /**
     * Returns the lisp string representing this object.
     */
    public String toLispString() {
        return "{procedure}";
    }

    /**
     * Throws the BadArgumentListException object.
     */
    private void handleBadArgumentCount(int numArgs, int expected) throws BadArgumentListException {

        throw new BadArgumentListException("Expected " + expected + (expected == 1 ? " arg" : " args") + "; got " + numArgs);
    }

    /** Arguments of this procedure */
    private List<Object> procArgs;

    /** If this procedure has argument or not */
    private boolean procHasListArgs;

    /** The count of this procedure arguments */
    private int procRegularArgCount;

    /** Lisp environment of this procedure */
    private LispEnv procEnv;

    /** The body of this procedure */
    private LispCons procBody;
}

/* */
