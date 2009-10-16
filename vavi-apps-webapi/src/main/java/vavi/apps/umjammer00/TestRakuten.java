/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;


/**
 * TestRakuten. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080212 nsano initial version <br>
 */
public class TestRakuten {

    /** */
    public static void main(String[] args) throws Exception {
        String url = "http://api.rakuten.co.jp/rws/1.10/rest?" +
            "developerId=" + args[0] +
            "&operation=VacantHotelSearch" +
            "&version=2007-11-21" +
            "&largeClassCode=japan" +
            "&middleClassCode=kanagawa" +
            "&smallClassCode=hakone" +
            "&checkinDate=2008-08-30" +
            "&checkoutDate=2008-08-31" +
            "&adultNum=4";
        URLConnection uc = new URL(url).openConnection();
        Reader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
        Writer writer = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
        while (true) {
            int c = reader.read();
            if (c < 0) {
                break;
            }
            writer.write(c);
        }
    }
}

/* */
