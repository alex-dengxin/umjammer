/*
 * Copyright (c) 2005 by Naohide Sano, All rights rserved.
 *
 * Programmed by Naohide Sano
 */

import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * t113_1
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (vavi)
 * @version 0.00 051215 nsano initial version <br>
 */
public class t113_1 {

    /** */
    public static void exec(URLConnection connection) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        TrustManager[] tm = { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        }};
        sslContext.init(null, tm, new SecureRandom());
        SSLSocketFactory sslsf = sslContext.getSocketFactory();
        ((HttpsURLConnection) connection).setSSLSocketFactory(sslsf);
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        ((HttpsURLConnection) connection).setHostnameVerifier(hv);
    }
}

/* */
