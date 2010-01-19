/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * ApplySpecialForm で例外が発生した場合にスローされます。
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BadApplyArgumentsException extends LispException {

    /** 詳細メッセージを持たない BadApplyArgumentsException を構築します。 */
    public BadApplyArgumentsException() {
        super();
    }

    /** 詳細メッセージを持つ BadApplyArgumentsException を構築します。 */
    public BadApplyArgumentsException(String s) {
        super(s);
    }
}

/* */
