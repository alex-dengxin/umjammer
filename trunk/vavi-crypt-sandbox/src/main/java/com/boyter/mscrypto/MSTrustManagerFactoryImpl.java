/*
 * Copyright (c) 2001 Brian Boyter
 * All rights reserved
 *
 * This software is released subject to the GNU Public License.  See
 * the full license included with this distribution.
 */

package com.boyter.mscrypto;

import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.security.KeyStoreException;
import java.security.KeyStore;


/**
 * MSTrustManagerFactoryImpl.
 * 
 * @author Brian Boyter
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano modified <br>
 */
public final class MSTrustManagerFactoryImpl extends TrustManagerFactorySpi {
    /** */
    private X509TrustManager trustManager;

    /**
     * @param ks this method use windows key store, so set null 
     */
    protected void engineInit(KeyStore ks) throws KeyStoreException {
        trustManager = new MSTrustManagerImpl(null);
    }

    /**
     * Returns one trust manager for each type of trust material.
     */
    protected TrustManager[] engineGetTrustManagers() {
        return new TrustManager[] {
            trustManager
        };
    }

    /**
     * Initializes this factory with a source of provider-specific key material.
     */
    protected void engineInit(ManagerFactoryParameters spec) {
    }
}

/* */
