/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class GraphicScreen4_4 extends GraphicScreen {
    public void write_byte(int a, byte b) throws MC68000Exception {
        if ((a & 1) == 0) {
            return;
        }
        m[a] = (byte) (b & 15);
        crtc.sc[(a >> 11) - crtc.g_scroll_y[0] & 1023]++;
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        m[a + 1] = (byte) (s & 15);
        crtc.sc[(a >> 11) - crtc.g_scroll_y[0] & 1023]++;
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        i &= 983055;
        m[a + 1] = (byte) (i >> 16);
        m[a + 3] = (byte) i;
        int y = (a >> 11) - crtc.g_scroll_y[0] & 1023;
        crtc.sc[y]++;
        if ((a & 2047) >= 2045) {
            crtc.sc[y + 1]++;
        }
    }
}

