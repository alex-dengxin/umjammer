/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class AreaSet extends MemoryMappedDevice implements X68000Device {

    private X68000 x68000;

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        return true;
    }

    public void reset() {
        x68000.userProtect(0, 8192, true);
        x68000.userProtect(8192, 2088960, false);
    }

    public byte read_byte(int a) throws MC68000Exception {
        x68000.bus_error_on_read(a);
        return (byte) -1;
    }

    public short read_short_big(int a) throws MC68000Exception {
        x68000.bus_error_on_read(a);
        return (short) -1;
    }

    public int read_int_big(int a) throws MC68000Exception {
        x68000.bus_error_on_read(a);
        return -1;
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        if (a == 15228929) {
            int length = 1 + (b & 255) << 13;
            x68000.userProtect(0, length, true);
            x68000.userProtect(length, 2097152 - length, false);
        } else {
            x68000.bus_error_on_write(a);
        }
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        x68000.bus_error_on_write(a);
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        x68000.bus_error_on_write(a);
    }
}

/* */
