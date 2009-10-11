/*
 * @(#) $Id: JSTKSocketUtil.java,v 1.1.1.1 2003/10/05 18:39:22 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Vector;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.jstk.JSTKArgs;
import org.jstk.JSTKException;


public class JSTKSocketUtil {

    public static JSTKServerSocket createServerSocket(JSTKArgs args) throws JSTKException {
        try {
            String inport = args.get("inport");
            String inetAddrVal = args.get("inetaddr");
//            boolean verbose = Boolean.valueOf(args.get("verbose")).booleanValue();
            String inproto = args.get("inproto");
            boolean nio = Boolean.valueOf(args.get("nio")).booleanValue();

            int lport = Integer.parseInt(inport);
            JSTKServerSocket jss = null;

            if (nio && !inproto.equalsIgnoreCase("SSL")) {
                InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), lport);
                ServerSocketChannel ssc = ServerSocketChannel.open();
                ssc.socket().bind(isa);
                jss = JSTKServerSocket.getInstance(ssc);
            } else {
                ServerSocketFactory ssf = null;
                ServerSocket serverSocket = null;
                if (inproto.equalsIgnoreCase("SSL")) {
                    ssf = SSLServerSocketFactory.getDefault();
                } else {
                    ssf = ServerSocketFactory.getDefault();
                }
                if (inetAddrVal == null) {
                    serverSocket = ssf.createServerSocket(lport);
                } else {
                    InetAddress ia = InetAddress.getByName(inetAddrVal);
                    serverSocket = ssf.createServerSocket(lport, 50, ia);
                }
                if (serverSocket instanceof SSLServerSocket) {
                    String[] csarray = getCSFileCipherSuites(args);
                    if (csarray != null) {
                        ((SSLServerSocket) serverSocket).setEnabledCipherSuites(csarray);
                    }
                }
                jss = JSTKServerSocket.getInstance(serverSocket);
            }

            return jss;
        } catch (Exception exc) {
            throw new JSTKException("Could not create Server Scoket: " + exc, exc);
        }
    }

    public static JSTKSocket connect(JSTKArgs args) throws JSTKException {
        try {
            String host = args.get("host");
            int port = Integer.parseInt(args.get("port"));
            String inetAddrVal = args.get("inetaddr");
//            boolean verbose = Boolean.valueOf(args.get("verbose")).booleanValue();
            String outproto = args.get("outproto");
            Socket socket = null;

            if (getIOLibrary(args, outproto).equalsIgnoreCase("NIO")) {
                InetSocketAddress isa = new InetSocketAddress(InetAddress.getByName(host), port);
                java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open();
                sc.connect(isa);
                sc.socket().setTcpNoDelay(true);
                return JSTKSocket.getInstance(sc);
            } else {
                SocketFactory sf = null;
                if (outproto.equalsIgnoreCase("SSL")) {
                    sf = SSLSocketFactory.getDefault();
                } else {
                    sf = SocketFactory.getDefault();
                }
                if (inetAddrVal == null) {
                    socket = sf.createSocket(host, port);
                } else {
                    InetAddress ia = InetAddress.getByName(inetAddrVal);
                    socket = sf.createSocket(host, port, ia, port + 1);
                }
                socket.setTcpNoDelay(true);
                if (socket instanceof SSLSocket) {
                    String[] csarray = getCSFileCipherSuites(args);
                    if (csarray != null) {
                        ((SSLSocket) socket).setEnabledCipherSuites(csarray);
                    }
                }

                return JSTKSocket.getInstance(socket);
            }
        } catch (Exception exc) {
            throw new JSTKException("Could not create Scoket: " + exc, exc);
        }
    }

    public static void print(JSTKSocket jsocket, String dir) {
        try {
            Socket socket = jsocket.getSocket();

            InetSocketAddress localAddr = (InetSocketAddress) socket.getLocalSocketAddress();
            InetSocketAddress remoteAddr = (InetSocketAddress) socket.getRemoteSocketAddress();
            String localAddrId = localAddr.getHostName() + ":" + localAddr.getPort();
            String remoteAddrId = remoteAddr.getHostName() + ":" + remoteAddr.getPort();

            System.out.println("  Connection   : " + localAddrId + dir + remoteAddrId);
            if (socket instanceof SSLSocket) {
                SSLSession sess = ((SSLSocket) socket).getSession();
                System.out.println("  Protocol     : " + sess.getProtocol());
                System.out.println("  Cipher Suite : " + sess.getCipherSuite());
                Certificate[] localCerts = sess.getLocalCertificates();
                if (localCerts != null && localCerts.length > 0)
                    printCertDNs(localCerts, "  Local Certs : ");

                Certificate[] remoteCerts = null;
                try {
                    remoteCerts = sess.getPeerCertificates();
                    printCertDNs(remoteCerts, "  Remote Certs: ");
                } catch (SSLPeerUnverifiedException exc) {
                    System.out.println("  Remote Certs: Unverified");
                }
            } else {
                System.out.println("  Protocol     : TCP");
            }
        } catch (Exception exc) {
            System.err.println("Could not print Socket Information: " + exc);
        }
    }

    private static void printCertDNs(Certificate[] certs, String label) {
        System.out.println(label + "[0]" + ((X509Certificate) certs[0]).getSubjectDN());
        StringBuffer indent = new StringBuffer();
        for (int i = label.length(); i > 0; i--)
            indent.append(" ");
        for (int i = 1; i < certs.length; i++) {
            System.out.println(indent.toString() + "[" + i + "]" + ((X509Certificate) certs[i]).getSubjectDN());
        }
    }

    public static String getIOLibrary(JSTKArgs args, String proto) {
        boolean nio = Boolean.valueOf(args.get("nio")).booleanValue();
        if (nio && !proto.equalsIgnoreCase("SSL"))
            return "NIO";
        else
            return "CLASSIC";
    }

    public static String[] getCSFileCipherSuites(JSTKArgs args) {
        String csfile = args.get("csfile");
        try {
            if (csfile != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csfile)));
                Vector<String> v = new Vector<String>();
                String s;
                while ((s = br.readLine()) != null) {
                    s = s.trim();
                    if (s.length() > 0)
                        v.add(s);
                }
                String[] csarray = new String[v.size()];
                for (int i = 0; i < v.size(); i++) {
                    csarray[i] = v.elementAt(i);
                }
                return csarray;
            }
        } catch (IOException ioe) {
            System.err.println("Error reading csfile: " + csfile + ", Exception: " + ioe);
        }
        return null;
    }
}
