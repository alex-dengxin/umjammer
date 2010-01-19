/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Original by SunSoft Java Series CD-ROM "Java by Example"
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * 登録されていないシンボルを使用した場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class UnboundSymbolException extends LispException {

    /** 詳細メッセージを持たない UnboundSymbolException を構築します． */
    public UnboundSymbolException() {
        super();
    }

    /** 詳細メッセージを持つ UnboundSymbolException を構築します． */
    public UnboundSymbolException(String s) {
        super(s);
    }
}

/* */
