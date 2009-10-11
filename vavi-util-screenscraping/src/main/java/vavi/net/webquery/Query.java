/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.webquery;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Query. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080220 nsano initial version <br>
 */
public class Query {

    /** */
    private URL url;

    /** */
    private Map<String,List<String>> headerFields;
    
    /** */
    private Map<String,List<String>> requestProperties;

    /** */
    public URL getUrl() {
        return url;
    }

    /** */
    public void setUrl(URL url) {
        this.url = url;
    }

    /** */
    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    /** */
    public void setHeaderField(String key, String value) {
        if (headerFields.containsKey(key)) {
            List<String> values = new ArrayList<String>();
            values.add(value);
            headerFields.put(key, values);
        } else {
            headerFields.get(key).add(value);
        }
    }

    /** */
    public Map<String, List<String>> getRequestProperties() {
        return requestProperties;
    }

    /** */
    public void setRequestProperty(String key, String value) {
        if (requestProperties.containsKey(key)) {
            List<String> values = new ArrayList<String>();
            values.add(value);
            requestProperties.put(key, values);
        } else {
            requestProperties.get(key).add(value);
        }
    }
}

/* */
