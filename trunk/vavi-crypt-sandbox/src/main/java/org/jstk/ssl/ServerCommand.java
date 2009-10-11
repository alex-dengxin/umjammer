/*
 * @(#) $Id: ServerCommand.java,v 1.1.1.1 2003/10/05 18:39:24 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class ServerCommand extends JSTKCommandAdapter {

    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("inport", "9000");
        defaults.put("inproto", "TCP");
        defaults.put("mode", "echo");
        defaults.put("bufsize", "8192");
        defaults.put("action", "read-only");
        defaults.put("waittime", "5");
    }

    public String briefDescription() {
        String briefDesc = "simple echo server for TCP or SSL connections";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -inport <inport>: Port No. to accept incoming connection.[" + defaults.get("inport") + "]\n" + "  -inproto <proto>: Incoming connection protocol (TCP|SSL|RMI|SRMI).[" + defaults.get("inproto") + "]\n" + "  -mode <mode>    : Receiving mode (echo|bench).[" + defaults.get("mode") + "]\n" + "  -action <action>: What action to take -- applicable for \"bench\" mode\n" + "            (accept-wait|read-only|read-write|serve-url).[" + defaults.get("action") + "]\n"
                             + "  -bufsize <bufsz>: Receiving Buffer Size (in bytes).[" + defaults.get("bufsize") + "]\n" + "  -needcauth      : Must authenticate client.\n" + "  -wantcauth      : Negotiate to authenticate client.\n" + "  -verbose        : Print a message on receiving data.\n" + "  -showdata       : Show the received data on stdout.\n" + "  -inetaddr <addr>: Use this IP address (useful for multi-homed hosts).\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-inport <inport>] [-ssl [-cauth]] [-inetaddr <addr>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "", "-inport 2950", "-ssl -cauth"
        };
        return sampleUses;
    }

    private void ServerLoop(JSTKServerSocket jss, JSTKBuffer buf, boolean verbose, boolean bench, boolean echo, boolean invalidate) {
        int outIdx = 0;
        while (true) {
            JSTKSocket socket = null;
            try {
                if (!bench || verbose)
                    System.out.println("[" + outIdx + "]ServerLoop:: Waiting for connection ...");
                socket = jss.accept();
                if (!bench || verbose)
                    System.out.println("[" + outIdx + "]ServerLoop:: ... Accepted a connection.");
                if (!bench || verbose)
                    JSTKSocketUtil.print(socket, " <-- ");
            } catch (IOException ioe) {
                System.out.println("[" + outIdx + "]ServerLoop:: Exception on accept: " + ioe);
                return;
            }

            try {
                Thread th = new ServerThread(socket, buf, outIdx, verbose, bench, echo, invalidate);
                th.start();
            } catch (IOException ioe) {
                System.out.println("[" + outIdx + "]ServerLoop:: Exception starting the worker thread: " + ioe);
                return;
            }
            ++outIdx;
        }
    }

    private JSTKResult runRMIServer(JSTKArgs args, RMIServerImpl impl) throws Exception {
//        int waitTime = Integer.parseInt(args.get("waittime"));
        int regPort = 1099;
        try {
            java.rmi.registry.LocateRegistry.createRegistry(regPort);
        } catch (Exception exc) {
            return new JSTKResult(null, false, "Could not create rmiregistry in local JVM: " + exc);
        }
        System.out.println("RMI Registry started. Listens for requests on port: " + regPort);

        String url = "//" + InetAddress.getLocalHost().getHostName() + "/RMIServer";
        try {
            java.rmi.Naming.rebind(url, impl);
        } catch (Exception exc) {
            return new JSTKResult(null, false, "Could not rebind \"" + url + "\": " + exc);
        }

        System.out.println("RMIServer bound in registry. Ready to service RMI calls.");
        System.out.println("Press Enter to exit ...");
        System.in.read();
        return new JSTKResult(null, true, "DONE");
    }

    private JSTKResult runRMIServer(JSTKArgs args) throws Exception {
        int inport = Integer.parseInt(args.get("inport"));
        RMIServerImpl impl = new RMIServerImpl(inport, new RMITCPClientSocketFactory(), new RMITCPServerSocketFactory());
        return runRMIServer(args, impl);
    }

    private JSTKResult runSRMIServer(JSTKArgs args) throws Exception {
        int inport = Integer.parseInt(args.get("inport"));
        RMIServerImpl impl = new RMIServerImpl(inport, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory());
        return runRMIServer(args, impl);
    }

    private JSTKResult runTCPServer(JSTKArgs args, JSTKServerSocket jss) throws Exception {
        String mode = args.get("mode");
        String action = args.get("action");
        String inproto = args.get("inproto");
        boolean verbose = Boolean.valueOf(args.get("verbose")).booleanValue();
        boolean invalidate = Boolean.valueOf(args.get("invalidate")).booleanValue();
        int bufsize = Integer.parseInt(args.get("bufsize"));

        System.out.println("  I/O library  : " + JSTKSocketUtil.getIOLibrary(args, inproto));
        System.out.println("  -----------------------------------");

        JSTKBuffer buf = JSTKBuffer.getInstance(bufsize, args);
        if (mode.equalsIgnoreCase("bench")) {
            if (action.equalsIgnoreCase("accept-wait")) {
                ServerLoop(jss, buf, verbose, true, false, invalidate);
            } else if (action.equalsIgnoreCase("read-only")) {
                ServerLoop(jss, buf, verbose, true, false, invalidate);
            } else if (action.equalsIgnoreCase("read-write")) {
                ServerLoop(jss, buf, verbose, true, true, invalidate);
            } else {
                return new JSTKResult(null, false, "Unknown action: " + action);
            }
        } else if (mode.equalsIgnoreCase("echo")) {
            ServerLoop(jss, buf, verbose, false, true, invalidate);
        } else {
            return new JSTKResult(null, false, "Unknown mode: " + mode);
        }
        return new JSTKResult(null, true, "DONE");
    }

    private JSTKResult runSSLServer(JSTKArgs args) throws Exception {
        boolean needCAuth = Boolean.valueOf(args.get("needcauth")).booleanValue();
        boolean wantCAuth = Boolean.valueOf(args.get("wantcauth")).booleanValue();
        System.out.println("  Client Auth  : " + (needCAuth ? "INSIST" : (wantCAuth ? "NEGOTIATE" : "DONT ASK")));
        String[] csarray = JSTKSocketUtil.getCSFileCipherSuites(args);
        if (csarray != null) {
            System.out.println("  Cipher Suites to be enabled   : ");
            for (int i = 0; i < csarray.length; i++)
                System.out.println("         " + csarray[i]);
        }

        JSTKServerSocket jss = JSTKSocketUtil.createServerSocket(args);
        // Preference to NeedClientAuth -- if both need and want flags are set.
        if (needCAuth)
            jss.setNeedClientAuth(true);
        else if (wantCAuth)
            jss.setWantClientAuth(true);
        return runTCPServer(args, jss);
    }

    private JSTKResult runHTTPServer(JSTKArgs args) {
        return null;
    }

    private JSTKResult runHTTPSServer(JSTKArgs args) {
        return null;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String inport = args.get("inport");
//            String inetAddrVal = args.get("inetaddr");
            String mode = args.get("mode");
            String action = args.get("action");
            String inproto = args.get("inproto");

//            boolean showData = Boolean.valueOf(args.get("showdata")).booleanValue();
//            boolean verbose = Boolean.valueOf(args.get("verbose")).booleanValue();
            int bufsize = Integer.parseInt(args.get("bufsize"));

            int lport = Integer.parseInt(inport);

            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("  Local Host   : " + localHost + ", Listen Port: " + lport);
            System.out.println("  Buffer Size  : " + bufsize);
            System.out.println("  Exec. Mode   : " + mode);
            System.out.println("  Server Action: " + action);
            System.out.println("  IN protocol  : " + inproto);

            if (inproto.equalsIgnoreCase("RMI")) {
                return runRMIServer(args);
            } else if (inproto.equalsIgnoreCase("SRMI")) {
                return runSRMIServer(args);
            } else if (inproto.equalsIgnoreCase("TCP")) {
                JSTKServerSocket jss = JSTKSocketUtil.createServerSocket(args);
                return runTCPServer(args, jss);
            } else if (inproto.equalsIgnoreCase("SSL")) {
                return runSSLServer(args);
            } else if (inproto.equalsIgnoreCase("HTTP")) {
                return runHTTPServer(args);
            } else if (inproto.equalsIgnoreCase("HTTPS")) {
                return runHTTPSServer(args);
            }
            return new JSTKResult(null, false, "Unknown IN Protocol: " + inproto);
        } catch (Exception exc) {
            throw new JSTKException("ServerCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        ServerCommand serverCmd = new ServerCommand();
        JSTKResult result = (JSTKResult) serverCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
