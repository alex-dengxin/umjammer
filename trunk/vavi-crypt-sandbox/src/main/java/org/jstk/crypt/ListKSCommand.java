/*
 * @(#) $Id: ListKSCommand.java,v 1.1.1.1 2003/10/05 18:39:17 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class ListKSCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("kstype", "JCEKS");
        defaults.put("keystore", "my.keystore");
        defaults.put("storepass", "changeit");
    }

    public String briefDescription() {
        return "lists keystore entries";
    }

    public String optionsDescription() {
        return "  -keystore <keystore>: the keystore.[" + defaults.get("keystore") + "]\n" + "  -storepass <storepass>: Password for keystore.[" + defaults.get("storepass") + "]\n" + "  -kstype <type>        : the keystore type.[" + defaults.get("type") + "]\n" + "  -alias <alias>      : alias to access the key in the keystore.[" + defaults.get("alias") + "]\n" + "  -keypass <keypass>  : Password for key in the keystore.[" + defaults.get("keypass") + "]\n"
               + "  -provider <provider>: provider name for KeyStore.\n";

    }

    public String[] useForms() {
        String[] forms = {
            "[-keystore <keystore>] [-kstype (JCEKS|JKS|PKCS12)]\n" + "\t[-storepass <storepass>] [-alias <alias>] [-keypass <keypass>]\n" + "\t[-provider <provider>]"
        };
        return forms;
    }

    public String[] sampleUses() {
        String[] uses = {
            "", "-keystore test.ks -storepass testpass", "-alias test.key"
        };
        return uses;
    }

    private String formatEntry(KeyStore ks, String alias, String keypass) throws Exception {
        StringBuffer sb = new StringBuffer();

        sb.append("KeyStore entry \"" + alias + "\": ");
        if (ks.isKeyEntry(alias)) {
            try {
                sb.append("Key entry.\n");
                Key key = ks.getKey(alias, keypass.toCharArray());
                String keytype = (key instanceof SecretKey ? "SecretKey" : (key instanceof PrivateKey ? "PrivateKey" : "PublicKey"));
                sb.append(KeyUtil.format(key, keytype));
            } catch (UnrecoverableKeyException e) {
                sb.append("Cannot Receover Key from KeyStore.\n");
            }
        } else {
            sb.append("Certificate Entry.\n");
        }
        return sb.toString();
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        StringBuffer sb = new StringBuffer();
        try {
            args.setDefaults(defaults);
            String keystore = args.get("keystore");
            String storepass = args.get("storepass");
            String type = args.get("kstype");
            String providerName = args.get("provider");
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

            if (alias != null) {
                if (ks.containsAlias(alias)) {
                    sb.append(formatEntry(ks, alias, keypass));
                } else {
                    sb.append("No such Entry: " + alias + ".\n");
                }
            } else {
                Enumeration<String> enumeration = ks.aliases();

                while (enumeration.hasMoreElements()) {
                    String alias0 = enumeration.nextElement();
                    sb.append(formatEntry(ks, alias0, keypass));
                }
            }
        } catch (Exception exc) {
            throw new JSTKException("ListKSCommand.execute() failed", exc);
        }
        return new JSTKResult(null, true, sb.toString());
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        GenKCommand genKCmd = new GenKCommand();
        JSTKResult result = (JSTKResult) genKCmd.execute(opts);
        System.out.print(result);
        System.out.flush();
    }
}
