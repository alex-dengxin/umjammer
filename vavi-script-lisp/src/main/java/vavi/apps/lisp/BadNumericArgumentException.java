/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * 引数に正しくない数値があった場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * 
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BadNumericArgumentException extends LispException {

    /** 詳細メッセージを持たない BadNumericArgumentException を構築します． */
    public BadNumericArgumentException() {
        super();
    }

    /** 詳細メッセージを持つ BadNumericArgumentException を構築します． */
    public BadNumericArgumentException(String s) {
        super(s);
    }
}

/* */
