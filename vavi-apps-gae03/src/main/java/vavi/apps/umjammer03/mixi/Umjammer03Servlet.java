
package vavi.apps.umjammer03.mixi;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.signature.RSA_SHA1;


/**
 * OAuth analysis 
 */
@SuppressWarnings("serial")
public class Umjammer03Servlet extends HttpServlet {

    private final static String CERTIFICATE =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICdzCCAeCgAwIBAgIJAOi/chE0MhufMA0GCSqGSIb3DQEBBQUAMDIxCzAJBgNV\n" +
        "BAYTAkpQMREwDwYDVQQKEwhtaXhpIEluYzEQMA4GA1UEAxMHbWl4aS5qcDAeFw0w\n" +
        "OTA0MjgwNzAyMTVaFw0xMDA0MjgwNzAyMTVaMDIxCzAJBgNVBAYTAkpQMREwDwYD\n" +
        "VQQKEwhtaXhpIEluYzEQMA4GA1UEAxMHbWl4aS5qcDCBnzANBgkqhkiG9w0BAQEF\n" +
        "AAOBjQAwgYkCgYEAwEj53VlQcv1WHvfWlTP+T1lXUg91W+bgJSuHAD89PdVf9Ujn\n" +
        "i92EkbjqaLDzA43+U5ULlK/05jROnGwFBVdISxULgevSpiTfgbfCcKbRW7hXrTSm\n" +
        "jFREp7YOvflT3rr7qqNvjm+3XE157zcU33SXMIGvX1uQH/Y4fNpEE1pmX+UCAwEA\n" +
        "AaOBlDCBkTAdBgNVHQ4EFgQUn2ewbtnBTjv6CpeT37jrBNF/h6gwYgYDVR0jBFsw\n" +
        "WYAUn2ewbtnBTjv6CpeT37jrBNF/h6ihNqQ0MDIxCzAJBgNVBAYTAkpQMREwDwYD\n" +
        "VQQKEwhtaXhpIEluYzEQMA4GA1UEAxMHbWl4aS5qcIIJAOi/chE0MhufMAwGA1Ud\n" +
        "EwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADgYEAR7v8eaCaiB5xFVf9k9jOYPjCSQIJ\n" +
        "58nLY869OeNXWWIQ17Tkprcf8ipxsoHj0Z7hJl/nVkSWgGj/bJLTVT9DrcEd6gLa\n" +
        "H5TbGftATZCAJ8QJa3X2omCdB29qqyjz4F6QyTi930qekawPBLlWXuiP3oRNbiow\n" +
        "nOLWEi16qH9WuBs=\n" +
        "-----END CERTIFICATE-----";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        verifyFetch(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        verifyFetch(req, resp);
    }

    private void verifyFetch(HttpServletRequest request, HttpServletResponse resp) throws IOException, ServletException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            OAuthServiceProvider provider = new OAuthServiceProvider(null, null, null);
            OAuthConsumer consumer = new OAuthConsumer(null, "mixi.jp", null, provider);
            consumer.setProperty(RSA_SHA1.X509_CERTIFICATE, CERTIFICATE);

            String method = request.getMethod();
            String requestUrl = getRequestUrl(request);
            List<? extends Map.Entry<?, ?>> requestParameters = getRequestParameters(request);

            OAuthMessage message = new OAuthMessage(method, requestUrl, requestParameters);

            OAuthAccessor accessor = new OAuthAccessor(consumer);
            out.print("*** OAuthMessage Params:");
            out.print("URL: " + htmlEncode(message.URL));
            for (java.util.Map.Entry<?, ?> param : message.getParameters()) {
                String key = param.getKey().toString();
                String value = param.getValue().toString();
                out.print("</br>");
                out.print("Param Name-->" + htmlEncode(key));
                out.print(" ");
                out.print("Value-->" + htmlEncode(value));
            }
            out.print("</br>");
            out.print(" VALIDATING SIGNATURE ");
            out.print("</br>");
            OAuthValidator validator = new SimpleOAuthValidator();
            validator.validateMessage(message, accessor);
            out.print("REQUEST STATUS::OK");
            out.print("</br>");
        } catch (OAuthProblemException ope) {
            out.print("</br>");
            out.print("OAuthProblemException-->" + htmlEncode(ope.getProblem()));
        } catch (Exception e) {
            out.println(e);
            System.out.println(e);
            throw new ServletException(e);
        } finally {
            out.flush();
        }
    }

    /**
     * Constructs and returns the full URL associated with the passed request
     * object.
     * 
     * @param request Servlet request object with methods for retrieving the
     *            various components of the request URL
     */
    public static String getRequestUrl(HttpServletRequest request) {
        StringBuilder requestUrl = new StringBuilder();
        String scheme = request.getScheme();
        int port = request.getLocalPort();

        requestUrl.append(scheme);
        requestUrl.append("://");
        requestUrl.append(request.getServerName());

        if ((port != 0) && ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443))) {
            requestUrl.append(":");
            requestUrl.append(port);
        }

        requestUrl.append(request.getContextPath());
        requestUrl.append(request.getServletPath());

        return requestUrl.toString();
    }

    /**
     * Constructs and returns a List of OAuth.Parameter objects, one per
     * parameter in the passed request.
     * 
     * @param request Servlet request object with methods for retrieving the
     *            full set of parameters passed with the request
     */
    public static List<Parameter> getRequestParameters(HttpServletRequest request) {

        List<Parameter> parameters = new ArrayList<Parameter>();

        for (Object e : request.getParameterMap().entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) e;

            String[] values = (String[]) entry.getValue();
            for (String value : values) {
                parameters.add(new Parameter((String) entry.getKey(), value));
            }
        }

        return parameters;
    }

    /**
     * Return the HTML representation of the given plain text. Characters that
     * would have special significance in HTML are replaced by <a
     * href="http://www.w3.org/TR/html401/sgml/entities.html">character entity
     * references</a>. Whitespace is not converted.
     */
    public static String htmlEncode(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder html = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
            case '<':
                html.append("&lt;");
                break;
            case '>':
                html.append("&gt;");
                break;
            case '&':
                html.append("&amp;");
                // This also takes care of numeric character references;
                // for example &#169 becomes &amp;#169.
                break;
            case '"':
                html.append("&quot;");
                break;
            default:
                html.append(c);
                break;
            }
        }
        return html.toString();
    }

    /** A name/value pair. */
    public static class Parameter implements Map.Entry<String, String> {

        public Parameter(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private final String key;

        private String value;

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String setValue(String value) {
            try {
                return this.value;
            } finally {
                this.value = value;
            }
        }

        @Override
        public String toString() {
            return percentEncode(getKey()) + '=' + percentEncode(getValue());
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Parameter that = (Parameter) obj;
            if (key == null) {
                if (that.key != null)
                    return false;
            } else if (!key.equals(that.key))
                return false;
            if (value == null) {
                if (that.value != null)
                    return false;
            } else if (!value.equals(that.value))
                return false;
            return true;
        }
    }

    /** The encoding used to represent characters as bytes. */
    public static final String ENCODING = "UTF-8";

    public static String percentEncode(String s) {
        if (s == null) {
            return "";
        }
        try {
            return URLEncoder.encode(s, ENCODING)
                    // OAuth encodes some characters differently:
                    .replace("+", "%20").replace("*", "%2A")
                    .replace("%7E", "~");
            // This could be done faster with more hand-crafted code.
        } catch (UnsupportedEncodingException wow) {
            throw new RuntimeException(wow.getMessage(), wow);
        }
    }
}
