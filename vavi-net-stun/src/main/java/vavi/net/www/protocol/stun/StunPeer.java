/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.www.protocol.stun;

import vavi.net.www.protocol.p2p.Peer;


/**
 * StunPeer. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051124 nsano initial version <br>
 */
public class StunPeer implements Peer {

    /** */
    private String name;

    /** */
    public StunPeer(String name) {
        setName(name);
    }

    /* */
    public String getName() {
        return name;
    }

    /* */
    public void setName(String name) {
        this.name = name;
    }
}

/* */
