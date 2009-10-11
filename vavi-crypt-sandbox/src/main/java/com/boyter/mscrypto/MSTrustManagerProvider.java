/*
 * Copyright (c) 2001 Brian Boyter
 * All rights reserved
 *
 * This software is released subject to the GNU Public License.  See
 * the full license included with this distribution.
 */

package com.boyter.mscrypto;

import java.security.Provider;


/**
 * MSTrustMgrProvider.
 * 
 * @author Brian Boyter
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano modified <br>
 */
public final class MSTrustManagerProvider extends Provider {
    /** */
    public MSTrustManagerProvider() {
        super("MSTMF", 1.0, "MSTMF implements MS Trust Factory");
        put("TrustManagerFactory.MSTMF", "com.boyter.mscrypto.MSTrustManagerFactoryImpl");
    }
}

/* */
