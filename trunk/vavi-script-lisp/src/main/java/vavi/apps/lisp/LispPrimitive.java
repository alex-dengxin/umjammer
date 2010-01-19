/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;

import java.util.List;


/**
 * Lisp の式に現れるプリミティブの基本クラスです。
 * ユーザがプリミティブを作成する場合はこのクラスを拡張して使用します。
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * 
 * @version 0.00 970730 nsano make the initial version <br>
 */
public class LispPrimitive implements LispFunction, LispPrintable {

    /**
     * ユーザが作成するプリミティブはこのメソッドをオーバーライドしてください。
     * 
     * @param args 式の引数のリスト．
     * @param interp Lisp インタープリタのインスタンス．
     * @throws LispException If a lisp error occurs.
     */
    public Object apply(List<Object> args, LispInterpreter interp) throws LispException {
        return apply(args);
    }

    /**
     * ユーザが作成するプリミティブはこのメソッドをオーバーライドしてください。
     * 
     * @param args 式の引数のリスト．
     * @throws LispException If a lisp error occurs.
     */
    public Object apply(List<Object> args) throws LispException {
        return LispCons.emptyList;
    }

    /**
     * このプリミティブを表す Lisp としての文字列を返します。
     */
    public String toLispString() {
        return "{primitive}";
    }

    /**
     * 式の引数の数を調べます．
     * 
     * @param name プリミティブの名前
     * @param args 引数のリスト
     * @param len 引数の数
     * @throws WrongArgumentCountException 引数の数が違う場合
     */
    public void checkArgs(String name, List<Object> args, int len) throws WrongArgumentCountException {

        if (args.size() != len) {
            throw new WrongArgumentCountException(name + ": " + "expected " + len + (len == 1 ? " arg; " : " args; ") + "got " + args.size() + ".");
        }
    }
}

/* */
