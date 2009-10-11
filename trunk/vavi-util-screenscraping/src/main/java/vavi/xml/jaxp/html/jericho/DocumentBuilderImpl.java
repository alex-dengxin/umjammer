/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.jaxp.html.jericho;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * DocumentBuilderImpl. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060926 nsano initial version <br>
 */
public class DocumentBuilderImpl extends DocumentBuilder {

    /** */
    private DocumentBuilderFactory dbf;

    /** */
    private EntityResolver er = null;

    /** */
    private ErrorHandler eh = null;

    /** */
    @SuppressWarnings("unused")
    private DOMImplementation domParser = new DOMImplementation() {

        public Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype) throws DOMException {
            // TODO Auto-generated method stub
            return null;
        }

        public DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) throws DOMException {
            // TODO Auto-generated method stub
            return null;
        }

        public Object getFeature(String feature, String version) {
            // TODO Auto-generated method stub
            return null;
        }

        public boolean hasFeature(String feature, String version) {
            // TODO Auto-generated method stub
            return false;
        }
        
    };

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
        throw new UnsupportedOperationException("not implmented yet");
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
//            domParser.setInputEncoding(encoding);
//            domParser.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
        }
//System.err.println("encoding: " + domParser.getInputEncoding());

        InputStream inputStream = is.getByteStream();
        if (inputStream == null) {
            throw new IllegalStateException("InputSource is not made of inputStream");
        }
        Document document = null;//domParser.parseDOM(inputStream, null);
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
