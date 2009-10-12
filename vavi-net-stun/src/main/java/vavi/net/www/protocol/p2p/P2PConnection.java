/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.www.protocol.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * P2PConnection. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051011 nsano initial version <br>
 */
public abstract class P2PConnection {

    /** unicast */
    public abstract InputStream getInputStream() throws IOException;

    /** unicast */
    public abstract OutputStream getOutputStream() throws IOException;
}

/* */
