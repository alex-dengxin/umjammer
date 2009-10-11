/*
 * Copyright (c) 2002 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;


/**
 * XML pretty printer.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 031103 vavi initial version <br>
 */
public class PrettyPrinter {

    /** */
    private StreamResult result;

    /** */
    public PrettyPrinter(Writer writer) {
        result = new StreamResult(writer);
    }

    /** */
    public PrettyPrinter(OutputStream os) {
        try {
            result = new StreamResult(new OutputStreamWriter(os, System.getProperty("file.encoding")));
        } catch (UnsupportedEncodingException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /**
     *
     */
    public void print(Node node) throws IOException {

        Source source = new DOMSource(node);

//      String publicId = document.getDoctype().getPublicId();
//System.err.println("---- public id ----");
//System.err.println(publicId);
        
//      String systemId = document.getDoctype().getSystemId();
//System.err.println("---- system id ----");
//System.err.println(systemId);

        // set encode
        Properties props = new Properties();
        props.setProperty(OutputKeys.INDENT, "yes");
//      props.setProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);
//      props.setProperty(OutputKeys.DOCTYPE_PUBLIC, publicId);
//      props.setProperty(OutputKeys.ENCODING, "Shift_JIS");
        transformer.setOutputProperties(props);
        
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    /** */
    private static Transformer transformer;

    /** */ {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
e.printStackTrace();
            System.exit(1);
        }
    }
}

/* */
