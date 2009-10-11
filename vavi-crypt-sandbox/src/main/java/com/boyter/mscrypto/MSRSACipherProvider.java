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
 * MSRSACipherProvider.
 * 
 * @author Brian Boyter
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano modified <br>
 */
public final class MSRSACipherProvider extends Provider {

    /** */
    public MSRSACipherProvider() {
        super("MSRSACipher", 1.0, "MSRSACipherProvider implements Microsoft RSA Decryption");
        put("Cipher.RSA/ECB/PKCS1Block02Pad", "com.boyter.mscrypto.MSRSACipherFactoryImpl");
        put("Cipher.RSA/ECB/PKCS1Padding", "com.boyter.mscrypto.MSRSACipherFactoryImpl");
        put("Cipher.RSA/ECB/PKCS1", "com.boyter.mscrypto.MSRSACipherFactoryImpl");
    }
}

/* */
