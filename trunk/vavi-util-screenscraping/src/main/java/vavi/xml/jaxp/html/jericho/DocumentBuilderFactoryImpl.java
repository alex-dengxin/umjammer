/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.jaxp.html.jericho;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;


/**
 * DocumentBuilderFactoryImpl. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060926 nsano initial version <br>
 */
public class DocumentBuilderFactoryImpl extends DocumentBuilderFactory {

    /** */
    private String encoding;

    /** */
    public DocumentBuilderFactoryImpl() {
    }

    /**
     * 
     */
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        return new DocumentBuilderImpl(this);
    }

    /**
     * Allows the user to set specific attributes on the underlying implementation.
     */
    public void setAttribute(String name, Object value) throws IllegalArgumentException {
        if ("encoding".equals(name)) {
            this.encoding = (String) value;
        } else {
            throw new IllegalArgumentException("unknown attribute: " + name);
        }
    }

    /**
     * Allows the user to retrieve specific attributes on the underlying implementation.
     */
    public Object getAttribute(String name) throws IllegalArgumentException {
        if ("encoding".equals(name)) {
            return encoding;
        } else {
            throw new IllegalArgumentException("unknown attribute: " + name);
        }
    }

    /** @see javax.xml.parsers.DocumentBuilderFactory#setFeature(java.lang.String, boolean) */
    public void setFeature(String feature, boolean enabled) throws ParserConfigurationException {
        throw new IllegalArgumentException("No feature are implemented");
    }

    /** @see javax.xml.parsers.DocumentBuilderFactory#getFeature(java.lang.String) */
    public boolean getFeature(String feature) throws ParserConfigurationException {
        throw new IllegalArgumentException("No feature are implemented");
    }
}

/* */
