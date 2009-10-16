/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import vavi.net.rest.Rest;


/**
 * Test. YahooJapanLocalSearch (castor)
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070224 nsano initial version <br>
 */
public class Test {
    /** */
    public static void main(String[] args) throws Exception {
        Test test = new Test();
//        test.test3(args);
        test.test4(args);
    }

    /** */
    void test3(String[] args) throws Exception {
        String address = args[0];

        final String YJWS_REST_URL_LOCAL_SEARCH = "http://api.map.yahoo.co.jp/LocalSearchService/V1/LocalSearch";
        String url = YJWS_REST_URL_LOCAL_SEARCH + "?" +
            "appid" + "=" + System.getProperty("yjws.appid") + "&" +
            "p" + "=" + URLEncoder.encode(address, "UTF-8");
System.err.println("url: " + url);
        Request request = new Request(Method.GET, url);

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        output.write(System.out);
    }

    /** */
    void test1() throws Exception {
        // Outputting the content of a Web page
        Client client = new Client(Protocol.HTTP);
        client.get("http://www.restlet.org").getEntity().write(System.out);
    }

    /** */
    void test2() throws Exception {
        // Prepare the request
        Request request = new Request(Method.GET, "http://www.restlet.org");
        request.setReferrerRef("http://www.mysite.org");

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        output.write(System.out);
    }

    /** */
    void test4(String[] args) throws Exception {
        String address = args[0];

        YahooJapanLocalSearch queryBean = new YahooJapanLocalSearch();
        queryBean.setToken(System.getProperty("yjws.appid"));
        queryBean.setKeyword(address);

        vavi.apps.umjammer00.castor1.LocalSearchResult resultBean = getLocalSearchResult(queryBean);
System.err.println("count: " + resultBean.getCount());
System.err.println("viewCount: " + resultBean.getViewCount());
for (vavi.apps.umjammer00.castor1.Item item : resultBean.getItem()) {
 System.err.println("item/category: " + item.getCategory());
 System.err.println("item/title: " + item.getTitle());
 System.err.println("item/address: " + item.getAddress());
 System.err.println("item/addressLevel: " + item.getAddressLevel());
 System.err.println("item/tky97: " + item.getDatumTky97().getLat() + ", " + item.getDatumTky97().getLon());
 System.err.println("item/wgs84: " + item.getDatumWgs84().getLat() + ", " + item.getDatumWgs84().getLon());
}
    }

    /** as RPC */
    vavi.apps.umjammer00.castor1.LocalSearchResult getLocalSearchResult(YahooJapanLocalSearch queryBean) {
        String url = Rest.Util.getUrl(queryBean);
System.err.println("url: " + url);
        Request request = new Request(Method.GET, url);

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        try {
            return vavi.apps.umjammer00.castor1.LocalSearchResult.unmarshal(new InputStreamReader(output.getStream(), "UTF-8"));
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
