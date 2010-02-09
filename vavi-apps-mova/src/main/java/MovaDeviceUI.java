/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;


/**
 * MovaDeviceUI.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version 0.00 040313 nsano initial version <br>
 */
public class MovaDeviceUI extends DeviceUI {

    /** */
    public MovaDeviceUI(String serialPort) throws IOException {
        super(new MovaDevice(serialPort));
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        MovaDeviceUI ui = new MovaDeviceUI(args[0]);
    }
}

/* */
