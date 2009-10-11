/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.gps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import vavi.util.Debug;


/**
 * HGR utility.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030327 nsano initial version <br>
 */
public class HgrUtil {

    /**
     * power on
     * power off
     * gps off
     * gps on
     * id
     * change 
     * set interval standalone
     * set interval pclinked
     * show memory usage
     */
    public HgrUtil() throws IOException {

        final HgrInputStream is = new HgrInputStream();
        HgrOutputStream os = new HgrOutputStream();

        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        String line = is.readLine();
                        System.out.println(line);
                    }
                    catch (Exception e) {
Debug.println(e);
                    }
                }
            }
        });
        thread.start();
        BufferedReader r =
            new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.err.print("> ");
            String line = r.readLine();
            if (line != null) {
                os.writeLine(line);
            }
        }
    }

    /** */
    public static void main(String[] args) throws Exception {
        new HgrUtil();
    }
}

/* */
