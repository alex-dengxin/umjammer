/*
 * @(#) $Id: ASN1Integer.java,v 1.1.1.1 2003/10/05 18:39:11 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

import java.math.BigInteger;


public class ASN1Integer extends ASN1Type {
    public ASN1Integer() {
        super(UNIVERSAL, NONE, INTEGER, INTEGER);
    }

    public ASN1Integer(byte tagClass, int taggingMethod, int tagNumber) {
        super(tagClass, taggingMethod, tagNumber, INTEGER);
    }

    public void setValue(BigInteger value) {
        byte[] val = value.toByteArray();
        setValue(val);
    }

    public void setDefaultValue(BigInteger value) {
        this.defvalue = value.toByteArray();
    }

    public BigInteger getDefaultValue() {
        return new BigInteger(defvalue);
    }

    public String toString() {
        if (getValue() == null)
            return null;
        return (new BigInteger(getValue())).toString();
    }

    public static void main(String[] args) {
        byte[] bytes = {
            (byte) 0x00, 0x01
        };
        ASN1Integer ai = new ASN1Integer();
        ai.setValue(bytes);
        System.out.println(ai.toString());
    }
}
