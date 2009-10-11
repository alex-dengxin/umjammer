/*
 * @(#) $Id: RevokedCerts.java,v 1.1.1.1 2003/10/05 18:39:15 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert.ca;

import java.security.cert.Certificate;
import java.util.Iterator;


public interface RevokedCerts {
    public void add(Certificate cert) throws CADatabaseException;

    public boolean exists(Certificate cert) throws CADatabaseException;

    public Iterator<RevokedCert> iterator();
}
