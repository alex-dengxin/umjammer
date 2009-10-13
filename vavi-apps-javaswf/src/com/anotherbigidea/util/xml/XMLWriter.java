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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Write XML text to an output stream
 */
public class XMLWriter extends SaxHandlerBase {
    protected Writer out;
    protected boolean started = false;
    protected int indentDepth;
    protected static String encoding = "UTF-8";

    public XMLWriter(OutputStream outstream) throws IOException {
        out = new OutputStreamWriter(outstream, encoding);
    }

    public XMLWriter(PrintWriter writer) {
        out = writer;
    }

    public void startDocument() throws SAXException {
        try {
            out.write("<?xml version='1.0' encoding='" + encoding + "' ?>\n");
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    public void endDocument() throws SAXException {
        try {
            out.flush();
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    protected void completeElement() throws IOException {
        if (!started) {
            return;
        }

        out.write(" >\n");
        started = false;
    }

    public static String normalize(char[] chars, int start, int length) {
        StringBuffer buff = new StringBuffer();

        for (int i = start; i < (start + length); i++) {
            char c = chars[i];

            switch (c) {
            case '\'':
                buff.append("&apos;");
                break;
            case '"':
                buff.append("&quot;");
                break;
            case '&':
                buff.append("&amp;");
                break;
            case '<':
                buff.append("&lt;");
                break;
            case '>':
                buff.append("&gt;");
                break;
            default:
                buff.append(c);
                break;
            }
        }

        return buff.toString();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        try {
            completeElement();
            started = true;

            for (int i = 0; i < indentDepth; i++) {
                out.write(" ");
            }
            out.write("<" + qName);

            if (atts != null) {
                int count = atts.getLength();

                for (int i = 0; i < count; i++) {
                    String name = atts.getQName(i);
                    String value = atts.getValue(i);

                    out.write(" " + name + "='" + normalize(value.toCharArray(), 0, value.length()) + "'");
                }
            }

            indentDepth++;
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        try {
            indentDepth--;
            if (started) {
                out.write(" />\n");
            } else {
                for (int i = 0; i < indentDepth; i++) {
                    out.write(" ");
                }
                out.write("</" + qName + ">\n");
            }
            started = false;
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            completeElement();
            out.write(normalize(ch, start, length));
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        try {
            completeElement();
            out.write("<?" + target + " " + data + "?>\n");
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }
}
