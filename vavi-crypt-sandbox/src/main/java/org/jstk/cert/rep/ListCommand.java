/*
 * @(#) $Id: ListCommand.java,v 1.1.1.1 2003/10/05 18:39:15 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert.rep;

import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class ListCommand extends JSTKCommandAdapter {
    private int index = 0;

    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("reptype", "JSTK");
        defaults.put("repfile", "my.rep");
    }

    public String briefDescription() {
        String briefDesc = "lists the contents of a repository";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -repfile <repfile>: Repository file.[" + defaults.get("repfile") + "]\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-repfile <repfile>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "-repfile test.rep"
        };
        return sampleUses;
    }

    public void formatX509Certificate(X509Certificate cert, StringBuffer sb) {
        sb.append("X509CERT[" + index + "]: ");
        sb.append("Serial No: " + cert.getSerialNumber());
        sb.append(", Issuer: " + cert.getIssuerX500Principal());
        sb.append(", Subject: " + cert.getSubjectX500Principal() + "\n");
    }

    public void formatX509CRL(X509CRL crl, StringBuffer sb) {
        sb.append("X509CRL[" + index + "]: ");
        sb.append("Issuer: " + crl.getIssuerX500Principal());
        sb.append(", This Update: " + crl.getThisUpdate());
        sb.append(", Next Update: " + crl.getNextUpdate() + "\n");
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
//          String infile = args.get("infile");
            String repfile = args.get("repfile");
            FileBasedRepository fbr = new FileBasedRepository(repfile);
            Iterator<Object> itr = fbr.getRepository().iterator();
            StringBuffer sb = new StringBuffer();

            index = 0;
            while (itr.hasNext()) {
                Object entry = itr.next();
                if (entry instanceof X509Certificate) {
                    formatX509Certificate((X509Certificate) entry, sb);
                } else if (entry instanceof X509CRL) {
                    formatX509CRL((X509CRL) entry, sb);
                }
                ++index;
            }

            return new JSTKResult(null, true, sb.toString());
        } catch (Exception exc) {
            throw new JSTKException("ListCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        ListCommand listCmd = new ListCommand();
        JSTKResult result = (JSTKResult) listCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
