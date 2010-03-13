/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import vavi.net.ws.webservicex.sendsmsworld.SendSMSWorld;
import vavi.net.ws.webservicex.sendsmsworld.SendSMSWorldSoap;


/**
 * SendSMS. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070930 nsano initial version <br>
 */
public class SendSMS {

    /**
     * 
     * @param args 0: email, 1: country, 2: phone, 3: message
     */
    public static void main(String[] args) throws Exception {
        String email = args[0];
        String country = args[1];
        String phone = args[2];
        String message = args[3];

        SendSMSWorld service = new SendSMSWorld();
        SendSMSWorldSoap client = service.getSendSMSWorldSoap();

        String result = client.sendSMS(email, country, phone, message);

        System.out.println(result);
    }
}

/* */
