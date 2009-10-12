/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.www.protocol.p2p;

import java.util.ArrayList;
import java.util.List;


/**
 * ConnectionSupport. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051123 nsano initial version <br>
 */
public class P2PConnectionSupport {
    /** */
    private List<P2PConnectionListener> connectionListeners = new ArrayList<P2PConnectionListener>();

    /** */
    public void addP2PConnectionListener(P2PConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    /** */
    public void removeP2PConnectionListener(P2PConnectionListener connectionListener) {
        connectionListeners.remove(connectionListener);
    }

    /** */
    public synchronized void dataArrived(P2PConnectionEvent event) {
        for (P2PConnectionListener connectionListener : new ArrayList<P2PConnectionListener>(connectionListeners)) {
            connectionListener.dataArrived(event);
        }
    }

    /** */
    public void newNode(P2PConnectionEvent event) {
        for (P2PConnectionListener connectionListener : new ArrayList<P2PConnectionListener>(connectionListeners)) {
            connectionListener.newNode(event);
        }
    }

    /** */
    public void connected(P2PConnectionEvent event) {
        for (P2PConnectionListener connectionListener : new ArrayList<P2PConnectionListener>(connectionListeners)) {
            connectionListener.connected(event);
        }
    }
}

/** */
