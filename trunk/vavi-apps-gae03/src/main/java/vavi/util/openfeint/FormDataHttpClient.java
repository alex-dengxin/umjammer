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
import java.util.HashMap;
import java.util.Map;

import net.oauth.http.HttpClient;
import net.oauth.http.HttpMessage;
import net.oauth.http.HttpResponseMessage;


/**
 * FormDataHttpClient. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/02/14 nsano initial version <br>
 */
public class FormDataHttpClient implements HttpClient {

    private static final String acceptLanguage = "en;q=0.3";
    private static final String userAgent = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 2_0 like Mac OS X;ja-jp) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1Mobile/5A345";

    private String blobName;
    private InputStream blobIs;

    public FormDataHttpClient(String blobName, InputStream blobIs) {
        this.blobName = blobName;
        this.blobIs = blobIs;
    }

    private static final String boundaryString = "sadf8as9fha3fpaef8ah";

    /* */
    public HttpResponseMessage execute(HttpMessage request, Map<String, Object> parameters) throws IOException {
        URL url = new URL(request.url.toExternalForm());
System.err.println(url);
            
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//System.err.println(request.method);
        if (!request.method.equalsIgnoreCase("POST")) {
            throw new IllegalArgumentException("post only");
        }
        conn.setDoOutput(true);
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
            if ("Content-Type".equalsIgnoreCase(header.getKey())) {
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);
            } else if ("Content-Length".equalsIgnoreCase(header.getKey())) {
                // ignore
            } else {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        Map<String, String> params = new HashMap<String, String>();
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
//System.err.println(sb);
        String[] paramStrings = sb.toString().split("&");
        for (String paramString : paramStrings) {
            String[] pair = paramString.split("=");
            params.put(pair[0], pair[1]);
        }

        PrintStream ps = new PrintStream(conn.getOutputStream());
        for (Map.Entry<String, String> param : params.entrySet()) {
            ps.print("--" + boundaryString + "\r\n");
            ps.print("Content-Disposition: form-data; name=\"" + param.getKey() + "\"\r\n");
            ps.print("\r\n");
            ps.print(param.getValue() + "\r\n");
        }

        ps.print("--" + boundaryString + "\r\n");
        ps.print("Content-Disposition: form-data; name=\"" + blobName + "\"; filename=\"" + "image.jpg" + "\"\r\n");
        ps.print("Content-Type: " + "image/jpeg" + "\r\n");
        ps.print("Content-Transfer-Encoding: " + "binary" + "\r\n");  
        ps.print("\r\n");
        byte[] bytes = new byte[8192];
//int i = 0;
//System.err.println("available: " + blobIs.available());
        while (blobIs.available() > 0) {
            int r = blobIs.read(bytes, 0, bytes.length);
            if (r < 0) {
                break;
            }
//if (i == 0) {
// System.err.printf("%02x %02x %02x %02x\n", bytes[0] & 0xff, bytes[1] & 0xff, bytes[2] & 0xff, bytes[3] & 0xff);
//}
            ps.write(bytes, 0, r);
//i++;
        }
        
        ps.print("\r\n");
        ps.print("--" + boundaryString + "--" + "\r\n");

        ps.close();

        conn.connect();

        return new HttpMethodResponse(conn);
    }
}

/* */
