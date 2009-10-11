/*
 * @(#) $Id: ASN1Seq.java,v 1.1.1.1 2003/10/05 18:39:12 pankaj_kumar Exp $
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

import vavi.util.Debug;


/**
 * ASN1Seq.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050317 nsano initial version <br>
 */
public class ASN1Seq extends ASN1Type {
    /** */
    protected List<ASN1Type> elems = new ArrayList<ASN1Type>();

    /** */
    private boolean ignoreMembers = false;

    /** */
    public ASN1Seq() {
        super(UNIVERSAL, NONE, SEQUENCE, SEQUENCE);
        setConsMask(CONSTRUCTED);
    }

    /** */
    public ASN1Seq(byte tagClass, int taggingMethod, int tagNumber) {
        super(tagClass, taggingMethod, tagNumber, SEQUENCE);
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

        Debug.println("[ASN1Seq.decode()] event = " + event + ", off = " + parser.getOffset() + ", len = " + parser.getLength());
        if (event != ASN1PullParser.START_SEQ) {
            throw new ASN1PullParserException("unexpected type");
        }

        consMask = parser.getConsMask();
        int expSize = elems.size();
        int idx = 0;
        while ((event = parser.next()) != ASN1PullParser.END_SEQ) {
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
        Debug.println("configured for: " + expSize + ", found: " + elems.size());
        logger.exiting(getClass().getName(), "decode");
    }

    /** */
    public byte[] encode() {
        logger.entering(getClass().getName(), "encode");
        if (ignoreMembers) {
            logger.fine("Ignoring members. Perhaps the encoded value has been set ...");
            byte[] bytes = value;
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
            if (encoded != null) {
                len += encoded.length;
                elemEncodings.add(encoded);
            }
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
            if (encoded != null) {
                for (int j = 0; j < encoded.length; j++) {
                    bytes[idx++] = encoded[j];
                }
            }
        }
        logger.fine("[ASN1Seq.encode()] idOctet = " + Integer.toHexString(idOctet) + ", #lenOctets = " + lenEncoding.length + ", len = " + len);
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
    public ASN1Type get(int idx) {
        return elems.get(idx);
    }

    /** */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SEQ(");
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
