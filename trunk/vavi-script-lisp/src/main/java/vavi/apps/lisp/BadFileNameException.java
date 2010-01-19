/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * ファイル名が正しくない場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BadFileNameException extends LispException {

    /** 詳細メッセージを持たない BadFileNameException を構築します． */
    public BadFileNameException() {
        super();
    }

    /** 詳細メッセージを持つ BadFileNameException を構築します． */
    public BadFileNameException(String s) {
        super(s);
    }
}

/* */
