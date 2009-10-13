/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.ggps.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


/**
 * GpxFragment. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070304 nsano initial version <br>
 */
@Entity
public class GpxFragment implements vavi.persistence.Entity<Long> {

    /** */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /* */
    public Long getId() {
        return id;
    }

    /* */
    public void setId(Long id) {
        this.id = id;
    }

}

/* */
