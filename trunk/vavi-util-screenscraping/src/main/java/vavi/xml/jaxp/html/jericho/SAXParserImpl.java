/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.jaxp.html.jericho;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;


/**
 * This implements JAXP parser interface for Tidy HTML parser.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 060926 nsano initial version <br>
 */
@SuppressWarnings(value="deprecation")
public class SAXParserImpl extends SAXParser {

    /** */
    private org.xml.sax.Parser parser;

    /** */
    SAXParserImpl() throws SAXException, ParserConfigurationException {
        parser = new org.cyberneko.html.parsers.SAXParser(); // TODO use Tidy
    }
    
    /** */
    public org.xml.sax.Parser getParser() throws SAXException {
        return parser;
    }

    /** */
    public XMLReader getXMLReader() throws SAXException {
        throw new UnsupportedOperationException("not implemented");
    }

    /** */
    public void setProperty(String name, Object value) {
System.err.println("not implemented");
    }

    /** */
    public Object getProperty(String name) {
        throw new UnsupportedOperationException("not implemented");
    }

    /** */
    public boolean isNamespaceAware() {
        return false;
    }

    /** */
    public boolean isValidating() {
        return false;
    }
}

/* */
