/*
 * @(#) $Id: ProxyThread.java,v 1.1.1.1 2003/10/05 18:39:23 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.io.IOException;
import java.util.Vector;

import org.jstk.JSTKArgs;


public class ProxyThread extends Thread {
    public class Forwarder extends Thread {
        private JSTKSocket inSock;

        private JSTKSocket outSock;

        private JSTKBuffer buf;

        String id;

        private Vector<ProtocolAnalyzer> paVec = new Vector<ProtocolAnalyzer>();

        public Forwarder(String id, JSTKSocket inSock, JSTKSocket outSock, JSTKBuffer buf) {
            super(id);
            this.id = id;
            this.inSock = inSock;
            this.outSock = outSock;
            this.buf = buf;
        }

        public void addProtocolAnalyzer(ProtocolAnalyzer pa) {
            this.paVec.add(pa);
        }

        public void run() {
            try {
//                int n;
                while ((/*n =*/ inSock.read(buf)) != -1) {
                    for (int i = 0; i < paVec.size(); i++) {
                        ProtocolAnalyzer pa = paVec.elementAt(i);
                        pa.analyze(buf);
                    }
                    outSock.write(buf);
                }
                inSock.close();
                outSock.close();
            } catch (Exception e) {
                try {
                    inSock.close();
                    outSock.close();
                } catch (Exception exc) {
                }
            }
        }
    }

    private JSTKSocket socket1 = null;

    private boolean verbose = false;

//    private boolean showdata = false;

//    private String threadId = null;

    private int bufsize = 0;

    private JSTKArgs args = null;

    private int thdIndex;

    String host;

    int port;

    public ProxyThread(JSTKSocket socket, int thdIndex, JSTKArgs args) throws IOException {
        super("ProxyThread");
        this.socket1 = socket;
        this.thdIndex = thdIndex;
//        showdata = Boolean.valueOf(args.get("showdata")).booleanValue();
        verbose = Boolean.valueOf(args.get("verbose")).booleanValue();
        bufsize = Integer.parseInt(args.get("bufsize"));
        this.args = args;

        this.socket1.getSocket().setTcpNoDelay(true);
    }

    public void run() {
//        threadId = Thread.currentThread().toString();
        JSTKSocket socket2 = null;
        try {
            socket2 = JSTKSocketUtil.connect(args);
        } catch (Exception e) {
            String remoteHost = args.get("host") + ":" + args.get("port");
            System.err.println("Connection failed: " + remoteHost + ". Exception: " + e);
            socket1.close();
            return;
        }

        if (verbose) {
            System.out.println("[" + thdIndex + "] Established Connection ...");
            JSTKSocketUtil.print(socket2, " --> ");
        }
        JSTKBuffer fbuf = JSTKBuffer.getInstance(bufsize, args);
        JSTKBuffer rbuf = JSTKBuffer.getInstance(bufsize, args);

        Forwarder cin2cout = new Forwarder("FowardTyhread", socket1, socket2, fbuf);
        Forwarder cout2cin = new Forwarder("ReverseThread", socket2, socket1, rbuf);
        String patype = args.get("patype");
        if (patype != null) {
            String[] patypes = patype.split(",");
            for (int i = 0; i < patypes.length; i++) {
                String pt = patypes[i].trim();
                ProtocolAnalyzer fpa = ProtocolAnalyzerFactory.getInstance(pt, "-->");
                ProtocolAnalyzer rpa = ProtocolAnalyzerFactory.getInstance(pt, "<--");
                if (fpa != null && rpa != null) {
                    cin2cout.addProtocolAnalyzer(fpa);
                    cout2cin.addProtocolAnalyzer(rpa);
                    if (verbose)
                        System.out.println("Analyzer added for PA Type: " + pt);
                } else {
                    if (verbose)
                        System.out.println("Analyzer not found for PA Type: " + pt);
                }
            }
        }
        try {
            cin2cout.start();
            cout2cin.start();
            cin2cout.join();
            if (verbose) {
                System.out.println("[" + thdIndex + "] ... Connection Over.");
            }
        } catch (Exception exc) {
            System.err.println("Exception: " + exc);
        }
    }
}
