/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.microedition.midlet;

import vavi.microedition.lcdui.Resource;
import vavi.microedition.security.SecurityToken;


/**
 * MIDletSuite.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040920 nsano initial version <br>
 */
public class MIDletSuite {

    /**
     * @param key
     * @return String
     */
    public String getProperty(String key) {
        return Resource.getString(key);
    }

    /**
     * @param midp
     */
    public void checkIfPermissionAllowed(String midp) {
        SecurityToken s = new SecurityToken();
        s.checkIfPermissionAllowed(midp);
    }
}

/* */
