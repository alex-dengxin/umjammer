/*
 * @(#) $Id: RMIServerInterface.java,v 1.1.1.1 2003/10/05 18:39:23 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RMIServerInterface extends Remote {
    public void writeOnly(byte[] buf) throws RemoteException;

    public byte[] writeRead(byte[] buf) throws RemoteException;
}
