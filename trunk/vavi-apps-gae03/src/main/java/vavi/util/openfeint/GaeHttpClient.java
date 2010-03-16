/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.openfeint;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import net.oauth.http.HttpClient;
import net.oauth.http.HttpMessage;
import net.oauth.http.HttpResponseMessage;


/**
 * GaeHttpClient. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/02/14 nsano initial version <br>
 */
public class GaeHttpClient implements HttpClient {

    private static final String acceptLanguage = "en;q=0.3";
    private static final String userAgent = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 2_0 like Mac OS X;ja-jp) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1Mobile/5A345";

    /* */
    @Override
    public HttpResponseMessage execute(HttpMessage request, Map<String, Object> parameters) throws IOException {
        URL url = new URL(request.url.toExternalForm());
System.err.println(url);
            
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//System.err.println(request.method);
        if (request.method.equalsIgnoreCase("POST")) {
            conn.setDoOutput(true);
        }
        conn.setRequestMethod(request.method);
        conn.setRequestProperty("Accept-Language", acceptLanguage);
        conn.setRequestProperty("User-Agent", userAgent);
        for (Map.Entry<String, Object> p : parameters.entrySet()) {
            String name = p.getKey();
            String value = p.getValue().toString();
            if (FOLLOW_REDIRECTS.equals(name)) {
                conn.setInstanceFollowRedirects(Boolean.parseBoolean(value));
            } else if (READ_TIMEOUT.equals(name)) {
                conn.setReadTimeout(10 * 1000);
            } else if (CONNECT_TIMEOUT.equals(name)) {
                conn.setConnectTimeout(10 * 1000);
            }
        }
            
        for (Map.Entry<String, String> header : request.headers) {
//System.err.println(header.getKey() + ": " + header.getValue());
            conn.setRequestProperty(header.getKey(), header.getValue());
        }

        if (conn.getDoOutput()) {
            StringBuilder sb = new StringBuilder();
            InputStream is = request.getBody();
            if (is != null) {
                while (is.available() > 0) {
                    sb.append((char) is.read());
                }
            }
            if (sb.length() > 0) {
                conn.setRequestProperty("Content-Length", String.valueOf(sb.length()));
            }
System.err.println(sb);
            PrintStream ps = new PrintStream(conn.getOutputStream());
            ps.print(sb);
            ps.close();
        }

        conn.connect();

        return new HttpMethodResponse(conn);
    }
}

/* */
