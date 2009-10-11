/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.jaxp.html.tidy;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * This implements JAXP parser factory interface for Tidy HTML parser.
 * <p>
 * To use cyberneko HTML parser via JAXP, set system property
 * "javax.xml.parsers.SAXParserFactory" to
 * "org.cyberneko.parsers.SAXParserFactoryImpl" before invoking
 * <code>javax.xml.parsers.SAXParserFactory.newInstance</code>.
 * </p>
 * <p>
 * For example: <code><pre>
 *  import javax.xml.parsers.*;
 *  
 *  System.setProperty(&quot;javax.xml.parsers.SAXParserFactory&quot;,
 *  &quot;org.cyberneko.parsers.SAXParserFactoryImpl&quot;);
 *  
 *  SAXParserFactory factory = SAXParserFactory.newInstance();
 *  factory.setNamespaceAware(false);
 *  factory.setValidating(false);
 *  
 *  SAXParser parser = factory.newSAXParser();
 *  parser.parse(new java.io.FileInputStream(&quot;mydoc.xml&quot;), myHandler);
 * </pre></code>
 * </p>
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 031103 nsano initial version <br>
 */
public class SAXParserFactoryImpl extends SAXParserFactory {

    /** */
    public SAXParserFactoryImpl() {
    }
    
    /** */
    public SAXParser newSAXParser()
        throws SAXException, ParserConfigurationException {

        return new SAXParserImpl();
    }

    /** */
    public void setFeature(String name, boolean value) {
        throw new UnsupportedOperationException("not implemented");
    }

    /** */
    public boolean getFeature(String name) {
        throw new UnsupportedOperationException("not implemented");
    }
}

/* */
