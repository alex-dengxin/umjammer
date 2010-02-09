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
 * SerialPortDevicePlug.
 *		
 * @author	<a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version	0.00	040309	nsano	initial version <br>
 */
public class SerialPortDevicePlug extends BasicDevicePlug {

    /** */
    private String ioDeviceClass = "vavi.io.SerialPortDevice";

    /** */
    private String ioDeviceName = "COM1";

    /** */
    protected String getIODeviceClass() {
        return ioDeviceClass;
    }

    /** */
    protected String getIODeviceName() {
        return ioDeviceName;
    }

    /** */
    public SerialPortDevicePlug(String name) {
        super(name);
Debug.println("name: " + name);

        try {
            final String path = "comm.properties";
            Properties props = new Properties();
            props.load(SerialPortDevicePlug.class.getResourceAsStream(path));

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
        }
        catch (IOException e) {
Debug.printStackTrace(e);
            throw new InternalError(e.toString());
        }
    }
}

/* */
