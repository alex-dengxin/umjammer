/*
 * @(#) $Id: OidMap.java,v 1.1.1.1 2003/10/05 18:39:12 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * OidMap.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050317 nsano initial version <br>
 */
public class OidMap {
    /** */
    private static Map<String, String> id2NameMap = new HashMap<String, String>();

    /** */
    private static Map<String, String> name2IdMap = new HashMap<String, String>();

    /** */
    private static Logger logger = ASN1Type.logger;

    /** */
    static {
        try {
            String oidmapFile = System.getProperty("org.jstk.asn1.oidmap");
            if (oidmapFile == null) {
                oidmapFile = "org/jstk/asn1/oid.names";
            }
            logger.fine("oidmap file: " + oidmapFile);
            ClassLoader loader = (new OidMap()).getClass().getClassLoader();
            InputStream is = loader.getResourceAsStream(oidmapFile);
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                Enumeration<?> enumeration = props.propertyNames();
                while (enumeration.hasMoreElements()) {
                    String id = (String) enumeration.nextElement();
                    String name = props.getProperty(id);
                    id2NameMap.put(id, name.toUpperCase());
                    name2IdMap.put(name.toUpperCase(), id);
                }
            } else {
                logger.warning("cannot read oimap file: " + oidmapFile);
            }
        } catch (Exception e) {
            logger.warning("OidMap initialization failed. Exception: " + e);
        }
    }

    /** */
    protected OidMap() {
    }

    /** */
    public static String getName(String id) {
        String name = id2NameMap.get(id);
        if (name == null) {
            name = id;
        }
        return name;
    }

    /** */
    public static String getId(String name) {
        return name2IdMap.get(name.toUpperCase());
    }

    /** */
    public static void main(String[] args) {
        System.out.println("id2NameMap::" + id2NameMap.toString());
        System.out.println("name2IdMap::" + name2IdMap.toString());
    }
}

/* */
