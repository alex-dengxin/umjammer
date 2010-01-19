/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * 読みこむファイル名が正しくない場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BadLoadFileNameException extends LispException {

    /** 詳細メッセージを持たない BadLoadFileNameException を構築します． */
    public BadLoadFileNameException() {
        super();
    }

    /** 詳細メッセージを持つ BadLoadFileNameException を構築します． */
    public BadLoadFileNameException(String s) {
        super(s);
    }
}

/* */
