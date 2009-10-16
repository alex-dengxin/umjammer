/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import java.io.InputStreamReader;
import java.util.Random;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import vavi.net.rest.Rest;


/**
 * Test7. YahooJapanParse (JAXB)
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080319 nsano initial version <br>
 */
public class Test7 {
    /** */
    public static void main(String[] args) throws Exception {
        Test7 test = new Test7();
        test.test(args);
    }

    public Random random = new Random(System.currentTimeMillis());

    /** */
    void test(String[] args) throws Exception {
        String sentence = args[0];

System.err.println("sentence: " + sentence);
        YahooJapanParse queryBean = new YahooJapanParse();
        queryBean.appid = System.getProperty("yjws.appid");
        queryBean.sentence = sentence;

        vavi.apps.umjammer00.castor7.ResultSet resultBean = getResult(queryBean);
        vavi.apps.umjammer00.castor7.Ma_result ma_result = resultBean.getMa_result();
        vavi.apps.umjammer00.castor7.Word_list word_list = ma_result.getWord_list();
        for (vavi.apps.umjammer00.castor7.Word word : word_list.getWord()) {
            System.err.println(word.getSurface() + ", " + word.getPos());
        }
    }

    /** as RPC */
    public vavi.apps.umjammer00.castor7.ResultSet getResult(YahooJapanParse queryBean) {
        String url = Rest.Util.getUrl(queryBean);
System.err.println("url: " + url);
        Request request = new Request(Method.GET, url);

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        try {
            return vavi.apps.umjammer00.castor7.ResultSet.unmarshal(new InputStreamReader(output.getStream(), "UTF-8"));
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
