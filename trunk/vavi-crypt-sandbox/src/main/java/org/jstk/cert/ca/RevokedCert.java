/*
 * @(#) $Id: RevokedCert.java,v 1.1.1.1 2003/10/05 18:39:15 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert.ca;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RevokedCert {
    private Date revocationDate;

    private BigInteger serialNumber;

    public RevokedCert(String date, String serialNo) {
        DateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
        try {
            revocationDate = sdf.parse(date);
            serialNumber = new BigInteger(serialNo);
        } catch (Exception exc) {
            System.err.println("RevokedCert::Unexpected exception: " + exc);
        }
    }

    public Date getRevocationDate() {
        return revocationDate;
    }

    public BigInteger getSerialNumber() {
        return serialNumber;
    }
}
