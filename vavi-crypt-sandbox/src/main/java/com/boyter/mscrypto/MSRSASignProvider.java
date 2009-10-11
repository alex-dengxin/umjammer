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
 * MSRSASignProvider.
 * 
 * @author Brian Boyter
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050604 nsano modified <br>
 */
public final class MSRSASignProvider extends Provider {

    /** */
    public MSRSASignProvider() {
        super("MicrosoftRSASign", 1.0, "MSRSASignProvider implements Microsoft RSA Signature");
        put("Signature.MD5withRSA", "com.boyter.mscrypto.MSMD5RSASignature");
        put("Signature.SHA1withRSA", "com.boyter.mscrypto.MSSHARSASignature");
    }
}

/* */
