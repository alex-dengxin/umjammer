/*
 * @(#) $Id: JSTKOptions.java,v 1.1.1.1 2003/10/05 18:39:11 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * JSTKOptions.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050318 nsano initial version <br>
 */
public class JSTKOptions implements JSTKArgs {
    /** */
    private Map<String, String> options = new HashMap<String, String>();

    /** */
    private Map<String, String> defaults = null;

    /** */
    private List<String> pArgs = new ArrayList<String>(); // Positional Args.

    /** */
    public void parse(String[] args, int beginIndex) {
        String key, value;
        int index = beginIndex;
        while (args.length > index) {
            value = "true";
            if (args[index].startsWith("-")) {
                key = args[index++].substring(1);
                if (args.length > index && !args[index].startsWith("-")) {
                    value = args[index++];
                }
                set(key, value);
            } else {
                pArgs.add(args[index]);
                ++index;
            }
        }
    }

    /** */
    public int getNum() {
        return pArgs.size();
    }

    /** */
    public void setDefaults(Map<String, String> defaults) {
        this.defaults = defaults;
    }

    /** */
    public String get(String name) {
        String value = options.get(name);
        if (value == null && defaults != null) {
            value = defaults.get(name);
        }
        return value;
    }

    /** */
    public String get(int pos) {
        return pArgs.get(pos);
    }

    /** */
    public void set(String name, String value) {
        options.put(name, value);
    }
}
