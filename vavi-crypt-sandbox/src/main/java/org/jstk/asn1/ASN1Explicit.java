/*
 * @(#) $Id: ASN1Explicit.java,v 1.1.1.1 2003/10/05 18:39:11 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

import java.io.IOException;


public class ASN1Explicit extends ASN1Type {
    private ASN1Type instance = null;

    public ASN1Explicit() {
        super(UNIVERSAL, NONE, ANY, ANY);
    }

    public ASN1Explicit(byte tagClass, int taggingMethod, int tagNumber) {
        super(tagClass, taggingMethod, tagNumber, ANY);
        consMask = CONSTRUCTED;
    }

    public void decode(ASN1PullParser parser) throws ASN1PullParserException, IOException {
        int event = parser.next();

        if ((event != tagNumber) || (parser.getTagClass() != tagClass)) {
            parser.prev(); // skip
            return;
        }
        length = parser.getLength();
        if (length > 0) {
            instance = new ASN1Any();
            instance.decode(parser);
        }
    }

    public byte[] encode() {
        logger.entering(getClass().getName(), "encode");
        if (instance == null)
            setValue(null);
        else
            setValue(instance.encode());
        byte[] bytes = encode1();
        logger.exiting(getClass().getName(), "encode");
        return bytes;
    }

    public void setInstance(ASN1Type instance) {
        this.instance = instance;
    }

    public ASN1Type getInstance() {
        return instance;
    }
}
