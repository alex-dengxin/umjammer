/*
 * @(#) $Id: JSTKShellRMIServerImpl.java,v 1.1.1.1 2003/10/05 18:39:19 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.jstksh;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import javax.security.auth.Subject;


public class JSTKShellRMIServerImpl extends java.rmi.server.UnicastRemoteObject implements JSTKShell {
    protected JSTKShell shell = null;

    protected Subject sub = null;

    public JSTKShellRMIServerImpl() throws java.rmi.RemoteException {
        super(0);
        shell = new JSTKShellServer();
    }

    public JSTKShellRMIServerImpl(int port) throws java.rmi.RemoteException {
        super(port);
        shell = new JSTKShellServer();
    }

    public JSTKShellRMIServerImpl(int port, RMIClientSocketFactory clientFactory, RMIServerSocketFactory serverFactory) throws java.rmi.RemoteException {
        super(port, clientFactory, serverFactory);
        shell = new JSTKShellServer();
    }

    public void setSubject(Subject sub) {
        this.sub = sub;
    }

    public String execCommand(String[] cmdargs) throws RemoteException {
        try {
            return shell.execCommand(cmdargs);
        } catch (Exception e) {
            throw new RemoteException("Exception at RMI Server", e);
        }
    }

    public String createSession() throws RemoteException {
        try {
            return shell.createSession();
        } catch (Exception e) {
            throw new RemoteException("Exception at RMI Server", e);
        }
    }

    public void destroySession(String sessId) throws RemoteException {
        try {
            shell.destroySession(sessId);
        } catch (Exception e) {
            throw new RemoteException("Exception at RMI Server", e);
        }
    }

    public void initialize(String[] args, String serverId) throws Exception {
        int regPort = 1099;
        try {
            java.rmi.registry.LocateRegistry.createRegistry(regPort);
        } catch (Exception exc) {
            System.err.println("Could not create rmiregistry in local JVM: " + exc);
            System.exit(1);
        }
        System.out.println("RMI Registry started. Listens for requests on port: " + regPort);

        Naming.rebind("//localhost/" + serverId, this);
        System.out.println(serverId + " bound in registry ...");
    }

    public static void main(String[] args) throws Exception {
        JSTKShellRMIServerImpl impl = new JSTKShellRMIServerImpl();
        impl.initialize(args, "JSTKShellRMIServer");
    }
}
