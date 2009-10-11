/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.gps;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import vavi.io.IODeviceInputStream;


/**
 * HgrInputStream.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030327 nsano initial version <br>
 */
public class HgrInputStream extends IODeviceInputStream {

    /** */
    public HgrInputStream() {
        super(HgrDevice.getInstance());
    }

    /** */
    private Queue<String> fifo = new LinkedList<String>();

    /** */
    public String readLine() throws IOException {
        while (true) {
            String line = super.readLine();
            if (line.startsWith("SM00")) {
                fifo.offer(line);
            }
            else {
                return line;
            }
        }
    }

    /** */
    public String readGPSLine() throws IOException {
        while (fifo.peek() == null) {
            Thread.yield();
            try { Thread.sleep(20); } catch (Exception e) {}
        }

        String line = fifo.poll();
        return line;
    }
}

/* */
