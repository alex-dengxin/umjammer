/*
 * @(#) $Id: DigestCommand.java,v 1.1.1.1 2003/10/05 18:39:16 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.JSTKUtil;


public class DigestCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("algorithm", "SHA");
    }

    public String briefDescription() {
        String briefDesc = "creates or verifies message digest";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -verify             : match the digest of the infile with given digest.\n" + "  -infile <infile>    : message file.\n" + "  -mdfile <mdfile>    : message digest file.\n" + "  -mdbytes <mdbytes>  : message digest bytes in hexadecimal.\n" + "  -algorithm <alg>    : algorithm for message digest computation.[" + defaults.get("algorithm") + "]\n" + "  -provider <provider>: provider name for MessageDigest.\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "-infile <infile> [-mdfile <mdfile>]\n" + "\t[-algorithm <alg>] [-provider <provider>]", "-verify -infile <infile> (-mdfile <dfile> | -mdbytes\n" + "\t<mdbytes>) [-algorithm <alg>] [-provider <provider>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "-infile test.txt", "-infile test.txt -mdfile test.md", "-verify -infile test.txt -mdfile test.md", "-verify -infile test.txt -mdbytes <...>"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);

            String providerName = args.get("provider");
            String algorithm = args.get("algorithm");
            boolean verify = Boolean.valueOf(args.get("verify")).booleanValue();
//            boolean stream = Boolean.valueOf(args.get("stream")).booleanValue();
            String mdString = args.get("mdbytes");
            String infile = args.get("infile");
            String mdfile = args.get("mdfile");

            // Do the validations on arguments
            if (infile == null)
                return new JSTKResult(null, false, "no message file specified");

            byte[] mdbytesV = null; // holds the digest bytes supplied for verification.

            if (verify) {
                if (mdString != null && mdfile != null)
                    return new JSTKResult(null, false, "too many digests to verify against");

                if (mdString != null) {
                    mdbytesV = JSTKUtil.bytesFromHexString(mdString);
                } else if (mdfile != null) {
                    mdbytesV = JSTKUtil.bytesFromFile(mdfile);
                } else {
                    return new JSTKResult(null, false, "no digest to verify against");
                }
            }

            MessageDigest md = null;
            if (providerName != null)
                md = MessageDigest.getInstance(algorithm, providerName);
            else
                md = MessageDigest.getInstance(algorithm);

            byte[] bytes = JSTKUtil.bytesFromFile(infile);
            md.update(bytes);
            byte[] mdbytes = md.digest();
            if (verify) {
                if (MessageDigest.isEqual(mdbytes, mdbytesV))
                    return new JSTKResult(Boolean.TRUE, true, "verification succeeded");
                else
                    return new JSTKResult(Boolean.FALSE, true, "verification failed");
            } else {
                if (mdfile != null) {
                    JSTKUtil.bytesToFile(mdbytes, mdfile);
                    return new JSTKResult(mdbytes, true, "digest written to file: " + mdfile);
                } else {
                    String hexString = JSTKUtil.hexStringFromBytes(mdbytes);
                    return new JSTKResult(mdbytes, true, "Message Digest (Hex)::\n" + hexString);
                }
            }
        } catch (Exception exc) {
            throw new JSTKException("DigestCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        DigestCommand digestCmd = new DigestCommand();
        JSTKResult result = (JSTKResult) digestCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
