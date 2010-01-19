/*
 * Lisp Interpreter
 *
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Original by SunSoft Java Series CD-ROM "Java by Example"
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;

import java.util.ArrayList;
import java.util.List;


/**
 * This class supports a mapping between names and values. It is used to store the values of variables for the Lisp system. Each
 * LispEnv is linked to a "parent" LispEnv. If a variable is not found during name lookup, the lookup process continues with the
 * parent.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * 
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispEnv implements LispPrintable {

    /**
     * Creates lisp environment.
     */
    public LispEnv() {
        localVars = new ArrayList<Object>();
        localVals = new ArrayList<Object>();
    }

    /**
     * Creates lisp environment.
     * 
     * @param vars symbols
     * @param vals values of symbols
     */
    public LispEnv(List<Object> vars, List<Object> vals) {
        localVars = vars;
        localVals = vals;
    }

    /**
     * Extends environment address.
     * 
     * @param vars symbols
     * @param vals values of symbols
     */
    public LispEnv extend(List<Object> vars, List<Object> vals) {
        LispEnv newEnv = new LispEnv(vars, vals);
        newEnv.parent = this;
        return newEnv;
    }

    /**
     * Sets the new sybmbol with value.
     * 
     * @param sym the lisp symbol
     * @param val value of the symbol
     * @throws UnboundSymbolException If the symbol is not bound
     */
    public void store(LispSymbol sym, Object val) throws UnboundSymbolException {
        lookupUpdate(sym, val);
    }

    /**
     * Gets the value of the symbol.
     * 
     * @param sym the lisp symbol
     * @throws UnboundSymbolException If the symbol is not bound.
     */
    public Object fetch(LispSymbol sym) throws UnboundSymbolException {
        return lookupUpdate(sym, null);
    }

    /**
     * Defines the new symbol with value.
     * 
     * @param sym the lisp symbol
     * @param val value of the symbol
     * @throws UnboundSymbolException If the symbol is not bound.
     */
    public void define(LispSymbol sym, Object val) throws UnboundSymbolException {
        int index = localVars.indexOf(sym);

        if (index != -1) {
            while (localVals.size() <= index) {
                localVals.add(null);
            }

            store(sym, val);
        } else {
            localVars.add(sym);
            localVals.add(val);
        }
    }

    /**
     * Returns the lisp string representing this object.
     */
    public String toLispString() {
        return "{environment}";
    }

    /**
     * Updates value of the specified symbol.
     * 
     * @param sym the lisp symbol
     * @param valToStore value of the symbol
     */
    private Object lookupUpdate(LispSymbol sym, Object valToStore) throws UnboundSymbolException {
        int varIndex = localVars.indexOf(sym);

        if (varIndex != -1) {
            if (valToStore == null) {
                return localVals.get(varIndex);
            } else {
                localVals.add(varIndex, valToStore);
                return sym;
            }
        }

        if (parent == null) {
            Object symVal = sym.localValue();

            if (symVal == null) {
                throw new UnboundSymbolException();
            }

            if (valToStore == null) {
                return symVal;
            } else {
                sym.setLocalValue(valToStore);
                return null;
            }
        } else {
            return parent.lookupUpdate(sym, valToStore);
        }
    }

    /** Parent environment */
    private LispEnv parent = null;

    /** Local variables */
    private List<Object> localVars;

    /** Local values */
    private List<Object> localVals;
}

/* */
