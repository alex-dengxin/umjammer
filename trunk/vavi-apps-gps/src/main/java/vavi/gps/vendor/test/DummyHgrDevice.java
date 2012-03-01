/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.test;

import java.util.Properties;

import vavi.util.Debug;


/**
 * HGR ‚Ì
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030324 nsano initial version <br>
 *          0.01 030328 nsano add HGR emulation <br>
 *          0.02 030331 nsano separate HGR emulation <br>
 *          0.03 030331 nsano separate IODevice <br>
 */
public class DummyHgrDevice extends SharedMemoryDevice {

    /** */
    private static String emulatorClass = "vavi.gps.vendor.test.HgrEmulator1";

    /** */
    public DummyHgrDevice(String name) {

        super("input", "output");

        final Class<?> c = DummyHgrDevice.class;

        try {
            Properties props = new Properties();

            props.load(c.getResourceAsStream("DummyHgrDevice.properties"));

            String value = props.getProperty("emulator.class");
            if (value != null) {
                emulatorClass = value;
            }

            @SuppressWarnings("unused")
            HgrEmulator emulator = (HgrEmulator) Class.forName(emulatorClass).newInstance();
        } catch (Exception e) {
Debug.printStackTrace(e);
            throw new InternalError(e.toString());
        }
    }
}

/* */
