/*
 * @(#) $Id: ShowCommand.java,v 1.1.1.1 2003/10/05 18:39:14 pankaj_kumar Exp $
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
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class ShowCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        // defaults.put("infile", "my.cer");
    }

    public String briefDescription() {
        String briefDesc = "display contents of a PKI file";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -infile <infile>  : File having the PKI material ( cert, certpath, CRL, ...).\n" + defaults.get("infile") + "]\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "-infile <infile>"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "-infile test.cer"
        };
        return sampleUses;
    }

    public void formatX509Certificate(X509Certificate cert, StringBuffer sb, String indent) {
        sb.append(indent + "Certificate:\n");
        sb.append(indent + "  Data:\n");
        sb.append(indent + "    Version: " + cert.getVersion() + "\n");
        sb.append(indent + "    Serial Number: " + cert.getSerialNumber() + "\n");
        sb.append(indent + "    Signature Algorithm: " + cert.getSigAlgName() + "\n");
        sb.append(indent + "    Issuer: " + cert.getIssuerX500Principal() + "\n");
        sb.append(indent + "    Validity:\n");
        sb.append(indent + "      Not Before: " + cert.getNotBefore() + " \n");
        sb.append(indent + "      Not After: " + cert.getNotAfter() + " \n");
        sb.append(indent + "    Subject: " + cert.getSubjectX500Principal() + "\n");
        sb.append(indent + "    Extensions: \n");

        sb.append(indent + "      X509v3 Basic Constraints:\n");
        int pathLen = cert.getBasicConstraints();
        if (pathLen != -1) // Not a CA
            sb.append(indent + "        CA: TRUE, pathLen: " + pathLen + "\n");
        else
            sb.append(indent + "        CA: FALSE\n");

        boolean[] keyUsage = cert.getKeyUsage();
        if (keyUsage != null) {
            KeyUsage ku = new KeyUsage(keyUsage);
            sb.append(indent + "      Key Usage: " + ku.getKeyUsageString() + "\n");
        }

        List<String> list = null;
        try {
            list = cert.getExtendedKeyUsage();
        } catch (CertificateParsingException cpe) {
        }

        if (list != null) {
            sb.append(indent + "      Extended Key Usage:");
            Iterator<String> li = list.iterator();
            while (li.hasNext()) {
                sb.append(" ");
                sb.append(li.next());
            }
            sb.append("\n");
        }
    }

    public void formatCertPath(CertPath cp, StringBuffer sb) {
        List<? extends Certificate> list = cp.getCertificates();
        Iterator<? extends Certificate> li = list.iterator();
        sb.append("CertPath:\n");
        int index = 0;
        while (li.hasNext()) {
            sb.append("CertPath Component: " + index + "\n");
            X509Certificate cert = (X509Certificate) li.next();
            formatX509Certificate(cert, sb, "  ");
            ++index;
        }
    }

    public void formatX509CRL(X509CRL crl, StringBuffer sb) {
        sb.append("CRL:\n");
        sb.append("  Version: " + crl.getVersion() + "\n");
        sb.append("  Signature Algorithm: " + crl.getSigAlgName() + "\n");
        sb.append("  Issuer: " + crl.getIssuerX500Principal() + "\n");
        sb.append("  This Update: " + crl.getThisUpdate() + "\n");
        sb.append("  Next Update: " + crl.getNextUpdate() + "\n");

        Set<? extends X509CRLEntry> revokedCerts = crl.getRevokedCertificates();
        if (revokedCerts == null)
            return;
        Iterator<? extends X509CRLEntry> itr = revokedCerts.iterator();
        int index = 0;
        while (itr.hasNext()) {
            formatX509CRLEntry(itr.next(), sb, index);
            ++index;
        }
    }

    public void formatX509CRLEntry(X509CRLEntry crlEntry, StringBuffer sb, int index) {
        sb.append("  CRLEntry[" + index + "]:\n");
        sb.append("    Serial Number: " + crlEntry.getSerialNumber() + "\n");
        sb.append("    Revocation Date: " + crlEntry.getRevocationDate() + "\n");
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String infile = args.get("infile");
            if (infile == null) {
                return new JSTKResult(null, false, "No input file. Specify -infile option.");
            }

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            StringBuffer sb = new StringBuffer();

            File file = new File(infile);
            int bufsize = (int) file.length() + 1024; // Added 1024 for extra safety.
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(infile), bufsize);
            bis.mark(bufsize);

            try {
                Certificate cert = cf.generateCertificate(bis);
                formatX509Certificate((X509Certificate) cert, sb, "");
                return new JSTKResult(null, true, sb.toString());
            } catch (CertificateException ce) {
                CertTool.logger.fine("Cannot parse input as a Certificate");
                CertTool.logger.log(java.util.logging.Level.FINER, "Not a Certificate", ce);
            } // Fall through.

            bis.reset();
            try {
                CertPath cp = cf.generateCertPath(bis, "PkiPath");
                formatCertPath(cp, sb);
                return new JSTKResult(null, true, sb.toString());
            } catch (CertificateException ce) {
                CertTool.logger.fine("Cannot parse input as a PkiPath Cert Path");
                CertTool.logger.log(java.util.logging.Level.FINER, "Not a PkiPath Cert Path", ce);
            } // Fall through.

            bis.reset();
            try {
                CertPath cp = cf.generateCertPath(bis, "PKCS7");
                formatCertPath(cp, sb);
                return new JSTKResult(null, true, sb.toString());
            } catch (CertificateException ce) {
                CertTool.logger.fine("Cannot parse input as a PKCS7 Cert Path");
                CertTool.logger.log(java.util.logging.Level.FINER, "Not a PKCS7 Cert Path", ce);
            } // Fall through.

            bis.reset();
            try {
                X509CRL crl = (X509CRL) cf.generateCRL(bis);
                formatX509CRL(crl, sb);
                return new JSTKResult(null, true, sb.toString());
            } catch (CRLException crle) {
                CertTool.logger.fine("Cannot parse input as a CRL");
                CertTool.logger.log(java.util.logging.Level.FINER, "Not a CRL", crle);
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
