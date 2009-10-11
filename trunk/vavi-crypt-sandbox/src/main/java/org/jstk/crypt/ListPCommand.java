/*
 * @(#) $Id: ListPCommand.java,v 1.1.1.1 2003/10/05 18:39:18 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class ListPCommand extends JSTKCommandAdapter {
    public String briefDescription() {
        return "lists cryptographic providers and services offered";
    }

    public String optionsDescription() {
        return "  -provider <provider>: limit to this provider name.\n" + "  -info               : print provider info.\n" + "  -props              : print provider properties.\n" + "  -csinfo             : print cryptographic services.\n";
    }

    public String[] useForms() {
        String[] useForms = {
            "[-provider <provider>] [-info] [-props] [-csinfo]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] uses = {
            "", "-info", "-csinfo", "-props", "-info -props -csinfo", "-provider SunJCE -props", "-provider SunJCE -csinfo"
        };
        return uses;
    }

    private String formText(String left, String right, int maxLeftLen) {
        StringBuffer sb = new StringBuffer();
        sb.append(left);
        int blanksNeeded = maxLeftLen - left.length();
        while (blanksNeeded-- > 0)
            sb.append(" ");
        if (left.length() > 0)
            sb.append(" : " + right);
        else
            sb.append("   " + right);
        return sb.toString();
    }

    private void appendProvider(SecurityInfo.ProviderInfo provider, boolean info, boolean props, boolean csinfo) {

        if (info) // Append provider info.
            result.appendText("Provider Info:: \n" + provider.info + "\n");

        if (props) { // Append properties
            result.appendText("Provider Properties::\n");
            /*
             * Make the output less verbose. result.appendText(formText("property name", "property value", 32) + "\n");
             * result.appendText("---------------------------------------------------------------\n");
             */
            Enumeration<?> propNames = provider.props.propertyNames();
            int idx = 0;
            while (propNames.hasMoreElements()) {
                String key = (String) propNames.nextElement();
                String value = provider.props.getProperty(key);
                String left = "[" + idx + "] " + key;
                result.appendText(formText(left, value, 32) + "\n");
                ++idx;
            }
            result.appendText("---------------------------------------------------------------\n");
        }

        if (csinfo) { // Append service and algo/type info.
            result.appendText("Cryptographic Services::\n");
            /*
             * Make the output less verbose. result.appendText(formText("cryptographic service", "<algorith>|<type>", 20) +
             * "\n"); result.appendText("---------------------------------------------------------------\n");
             */

            Iterator<Map.Entry<String, Vector<SecurityInfo.AlgInfo>>> itr = provider.svcmap.entrySet().iterator();
            int idx = 0;
            while (itr.hasNext()) {
                Map.Entry<String, Vector<SecurityInfo.AlgInfo>> ent = itr.next();
                String key = ent.getKey();
                Vector<SecurityInfo.AlgInfo> algs = ent.getValue();

                for (int i = 0; i < algs.size(); i++) {
                    SecurityInfo.AlgInfo ai = algs.elementAt(i);
                    String left = "";
                    if (i == 0)
                        left = "[" + idx + "] " + key;
                    result.appendText(formText(left, ai.toString(), 20) + "\n");
                    if (ai.props != null) {
                        Enumeration<?> propNames = ai.props.propertyNames();
                        while (propNames.hasMoreElements()) {
                            String key1 = (String) propNames.nextElement();
                            String value1 = ai.props.getProperty(key1);
                            result.appendText(formText("", key1 + " = " + value1, 24) + "\n");
                        }
                    }
                }
                ++idx;
            }
            result.appendText("---------------------------------------------------------------\n");
        }
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        String providerName = args.get("provider");
        boolean info = Boolean.valueOf(args.get("info")).booleanValue();
        boolean props = Boolean.valueOf(args.get("props")).booleanValue();
        boolean csinfo = Boolean.valueOf(args.get("csinfo")).booleanValue();

        try {
            SecurityInfo si = new SecurityInfo();
            result = new JSTKResult(si, true, "");
            for (int i = 0; i < si.providers.length; i++) {
                if ((providerName == null) || (si.providers[i].name.matches(providerName))) {
                    result.appendText("Provider[" + i + "]:: " + si.providers[i].name + " " + si.providers[i].version + "\n");
                    appendProvider(si.providers[i], info, props, csinfo);
                }
            }
            return result;
        } catch (Exception exc) {
            throw new JSTKException("ListPCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        ListPCommand listPCmd = new ListPCommand();
        JSTKResult result = (JSTKResult) listPCmd.execute(opts);
        System.out.println(result.getText());
    }
}
