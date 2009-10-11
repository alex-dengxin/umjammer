/*
 * @(#) $Id: DefASN1PullParser.java,v 1.1.1.1 2003/10/05 18:39:12 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Stack;

import org.jstk.JSTKOptions;
import org.jstk.pem.PEMData;


/**
 * DefASN1PullParser.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050317 nsano initial version <br>
 */
public class DefASN1PullParser implements ASN1PullParser {
    /** */
    private InputStream inputStream;

    /** */
    private int curTagNumber = 0;

    /** */
    private byte curTagClass = 0x00;

    /** */
    private byte consMask = ASN1Type.PRIMITIVE;

    /** */
    private int curLength = 0;

    /** */
    private int curOffset = 0;

    /** */
    private int startOffset = 0;

    /** */
    private int curDepth = 0;

    /** */
    private int curHLength = 0;

    /** */
    private byte[] curContent = null;

    /** */
    private boolean prevFlag = false;

    /** */
    private int curEvent;

    /** */
    private static class STypeObj {
        int remainingLen;

        int type;

        STypeObj(int len, int type) {
            this.remainingLen = len;
            this.type = type;
        }
    };

    /** */
    private Stack<STypeObj> activeSTypes = new Stack<STypeObj>();

    /** */
    public int next() throws ASN1PullParserException, IOException {
        if (prevFlag) {
            prevFlag = false;
            return curEvent;
        }

        curContent = null;
        // Check if a SET or SEQ has ended
        STypeObj activeSTypeObj = null;
        if (!activeSTypes.empty()) {
            activeSTypeObj = activeSTypes.peek();
            if (activeSTypeObj.remainingLen == 0) {
                activeSTypes.pop();
                --curDepth;
                // activeSTypeObj = null;
                // System.out.println("SEQ or SET ended. activeSTypeObj.type = " + activeSTypeObj.type);
                curEvent = (activeSTypeObj.type == SEQ ? END_SEQ : END_SET);
                return curEvent;
            }
        }

        if (inputStream.available() == 0) { // End of file reached.
        // System.out.println("End of File");
            curEvent = EOF;
            return (curEvent);
        }

        startOffset = curOffset;
        // System.out.println("startOffset = " + startOffset);
        // Get the tag number
        byte curOctet = read(inputStream); // First octet.
        curTagClass = (byte) (curOctet & CLASSBITS);
        curTagNumber = (curOctet & TAGBITS);
        consMask = (byte) (curOctet & ASN1Type.CONSTRUCTED);
        // System.out.println("curTagClass = " + curTagClass + ", curTagNumber =
        // " + curTagNumber);

        // Check for high tag number format.
        if (curTagNumber == TAGBITS) { // bits 5-1 are 1s ==> high tag number format
            System.out.println("High tag number format");
            curTagNumber = 0;
            int noOctets = 0;
            do {
                curOctet = (byte) inputStream.read();
                curTagNumber = 128 * curTagNumber + (curOctet & 0x7f);
                ++noOctets;
                if (noOctets > 4) {
                    throw new ASN1PullParserException("tag number more than 4 octets");
                }
            } while ((curOctet & 0x80) == 0x80); // There is a possibility of a runaway loop.
        }

        // Get the length
        curOctet = read(inputStream);
        if ((curOctet & 0x80) == 0) { // short form
            curLength = curOctet & 0x7f;
        } else { // long form
            int noOctets = curOctet & 0x7f;
            if (noOctets > 4) {
                throw new ASN1PullParserException("length more than 4 octets");
            }
            curLength = 0;
            for (int i = 0; i < noOctets; i++) {
                curOctet = read(inputStream);
                curLength = 256 * curLength + (curOctet & 0xff);
            }
        }

        curHLength = curOffset - startOffset;
        // System.out.println("hl = " + hl + ", curLength = " + curLength);
        if (activeSTypeObj != null) {
            activeSTypeObj.remainingLen -= (curHLength + curLength);
        }
        curEvent = curTagNumber;
        if (curTagNumber == SET) {
            activeSTypes.push(new STypeObj(curLength, SET));
            ++curDepth;
            curEvent = START_SET;
        } else if (curTagNumber == SEQ) {
            activeSTypes.push(new STypeObj(curLength, SEQ));
            ++curDepth;
            curEvent = START_SEQ;
        } else if (consMask == ASN1Type.CONSTRUCTED) {
            if (activeSTypeObj != null) {
                activeSTypeObj.remainingLen += curLength;
            }
        }

        if (consMask == ASN1Type.PRIMITIVE) {
            curContent = read(inputStream, curLength);
        }
        return curEvent;
    }

    /** */
    public void prev() throws ASN1PullParserException {
        prevFlag = true;
    }

    /** */
    public int getOffset() {
        return startOffset;
    }

    /** */
    public int getDepth() {
        return curDepth;
    }

    /** */
    public int getHLength() {
        return curHLength;
    }

    /** */
    public int getLength() {
        return curLength;
    }

    /** */
    public int getType() {
        return curTagNumber;
    }

    /** */
    public int getTagNumber() {
        return curTagNumber;
    }

    /** */
    public byte getTagClass() {
        return curTagClass;
    }

    /** */
    public byte getConsMask() {
        return consMask;
    }

