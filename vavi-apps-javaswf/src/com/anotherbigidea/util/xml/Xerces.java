package com.anotherbigidea.util.xml;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.xerces.parsers.SAXParser;


/**
 * Utilities for using the Apache Xerces XML Parser
 */
public class Xerces {
    public static void parse(DefaultHandler handler, InputStream in) throws SAXException, IOException {
        SAXParser parser = new SAXParser();

        InputSource source = new InputSource(in);

        parser.setContentHandler(handler);
        parser.setErrorHandler(handler);
        parser.parse(source);

        in.close();
    }
}
