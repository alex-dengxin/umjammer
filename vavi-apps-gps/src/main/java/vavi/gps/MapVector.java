/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps;

/**
 * MapVector.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030318 nsano initial version <br>
 */
public class MapVector {

    /** ‘¬“x km/hour */
    private int velocity;

    /** */
    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    /** */
    public int getVelocity() {
        return velocity;
    }

    /** •ûˆÊ (True North) in degrees (–k‚©‚çŽžŒv‰ñ‚è) */
    private int bearingDirection;

    /** */
    public void setBearingDirection(int bearingDirection) {
        this.bearingDirection = bearingDirection;
    }

    /** */
    public int getBearingDirection() {
        return bearingDirection;
    }

    /** */
    public String toString() {
        return velocity + " km/h, " +
            bearingDirection + "ß";
    }
}

/* */
