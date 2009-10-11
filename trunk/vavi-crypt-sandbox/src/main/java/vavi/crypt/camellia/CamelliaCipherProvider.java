/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.crypt.camellia;

import java.security.Key;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;


/**
 * CamelliaCipherProvider.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 06xxxx nsano initial version <br>
 */
public final class CamelliaCipherProvider extends Provider {

    /** */
    public CamelliaCipherProvider() {
        super("CamelliaCipher", 1.0, "CamelliaCipherProvider implements NTT Camellia Decryption");
        put("Cipher.Camellia", "vavi.crypto.camellia.Camellia");
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        Security.addProvider(new CamelliaCipherProvider());
        Cipher cipher = Cipher.getInstance("Camellia", "CamelliaCipher");
        SecureRandom random = new SecureRandom();
        Key key = new CamelliaCipher.CamelliaKey("sanonaohide01234");
        cipher.init(Cipher.ENCRYPT_MODE, key, random);
        String plain = "ñ{ì˙ÇÕê∞ìVÇ»ÇËÅB";
        byte[] input = plain.getBytes("UTF-8");
        byte[] encrypted = cipher.doFinal(input, 0, input.length);
        cipher.init(Cipher.DECRYPT_MODE, key, random);
        byte[] decrypted = cipher.doFinal(encrypted, 0, encrypted.length);
        System.err.println(new String(decrypted, "UTF-8"));
    }
}

/* */
