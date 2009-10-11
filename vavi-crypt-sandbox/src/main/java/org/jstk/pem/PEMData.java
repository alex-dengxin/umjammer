/*
 * @(#) $Id: PEMData.java,v 1.1.1.1 2003/10/05 18:39:20 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class PEMData {
    private String preEB = null;

    private String postEB = null;

    private String text = null;

    private byte[] raw = null;

    private static final int BEGIN = 0;

    private static final int GOT_PREEB = 1;

    private static final int GOT_POSTEB = 2;

    private static final String MARKER = "-----";

    public PEMData(String text) {
        this.text = text;
    }

    public PEMData(BufferedReader reader) throws IOException,
                                         InvalidPEMFormatException {
        String curLine;
        int state = BEGIN;
        StringBuffer sb = new StringBuffer();
        while ((curLine = reader.readLine()) != null) {
            if (state == BEGIN) {
                if (curLine.startsWith(MARKER) && curLine.endsWith(MARKER)) {
                    preEB = curLine;
                    state = GOT_PREEB;
                } else {
                    throw new InvalidPEMFormatException("no PreEB");
                }
            } else if (state == GOT_PREEB) {
                if (curLine.startsWith(MARKER) && curLine.endsWith(MARKER)) {
                    postEB = curLine;
                    state = GOT_POSTEB;
                } else {
                    sb.append(curLine);
                }
            } else if (state == GOT_POSTEB) {
                throw new InvalidPEMFormatException("data after PostEB");
            }
        }
        if (state != GOT_POSTEB) {
            throw new InvalidPEMFormatException("no PostEB");
        }
        text = sb.toString();
    }

    public PEMData(byte[] raw) {
        this(raw, "", "");
    }

    public PEMData(byte[] raw, String preEB, String postEB) {
        this.raw = raw;
        this.preEB = preEB;
        this.postEB = postEB;
    }

    public String getPreEB() {
        return preEB;
    }

    public String getPostEB() {
        return postEB;
    }

    public String getText() {
        return text;
    }

    public byte[] decode() throws InvalidPEMFormatException {
        if (text != null)
            return decode(text);
        return null;
    }

    public byte[] decode(String base64) throws InvalidPEMFormatException {
        try {
            int pad = 0;
            for (int i = base64.length() - 1; (i > 0) && (base64.charAt(i) == '='); i--) {
                pad++;
            }

            int length = base64.length() / 4 * 3 - pad;
            raw = new byte[length];

            for (int i = 0, rawIndex = 0; i < base64.length(); i += 4, rawIndex += 3) {
                int block = (getValue(base64.charAt(i)) << 18) + (getValue(base64.charAt(i + 1)) << 12) + (getValue(base64.charAt(i + 2)) << 6) + (getValue(base64.charAt(i + 3)));

                for (int j = 2; j >= 0; j--) {
                    if (rawIndex + j < raw.length) {
                        raw[rawIndex + j] = (byte) (block & 0xff);
                    }

                    block >>= 8;
                }
            }

            return raw;
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidPEMFormatException("illegal bit length");
        }
    }

    public String encode() {
        if (raw != null)
            return encode(raw);
        return null;
    }

    public String encode(byte[] raw) {
        StringBuffer sb = new StringBuffer();
        int n24bits = raw.length / 3;
        int rem = raw.length - (n24bits * 3);
        int niters = n24bits + (rem > 0 ? 1 : 0);

        for (int i = 0; i < niters; i++) {
            int block = 0;
            for (int j = 0; j < 3; j++) {
                int val = (i * 3 + j < raw.length ? raw[i * 3 + j] : 0);
                block |= (0x000000ff & val);
                if (j < 2)
                    block <<= 8;
            }

            for (int j = 0; j < 4; j++) {
                int bb = (block >> ((3 - j) * 6)) & 0x0000003f;
                char ch = getChar(bb);
                if (i == n24bits) {
                    if ((rem == 1 && j > 1) || (rem == 2 && j > 2))
                        ch = '=';
                }
                sb.append(ch);
            }
            if (((i + 1) & 0x0000000f) == 0)
                sb.append("\n");
        }
        text = sb.toString();
        return text;
    }

    private int getValue(char c) {
        if ((c >= 'A') && (c <= 'Z'))
            return c - 'A';
        else if ((c >= 'a') && (c <= 'z'))
            return c - 'a' + 26;
        else if ((c >= '0') && (c <= '9'))
            return c - '0' + 52;
        else if (c == '+')
            return 62;
        else if (c == '/')
            return 63;
        else if (c == '=')
            return 0;

        return -1;
    }

    private char getChar(int b6bit) {
        if (b6bit >= 0 && b6bit < 26)
            return (char) ('A' + b6bit);
        else if (b6bit < 52)
            return (char) ('a' + (b6bit - 26));
        else if (b6bit < 62)
            return (char) ('0' + (b6bit - 52));
        else if (b6bit == 62)
            return '+';
        else if (b6bit == 63)
            return '/';
        else {
            System.err.println("ERROR: Invalid Input to PEMData.getChar(): " + b6bit);
            return '\0';
        }
    }

    public String toString() {
        return ("PRE_EB: " + preEB + "\nTEXT: " + text + "\nPOST-EB: " + postEB);
    }

    public static InputStream getDERInputStream(String file) throws IOException {
        InputStream is = null;
        try { // Try PEM format
            BufferedReader reader = new BufferedReader(new FileReader(file));
            PEMData x = new PEMData(reader);
            is = new ByteArrayInputStream(x.decode());
        } catch (InvalidPEMFormatException exc) { // Assume DER format
            is = new FileInputStream(file);
        }
        return is;
    }

    private static void printusageAndExit() {
        System.out.println("Usage:: java org.jstk.pem.PEMData (encode|decode) (-infile <file>|-text <text>)");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        PEMData pemData = null;
        String text = null;
        String infile = null;

        if (args.length == 3) {
            if (args[1].equals("-infile")) {
                infile = args[2];
            } else if (args[1].equals("-text")) {
                text = args[2];
            } else {
                printusageAndExit();
            }

            if (args[0].equals("decode")) {
                if (infile != null) {
                    BufferedReader reader = new BufferedReader(new FileReader(infile));
                    pemData = new PEMData(reader);
                } else {
                    pemData = new PEMData(text);
                }
                byte[] raw = pemData.decode();
                System.out.println(new String(raw));
            } else if (args[0].equals("encode")) {
                if (infile != null) {
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(infile));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int n;
                    while ((n = bis.read(buf, 0, buf.length)) > 0) {
                        baos.write(buf, 0, n);
                    }
                    pemData = new PEMData(baos.toByteArray());
                } else {
                    pemData = new PEMData(text.getBytes());
                }
                text = pemData.encode();
                System.out.println(text);
            } else {
                printusageAndExit();
            }
        } else {
            printusageAndExit();
        }
    }
}
