/*
 * @(#) $Id: TBSCertList.java,v 1.1.1.1 2003/10/05 18:39:21 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import org.jstk.asn1.ASN1Explicit;
import org.jstk.asn1.ASN1Integer;
import org.jstk.asn1.ASN1Seq;
import org.jstk.asn1.ASN1UTCTime;


/*
 * TBSCertList ::= SEQUENCE { version Version OPTIONAL, -- if present, shall be v2 signature AlgorithmIdentifier, issuer Name,
 * thisUpdate Time, nextUpdate Time OPTIONAL, revokedCertificates SEQUENCE OF SEQUENCE { userCertificate CertificateSerialNumber,
 * revocationDate Time, crlEntryExtensions Extensions OPTIONAL, } OPTIONAL, crlExtensions [0] EXPLICIT Extensions OPTIONAL }
 */
public class TBSCertList extends ASN1Seq {
    public static class RevokedCert extends ASN1Seq {
        public ASN1Integer userCertificate = new ASN1Integer();

        public ASN1UTCTime revocationDate = new ASN1UTCTime();

        public ASN1Seq crlEntryExtensions = new ASN1Seq();

        public RevokedCert() {
            super();
            crlEntryExtensions.setOptional(true);

            add(userCertificate);
            add(revocationDate);
            add(crlEntryExtensions);
        }

        public ASN1Integer getUserCertificate() {
            return userCertificate;
        }

        public ASN1UTCTime getRevocationDate() {
            return revocationDate;
        }

        public ASN1Seq getCRLEntryExtensions() {
            return crlEntryExtensions;
        }
    }

    public static class RevokedCerts extends ASN1Seq {
        public RevokedCerts() {
            super();
        }
    }

    private ASN1Integer version = new ASN1Integer();

    private AlgorithmIdentifier algorithm = new AlgorithmIdentifier();

    private Name issuer = new Name();

    private ASN1UTCTime thisUpdate = new ASN1UTCTime();

    private ASN1UTCTime nextUpdate = new ASN1UTCTime();

    private RevokedCerts revokedCerts = new RevokedCerts();

    private ASN1Explicit crlExtensions = new ASN1Explicit(CONTEXT, EXPLICIT, 0);

    public TBSCertList() {
        super();
        version.setOptional(true);
        nextUpdate.setOptional(true);
        revokedCerts.setOptional(true);
        crlExtensions.setOptional(true);

        add(version);
        add(algorithm);
        add(issuer);
        add(thisUpdate);
        add(nextUpdate);
        add(revokedCerts);
        add(crlExtensions);
    }

    public ASN1Integer getVersion() {
        return version;
    }

    public AlgorithmIdentifier getAlgorithm() {
        return algorithm;
    }

    public Name getIssuer() {
        return issuer;
    }

    public ASN1UTCTime getThisUpdate() {
        return thisUpdate;
    }

    public ASN1UTCTime getNextUpdate() {
        return nextUpdate;
    }

    public RevokedCerts getRevokedCerts() {
        return revokedCerts;
    }

    public ASN1Explicit getCRLExtensions() {
        return crlExtensions;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("TBSCertList-SEQ(" + version.toString() + ", ");
        sb.append(algorithm.toString());
        sb.append(", " + issuer.toString() + ", " + thisUpdate.toString());
        sb.append(", " + nextUpdate.toString() + ", " + revokedCerts.toString() + crlExtensions.toString() + ")");
        return sb.toString();
    }
}
