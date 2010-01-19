/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.LispCons;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the primitive of <i>set-car!</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispSetCarFun extends LispPrimitive {

    /**
     * Processes <i>set-car!</i> expression.
     * <p>
     * <tt>
     * (set-car! <i>target</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @throws WrongArgumentCountException If the argument count is wrong
     */
    public Object apply(List<Object> args) throws WrongArgumentCountException {

        checkArgs("set-car!", args, 2);

        ((LispCons) args.get(0)).setCar(args.get(1));
        return args.get(1);
    }
}

/* */
