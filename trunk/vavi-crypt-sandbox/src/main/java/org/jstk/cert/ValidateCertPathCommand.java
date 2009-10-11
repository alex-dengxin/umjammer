/*
 * @(#) $Id: ValidateCertPathCommand.java,v 1.1.1.1 2003/10/05 18:39:14 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.cert.rep.FileBasedRepository;


public class ValidateCertPathCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("cerfile", "my.cer");
        defaults.put("truststore", "my.ts");
        defaults.put("storetype", "JCEKS");
        defaults.put("crlfile", "my.crl");
    }

    public String briefDescription() {
        String briefDesc = "display contents of a Certificate or Certificate Chain";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -cerfile <cerfile>  : File having the certificate chain.[" + defaults.get("cerfile") + "]\n" + "  -truststore <file>  : keystore with trusted certificates.[" + defaults.get("truststore") + "]\n" + "  -storetype <type>   : keystore type (JKS or JCEKS).[" + defaults.get("storetype") + "]\n" + "  -repfile <repfile>  : repository file.\n" + "  -crlfile <crlfile>  : CRL file.[" + defaults.get("crlfile") + "]\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-cerfile <cerfile>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "", "-cerfile test.cer"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String cerfile = args.get("cerfile");
            String trustStoreFile = args.get("truststore");
            String storeType = args.get("storetype");
            String crlfile = args.get("crlfile");
            String repfile = args.get("repfile");

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            CertPath cp;
            StringBuffer sb = new StringBuffer();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(cerfile));
            bis.mark(1024);
            try {
                try {
                    cp = cf.generateCertPath(bis);
                } catch (CertificateException ce) { // Try PKCS7 format.
                    bis.reset();
                    cp = cf.generateCertPath(bis, "PKCS7");
                }
                bis.close();
            } catch (CertificateException ce) { // Not a certpath.
                bis.reset();
                /*Certificate cert =*/ cf.generateCertificate(bis);
                bis.close();
                throw new JSTKException("Validation of Certificate not supported.");
            }

            CertPathValidator cpv = CertPathValidator.getInstance("PKIX");

            // Create the PKIX parameters.
            FileInputStream fis = new FileInputStream(trustStoreFile);
            KeyStore trustStore = KeyStore.getInstance(storeType);
            trustStore.load(fis, null);
            PKIXParameters pkixParams = new PKIXParameters(trustStore);
            pkixParams.setRevocationEnabled(false);

            // Check for CRL
            if (crlfile != null && (new java.io.File(crlfile)).exists()) {
                BufferedInputStream bis1 = new BufferedInputStream(new FileInputStream(crlfile));
                CRL crl = cf.generateCRL(bis1);
                Vector<CRL> params = new Vector<CRL>();
                params.add(crl);
                CollectionCertStoreParameters csParams = new CollectionCertStoreParameters(params);
                CertStore cs = CertStore.getInstance("Collection", csParams);
                pkixParams.addCertStore(cs);
                pkixParams.setRevocationEnabled(true);
            }

            // Check for Repositroy
            if (repfile != null && (new java.io.File(repfile)).exists()) {
                FileBasedRepository fbr = new FileBasedRepository(repfile);
                Collection<?> params = fbr.getRepository();
                CollectionCertStoreParameters csParams = new CollectionCertStoreParameters(params);
                CertStore cs = CertStore.getInstance("Collection", csParams);
                pkixParams.addCertStore(cs);
                // pkixParams.setRevocationEnabled(true);
            }

            try {
                PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) cpv.validate(cp, pkixParams);
                /*PolicyNode policyTree =*/ result.getPolicyTree();
                /*PublicKey subjectPublicKey =*/ result.getPublicKey();
                sb.append("Validation succeeded.");
            } catch (CertPathValidatorException cpve) {
                sb.append("Validation failed. cert[" + cpve.getIndex() + "] :" + cpve.getMessage());
            }

            return new JSTKResult(null, true, sb.toString());
        } catch (Exception exc) {
            throw new JSTKException("VerifyCertCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        ValidateCertPathCommand validateCPCmd = new ValidateCertPathCommand();
        JSTKResult result = (JSTKResult) validateCPCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
