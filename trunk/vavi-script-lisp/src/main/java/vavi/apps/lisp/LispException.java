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
 * このパッケージ内の例外の基本クラスです．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public class LispException extends Exception {

    /** 詳細メッセージを持たない LispException を構築します． */
    public LispException() {
        super();
    }

    /** 詳細メッセージを持つ LispException を構築します． */
    public LispException(String s) {
        super(s);
    }
}

/* */
