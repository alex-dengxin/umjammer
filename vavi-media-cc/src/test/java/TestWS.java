/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import vavi.net.ws.webservicex.translation.TranslateService;
import vavi.net.ws.webservicex.translation.TranslateServiceSoap;


/**
 * TestWS. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070930 nsano initial version <br>
 */
public class TestWS {

    /**
     * 
     * @param args 0: language, 1: text
     */
    public static void main(String[] args) throws Exception {
        String languageMode = args[0];
        String text = args[1];
//      ApplicationContext applicationContext = new ClassPathXmlApplicationContext("ApplicationContext.xml");
//      Translate translate = (Translate) applicationContext.getBean("client");

        TranslateService service = new TranslateService();
        TranslateServiceSoap client = service.getTranslateServiceSoap();

        String result = client.translate(languageMode, text);

        System.out.println(result);
    }
}

/* */
