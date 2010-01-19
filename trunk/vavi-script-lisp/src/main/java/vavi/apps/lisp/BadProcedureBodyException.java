/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * プロシージャのボディが正しくない場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 */
public final class BadProcedureBodyException extends LispException {

    /** 詳細メッセージを持たない BadProcedureBodyException を構築します． */
    public BadProcedureBodyException() {
        super();
    }

    /** 詳細メッセージを持つ BadProcedureBodyException を構築します． */
    public BadProcedureBodyException(String s) {
        super(s);
    }
}

/* */
