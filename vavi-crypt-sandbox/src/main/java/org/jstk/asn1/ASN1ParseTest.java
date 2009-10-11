/*
 * @(#) $Id: ASN1ParseTest.java,v 1.1.1.1 2003/10/05 18:39:12 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jstk.pem.InvalidPEMFormatException;
import org.jstk.pem.PEMData;


public class ASN1ParseTest extends TestCase {
    public static final Logger logger = ASN1Type.logger;

    class InputFile {
        byte[] bytes;

        String file;

        InputFile(byte[] bytes, String file) {
            this.bytes = bytes;
            this.file = file;
        }
    }

    protected String[] inputFiles = new String[] {
        "data/test.csr", "data/test1.cer", "data/test2.pem"
    };

    protected List<InputFile> inputStreamVec = new ArrayList<InputFile>();

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    protected void setUp() {
        for (int i = 0; i < inputFiles.length; i++) {
            String file = inputFiles[i];
            byte[] bytes = null;
            try { // Try PEM format
                BufferedReader reader = new BufferedReader(new FileReader(file));
                PEMData x = new PEMData(reader);
                bytes = x.decode();
            } catch (InvalidPEMFormatException exc) { // Assume DER format
                ByteArrayOutputStream baos = null;
                try {
                    FileInputStream is = new FileInputStream(file);
                    baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int n;
                    while ((n = is.read(buf)) > 0)
                        baos.write(buf, 0, n);
                } catch (IOException ioe) { // Input file has a problem
                    logger.info("I/O problem with : " + file + ", Exception: " + ioe + ". Skipping ...");
                    continue;
                }
                bytes = baos.toByteArray();
            } catch (IOException ioe) { // Input file has a problem
                logger.info("I/O problem with : " + file + ", Exception: " + ioe + ". Skipping ...");
                continue;
            }
            inputStreamVec.add(new InputFile(bytes, file));
        }
    }

    public static Test suite() {
        return new TestSuite(ASN1ParseTest.class);
    }

    public void testParse() {
        logger.entering(getClass().getName(), "testParse");
        for (int i = 0; i < inputStreamVec.size(); i++) {
            DefASN1PullParser parser = new DefASN1PullParser();
            InputFile inpf = inputStreamVec.get(i);
            parser.setInput(new ByteArrayInputStream(inpf.bytes));
            try {
                while (parser.next() != ASN1PullParser.EOF)
                    ;
            } catch (Exception e) {
                fail("parsing failed for file: " + inpf.file + ", Exception: " + e);
            }
            logger.info("parsing succeeded for file: " + inpf.file);
        }
        logger.exiting(getClass().getName(), "testParse");
    }

    public void testDecode() {
        logger.entering(getClass().getName(), "testDecode");
        for (int i = 0; i < inputStreamVec.size(); i++) {
            DefASN1PullParser parser = new DefASN1PullParser();
            InputFile inpf = inputStreamVec.get(i);
            parser.setInput(new ByteArrayInputStream(inpf.bytes));
            try {
                ASN1Any any = new ASN1Any();
                any.decode(parser);
            } catch (Exception e) {
                fail("decode failed for file: " + inpf.file + ", Exception: " + e);
            }
            logger.info("decode succeeded for file: " + inpf.file);
        }
        logger.exiting(getClass().getName(), "testDecode");
    }

    public void testRoundTrip() {
        logger.entering(getClass().getName(), "testRoundTrip");
        for (int i = 0; i < inputStreamVec.size(); i++) {
            DefASN1PullParser parser = new DefASN1PullParser();
            InputFile inpf = inputStreamVec.get(i);
            parser.setInput(new ByteArrayInputStream(inpf.bytes));
            try {
                ASN1Any any = new ASN1Any();
                any.decode(parser);
                byte[] encoded = any.encode();
                assertTrue(equalsByteArray(inpf.bytes, encoded));
            } catch (Exception e) {
                fail("roundtrip test failed for file: " + inpf.file + ", Exception: " + e);
            }
            logger.info("roundtrip test succeeded for file: " + inpf.file);
        }
        logger.exiting(getClass().getName(), "testRoundTrip");
    }

    public boolean equalsByteArray(byte[] aa, byte[] ba) {
        if (aa != null && ba != null && aa.length == ba.length) {
            for (int i = 0; i < aa.length; i++) {
                if (aa[i] != ba[i])
                    return false;
            }
            return true;
        } else if (aa == null && ba == null) {
            return true;
        }
        return true;
    }
}
