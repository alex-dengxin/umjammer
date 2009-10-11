/*
 * @(#) $Id: JSTKShellAuthRMIServerImpl.java,v 1.1.1.1 2003/10/05 18:39:19 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.jstksh;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import javax.security.auth.Subject;


public class JSTKShellAuthRMIServerImpl extends JSTKShellRMIServerImpl {

    public JSTKShellAuthRMIServerImpl() throws java.rmi.RemoteException {
        super(0);
    }

    public JSTKShellAuthRMIServerImpl(int port) throws java.rmi.RemoteException {
        super(port);
    }

    public JSTKShellAuthRMIServerImpl(int port, RMIClientSocketFactory clientFactory, RMIServerSocketFactory serverFactory) throws java.rmi.RemoteException {
        super(port, clientFactory, serverFactory);
        shell = new JSTKShellServer();
    }

    public String execCommand(String[] cmdargs) throws RemoteException {
        try {
            JSTKShellActions.ExecCommandAction action = new JSTKShellActions.ExecCommandAction(shell, cmdargs);
            return (String) Subject.doAs(sub, action);
        } catch (Exception e) {
            throw new RemoteException("Exception at RMI Server", e);
        }
    }

    public String createSession() throws RemoteException {
        try {
            JSTKShellActions.CreateSessionAction action = new JSTKShellActions.CreateSessionAction(shell);
            return (String) Subject.doAs(sub, action);
        } catch (Exception e) {
            throw new RemoteException("Exception at RMI Server", e);
        }
    }

    public void destroySession(String sessId) throws RemoteException {
        try {
            JSTKShellActions.DestroySessionAction action = new JSTKShellActions.DestroySessionAction(shell, sessId);
            Subject.doAs(sub, action);
        } catch (Exception e) {
            throw new RemoteException("Exception at RMI Server", e);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKShellAuthRMIServerImpl impl = new JSTKShellAuthRMIServerImpl();
        impl.initialize(args, "JSTKShellAuthRMIServer");
    }
}
