/*
 * @(#) $Id: JSTKCommandAdapter.java,v 1.1.1.1 2003/10/05 18:39:10 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk;

public abstract class JSTKCommandAdapter implements JSTKCommand {

    protected String briefDesc = "Unspecified";

    protected String optionsDesc = "Unspecified";

    protected String[] sampleUses = {
        "Unspecified"
    };

    protected String[] useForms = {
        "Unspecified"
    };

    protected JSTKResult result;

    protected JSTKPerfData perfData = new JSTKPerfData();

    protected String resultDesc = "Failed";

    protected boolean success = false;

    public Object execute(JSTKArgs args) throws JSTKException {
        return null;
    }

    public String briefDescription() {
        return briefDesc;
    }

    public String optionsDescription() {
        return optionsDesc;
    }

    public String[] sampleUses() {
        return sampleUses;
    }

    public String[] useForms() {
        return useForms;
    }

    public String getResultDescription() {
        return resultDesc;
    }

    public boolean succeeded() {
        return success;
    }

    public boolean failed() {
        return !success;
    }

    public void setPerfData(JSTKPerfData pData) {
        perfData = pData;
    }

    public JSTKPerfData getPerfData() {
        return perfData;
    }
}
