/*
 * @(#) $Id: ASN1Boolean.java,v 1.1.1.1 2003/10/05 18:39:11 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

public class ASN1Boolean extends ASN1Type {
    public ASN1Boolean() {
        super(UNIVERSAL, NONE, BOOLEAN, BOOLEAN);
    }

    public void setValue(boolean boolValue) {
        byte[] val = new byte[1];
        val[0] = (byte) (boolValue ? 0x01 : 0x00);
        setValue(val);
    }

    public String toString() {
        return (getValue() != null && getValue()[0] != 0x00 ? "true" : "false");
    }
}
