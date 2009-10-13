/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.ggps;

import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import vavi.apps.ggps.jaxb.GpxType;


/**
 * Test. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070326 nsano initial version <br>
 */
public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String file = args[0];

        JAXBContext jc = JAXBContext.newInstance("vavi.apps.ggps.jaxb");

        Unmarshaller u = jc.createUnmarshaller();
        GpxType gpxType = (GpxType) u.unmarshal(new FileInputStream(file));


        Marshaller m = jc.createMarshaller();
        m.marshal(gpxType, System.out);
    }
}

/* */
