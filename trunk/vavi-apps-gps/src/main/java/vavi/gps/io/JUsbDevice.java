/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.io;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import vavi.io.IODevice;


/**
 * JUsbDevice.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040628 nsano initial version <br>
 */
public abstract class JUsbDevice implements IODevice {

    /** */
    public JUsbDevice() throws IOException {
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

        int c = inputFifo.poll();
//System.err.println(StringUtil.toHex2(c));
        return c;
    }

    /** */
    protected void readToFifo() throws IOException {

//      byte[] buf = new byte[packetLength];
//      int l = pipe.read(buf, 0, packetLength);
        
//      for (int i = 0; i < l; i++) {
//          inputFifo.push(buf[i]);
//      }
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

//      if (outputFifo.size() == packetLength) {
//          writeFromFifo();
//      }
    }

    /** */
    protected void writeFromFifo() throws IOException {

//      byte[] buf = new byte[packetLength];
        
//      for (int i = 0; i < packetLength; i++) {
//          buf[i] = (byte) outputFifo.pop();
//      }

//      pipe.write(buf, 0, packetLength);
    }

    /** */
    public void flush() throws IOException {
        if (outputFifo.poll() != null) {
//          for (int i = outputFifo.size(); i < packetLength; i++) {
//              outputFifo.push(0);
//          }
//          writeFromFifo();
        }
    }

    /** */
    public void close() throws IOException {
//      pipe.close();
//      usb.close();
    }
}

/* */
