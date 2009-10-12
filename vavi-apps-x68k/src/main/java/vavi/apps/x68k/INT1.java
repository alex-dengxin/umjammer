/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class INT1 extends MemoryMappedDevice implements X68000Device, InterruptDevice {
    private X68000 x68000;

    public INT1() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        return true;
    }

    public void reset() {
    }

    public int acknowledge() {
        return 96;
    }

    public void done(int vector) {
    }
}

