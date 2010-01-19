/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * 引数の数が違う場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class WrongArgumentCountException extends LispException {

    /** 詳細メッセージを持たない WrongArgumentCountException を構築します． */
    public WrongArgumentCountException() {
        super();
    }

    /** 詳細メッセージを持つ WrongArgumentCountException を構築します． */
    public WrongArgumentCountException(String s) {
        super(s);
    }
}

/* */
