/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.InputStream;

import org.apache.commons.jxpath.Container;
import org.apache.commons.jxpath.JXPathContext;

import vavi.xml.util.jxpath.html.HtmlDocumentContainer;
import vavix.util.screenscrape.XPathScraper;


/**
 * 、JXPath で切り出す機です。
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 031103 nsano initial version <br>
 *          0.01 031228 nsano outsource xpath <br>
 */
public class StringJXPathXPathScraper extends XPathScraper<InputStream, String> {

    /** encoding for html */
    private String encoding;

    StringJXPathXPathScraper(String xpath, String encoding) {
        super(xpath);
        this.encoding = encoding;
    }

    /** 翻訳します。 */
    public String scrape(InputStream source) {

        Container dc = new HtmlDocumentContainer(source, encoding);
        JXPathContext context = JXPathContext.newContext(dc);

        return (String) context.getValue(xpath);

//        DocumentContainer.registerXMLParser(DocumentContainer.MODEL_DOM, new HTMLParser(encoding));
        // TODO 気に入らん、org.w3c.dom.Document が引数やろ？
//        DocumentContainer dc = new DocumentContainer(url, DocumentContainer.MODEL_DOM);
//Debug.println("dc: " + dc.getValue());
//Debug.println(dc);
//        JXPathContext context = JXPathContext.newContext(dc);
//Debug.println(context);

//        String value = (String) context.getValue(xpath);
//Debug.println(value);

//        return value;
    }
    
    //----
    
    /** */
    public static void main(String[] args) throws Exception {
        StringJXPathXPathScraper scraper = new StringJXPathXPathScraper(args[1], "JISAutoDetect");
        InputStream is = StringJXPathXPathScraper.class.getResourceAsStream(args[0]);
        System.out.println(scraper.scrape(is));
    }
}

/* */
