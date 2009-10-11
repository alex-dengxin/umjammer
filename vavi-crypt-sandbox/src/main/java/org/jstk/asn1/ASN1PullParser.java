/*
 * @(#) $Id: ASN1PullParser.java,v 1.1.1.1 2003/10/05 18:39:12 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;


/**
 * ASN1PullParser.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050317 nsano initial version <br>
 */
public interface ASN1PullParser {
    /** */
    public static final int ANY = 0;

    /** */
    public static final int BOOLEAN = 1;

    /** */
    public final static int INTEGER = 2;

    /** */
    public final static int BIT_STRING = 3;

    /** */
    public final static int OCTET_STRING = 4;

    /** */
    public final static int NULL = 5;

    /** */
    public final static int OID = 6;

    /** */
    public final static int START_SEQ = 7;

    /** */
    public final static int END_SEQ = 8;

    /** */
    public final static int START_SET = 9;

    /** */
    public final static int END_SET = 10;

    /** */
    public final static int SEQ = 16;

    /** */
    public final static int SET = 17;

    /** */
    public final static int PrintableString = 19;

    /** */
    public final static int T61String = 20;

    /** */
    public final static int IA5String = 22;

    /** */
    public final static int UTCTime = 23;

    /** */
    public final static int EOF = -1;

    /** */
    public final static int UNKNOWN = -2;

    /** */
    public final static byte CLASSBITS = (byte) 0xc0;

    /** */
    public final static byte TAGBITS = 0x1f;

    /** */
    public int next() throws ASN1PullParserException, IOException;

    /** */
    public void prev() throws ASN1PullParserException;

    /** */
    public int getLength();

    /** */
    public int getOffset();

    /** */
    public byte[] getContent();

    /** */
    public int getInteger();

    /** */
    public int getTagNumber();

    /** */
    public byte getTagClass();

    /** */
    public byte getConsMask();

    /** */
    public void setInput(InputStream is);

    /** */
    public void printParsed(PrintStream ps) throws IOException, ASN1PullParserException;
}

/* */
