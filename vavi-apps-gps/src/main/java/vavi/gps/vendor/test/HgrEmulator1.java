/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TimerTask;

import vavi.util.Debug;


/**
 * HGR のエミュレーションを行うクラスです。
 * 
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	030331	nsano	initial version <br>
 */
public class HgrEmulator1 extends HgrEmulator {

    /** シリアル回線への GPS データの出力 */
    protected TimerTask getOutputTimerTask() {
        return new TimerTask() {
            public void run() {
                try {
                    os.writeLine(dummyString);
                } catch (IOException e) {
Debug.println(e);
                }
            }
        };
    }

    //----

    /** */
    private String ioDeviceName = "dummy1";

    /** */
    protected String getIODeviceName() {
        return ioDeviceName;
    }

    /** */
    private String dummyString;

    /** */
    private static String file = "tmp/ips.txt";

    /** */
    public HgrEmulator1() {
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));

            dummyString = r.readLine();
        } catch (Exception e) {
Debug.println(e);
        }
    }
}

/* */
