/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.www.protocol.stun;

import java.io.IOException;
import java.io.OutputStream;

import vavi.net.stun.Node;
import vavi.net.stun.messaging.ID;


/**
 * StunOutputStream. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051123 nsano initial version <br>
 */
class StunUnicastOutputStream extends OutputStream {
    /** */
    private Node node;

    /** */
    private ID id;

    /** */
    public StunUnicastOutputStream(Node node, ID id) {
        this.node = node;
        this.id = id;
    }

    /** */
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException("use #write(byte,int,int)V");
    }

    /** */
    public void write(byte[] b, int offset, int length) throws IOException {
        byte[] message = new byte[length];
        System.arraycopy(b, offset, message, 0, length);
        node.sendUnicastMessage(message, id);
    }
}

/* */
