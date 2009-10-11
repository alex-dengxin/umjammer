/*
 * @(#) $Id: DataDisplayAnalyzer.java,v 1.1.1.1 2003/10/05 18:39:22 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import org.jstk.JSTKUtil;


public class DataDisplayAnalyzer implements ProtocolAnalyzer {
    private String label = null;

    public DataDisplayAnalyzer(String label) {
        this.label = label;
    }

    public void analyze(JSTKBuffer buf) {
        System.out.println("[TCP] C " + label + " S (" + buf.getNBytes() + " bytes)");
        System.out.print(JSTKUtil.readableFromBytes(buf.getByteArray(), 0, buf.getNBytes()));
    }
}
