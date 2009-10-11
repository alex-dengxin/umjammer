/*
 * Copyright (c) 2001 Brian Boyter
 * All rights reserved
 *
 * This software is released subject to the GNU Public License.  See
 * the full license included with this distribution.
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


/**
 * EchoClient. 
 *
 * @author  <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00    050314  nsano   initial version <br>
 */
public class EchoClient {

    /** */
    public static void main(String[] arstring) throws Exception {
// System.setProperty("javax.net.debug","ssl");
System.setProperty("mscrypto.debug", "true");
        SSLSocketFactory factory = null;
        SSLContext ctx = null;
        KeyManagerFactory kmf;
        TrustManagerFactory tmf;

        Security.addProvider(new com.boyter.mscrypto.MSKeyManagerProvider());
        Security.addProvider(new com.boyter.mscrypto.MSTrustManagerProvider());

        kmf = KeyManagerFactory.getInstance("MSKMF");
        kmf.init(null, null);

        tmf = TrustManagerFactory.getInstance("MSTMF");
System.out.println("TrustManagerProvider name: " + tmf.getProvider().getInfo());
        tmf.init((KeyStore) null);

        ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        factory = ctx.getSocketFactory();

        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        SSLSocket sslSocket = (SSLSocket) factory.createSocket("localhost", 9999);

        InputStream inputStream = System.in;
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        OutputStream outputStream = sslSocket.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        // send a line to the server
        String testtext = "\n****Connection opened from EchoClient****\n\n";
        writer.write(testtext, 0, testtext.length());
        writer.flush();

        String string = null;
        while ((string = bufferedReader.readLine()) != null) {
            string = string + '\n';
            writer.write(string, 0, string.length());
            writer.flush();
        }
    }
}

/* */
