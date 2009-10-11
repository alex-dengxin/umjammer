/*
 * @(#) $Id: SignCommand.java,v 1.1.1.1 2003/10/05 18:39:18 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.JSTKUtil;


public class SignCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("algorithm", "SHAwithDSA");
        defaults.put("keystore", "my.keystore");
        defaults.put("storepass", "changeit");
        defaults.put("kstype", "JCEKS");
        defaults.put("alias", "mykey");
    }

    public String briefDescription() {
        String briefDesc = "creates or verifies digital signature";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -verify             : verify the signature.\n" + "  -infile <infile>    : message file.\n" + "  -sigfile <sigfile>  : signature file.\n" + "  -sigbytes <sigbytes>: signature bytes in hexadecimal.\n" + "  -algorithm <alg>    : algorithm for signature generation.[" + defaults.get("algorithm") + "]\n" + "  -keyfile <keyfile>  : File having the serialized key.\n" + "  -keystore <keystore>: the keystore.[" + defaults.get("keystore") + "]\n"
                             + "  -storepass <storepass>: Password for keystore.[" + defaults.get("storepass") + "]\n" + "  -kstype <kstype>    : the keystore type.[" + defaults.get("kstype") + "]\n" + "  -alias <alias>      : alias to access the key in the keystore.[" + defaults.get("alias") + "]\n" + "  -keypass <keypass>  : Password for key in the keystore.\n" + "  -provider <provider>: provider name for MessageDigest.\n" + "\n"
                             + "  <<keyinfo>> := (-keyfile <keyfile>|[-keystore <keystore>] [-storepass\n" + "      <storepass>] [-kstype <kstype>] [-alias <alias>] [-keypass <keypass>])\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "-infile <infile> [-sigfile <sigfile>] <<keyinfo>>\n" + "\t[-algorithm <alg>] [-provider <provider>]", "-verify -infile <infile> (-sigfile <sigfile> | -sigbytes\n" + "\t<sigbytes>) <<keyinfo>> [-algorithm <alg>] [-provider <provider>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "-infile test.txt -keyfile prv.key", "-verify -infile test.txt -keyfile prv.key -sigbytes <...>", "-infile test.txt -sigfile test.sig", "-verify -infile test.txt -sigfile test.sig"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);

            String providerName = args.get("provider");
            String algorithm = args.get("algorithm");
            boolean verify = Boolean.valueOf(args.get("verify")).booleanValue();
            String sigString = args.get("sigbytes");
            String infile = args.get("infile");
            String sigfile = args.get("sigfile");

            // Do the validations on arguments
            if (infile == null)
                return new JSTKResult(null, false, "no file to be signed");

            Signature sig = null;
            if (providerName != null)
                sig = Signature.getInstance(algorithm, providerName);
            else
                sig = Signature.getInstance(algorithm);

            byte[] bytes = JSTKUtil.bytesFromFile(infile);

            if (verify) {
                PublicKey key;
                try {
                    key = (PublicKey) KeyUtil.getKey(args, PublicKey.class); // Get key from keyfile or keystore
                } catch (Exception e) {
                    return new JSTKResult(null, false, e.getMessage());
                }
                if (sigString != null && sigfile != null)
                    return new JSTKResult(null, false, "too many signatures to verify against");

                byte[] sigbytesV = null; // holds the signature bytes supplied for verification.
                if (sigString != null) {
                    sigbytesV = JSTKUtil.bytesFromHexString(sigString);
                } else if (sigfile != null) {
                    sigbytesV = JSTKUtil.bytesFromFile(sigfile);
                } else {
                    return new JSTKResult(null, false, "no signature to verify against");
                }
                sig.initVerify(key);
                perfData.updateBegin();
                sig.update(bytes);
                boolean verified = sig.verify(sigbytesV);
                perfData.updateEnd(bytes.length);

                if (verified)
                    return new JSTKResult(Boolean.TRUE, true, "verification succeeded");
                else
                    return new JSTKResult(Boolean.FALSE, true, "verification failed");
            } else {
                PrivateKey key;
                try {
                    key = (PrivateKey) KeyUtil.getKey(args, PrivateKey.class); // Get key from keyfile or keystore
                } catch (Exception e) {
                    return new JSTKResult(null, false, e.getMessage());
                }

                sig.initSign(key);
                perfData.updateBegin();
                sig.update(bytes);
                byte[] sigbytes = sig.sign();
                perfData.updateEnd(bytes.length);
                if (sigfile != null) {
                    JSTKUtil.bytesToFile(sigbytes, sigfile);
                    return new JSTKResult(sigbytes, true, "signature written to file: " + sigfile);
                } else {
                    String hexString = JSTKUtil.hexStringFromBytes(sigbytes);
                    return new JSTKResult(sigbytes, true, "Signature (Hex)::\n" + hexString);
                }
            }
        } catch (Exception exc) {
            throw new JSTKException("DigestCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        SignCommand signCmd = new SignCommand();
        JSTKResult result = (JSTKResult) signCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
