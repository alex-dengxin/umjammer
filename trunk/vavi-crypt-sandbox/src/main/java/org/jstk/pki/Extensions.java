/*
 * @(#) $Id: Extensions.java,v 1.1.1.1 2003/10/05 18:39:21 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import java.io.IOException;

import org.jstk.asn1.ASN1OctetString;
import org.jstk.asn1.ASN1Oid;
import org.jstk.asn1.ASN1PullParser;
import org.jstk.asn1.ASN1PullParserException;
import org.jstk.asn1.ASN1Seq;
import org.jstk.asn1.ASN1Type;


/*
 * Extensions := SEQUENCE SIZE (1..MAX) OF Extension
 */
public class Extensions extends ASN1Type {
    private ASN1Seq extensions = new ASN1Seq();

    static class Extension extends ASN1Seq {
        ASN1Oid extnID = new ASN1Oid();

        // ASN1Boolean critical = new ASN1Boolean();
        ASN1OctetString extnValue = new ASN1OctetString();

        Extension() {
            super();
            add(extnID);
            // add(critical);
            add(extnValue);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Extension-SEQ(" + extnID.toString() + ", " + extnValue.toString() + ")");
            return sb.toString();
        }
    }

    public Extensions(byte tagClass, int taggingMethod, int tagNumber) {
        super(tagClass, taggingMethod, tagNumber, 0);
        consMask = CONSTRUCTED;
    }

    public void decode(ASN1PullParser parser) throws ASN1PullParserException, IOException {
        int event = parser.next();

        if ((event != tagNumber) || (parser.getTagClass() != tagClass)) {
            parser.prev(); // skip
            return;
        }
        if (parser.next() != ASN1PullParser.START_SEQ)
            throw new ASN1PullParserException("unexpected type");
        length = parser.getLength();
        while ((event = parser.next()) != ASN1PullParser.END_SEQ) {
            parser.prev();
            Extension extension = new Extension();
            extension.decode(parser);
            extensions.add(extension);
        }
    }

    public byte[] encode() {
        logger.entering(getClass().getName(), "encode");
        byte[] bytes = null;
        if (extensions.size() > 0) {
            bytes = extensions.encode();
            value = bytes;
            length = bytes.length;
            bytes = encode1();
            logger.fine("extensions encoded");
        } else {
            logger.fine("extensions NOT encoded");
        }
        logger.exiting(getClass().getName(), "encode");
        return bytes;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Extensions-SEQ(");
        for (int i = 0; i < extensions.size(); i++) {
            ASN1Type elem = extensions.get(i);
            if (i > 0)
                sb.append(", ");
            sb.append(elem.toString());
        }
        sb.append(")");
        return sb.toString();
    }
}
