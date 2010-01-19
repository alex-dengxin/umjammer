/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * 正しくない loop が発生した場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BadLoopException extends LispException {

    /** 詳細メッセージを持たない BadLoopException を構築します． */
    public BadLoopException() {
        super();
    }

    /** 詳細メッセージを持つ BadLoopException を構築します． */
    public BadLoopException(String s) {
        super(s);
    }
}

/* */
