/*
 * @(#) $Id: FileBasedCADatabaseParams.java,v 1.2 2003/10/28 08:15:55 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net).
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the
 * root directory of the containing software.
 */
/* 10/27/03, Pankaj: Added storetype as a parameter. */

package org.jstk.cert.ca;

import java.security.PrivateKey;
import java.security.cert.Certificate;


// A Marker interface
public class FileBasedCADatabaseParams implements CADatabaseParams {
    private String caDirName;

    private boolean createCA;

    private Certificate[] caCerts;

    private PrivateKey caPrivateKey;

    private String password;

    private String storeType;

    public FileBasedCADatabaseParams(String caDirName) {
        this.caDirName = caDirName;
        createCA = false;
    }

    public FileBasedCADatabaseParams(String caDirName, Certificate[] caCerts, PrivateKey caPrivateKey) {
        this.caDirName = caDirName;
        createCA = true;
        this.caCerts = caCerts;
        this.caPrivateKey = caPrivateKey;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getCADirName() {
        return caDirName;
    }

    public boolean getCreateCA() {
        return createCA;
    }

    public Certificate[] getCACerts() {
        return caCerts;
    }

    public PrivateKey getCAPrivateKey() {
        return caPrivateKey;
    }

    public String getPassword() {
        return password;
    }

    public String getStoreType() {
        return storeType;
    }
}
