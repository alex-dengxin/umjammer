/*
 * @(#) $Id: IssueCertCommand.java,v 1.1.1.1 2003/10/05 18:39:14 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertPath;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.asn1.ASN1Explicit;
import org.jstk.asn1.ASN1Oid;
import org.jstk.asn1.ASN1Seq;
import org.jstk.asn1.ASN1Set;
import org.jstk.asn1.DefASN1PullParser;
import org.jstk.cert.ca.CADatabase;
import org.jstk.cert.ca.FileBasedCADatabaseParams;
import org.jstk.pki.ContentInfo;
import org.jstk.pki.SignedData;


public class IssueCertCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("csrfile", "my.csr");
        defaults.put("cerfile", "my.cer");
        defaults.put("capath", "0");
        defaults.put("cadir", "cadir");
        defaults.put("cpfmt", "pkcs7");
        defaults.put("keyalg", "RSA");
        defaults.put("keysize", "1024");
        defaults.put("sigalg", "SHA1WithRSA");
    }

    public String briefDescription() {
        String briefDesc = "issues certificate based on Certificate Signing Request (CSR)";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -cadir <cadir>      : CA directory.[" + defaults.get("cadir") + "]\n" + "  -csrfile <csrfile>  : CSR file.[" + defaults.get("csrfile") + "]\n" + "  -cerfile <cerfile>  : File to write issued Certificate.[" + defaults.get("cerfile") + "]\n" + "  -ca                 : Allow generated cert. to be used as a CA cert.\n" + "  -capath <length>    : Certificate signing path length.[" + defaults.get("capath") + "]\n"
                             + "  -keyalg <keyalg>    : Algorithm for Key Pair generation (RSA|DSA).[" + defaults.get("keyalg") + "]\n" + "  -keysize <keysize>  : Size of key (no. of bits).[" + defaults.get("keysize") + "]\n" + "  -sigalg <sigalg>    : Signature Algorithm. Should match Key Algorithm.[" + defaults.get("sigalg") + "]\n" + "  -password <passwd>  : Password for CA keystore.\n" + "  -extnconf <conffile>: Configuration file to indicate extensions.\n"
                             + "  -cpfmt <cpfmt>      : Certificate Path format (pkcs7, pkipath or x509).[" + defaults.get("cpfmt") + "]\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-csrfile <csrfile>] [-cerfile <cerfile>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "", "-csrfile test.csr -cerfile test.cer"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String csrfile = args.get("csrfile");
            String cerfile = args.get("cerfile");
            String cadir = args.get("cadir");
            String cpfmt = args.get("cpfmt");
            boolean caFlag = Boolean.valueOf(args.get("ca")).booleanValue();
            int pathLen = Integer.parseInt(args.get("capath"));
//            String keyAlg = args.get("keyalg");
            String sigAlg = args.get("sigalg");
            String password = args.get("password");
            String conffile = args.get("extnconf");
//            int keySize = Integer.parseInt(args.get("keysize"));

            boolean pkcs7Format = false;
            boolean pkipathFormat = false;
//            boolean x509Format = false;
            if (cpfmt.equalsIgnoreCase("pkcs7"))
                pkcs7Format = true;
            else if (cpfmt.equalsIgnoreCase("pkipath"))
                pkipathFormat = true;
//            else if (cpfmt.equalsIgnoreCase("x509"))
//                x509Format = true;
            else
                return new JSTKResult(null, false, "Invalid CertPath format: " + cpfmt);

            if (password == null)
                return new JSTKResult(null, false, "CA keystore password not specified. Use -password option.");

            KeyUsage ku = null;
            List<String> eku = null;

            if (conffile != null) { // Read conffile
                Properties props = new Properties();
                try {
                    FileInputStream fis = new FileInputStream(conffile);
                    props.load(fis);
                } catch (IOException ioe) {
                    return new JSTKResult(null, false, "Cannot read extnconf file: " + ioe);
                }

                // Examine KeyUsage setting
                String kuFlag = props.getProperty("KeyUsage");
                if (kuFlag != null && kuFlag.equalsIgnoreCase("true")) {
                    ku = new KeyUsage();
                    String kuString = null;
                    int index = 0;
                    while ((kuString = KeyUsage.getKeyUsageString(index)) != null) {
                        String kuStringFlag = props.getProperty("KeyUsage." + kuString);
                        if (kuStringFlag != null && kuStringFlag.equalsIgnoreCase("true")) {
                            ku.setKeyUsage(index, true);
                        }
                        ++index;
                    }
                }

                // Examine ExtendedKeyUsage setting
                String ekuFlag = props.getProperty("ExtendedKeyUsage");
                if (ekuFlag != null && ekuFlag.equalsIgnoreCase("true")) {
                    eku = new LinkedList<String>();
                    String ekuOId = null;
                    int index = 0;
                    while ((ekuOId = props.getProperty("ExtendedKeyUsage.ObjectId." + index)) != null) {
                        eku.add(ekuOId);
                        ++index;
                    }
                }
            }

            FileBasedCADatabaseParams fbParams = new FileBasedCADatabaseParams(cadir);
            fbParams.setPassword(password);

            CADatabase cadb = CADatabase.getInstance("file", fbParams);
            CertificateGenerator cg = new CertificateGenerator((X509Certificate) cadb.getCACert(), cadb.getCAPrivateKey());

            cg.setBasicConstraints(caFlag, pathLen);
            cg.setSigAlg(sigAlg);
            cg.setKeyUsage(ku);
            cg.setExtendedKeyUsage(eku);

            X509Certificate cert = cg.generateCertificate(csrfile, cadb.nextSerialNumber());
            cadb.getIssuedCerts().add(cert);

            byte[] outBytes;
            if (pkipathFormat) {
                try {
                    CertPath caCertPath = cadb.getCACertPath();
                    byte[] caCertPathBytes = caCertPath.getEncoded();
                    ASN1Seq cpSeq = new ASN1Seq();
                    cpSeq.decode(DefASN1PullParser.getInstance(caCertPathBytes));
                    byte[] certBytes = cert.getEncoded();
                    ASN1Seq certSeq = new ASN1Seq();
                    certSeq.decode(DefASN1PullParser.getInstance(certBytes));
                    cpSeq.add(certSeq);
                    outBytes = cpSeq.encode();
                } catch (Exception e) {
                    throw new JSTKException("cannot form PkiPath certpath", e);
                }
            } else if (pkcs7Format) {
                try {
                    CertPath caCertPath = cadb.getCACertPath();
                    byte[] caCertPathBytes = caCertPath.getEncoded();
                    ASN1Seq cpSeq = new ASN1Seq();
                    cpSeq.decode(DefASN1PullParser.getInstance(caCertPathBytes));
                    byte[] certBytes = cert.getEncoded();
                    ASN1Seq certSeq = new ASN1Seq();
                    certSeq.decode(DefASN1PullParser.getInstance(certBytes));
                    ContentInfo ci = new ContentInfo();
                    ASN1Oid contentType = ci.getContentType();
                    contentType.setOid("1.2.840.113549.1.7.2");
                    ASN1Explicit content = ci.getContent();
                    SignedData sd = new SignedData();
                    sd.getVersion().setValue(new java.math.BigInteger("1"));
                    sd.getContentInfo().getContentType().setOid("1.2.840.113549.1.7.1");
                    content.setInstance(sd);
                    ASN1Set certs = sd.getCertificates();

                    certs.add(certSeq);
                    for (int i = cpSeq.size() - 1; i >= 0; i--)
                        certs.add(cpSeq.get(i));
                    outBytes = ci.encode();
                } catch (Exception e) {
                    throw new JSTKException("cannot form PKCS#7 certpath", e);
                }
            } else {
                outBytes = cert.getEncoded();
            }
            FileOutputStream fos = new FileOutputStream(cerfile);
            fos.write(outBytes);
            fos.close();

            return new JSTKResult(null, true, "Issued Certificate written to file: " + cerfile);
        } catch (Exception exc) {
            throw new JSTKException("IssueCertCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        IssueCertCommand issueCertCmd = new IssueCertCommand();
        JSTKResult result = (JSTKResult) issueCertCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
