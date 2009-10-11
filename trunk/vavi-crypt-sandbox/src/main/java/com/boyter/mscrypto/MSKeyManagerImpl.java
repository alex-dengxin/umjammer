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
import java.math.BigInteger;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.X509KeyManager;

import com.boyter.mscrypto.MSCryptoManager.Flag;

import vavi.util.Debug;


/**
 * MSKeyManagerImpl.
 * 
 * @author Brian Boyter
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano modified <br>
 */
final class MSKeyManagerImpl implements X509KeyManager {
    /**
     * @param ks use windows key store, so this means nothing, set null
     * @param passphrase use windows key store, so this means nothing, set null
     */
    MSKeyManagerImpl(KeyStore ks, char[] passphrase) throws KeyStoreException {
    }

    /** native interface */
    private static final MSCryptoManager msCryptoManager = MSCryptoManager.getInstance();

    /**
     * Choose an alias to authenticate the client side of a secure socket given
     * the public key type and the list of certificate issuer authorities
     * recognized by the peer (if any).
     */
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        String alias = null;

Debug.println(">>>> chooseClientAlias: entered: issures: " + issuers.length + ", types: " + keyType.length);

        try {
            List<String> aliases = new ArrayList<String>();
            for (int i = 0; i < keyType.length; i++) {
Debug.println(i + ": " + keyType[i]);
                String[] tmp = getClientAliases(keyType[i], issuers);
                aliases.addAll(Arrays.asList(tmp));
            }
            if (aliases.size() == 0) {
Debug.println("chooseClientAlias: something wrong - no aliases");
                return null;
            }
Debug.println("aliases: " + aliases.size());
            alias = aliases.get(0);
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }

Debug.println("<<<< chooseClientAlias: " + alias);
        return alias;
    }

    /**
     * Choose an alias to authenticate the server side of a secure socket given
     * the public key type and the list of certificate issuer authorities
     * recognized by the peer (if any).
     */
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        String alias = null;

Debug.println(">>>> chooseServerAlias: return server alias");

        try {
            String[] aliases = getServerAliases(keyType, issuers);
            if (aliases == null) {
Debug.println("chooseServerAlias: something wrong - no aliases");
                return null;
            }
            alias = aliases[0];
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }

