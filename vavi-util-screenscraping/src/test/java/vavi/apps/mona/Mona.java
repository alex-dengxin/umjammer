/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.mona;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;

import vavi.util.Debug;
import vavix.util.screenscrape.Scraper;
import vavix.util.screenscrape.SimpleXPathScraper;


/**
 * 2ch.
 * 
 * @see "http://www.monazilla.org/"
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060922 nsano initial version <br>
 */
class Mona {

    static String urlIdsRegex;
    static String menuUrl;
    static String userAgent;

    /** */
    static {
        try {
            Properties props = new Properties();
            props.load(Mona.class.getResourceAsStream("/vavi/apps/mona/Mona.properties"));

            menuUrl = props.getProperty("mona.url.menu");
            userAgent = props.getProperty("useragent");
        } catch (Exception e) {
Debug.printStackTrace(e);
            System.exit(1);
        }
    }

    /** */
    static class Menu {
        String category;
        String url;
        String key;
        /** bbs.txt */
        static List<Menu> readMenus() throws IOException {
            URLConnection uc = new URL(menuUrl).openConnection();
            Scanner scanner = new Scanner(uc.getInputStream());
            List<Menu> menus = new ArrayList<Menu>();
            while (scanner.hasNextLine()) {
                Menu menu = new Menu();
                menu.category = scanner.nextLine();
                menu.url = scanner.nextLine();
                menu.key = scanner.nextLine();
                menus.add(menu);
            }
            return menus;
        }
        /**  */
        static List<Menu> readMenus2() throws IOException {
            MenuScraper scanner = new MenuScraper();
            List<Menu> menus = scanner.scrape(new URL(menuUrl));
            return menus;
        }
    }

    /**
     * <pre>
     * @HtmlDom(url = "")
     * Menu {
     *  @XPath("/html/body/form/font/b")
     *  Category category;
     *  @XPath("/html/body/form/font/a[3]")
     *  URL url;
     * }
     * Iterable i = url_xpath(, 
     * <pre>
     */
    static class MenuScraper implements Scraper<URL, List<Menu>> {
        /** */
        class MenuXPathScraper extends SimpleXPathScraper<NodeList> {
            MenuXPathScraper(String xpath) {
                super(xpath);
            }
            public NodeList scrape(InputStream source) {
                NodeList nodeList = null;
                try {
                    nodeList = (NodeList) xPath.evaluate(xpath, source, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    throw (RuntimeException) new IllegalStateException().initCause(e);
                }
                return nodeList;
            }
        }

        /**
         * <pre>
         * /html/body/form/font/b
         * </pre>
         * @param url UtaMap  Yahoo! URL 
         */
        public List<Menu> scrape(URL url) {
            try {
                URLConnection uc = url.openConnection();
                uc.getInputStream();
                return null;
            } catch (Exception e) {
                throw (RuntimeException) new IllegalStateException().initCause(e);
            }
        }
    }

    /**
     * @param args 0: url
     */
    public static void main(String[] args) throws Exception {
//        String url = args[0];
        List<Menu> menus = Menu.readMenus();
        for (Menu menu : menus) {
            System.err.println(menu.category + ": " + menu.url);
        }
    }
}

/* */
