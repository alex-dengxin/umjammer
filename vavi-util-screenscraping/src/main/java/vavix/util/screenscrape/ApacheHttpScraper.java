/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import vavi.net.http.HttpContext;
import vavi.util.Debug;


/**
 * Apache Commons HttpClient POST method.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 071004 nsano initial version <br>
 */
public class ApacheHttpScraper<O> extends AbstractApacheHttpScraper<HttpContext, O> {

    /** */
    protected ApacheHttpScraper(Scraper<InputStream, O> scraper) {
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
    public ApacheHttpScraper(Scraper<InputStream, O> scraper, Properties props) {
        super(scraper, props);
    }

    /**
     * @throws IllegalStateException when an error occurs
     */
    public O scrape(HttpContext request) {
        try {
            HttpClient client = new HttpClient();

            String url = "http://" + request.getRemoteHost() + ":" + request.getRemotePort() + request.getRequestURI();
Debug.println("post url: " + url);
            PostMethod post;
            if (proxyHost != null) {
                post = new PostMethod();
                applyProxy(post, request.getRemoteHost());
            } else {
                post = new PostMethod(url);
            }
            applyAuthentication(client, post);
            applyCookies(client);
            applyRequestHeaders(post);
            applyRequestParameters(post, request);
            post.setHttp11(true);
            int status = client.executeMethod(post);

            errorHandler.handle(status);

            retrieveResponseHeaders(post);
            retrieveCookies(client);

            O value = scraper.scrape(post.getResponseBodyAsStream());

            post.releaseConnection();

            return value;
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    private void applyRequestParameters(PostMethod method, HttpContext request) {
        for (String name : request.getParameters().keySet()) {
            for (String value : request.getParameters().get(name)) {;
Debug.println("post param: " + name + ": " + value);
                method.setParameter(name, value);
            }
        }
    }
}

/* */
