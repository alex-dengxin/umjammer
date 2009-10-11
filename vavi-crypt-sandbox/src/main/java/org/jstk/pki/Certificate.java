/*
 * @(#) $Id: Certificate.java,v 1.1.1.1 2003/10/05 18:39:20 pankaj_kumar Exp $
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
 * Certificate ::= SEQUENCE { tbsCertificate TBSCertificate, algorithm AlgorithmIdentifier, signatureBytes BIT STRING }
 */
public class Certificate extends ASN1Seq {
    private TBSCertificate tbsCertificate = new TBSCertificate();

    private AlgorithmIdentifier algorithm = new AlgorithmIdentifier();

    private ASN1BitString signatureBytes = new ASN1BitString();

    public Certificate() {
        super();
        add(tbsCertificate);
        add(algorithm);
        add(signatureBytes);
    }

    public TBSCertificate getTBSCertificate() {
        return tbsCertificate;
    }

    public AlgorithmIdentifier getAlgorithm() {
        return algorithm;
    }

    public ASN1BitString getSignatureBytes() {
        return signatureBytes;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Certificate-SEQ(" + tbsCertificate.toString() + ", ");
        sb.append(algorithm.toString() + ", " + signatureBytes.toString() + ")");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage::java org.jstk.pki.Certificate <file>");
            return;
        }
        String file = args[0];
        InputStream is = null;
        try { // Try PEM format
            BufferedReader reader = new BufferedReader(new FileReader(file));
            PEMData x = new PEMData(reader);
            is = new ByteArrayInputStream(x.decode());
        } catch (InvalidPEMFormatException exc) { // Assume DER format
            is = new FileInputStream(file);
        }
        DefASN1PullParser parser = new DefASN1PullParser();
        parser.setInput(is);

        Certificate cert = new Certificate();
        cert.decode(parser);
        System.out.println(cert.toString());

        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 1);
        String outfile = opts.get("encode");
        if (outfile != null) {
            System.out.println("Writing the data in DER format to file: " + outfile);
            FileOutputStream fos = new FileOutputStream(outfile);
            byte[] encoded = cert.encode();
            fos.write(encoded);
            fos.close();
        }
    }
}
