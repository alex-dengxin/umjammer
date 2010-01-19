/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * ファイルのリードライトの方向が正しくない場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BadFileIODirectionException extends LispException {

    /** 詳細メッセージを持たない BadFileIODirectionException を構築します． */
    public BadFileIODirectionException() {
        super();
    }

    /** 詳細メッセージを持つ BadFileIODirectionException を構築します． */
    public BadFileIODirectionException(String s) {
        super(s);
    }
}

/* */
