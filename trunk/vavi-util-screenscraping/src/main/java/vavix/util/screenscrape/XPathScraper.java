/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;


/**
 * DOM 化可能な Stream から {@link #xpath} を用いてデータを切り出すインターフェースです。
 *
 * {@link StringApacheXPathScraper} と {@link StringSimpleXPathScraper} のサンプルを参照してください。
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051014 nsano initial version <br>
 */
public abstract class XPathScraper<I, O> implements Scraper<I, O> {

    /** 切り出しに用いる XPath */
    protected String xpath;

    /** @param xpath 切り出しに用いる XPath 文字列 */
    protected XPathScraper(String xpath) {
        this.xpath = xpath;
    }
}

/* */
