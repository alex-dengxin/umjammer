/*
 * Copyright (c) 2001 Brian Boyter
 * All rights reserved
 *
 * This software is released subject to the GNU Public License.  See
 * the full license included with this distribution.
 */

package com.boyter.mscrypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;

import vavi.util.Debug;


/**
 * MSCryptoManager.
 * 
 * @author Brian Boyter
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano modified <br>
 */
public class MSCryptoManager {

    /** */
    private MSCryptoManager() {
    }

    /** */
    private static MSCryptoManager cryptoManager = new MSCryptoManager();  

    /** */
    public static MSCryptoManager getInstance() {
        return cryptoManager;
    }

    /** */
    private CertStore caCerts = null;

    /** */
    public enum Flag {
        /** */
        RejectTheCertIfWeCannotTellIfItIsRevoked,
        /** */
        AcceptTheCertAnyway,
        /** */
        AskTheUser;
    }

    /**
     * Returns true if the cert is valid.
     * 
     * Certificate is valid if: a. Has a chain of trust back to a trusted root
     * CA. b. The certs in the cert chain are not expired. c. The certs in the
     * cert chain have not been revoked. d. The cert is valid for the purpose it
     * is being used.
     * 
     * The DontKnowFlag tells this routine what to do if the revocation status
     * of the cert cannot be established: 0 = reject the cert 1 = accept the
     * cert anyway 2 = prompt the user what he wants to do
     * 
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public final boolean isCertValid(X509Certificate cert, Flag dontKnowFlag)
        throws GeneralSecurityException, IOException {
Debug.println("isCertValid: Entered");

        // Get the certificate chain
        X509Certificate[] CertChain = getCertChain(cert);
        if (CertChain == null) {
            return false;
        }

        return isCertChainValid(CertChain, dontKnowFlag);
    }

    /**
     * @throws GeneralSecurityException 
     * @throws IOException 
     */
    public boolean isCertChainValid(X509Certificate[] certChain, Flag dontKnowFlag)
        throws GeneralSecurityException, IOException {

Debug.println("isCertChainValid: Entered: x " + certChain.length);

        for (int i = 0; i < certChain.length; i++) {
            X509Certificate cert = certChain[i];
            Principal subjectDN = cert.getSubjectDN();
Debug.println(" CertChain[" + i + "]: " + subjectDN.toString());
            Principal issuerDN = cert.getIssuerDN();
            X509Certificate issuerCert = cert;

            if (i < certChain.length - 1) { // true if not a root CA cert
                issuerCert = certChain[i + 1]; // issuer is next cert in the
            } // chain

            // is cert within validity period?
            if (!isCertWithinValidityPeriod(cert)) {
                return false;
            }

            // verify the signature on the cert
            if (!verifySignature(issuerCert, cert)) {
                return false;
            }

            // is cert revoked?
            if (subjectDN.equals(issuerDN)) { // test if this is a root CA
                // cert
Debug.println(" Assume root CA certs are never revoked: " + subjectDN);
                continue; // skip the root CA
            }

            if (isCertRevoked(cert, dontKnowFlag)) {
Debug.println("revoked: " + cert.getSubjectDN());
                return false;
            }
        }

Debug.println("isCertChainValid: YES - cert chain is trusted");

        return true;
    }

