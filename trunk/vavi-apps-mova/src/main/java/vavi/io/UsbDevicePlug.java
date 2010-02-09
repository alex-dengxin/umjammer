/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.io;

import java.io.IOException;
import java.util.Properties;
import vavi.util.Debug;


/**
 * UsbDevicePlug.
 *		
 * @author	<a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version	0.00	040309	nsano	initial version <br>
 */
public class UsbDevicePlug extends BasicDevicePlug {

    /** */
    private String ioDeviceClass = "vavi.io.UsbDevice";

    /** */
    private String ioDeviceName = "default";

    /** */
    protected String getIODeviceClass() {
        return ioDeviceClass;
    }

    /** */
    protected String getIODeviceName() {
        return ioDeviceName;
    }

    /** */
    public UsbDevicePlug(String name) {
        super(name);
Debug.println("name: " + name);

        try {
            final String path = "usb.properties";
            Properties props = new Properties();
            props.load(UsbDevicePlug.class.getResourceAsStream(path));

            String key = "ioDevice.class";
            String value = props.getProperty(key);
            if (value != null) {
                ioDeviceClass = value;
Debug.println("ioDevice: " + ioDeviceClass);
            }

            key = "ioDevice.name";
            value = props.getProperty(key);
            if (value != null) {
                ioDeviceName = value;
Debug.println("name: " + ioDeviceName);
            }
        } catch (IOException e) {
Debug.printStackTrace(e);
            throw new InternalError(e.toString());
        }
    }
}

/* */
