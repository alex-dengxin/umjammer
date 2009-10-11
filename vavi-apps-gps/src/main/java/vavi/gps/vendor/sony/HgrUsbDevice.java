/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.sony;

import java.io.IOException;

import vavi.io.UsbDevice;
import vavi.uusbd.Usb;


/**
 * HgrUsbDevice
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030322 nsano initial version <br>
 *          0.01 030323 nsano rename, extend UsbDevice <br>
 */
public class HgrUsbDevice extends UsbDevice {

    /** */
    public static final int VENDOR_ID_SONY = 0x54c;

    /** */
    public static final int PRODUCT_ID_HGR1 = 0x001d;
    /** */
    public static final int PRODUCT_ID_HGR3 = 0x0040;

    /** */
    public HgrUsbDevice(String name) throws IOException {
    }

    /** */
    protected int getMaxPacketLength() {
        return 8;
    }

    /** */
    protected void writeFromFifo() throws IOException {

        byte[] buf = new byte[packetLength];

        for (int i = 0; i < packetLength; i++) {
            buf[i] = (byte) outputFifo.poll().intValue();
        }

        usb.sendClassRequest(false,
                             Usb.RECIPIENT_INTERFACE,
                             0x09,
                             0x100,
                             0,
                             packetLength,
                             buf);
    }
}

/* */
