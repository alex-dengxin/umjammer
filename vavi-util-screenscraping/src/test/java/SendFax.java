/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import vavi.net.ws.webservicex.sendfax.Fax;
import vavi.net.ws.webservicex.sendfax.FaxSoap;


/**
 * SendFax. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070930 nsano initial version <br>
 */
public class SendFax {

    /**
     * 
     * @param args 0: , 1: 
     */
    public static void main(String[] args) throws Exception {
        String email = args[0];
        String subject = args[1];
        String fax = args[2];
        String message = args[3];
        String to = args[4];

        Fax service = new Fax();
        FaxSoap client = service.getFaxSoap();

        String result = client.sendTextToFax(email, subject, fax, message, to);

        System.out.println(result);
    }
}

/* */
