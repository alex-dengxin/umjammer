/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.jaxp.html.tidy;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * DocumentBuilderImpl.
 *
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 031103 nsano initial version <br>
 *          0.01 040312 nsano i18n <br>
 */
public class DocumentBuilderImpl extends DocumentBuilder {

    /** */
    private DocumentBuilderFactory dbf;

    /** */
    private EntityResolver er = null;

    /** */
    private ErrorHandler eh = null;

    /** */
    private Tidy domParser = null;

    /** */
    private boolean namespaceAware = false;

    /** */
    private boolean validating = false;

    /** */
    DocumentBuilderImpl(DocumentBuilderFactory dbf) throws ParserConfigurationException {
        init(dbf);
    }

    /** */
    private void init(DocumentBuilderFactory dbf) throws ParserConfigurationException {

        this.dbf = dbf;

        domParser = new Tidy();

        domParser.setQuiet(true);
        domParser.setShowWarnings(false);
        domParser.setOnlyErrors(false);
    }

    /** */
    public Document newDocument() {
        return (Document) new org.w3c.tidy.Node(org.w3c.tidy.Node.ROOT_NODE, new byte[0], 0, 0) {
            public Node getAdapter() {
                return super.getAdapter();
            }
        }.getAdapter();
    }

    /** */
    public DOMImplementation getDOMImplementation() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** */
    public Document parse(InputSource is) throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputSource cannot be null");
        }

        if (er != null) {
            System.err.println("ignore entity resolver");
        }

        if (eh != null) {
            System.err.println("ignore error handler");
        }

        String encoding = (String) dbf.getAttribute("encoding");
// System.err.println("encoding: " + encoding);
        if (encoding != null) {
//            domParser.setInputEncoding(encoding); TODO
             domParser.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
        }
//System.err.println("encoding: " + domParser.getInputEncoding());

        InputStream inputStream = is.getByteStream();
        if (inputStream == null) {
            throw new IllegalStateException("InputSource is not made of inputStream");
        }
        Document document = domParser.parseDOM(inputStream, null);
        return document;
    }

    /** */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /** */
    public boolean isValidating() {
        return validating;
    }

    /** */
    public void setEntityResolver(EntityResolver er) {
        this.er = er;
    }

    /** */
    public void setErrorHandler(ErrorHandler eh) {
        // If app passes in a ErrorHandler of null,
        // then ignore all errors and warnings
        this.eh = (eh == null) ? new DefaultHandler() : eh;
    }
}

/* */
