/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.karaoke;

import junit.framework.TestCase;


/**
 * JmfTest. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080906 nsano initial version <br>
 */
public class JmfTest extends TestCase {

    public void test01() throws Exception {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
System.err.println("system cl: " + cl);

        ClassLoader cl2 = cl.getParent();
System.err.println("system parent cl: " + cl2);
//        cl2.xs

        ClassLoader cl3 = cl2.getParent();
System.err.println("system grand parent cl: " + cl3);
    }

    public void test02() throws Exception {
//        String prefix = "javax.tv.media";
//        Package p = Package.getPackage(prefix);
        Package[] ps = Package.getPackages();
        for (int i = 0; i < ps.length; i++) {
//            if (ps[i].getName().indexOf("media") != -1) {
System.err.println(i + ": " + ps[i]);
//            }
        }
    }
}

/* */
