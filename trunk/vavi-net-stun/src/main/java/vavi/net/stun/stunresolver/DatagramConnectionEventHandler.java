/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.stunresolver;

import vavi.net.stun.messaging.ID;


/**
 * @author suno
 * Created on 2003/07/01
 */
public interface DatagramConnectionEventHandler {
    /** */
    void onDatagramConnectionEstablished(DatagramConnection connection);

    /** */
    void onDatagramConnectionFailed(ID id);
}

/* */
