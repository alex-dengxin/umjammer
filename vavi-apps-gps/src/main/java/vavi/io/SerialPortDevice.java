/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import vavi.util.Debug;


/**
 * SerialPortDevice
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030322 nsano initial version <br>
 */
public class SerialPortDevice implements IODevice {

    /** */
    private String serialName = "COM1";

    /** */
    private SerialPort serialPort;

    /** */
    private InputStream is;

    /** */
    private OutputStream os;

    /**
     * TODO read from file, COM port properties (4800, 8, 1, none, ...)
     * 
     * @param name シリアルポート番号を指定します
     */
    public SerialPortDevice(String name) {

        this.serialName = name;

        Enumeration<?> e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) e.nextElement();
            Debug.println("type: " + portId.getPortType());
            Debug.println("name: [" + portId.getName() + "]");
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL && portId.getName().equals(serialName)) {
                try {
                    serialPort = (SerialPort) portId.open(getClass().getName(), 2000);
                    serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    is = serialPort.getInputStream();
                    os = serialPort.getOutputStream();
                    return;
                } catch (Exception f) {
                    throw new InternalError(f.toString());
                }
            }
        }

        throw new InternalError("no such port: " + serialName);
    }

    // -------------------------------------------------------------------------

    /** */
    public int read() throws IOException {
        return is.read();
    }

    /** */
    public int available() throws IOException {
        return is.available();
    }

    /** */
    public void write(int b) throws IOException {
        os.write(b);
    }

    /** */
    public void flush() throws IOException {
        os.flush();
    }

    /** */
    public void close() throws IOException {
        is.close();
        os.close();
        serialPort.close();
    }
}

/* */
