/*
 * @(#) $Id: ASN1IA5String.java,v 1.1.1.1 2003/10/05 18:39:11 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

public class ASN1IA5String extends ASN1Type {
//    private static final char[] hexChars = {
//        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
//    };

    public ASN1IA5String() {
        super(UNIVERSAL, NONE, IA5String, IA5String);
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public String getString() {
        return new String(value);
    }

    public void setString(String s) {
        this.value = s.getBytes();
    }

    public String toString() {
        if (value == null)
            return null;
        return "IA5String: " + new String(value);
    }

    public static void main(String[] args) {
        byte[] bytes = {
            0x54, 0x65, 0x73, 0x74, 0x20, 0x55, 0x73, 0x65, 0x72, 0x20, 0x31
        };
        ASN1IA5String ps = new ASN1IA5String();
        ps.setValue(bytes);
        System.out.println(ps.toString());
    }
}
