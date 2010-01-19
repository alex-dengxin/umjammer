/*
 * Lisp Interpreter, All rights reserved.
 *
 * Copyright (c) 1997 by Naohide Sano
 *
 * Original by SunSoft Java Series CD-ROM "Java by Example"
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * シンタックスに間違いがあった場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class InvalidLispExpressionException extends LispException {

    /**
     * 詳細メッセージを持たない InvalidLispExpressionException を構築します．
     */
    public InvalidLispExpressionException() {
        super();
    }

    /** 詳細メッセージを持つ InvalidLispExpressionException を構築します． */
    public InvalidLispExpressionException(String s) {
        super(s);
    }
}

/* */
