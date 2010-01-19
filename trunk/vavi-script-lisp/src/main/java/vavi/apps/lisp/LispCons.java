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
 * This class representing list.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispCons implements LispPrintable {

    /** The empty list */
    public static final LispCons emptyList = new LispCons(null, null);

    /** Initializes */
    static {
        emptyList.car = emptyList;
        emptyList.cdr = emptyList;
    }

    /** Creates an empty list */
    public LispCons() {
        car = emptyList;
        cdr = emptyList;
    }

    /** Creates a list */
    public LispCons(Object newCar, Object newCdr) {
        car = newCar;
        cdr = newCdr;
    }

    /** Gets the previous list */
    public Object getCar() {
        return car;
    }

    /** Gets the next list */
    public Object getCdr() {
        return cdr;
    }

    /** Sets the previous list */
    public void setCar(Object newCar) {
        car = newCar;
    }

    /** Sets the next list */
    public void setCdr(Object newCdr) {
        cdr = newCdr;
    }

    /** */
    public Object getCadr() {
        return ((LispCons) cdr).getCar();
    }

    /** */
    public Object getCddr() {
        return ((LispCons) cdr).getCdr();
    }

    /** */
    public Object getCaddr() {
        return ((LispCons) getCddr()).getCar();
    }

    /** */
    public String toString() {
        return toLispString();
    }

    /** Returns a string representing this primitive */
    public String toLispString() {
        StringBuilder buf = new StringBuilder();
        write(buf, true);
        return buf.toString();
    }

    /** Prints out a string representing this primitive to the buffer */
    private void write(StringBuilder buf, boolean first_element) {
        if (this == emptyList) {
            buf.append("()");
            return;
        }

        if (!first_element) {
            buf.append(' ');
        } else {
            buf.append('(');
        }

        if (getCar() instanceof LispCons) {
            ((LispCons) getCar()).write(buf, true);
        } else {
            buf.append(LispPrinter.toLispString(getCar()));
        }

        if (getCdr() == emptyList) {
            buf.append(')');
        } else if (getCdr() instanceof LispCons) {
            ((LispCons) getCdr()).write(buf, false);
        } else {
            buf.append(" . ");
            buf.append(LispPrinter.toLispString(getCdr()));
            buf.append(')');
        }
    }

    /** The previous list */
    private Object car;

    /** The next list */
    private Object cdr;
}

/* */
