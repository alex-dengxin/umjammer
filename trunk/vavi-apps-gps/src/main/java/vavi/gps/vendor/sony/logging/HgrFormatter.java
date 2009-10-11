/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.sony.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;


/**
 * The formatter for HGR.
 *
 * @todo	use GPSml, limit logging data
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	030324	nsano	initial version <br>
 */
public class HgrFormatter extends Formatter {

    /** */
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getMessage());
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }
}

/* */
