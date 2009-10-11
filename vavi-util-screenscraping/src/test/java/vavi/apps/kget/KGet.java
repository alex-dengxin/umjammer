/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.kget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import vavi.util.Debug;
import vavix.util.screenscrape.Scraper;


/**
 * KGet Downloader.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060922 nsano initial version <br>
 */
class KGet {

    static String lyricsUrlXpath;
    static String targetXpath;
    static String searchUrlFormat;
    static String lyricsUrlFormat;
    static String userAgent;
    static String encoding;

    /** */
    static {
        try {
            Properties props = new Properties();
            props.load(KGet.class.getResourceAsStream("/vavi/apps/kget/KGet.properties"));

            searchUrlFormat = props.getProperty("search.url.format");
            lyricsUrlFormat = props.getProperty("lyrics.url.format");
            lyricsUrlXpath = props.getProperty("lyrics.url.xpath");
            targetXpath = props.getProperty("target.xpath");
            userAgent = props.getProperty("useragent");
            encoding = props.getProperty("encoding");
        } catch (Exception e) {
Debug.printStackTrace(e);
            System.exit(1);
        }
    }

    /** */
    static class KGetURLScraper implements Scraper<String[], File> {
        /** */
        private HttpClient httpClient = new HttpClient();

        XPath xPath = XPathFactory.newInstance().newXPath();

        /**
         * <pre>
         * /kashi.swf?sn=27f8851e254bd313506f3ade935b3f3b/27574
         * /showKashi.asp?sn=27f8851e254bd313506f3ade935b3f3b/27574
         * </pre>
         * @param args 
         */
        public File scrape(String[] args) {
            try {
                String artist = URLEncoder.encode(args[0], encoding);
                String title = URLEncoder.encode(args[1], encoding);

                // 1. 
                String searchUrl = String.format(searchUrlFormat, 0, artist, title, "", "");
System.err.println("search: " + searchUrl);

                GetMethod get = new GetMethod(searchUrl);
                get.setRequestHeader("User-Agent", userAgent);
                int status = httpClient.executeMethod(get);
                if (status != 200) {
                    throw new IllegalStateException("unexpected result getting 'search': " + status);
                }

                Reader reader = new InputStreamReader(get.getResponseBodyAsStream(), encoding);
                reader.read(); // cr special hack!
                reader.read(); // lf special hack!
                InputSource is = new InputSource(reader);
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse(is);
//PrettyPrinter pp = new PrettyPrinter(System.err);
//JTreePrinter pp = new JTreePrinter(false);
//pp.print(document);
//System.err.println("lyricsUrlXpath: " + lyricsUrlXpath);
                String lyricsUrl = xPath.evaluate(lyricsUrlXpath, document);
System.err.println("lyricsUrl: " + lyricsUrl);
                if (lyricsUrl.equals("")) {
                    throw new IllegalArgumentException(args[0] + "/" + args[1] + " not found.");
                }

                // 2. 
                get = new GetMethod(lyricsUrl);
                get.setRequestHeader("User-Agent", userAgent);
                status = httpClient.executeMethod(get);
                if (status == 302) {
                    // 
                    lyricsUrl = get.getResponseHeader("Location").getValue();
System.err.println("redirectUrl: " + lyricsUrl);
                    get = new GetMethod(lyricsUrl);
                    status = httpClient.executeMethod(get);
                }
                if (status != 200) {
                    throw new IllegalStateException("unexpected result getting 'search': " + status);
                }

                reader = new InputStreamReader(get.getResponseBodyAsStream(), encoding);
                is = new InputSource(reader);
                documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = documentBuilder.parse(is);

                String target = xPath.evaluate(targetXpath, document);
System.err.println("target: " + target);
                if (lyricsUrl.equals("")) {
                    throw new IllegalArgumentException(args[0] + "/" + args[1] + " not found.");
                }

                // 
                String sn = null;
                String[] argsElements = target.substring(target.indexOf('?') + 1).split("&");
                for (String argsElement : argsElements) {
                    String[] pairElement = argsElement.split("=");
                    String name = URLDecoder.decode(pairElement[0], encoding);
                    if (pairElement.length > 1) {
                        String value = URLDecoder.decode(pairElement[1], encoding);
                        if (name.equals("n")) {
                            sn = value;
                        }
                    }
                }

                String tagetUrl = String.format(lyricsUrlFormat, sn);
System.err.println("target: " + tagetUrl);
                get = new GetMethod(tagetUrl);
                get.setRequestHeader("User-Agent", userAgent);
                status = httpClient.executeMethod(get);
                if (status != 200) {
                    throw new IllegalStateException("unexpected result getting 'lyrics': " + status);
                }

                String result = new String(get.getResponseBody(), encoding);

                get.releaseConnection();

                String lyrics = null;
                argsElements = result.split("&");
                for (String argsElement : argsElements) {
                    String[] pairElement = argsElement.split("=");
                    String name = pairElement[0];
                    if (pairElement.length > 1) {
                        String value = pairElement[1];
                        if (name.equals("kashiText")) {
                            lyrics = value;
                        }
                    }
                }
System.err.println("lyrics:\n" + lyrics);

                // 
                File file = File.createTempFile("kget", ".txt");
                FileOutputStream out = new FileOutputStream(file);
                out.write(lyrics.getBytes());
                out.flush();
                out.close();
                
                return file;
            } catch (Exception e) {
                throw (RuntimeException) new IllegalStateException().initCause(e);
            }
        }
    }

    /**
     * @param args 0: Yahoo! url for lyrics 
     */
    public static void main(String[] args) throws Exception {
        String artist = args[0];
        String title = args[1];

        Scraper<String[], File> scraper = new KGetURLScraper();
        File file = scraper.scrape(new String[] { artist, title });
System.err.println("file:\n" + file);
    }
}

/* */
