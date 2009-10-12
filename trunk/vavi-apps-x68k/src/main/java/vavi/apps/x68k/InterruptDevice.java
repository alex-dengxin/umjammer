/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


interface InterruptDevice {

    int acknowledge();

    void done(int vector);
}

/* */
