/*
 * @(#) $Id: CRLGenCommand.java,v 1.1.1.1 2003/10/05 18:39:13 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.asn1.ASN1BitString;
import org.jstk.asn1.ASN1Null;
import org.jstk.asn1.OidMap;
import org.jstk.cert.ca.CADatabase;
import org.jstk.cert.ca.FileBasedCADatabaseParams;
import org.jstk.cert.ca.RevokedCert;
import org.jstk.cert.ca.RevokedCerts;
import org.jstk.pki.AlgorithmIdentifier;
import org.jstk.pki.CertificateList;
import org.jstk.pki.Name;
import org.jstk.pki.TBSCertList;


public class CRLGenCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("crlfile", "my.crl");
        defaults.put("cadir", "cadir");
    }

    public String briefDescription() {
        String briefDesc = "generates CRL of all the revoked certificates";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -crlfile <crlfile>  : CRL file.[" + defaults.get("crlfile") + "]\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-crlfile <crlfile>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "", "-crlfile test.crl"
        };
        return sampleUses;
    }

    private void signTBSCertList(CertificateList certList, String sigAlgorithm, PrivateKey prvKey) throws Exception {

        AlgorithmIdentifier algorithm = certList.getAlgorithm();
        algorithm.setOid(OidMap.getId(sigAlgorithm));
        algorithm.setParams(new ASN1Null());

        TBSCertList tbsCertList = certList.getTBSCertList();

        // Get the DER encoded TBSCertificate and sign it.
        byte[] encodedTBSCertList = tbsCertList.encode();
        Signature sig = Signature.getInstance(sigAlgorithm);
        sig.initSign(prvKey);
        sig.update(encodedTBSCertList);
        byte[] sigbytes = sig.sign();

        ASN1BitString signatureBytes = certList.getSignatureBytes();
        signatureBytes.setValue(sigbytes);
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String crlfile = args.get("crlfile");
            String cadir = args.get("cadir");

            FileBasedCADatabaseParams fbParams = new FileBasedCADatabaseParams(cadir);
            CADatabase cadb = CADatabase.getInstance("file", fbParams);

            // cadb.getIssuedCerts().add(cert);

            byte[] outBytes;
            CertificateList certList = new CertificateList();

            TBSCertList tbsCertList = certList.getTBSCertList();

            tbsCertList.getVersion().setValue(new BigInteger("1"));

            AlgorithmIdentifier algorithm = tbsCertList.getAlgorithm();
            algorithm.setOid(OidMap.getId("dsaWithSHA1"));
            algorithm.setParams(new ASN1Null());

            X509Certificate caCert = (X509Certificate) cadb.getCACert();

            // Setup Issuer
            javax.security.auth.x500.X500Principal p = caCert.getSubjectX500Principal();
            Name issuer = tbsCertList.getIssuer();
            issuer.setValue(p.getEncoded());
            issuer.setIgnoreMembers(true);

            java.util.Calendar cal = java.util.Calendar.getInstance();
            java.util.Date thisUpdate = cal.getTime();
            tbsCertList.getThisUpdate().setDate(thisUpdate);
            cal.add(Calendar.DATE, 30);
            java.util.Date nextUpdate = cal.getTime();
            tbsCertList.getNextUpdate().setDate(nextUpdate);

            // Add revoked certificates.
            TBSCertList.RevokedCerts rcs = tbsCertList.getRevokedCerts();
            RevokedCerts revokedCerts = cadb.getRevokedCerts();
            Iterator<? extends RevokedCert> itr = revokedCerts.iterator();
            while (itr.hasNext()) {
                TBSCertList.RevokedCert rc = new TBSCertList.RevokedCert();
                RevokedCert revokedCert = itr.next();
                rc.getUserCertificate().setValue(revokedCert.getSerialNumber());
                rc.getRevocationDate().setDate(revokedCert.getRevocationDate());
                rcs.add(rc);
            }

            signTBSCertList(certList, "dsaWithSHA1", cadb.getCAPrivateKey());

            outBytes = certList.encode();
            FileOutputStream fos = new FileOutputStream(crlfile);
            fos.write(outBytes);
            fos.close();

            return new JSTKResult(null, true, "Generated CRL written to file: " + crlfile);
        } catch (Exception exc) {
            throw new JSTKException("CRLGenCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        CRLGenCommand crlGenCmd = new CRLGenCommand();
        JSTKResult result = (JSTKResult) crlGenCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
