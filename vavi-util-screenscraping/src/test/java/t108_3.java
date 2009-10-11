/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import vavix.util.translation.Translator;
import vavi.util.Debug;
import vavix.util.translation.InfoseekJapanTranslator;


/**
 * properties 
 */
public class t108_3 {
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
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
                translated = "Ç∑ÇŒÇµÇ¡Ç±Ç¢íÉêFÇÃåœÇÕÇÃÇÎÇ‹Ç»å¢ÇîÚÇ—âzÇ¶ÇÈ[" + value + "]";
            }
            props.setProperty(key, translated);
        }

        props.store(new FileOutputStream(args[1]), "created by t1");
    }
}

/* */
