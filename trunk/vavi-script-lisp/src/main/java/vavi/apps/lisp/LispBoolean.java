/*
 * Lisp Interpreter
 *
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Original by SunSoft Java Series CD-ROM "Java by Example"
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * This class representing boolean value of lisp.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * 
 * @version 0.00 970730 nsano make the initial version <br>
 *          0.01 980128 nsano define symbols as constant <br>
 */
public final class LispBoolean implements LispPrintable {

    /** The true value of the LispBoolean. */
    public static final LispBoolean trueValue = new LispBoolean();

    /** The false value of the LispBoolean. */
    public static final LispBoolean falseValue = new LispBoolean();

    /** The true symbol of the LispBoolean. */
    public static final String trueSymbol = "#t";

    /** The false symbol of the LispBoolean. */
    public static final String falseSymbol = "#f";

    /**
     * Gets the value of the LispBoolean.
     * 
     * @param flag boolean
     * @return the value of the lisp boolean
     */
    public static LispBoolean select(boolean flag) {
        if (flag) {
            return trueValue;
        } else {
            return falseValue;
        }
    }

    /**
     * Returns a String object representing this LispBoolean's value.
     * 
     * @return a string representing this object's value.
     */
    public String toString() {
        return toLispString();
    }

    /**
     * Returns a String object representing this LispBoolean's symbol.
     * 
     * @return a string representing this object's symbol.
     */
    public String toLispString() {
        if (this == LispBoolean.falseValue) {
            return falseSymbol;
        } else {
            return trueSymbol;
        }
    }
}

/* */
