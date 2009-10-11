/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;

import vavi.util.Debug;
import vavix.util.screenscrape.XPathScraper;


/**
 * StringJaxenXPathScraper.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 031103 nsano initial version <br>
 *          0.01 031228 nsano outsource xpath <br>
 */
public class StringJaxenXPathScraper extends XPathScraper<InputStream, String> {

    /** encoding for html */
    private String encoding;

    StringJaxenXPathScraper(String xpath, String encoding) {
        super(xpath);
        this.encoding = encoding;
    }

    /** */
    public String scrape(InputStream source) {

        try {
            InputSource is = new InputSource(new InputStreamReader(source, encoding));
            Document document = builder.parse(is);
// PrettyPrinter pp = new PrettyPrinter(new PrintWriter(System.out));
// pp.print(document);

            XPath xPath = new DOMXPath(xpath);
            List<?> list = xPath.selectNodes(document);
// Debug.println(list.size());
            Iterator<?> i = list.iterator();
            String value = null;
            if (i.hasNext()) {
                Node node = (Node) i.next();
// Debug.println(node);
                value = node.getNodeValue();
            }

            return value;
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        } catch (SAXException e) {
            throw (RuntimeException) new IllegalArgumentException("wrong input").initCause(e);
        } catch (JaxenException e) {
            throw (RuntimeException) new IllegalStateException("maybe xpath is wrong").initCause(e);
        }
    }

    /** */
    private static DocumentBuilder builder;

    /** */
    static {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        try {
            builder = dbf.newDocumentBuilder();
Debug.println("builder: " + builder);
Debug.println("isValidating: " + builder.isValidating());
        } catch (ParserConfigurationException e) {
Debug.printStackTrace(e);
            System.exit(1);
        }
    }

    // ----

    /** */
    public static void main(String[] args) throws Exception {
        InputStream is = StringJaxenXPathScraper.class.getResourceAsStream(args[0]);
        for (int i = 1; i < args.length; i++) {
            StringJaxenXPathScraper scraper = new StringJaxenXPathScraper(args[i], "UTF-8");
            System.out.println(scraper.scrape(is));
        }
    }
}

/* */
