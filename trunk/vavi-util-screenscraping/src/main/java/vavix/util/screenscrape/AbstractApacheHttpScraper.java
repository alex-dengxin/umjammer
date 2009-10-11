/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;


/**
 * Apache Commons HttpClient Base.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 071004 nsano initial version <br>
 */
abstract class AbstractApacheHttpScraper<I, O> extends AbstractHttpScraper<I, O> implements Scraper<I, O> {
    /** */
    protected String realm;
    /** */
    protected String host;
    /** */
    protected Credentials credentials;

    /** */
    protected AbstractApacheHttpScraper(Scraper<InputStream, O> scraper) {
        super(scraper);
        responseHeaders = new HashMap<String, List<String>>();
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
    public AbstractApacheHttpScraper(Scraper<InputStream, O> scraper, Properties props) {
        this(scraper);
        // auth
        String account = props.getProperty("auth.account");
        String password = props.getProperty("auth.password");
        if (account != null && password != null) {
            credentials = new UsernamePasswordCredentials(account, password);
            realm = props.getProperty("auth.realm");
            host = props.getProperty("auth.host");
        }
        injectRequestHeaders(props);
        injectProxy(props);
    }

    /** */
    protected ErrorHandler<Integer> errorHandler = new ErrorHandler<Integer>() {
        public void handle(Integer status) throws IOException {
            if (status != 200) {
                throw new IllegalStateException("unexpected result: " + status);
            }
        }
    };

    /** */
    protected void applyAuthentication(HttpClient client, HttpMethod method) {
        if (credentials != null) {
            client.getState().setCredentials(realm, host, credentials);
            method.setDoAuthentication(true);
        }
    }

    /** */
    protected void applyRequestHeaders(HttpMethod method) {
        for (String name : requestHeaders.keySet()) {
            String value = requestHeaders.get(name);
            method.setRequestHeader(name, value);
        }
    }

    /** */
    protected void retrieveResponseHeaders(HttpMethod method) {
        for (Header header : method.getResponseHeaders()) {
            String name = header.getName();
            String value = header.getValue();
            if (responseHeaders.containsKey(name)) {
                List<String> values = responseHeaders.get(name);
                values.add(value);
            } else {
                List<String> values = new ArrayList<String>();
                values.add(value);
                responseHeaders.put(name, values);
            }
        }
    }

    /** */
    protected void applyProxy(HttpMethodBase method, String host) {
        HostConfiguration hc = new HostConfiguration();
        hc.setHost(host);
        hc.setProxy(proxyHost, proxyPort);
        method.setHostConfiguration(hc);
    }

    /** */
    protected Cookie[] cookies; 

    /** to application */
    public Cookie[] getCookies() {
        return cookies;
    }

    /** from application */
    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    /** to HttpClient */
    protected void applyCookies(HttpClient httpClient) {
        httpClient.getState().addCookies(cookies);
    }

    /** from HttpClient */
    protected void retrieveCookies(HttpClient httpClient) {
        this.cookies = httpClient.getState().getCookies();
    }
}

/* */
