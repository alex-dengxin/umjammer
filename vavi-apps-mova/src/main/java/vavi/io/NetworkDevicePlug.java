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
 * NetwotkDevicePlug.
 *		
 * @author	<a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version	0.00	040309	nsano	initial version <br>
 */
public class NetworkDevicePlug extends BasicDevicePlug {

    /** */
    private String ioDeviceClass = "vavi.io.InetServerDevice";

    /** */
    private String ioDeviceName = "6900";

    /** */
    protected String getIODeviceClass() {
        return ioDeviceClass;
    }

    /** */
    protected String getIODeviceName() {
        return ioDeviceName;
    }

    /** */
    public NetworkDevicePlug(String name) {
        super(name);

        try {
            final String path = "network.properties";
            Properties props = new Properties();
            props.load(NetworkDevicePlug.class.getResourceAsStream(path));

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
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
