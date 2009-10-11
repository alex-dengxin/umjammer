/*
 * @(#) $Id: CryptCommand.java,v 1.1.1.1 2003/10/05 18:39:16 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKResult;


public class CryptCommand extends JSTKCommandAdapter {
    protected static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("keystore", "my.keystore");
        defaults.put("storepass", "changeit");
        defaults.put("kstype", "JCEKS");
        defaults.put("alias", "mykey");
        defaults.put("transform", "DES/CFB8/NoPadding");
        defaults.put("algorithm", "PBEWithMD5AndDES");
    }

    public String briefDescription() {
        return "encrypt/decrypt using password or key from file or keystore";
    }

    public String optionsDescription() {
        return "  -op (enc|dec)       : Cryptographic operation: encryption or decryption.\n" + "  -infile <infile>    : Input data file.\n" + "  -outfile <outfile>  : Output data file.\n" + "  -password <password>: Password for Password Based Encryption(PBE).\n" + "  -transform <transform>: Cipher transform(<alg>/<mode>/<padding>).[" + defaults.get("transform") + "]\n" + "  -algorithm <algo>   : Algo. for password based encryption.[" + defaults.get("algorithm") + "]\n"
               + "  -stream             : use CipherInputStream or CipherOutputStream.\n" + "  -iv <ivbytes>       : initialization vector bytes.\n" + "  -keyfile <keyfile>  : File having the serialized key.\n" + "  -keystore <keystore>: the keystore.[" + defaults.get("keystore") + "]\n" + "  -storepass <storepass>: Password for keystore.[" + defaults.get("storepass") + "]\n" + "  -kstype <kstype>    : the keystore type.[" + defaults.get("kstype") + "]\n"
               + "  -alias <alias>      : alias to access the key in the keystore.[" + defaults.get("alias") + "]\n" + "  -keypass <keypass>  : Password for key in the keystore.\n" + "  -provider <provider>: provider name for KeyStore and Cipher.\n" + "\n" + "  <<keyinfo>> := (-keyfile <keyfile>|[-keystore <keystore>] [-storepass\n" + "      <storepass>] [-kstype <kstype>] [-alias <alias>] [-keypass <keypass>])\n";
    }

    public String[] useForms() {
        String[] forms = {
            "-op (enc|dec) -infile <infile> -outfile <outfile> -password\n" + "\t<password> [-algorithm <algorithm>] [-stream]", "-op (enc|dec) -infile <infile> -outfile <outfile> <<keyinfo>>\n" + "\t[-stream] [-iv <ivbytes>] [-transform <transform>] [-provider <provider>]"
        };
        return forms;
    }

    public String[] sampleUses() {
        String[] uses = {
            "-op enc -infile clear.data -outfile enc.data -password changeit", "-op enc -infile clear.data -outfile enc.data -password changeit -stream", "-op dec -infile enc.data -outfile dec.data -keyfile my.secretkey -iv 88888888", "-op enc -infile clear.data -outfile enc.data -transform RSA/ECB/PKCS#1",
        };
        return uses;
    }

    protected Cipher initCipher(JSTKArgs args, int cipherMode) throws Exception {
        String password = args.get("password");
//      String keyfile = args.get("keyfile");
        String providerName = args.get("provider");
        Key key = null;
        if (password != null) {
            byte[] salt = {
                (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99
            };
            int count = 20;
            String algorithm = args.get("algorithm");
            PBEParameterSpec paramSpec = new PBEParameterSpec(salt, count);
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance(algorithm);
            key = keyFac.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(cipherMode, key, paramSpec);
            return cipher;
        } else { // Key file specified
            key = KeyUtil.getKey(args, Key.class); // Get key from keyfile or keystore

            // KeyUtil.getKey() would return PublicKey if keyfile contains a keypair.
            if (cipherMode == Cipher.DECRYPT_MODE && (key instanceof PublicKey)) {
                key = KeyUtil.getKey(args, PrivateKey.class); // Get key from keyfile or keystore
            }
        }

        String transform = args.get("transform");
        Cipher cipher;
        if (providerName != null)
            cipher = Cipher.getInstance(transform, providerName);
        else
            cipher = Cipher.getInstance(transform);

        String ivString = args.get("iv");
        if (ivString != null) {
            IvParameterSpec ips = new IvParameterSpec(ivString.getBytes());
            cipher.init(cipherMode, key, ips);
        } else {
            cipher.init(cipherMode, key);
        }
        return cipher;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);

            boolean stream = Boolean.valueOf(args.get("stream")).booleanValue();

            int cipherMode;
            String cryptOp = args.get("op");
            if (cryptOp == null) {
                return new JSTKResult(null, false, "no cryptographic operation specified");
            } else if (cryptOp.equals("enc")) {
                cipherMode = Cipher.ENCRYPT_MODE;
            } else if (cryptOp.equals("dec")) {
                cipherMode = Cipher.DECRYPT_MODE;
            } else {
                return new JSTKResult(null, false, "unknown cryptographic operation: " + cryptOp);
            }

            String infileName = args.get("infile");
            if (infileName == null)
                return new JSTKResult(null, false, "no input file specified");

            String outfileName = args.get("outfile");
            if (outfileName == null)
                return new JSTKResult(null, false, "no output file specified");

            Cipher cipher = null;
            try {
                cipher = initCipher(args, cipherMode);
            } catch (Exception e) {
                return new JSTKResult(null, false, e.getMessage());
            }

            FileInputStream fis = new FileInputStream(infileName);
            FileOutputStream fos = new FileOutputStream(outfileName);

            if (stream) { // Use CipherStream APIs
                InputStream is = null;
                OutputStream os = null;

                if (cipherMode == Cipher.DECRYPT_MODE) {
                    is = new CipherInputStream(fis, cipher);
                    os = fos;
                } else {
                    is = fis;
                    os = new CipherOutputStream(fos, cipher);
                }

                // Operate on 1KB chunks.
                perfData.updateBegin();
                byte[] buf = new byte[1024];
                int n, tot = 0;
                while ((n = is.read(buf)) > 0) {
                    os.write(buf, 0, n);
                    tot += n;
                }
                perfData.updateEnd(tot);
                is.close();
                os.close();
            } else {
                // Read the file in a memory buffer first.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n;
                while ((n = fis.read(buf)) > 0)
                    baos.write(buf, 0, n);
                fis.close();
                byte[] ibuf = baos.toByteArray();

                perfData.updateBegin();
                byte[] obuf = cipher.doFinal(ibuf);
                perfData.updateEnd(ibuf.length);

                fos.write(obuf);
                fos.close();
            }

            return new JSTKResult(null, true, cryptOp + "rypted file \"" + infileName + "\" to \"" + outfileName + "\"");
        } catch (Exception exc) {
            throw new JSTKException("CryptCommand.execute() failed", exc);
        }
    }
}
