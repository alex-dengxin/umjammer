/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;


/**
 * Java SE XPath.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051014 nsano initial version <br>
 */
public abstract class SimpleXPathScraper<O> extends XPathScraper<InputStream, O> {

    /** */
    protected XPath xPath = XPathFactory.newInstance().newXPath();

    /** */
    public SimpleXPathScraper(String xpath) {
        super(xpath);
    }
}

/* */
