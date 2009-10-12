/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.www.protocol.stun;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import vavi.net.stun.Node;
import vavi.net.stun.NodeInfo;
import vavi.net.stun.messaging.ID;
import vavi.net.stun.messaging.Message;
import vavi.net.stun.stun.STUNCheckServer.StunType;
import vavi.net.stun.stunresolver.DatagramConnection;
import vavi.net.www.protocol.p2p.P2PConnectionEvent;
import vavi.net.www.protocol.p2p.P2PURLConnection;
import vavi.util.Debug;


/**
 * StunURLConnection. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051011 nsano initial version <br>
 */
public class StunURLConnection extends P2PURLConnection {

    /** */
    private static final String NOTIFYPRESENCE = "NOTIFYMYPRESENCE";

    /** */
    StunURLConnection(URL url, String id, NodeInfo[] nodeInfos) {
        this(url, id, nodeInfos, null);
    }

    /** */
    StunURLConnection(URL url, String id, NodeInfo[] nodeInfos, StunType type) {
        super(url);
Debug.println("enter <init>");
        //
        if (type == null) {
            this.node = new MyNode(ID.getID(id.getBytes()), nodeInfos);
        } else {
            this.node = new MyNode(ID.getID(id.getBytes()), nodeInfos, type);
        }

        //
        try {
            node.sendBroadcastMessage(NOTIFYPRESENCE.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
Debug.println("exit <init>");
    }

    /** */
    private class MyNode extends Node {

        /** */
        public MyNode(ID id, NodeInfo[] servers) {
            super(id, servers);
            NodeInfo nodeInfo = getLocalNodeInfo();
System.err.println("started at:" + nodeInfo.toString());
        }

        /** */
        public MyNode(ID id, NodeInfo[] servers, StunType type) {
            super(id, servers, type);
            NodeInfo nodeInfo = getLocalNodeInfo();
System.err.println("started at:" + nodeInfo.toString());
        }

        /** */
        public void onDatagramConnectionEstablished(DatagramConnection connection) {
            connectionSupport.connected(new P2PConnectionEvent(this, new StunP2PConnection(connection, StunURLConnection.this)));
System.err.println("connected: " + connection);
        }

        /** */
        public void onDatagramConnectionFailed(ID id) {
            connectionSupport.connected(null);
System.err.println("connection failed: " + id);
        }

        /** */
        public void onMessageArrived(Message message) {
            if (!(new String(message.getData()).equals(NOTIFYPRESENCE))) {
                connectionSupport.dataArrived(new P2PConnectionEvent(this, message.getData()));
            } else if (message.getDestination().equals(ID.BROADCASTID)) {
                try {
                    node.sendUnicastMessage(NOTIFYPRESENCE.getBytes(), message.getSource());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            connectionSupport.newNode(new P2PConnectionEvent(this, new StunPeer(message.getSource().toString())));
System.err.println("new node: " + message.getSource());
        }
    }

    /** */
    private Node node;

    /** */
    Node getNode() {
        return node;
    }

    /** */
    private String id;

    /**
     * special connect method
     * TODO use Peer as an argment & move to super class
     */
    public synchronized void connect(String id) throws IOException {
        this.id = id;
        connect();
        this.id = null;
    }

    /** TODO be unsupported */
    public void connect() throws IOException {
Debug.println("enter connect");
        if (id == null) {
            throw new IllegalStateException("id is not specified");
        }
Debug.println("id: " + id + ", connection: " + this.hashCode());
        node.connectTo(ID.getID(id.getBytes()));
Debug.println("exit connect");
    }

    /**
     * multicast
     * TODO move to super class ???
     * @throws UnsupportedOperationException
     */
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("not suppoerted");
    }

    /** multicast */
    public OutputStream getOutputStream() throws IOException {
        return new StunMulticastOutputStream(node);
    }
}

/* */
