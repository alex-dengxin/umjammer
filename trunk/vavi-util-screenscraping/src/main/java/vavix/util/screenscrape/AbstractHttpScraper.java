/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * AbstractHttpScraper.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 071004 nsano initial version <br>
 */
abstract class AbstractHttpScraper<I, O> implements Scraper<I, O> {
    /** */
    protected Scraper<InputStream, O> scraper;

    /** */
    public AbstractHttpScraper(Scraper<InputStream, O> scraper) {
        this.scraper = scraper;
    }

    /** */
    protected Map<String, String> requestHeaders = new HashMap<String, String>();

    /** */
    protected Map<String, List<String>> responseHeaders;

    /** */
    protected interface ErrorHandler<T> {
        void handle(T connection) throws IOException;
    }

    /** */
    protected void injectRequestHeaders(Properties props) {
        final String headerKey = "header.";
        Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith(headerKey)) {
                String value = props.getProperty(key);
                String name = key.substring(headerKey.length());
                requestHeaders.put(name, value);
            }
        }
    }

    /** */
    protected String proxyHost;

    /** */
    protected int proxyPort;

    /** */
    protected void injectProxy(Properties props) {
        this.proxyHost = props.getProperty("proxy.host");
        this.proxyPort = Integer.parseInt(props.getProperty("proxy.port"));
    }
}

/* */
