/*
 * @(#) $Id: ASN1Set.java,v 1.1.1.1 2003/10/05 18:39:12 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/** */
public class ASN1Set extends ASN1Type {
    /** */
    protected List<ASN1Type> elems = new ArrayList<ASN1Type>();

    /** */
    private boolean ignoreMembers = false;

    /** */
    public ASN1Set() {
        this(UNIVERSAL, NONE, SET);
    }

    /** */
    public ASN1Set(byte tagClass, int taggingMethod, int tagNumber) {
        super(tagClass, taggingMethod, tagNumber, SET);
        setConsMask(CONSTRUCTED);
    }

    /** */
    public void setIgnoreMembers(boolean flag) {
        ignoreMembers = flag;
    }

    /** */
    public void decode(ASN1PullParser parser) throws ASN1PullParserException, IOException {
        logger.entering(getClass().getName(), "decode");
        int event = parser.next();
        if (event != ASN1PullParser.START_SET) {
            throw new ASN1PullParserException("unexpected type");
        }
        logger.fine("[ASN1Set.decode()] event = " + event + ", off = " + parser.getOffset() + ", len = " + parser.getLength());
        int expSize = elems.size();
        int idx = 0;
        while ((event = parser.next()) != ASN1PullParser.END_SET) {
            ASN1Type elem = null;
            if (idx < elems.size()) {
                elem = elems.get(idx);
            } else {
                elem = ASN1Any.createASN1Type(event, parser.getTagClass());
                elems.add(elem);
            }
            parser.prev();
            elem.decode(parser);
            ++idx;
        }
        logger.fine("configured for: " + expSize + ", found: " + elems.size());
        logger.exiting(getClass().getName(), "decode");
    }

    /** */
    public byte[] encode() {
        logger.entering(getClass().getName(), "encode");
        if (ignoreMembers) {
            logger.fine("Ignoring members. Perhaps the encoded value has been set ...");
            byte[] bytes = encode1();
            logger.exiting(getClass().getName(), "encode");
            return bytes;
        }

        if (elems == null) {
            return null;
        }
        List<byte[]> elemEncodings = new ArrayList<byte[]>();
        int len = 0;
        for (int i = 0; i < elems.size(); i++) {
            ASN1Type elem = elems.get(i);
            byte[] encoded = elem.encode();
            if (encoded == null) {
                return null;
            }
            len += encoded.length;
            elemEncodings.add(encoded);
        }
        byte idOctet = (byte) (tagClass | consMask | tagNumber);
        byte[] lenEncoding = encodeLen(len);
        byte[] bytes = new byte[1 + lenEncoding.length + len];
        int idx = 0;
        bytes[idx++] = idOctet;
        for (int i = 0; i < lenEncoding.length; i++) {
            bytes[idx++] = lenEncoding[i];
        }
        for (int i = 0; i < elemEncodings.size(); i++) {
            byte[] encoded = elemEncodings.get(i);
            for (int j = 0; j < encoded.length; j++) {
                bytes[idx++] = encoded[j];
            }
        }
        logger.fine("[ASN1Set.encode()] idOctet = " + Integer.toHexString(idOctet) + ", #lenOctets = " + lenEncoding.length + ", len = " + len);
        logger.exiting(getClass().getName(), "encode");
        return bytes;
    }

    /** */
    public int size() {
        return elems.size();
    }

    /** */
    public void add(ASN1Type o) {
        elems.add(o);
    }

    /** */
    public ASN1Type elementAt(int idx) {
        return elems.get(idx);
    }

    /** */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SET(");
        for (int i = 0; i < elems.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(elems.get(i).toString());
        }
        sb.append(")");
        return sb.toString();
    }
}

/* */
