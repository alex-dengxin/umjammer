/*
 * Copyright (c) 2001 Brian Boyter
 * All rights reserved
 *
 * This software is released subject to the GNU Public License.  See
 * the full license included with this distribution.
 */

package com.boyter.mscrypto;

import java.util.ArrayList;
import java.util.List;

import vavi.util.Debug;


/**
 * This class ASN-parses the CDP (CRL Distribution Point) extension
 * of an X.509 certificate.
 * <ul>
 * <li>The CDP starts out as a byte array.
 * <li>It then has two nested sequences.
 * <li>Each sequence has two contexts.
 * <li>If a sequence has a DistributionPointName.GeneralNames,
 * <li>then that URL is converted into a String.
 * <li>An array of URLs is returned to the calling program.
 * </ul>
 * <pre>
 * -----------------------------------------------------------------
 * This is an extract from RFC2459 para 4.2.1.14 (CRL Distribution Points)
 *
 * cRLDistributionPoints ::= {
 * CRLDistPointsSyntax }
 *
 * CRLDistPointsSyntax ::= SEQUENCE SIZE (1..MAX) OF DistributionPoint
 *
 * DistributionPoint ::= SEQUENCE {
 * distributionPoint [0] DistributionPointName OPTIONAL,
 * reasons [1] ReasonFlags OPTIONAL,
 * cRLIssuer [2] GeneralNames OPTIONAL }
 *
 * DistributionPointName ::= CHOICE {
 * fullName [0] GeneralNames,
 * nameRelativeToCRLIssuer [1] RelativeDistinguishedName }
 * ------------------------------------------------------------------
 * </pre>
 * @author Brian Boyter
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano modified <br>
 */
class CDPParser {

    /** */
    public static String[] parseCDP(byte[] cdpBlob) {
//Debug.println(cdpBlob.length + " bytes:\n" + StringUtil.getDump(cdpBlob));
        byte[] seq1 = null;
        byte[] seq2 = null;
        List<String> urlList = new ArrayList<String>();

        // check to make sure this is a byte array
        if (cdpBlob[0] != 0x04) {
Debug.println("parseCDP: ASN parse error: CDP is not an Octet String");
            return null; // blob must be an octet string
        }

        // check to make sure we have all of the bytes we should have
        if (getAsnTagLength(cdpBlob, 0) != cdpBlob.length - getAsnTagSize(cdpBlob, 0)) {
Debug.println("ASN parse error: CDP wrong size: " + (cdpBlob.length - 2) + ", " + getAsnTagLength(cdpBlob, 0));
            return null; // blob has the wrong length
        }
//Debug.println("asnTagLength: " + getAsnTagLength(cdpBlob, 0) + ", " + getAsnTagSize(cdpBlob, 0));

        // skip over the first tag
        int index1 = getAsnTagSize(cdpBlob, 0);
//Debug.println("index1: " + index1);

        while ((seq1 = getAsnSequence(cdpBlob, index1)) != null) {
//Debug.println("SEQ1:\n" + StringUtil.getDump(seq1));

            // move pointer to next sequence
            index1 = getAsnTagSize(cdpBlob, index1) + seq1.length + index1;
//Debug.println("index1(moved): " + index1);

            int index2 = 0;
            while ((seq2 = getAsnSequence(seq1, index2)) != null) {
//Debug.println("SEQ2:\n" + StringUtil.getDump(seq2));

                // move pointer to next sequence
                index2 = getAsnTagSize(seq1, index2) + seq2.length + index2;
//Debug.println("index2(moved): " + index2);

                // 1. there should be a context specific tag next
                int tagType = seq2[0] & 0xe0;
                if (tagType != 0x00a0) {
Debug.println("parseCDP: parse error - context specific tag 1 missing - skip");
                    continue;
                }
//Debug.println("tag1: " + StringUtil.toHex2(seq2[0]));

                // get the first context value (looking for 0 which is a distribution point)
                int contextVal = getAsnContextValue(seq2, 0);
//Debug.println("contextVal: " + contextVal + ", " + StringUtil.toHex2(seq2[0]));

                if (contextVal != 0) {
                    switch (contextVal) {
                    case 1: // reasonFlags
Debug.println("parseCDP: reasonFlag found - skip");
                        continue;
                    case 2: // CRLissuer
Debug.println("parseCDP: CRLissuer found - skip");
                        continue;
                    default: // unknown
Debug.println("parseCDP: parse error - unknown DistributionPoint type - skip: " + contextVal);
                        continue;
                    }
                }

                // This sequence contains a DistributionPointName

                // 2. there should be a context specific tag next
                tagType = seq2[2] & 0xe0;
                if (tagType != 0x00a0) {
Debug.println("parseCDP: parse error - context specific tag 2 missing - skip");
                    continue;
                }

                // get the second context value (looking for 0 which is a
                // fullName)
                contextVal = getAsnContextValue(seq2, 2);

                if (contextVal != 0)
                    switch (contextVal) {
                    case 1: // rfc822Name
Debug.println("parseCDP: nameRelativeToCRLIssuer found - skip");
                        continue;
                    default: // unknown
Debug.println("parseCDP: parse error - unknown DistributionPointName type - skip: " + contextVal);
                        continue;
                    }

                // there should be a context specific OID tag next
                tagType = seq2[4];
                if (tagType != -122) {
Debug.println("parseCDP: parse error - context specific OID tag missing - skip: " + tagType);
                    continue;
                }

                // copy the CRL's URL into a new byte array
                int urlSize = getAsnTagLength(seq2, 4);
//if (urlSize > seq2.length) {
// Debug.println("??? urlSize: " + urlSize + ", " + seq2.length);
// continue;
//}
                String URL = new String(seq2, getAsnTagSize(seq2, 4) + 4, urlSize);
Debug.println("parseCDP: found CDP URL(" + urlSize + "): " + URL);

                urlList.add(URL);
            }
        }

        String[] urls = new String[urlList.size()];
        urlList.toArray(urls);

        return urls;
    }

    /**
     * returns a new byte array containing this sequence
     */
    private static byte[] getAsnSequence(byte[] blob, int index) {
        if (index > (blob.length) - 1) {
            return null;
        }

        // get the size of this seq
        int size = getAsnTagLength(blob, index);
//Debug.println("size: " + size + ", index[" + index + "]=" + StringUtil.toHex2(blob[index]));
        // copy this seq into a new byte array
        byte[] out = new byte[size];
        System.arraycopy(blob, index + getAsnTagSize(blob, index), out, 0, size);

        return out;
    }

    /**
     * returns the context value
     */
    private static int getAsnContextValue(byte[] blob, int index) {
        return blob[index] & 0x1f;
    }

    /**
     * returns the size of the data block which follows this tag
     */
    private static int getAsnTagLength(byte[] blob, int index) {
        byte b1 = blob[index + 1];
        if ((b1 & 0x80) != 0) { // the length is > 127
            int n = b1 & 0x7f; // the number of bytes that make up the length
            int t = 0;
            for (int i = 0; i < n; i++) {
                t = (t << 8) + (blob[index + 2 + i] & 0xff);
            }
            return t;
        } else {
            // length is < 128
            return b1;
        }
    }

    /**
     * returns the size of this tag
     */
    private static int getAsnTagSize(byte[] blob, int index) {
        byte b1 = blob[index + 1];
        if ((b1 & 0x80) != 0) { // the length is > 127
            int n = b1 & 0x7f; // the number of bytes that make up the length
            return n + 2;
        } else {
            // length is < 128
            return 2;
        }
    }
}

/* */
