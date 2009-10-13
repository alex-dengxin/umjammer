/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.microedition.lcdui;


/**
 * DisplayManager.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040920 nsano initial version <br>
 */
interface DisplayManager {

    /**
     * 
     */
    void suspendPainting();

    /**
     * 
     */
    void resumePainting();

    /**
     * @param str
     */
    void inputMethodEvent(String str);

    /**
     * 
     */
    void killCurrent();

    /**
     * @param type
     * @param code
     */
    void keyEvent(int type, int code);

    /**
     * @param type
     * @param x
     * @param y
     */
    void pointerEvent(int type, int x, int y);

    /**
     * @param type
     */
    void commandAction(int type);

    /**
     * 
     */
    void suspendAll();

    /**
     * 
     */
    void resumeAll();

    /**
     * 
     */
    void shutdown();

    /**
     * 
     */
    void suspendCurrent();

    /**
     * 
     */
    void resumePrevious();

    /**
     * @param parent
     * @param d
     */
    void screenChange(Display parent, Displayable d);

    /**
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param target
     */
    void repaint(int x1, int y1, int x2, int y2, Object target);

    /**
     * 
     */
    void callSerially();

    /**
     * @param src
     */
    void callInvalidate(Item src);

    /**
     * @param src
     */
    void callItemStateChanged(Item src);

}

/* */
