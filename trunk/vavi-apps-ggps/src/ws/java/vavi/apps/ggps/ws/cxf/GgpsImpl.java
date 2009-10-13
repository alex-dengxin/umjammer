/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.ggps.ws.cxf;

import javax.jws.WebService;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import vavi.apps.ggps.models.GpxFragment;
import vavi.apps.ggps.services.GgpsService;


/**
 * GgpsImpl. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070304 nsano initial version <br>
 */
@WebService(endpointInterface="org.apache.cxf.quoteReporter")
public class GgpsImpl implements GgpsSEI {

    /** */
    private GgpsService ggpsService;

    public GgpsImpl() {
        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext-persistence.xml");
            this.ggpsService = GgpsService.class.cast(applicationContext.getBean("ggpsService"));
        } catch (RuntimeException e) {
e.printStackTrace(System.err);
            throw e;
        }
    }

    /** */
    public GpxFragment getCurrentGpxFragment() {
        return ggpsService.getCurrentGpxFragment();
    }

    /** */
    public void setCurrentGpxFragment(GpxFragment gpx) {
        ggpsService.setCurrentGpxFragment(gpx);
    }
}

/* */
