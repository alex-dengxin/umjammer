/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.sony;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Test;

import vavi.io.IODeviceOutputStream;

import static org.junit.Assert.fail;


/**
 * HgrTest. 
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2011/11/03 umjammer initial version <br>
 */
public class HgrTest {

    @Test
    public void test() {
        fail("Not yet implemented");
    }

    //-------------------------------------------------------------------------

    static class HackedHgr extends Hgr {
        public HackedHgr(String name) {
            super(name);
        }
        public void makeSureInputStreamOpened() {
            super.makeSureInputStreamOpened();
        }
        public void makeSureOutputStreamOpened() {
            super.makeSureOutputStreamOpened();
        }
        public IODeviceOutputStream getOut() {
            return os;
        }
    }

    /** */
    public static void main(String[] args) throws Exception {
        HackedHgr hgr = new HackedHgr(args[0]);
        hgr.start();
        hgr.makeSureInputStreamOpened();
        hgr.makeSureOutputStreamOpened();
        BufferedReader r =
            new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.err.print("> ");
            String line = r.readLine();
            if (line != null) {
                hgr.getOut().writeLine(line);
            }
        }
    }
}

/* */
