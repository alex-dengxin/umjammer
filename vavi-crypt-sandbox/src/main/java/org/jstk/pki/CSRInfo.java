/*
 * @(#) $Id: CSRInfo.java,v 1.1.1.1 2003/10/05 18:39:21 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import org.jstk.asn1.ASN1Any;
import org.jstk.asn1.ASN1Integer;
import org.jstk.asn1.ASN1Seq;


/*
 * CSRInfo ::= SEQUENCE { version INTEGER, subject Name, publicKeyInfo SubjectPublicKeyInfo, attributes SET OF ... }
 */
public class CSRInfo extends ASN1Seq {
    // A sequence of following elements.
    private ASN1Integer version = new ASN1Integer();

    private Name subject = new Name();

    private SubjectPublicKeyInfo publicKeyInfo = new SubjectPublicKeyInfo();

    private ASN1Any attributes = new ASN1Any();

    public CSRInfo() {
        super();
        attributes.setTagNumber(0);
        attributes.setConsMask(CONSTRUCTED);

        add(version);
        add(subject);
        add(publicKeyInfo);
        add(attributes);
    }

    public ASN1Integer getVersion() {
        return version;
    }

    public Name getSubject() {
        return subject;
    }

    public void setSubject(Name subject) {
        this.subject = subject;
    }

    public SubjectPublicKeyInfo getPublicKeyInfo() {
        return publicKeyInfo;
    }

    public ASN1Any getAttributes() {
        return attributes;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CSRInfo-SEQ(" + version.toString() + ", " + subject.toString() + ", ");
        sb.append(publicKeyInfo.toString() + ", " + attributes.toString() + ")");
        return sb.toString();
    }
}
