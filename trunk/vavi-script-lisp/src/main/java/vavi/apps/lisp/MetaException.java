/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;


/**
 * 入れ子の例外を持つ例外クラスです．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 991209 nsano make the initial version <br>
 */
public final class MetaException extends LispException {

    /** 実際の例外 */
    private Throwable throwable;

    /**
     * 詳細メッセージを持たない MetaException を構築します．
     */
    public MetaException(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * 詳細メッセージを持つ MetaException を構築します．
     */
    public Throwable getThrowable() {
        return throwable;
    }
}

/* */
