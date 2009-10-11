/*
 * @(#) $Id: RMISSLServerSocketFactory.java,v 1.1.1.1 2003/10/05 18:39:23 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.server.RMIServerSocketFactory;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ServerSocketFactory;


public class RMISSLServerSocketFactory implements RMIServerSocketFactory, Serializable {
    public ServerSocket createServerSocket(int port) throws IOException {
        ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
        ServerSocket socket = factory.createServerSocket(port);
        return socket;
    }
}
