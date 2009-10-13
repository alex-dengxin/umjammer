/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.ggps.services.impl;

import vavi.apps.ggps.models.GpxFragment;
import vavi.apps.ggps.services.GgpsService;
import vavi.persistence.Dao;


/**
 * GgpsServiceImpl. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070304 nsano initial version <br>
 */
public class GgpsServiceImpl implements GgpsService {

    private Dao<GpxFragment, Long> ggpsDao;

    /** */
    public void setGgpsDao(Dao<GpxFragment, Long> ggpsDao) {
        this.ggpsDao = ggpsDao;
    }

    /** */
    public GpxFragment getCurrentGpxFragment() {
        return null;
    }

    /** */
    public void setCurrentGpxFragment(GpxFragment gpx) {
        ggpsDao.persist(gpx);
    }
}

/* */
