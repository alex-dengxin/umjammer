/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import java.io.InputStreamReader;

import net.java.sen.StringTagger;
import net.java.sen.Token;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import vavi.net.rest.Rest;


/**
 * Test. YahooJapanSearch (Castor)
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080319 nsano initial version <br>
 */
public class Test4 {
    /** */
    public static void main(String[] args) throws Exception {
        Test4 test = new Test4();
        test.test(args);
    }

    /** */
    void test(String[] args) throws Exception {
        String query = args[0];
        StringTagger tagger = StringTagger.getInstance();

        YahooJapanSearch queryBean = new YahooJapanSearch();
        queryBean.appid = System.getProperty("yjws.appid");
        queryBean.query = query;

        vavi.apps.umjammer00.castor4.ResultSet resultBean = getResult(queryBean);
        int i = 0;
        for (vavi.apps.umjammer00.castor4.Result result : resultBean.getResult()) {
            System.err.println("title[" + i + "]: " + result.getTitle());
            System.err.println("summary[" + i + "]: " + result.getSummary());
            i++;

            for (Token token : tagger.analyze(result.getTitle())) {
                System.err.println(token + "\t" + token.getPos());
            }
        }
    }

    /** as RPC */
    vavi.apps.umjammer00.castor4.ResultSet getResult(YahooJapanSearch queryBean) {
        String url = Rest.Util.getUrl(queryBean);
System.err.println("url: " + url);
        Request request = new Request(Method.GET, url);

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        try {
//Reader r = new InputStreamReader(output.getStream(), "JISAutoDetect");
//while (r.ready()) {
//  System.err.print((char) r.read());
//}
            return vavi.apps.umjammer00.castor4.ResultSet.unmarshal(new InputStreamReader(output.getStream(), "UTF-8"));
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
