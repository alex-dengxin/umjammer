/*
 * @(#) $Id: RevokeCertCommand.java,v 1.1.1.1 2003/10/05 18:39:14 pankaj_kumar Exp $
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
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.cert.ca.CADatabase;
import org.jstk.cert.ca.FileBasedCADatabaseParams;


public class RevokeCertCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("cerfile", "my.cer");
        defaults.put("cadir", "cadir");
    }

    public String briefDescription() {
        String briefDesc = "revokes a previously issued certificate";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -cerfile <cerfile>  : File having the DER oe PEM encoded Certificate.[" + defaults.get("cerfile") + "]\n";
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

    public X509Certificate readCertificate(String cerfile) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(cerfile));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = null;
        bis.mark(1024);
        try {
            CertPath cp;
            try {
                cp = cf.generateCertPath(bis);
            } catch (CertificateException ce) { // Try PKCS7 format.
                bis.reset();
                cp = cf.generateCertPath(bis, "PKCS7");
            }
            List<? extends Certificate> list = cp.getCertificates();
            Iterator<? extends Certificate> li = list.iterator();
            if (li.hasNext()) { // take the first certificate in the chain
                cert = (X509Certificate) li.next();
            }
        } catch (CertificateException ce) { // Not a certpath.
            bis.reset();
            cert = (X509Certificate) cf.generateCertificate(bis);
        }
        bis.close();
        return cert;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String cerfile = args.get("cerfile");
            String cadir = args.get("cadir");

            X509Certificate cert = readCertificate(cerfile);

            FileBasedCADatabaseParams fbParams = new FileBasedCADatabaseParams(cadir);
            CADatabase cadb = CADatabase.getInstance("file", fbParams);
            if (!cadb.getIssuedCerts().exists(cert)) {
                return new JSTKResult(null, false, "Certificate not issued. serial no.: " + cert.getSerialNumber());
            }
            if (cadb.getRevokedCerts().exists(cert)) {
                return new JSTKResult(null, false, "Certificate already revoked. serial no.: " + cert.getSerialNumber());
            }
            cadb.getRevokedCerts().add(cert);

            return new JSTKResult(null, true, "Certificate revoked. serial no.: " + cert.getSerialNumber());
        } catch (Exception exc) {
            throw new JSTKException("RevokeCertCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        RevokeCertCommand revokeCertCmd = new RevokeCertCommand();
        JSTKResult result = (JSTKResult) revokeCertCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
