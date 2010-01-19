/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * 引数の型が合わない場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */

public final class BadArgumentTypeException extends LispException {

    /** 詳細メッセージを持たない BadArgumentTypeException を構築します． */
    public BadArgumentTypeException() {
        super();
    }

    /** 詳細メッセージを持つ BadArgumentTypeException を構築します． */
    public BadArgumentTypeException(String s) {
        super(s);
    }
}

/* */
