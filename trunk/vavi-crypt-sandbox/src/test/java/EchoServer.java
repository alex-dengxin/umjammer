/*
 * Copyright (c) 2001 Brian Boyter
 * All rights reserved
 *
 * This software is released subject to the GNU Public License.  See
 * the full license included with this distribution.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;


/**
 * EchoServer.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano initial version <br>
 */
public class EchoServer {
    /**
     * @throws Exception
     */
    public static void main(String[] arstring) throws Exception {
// System.setProperty("javax.net.debug","ssl");
System.setProperty("mscrypto.debug", "true");

        Security.addProvider(new com.boyter.mscrypto.MSKeyManagerProvider());
        Security.addProvider(new com.boyter.mscrypto.MSTrustManagerProvider());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("MSKMF");
        kmf.init(null, null);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("MSTMF");
System.out.println("TrustManagerProvider name: " + tmf.getProvider().getInfo());
        tmf.init((KeyStore) null);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SSLServerSocketFactory factory = ctx.getServerSocketFactory();

        int threadCount = 1;

        SSLServerSocket sslServerSocket = (SSLServerSocket) factory.createServerSocket(9999);
        sslServerSocket.setNeedClientAuth(true);

        while (true) {
            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
            System.out.println("connection made - make new thread number " + threadCount);
            new ThreadedEchoHandler(sslSocket, threadCount).start();
            threadCount++;
        }
    }
}

/** */
class ThreadedEchoHandler extends Thread {
    /** */
    public ThreadedEchoHandler(Socket s, int count) {
        socket = s;
        counter = count;
    }

    /** */
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // PrintWriter out = new PrintWriter(socket.getOutputStream(), true
            // /* autoFlush */);

            String string = null;
            while ((string = in.readLine()) != null) {
                if (string == null)
                    break;
                System.out.println(counter + ": " + string);
                System.out.flush();
            }

            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /** */
    private Socket socket;

    /** */
    private int counter;
}

/* */
