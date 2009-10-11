/*
 * @(#) $Id: RMIServerImpl.java,v 1.1.1.1 2003/10/05 18:39:23 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;


public class RMIServerImpl extends java.rmi.server.UnicastRemoteObject implements RMIServerInterface {

    public RMIServerImpl() throws java.rmi.RemoteException {
        super(0);
    }

    public RMIServerImpl(int port) throws java.rmi.RemoteException {
        super(port);
    }

    public RMIServerImpl(int port, RMIClientSocketFactory clientFactory, RMIServerSocketFactory serverFactory) throws java.rmi.RemoteException {
        super(port, clientFactory, serverFactory);
    }

    public void writeOnly(byte[] buf) {
        // Do nothing.
    }

    public byte[] writeRead(byte[] buf) {
        return buf;
    }
}
