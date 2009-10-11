/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.util.jxpath.html;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.jxpath.xml.XMLParser;

import vavi.xml.jaxp.html.cyberneko.DocumentBuilderFactoryImpl;

import org.xml.sax.SAXException;


/**
 * This implements XML parser interface for cyberneko HTML parser.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 031103 nsano initial version <br>
 *          0.01 040312 nsano i18n <br>
 */
public class HTMLParser implements XMLParser {

    /** */
    private String encoding;

    /** */
    public HTMLParser() {
    }

    /** */
    public HTMLParser(String encoding) {
        this.encoding = encoding;
    }

    /** */
    public Object parseXML(InputStream is) {
//Debug.println(is);
        try {
            DocumentBuilderFactory dbf = null;
            dbf = new DocumentBuilderFactoryImpl();
            if (encoding != null) {
//Debug.println("encoding: " + encoding);
                dbf.setAttribute("encoding", encoding);
            }
            DocumentBuilder builder = dbf.newDocumentBuilder();
//Debug.println(builder);

            Object result = builder.parse(is);
//Debug.println(result);
            return result;
        } catch (ParserConfigurationException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        } catch (SAXException e) {
          throw (RuntimeException) new IllegalArgumentException().initCause(e);
        } catch (IOException e) {
          throw (RuntimeException) new IllegalArgumentException().initCause(e);
        }
    }
}

/* */
