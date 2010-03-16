package vavi.util.openfeint;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import net.oauth.OAuth;
import net.oauth.http.HttpResponseMessage;


class HttpMethodResponse extends HttpResponseMessage {

    /**
     * Construct an OAuthMessage from the HTTP response, including parameters
     * from OAuth WWW-Authenticate headers and the body. The header parameters
     * come first, followed by the ones from the response body.
     */
    public HttpMethodResponse(HttpURLConnection conn) throws IOException {
        super(conn.getRequestMethod(), conn.getURL());
        this.conn = conn;
    }

    private final HttpURLConnection conn;

    @Override
    public int getStatusCode() {
        try {
            return conn.getResponseCode();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public InputStream openBody() throws IOException {
        InputStream is = conn.getInputStream();
        // for GAE, normally followings place at constructor
        for (Map.Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
            for (String value : header.getValue()) {
                headers.add(new OAuth.Parameter(header.getKey(), value));
            }
        }
        return is;
    }

    protected void finalize() throws Throwable {
        conn.disconnect();
    }
}