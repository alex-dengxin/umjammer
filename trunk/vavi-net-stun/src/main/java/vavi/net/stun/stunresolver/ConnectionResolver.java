/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.stunresolver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import vavi.net.stun.NodeInfo;
import vavi.net.stun.messaging.ID;
import vavi.net.stun.messaging.Message;
import vavi.net.stun.routing.ControlMessageHandler;
import vavi.net.stun.routing.RoutingPolicy;
import vavi.net.stun.stun.STUNCheckServer;
import vavi.net.stun.stun.STUNResponse;
import vavi.util.Debug;



/**
 * @author suno
 * Created on 2003/07/01
 */
public class ConnectionResolver implements ControlMessageHandler {
    private RoutingPolicy policy;

    public ConnectionResolver(RoutingPolicy policy) {
        this.policy = policy;
        STUNConnectionResolver.setRoutingPolicy(policy);
    }

    public void connectTo(ID id) throws IOException {
        STUNConnectionResolver.getDirectConnection(id);
    }

    public void onControlMessageArrived(Message message) {
        STUNConnectionResolver.handleControlMessage(message);
    }
}

/* */


/**
 * STUNConnectionResolver.
 *
 * STUNType 1byte
 * MessageType 1byte
 * LocalPort 4bytes
 * RemotePort 4bytes
 * ip 4bytes
 * port 4bytes
 */
class STUNConnectionResolver {
    /** */
    private static List<STUNConnectionResolver> resolvers = new ArrayList<STUNConnectionResolver>();

    /** */
    private ID remoteID;

    /** */
    private DatagramSocket socket;

    /** */
    private DatagramPacket sendPacket;

    /** */
    private int remotePort = 0;

    /** */
    public static final byte CONNECTIONREQUEST = 1;

    /** */
    public static final byte CONNECTTO = 2;

    /** */
    public static final int HEADERLENGTH = 10;

    /** */
    private static RoutingPolicy policy;

    /** */
    private boolean isListening;

