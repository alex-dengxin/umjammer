/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.ggps.ws.cxf;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.WebParam.Mode;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import vavi.apps.ggps.models.GpxFragment;


/**
 * GgpsSEI. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070304 nsano initial version <br>
 */
@WebService(name = "Ggps",
            targetNamespace = "http://ws.ggps.apps.vavi/Ggps",
            serviceName = "Ggps")
public interface GgpsSEI {
    /** */
    @WebMethod(operationName = "getCurrentGpxFragment",
               action = "urn:getCurrentGpxFragment")
    @ResponseWrapper(targetNamespace="http://ggps.apps.vavi/types",
                     className="vavi.apps.ggps.GpxFragment")
    @WebResult(targetNamespace="http://ggps.apps.vavi/types",
               name="currentGpxFragment")
    GpxFragment getCurrentGpxFragment();
    /** */
    @WebMethod(operationName = "setCurrentGpxFragment",
               action = "urn:setCurrentGpxFragment")
    @RequestWrapper(targetNamespace="http://ggps.apps.vavi/types",
                    className="vavi.apps.ggps.GpxFragment")
    void setCurrentGpxFragment(@WebParam(targetNamespace="http://ggps.apps.vavi/types",
                                         name="gpxFragment",
                                         mode=Mode.IN) GpxFragment gpx);
}

/* */
