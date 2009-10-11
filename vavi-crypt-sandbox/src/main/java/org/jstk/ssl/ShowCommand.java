/*
 * @(#) $Id: ShowCommand.java,v 1.1.1.1 2003/10/05 18:39:24 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class ShowCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {

    }

    public String briefDescription() {
        String briefDesc = "displays the SSL related information";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -cs            : Displays the supported and enabled cipher suites.\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "-cs"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "-cs"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            boolean cipherSuite = Boolean.valueOf(args.get("cs")).booleanValue();
            StringBuffer sb = new StringBuffer();

            if (cipherSuite) {
                SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                String[] supportedCSuites = sf.getSupportedCipherSuites();
                String[] enabledCSuites = sf.getDefaultCipherSuites();

                sb.append("  Supported Cipher Suites:\n");
                for (int i = 0; i < supportedCSuites.length; i++) {
                    sb.append("             [" + i + "] " + supportedCSuites[i] + "\n");
                }
                sb.append("  Enabled Cipher Suites  :\n");
                for (int i = 0; i < enabledCSuites.length; i++) {
                    sb.append("             [" + i + "] " + enabledCSuites[i] + "\n");
                }
            }
            return new JSTKResult(null, true, sb.toString());
        } catch (Exception exc) {
            throw new JSTKException("ServerCommand execution failed", exc);
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
