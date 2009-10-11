/*
 * @(#) $Id: ASN1Type.java,v 1.1.1.1 2003/10/05 18:39:12 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

import java.io.IOException;
import java.util.logging.Logger;


public abstract class ASN1Type {
    // tag classes
    public static final byte UNIVERSAL = 0;

    public static final byte APPLICATION = 0x40;

    public static final byte CONTEXT = (byte) 0x80;

    public static final byte PRIVATE = (byte) 0xc0;

    // Masks for interpreting the tag
    public static final byte CLASSBITS = (byte) 0xc0;

    public static final byte TAGBITS = 0x1f;

    // Primitive or constructed flags
    public static final byte PRIMITIVE = 0;

    public static final byte CONSTRUCTED = 0x20;

    // Tagging methods
    public static final int NONE = 0;

    public static final int IMPLICIT = 1;

    public static final int EXPLICIT = 2;

    // Universal tags
    public static final int ANY = 0;

    public static final int BOOLEAN = 1;

    public static final int INTEGER = 2;

    public static final int BIT_STRING = 3;

    public static final int OCTET_STRING = 4;

    public static final int NULL = 5;

    public static final int OID = 6;

    public static final int SEQUENCE = 16;

    public static final int SET = 17;

    public static final int PrintableString = 19;

    public static final int T61String = 20;

    public static final int IA5String = 22;

    public static final int UTCTime = 23;

    public static final Logger logger = Logger.getLogger("org.jstk.asn1");

    // variables to hold different values
    protected byte tagClass;

    protected int taggingMethod;

    protected int tagNumber;

    protected byte consMask = PRIMITIVE;

    protected int type;

    protected int length;

    // flags and values
    protected boolean optional = false;

    protected byte[] defvalue = null;

    protected byte[] value;

    public ASN1Type(byte tagClass, int taggingMethod, int tagNumber, int type) {
        this.tagClass = tagClass;
        this.taggingMethod = taggingMethod;
        this.tagNumber = tagNumber;
        this.type = type;
    }

    public void setTagClass(byte tagClass) {
        this.tagClass = tagClass;
    }

    public byte getTagClass() {
        return tagClass;
    }

    public void setTaggingMethod(int taggingMethod) {
        this.taggingMethod = taggingMethod;
    }

    public int getTaggingMethod() {
        return taggingMethod;
    }

    public void setTagNumber(int tagNumber) {
        this.tagNumber = tagNumber;
    }

    public int getTagNumber() {
        return tagNumber;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setConsMask(byte consMask) {
        this.consMask = consMask;
    }

    public int getConsMask() {
        return consMask;
    }

    public void setOptional(boolean flag) {
        this.optional = flag;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
        this.length = (value != null ? value.length : 0);
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void decode(ASN1PullParser parser) throws ASN1PullParserException, IOException {
        logger.entering(getClass().getName(), "decode");
        boolean processEvent = true;
        int event = parser.next();
        length = parser.getLength();
        logger.fine("[ASN1Type.decode()] event = " + event + ", off = " + parser.getOffset() + ", len = " + length);

        if ((event != tagNumber) || (parser.getTagClass() != tagClass)) {
            if (optional || (defvalue != null)) {
                parser.prev(); // skip
                processEvent = false;
                length = 0;
                logger.fine("skipping ..." + (optional ? "optional tag not found" : "assuming default value"));
            } else {
                throw new ASN1PullParserException("unexpected type");
            }
        }
        consMask = parser.getConsMask();
        if (processEvent)
            value = parser.getContent();
        logger.exiting(getClass().getName(), "decode");
    }

    protected byte[] encodeLen(int len) {
        int lenOctets = 0;
        if (len > 127) {
            while (len != 0) {
                ++lenOctets;
                len = (len >> 8);
            }
        }
        byte[] bytes = new byte[1 + lenOctets];
        len = length;
        if (len > 127) {
            bytes[0] = (byte) (0x80 | lenOctets);
            while (lenOctets > 0) {
                bytes[lenOctets] = (byte) (len & 0x000000ff);
                len = (len >> 8);
                --lenOctets;
            }
        } else {
            bytes[0] = (byte) len;
        }
        return bytes;
    }

    // Not very efficeint, but will do.
    protected byte[] encode1() {
        if ((optional || defvalue != null) && (length == 0))
            return null;
        byte idOctet = (byte) (tagClass | consMask | tagNumber);
        byte[] lenEncoding = encodeLen(length);
        int len = 0;
        if (value != null)
            len = value.length;
        byte[] bytes = new byte[1 + lenEncoding.length + len];
        bytes[0] = idOctet;
        for (int i = 0; i < lenEncoding.length; i++) {
            bytes[1 + i] = lenEncoding[i];
        }
        for (int i = 0; i < len; i++) {
            bytes[1 + lenEncoding.length + i] = value[i];
        }
        logger.fine("[ASN1Type.encode1()] idOctet = " + Integer.toHexString(idOctet) + ", #lenOctets = " + lenEncoding.length + ", length = " + length + ", len = " + len);
        return bytes;
    }

    public byte[] encode() {
        logger.entering(getClass().getName(), "encode");
        byte[] bytes = encode1();
        logger.exiting(getClass().getName(), "encode");
        return bytes;
    }
}
