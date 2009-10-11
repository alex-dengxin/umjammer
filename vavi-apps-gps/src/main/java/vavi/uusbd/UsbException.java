/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.uusbd;

import java.io.IOException;

/**
 * UsbException.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030314 nsano initial version <br>
 */
public class UsbException extends IOException {

    /** */
    public UsbException() {
        super();
    }

    /** */
    public UsbException(String message) {
        super(message);
    }
}

/* */
