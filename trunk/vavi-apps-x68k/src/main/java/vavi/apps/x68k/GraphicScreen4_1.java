/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class GraphicScreen4_1 extends GraphicScreen {

    public byte read_byte(int a) throws MC68000Exception {
        if ((a & 1) == 0) {
            return 0;
        }
        return m[a];
    }

    public short read_short_big(int a) throws MC68000Exception {
        return m[a + 1];
    }

    public int read_int_big(int a) throws MC68000Exception {
        return (m[a + 1] << 16) + m[a + 3];
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        if ((a & 1) == 0) {
            return;
        }
        m[a] = (byte) (b & 15);
        int y = (a >> 10) - crtc.g_scroll_y[1] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        m[a + 1] = (byte) (s & 15);
        int y = (a >> 10) - crtc.g_scroll_y[1] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        i &= 983055;
        m[a + 1] = (byte) (i >> 16);
        m[a + 3] = (byte) i;
        if ((a & 1023) < 1021) {
            int y = (a >> 10) - crtc.g_scroll_y[1] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 512]++;
        } else {
            int y = (a >> 10) - crtc.g_scroll_y[1] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 1]++;
            crtc.sc[y + 512]++;
            crtc.sc[y + 513]++;
        }
    }
}

