/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.prim;

import java.util.List;

import vavi.apps.lisp.BadNumericArgumentException;
import vavi.apps.lisp.LispPrimitive;


/**
 * This class processes the primitive of <i>*</i>.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispTimesFun extends LispPrimitive {

    /**
     * ä|ÇØéZÇçsÇ¢Ç‹Ç∑ÅD
     * <p>
     * <tt>
     * (* <i>number1</i> <i>number2</i> [<i>number3</i> ...])
     * </tt>
     * <p>
     * 
     * @param args arguments
     * @return the value
     * @throws BadNumericArgumentException If bad argument are exist.
     */
    public Object apply(List<Object> args) throws BadNumericArgumentException {

        int result = 1;
        int index = 0;

        for (index = 0; index < args.size(); index++) {
            if (!(args.get(index) instanceof Integer)) {
                break;
            }

            result *= ((Integer) args.get(index)).intValue();

            if (result == 0) {
                return new Integer(0);
            }
        }

        if (index == args.size()) {
            return new Integer(result);
        } else {
            double dresult = result;

            while (index < args.size()) {
                Object arg = args.get(index);

                if (!(arg instanceof Number)) {
                    throw new BadNumericArgumentException("Invalid numeric argument: " + arg);
                }

                dresult *= ((Number) arg).doubleValue();
                index++;
            }

            return new Double(dresult);
        }
    }
}

/* */
