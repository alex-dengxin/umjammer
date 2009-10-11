/*
 * @(#) $Id: GenKCommand.java,v 1.1.1.1 2003/10/05 18:39:17 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class GenKCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("algorithm", "DES");
        defaults.put("keysize", "56");
        defaults.put("action", "discard");
        defaults.put("file", "my.secretkey");
        defaults.put("keystore", "my.keystore");
        defaults.put("storepass", "changeit");
        defaults.put("kstype", "JCEKS");
        defaults.put("alias", "mykey");
    }

    public String briefDescription() {
        return "generates a secret key ( for symmetric algorithms )";
    }

    public String[] useForms() {
        String[] forms = {
            "[-algorithm <alg> -keysize <keysize>] [-action\n" + "\t(print|discard)] [-provider <provider>]", "[-algorithm <alg> -keysize <keysize>] [-action save\n" + "\t[-file <filename>]] [-provider <provider>]", "[-algorithm <alg> -keysize <keysize>] [-action store\n" + "\t[-keystore <keystore>] [-kstype (JCEKS|JKS)] [-storepass <storepass>]\n" + "\t[-alias <alias>] [-keypass <keypass>]] [-provider <provider>]"
        };
        return forms;
    }

    public String optionsDescription() {
        return "  -action <action>    : what to do with the key?(print|store|save|discard).[" + defaults.get("action") + "]\n" + "  -file <filename>    : where to save the serialized key?[" + defaults.get("filename") + "]\n" + "  -keystore <keystore>: where to store the key?[" + defaults.get("keystore") + "]\n" + "  -kstype <kstype>    : keystore type.[" + defaults.get("kstype") + "]\n" + "  -storepass <storepass>: Password for keystore.[" + defaults.get("storepass") + "]\n"
               + "  -alias <alias>      : alias to access the key in the keystore.[" + defaults.get("alias") + "]\n" + "  -keypass <keypass>  : Password for key in the keystore.[" + defaults.get("keypass") + "]\n" + "  -keysize <keysize>  : Key size (in bits).[" + defaults.get("keysize") + "]\n" + "  -algorithm <alg>    : Algorithm for secret key generator.[" + defaults.get("algorithm") + "]\n" + "  -provider <provider>: provider name for KeyGenerator.\n";
    }

    public String[] sampleUses() {
        String[] uses = {
            "", "-algorithm DESede -keysize 112 -action print", "-action store -keystore test.ks -storepass changeit -alias testkey1", "-action save -file test1.key"
        };
        return uses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String providerName = args.get("provider");
            String algorithm = args.get("algorithm");
            String keysizeString = args.get("keysize");
            int keysize = Integer.parseInt(keysizeString);
            String action = args.get("action");

            KeyGenerator kg;
            if (providerName != null)
                kg = KeyGenerator.getInstance(algorithm, providerName);
            else
                kg = KeyGenerator.getInstance(algorithm);

            kg.init(keysize, new SecureRandom());
            SecretKey key = kg.generateKey();

            if (action.equals("discard")) {
                return new JSTKResult(key, true, "Secret Key generated");
            } else if (action.equals("save")) { // Save the serialized object in a file
                String fileName = args.get("file");
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
                oos.writeObject(key);
                oos.close();
                return new JSTKResult(key, true, "SecretKey written to file: " + fileName);
            } else if (action.equals("store")) { // Store the key in a keystore
                String keystore = args.get("keystore");
                String storepass = args.get("storepass");
                String kstype = args.get("kstype");

                String keypass = args.get("keypass");
                if (keypass == null)
                    keypass = storepass;
                String alias = args.get("alias");

                KeyStore ks;
                if (providerName != null)
                    ks = KeyStore.getInstance(kstype, providerName);
                else
                    ks = KeyStore.getInstance(kstype);

                FileInputStream fis;
                try {
                    fis = new FileInputStream(keystore);
                    ks.load(fis, storepass.toCharArray());
                    fis.close();
                } catch (IOException ioe) { // File cannot be open for reading.
                    ks.load(null, storepass.toCharArray());
                }

                ks.setKeyEntry(alias, key, keypass.toCharArray(), null);
                FileOutputStream fos = new FileOutputStream(keystore);
                ks.store(fos, storepass.toCharArray());
                return new JSTKResult(key, true, "SecretKey stored to keystore \"" + keystore + "\" with alias: " + alias);
            } else if (action.equals("print")) {
                return new JSTKResult(key, true, KeyUtil.format(key, "SecretKey"));
            }
            return new JSTKResult(null, false, "unknown action: " + action);
        } catch (Exception exc) {
            throw new JSTKException("GenKCommand.execute() failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        GenKCommand genKCmd = new GenKCommand();
        JSTKResult result = (JSTKResult) genKCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
