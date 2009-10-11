/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.sony;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import vavi.gps.BasicGpsData;
import vavi.gps.GpsDevice;
import vavi.gps.GpsData;
import vavi.util.Debug;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;


/**
 * HGR Logger device.
 * 
 * @todo 汎用化、HGR から独立、Formatter で差を吸収
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030327 nsano initial version <br>
 */
public class HgrLogger extends GpsDevice {

    /** ログを取るかどうか */
    private boolean logging = false;

    /** 何回に一回ログを取るか */
    private int interval = 10;

    /** ロガー */
    private Logger logger;

    /** TODO name を何に使うか？ Formatter クラス？ */
    public HgrLogger(String name) {

        try {
            Properties props = new Properties();

            props.load(Hgr.class.getResourceAsStream("Hgr.properties"));

            String value = props.getProperty("hgr.logging");
            if (value != null) {
Debug.println("logging: " + value);
                logging = new Boolean(value).booleanValue();
            }

            if (logging) {
                value = props.getProperty("hgr.logging.formatter");
                if (value != null) {
Debug.println("configClass: " + value);
		    Handler h = new FileHandler("%h/.hgr.log", true);
                    Formatter f =
                        (Formatter) Class.forName(value).newInstance();

                    h.setFormatter(f);

                    logger = Logger.getLogger(Hgr.class.getName());
                    logger.setUseParentHandlers(false);
                    logger.addHandler(h);
                }

                value = props.getProperty("hgr.logging.interval");
                if (value != null) {
Debug.println("interval: " + value);
		    interval = Integer.parseInt(value);
                }
            }
        } catch (Exception e) {
Debug.printStackTrace(e);
            throw new InternalError(e.toString());
        }
    }

    /** Does nothing. */
    public void start() {
Debug.println("here");
    }

    /** 現在のカウンタ */
    private int logCount = 0;

    /** @throws IllegalStateException always be thrown */
    protected Runnable getInputThread() {
        throw new IllegalStateException("This class cannot be input device.");
    }

    /** */
    protected GenericListener getOutputGenericListener() {

        return new GenericListener() {
            public void eventHappened(GenericEvent ev) {
                try {
                    GpsData gpsData = (GpsData) ev.getArguments()[0];
//System.err.println((logCount + 1) + "/" + interval);
                    if (logging && (logCount == (interval - 1))) {
                        if (gpsData.ready()) {
                            byte[] line = ((BasicGpsData) gpsData).getRawData();
                            logger.log(Level.INFO, new String(line));
                        }
                        logCount = 0;
                    } else {
                        logCount++;
                    }
                } catch (Exception e) {
Debug.printStackTrace(e);
                }
            }
        };
    }
}

/* */
