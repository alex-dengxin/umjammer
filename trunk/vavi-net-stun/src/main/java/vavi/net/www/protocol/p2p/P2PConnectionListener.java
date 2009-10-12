/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.www.protocol.p2p;


/**
 * P2PConnectionListener. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051123 nsano initial version <br>
 */
public interface P2PConnectionListener {
    /** TODO naming */
    void dataArrived(P2PConnectionEvent event);

    /** TODO naming */
    void newNode(P2PConnectionEvent event);

    /** */
    void connected(P2PConnectionEvent event);
}

/** */
