/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.test;

import java.io.IOException;
import java.util.Properties;
import vavi.gps.BasicGpsDevice;
import vavi.gps.GpsFormat;
import vavi.util.Debug;


/**
 * Raw GPS device.
 * なるべく inputDevice として設定しないでください。
 * 
 * @todo 汎用化して vavi.gps パッケージへの昇格
 * @see RawGpsFormat#parse(byte[])
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030325 nsano initial version <br>
 *          0.01 030326 nsano extends BasicGpsDevice <br>
 */
public class Raw extends BasicGpsDevice {

    /** */
    private String ioDeviceClass = "vavi.gps.io.InetServerDevice";

    /** */
    private String ioDeviceName = "5750";

    /** */
    protected String getIODeviceClass() {
        return ioDeviceClass;
    }

    /** */
    protected String getIODeviceName() {
        return ioDeviceName;
    }

    /** */
    private GpsFormat gpsFormat = new RawGpsFormat();

    /** */
    protected GpsFormat getGpsFormat() {
        // Debug.println("gpsFormat: " + gpsFormat);
        return gpsFormat;
    }

    /** */
    public Raw(String name) {
        super(name);

        try {
            Properties props = new Properties();

            props.load(Raw.class.getResourceAsStream("Raw.properties"));

            String key = "ioDevice.class." + this.name;
            String value = props.getProperty(key);
            if (value != null) {
                ioDeviceClass = value;
                Debug.println("ioDevice: " + ioDeviceClass);
            }

            key = "ioDevice.name." + this.name;
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

    /** Does nothing. */
    public void start() {
        Debug.println("here");
    }
}

/* */
