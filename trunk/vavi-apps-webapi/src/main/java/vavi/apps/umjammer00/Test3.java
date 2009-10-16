/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import vavi.net.rest.Rest;


/**
 * Test3. YouTubeListByTag (castor)
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070322 nsano initial version <br>
 */
public class Test3 {
    /** */
    public static void main(String[] args) throws Exception {
        Test3 test = new Test3();
        test.test1(args);
    }

    /** */
    void test1(String[] args) throws Exception {
        String tag = args[0];

        YouTubeListByTag queryBean = new YouTubeListByTag();
        queryBean.setToken(System.getProperty("rws.appid"));
        queryBean.setTag(tag);

        getResponse(queryBean);
    }

    /** as RPC */
    void getResponse(YouTubeListByTag queryBean) {
        String url = Rest.Util.getUrl(queryBean);
System.err.println("url: " + url);
        Request request = new Request(Method.GET, url);

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        try {
            output.write(System.out);
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
