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
 * ビルトインのプリミティブが正しくない場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BadBuiltInException extends LispException {

    /** 詳細メッセージを持たない BadBuiltInException を構築します． */
    public BadBuiltInException() {
        super();
    }

    /** 詳細メッセージを持つ BadBuiltInException を構築します． */
    public BadBuiltInException(String s) {
        super(s);
    }
}

/* */
