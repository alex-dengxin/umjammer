/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.nmea;

import java.io.IOException;
import java.util.Properties;
import vavi.gps.BasicGpsDevice;
import vavi.gps.GpsFormat;
import vavi.util.Debug;


/**
 * NMEA device.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030320 nsano initial version <br>
 *          0.01 030326 nsano specification compliant <br>
 *          0.02 030326 nsano extends BasicGpsDevice <br>
 */
public class Nmea extends BasicGpsDevice {

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
    private GpsFormat gpsFormat = new NmeaGpsFormat();

    /** */
    protected GpsFormat getGpsFormat() {
        return gpsFormat;
    }

    /** */
    public Nmea(String name) {
        super(name);

        try {
            Properties props = new Properties();

            props.load(Nmea.class.getResourceAsStream("Nmea.properties"));

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
        }
        catch (IOException e) {
Debug.printStackTrace(e);
            throw new InternalError(e.toString());
        }
    }
}

/* */