    /** */
    private STUNConnectionResolver(ID remoteID, int remotePort, boolean listeningFlag) {
        this.remotePort = remotePort;
        this.remoteID = remoteID;
        this.isListening = listeningFlag;
        try {
            socket = new DatagramSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** */
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    /** */
    private void sendGeneralConnectionMessage(byte type) throws IOException {
        Message message = this.createResolverMessage(type);
        policy.sendMessage(message);
    }

    /** */
    public Message createResolverMessage(byte messageType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        STUNResponse response = getRouterIP_Port(socket);
Debug.println(response);
Debug.println("Local port: " + socket.getLocalPort());
        dos.writeByte(getLocalSTUNType().getType()); // 0
        dos.writeByte(messageType); // 1
        dos.writeInt(getLocalPort()); // 2
        dos.writeInt(getRemotePort()); // 6
        dos.write(response.getLocalIp().getAddress()); // 10
        if (getLocalSTUNType().equals(STUNCheckServer.StunType.SYMMETRIC)) {
            dos.writeInt(response.getLocalPort() + 1); // 14
        } else {
            dos.writeInt(response.getLocalPort()); // 14
        }
Debug.println("resolver message size:" + dos.size());
        return new Message(getLocalID(), getRemoteID(), Message.Type.CONTROL, baos.toByteArray());
    }

    /** */
    public ID getLocalID() {
        return policy.getLocalID();
    }

    /** */
    public ID getRemoteID() {
        return this.remoteID;
    }

    /** */
    public int getRemotePort() {
        if (sendPacket != null) {
            return sendPacket.getPort();
        } else {
            return remotePort;
        }
    }

    /** */
    private static int getIntFromBytes(byte[] data, int offset) {
        return (((data[offset] & 0xff) << 24) | ((data[offset + 1] & 0xff) << 16) | ((data[offset + 2] & 0xff) << 8) | (data[offset + 3] & 0xff));
    }

    /** */
    public void handleConnectionRequest(Message message) throws IOException {
        byte[] ipBytes = new byte[4];
        System.arraycopy(message.getData(), HEADERLENGTH, ipBytes, 0, 4);
        sendConnectTo();
        handleConnectTo(message);
    }

    /** */
    public void handleConnectTo(Message message) {
        int port = getIntFromBytes(message.getData(), HEADERLENGTH + 4);
        byte[] ipBytes = new byte[4];
        byte[] sendBuffer = new byte[64];
        System.arraycopy(message.getData(), HEADERLENGTH, ipBytes, 0, 4);
        try {
            this.sendPacket = new DatagramPacket(sendBuffer, message.toBytes().length, InetAddress.getByAddress(ipBytes), port);
Debug.println("connecting to:" + sendPacket.getAddress().getCanonicalHostName() + ":" + sendPacket.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread() {
            public void run() {
                byte[] someSendBuffer = new byte[64];
                someSendBuffer[0] = 0;
Debug.println("initiating direct connection");
                try {
                    STUNConnectionResolver.this.socket.setSoTimeout(100);
                    sendPacket.setData(someSendBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                someSendBuffer[0] = 1;
                int someCount = 0, someRemoteCount = 0;

                for (int i = 0; i < 50; i++) {
                    try {
                        someSendBuffer[0] = (byte) someCount;
                        sendPacket.setData(someSendBuffer);
Debug.println("ack to " + sendPacket.getSocketAddress());
                        STUNConnectionResolver.this.socket.send(sendPacket);
                        socket.receive(sendPacket);
                        someRemoteCount = sendPacket.getData()[0];
                        someCount++;
                    } catch (SocketTimeoutException e) {
Debug.println("ack timed out." + socket.getLocalAddress() + ":" + socket.getLocalPort());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                while (true) {
                    try {
                        socket.receive(sendPacket);
                    } catch (IOException e) {
                        break;
                    }
                }
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                if (someCount > 0 && someRemoteCount > 0) {
                    try {
                        STUNConnectionResolver.this.socket.setSoTimeout(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    STUNConnectionResolver.removeResolver(getLocalPort());
                    policy.onDatagramConnectionEstablished(new DatagramConnection(socket, getRemoteID(), sendPacket.getAddress(), sendPacket.getPort()));
                } else {
Debug.println("connection timed out");
                    STUNConnectionResolver.removeResolver(getLocalPort());
                    if (!isListening()) {
                        policy.onDatagramConnectionFailed(remoteID);
                    }
                }
                return;
            }
        }.start();
    }

    /** */
    public boolean isListening() {
        return this.isListening;
    }

    /** */
    public void sendConnectionRequest() throws IOException {
        sendGeneralConnectionMessage(CONNECTIONREQUEST);
    }

    /** */
    public void sendConnectTo() throws IOException {
        sendGeneralConnectionMessage(CONNECTTO);
    }

    /** */
    private STUNResponse getRouterIP_Port(DatagramSocket aSocket) {
        NodeInfo[] someNodes = policy.getAllNodeInfo();
        for (int i = 0; i < someNodes.length; i++) {
            STUNResponse someResponse = STUNCheckServer.getRouterInetAddr(aSocket, someNodes[i].getIP(), someNodes[i].getSTUNPort1());
            if (someResponse != null) {
                return someResponse;
            }
        }
        return null;
    }

    /** */
    public static void setRoutingPolicy(RoutingPolicy policy) {
        STUNConnectionResolver.policy = policy;
    }

    /** */
    private STUNCheckServer.StunType getLocalSTUNType() {
        return policy.getLocalSTUNType();
    }

    /** */
    static public void removeResolver(int localPort) {
        resolvers.remove(String.valueOf(localPort));
    }

    /** */
    static private STUNConnectionResolver getByLocalPort(int port) {
        STUNConnectionResolver someResolver;
        for (int i = 0; i < resolvers.size(); i++) {
            someResolver = resolvers.get(i);
            if (someResolver.getLocalPort() == port) {
                return someResolver;
            }
        }

        return null;
    }

    /** */
    static private STUNConnectionResolver getResolverPattern(ID remoteID, int localPort, int remotePort) {
Debug.println("getResolverPattern :" + remoteID + " -> localhost:" + localPort);
        STUNConnectionResolver someResolver;
        if (localPort > 0 && (someResolver = getByLocalPort(localPort)) != null) {
            return someResolver;
        } else {
            someResolver = new STUNConnectionResolver(remoteID, remotePort, true);
            resolvers.add(someResolver);
            return someResolver;
        }
    }

    /** */
    public static void getDirectConnection(ID remoteID) throws IOException {
        STUNConnectionResolver someResolver = new STUNConnectionResolver(remoteID, 0, true);
        resolvers.add(someResolver);
        someResolver.sendConnectionRequest();
    }

    /** */
    public static void handleControlMessage(Message message) {
        STUNConnectionResolver someResolver;
        byte[] someData = message.getData();
        int someRemotePort = getIntFromBytes(someData, 2);
        int someLocalPort = getIntFromBytes(someData, 6);
        someResolver = getResolverPattern(message.getSource(), someLocalPort, someRemotePort);
        switch (message.getData()[1]) {
        case CONNECTIONREQUEST:
            try {
                    someResolver.handleConnectionRequest(message);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            break;
        case CONNECTTO:
            someResolver.handleConnectTo(message);
            break;
        }
    }
}

/* */
