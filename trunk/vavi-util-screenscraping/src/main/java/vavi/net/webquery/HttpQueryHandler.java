/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.webquery;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;


/**
 * HttpQueryHandler. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080219 nsano initial version <br>
 */
public class HttpQueryHandler implements QueryHandler {

    /**
     * create URL for HTTP protocol GET method
     */
    @Override
    public Query getQuery(String url, Map<String, String> parameters, boolean doInput, boolean doOutput) {
        try {
            if (doInput == true && doOutput == false) {
                StringBuilder sb = new StringBuilder();
    
                for (Entry<String, String> entry : parameters.entrySet()) {
                    sb.append(sb.length() == 0 ? '?' : '&');
                    sb.append(entry.getKey());
                    sb.append('=');
                    sb.append(entry.getValue());
                }
    
                Query query = new Query();
                query.setUrl(new URL(url));
                return query;
            } else if (doInput == true && doOutput == true) {
                throw new UnsupportedOperationException("unsupported method: POST");
            } else {
                throw new IllegalArgumentException("both are null doInput and doOutput");
            }
        } catch (MalformedURLException e) {
            throw (RuntimeException) new IllegalArgumentException("url: " + url).initCause(e);
        }
    }
}

/* */
