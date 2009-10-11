/*
 * @(#) $Id: SetupCACommand.java,v 1.2 2003/10/28 08:15:39 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net).
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the
 * root directory of the containing software.
 */
/* 10/27/03, Pankaj: Added storetype as an option. */

package org.jstk.cert;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.cert.ca.CADatabase;
import org.jstk.cert.ca.FileBasedCADatabaseParams;


public class SetupCACommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("cadir", "cadir");
        defaults.put("capath", "2");
        defaults.put("days", "1000");
        defaults.put("serial", "100");
        defaults.put("keyalg", "RSA");
        defaults.put("keysize", "2048");
        defaults.put("sigalg", "SHA1WithRSA");
        defaults.put("storetype", "JCEKS");
        defaults.put("dn", "CN=JSTK Test Root CA, OU=JSTK Operations, O=JSTK Inc, C=US");
    }

    public String briefDescription() {
        String briefDesc = "setup a filebased CA";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -cadir <cadir>      : Directory to store CA information.[" + defaults.get("cadir") + "]\n" + "  -dn <dn>            : CA distinguished name.[" + defaults.get("dn") + "]\n" + "  -capath <pathlen>   : path length.[" + defaults.get("capath") + "]\n" + "  -days <days>        : Validity period from the time of setup.[" + defaults.get("days") + "]\n" + "  -serial <serial>    : Serial no. of the CA certificate.[" + defaults.get("serial") + "]\n"
                             + "  -keyalg <keyalg>    : Algorithm for Key Pair generation (RSA|DSA).[" + defaults.get("keyalg") + "]\n" + "  -keysize <keysize>  : Size of key (no. of bits).[" + defaults.get("keysize") + "]\n" + "  -sigalg <sigalg>    : Signature Algorithm. Should match Key Algorithm.[" + defaults.get("sigalg") + "]\n" + "  -storetype <kstype> : KeyStore Type (JKS|JCEKS).[" + defaults.get("storetype") + "]\n" + "  -password <passwd>  : Password for CA keystore.\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[<options>] -password <passwd>"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "", "-cadir testca -days 3650 -password changeit"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String cadir = args.get("cadir");
            String dn = args.get("dn");
            String serialNo = args.get("serial");
            String keyAlg = args.get("keyalg");
            String sigAlg = args.get("sigalg");
            String password = args.get("password");
            String storeType = args.get("storetype");
            int pathLen = Integer.parseInt(args.get("capath"));
            int noDays = Integer.parseInt(args.get("days"));
            int keySize = Integer.parseInt(args.get("keysize"));

            if (password == null) {
                return new JSTKResult(null, false, "CA keystore password not specified. Use -password option.");
            }

            KeyPairGenerator kpg = KeyPairGenerator.getInstance(keyAlg);
            kpg.initialize(keySize);
            KeyPair kp = kpg.generateKeyPair();

            CertificateGenerator cg = new CertificateGenerator();

            cg.setBasicConstraints(true, pathLen);
            cg.setSigAlg(sigAlg);

            Certificate[] certs = null;
            X509Certificate cert = cg.generateSelfSignedCertificate(dn, kp, new BigInteger(serialNo), noDays);
            certs = new Certificate[1];
            certs[0] = cert;
            FileBasedCADatabaseParams fbParams = new FileBasedCADatabaseParams(cadir, certs, kp.getPrivate());
            fbParams.setPassword(password);
            fbParams.setStoreType(storeType);

            /*CADatabase cadb =*/ CADatabase.getInstance("file", fbParams);

            return new JSTKResult(null, true, "CA setup successful: " + cadir);
        } catch (Exception exc) {
            throw new JSTKException("SetupCACommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        SetupCACommand setupCACmd = new SetupCACommand();
        JSTKResult result = (JSTKResult) setupCACmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
