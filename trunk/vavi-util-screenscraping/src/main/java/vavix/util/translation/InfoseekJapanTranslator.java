/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.translation;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.Cookie;

import vavix.util.screenscrape.PropertiesUserAgentDao;

import vavi.net.http.HttpContext;
import vavi.util.Debug;
import vavix.util.screenscrape.ApacheHttpScraper;
import vavix.util.screenscrape.SimpleURLScraper;
import vavix.util.screenscrape.StringI18nSimpleXPathScraper;
import vavix.util.screenscrape.StringSimpleXPathScraper;
import vavix.util.screenscrape.UserAgentSwitcher;


/**
 * Infoseek Japan ÇÃã@äBñ|ñÛÇóòópÇ∑ÇÈñ|ñÛã@Ç≈Ç∑ÅB
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 030225 nsano initial version <br>
 *          0.01 030226 nsano be one of service provider <br>
 *          0.02 030309 nsano repackage <br>
 */
public class InfoseekJapanTranslator implements Translator {

    /** url host */
    private static String HOST;
    /** url port */
    private static int PORT;
    /** url path, specify one {0} */
    private static String TO_LOCAL;
    /** url path, specify one {0} */
    private static String TO_GLOBAL;

    /** url encoding */
    private static String encoding;

    /**
     * Commons HttpClient, POST method 
     */
    private static class MyScraper extends ApacheHttpScraper<String> {
        /** */
        static UserAgentSwitcher userAgentSwitcher;
        /* */
        static {
            userAgentSwitcher = new UserAgentSwitcher();
            userAgentSwitcher.setUserAgentDao(new PropertiesUserAgentDao());
        }
        /** */
        public MyScraper(String xpath) {
            super(new StringSimpleXPathScraper(xpath));
        }
        /** */
        public MyScraper(String xpath, final String referer) {
            super(new StringI18nSimpleXPathScraper(xpath, encoding),
                  new Properties() {
                    {
                        String userAgent = userAgentSwitcher.getUserAgent();

                        setProperty("header.User-Agent", userAgent);
                        setProperty("header.Referer", referer);
                        setProperty("header.Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
                        setProperty("header.Accept-Language", "ja,en-us;q=0.5");
                        setProperty("header.Accept-Encoding", "gzip,deflate");
                        setProperty("header.Accept-Charset", "Shift_JIS,utf-8;q=0.7,*;q=0.7");
                        setProperty("header.Keep-Alive", "300");

                        setProperty("proxy.host", "localhost");
                        setProperty("proxy.port", "8080");
                    }
                  });
        }
    }

    /**
     * Java SE, GET method
     */
    private static class MyScraper2 extends SimpleURLScraper<String> {
        /** */
        static UserAgentSwitcher userAgentSwitcher;
        /* */
        static {
            if (userAgentSwitcher == null) {
                userAgentSwitcher = new UserAgentSwitcher();
                userAgentSwitcher.setUserAgentDao(new PropertiesUserAgentDao());
            }
        }
        /** */
        public MyScraper2(String xpath) {
            super(new StringSimpleXPathScraper(xpath));
        }
        /** */
        public MyScraper2(String xpath, final String cookie, final String referer) {
            super(new StringI18nSimpleXPathScraper(xpath, encoding),
                  new Properties() {
                    {
                        String userAgent = userAgentSwitcher.getUserAgent();

                        setProperty("header.User-Agent", userAgent);
                        setProperty("header.Referer", referer);
                        setProperty("header.Cookie", cookie);

                        setProperty("proxy.host", "localhost");
                        setProperty("proxy.port", "8080");
                    }
                  });
        }
    }

    /**
     * @param word use {@link #encoding} when url encoding 
     */
    public String toLocal(String word) throws IOException {
        return translate(word, TO_LOCAL, "0");
    }

