/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.net.URL;
import java.util.Properties;

import vavi.util.Debug;
import vavix.util.screenscrape.Scraper;
import vavix.util.screenscrape.SimpleURLScraper;
import vavix.util.screenscrape.StringSimpleXPathScraper;


/**
 * XPath 
 * xpath.properties  HTML  xpath 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040311 nsano initial version <br>
 */
public class getGlobalIp {

    /** @see "xpath.properties" */
    static String xpath;

    /** */
    static {
        final Class<?> clazz = getGlobalIp.class;
        final String path = "xpath.properties";
        try {
            Properties props = new Properties();
            props.load(clazz.getResourceAsStream(path));

            xpath = props.getProperty("xpath");
        } catch (Exception e) {
Debug.printStackTrace(e);
            System.exit(1);
        }
    }

    /**
     * 
     * @param args url realm host account password
     * realm 
     */
    public static void main(String[] args) throws Exception {
        String url = args[0];

        Properties props = new Properties();
        props.setProperty("realm", args[1]);
        props.setProperty("host", args[2]);
        props.setProperty("account", args[3]);
        props.setProperty("password", args[4]);

//      Scraper<URL, String> scraper = new ApacheURLScraper(new ApacheStreamXPathScraper(xpath), props);
        Scraper<URL, String> scraper = new SimpleURLScraper<String>(new StringSimpleXPathScraper(xpath), props);

        System.out.println(scraper.scrape(new URL(url)));
    }
}

/* */
