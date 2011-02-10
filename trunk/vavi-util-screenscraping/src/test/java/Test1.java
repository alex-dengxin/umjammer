/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.om.NodeInfo;

import org.xml.sax.InputSource;


/**
 * Test1. 
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2011/02/10 umjammer initial version <br>
 */
public class Test1 {

    /** */
    public static void main(String[] args) throws Exception {
// これをやると下で xPath Exception @ Eclipse
// System.setProperty("javax.xml.parsers.SAXParserFactory", "vavi.xml.jaxp.html.cyberneko.SAXParserFactoryImpl");    

        System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME + ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI, "net.sf.saxon.xpath.XPathFactoryImpl");    
        XPath xPath = XPathFactory.newInstance().newXPath();
        
        URLConnection connection = new URL("file:///Users/nsano/Music/iTunes/iTunes%20Music%20Library.xml").openConnection();
        InputStream is = connection.getInputStream();
        Reader reader = new BufferedReader(new InputStreamReader(is));
long c = 0;
while (reader.ready()) {
 if (reader.read() < 0) { break; }
 c++;
}
System.err.println(c + " bytes");

        connection = new URL("file:///Users/nsano/Music/iTunes/iTunes%20Music%20Library.xml").openConnection();
        is = connection.getInputStream();
        reader = new BufferedReader(new InputStreamReader(is));

        InputSource in = new InputSource(reader);

        String xpath = "/plist/dict/dict/dict";
        
        @SuppressWarnings("unchecked")
        List<NodeInfo> nodeList = (List<NodeInfo>) xPath.evaluate(xpath, in, XPathConstants.NODESET);
System.err.println("nodeList: " + nodeList.size());
    }
}

/* */
