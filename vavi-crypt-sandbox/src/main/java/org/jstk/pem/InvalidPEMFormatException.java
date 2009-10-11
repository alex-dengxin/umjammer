/*
 * @(#) $Id: InvalidPEMFormatException.java,v 1.1.1.1 2003/10/05 18:39:20 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pem;

public class InvalidPEMFormatException extends Exception {
    public InvalidPEMFormatException() {
        super("reason not specified");
    }

    public InvalidPEMFormatException(String msg) {
        super(msg);
    }
}
