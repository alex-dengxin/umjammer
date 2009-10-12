/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.www.protocol.p2p;

import java.util.EventObject;


/**
 * P2PConnectionListener. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051123 nsano initial version <br>
 */
public class P2PConnectionEvent extends EventObject {

    /**
     * @param peer the peer to set
     */
    public P2PConnectionEvent(Object source, Peer peer) {
        super(source);
        this.peer = peer;
    }

    /**
     * @param data the data to set
     */
    public P2PConnectionEvent(Object source, Object data) {
        super(source);
        this.data = data;
    }

    /**
     * @param connection the connection to set
     */
    public P2PConnectionEvent(Object source, P2PConnection connection) {
        super(source);
        this.connection = connection;
    }

    /** */
    private Object data;

    /** */
    private Peer peer;

    /** */
    private P2PConnection connection;

    /**
     * @return the data
     */
    public Object getData() {
        return data;
    }

    /**
     * @return the peer
     */
    public Peer getPeer() {
        return peer;
    }

    /**
     * @return the connection
     */
    public P2PConnection getConnection() {
        return connection;
    }
}

/** */
