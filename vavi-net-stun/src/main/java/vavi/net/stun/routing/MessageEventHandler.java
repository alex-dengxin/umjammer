/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.routing;

import java.io.IOException;

import vavi.net.stun.NodeInfo;
import vavi.net.stun.messaging.ID;
import vavi.net.stun.messaging.Message;
import vavi.net.stun.network.Connection;



/**
 * @author suno
 * Created on 2003/07/01
 */
public interface MessageEventHandler {
    /** */
    public void onMessageArrived(Connection connection, Message message) throws IOException;

    /** */
    public void onConnectionEstablished(Connection connection, ID id, NodeInfo nodeInfo) throws IOException;

    /** */
    public void onDisconnect(Connection connection) throws IOException;
}

/* */
