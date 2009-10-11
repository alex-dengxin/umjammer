/*
 * @(#) $Id: Validity.java,v 1.1.1.1 2003/10/05 18:39:21 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import org.jstk.asn1.ASN1Seq;
import org.jstk.asn1.ASN1UTCTime;


/*
 * Validity ::= SEQUENCE { notBefore UTCTime, notAfter UTCTime }
 */
public class Validity extends ASN1Seq {
    private ASN1UTCTime notBefore = new ASN1UTCTime();

    private ASN1UTCTime notAfter = new ASN1UTCTime();

    public Validity() {
        super();
        add(notBefore);
        add(notAfter);
    }

    public ASN1UTCTime getNotBefore() {
        return notBefore;
    }

    public ASN1UTCTime getNotAfter() {
        return notAfter;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Validity-SEQ(" + notBefore.toString() + ", ");
        sb.append(notAfter.toString() + ")");
        return sb.toString();
    }
}
