/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.network;

import java.io.IOException;

import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * @author suno
 * Created on 2003/07/01
 */
public class NullNetworkEventHandler implements NetworkEventHandler {
    /** */
    public void onConnectionEstablished(Connection connection) throws IOException {
Debug.println(connection);
    }

    /** */
    public void onPacketArrived(Connection connection, byte[] data) throws IOException {
Debug.println(connection + ": " + StringUtil.getDump(data));
    }

    /** */
    public void onDisconnect(Connection connection) throws IOException {
Debug.println(connection);
    }
}

/* */
