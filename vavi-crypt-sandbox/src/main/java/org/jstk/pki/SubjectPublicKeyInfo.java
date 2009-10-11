/*
 * @(#) $Id: SubjectPublicKeyInfo.java,v 1.1.1.1 2003/10/05 18:39:21 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import org.jstk.asn1.ASN1BitString;
import org.jstk.asn1.ASN1Seq;


/*
 * SubjectPublicKeyInfo ::= SEQUENCE { algorithm AlgorithmIdentifier, subjectPublicKey BIT STRING }
 */
public class SubjectPublicKeyInfo extends ASN1Seq {
    private AlgorithmIdentifier algorithm = new AlgorithmIdentifier();

    private ASN1BitString subjectPublicKey = new ASN1BitString();

    public SubjectPublicKeyInfo() {
        super();
        add(algorithm);
        add(subjectPublicKey);
    }

    public void reinitialize(SubjectPublicKeyInfo pkInfo) {
        elems = pkInfo.elems; // Shallow copy. beware !!
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SubjectPublicKeyInfo-SEQ(" + algorithm.toString() + ", ");
        sb.append(subjectPublicKey.toString() + ")");
        return sb.toString();
    }
}
