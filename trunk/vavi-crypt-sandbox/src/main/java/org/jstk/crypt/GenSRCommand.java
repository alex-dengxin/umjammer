/*
 * @(#) $Id: GenSRCommand.java,v 1.1.1.1 2003/10/05 18:39:17 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.JSTKUtil;


public class GenSRCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("algorithm", "SHA1PRNG");
        defaults.put("size", "16");
        defaults.put("seed", "1234567890");
        defaults.put("num", "1");
    }

    public String briefDescription() {
        return "generates a secret key ( for symmetric algorithms )";
    }

    public String[] useForms() {
        String[] forms = {
            "[-algorithm <alg>] [-size <size>] [-seed <seed>]\n" + "\t[-num <num>] [-pprovider <provider>]\n"
        };
        return forms;
    }

    public String optionsDescription() {
        return "  -algorithm <alg>    : Algorithm for secure random generator.[" + defaults.get("algorithm") + "]\n" + "  -size <size>        : no. of random bytes to be generated.[" + defaults.get("size") + "]\n" + "  -seed <seed>        : seed to the random no. generator ( a long ).[" + defaults.get("seed") + "]\n" + "  -num <num>          : how many random nos. to generate.[" + defaults.get("num") + "]\n" + "  -provider <provider>: provider name for SecureRandom.\n";
    }

    public String[] sampleUses() {
        String[] uses = {
            "", "-size 20", "-seed 9876543210", "-num 10"
        };
        return uses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        StringBuffer sb = new StringBuffer();
        try {
            args.setDefaults(defaults);
            String providerName = args.get("provider");
            String algorithm = args.get("algorithm");
            String sizeString = args.get("size");
            int size = Integer.parseInt(sizeString);
            String seedString = args.get("seed");
            long seed = Long.parseLong(seedString);
            String numString = args.get("num");
            int num = Integer.parseInt(numString);

            SecureRandom sr = null;
            if (providerName != null) {
                sr = SecureRandom.getInstance(algorithm, providerName);
            } else {
                sr = SecureRandom.getInstance(algorithm);
            }
            sr.setSeed(seed);
            byte[] randomBytes = new byte[size];

            sb.append("Generated Random Bytes: \n");
            for (int i = 0; i < num; i++) {
                sr.nextBytes(randomBytes);
                sb.append("[" + i + "]: " + JSTKUtil.hexStringFromBytes(randomBytes) + "\n");
            }
        } catch (Exception exc) {
            throw new JSTKException("GenKCommand.execute() failed", exc);
        }
        return new JSTKResult(null, true, sb.toString());
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        GenSRCommand genSRCmd = new GenSRCommand();
        JSTKResult result = (JSTKResult) genSRCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
