/*
 * @(#) $Id: AlgorithmIdentifier.java,v 1.1.1.1 2003/10/05 18:39:20 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import org.jstk.asn1.ASN1Any;
import org.jstk.asn1.ASN1Oid;
import org.jstk.asn1.ASN1Seq;
import org.jstk.asn1.ASN1Type;


/*
 * AlgorithmIdentifier ::= SEQUENCE { algorithm OBJECT IDENTIFIER, parameters ANY DEFINED BY algorithm OPTIONAL }
 */
public class AlgorithmIdentifier extends ASN1Seq {
    private ASN1Oid algorithm = new ASN1Oid();

    private ASN1Any parameters = new ASN1Any();

    public AlgorithmIdentifier() {
        super();
        add(algorithm);
        add(parameters);
    }

    public void setOid(String oid) {
        algorithm.setOid(oid);
    }

    public void setParams(ASN1Type params) {
        parameters.setInstance(params);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AlgorithmIdentifier-SEQ(" + algorithm.toString() + ", ");
        sb.append(parameters.toString() + ")");
        return sb.toString();
    }
}
