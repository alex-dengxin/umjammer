/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import java.io.InputStreamReader;
import java.util.Date;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import vavi.net.rest.Rest;


/**
 * Test2. RakutenVacantHotelSearch (castor)
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070310 nsano initial version <br>
 */
public class Test2 {
    /** */
    public static void main(String[] args) throws Exception {
        Test2 test = new Test2();
        test.test1(args);
    }

    /** */
    void test1(String[] args) throws Exception {
        int hotelNo = Integer.parseInt(args[0]);

        RakutenVacantHotelSearch queryBean = new RakutenVacantHotelSearch();
        queryBean.setToken(System.getProperty("rws.appid"));
        queryBean.setHotelNo(hotelNo);
        queryBean.setCheckinDate(new Date(System.currentTimeMillis()));
        queryBean.setCheckoutDate(new Date(System.currentTimeMillis() + 60 * 60 * 24 * 2 * 1000));
        queryBean.setMaxCharge(10000);

        vavi.apps.umjammer00.castor2.Response response = getResponse(queryBean);
//        Hotel hotel = response.getBody().getVacantHotelSearch().getHotel();
//System.err.println("hotel.name: " + hotel.getHotelName());
//System.err.println("hotel.tel: " + hotel.getTelephoneNo());
//System.err.println("hotel.sp: " + hotel.getHotelSpecial());
//for (Room room : response.getBody().getVacantHotelSearch().getRoom()) {
// System.err.println("room.: " + room.getRoomName());
//}
    }

    /** as RPC */
    vavi.apps.umjammer00.castor2.Response getResponse(RakutenVacantHotelSearch queryBean) {
        String url = Rest.Util.getUrl(queryBean);
System.err.println("url: " + url);
        Request request = new Request(Method.GET, url);

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        try {
//output.write(System.err);
            return vavi.apps.umjammer00.castor2.Response.unmarshal(new InputStreamReader(output.getStream(), "UTF-8"));
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
