/*
 * @(#) $Id: SecurityInfo.java,v 1.1.1.1 2003/10/05 18:39:18 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;


public class SecurityInfo {
    public static class AlgInfo {
        Set<String> aliases;

        Properties props;

        public AlgInfo(String alg1, String alg2, String pn, String pv) {
            aliases = new HashSet<String>();
            aliases.add(alg1);
            if (alg2 != null)
                aliases.add(alg2);
            addProperty(pn, pv);
        }

        public boolean addConditionally(String alg1, String alg2, String pn, String pv) {
            if (aliases.contains(alg1)) {
                if (alg2 != null)
                    aliases.add(alg2);
                addProperty(pn, pv);
                return true;
            } else if (alg2 != null && aliases.contains(alg2)) {
                aliases.add(alg1);
                addProperty(pn, pv);
                return true;
            }
            return false;
        }

        public void addProperty(String pn, String pv) {
            if (pn != null) {
                if (props == null)
                    props = new Properties();
                props.setProperty(pn, pv);
            }
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            Iterator<String> aitr = aliases.iterator();
            boolean firstAlias = true;
            while (aitr.hasNext()) {
                String alg = aitr.next();
                if (!firstAlias)
                    sb.append("|");
                sb.append(alg);
                firstAlias = false;
            }
            return sb.toString();
        }
    }

    public static class ProviderInfo {
        String name;

        double version;

        String info;

        Properties props;

        Map<String, Vector<SecurityInfo.AlgInfo>> svcmap;

        public ProviderInfo(Provider provider) {
            name = provider.getName();
            version = provider.getVersion();
            info = provider.getInfo();

            props = new Properties();
            svcmap = new HashMap<String, Vector<SecurityInfo.AlgInfo>>();
            Iterator<Map.Entry<Object, Object>> itr = provider.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<Object, Object> ent = itr.next();
                String key = (String) ent.getKey();
                String value = (String) ent.getValue();
                props.setProperty(key, value);

                String[] comps = key.split("\\.");
                String svc = null;
                String alg1 = null;
                String alg2 = null;
                String pname = null, pvalue = null;
                if (comps.length == 2) { // key is of form:: <svc>.<alg>
                    svc = comps[0];
                    alg1 = comps[1];
                    String[] subcomps = alg1.split(" ");
                    if (subcomps.length > 1) {
                        alg1 = subcomps[0];
                        pname = subcomps[1];
                        pvalue = value;
                    }
                } else if (comps.length == 4) { // key is of form:: Alg.Alias.<svc>.<alg>
                    svc = comps[2];
                    alg1 = comps[3];
                    alg2 = value;
                } else { // Ignore other entries.
                    continue;
                }
                Vector<AlgInfo> algs = svcmap.get(svc);
                if (algs == null) { // Found a new service.
                    algs = new Vector<AlgInfo>();

                    algs.add(new AlgInfo(alg1, alg2, pname, pvalue));
                    svcmap.put(svc, algs);
                } else { // Exisiting service
                    boolean found = false;
                    for (int i = 0; i < algs.size() && !found; i++) {
                        AlgInfo ai = algs.elementAt(i);
                        found = ai.addConditionally(alg1, alg2, pname, pvalue);
                    }
                    if (!found) // New Algorithm
                        algs.add(new AlgInfo(alg1, alg2, pname, pvalue));
                }
            }
        }
    }

    public ProviderInfo[] providers = null; // array of ProviderInfo

    public SecurityInfo() {
        Provider[] ps = Security.getProviders();
        providers = new ProviderInfo[ps.length];
        for (int i = 0; i < ps.length; i++) {
            providers[i] = new ProviderInfo(ps[i]);
        }
    }
}
