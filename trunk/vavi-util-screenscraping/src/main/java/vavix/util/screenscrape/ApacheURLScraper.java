/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;


/**
 * Apache Commons HttpClient GET method.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051014 nsano initial version <br>
 */
public class ApacheURLScraper<O> extends AbstractApacheHttpScraper<URL, O> {

    /** */
    protected ApacheURLScraper(Scraper<InputStream, O> scraper) {
        super(scraper);
    }

    /**
     * @param props use followings
     * <pre>
     *  "auth.account" BASIC 認証アカウント名
     *  "auth.password" BASIC 認証パスワード
     *  "auth.realm" BASIC 認証レルム
     *  "auth.host"
     *  "header.${header.name}"
     * </pre>
     */
    public ApacheURLScraper(Scraper<InputStream, O> scraper, Properties props) {
        super(scraper, props);
    }

    /**
     * @throws IllegalStateException when an error occurs
     */
    public O scrape(URL url) {
        try {
            HttpClient client = new HttpClient();

            GetMethod get = new GetMethod(url.toString());
            applyAuthentication(client, get);
            applyCookies(client);
            applyRequestHeaders(get);
            int status = client.executeMethod(get);

            errorHandler.handle(status);

            retrieveResponseHeaders(get);
            retrieveCookies(client);

            O value = scraper.scrape(get.getResponseBodyAsStream());

            get.releaseConnection();

            return value;
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
