/*
 * Copyright (c) 2001 Brian Boyter
 * All rights reserved
 *
 * This software is released subject to the GNU Public License.  See
 * the full license included with this distribution.
 */

package com.boyter.mscrypto;

import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.KeyManager;
import java.security.KeyStoreException;
import java.security.KeyStore;


/**
 * MSKeyManagerFactoryImpl.
 * 
 * @author Brian Boyter
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano modified <br>
 */
public final class MSKeyManagerFactoryImpl extends KeyManagerFactorySpi {
    /** */
    private X509KeyManager keyManager;

    /**
     * @param ks use windows key store, set null
     * @param passphrase use windows key, set null
     */
    protected void engineInit(KeyStore ks, char[] passphrase) throws KeyStoreException {
        keyManager = new MSKeyManagerImpl(null, null);
    }

    /** Returns one trust manager for each type of trust material. */
    protected KeyManager[] engineGetKeyManagers() {
        return new KeyManager[] {
            keyManager
        };
    }

    /** Initializes this factory with a source of provider-specific key material. */
    protected void engineInit(ManagerFactoryParameters spec) {
    }
}

/* */
