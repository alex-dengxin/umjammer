/*
 * Copyright (c) 2001, David N. Main, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or
 * promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.anotherbigidea.util.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;


/**
 * Base class for SAX2 Parsers
 */
public abstract class SaxParserBase implements XMLReader {
    protected EntityResolver resolver;
    protected DTDHandler dtdhandler;
    protected ContentHandler contenthandler;
    protected ErrorHandler errorhandler;
    protected List<String> elementStack = new ArrayList<String>();
    protected String namespace;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    protected SaxParserBase(String namespace) {
        this.namespace = namespace;
    }

    protected void startDoc() throws IOException {
        if (contenthandler == null) {
            return;
        }

        try {
            contenthandler.startDocument();
        } catch (SAXException saxex) {
            throw new IOException(saxex.toString());
        }
    }

    protected void endDoc() throws IOException {
        if (contenthandler == null) {
            return;
        }

        try {
            contenthandler.endDocument();
        } catch (SAXException saxex) {
            throw new IOException(saxex.toString());
        }
    }

    protected void text(String text) throws IOException {
        if (contenthandler == null) {
            return;
        }

        try {
            contenthandler.characters(text.toCharArray(), 0, text.length());
        } catch (SAXException saxex) {
            throw new IOException(saxex.toString());
        }
    }

    protected void element(String name, String[] attributes) throws IOException {
        if (contenthandler == null) {
            return;
        }

        AttributesImpl attrs = new AttributesImpl();

        if (attributes != null) {
            int topIndex = attributes.length - 1;
            for (int i = 0; i < topIndex; i += 2) {
                String attName = attributes[i];
                String value = attributes[i + 1];

                if ((attName != null) && (value != null)) {
                    attrs.addAttribute("", attName, attName, "CDATA", value);
                }
            }
        }

        try {
            contenthandler.startElement(namespace, name, name, attrs);
            contenthandler.endElement(namespace, name, name);
        } catch (SAXException saxex) {
            throw new IOException(saxex.toString());
        }
    }

    protected void start(String name, String[] attributes) throws IOException {
        elementStack.add(name);
        if (contenthandler == null) {
            return;
        }

        AttributesImpl attrs = new AttributesImpl();

        if (attributes != null) {
            int topIndex = attributes.length - 1;
            for (int i = 0; i < topIndex; i += 2) {
                String attName = attributes[i];
                String value = attributes[i + 1];

                if ((attName != null) && (value != null)) {
                    attrs.addAttribute("", attName, attName, "CDATA", value);
                }
            }
        }

        try {
            contenthandler.startElement(namespace, name, name, attrs);
        } catch (SAXException saxex) {
            throw new IOException(saxex.toString());
        }
    }

    protected void end() throws IOException {
        if (elementStack.isEmpty()) {
            return;
        }
        if (contenthandler == null) {
            return;
        }

        String name = elementStack.remove(elementStack.size() - 1);

        try {
            contenthandler.endElement(namespace, name, name);
        } catch (SAXException saxex) {
            throw new IOException(saxex.toString());
        }
    }

    //============ XMLReader interface follows: ================
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals("http://xml.org/sax/features/namespaces")) {
            return true;
        }

        if (name.equals("http://xml.org/sax/features/namespace-prefixes")) {
            return false;
        }

        throw new SAXNotRecognizedException(name);
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals("http://xml.org/sax/features/namespaces") || name.equals("http://xml.org/sax/features/namespace-prefixes")) {
            return;
        }

        throw new SAXNotRecognizedException(name);
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException(name);
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException(name);
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }

    public EntityResolver getEntityResolver() {
        return resolver;
    }

    public void setDTDHandler(DTDHandler handler) {
        this.dtdhandler = handler;
    }

    public DTDHandler getDTDHandler() {
        return dtdhandler;
    }

    public void setContentHandler(ContentHandler handler) {
        this.contenthandler = handler;
    }

    public ContentHandler getContentHandler() {
        return contenthandler;
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.errorhandler = handler;
    }

    public ErrorHandler getErrorHandler() {
        return errorhandler;
    }

    public abstract void parse(InputSource input) throws IOException, SAXException;

    public abstract void parse(String systemId) throws IOException, SAXException;
}
