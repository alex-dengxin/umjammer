/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.translation;

import java.io.IOException;
import java.util.Locale;


/**
 * 双方向翻訳機のインターフェースです。
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 030225 nsano initial version <br>
 *          0.01 030226 nsano be interface <br>
 *          0.02 030309 nsano repackage <br>
 */
public interface Translator {

    /** ローカルな言語に翻訳します。 */
    String toLocal(String word) throws IOException;

    /** グローバルな言語(英語)に翻訳します。 */
    String toGlobal(String word) throws IOException;

    /** ローカル側のロケールを取得します。 */
    Locale getLocalLocale();

    /** グローバル側のロケールを取得します。 */
    Locale getGlobalLocal();
}

/* */
