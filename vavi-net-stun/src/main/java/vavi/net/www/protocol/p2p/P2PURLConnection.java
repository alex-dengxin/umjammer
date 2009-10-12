/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.www.protocol.p2p;

import java.net.URL;
import java.net.URLConnection;


/**
 * P2PURLConnection. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051011 nsano initial version <br>
 */
public abstract class P2PURLConnection extends URLConnection {

    /** */
    protected P2PURLConnection(URL url) {
        super(url);
    }

    /** */
    protected P2PConnectionSupport connectionSupport = new P2PConnectionSupport();

    /** */
    public void addP2PConnectionListener(P2PConnectionListener connectionListener) {
        connectionSupport.addP2PConnectionListener(connectionListener);
    }

    /** */
    public void removeP2PConnectionListener(P2PConnectionListener connectionListener) {
        connectionSupport.removeP2PConnectionListener(connectionListener);
    }
}

/* */
