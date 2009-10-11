/*
 * @(#) $Id: JSTKServerSocket.java,v 1.1.1.1 2003/10/05 18:39:22 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.nio.channels.ServerSocketChannel;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;


public abstract class JSTKServerSocket {
    private static class ServerSocketWrapper extends JSTKServerSocket {
        private ServerSocket ss = null;

        private ServerSocketWrapper(ServerSocket ss) {
            this.ss = ss;
        }

        public JSTKSocket accept() throws IOException {
            return JSTKSocket.getInstance(ss.accept());
        }

        public void setNeedClientAuth(boolean flag) {
            if (ss instanceof SSLServerSocket) {
                ((SSLServerSocket) ss).setNeedClientAuth(flag);
            }
        }

        public void setWantClientAuth(boolean flag) {
            if (ss instanceof SSLServerSocket) {
                ((SSLServerSocket) ss).setWantClientAuth(flag);
            }
        }
    }

    private static class ServerSocketChannelWrapper extends JSTKServerSocket {
        private ServerSocketChannel ssc = null;

        private ServerSocketChannelWrapper(ServerSocketChannel ssc) {
            this.ssc = ssc;
        }

        public JSTKSocket accept() throws IOException {
            return JSTKSocket.getInstance(ssc.accept());
        }

        public void setNeedClientAuth(boolean flag) {
            // Underlying socket can't be SSLServerSocket. Do nothing.
        }

        public void setWantClientAuth(boolean flag) {
            // Underlying socket can't be SSLServerSocket. Do nothing.
        }
    }

    public static JSTKServerSocket getInstance(ServerSocket ss) {
        return new ServerSocketWrapper(ss);
    }

    public static JSTKServerSocket getInstance(ServerSocketChannel ssc) {
        return new ServerSocketChannelWrapper(ssc);
    }

    public abstract JSTKSocket accept() throws IOException;

    public abstract void setNeedClientAuth(boolean flag);

    public abstract void setWantClientAuth(boolean flag);
}