Debug.println("<<<< chooseServerAlias: " + alias);

        return alias;
    }

    /**
     * Returns the certificate chain to validate the given alias.
     */
    public X509Certificate[] getCertificateChain(String alias) {

Debug.println(">>>> getCertificateChain: entered, alias:" + alias);

        X509Certificate[] certChain = null;
        X509Certificate cert = null;

        try {
            byte[] certBlob = msCryptoManager.getCert("My", alias);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bais = new ByteArrayInputStream(certBlob);
            cert = (X509Certificate) cf.generateCertificate(bais);
            bais.close();

            certChain = msCryptoManager.getCertChain(cert);

        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }

Debug.println("<<<< getCertificateChain: certChain:" + (certChain != null ? certChain.length : -1));
        return certChain;
    }

    /**
     * Get the matching aliases for authenticating the client side of a secure
     * socket given the public key type and the list of certificate issuer
     * authorities recognized by the peer (if any).
     */
    public String[] getClientAliases(String keyType, Principal[] issuers) {
Debug.println(">>>> getClientAliases: entered: " + keyType);
        String[] validAliases = null;

        try {
            String[] aliases = msCryptoManager.getAliases("My");
            if (aliases == null) {
                throw new IllegalStateException("No client aliases found");
            }

            // now throw out any aliases not signed by an approved issuer,
            // expired, or revoked
Debug.println("Number of accepted issuers: " + (issuers != null ? issuers.length : -1));
            validAliases = checkAlias(aliases, issuers);

        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }

Debug.println("<<<< aliases found: " + validAliases.length);
for (int i = 0; i < validAliases.length; i++) {
 Debug.println("getClientAliases: alias: " + validAliases[i]);
}

        return validAliases;
    }

    /**
     * Get the matching aliases for authenticating the server side of a secure
     * socket given the public key type and the list of certificate issuer
     * authorities recognized by the peer (if any).
     */
    public String[] getServerAliases(String keyType, Principal[] issuers) {
Debug.println("<<<< getServerAliases: return array of aliases ");
        String[] validAliases = null;

        try {
            String[] aliases = msCryptoManager.getAliases("My");
            if (aliases == null) {
Debug.println(">>>> No server aliases found");
                return null;
            }

            // now throw out any aliases not signed by an approved issuer,
            // expired, or revoked
            validAliases = checkAlias(aliases, issuers);

        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }

Debug.println(">>>> aliases found: " + validAliases.length);
for (int i = 0; i < validAliases.length; i++) {
 Debug.println("getServerAliases: alias: " + validAliases[i]);
}

        return validAliases;
    }

    /**
     * returns the RSA private key for the given alias
     */
    public PrivateKey getPrivateKey(String alias) {
        RSAPrivateKey rsaprivkey = null;
        RSAPrivateCrtKey rsaprivcrtkey = null;
        BigInteger mod = null;
        BigInteger exp = null;
        BigInteger coeff = null;
        BigInteger p = null;
        BigInteger q = null;
        BigInteger expp = null;
        BigInteger expq = null;
        BigInteger pubExp = null;
        byte[] pubExpBlob = new byte[4];
        byte[] keySizeBlob = new byte[4];
        int keySize;

Debug.println("<<<< getPrivateKey: entered, alias: " + alias);

        try {
            byte[] keyblob = msCryptoManager.getPrivateKey(alias);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            if (keyblob == null) { // generate a dummy key
                byte[] modblob = new byte[128];
                for (int i = 0; i < 128; i++) {
                    modblob[i] = 127;
                }
                mod = new BigInteger(modblob);
                exp = mod;

                RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(mod, exp);
                rsaprivkey = (RSAPrivateKey) kf.generatePrivate(privKeySpec);

Debug.println("getPrivateKey: normal exit");
                return rsaprivkey;

            } else { // use the key that got exported
                for (int i = 0; i < 4; i++) {
                    pubExpBlob[i] = keyblob[19 - i];
                    keySizeBlob[i] = keyblob[15 - i];
                }
                BigInteger bigKeySize = new BigInteger(keySizeBlob);
                keySize = bigKeySize.intValue();
Debug.println("keysize: " + keySize);

                byte[] modBlob = new byte[(keySize / 8)];
                byte[] expBlob = new byte[(keySize / 8)];
                byte[] pBlob = new byte[keySize / 16];
                byte[] qBlob = new byte[keySize / 16];
                byte[] exppBlob = new byte[keySize / 16];
                byte[] expqBlob = new byte[keySize / 16];
                byte[] coefBlob = new byte[keySize / 16];

                for (int i = 0; i < keySize / 8; i++) {
                    modBlob[i] = keyblob[19 - i + (keySize / 16) * 2];
                    expBlob[i] = keyblob[19 - i + (keySize / 16) * 9];
                }

                for (int i = 0; i < keySize / 16; i++) {
                    pBlob[i] = keyblob[19 - i + (keySize / 16) * 3];
                    qBlob[i] = keyblob[19 - i + (keySize / 16) * 4];
                    exppBlob[i] = keyblob[19 - i + (keySize / 16) * 5];
                    expqBlob[i] = keyblob[19 - i + (keySize / 16) * 6];
                    coefBlob[i] = keyblob[19 - i + (keySize / 16) * 7];
                }

                mod = new BigInteger(1, modBlob);
                exp = new BigInteger(1, expBlob);
                coeff = new BigInteger(1, coefBlob);
                p = new BigInteger(1, pBlob);
                q = new BigInteger(1, qBlob);
                expp = new BigInteger(1, exppBlob);
                expq = new BigInteger(1, expqBlob);
                pubExp = new BigInteger(1, pubExpBlob);

                RSAPrivateCrtKeySpec privCrtKeySpec = new RSAPrivateCrtKeySpec(mod, pubExp, exp, p, q, expp, expq, coeff);
                rsaprivcrtkey = (RSAPrivateCrtKey) kf.generatePrivate(privCrtKeySpec);
            }
        } catch (Exception e) {
            Debug.println(Level.SEVERE, ">>>> " + e);
//            throw (RuntimeException) new IllegalStateException().initCause(e);
        }

// Debug.println("mod: " + rsaprivcrtkey.getModulus());
// Debug.println("pubexp: " + rsaprivcrtkey.getPublicExponent());
// Debug.println("privexp: " + rsaprivcrtkey.getPrivateExponent());
// Debug.println("p: " + rsaprivcrtkey.getPrimeP());
// Debug.println("q: " + rsaprivcrtkey.getPrimeQ());
// Debug.println("expp: " + rsaprivcrtkey.getPrimeExponentP());
// Debug.println("expq: " + rsaprivcrtkey.getPrimeExponentQ());
// Debug.println("coeff: " + rsaprivcrtkey.getCrtCoefficient());

if (rsaprivcrtkey != null) {
Debug.println(">>>> getPrivateKey: normal exit");
}
        return rsaprivcrtkey;
    }

    /**
     * remove any aliases not signed by an approved issuer,
     * expired, or revoked
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private static String[] checkAlias(String[] aliases, Principal[] issuers) throws GeneralSecurityException, IOException {

Debug.println(">>>> CheckAlias: entered");
        X509Certificate cert = null;
        List<String> aliasList = new ArrayList<String>();
        List<String> issuerList = new ArrayList<String>();

Debug.println("aliases: " + aliases.length);
Debug.println("issuers: " + (issuers != null ? issuers.length : -1));
        for (int i = 0; i < aliases.length; i++) {
            aliasList.add(aliases[i]);
        }

        if (issuers != null) {
            for (int i = 0; i < issuers.length; i++) {
                issuerList.add(issuers[i].toString());
            }
        }

        // iterate thru the list of aliases
        Iterator<String> iter = aliasList.iterator();
        while (iter.hasNext()) {
            String alias = iter.next();

            // get the cert for this alias
            byte[] certBlob = msCryptoManager.getCert("My", alias);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream input = new ByteArrayInputStream(certBlob);
            cert = (X509Certificate) cf.generateCertificate(input);
            input.close();

            // is this alias's cert signed by an approved issuer?
            if (issuerList.size() != 0) {
                String certIssuer = cert.getIssuerDN().toString();
Debug.println("CheckAlias: certIssuer: " + certIssuer);
                if (!issuerList.contains(certIssuer)) {
                    iter.remove();
Debug.println("CheckAlias: no issuer found for alias " + alias);
                    continue;
                }
            }

            if (!msCryptoManager.isCertValid(cert, Flag.AcceptTheCertAnyway)) {
                iter.remove();
Debug.println("CheckAlias: cert is expired or revoked for alias " + alias);
                continue;
            }

Debug.println("CheckAlias: alias is valid " + alias);
        }

        aliases = new String[aliasList.size()];
        aliasList.toArray(aliases);

Debug.println("<<<< CheckAlias: valid aliases: " + aliases.length);
        return aliases;
    }
}

/* */