    /**
     * check if the cert is revoked
     * 
     * @throws CertificateEncodingException 
     * @throws IOException 
     * @return true when cert has revoked  
     */
    private final boolean isCertRevoked(X509Certificate cert, Flag dontKnowFlag)
        throws CertificateEncodingException, IOException {

        byte[] certBlob = null;

Debug.println("isCertRevoked: Entered flag: " + dontKnowFlag);

        certBlob = cert.getEncoded();

        // Does the cert have a CDP (CRL distribution point)???
        byte[] cdpBlob = cert.getExtensionValue("2.5.29.31");
        if (cdpBlob == null) {
Debug.println("isCertRevoked: cert does not contain a CDP");
Debug.println("Cannot determine if certificate is revoked (no CDP)");
            return !askUserWhatHeWantsToDo(cert.getSubjectDN().toString(), dontKnowFlag);
        }

        // yes there is a CDP - ASN parse the CDP
        String[] urlArray = CDPParser.parseCDP(cdpBlob);
//Debug.println("urlArray: " + urlArray.length);
        boolean crlDownloadOk = false;
        if (urlArray != null) {
            for (int i = 0; i < urlArray.length; i++) {
                String URL = urlArray[i];

                // go fetch that CRL
Debug.println("isCertRevoked: fetching the CRL, URL: " + URL);
                if (getCRL(URL)) {
                    crlDownloadOk = true;
                    break; // url was fetched correctly
                } else {
Debug.println("Download failed for CRL: " + URL);
                }
            }
            if (!crlDownloadOk) {
                return !askUserWhatHeWantsToDo(cert.getSubjectDN().toString(), dontKnowFlag);
            }
        }

        // is the cert revoked???
        int revocationStatus = verifyCertRevocation(certBlob);

//Debug.println("isCertRevoked: revocationStatus: " + revocationStatus);

        switch (revocationStatus) {
        case 0:
            // cert is revoked
Debug.println("Certificate " + cert.getSubjectDN().toString() + "is revoked");
            return !askUserWhatHeWantsToDo(cert.getSubjectDN().toString(), dontKnowFlag);
        case 1:
            // cert is not revoked
Debug.println("isCertRevoked: the cert has not been revoked");
            return false;
        default:
Debug.println("Cannot determine if certificate [" + cert.getSubjectDN().toString() + "] is revoked or not, cause: " + revocationStatus);
            return !askUserWhatHeWantsToDo(cert.getSubjectDN().toString(), dontKnowFlag);
        }
    }

    /** TODO out source */
    private CallbackHandler handler = new com.sun.security.auth.callback.DialogCallbackHandler();

    /**
     * helper function
     * @return true if accepted
     */
    private final boolean askUserWhatHeWantsToDo(String dn, Flag dontKnowFlag) {

        switch (dontKnowFlag) {
        case RejectTheCertIfWeCannotTellIfItIsRevoked: // reject the cert
            return false;
        case AcceptTheCertAnyway: // accept the cert anyway
            return true;
        default: // ask the user what he wants to do
            break;
        }

        try {
            ConfirmationCallback cc = new ConfirmationCallback("Do you want to accept the cert anyway?\n" + dn, ConfirmationCallback.WARNING, ConfirmationCallback.YES_NO_OPTION, ConfirmationCallback.NO);
            handler.handle(new Callback[] {
                cc
            });
            boolean result = cc.getOptionType() == ConfirmationCallback.YES;
Debug.println("result: " + result);
            return  result;
        } catch (Exception e) {
Debug.printStackTrace(e);
            return false;
        }
    }

    /**
     * helper function
     */
    private final boolean isCertWithinValidityPeriod(X509Certificate cert) {

        // check the validity period
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
Debug.println("isCertWithinValidityPeriod: NO - cert is expired");
            return false;
        } catch (CertificateNotYetValidException e) {
Debug.println("isCertWithinValidityPeriod: NO - cert is notYetValid");
            return false;
        }

