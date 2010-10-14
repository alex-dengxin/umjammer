/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.jaxp.html.cyberneko;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;


/**
 * This implements JAXP parser interface for cyberneko HTML parser.
 *
 * @author	<a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version	0.00	031103	nsano	initial version <br>
 */
@SuppressWarnings(value="deprecation")
public class SAXParserImpl extends SAXParser {

    /** */
    private org.xml.sax.Parser parser;

    /** */
    SAXParserImpl() throws SAXException, ParserConfigurationException {
        parser = new org.cyberneko.html.parsers.SAXParser();
    }
    
    /** */
    public org.xml.sax.Parser getParser() throws SAXException {
        return parser;
    }

    /** */
    public XMLReader getXMLReader() throws SAXException {
        return new XMLReader() {
            Map<String, Object> properties = new HashMap<String, Object>();
            @Override
            public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
                properties.put(name, value);
            }
            Map<String, Boolean> features = new HashMap<String, Boolean>();
            @Override
            public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
                features.put(name, value);
            }
            ErrorHandler errorHandler;
            @Override
            public void setErrorHandler(ErrorHandler handler) {
                errorHandler = handler;
            }
            EntityResolver entityResolver;
            @Override
            public void setEntityResolver(EntityResolver resolver) {
                entityResolver = resolver;
            }
            DTDHandler dtdHandler;
            @Override
            public void setDTDHandler(DTDHandler handler) {
                dtdHandler = handler;
            }
            ContentHandler contentHandler;
            @Override
            public void setContentHandler(ContentHandler handler) {
                contentHandler = handler;
            }
            @Override
            public void parse(String systemId) throws IOException, SAXException {
                if (dtdHandler != null)
                    parser.setDTDHandler(dtdHandler);
                if (entityResolver != null)
                    parser.setEntityResolver(entityResolver);
                if (errorHandler != null)
                    parser.setErrorHandler(errorHandler);
                parser.parse(systemId);
            }
            @Override
            public void parse(InputSource input) throws IOException, SAXException {
                if (dtdHandler != null)
                    parser.setDTDHandler(dtdHandler);
                if (entityResolver != null)
                    parser.setEntityResolver(entityResolver);
                if (errorHandler != null)
                    parser.setErrorHandler(errorHandler);
                parser.parse(input);
            }
            @Override
            public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
                return properties.get(name);
            }
            @Override
            public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
                return features.get(name);
            }
            @Override
            public ErrorHandler getErrorHandler() {
                return errorHandler;
            }
            @Override
            public EntityResolver getEntityResolver() {
                return entityResolver;
            }
            @Override
            public DTDHandler getDTDHandler() {
                return dtdHandler;
            }
            @Override
            public ContentHandler getContentHandler() {
                return contentHandler;
            }
        };
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
