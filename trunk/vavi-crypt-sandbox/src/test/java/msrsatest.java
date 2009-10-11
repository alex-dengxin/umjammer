/*
 * Copyright (c) 2001 Brian Boyter
 * All rights reserved
 *
 * This software is released subject to the GNU Public License.  See
 * the full license included with this distribution.
 */

import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import vavi.util.Debug;


/**
 * msrsatest.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano initial version <br>
 */
public class msrsatest {

    /** */
    public static void main(String[] args) throws Exception {

Security.addProvider(new com.boyter.mscrypto.MSKeyManagerProvider());
Security.addProvider(new com.boyter.mscrypto.MSTrustManagerProvider());
Security.addProvider(new com.boyter.mscrypto.MSRSACipherProvider());
Security.addProvider(new com.boyter.mscrypto.MSRSASignProvider());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("MSKMF");
        kmf.init(null, null);

        X509KeyManager xkm = (X509KeyManager) kmf.getKeyManagers()[0];

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("MSTMF");
        tmf.init((KeyStore) null);

        X509TrustManager xtm = (X509TrustManager) tmf.getTrustManagers()[0];

        X509Certificate[] issuerCerts = xtm.getAcceptedIssuers();
Debug.println("number of CA certs found: " + issuerCerts.length);
        Principal[] issuers = new Principal[issuerCerts.length];

        for (int i = 0; i < issuerCerts.length; i++) {
            issuers[i] = issuerCerts[i].getSubjectDN();
        }
        String[] aliases = xkm.getClientAliases("X509", issuers);
Debug.println("number of aliases found: " + aliases.length);
        PrivateKey privkey = xkm.getPrivateKey(aliases[0]);
        X509Certificate[] chain = xkm.getCertificateChain(aliases[0]);
        PublicKey pubkey = chain[0].getPublicKey();

// Debug.println("test if cert is valid");
// if (MSValidCertificate.isCertValid(chain[0], 0)) {
//  Debug.println("cert is valid");
// } else {
//  Debug.println("cert is not valid");
// }
        String message = "RSA cipher test - OK";
        byte[] messageBytes = message.getBytes("UTF8");

        Cipher rc = Cipher.getInstance("RSA/ECB/PKCS1Padding", "MSRSACipher");
Debug.println("using provider: " + rc.getProvider().getName());

Debug.println("\n\nM   SrsaEncypt test");
        rc.init(Cipher.ENCRYPT_MODE, pubkey);
        byte[] encryptedMessage = rc.doFinal(messageBytes);

Debug.println("\n\nMSrsaDecrypt test");
        rc.init(Cipher.DECRYPT_MODE, privkey);
        byte[] decryptedMessage = rc.doFinal(encryptedMessage);
        String decryptedMessageString = new String(decryptedMessage, "UTF8");
Debug.println("\nDecrypted message: " + decryptedMessageString);

Debug.println("\n\nMD5withRSA Signature test");
        Signature rsa = Signature.getInstance("MD5withRSA");
Debug.println("using provider: " + rsa.getProvider().getName());
        rsa.initSign(privkey);
        rsa.update(messageBytes);
        byte[] sig = rsa.sign();
Debug.println("signature OK - length: " + sig.length);

Debug.println("\n\nMD5withRSA Signature verify test");
        rsa.initVerify(pubkey);
        rsa.update(messageBytes);
if (rsa.verify(sig)) {
 Debug.println("signature verify OK");
} else {
 Debug.println("signature verify failed");
}
Debug.println("\n\nSHA1withRSA Signature test");
        rsa = Signature.getInstance("SHA1withRSA");
Debug.println("using provider: " + rsa.getProvider().getName());
        rsa.initSign(privkey);
        rsa.update(messageBytes);
        sig = rsa.sign();
Debug.println("signature OK - length: " + sig.length);

Debug.println("\n\nSHA1withRSA Signature verify test");
        rsa.initVerify(pubkey);
        rsa.update(messageBytes);
if (rsa.verify(sig)) {
 Debug.println("signature verify OK");
} else {
 Debug.println("signature verify failed");
}
    }
}

/* */
