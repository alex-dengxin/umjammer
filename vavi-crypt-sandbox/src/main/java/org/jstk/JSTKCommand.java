/*
 * @(#) $Id: JSTKCommand.java,v 1.1.1.1 2003/10/05 18:39:10 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk;

public interface JSTKCommand {
    public Object execute(JSTKArgs args) throws JSTKException;

    public String briefDescription();

    public String optionsDescription();

    public String[] sampleUses();

    public String[] useForms();

    public String getResultDescription();

    public boolean succeeded();

    public boolean failed();

    public void setPerfData(JSTKPerfData pData);

    public JSTKPerfData getPerfData();
}
