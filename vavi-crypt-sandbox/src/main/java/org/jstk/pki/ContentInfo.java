/*
 * @(#) $Id: ContentInfo.java,v 1.1.1.1 2003/10/05 18:39:20 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import org.jstk.asn1.ASN1Explicit;
import org.jstk.asn1.ASN1Oid;
import org.jstk.asn1.ASN1Seq;


/*
 * From PKCS#7: ContentInfo ::= SEQUENCE { contentType ContentType, content [0] EXPLICIT ANY DEFINED BY contentType OPTIONAL, }
 */
public class ContentInfo extends ASN1Seq {
    private ASN1Oid contentType = new ASN1Oid();

    private ASN1Explicit content = new ASN1Explicit(CONTEXT, EXPLICIT, 0);

    public ContentInfo() {
        super();
        content.setOptional(true);

        add(contentType);
        add(content);
    }

    public ASN1Oid getContentType() {
        return contentType;
    }

    public ASN1Explicit getContent() {
        return content;
    }

    public String toString() {
        return ("ContentInfo-SEQ(" + contentType.toString() + ", " + content.toString() + ")");
    }
}
