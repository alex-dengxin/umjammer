/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import vavi.util.Debug;
import vavix.util.ComUtil;


/**
 * Synchronizer for Windows Media Player.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030303 nsano initial version <br>
 */
public class WMPSynchronizer implements Synchronizer {

    /** */
    public void synchronize() {
    }

    /** */
    public static void main(String[] args) {
        new WMPSynchronizer();
    }

    /** */
    public WMPSynchronizer() {
        ComThread.InitSTA();

        // manager
        ActiveXComponent activex = new ActiveXComponent("Shell.Application");
        Dispatch apps = activex.getObject();
Debug.println("apps: " + apps);

        Variant result = Dispatch.invoke(apps, "windows", Dispatch.Method, new Object[] {}, new int[1]);
Debug.println("windows: " + ComUtil.toObject(result));

        ComThread.Release();
    }
}

/* */
