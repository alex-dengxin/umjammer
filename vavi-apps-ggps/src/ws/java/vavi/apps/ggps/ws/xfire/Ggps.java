/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.ggps.ws.xfire;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import vavi.apps.ggps.models.GpxFragment;
import vavi.apps.ggps.services.GgpsService;


/**
 * Ggps. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070304 nsano initial version <br>
 */
@WebService(name = "Ggps",
            targetNamespace = "http://ws.ggps.apps.vavi/Ggps",
            serviceName = "Ggps")
public class Ggps {

    /** */
    private GgpsService ggpsService;

    public Ggps() {
        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext-persistence.xml");
            this.ggpsService = GgpsService.class.cast(applicationContext.getBean("ggpsService"));
        } catch (RuntimeException e) {
e.printStackTrace(System.err);
            throw e;
        }
    }

    /** */
    @WebMethod(operationName = "getCurrentGpxFragment", action = "urn:getCurrentGpxFragment")
    public GpxFragment getCurrentGpxFragment(String token) {
        return ggpsService.getCurrentGpxFragment();
    }

    /** */
    @WebMethod(operationName = "setCurrentGpxFragment", action = "urn:setCurrentGpxFragment")
    public void setCurrentGpxFragment(String token, GpxFragment gpx) {
        ggpsService.setCurrentGpxFragment(gpx);
    }
}

/* */
