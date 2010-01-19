/*
 * Lisp Interpreter
 *
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * コメントを読んだ場合にスローされます．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970806 nsano make the initial version <br>
 */
public final class CommentLispException extends LispException {

    /** 詳細メッセージを持たない CommentLispException を構築します． */
    public CommentLispException() {
        super();
    }

    /** 詳細メッセージを持つ CommentLispException を構築します． */
    public CommentLispException(String s) {
        super(s);
    }
}

/* */
