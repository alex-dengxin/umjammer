/*
 * @(#) $Id: ASN1Oid.java,v 1.1.1.1 2003/10/05 18:39:11 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

import java.io.ByteArrayOutputStream;


/**
 * From: A Layman's Guide to a Subset of ASN.1, BER, and DER Encoding of object identifier. BER encoding. Primitive. Contents
 * octets are as follows, where value1, ..., valuen denote the integer values of the components in the complete object identifier:
 * 
 * 1. The first octet has value 40 * value1 + value2. (This is unambiguous, since value1 is limited to values 0, 1, and 2; value2
 * is limited to the range 0 to 39 when value1 is 0 or 1; and, according to X.208, n is always at least 2.) 2. The following
 * octets, if any, encode value3, ..., valuen. Each value is encoded base 128, most significant digit first, with as few digits as
 * possible, and the most significant bit of each octet except the last in the value's encoding set to "1."
 * 
 * Example: The first octet of the BER encoding of RSA Data Security, Inc.'s object identifier is 40 * 1 + 2 = 42 = 2a16. The
 * encoding of 840 = 6 * 128 + 4816 is 86 48 and the encoding of 113549 = 6 * 1282 + 7716 * 128 + d16 is 86 f7 0d. This leads to
 * the following BER encoding: 06 06 2a 86 48 86 f7 0d
 */
public class ASN1Oid extends ASN1Type {
    /** */
    public ASN1Oid() {
        super(UNIVERSAL, NONE, OID, OID);
    }

    /** */
    public void setOid(String oid) {
        String[] components = oid.split("\\.");
        int value1 = Integer.parseInt(components[0]);
        int value2 = Integer.parseInt(components[1]);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(value1 * 40 + value2);
        int index = 2;
        while (index < components.length) {
            int valuei = Integer.parseInt(components[index]);
            while (valuei > 0) {
                int octetValue = valuei;
                int d = 1;
                while (octetValue >= 128) {
                    d = 128 * d;
                    octetValue = octetValue / 128;
                }
                valuei = valuei % d;
                // Write the octet to byte stream
                if (valuei != 0)
                    baos.write(octetValue | 0x80);
                else
                    baos.write(octetValue);
            }
            ++index;
        }
        this.value = baos.toByteArray();
        this.length = this.value.length;
    }

    /** */
    public String toString() {
        if (value == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        int firstOctet = value[0];
        int value1 = firstOctet / 40;
        int value2 = firstOctet % 40;
        sb.append(value1).append('.').append(value2);
        int index = 1;
        while (index < value.length) {
            int valuei = 0;
            do {
                valuei = valuei * 128 + (value[index] & 0x7f);
            } while ((value[index++] & 0x80) == 0x80);
            sb.append('.').append(valuei);
        }
        return sb.toString();
    }

    /** */
    public static void main(String[] args) {
        byte[] bytes = {
            (byte) 0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x0d
        };
        ASN1Oid oid = new ASN1Oid();
        oid.setValue(bytes);
        System.out.println(oid.toString());
        oid.setOid("1.5.8");
        System.out.println(oid.toString());
        oid.setOid("1.2.840.113549.1");
        System.out.println(oid.toString());
        oid.setOid("2.5.4.6");
        System.out.println(oid.toString());
        oid.setOid("2.5.4.3");
        System.out.println(oid.toString());
    }
}

/* */
