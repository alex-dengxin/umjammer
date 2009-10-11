/*
 * @(#) $Id: ProtocolAnalyzerFactory.java,v 1.1.1.1 2003/10/05 18:39:22 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

public class ProtocolAnalyzerFactory {
    public static ProtocolAnalyzer getInstance(String patype, String label) {
        if (patype.equalsIgnoreCase("dd"))
            return new DataDisplayAnalyzer(label);
        if (patype.equalsIgnoreCase("http"))
            return new HttpAnalyzer(label);
        else if (patype.equalsIgnoreCase("ssl"))
            return new SSLAnalyzer(label);
        return null;
    }
}
