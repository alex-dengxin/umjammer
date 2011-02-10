/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import vavix.util.translation.Translator;
import vavi.util.Debug;
import vavix.util.translation.InfoseekJapanTranslator;
import vavi.util.win32.WindowsProperties;


/**
 * ini 形式のファイルの値を翻訳します。
 */
public class t108_1 {
    public static void main(String[] args) throws IOException {
        WindowsProperties props = new WindowsProperties();
        props.load(new FileInputStream(args[0]));

        Translator translator = new InfoseekJapanTranslator();

        Iterator<Object> i = props.keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            String value = props.getProperty(key);
            String translated = null;
            try {
                translated = translator.toLocal(value);
            } catch (IOException e) {
Debug.println(e);
                translated = "★★★ 翻訳失敗 ★★★[" + value + "]";
            }
            props.setProperty(key, translated);
        }

        props.store(new FileOutputStream(args[1]), "created by t1");
    }
}

/* */
