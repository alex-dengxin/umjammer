/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.microedition.lcdui;


/**
 * DisplayManagerFactory.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040920 nsano initial version <br>
 */
class DisplayManagerFactory {

    private static DisplayManager displayManager;

    /** */
    public static void SetDisplayManagerImpl(DisplayManager displayManager) {
        DisplayManagerFactory.displayManager = displayManager;
    }

    /** */
    public static DisplayManager getDisplayManager() {
        return displayManager;
    }
}

/* */
