/*
 * @(#) $Id: ClientCommand.java,v 1.1.1.1 2003/10/05 18:39:22 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class ClientCommand extends JSTKCommandAdapter {
    public class ReaderThread extends Thread {
        private JSTKSocket socket;

        private JSTKBuffer buf;

        public ReaderThread(JSTKSocket socket, JSTKBuffer buf) {
            super("ReaderThread");
            this.socket = socket;
            this.buf = buf;
        }

        public void run() {
            try {
//                int n;
                while ((/*n =*/ socket.read(buf)) != -1)
                    ;
            } catch (Exception e) {
            }
        }
    }

    public class CustomHostnameVerifier implements HostnameVerifier {
        private String expectedHostname;

        public CustomHostnameVerifier(String expectedHostname) {
            this.expectedHostname = expectedHostname;
        }

        public boolean verify(String hostname, SSLSession sess) {
            System.out.print("Expected: " + expectedHostname + ", Got: " + hostname + ". ");
            System.out.print("Proceed(yes/no)?");
            System.out.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String response = null;
            try {
                response = br.readLine();
            } catch (IOException ioe) {
                response = "no";
            }
            if ("yes".equalsIgnoreCase(response.trim()))
                return true;
            return false;
        }
    }

    private static int outPort = -1;

    public static int getOutPort() {
        return outPort;
    }

    public static void setOutPort(int port) {
        outPort = port;
    }

    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("host", "localhost");
        defaults.put("port", "9000");
        defaults.put("outproto", "TCP");
        defaults.put("mode", "prompt");
        defaults.put("bufsize", "8192");
        defaults.put("num", "2048");
        defaults.put("action", "write-only");
    }

    public String briefDescription() {
        String briefDesc = "simple client for TCP or SSL connections";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -host <host>    : Remote host machine name or IP address.[" + defaults.get("host") + "]\n" + "  -port <port>    : Destination port on remote host.[" + defaults.get("port") + "]\n" + "  -outproto <proto>: Outgoing connection protocol (TCP or SSL).[" + defaults.get("outproto") + "]\n" + "  -csfile <csfile>: File having cipher suits to be enabled.\n" + "  -mode <mode>    : Client operation mode(prompt|bench|read-url).[" + defaults.get("mode") + "]\n"
                             + "  -action <action>: What action to take -- applicable for \"bench\" mode\n" + "                      (open-close|write-only|write-read|read-url).[" + defaults.get("action") + "]\n" + "  -bufsize <size> : len. of buf (for bench mode).[" + defaults.get("bufsize") + "]\n" + "  -num <num>      : no. of times buf is written/read(for bench mode).[" + defaults.get("num") + "]\n" + "  -url <httpurl>  : URL to be read (for bench/read-url or read-url).\n"
                             + "  -pattern <pat>  : Fill buffer with this pattern(for bench mode).\n" + "  -verbose        : Verbose output.\n" + "  -conn close     : Close connection with every read-url action.\n" + "  -inetaddr <addr>: Network IP address (useful for multi-homed hosts).\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-host <host>] [-port <port>] [-ssl] [-inetaddr <addr>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "", "-host venus -port 2950", "-ssl"
        };
        return sampleUses;
    }

    private JSTKResult runRMIClient(JSTKArgs args) throws Exception {
        int bufsize = Integer.parseInt(args.get("bufsize"));
        int num = Integer.parseInt(args.get("num"));
        int port = Integer.parseInt(args.get("port"));
        String mode = args.get("mode");
        String action = args.get("action");
        boolean verbose = Boolean.valueOf(args.get("verbose")).booleanValue();
//        String pattern = args.get("pattern");
        String host = args.get("host");

        setOutPort(port);
        RMIServerInterface obj = null;
        String url = "//" + host + "/RMIServer";
        try {
            obj = (RMIServerInterface) java.rmi.Naming.lookup(url);
        } catch (Exception exc) {
            return new JSTKResult(null, false, "Failed to lookup \"" + url + "\": " + exc);
        }

        if (mode.equalsIgnoreCase("bench")) {
            int numBytes = 0;
            byte[] buf = new byte[bufsize];
            if (action.equalsIgnoreCase("write-only")) {
                long st = System.currentTimeMillis();
                for (int i = 0; i < num; i++) {
                    obj.writeOnly(buf);
                    if (verbose)
                        System.out.println("[" + i + "]Wrote: " + buf.length + " bytes.");
                    numBytes += buf.length;
                }
                long et = System.currentTimeMillis();
                double writeRate = (numBytes * 1000.0) / (et - st);
                double tt = (et - st) / 1000.0;

                System.out.print("Invoked RMI method " + num + " times in " + tt + " seconds.\n");
                System.out.println(" Write Rate: " + writeRate + " bytes/sec.");
            } else if (action.equalsIgnoreCase("write-read")) {
                int numRead = 0;
                long st = System.currentTimeMillis();
                for (int i = 0; i < num; i++) {
                    byte[] tbuf = obj.writeRead(buf);
                    if (verbose)
                        System.out.println("[" + i + "]Wrote: " + buf.length + " bytes, Read: " + tbuf.length + " bytes.");
                    numBytes += buf.length;
                    numRead += tbuf.length;
                }
                long et = System.currentTimeMillis();
                double writeRate = (numBytes * 1000.0) / (et - st);
                double readRate = (numRead * 1000.0) / (et - st);
                double tt = (et - st) / 1000.0;

                System.out.print("Invoked RMI method " + num + " times in " + tt + " seconds.\n");
                System.out.println(" Write Rate: " + writeRate + " bytes/sec.");
                System.out.println(" Read Rate : " + readRate + " bytes/sec.");
            } else {
                return new JSTKResult(null, true, "Unknown action: " + action);
            }
        } else if (mode.equalsIgnoreCase("prompt")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("Enter Message: ");
                System.out.flush();
                String inp = br.readLine();
                if (inp.equalsIgnoreCase("quit"))
                    break;
                byte[] tbuf = obj.writeRead(inp.getBytes());
                System.out.println("Server Returned: " + new String(tbuf));
            }
        } else {
            return new JSTKResult(null, true, "Unknown mode: " + mode);
        }
        return new JSTKResult(null, true, "DONE");
    }

    private JSTKResult processReadURL(JSTKArgs args, int num, int bufsize, String mode, boolean verbose) throws IOException {
        boolean connClose = false;
        String urlString = args.get("url");
        String connString = args.get("conn");
        if (connString != null && connString.equalsIgnoreCase("close"))
            connClose = true;
        if (urlString == null)
            return new JSTKResult(null, false, "No URL specified");

        System.out.println("  URL          : " + urlString);

        URL url = new URL(urlString);
        CustomHostnameVerifier custVerifier = new CustomHostnameVerifier(url.getHost());
        HttpsURLConnection.setDefaultHostnameVerifier(custVerifier);
        if (mode.equalsIgnoreCase("bench")) {
            long st = System.currentTimeMillis();
            int numBytes = 0;
            byte[] buf = new byte[bufsize];
            for (int i = 0; i < num; i++) {
                HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
                BufferedInputStream bis = new BufferedInputStream(urlCon.getInputStream());
                int n;
                int nread = 0;
                while ((n = bis.read(buf)) != -1) {
                    nread += n;
                }
                if (verbose) {
                    System.out.println("[" + i + "]Read: " + nread + " bytes from: " + urlString);
                }
                numBytes += nread;
                if (connClose)
                    urlCon.disconnect();
            }
            long et = System.currentTimeMillis();
            double browseRate = (numBytes * 1000.0) / (et - st);
            double tt = (et - st) / 1000.0;

            System.out.print("Read URL " + urlString + " " + num + " times in " + tt + " seconds.\n");
            System.out.println(" Browse Rate: " + browseRate + " bytes/sec.");
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        return new JSTKResult(null, true, "DONE");
    }

    private JSTKResult runTCPClient(JSTKArgs args) throws Exception {
        String host = args.get("host");
        int port = Integer.parseInt(args.get("port"));
        int bufsize = Integer.parseInt(args.get("bufsize"));
        int num = Integer.parseInt(args.get("num"));
//        String inetAddrVal = args.get("inetaddr");
        String mode = args.get("mode");
        String action = args.get("action");
        boolean verbose = Boolean.valueOf(args.get("verbose")).booleanValue();
        String pattern = args.get("pattern");
        String outproto = args.get("outproto");

        System.out.println("  I/O library  : " + JSTKSocketUtil.getIOLibrary(args, outproto));
        System.out.println("  Remote Host  : " + host + ", Remote Port: " + port);
        System.out.println("  -----------------------------------");

        JSTKSocket socket = JSTKSocketUtil.connect(args);
        JSTKSocketUtil.print(socket, " --> ");
        if (mode.equalsIgnoreCase("prompt")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            // byte[] buf = new byte[1024];
            JSTKBuffer buf = JSTKBuffer.getInstance(bufsize, args);
            while (true) {
                System.out.print("Enter Message: ");
                System.out.flush();
                String inp = br.readLine();
                if (inp.equalsIgnoreCase("quit"))
                    break;
                buf.clear();
                buf.putBytes(inp.getBytes());
                socket.write(buf);
                /*int n = */socket.read(buf);
                System.out.println("Server Returned: " + new String(buf.getBytes()));
            }
            socket.close();
        } else if (mode.equalsIgnoreCase("bench")) {
            JSTKBuffer buf = JSTKBuffer.getInstance(bufsize, args);
            /*JSTKBuffer buf1 =*/ JSTKBuffer.getInstance(bufsize, args);
            (new PatternUtil("00000000")).fillPattern(buf);

            PatternUtil pu = new PatternUtil(pattern);

            long numBytes = 0;
            if (action.equalsIgnoreCase("write-only")) {
                long st = System.currentTimeMillis();
                for (int i = 0; i < num; i++) {
                    if (pu.needToFill())
                        pu.fillPattern(buf);
                    socket.write(buf);
                    numBytes += buf.getNBytes();
                    if (verbose) {
                        System.out.println("[" + i + "]Wrote: " + buf.length() + " bytes.");
                    }
                }
                long et = System.currentTimeMillis();
                socket.close();
                double xferRate = (numBytes / (et - st)) * (1000.0 / 1024.0);
                double tt = (et - st) / 1000.0;

                System.out.print("Sent " + numBytes + " bytes in " + tt + " seconds.\n");
                System.out.println(" 1-way Xfer Rate: " + xferRate + " KB/sec.");
            } else if (action.equalsIgnoreCase("write-read")) {
                int numBytesRead = 0;
                long st = System.currentTimeMillis();
                for (int i = 0; i < num; i++) {
                    if (pu.needToFill())
                        pu.fillPattern(buf);
                    socket.write(buf);
                    numBytes += buf.getNBytes();
                    if (verbose) {
                        System.out.println("[" + i + "]Sent: " + buf.length() + " bytes.");
                    }
                    int nread = 0;
                    while (nread < bufsize) {
                        int n = socket.read(buf);
                        if (n == -1)
                            break;
                        nread += n;
                    }
                    numBytesRead += nread;
                }
                long et = System.currentTimeMillis();
                socket.close();
                double xferRate = (numBytes / (et - st)) * (1000.0 / 1024.0);
                double tt = (et - st) / 1000.0;

                System.out.print("Wrote " + numBytes + "and Read " + numBytesRead + " bytes in " + tt + " seconds.\n");
                System.out.println(" 2-way Xfer Rate: " + xferRate + " KB/sec.");
            } else if (action.equalsIgnoreCase("open-close")) {
                socket.close(); // First connection not counted in the benchmark.
                boolean invalidate = Boolean.valueOf(args.get("invalidate")).booleanValue();
                long st = System.currentTimeMillis();
                for (int i = 0; i < num; i++) {
                    socket = JSTKSocketUtil.connect(args);
                    if (verbose)
                        System.out.println("[" + i + "]Opened Socket ...");

                    if (invalidate && socket.getSocket() instanceof SSLSocket) {
                        if (verbose)
                            System.out.println("[" + i + "]Invalidating the SSLSession ...");
                        SSLSocket sslSock = (SSLSocket) socket.getSocket();
                        SSLSession sess = sslSock.getSession();
                        sess.invalidate();
                    }

                    socket.close();
                    if (verbose)
                        System.out.println("[" + i + "]Closed Socket ...");
                }
                long et = System.currentTimeMillis();
                double connRate = (num * 1000.0) / (et - st);
                double tt = (et - st) / 1000.0;

                System.out.print("" + num + " connections in " + tt + " seconds.\n");
                System.out.println(" Connection Rate: " + connRate + " connections/sec.");
            } else if (action.equalsIgnoreCase("read-url")) {
                socket.close(); // First connection not counted in the benchmark.
                long st = System.currentTimeMillis();
                for (int i = 0; i < num; i++) {
                    socket = JSTKSocketUtil.connect(args);
                    socket.close();
                }
                long et = System.currentTimeMillis();
                double connRate = (num * 1000.0) / (et - st);
                double tt = (et - st) / 1000.0;

                System.out.print("" + num + " connections in " + tt + " seconds.\n");
                System.out.println(" Connection Rate: " + connRate + " connections/sec.");
            } else {
                return new JSTKResult(null, false, "Unknown action: " + action);
            }
        } else {
            return new JSTKResult(null, false, "Unknown mode: " + mode);
        }
        return new JSTKResult(null, true, "DONE");
    }

    private JSTKResult runSSLClient(JSTKArgs args) throws Exception {
        String[] csarray = JSTKSocketUtil.getCSFileCipherSuites(args);
        if (csarray != null) {
            System.out.println("  Cipher Suites to be enabled   : ");
            for (int i = 0; i < csarray.length; i++)
                System.out.println("         " + csarray[i]);
        }
        return runTCPClient(args);
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
//            String host = args.get("host");
//            int port = Integer.parseInt(args.get("port"));
            int bufsize = Integer.parseInt(args.get("bufsize"));
            int num = Integer.parseInt(args.get("num"));
//            String inetAddrVal = args.get("inetaddr");
            String mode = args.get("mode");
            String action = args.get("action");
            boolean verbose = Boolean.valueOf(args.get("verbose")).booleanValue();
//            String pattern = args.get("pattern");
            String outproto = args.get("outproto");
            String urlString = args.get("url");

            if (urlString != null) {
                if (urlString.startsWith("https"))
                    outproto = "HTTPS";
                else if (urlString.startsWith("http"))
                    outproto = "HTTP";
                else
                    return new JSTKResult(null, false, "Unknown URL format: " + urlString);
            }

            System.out.println("  Client Mode  : " + mode);
            System.out.println("  OUT protocol : " + outproto);

            if (mode.equalsIgnoreCase("bench")) {
                System.out.println("  Client Action: " + action);
                System.out.println("  Buffer Size  : " + bufsize);
                System.out.println("  Iterations   : " + num);
            }

            if (outproto.equalsIgnoreCase("RMI") || outproto.equalsIgnoreCase("SRMI")) {
                return runRMIClient(args);
            } else if (outproto.equalsIgnoreCase("TCP")) {
                return runTCPClient(args);
            } else if (outproto.equalsIgnoreCase("SSL")) {
                return runSSLClient(args);
            } else if (outproto.equalsIgnoreCase("HTTP")) {
                return processReadURL(args, num, bufsize, mode, verbose);
            } else if (outproto.equalsIgnoreCase("HTTPS")) {
                return processReadURL(args, num, bufsize, mode, verbose);
            }
            return new JSTKResult(null, false, "Unknown OUT Protocol: " + outproto);
        } catch (Exception exc) {
            throw new JSTKException("ClientCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        ClientCommand clientCmd = new ClientCommand();
        JSTKResult result = (JSTKResult) clientCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
