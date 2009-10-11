/*
 * @(#) $Id: Version.java,v 1.1.1.1 2003/10/05 18:39:21 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import java.math.BigInteger;
import java.io.IOException;
import org.jstk.asn1.*;


/*
 * Version ::= INTEGER { v1(0), v2(1), v3(2) }
 */
public class Version extends ASN1Type {
    private ASN1Integer version = new ASN1Integer();

    public Version(byte tagClass, int taggingMethod, int tagNumber) {
        super(tagClass, taggingMethod, tagNumber, 0);
        consMask = CONSTRUCTED;
        version.setDefaultValue(new BigInteger("0"));
    }

    public ASN1Integer getVersion() {
        return version;
    }

    public void decode(ASN1PullParser parser) throws ASN1PullParserException, IOException {
        int event = parser.next();

        if ((event != tagNumber) || (parser.getTagClass() != tagClass)) {
            parser.prev(); // skip
            return;
        }
        version.decode(parser);
    }

    public byte[] encode() {
        logger.entering(getClass().getName(), "encode");
        byte[] bytes = version.encode();
        if (bytes != null) {
            value = bytes;
            length = bytes.length;
            bytes = encode1();
            logger.fine("non-default version encoded");
        } else {
            logger.fine("default version NOT encoded");
        }
        logger.exiting(getClass().getName(), "encode");
        return bytes;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Version-INTEGER(" + version.toString() + ")");
        return sb.toString();
    }
}