        return true;
    }

    /**
     * helper function
     * @return <code>true</code> signature is valid
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     * @throws CertificateException 
     * @throws InvalidKeyException 
     */
    private final boolean verifySignature(X509Certificate issuerCert, X509Certificate cert)
        throws InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException {

        PublicKey key = issuerCert.getPublicKey();
        try {
            cert.verify(key);
        } catch (SignatureException e) {
Debug.println("verifySignature: NG - cert has been corrupted");
            return false;
        }

Debug.println("verifySignature: OK - cert signature verified");
        return true;
    }

    /**
     * Reads Microsoft certificate store Returns array of trusted root CA
     * certificates
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public final CertStore getCaCerts() throws GeneralSecurityException, IOException {

Debug.println("getCACerts: entered\n");

        if (caCerts != null) {
            return caCerts;
        }

        String[] encodedCerts = getCACerts();
Debug.println("getCACerts: " + encodedCerts.length + " CA certs found");
        List<X509Certificate> caList = new ArrayList<X509Certificate>(encodedCerts.length);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        for (int i = 0; i < encodedCerts.length; i++) {
            try {
//Debug.println(encodedCerts[i].getBytes().length + " bytes\n" + StringUtil.getDump(encodedCerts[i]));
                InputStream input = new ByteArrayInputStream(encodedCerts[i].getBytes("UTF-8"));
                X509Certificate cert = (X509Certificate) cf.generateCertificate(input);
                input.close();
//Debug.println("[" + i + "]: " + cert.getSubjectX500Principal().getName());
                caList.add(cert);
            } catch (GeneralSecurityException e) {
Debug.println("error in following cert:[" + i + "]\n" + encodedCerts[i]);
Debug.printStackTrace(e);
            }
        }

Debug.println("getCACerts: get list of CRL URLs");

        CollectionCertStoreParameters caCcsp = new CollectionCertStoreParameters(caList);
        caCerts = CertStore.getInstance("Collection", caCcsp);

Debug.println("getCACerts: normal exit");

        return caCerts;
    }

    /**
     * Returns the certificate chain to validate the given alias.
     * 
     * @throws IOException
     * @throws CertStoreException
     */
    public final X509Certificate[] getCertChain(X509Certificate cert) throws GeneralSecurityException, IOException {

Debug.println("getCertChain: entered");

        List<X509Certificate> certChainList = new ArrayList<X509Certificate>();
        // X509Certificate issuercert;
        boolean match;
        X509Certificate[] issuerArray = null;

        getCaCerts();

        Principal subject = cert.getSubjectDN();
        Principal issuer = cert.getIssuerDN();
Debug.println("  add to cert chain: " + subject);
        certChainList.add(cert);

        while (!(issuer.equals(subject))) { // stop if issuer==subject (root CA)
            match = false;

            X509CertSelector xcs = new X509CertSelector();
            xcs.setCertificateValid(new Date());
            // xcs.setSubject(issuer.toString());
            Collection<? extends Certificate> certCollection = caCerts.getCertificates(xcs);

            // using setSubject on the X509CertSelector is broken.
            // Note that it is commented out above.
            // This got broken in JDK 1.4.0-rc and may work again in the
            // future....
            // In the meantime, the workaround is below. Instead of using
            // CertSelector
            // to find certs with a specific subject, we must loop thru the
            // certcollection
            // and remove certs that do not match the desired subject
            Iterator<? extends Certificate> certIterator = certCollection.iterator();
            while (certIterator.hasNext()) {
                X509Certificate cacert = (X509Certificate) (certIterator.next());
                if (!cacert.getSubjectDN().equals(issuer)) {
                    certIterator.remove();
                }
            }

Debug.println(certCollection.size() + " certs found");
            issuerArray = new X509Certificate[certCollection.size()];
            issuerArray = certCollection.toArray(issuerArray);

            for (int i = 0; i < issuerArray.length; i++)
                if (verifySignature(issuerArray[i], cert)) {
                    match = true;
                    cert = issuerArray[i];
                    subject = cert.getSubjectDN();
                    issuer = cert.getIssuerDN();
Debug.println("  add to cert chain: " + subject);
                    certChainList.add(cert);
                    break;
                }
            if (!match) {
Debug.println("ERROR - certChain is broken");
                return null;
            }
        }

        X509Certificate[] certChain = new X509Certificate[certChainList.size()];
        certChainList.toArray(certChain);

Debug.println(certChain.length + " certs in cert chain");

//for (int i = 0; i < certChain.length; i++) {
// Debug.println("getCertChain: " + certChain[i].getSubjectDN());
//}

        return certChain;
    }

    /** */
    private native int verifyCertRevocation(byte[] Cert);

    /** */
    native String[] getAliases(String certStore);

    /** */
    native byte[] getPrivateKey(String alias);

    /** */
    native byte[] getCert(String certStore, String alias);

    /** */
    native void initRSASign(byte[] privatekey, String hashalg);

    /** */
    native void updateRSASign(byte[] data);

    /** */
    native byte[] getRSASign();

    /** */
    native byte[] getRSASignHash(byte[] hash, byte[] privatekey, String hashalg);

    /** */
    private native String[] getCACerts();

    /** */
    native byte[] decryptRSA(String padalg, byte[] data);

    /** */
    native byte[] encryptRSA(String padalg, byte[] data);

    /** */
    native int getRSAKeysize();

    /** */
    private native boolean getCRL(String url);

    static {
        System.loadLibrary("mscrypto");
    }
}

/* */
