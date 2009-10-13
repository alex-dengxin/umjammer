/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.ggps.services;

import vavi.apps.ggps.models.GpxFragment;


/**
 * GgpsService. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070304 nsano initial version <br>
 */
public interface GgpsService {

    /** */
    public GpxFragment getCurrentGpxFragment();

    /** */
    public void setCurrentGpxFragment(GpxFragment gpx);
}

/* */
