/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.stunresolver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import vavi.net.stun.messaging.ID;


/**
 * @author suno
 * Created on 2003/07/01
 */
public class DatagramConnection {
    /** */
    private DatagramSocket socket;

    /** */
    private InetAddress destinationAddress;

    /** */
    private int destinationPort;

    /** */
    private ID id;

    /** */
    private byte[] buffer = new byte[1500];

    /** */
    private DatagramPacket receivePacket;

    /** */
    private DatagramPacket sendPacket;

    /** */
    public DatagramConnection(DatagramSocket socket, ID remoteID, InetAddress remoteAddr, int remotePort) {
        this.socket = socket;
        this.id = remoteID;
        this.destinationAddress = remoteAddr;
        this.destinationPort = remotePort;
        byte[] someBuffer = new byte[1];
        this.sendPacket = new DatagramPacket(someBuffer, 0, destinationAddress, destinationPort);
        this.receivePacket = new DatagramPacket(buffer, buffer.length);
    }

    /** */
    public InetAddress getRemoteAddress() {
        return destinationAddress;
    }

    /** */
    public InetAddress getLocalAddress() {
        return socket.getLocalAddress();
    }

    /** */
    public int getRemotePort() {
        return destinationPort;
    }

    /** */
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    /** */
    public ID getID() {
        return id;
    }

    /** */
    public void send(byte[] data) throws IOException {
        send(data, 0, data.length);
    }

    /** */
    public void send(byte[] data, int offset, int length) throws IOException {
        sendPacket.setData(data, offset, length);
        socket.send(sendPacket);
    }

    /** */
    public void close() {
        socket.close();
    }

    /** */
    public byte[] receive() throws IOException {
        while (true) {
            socket.receive(receivePacket);
            if (receivePacket.getAddress().equals(destinationAddress)) {
                return receivePacket.getData();
            }
        }
    }
}

/* */
