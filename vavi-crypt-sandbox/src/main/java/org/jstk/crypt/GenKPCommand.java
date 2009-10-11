/*
 * @(#) $Id: GenKPCommand.java,v 1.1.1.1 2003/10/05 18:39:17 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class GenKPCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("algorithm", "DSA");
        defaults.put("keysize", "512");
        defaults.put("action", "discard");
        defaults.put("file", "my.keypair");
    }

    public String briefDescription() {
        return "generates a key-pair ( for asymmetric algorithms )";
    }

    public String optionsDescription() {
        return "  -algorithm <alg>    : Algorithm for secret key generator.[" + defaults.get("algorithm") + "]\n" + "  -keysize <keysize>  : Key size (in bits).[" + defaults.get("keysize") + "]\n" + "  -action <action>    : what to do with the key?(print|save|discard).[" + defaults.get("action") + "]\n" + "  -file <filename>    : where to save the serialized key-pair?[" + defaults.get("file") + "]\n" + "  -provider <provider>: provider name for KeyPairGenerator.\n";
    }

    public String[] useForms() {
        String[] forms = {
            "[-algorithm <alg> -keysize <keysize>] [-action\n" + "\t(print|discard)] [-provider <provider>]", "[-algorithm <alg> -keysize <keysize>] [-action save\n" + "\t[-file <filename>]] [-provider <provider>]"
        };
        return forms;
    }

    public String[] sampleUses() {
        String[] uses = {
            "", "-algorithm DESede action -print", "-action save -file test1.key"
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

            KeyPairGenerator kpg;
            if (providerName != null)
                kpg = KeyPairGenerator.getInstance(algorithm, providerName);
            else
                kpg = KeyPairGenerator.getInstance(algorithm);

            kpg.initialize(keysize);
            KeyPair kp = kpg.generateKeyPair();

            if (action.equals("discard")) {
                return new JSTKResult(kp, true, "Public and Private key pair generated");
            } else if (action.equals("save")) { // Save the serialized object in a file
                String fileName = args.get("file");
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
                oos.writeObject(kp);
                oos.close();
                return new JSTKResult(kp, true, "KeyPair written to file: " + fileName);
            } else if (action.equals("print")) {
                return new JSTKResult(kp, true, KeyUtil.format(kp.getPublic(), "PublicKey") + KeyUtil.format(kp.getPrivate(), "PrivateKey"));
            }
            return new JSTKResult(null, false, "unknown action: " + action);
        } catch (Exception exc) {
            throw new JSTKException("GenKCommand.execute() failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        GenKPCommand genKPCmd = new GenKPCommand();
        JSTKResult result = (JSTKResult) genKPCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
