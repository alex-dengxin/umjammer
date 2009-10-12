/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.network;

import java.io.IOException;
import java.net.ServerSocket;

import vavi.util.Debug;


/**
 * @author suno
 * Created on 2003/06/29
 */
public class Server extends Thread {
    /** */
    public static int DEFAULTPORT = 22222;

    /** */
    private ServerSocket serverSocket;

    /** */
    private NetworkEventHandler handler;

    /** */
    public Server(NetworkEventHandler handler, int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.handler = handler;
    }

    /** */
    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }

    /** */
    public void run() {
Debug.println("server started: " + getName() + ": " + serverSocket);
Debug.println("handler: " + handler);
        while (true) {
            try {
                new Connection(handler, serverSocket.accept(), getName() + ": " + serverSocket);
            } catch (Exception e) {
                Debug.printStackTrace(e);
            }
        }
    }
}

/* */
