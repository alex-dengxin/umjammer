/*
 * @(#) $Id: ASN1OctetString.java,v 1.1.1.1 2003/10/05 18:39:11 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

/**
 * ASN1OctetString.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050317 nsano initial version <br>
 */
public class ASN1OctetString extends ASN1Type {
    private static char[] hexChars = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public ASN1OctetString() {
        super(UNIVERSAL, NONE, OCTET_STRING, OCTET_STRING);
    }

    public String toString() {
        if (value == null) {
            return "ASN1OctetString: null";
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < value.length; i++) {
            byte cbyte = value[i];
            sb.append(hexChars[(0x000000f0 & cbyte) >> 4]);
            sb.append(hexChars[(0x0000000f & cbyte)]);
        }
        return "ASN1OctetString: " + sb.toString();
    }

    public static void main(String[] args) {
        byte[] bytes = {
            (byte) 0xf1, (byte) 0xc1
        };
        ASN1OctetString os = new ASN1OctetString();
        os.setValue(bytes);
        System.out.println(os.toString());
    }
}
