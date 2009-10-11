/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.util.jxpath.html;

import java.io.InputStream;

import org.apache.commons.jxpath.Container;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.xml.XMLParser;


/**
 * HtmlDocumentContainer.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version 0.00 040311 nsano initial version <br>
 */
public class HtmlDocumentContainer implements Container {

    /** */
    private String encoding;
    /** */
    private Object document;
    /** */
    private InputStream stream;

    /**
     * Use this constructor if the desired model is HTML.
     *
     * @param stream is a InputStream for an XML file.
     * Use getClass().getResource(resourceName) to load XML from a
     * resource file.
     */
    public HtmlDocumentContainer(InputStream stream) {
        this(stream, null);
    }

    /**
     * @param stream is a stream for an XML file. Use getClass().getResource
     * (resourceName) to load XML from a resource file.
     *
     * @param encoding 
     */
    public HtmlDocumentContainer(InputStream stream, String encoding) {
        this.stream = stream;
        if (stream == null) {
            throw new JXPathException("XML InputStream is null");
        }
        this.encoding = encoding;
    }

    /**
     * Reads XML, caches it internally and returns the Document.
     */
    public Object getValue() {
        if (document == null) {
            try {
                document = parseXML(stream);
            } catch (Exception e) {
                throw new JXPathException("Cannot read XML from stream", e);
            }
        }
        return document;
    }

    /**
     * Parses XML using the parser for the specified model.
     */
    public Object parseXML(InputStream stream) {
        XMLParser parser = getParser(encoding);
        return parser.parseXML(stream);
    }

    /**
     * Throws an UnsupportedOperationException
     */
    public void setValue(Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Maps a model type to a parser.
     */
    private static final XMLParser getParser(String encoding) {

        XMLParser parser = null;

        if (encoding != null) {
//Debug.println("encoding: " + encoding);
            parser = new vavi.xml.util.jxpath.html.HTMLParser(encoding);
        } else {
            parser = new vavi.xml.util.jxpath.html.HTMLParser();
        }

        return parser;
    }
}

/* */
