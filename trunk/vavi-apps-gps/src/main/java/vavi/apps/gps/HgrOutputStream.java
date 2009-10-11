/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.gps;

import vavi.io.IODeviceOutputStream;


/**
 * HgrOutputStream
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030327 nsano initial version <br>
 */
public class HgrOutputStream extends IODeviceOutputStream {

    /** */
    public HgrOutputStream() {
        super(HgrDevice.getInstance());
    }
}

/* */
