/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps;

import java.text.DecimalFormat;


/**
 * Latitude, Longitude in DMS.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030318 nsano initial version <br>
 *          0.01 030318 nsano change seconds' resolution <br>
 *          0.02 030320 nsano fix seconds <br>
 */
public class PointSurface {

    /** */
    public static final int NORTH_LATITUDE = 0;
    /** */
    public static final int SOUTH_LATITUDE = 1;
    /** */
    public static final int EAST_LONGITUDE = 2;
    /** */
    public static final int WEST_LONGITUDE = 3;

    /** */
    private static final String typeStrings = "NSEW";

    /** Ž¯•ÊŽq */
    private int type;

    /** */
    public void setType(int type) {
        this.type = type;
    }

    /** */
    public int getType() {
        return type;
    }

    /** “x */
    private int degrees;

    /** */
    public void setDegrees(int degrees) {
        this.degrees = degrees;
    }

    /** */
    public int getDegrees() {
        return degrees;
    }
    /** •ª */
    private int minutes;

    /** */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    /** */
    public int getMinutes() {
        return minutes;
    }

    /** •b */
    private float seconds;

    /** */
    public void setSeconds(float seconds) {
        this.seconds = seconds;
    }

    /** */
    public float getSeconds() {
        return seconds;
    }

    /** */
    public String toString() {

        StringBuilder sb = new StringBuilder();

        final DecimalFormat df2 = new DecimalFormat("00");
        final DecimalFormat df3 = new DecimalFormat("000");
        final DecimalFormat df2_4 = new DecimalFormat("00.00");

        sb.append(typeStrings.charAt(type));
        sb.append(" ");

        if (type == EAST_LONGITUDE || type == WEST_LONGITUDE) {
            sb.append(df3.format(degrees));
        } else {
            sb.append(df2.format(degrees));
        }
        sb.append("ß");

        sb.append(df2.format(minutes));
        sb.append("'");

        sb.append(df2_4.format(seconds));
        sb.append("\"");

        return sb.toString();
    }

    /**
     * DMM
     */
    public String toNmeaString() {

        StringBuilder sb = new StringBuilder();

        final DecimalFormat df2 = new DecimalFormat("00");
        final DecimalFormat df3 = new DecimalFormat("000");
        final DecimalFormat df2_4 = new DecimalFormat("00.0000");

        if (type == EAST_LONGITUDE || type == WEST_LONGITUDE) {
            sb.append(df3.format(degrees));
        } else {
            sb.append(df2.format(degrees));
        }

        sb.append(df2_4.format(minutes + seconds / 60f));
        sb.append(",");
        sb.append(typeStrings.charAt(type));

        return sb.toString();
    }
}

/* */
