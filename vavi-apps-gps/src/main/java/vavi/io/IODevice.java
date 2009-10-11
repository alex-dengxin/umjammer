/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.io;

import java.io.IOException;


/**
 * IODevice.
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	030323	nsano	initial version <br>
 */
public interface IODevice {

    /** */
    int read() throws IOException;

    /** */
    int available() throws IOException;

    /** */
    void write(int c) throws IOException;

    /** */
    void flush() throws IOException;

    /** */
    void close() throws IOException;
}

/* */
