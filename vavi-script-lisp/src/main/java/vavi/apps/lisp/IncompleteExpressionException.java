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
 * S 式が正しく終了していない場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class IncompleteExpressionException extends LispException {

    /**
     * 詳細メッセージを持たない IncompleteExpressionException を構築します．
     */
    public IncompleteExpressionException() {
        super();
    }

    /** 詳細メッセージを持つ IncompleteExpressionException を構築します． */
    public IncompleteExpressionException(String s) {
        super(s);
    }
}

/* */
