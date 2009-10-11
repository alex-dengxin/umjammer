/*
 * @(#) $Id: AddCommand.java,v 1.1.1.1 2003/10/05 18:39:15 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert.rep;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class AddCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("reptype", "JSTK");
        defaults.put("repfile", "my.rep");
    }

    public String briefDescription() {
        String briefDesc = "adds a Certificate or CRL to repository";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -repfile <repfile>: Repository file.[" + defaults.get("repfile") + "]\n" + "  -infile <infile>  : File having the Certificate or CRL.\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "-infile <infile> [-repfile <repfile>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "-infile test.cer"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String infile = args.get("infile");
            if (infile == null) {
                return new JSTKResult(null, false, "No input file. Specify -infile option.");
            }
            String repfile = args.get("repfile");
            FileBasedRepository fbr = new FileBasedRepository(repfile);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

//          StringBuffer sb = new StringBuffer();

            File file = new File(infile);
            int bufsize = (int) file.length() + 1024; // Added 1024 for extra safety.
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(infile), bufsize);
            bis.mark(bufsize);

            try {
                X509Certificate cert = (X509Certificate) cf.generateCertificate(bis);
                fbr.getRepository().add(cert);
                fbr.save();
                return new JSTKResult(null, true, "X509 Certificate added to repository: " + repfile);
            } catch (CertificateException ce) {
                RepTool.logger.fine("Cannot parse input as a Certificate");
                RepTool.logger.log(java.util.logging.Level.FINER, "Not a Certificate", ce);
            } // Fall through.

            bis.reset();
            try {
                X509CRL crl = (X509CRL) cf.generateCRL(bis);
                fbr.getRepository().add(crl);
                fbr.save();
                return new JSTKResult(null, true, "X509 CRL added to repository: " + repfile);
            } catch (CRLException crle) {
                RepTool.logger.fine("Cannot parse input as a CRL");
                RepTool.logger.log(java.util.logging.Level.FINER, "Not a CRL", crle);
            } // Fall through.

            return new JSTKResult(null, false, "Unknown format");
        } catch (Exception exc) {
            throw new JSTKException("ShowCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        AddCommand addCmd = new AddCommand();
        JSTKResult result = (JSTKResult) addCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
