/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.microedition.lcdui;



/**
 * DisplayAccess. 
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	040920	nsano	initial version <br>
 */
public interface DisplayAccess {

    /** */
    boolean wantsForeground();

    /** */
    void foregroundNotify(boolean b);

    /** */
    Display getDisplay();

    /** */
    void inputMethodEvent(String str);

    /** */
    void resumePainting();

    /**
     * 
     */
    void suspendPainting();

    /**
     * @param item
     */
    void callItemStateChanged(Item item);

    /**
     * @param item
     */
    void callInvalidate(Item item);

    /**
     * 
     */
    void callSerially();

    /**
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param target
     */
    void repaint(int x1, int y1, int x2, int y2, Object target);

    /**
     * @param id
     */
    void commandAction(int id);

    /**
     * @param type
     * @param x
     * @param y
     */
    void pointerEvent(int type, int x, int y);

    /**
     * @param type
     * @param keyCode
     */
    void keyEvent(int type, int keyCode);

    /** */
    int getKeyMask();

    /**
     * @param displayable
     * @param offscreen_buffer
     * @param x
     * @param y
     * @param width
     * @param height
     */
    void flush(Displayable displayable, Image offscreen_buffer, int x, int y, int width, int height);

}

/* */
