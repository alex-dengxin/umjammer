/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.jaxp.html.cyberneko;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.html.dom.HTMLDOMImplementationImpl;
import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMParser;


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
    private Object domParser = null;

    /** */
    private boolean namespaceAware = false;

    /** */
    private boolean validating = false;

    /** */
    DocumentBuilderImpl(DocumentBuilderFactory dbf) throws ParserConfigurationException {

        this.dbf = dbf;

        try {
//              if ("fragment".equalsIgnoreCase(dbf.getAttribute("http://cyberneko.org/dom/attribute/parser"))) {
//                  domParser = new DOMFragmentParser();
//              } else {
                  domParser = new DOMParser();
                  DOMParser parser = (DOMParser) domParser;

                  // Validation
                  parser.setFeature("http://xml.org/sax/features/validation", this.dbf.isValidating());
                  parser.setFeature("http://cyberneko.org/html/features/augmentations", true);
                  parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
//System.err.println("elements: " + parser.getProperty("http://cyberneko.org/html/properties/names/elems"));
//              }

        } catch (SAXException e) {
            // Handles both SAXNotSupportedException, SAXNotRecognizedException
            throw new ParserConfigurationException(e.getMessage());
        }
    }

    /** */
    public Document newDocument() {
        return new HTMLDocumentImpl();
    }

    /** */
    public DOMImplementation getDOMImplementation() {
        return HTMLDOMImplementationImpl.getHTMLDOMImplementation();
    }

    /** */
    public Document parse(InputSource is) throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputSource cannot be null");
        }

        String encoding = (String) dbf.getAttribute("encoding");
//System.err.println("encoding: " + encoding);

//        if (encoding == null) {
//            encoding = findEncoding(is);
//        }

        if (encoding != null) {
            is.setEncoding(encoding);
        }
//System.err.println("encoding: " + is.getEncoding());

//        if (domParser instanceof DOMFragmentParser) {
//            DOMFragmentParser parser = (DOMFragmentParser) domParser;
//            if (eh != null) {
//                parser.setErrorHandler(eh);
//            }
//            parser.parse(is);
//            return parser.ggetDocument();
//        } else {
            DOMParser parser = (DOMParser) domParser;
            if (er != null) {
                parser.setEntityResolver(er);
            }
            if (eh != null) {
                parser.setErrorHandler(eh);
            }
            parser.parse(is);
            return parser.getDocument();
//        }
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

    /**
     * TODO 全然機能してない
     * find encoding specified in html
     * @param is need mark supported
     * @return null if not found
     */
//    private String findEncoding(InputSource is) throws SAXException, IOException {
//        String encoding = null;
//System.err.println(StringUtil.paramString(is));
//
//        Reader reader = null; 
//        InputStream in = is.getByteStream();
//        if (in == null) {
//            reader = is.getCharacterStream();
//            if (!reader.markSupported()) {
//System.err.println("mark unsupported reader");
//                return null;
//            }
//            reader.mark(16384);
//        } else {
//            if (!in.markSupported()) {
//System.err.println("mark unsupported stream");
//                return null;
//            }
//            in.mark(16384);
//        }
//
//        DOMParser parser = (DOMParser) domParser;
//        parser.parse(is);
//        Document document = parser.getDocument();
//
//        try {
//            XPath xpath = XPathFactory.newInstance().newXPath();
//            String value = xpath.evaluate("//META[@http-equiv='Content-Type']/@content", document);
//            StringTokenizer st = new StringTokenizer(value, "; \t\r\n");
//            while (st.hasMoreTokens()) {
//                String token = st.nextToken().toLowerCase();
//                if (token.indexOf("charset") != -1) {
//                    st = new StringTokenizer(token, "= \t\r\n");
//                    if (!st.hasMoreTokens()) {
//                        break;
//                    }
//                    st.nextToken();
//                    if (!st.hasMoreTokens()) {
//                        break;
//                    }
//                    encoding = st.nextToken();
//                }
//            }
//System.err.println("encoding: " + encoding);
//        } catch (XPathExpressionException e) {
//System.err.println(e);
//        }
//
//        if (in != null) {
//            in.reset();
//        } else {
//            reader.reset();
//        }
//
//        return encoding;
//    }
}

/* */
