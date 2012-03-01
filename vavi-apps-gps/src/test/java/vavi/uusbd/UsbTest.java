/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.uusbd;

import java.io.IOException;

import org.junit.Test;

import vavi.util.StringUtil;

import static org.junit.Assert.fail;


/**
 * UsbTest. 
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2011/11/03 umjammer initial version <br>
 */
public class UsbTest {

    @Test
    public void test() {
        fail("Not yet implemented");
    }

    // ------------------------------------------------------------------------

    /** */
    public static void main(String[] args) throws IOException {

//        String command = args[0];
        String command = "!PUON\r\n";

        Usb usb = new Usb();
        usb.sendClassRequest(false,
                             Usb.RECIPIENT_INTERFACE,
                             0x09,
                             0x100,
                             0,
                             8,
                             command.getBytes());

        Pipe pipe = new Pipe(usb, 0, 0);
//      pipe.write(command.getBytes(), 0, command.getBytes().length);

        byte[] buf = new byte[8];
        while (true) {
            int l = pipe.read(buf, 0, 8);
System.err.println("got: " + l);
System.err.println(StringUtil.getDump(buf));
try{ Thread.sleep(1000); } catch (Exception e) {}
        }
    }
}

/* */
