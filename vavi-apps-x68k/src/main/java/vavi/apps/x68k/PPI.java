/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class PPI extends MemoryMappedDevice implements X68000Device {
    private int port_c_data;
    private boolean j1_forward, j2_forward;
    private boolean j1_back, j2_back;
    private boolean j1_left, j2_left;
    private boolean j1_right, j2_right;
    private boolean j1_triger_a, j2_triger_a;
    private boolean j1_triger_b, j2_triger_b;

    private X68000 x68000;

    private Sound sound;

    public PPI() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        this.sound = x68000.sound;
        reset();
        return true;
    }

    public void reset() {
        port_c_data = 0;
        j1_forward = false;
        j2_forward = false;
        j1_back = false;
        j2_back = false;
        j1_left = false;
        j2_left = false;
        j1_right = false;
        j2_right = false;
        j1_triger_a = false;
        j2_triger_a = false;
        j1_triger_b = false;
        j2_triger_b = false;
    }

    public byte read_byte(int a) throws MC68000Exception {
        switch (a) {
        case 15310849:
            return (byte) ~((j1_forward && !j1_back ? 1 : 0) + (j1_back && !j1_forward ? 2 : 0) + (j1_left && !j1_right ? 4 : 0) + (j1_right && !j1_left ? 8 : 0) + (j1_triger_a ? 32 : 0) + (j1_triger_b ? 64 : 0));
        case 15310851:
            return (byte) ~((j2_forward && !j2_back ? 1 : 0) + (j2_back && !j2_forward ? 2 : 0) + (j2_left && !j2_right ? 4 : 0) + (j2_right && !j2_left ? 8 : 0) + (j2_triger_a ? 32 : 0) + (j2_triger_b ? 64 : 0));
        case 15310853:
            return (byte) port_c_data;
        case 15310855:
            return 0;
        }
        return -1;
    }

    public short read_short_big(int a) throws MC68000Exception {
        return (short) ((read_byte(a) << 8) + (read_byte(a + 1) & 255));
    }

    public int read_int_big(int a) throws MC68000Exception {
        return (read_short_big(a) << 16) + (read_short_big(a + 2) & 65535);
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        switch (a) {
        case 15310849:
            return;
        case 15310851:
            return;
        case 15310853:
            port_c_data = b & 255;
            sound.set_pan(port_c_data);
            sound.set_divide(port_c_data >> 2);
            return;
        case 15310855:
            if (b >= 0) {
                int n = b >> 1 & 7;
                port_c_data = (port_c_data & ~(1 << n)) + ((b & 1) << n);
                if (n < 2) {
                    sound.set_pan(port_c_data);
                } else if (n == 3) {
                    sound.set_divide(port_c_data >> 2);
                }
            }
            return;
        }
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        write_byte(a, (byte) (s >> 8));
        write_byte(a + 1, (byte) s);
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        write_short_big(a, (short) (i >> 16));
        write_short_big(a + 2, (short) i);
    }

    public void forward(boolean bool) {
        j1_forward = bool;
    }

    public void back(boolean bool) {
        j1_back = bool;
    }

    public void left(boolean bool) {
        j1_left = bool;
    }

    public void right(boolean bool) {
        j1_right = bool;
    }

    public void triger_a(boolean bool) {
        j1_triger_a = bool;
    }

    public void triger_b(boolean bool) {
        j1_triger_b = bool;
    }
}

