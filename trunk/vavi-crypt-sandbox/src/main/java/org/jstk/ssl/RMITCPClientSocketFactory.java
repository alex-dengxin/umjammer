/*
 * @(#) $Id: RMITCPClientSocketFactory.java,v 1.1.1.1 2003/10/05 18:39:23 pankaj_kumar Exp $
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
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;


public class RMITCPClientSocketFactory implements RMIClientSocketFactory, Serializable {
    public Socket createSocket(String host, int port) throws IOException {
        int outPort = ClientCommand.getOutPort();
        if (outPort != -1)
            port = outPort;
        Socket socket = new Socket(host, port);
        return socket;
    }
}
