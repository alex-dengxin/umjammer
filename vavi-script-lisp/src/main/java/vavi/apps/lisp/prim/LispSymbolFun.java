/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.LispBoolean;
import vavi.apps.lisp.LispPrimitive;
import vavi.apps.lisp.LispSymbol;
import vavi.apps.lisp.WrongArgumentCountException;


/**
 * This class processes the lisp primitive of <i>symbol?</i>. The function "symbol?" takes one argument, and returns true if the
 * argument is a lisp symbol or returns false.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispSymbolFun extends LispPrimitive {

    /**
     * ÉVÉìÉ{ÉãÇ©Ç«Ç§Ç©Çîªï ÇµÇ‹Ç∑ÅD
     * <p>
     * <tt>
     * (symbol? <i>target</i>)
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @return true: the argument is a lisp symbol <br>
     *         false: the argument is not a lisp symbol
     * @throws WrongArgumentCountException If argument count is wrong
     */
    public Object apply(List<Object> args) throws WrongArgumentCountException {

        checkArgs("symbol?", args, 1);

        if (args.get(0) instanceof LispSymbol) {
            return LispBoolean.trueValue;
        } else {
            return LispBoolean.falseValue;
        }
    }
}

/* */
