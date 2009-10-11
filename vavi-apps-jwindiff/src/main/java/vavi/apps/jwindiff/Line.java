/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;


/**
 * Line.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040612 vavi refactoring <br>
 */
class Line {
    /** */
    enum Type {
        /** */
        PLAIN,
        /** */
        DELETED,
        /** */
        INSERTED,
        /** */
        MOVEDTO,
        /** */
        MOVEDFROM;
    }

    /** */
    private int lineNumber;

    /** */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /** */
    public int getLineNumber() {
        return lineNumber;
    }

    /** */
    private String line;

    /** */
    public void setLine(String line) {
        this.line = line;
    }

    /** */
    public String getLine() {
        return line;
    }

    /** */
    private Type flag;

    /** */
    public void setFlag(Type flag) {
        this.flag = flag;
    }

    /** */
    public Type getFlag() {
        return flag;
    }

    /** */
    public Line(int lineNumber, String line, Type flag) {
        this.lineNumber = lineNumber;
        this.line = line;
        this.flag = flag;
    }
}

/* */
