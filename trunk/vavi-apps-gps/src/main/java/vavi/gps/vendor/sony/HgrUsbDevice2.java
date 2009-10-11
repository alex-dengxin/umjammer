/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.sony;

import java.io.IOException;

import vavi.gps.io.JUsbDevice;


/**
 * HgrUsbDevice2
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040628 nsano initial version <br>
 */
public class HgrUsbDevice2 extends JUsbDevice {

    /** */
    public static final int VENDOR_ID_SONY = 0x54c;

    /** */
    public static final int PRODUCT_ID_HGR3 = 0x0040;

    /** */
    public HgrUsbDevice2(String name) throws IOException {
    }

    /** */
    protected int getMaxPacketLength() {
        return 8;
    }
}

/* */
