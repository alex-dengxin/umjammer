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
 * This class is the base class of built in primitives.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispBuiltIn implements LispPrintable {

    /** Creates a built in primitive */
    public LispBuiltIn(String newName, String newPrefix) {
        name = newPrefix + "." + newName;
    }

    /** Returns a string representing this primitive */
    public String toLispString() {
        return "{built-in reference: " + getName() + "}";
    }

    /** Gets the name */
    public String getName() {
        return name;
    }

    /** The name */
    private String name;
}

/* */
