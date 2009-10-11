/*
 * @(#) $Id: JSTKSocket.java,v 1.1.1.1 2003/10/05 18:39:22 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public abstract class JSTKSocket {
    private static class SocketWrapper extends JSTKSocket {
        private Socket sock = null;

        private SocketWrapper(Socket sock) {
            this.sock = sock;
        }

        public int read(JSTKBuffer buf) throws IOException {
            byte[] ba = buf.getByteArray();
            int n = sock.getInputStream().read(ba, 0, ba.length);
            buf.setNBytes(n);
            return n;
        }

        public void write(JSTKBuffer buf) throws IOException {
            byte[] ba = buf.getByteArray();
            sock.getOutputStream().write(ba, 0, buf.getNBytes());
        }

        public Socket getSocket() {
            return sock;
        }

        public void close() {
            try {
                sock.close();
            } catch (Exception e) {
                System.err.println("Exception in Socket.close(). Ignoring ... Exception: " + e);
            }
        }
    }

    private static class SocketChannelWrapper extends JSTKSocket {
        private SocketChannel sc = null;

        private SocketChannelWrapper(SocketChannel sc) {
            this.sc = sc;
        }

        public int read(JSTKBuffer buf) throws IOException {
            ByteBuffer bb = buf.getByteBuffer();
            bb.clear();
            return sc.read(bb);
        }

        public void write(JSTKBuffer buf) throws IOException {
            ByteBuffer bb = buf.getByteBuffer();
            bb.flip();
            sc.write(bb);
        }

        public Socket getSocket() {
            return sc.socket();
        }

        public void close() {
            try {
                sc.close();
            } catch (Exception e) {
                System.err.println("Exception in SocketChannel.close(). Ignoring ... Exception: " + e);
            }
        }
    }

    public static JSTKSocket getInstance(Socket sock) {
        return new SocketWrapper(sock);
    }

    public static JSTKSocket getInstance(SocketChannel sc) {
        return new SocketChannelWrapper(sc);
    }

    public abstract void close();

    public abstract int read(JSTKBuffer jbuf) throws IOException;

    public abstract void write(JSTKBuffer jbuf) throws IOException;

    public abstract Socket getSocket();
}
