/*
 * @(#) $Id: ASN1PullParserException.java,v 1.1.1.1 2003/10/05 18:39:12 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

public class ASN1PullParserException extends Exception {
    public ASN1PullParserException() {
        super();
    }

    public ASN1PullParserException(String message) {
        super(message);
    }

    public ASN1PullParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ASN1PullParserException(Throwable cause) {
        super(cause);
    }
}
