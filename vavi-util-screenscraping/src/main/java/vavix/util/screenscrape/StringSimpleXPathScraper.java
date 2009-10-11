/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.InputStream;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.InputSource;


/**
 * Java SE XPath.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051014 nsano initial version <br>
 */
public class StringSimpleXPathScraper extends SimpleXPathScraper<String> {

    /** */
    public StringSimpleXPathScraper(String xpath) {
        super(xpath);
    }

    /** */
    public String scrape(InputStream source) {
        try {
            return xPath.evaluate(xpath, new InputSource(source));
        } catch (XPathExpressionException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
