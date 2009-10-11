/*
 * @(#) $Id: ServerThread.java,v 1.1.1.1 2003/10/05 18:39:24 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.io.IOException;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;


public class ServerThread extends Thread {
    private JSTKSocket socket = null;

    private JSTKBuffer buf = null;

    private int outIdx;

    private boolean verbose;

    private boolean bench;

    private boolean echo;

    private boolean invalidate;

    public ServerThread(JSTKSocket socket, JSTKBuffer buf, int outIdx, boolean verbose, boolean bench, boolean echo, boolean invalidate) throws IOException {
        super("ServerThread");
        this.socket = socket;
        this.buf = buf;
        this.outIdx = outIdx;
        this.verbose = verbose;
        this.bench = bench;
        this.echo = echo;
        this.invalidate = invalidate;
    }

    public void run() {
        int n;
        try {
            int inIdx = 0;
            while ((n = socket.read(buf)) != -1) {
                if (!bench || verbose)
                    System.out.println("[" + outIdx + ", " + inIdx + "]ServerLoop:: read " + n + " bytes.");
                if (echo) {
                    socket.write(buf);
                    if (!bench || verbose)
                        System.out.println("[" + outIdx + ", " + inIdx + "]ServerLoop:: wrote " + n + " bytes.");
                }
                ++inIdx;
            }

        } catch (IOException ioe) {
            if (!bench || verbose)
                System.out.println("[" + outIdx + "]ServerThread:: Exception on read: " + ioe);
        }

        if (invalidate && socket.getSocket() instanceof SSLSocket) {
            if (verbose)
                System.out.println("[" + outIdx + "]Invalidating the SSLSession ...");
            SSLSocket sslSock = (SSLSocket) socket.getSocket();
            SSLSession sess = sslSock.getSession();
            sess.invalidate();
        }

        socket.close();
        if (!bench || verbose)
            System.out.println("[" + outIdx + "]ServerThread:: Socket closed.");
    }
}
