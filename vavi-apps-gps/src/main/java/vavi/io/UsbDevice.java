/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.io;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import vavi.uusbd.Pipe;
import vavi.uusbd.Usb;


/**
 * UsbDevice.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030322 nsano initial version <br>
 */
public abstract class UsbDevice implements IODevice {

    /** */
    protected Usb usb;

    /** */
    protected Pipe pipe;

    /** */
    protected int packetLength;

    /** */
    public UsbDevice() throws IOException {
        this.usb = new Usb();
        this.pipe = new Pipe(usb, 0, 0);

        this.packetLength = getMaxPacketLength();
    }

    /** */
    protected abstract int getMaxPacketLength();

    /** */
    protected Queue<Integer> inputFifo = new LinkedList<Integer>();

    /** */
    public int read() throws IOException {
        if (inputFifo.peek() == null) {
            readToFifo();
        }

        int c = inputFifo.poll().intValue();
// System.err.println(StringUtil.toHex2(c));
        return c;
    }

    /** */
    protected void readToFifo() throws IOException {

        byte[] buf = new byte[packetLength];
        int l = pipe.read(buf, 0, packetLength);

        for (int i = 0; i < l; i++) {
            inputFifo.offer(new Integer(buf[i]));
        }
    }

    /** */
    public int available() throws IOException {
        return inputFifo.size();
    }

    /** */
    protected Queue<Integer> outputFifo = new LinkedList<Integer>();

    /** */
    public void write(int c) throws IOException {
        outputFifo.offer(c);

        if (outputFifo.size() == packetLength) {
            writeFromFifo();
        }
    }

    /** */
    protected void writeFromFifo() throws IOException {

        byte[] buf = new byte[packetLength];

        for (int i = 0; i < packetLength; i++) {
            buf[i] = (byte) outputFifo.poll().intValue();
        }

        pipe.write(buf, 0, packetLength);
    }

    /** */
    public void flush() throws IOException {
        if (outputFifo.size() > 0) {
            for (int i = outputFifo.size(); i < packetLength; i++) {
                outputFifo.offer(0);
            }
            writeFromFifo();
        }
    }

    /** */
    public void close() throws IOException {
        pipe.close();
        usb.close();
    }
}

/* */
