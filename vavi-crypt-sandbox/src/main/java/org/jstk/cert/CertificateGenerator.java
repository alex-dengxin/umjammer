/*
 * @(#) $Id: CertificateGenerator.java,v 1.1.1.1 2003/10/05 18:39:13 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.jstk.JSTKException;
import org.jstk.asn1.ASN1BitString;
import org.jstk.asn1.ASN1Boolean;
import org.jstk.asn1.ASN1Explicit;
import org.jstk.asn1.ASN1Integer;
import org.jstk.asn1.ASN1Null;
import org.jstk.asn1.ASN1OctetString;
import org.jstk.asn1.ASN1Oid;
import org.jstk.asn1.ASN1Seq;
import org.jstk.asn1.DefASN1PullParser;
import org.jstk.asn1.OidMap;
import org.jstk.pki.AlgorithmIdentifier;
import org.jstk.pki.CSR;
import org.jstk.pki.Name;
import org.jstk.pki.SubjectPublicKeyInfo;
import org.jstk.pki.TBSCertificate;
import org.jstk.pki.Validity;


public class CertificateGenerator {
    public final static int DEFAULT_VALIDITY_PERIOD = 365; // No. of days.

    public final static String DEFAULT_SIGNATURE_ALGORITHM = "dsaWithSHA1";

    private X509Certificate issuerCert = null;

    private PrivateKey issuerKey = null;

    private boolean caFlag = false;

    private int pathLen = 0;

    private String sigAlg = DEFAULT_SIGNATURE_ALGORITHM;

    private KeyUsage keyUsage = null;

    private List<String> extendedKeyUsage = null;

    public CertificateGenerator() {
    }

    public CertificateGenerator(X509Certificate issuerCert, PrivateKey issuerKey) {
        this.issuerCert = issuerCert;
        this.issuerKey = issuerKey;
    }

    public X509Certificate generateCertificate(String csrFile, BigInteger serialNo) throws JSTKException {
        return generateCertificate(csrFile, serialNo, DEFAULT_VALIDITY_PERIOD);
    }

    public void setBasicConstraints(boolean caFlag, int pathLen) {
        this.caFlag = caFlag;
        this.pathLen = pathLen;
    }

    public void setSigAlg(String sigAlg) {
        this.sigAlg = sigAlg;
    }

    public void setKeyUsage(KeyUsage keyUsage) {
        this.keyUsage = keyUsage;
    }

    public void setExtendedKeyUsage(List<String> extendedKeyUsage) {
        this.extendedKeyUsage = extendedKeyUsage;
    }

    public X509Certificate generateCertificate(String csrFile, BigInteger serialNo, int noDays) throws JSTKException {
        Calendar cal = Calendar.getInstance();
        Date notBefore = cal.getTime();
        cal.add(Calendar.DATE, noDays);
        Date notAfter = cal.getTime();
        return generateCertificate(csrFile, serialNo, notBefore, notAfter, sigAlg);
    }

    public X509Certificate generateCertificate(String csrFile, BigInteger serialNo, Date notBefore, Date notAfter, String sigAlgorithm) throws JSTKException {
        if (issuerCert == null || issuerKey == null) {
            throw new JSTKException("CertificateGenerator not initialized.");
        }
        try {
            // Get the CSR
            InputStream csris = org.jstk.pem.PEMData.getDERInputStream(csrFile);
            CSR csr = new CSR();
            DefASN1PullParser parser = new DefASN1PullParser();
            parser.setInput(csris);
            csr.decode(parser);

            // Setup Certificate.
            org.jstk.pki.Certificate cert = new org.jstk.pki.Certificate();
            TBSCertificate tbsCertificate = cert.getTBSCertificate();

            ASN1Integer version = tbsCertificate.getVersion().getVersion();
            version.setValue((new BigInteger("2")).toByteArray());

            tbsCertificate.getSerialNumber().setValue(serialNo.toByteArray());

            AlgorithmIdentifier algorithmId = tbsCertificate.getAlgorithm();
            algorithmId.setOid(OidMap.getId(sigAlgorithm));
            algorithmId.setParams(new ASN1Null());

            // Setup Issuer
            javax.security.auth.x500.X500Principal p = issuerCert.getSubjectX500Principal();
            Name issuer = tbsCertificate.getIssuer();
            issuer.setValue(p.getEncoded());
            issuer.setIgnoreMembers(true);

            // Setup validity period
            Validity validity = tbsCertificate.getValidity();
            validity.getNotBefore().setDate(notBefore);
            validity.getNotAfter().setDate(notAfter);

            // Setup Subject
            Name subject = tbsCertificate.getSubject();
            subject.reinitialize(csr.getCSRInfo().getSubject());
            SubjectPublicKeyInfo publicKeyInfo = tbsCertificate.getSubjectPublicKeyInfo();
            publicKeyInfo.reinitialize(csr.getCSRInfo().getPublicKeyInfo());

            // Setup Extensions
            ASN1Explicit extensions = tbsCertificate.getExtensions();
            ASN1Seq extnsSeq = new ASN1Seq();

            ASN1Seq basicConsExtn = new ASN1Seq();
            basicConsExtn.setValue(encodeBasicConstraints());
            basicConsExtn.setIgnoreMembers(true);
            extnsSeq.add(basicConsExtn);

            if (keyUsage != null) { // Add KeyUsage extension.
                extnsSeq.add(createKeyUsage());
            }

            if (extendedKeyUsage != null) { // Add KeyUsage extension.
                extnsSeq.add(createExtendedKeyUsage());
            }

            extensions.setInstance(extnsSeq);

            // Setup Algorithm identifier. Note that this is duplicate !! previous one
            // gets signed ( cannot be tampered ).
            AlgorithmIdentifier algorithmId1 = cert.getAlgorithm();
            algorithmId1.setOid(OidMap.getId(sigAlgorithm));
            algorithmId1.setParams(new ASN1Null());

            X509Certificate c = signCertificate(cert, sigAlgorithm, issuerKey);
            return c;
        } catch (Exception exc) {
            throw new JSTKException("generateCertificate failed", exc);
        }
    }

    public X509Certificate generateSelfSignedCertificate(String dn, KeyPair kp, BigInteger serialNo, int noDays) throws JSTKException {
        Calendar cal = Calendar.getInstance();
        Date notBefore = cal.getTime();
        cal.add(Calendar.DATE, noDays);
        Date notAfter = cal.getTime();
        return generateSelfSignedCertificate(dn, kp, serialNo, notBefore, notAfter, sigAlg);
    }

    public X509Certificate generateSelfSignedCertificate(String dn, KeyPair kp, BigInteger serialNo, Date notBefore, Date notAfter, String sigAlgorithm) throws JSTKException {
        try {
            // Setup Certificate.
            org.jstk.pki.Certificate cert = new org.jstk.pki.Certificate();
            TBSCertificate tbsCertificate = cert.getTBSCertificate();

            ASN1Integer version = tbsCertificate.getVersion().getVersion();
            version.setValue((new BigInteger("2")).toByteArray());

            tbsCertificate.getSerialNumber().setValue(serialNo.toByteArray());

            AlgorithmIdentifier algorithmId = tbsCertificate.getAlgorithm();
            algorithmId.setOid(OidMap.getId(sigAlgorithm));
            algorithmId.setParams(new ASN1Null());

            // Setup Issuer
            X500Principal p = new X500Principal(dn);
            Name issuer = tbsCertificate.getIssuer();
            issuer.setValue(p.getEncoded());
            issuer.setIgnoreMembers(true);

            // Setup validity period
            Validity validity = tbsCertificate.getValidity();
            validity.getNotBefore().setDate(notBefore);
            validity.getNotAfter().setDate(notAfter);

            // Setup Subject
            Name subject = tbsCertificate.getSubject();
            subject.setValue(p.getEncoded());
            subject.setIgnoreMembers(true);
            SubjectPublicKeyInfo publicKeyInfo = tbsCertificate.getSubjectPublicKeyInfo();
            byte[] encoded = kp.getPublic().getEncoded();
            publicKeyInfo.setValue(encoded);
            publicKeyInfo.setIgnoreMembers(true);

            // Setup Extensions
            ASN1Explicit extensions = tbsCertificate.getExtensions();
            ASN1Seq extnsSeq = new ASN1Seq();

            ASN1Seq basicConsExtn = new ASN1Seq();
            basicConsExtn.setValue(encodeBasicConstraints());
            basicConsExtn.setIgnoreMembers(true);
            extnsSeq.add(basicConsExtn);

            extensions.setInstance(extnsSeq);

            // Setup Algorithm identifier. Note that this is duplicate !! previous one
            // gets signed ( cannot be tampered ).
            AlgorithmIdentifier algorithmId1 = cert.getAlgorithm();
            algorithmId1.setOid(OidMap.getId(sigAlgorithm));
            algorithmId1.setParams(new ASN1Null());

            X509Certificate c = signCertificate(cert, sigAlgorithm, kp.getPrivate());
            return c;
        } catch (Exception exc) {
            throw new JSTKException("generateCertificate failed", exc);
        }
    }

    byte[] encodeBasicConstraints() {
        ASN1Seq basicConsExtn = new ASN1Seq();
        ASN1Oid oid = new ASN1Oid();
        oid.setOid("2.5.29.19");

        ASN1Seq basicConstraints = new ASN1Seq();
        ASN1Boolean ab = new ASN1Boolean();
        ab.setValue(caFlag);
        ASN1Integer ai = new ASN1Integer();
        ai.setValue(new BigInteger(Integer.toString(pathLen)));
        basicConstraints.add(ab);
        basicConstraints.add(ai);

        ASN1OctetString aos = new ASN1OctetString();
        aos.setValue(basicConstraints.encode());

        basicConsExtn.add(oid);
        basicConsExtn.add(aos);
        return basicConsExtn.encode();
    }

    ASN1Seq createKeyUsage() {
        ASN1Seq keyUsageExtn = new ASN1Seq();
        ASN1Oid oid = new ASN1Oid();
        oid.setOid("2.5.29.15");
        ASN1BitString abs = new ASN1BitString();
        abs.setValue(keyUsage.getBitString(), keyUsage.getNumUnusedBits());

        ASN1OctetString aos = new ASN1OctetString();
        aos.setValue(abs.encode());

        keyUsageExtn.add(oid);
        keyUsageExtn.add(aos);
        return keyUsageExtn;
    }

    ASN1Seq createExtendedKeyUsage() {
        ASN1Seq ekuExtn = new ASN1Seq();
        ASN1Oid oid = new ASN1Oid();
        oid.setOid("2.5.29.37");
        ASN1Seq ids = new ASN1Seq();
        for (int i = 0; i < extendedKeyUsage.size(); i++) {
            ASN1Oid id = new ASN1Oid();
            id.setOid(extendedKeyUsage.get(i));
            ids.add(id);
        }

        ASN1OctetString aos = new ASN1OctetString();
        aos.setValue(ids.encode());

        ekuExtn.add(oid);
        ekuExtn.add(aos);
        return ekuExtn;
    }

    X509Certificate signCertificate(org.jstk.pki.Certificate cert, String sigAlgorithm, PrivateKey prvKey) throws Exception {
        TBSCertificate tbsCertificate = cert.getTBSCertificate();

        // Get the DER encoded TBSCertificate and sign it.
        byte[] encodedTBSCertificate = tbsCertificate.encode();
        Signature sig = Signature.getInstance(sigAlgorithm);
        sig.initSign(prvKey);
        sig.update(encodedTBSCertificate);
        byte[] sigbytes = sig.sign();

        ASN1BitString signatureBytes = cert.getSignatureBytes();
        signatureBytes.setValue(sigbytes);

        byte[] certBytes = cert.encode();

        ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate c = (X509Certificate) cf.generateCertificate(bais);
        return c;
    }
}
