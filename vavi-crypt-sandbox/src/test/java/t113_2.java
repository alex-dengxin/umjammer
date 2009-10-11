/*
 * Copyright (c) 2003 by Naohide Sano, All rights rserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import vavi.util.Debug;


/**
 * MS SSL.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (vavi)
 * @version 0.00 031205 nsano initial version <br>
 */
public class t113_2 {

    /**
     * The program entry.
     */
    public static void main(String[] args) throws Exception {
//      Security.addProvider(new com.boyter.mscrypto.MSKeyManagerProvider());
//      Security.addProvider(new com.boyter.mscrypto.MSTrustManagerProvider());

        new t113_2(args);
    }

    /** */
    public t113_2(String[] args) throws Exception {
        URL url = new URL(args[0]);

        HttpURLConnection huc = (HttpURLConnection) url.openConnection();

        //----

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("MSKMF");
        kmf.init(null, null);
        KeyManager[] km = kmf.getKeyManagers();

        // 証明書の信頼性を決定するためのインターフェース
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("MSTMF");
        tmf.init((KeyStore) null);
        TrustManager[] tm = tmf.getTrustManagers();

        // ソケットプロトコルを実装するSSLContextを作成
        SSLContext sslContext = SSLContext.getInstance("SSL");
        // SSLContextを初期化
        sslContext.init(km, tm, new SecureRandom());
        // SSLContextのSocketFactoryを取得
        SSLSocketFactory sslSF = sslContext.getSocketFactory();
        // URLConnectionにSocketFactoryをセット
        ((HttpsURLConnection) huc).setSSLSocketFactory(sslSF);

        //----

        // ホスト名を無視させる
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
Debug.println(hostname + ", "+ session);
                return true;
            }
        };
        ((HttpsURLConnection) huc).setHostnameVerifier(hv);

        // HTMLファイルをStreamで取得
        InputStream in = new BufferedInputStream(huc.getInputStream());
        OutputStream os = System.out;
        // OutputStreamに出力
        byte bb[] = new byte[1024];
        int length = 0;
        while ((length = in.read(bb, 0, bb.length)) != -1) {
            os.write(bb, 0, length);
        }
        in.close();
    }
}

/* */
