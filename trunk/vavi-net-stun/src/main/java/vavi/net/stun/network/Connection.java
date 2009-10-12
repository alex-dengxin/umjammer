/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vavi.util.Debug;


/**
 * @author suno
 * Created on 2003/06/29
 */
public class Connection {
    /** */
    private DataInputStream inputStream;

    /** */
    private DataOutputStream outputStream;

    /** */
    private Socket socket;

    /** */
    public enum Type {
        /** */
        UNKNOWN,
        /** */
        SERVER,
        /** */
        CHILD
    }

    /** */
    private Type connectionType;

    /** */
    private NetworkEventHandler handler;

    /** */
    private ExecutorService service = Executors.newSingleThreadExecutor();

    /** */
    private String name;

    /**
     * {@link NetworkEventHandler#onConnectionEstablished(Connection)} が発行されます。
     */
    public Connection(NetworkEventHandler handler, InetAddress host, int port, Type connectionType) throws IOException {
        this.handler = handler;
        this.socket = new Socket(host, port);
        this.connectionType = connectionType;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        outputStream.writeInt(connectionType.ordinal());
        handler.onConnectionEstablished(this);
    }

    /**
     * called by Server.
     * {@link NetworkEventHandler#onConnectionEstablished(Connection)} が発行されます。
     */
    public Connection(NetworkEventHandler handler, Socket socket, String name) throws IOException {
        this.handler = handler;
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.connectionType = Type.values()[inputStream.readInt()];
setName(name + ": " + this);
Debug.println("accepted a socket: " + name + ": " + socket);
        handler.onConnectionEstablished(this);
    }

    /** */
    public Type getConnectionType() {
        return connectionType;
    }

    /** */
    private Runnable server = new Runnable() {
        public void run() {
            try {
                while (true) {
                    byte[] buffer = new byte[2048];
                    int length = inputStream.read(buffer);
                    if (length < 0) {
Debug.println("got eof: " + name);
                        break;
                    } else {
                        byte[] received = new byte[length];
                        System.arraycopy(buffer, 0, received, 0, length);
                        handler.onPacketArrived(Connection.this, received);
                    }
                }
            } catch (Exception e) {
                Debug.println(name + ": " + e);
            } finally {
                try {
                    close();
Debug.println("connection closed: " + name + ": " + socket);
                    handler.onDisconnect(Connection.this);
                } catch (IOException f) {
Debug.printStackTrace(f);
                }
            }
        }
    };

    /** */
    public int read(byte[] buffer) throws IOException {
        return inputStream.read(buffer);
    }

    /** */
    public void write(byte[] buffer) throws IOException {
        outputStream.write(buffer);
    }

    /** */
    public void close() throws IOException {
        try {
            socket.close();
        } finally {
            handler.onDisconnect(this);
        }
    }

    /** */
    public InetAddress getRemoteInetAddress() {
        return socket.getInetAddress();
    }

    /** */
    public int getRemotePort() {
        return socket.getPort();
    }

    /** */
    public void start() {
Debug.printStackTrace(new Exception("*** DUMMY ***"));
        service.execute(server);
    }

    /** */
    public void setName(String name) {
        this.name = name;
    }

    /** */
    public String toString() {
        return socket + ":" + connectionType;
    }
}

/* */
