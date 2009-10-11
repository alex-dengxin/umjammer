/*
 * @(#) $Id: SignedData.java,v 1.1.1.1 2003/10/05 18:39:21 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import org.jstk.asn1.ASN1Integer;
import org.jstk.asn1.ASN1Seq;
import org.jstk.asn1.ASN1Set;


/*
 * From PKCS#7: SignedData ::= SEQUENCE { version Version, digestAlgorithms DigestAlgorithmsIdentifiers, contentInfo ContentInfo,
 * certificates [0] IMPLICIT ExtendedCertificatesAndCertificates OPTIONAL, crls [1] IMPLICIT CertificateRevocationLists OPTIONAL,
 * signerInfos SignerInfos, }
 */
public class SignedData extends ASN1Seq {
    private ASN1Integer version = new ASN1Integer();

    private ASN1Set digestAlgorithms = new ASN1Set();

    private ContentInfo contentInfo = new ContentInfo();

    private ASN1Set certificates = new ASN1Set(CONTEXT, IMPLICIT, 0);

    private ASN1Set crls = new ASN1Set(CONTEXT, IMPLICIT, 1);

    private ASN1Set signerInfos = new ASN1Set();

    public SignedData() {
        super();
        certificates.setOptional(true);
        crls.setOptional(true);

        add(version);
        add(digestAlgorithms);
        add(contentInfo);
        add(certificates);
        add(crls);
        add(signerInfos);
    }

    public ASN1Integer getVersion() {
        return version;
    }

    public ASN1Set getDigestAlgorithms() {
        return digestAlgorithms;
    }

    public ContentInfo getContentInfo() {
        return contentInfo;
    }

    public ASN1Set getCertificates() {
        return certificates;
    }

    public ASN1Set getCrls() {
        return crls;
    }

    public ASN1Set getSignerInfos() {
        return signerInfos;
    }

    public String toString() {
        return ("SignedData-SEQ(TODO)");
    }
}
