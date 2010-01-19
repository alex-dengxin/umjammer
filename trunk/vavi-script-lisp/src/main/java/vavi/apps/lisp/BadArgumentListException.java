/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * 引数のリストが正しくない場合にスローされます。
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BadArgumentListException extends LispException {

    /** 詳細メッセージを持たない BadArgumentListException を構築します。 */
    public BadArgumentListException() {
        super();
    }

    /** 詳細メッセージを持つ BadArgumentListException を構築します。 */
    public BadArgumentListException(String s) {
        super(s);
    }
}

/* */
