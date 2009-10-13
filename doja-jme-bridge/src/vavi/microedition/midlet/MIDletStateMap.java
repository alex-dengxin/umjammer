/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.microedition.midlet;

/**
 * MIDletStateMap. 
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	040920	nsano	initial version <br>
 */
public class MIDletStateMap {

    /**
     * @param midlet
     * @return MIDletState
     */
    public static MIDletState getState(MIDlet midlet) {
        return mapImpl.getStateImpl(midlet);
    }

    /** */
    private static MIDletStateMapImpl mapImpl;

    /**
     * @param mapImpl
     */
    public static void setMapImpl(MIDletStateMapImpl mapImpl) {
        MIDletStateMap.mapImpl = mapImpl;
    }
}

/* */
