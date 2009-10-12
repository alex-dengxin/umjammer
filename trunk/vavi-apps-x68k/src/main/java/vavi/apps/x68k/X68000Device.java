/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


interface X68000Device {

    boolean init(X68000 x68000);

    void reset();
}

/* */
