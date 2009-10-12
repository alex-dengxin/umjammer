/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class SRAM extends MemoryMappedDevice implements X68000Device {
    final private static int data[] = {
        130, 119, 54, 56, 48, 48, 48, 87, 0, 192, 0, 0, 0, 191, 255, 252, 0, 237, 1, 0, 255, 255, 255, 255, 0, 0, 78, 7, 0, 16, 0, 0, 0, 0, 255, 255, 0, 0, 7, 0, 14, 0, 13, 0, 0, 0, 0, 0, 248, 62, 255, 192, 255, 254, 222, 108, 64, 34, 3, 2, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 220, 0, 4, 0, 1, 1, 0, 0, 0, 32, 0, 9, 249, 1, 255, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 88, 6, 6, 96, 2, 254, 32, 0, 0, 35, 0, 216, 0
    };

    private X68000 x68000;

    public SRAM() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        for (int i = 0; i < data.length; i++) {
            m[15532032 + i] = (byte) data[i];
        }
        String boot = x68000.getStringParameter("BOOT", null);
        if (boot != null) {
            if (boot.compareTo("HD0") == 0) {
                m[15532056] = (byte) -128;
                m[15532057] = 0;
            }
        }
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
        if (a == 15532075) {
            x68000.keyboard.setKana(b);
        } else if (a == 15532121) {
            x68000.keyboard.setXchg(b);
        }
    }

    public void write_short_big(int a, short s) {
        x68000.clock_count += x68000.clock_unit;
        m[a] = (byte) (s >>> 8);
        m[a + 1] = (byte) s;
        if (a >= 15532074 && a <= 15532075) {
            x68000.keyboard.setKana(m[15532075]);
        } else if (a >= 15532120 && a <= 15532121) {
            x68000.keyboard.setXchg(m[15532121]);
        }
    }

    public void write_int_big(int a, int i) {
        x68000.clock_count += x68000.clock_unit * 2;
        m[a] = (byte) (i >>> 24);
        m[a + 1] = (byte) (i >>> 16);
        m[a + 2] = (byte) (i >>> 8);
        m[a + 3] = (byte) i;
        if (a >= 15532072 && a <= 15532075) {
            x68000.keyboard.setKana(m[15532075]);
        } else if (a >= 15532118 && a <= 15532121) {
            x68000.keyboard.setXchg(m[15532121]);
        }
    }
}

