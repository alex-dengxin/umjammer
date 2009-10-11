/*
 * @(#) $Id: CADatabaseSpi.java,v 1.1.1.1 2003/10/05 18:39:15 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert.ca;

import java.security.cert.Certificate;
import java.security.cert.CertPath;
import java.security.PrivateKey;
import java.math.BigInteger;


public interface CADatabaseSpi {
    public IssuedCerts getIssuedCerts();

    public RevokedCerts getRevokedCerts();

    public Certificate getCACert();

    public CertPath getCACertPath();

    public PrivateKey getCAPrivateKey();

    public BigInteger nextSerialNumber() throws CADatabaseException;
}
