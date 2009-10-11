/*
 * @(#) $Id: CutCommand.java,v 1.1.1.1 2003/10/05 18:39:13 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class CutCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        // defaults.put("infile", "my.cer");
    }

    public String briefDescription() {
        String briefDesc = "take out a component of a certification path";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -infile <infile>  : File having the certification path.\n" + "  -outfile <outfile>: File to store the component.\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "-infile <infile> -outfile <outfile>"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "-infile test.cer -outfile test1.cer"
        };
        return sampleUses;
    }

    public void writeCert(Certificate cert, String file) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(cert.getEncoded());
        fos.close();
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String infile = args.get("infile");
            String outfile = args.get("outfile");
            if (infile == null)
                return new JSTKResult(null, false, "No input file. Specify -infile option.");

            if (outfile == null)
                return new JSTKResult(null, false, "No output file. Specify -outfile option.");

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

//          StringBuffer sb = new StringBuffer();

            File file = new File(infile);
            int bufsize = (int) file.length() + 1024; // Added 1024 for extra safety.
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(infile), bufsize);
            bis.mark(bufsize);

            try {
                Certificate cert = cf.generateCertificate(bis);
                writeCert(cert, outfile);
                return new JSTKResult(null, true, "Wrote certificate to file: " + outfile);
            } catch (CertificateException ce) {
                CertTool.logger.fine("Cannot parse input as a Certificate");
                CertTool.logger.log(java.util.logging.Level.FINER, "Not a Certificate", ce);
            } // Fall through.

            bis.reset();
            try {
                CertPath cp = cf.generateCertPath(bis, "PkiPath");
                List<? extends Certificate> list = cp.getCertificates();
                Certificate cert = list.get(0);
                writeCert(cert, outfile);
                return new JSTKResult(null, true, "Wrote certificate to file: " + outfile);
            } catch (CertificateException ce) {
                CertTool.logger.fine("Cannot parse input as a PkiPath Cert Path");
                CertTool.logger.log(java.util.logging.Level.FINER, "Not a PkiPath Cert Path", ce);
            } // Fall through.

            bis.reset();
            try {
                CertPath cp = cf.generateCertPath(bis, "PKCS7");
                List<? extends Certificate> list = cp.getCertificates();
                Certificate cert = list.get(0);
                writeCert(cert, outfile);
                return new JSTKResult(null, true, "Wrote certificate to file: " + outfile);
            } catch (CertificateException ce) {
                CertTool.logger.fine("Cannot parse input as a PKCS7 Cert Path");
                CertTool.logger.log(java.util.logging.Level.FINER, "Not a PKCS7 Cert Path", ce);
            } // Fall through.

            return new JSTKResult(null, false, "Unknown format");
        } catch (Exception exc) {
            throw new JSTKException("ShowCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        ShowCommand showCmd = new ShowCommand();
        JSTKResult result = (JSTKResult) showCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
