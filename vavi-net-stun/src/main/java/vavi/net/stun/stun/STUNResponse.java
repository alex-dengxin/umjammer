/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.stun;

import java.net.InetAddress;


/**
 * @author suno
 * Created on 2003/07/07
 */
public class STUNResponse {
    /** */
    private InetAddress localIp;
    /** */
    private InetAddress remoteIp;

    /** */
    private int localPort;
    /** */
    private int remotePort;

    /** */
    public STUNResponse(InetAddress localIp, int localPort, InetAddress remoteIp, int remotePort) {
        this.localIp = localIp;
        this.remoteIp = remoteIp;
        this.localPort = localPort;
        this.remotePort = remotePort;
    }

    /** */
    public InetAddress getLocalIp() {
        return localIp;
    }

    /** */
    public InetAddress getRemoteIp() {
        return remoteIp;
    }

    /** */
    public int getLocalPort() {
        return localPort;
    }

    /** */
    public int getRemotePort() {
        return remotePort;
    }

    /** */
    public String toString() {
        return "Router[" + localIp.getCanonicalHostName() + ":" + localPort + "] Server[" + remoteIp.getCanonicalHostName() + ":" + remotePort + "]";
    }
}

/* */
