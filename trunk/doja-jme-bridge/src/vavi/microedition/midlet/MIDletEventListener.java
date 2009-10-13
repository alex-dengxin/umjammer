/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.microedition.midlet;


/**
 * MIDletEventListener. 
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	040920	nsano	initial version <br>
 */
public interface MIDletEventListener {

    /**
     * @param midlet
     */
    public void destroyMIDlet(MIDlet midlet);

    /**
     * @param midlet
     */
    public void startMIDlet(MIDlet midlet);

    /**
     * @param midlet
     */
    public void pauseMIDlet(MIDlet midlet);
}

/* */
