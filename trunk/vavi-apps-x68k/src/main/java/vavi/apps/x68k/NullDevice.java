/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class NullDevice extends MemoryMappedDevice implements X68000Device {
    private X68000 x68000;

    public NullDevice() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        return true;
    }

    public void reset() {
    }

    public byte read_byte(int a) throws MC68000Exception {
        x68000.bus_error_on_read(a);
        return 0;
    }

    public short read_short_big(int a) throws MC68000Exception {
        x68000.bus_error_on_read(a);
        return 0;
    }

    public int read_int_big(int a) throws MC68000Exception {
        x68000.bus_error_on_read(a);
        return 0;
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        x68000.bus_error_on_write(a);
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        x68000.bus_error_on_write(a);
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        x68000.bus_error_on_write(a);
    }
}

