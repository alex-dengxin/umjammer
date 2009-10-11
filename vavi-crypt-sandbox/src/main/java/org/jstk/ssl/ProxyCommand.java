/*
 * @(#) $Id: ProxyCommand.java,v 1.1.1.1 2003/10/05 18:39:23 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class ProxyCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("inport", "8995");
        defaults.put("host", "localhost");
        defaults.put("port", "9000");
        defaults.put("inproto", "TCP");
        defaults.put("outproto", "TCP");
        defaults.put("bufsize", "8192");
    }

    public String briefDescription() {
        String briefDesc = "simple proxy for TCP or SSL connections";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -inport <inport>: Port No. to accept incoming connection.[" + defaults.get("inport") + "]\n" + "  -host <host>    : Remote host machine name or IP address.[" + defaults.get("host") + "]\n" + "  -port <port>    : Destination port on remote host.[" + defaults.get("port") + "]\n" + "  -patype <type>  : Protocol Analyzed Type (dd, http or ssl).\n" + "  -bufsize <bufsz>: Receiving Buffer Size (in bytes).[" + defaults.get("bufsize") + "]\n"
                             + "  -inproto <proto>: Incoming connection protocol(TCP or SSL).[" + defaults.get("inproto") + "]\n" + "  -outproto <proto>: Outgoing connection protocol(TCP or SSL).[" + defaults.get("outproto") + "]\n" + "  -verbose        : Print a message on receiving data.\n" + "  -showdata       : Show the received data on stdout.\n" + "  -inetaddr <addr>: Use this IP address (useful for multi-homed hosts).\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-inport <inport>] [-port <port>] [-patype <type>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "", "-inport 2950", "-ssl -cauth"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String inport = args.get("inport");
            String host = args.get("host");
            int port = Integer.parseInt(args.get("port"));
//            String inetAddrVal = args.get("inetaddr");
//            boolean showData = Boolean.valueOf(args.get("showdata")).booleanValue();
            boolean verbose = Boolean.valueOf(args.get("verbose")).booleanValue();
            int bufsize = Integer.parseInt(args.get("bufsize"));
            String inproto = args.get("inproto");
            String outproto = args.get("outproto");

            int lport = Integer.parseInt(inport);

            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("  Local Host   : " + localHost + ", Listen Port: " + lport);
            System.out.println("  IN protocol  : " + inproto);
            System.out.println("  I/O library  : " + JSTKSocketUtil.getIOLibrary(args, inproto));

            System.out.println("  Remote Host  : " + host + ", Port: " + port);
            System.out.println("  OUT protocol : " + outproto);
            System.out.println("  I/O library  : " + JSTKSocketUtil.getIOLibrary(args, outproto));
            System.out.println("  Buffer Size  : " + bufsize);
            System.out.println("  -----------------------------------");
            JSTKServerSocket jss = JSTKSocketUtil.createServerSocket(args);

            int count = 0;
            while (true) {
                JSTKSocket socket = jss.accept();
                if (verbose) {
                    System.out.println("[" + count + "] Accepted Connection Request ...");
                    JSTKSocketUtil.print(socket, " <-- ");
                }
                new ProxyThread(socket, count, args).start();
                ++count;
            }
        } catch (Exception exc) {
            throw new JSTKException("ProxyCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        ProxyCommand proxyCmd = new ProxyCommand();
        JSTKResult result = (JSTKResult) proxyCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
