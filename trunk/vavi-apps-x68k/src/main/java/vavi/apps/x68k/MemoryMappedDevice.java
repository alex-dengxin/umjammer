/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class MemoryMappedDevice {
    protected static byte m[];
    static {
        m = new byte[16777216];
        for (int i = 0; i < 16777216; i += 8) {
            m[i] = 0;
            m[i + 1] = 0;
            m[i + 2] = 0;
            m[i + 3] = 0;
            m[i + 4] = 0;
            m[i + 5] = 0;
            m[i + 6] = 0;
            m[i + 7] = 0;
        }
    }

    public byte read_byte(int a) throws MC68000Exception {
        return m[a];
    }

    public short read_short_big(int a) throws MC68000Exception {
        return (short) ((m[a] << 8) + (m[a + 1] & 255));
    }

    public int read_int_big(int a) throws MC68000Exception {
        return (m[a] << 24) + ((m[a + 1] & 255) << 16) + ((m[a + 2] & 255) << 8) + (m[a + 3] & 255);
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        m[a] = b;
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        m[a] = (byte) (s >> 8);
        m[a + 1] = (byte) s;
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        m[a] = (byte) (i >> 24);
        m[a + 1] = (byte) (i >> 16);
        m[a + 2] = (byte) (i >> 8);
        m[a + 3] = (byte) i;
    }
}

