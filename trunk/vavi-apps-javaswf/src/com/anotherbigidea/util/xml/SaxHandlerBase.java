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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import vavi.util.Debug;


/**
 * Base class for SAX2 Content Handlers
 */
public abstract class SaxHandlerBase extends DefaultHandler {
    protected Map<String, Object> elementTypes = new HashMap<String, Object>();
    protected SaxHandlerBase.ElementType elemType;
    protected List<SaxHandlerBase.ElementType> elems = new ArrayList<SaxHandlerBase.ElementType>();
    protected boolean gatherMode = false;
    protected List<Object[]> gatherBuffer;
    protected SaxHandlerBase.GatheringElementType gatheringElement;

    // Start gathering elements/chars for later dispatch
    public void startGatherMode(SaxHandlerBase.GatheringElementType elem) {
        gatheringElement = elem;
        gatherBuffer = new ArrayList<Object[]>();
        gatherMode = true;
    }

    // stop gathering and dispatch the gathered elements
    public void endGatherMode() throws Exception {
        gatherMode = false;
        gatheringElement = null;

        // replay the elements
        for (Iterator<Object[]> it = gatherBuffer.iterator(); it.hasNext();) {
            Object[] elem = it.next();

            SaxHandlerBase.ElementType type = (SaxHandlerBase.ElementType) elem[0];
            if (type == null) {
                continue;
            }

            if (elem[1] == null) //element end
             {
                type.endElement();
            } else if (elem[1] instanceof String) {
                String charstring = (String) elem[1];
                char[] chars = charstring.toCharArray();
                type.characters(chars, 0, chars.length);
            } else {
                Attributes atts = (Attributes) elem[1];
                type.startElement(atts);
            }
        }

        gatherBuffer = null;
    }

    // dispatch all the gathered elements that match the name
    public void dispatchAllMatchingGatheredElements(String elemName) throws Exception {
        SaxHandlerBase.ElementType dispelem = (SaxHandlerBase.ElementType) elementTypes.get(elemName);

        if (dispelem == null) {
            return;
        }
        boolean found = false;

        for (Iterator<Object[]> it = gatherBuffer.iterator(); it.hasNext();) {
            Object[] elem = it.next();

            SaxHandlerBase.ElementType type = (SaxHandlerBase.ElementType) elem[0];
            if (type == null) {
                continue;
            }

            if (type == dispelem) {
                found = true;
            }

            if (found) {
                it.remove();

                if (elem[1] == null) //element end
                 {
                    type.endElement();
                    if (type == dispelem) {
                        found = false; //done dispatching this element
                    }
                } else if (elem[1] instanceof String) {
                    String charstring = (String) elem[1];
                    char[] chars = charstring.toCharArray();
                    type.characters(chars, 0, chars.length);
                } else {
                    Attributes atts = (Attributes) elem[1];
                    type.startElement(atts);
                }
            }
        }
    }

    // dispatch the first gathered element that matches the name
    public void dispatchGatheredElement(String elemName) throws Exception {
        SaxHandlerBase.ElementType dispelem = (SaxHandlerBase.ElementType) elementTypes.get(elemName);

        if (dispelem == null) {
            return;
        }
        boolean found = false;

        for (Iterator<Object[]> it = gatherBuffer.iterator(); it.hasNext();) {
            Object[] elem = it.next();

            SaxHandlerBase.ElementType type = (SaxHandlerBase.ElementType) elem[0];
            if (type == null) {
                continue;
            }

            if (type == dispelem) {
                found = true;
            }

            if (found) {
                it.remove();

                if (elem[1] == null) //element end
                 {
                    type.endElement();
                    if (type == dispelem) {
                        return; //done dispatching
                    }
                } else if (elem[1] instanceof String) {
                    String charstring = (String) elem[1];
                    char[] chars = charstring.toCharArray();
                    type.characters(chars, 0, chars.length);
                } else {
                    Attributes atts = (Attributes) elem[1];
                    type.startElement(atts);
                }
            }
        }
    }

    public static class ElementType {
        public void startElement(Attributes atts) throws Exception {
        }

        public void endElement() throws Exception {
        }

        public void characters(char[] ch, int start, int length) throws Exception {
        }
    }

    public static class ContentElementType extends SaxHandlerBase.ElementType {
        protected Attributes attrs;
        protected StringBuffer buff;

        public void startElement(Attributes atts) throws Exception {
            attrs = new AttributesImpl(atts);
            buff = new StringBuffer();
        }

        public void characters(char[] ch, int start, int length) throws Exception {
//System.err.println("buff: " + buff);
if (buff == null) {
 Debug.println("ContentElementType::characters: buff: " + new String(ch, start, length));
 return; // TODO vavi null
}
            buff.append(ch, start, length);
        }
    }

    public static class GatheringElementType extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
        }

        public boolean gatherElement(String localName, Attributes atts) {
            return true;
        }
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        try {
            elemType = (SaxHandlerBase.ElementType) elementTypes.get(localName);

            if (gatherMode) { //gather the element for later processing
                if (gatheringElement.gatherElement(localName, atts)) {
                    gatherBuffer.add(new Object[] {
                                         elemType, new AttributesImpl(atts)
                                     });
                }
            } else {
                if (elemType == null) {
                    return;
                }
                elemType.startElement(atts);
            }

            elems.add(elemType);
        } catch (SAXException saxex) {
            throw saxex;
        } catch (Exception ex) {
            //ex.printStackTrace();
            throw new SAXException(ex);
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
Debug.println(localName);
        try {
            elemType = (SaxHandlerBase.ElementType) elementTypes.get(localName);
            if (elemType == null) {
                return;
            }

            if (elemType == gatheringElement) {
                gatherMode = false;
            }

            if (gatherMode) { //gather the element for later processing
                gatherBuffer.add(new Object[] { elemType, null });
            } else {
                elemType.endElement();
            }

            if (!elems.isEmpty()) {
                elemType = elems.remove(elems.size() - 1);
            } else {
                elemType = null;
            }
        } catch (SAXException saxex) {
            throw saxex;
        } catch (Exception ex) {
ex.printStackTrace();
            throw new SAXException(ex);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            if (elemType == null) {
                return;
            }

            if (gatherMode) { //gather the element for later processing
                gatherBuffer.add(new Object[] {
                                     elemType, new String(ch, start, length)
                                 });
            } else {
                elemType.characters(ch, start, length);
            }
        } catch (SAXException saxex) {
            throw saxex;
        } catch (Exception ex) {
ex.printStackTrace();
            throw new SAXException(ex);
        }
    }

    public static String getAttr(Attributes attrs, String name, String defaultValue) {
        String value = attrs.getValue("", name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static int getAttrInt(Attributes attrs, String name, int defaultValue) {
        String value = attrs.getValue("", name);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static double getAttrDouble(Attributes attrs, String name, double defaultValue) {
        String value = attrs.getValue("", name);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static boolean getAttrBool(Attributes attrs, String name, boolean defaultValue) {
        String value = attrs.getValue("", name);
        if (value == null) {
            return defaultValue;
        }

        if (value.equalsIgnoreCase("yes")) {
            return true;
        }
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }
}
