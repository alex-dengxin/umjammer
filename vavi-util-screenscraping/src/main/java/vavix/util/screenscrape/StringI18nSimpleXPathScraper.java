/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.InputSource;

import vavix.util.screenscrape.SimpleXPathScraper;


/**
 * Java SE XPath Ç≈êÿÇËèoÇ∑ Scraper Ç≈Ç∑ÅB
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 050909 nsano initial version <br>
 */
public class StringI18nSimpleXPathScraper extends SimpleXPathScraper<String> {

    /** encoding for html */
    private String encoding;

    /** */
    public StringI18nSimpleXPathScraper(String xpath, String encoding) {
        super(xpath);
        this.encoding = encoding;
    }

    /** ÅB */
    public String scrape(InputStream is) {

        try {
            InputSource in = new InputSource(new InputStreamReader(is, encoding));
//try {
// PrettyPrinter pp = new PrettyPrinter(System.out);
// pp.print(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is));
//} catch (Exception e) {
// Debug.println(e);
//}
            String value = xPath.evaluate(xpath, in);
            return value;
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        } catch (XPathExpressionException e) {
            throw (RuntimeException) new IllegalArgumentException("wrong input").initCause(e);
        }
    }

    // ----

    /**
     * @param args 0: url, 1: xpath 
     */
    public static void main(String[] args) throws Exception {
        InputStream is = StringI18nSimpleXPathScraper.class.getResourceAsStream(args[0]);
        for (int i = 1; i < args.length; i++) {
            StringI18nSimpleXPathScraper scraper = new StringI18nSimpleXPathScraper(args[1], "UTF-8");
            System.out.println(scraper.scrape(is));
        }
    }
}

/* */
