/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class GraphicScreen16_0 extends GraphicScreen {

    public byte read_byte(int a) throws MC68000Exception {
        return (byte) ((a & 1) == 0 ? (m[a + 1572865] << 4) + m[a + 1048577] : (m[a + 524288] << 4) + m[a]);
    }

    public short read_short_big(int a) throws MC68000Exception {
        return (short) ((m[a + 1572865] << 12) + (m[a + 1048577] << 8) + (m[a + 524289] << 4) + m[a + 1]);
    }

    public int read_int_big(int a) throws MC68000Exception {
        return (m[a + 1572865] << 28) + (m[a + 1048577] << 24) + (m[a + 524289] << 20) + (m[a + 1] << 16) + (m[a + 1572867] << 12) + (m[a + 1048579] << 8) + (m[a + 524291] << 4) + m[a + 3];
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        if ((a & 1) != 0) {
            m[a] = (byte) (b & 15);
            m[a + 524288] = (byte) (b >> 4 & 15);
        } else {
            m[a + 1048577] = (byte) (b & 15);
            m[a + 1572865] = (byte) (b >> 4 & 15);
        }
        a >>= 10;
        int y = a - crtc.g_scroll_y[0] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
        y = a - crtc.g_scroll_y[1] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
        y = a - crtc.g_scroll_y[2] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
        y = a - crtc.g_scroll_y[3] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        m[a + 1] = (byte) (s & 15);
        m[a + 524289] = (byte) (s >> 4 & 15);
        m[a + 1048577] = (byte) (s >> 8 & 15);
        m[a + 1572865] = (byte) (s >> 12 & 15);
        a >>= 10;
        int y = a - crtc.g_scroll_y[0] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
        y = a - crtc.g_scroll_y[1] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
        y = a - crtc.g_scroll_y[2] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
        y = a - crtc.g_scroll_y[3] & 511;
        crtc.sc[y]++;
        crtc.sc[y + 512]++;
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        m[a + 3] = (byte) (i & 15);
        m[a + 524291] = (byte) (i >> 4 & 15);
        m[a + 1048579] = (byte) (i >> 8 & 15);
        m[a + 1572867] = (byte) (i >> 12 & 15);
        m[a + 1] = (byte) (i >> 16 & 15);
        m[a + 524289] = (byte) (i >> 20 & 15);
        m[a + 1048577] = (byte) (i >> 24 & 15);
        m[a + 1572865] = (byte) (i >> 28 & 15);
        if ((a & 1023) < 1021) {
            a >>= 10;
            int y = a - crtc.g_scroll_y[0] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 512]++;
            y = a - crtc.g_scroll_y[1] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 512]++;
            y = a - crtc.g_scroll_y[2] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 512]++;
            y = a - crtc.g_scroll_y[3] & 511;
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
            y = a - crtc.g_scroll_y[2] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 1]++;
            crtc.sc[y + 512]++;
            crtc.sc[y + 513]++;
            y = a - crtc.g_scroll_y[3] & 511;
            crtc.sc[y]++;
            crtc.sc[y + 1]++;
            crtc.sc[y + 512]++;
            crtc.sc[y + 513]++;
        }
    }
}

/* */
