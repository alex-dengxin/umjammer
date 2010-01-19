/*
 * Lisp Interpreter
 *
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Original by SunSoft Java Series CD-ROM "Java by Example"
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;

import java.util.Map;


/**
 * This class implements a model of lisp symbol.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispSymbol {

    /**
     * Creates a lisp symbol which value is null.
     * 
     * @param s the symbol name
     */
    public LispSymbol(String s) {
        localVal = null;
        name = s;
    }

    /**
     * If a symbol is found in the hash table, returns the symbol, else adds a lisp symbol to the hash table.
     * 
     * @param h the hash table of lisp symbols
     * @param s the symbol name
     */
    public static LispSymbol intern(Map<String, LispSymbol> h, String s) {

        Object existing = h.get(s);
        if (existing != null) {
            return (LispSymbol) existing;
        } else {
            LispSymbol sym = new LispSymbol(s);
            h.put(s, sym);
            return sym;
        }
    }

    /**
     * Gets the value of this symbol.
     * 
     * @return the symbol value
     */
    public Object localValue() {
        return localVal;
    }

    /**
     * Sets the value of this symbol
     * 
     * @param val the symbol value
     */
    public void setLocalValue(Object val) {
        localVal = val;
    }

    /**
     * Gets the name of this symbol
     * 
     * @return the symbol name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the string representing this object.
     */
    public String toString() {
        return name;
    }

    /** value of this symbol */
    private Object localVal;

    /** the name of this symbol */
    private String name;
}

/* */
