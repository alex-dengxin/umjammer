/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class IPLROM extends MemoryMappedDevice implements X68000Device {
    private X68000 x68000;

    public IPLROM() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        if (x68000.getFile(x68000.getStringParameter("IPLROM", "IPLROM.DAT.gz"), m, 16646144, 131072, 131072) < 0) {
            return false;
        }
        m[16713626] = (byte) 0;
        m[16713627] = (byte) -64;
        m[16713628] = (byte) 0;
        m[16713629] = (byte) 0;
        m[16713706] = (byte) -1;
        return true;
    }

    public void reset() {
    }

    public byte read_byte(int a) {
        x68000.clock_count += x68000.clock_unit;
        return m[a];
    }

    public short read_short_big(int a) {
        x68000.clock_count += x68000.clock_unit;
        return (short) ((m[a] << 8) + (m[a + 1] & 255));
    }

    public int read_int_big(int a) {
        x68000.clock_count += x68000.clock_unit * 2;
        return ((m[a] & 255) << 24) + ((m[a + 1] & 255) << 16) + ((m[a + 2] & 255) << 8) + (m[a + 3] & 255);
    }

    public void write_byte(int a, byte b) {
        x68000.clock_count += x68000.clock_unit;
        m[a] = b;
    }

    public void write_short_big(int a, short s) {
        x68000.clock_count += x68000.clock_unit;
        m[a] = (byte) (s >>> 8);
        m[a + 1] = (byte) s;
    }

    public void write_int_big(int a, int i) {
        x68000.clock_count += x68000.clock_unit * 2;
        m[a] = (byte) (i >>> 24);
        m[a + 1] = (byte) (i >>> 16);
        m[a + 2] = (byte) (i >>> 8);
        m[a + 3] = (byte) i;
    }
}

