/*
 * @(#) $Id: JSTKArgs.java,v 1.1.1.1 2003/10/05 18:39:10 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk;

import java.util.Map;


/**
 * JSTKArgs.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050318 nsano initial version <br>
 */
public interface JSTKArgs {
    /** */
    public void setDefaults(Map<String, String> defaults);

    /** */
    public String get(String name);

    /** */
    public void set(String name, String value);

    /** */
    public int getNum();

    /** */
    public String get(int position);
}

/* */
