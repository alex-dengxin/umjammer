/*
 * @(#) $Id: CertTool.java,v 1.2 2003/10/28 08:46:18 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net).
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the
 * root directory of the containing software.
 */

package org.jstk.cert;

import java.util.logging.Logger;

import org.jstk.JSTKAbstractTool;


public class CertTool extends JSTKAbstractTool {
    public static final Logger logger = Logger.getLogger("org.jstk.cert");
    static {
        cmds.put("issue", new IssueCertCommand());
        cmds.put("revoke", new RevokeCertCommand());
        cmds.put("show", new ShowCommand());
        cmds.put("crl", new CRLGenCommand());
        cmds.put("validate", new ValidateCertPathCommand());
        cmds.put("build", new BuildCertPathCommand());
        cmds.put("setupca", new SetupCACommand());
        cmds.put("exportca", new ExportCACertCommand());
        cmds.put("cut", new CutCommand());
    }

    public String progName() {
        String progName = System.getProperty("org.jstk.cert.progname");
        if (progName == null)
            progName = "java org.jstk.cert.CertTool";

        return progName;
    }

    public String briefDescription() {
        return "a minimal CA tool";
    }

    public static void main(String[] args) throws Exception {
        CertTool ct = new CertTool();
        System.exit(ct.execute(args));
    }
}
