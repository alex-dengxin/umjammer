/*
 * @(#) $Id: HttpAnalyzer.java,v 1.1.1.1 2003/10/05 18:39:22 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class HttpAnalyzer implements ProtocolAnalyzer {
    private String label = null;

    public HttpAnalyzer(String label) {
        this.label = label;
    }

    public void analyze(JSTKBuffer buf) {
        System.out.println("[HTTP] C " + label + " S (" + buf.getNBytes() + " bytes)");
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(buf.getByteArray(), 0, buf.getNBytes());
            BufferedReader br = new BufferedReader(new InputStreamReader(bais));

            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        } catch (IOException ioe) {
            // Do nothing.
        }
        // System.out.print(JSTKUtil.readableFromBytes(buf.getByteArray(), 0, buf.getNBytes()));
    }
}
