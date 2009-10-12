/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.network;

import java.io.IOException;


/**
 * @author suno
 * Created on 2003/06/29
 */
public interface NetworkEventHandler {
    /** */
    void onConnectionEstablished(Connection connection) throws IOException;

    /** */
    void onPacketArrived(Connection connection, byte[] data) throws IOException;

    /** */
    void onDisconnect(Connection connection) throws IOException;
}

/* */
