/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import vavi.net.ws.webservicex.geoipservice.GeoIPService;
import vavi.net.ws.webservicex.geoipservice.GeoIPServiceSoap;


/**
 * GeoIP. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070930 nsano initial version <br>
 */
public class GeoIP {

    /**
     * 
     * @param args 0: ip address 
     */
    public static void main(String[] args) throws Exception {
        String ip = args[0];

        GeoIPService service = new GeoIPService();
        GeoIPServiceSoap client = service.getGeoIPServiceSoap();

        vavi.net.ws.webservicex.geoipservice.GeoIP result = client.getGeoIP(ip);

        System.out.println(ip + ": " + result.getCountryCode() + ", " + result.getCountryName());
    }
}

/* */
