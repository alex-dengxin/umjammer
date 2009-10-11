/*
 * @(#) $Id: MacCommand.java,v 1.1.1.1 2003/10/05 18:39:18 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.JSTKUtil;


public class MacCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("algorithm", "HmacSHA1");
        defaults.put("keystore", "my.keystore");
        defaults.put("storepass", "changeit");
        defaults.put("kstype", "JCEKS");
        defaults.put("alias", "mykey");
    }

    public String briefDescription() {
        String briefDesc = "creates or verifies message authentication code (mac)";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -verify             : verify the mac.\n" + "  -infile <infile>    : message file.\n" + "  -macfile <macfile>  : mac file.\n" + "  -macbytes <macbytes>: mac bytes in hexadecimal.\n" + "  -algorithm <alg>    : algorithm for mac generation.[" + defaults.get("algorithm") + "]\n" + "  -keyfile <keyfile>  : File having the serialized key.\n" + "  -keystore <keystore>: the keystore.[" + defaults.get("keystore") + "]\n" + "  -storepass <storepass>: Password for keystore.["
                             + defaults.get("storepass") + "]\n" + "  -kstype <kstype>    : the keystore type.[" + defaults.get("kstype") + "]\n" + "  -alias <alias>      : alias to access the key in the keystore.[" + defaults.get("alias") + "]\n" + "  -keypass <keypass>  : Password for key in the keystore.\n" + "  -provider <provider>: provider name for MessageDigest.\n" + "\n" + "  <<keyinfo>> := (-keyfile <keyfile>|[-keystore <keystore>] [-storepass\n"
                             + "      <storepass>] [-kstype <kstype>] [-alias <alias>] [-keypass <keypass>])\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "-infile <infile> [-macfile <macfile>] <<keyinfo>>\n" + "\t[-algorithm <alg>] [-provider <provider>]", "-verify -infile <infile> (-macfile <macfile> | -macbytes\n" + "\t<macbytes>) <<keyinfo>> [-algorithm <alg>] [-provider <provider>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "-infile test.txt -keyfile prv.key", "-verify -infile test.txt -keyfile prv.key -macbytes <...>", "-infile test.txt -macfile test.mac", "-verify -infile test.txt -macfile test.mac"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);

            String providerName = args.get("provider");
            String algorithm = args.get("algorithm");
            boolean verify = Boolean.valueOf(args.get("verify")).booleanValue();
            String macString = args.get("macbytes");
            String infile = args.get("infile");
            String macfile = args.get("macfile");

            // Do the validations on arguments
            if (infile == null)
                return new JSTKResult(null, false, "no file to be maced");

            Mac mac = null;
            if (providerName != null)
                mac = Mac.getInstance(algorithm, providerName);
            else
                mac = Mac.getInstance(algorithm);

            byte[] bytes = JSTKUtil.bytesFromFile(infile);
            SecretKey key;
            try {
                key = (SecretKey) KeyUtil.getKey(args, SecretKey.class); // Get key from keyfile or keystore
            } catch (Exception e) {
                return new JSTKResult(null, false, e.getMessage());
            }
            mac.init(key);
            mac.update(bytes);
            byte[] macbytes = mac.doFinal();

            if (verify) {
                if (macString != null && macfile != null)
                    return new JSTKResult(null, false, "too many macs to verify against");

                byte[] macbytesV = null; // holds the mac bytes supplied for verification.
                if (macString != null) {
                    macbytesV = JSTKUtil.bytesFromHexString(macString);
                } else if (macfile != null) {
                    macbytesV = JSTKUtil.bytesFromFile(macfile);
                } else {
                    return new JSTKResult(null, false, "no mac to verify against");
                }

                if (JSTKUtil.equals(macbytes, macbytesV))
                    return new JSTKResult(Boolean.TRUE, true, "verification succeeded");
                else
                    return new JSTKResult(Boolean.FALSE, true, "verification failed");
            } else {
                if (macfile != null) {
                    JSTKUtil.bytesToFile(macbytes, macfile);
                    return new JSTKResult(macbytes, true, "mac written to file: " + macfile);
                } else {
                    String hexString = JSTKUtil.hexStringFromBytes(macbytes);
                    return new JSTKResult(macbytes, true, "Mac (Hex)::\n" + hexString);
                }
            }
        } catch (Exception exc) {
            throw new JSTKException("DigestCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        SignCommand macCmd = new SignCommand();
        JSTKResult result = (JSTKResult) macCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