    /** post */
    private String translate(String word, String base, String selector) throws IOException {
        MyScraper scraper1 = new MyScraper(xpath1);
        HttpContext request = new HttpContext();
        URL url01 = new URL(url1);
        request.setRemoteHost(url01.getHost());
        request.setRemotePort(80);
        request.setRequestURI(url01.getPath());
        String token = scraper1.scrape(request);
        Cookie[] cookie = scraper1.getCookies();
Debug.println("token: " + token);

        String file = MessageFormat.format(base, word, token);
        URL url = new URL("http", HOST, PORT, file);
Debug.println("url: " + url);

        MyScraper scraper2 = new MyScraper(xpath2, url1);
        scraper2.setCookies(cookie);
        request.setRemoteHost(url.getHost());
        request.setRemotePort(80);
        request.setRequestURI(url.getPath());
        Map<String, String[]> params = request.getParameters();
        params.put("ac", new String[] { "Text" });
        params.put("lng", new String[] { "en" });
        params.put("token", new String[] { token });
        params.put("selector", new String[] { selector });
        params.put("original", new String[] { word });
//        params.put("submit", new String[] { new String("Å@ñ|ñÛÅ@".getBytes("ISO8859_1"), "ISO8859_1") });
        String converted = scraper2.scrape(request);
        return converted;
    }

    /** get */
    private String translate2(String word, String base, String selector) throws IOException {
        MyScraper2 scraper1 = new MyScraper2(xpath1);
        String token = scraper1.scrape(new URL(url1));
        String cookie = scraper1.getCookie();
//      Debug.println("token: " + token);

        word = URLEncoder.encode(word, encoding);
        String file = MessageFormat.format(TO_LOCAL, word, token);
        URL url = new URL("http", HOST, PORT, file);
Debug.println("url: " + url);

        MyScraper2 scraper2 = new MyScraper2(xpath2, cookie, url1);
        String converted = scraper2.scrape(url);
        return converted;
    }

    /**
     * @param word use {@link #encoding} when url encoding 
     */
    public String toGlobal(String word) throws IOException {
        return translate2(word, TO_GLOBAL, "1");
    }

    /** */
    private static String url1;
    /** */
    private static String xpath1;
    /** */
    private static String xpath2;

    /** */
    static {
        final Class<?> clazz = InfoseekJapanTranslator.class;
        final String path = "InfoseekJapanTranslator.properties";
        try {
            Properties props = new Properties();
            props.load(clazz.getResourceAsStream(path));
            
            HOST = props.getProperty("host");
            PORT = Integer.parseInt(props.getProperty("port"));
            TO_LOCAL = props.getProperty("file.toLocal");
            TO_GLOBAL = props.getProperty("file.toGlobal");
            
            encoding = props.getProperty("encoding");

            xpath1 = props.getProperty("xpath1");
            url1 = props.getProperty("url1");
            xpath2   = props.getProperty("xpath2");
        } catch (Exception e) {
Debug.printStackTrace(e);
            System.exit(1);
        }
    }
    
    /** */
    public Locale getLocalLocale() {
        return Locale.JAPANESE;
    }

    /** */
    public Locale getGlobalLocal() {
        return Locale.ENGLISH;
    }

    //----

    /**
     * @param args [0] Japanese sentence, [1] English sentence 
     */
    public static void main(String[] args) throws IOException {

        Translator translator = new InfoseekJapanTranslator();

        System.out.println("---- E to J ----");
        System.out.println("I: " + args[0]);
long t1 = System.currentTimeMillis();
        System.out.println("O: " + translator.toLocal(args[0]));
System.out.println("This translation costs " + (System.currentTimeMillis() - t1) + " ms");

        System.out.println("---- J to E ----");
        System.out.println("I: " + args[1]);
long t2 = System.currentTimeMillis();
        System.out.println("O: " + translator.toGlobal(args[1]));
System.out.println("This translation costs " + (System.currentTimeMillis() - t2) + " ms");
    }
}

/* */
