/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;


/**
 * NodeInfo.
 * <p>
 * "ip:tcpPort:udpServerListPort:stunPort1:stunPort2"
 * </p>
 * @author suno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030704 suno original version <br>
 *          1.00 050728 nsano refine <br>
 */
public class NodeInfo {
    /** */
    private InetAddress ipAddress;

    /** */
    private int tcpConnectionPort;

    /** */
    private int udpServerListPort;

    /** */
    private int udpSTUNPort1;

    /** */
    private int udpSTUNPort2;

    /** */
    private static final String DELIMITER = ":";

    /** */
    public NodeInfo(String ip, int tcpPort, int udpServerListPort, int udpSTUNPort1, int udpSTUNPort2) {
        try {
            this.ipAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e);
        }
        this.tcpConnectionPort = tcpPort;
        this.udpServerListPort = udpServerListPort;
        this.udpSTUNPort1 = udpSTUNPort1;
        this.udpSTUNPort2 = udpSTUNPort2;
    }

    /** */
    public NodeInfo(InetAddress ip, int tcpPort, int udpServerListPort, int udpSTUNPort1, int udpSTUNPort2) {
        this.ipAddress = ip;
        this.tcpConnectionPort = tcpPort;
        this.udpServerListPort = udpServerListPort;
        this.udpSTUNPort1 = udpSTUNPort1;
        this.udpSTUNPort2 = udpSTUNPort2;
    }

    /** */
    public boolean equals(Object object) {
        if (!(object instanceof NodeInfo)) {
            return false;
        }
        NodeInfo someNodeInfo = (NodeInfo) object;
        return ipAddress.equals(someNodeInfo.ipAddress) &&
               tcpConnectionPort == someNodeInfo.tcpConnectionPort &&
               udpServerListPort == someNodeInfo.udpServerListPort &&
               udpSTUNPort1 == someNodeInfo.udpSTUNPort1 &&
               udpSTUNPort2 == someNodeInfo.udpSTUNPort2;
    }

    /** */
    public InetAddress getIP() {
        return ipAddress;
    }

    /** */
    public int getTCPConnectionPort() {
        return tcpConnectionPort;
    }

    /** */
    public int getServerListPort() {
        return udpServerListPort;
    }

    /** */
    public int getSTUNPort1() {
        return udpSTUNPort1;
    }

    /** */
    public int getSTUNPort2() {
        return udpSTUNPort2;
    }

    /**
     * @return "ip:tcpPort:udpServerListPort:stunPort1:stunPort2" 
     */
    public String toString() {
        String nodeInfo = ipAddress.getHostAddress();
        nodeInfo += DELIMITER + getTCPConnectionPort();
        nodeInfo += DELIMITER + getServerListPort();
        nodeInfo += DELIMITER + getSTUNPort1();
        nodeInfo += DELIMITER + getSTUNPort2();
        return nodeInfo;
    }

    /**
     * @param nodeInfo "ip:tcpPort:udpServerListPort:stunPort1:stunPort2"
     */
    public static NodeInfo parseNodeInfo(String nodeInfo) {
        try {
            StringTokenizer st = new StringTokenizer(nodeInfo, DELIMITER);
            InetAddress ip = InetAddress.getByName(st.nextToken());
            int tcpPort = Integer.parseInt(st.nextToken());
            int udpServerListPort = Integer.parseInt(st.nextToken());
            int stunPort1 = Integer.parseInt(st.nextToken());
            int stunPort2 = Integer.parseInt(st.nextToken());
            NodeInfo someNodeInfo = new NodeInfo(ip, tcpPort, udpServerListPort, stunPort1, stunPort2);
            return someNodeInfo;
        } catch (UnknownHostException e) {
            throw (RuntimeException) new IllegalArgumentException(nodeInfo).initCause(e);
        } catch (NumberFormatException e) {
            throw (RuntimeException) new IllegalArgumentException(nodeInfo).initCause(e);
        }
    }
}

/* */
