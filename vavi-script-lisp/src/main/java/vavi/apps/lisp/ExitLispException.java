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
 * exit プリミティブを処理した場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class ExitLispException extends LispException {

    /** 詳細メッセージを持たない ExitLispException を構築します． */
    public ExitLispException() {
        super();
    }

    /** 詳細メッセージを持つ ExitLispException を構築します． */
    public ExitLispException(String s) {
        super(s);
    }
}

/* */
