/*
 * @(#) $Id: CertificateList.java,v 1.1.1.1 2003/10/05 18:39:20 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import java.io.*;

import org.jstk.asn1.*;
import org.jstk.pem.*;
import org.jstk.JSTKOptions;


/*
 * CertificateList ::= SEQUENCE { tbsCertList TBSCertList, algorithm AlgorithmIdentifier, signatureBytes BIT STRING }
 */
public class CertificateList extends ASN1Seq {
    private TBSCertList tbsCertList = new TBSCertList();

    private AlgorithmIdentifier algorithm = new AlgorithmIdentifier();

    private ASN1BitString signatureBytes = new ASN1BitString();

    public CertificateList() {
        super();
        add(tbsCertList);
        add(algorithm);
        add(signatureBytes);
    }

    public TBSCertList getTBSCertList() {
        return tbsCertList;
    }

    public AlgorithmIdentifier getAlgorithm() {
        return algorithm;
    }

    public ASN1BitString getSignatureBytes() {
        return signatureBytes;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CertificateList-SEQ(" + tbsCertList.toString() + ", ");
        sb.append(algorithm.toString() + ", " + signatureBytes.toString() + ")");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage::java org.jstk.pki.CertificateList <file> [-encode <outfile>]");
            return;
        }
        String file = args[0];
        InputStream is = PEMData.getDERInputStream(file);

        ASN1PullParser parser = DefASN1PullParser.getInstance(is);
        parser.setInput(is);

        CertificateList certList = new CertificateList();
        certList.decode(parser);
        System.out.println(certList.toString());

        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 1);
        String outfile = opts.get("encode");
        if (outfile != null) {
            System.out.println("Writing the data in DER format to file: " + outfile);
            FileOutputStream fos = new FileOutputStream(outfile);
            byte[] encoded = certList.encode();
            fos.write(encoded);
            fos.close();
        }
    }
}
