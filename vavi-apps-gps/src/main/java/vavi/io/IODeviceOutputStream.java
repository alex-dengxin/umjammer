/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.io;

import java.io.OutputStream;
import java.io.IOException;


/**
 * IODeviceOutputStream
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030323 nsano initial version <br>
 *          0.01 030325 nsano add writeLine(os, b) <br>
 *          0.02 030326 nsano be real class <br>
 *          0.03 030325 nsano add writeLine(os) <br>
 *          0.04 030331 nsano add writeLine(b), writeLine() <br>
 */
public class IODeviceOutputStream extends OutputStream {

    /** */
    private IODevice device;

    /** */
    public IODeviceOutputStream(IODevice device) {
        this.device = device;
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

    /** */
    public void writeLine(String b) throws IOException {
        writeLine(this, b);
    }

    /** */
    public void writeLine() throws IOException {
        writeLine(this);
    }

    /** */
    public static void writeLine(OutputStream os, String b) throws IOException {

        os.write(b.getBytes(), 0, b.getBytes().length);
        writeLine(os);
    }

    /** */
    public static void writeLine(OutputStream os) throws IOException {

        os.write('\r');
        os.write('\n');
        os.flush();
    }
}

/* */
