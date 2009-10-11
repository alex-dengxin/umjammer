/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;


/**
 * PropertiesUserAgentDao. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 071003 nsano initial version <br>
 */
public class PropertiesUserAgentDao implements UserAgentDao {

    /** */
    private static List<String> userAgents = new ArrayList<String>();

    /** always returns same instances */
    public List<String> getUserAgents() {
        return userAgents;
    }

    /* */
    static {
        try {
            Properties props = new Properties();
            props.load(PropertiesUserAgentDao.class.getResourceAsStream("useragent.properties"));
            Enumeration<?> e = props.propertyNames();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                String value = props.getProperty(name);
                userAgents.add(value);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}

/* */
