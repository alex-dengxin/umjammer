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
 * MSKeyManagerProvider.
 * 
 * @author Brian Boyter
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano modified <br>
 */
public final class MSKeyManagerProvider extends Provider {

    /** */
    public MSKeyManagerProvider() {
        super("MSKMF", 1.0, "MSKMF implements MS Key Factory");
        put("KeyManagerFactory.MSKMF", "com.boyter.mscrypto.MSKeyManagerFactoryImpl");
    }
}

/* */
