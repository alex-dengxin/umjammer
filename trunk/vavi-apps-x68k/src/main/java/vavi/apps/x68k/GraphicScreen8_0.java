/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class GraphicScreen8_0 extends GraphicScreen {
    public byte read_byte(int a) throws MC68000Exception {
        if ((a & 1) == 0) {
            return 0;
        }
        return (byte) ((m[a + 524288] << 4) + m[a]);
    }

    public short read_short_big(int a) throws MC68000Exception {
        return (short) ((m[a + 524289] << 4) + m[a + 1]);
    }

    public int read_int_big(int a) throws MC68000Exception {
        return (m[a + 524289] << 20) + (m[a + 1] << 16) + (m[a + 524291] << 4) + m[a + 3];
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        if ((a & 1) == 0) {
            return;
        }
        m[a] = (byte) (b & 15);
        m[a + 524288] = (byte) (b >> 4 & 15);
        a >>= 10;
        int y = a - crtc.g_scroll_y[0] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
        y = a - crtc.g_scroll_y[1] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        m[a + 1] = (byte) (s & 15);
        m[a + 524289] = (byte) (s >> 4 & 15);
        a >>= 10;
        int y = a - crtc.g_scroll_y[0] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
        y = a - crtc.g_scroll_y[1] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        m[a + 3] = (byte) (i & 15);
        m[a + 524291] = (byte) (i >> 4 & 15);
        m[a + 1] = (byte) (i >> 16 & 15);
        m[a + 524289] = (byte) (i >> 20 & 15);
        if ((a & 1023) < 1021) {
            a >>= 10;
            int y = a - crtc.g_scroll_y[0] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 512]++;
            y = a - crtc.g_scroll_y[1] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 512]++;
        } else {
            a >>= 10;
            int y = a - crtc.g_scroll_y[0] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 1]++;
            crtc.sc[y + 512]++;
            crtc.sc[y + 513]++;
            y = a - crtc.g_scroll_y[1] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 1]++;
            crtc.sc[y + 512]++;
            crtc.sc[y + 513]++;
        }
    }
}

