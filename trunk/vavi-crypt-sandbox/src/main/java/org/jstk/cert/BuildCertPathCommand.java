/*
 * @(#) $Id: BuildCertPathCommand.java,v 1.1.1.1 2003/10/05 18:39:13 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509CertSelector;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.cert.rep.FileBasedRepository;


public class BuildCertPathCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("truststore", "my.ts");
        defaults.put("storetype", "JCEKS");
        defaults.put("outfile", "my.p7b");
    }

    public String briefDescription() {
        String briefDesc = "display contents of a Certificate or Certificate Chain";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -dn <dname>         : Distinguished name of the target subject.\n" + "  -truststore <file>  : keystore with trusted certificates.[" + defaults.get("truststore") + "]\n" + "  -storetype <type>   : keystore type (JKS or JCEKS).[" + defaults.get("storetype") + "]\n" + "  -outfile <outfile>  : file to write the certificate chain in PKCS#7 format.[" + defaults.get("outfile") + "]\n" + "  -repfile <repfile>  : repository file.\n";
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
            String dn = args.get("dn");
            String trustStoreFile = args.get("truststore");
            String storeType = args.get("storetype");
            String repfile = args.get("repfile");
            String outfile = args.get("outfile");

            if (dn == null)
                return new JSTKResult(null, false, "Must specify dn of the target subject.");

            StringBuffer sb = new StringBuffer();

            CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");

            // Create the PKIX parameters.
            FileInputStream fis = new FileInputStream(trustStoreFile);
            KeyStore trustStore = KeyStore.getInstance(storeType);
            trustStore.load(fis, null);
            X509CertSelector targetConstraints = new X509CertSelector();
            targetConstraints.setSubject(dn);
            PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustStore, targetConstraints);
            pkixParams.setMaxPathLength(5);
            pkixParams.setRevocationEnabled(false);

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
                PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) cpb.build(pkixParams);
                CertPath cp = result.getCertPath();
                FileOutputStream fos = new FileOutputStream(outfile);
                fos.write(cp.getEncoded());
                fos.close();
                sb.append("Build succeeded. CertPath written to file: " + outfile);
            } catch (CertPathBuilderException cpbe) {
                sb.append("Build failed:" + cpbe.getMessage());
            }

            return new JSTKResult(null, true, sb.toString());
        } catch (Exception exc) {
            throw new JSTKException("BuildCertPathCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        BuildCertPathCommand buildCmd = new BuildCertPathCommand();
        JSTKResult result = (JSTKResult) buildCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
