/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.List;
import java.util.Properties;


/**
 * Java SE HttpUrlConnection GET method.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051014 nsano initial version <br>
 */
public class SimpleURLScraper<O> extends AbstractHttpScraper<URL, O> {

    /** */
    public SimpleURLScraper(Scraper<InputStream, O> scraper) {
        super(scraper);
    }

    /** */
    protected ErrorHandler<HttpURLConnection> errorHandler = new ErrorHandler<HttpURLConnection>() {
        public void handle(HttpURLConnection connection) throws IOException {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IllegalStateException("unexpected result: " + status);
            }
        }
    };

    /**
     * @param props use followings
     * <pre>
     *  "auth.account"
     *  "auth.password"
     *  "header.${header.name}"
     * </pre>
     */
    public SimpleURLScraper(Scraper<InputStream, O> scraper, Properties props) {
        this(scraper);
        final String account = props.getProperty("auth.account");
        final String password = props.getProperty("auth.password");
        if (account != null && password != null) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication(account, password.toCharArray());
                }
            });
        }
        injectRequestHeaders(props);
        injectProxy(props);
    }

    /**
     * @throws IllegalStateException when an error occurs 
     */
    public O scrape(URL url) {
        try {
            applyProxy();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);
            applyRequestHeaders(connection);

            errorHandler.handle(connection);

            applyResponseHeaders(connection); 

            O value = scraper.scrape(connection.getInputStream());

            connection.disconnect();

            return value;
        } catch (IOException e) {
e.printStackTrace(System.err);
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    private void applyRequestHeaders(HttpURLConnection connection) {
        for (String name : requestHeaders.keySet()) {
            String value = requestHeaders.get(name);
//Debug.println("header: " + name + " = " + value);
            connection.setRequestProperty(name, value);
        }
    }

    /** */
    private void applyResponseHeaders(HttpURLConnection connection) {
        responseHeaders = connection.getHeaderFields(); 
    }

    /** */
    protected void applyProxy() {
        if (proxyHost != null) {
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", String.valueOf(proxyPort));
        }
    }

    /** */
    public String getCookie() {
        List<String> values = responseHeaders.get("Set-Cookie"); 

        String cookieValue = null; 
        for (String value : values) {
             if (cookieValue == null) {
                 cookieValue = value;
             } else {
                 cookieValue = cookieValue + ";" + value;
             }
        } 
//Debug.println("cookie: " + cookieValue);
        return cookieValue;
    }
}

/* */
