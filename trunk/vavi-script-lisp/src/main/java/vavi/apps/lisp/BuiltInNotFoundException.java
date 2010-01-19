/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Original by SunSoft Java Series CD-ROM "Java by Example"
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * ビルトインのプリミティブが存在しない場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BuiltInNotFoundException extends LispException {

    /** 詳細メッセージを持たない BuiltInNotFoundException を構築します． */
    public BuiltInNotFoundException() {
        super();
    }

    /** 詳細メッセージを持つ BuiltInNotFoundException を構築します． */
    public BuiltInNotFoundException(String s) {
        super(s);
    }
}

/* */
