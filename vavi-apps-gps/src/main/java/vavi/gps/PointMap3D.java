/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps;


/**
 * Point on a map.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030318 nsano initial version <br>
 */
public class PointMap3D {

    /** 緯度 */
    private PointSurface latitude;

    /** */
    public void setLatitude(PointSurface latitude) {
        this.latitude = latitude;
    }

    /** */
    public PointSurface getLatitude() {
        return latitude;
    }

    /** 経度 */
    private PointSurface longitude;

    /** */
    public void setLongitude(PointSurface longitude) {
        this.longitude = longitude;
    }

    /** */
    public PointSurface getLongitude() {
        return longitude;
    }

    /** 高度 (against earth ellipsoid) in meters */
    private int altitude;

    /** */
    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    /** */
    public int getAltitude() {
        return altitude;
    }

    /** */
    public String toString() {
        return "Lat " + latitude +
            ", Lon " + longitude + ", " +
            "Alt " + altitude + "m";
    }
}

/* */

