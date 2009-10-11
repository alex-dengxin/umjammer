/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.test;

import java.io.IOException;
import java.util.Queue;

import vavi.io.IODevice;


/**
 * 
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	030331	nsano	initial version <br>
 */
public class SharedMemoryDevice implements IODevice {

    /** */
    private Queue<Integer> inputFifo;

    /** */
    private Queue<Integer> outputFifo;

    /** */
    public SharedMemoryDevice(String reader, String writer) {
        inputFifo = SharedFifo.<Integer>newInstance(reader);
        outputFifo = SharedFifo.<Integer>newInstance(writer);
    }

    /** */
    public int read() throws IOException {
        while (inputFifo.peek() == null) {	// TODO
            Thread.yield();
            try { Thread.sleep(20); } catch (Exception e) {}
//Thread.currentThread().getThreadGroup().list();
        }

        int c = inputFifo.poll();
//System.err.println(StringUtil.toHex2(c));
        return c;
    }

    /** */
    public int available() throws IOException {
        return inputFifo.size();
    }

    /** */
    public void write(int b) throws IOException {
//Debug.println(String.valueOf((char) b));
        outputFifo.offer(b);
    }

    /** */
    public void flush() throws IOException {
//Debug.println("<< " + outputFifo.size() + ", >> " + inputFifo.size());
    }

    /** */
    public void close() throws IOException {
    }
}

/* */
