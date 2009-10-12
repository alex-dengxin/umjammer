/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class MainMemory extends MemoryMappedDevice implements X68000Device {
    private X68000 x68000;

    public MainMemory() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        return true;
    }

    public void reset() {
    }
}

