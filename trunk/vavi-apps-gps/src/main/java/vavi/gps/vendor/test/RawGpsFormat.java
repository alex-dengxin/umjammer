/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.test;

import vavi.gps.BasicGpsData;
import vavi.gps.GpsData;
import vavi.gps.GpsFormat;
import vavi.util.Debug;


/**
 * RAW GPS Format.
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	030326	nsano	initial version <br>
 */
public class RawGpsFormat implements GpsFormat {

    /**
     * GpsData ‚É setRawData ‚µ‚©Ý’è‚µ‚Ä‚¢‚È‚¢‚Ì‚Å‚È‚é‚×‚­ Raw GpsDevce ‚ð
     * inputDevice ‚Æ‚µ‚ÄÝ’è‚µ‚È‚¢‚Å‚­‚¾‚³‚¢B
     */
    public GpsData parse(byte[] line) {
        BasicGpsData data = new BasicGpsData();
        data.setRawData(line);
        return data;
    }

    /** */
    public byte[] format(GpsData data) {
        byte[] line = ((BasicGpsData) data).getRawData();

        // HGR3 -> IPS-5000
        if (line.length == 148 &&
            line[0] == 'S' &&
            line[1] == 'M' &&
            line[2] == '0' &&
            line[3] == '0') {
            byte[] ips = new byte[108];
            System.arraycopy("SONY80".getBytes(), 0, ips, 0, 6);
            System.arraycopy(line, 6, ips, 6, 97);
            System.arraycopy(line, 143, ips, 103, 5);

            return ips;
        }
        else {
Debug.println("line: " + new String(line));
	    throw new IllegalArgumentException(new String(line));
        }
    }
}

/* */
