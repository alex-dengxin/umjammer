/*
 * @(#) $Id: ExportCommand.java,v 1.1.1.1 2003/10/05 18:39:17 pankaj_kumar Exp $
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
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKResult;
import org.jstk.pem.PEMData;


public class ExportCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();

    private boolean pkcs12Output = false;
    static {
        defaults.put("kstype", "JKS");
        defaults.put("keystore", "my.keystore");
        defaults.put("storepass", "changeit");
        defaults.put("outform", "PEM");
    }

    public String briefDescription() {
        return "exports key or cert entries from keystore to files";
    }

    public String optionsDescription() {
        return "  -keystore <keystore>: the keystore.[" + defaults.get("keystore") + "]\n" + "  -storepass <storepass>: Password for keystore.[" + defaults.get("storepass") + "]\n" + "  -kstype <type>        : the keystore type.[" + defaults.get("type") + "]\n" + "  -alias <alias>       : alias to access the key in the keystore.[" + defaults.get("alias") + "]\n" + "  -keypass <keypass>   : Password for key in the keystore.[" + defaults.get("keypass") + "]\n"
               + "  -outform <outform>   : Format of exported data(DER|PEM|PKCS12).[" + defaults.get("keypass") + "]\n" + "  -provider <provider> : provider name for KeyStore.\n";
    }

    public String[] useForms() {
        String[] forms = {
            "[-keystore <keystore>] [-kstype (JCEKS|JKS|PKCS12)]\n" + "\t[-storepass <storepass>] [-alias <alias>] [-keypass <keypass>]\n" + "\t[-outform <outform>][-provider <provider>]"
        };
        return forms;
    }

    public String[] sampleUses() {
        String[] uses = {
            "", "-keystore test.ks -storepass testpass", "-alias test.key -outform PKCS12"
        };
        return uses;
    }

    private void exportKey(Key key, String alias, StringBuffer sb, String outform) throws Exception {
        String keytype = (key instanceof SecretKey ? "SecretKey" : (key instanceof PrivateKey ? "PrivateKey" : "PublicKey"));
        String keyfile = alias + (outform.equalsIgnoreCase("PEM") ? ".pem" : ".key.der");
        FileOutputStream fos = new FileOutputStream(keyfile);
        byte[] derEncodedKey = key.getEncoded();
        if (outform.equalsIgnoreCase("PEM")) {
            PEMData pemData = new PEMData(derEncodedKey);
            String base64Data = pemData.encode();
            String preEB = "-----BEGIN PRIVATE KEY-----\n";
            String postEB = "\n-----END PRIVATE KEY-----\n";
            fos.write(preEB.getBytes());
            fos.write(base64Data.getBytes());
            fos.write(postEB.getBytes());
        } else {
            fos.write(derEncodedKey);
        }
        fos.close();
        sb.append("Exported " + keytype + " to file: " + keyfile + "\n");
    }

    private void exportCertChain(Certificate[] certs, String alias, StringBuffer sb, String outform) throws Exception {
        if (certs == null)
            return;

        for (int i = 0; i < certs.length; i++) {
            String cerfile = alias + (outform.equalsIgnoreCase("PEM") ? ".pem" : ".crt." + i + ".der");
            boolean append = (outform.equalsIgnoreCase("PEM") ? true : false);
            FileOutputStream fos = new FileOutputStream(cerfile, append);
            byte[] derEncodedCert = certs[i].getEncoded();
            if (outform.equalsIgnoreCase("PEM")) {
                PEMData pemData = new PEMData(derEncodedCert);
                String base64Data = pemData.encode();
                String preEB = "-----BEGIN CERTIFICATE-----\n";
                String postEB = "\n-----END CERTIFICATE-----\n";
                fos.write(preEB.getBytes());
                fos.write(base64Data.getBytes());
                fos.write(postEB.getBytes());
            } else {
                fos.write(derEncodedCert);
            }
            fos.close();
            sb.append("Appended Certificate#" + i + " to file: " + cerfile + "\n");
        }
    }

    private String exportEntry(KeyStore ks, String alias, String keypass, String outform) throws Exception {
        StringBuffer sb = new StringBuffer();
        if (ks.isKeyEntry(alias)) {
            try {
                Key key = ks.getKey(alias, keypass.toCharArray());
                exportKey(key, alias, sb, outform);

                Certificate[] certs = ks.getCertificateChain(alias);
                exportCertChain(certs, alias, sb, outform);
                if (pkcs12Output) { // Convert the <alias>.pem file into a <alias>.p12 file.
                    String pemfile = alias + ".pem";
                    String p12file = alias + ".p12";
                    pem2pkcs12(pemfile, p12file, keypass, sb);
                }
            } catch (UnrecoverableKeyException e) {
                sb.append("Cannot Receover Key from KeyStore.\n");
            }
        } else {
            Certificate[] certs = ks.getCertificateChain(alias);
            exportCertChain(certs, alias, sb, outform);
        }
        return sb.toString();
    }

    // This function relies on openssl. Not pure Java.
    private void pem2pkcs12(String pemfile, String p12file, String keypass, StringBuffer sb) {
        // Check if openssl is present.
        if (!opensslPresent()) {
            sb.append("*** ERROR *** Couldn't find openssl. Cannot convert PEM to PKCS12.");
        } else {
            String cmd = "openssl pkcs12 -export -in " + pemfile + " -out " + p12file + " -password pass:" + keypass;
            int r = runOSCommand(cmd);
            if (r == 0) {
                sb.append("Converted PEM file " + pemfile + " to PKCS12 file " + p12file);
            } else {
                sb.append("*** ERROR *** Conversion of PEM file " + pemfile + " to PKCS12 file " + p12file + " FAILED");
            }
        }
    }

    private boolean opensslPresent() {
        if (runOSCommand("openssl exit") == 0) {
            return true;
        } else {
            return false;
        }
    }

    private int runOSCommand(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            return p.waitFor();
        } catch (IOException ioe) {
            return 1;
        } catch (InterruptedException ie) {
            return 1;
        }
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        StringBuffer sb = new StringBuffer();
        try {
            args.setDefaults(defaults);
            pkcs12Output = false;
            String keystore = args.get("keystore");
            String storepass = args.get("storepass");
            String type = args.get("kstype");
            String providerName = args.get("provider");
            String keypass = args.get("keypass");
            String outform = args.get("outform");
            if (outform.equalsIgnoreCase("PKCS12")) {
                pkcs12Output = true;
                outform = "PEM";
            }
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
                    sb.append(exportEntry(ks, alias, keypass, outform));
                } else {
                    sb.append("No such Entry: " + alias + ".\n");
                }
            } else {
                Enumeration<String> enumeration = ks.aliases();

                while (enumeration.hasMoreElements()) {
                    String alias0 = enumeration.nextElement();
                    sb.append(exportEntry(ks, alias0, keypass, outform));
                }
            }
        } catch (Exception exc) {
            throw new JSTKException("ExportCommand.execute() failed", exc);
        }
        return new JSTKResult(null, true, sb.toString());
    }
}
