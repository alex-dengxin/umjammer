/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;


/**
 * FomaDeviceUI.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version 0.00 040313 nsano initial version <br>
 */
public class FomaDeviceUI extends DeviceUI {

    /** */
    public FomaDeviceUI(String serialPort) throws IOException {
        super(new FomaDevice(serialPort));
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        FomaDeviceUI ui = new FomaDeviceUI(args[0]);
    }
}

/* */
