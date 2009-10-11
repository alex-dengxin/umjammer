/*
 * @(#) $Id: ExportCACertCommand.java,v 1.1 2003/10/28 08:46:40 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net).
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the
 * root directory of the containing software.
 */

package org.jstk.cert;

import java.io.FileOutputStream;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;
import org.jstk.cert.ca.CADatabase;
import org.jstk.cert.ca.FileBasedCADatabaseParams;


public class ExportCACertCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("cadir", "cadir");
        defaults.put("cerfile", "ca.cer");
    }

    public String briefDescription() {
        String briefDesc = "exports the CA certificate or certpath";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -cadir <cadir>      : CA directory.[" + defaults.get("cadir") + "]\n" + "  -cerfile <cerfile>  : File to write exported Certificate.[" + defaults.get("cerfile") + "]\n" + "  -password <passwd>  : Password for CA keystore.\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-cadir <cadir>] [-cerfile <cerfile>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "-password <password>", "-cerfile test.cer -password <password>"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String cerfile = args.get("cerfile");
            String cadir = args.get("cadir");
            String password = args.get("password");
            if (password == null)
                return new JSTKResult(null, false, "CA keystore password not specified. Use -password option.");

            FileBasedCADatabaseParams fbParams = new FileBasedCADatabaseParams(cadir);
            fbParams.setPassword(password);

            CADatabase cadb = CADatabase.getInstance("file", fbParams);
            Certificate cert = cadb.getCACert();

            FileOutputStream fos = new FileOutputStream(cerfile);
            fos.write(cert.getEncoded());
            fos.close();

            return new JSTKResult(null, true, "Exported Certificate written to file: " + cerfile);
        } catch (Exception exc) {
            throw new JSTKException("ExportCACertCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        ExportCACertCommand expCertCmd = new ExportCACertCommand();
        JSTKResult result = (JSTKResult) expCertCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
