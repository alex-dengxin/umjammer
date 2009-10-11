/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.gps;

import java.io.IOException;

import vavi.io.IODevice;
import vavi.util.Debug;


/**
 * HgrDevice
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030327 nsano initial version <br>
 *          0.01 030331 nsano be generic <br>
 */
public class HgrDevice implements IODevice {

    /** */
    private HgrDevice() throws IOException {
    }

    /** */
    private static IODevice device;

    /** */
    public static final IODevice getInstance() {
        try {
            if (device == null) {
//	            device = new vavi.gps.vendor.test.DummyHgrDevice("HGR3S");
                device = new vavi.gps.vendor.sony.HgrUsbDevice("HGR3S");
            }
            return device;
        } catch (Exception e) {
Debug.println(e);
            throw new InternalError(e.getMessage());
        }
    }

    /** */
    public int read() throws IOException {
        return device.read();
    }

    /** */
    public int available() throws IOException {
        return device.available();
    }

    /** */
    public void write(int c) throws IOException {
        device.write(c);
    }

    /** */
    public void flush() throws IOException {
        device.flush();
    }

    /** */
    public void close() throws IOException {
        device.close();
    }
}

/* */
