/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps;


/**
 * Basic GPS data.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030326 nsano initial version <br>
 */
public class BasicGpsData extends GpsData {

    /** TODO ‘½•ª GpsData ƒNƒ‰ƒX‚ÉˆÚ‚·‚×‚« */
    private byte[] rawData;

    /** */
    public void setRawData(byte[] rawData) {
        this.rawData = new byte[rawData.length];
        System.arraycopy(rawData, 0, this.rawData, 0, rawData.length);
    }

    /** */
    public byte[] getRawData() {
        return rawData;
    }

    //----

    /** */
    public boolean ready() {
        return false;
    }

    /** */
    public int getMaxChannels() {
        return 0;
    }
}

/* */
