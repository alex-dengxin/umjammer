/*
 * @(#) $Id: ASN1BitString.java,v 1.1.1.1 2003/10/05 18:39:11 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

public class ASN1BitString extends ASN1Type {
    public ASN1BitString() {
        super(UNIVERSAL, NONE, BIT_STRING, BIT_STRING);
    }

    public ASN1BitString(byte tagClass, int taggingMethod, int tagNumber) {
        super(tagClass, taggingMethod, tagNumber, BIT_STRING);
    }

    public String toString() {
        if (value == null)
            return "ASN1BitString: null";

        StringBuffer sb = new StringBuffer();
        int noUnusedBits = value[0];
        for (int i = 1; i < value.length; i++) {
            byte cbyte = value[i];
            for (int j = 0; j < 8; j++) {
                if ((cbyte & 0x80) == 0x80)
                    sb.append("1");
                else
                    sb.append("0");
                cbyte = (byte) (cbyte << 1);
                if ((i == value.length - 1) && (j + noUnusedBits == 8 - 1))
                    break;
            }
        }
        return "ASN1BitString: " + sb.toString();
    }

    public byte[] getValue() {
        if (this.length < 1)
            return null;
        byte[] bytes = new byte[this.length - 1];
        System.arraycopy(value, 1, bytes, 0, this.length - 1);
        return bytes;
    }

    public void setValue(byte[] value) {
        setValue(value, 0);
    }

    public void setValue(byte[] value, int noUnusedBits) {
        this.length = (value != null ? value.length + 1 : 1);
        byte[] bytes = new byte[this.length];
        bytes[0] = (byte) noUnusedBits;
        System.arraycopy(value, 0, bytes, 1, this.length - 1);
        this.value = bytes;
    }

    public static void main(String[] args) {
        byte[] bytes = {
            (byte) 0xf1, 0x01
        };
        ASN1BitString bs = new ASN1BitString();
        bs.setValue(bytes);
        System.out.println(bs.toString());
    }
}
