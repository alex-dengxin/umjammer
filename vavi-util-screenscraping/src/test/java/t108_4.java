/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import vavi.xml.util.PrettyPrinter;


/**
 * javadoc 形式のファイルをリーバースエンジニアリングします。
 */
public class t108_4 {
    public static void main(String[] args) throws Exception {
        InputStream is = new URL(args[0]).openStream();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
System.err.println("builder: " + documentBuilder);
        Document document = documentBuilder.parse(is);

        PrettyPrinter pp = new PrettyPrinter(System.err);
        pp.print(document);
    }
}

/* */
