/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;


/**
 * IODeviceInputStream.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030323 nsano initial version <br>
 *          0.01 030325 nsano add readLine <br>
 *          0.02 030326 nsano be real class <br>
 *          0.03 030331 nsano add readLine <br>
 */
public class IODeviceInputStream extends InputStream {

    /** */
    private IODevice device;

    /** */
    public IODeviceInputStream(IODevice device) {
        this.device = device;
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
    public void close() throws IOException {
        device.close();
    }

    /** */
    public String readLine() throws IOException {
        return readLine(this);
    }

    /** */
    public static String readLine(InputStream is) throws IOException {

        Queue<Integer> fifo = new LinkedList<Integer>();

        int flag = 1;

        while (flag > 0) {
            int c = is.read();

            if (c == 0x00) {
                continue;
            } else if (c == 0x0d) {
                flag = 2;
            } else if (c == 0x0a) {
                if (flag == 2) {
                    flag = 0; // terminate while
                    break;
                } else {
                    flag = 1;
                    fifo.offer(new Integer(c));
                }
            } else {
                if (flag == 2) {
                    fifo.offer(new Integer(0x0d));
                }
                flag = 1;
                fifo.offer(new Integer(c));
            }
        }

        int size = fifo.size();
        byte[] buf = new byte[size];
        for (int i = 0; i < size; i++) {
            buf[i] = (byte) fifo.poll().intValue();
        }

        return new String(buf);
    }
}

/* */
