/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */


package vavi.microedition.midlet;

import vavi.microedition.io.ConnectionNotFoundException;
import vavi.microedition.lcdui.Display;


/**
 * MIDletState.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040920 nsano initial version <br>
 */
public class MIDletState {

    /** */
    protected MIDlet midlet;

    /**
     * @param midlet
     */
    public MIDletState(MIDlet midlet) {
        this.midlet = midlet;
    }

    /**
     * @return Display
     */
    public Display getDisplay() {
        // TODO Auto-generated method stub
        return Display.getDisplay(midlet);
    }
    

    /** */
    public void notifyDestroyed() {
        midlet.notifyDestroyed();
    }

    /** */
    public void notifyPaused() {
        midlet.notifyPaused();
    }

    /** */
    public MIDletSuite getMIDletSuite() {
        // TODO Auto-generated method stub
        return null;
    }

    /** */
    public void resumeRequest() {
        midlet.resumeRequest();
    }

    /** */
    public boolean platformRequest(String url) throws ConnectionNotFoundException {
        return midlet.platformRequest(url);
    }

    /** */
    public int checkPermission(String permission) {
        return midlet.checkPermission(permission);
    }

    /**
     * @return boolean
     */
    public boolean isDispatchThread() {
        // TODO Auto-generated method stub
        return false;
    }    
}

/* */
