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
 * 評価する S 式が正しくない場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BadEvalExpressionException extends LispException {

    /** 詳細メッセージを持たない BadEvalExpressionException を構築します． */
    public BadEvalExpressionException() {
        super();
    }

    /** 詳細メッセージを持つ BadEvalExpressionException を構築します． */
    public BadEvalExpressionException(String s) {
        super(s);
    }
}

/* */
