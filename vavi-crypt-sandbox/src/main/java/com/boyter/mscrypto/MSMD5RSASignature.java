/*
 * Copyright (c) 2001 Brian Boyter
 * All rights reserved
 *
 * This software is released subject to the GNU Public License.  See
 * the full license included with this distribution.
 */

package com.boyter.mscrypto;

import java.security.NoSuchAlgorithmException;

import com.boyter.mscrypto.MSRSASignFactoryImpl;


/**
 * MSMD5RSASignature.
 * 
 * @author Brian Boyter
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050314 nsano modified <br>
 */
public final class MSMD5RSASignature extends MSRSASignFactoryImpl {

    /**
     * @throws NoSuchAlgorithmException
     */
    public MSMD5RSASignature() throws NoSuchAlgorithmException {
        super.setMessageDigestType("MD5");
    }
}

/* */
