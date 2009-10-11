/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;

import vavix.util.screenscrape.Scraper;


/**
 * 、文字列で切り出す機です。
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 031103 nsano initial version <br>
 *          0.01 031228 nsano outsource retrying <br>
 */
public class StringPrimitiveScraper implements Scraper<InputStream, String> {

    /** encoding for html */
    private String encoding;

    /** */
    StringPrimitiveScraper(String encoding) {
        this.encoding = encoding;
    }

    /** */
    public String scrape(InputStream is) {
        String html;
        try {
            html = toString(is);
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }

        String[] htmlParts = html.split("translation_TEXT");
// Debug.println("htmlParts.length: " + htmlParts.length);
        if (htmlParts.length == 3) {
// Debug.println(htmlParts[1]);
            String[] parts1 = htmlParts[1].split("\\<textarea[\\w\"=\\s]+\\>");
// Debug.println("parts1.length: " + parts1.length);
            if (parts1.length == 2) {
// Debug.println(parts1[1]);
                String[] parts2 = parts1[1].split("\\</textarea\\>");
                if (parts2.length == 2) {
// Debug.println("parts2.length: " + parts2.length);
                    return parts2[0];
                }
            }
        }

        throw new NoSuchElementException();
    }

    /** */
    private String toString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));

        while (br.ready()) {
            String l = br.readLine();
            sb.append(l);
        }

        br.close();

// Debug.println(sb);

        return sb.toString();
    }
}

/* */
