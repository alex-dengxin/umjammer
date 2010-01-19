/*
 * Lisp Interpreter
 *
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Original by SunSoft Java Series CD-ROM "Java by Example"
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;

import java.io.PrintWriter;


/**
 * Prints out lisp primitives as S expression.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class LispPrinter {

    /**
     * Prints lisp primitive to <code>System.out</code> with EOL.
     */
    public static void println(Object toPrint) {
        println(new PrintWriter(System.out, true), toPrint);
    }

    /**
     * Prints lisp primitive to <code>System.out</code>.
     */
    public static void print(Object toPrint) {
        print(new PrintWriter(System.out, true), toPrint);
    }

    /**
     * Prints lisp primitive to <code>PrintWriter</code> with EOL.
     */
    public static void println(PrintWriter out, Object toPrint) {
        print(out, toPrint);
        out.println();
    }

    /**
     * Prints lisp primitive to <code>PrintWriter</code>.
     */
    public static void print(PrintWriter out, Object toPrint) {
        out.print(toLispString(toPrint));
    }

    /**
     * Returns a string representing the specified object.
     */
    public static String toLispString(Object toPrint) {
        if (toPrint instanceof LispPrintable) {
            return ((LispPrintable) toPrint).toLispString();
        }

        if (toPrint instanceof String) {
            String stringToPrint = (String) toPrint;

            StringBuilder buf = new StringBuilder();

            buf.append('"');

            if (stringToPrint.indexOf("\"") != -1) {
                for (int i = 0; i < stringToPrint.length(); i++) {
                    char currentChar = stringToPrint.charAt(i);

                    if (currentChar == '"') {
                        buf.append("\\\"");
                    } else {
                        buf.append(currentChar);
                    }
                }
            } else {
                buf.append(stringToPrint);
            }

            buf.append('"');
            return buf.toString();
        }

        return toPrint.toString();
    }
}

/* */
