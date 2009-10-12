/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import vavi.io.AbstractDevicePlug;
import vavi.io.BasicDevicePlug;
import vavi.util.Debug;


/**
 * t907_1
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 040309 nsano initial version <br>
 */
public class t907_1 {
    /** */
    private String inputClass = "vavi.io.NetworkDevicePlug";

    /** */
    private String inputName = "6900";

    /** */
    private String outputClass = "vavi.io.SerialPortDevicePlug";

    /** */
    private String outputName = "COM1";

    /** */
    private AbstractDevicePlug inputDevice;

    /** */
    private AbstractDevicePlug outputDevice;

    /** */
    public t907_1() {
        try {
            final String path = "sniffer.properties";
            Properties props = new Properties();
            props.load(t907_1.class.getResourceAsStream(path));

            String value = props.getProperty("inputDevice.class");

            if (value != null) {
                inputClass = value;
Debug.println("input: " + inputClass);
            }

            value = props.getProperty("inputDevice.name");

            if (value != null) {
                inputName = value;
Debug.println("input: " + inputName);
            }

            inputDevice = BasicDevicePlug.newInstance(inputClass, inputName);

            value = props.getProperty("outputDevice.class");

            if (value != null) {
                outputClass = value;
Debug.println("output: " + outputClass);
            }

            value = props.getProperty("outputDevice.name");

            if (value != null) {
                outputName = value;
Debug.println("output: " + outputName);
            }

            outputDevice = BasicDevicePlug.newInstance(outputClass, outputName);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
Debug.printStackTrace(t);
            throw (RuntimeException) new IllegalStateException().initCause(t);
        } catch (Exception e) {
Debug.printStackTrace(e);
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }

        // start
        inputDevice.connect(outputDevice);
        inputDevice.start();
        outputDevice.start();
    }

    //-------------------------------------------------------------------------

    /** */
    public static void main(String[] args) {
        try {
            new t907_1();
        } catch (Throwable e) {
Debug.printStackTrace(e);
            System.exit(1);
        }
    }
}

/* */
