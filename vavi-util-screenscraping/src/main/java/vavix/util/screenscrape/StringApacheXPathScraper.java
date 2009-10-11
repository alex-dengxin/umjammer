/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.InputStream;

import org.apache.commons.jxpath.Container;
import org.apache.commons.jxpath.JXPathContext;

import vavi.xml.util.jxpath.html.HtmlDocumentContainer;


/**
 * Apache Commons JXPath.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051014 nsano initial version <br>
 */
public class StringApacheXPathScraper extends XPathScraper<InputStream, String> {

    /** */
    public StringApacheXPathScraper(String xpath) {
        super(xpath);
    }

    /** */
    public String scrape(InputStream source) {
        Container dc = new HtmlDocumentContainer(source, "JISAutoDetect");
//try {
// PrettyPrinter pp = new PrettyPrinter(new PrintWriter(System.err));
// pp.print((Document) dc.getValue());
// Debug.println(dc);
//} catch (IOException e) {
// e.printStackTrace();
//}
        JXPathContext context = JXPathContext.newContext(dc);
//Debug.println(context);

        return (String) context.getValue(xpath);
    }
}

/* */
