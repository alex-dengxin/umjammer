/*
 * @(#) $Id: KeyUtil.java,v 1.1.1.1 2003/10/05 18:39:17 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.security.*;
import javax.crypto.SecretKey;
import java.io.*;
import org.jstk.JSTKUtil;
import org.jstk.JSTKArgs;


public class KeyUtil {
    public static String format(Key key, String label) {
        StringBuffer sb = new StringBuffer();
        sb.append(label + "::\n");
        sb.append("alg = " + key.getAlgorithm() + ", fmt = " + key.getFormat() + ", encoded content = \n");
        sb.append(JSTKUtil.hexStringFromBytes(key.getEncoded()) + "\n");
        return sb.toString();
    }

    public static void printKey(Key key, String label) {
        System.out.println(format(key, label));
    }

    public static Key getKey(JSTKArgs args, Class<?> keyClass) throws Exception {
        String keyfile = args.get("keyfile");
        Object obj = null;
        if (keyfile != null) { // Key file specified
            FileInputStream fis = new FileInputStream(keyfile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            obj = ois.readObject();
        } else { // Look in the keystore
            String providerName = args.get("provider");
            String keystore = args.get("keystore");
            String storepass = args.get("storepass");
            String type = args.get("kstype");
            String keypass = args.get("keypass");
            if (keypass == null)
                keypass = storepass;
            String alias = args.get("alias");

            FileInputStream fis = new FileInputStream(keystore);

            KeyStore ks;
            if (providerName != null)
                ks = KeyStore.getInstance(type, providerName);
            else
                ks = KeyStore.getInstance(type);

            ks.load(fis, storepass.toCharArray());
            fis.close();
            if (ks.isKeyEntry(alias)) {
                Key key = ks.getKey(alias, keypass.toCharArray());
                if (key instanceof SecretKey) {
                    obj = key;
                } else if (key instanceof PrivateKey) {
                    PrivateKey prvKey = (PrivateKey) key;
                    java.security.cert.Certificate cert = ks.getCertificate(alias);
                    PublicKey pubKey = null;
                    if (cert != null) {
                        pubKey = cert.getPublicKey();
                        obj = new KeyPair(pubKey, prvKey);
                    } else {
                        obj = prvKey;
                    }
                }
            } else if (ks.isCertificateEntry(alias)) {
                java.security.cert.Certificate cert = ks.getCertificate(alias);
                obj = cert.getPublicKey();
            }
        }
        if (obj == null) {
            throw new Exception("Key not found");
        } else if (obj instanceof Key) {
            if (keyClass.isInstance(obj))
                return (Key) obj;
            else
                throw new Exception("unknown object in place of key");
        }
        if (obj instanceof KeyPair) {
            PublicKey pubK = ((KeyPair) obj).getPublic();
            PrivateKey prvK = ((KeyPair) obj).getPrivate();
            if (keyClass.isInstance(pubK))
                return pubK;
            else if (keyClass.isInstance(prvK))
                return prvK;
            else
                throw new Exception("key type mismatch");
        } else {
            throw new Exception("unexpected object in keyfile: " + obj.getClass().getName());
        }
    }

    public static java.security.cert.Certificate getCertificate(JSTKArgs args) throws Exception {
        // Look in the keystore
        String providerName = args.get("provider");
        String keystore = args.get("keystore");
        String storepass = args.get("storepass");
        String type = args.get("kstype");
        String keypass = args.get("keypass");
        if (keypass == null)
            keypass = storepass;
        String alias = args.get("alias");

        FileInputStream fis = new FileInputStream(keystore);

        KeyStore ks;
        if (providerName != null)
            ks = KeyStore.getInstance(type, providerName);
        else
            ks = KeyStore.getInstance(type);

        ks.load(fis, storepass.toCharArray());
        fis.close();
        java.security.cert.Certificate cert = ks.getCertificate(alias);
        return cert;
    }

    public static KeyPair getKeyPair(JSTKArgs args) throws Exception {
        String keyfile = args.get("keyfile");
        Object obj = null;
        if (keyfile != null) { // Key file specified
            FileInputStream fis = new FileInputStream(keyfile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            obj = ois.readObject();
        } else { // Look in the keystore
            String providerName = args.get("provider");
            String keystore = args.get("keystore");
            String storepass = args.get("storepass");
            String type = args.get("kstype");
            String keypass = args.get("keypass");
            if (keypass == null)
                keypass = storepass;
            String alias = args.get("alias");

            FileInputStream fis = new FileInputStream(keystore);

            KeyStore ks;
            if (providerName != null)
                ks = KeyStore.getInstance(type, providerName);
            else
                ks = KeyStore.getInstance(type);

            ks.load(fis, storepass.toCharArray());
            fis.close();
            if (ks.isKeyEntry(alias)) {
                obj = ks.getKey(alias, keypass.toCharArray());
                throw new Exception("found key entry. rest not implemented.");
            } else {
                throw new Exception("key entry expected. found certificate entry.");
            }
        }
        if (!(obj instanceof KeyPair)) {
            throw new Exception("unexpected object in keyfile: " + obj.getClass().getName());
        }
        return (KeyPair) obj;
    }
}