    /** */
    public String getTypeString() {
        if ((consMask == ASN1Type.CONSTRUCTED) && (curTagNumber != SEQ) && (curTagNumber != SET)) {
            return "cons: [" + curTagNumber + "]";
        }
        switch (curTagNumber) {
        case BOOLEAN:
            return "prim: BOOLEAN";
        case INTEGER:
            return "prim: INTEGER";
        case BIT_STRING:
            return "prim: BIT STRING";
        case OCTET_STRING:
            return "prim: OCTET STRING";
        case NULL:
            return "prim: NULL";
        case OID:
            return "prim: OBJECT";
        case SEQ:
            return "cons: SEQUENCE";
        case SET:
            return "cons: SET";
        case PrintableString:
            return "prim: PRINTABLESTRING";
        case T61String:
            return "prim: T61STRING";
        case IA5String:
            return "prim: IA5STRING";
        case UTCTime:
            return "prim: UTCTIME";
        default:
            return "cons: " + curTagNumber;
        }
    }

    /** */
    public byte[] getContent() {
        return curContent;
    }

    /** */
    public int getInteger() {
        return 0;
    }

    /** */
    public void setInput(InputStream is) {
        this.inputStream = is;
        curOffset = 0;
    }

    /** */
    public byte read(InputStream is) throws IOException {
        int curByte = is.read();
        if (curByte == -1) {
            throw new IOException("unexpected end of file");
        }
        ++curOffset;
        return (byte) curByte;
    }

    /** */
    public byte[] read(InputStream is, int nbytes) throws IOException {
        byte[] bytes = new byte[nbytes];
        int n = is.read(bytes);
        if (n < nbytes) {
            throw new IOException("unexpected end of file");
        }
        curOffset += nbytes;
        return bytes;
    }

    /** */
    private static String formatInt(int num, int size) {
        String numS = Integer.toString(num);
        return formatString(numS, size, false);
    }

    /** */
    private static String formatString(String s, int size, boolean leftJustify) {
        StringBuffer sb = new StringBuffer();
        if (s.length() >= size) {
            return s;
        }
        if (leftJustify) {
            sb.append(s);
        }
        for (int i = size - s.length(); i > 0; i--) {
            sb.append(" ");
        }
        if (!leftJustify) {
            sb.append(s);
        }
        return sb.toString();
    }

    /** */
    public static void printParsed(InputStream is) throws IOException, ASN1PullParserException {
        DefASN1PullParser parser = new DefASN1PullParser();
        parser.setInput(is);
        parser.printParsed(System.out);
    }

    /** */
    public void printParsed(PrintStream ps) throws IOException, ASN1PullParserException {
        int event;
        while ((event = next()) != EOF) {
            if (event == END_SEQ || event == END_SET) {
                continue;
            }

            StringBuffer sb = new StringBuffer();
            sb.append(formatInt(getOffset(), 5));
            sb.append(":d=");
            sb.append(formatInt(getDepth(), 1));
            sb.append("  hl=");
            sb.append(formatInt(getHLength(), 1));
            sb.append(" l=");
            sb.append(formatInt(getLength(), 4));
            sb.append(" ");
            sb.append(formatString(getTypeString(), 24, true));

            if (event == OID) {
                sb.append(":");
                ASN1Oid oid = new ASN1Oid();
                oid.setValue(getContent());
                sb.append(formatString(OidMap.getName(oid.toString()), 28, true));
            } else if (event == PrintableString) {
                sb.append(":");
                ASN1PrintableString aps = new ASN1PrintableString();
                aps.setValue(getContent());
                sb.append(formatString(aps.toString(), 28, true));
            } else if (event == INTEGER) {
                sb.append(":");
                ASN1Integer ai = new ASN1Integer();
                ai.setValue(getContent());
                sb.append(formatString(ai.toString(), 28, true));
            } else if (event == BOOLEAN) {
                sb.append(":");
                ASN1Boolean ab = new ASN1Boolean();
                ab.setValue(getContent());
                sb.append(formatString(ab.toString(), 28, true));
            }
            ps.println(sb.toString());
        }
    }

    /** */
    public static ASN1PullParser getInstance(byte[] bytes) {
        ASN1PullParser parser = new DefASN1PullParser();
        parser.setInput(new ByteArrayInputStream(bytes));
        return parser;
    }

    /** */
    public static ASN1PullParser getInstance(InputStream is) {
        ASN1PullParser parser = new DefASN1PullParser();
        parser.setInput(is);
        return parser;
    }

    /** */
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || "help".equalsIgnoreCase(args[0]) || "-help".equalsIgnoreCase(args[0])) {
            System.out.println("Usage::asn1parse <infile> [-encode <outfile>]");
            return;
        }
        String file = args[0];
        InputStream is = PEMData.getDERInputStream(file);
        printParsed(is);

        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 1);
        String outfile = opts.get("encode");
        if (outfile != null) {
            System.out.println("Writing the data in DER format to file: " + outfile);
            FileOutputStream fos = new FileOutputStream(outfile);
            is = PEMData.getDERInputStream(file);
            DefASN1PullParser parser = new DefASN1PullParser();
            parser.setInput(is);

            ASN1Any any = new ASN1Any();
            any.decode(parser);
            byte[] encoded = any.encode();
            fos.write(encoded);
            fos.close();
        }
    }
}

/* */
