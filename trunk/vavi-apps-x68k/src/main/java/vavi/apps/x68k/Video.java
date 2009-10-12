/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;


class Video extends MemoryMappedDevice implements X68000Device {

    public int r1, r2, r3;
    private int iw, ih, is, dw;
    private int stretch_mode;
    private int v1, v2;
    private int bitmap[];
    private MemoryImageSource source;
    public Image image;
    public int offset_x = 0, offset_y = 0;

    private static int palet_base[][];
    private int palet_conv[];
    private int palet16g8[], palet16ts[];
    private int palet32g8[], palet32ts[];
    private int palet8g16l[], palet8g16h[];
    private int s_buf[];
    private byte s_prw[];
    private boolean sprite_existence;
    private short s_n[];
    private static byte prw_stg[];
    private static byte prw_g0[];
    private static byte prw_g1[];
    private static byte prw_g2[];
    private static byte prw_g3[];

    static {
        palet_base = new int[16][];
        for (int j = 0; j <= 15; j++) {
            int max = 17 * j;
            int palet_temp[] = new int[65536];
            for (int i = 0; i <= 65535; i++) {
                palet_temp[i] = -16777216 + (((i >> 5 & 62) + (i & 1)) * max / 63 << 16) + (((i >> 10 & 62) + (i & 1)) * max / 63 << 8) + (i & 63) * max / 63;
            }
            palet_base[j] = palet_temp;
        }
        prw_stg = new byte[131072];
        for (int i = 0; i <= 131071; i++) {
            int m = (i & 65536) != 0 ? 4 : (i & 32768) != 0 ? 3 : (i & 16384) != 0 ? 2 : 1;
            int s = i >> 4 & 768;
            int t = i >> 2 & 768;
            int g = i & 768;
            switch ((i >> 3 & 12) + ((i & 16) >> 3) + ((i & 15) + 15 >> 4)) {
            case 0:
                prw_stg[i] = 0;
                continue;
            case 1:
                if (m == 4) {
                    prw_stg[i] = 0;
                    continue;
                }
                prw_stg[i] = (byte) m;
                continue;
            case 2:
                if (m != 4) {
                    prw_stg[i] = 0;
                    continue;
                }
            case 3:
                prw_stg[i] = (byte) m;
                continue;
            case 4:
                prw_stg[i] = 5;
                continue;
            case 5:
                if (m == 4) {
                    prw_stg[i] = 5;
                    continue;
                }
                prw_stg[i] = (byte) (g <= t ? 5 + m : 5 + m + 4);
                continue;
            case 6:
                if (m != 4) {
                    prw_stg[i] = 5;
                    continue;
                }
            case 7:
                prw_stg[i] = (byte) (g <= t ? 5 + m : 5 + m + 4);
                continue;
            case 8:
                prw_stg[i] = 14;
                continue;
            case 9:
                if (m == 4) {
                    prw_stg[i] = 14;
                    continue;
                }
                prw_stg[i] = (byte) (g <= s ? 14 + m : 14 + m + 4);
                continue;
            case 10:
                if (m != 4) {
                    prw_stg[i] = 14;
                    continue;
                }
            case 11:
                prw_stg[i] = (byte) (g <= s ? 14 + m : 14 + m + 4);
                continue;
            case 12:
                prw_stg[i] = (byte) (t <= s ? 23 : 36);
                continue;
            case 13:
                if (m == 4) {
                    prw_stg[i] = (byte) (t <= s ? 23 : 36);
                    continue;
                }
                if (g <= t) {
                    if (t <= s) {
                        prw_stg[i] = (byte) (23 + m);
                    } else if (g <= s) {
                        prw_stg[i] = (byte) (36 + m);
                    } else {
                        prw_stg[i] = (byte) (36 + m + 4);
                    }
                } else {
                    if (g <= s) {
                        prw_stg[i] = (byte) (23 + m + 4);
                    } else if (t <= s) {
                        prw_stg[i] = (byte) (23 + m + 8);
                    } else {
                        prw_stg[i] = (byte) (36 + m + 8);
                    }
                }
                continue;
            case 14:
                if (m != 4) {
                    prw_stg[i] = (byte) (t <= s ? 23 : 36);
                    continue;
                }
            case 15:
                if (g <= t) {
                    if (t <= s) {
                        prw_stg[i] = (byte) (23 + m);
                    } else if (g <= s) {
                        prw_stg[i] = (byte) (36 + m);
                    } else {
                        prw_stg[i] = (byte) (36 + m + 4);
                    }
                } else {
                    if (g <= s) {
                        prw_stg[i] = (byte) (23 + m + 4);
                    } else if (t <= s) {
                        prw_stg[i] = (byte) (23 + m + 8);
                    } else {
                        prw_stg[i] = (byte) (36 + m + 8);
                    }
                }
                continue;
            }
        }
        prw_g0 = new byte[4096];
        prw_g1 = new byte[4096];
        prw_g2 = new byte[4096];
        prw_g3 = new byte[4096];
        for (int i = 0; i < 4096; i += 16) {
            byte g0 = (byte) (i >> 4 & 3);
            byte g1 = (byte) (i >> 6 & 3);
            byte g2 = (byte) (i >> 8 & 3);
            byte g3 = (byte) (i >> 10 & 3);
            prw_g0[i] = -1;
            prw_g1[i] = -1;
            prw_g2[i] = -1;
            prw_g3[i] = -1;
            prw_g0[i + 1] = g0;
            prw_g1[i + 1] = -1;
            prw_g2[i + 1] = -1;
            prw_g3[i + 1] = -1;
            prw_g0[i + 2] = g1;
            prw_g1[i + 2] = -1;
            prw_g2[i + 2] = -1;
            prw_g3[i + 2] = -1;
            prw_g0[i + 3] = g0;
            prw_g1[i + 3] = g1;
            prw_g2[i + 3] = -1;
            prw_g3[i + 3] = -1;
            prw_g0[i + 4] = g2;
            prw_g1[i + 4] = -1;
            prw_g2[i + 4] = -1;
            prw_g3[i + 4] = -1;
            prw_g0[i + 5] = g0;
            prw_g1[i + 5] = g2;
            prw_g2[i + 5] = -1;
            prw_g3[i + 5] = -1;
            prw_g0[i + 6] = g1;
            prw_g1[i + 6] = g2;
            prw_g2[i + 6] = -1;
            prw_g3[i + 6] = -1;
            prw_g0[i + 7] = g0;
            prw_g1[i + 7] = g1;
            prw_g2[i + 7] = g2;
            prw_g3[i + 7] = -1;
            prw_g0[i + 8] = g3;
            prw_g1[i + 8] = -1;
            prw_g2[i + 8] = -1;
            prw_g3[i + 8] = -1;
            prw_g0[i + 9] = g0;
            prw_g1[i + 9] = g3;
            prw_g2[i + 9] = -1;
            prw_g3[i + 9] = -1;
            prw_g0[i + 10] = g1;
            prw_g1[i + 10] = g3;
            prw_g2[i + 10] = -1;
            prw_g3[i + 10] = -1;
            prw_g0[i + 11] = g0;
            prw_g1[i + 11] = g1;
            prw_g2[i + 11] = g3;
            prw_g3[i + 11] = -1;
            prw_g0[i + 12] = g2;
            prw_g1[i + 12] = g3;
            prw_g2[i + 12] = -1;
            prw_g3[i + 12] = -1;
            prw_g0[i + 13] = g0;
            prw_g1[i + 13] = g2;
            prw_g2[i + 13] = g3;
            prw_g3[i + 13] = -1;
            prw_g0[i + 14] = g1;
            prw_g1[i + 14] = g2;
            prw_g2[i + 14] = g3;
            prw_g3[i + 14] = -1;
            prw_g0[i + 15] = g0;
            prw_g1[i + 15] = g1;
            prw_g2[i + 15] = g2;
            prw_g3[i + 15] = g3;
        }
    }

    private X68000 x68000;

    private Graphics graphics;

    private GraphicScreen graphic_screen;

    private CRTC crtc;

    private SpriteScreen sprite_screen;

    public Video() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        graphics = x68000.graphics;
        graphic_screen = x68000.graphic_screen;
        crtc = x68000.crtc;
        sprite_screen = x68000.sprite_screen;
        palet16g8 = new int[256];
        palet16ts = new int[256];
        palet32g8 = new int[256];
        palet32ts = new int[256];
        palet8g16l = new int[256];
        palet8g16h = new int[256];
        for (int i = 0; i <= 255; i++) {
            palet16g8[i] = 0;
            palet16ts[i] = 0;
            palet32g8[i] = -16777216;
            palet32ts[i] = -16777216;
            palet8g16l[i] = 0;
            palet8g16h[i] = 0;
        }
        s_buf = new int[1056];
        s_prw = new byte[1056];
        sprite_existence = true;
        s_n = new short[128];
        r1 = 0;
        r2 = 0;
        r3 = 0;
        reset();
        return true;
    }

    public void reset() {
        palet_conv = palet_base[15];
    }

    public byte read_byte(int a) {
        if (a < 15213056) {
            int n = (a & 511) >> 1;
            return (a & 1) == 0 ? (byte) (palet16g8[n] >> 8) : (byte) palet16g8[n];
        }
        if (a < 15213568) {
            int n = (a & 511) >> 1;
            return (a & 1) == 0 ? (byte) (palet16ts[n] >> 8) : (byte) palet16ts[n];
        }
        switch (a) {
        case 15213568:
            return 0;
        case 15213569:
            return (byte) r1;
        case 15213824:
            return (byte) (r2 >> 8);
        case 15213825:
            return (byte) r2;
        case 15214080:
            return (byte) (r3 >> 8);
        case 15214081:
            return (byte) r3;
        }
        return 0;
    }

    public short read_short_big(int a) {
        if (a < 15213056) {
            return (short) palet16g8[(a & 511) >> 1];
        }
        if (a < 15213568) {
            return (short) palet16ts[(a & 511) >> 1];
        }
        switch (a) {
        case 15213568:
            return (short) r1;
        case 15213824:
            return (short) r2;
        case 15214080:
            return (short) r3;
        }
        return 0;
    }

    public int read_int_big(int a) {
        return ((read_short_big(a) & 65535) << 16) + (read_short_big(a + 2) & 65535);
    }

    public void write_byte(int a, byte b) {
        if (a < 15213056) {
            int n = (a & 511) >> 1;
            if ((a & 1) == 0) {
                palet32g8[n] = palet_conv[palet16g8[n] = ((b & 255) << 8) + (palet16g8[n] & 255)];
                if ((n & 1) == 0) {
                    palet8g16l[n] = b & 255;
                } else {
                    palet8g16h[n - 1] = (b & 255) << 8;
                }
            } else {
                palet32g8[n] = palet_conv[palet16g8[n] = (palet16g8[n] & 65280) + (b & 255)];
                if ((n & 1) == 0) {
                    palet8g16l[n + 1] = b & 255;
                } else {
                    palet8g16h[n] = (b & 255) << 8;
                }
            }
            if ((r3 & 31) != 0) {
                crtc.updateAll();
            }
            return;
        }
        if (a < 15213568) {
            int n = (a & 511) >> 1;
            palet32ts[n] = palet_conv[palet16ts[n] = (a & 1) == 0 ? ((b & 255) << 8) + (palet16ts[n] & 255) : (palet16ts[n] & 65280) + (b & 255)];
            if ((r3 & 64) != 0 || n < 16 && (r3 & 32) != 0) {
                crtc.updateAll();
            }
            return;
        }
        switch (a) {
        case 15213568:
            break;
        case 15213569:
            r1 = b & 7;
            break;
        case 15213824:
            r2 = ((b & 63) << 8) + (r2 & 255);
            break;
        case 15213825:
            r2 = (r2 & 16128) + (b & 255);
            break;
        case 15214080:
            r3 = ((b & 255) << 8) + (r3 & 255);
            break;
        case 15214081:
            r3 = (r3 & 65280) + (b & 127);
            break;
        }
        crtc.updateAll();
    }

    public void write_short_big(int a, short s) {
        if (a < 15213056) {
            int n = (a & 511) >> 1;
            palet32g8[n] = palet_conv[palet16g8[n] = s & 65535];
            if ((n & 1) == 0) {
                palet8g16l[n] = s >> 8 & 255;
                palet8g16l[n + 1] = s & 255;
            } else {
                palet8g16h[n - 1] = s & 65280;
                palet8g16h[n] = (s & 255) << 8;
            }
            if ((r3 & 31) != 0) {
                crtc.updateAll();
            }
            return;
        }
        if (a < 15213568) {
            int n = (a & 511) >> 1;
            palet32ts[n] = palet_conv[palet16ts[n] = s & 65535];
            if ((r3 & 64) != 0 || n < 16 && (r3 & 32) != 0) {
                crtc.updateAll();
            }
            return;
        }
        switch (a) {
        case 15213568:
            r1 = s & 7;
            break;
        case 15213824:
            r2 = s & 16383;
            break;
        case 15214080:
            r3 = s & 65407;
            break;
        }
        crtc.updateAll();
    }

    public void write_int_big(int a, int i) {
        write_short_big(a, (short) (i >> 16));
        write_short_big(a + 2, (short) i);
    }

    public void setContrast(int contrast) {
        palet_conv = palet_base[contrast];
        for (int n = 0; n <= 255; n++) {
            palet32g8[n] = palet_conv[palet16g8[n]];
            palet32ts[n] = palet_conv[palet16ts[n]];
        }
        crtc.updateAll();
    }

    public void initScreen(int width, int height, int h_stretch_mode) {
        iw = width * h_stretch_mode >> 3;
        ih = height;
        is = iw * ih;
        dw = width;
        stretch_mode = h_stretch_mode;
        bitmap = new int[is];
        for (int i = 0; i < is; i++) {
            bitmap[i] = -16777216;
        }
        source = new MemoryImageSource(iw, ih, bitmap, 0, iw);
        source.setAnimated(true);
        source.setFullBufferUpdates(false);
        image = x68000.createImage(source);
        int previous_offset_x = offset_x;
        int previous_offset_y = offset_y;
        offset_x = (768 - iw) / 2;
        offset_y = (512 - ih) / 2;
        if (offset_x > previous_offset_x || offset_y > previous_offset_y) {
            graphics.setColor(Color.darkGray);
            graphics.fillRect(0, 0, 768, 512);
        }
        sprite_existence = true;
        v1 = ih - 1;
        v2 = 0;
    }

    public void drawRaster(int src, int dst) {
        switch (prw_stg[(r1 << 14) + (r2 & 16128) + (r3 & 127)]) {
        case 0:
            draw_null(src, dst);
            break;
        case 1:
            draw_e(src, dst);
            break;
        case 2:
            draw_f(src, dst);
            break;
        case 3:
            draw_g(src, dst);
            break;
        case 4:
            draw_h(src, dst);
            break;
        case 5:
            draw_t(src, dst);
            break;
        case 6:
            draw_et(src, dst);
            break;
        case 7:
            draw_ft(src, dst);
            break;
        case 8:
            draw_gt(src, dst);
            break;
        case 9:
            draw_ht(src, dst);
            break;
        case 10:
            draw_te(src, dst);
            break;
        case 11:
            draw_tf(src, dst);
            break;
        case 12:
            draw_tg(src, dst);
            break;
        case 13:
            draw_th(src, dst);
            break;
        case 14:
            draw_s(src, dst);
            break;
        case 15:
            draw_es(src, dst);
            break;
        case 16:
            draw_fs(src, dst);
            break;
        case 17:
            draw_gs(src, dst);
            break;
        case 18:
            draw_hs(src, dst);
            break;
        case 19:
            draw_se(src, dst);
            break;
        case 20:
            draw_sf(src, dst);
            break;
        case 21:
            draw_sg(src, dst);
            break;
        case 22:
            draw_sh(src, dst);
            break;
        case 23:
            draw_ts(src, dst);
            break;
        case 24:
            draw_ets(src, dst);
            break;
        case 25:
            draw_fts(src, dst);
            break;
        case 26:
            draw_gts(src, dst);
            break;
        case 27:
            draw_hts(src, dst);
            break;
        case 28:
            draw_tes(src, dst);
            break;
        case 29:
            draw_tfs(src, dst);
            break;
        case 30:
            draw_tgs(src, dst);
            break;
        case 31:
            draw_ths(src, dst);
            break;
        case 32:
            draw_tse(src, dst);
            break;
        case 33:
            draw_tsf(src, dst);
            break;
        case 34:
            draw_tsg(src, dst);
            break;
        case 35:
            draw_tsh(src, dst);
            break;
        case 36:
            draw_st(src, dst);
            break;
        case 37:
            draw_est(src, dst);
            break;
        case 38:
            draw_fst(src, dst);
            break;
        case 39:
            draw_gst(src, dst);
            break;
        case 40:
            draw_hst(src, dst);
            break;
        case 41:
            draw_set(src, dst);
            break;
        case 42:
            draw_sft(src, dst);
            break;
        case 43:
            draw_sgt(src, dst);
            break;
        case 44:
            draw_sht(src, dst);
            break;
        case 45:
            draw_ste(src, dst);
            break;
        case 46:
            draw_stf(src, dst);
            break;
        case 47:
            draw_stg(src, dst);
            break;
        case 48:
            draw_sth(src, dst);
            break;
        }
        switch (stretch_mode) {
        case 11:
            stretch11(dst);
            break;
        case 14:
            stretch14(dst);
            break;
        case 21:
            stretch21(dst);
            break;
        case 28:
            stretch28(dst);
            break;
        }
        if (dst < v1) {
            v1 = dst;
        }
        if (dst > v2) {
            v2 = dst;
        }
    }

    private void draw_null(int src, int dst) {
        int da = iw * dst, db = da + dw;
        while (da < db) {
            bitmap[da] = -16777216;
            bitmap[da + 1] = -16777216;
            bitmap[da + 2] = -16777216;
            bitmap[da + 3] = -16777216;
            bitmap[da + 4] = -16777216;
            bitmap[da + 5] = -16777216;
            bitmap[da + 6] = -16777216;
            bitmap[da + 7] = -16777216;
            da += 8;
        }
    }

    private void draw_e(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                bitmap[da] = palet32g8[m[gy0 + (gx0 + 1 & 1023)]];
                bitmap[da + 1] = palet32g8[m[gy0 + (gx0 + 3 & 1023)]];
                bitmap[da + 2] = palet32g8[m[gy0 + (gx0 + 5 & 1023)]];
                bitmap[da + 3] = palet32g8[m[gy0 + (gx0 + 7 & 1023)]];
                bitmap[da + 4] = palet32g8[m[gy0 + (gx0 + 9 & 1023)]];
                bitmap[da + 5] = palet32g8[m[gy0 + (gx0 + 11 & 1023)]];
                bitmap[da + 6] = palet32g8[m[gy0 + (gx0 + 13 & 1023)]];
                bitmap[da + 7] = palet32g8[m[gy0 + (gx0 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    int p;
                    bitmap[da] = palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]];
                    bitmap[da + 1] = palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]];
                    bitmap[da + 2] = palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]];
                    bitmap[da + 3] = palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]];
                    bitmap[da + 4] = palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]];
                    bitmap[da + 5] = palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]];
                    bitmap[da + 6] = palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]];
                    bitmap[da + 7] = palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        int p;
                        bitmap[da] = palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]];
                        bitmap[da + 1] = palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]];
                        bitmap[da + 2] = palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]];
                        bitmap[da + 3] = palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]];
                        bitmap[da + 4] = palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]];
                        bitmap[da + 5] = palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]];
                        bitmap[da + 6] = palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]];
                        bitmap[da + 7] = palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        int p;
                        bitmap[da] = palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]];
                        bitmap[da + 1] = palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]];
                        bitmap[da + 2] = palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]];
                        bitmap[da + 3] = palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]];
                        bitmap[da + 4] = palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]];
                        bitmap[da + 5] = palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]];
                        bitmap[da + 6] = palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]];
                        bitmap[da + 7] = palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                    }
                }
            }
        }
    }

    private void draw_f(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            while (da < db) {
                bitmap[da] = palet32g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]];
                bitmap[da + 1] = palet32g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]];
                bitmap[da + 2] = palet32g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]];
                bitmap[da + 3] = palet32g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]];
                bitmap[da + 4] = palet32g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]];
                bitmap[da + 5] = palet32g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]];
                bitmap[da + 6] = palet32g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]];
                bitmap[da + 7] = palet32g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            while (da < db) {
                int p;
                bitmap[da] = palet32g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]];
                bitmap[da + 1] = palet32g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]];
                bitmap[da + 2] = palet32g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]];
                bitmap[da + 3] = palet32g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]];
                bitmap[da + 4] = palet32g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]];
                bitmap[da + 5] = palet32g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]];
                bitmap[da + 6] = palet32g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]];
                bitmap[da + 7] = palet32g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
            }
        }
    }

    private void draw_g(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        while (da < db) {
            bitmap[da] = palet_conv[palet8g16h[(m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask_l]];
            bitmap[da + 1] = palet_conv[palet8g16h[(m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask_l]];
            bitmap[da + 2] = palet_conv[palet8g16h[(m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask_l]];
            bitmap[da + 3] = palet_conv[palet8g16h[(m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask_l]];
            bitmap[da + 4] = palet_conv[palet8g16h[(m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask_l]];
            bitmap[da + 5] = palet_conv[palet8g16h[(m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask_l]];
            bitmap[da + 6] = palet_conv[palet8g16h[(m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask_l]];
            bitmap[da + 7] = palet_conv[palet8g16h[(m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask_l]];
            da += 8;
            gx0 += 16;
            gx1 += 16;
            gx2 += 16;
            gx3 += 16;
        }
    }

    private void draw_h(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            bitmap[da] = palet32g8[m[gy0 + (gx0 + 1 & 2047)]];
            bitmap[da + 1] = palet32g8[m[gy0 + (gx0 + 3 & 2047)]];
            bitmap[da + 2] = palet32g8[m[gy0 + (gx0 + 5 & 2047)]];
            bitmap[da + 3] = palet32g8[m[gy0 + (gx0 + 7 & 2047)]];
            bitmap[da + 4] = palet32g8[m[gy0 + (gx0 + 9 & 2047)]];
            bitmap[da + 5] = palet32g8[m[gy0 + (gx0 + 11 & 2047)]];
            bitmap[da + 6] = palet32g8[m[gy0 + (gx0 + 13 & 2047)]];
            bitmap[da + 7] = palet32g8[m[gy0 + (gx0 + 15 & 2047)]];
            da += 8;
            gx0 += 16;
        }
    }

    private void draw_t(int src, int dst) {
        int da = iw * dst, db = da + dw;
        if ((crtc.r10 & 7) == 0) {
            int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3;
            while (da < db) {
                byte p0 = m[14680064 + ty + tx];
                byte p1 = m[14811136 + ty + tx];
                byte p2 = m[14942208 + ty + tx];
                byte p3 = m[15073280 + ty + tx];
                tx = tx + 1 & 127;
                bitmap[da] = palet32ts[(p3 >> 4 & 8) + (p2 >> 5 & 4) + (p1 >> 6 & 2) + (p0 >> 7 & 1)];
                bitmap[da + 1] = palet32ts[(p3 >> 3 & 8) + (p2 >> 4 & 4) + (p1 >> 5 & 2) + (p0 >> 6 & 1)];
                bitmap[da + 2] = palet32ts[(p3 >> 2 & 8) + (p2 >> 3 & 4) + (p1 >> 4 & 2) + (p0 >> 5 & 1)];
                bitmap[da + 3] = palet32ts[(p3 >> 1 & 8) + (p2 >> 2 & 4) + (p1 >> 3 & 2) + (p0 >> 4 & 1)];
                bitmap[da + 4] = palet32ts[(p3 & 8) + (p2 >> 1 & 4) + (p1 >> 2 & 2) + (p0 >> 3 & 1)];
                bitmap[da + 5] = palet32ts[(p3 << 1 & 8) + (p2 & 4) + (p1 >> 1 & 2) + (p0 >> 2 & 1)];
                bitmap[da + 6] = palet32ts[(p3 << 2 & 8) + (p2 << 1 & 4) + (p1 & 2) + (p0 >> 1 & 1)];
                bitmap[da + 7] = palet32ts[(p3 << 3 & 8) + (p2 << 2 & 4) + (p1 << 1 & 2) + (p0 & 1)];
                da += 8;
            }
        } else {
            int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
            short p0 = (short) (m[14680064 + ty + tx] << ts);
            short p1 = (short) (m[14811136 + ty + tx] << ts);
            short p2 = (short) (m[14942208 + ty + tx] << ts);
            short p3 = (short) (m[15073280 + ty + tx] << ts);
            tx = tx + 1 & 127;
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                bitmap[da] = palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                bitmap[da + 1] = palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                bitmap[da + 2] = palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                bitmap[da + 3] = palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                bitmap[da + 4] = palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                bitmap[da + 5] = palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                bitmap[da + 6] = palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                bitmap[da + 7] = palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                da += 8;
            }
        }
    }

    private void draw_et(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]];
                bitmap[da + 1] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]];
                bitmap[da + 2] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]];
                bitmap[da + 3] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]];
                bitmap[da + 4] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]];
                bitmap[da + 5] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]];
                bitmap[da + 6] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]];
                bitmap[da + 7] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]];
                da += 8;
                gx0 += 16;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]];
                    bitmap[da + 1] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]];
                    bitmap[da + 2] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]];
                    bitmap[da + 3] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]];
                    bitmap[da + 4] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]];
                    bitmap[da + 5] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]];
                    bitmap[da + 6] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]];
                    bitmap[da + 7] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]];
                        bitmap[da + 1] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]];
                        bitmap[da + 2] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]];
                        bitmap[da + 3] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]];
                        bitmap[da + 4] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]];
                        bitmap[da + 5] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]];
                        bitmap[da + 6] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]];
                        bitmap[da + 7] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]];
                        bitmap[da + 1] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]];
                        bitmap[da + 2] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]];
                        bitmap[da + 3] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]];
                        bitmap[da + 4] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]];
                        bitmap[da + 5] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]];
                        bitmap[da + 6] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]];
                        bitmap[da + 7] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                    }
                }
            }
        }
    }

    private void draw_ft(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]];
                bitmap[da + 1] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]];
                bitmap[da + 2] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]];
                bitmap[da + 3] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]];
                bitmap[da + 4] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]];
                bitmap[da + 5] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]];
                bitmap[da + 6] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]];
                bitmap[da + 7] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]];
                bitmap[da + 1] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]];
                bitmap[da + 2] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]];
                bitmap[da + 3] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]];
                bitmap[da + 4] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]];
                bitmap[da + 5] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]];
                bitmap[da + 6] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]];
                bitmap[da + 7] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? p : palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
            }
        }
    }

    private void draw_gt(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = palet_conv[(p = (m[gy3 + (gx3 + 1 & 1023)] << 12) + (m[gy2 + (gx2 + 1 & 1023)] << 8) + (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]];
            bitmap[da + 1] = palet_conv[(p = (m[gy3 + (gx3 + 3 & 1023)] << 12) + (m[gy2 + (gx2 + 3 & 1023)] << 8) + (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]];
            bitmap[da + 2] = palet_conv[(p = (m[gy3 + (gx3 + 5 & 1023)] << 12) + (m[gy2 + (gx2 + 5 & 1023)] << 8) + (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]];
            bitmap[da + 3] = palet_conv[(p = (m[gy3 + (gx3 + 7 & 1023)] << 12) + (m[gy2 + (gx2 + 7 & 1023)] << 8) + (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]];
            bitmap[da + 4] = palet_conv[(p = (m[gy3 + (gx3 + 9 & 1023)] << 12) + (m[gy2 + (gx2 + 9 & 1023)] << 8) + (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]];
            bitmap[da + 5] = palet_conv[(p = (m[gy3 + (gx3 + 11 & 1023)] << 12) + (m[gy2 + (gx2 + 11 & 1023)] << 8) + (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]];
            bitmap[da + 6] = palet_conv[(p = (m[gy3 + (gx3 + 13 & 1023)] << 12) + (m[gy2 + (gx2 + 13 & 1023)] << 8) + (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]];
            bitmap[da + 7] = palet_conv[(p = (m[gy3 + (gx3 + 15 & 1023)] << 12) + (m[gy2 + (gx2 + 15 & 1023)] << 8) + (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]];
            da += 8;
            gx0 += 16;
            gx1 += 16;
            gx2 += 16;
            gx3 += 16;
        }
    }

    private void draw_ht(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 1 & 2047)]]) != 0 ? p : palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]];
            bitmap[da + 1] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 3 & 2047)]]) != 0 ? p : palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]];
            bitmap[da + 2] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 5 & 2047)]]) != 0 ? p : palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]];
            bitmap[da + 3] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 7 & 2047)]]) != 0 ? p : palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]];
            bitmap[da + 4] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 9 & 2047)]]) != 0 ? p : palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]];
            bitmap[da + 5] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 11 & 2047)]]) != 0 ? p : palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]];
            bitmap[da + 6] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 13 & 2047)]]) != 0 ? p : palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]];
            bitmap[da + 7] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 15 & 2047)]]) != 0 ? p : palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]];
            da += 8;
            gx0 += 16;
        }
    }

    private void draw_te(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 1 & 1023)]];
                bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 3 & 1023)]];
                bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 5 & 1023)]];
                bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 7 & 1023)]];
                bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 9 & 1023)]];
                bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 11 & 1023)]];
                bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 13 & 1023)]];
                bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]];
                    bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]];
                    bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]];
                    bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]];
                    bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]];
                    bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]];
                    bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]];
                    bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]];
                        bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]];
                        bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]];
                        bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]];
                        bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]];
                        bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]];
                        bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]];
                        bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]];
                        bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]];
                        bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]];
                        bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]];
                        bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]];
                        bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]];
                        bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]];
                        bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                    }
                }
            }
        }
    }

    private void draw_tf(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]];
                bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]];
                bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]];
                bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]];
                bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]];
                bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]];
                bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]];
                bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]];
                bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]];
                bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]];
                bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]];
                bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]];
                bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]];
                bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]];
                bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
            }
        }
    }

    private void draw_tg(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        switch (r3 & 32512) {
        case 7424:
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                if ((p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0) {
                    bitmap[da] = palet_conv[p];
                } else if (((p = (m[gy3 + (gx3 + 1 & 1023)] << 12) + (m[gy2 + (gx2 + 1 & 1023)] << 8) + (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask) & 1) == 0) {
                    bitmap[da] = palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]];
                } else {
                    bitmap[da] = palet_conv[p >> 1 & 31710];
                }
                if ((p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0) {
                    bitmap[da + 1] = palet_conv[p];
                } else if (((p = (m[gy3 + (gx3 + 3 & 1023)] << 12) + (m[gy2 + (gx2 + 3 & 1023)] << 8) + (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask) & 1) == 0) {
                    bitmap[da + 1] = palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]];
                } else {
                    bitmap[da + 1] = palet_conv[p >> 1 & 31710];
                }
                if ((p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0) {
                    bitmap[da + 2] = palet_conv[p];
                } else if (((p = (m[gy3 + (gx3 + 5 & 1023)] << 12) + (m[gy2 + (gx2 + 5 & 1023)] << 8) + (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask) & 1) == 0) {
                    bitmap[da + 2] = palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]];
                } else {
                    bitmap[da + 2] = palet_conv[p >> 1 & 31710];
                }
                if ((p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0) {
                    bitmap[da + 3] = palet_conv[p];
                } else if (((p = (m[gy3 + (gx3 + 7 & 1023)] << 12) + (m[gy2 + (gx2 + 7 & 1023)] << 8) + (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask) & 1) == 0) {
                    bitmap[da + 3] = palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]];
                } else {
                    bitmap[da + 3] = palet_conv[p >> 1 & 31710];
                }
                if ((p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0) {
                    bitmap[da + 4] = palet_conv[p];
                } else if (((p = (m[gy3 + (gx3 + 9 & 1023)] << 12) + (m[gy2 + (gx2 + 9 & 1023)] << 8) + (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask) & 1) == 0) {
                    bitmap[da + 4] = palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]];
                } else {
                    bitmap[da + 4] = palet_conv[p >> 1 & 31710];
                }
                if ((p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0) {
                    bitmap[da + 5] = palet_conv[p];
                } else if (((p = (m[gy3 + (gx3 + 11 & 1023)] << 12) + (m[gy2 + (gx2 + 11 & 1023)] << 8) + (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask) & 1) == 0) {
                    bitmap[da + 5] = palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]];
                } else {
                    bitmap[da + 5] = palet_conv[p >> 1 & 31710];
                }
                if ((p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0) {
                    bitmap[da + 6] = palet_conv[p];
                } else if (((p = (m[gy3 + (gx3 + 13 & 1023)] << 12) + (m[gy2 + (gx2 + 13 & 1023)] << 8) + (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask) & 1) == 0) {
                    bitmap[da + 6] = palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]];
                } else {
                    bitmap[da + 6] = palet_conv[p >> 1 & 31710];
                }
                if ((p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0) {
                    bitmap[da + 7] = palet_conv[p];
                } else if (((p = (m[gy3 + (gx3 + 15 & 1023)] << 12) + (m[gy2 + (gx2 + 15 & 1023)] << 8) + (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask) & 1) == 0) {
                    bitmap[da + 7] = palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]];
                } else {
                    bitmap[da + 7] = palet_conv[p >> 1 & 31710];
                }
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
            }
            break;
        default:
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask_l]];
                bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask_l]];
                bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask_l]];
                bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask_l]];
                bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask_l]];
                bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask_l]];
                bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask_l]];
                bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask_l]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
            }
        }
    }

    private void draw_th(int src, int dst) {
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 1 & 2047)]];
            bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 3 & 2047)]];
            bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 5 & 2047)]];
            bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 7 & 2047)]];
            bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 9 & 2047)]];
            bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 11 & 2047)]];
            bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 13 & 2047)]];
            bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 15 & 2047)]];
            da += 8;
            gx0 += 16;
        }
    }

    private void draw_s(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_null(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        while (da < db) {
            bitmap[da] = palet_conv[s_buf[sx]];
            bitmap[da + 1] = palet_conv[s_buf[sx + 1]];
            bitmap[da + 2] = palet_conv[s_buf[sx + 2]];
            bitmap[da + 3] = palet_conv[s_buf[sx + 3]];
            bitmap[da + 4] = palet_conv[s_buf[sx + 4]];
            bitmap[da + 5] = palet_conv[s_buf[sx + 5]];
            bitmap[da + 6] = palet_conv[s_buf[sx + 6]];
            bitmap[da + 7] = palet_conv[s_buf[sx + 7]];
            da += 8;
            sx += 8;
        }
    }

    private void draw_es(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_e(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                int p;
                bitmap[da] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? p : s_buf[sx]];
                bitmap[da + 1] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? p : s_buf[sx + 1]];
                bitmap[da + 2] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? p : s_buf[sx + 2]];
                bitmap[da + 3] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? p : s_buf[sx + 3]];
                bitmap[da + 4] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? p : s_buf[sx + 4]];
                bitmap[da + 5] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? p : s_buf[sx + 5]];
                bitmap[da + 6] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? p : s_buf[sx + 6]];
                bitmap[da + 7] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? p : s_buf[sx + 7]];
                da += 8;
                gx0 += 16;
                sx += 8;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    int p;
                    bitmap[da] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]]) != 0 ? p : s_buf[sx]];
                    bitmap[da + 1] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]]) != 0 ? p : s_buf[sx + 1]];
                    bitmap[da + 2] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]]) != 0 ? p : s_buf[sx + 2]];
                    bitmap[da + 3] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]]) != 0 ? p : s_buf[sx + 3]];
                    bitmap[da + 4] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]]) != 0 ? p : s_buf[sx + 4]];
                    bitmap[da + 5] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]]) != 0 ? p : s_buf[sx + 5]];
                    bitmap[da + 6] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]]) != 0 ? p : s_buf[sx + 6]];
                    bitmap[da + 7] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]]) != 0 ? p : s_buf[sx + 7]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    sx += 8;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        int p;
                        bitmap[da] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? p : s_buf[sx]];
                        bitmap[da + 1] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? p : s_buf[sx + 1]];
                        bitmap[da + 2] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? p : s_buf[sx + 2]];
                        bitmap[da + 3] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? p : s_buf[sx + 3]];
                        bitmap[da + 4] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? p : s_buf[sx + 4]];
                        bitmap[da + 5] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? p : s_buf[sx + 5]];
                        bitmap[da + 6] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? p : s_buf[sx + 6]];
                        bitmap[da + 7] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? p : s_buf[sx + 7]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        sx += 8;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        int p;
                        bitmap[da] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]]) != 0 ? p : s_buf[sx]];
                        bitmap[da + 1] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]]) != 0 ? p : s_buf[sx + 1]];
                        bitmap[da + 2] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]]) != 0 ? p : s_buf[sx + 2]];
                        bitmap[da + 3] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]]) != 0 ? p : s_buf[sx + 3]];
                        bitmap[da + 4] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]]) != 0 ? p : s_buf[sx + 4]];
                        bitmap[da + 5] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]]) != 0 ? p : s_buf[sx + 5]];
                        bitmap[da + 6] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]]) != 0 ? p : s_buf[sx + 6]];
                        bitmap[da + 7] = palet_conv[(p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]]) != 0 ? p : s_buf[sx + 7]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                        sx += 8;
                    }
                }
            }
        }
    }

    private void draw_fs(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_f(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            while (da < db) {
                int p;
                bitmap[da] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? p : s_buf[sx]];
                bitmap[da + 1] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? p : s_buf[sx + 1]];
                bitmap[da + 2] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? p : s_buf[sx + 2]];
                bitmap[da + 3] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? p : s_buf[sx + 3]];
                bitmap[da + 4] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? p : s_buf[sx + 4]];
                bitmap[da + 5] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? p : s_buf[sx + 5]];
                bitmap[da + 6] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? p : s_buf[sx + 6]];
                bitmap[da + 7] = palet_conv[(p = palet16g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? p : s_buf[sx + 7]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                sx += 8;
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            while (da < db) {
                int p;
                bitmap[da] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? p : s_buf[sx]];
                bitmap[da + 1] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? p : s_buf[sx + 1]];
                bitmap[da + 2] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? p : s_buf[sx + 2]];
                bitmap[da + 3] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? p : s_buf[sx + 3]];
                bitmap[da + 4] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? p : s_buf[sx + 4]];
                bitmap[da + 5] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? p : s_buf[sx + 5]];
                bitmap[da + 6] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? p : s_buf[sx + 6]];
                bitmap[da + 7] = palet_conv[(p = palet16g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? p : s_buf[sx + 7]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
                sx += 8;
            }
        }
    }

    private void draw_gs(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_g(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        while (da < db) {
            int p;
            bitmap[da] = palet_conv[(p = (m[gy3 + (gx3 + 1 & 1023)] << 12) + (m[gy2 + (gx2 + 1 & 1023)] << 8) + (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : s_buf[sx]];
            bitmap[da + 1] = palet_conv[(p = (m[gy3 + (gx3 + 3 & 1023)] << 12) + (m[gy2 + (gx2 + 3 & 1023)] << 8) + (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : s_buf[sx + 1]];
            bitmap[da + 2] = palet_conv[(p = (m[gy3 + (gx3 + 5 & 1023)] << 12) + (m[gy2 + (gx2 + 5 & 1023)] << 8) + (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : s_buf[sx + 2]];
            bitmap[da + 3] = palet_conv[(p = (m[gy3 + (gx3 + 7 & 1023)] << 12) + (m[gy2 + (gx2 + 7 & 1023)] << 8) + (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : s_buf[sx + 3]];
            bitmap[da + 4] = palet_conv[(p = (m[gy3 + (gx3 + 9 & 1023)] << 12) + (m[gy2 + (gx2 + 9 & 1023)] << 8) + (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : s_buf[sx + 4]];
            bitmap[da + 5] = palet_conv[(p = (m[gy3 + (gx3 + 11 & 1023)] << 12) + (m[gy2 + (gx2 + 11 & 1023)] << 8) + (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : s_buf[sx + 5]];
            bitmap[da + 6] = palet_conv[(p = (m[gy3 + (gx3 + 13 & 1023)] << 12) + (m[gy2 + (gx2 + 13 & 1023)] << 8) + (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : s_buf[sx + 6]];
            bitmap[da + 7] = palet_conv[(p = (m[gy3 + (gx3 + 15 & 1023)] << 12) + (m[gy2 + (gx2 + 15 & 1023)] << 8) + (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask) != 0 ? palet8g16h[p >> 8 & 255] + palet8g16l[p & 255] : s_buf[sx + 7]];
            da += 8;
            gx0 += 16;
            gx1 += 16;
            gx2 += 16;
            gx3 += 16;
            sx += 8;
        }
    }

    private void draw_hs(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_h(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            int p;
            bitmap[da] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 1 & 2047)]]) != 0 ? p : s_buf[sx]];
            bitmap[da + 1] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 3 & 2047)]]) != 0 ? p : s_buf[sx + 1]];
            bitmap[da + 2] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 5 & 2047)]]) != 0 ? p : s_buf[sx + 2]];
            bitmap[da + 3] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 7 & 2047)]]) != 0 ? p : s_buf[sx + 3]];
            bitmap[da + 4] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 9 & 2047)]]) != 0 ? p : s_buf[sx + 4]];
            bitmap[da + 5] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 11 & 2047)]]) != 0 ? p : s_buf[sx + 5]];
            bitmap[da + 6] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 13 & 2047)]]) != 0 ? p : s_buf[sx + 6]];
            bitmap[da + 7] = palet_conv[(p = palet16g8[m[gy0 + (gx0 + 15 & 2047)]]) != 0 ? p : s_buf[sx + 7]];
            da += 8;
            gx0 += 16;
            sx += 8;
        }
    }

    private void draw_se(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_e(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                int p;
                bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 1 & 1023)]];
                bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 3 & 1023)]];
                bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 5 & 1023)]];
                bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 7 & 1023)]];
                bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 9 & 1023)]];
                bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 11 & 1023)]];
                bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 13 & 1023)]];
                bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                sx += 8;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    int p;
                    bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]];
                    bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]];
                    bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]];
                    bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]];
                    bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]];
                    bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]];
                    bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]];
                    bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    sx += 8;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        int p;
                        bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]];
                        bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]];
                        bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]];
                        bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]];
                        bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]];
                        bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]];
                        bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]];
                        bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        sx += 8;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        int p;
                        bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]];
                        bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]];
                        bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]];
                        bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]];
                        bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]];
                        bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]];
                        bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]];
                        bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                        sx += 8;
                    }
                }
            }
        }
    }

    private void draw_sf(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_f(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            while (da < db) {
                int p;
                bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]];
                bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]];
                bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]];
                bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]];
                bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]];
                bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]];
                bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]];
                bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                sx += 8;
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            while (da < db) {
                int p;
                bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]];
                bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]];
                bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]];
                bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]];
                bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]];
                bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]];
                bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]];
                bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
                sx += 8;
            }
        }
    }

    private void draw_sg(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_g(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        while (da < db) {
            int p;
            bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask_l]];
            bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask_l]];
            bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask_l]];
            bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask_l]];
            bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask_l]];
            bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask_l]];
            bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask_l]];
            bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask_l]];
            da += 8;
            gx0 += 16;
            gx1 += 16;
            gx2 += 16;
            gx3 += 16;
            sx += 8;
        }
    }

    private void draw_sh(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_h(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            int p;
            bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 1 & 2047)]];
            bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 3 & 2047)]];
            bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 5 & 2047)]];
            bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 7 & 2047)]];
            bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 9 & 2047)]];
            bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 11 & 2047)]];
            bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 13 & 2047)]];
            bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 15 & 2047)]];
            da += 8;
            gx0 += 16;
            sx += 8;
        }
    }

    private void draw_ts(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_t(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
            bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
            bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
            bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
            bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
            bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
            bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
            bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
            da += 8;
            sx += 8;
        }
    }

    private void draw_ets(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_et(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16g8[m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                bitmap[da + 1] = (p = palet16g8[m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                bitmap[da + 2] = (p = palet16g8[m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                bitmap[da + 3] = (p = palet16g8[m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                bitmap[da + 4] = (p = palet16g8[m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                bitmap[da + 5] = (p = palet16g8[m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                bitmap[da + 6] = (p = palet16g8[m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                bitmap[da + 7] = (p = palet16g8[m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                da += 8;
                gx0 += 16;
                sx += 8;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                    bitmap[da + 1] = (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                    bitmap[da + 2] = (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                    bitmap[da + 3] = (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                    bitmap[da + 4] = (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                    bitmap[da + 5] = (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                    bitmap[da + 6] = (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                    bitmap[da + 7] = (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    sx += 8;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                        bitmap[da + 1] = (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                        bitmap[da + 2] = (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                        bitmap[da + 3] = (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                        bitmap[da + 4] = (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                        bitmap[da + 5] = (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                        bitmap[da + 6] = (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                        bitmap[da + 7] = (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        sx += 8;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                        bitmap[da + 1] = (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                        bitmap[da + 2] = (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                        bitmap[da + 3] = (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                        bitmap[da + 4] = (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                        bitmap[da + 5] = (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                        bitmap[da + 6] = (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                        bitmap[da + 7] = (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                        sx += 8;
                    }
                }
            }
        }
    }

    private void draw_fts(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_ft(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                bitmap[da + 1] = (p = palet16g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                bitmap[da + 2] = (p = palet16g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                bitmap[da + 3] = (p = palet16g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                bitmap[da + 4] = (p = palet16g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                bitmap[da + 5] = (p = palet16g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                bitmap[da + 6] = (p = palet16g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                bitmap[da + 7] = (p = palet16g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                sx += 8;
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                bitmap[da + 1] = (p = palet16g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                bitmap[da + 2] = (p = palet16g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                bitmap[da + 3] = (p = palet16g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                bitmap[da + 4] = (p = palet16g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                bitmap[da + 5] = (p = palet16g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                bitmap[da + 6] = (p = palet16g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                bitmap[da + 7] = (p = palet16g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
                sx += 8;
            }
        }
    }

    private void draw_gts(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_gt(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = (m[gy3 + (gx3 + 1 & 1023)] << 12) + (m[gy2 + (gx2 + 1 & 1023)] << 8) + (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
            bitmap[da + 1] = (p = (m[gy3 + (gx3 + 3 & 1023)] << 12) + (m[gy2 + (gx2 + 3 & 1023)] << 8) + (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
            bitmap[da + 2] = (p = (m[gy3 + (gx3 + 5 & 1023)] << 12) + (m[gy2 + (gx2 + 5 & 1023)] << 8) + (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
            bitmap[da + 3] = (p = (m[gy3 + (gx3 + 7 & 1023)] << 12) + (m[gy2 + (gx2 + 7 & 1023)] << 8) + (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
            bitmap[da + 4] = (p = (m[gy3 + (gx3 + 9 & 1023)] << 12) + (m[gy2 + (gx2 + 9 & 1023)] << 8) + (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
            bitmap[da + 5] = (p = (m[gy3 + (gx3 + 11 & 1023)] << 12) + (m[gy2 + (gx2 + 11 & 1023)] << 8) + (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
            bitmap[da + 6] = (p = (m[gy3 + (gx3 + 13 & 1023)] << 12) + (m[gy2 + (gx2 + 13 & 1023)] << 8) + (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
            bitmap[da + 7] = (p = (m[gy3 + (gx3 + 15 & 1023)] << 12) + (m[gy2 + (gx2 + 15 & 1023)] << 8) + (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
            da += 8;
            gx0 += 16;
            gx1 += 16;
            gx2 += 16;
            gx3 += 16;
            sx += 8;
        }
    }

    private void draw_hts(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_ht(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = palet16g8[m[gy0 + (gx0 + 1 & 2047)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
            bitmap[da + 1] = (p = palet16g8[m[gy0 + (gx0 + 3 & 2047)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
            bitmap[da + 2] = (p = palet16g8[m[gy0 + (gx0 + 5 & 2047)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
            bitmap[da + 3] = (p = palet16g8[m[gy0 + (gx0 + 7 & 2047)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
            bitmap[da + 4] = (p = palet16g8[m[gy0 + (gx0 + 9 & 2047)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
            bitmap[da + 5] = (p = palet16g8[m[gy0 + (gx0 + 11 & 2047)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
            bitmap[da + 6] = (p = palet16g8[m[gy0 + (gx0 + 13 & 2047)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
            bitmap[da + 7] = (p = palet16g8[m[gy0 + (gx0 + 15 & 2047)]]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
            da += 8;
            gx0 += 16;
            sx += 8;
        }
    }

    private void draw_tes(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_te(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                da += 8;
                gx0 += 16;
                sx += 8;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                    bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                    bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                    bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                    bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                    bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                    bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                    bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    sx += 8;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                        bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                        bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                        bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                        bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                        bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                        bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                        bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        sx += 8;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                        bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                        bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                        bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                        bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                        bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                        bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                        bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                        sx += 8;
                    }
                }
            }
        }
    }

    private void draw_tfs(int src, int dst) {
        draw_sprite_buffer(src);
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            switch (r3 & 32512) {
            case 7680:
                g2 = prw_g2[t | 15];
                g3 = g2 ^ 1;
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10),
                gx2 = crtc.g_scroll_x[g2] << 1;
                int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10),
                gx3 = crtc.g_scroll_x[g3] << 1;
                break;
            case 7936:
                break;
            default:
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                    bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                    bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                    bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                    bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                    bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                    bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                    bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    sx += 8;
                }
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            switch (r3 & 32512) {
            case 7680:
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p, q;
                    if ((p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0) {
                        bitmap[da] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]]) != 0) {
                            bitmap[da] = palet_conv[p];
                        } else {
                            bitmap[da] = palet_conv[s_buf[sx]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da] = palet_conv[p];
                        } else {
                            bitmap[da] = palet_conv[s_buf[sx]];
                        }
                    }
                    if ((p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0) {
                        bitmap[da + 1] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]]) != 0) {
                            bitmap[da + 1] = palet_conv[p];
                        } else {
                            bitmap[da + 1] = palet_conv[s_buf[sx + 1]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 1] = palet_conv[p];
                        } else {
                            bitmap[da + 1] = palet_conv[s_buf[sx + 1]];
                        }
                    }
                    if ((p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0) {
                        bitmap[da + 2] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]]) != 0) {
                            bitmap[da + 2] = palet_conv[p];
                        } else {
                            bitmap[da + 2] = palet_conv[s_buf[sx + 2]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 2] = palet_conv[p];
                        } else {
                            bitmap[da + 2] = palet_conv[s_buf[sx + 2]];
                        }
                    }
                    if ((p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0) {
                        bitmap[da + 3] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]]) != 0) {
                            bitmap[da + 3] = palet_conv[p];
                        } else {
                            bitmap[da + 3] = palet_conv[s_buf[sx + 3]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 3] = palet_conv[p];
                        } else {
                            bitmap[da + 3] = palet_conv[s_buf[sx + 3]];
                        }
                    }
                    if ((p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0) {
                        bitmap[da + 4] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]]) != 0) {
                            bitmap[da + 4] = palet_conv[p];
                        } else {
                            bitmap[da + 4] = palet_conv[s_buf[sx + 4]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 4] = palet_conv[p];
                        } else {
                            bitmap[da + 4] = palet_conv[s_buf[sx + 4]];
                        }
                    }
                    if ((p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0) {
                        bitmap[da + 5] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]]) != 0) {
                            bitmap[da + 5] = palet_conv[p];
                        } else {
                            bitmap[da + 5] = palet_conv[s_buf[sx + 5]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 5] = palet_conv[p];
                        } else {
                            bitmap[da + 5] = palet_conv[s_buf[sx + 5]];
                        }
                    }
                    if ((p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0) {
                        bitmap[da + 6] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]]) != 0) {
                            bitmap[da + 6] = palet_conv[p];
                        } else {
                            bitmap[da + 6] = palet_conv[s_buf[sx + 6]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 6] = palet_conv[p];
                        } else {
                            bitmap[da + 6] = palet_conv[s_buf[sx + 6]];
                        }
                    }
                    if ((p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0) {
                        bitmap[da + 7] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]]) != 0) {
                            bitmap[da + 7] = palet_conv[p];
                        } else {
                            bitmap[da + 7] = palet_conv[s_buf[sx + 7]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 7] = palet_conv[p];
                        } else {
                            bitmap[da + 7] = palet_conv[s_buf[sx + 7]];
                        }
                    }
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    gx2 += 16;
                    gx3 += 16;
                    sx += 8;
                }
                break;
            case 7936:
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p, q;
                    if ((p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0) {
                        bitmap[da] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]]) != 0) {
                            bitmap[da] = palet_conv[p];
                        } else {
                            bitmap[da] = palet_conv[s_buf[sx]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = s_buf[sx];
                        bitmap[da] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0) {
                        bitmap[da + 1] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]]) != 0) {
                            bitmap[da + 1] = palet_conv[p];
                        } else {
                            bitmap[da + 1] = palet_conv[s_buf[sx + 1]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = s_buf[sx + 1];
                        bitmap[da + 1] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0) {
                        bitmap[da + 2] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]]) != 0) {
                            bitmap[da + 2] = palet_conv[p];
                        } else {
                            bitmap[da + 2] = palet_conv[s_buf[sx + 2]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = s_buf[sx + 2];
                        bitmap[da + 2] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0) {
                        bitmap[da + 3] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]]) != 0) {
                            bitmap[da + 3] = palet_conv[p];
                        } else {
                            bitmap[da + 3] = palet_conv[s_buf[sx + 3]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = s_buf[sx + 3];
                        bitmap[da + 3] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0) {
                        bitmap[da + 4] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]]) != 0) {
                            bitmap[da + 4] = palet_conv[p];
                        } else {
                            bitmap[da + 4] = palet_conv[s_buf[sx + 4]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = s_buf[sx + 4];
                        bitmap[da + 4] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0) {
                        bitmap[da + 5] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]]) != 0) {
                            bitmap[da + 5] = palet_conv[p];
                        } else {
                            bitmap[da + 5] = palet_conv[s_buf[sx + 5]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = s_buf[sx + 5];
                        bitmap[da + 5] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0) {
                        bitmap[da + 6] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]]) != 0) {
                            bitmap[da + 6] = palet_conv[p];
                        } else {
                            bitmap[da + 6] = palet_conv[s_buf[sx + 6]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = s_buf[sx + 6];
                        bitmap[da + 6] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0) {
                        bitmap[da + 7] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]]) != 0) {
                            bitmap[da + 7] = palet_conv[p];
                        } else {
                            bitmap[da + 7] = palet_conv[s_buf[sx + 7]];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = s_buf[sx + 7];
                        bitmap[da + 7] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    gx2 += 16;
                    gx3 += 16;
                    sx += 8;
                }
                break;
            default:
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
                    bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
                    bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
                    bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
                    bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
                    bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
                    bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
                    bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    gx2 += 16;
                    gx3 += 16;
                    sx += 8;
                }
            }
        }
    }

    private void draw_tgs(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_tg(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 1 & 1023)] << 12) + (m[gy2 + (gx2 + 1 & 1023)] << 8) + (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet_conv[s_buf[sx]];
            bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 3 & 1023)] << 12) + (m[gy2 + (gx2 + 3 & 1023)] << 8) + (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet_conv[s_buf[sx + 1]];
            bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 5 & 1023)] << 12) + (m[gy2 + (gx2 + 5 & 1023)] << 8) + (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet_conv[s_buf[sx + 2]];
            bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 7 & 1023)] << 12) + (m[gy2 + (gx2 + 7 & 1023)] << 8) + (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet_conv[s_buf[sx + 3]];
            bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 9 & 1023)] << 12) + (m[gy2 + (gx2 + 9 & 1023)] << 8) + (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet_conv[s_buf[sx + 4]];
            bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 11 & 1023)] << 12) + (m[gy2 + (gx2 + 11 & 1023)] << 8) + (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet_conv[s_buf[sx + 5]];
            bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 13 & 1023)] << 12) + (m[gy2 + (gx2 + 13 & 1023)] << 8) + (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet_conv[s_buf[sx + 6]];
            bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 15 & 1023)] << 12) + (m[gy2 + (gx2 + 15 & 1023)] << 8) + (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet_conv[s_buf[sx + 7]];
            da += 8;
            gx0 += 16;
            gx1 += 16;
            gx2 += 16;
            gx3 += 16;
            sx += 8;
        }
    }

    private void draw_ths(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_th(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 1 & 2047)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx]];
            bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 3 & 2047)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 1]];
            bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 5 & 2047)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 2]];
            bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 7 & 2047)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 3]];
            bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 9 & 2047)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 4]];
            bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 11 & 2047)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 5]];
            bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 13 & 2047)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 6]];
            bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 15 & 2047)]]) != 0 ? palet_conv[p] : palet_conv[s_buf[sx + 7]];
            da += 8;
            gx0 += 16;
            sx += 8;
        }
    }

    private void draw_tse(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_te(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 1 & 1023)]];
                bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 3 & 1023)]];
                bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 5 & 1023)]];
                bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 7 & 1023)]];
                bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 9 & 1023)]];
                bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 11 & 1023)]];
                bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 13 & 1023)]];
                bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                sx += 8;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]];
                    bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]];
                    bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]];
                    bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]];
                    bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]];
                    bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]];
                    bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]];
                    bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    sx += 8;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]];
                        bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]];
                        bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]];
                        bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]];
                        bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]];
                        bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]];
                        bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]];
                        bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        sx += 8;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]];
                        bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]];
                        bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]];
                        bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]];
                        bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]];
                        bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]];
                        bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]];
                        bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                        sx += 8;
                    }
                }
            }
        }
    }

    private void draw_tsf(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_tf(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]];
                bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]];
                bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]];
                bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]];
                bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]];
                bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]];
                bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]];
                bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                sx += 8;
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]];
                bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]];
                bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]];
                bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]];
                bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]];
                bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]];
                bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]];
                bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
                sx += 8;
            }
        }
    }

    private void draw_tsg(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_tg(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask_l]];
            bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask_l]];
            bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask_l]];
            bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask_l]];
            bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask_l]];
            bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask_l]];
            bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask_l]];
            bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask_l]];
            da += 8;
            gx0 += 16;
            gx1 += 16;
            gx2 += 16;
            gx3 += 16;
            sx += 8;
        }
    }

    private void draw_tsh(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_th(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 1 & 2047)]];
            bitmap[da + 1] = (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 3 & 2047)]];
            bitmap[da + 2] = (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 5 & 2047)]];
            bitmap[da + 3] = (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 7 & 2047)]];
            bitmap[da + 4] = (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 9 & 2047)]];
            bitmap[da + 5] = (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 11 & 2047)]];
            bitmap[da + 6] = (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 13 & 2047)]];
            bitmap[da + 7] = (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 15 & 2047)]];
            da += 8;
            gx0 += 16;
            sx += 8;
        }
    }

    private void draw_st(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_t(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
            bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
            bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
            bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
            bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
            bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
            bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
            bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
            da += 8;
            sx += 8;
        }
    }

    private void draw_est(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_et(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16g8[m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                bitmap[da + 1] = (p = palet16g8[m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                bitmap[da + 2] = (p = palet16g8[m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                bitmap[da + 3] = (p = palet16g8[m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                bitmap[da + 4] = (p = palet16g8[m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                bitmap[da + 5] = (p = palet16g8[m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                bitmap[da + 6] = (p = palet16g8[m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                bitmap[da + 7] = (p = palet16g8[m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                da += 8;
                gx0 += 16;
                sx += 8;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                    bitmap[da + 1] = (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                    bitmap[da + 2] = (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                    bitmap[da + 3] = (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                    bitmap[da + 4] = (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                    bitmap[da + 5] = (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                    bitmap[da + 6] = (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                    bitmap[da + 7] = (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    sx += 8;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                        bitmap[da + 1] = (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                        bitmap[da + 2] = (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                        bitmap[da + 3] = (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                        bitmap[da + 4] = (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                        bitmap[da + 5] = (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                        bitmap[da + 6] = (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                        bitmap[da + 7] = (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        sx += 8;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                        bitmap[da + 1] = (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                        bitmap[da + 2] = (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                        bitmap[da + 3] = (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                        bitmap[da + 4] = (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                        bitmap[da + 5] = (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                        bitmap[da + 6] = (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                        bitmap[da + 7] = (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                        sx += 8;
                    }
                }
            }
        }
    }

    private void draw_fst(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_ft(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                bitmap[da + 1] = (p = palet16g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                bitmap[da + 2] = (p = palet16g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                bitmap[da + 3] = (p = palet16g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                bitmap[da + 4] = (p = palet16g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                bitmap[da + 5] = (p = palet16g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                bitmap[da + 6] = (p = palet16g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                bitmap[da + 7] = (p = palet16g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                sx += 8;
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = palet16g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                bitmap[da + 1] = (p = palet16g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                bitmap[da + 2] = (p = palet16g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                bitmap[da + 3] = (p = palet16g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                bitmap[da + 4] = (p = palet16g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                bitmap[da + 5] = (p = palet16g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                bitmap[da + 6] = (p = palet16g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                bitmap[da + 7] = (p = palet16g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
                sx += 8;
            }
        }
    }

    private void draw_gst(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_gt(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = (m[gy3 + (gx3 + 1 & 1023)] << 12) + (m[gy2 + (gx2 + 1 & 1023)] << 8) + (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
            bitmap[da + 1] = (p = (m[gy3 + (gx3 + 3 & 1023)] << 12) + (m[gy2 + (gx2 + 3 & 1023)] << 8) + (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
            bitmap[da + 2] = (p = (m[gy3 + (gx3 + 5 & 1023)] << 12) + (m[gy2 + (gx2 + 5 & 1023)] << 8) + (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
            bitmap[da + 3] = (p = (m[gy3 + (gx3 + 7 & 1023)] << 12) + (m[gy2 + (gx2 + 7 & 1023)] << 8) + (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
            bitmap[da + 4] = (p = (m[gy3 + (gx3 + 9 & 1023)] << 12) + (m[gy2 + (gx2 + 9 & 1023)] << 8) + (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
            bitmap[da + 5] = (p = (m[gy3 + (gx3 + 11 & 1023)] << 12) + (m[gy2 + (gx2 + 11 & 1023)] << 8) + (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
            bitmap[da + 6] = (p = (m[gy3 + (gx3 + 13 & 1023)] << 12) + (m[gy2 + (gx2 + 13 & 1023)] << 8) + (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
            bitmap[da + 7] = (p = (m[gy3 + (gx3 + 15 & 1023)] << 12) + (m[gy2 + (gx2 + 15 & 1023)] << 8) + (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
            da += 8;
            gx0 += 16;
            gx1 += 16;
            gx2 += 16;
            gx3 += 16;
            sx += 8;
        }
    }

    private void draw_hst(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_ht(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = palet16g8[m[gy0 + (gx0 + 1 & 2047)]]) != 0 ? palet_conv[p] : (p = s_buf[sx]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
            bitmap[da + 1] = (p = palet16g8[m[gy0 + (gx0 + 3 & 2047)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
            bitmap[da + 2] = (p = palet16g8[m[gy0 + (gx0 + 5 & 2047)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
            bitmap[da + 3] = (p = palet16g8[m[gy0 + (gx0 + 7 & 2047)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
            bitmap[da + 4] = (p = palet16g8[m[gy0 + (gx0 + 9 & 2047)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
            bitmap[da + 5] = (p = palet16g8[m[gy0 + (gx0 + 11 & 2047)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
            bitmap[da + 6] = (p = palet16g8[m[gy0 + (gx0 + 13 & 2047)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
            bitmap[da + 7] = (p = palet16g8[m[gy0 + (gx0 + 15 & 2047)]]) != 0 ? palet_conv[p] : (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
            da += 8;
            gx0 += 16;
            sx += 8;
        }
    }

    private void draw_set(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_et(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                da += 8;
                gx0 += 16;
                sx += 8;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                    bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                    bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                    bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                    bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                    bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                    bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                    bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    sx += 8;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                        bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                        bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                        bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                        bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                        bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                        bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                        bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        sx += 8;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                        bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                        bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                        bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                        bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                        bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                        bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                        bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                        sx += 8;
                    }
                }
            }
        }
    }

    private void draw_sft(int src, int dst) {
        draw_sprite_buffer(src);
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            switch (r3 & 32512) {
            case 7680:
                g2 = prw_g2[t | 15];
                g3 = g2 ^ 1;
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10),
                gx2 = crtc.g_scroll_x[g2] << 1;
                int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10),
                gx3 = crtc.g_scroll_x[g3] << 1;
                break;
            case 7936:
                break;
            default:
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                    bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                    bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                    bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                    bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                    bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                    bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                    bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    sx += 8;
                }
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            switch (r3 & 32512) {
            case 7680:
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p, q;
                    if ((p = s_buf[sx]) != 0) {
                        bitmap[da] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]]) != 0) {
                            bitmap[da] = palet_conv[p];
                        } else {
                            bitmap[da] = palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da] = palet_conv[p];
                        } else {
                            bitmap[da] = palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                        }
                    }
                    if ((p = s_buf[sx + 1]) != 0) {
                        bitmap[da + 1] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]]) != 0) {
                            bitmap[da + 1] = palet_conv[p];
                        } else {
                            bitmap[da + 1] = palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 1] = palet_conv[p];
                        } else {
                            bitmap[da + 1] = palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                        }
                    }
                    if ((p = s_buf[sx + 2]) != 0) {
                        bitmap[da + 2] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]]) != 0) {
                            bitmap[da + 2] = palet_conv[p];
                        } else {
                            bitmap[da + 2] = palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 2] = palet_conv[p];
                        } else {
                            bitmap[da + 2] = palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                        }
                    }
                    if ((p = s_buf[sx + 3]) != 0) {
                        bitmap[da + 3] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]]) != 0) {
                            bitmap[da + 3] = palet_conv[p];
                        } else {
                            bitmap[da + 3] = palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 3] = palet_conv[p];
                        } else {
                            bitmap[da + 3] = palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                        }
                    }
                    if ((p = s_buf[sx + 4]) != 0) {
                        bitmap[da + 4] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]]) != 0) {
                            bitmap[da + 4] = palet_conv[p];
                        } else {
                            bitmap[da + 4] = palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 4] = palet_conv[p];
                        } else {
                            bitmap[da + 4] = palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                        }
                    }
                    if ((p = s_buf[sx + 5]) != 0) {
                        bitmap[da + 5] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]]) != 0) {
                            bitmap[da + 5] = palet_conv[p];
                        } else {
                            bitmap[da + 5] = palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 5] = palet_conv[p];
                        } else {
                            bitmap[da + 5] = palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                        }
                    }
                    if ((p = s_buf[sx + 6]) != 0) {
                        bitmap[da + 6] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]]) != 0) {
                            bitmap[da + 6] = palet_conv[p];
                        } else {
                            bitmap[da + 6] = palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 6] = palet_conv[p];
                        } else {
                            bitmap[da + 6] = palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                        }
                    }
                    if ((p = s_buf[sx + 7]) != 0) {
                        bitmap[da + 7] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]]) != 0) {
                            bitmap[da + 7] = palet_conv[p];
                        } else {
                            bitmap[da + 7] = palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)] | 1];
                        if ((p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)) != 0) {
                            bitmap[da + 7] = palet_conv[p];
                        } else {
                            bitmap[da + 7] = palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                        }
                    }
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    gx2 += 16;
                    gx3 += 16;
                    sx += 8;
                }
                break;
            case 7936:
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p, q;
                    if ((p = s_buf[sx]) != 0) {
                        bitmap[da] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]]) != 0) {
                            bitmap[da] = palet_conv[p];
                        } else {
                            bitmap[da] = palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                        bitmap[da] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = s_buf[sx + 1]) != 0) {
                        bitmap[da + 1] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]]) != 0) {
                            bitmap[da + 1] = palet_conv[p];
                        } else {
                            bitmap[da + 1] = palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                        bitmap[da + 1] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = s_buf[sx + 2]) != 0) {
                        bitmap[da + 2] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]]) != 0) {
                            bitmap[da + 2] = palet_conv[p];
                        } else {
                            bitmap[da + 2] = palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                        bitmap[da + 2] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = s_buf[sx + 3]) != 0) {
                        bitmap[da + 3] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]]) != 0) {
                            bitmap[da + 3] = palet_conv[p];
                        } else {
                            bitmap[da + 3] = palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                        bitmap[da + 3] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = s_buf[sx + 4]) != 0) {
                        bitmap[da + 4] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]]) != 0) {
                            bitmap[da + 4] = palet_conv[p];
                        } else {
                            bitmap[da + 4] = palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                        bitmap[da + 4] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = s_buf[sx + 5]) != 0) {
                        bitmap[da + 5] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]]) != 0) {
                            bitmap[da + 5] = palet_conv[p];
                        } else {
                            bitmap[da + 5] = palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                        bitmap[da + 5] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = s_buf[sx + 6]) != 0) {
                        bitmap[da + 6] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]]) != 0) {
                            bitmap[da + 6] = palet_conv[p];
                        } else {
                            bitmap[da + 6] = palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                        bitmap[da + 6] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    if ((p = s_buf[sx + 7]) != 0) {
                        bitmap[da + 7] = palet_conv[p];
                    } else if (((p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) & 1) == 0) {
                        if ((p = palet16g8[p != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]]) != 0) {
                            bitmap[da + 7] = palet_conv[p];
                        } else {
                            bitmap[da + 7] = palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                        }
                    } else {
                        p = palet16g8[p & -2];
                        q = palet16g8[(m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)] | 1];
                        p = (((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1);
                        q = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                        bitmap[da + 7] = palet_conv[(((p & 63550) + (q & 63550) & 127100) + ((p & 1984) + (q & 1984) & 3968) >> 1) + (q & 1)];
                    }
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    gx2 += 16;
                    gx3 += 16;
                    sx += 8;
                }
                break;
            default:
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
                    bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
                    bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
                    bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
                    bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
                    bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
                    bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
                    bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    gx2 += 16;
                    gx3 += 16;
                    sx += 8;
                }
            }
        }
    }

    private void draw_sgt(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_gt(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = s_buf[sx] & 65534) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 1 & 1023)] << 12) + (m[gy2 + (gx2 + 1 & 1023)] << 8) + (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
            bitmap[da + 1] = (p = s_buf[sx + 1] & 65534) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 3 & 1023)] << 12) + (m[gy2 + (gx2 + 3 & 1023)] << 8) + (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
            bitmap[da + 2] = (p = s_buf[sx + 2] & 65534) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 5 & 1023)] << 12) + (m[gy2 + (gx2 + 5 & 1023)] << 8) + (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
            bitmap[da + 3] = (p = s_buf[sx + 3] & 65534) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 7 & 1023)] << 12) + (m[gy2 + (gx2 + 7 & 1023)] << 8) + (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
            bitmap[da + 4] = (p = s_buf[sx + 4] & 65534) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 9 & 1023)] << 12) + (m[gy2 + (gx2 + 9 & 1023)] << 8) + (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
            bitmap[da + 5] = (p = s_buf[sx + 5] & 65534) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 11 & 1023)] << 12) + (m[gy2 + (gx2 + 11 & 1023)] << 8) + (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
            bitmap[da + 6] = (p = s_buf[sx + 6] & 65534) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 13 & 1023)] << 12) + (m[gy2 + (gx2 + 13 & 1023)] << 8) + (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
            bitmap[da + 7] = (p = s_buf[sx + 7] & 65534) != 0 ? palet_conv[p] : (p = (m[gy3 + (gx3 + 15 & 1023)] << 12) + (m[gy2 + (gx2 + 15 & 1023)] << 8) + (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask) != 0 ? palet_conv[palet8g16h[p >> 8 & 255] + palet8g16l[p & 255]] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
            da += 8;
            gx0 += 16;
            gx1 += 16;
            gx2 += 16;
            gx3 += 16;
            sx += 8;
        }
    }

    private void draw_sht(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_ht(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 1 & 2047)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)];
            bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 3 & 2047)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)];
            bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 5 & 2047)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)];
            bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 7 & 2047)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)];
            bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 9 & 2047)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)];
            bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 11 & 2047)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)];
            bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 13 & 2047)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)];
            bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16g8[m[gy0 + (gx0 + 15 & 2047)]]) != 0 ? palet_conv[p] : palet32ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)];
            da += 8;
            gx0 += 16;
            sx += 8;
        }
    }

    private void draw_ste(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_te(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        if ((g1 = prw_g1[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 1 & 1023)]];
                bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 3 & 1023)]];
                bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 5 & 1023)]];
                bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 7 & 1023)]];
                bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 9 & 1023)]];
                bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 11 & 1023)]];
                bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 13 & 1023)]];
                bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                sx += 8;
            }
        } else {
            int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
            if ((g2 = prw_g2[t]) < 0) {
                while (da < db) {
                    p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                    p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                    p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                    p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                    tx = tx + 1 & 127;
                    int p;
                    bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 1 & 1023)]];
                    bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 3 & 1023)]];
                    bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 5 & 1023)]];
                    bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 7 & 1023)]];
                    bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 9 & 1023)]];
                    bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 11 & 1023)]];
                    bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 13 & 1023)]];
                    bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : m[gy1 + (gx1 + 15 & 1023)]];
                    da += 8;
                    gx0 += 16;
                    gx1 += 16;
                    sx += 8;
                }
            } else {
                int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
                if ((g3 = prw_g3[t]) < 0) {
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 1 & 1023)]];
                        bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 3 & 1023)]];
                        bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 5 & 1023)]];
                        bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 7 & 1023)]];
                        bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 9 & 1023)]];
                        bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 11 & 1023)]];
                        bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 13 & 1023)]];
                        bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : m[gy2 + (gx2 + 15 & 1023)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        sx += 8;
                    }
                } else {
                    int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
                    while (da < db) {
                        p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                        p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                        p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                        p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                        tx = tx + 1 & 127;
                        int p;
                        bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 1 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 1 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 1 & 1023)]];
                        bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 3 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 3 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 3 & 1023)]];
                        bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 5 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 5 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 5 & 1023)]];
                        bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 7 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 7 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 7 & 1023)]];
                        bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 9 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 9 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 9 & 1023)]];
                        bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 11 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 11 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 11 & 1023)]];
                        bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 13 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 13 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 13 & 1023)]];
                        bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (p = m[gy1 + (gx1 + 15 & 1023)]) != 0 ? p : (p = m[gy2 + (gx2 + 15 & 1023)]) != 0 ? p : m[gy3 + (gx3 + 15 & 1023)]];
                        da += 8;
                        gx0 += 16;
                        gx1 += 16;
                        gx2 += 16;
                        gx3 += 16;
                        sx += 8;
                    }
                }
            }
        }
    }

    private void draw_stf(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_tf(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int t = ((r2 & 255) << 4) + (r3 & 15);
        int g0 = prw_g0[t], g1 = g0 ^ 1, g2, g3;
        int gy0 = 12582912 + (g0 << 19) + ((crtc.g_scroll_y[g0] + src & 511) << 10), gx0 = crtc.g_scroll_x[g0] << 1;
        int gy1 = 12582912 + (g1 << 19) + ((crtc.g_scroll_y[g1] + src & 511) << 10), gx1 = crtc.g_scroll_x[g1] << 1;
        if ((g2 = prw_g2[t]) < 0) {
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]];
                bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]];
                bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]];
                bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]];
                bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]];
                bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]];
                bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]];
                bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                sx += 8;
            }
        } else {
            g3 = g2 ^ 1;
            int gy2 = 12582912 + (g2 << 19) + ((crtc.g_scroll_y[g2] + src & 511) << 10), gx2 = crtc.g_scroll_x[g2] << 1;
            int gy3 = 12582912 + (g3 << 19) + ((crtc.g_scroll_y[g3] + src & 511) << 10), gx3 = crtc.g_scroll_x[g3] << 1;
            while (da < db) {
                p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
                p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
                p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
                p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
                tx = tx + 1 & 127;
                int p;
                bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)]];
                bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)]];
                bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)]];
                bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)]];
                bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)]];
                bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)]];
                bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)]];
                bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[(p = (m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)]) != 0 ? p : (m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)]];
                da += 8;
                gx0 += 16;
                gx1 += 16;
                gx2 += 16;
                gx3 += 16;
                sx += 8;
            }
        }
    }

    private void draw_stg(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_tg(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int mask = 15 << (r2 << 2 & 12) & -(r3 & 1) | 15 << (r2 & 12) & -(r3 >> 1 & 1) | 15 << (r2 >> 2 & 12) & -(r3 >> 2 & 1) | 15 << (r2 >> 4 & 12) & -(r3 >> 3 & 1);
        int mask_h = mask >> 8, mask_l = mask & 255;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 511) << 10), gx0 = crtc.g_scroll_x[0] << 1;
        int gy1 = 13107200 + ((crtc.g_scroll_y[1] + src & 511) << 10), gx1 = crtc.g_scroll_x[1] << 1;
        int gy2 = 13631488 + ((crtc.g_scroll_y[2] + src & 511) << 10), gx2 = crtc.g_scroll_x[2] << 1;
        int gy3 = 14155776 + ((crtc.g_scroll_y[3] + src & 511) << 10), gx3 = crtc.g_scroll_x[3] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 1 & 1023)] << 4) + m[gy2 + (gx2 + 1 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 1 & 1023)] << 4) + m[gy0 + (gx0 + 1 & 1023)] & mask_l]];
            bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 3 & 1023)] << 4) + m[gy2 + (gx2 + 3 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 3 & 1023)] << 4) + m[gy0 + (gx0 + 3 & 1023)] & mask_l]];
            bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 5 & 1023)] << 4) + m[gy2 + (gx2 + 5 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 5 & 1023)] << 4) + m[gy0 + (gx0 + 5 & 1023)] & mask_l]];
            bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 7 & 1023)] << 4) + m[gy2 + (gx2 + 7 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 7 & 1023)] << 4) + m[gy0 + (gx0 + 7 & 1023)] & mask_l]];
            bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 9 & 1023)] << 4) + m[gy2 + (gx2 + 9 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 9 & 1023)] << 4) + m[gy0 + (gx0 + 9 & 1023)] & mask_l]];
            bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 11 & 1023)] << 4) + m[gy2 + (gx2 + 11 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 11 & 1023)] << 4) + m[gy0 + (gx0 + 11 & 1023)] & mask_l]];
            bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 13 & 1023)] << 4) + m[gy2 + (gx2 + 13 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 13 & 1023)] << 4) + m[gy0 + (gx0 + 13 & 1023)] & mask_l]];
            bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet_conv[palet8g16h[(m[gy3 + (gx3 + 15 & 1023)] << 4) + m[gy2 + (gx2 + 15 & 1023)] & mask_h] + palet8g16l[(m[gy1 + (gx1 + 15 & 1023)] << 4) + m[gy0 + (gx0 + 15 & 1023)] & mask_l]];
            da += 8;
            gx0 += 16;
            gx1 += 16;
            gx2 += 16;
            gx3 += 16;
            sx += 8;
        }
    }

    private void draw_sth(int src, int dst) {
        draw_sprite_buffer(src);
        if (!sprite_existence) {
            draw_th(src, dst);
            return;
        }
        int sx = 16;
        int da = iw * dst, db = da + dw;
        int ty = (crtc.r11 + src & 1023) << 7, tx = crtc.r10 >> 3, ts = crtc.r10 & 7, tt = 8 - ts;
        short p0 = (short) (m[14680064 + ty + tx] << ts);
        short p1 = (short) (m[14811136 + ty + tx] << ts);
        short p2 = (short) (m[14942208 + ty + tx] << ts);
        short p3 = (short) (m[15073280 + ty + tx] << ts);
        tx = tx + 1 & 127;
        int gy0 = 12582912 + ((crtc.g_scroll_y[0] + src & 1023) << 11), gx0 = crtc.g_scroll_x[0] << 1;
        while (da < db) {
            p0 = (short) ((p0 << tt) + (m[14680064 + ty + tx] & 255) << ts);
            p1 = (short) ((p1 << tt) + (m[14811136 + ty + tx] & 255) << ts);
            p2 = (short) ((p2 << tt) + (m[14942208 + ty + tx] & 255) << ts);
            p3 = (short) ((p3 << tt) + (m[15073280 + ty + tx] & 255) << ts);
            tx = tx + 1 & 127;
            int p;
            bitmap[da] = (p = s_buf[sx]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 12 & 8) + (p2 >> 13 & 4) + (p1 >> 14 & 2) + (p0 >> 15 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 1 & 2047)]];
            bitmap[da + 1] = (p = s_buf[sx + 1]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 11 & 8) + (p2 >> 12 & 4) + (p1 >> 13 & 2) + (p0 >> 14 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 3 & 2047)]];
            bitmap[da + 2] = (p = s_buf[sx + 2]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 10 & 8) + (p2 >> 11 & 4) + (p1 >> 12 & 2) + (p0 >> 13 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 5 & 2047)]];
            bitmap[da + 3] = (p = s_buf[sx + 3]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 9 & 8) + (p2 >> 10 & 4) + (p1 >> 11 & 2) + (p0 >> 12 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 7 & 2047)]];
            bitmap[da + 4] = (p = s_buf[sx + 4]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 8 & 8) + (p2 >> 9 & 4) + (p1 >> 10 & 2) + (p0 >> 11 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 9 & 2047)]];
            bitmap[da + 5] = (p = s_buf[sx + 5]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 7 & 8) + (p2 >> 8 & 4) + (p1 >> 9 & 2) + (p0 >> 10 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 11 & 2047)]];
            bitmap[da + 6] = (p = s_buf[sx + 6]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 6 & 8) + (p2 >> 7 & 4) + (p1 >> 8 & 2) + (p0 >> 9 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 13 & 2047)]];
            bitmap[da + 7] = (p = s_buf[sx + 7]) != 0 ? palet_conv[p] : (p = palet16ts[(p3 >> 5 & 8) + (p2 >> 6 & 4) + (p1 >> 7 & 2) + (p0 >> 8 & 1)]) != 0 ? palet_conv[p] : palet32g8[m[gy0 + (gx0 + 15 & 2047)]];
            da += 8;
            gx0 += 16;
            sx += 8;
        }
    }

    private void stretch11(int dst) {
        int da = iw * dst;
        switch (dw >> 3) {
        case 128:
            bitmap[da + 1407] = bitmap[da + 1023];
            bitmap[da + 1406] = bitmap[da + 1022];
            bitmap[da + 1404] = bitmap[da + 1405] = bitmap[da + 1021];
            bitmap[da + 1403] = bitmap[da + 1020];
            bitmap[da + 1402] = bitmap[da + 1019];
            bitmap[da + 1400] = bitmap[da + 1401] = bitmap[da + 1018];
            bitmap[da + 1399] = bitmap[da + 1017];
            bitmap[da + 1397] = bitmap[da + 1398] = bitmap[da + 1016];
        case 127:
            bitmap[da + 1396] = bitmap[da + 1015];
            bitmap[da + 1395] = bitmap[da + 1014];
            bitmap[da + 1393] = bitmap[da + 1394] = bitmap[da + 1013];
            bitmap[da + 1392] = bitmap[da + 1012];
            bitmap[da + 1391] = bitmap[da + 1011];
            bitmap[da + 1389] = bitmap[da + 1390] = bitmap[da + 1010];
            bitmap[da + 1388] = bitmap[da + 1009];
            bitmap[da + 1386] = bitmap[da + 1387] = bitmap[da + 1008];
        case 126:
            bitmap[da + 1385] = bitmap[da + 1007];
            bitmap[da + 1384] = bitmap[da + 1006];
            bitmap[da + 1382] = bitmap[da + 1383] = bitmap[da + 1005];
            bitmap[da + 1381] = bitmap[da + 1004];
            bitmap[da + 1380] = bitmap[da + 1003];
            bitmap[da + 1378] = bitmap[da + 1379] = bitmap[da + 1002];
            bitmap[da + 1377] = bitmap[da + 1001];
            bitmap[da + 1375] = bitmap[da + 1376] = bitmap[da + 1000];
        case 125:
            bitmap[da + 1374] = bitmap[da + 999];
            bitmap[da + 1373] = bitmap[da + 998];
            bitmap[da + 1371] = bitmap[da + 1372] = bitmap[da + 997];
            bitmap[da + 1370] = bitmap[da + 996];
            bitmap[da + 1369] = bitmap[da + 995];
            bitmap[da + 1367] = bitmap[da + 1368] = bitmap[da + 994];
            bitmap[da + 1366] = bitmap[da + 993];
            bitmap[da + 1364] = bitmap[da + 1365] = bitmap[da + 992];
        case 124:
            bitmap[da + 1363] = bitmap[da + 991];
            bitmap[da + 1362] = bitmap[da + 990];
            bitmap[da + 1360] = bitmap[da + 1361] = bitmap[da + 989];
            bitmap[da + 1359] = bitmap[da + 988];
            bitmap[da + 1358] = bitmap[da + 987];
            bitmap[da + 1356] = bitmap[da + 1357] = bitmap[da + 986];
            bitmap[da + 1355] = bitmap[da + 985];
            bitmap[da + 1353] = bitmap[da + 1354] = bitmap[da + 984];
        case 123:
            bitmap[da + 1352] = bitmap[da + 983];
            bitmap[da + 1351] = bitmap[da + 982];
            bitmap[da + 1349] = bitmap[da + 1350] = bitmap[da + 981];
            bitmap[da + 1348] = bitmap[da + 980];
            bitmap[da + 1347] = bitmap[da + 979];
            bitmap[da + 1345] = bitmap[da + 1346] = bitmap[da + 978];
            bitmap[da + 1344] = bitmap[da + 977];
            bitmap[da + 1342] = bitmap[da + 1343] = bitmap[da + 976];
        case 122:
            bitmap[da + 1341] = bitmap[da + 975];
            bitmap[da + 1340] = bitmap[da + 974];
            bitmap[da + 1338] = bitmap[da + 1339] = bitmap[da + 973];
            bitmap[da + 1337] = bitmap[da + 972];
            bitmap[da + 1336] = bitmap[da + 971];
            bitmap[da + 1334] = bitmap[da + 1335] = bitmap[da + 970];
            bitmap[da + 1333] = bitmap[da + 969];
            bitmap[da + 1331] = bitmap[da + 1332] = bitmap[da + 968];
        case 121:
            bitmap[da + 1330] = bitmap[da + 967];
            bitmap[da + 1329] = bitmap[da + 966];
            bitmap[da + 1327] = bitmap[da + 1328] = bitmap[da + 965];
            bitmap[da + 1326] = bitmap[da + 964];
            bitmap[da + 1325] = bitmap[da + 963];
            bitmap[da + 1323] = bitmap[da + 1324] = bitmap[da + 962];
            bitmap[da + 1322] = bitmap[da + 961];
            bitmap[da + 1320] = bitmap[da + 1321] = bitmap[da + 960];
        case 120:
            bitmap[da + 1319] = bitmap[da + 959];
            bitmap[da + 1318] = bitmap[da + 958];
            bitmap[da + 1316] = bitmap[da + 1317] = bitmap[da + 957];
            bitmap[da + 1315] = bitmap[da + 956];
            bitmap[da + 1314] = bitmap[da + 955];
            bitmap[da + 1312] = bitmap[da + 1313] = bitmap[da + 954];
            bitmap[da + 1311] = bitmap[da + 953];
            bitmap[da + 1309] = bitmap[da + 1310] = bitmap[da + 952];
        case 119:
            bitmap[da + 1308] = bitmap[da + 951];
            bitmap[da + 1307] = bitmap[da + 950];
            bitmap[da + 1305] = bitmap[da + 1306] = bitmap[da + 949];
            bitmap[da + 1304] = bitmap[da + 948];
            bitmap[da + 1303] = bitmap[da + 947];
            bitmap[da + 1301] = bitmap[da + 1302] = bitmap[da + 946];
            bitmap[da + 1300] = bitmap[da + 945];
            bitmap[da + 1298] = bitmap[da + 1299] = bitmap[da + 944];
        case 118:
            bitmap[da + 1297] = bitmap[da + 943];
            bitmap[da + 1296] = bitmap[da + 942];
            bitmap[da + 1294] = bitmap[da + 1295] = bitmap[da + 941];
            bitmap[da + 1293] = bitmap[da + 940];
            bitmap[da + 1292] = bitmap[da + 939];
            bitmap[da + 1290] = bitmap[da + 1291] = bitmap[da + 938];
            bitmap[da + 1289] = bitmap[da + 937];
            bitmap[da + 1287] = bitmap[da + 1288] = bitmap[da + 936];
        case 117:
            bitmap[da + 1286] = bitmap[da + 935];
            bitmap[da + 1285] = bitmap[da + 934];
            bitmap[da + 1283] = bitmap[da + 1284] = bitmap[da + 933];
            bitmap[da + 1282] = bitmap[da + 932];
            bitmap[da + 1281] = bitmap[da + 931];
            bitmap[da + 1279] = bitmap[da + 1280] = bitmap[da + 930];
            bitmap[da + 1278] = bitmap[da + 929];
            bitmap[da + 1276] = bitmap[da + 1277] = bitmap[da + 928];
        case 116:
            bitmap[da + 1275] = bitmap[da + 927];
            bitmap[da + 1274] = bitmap[da + 926];
            bitmap[da + 1272] = bitmap[da + 1273] = bitmap[da + 925];
            bitmap[da + 1271] = bitmap[da + 924];
            bitmap[da + 1270] = bitmap[da + 923];
            bitmap[da + 1268] = bitmap[da + 1269] = bitmap[da + 922];
            bitmap[da + 1267] = bitmap[da + 921];
            bitmap[da + 1265] = bitmap[da + 1266] = bitmap[da + 920];
        case 115:
            bitmap[da + 1264] = bitmap[da + 919];
            bitmap[da + 1263] = bitmap[da + 918];
            bitmap[da + 1261] = bitmap[da + 1262] = bitmap[da + 917];
            bitmap[da + 1260] = bitmap[da + 916];
            bitmap[da + 1259] = bitmap[da + 915];
            bitmap[da + 1257] = bitmap[da + 1258] = bitmap[da + 914];
            bitmap[da + 1256] = bitmap[da + 913];
            bitmap[da + 1254] = bitmap[da + 1255] = bitmap[da + 912];
        case 114:
            bitmap[da + 1253] = bitmap[da + 911];
            bitmap[da + 1252] = bitmap[da + 910];
            bitmap[da + 1250] = bitmap[da + 1251] = bitmap[da + 909];
            bitmap[da + 1249] = bitmap[da + 908];
            bitmap[da + 1248] = bitmap[da + 907];
            bitmap[da + 1246] = bitmap[da + 1247] = bitmap[da + 906];
            bitmap[da + 1245] = bitmap[da + 905];
            bitmap[da + 1243] = bitmap[da + 1244] = bitmap[da + 904];
        case 113:
            bitmap[da + 1242] = bitmap[da + 903];
            bitmap[da + 1241] = bitmap[da + 902];
            bitmap[da + 1239] = bitmap[da + 1240] = bitmap[da + 901];
            bitmap[da + 1238] = bitmap[da + 900];
            bitmap[da + 1237] = bitmap[da + 899];
            bitmap[da + 1235] = bitmap[da + 1236] = bitmap[da + 898];
            bitmap[da + 1234] = bitmap[da + 897];
            bitmap[da + 1232] = bitmap[da + 1233] = bitmap[da + 896];
        case 112:
            bitmap[da + 1231] = bitmap[da + 895];
            bitmap[da + 1230] = bitmap[da + 894];
            bitmap[da + 1228] = bitmap[da + 1229] = bitmap[da + 893];
            bitmap[da + 1227] = bitmap[da + 892];
            bitmap[da + 1226] = bitmap[da + 891];
            bitmap[da + 1224] = bitmap[da + 1225] = bitmap[da + 890];
            bitmap[da + 1223] = bitmap[da + 889];
            bitmap[da + 1221] = bitmap[da + 1222] = bitmap[da + 888];
        case 111:
            bitmap[da + 1220] = bitmap[da + 887];
            bitmap[da + 1219] = bitmap[da + 886];
            bitmap[da + 1217] = bitmap[da + 1218] = bitmap[da + 885];
            bitmap[da + 1216] = bitmap[da + 884];
            bitmap[da + 1215] = bitmap[da + 883];
            bitmap[da + 1213] = bitmap[da + 1214] = bitmap[da + 882];
            bitmap[da + 1212] = bitmap[da + 881];
            bitmap[da + 1210] = bitmap[da + 1211] = bitmap[da + 880];
        case 110:
            bitmap[da + 1209] = bitmap[da + 879];
            bitmap[da + 1208] = bitmap[da + 878];
            bitmap[da + 1206] = bitmap[da + 1207] = bitmap[da + 877];
            bitmap[da + 1205] = bitmap[da + 876];
            bitmap[da + 1204] = bitmap[da + 875];
            bitmap[da + 1202] = bitmap[da + 1203] = bitmap[da + 874];
            bitmap[da + 1201] = bitmap[da + 873];
            bitmap[da + 1199] = bitmap[da + 1200] = bitmap[da + 872];
        case 109:
            bitmap[da + 1198] = bitmap[da + 871];
            bitmap[da + 1197] = bitmap[da + 870];
            bitmap[da + 1195] = bitmap[da + 1196] = bitmap[da + 869];
            bitmap[da + 1194] = bitmap[da + 868];
            bitmap[da + 1193] = bitmap[da + 867];
            bitmap[da + 1191] = bitmap[da + 1192] = bitmap[da + 866];
            bitmap[da + 1190] = bitmap[da + 865];
            bitmap[da + 1188] = bitmap[da + 1189] = bitmap[da + 864];
        case 108:
            bitmap[da + 1187] = bitmap[da + 863];
            bitmap[da + 1186] = bitmap[da + 862];
            bitmap[da + 1184] = bitmap[da + 1185] = bitmap[da + 861];
            bitmap[da + 1183] = bitmap[da + 860];
            bitmap[da + 1182] = bitmap[da + 859];
            bitmap[da + 1180] = bitmap[da + 1181] = bitmap[da + 858];
            bitmap[da + 1179] = bitmap[da + 857];
            bitmap[da + 1177] = bitmap[da + 1178] = bitmap[da + 856];
        case 107:
            bitmap[da + 1176] = bitmap[da + 855];
            bitmap[da + 1175] = bitmap[da + 854];
            bitmap[da + 1173] = bitmap[da + 1174] = bitmap[da + 853];
            bitmap[da + 1172] = bitmap[da + 852];
            bitmap[da + 1171] = bitmap[da + 851];
            bitmap[da + 1169] = bitmap[da + 1170] = bitmap[da + 850];
            bitmap[da + 1168] = bitmap[da + 849];
            bitmap[da + 1166] = bitmap[da + 1167] = bitmap[da + 848];
        case 106:
            bitmap[da + 1165] = bitmap[da + 847];
            bitmap[da + 1164] = bitmap[da + 846];
            bitmap[da + 1162] = bitmap[da + 1163] = bitmap[da + 845];
            bitmap[da + 1161] = bitmap[da + 844];
            bitmap[da + 1160] = bitmap[da + 843];
            bitmap[da + 1158] = bitmap[da + 1159] = bitmap[da + 842];
            bitmap[da + 1157] = bitmap[da + 841];
            bitmap[da + 1155] = bitmap[da + 1156] = bitmap[da + 840];
        case 105:
            bitmap[da + 1154] = bitmap[da + 839];
            bitmap[da + 1153] = bitmap[da + 838];
            bitmap[da + 1151] = bitmap[da + 1152] = bitmap[da + 837];
            bitmap[da + 1150] = bitmap[da + 836];
            bitmap[da + 1149] = bitmap[da + 835];
            bitmap[da + 1147] = bitmap[da + 1148] = bitmap[da + 834];
            bitmap[da + 1146] = bitmap[da + 833];
            bitmap[da + 1144] = bitmap[da + 1145] = bitmap[da + 832];
        case 104:
            bitmap[da + 1143] = bitmap[da + 831];
            bitmap[da + 1142] = bitmap[da + 830];
            bitmap[da + 1140] = bitmap[da + 1141] = bitmap[da + 829];
            bitmap[da + 1139] = bitmap[da + 828];
            bitmap[da + 1138] = bitmap[da + 827];
            bitmap[da + 1136] = bitmap[da + 1137] = bitmap[da + 826];
            bitmap[da + 1135] = bitmap[da + 825];
            bitmap[da + 1133] = bitmap[da + 1134] = bitmap[da + 824];
        case 103:
            bitmap[da + 1132] = bitmap[da + 823];
            bitmap[da + 1131] = bitmap[da + 822];
            bitmap[da + 1129] = bitmap[da + 1130] = bitmap[da + 821];
            bitmap[da + 1128] = bitmap[da + 820];
            bitmap[da + 1127] = bitmap[da + 819];
            bitmap[da + 1125] = bitmap[da + 1126] = bitmap[da + 818];
            bitmap[da + 1124] = bitmap[da + 817];
            bitmap[da + 1122] = bitmap[da + 1123] = bitmap[da + 816];
        case 102:
            bitmap[da + 1121] = bitmap[da + 815];
            bitmap[da + 1120] = bitmap[da + 814];
            bitmap[da + 1118] = bitmap[da + 1119] = bitmap[da + 813];
            bitmap[da + 1117] = bitmap[da + 812];
            bitmap[da + 1116] = bitmap[da + 811];
            bitmap[da + 1114] = bitmap[da + 1115] = bitmap[da + 810];
            bitmap[da + 1113] = bitmap[da + 809];
            bitmap[da + 1111] = bitmap[da + 1112] = bitmap[da + 808];
        case 101:
            bitmap[da + 1110] = bitmap[da + 807];
            bitmap[da + 1109] = bitmap[da + 806];
            bitmap[da + 1107] = bitmap[da + 1108] = bitmap[da + 805];
            bitmap[da + 1106] = bitmap[da + 804];
            bitmap[da + 1105] = bitmap[da + 803];
            bitmap[da + 1103] = bitmap[da + 1104] = bitmap[da + 802];
            bitmap[da + 1102] = bitmap[da + 801];
            bitmap[da + 1100] = bitmap[da + 1101] = bitmap[da + 800];
        case 100:
            bitmap[da + 1099] = bitmap[da + 799];
            bitmap[da + 1098] = bitmap[da + 798];
            bitmap[da + 1096] = bitmap[da + 1097] = bitmap[da + 797];
            bitmap[da + 1095] = bitmap[da + 796];
            bitmap[da + 1094] = bitmap[da + 795];
            bitmap[da + 1092] = bitmap[da + 1093] = bitmap[da + 794];
            bitmap[da + 1091] = bitmap[da + 793];
            bitmap[da + 1089] = bitmap[da + 1090] = bitmap[da + 792];
        case 99:
            bitmap[da + 1088] = bitmap[da + 791];
            bitmap[da + 1087] = bitmap[da + 790];
            bitmap[da + 1085] = bitmap[da + 1086] = bitmap[da + 789];
            bitmap[da + 1084] = bitmap[da + 788];
            bitmap[da + 1083] = bitmap[da + 787];
            bitmap[da + 1081] = bitmap[da + 1082] = bitmap[da + 786];
            bitmap[da + 1080] = bitmap[da + 785];
            bitmap[da + 1078] = bitmap[da + 1079] = bitmap[da + 784];
        case 98:
            bitmap[da + 1077] = bitmap[da + 783];
            bitmap[da + 1076] = bitmap[da + 782];
            bitmap[da + 1074] = bitmap[da + 1075] = bitmap[da + 781];
            bitmap[da + 1073] = bitmap[da + 780];
            bitmap[da + 1072] = bitmap[da + 779];
            bitmap[da + 1070] = bitmap[da + 1071] = bitmap[da + 778];
            bitmap[da + 1069] = bitmap[da + 777];
            bitmap[da + 1067] = bitmap[da + 1068] = bitmap[da + 776];
        case 97:
            bitmap[da + 1066] = bitmap[da + 775];
            bitmap[da + 1065] = bitmap[da + 774];
            bitmap[da + 1063] = bitmap[da + 1064] = bitmap[da + 773];
            bitmap[da + 1062] = bitmap[da + 772];
            bitmap[da + 1061] = bitmap[da + 771];
            bitmap[da + 1059] = bitmap[da + 1060] = bitmap[da + 770];
            bitmap[da + 1058] = bitmap[da + 769];
            bitmap[da + 1056] = bitmap[da + 1057] = bitmap[da + 768];
        case 96:
            bitmap[da + 1055] = bitmap[da + 767];
            bitmap[da + 1054] = bitmap[da + 766];
            bitmap[da + 1052] = bitmap[da + 1053] = bitmap[da + 765];
            bitmap[da + 1051] = bitmap[da + 764];
            bitmap[da + 1050] = bitmap[da + 763];
            bitmap[da + 1048] = bitmap[da + 1049] = bitmap[da + 762];
            bitmap[da + 1047] = bitmap[da + 761];
            bitmap[da + 1045] = bitmap[da + 1046] = bitmap[da + 760];
        case 95:
            bitmap[da + 1044] = bitmap[da + 759];
            bitmap[da + 1043] = bitmap[da + 758];
            bitmap[da + 1041] = bitmap[da + 1042] = bitmap[da + 757];
            bitmap[da + 1040] = bitmap[da + 756];
            bitmap[da + 1039] = bitmap[da + 755];
            bitmap[da + 1037] = bitmap[da + 1038] = bitmap[da + 754];
            bitmap[da + 1036] = bitmap[da + 753];
            bitmap[da + 1034] = bitmap[da + 1035] = bitmap[da + 752];
        case 94:
            bitmap[da + 1033] = bitmap[da + 751];
            bitmap[da + 1032] = bitmap[da + 750];
            bitmap[da + 1030] = bitmap[da + 1031] = bitmap[da + 749];
            bitmap[da + 1029] = bitmap[da + 748];
            bitmap[da + 1028] = bitmap[da + 747];
            bitmap[da + 1026] = bitmap[da + 1027] = bitmap[da + 746];
            bitmap[da + 1025] = bitmap[da + 745];
            bitmap[da + 1023] = bitmap[da + 1024] = bitmap[da + 744];
        case 93:
            bitmap[da + 1022] = bitmap[da + 743];
            bitmap[da + 1021] = bitmap[da + 742];
            bitmap[da + 1019] = bitmap[da + 1020] = bitmap[da + 741];
            bitmap[da + 1018] = bitmap[da + 740];
            bitmap[da + 1017] = bitmap[da + 739];
            bitmap[da + 1015] = bitmap[da + 1016] = bitmap[da + 738];
            bitmap[da + 1014] = bitmap[da + 737];
            bitmap[da + 1012] = bitmap[da + 1013] = bitmap[da + 736];
        case 92:
            bitmap[da + 1011] = bitmap[da + 735];
            bitmap[da + 1010] = bitmap[da + 734];
            bitmap[da + 1008] = bitmap[da + 1009] = bitmap[da + 733];
            bitmap[da + 1007] = bitmap[da + 732];
            bitmap[da + 1006] = bitmap[da + 731];
            bitmap[da + 1004] = bitmap[da + 1005] = bitmap[da + 730];
            bitmap[da + 1003] = bitmap[da + 729];
            bitmap[da + 1001] = bitmap[da + 1002] = bitmap[da + 728];
        case 91:
            bitmap[da + 1000] = bitmap[da + 727];
            bitmap[da + 999] = bitmap[da + 726];
            bitmap[da + 997] = bitmap[da + 998] = bitmap[da + 725];
            bitmap[da + 996] = bitmap[da + 724];
            bitmap[da + 995] = bitmap[da + 723];
            bitmap[da + 993] = bitmap[da + 994] = bitmap[da + 722];
            bitmap[da + 992] = bitmap[da + 721];
            bitmap[da + 990] = bitmap[da + 991] = bitmap[da + 720];
        case 90:
            bitmap[da + 989] = bitmap[da + 719];
            bitmap[da + 988] = bitmap[da + 718];
            bitmap[da + 986] = bitmap[da + 987] = bitmap[da + 717];
            bitmap[da + 985] = bitmap[da + 716];
            bitmap[da + 984] = bitmap[da + 715];
            bitmap[da + 982] = bitmap[da + 983] = bitmap[da + 714];
            bitmap[da + 981] = bitmap[da + 713];
            bitmap[da + 979] = bitmap[da + 980] = bitmap[da + 712];
        case 89:
            bitmap[da + 978] = bitmap[da + 711];
            bitmap[da + 977] = bitmap[da + 710];
            bitmap[da + 975] = bitmap[da + 976] = bitmap[da + 709];
            bitmap[da + 974] = bitmap[da + 708];
            bitmap[da + 973] = bitmap[da + 707];
            bitmap[da + 971] = bitmap[da + 972] = bitmap[da + 706];
            bitmap[da + 970] = bitmap[da + 705];
            bitmap[da + 968] = bitmap[da + 969] = bitmap[da + 704];
        case 88:
            bitmap[da + 967] = bitmap[da + 703];
            bitmap[da + 966] = bitmap[da + 702];
            bitmap[da + 964] = bitmap[da + 965] = bitmap[da + 701];
            bitmap[da + 963] = bitmap[da + 700];
            bitmap[da + 962] = bitmap[da + 699];
            bitmap[da + 960] = bitmap[da + 961] = bitmap[da + 698];
            bitmap[da + 959] = bitmap[da + 697];
            bitmap[da + 957] = bitmap[da + 958] = bitmap[da + 696];
        case 87:
            bitmap[da + 956] = bitmap[da + 695];
            bitmap[da + 955] = bitmap[da + 694];
            bitmap[da + 953] = bitmap[da + 954] = bitmap[da + 693];
            bitmap[da + 952] = bitmap[da + 692];
            bitmap[da + 951] = bitmap[da + 691];
            bitmap[da + 949] = bitmap[da + 950] = bitmap[da + 690];
            bitmap[da + 948] = bitmap[da + 689];
            bitmap[da + 946] = bitmap[da + 947] = bitmap[da + 688];
        case 86:
            bitmap[da + 945] = bitmap[da + 687];
            bitmap[da + 944] = bitmap[da + 686];
            bitmap[da + 942] = bitmap[da + 943] = bitmap[da + 685];
            bitmap[da + 941] = bitmap[da + 684];
            bitmap[da + 940] = bitmap[da + 683];
            bitmap[da + 938] = bitmap[da + 939] = bitmap[da + 682];
            bitmap[da + 937] = bitmap[da + 681];
            bitmap[da + 935] = bitmap[da + 936] = bitmap[da + 680];
        case 85:
            bitmap[da + 934] = bitmap[da + 679];
            bitmap[da + 933] = bitmap[da + 678];
            bitmap[da + 931] = bitmap[da + 932] = bitmap[da + 677];
            bitmap[da + 930] = bitmap[da + 676];
            bitmap[da + 929] = bitmap[da + 675];
            bitmap[da + 927] = bitmap[da + 928] = bitmap[da + 674];
            bitmap[da + 926] = bitmap[da + 673];
            bitmap[da + 924] = bitmap[da + 925] = bitmap[da + 672];
        case 84:
            bitmap[da + 923] = bitmap[da + 671];
            bitmap[da + 922] = bitmap[da + 670];
            bitmap[da + 920] = bitmap[da + 921] = bitmap[da + 669];
            bitmap[da + 919] = bitmap[da + 668];
            bitmap[da + 918] = bitmap[da + 667];
            bitmap[da + 916] = bitmap[da + 917] = bitmap[da + 666];
            bitmap[da + 915] = bitmap[da + 665];
            bitmap[da + 913] = bitmap[da + 914] = bitmap[da + 664];
        case 83:
            bitmap[da + 912] = bitmap[da + 663];
            bitmap[da + 911] = bitmap[da + 662];
            bitmap[da + 909] = bitmap[da + 910] = bitmap[da + 661];
            bitmap[da + 908] = bitmap[da + 660];
            bitmap[da + 907] = bitmap[da + 659];
            bitmap[da + 905] = bitmap[da + 906] = bitmap[da + 658];
            bitmap[da + 904] = bitmap[da + 657];
            bitmap[da + 902] = bitmap[da + 903] = bitmap[da + 656];
        case 82:
            bitmap[da + 901] = bitmap[da + 655];
            bitmap[da + 900] = bitmap[da + 654];
            bitmap[da + 898] = bitmap[da + 899] = bitmap[da + 653];
            bitmap[da + 897] = bitmap[da + 652];
            bitmap[da + 896] = bitmap[da + 651];
            bitmap[da + 894] = bitmap[da + 895] = bitmap[da + 650];
            bitmap[da + 893] = bitmap[da + 649];
            bitmap[da + 891] = bitmap[da + 892] = bitmap[da + 648];
        case 81:
            bitmap[da + 890] = bitmap[da + 647];
            bitmap[da + 889] = bitmap[da + 646];
            bitmap[da + 887] = bitmap[da + 888] = bitmap[da + 645];
            bitmap[da + 886] = bitmap[da + 644];
            bitmap[da + 885] = bitmap[da + 643];
            bitmap[da + 883] = bitmap[da + 884] = bitmap[da + 642];
            bitmap[da + 882] = bitmap[da + 641];
            bitmap[da + 880] = bitmap[da + 881] = bitmap[da + 640];
        case 80:
            bitmap[da + 879] = bitmap[da + 639];
            bitmap[da + 878] = bitmap[da + 638];
            bitmap[da + 876] = bitmap[da + 877] = bitmap[da + 637];
            bitmap[da + 875] = bitmap[da + 636];
            bitmap[da + 874] = bitmap[da + 635];
            bitmap[da + 872] = bitmap[da + 873] = bitmap[da + 634];
            bitmap[da + 871] = bitmap[da + 633];
            bitmap[da + 869] = bitmap[da + 870] = bitmap[da + 632];
        case 79:
            bitmap[da + 868] = bitmap[da + 631];
            bitmap[da + 867] = bitmap[da + 630];
            bitmap[da + 865] = bitmap[da + 866] = bitmap[da + 629];
            bitmap[da + 864] = bitmap[da + 628];
            bitmap[da + 863] = bitmap[da + 627];
            bitmap[da + 861] = bitmap[da + 862] = bitmap[da + 626];
            bitmap[da + 860] = bitmap[da + 625];
            bitmap[da + 858] = bitmap[da + 859] = bitmap[da + 624];
        case 78:
            bitmap[da + 857] = bitmap[da + 623];
            bitmap[da + 856] = bitmap[da + 622];
            bitmap[da + 854] = bitmap[da + 855] = bitmap[da + 621];
            bitmap[da + 853] = bitmap[da + 620];
            bitmap[da + 852] = bitmap[da + 619];
            bitmap[da + 850] = bitmap[da + 851] = bitmap[da + 618];
            bitmap[da + 849] = bitmap[da + 617];
            bitmap[da + 847] = bitmap[da + 848] = bitmap[da + 616];
        case 77:
            bitmap[da + 846] = bitmap[da + 615];
            bitmap[da + 845] = bitmap[da + 614];
            bitmap[da + 843] = bitmap[da + 844] = bitmap[da + 613];
            bitmap[da + 842] = bitmap[da + 612];
            bitmap[da + 841] = bitmap[da + 611];
            bitmap[da + 839] = bitmap[da + 840] = bitmap[da + 610];
            bitmap[da + 838] = bitmap[da + 609];
            bitmap[da + 836] = bitmap[da + 837] = bitmap[da + 608];
        case 76:
            bitmap[da + 835] = bitmap[da + 607];
            bitmap[da + 834] = bitmap[da + 606];
            bitmap[da + 832] = bitmap[da + 833] = bitmap[da + 605];
            bitmap[da + 831] = bitmap[da + 604];
            bitmap[da + 830] = bitmap[da + 603];
            bitmap[da + 828] = bitmap[da + 829] = bitmap[da + 602];
            bitmap[da + 827] = bitmap[da + 601];
            bitmap[da + 825] = bitmap[da + 826] = bitmap[da + 600];
        case 75:
            bitmap[da + 824] = bitmap[da + 599];
            bitmap[da + 823] = bitmap[da + 598];
            bitmap[da + 821] = bitmap[da + 822] = bitmap[da + 597];
            bitmap[da + 820] = bitmap[da + 596];
            bitmap[da + 819] = bitmap[da + 595];
            bitmap[da + 817] = bitmap[da + 818] = bitmap[da + 594];
            bitmap[da + 816] = bitmap[da + 593];
            bitmap[da + 814] = bitmap[da + 815] = bitmap[da + 592];
        case 74:
            bitmap[da + 813] = bitmap[da + 591];
            bitmap[da + 812] = bitmap[da + 590];
            bitmap[da + 810] = bitmap[da + 811] = bitmap[da + 589];
            bitmap[da + 809] = bitmap[da + 588];
            bitmap[da + 808] = bitmap[da + 587];
            bitmap[da + 806] = bitmap[da + 807] = bitmap[da + 586];
            bitmap[da + 805] = bitmap[da + 585];
            bitmap[da + 803] = bitmap[da + 804] = bitmap[da + 584];
        case 73:
            bitmap[da + 802] = bitmap[da + 583];
            bitmap[da + 801] = bitmap[da + 582];
            bitmap[da + 799] = bitmap[da + 800] = bitmap[da + 581];
            bitmap[da + 798] = bitmap[da + 580];
            bitmap[da + 797] = bitmap[da + 579];
            bitmap[da + 795] = bitmap[da + 796] = bitmap[da + 578];
            bitmap[da + 794] = bitmap[da + 577];
            bitmap[da + 792] = bitmap[da + 793] = bitmap[da + 576];
        case 72:
            bitmap[da + 791] = bitmap[da + 575];
            bitmap[da + 790] = bitmap[da + 574];
            bitmap[da + 788] = bitmap[da + 789] = bitmap[da + 573];
            bitmap[da + 787] = bitmap[da + 572];
            bitmap[da + 786] = bitmap[da + 571];
            bitmap[da + 784] = bitmap[da + 785] = bitmap[da + 570];
            bitmap[da + 783] = bitmap[da + 569];
            bitmap[da + 781] = bitmap[da + 782] = bitmap[da + 568];
        case 71:
            bitmap[da + 780] = bitmap[da + 567];
            bitmap[da + 779] = bitmap[da + 566];
            bitmap[da + 777] = bitmap[da + 778] = bitmap[da + 565];
            bitmap[da + 776] = bitmap[da + 564];
            bitmap[da + 775] = bitmap[da + 563];
            bitmap[da + 773] = bitmap[da + 774] = bitmap[da + 562];
            bitmap[da + 772] = bitmap[da + 561];
            bitmap[da + 770] = bitmap[da + 771] = bitmap[da + 560];
        case 70:
            bitmap[da + 769] = bitmap[da + 559];
            bitmap[da + 768] = bitmap[da + 558];
            bitmap[da + 766] = bitmap[da + 767] = bitmap[da + 557];
            bitmap[da + 765] = bitmap[da + 556];
            bitmap[da + 764] = bitmap[da + 555];
            bitmap[da + 762] = bitmap[da + 763] = bitmap[da + 554];
            bitmap[da + 761] = bitmap[da + 553];
            bitmap[da + 759] = bitmap[da + 760] = bitmap[da + 552];
        case 69:
            bitmap[da + 758] = bitmap[da + 551];
            bitmap[da + 757] = bitmap[da + 550];
            bitmap[da + 755] = bitmap[da + 756] = bitmap[da + 549];
            bitmap[da + 754] = bitmap[da + 548];
            bitmap[da + 753] = bitmap[da + 547];
            bitmap[da + 751] = bitmap[da + 752] = bitmap[da + 546];
            bitmap[da + 750] = bitmap[da + 545];
            bitmap[da + 748] = bitmap[da + 749] = bitmap[da + 544];
        case 68:
            bitmap[da + 747] = bitmap[da + 543];
            bitmap[da + 746] = bitmap[da + 542];
            bitmap[da + 744] = bitmap[da + 745] = bitmap[da + 541];
            bitmap[da + 743] = bitmap[da + 540];
            bitmap[da + 742] = bitmap[da + 539];
            bitmap[da + 740] = bitmap[da + 741] = bitmap[da + 538];
            bitmap[da + 739] = bitmap[da + 537];
            bitmap[da + 737] = bitmap[da + 738] = bitmap[da + 536];
        case 67:
            bitmap[da + 736] = bitmap[da + 535];
            bitmap[da + 735] = bitmap[da + 534];
            bitmap[da + 733] = bitmap[da + 734] = bitmap[da + 533];
            bitmap[da + 732] = bitmap[da + 532];
            bitmap[da + 731] = bitmap[da + 531];
            bitmap[da + 729] = bitmap[da + 730] = bitmap[da + 530];
            bitmap[da + 728] = bitmap[da + 529];
            bitmap[da + 726] = bitmap[da + 727] = bitmap[da + 528];
        case 66:
            bitmap[da + 725] = bitmap[da + 527];
            bitmap[da + 724] = bitmap[da + 526];
            bitmap[da + 722] = bitmap[da + 723] = bitmap[da + 525];
            bitmap[da + 721] = bitmap[da + 524];
            bitmap[da + 720] = bitmap[da + 523];
            bitmap[da + 718] = bitmap[da + 719] = bitmap[da + 522];
            bitmap[da + 717] = bitmap[da + 521];
            bitmap[da + 715] = bitmap[da + 716] = bitmap[da + 520];
        case 65:
            bitmap[da + 714] = bitmap[da + 519];
            bitmap[da + 713] = bitmap[da + 518];
            bitmap[da + 711] = bitmap[da + 712] = bitmap[da + 517];
            bitmap[da + 710] = bitmap[da + 516];
            bitmap[da + 709] = bitmap[da + 515];
            bitmap[da + 707] = bitmap[da + 708] = bitmap[da + 514];
            bitmap[da + 706] = bitmap[da + 513];
            bitmap[da + 704] = bitmap[da + 705] = bitmap[da + 512];
        case 64:
            bitmap[da + 703] = bitmap[da + 511];
            bitmap[da + 702] = bitmap[da + 510];
            bitmap[da + 700] = bitmap[da + 701] = bitmap[da + 509];
            bitmap[da + 699] = bitmap[da + 508];
            bitmap[da + 698] = bitmap[da + 507];
            bitmap[da + 696] = bitmap[da + 697] = bitmap[da + 506];
            bitmap[da + 695] = bitmap[da + 505];
            bitmap[da + 693] = bitmap[da + 694] = bitmap[da + 504];
        case 63:
            bitmap[da + 692] = bitmap[da + 503];
            bitmap[da + 691] = bitmap[da + 502];
            bitmap[da + 689] = bitmap[da + 690] = bitmap[da + 501];
            bitmap[da + 688] = bitmap[da + 500];
            bitmap[da + 687] = bitmap[da + 499];
            bitmap[da + 685] = bitmap[da + 686] = bitmap[da + 498];
            bitmap[da + 684] = bitmap[da + 497];
            bitmap[da + 682] = bitmap[da + 683] = bitmap[da + 496];
        case 62:
            bitmap[da + 681] = bitmap[da + 495];
            bitmap[da + 680] = bitmap[da + 494];
            bitmap[da + 678] = bitmap[da + 679] = bitmap[da + 493];
            bitmap[da + 677] = bitmap[da + 492];
            bitmap[da + 676] = bitmap[da + 491];
            bitmap[da + 674] = bitmap[da + 675] = bitmap[da + 490];
            bitmap[da + 673] = bitmap[da + 489];
            bitmap[da + 671] = bitmap[da + 672] = bitmap[da + 488];
        case 61:
            bitmap[da + 670] = bitmap[da + 487];
            bitmap[da + 669] = bitmap[da + 486];
            bitmap[da + 667] = bitmap[da + 668] = bitmap[da + 485];
            bitmap[da + 666] = bitmap[da + 484];
            bitmap[da + 665] = bitmap[da + 483];
            bitmap[da + 663] = bitmap[da + 664] = bitmap[da + 482];
            bitmap[da + 662] = bitmap[da + 481];
            bitmap[da + 660] = bitmap[da + 661] = bitmap[da + 480];
        case 60:
            bitmap[da + 659] = bitmap[da + 479];
            bitmap[da + 658] = bitmap[da + 478];
            bitmap[da + 656] = bitmap[da + 657] = bitmap[da + 477];
            bitmap[da + 655] = bitmap[da + 476];
            bitmap[da + 654] = bitmap[da + 475];
            bitmap[da + 652] = bitmap[da + 653] = bitmap[da + 474];
            bitmap[da + 651] = bitmap[da + 473];
            bitmap[da + 649] = bitmap[da + 650] = bitmap[da + 472];
        case 59:
            bitmap[da + 648] = bitmap[da + 471];
            bitmap[da + 647] = bitmap[da + 470];
            bitmap[da + 645] = bitmap[da + 646] = bitmap[da + 469];
            bitmap[da + 644] = bitmap[da + 468];
            bitmap[da + 643] = bitmap[da + 467];
            bitmap[da + 641] = bitmap[da + 642] = bitmap[da + 466];
            bitmap[da + 640] = bitmap[da + 465];
            bitmap[da + 638] = bitmap[da + 639] = bitmap[da + 464];
        case 58:
            bitmap[da + 637] = bitmap[da + 463];
            bitmap[da + 636] = bitmap[da + 462];
            bitmap[da + 634] = bitmap[da + 635] = bitmap[da + 461];
            bitmap[da + 633] = bitmap[da + 460];
            bitmap[da + 632] = bitmap[da + 459];
            bitmap[da + 630] = bitmap[da + 631] = bitmap[da + 458];
            bitmap[da + 629] = bitmap[da + 457];
            bitmap[da + 627] = bitmap[da + 628] = bitmap[da + 456];
        case 57:
            bitmap[da + 626] = bitmap[da + 455];
            bitmap[da + 625] = bitmap[da + 454];
            bitmap[da + 623] = bitmap[da + 624] = bitmap[da + 453];
            bitmap[da + 622] = bitmap[da + 452];
            bitmap[da + 621] = bitmap[da + 451];
            bitmap[da + 619] = bitmap[da + 620] = bitmap[da + 450];
            bitmap[da + 618] = bitmap[da + 449];
            bitmap[da + 616] = bitmap[da + 617] = bitmap[da + 448];
        case 56:
            bitmap[da + 615] = bitmap[da + 447];
            bitmap[da + 614] = bitmap[da + 446];
            bitmap[da + 612] = bitmap[da + 613] = bitmap[da + 445];
            bitmap[da + 611] = bitmap[da + 444];
            bitmap[da + 610] = bitmap[da + 443];
            bitmap[da + 608] = bitmap[da + 609] = bitmap[da + 442];
            bitmap[da + 607] = bitmap[da + 441];
            bitmap[da + 605] = bitmap[da + 606] = bitmap[da + 440];
        case 55:
            bitmap[da + 604] = bitmap[da + 439];
            bitmap[da + 603] = bitmap[da + 438];
            bitmap[da + 601] = bitmap[da + 602] = bitmap[da + 437];
            bitmap[da + 600] = bitmap[da + 436];
            bitmap[da + 599] = bitmap[da + 435];
            bitmap[da + 597] = bitmap[da + 598] = bitmap[da + 434];
            bitmap[da + 596] = bitmap[da + 433];
            bitmap[da + 594] = bitmap[da + 595] = bitmap[da + 432];
        case 54:
            bitmap[da + 593] = bitmap[da + 431];
            bitmap[da + 592] = bitmap[da + 430];
            bitmap[da + 590] = bitmap[da + 591] = bitmap[da + 429];
            bitmap[da + 589] = bitmap[da + 428];
            bitmap[da + 588] = bitmap[da + 427];
            bitmap[da + 586] = bitmap[da + 587] = bitmap[da + 426];
            bitmap[da + 585] = bitmap[da + 425];
            bitmap[da + 583] = bitmap[da + 584] = bitmap[da + 424];
        case 53:
            bitmap[da + 582] = bitmap[da + 423];
            bitmap[da + 581] = bitmap[da + 422];
            bitmap[da + 579] = bitmap[da + 580] = bitmap[da + 421];
            bitmap[da + 578] = bitmap[da + 420];
            bitmap[da + 577] = bitmap[da + 419];
            bitmap[da + 575] = bitmap[da + 576] = bitmap[da + 418];
            bitmap[da + 574] = bitmap[da + 417];
            bitmap[da + 572] = bitmap[da + 573] = bitmap[da + 416];
        case 52:
            bitmap[da + 571] = bitmap[da + 415];
            bitmap[da + 570] = bitmap[da + 414];
            bitmap[da + 568] = bitmap[da + 569] = bitmap[da + 413];
            bitmap[da + 567] = bitmap[da + 412];
            bitmap[da + 566] = bitmap[da + 411];
            bitmap[da + 564] = bitmap[da + 565] = bitmap[da + 410];
            bitmap[da + 563] = bitmap[da + 409];
            bitmap[da + 561] = bitmap[da + 562] = bitmap[da + 408];
        case 51:
            bitmap[da + 560] = bitmap[da + 407];
            bitmap[da + 559] = bitmap[da + 406];
            bitmap[da + 557] = bitmap[da + 558] = bitmap[da + 405];
            bitmap[da + 556] = bitmap[da + 404];
            bitmap[da + 555] = bitmap[da + 403];
            bitmap[da + 553] = bitmap[da + 554] = bitmap[da + 402];
            bitmap[da + 552] = bitmap[da + 401];
            bitmap[da + 550] = bitmap[da + 551] = bitmap[da + 400];
        case 50:
            bitmap[da + 549] = bitmap[da + 399];
            bitmap[da + 548] = bitmap[da + 398];
            bitmap[da + 546] = bitmap[da + 547] = bitmap[da + 397];
            bitmap[da + 545] = bitmap[da + 396];
            bitmap[da + 544] = bitmap[da + 395];
            bitmap[da + 542] = bitmap[da + 543] = bitmap[da + 394];
            bitmap[da + 541] = bitmap[da + 393];
            bitmap[da + 539] = bitmap[da + 540] = bitmap[da + 392];
        case 49:
            bitmap[da + 538] = bitmap[da + 391];
            bitmap[da + 537] = bitmap[da + 390];
            bitmap[da + 535] = bitmap[da + 536] = bitmap[da + 389];
            bitmap[da + 534] = bitmap[da + 388];
            bitmap[da + 533] = bitmap[da + 387];
            bitmap[da + 531] = bitmap[da + 532] = bitmap[da + 386];
            bitmap[da + 530] = bitmap[da + 385];
            bitmap[da + 528] = bitmap[da + 529] = bitmap[da + 384];
        case 48:
            bitmap[da + 527] = bitmap[da + 383];
            bitmap[da + 526] = bitmap[da + 382];
            bitmap[da + 524] = bitmap[da + 525] = bitmap[da + 381];
            bitmap[da + 523] = bitmap[da + 380];
            bitmap[da + 522] = bitmap[da + 379];
            bitmap[da + 520] = bitmap[da + 521] = bitmap[da + 378];
            bitmap[da + 519] = bitmap[da + 377];
            bitmap[da + 517] = bitmap[da + 518] = bitmap[da + 376];
        case 47:
            bitmap[da + 516] = bitmap[da + 375];
            bitmap[da + 515] = bitmap[da + 374];
            bitmap[da + 513] = bitmap[da + 514] = bitmap[da + 373];
            bitmap[da + 512] = bitmap[da + 372];
            bitmap[da + 511] = bitmap[da + 371];
            bitmap[da + 509] = bitmap[da + 510] = bitmap[da + 370];
            bitmap[da + 508] = bitmap[da + 369];
            bitmap[da + 506] = bitmap[da + 507] = bitmap[da + 368];
        case 46:
            bitmap[da + 505] = bitmap[da + 367];
            bitmap[da + 504] = bitmap[da + 366];
            bitmap[da + 502] = bitmap[da + 503] = bitmap[da + 365];
            bitmap[da + 501] = bitmap[da + 364];
            bitmap[da + 500] = bitmap[da + 363];
            bitmap[da + 498] = bitmap[da + 499] = bitmap[da + 362];
            bitmap[da + 497] = bitmap[da + 361];
            bitmap[da + 495] = bitmap[da + 496] = bitmap[da + 360];
        case 45:
            bitmap[da + 494] = bitmap[da + 359];
            bitmap[da + 493] = bitmap[da + 358];
            bitmap[da + 491] = bitmap[da + 492] = bitmap[da + 357];
            bitmap[da + 490] = bitmap[da + 356];
            bitmap[da + 489] = bitmap[da + 355];
            bitmap[da + 487] = bitmap[da + 488] = bitmap[da + 354];
            bitmap[da + 486] = bitmap[da + 353];
            bitmap[da + 484] = bitmap[da + 485] = bitmap[da + 352];
        case 44:
            bitmap[da + 483] = bitmap[da + 351];
            bitmap[da + 482] = bitmap[da + 350];
            bitmap[da + 480] = bitmap[da + 481] = bitmap[da + 349];
            bitmap[da + 479] = bitmap[da + 348];
            bitmap[da + 478] = bitmap[da + 347];
            bitmap[da + 476] = bitmap[da + 477] = bitmap[da + 346];
            bitmap[da + 475] = bitmap[da + 345];
            bitmap[da + 473] = bitmap[da + 474] = bitmap[da + 344];
        case 43:
            bitmap[da + 472] = bitmap[da + 343];
            bitmap[da + 471] = bitmap[da + 342];
            bitmap[da + 469] = bitmap[da + 470] = bitmap[da + 341];
            bitmap[da + 468] = bitmap[da + 340];
            bitmap[da + 467] = bitmap[da + 339];
            bitmap[da + 465] = bitmap[da + 466] = bitmap[da + 338];
            bitmap[da + 464] = bitmap[da + 337];
            bitmap[da + 462] = bitmap[da + 463] = bitmap[da + 336];
        case 42:
            bitmap[da + 461] = bitmap[da + 335];
            bitmap[da + 460] = bitmap[da + 334];
            bitmap[da + 458] = bitmap[da + 459] = bitmap[da + 333];
            bitmap[da + 457] = bitmap[da + 332];
            bitmap[da + 456] = bitmap[da + 331];
            bitmap[da + 454] = bitmap[da + 455] = bitmap[da + 330];
            bitmap[da + 453] = bitmap[da + 329];
            bitmap[da + 451] = bitmap[da + 452] = bitmap[da + 328];
        case 41:
            bitmap[da + 450] = bitmap[da + 327];
            bitmap[da + 449] = bitmap[da + 326];
            bitmap[da + 447] = bitmap[da + 448] = bitmap[da + 325];
            bitmap[da + 446] = bitmap[da + 324];
            bitmap[da + 445] = bitmap[da + 323];
            bitmap[da + 443] = bitmap[da + 444] = bitmap[da + 322];
            bitmap[da + 442] = bitmap[da + 321];
            bitmap[da + 440] = bitmap[da + 441] = bitmap[da + 320];
        case 40:
            bitmap[da + 439] = bitmap[da + 319];
            bitmap[da + 438] = bitmap[da + 318];
            bitmap[da + 436] = bitmap[da + 437] = bitmap[da + 317];
            bitmap[da + 435] = bitmap[da + 316];
            bitmap[da + 434] = bitmap[da + 315];
            bitmap[da + 432] = bitmap[da + 433] = bitmap[da + 314];
            bitmap[da + 431] = bitmap[da + 313];
            bitmap[da + 429] = bitmap[da + 430] = bitmap[da + 312];
        case 39:
            bitmap[da + 428] = bitmap[da + 311];
            bitmap[da + 427] = bitmap[da + 310];
            bitmap[da + 425] = bitmap[da + 426] = bitmap[da + 309];
            bitmap[da + 424] = bitmap[da + 308];
            bitmap[da + 423] = bitmap[da + 307];
            bitmap[da + 421] = bitmap[da + 422] = bitmap[da + 306];
            bitmap[da + 420] = bitmap[da + 305];
            bitmap[da + 418] = bitmap[da + 419] = bitmap[da + 304];
        case 38:
            bitmap[da + 417] = bitmap[da + 303];
            bitmap[da + 416] = bitmap[da + 302];
            bitmap[da + 414] = bitmap[da + 415] = bitmap[da + 301];
            bitmap[da + 413] = bitmap[da + 300];
            bitmap[da + 412] = bitmap[da + 299];
            bitmap[da + 410] = bitmap[da + 411] = bitmap[da + 298];
            bitmap[da + 409] = bitmap[da + 297];
            bitmap[da + 407] = bitmap[da + 408] = bitmap[da + 296];
        case 37:
            bitmap[da + 406] = bitmap[da + 295];
            bitmap[da + 405] = bitmap[da + 294];
            bitmap[da + 403] = bitmap[da + 404] = bitmap[da + 293];
            bitmap[da + 402] = bitmap[da + 292];
            bitmap[da + 401] = bitmap[da + 291];
            bitmap[da + 399] = bitmap[da + 400] = bitmap[da + 290];
            bitmap[da + 398] = bitmap[da + 289];
            bitmap[da + 396] = bitmap[da + 397] = bitmap[da + 288];
        case 36:
            bitmap[da + 395] = bitmap[da + 287];
            bitmap[da + 394] = bitmap[da + 286];
            bitmap[da + 392] = bitmap[da + 393] = bitmap[da + 285];
            bitmap[da + 391] = bitmap[da + 284];
            bitmap[da + 390] = bitmap[da + 283];
            bitmap[da + 388] = bitmap[da + 389] = bitmap[da + 282];
            bitmap[da + 387] = bitmap[da + 281];
            bitmap[da + 385] = bitmap[da + 386] = bitmap[da + 280];
        case 35:
            bitmap[da + 384] = bitmap[da + 279];
            bitmap[da + 383] = bitmap[da + 278];
            bitmap[da + 381] = bitmap[da + 382] = bitmap[da + 277];
            bitmap[da + 380] = bitmap[da + 276];
            bitmap[da + 379] = bitmap[da + 275];
            bitmap[da + 377] = bitmap[da + 378] = bitmap[da + 274];
            bitmap[da + 376] = bitmap[da + 273];
            bitmap[da + 374] = bitmap[da + 375] = bitmap[da + 272];
        case 34:
            bitmap[da + 373] = bitmap[da + 271];
            bitmap[da + 372] = bitmap[da + 270];
            bitmap[da + 370] = bitmap[da + 371] = bitmap[da + 269];
            bitmap[da + 369] = bitmap[da + 268];
            bitmap[da + 368] = bitmap[da + 267];
            bitmap[da + 366] = bitmap[da + 367] = bitmap[da + 266];
            bitmap[da + 365] = bitmap[da + 265];
            bitmap[da + 363] = bitmap[da + 364] = bitmap[da + 264];
        case 33:
            bitmap[da + 362] = bitmap[da + 263];
            bitmap[da + 361] = bitmap[da + 262];
            bitmap[da + 359] = bitmap[da + 360] = bitmap[da + 261];
            bitmap[da + 358] = bitmap[da + 260];
            bitmap[da + 357] = bitmap[da + 259];
            bitmap[da + 355] = bitmap[da + 356] = bitmap[da + 258];
            bitmap[da + 354] = bitmap[da + 257];
            bitmap[da + 352] = bitmap[da + 353] = bitmap[da + 256];
        case 32:
            bitmap[da + 351] = bitmap[da + 255];
            bitmap[da + 350] = bitmap[da + 254];
            bitmap[da + 348] = bitmap[da + 349] = bitmap[da + 253];
            bitmap[da + 347] = bitmap[da + 252];
            bitmap[da + 346] = bitmap[da + 251];
            bitmap[da + 344] = bitmap[da + 345] = bitmap[da + 250];
            bitmap[da + 343] = bitmap[da + 249];
            bitmap[da + 341] = bitmap[da + 342] = bitmap[da + 248];
        case 31:
            bitmap[da + 340] = bitmap[da + 247];
            bitmap[da + 339] = bitmap[da + 246];
            bitmap[da + 337] = bitmap[da + 338] = bitmap[da + 245];
            bitmap[da + 336] = bitmap[da + 244];
            bitmap[da + 335] = bitmap[da + 243];
            bitmap[da + 333] = bitmap[da + 334] = bitmap[da + 242];
            bitmap[da + 332] = bitmap[da + 241];
            bitmap[da + 330] = bitmap[da + 331] = bitmap[da + 240];
        case 30:
            bitmap[da + 329] = bitmap[da + 239];
            bitmap[da + 328] = bitmap[da + 238];
            bitmap[da + 326] = bitmap[da + 327] = bitmap[da + 237];
            bitmap[da + 325] = bitmap[da + 236];
            bitmap[da + 324] = bitmap[da + 235];
            bitmap[da + 322] = bitmap[da + 323] = bitmap[da + 234];
            bitmap[da + 321] = bitmap[da + 233];
            bitmap[da + 319] = bitmap[da + 320] = bitmap[da + 232];
        case 29:
            bitmap[da + 318] = bitmap[da + 231];
            bitmap[da + 317] = bitmap[da + 230];
            bitmap[da + 315] = bitmap[da + 316] = bitmap[da + 229];
            bitmap[da + 314] = bitmap[da + 228];
            bitmap[da + 313] = bitmap[da + 227];
            bitmap[da + 311] = bitmap[da + 312] = bitmap[da + 226];
            bitmap[da + 310] = bitmap[da + 225];
            bitmap[da + 308] = bitmap[da + 309] = bitmap[da + 224];
        case 28:
            bitmap[da + 307] = bitmap[da + 223];
            bitmap[da + 306] = bitmap[da + 222];
            bitmap[da + 304] = bitmap[da + 305] = bitmap[da + 221];
            bitmap[da + 303] = bitmap[da + 220];
            bitmap[da + 302] = bitmap[da + 219];
            bitmap[da + 300] = bitmap[da + 301] = bitmap[da + 218];
            bitmap[da + 299] = bitmap[da + 217];
            bitmap[da + 297] = bitmap[da + 298] = bitmap[da + 216];
        case 27:
            bitmap[da + 296] = bitmap[da + 215];
            bitmap[da + 295] = bitmap[da + 214];
            bitmap[da + 293] = bitmap[da + 294] = bitmap[da + 213];
            bitmap[da + 292] = bitmap[da + 212];
            bitmap[da + 291] = bitmap[da + 211];
            bitmap[da + 289] = bitmap[da + 290] = bitmap[da + 210];
            bitmap[da + 288] = bitmap[da + 209];
            bitmap[da + 286] = bitmap[da + 287] = bitmap[da + 208];
        case 26:
            bitmap[da + 285] = bitmap[da + 207];
            bitmap[da + 284] = bitmap[da + 206];
            bitmap[da + 282] = bitmap[da + 283] = bitmap[da + 205];
            bitmap[da + 281] = bitmap[da + 204];
            bitmap[da + 280] = bitmap[da + 203];
            bitmap[da + 278] = bitmap[da + 279] = bitmap[da + 202];
            bitmap[da + 277] = bitmap[da + 201];
            bitmap[da + 275] = bitmap[da + 276] = bitmap[da + 200];
        case 25:
            bitmap[da + 274] = bitmap[da + 199];
            bitmap[da + 273] = bitmap[da + 198];
            bitmap[da + 271] = bitmap[da + 272] = bitmap[da + 197];
            bitmap[da + 270] = bitmap[da + 196];
            bitmap[da + 269] = bitmap[da + 195];
            bitmap[da + 267] = bitmap[da + 268] = bitmap[da + 194];
            bitmap[da + 266] = bitmap[da + 193];
            bitmap[da + 264] = bitmap[da + 265] = bitmap[da + 192];
        case 24:
            bitmap[da + 263] = bitmap[da + 191];
            bitmap[da + 262] = bitmap[da + 190];
            bitmap[da + 260] = bitmap[da + 261] = bitmap[da + 189];
            bitmap[da + 259] = bitmap[da + 188];
            bitmap[da + 258] = bitmap[da + 187];
            bitmap[da + 256] = bitmap[da + 257] = bitmap[da + 186];
            bitmap[da + 255] = bitmap[da + 185];
            bitmap[da + 253] = bitmap[da + 254] = bitmap[da + 184];
        case 23:
            bitmap[da + 252] = bitmap[da + 183];
            bitmap[da + 251] = bitmap[da + 182];
            bitmap[da + 249] = bitmap[da + 250] = bitmap[da + 181];
            bitmap[da + 248] = bitmap[da + 180];
            bitmap[da + 247] = bitmap[da + 179];
            bitmap[da + 245] = bitmap[da + 246] = bitmap[da + 178];
            bitmap[da + 244] = bitmap[da + 177];
            bitmap[da + 242] = bitmap[da + 243] = bitmap[da + 176];
        case 22:
            bitmap[da + 241] = bitmap[da + 175];
            bitmap[da + 240] = bitmap[da + 174];
            bitmap[da + 238] = bitmap[da + 239] = bitmap[da + 173];
            bitmap[da + 237] = bitmap[da + 172];
            bitmap[da + 236] = bitmap[da + 171];
            bitmap[da + 234] = bitmap[da + 235] = bitmap[da + 170];
            bitmap[da + 233] = bitmap[da + 169];
            bitmap[da + 231] = bitmap[da + 232] = bitmap[da + 168];
        case 21:
            bitmap[da + 230] = bitmap[da + 167];
            bitmap[da + 229] = bitmap[da + 166];
            bitmap[da + 227] = bitmap[da + 228] = bitmap[da + 165];
            bitmap[da + 226] = bitmap[da + 164];
            bitmap[da + 225] = bitmap[da + 163];
            bitmap[da + 223] = bitmap[da + 224] = bitmap[da + 162];
            bitmap[da + 222] = bitmap[da + 161];
            bitmap[da + 220] = bitmap[da + 221] = bitmap[da + 160];
        case 20:
            bitmap[da + 219] = bitmap[da + 159];
            bitmap[da + 218] = bitmap[da + 158];
            bitmap[da + 216] = bitmap[da + 217] = bitmap[da + 157];
            bitmap[da + 215] = bitmap[da + 156];
            bitmap[da + 214] = bitmap[da + 155];
            bitmap[da + 212] = bitmap[da + 213] = bitmap[da + 154];
            bitmap[da + 211] = bitmap[da + 153];
            bitmap[da + 209] = bitmap[da + 210] = bitmap[da + 152];
        case 19:
            bitmap[da + 208] = bitmap[da + 151];
            bitmap[da + 207] = bitmap[da + 150];
            bitmap[da + 205] = bitmap[da + 206] = bitmap[da + 149];
            bitmap[da + 204] = bitmap[da + 148];
            bitmap[da + 203] = bitmap[da + 147];
            bitmap[da + 201] = bitmap[da + 202] = bitmap[da + 146];
            bitmap[da + 200] = bitmap[da + 145];
            bitmap[da + 198] = bitmap[da + 199] = bitmap[da + 144];
        case 18:
            bitmap[da + 197] = bitmap[da + 143];
            bitmap[da + 196] = bitmap[da + 142];
            bitmap[da + 194] = bitmap[da + 195] = bitmap[da + 141];
            bitmap[da + 193] = bitmap[da + 140];
            bitmap[da + 192] = bitmap[da + 139];
            bitmap[da + 190] = bitmap[da + 191] = bitmap[da + 138];
            bitmap[da + 189] = bitmap[da + 137];
            bitmap[da + 187] = bitmap[da + 188] = bitmap[da + 136];
        case 17:
            bitmap[da + 186] = bitmap[da + 135];
            bitmap[da + 185] = bitmap[da + 134];
            bitmap[da + 183] = bitmap[da + 184] = bitmap[da + 133];
            bitmap[da + 182] = bitmap[da + 132];
            bitmap[da + 181] = bitmap[da + 131];
            bitmap[da + 179] = bitmap[da + 180] = bitmap[da + 130];
            bitmap[da + 178] = bitmap[da + 129];
            bitmap[da + 176] = bitmap[da + 177] = bitmap[da + 128];
        case 16:
            bitmap[da + 175] = bitmap[da + 127];
            bitmap[da + 174] = bitmap[da + 126];
            bitmap[da + 172] = bitmap[da + 173] = bitmap[da + 125];
            bitmap[da + 171] = bitmap[da + 124];
            bitmap[da + 170] = bitmap[da + 123];
            bitmap[da + 168] = bitmap[da + 169] = bitmap[da + 122];
            bitmap[da + 167] = bitmap[da + 121];
            bitmap[da + 165] = bitmap[da + 166] = bitmap[da + 120];
        case 15:
            bitmap[da + 164] = bitmap[da + 119];
            bitmap[da + 163] = bitmap[da + 118];
            bitmap[da + 161] = bitmap[da + 162] = bitmap[da + 117];
            bitmap[da + 160] = bitmap[da + 116];
            bitmap[da + 159] = bitmap[da + 115];
            bitmap[da + 157] = bitmap[da + 158] = bitmap[da + 114];
            bitmap[da + 156] = bitmap[da + 113];
            bitmap[da + 154] = bitmap[da + 155] = bitmap[da + 112];
        case 14:
            bitmap[da + 153] = bitmap[da + 111];
            bitmap[da + 152] = bitmap[da + 110];
            bitmap[da + 150] = bitmap[da + 151] = bitmap[da + 109];
            bitmap[da + 149] = bitmap[da + 108];
            bitmap[da + 148] = bitmap[da + 107];
            bitmap[da + 146] = bitmap[da + 147] = bitmap[da + 106];
            bitmap[da + 145] = bitmap[da + 105];
            bitmap[da + 143] = bitmap[da + 144] = bitmap[da + 104];
        case 13:
            bitmap[da + 142] = bitmap[da + 103];
            bitmap[da + 141] = bitmap[da + 102];
            bitmap[da + 139] = bitmap[da + 140] = bitmap[da + 101];
            bitmap[da + 138] = bitmap[da + 100];
            bitmap[da + 137] = bitmap[da + 99];
            bitmap[da + 135] = bitmap[da + 136] = bitmap[da + 98];
            bitmap[da + 134] = bitmap[da + 97];
            bitmap[da + 132] = bitmap[da + 133] = bitmap[da + 96];
        case 12:
            bitmap[da + 131] = bitmap[da + 95];
            bitmap[da + 130] = bitmap[da + 94];
            bitmap[da + 128] = bitmap[da + 129] = bitmap[da + 93];
            bitmap[da + 127] = bitmap[da + 92];
            bitmap[da + 126] = bitmap[da + 91];
            bitmap[da + 124] = bitmap[da + 125] = bitmap[da + 90];
            bitmap[da + 123] = bitmap[da + 89];
            bitmap[da + 121] = bitmap[da + 122] = bitmap[da + 88];
        case 11:
            bitmap[da + 120] = bitmap[da + 87];
            bitmap[da + 119] = bitmap[da + 86];
            bitmap[da + 117] = bitmap[da + 118] = bitmap[da + 85];
            bitmap[da + 116] = bitmap[da + 84];
            bitmap[da + 115] = bitmap[da + 83];
            bitmap[da + 113] = bitmap[da + 114] = bitmap[da + 82];
            bitmap[da + 112] = bitmap[da + 81];
            bitmap[da + 110] = bitmap[da + 111] = bitmap[da + 80];
        case 10:
            bitmap[da + 109] = bitmap[da + 79];
            bitmap[da + 108] = bitmap[da + 78];
            bitmap[da + 106] = bitmap[da + 107] = bitmap[da + 77];
            bitmap[da + 105] = bitmap[da + 76];
            bitmap[da + 104] = bitmap[da + 75];
            bitmap[da + 102] = bitmap[da + 103] = bitmap[da + 74];
            bitmap[da + 101] = bitmap[da + 73];
            bitmap[da + 99] = bitmap[da + 100] = bitmap[da + 72];
        case 9:
            bitmap[da + 98] = bitmap[da + 71];
            bitmap[da + 97] = bitmap[da + 70];
            bitmap[da + 95] = bitmap[da + 96] = bitmap[da + 69];
            bitmap[da + 94] = bitmap[da + 68];
            bitmap[da + 93] = bitmap[da + 67];
            bitmap[da + 91] = bitmap[da + 92] = bitmap[da + 66];
            bitmap[da + 90] = bitmap[da + 65];
            bitmap[da + 88] = bitmap[da + 89] = bitmap[da + 64];
        case 8:
            bitmap[da + 87] = bitmap[da + 63];
            bitmap[da + 86] = bitmap[da + 62];
            bitmap[da + 84] = bitmap[da + 85] = bitmap[da + 61];
            bitmap[da + 83] = bitmap[da + 60];
            bitmap[da + 82] = bitmap[da + 59];
            bitmap[da + 80] = bitmap[da + 81] = bitmap[da + 58];
            bitmap[da + 79] = bitmap[da + 57];
            bitmap[da + 77] = bitmap[da + 78] = bitmap[da + 56];
        case 7:
            bitmap[da + 76] = bitmap[da + 55];
            bitmap[da + 75] = bitmap[da + 54];
            bitmap[da + 73] = bitmap[da + 74] = bitmap[da + 53];
            bitmap[da + 72] = bitmap[da + 52];
            bitmap[da + 71] = bitmap[da + 51];
            bitmap[da + 69] = bitmap[da + 70] = bitmap[da + 50];
            bitmap[da + 68] = bitmap[da + 49];
            bitmap[da + 66] = bitmap[da + 67] = bitmap[da + 48];
        case 6:
            bitmap[da + 65] = bitmap[da + 47];
            bitmap[da + 64] = bitmap[da + 46];
            bitmap[da + 62] = bitmap[da + 63] = bitmap[da + 45];
            bitmap[da + 61] = bitmap[da + 44];
            bitmap[da + 60] = bitmap[da + 43];
            bitmap[da + 58] = bitmap[da + 59] = bitmap[da + 42];
            bitmap[da + 57] = bitmap[da + 41];
            bitmap[da + 55] = bitmap[da + 56] = bitmap[da + 40];
        case 5:
            bitmap[da + 54] = bitmap[da + 39];
            bitmap[da + 53] = bitmap[da + 38];
            bitmap[da + 51] = bitmap[da + 52] = bitmap[da + 37];
            bitmap[da + 50] = bitmap[da + 36];
            bitmap[da + 49] = bitmap[da + 35];
            bitmap[da + 47] = bitmap[da + 48] = bitmap[da + 34];
            bitmap[da + 46] = bitmap[da + 33];
            bitmap[da + 44] = bitmap[da + 45] = bitmap[da + 32];
        case 4:
            bitmap[da + 43] = bitmap[da + 31];
            bitmap[da + 42] = bitmap[da + 30];
            bitmap[da + 40] = bitmap[da + 41] = bitmap[da + 29];
            bitmap[da + 39] = bitmap[da + 28];
            bitmap[da + 38] = bitmap[da + 27];
            bitmap[da + 36] = bitmap[da + 37] = bitmap[da + 26];
            bitmap[da + 35] = bitmap[da + 25];
            bitmap[da + 33] = bitmap[da + 34] = bitmap[da + 24];
        case 3:
            bitmap[da + 32] = bitmap[da + 23];
            bitmap[da + 31] = bitmap[da + 22];
            bitmap[da + 29] = bitmap[da + 30] = bitmap[da + 21];
            bitmap[da + 28] = bitmap[da + 20];
            bitmap[da + 27] = bitmap[da + 19];
            bitmap[da + 25] = bitmap[da + 26] = bitmap[da + 18];
            bitmap[da + 24] = bitmap[da + 17];
            bitmap[da + 22] = bitmap[da + 23] = bitmap[da + 16];
        case 2:
            bitmap[da + 21] = bitmap[da + 15];
            bitmap[da + 20] = bitmap[da + 14];
            bitmap[da + 18] = bitmap[da + 19] = bitmap[da + 13];
            bitmap[da + 17] = bitmap[da + 12];
            bitmap[da + 16] = bitmap[da + 11];
            bitmap[da + 14] = bitmap[da + 15] = bitmap[da + 10];
            bitmap[da + 13] = bitmap[da + 9];
            bitmap[da + 11] = bitmap[da + 12] = bitmap[da + 8];
        case 1:
            bitmap[da + 10] = bitmap[da + 7];
            bitmap[da + 9] = bitmap[da + 6];
            bitmap[da + 7] = bitmap[da + 8] = bitmap[da + 5];
            bitmap[da + 6] = bitmap[da + 4];
            bitmap[da + 5] = bitmap[da + 3];
            bitmap[da + 3] = bitmap[da + 4] = bitmap[da + 2];
            bitmap[da + 2] = bitmap[da + 1];
            bitmap[da] = bitmap[da + 1] = bitmap[da];
        }
    }

    private void stretch14(int dst) {
        int da = iw * dst;
        switch (dw >> 3) {
        case 128:
            bitmap[da + 1791] = bitmap[da + 1023];
            bitmap[da + 1789] = bitmap[da + 1790] = bitmap[da + 1022];
            bitmap[da + 1787] = bitmap[da + 1788] = bitmap[da + 1021];
            bitmap[da + 1785] = bitmap[da + 1786] = bitmap[da + 1020];
            bitmap[da + 1784] = bitmap[da + 1019];
            bitmap[da + 1782] = bitmap[da + 1783] = bitmap[da + 1018];
            bitmap[da + 1780] = bitmap[da + 1781] = bitmap[da + 1017];
            bitmap[da + 1778] = bitmap[da + 1779] = bitmap[da + 1016];
        case 127:
            bitmap[da + 1777] = bitmap[da + 1015];
            bitmap[da + 1775] = bitmap[da + 1776] = bitmap[da + 1014];
            bitmap[da + 1773] = bitmap[da + 1774] = bitmap[da + 1013];
            bitmap[da + 1771] = bitmap[da + 1772] = bitmap[da + 1012];
            bitmap[da + 1770] = bitmap[da + 1011];
            bitmap[da + 1768] = bitmap[da + 1769] = bitmap[da + 1010];
            bitmap[da + 1766] = bitmap[da + 1767] = bitmap[da + 1009];
            bitmap[da + 1764] = bitmap[da + 1765] = bitmap[da + 1008];
        case 126:
            bitmap[da + 1763] = bitmap[da + 1007];
            bitmap[da + 1761] = bitmap[da + 1762] = bitmap[da + 1006];
            bitmap[da + 1759] = bitmap[da + 1760] = bitmap[da + 1005];
            bitmap[da + 1757] = bitmap[da + 1758] = bitmap[da + 1004];
            bitmap[da + 1756] = bitmap[da + 1003];
            bitmap[da + 1754] = bitmap[da + 1755] = bitmap[da + 1002];
            bitmap[da + 1752] = bitmap[da + 1753] = bitmap[da + 1001];
            bitmap[da + 1750] = bitmap[da + 1751] = bitmap[da + 1000];
        case 125:
            bitmap[da + 1749] = bitmap[da + 999];
            bitmap[da + 1747] = bitmap[da + 1748] = bitmap[da + 998];
            bitmap[da + 1745] = bitmap[da + 1746] = bitmap[da + 997];
            bitmap[da + 1743] = bitmap[da + 1744] = bitmap[da + 996];
            bitmap[da + 1742] = bitmap[da + 995];
            bitmap[da + 1740] = bitmap[da + 1741] = bitmap[da + 994];
            bitmap[da + 1738] = bitmap[da + 1739] = bitmap[da + 993];
            bitmap[da + 1736] = bitmap[da + 1737] = bitmap[da + 992];
        case 124:
            bitmap[da + 1735] = bitmap[da + 991];
            bitmap[da + 1733] = bitmap[da + 1734] = bitmap[da + 990];
            bitmap[da + 1731] = bitmap[da + 1732] = bitmap[da + 989];
            bitmap[da + 1729] = bitmap[da + 1730] = bitmap[da + 988];
            bitmap[da + 1728] = bitmap[da + 987];
            bitmap[da + 1726] = bitmap[da + 1727] = bitmap[da + 986];
            bitmap[da + 1724] = bitmap[da + 1725] = bitmap[da + 985];
            bitmap[da + 1722] = bitmap[da + 1723] = bitmap[da + 984];
        case 123:
            bitmap[da + 1721] = bitmap[da + 983];
            bitmap[da + 1719] = bitmap[da + 1720] = bitmap[da + 982];
            bitmap[da + 1717] = bitmap[da + 1718] = bitmap[da + 981];
            bitmap[da + 1715] = bitmap[da + 1716] = bitmap[da + 980];
            bitmap[da + 1714] = bitmap[da + 979];
            bitmap[da + 1712] = bitmap[da + 1713] = bitmap[da + 978];
            bitmap[da + 1710] = bitmap[da + 1711] = bitmap[da + 977];
            bitmap[da + 1708] = bitmap[da + 1709] = bitmap[da + 976];
        case 122:
            bitmap[da + 1707] = bitmap[da + 975];
            bitmap[da + 1705] = bitmap[da + 1706] = bitmap[da + 974];
            bitmap[da + 1703] = bitmap[da + 1704] = bitmap[da + 973];
            bitmap[da + 1701] = bitmap[da + 1702] = bitmap[da + 972];
            bitmap[da + 1700] = bitmap[da + 971];
            bitmap[da + 1698] = bitmap[da + 1699] = bitmap[da + 970];
            bitmap[da + 1696] = bitmap[da + 1697] = bitmap[da + 969];
            bitmap[da + 1694] = bitmap[da + 1695] = bitmap[da + 968];
        case 121:
            bitmap[da + 1693] = bitmap[da + 967];
            bitmap[da + 1691] = bitmap[da + 1692] = bitmap[da + 966];
            bitmap[da + 1689] = bitmap[da + 1690] = bitmap[da + 965];
            bitmap[da + 1687] = bitmap[da + 1688] = bitmap[da + 964];
            bitmap[da + 1686] = bitmap[da + 963];
            bitmap[da + 1684] = bitmap[da + 1685] = bitmap[da + 962];
            bitmap[da + 1682] = bitmap[da + 1683] = bitmap[da + 961];
            bitmap[da + 1680] = bitmap[da + 1681] = bitmap[da + 960];
        case 120:
            bitmap[da + 1679] = bitmap[da + 959];
            bitmap[da + 1677] = bitmap[da + 1678] = bitmap[da + 958];
            bitmap[da + 1675] = bitmap[da + 1676] = bitmap[da + 957];
            bitmap[da + 1673] = bitmap[da + 1674] = bitmap[da + 956];
            bitmap[da + 1672] = bitmap[da + 955];
            bitmap[da + 1670] = bitmap[da + 1671] = bitmap[da + 954];
            bitmap[da + 1668] = bitmap[da + 1669] = bitmap[da + 953];
            bitmap[da + 1666] = bitmap[da + 1667] = bitmap[da + 952];
        case 119:
            bitmap[da + 1665] = bitmap[da + 951];
            bitmap[da + 1663] = bitmap[da + 1664] = bitmap[da + 950];
            bitmap[da + 1661] = bitmap[da + 1662] = bitmap[da + 949];
            bitmap[da + 1659] = bitmap[da + 1660] = bitmap[da + 948];
            bitmap[da + 1658] = bitmap[da + 947];
            bitmap[da + 1656] = bitmap[da + 1657] = bitmap[da + 946];
            bitmap[da + 1654] = bitmap[da + 1655] = bitmap[da + 945];
            bitmap[da + 1652] = bitmap[da + 1653] = bitmap[da + 944];
        case 118:
            bitmap[da + 1651] = bitmap[da + 943];
            bitmap[da + 1649] = bitmap[da + 1650] = bitmap[da + 942];
            bitmap[da + 1647] = bitmap[da + 1648] = bitmap[da + 941];
            bitmap[da + 1645] = bitmap[da + 1646] = bitmap[da + 940];
            bitmap[da + 1644] = bitmap[da + 939];
            bitmap[da + 1642] = bitmap[da + 1643] = bitmap[da + 938];
            bitmap[da + 1640] = bitmap[da + 1641] = bitmap[da + 937];
            bitmap[da + 1638] = bitmap[da + 1639] = bitmap[da + 936];
        case 117:
            bitmap[da + 1637] = bitmap[da + 935];
            bitmap[da + 1635] = bitmap[da + 1636] = bitmap[da + 934];
            bitmap[da + 1633] = bitmap[da + 1634] = bitmap[da + 933];
            bitmap[da + 1631] = bitmap[da + 1632] = bitmap[da + 932];
            bitmap[da + 1630] = bitmap[da + 931];
            bitmap[da + 1628] = bitmap[da + 1629] = bitmap[da + 930];
            bitmap[da + 1626] = bitmap[da + 1627] = bitmap[da + 929];
            bitmap[da + 1624] = bitmap[da + 1625] = bitmap[da + 928];
        case 116:
            bitmap[da + 1623] = bitmap[da + 927];
            bitmap[da + 1621] = bitmap[da + 1622] = bitmap[da + 926];
            bitmap[da + 1619] = bitmap[da + 1620] = bitmap[da + 925];
            bitmap[da + 1617] = bitmap[da + 1618] = bitmap[da + 924];
            bitmap[da + 1616] = bitmap[da + 923];
            bitmap[da + 1614] = bitmap[da + 1615] = bitmap[da + 922];
            bitmap[da + 1612] = bitmap[da + 1613] = bitmap[da + 921];
            bitmap[da + 1610] = bitmap[da + 1611] = bitmap[da + 920];
        case 115:
            bitmap[da + 1609] = bitmap[da + 919];
            bitmap[da + 1607] = bitmap[da + 1608] = bitmap[da + 918];
            bitmap[da + 1605] = bitmap[da + 1606] = bitmap[da + 917];
            bitmap[da + 1603] = bitmap[da + 1604] = bitmap[da + 916];
            bitmap[da + 1602] = bitmap[da + 915];
            bitmap[da + 1600] = bitmap[da + 1601] = bitmap[da + 914];
            bitmap[da + 1598] = bitmap[da + 1599] = bitmap[da + 913];
            bitmap[da + 1596] = bitmap[da + 1597] = bitmap[da + 912];
        case 114:
            bitmap[da + 1595] = bitmap[da + 911];
            bitmap[da + 1593] = bitmap[da + 1594] = bitmap[da + 910];
            bitmap[da + 1591] = bitmap[da + 1592] = bitmap[da + 909];
            bitmap[da + 1589] = bitmap[da + 1590] = bitmap[da + 908];
            bitmap[da + 1588] = bitmap[da + 907];
            bitmap[da + 1586] = bitmap[da + 1587] = bitmap[da + 906];
            bitmap[da + 1584] = bitmap[da + 1585] = bitmap[da + 905];
            bitmap[da + 1582] = bitmap[da + 1583] = bitmap[da + 904];
        case 113:
            bitmap[da + 1581] = bitmap[da + 903];
            bitmap[da + 1579] = bitmap[da + 1580] = bitmap[da + 902];
            bitmap[da + 1577] = bitmap[da + 1578] = bitmap[da + 901];
            bitmap[da + 1575] = bitmap[da + 1576] = bitmap[da + 900];
            bitmap[da + 1574] = bitmap[da + 899];
            bitmap[da + 1572] = bitmap[da + 1573] = bitmap[da + 898];
            bitmap[da + 1570] = bitmap[da + 1571] = bitmap[da + 897];
            bitmap[da + 1568] = bitmap[da + 1569] = bitmap[da + 896];
        case 112:
            bitmap[da + 1567] = bitmap[da + 895];
            bitmap[da + 1565] = bitmap[da + 1566] = bitmap[da + 894];
            bitmap[da + 1563] = bitmap[da + 1564] = bitmap[da + 893];
            bitmap[da + 1561] = bitmap[da + 1562] = bitmap[da + 892];
            bitmap[da + 1560] = bitmap[da + 891];
            bitmap[da + 1558] = bitmap[da + 1559] = bitmap[da + 890];
            bitmap[da + 1556] = bitmap[da + 1557] = bitmap[da + 889];
            bitmap[da + 1554] = bitmap[da + 1555] = bitmap[da + 888];
        case 111:
            bitmap[da + 1553] = bitmap[da + 887];
            bitmap[da + 1551] = bitmap[da + 1552] = bitmap[da + 886];
            bitmap[da + 1549] = bitmap[da + 1550] = bitmap[da + 885];
            bitmap[da + 1547] = bitmap[da + 1548] = bitmap[da + 884];
            bitmap[da + 1546] = bitmap[da + 883];
            bitmap[da + 1544] = bitmap[da + 1545] = bitmap[da + 882];
            bitmap[da + 1542] = bitmap[da + 1543] = bitmap[da + 881];
            bitmap[da + 1540] = bitmap[da + 1541] = bitmap[da + 880];
        case 110:
            bitmap[da + 1539] = bitmap[da + 879];
            bitmap[da + 1537] = bitmap[da + 1538] = bitmap[da + 878];
            bitmap[da + 1535] = bitmap[da + 1536] = bitmap[da + 877];
            bitmap[da + 1533] = bitmap[da + 1534] = bitmap[da + 876];
            bitmap[da + 1532] = bitmap[da + 875];
            bitmap[da + 1530] = bitmap[da + 1531] = bitmap[da + 874];
            bitmap[da + 1528] = bitmap[da + 1529] = bitmap[da + 873];
            bitmap[da + 1526] = bitmap[da + 1527] = bitmap[da + 872];
        case 109:
            bitmap[da + 1525] = bitmap[da + 871];
            bitmap[da + 1523] = bitmap[da + 1524] = bitmap[da + 870];
            bitmap[da + 1521] = bitmap[da + 1522] = bitmap[da + 869];
            bitmap[da + 1519] = bitmap[da + 1520] = bitmap[da + 868];
            bitmap[da + 1518] = bitmap[da + 867];
            bitmap[da + 1516] = bitmap[da + 1517] = bitmap[da + 866];
            bitmap[da + 1514] = bitmap[da + 1515] = bitmap[da + 865];
            bitmap[da + 1512] = bitmap[da + 1513] = bitmap[da + 864];
        case 108:
            bitmap[da + 1511] = bitmap[da + 863];
            bitmap[da + 1509] = bitmap[da + 1510] = bitmap[da + 862];
            bitmap[da + 1507] = bitmap[da + 1508] = bitmap[da + 861];
            bitmap[da + 1505] = bitmap[da + 1506] = bitmap[da + 860];
            bitmap[da + 1504] = bitmap[da + 859];
            bitmap[da + 1502] = bitmap[da + 1503] = bitmap[da + 858];
            bitmap[da + 1500] = bitmap[da + 1501] = bitmap[da + 857];
            bitmap[da + 1498] = bitmap[da + 1499] = bitmap[da + 856];
        case 107:
            bitmap[da + 1497] = bitmap[da + 855];
            bitmap[da + 1495] = bitmap[da + 1496] = bitmap[da + 854];
            bitmap[da + 1493] = bitmap[da + 1494] = bitmap[da + 853];
            bitmap[da + 1491] = bitmap[da + 1492] = bitmap[da + 852];
            bitmap[da + 1490] = bitmap[da + 851];
            bitmap[da + 1488] = bitmap[da + 1489] = bitmap[da + 850];
            bitmap[da + 1486] = bitmap[da + 1487] = bitmap[da + 849];
            bitmap[da + 1484] = bitmap[da + 1485] = bitmap[da + 848];
        case 106:
            bitmap[da + 1483] = bitmap[da + 847];
            bitmap[da + 1481] = bitmap[da + 1482] = bitmap[da + 846];
            bitmap[da + 1479] = bitmap[da + 1480] = bitmap[da + 845];
            bitmap[da + 1477] = bitmap[da + 1478] = bitmap[da + 844];
            bitmap[da + 1476] = bitmap[da + 843];
            bitmap[da + 1474] = bitmap[da + 1475] = bitmap[da + 842];
            bitmap[da + 1472] = bitmap[da + 1473] = bitmap[da + 841];
            bitmap[da + 1470] = bitmap[da + 1471] = bitmap[da + 840];
        case 105:
            bitmap[da + 1469] = bitmap[da + 839];
            bitmap[da + 1467] = bitmap[da + 1468] = bitmap[da + 838];
            bitmap[da + 1465] = bitmap[da + 1466] = bitmap[da + 837];
            bitmap[da + 1463] = bitmap[da + 1464] = bitmap[da + 836];
            bitmap[da + 1462] = bitmap[da + 835];
            bitmap[da + 1460] = bitmap[da + 1461] = bitmap[da + 834];
            bitmap[da + 1458] = bitmap[da + 1459] = bitmap[da + 833];
            bitmap[da + 1456] = bitmap[da + 1457] = bitmap[da + 832];
        case 104:
            bitmap[da + 1455] = bitmap[da + 831];
            bitmap[da + 1453] = bitmap[da + 1454] = bitmap[da + 830];
            bitmap[da + 1451] = bitmap[da + 1452] = bitmap[da + 829];
            bitmap[da + 1449] = bitmap[da + 1450] = bitmap[da + 828];
            bitmap[da + 1448] = bitmap[da + 827];
            bitmap[da + 1446] = bitmap[da + 1447] = bitmap[da + 826];
            bitmap[da + 1444] = bitmap[da + 1445] = bitmap[da + 825];
            bitmap[da + 1442] = bitmap[da + 1443] = bitmap[da + 824];
        case 103:
            bitmap[da + 1441] = bitmap[da + 823];
            bitmap[da + 1439] = bitmap[da + 1440] = bitmap[da + 822];
            bitmap[da + 1437] = bitmap[da + 1438] = bitmap[da + 821];
            bitmap[da + 1435] = bitmap[da + 1436] = bitmap[da + 820];
            bitmap[da + 1434] = bitmap[da + 819];
            bitmap[da + 1432] = bitmap[da + 1433] = bitmap[da + 818];
            bitmap[da + 1430] = bitmap[da + 1431] = bitmap[da + 817];
            bitmap[da + 1428] = bitmap[da + 1429] = bitmap[da + 816];
        case 102:
            bitmap[da + 1427] = bitmap[da + 815];
            bitmap[da + 1425] = bitmap[da + 1426] = bitmap[da + 814];
            bitmap[da + 1423] = bitmap[da + 1424] = bitmap[da + 813];
            bitmap[da + 1421] = bitmap[da + 1422] = bitmap[da + 812];
            bitmap[da + 1420] = bitmap[da + 811];
            bitmap[da + 1418] = bitmap[da + 1419] = bitmap[da + 810];
            bitmap[da + 1416] = bitmap[da + 1417] = bitmap[da + 809];
            bitmap[da + 1414] = bitmap[da + 1415] = bitmap[da + 808];
        case 101:
            bitmap[da + 1413] = bitmap[da + 807];
            bitmap[da + 1411] = bitmap[da + 1412] = bitmap[da + 806];
            bitmap[da + 1409] = bitmap[da + 1410] = bitmap[da + 805];
            bitmap[da + 1407] = bitmap[da + 1408] = bitmap[da + 804];
            bitmap[da + 1406] = bitmap[da + 803];
            bitmap[da + 1404] = bitmap[da + 1405] = bitmap[da + 802];
            bitmap[da + 1402] = bitmap[da + 1403] = bitmap[da + 801];
            bitmap[da + 1400] = bitmap[da + 1401] = bitmap[da + 800];
        case 100:
            bitmap[da + 1399] = bitmap[da + 799];
            bitmap[da + 1397] = bitmap[da + 1398] = bitmap[da + 798];
            bitmap[da + 1395] = bitmap[da + 1396] = bitmap[da + 797];
            bitmap[da + 1393] = bitmap[da + 1394] = bitmap[da + 796];
            bitmap[da + 1392] = bitmap[da + 795];
            bitmap[da + 1390] = bitmap[da + 1391] = bitmap[da + 794];
            bitmap[da + 1388] = bitmap[da + 1389] = bitmap[da + 793];
            bitmap[da + 1386] = bitmap[da + 1387] = bitmap[da + 792];
        case 99:
            bitmap[da + 1385] = bitmap[da + 791];
            bitmap[da + 1383] = bitmap[da + 1384] = bitmap[da + 790];
            bitmap[da + 1381] = bitmap[da + 1382] = bitmap[da + 789];
            bitmap[da + 1379] = bitmap[da + 1380] = bitmap[da + 788];
            bitmap[da + 1378] = bitmap[da + 787];
            bitmap[da + 1376] = bitmap[da + 1377] = bitmap[da + 786];
            bitmap[da + 1374] = bitmap[da + 1375] = bitmap[da + 785];
            bitmap[da + 1372] = bitmap[da + 1373] = bitmap[da + 784];
        case 98:
            bitmap[da + 1371] = bitmap[da + 783];
            bitmap[da + 1369] = bitmap[da + 1370] = bitmap[da + 782];
            bitmap[da + 1367] = bitmap[da + 1368] = bitmap[da + 781];
            bitmap[da + 1365] = bitmap[da + 1366] = bitmap[da + 780];
            bitmap[da + 1364] = bitmap[da + 779];
            bitmap[da + 1362] = bitmap[da + 1363] = bitmap[da + 778];
            bitmap[da + 1360] = bitmap[da + 1361] = bitmap[da + 777];
            bitmap[da + 1358] = bitmap[da + 1359] = bitmap[da + 776];
        case 97:
            bitmap[da + 1357] = bitmap[da + 775];
            bitmap[da + 1355] = bitmap[da + 1356] = bitmap[da + 774];
            bitmap[da + 1353] = bitmap[da + 1354] = bitmap[da + 773];
            bitmap[da + 1351] = bitmap[da + 1352] = bitmap[da + 772];
            bitmap[da + 1350] = bitmap[da + 771];
            bitmap[da + 1348] = bitmap[da + 1349] = bitmap[da + 770];
            bitmap[da + 1346] = bitmap[da + 1347] = bitmap[da + 769];
            bitmap[da + 1344] = bitmap[da + 1345] = bitmap[da + 768];
        case 96:
            bitmap[da + 1343] = bitmap[da + 767];
            bitmap[da + 1341] = bitmap[da + 1342] = bitmap[da + 766];
            bitmap[da + 1339] = bitmap[da + 1340] = bitmap[da + 765];
            bitmap[da + 1337] = bitmap[da + 1338] = bitmap[da + 764];
            bitmap[da + 1336] = bitmap[da + 763];
            bitmap[da + 1334] = bitmap[da + 1335] = bitmap[da + 762];
            bitmap[da + 1332] = bitmap[da + 1333] = bitmap[da + 761];
            bitmap[da + 1330] = bitmap[da + 1331] = bitmap[da + 760];
        case 95:
            bitmap[da + 1329] = bitmap[da + 759];
            bitmap[da + 1327] = bitmap[da + 1328] = bitmap[da + 758];
            bitmap[da + 1325] = bitmap[da + 1326] = bitmap[da + 757];
            bitmap[da + 1323] = bitmap[da + 1324] = bitmap[da + 756];
            bitmap[da + 1322] = bitmap[da + 755];
            bitmap[da + 1320] = bitmap[da + 1321] = bitmap[da + 754];
            bitmap[da + 1318] = bitmap[da + 1319] = bitmap[da + 753];
            bitmap[da + 1316] = bitmap[da + 1317] = bitmap[da + 752];
        case 94:
            bitmap[da + 1315] = bitmap[da + 751];
            bitmap[da + 1313] = bitmap[da + 1314] = bitmap[da + 750];
            bitmap[da + 1311] = bitmap[da + 1312] = bitmap[da + 749];
            bitmap[da + 1309] = bitmap[da + 1310] = bitmap[da + 748];
            bitmap[da + 1308] = bitmap[da + 747];
            bitmap[da + 1306] = bitmap[da + 1307] = bitmap[da + 746];
            bitmap[da + 1304] = bitmap[da + 1305] = bitmap[da + 745];
            bitmap[da + 1302] = bitmap[da + 1303] = bitmap[da + 744];
        case 93:
            bitmap[da + 1301] = bitmap[da + 743];
            bitmap[da + 1299] = bitmap[da + 1300] = bitmap[da + 742];
            bitmap[da + 1297] = bitmap[da + 1298] = bitmap[da + 741];
            bitmap[da + 1295] = bitmap[da + 1296] = bitmap[da + 740];
            bitmap[da + 1294] = bitmap[da + 739];
            bitmap[da + 1292] = bitmap[da + 1293] = bitmap[da + 738];
            bitmap[da + 1290] = bitmap[da + 1291] = bitmap[da + 737];
            bitmap[da + 1288] = bitmap[da + 1289] = bitmap[da + 736];
        case 92:
            bitmap[da + 1287] = bitmap[da + 735];
            bitmap[da + 1285] = bitmap[da + 1286] = bitmap[da + 734];
            bitmap[da + 1283] = bitmap[da + 1284] = bitmap[da + 733];
            bitmap[da + 1281] = bitmap[da + 1282] = bitmap[da + 732];
            bitmap[da + 1280] = bitmap[da + 731];
            bitmap[da + 1278] = bitmap[da + 1279] = bitmap[da + 730];
            bitmap[da + 1276] = bitmap[da + 1277] = bitmap[da + 729];
            bitmap[da + 1274] = bitmap[da + 1275] = bitmap[da + 728];
        case 91:
            bitmap[da + 1273] = bitmap[da + 727];
            bitmap[da + 1271] = bitmap[da + 1272] = bitmap[da + 726];
            bitmap[da + 1269] = bitmap[da + 1270] = bitmap[da + 725];
            bitmap[da + 1267] = bitmap[da + 1268] = bitmap[da + 724];
            bitmap[da + 1266] = bitmap[da + 723];
            bitmap[da + 1264] = bitmap[da + 1265] = bitmap[da + 722];
            bitmap[da + 1262] = bitmap[da + 1263] = bitmap[da + 721];
            bitmap[da + 1260] = bitmap[da + 1261] = bitmap[da + 720];
        case 90:
            bitmap[da + 1259] = bitmap[da + 719];
            bitmap[da + 1257] = bitmap[da + 1258] = bitmap[da + 718];
            bitmap[da + 1255] = bitmap[da + 1256] = bitmap[da + 717];
            bitmap[da + 1253] = bitmap[da + 1254] = bitmap[da + 716];
            bitmap[da + 1252] = bitmap[da + 715];
            bitmap[da + 1250] = bitmap[da + 1251] = bitmap[da + 714];
            bitmap[da + 1248] = bitmap[da + 1249] = bitmap[da + 713];
            bitmap[da + 1246] = bitmap[da + 1247] = bitmap[da + 712];
        case 89:
            bitmap[da + 1245] = bitmap[da + 711];
            bitmap[da + 1243] = bitmap[da + 1244] = bitmap[da + 710];
            bitmap[da + 1241] = bitmap[da + 1242] = bitmap[da + 709];
            bitmap[da + 1239] = bitmap[da + 1240] = bitmap[da + 708];
            bitmap[da + 1238] = bitmap[da + 707];
            bitmap[da + 1236] = bitmap[da + 1237] = bitmap[da + 706];
            bitmap[da + 1234] = bitmap[da + 1235] = bitmap[da + 705];
            bitmap[da + 1232] = bitmap[da + 1233] = bitmap[da + 704];
        case 88:
            bitmap[da + 1231] = bitmap[da + 703];
            bitmap[da + 1229] = bitmap[da + 1230] = bitmap[da + 702];
            bitmap[da + 1227] = bitmap[da + 1228] = bitmap[da + 701];
            bitmap[da + 1225] = bitmap[da + 1226] = bitmap[da + 700];
            bitmap[da + 1224] = bitmap[da + 699];
            bitmap[da + 1222] = bitmap[da + 1223] = bitmap[da + 698];
            bitmap[da + 1220] = bitmap[da + 1221] = bitmap[da + 697];
            bitmap[da + 1218] = bitmap[da + 1219] = bitmap[da + 696];
        case 87:
            bitmap[da + 1217] = bitmap[da + 695];
            bitmap[da + 1215] = bitmap[da + 1216] = bitmap[da + 694];
            bitmap[da + 1213] = bitmap[da + 1214] = bitmap[da + 693];
            bitmap[da + 1211] = bitmap[da + 1212] = bitmap[da + 692];
            bitmap[da + 1210] = bitmap[da + 691];
            bitmap[da + 1208] = bitmap[da + 1209] = bitmap[da + 690];
            bitmap[da + 1206] = bitmap[da + 1207] = bitmap[da + 689];
            bitmap[da + 1204] = bitmap[da + 1205] = bitmap[da + 688];
        case 86:
            bitmap[da + 1203] = bitmap[da + 687];
            bitmap[da + 1201] = bitmap[da + 1202] = bitmap[da + 686];
            bitmap[da + 1199] = bitmap[da + 1200] = bitmap[da + 685];
            bitmap[da + 1197] = bitmap[da + 1198] = bitmap[da + 684];
            bitmap[da + 1196] = bitmap[da + 683];
            bitmap[da + 1194] = bitmap[da + 1195] = bitmap[da + 682];
            bitmap[da + 1192] = bitmap[da + 1193] = bitmap[da + 681];
            bitmap[da + 1190] = bitmap[da + 1191] = bitmap[da + 680];
        case 85:
            bitmap[da + 1189] = bitmap[da + 679];
            bitmap[da + 1187] = bitmap[da + 1188] = bitmap[da + 678];
            bitmap[da + 1185] = bitmap[da + 1186] = bitmap[da + 677];
            bitmap[da + 1183] = bitmap[da + 1184] = bitmap[da + 676];
            bitmap[da + 1182] = bitmap[da + 675];
            bitmap[da + 1180] = bitmap[da + 1181] = bitmap[da + 674];
            bitmap[da + 1178] = bitmap[da + 1179] = bitmap[da + 673];
            bitmap[da + 1176] = bitmap[da + 1177] = bitmap[da + 672];
        case 84:
            bitmap[da + 1175] = bitmap[da + 671];
            bitmap[da + 1173] = bitmap[da + 1174] = bitmap[da + 670];
            bitmap[da + 1171] = bitmap[da + 1172] = bitmap[da + 669];
            bitmap[da + 1169] = bitmap[da + 1170] = bitmap[da + 668];
            bitmap[da + 1168] = bitmap[da + 667];
            bitmap[da + 1166] = bitmap[da + 1167] = bitmap[da + 666];
            bitmap[da + 1164] = bitmap[da + 1165] = bitmap[da + 665];
            bitmap[da + 1162] = bitmap[da + 1163] = bitmap[da + 664];
        case 83:
            bitmap[da + 1161] = bitmap[da + 663];
            bitmap[da + 1159] = bitmap[da + 1160] = bitmap[da + 662];
            bitmap[da + 1157] = bitmap[da + 1158] = bitmap[da + 661];
            bitmap[da + 1155] = bitmap[da + 1156] = bitmap[da + 660];
            bitmap[da + 1154] = bitmap[da + 659];
            bitmap[da + 1152] = bitmap[da + 1153] = bitmap[da + 658];
            bitmap[da + 1150] = bitmap[da + 1151] = bitmap[da + 657];
            bitmap[da + 1148] = bitmap[da + 1149] = bitmap[da + 656];
        case 82:
            bitmap[da + 1147] = bitmap[da + 655];
            bitmap[da + 1145] = bitmap[da + 1146] = bitmap[da + 654];
            bitmap[da + 1143] = bitmap[da + 1144] = bitmap[da + 653];
            bitmap[da + 1141] = bitmap[da + 1142] = bitmap[da + 652];
            bitmap[da + 1140] = bitmap[da + 651];
            bitmap[da + 1138] = bitmap[da + 1139] = bitmap[da + 650];
            bitmap[da + 1136] = bitmap[da + 1137] = bitmap[da + 649];
            bitmap[da + 1134] = bitmap[da + 1135] = bitmap[da + 648];
        case 81:
            bitmap[da + 1133] = bitmap[da + 647];
            bitmap[da + 1131] = bitmap[da + 1132] = bitmap[da + 646];
            bitmap[da + 1129] = bitmap[da + 1130] = bitmap[da + 645];
            bitmap[da + 1127] = bitmap[da + 1128] = bitmap[da + 644];
            bitmap[da + 1126] = bitmap[da + 643];
            bitmap[da + 1124] = bitmap[da + 1125] = bitmap[da + 642];
            bitmap[da + 1122] = bitmap[da + 1123] = bitmap[da + 641];
            bitmap[da + 1120] = bitmap[da + 1121] = bitmap[da + 640];
        case 80:
            bitmap[da + 1119] = bitmap[da + 639];
            bitmap[da + 1117] = bitmap[da + 1118] = bitmap[da + 638];
            bitmap[da + 1115] = bitmap[da + 1116] = bitmap[da + 637];
            bitmap[da + 1113] = bitmap[da + 1114] = bitmap[da + 636];
            bitmap[da + 1112] = bitmap[da + 635];
            bitmap[da + 1110] = bitmap[da + 1111] = bitmap[da + 634];
            bitmap[da + 1108] = bitmap[da + 1109] = bitmap[da + 633];
            bitmap[da + 1106] = bitmap[da + 1107] = bitmap[da + 632];
        case 79:
            bitmap[da + 1105] = bitmap[da + 631];
            bitmap[da + 1103] = bitmap[da + 1104] = bitmap[da + 630];
            bitmap[da + 1101] = bitmap[da + 1102] = bitmap[da + 629];
            bitmap[da + 1099] = bitmap[da + 1100] = bitmap[da + 628];
            bitmap[da + 1098] = bitmap[da + 627];
            bitmap[da + 1096] = bitmap[da + 1097] = bitmap[da + 626];
            bitmap[da + 1094] = bitmap[da + 1095] = bitmap[da + 625];
            bitmap[da + 1092] = bitmap[da + 1093] = bitmap[da + 624];
        case 78:
            bitmap[da + 1091] = bitmap[da + 623];
            bitmap[da + 1089] = bitmap[da + 1090] = bitmap[da + 622];
            bitmap[da + 1087] = bitmap[da + 1088] = bitmap[da + 621];
            bitmap[da + 1085] = bitmap[da + 1086] = bitmap[da + 620];
            bitmap[da + 1084] = bitmap[da + 619];
            bitmap[da + 1082] = bitmap[da + 1083] = bitmap[da + 618];
            bitmap[da + 1080] = bitmap[da + 1081] = bitmap[da + 617];
            bitmap[da + 1078] = bitmap[da + 1079] = bitmap[da + 616];
        case 77:
            bitmap[da + 1077] = bitmap[da + 615];
            bitmap[da + 1075] = bitmap[da + 1076] = bitmap[da + 614];
            bitmap[da + 1073] = bitmap[da + 1074] = bitmap[da + 613];
            bitmap[da + 1071] = bitmap[da + 1072] = bitmap[da + 612];
            bitmap[da + 1070] = bitmap[da + 611];
            bitmap[da + 1068] = bitmap[da + 1069] = bitmap[da + 610];
            bitmap[da + 1066] = bitmap[da + 1067] = bitmap[da + 609];
            bitmap[da + 1064] = bitmap[da + 1065] = bitmap[da + 608];
        case 76:
            bitmap[da + 1063] = bitmap[da + 607];
            bitmap[da + 1061] = bitmap[da + 1062] = bitmap[da + 606];
            bitmap[da + 1059] = bitmap[da + 1060] = bitmap[da + 605];
            bitmap[da + 1057] = bitmap[da + 1058] = bitmap[da + 604];
            bitmap[da + 1056] = bitmap[da + 603];
            bitmap[da + 1054] = bitmap[da + 1055] = bitmap[da + 602];
            bitmap[da + 1052] = bitmap[da + 1053] = bitmap[da + 601];
            bitmap[da + 1050] = bitmap[da + 1051] = bitmap[da + 600];
        case 75:
            bitmap[da + 1049] = bitmap[da + 599];
            bitmap[da + 1047] = bitmap[da + 1048] = bitmap[da + 598];
            bitmap[da + 1045] = bitmap[da + 1046] = bitmap[da + 597];
            bitmap[da + 1043] = bitmap[da + 1044] = bitmap[da + 596];
            bitmap[da + 1042] = bitmap[da + 595];
            bitmap[da + 1040] = bitmap[da + 1041] = bitmap[da + 594];
            bitmap[da + 1038] = bitmap[da + 1039] = bitmap[da + 593];
            bitmap[da + 1036] = bitmap[da + 1037] = bitmap[da + 592];
        case 74:
            bitmap[da + 1035] = bitmap[da + 591];
            bitmap[da + 1033] = bitmap[da + 1034] = bitmap[da + 590];
            bitmap[da + 1031] = bitmap[da + 1032] = bitmap[da + 589];
            bitmap[da + 1029] = bitmap[da + 1030] = bitmap[da + 588];
            bitmap[da + 1028] = bitmap[da + 587];
            bitmap[da + 1026] = bitmap[da + 1027] = bitmap[da + 586];
            bitmap[da + 1024] = bitmap[da + 1025] = bitmap[da + 585];
            bitmap[da + 1022] = bitmap[da + 1023] = bitmap[da + 584];
        case 73:
            bitmap[da + 1021] = bitmap[da + 583];
            bitmap[da + 1019] = bitmap[da + 1020] = bitmap[da + 582];
            bitmap[da + 1017] = bitmap[da + 1018] = bitmap[da + 581];
            bitmap[da + 1015] = bitmap[da + 1016] = bitmap[da + 580];
            bitmap[da + 1014] = bitmap[da + 579];
            bitmap[da + 1012] = bitmap[da + 1013] = bitmap[da + 578];
            bitmap[da + 1010] = bitmap[da + 1011] = bitmap[da + 577];
            bitmap[da + 1008] = bitmap[da + 1009] = bitmap[da + 576];
        case 72:
            bitmap[da + 1007] = bitmap[da + 575];
            bitmap[da + 1005] = bitmap[da + 1006] = bitmap[da + 574];
            bitmap[da + 1003] = bitmap[da + 1004] = bitmap[da + 573];
            bitmap[da + 1001] = bitmap[da + 1002] = bitmap[da + 572];
            bitmap[da + 1000] = bitmap[da + 571];
            bitmap[da + 998] = bitmap[da + 999] = bitmap[da + 570];
            bitmap[da + 996] = bitmap[da + 997] = bitmap[da + 569];
            bitmap[da + 994] = bitmap[da + 995] = bitmap[da + 568];
        case 71:
            bitmap[da + 993] = bitmap[da + 567];
            bitmap[da + 991] = bitmap[da + 992] = bitmap[da + 566];
            bitmap[da + 989] = bitmap[da + 990] = bitmap[da + 565];
            bitmap[da + 987] = bitmap[da + 988] = bitmap[da + 564];
            bitmap[da + 986] = bitmap[da + 563];
            bitmap[da + 984] = bitmap[da + 985] = bitmap[da + 562];
            bitmap[da + 982] = bitmap[da + 983] = bitmap[da + 561];
            bitmap[da + 980] = bitmap[da + 981] = bitmap[da + 560];
        case 70:
            bitmap[da + 979] = bitmap[da + 559];
            bitmap[da + 977] = bitmap[da + 978] = bitmap[da + 558];
            bitmap[da + 975] = bitmap[da + 976] = bitmap[da + 557];
            bitmap[da + 973] = bitmap[da + 974] = bitmap[da + 556];
            bitmap[da + 972] = bitmap[da + 555];
            bitmap[da + 970] = bitmap[da + 971] = bitmap[da + 554];
            bitmap[da + 968] = bitmap[da + 969] = bitmap[da + 553];
            bitmap[da + 966] = bitmap[da + 967] = bitmap[da + 552];
        case 69:
            bitmap[da + 965] = bitmap[da + 551];
            bitmap[da + 963] = bitmap[da + 964] = bitmap[da + 550];
            bitmap[da + 961] = bitmap[da + 962] = bitmap[da + 549];
            bitmap[da + 959] = bitmap[da + 960] = bitmap[da + 548];
            bitmap[da + 958] = bitmap[da + 547];
            bitmap[da + 956] = bitmap[da + 957] = bitmap[da + 546];
            bitmap[da + 954] = bitmap[da + 955] = bitmap[da + 545];
            bitmap[da + 952] = bitmap[da + 953] = bitmap[da + 544];
        case 68:
            bitmap[da + 951] = bitmap[da + 543];
            bitmap[da + 949] = bitmap[da + 950] = bitmap[da + 542];
            bitmap[da + 947] = bitmap[da + 948] = bitmap[da + 541];
            bitmap[da + 945] = bitmap[da + 946] = bitmap[da + 540];
            bitmap[da + 944] = bitmap[da + 539];
            bitmap[da + 942] = bitmap[da + 943] = bitmap[da + 538];
            bitmap[da + 940] = bitmap[da + 941] = bitmap[da + 537];
            bitmap[da + 938] = bitmap[da + 939] = bitmap[da + 536];
        case 67:
            bitmap[da + 937] = bitmap[da + 535];
            bitmap[da + 935] = bitmap[da + 936] = bitmap[da + 534];
            bitmap[da + 933] = bitmap[da + 934] = bitmap[da + 533];
            bitmap[da + 931] = bitmap[da + 932] = bitmap[da + 532];
            bitmap[da + 930] = bitmap[da + 531];
            bitmap[da + 928] = bitmap[da + 929] = bitmap[da + 530];
            bitmap[da + 926] = bitmap[da + 927] = bitmap[da + 529];
            bitmap[da + 924] = bitmap[da + 925] = bitmap[da + 528];
        case 66:
            bitmap[da + 923] = bitmap[da + 527];
            bitmap[da + 921] = bitmap[da + 922] = bitmap[da + 526];
            bitmap[da + 919] = bitmap[da + 920] = bitmap[da + 525];
            bitmap[da + 917] = bitmap[da + 918] = bitmap[da + 524];
            bitmap[da + 916] = bitmap[da + 523];
            bitmap[da + 914] = bitmap[da + 915] = bitmap[da + 522];
            bitmap[da + 912] = bitmap[da + 913] = bitmap[da + 521];
            bitmap[da + 910] = bitmap[da + 911] = bitmap[da + 520];
        case 65:
            bitmap[da + 909] = bitmap[da + 519];
            bitmap[da + 907] = bitmap[da + 908] = bitmap[da + 518];
            bitmap[da + 905] = bitmap[da + 906] = bitmap[da + 517];
            bitmap[da + 903] = bitmap[da + 904] = bitmap[da + 516];
            bitmap[da + 902] = bitmap[da + 515];
            bitmap[da + 900] = bitmap[da + 901] = bitmap[da + 514];
            bitmap[da + 898] = bitmap[da + 899] = bitmap[da + 513];
            bitmap[da + 896] = bitmap[da + 897] = bitmap[da + 512];
        case 64:
            bitmap[da + 895] = bitmap[da + 511];
            bitmap[da + 893] = bitmap[da + 894] = bitmap[da + 510];
            bitmap[da + 891] = bitmap[da + 892] = bitmap[da + 509];
            bitmap[da + 889] = bitmap[da + 890] = bitmap[da + 508];
            bitmap[da + 888] = bitmap[da + 507];
            bitmap[da + 886] = bitmap[da + 887] = bitmap[da + 506];
            bitmap[da + 884] = bitmap[da + 885] = bitmap[da + 505];
            bitmap[da + 882] = bitmap[da + 883] = bitmap[da + 504];
        case 63:
            bitmap[da + 881] = bitmap[da + 503];
            bitmap[da + 879] = bitmap[da + 880] = bitmap[da + 502];
            bitmap[da + 877] = bitmap[da + 878] = bitmap[da + 501];
            bitmap[da + 875] = bitmap[da + 876] = bitmap[da + 500];
            bitmap[da + 874] = bitmap[da + 499];
            bitmap[da + 872] = bitmap[da + 873] = bitmap[da + 498];
            bitmap[da + 870] = bitmap[da + 871] = bitmap[da + 497];
            bitmap[da + 868] = bitmap[da + 869] = bitmap[da + 496];
        case 62:
            bitmap[da + 867] = bitmap[da + 495];
            bitmap[da + 865] = bitmap[da + 866] = bitmap[da + 494];
            bitmap[da + 863] = bitmap[da + 864] = bitmap[da + 493];
            bitmap[da + 861] = bitmap[da + 862] = bitmap[da + 492];
            bitmap[da + 860] = bitmap[da + 491];
            bitmap[da + 858] = bitmap[da + 859] = bitmap[da + 490];
            bitmap[da + 856] = bitmap[da + 857] = bitmap[da + 489];
            bitmap[da + 854] = bitmap[da + 855] = bitmap[da + 488];
        case 61:
            bitmap[da + 853] = bitmap[da + 487];
            bitmap[da + 851] = bitmap[da + 852] = bitmap[da + 486];
            bitmap[da + 849] = bitmap[da + 850] = bitmap[da + 485];
            bitmap[da + 847] = bitmap[da + 848] = bitmap[da + 484];
            bitmap[da + 846] = bitmap[da + 483];
            bitmap[da + 844] = bitmap[da + 845] = bitmap[da + 482];
            bitmap[da + 842] = bitmap[da + 843] = bitmap[da + 481];
            bitmap[da + 840] = bitmap[da + 841] = bitmap[da + 480];
        case 60:
            bitmap[da + 839] = bitmap[da + 479];
            bitmap[da + 837] = bitmap[da + 838] = bitmap[da + 478];
            bitmap[da + 835] = bitmap[da + 836] = bitmap[da + 477];
            bitmap[da + 833] = bitmap[da + 834] = bitmap[da + 476];
            bitmap[da + 832] = bitmap[da + 475];
            bitmap[da + 830] = bitmap[da + 831] = bitmap[da + 474];
            bitmap[da + 828] = bitmap[da + 829] = bitmap[da + 473];
            bitmap[da + 826] = bitmap[da + 827] = bitmap[da + 472];
        case 59:
            bitmap[da + 825] = bitmap[da + 471];
            bitmap[da + 823] = bitmap[da + 824] = bitmap[da + 470];
            bitmap[da + 821] = bitmap[da + 822] = bitmap[da + 469];
            bitmap[da + 819] = bitmap[da + 820] = bitmap[da + 468];
            bitmap[da + 818] = bitmap[da + 467];
            bitmap[da + 816] = bitmap[da + 817] = bitmap[da + 466];
            bitmap[da + 814] = bitmap[da + 815] = bitmap[da + 465];
            bitmap[da + 812] = bitmap[da + 813] = bitmap[da + 464];
        case 58:
            bitmap[da + 811] = bitmap[da + 463];
            bitmap[da + 809] = bitmap[da + 810] = bitmap[da + 462];
            bitmap[da + 807] = bitmap[da + 808] = bitmap[da + 461];
            bitmap[da + 805] = bitmap[da + 806] = bitmap[da + 460];
            bitmap[da + 804] = bitmap[da + 459];
            bitmap[da + 802] = bitmap[da + 803] = bitmap[da + 458];
            bitmap[da + 800] = bitmap[da + 801] = bitmap[da + 457];
            bitmap[da + 798] = bitmap[da + 799] = bitmap[da + 456];
        case 57:
            bitmap[da + 797] = bitmap[da + 455];
            bitmap[da + 795] = bitmap[da + 796] = bitmap[da + 454];
            bitmap[da + 793] = bitmap[da + 794] = bitmap[da + 453];
            bitmap[da + 791] = bitmap[da + 792] = bitmap[da + 452];
            bitmap[da + 790] = bitmap[da + 451];
            bitmap[da + 788] = bitmap[da + 789] = bitmap[da + 450];
            bitmap[da + 786] = bitmap[da + 787] = bitmap[da + 449];
            bitmap[da + 784] = bitmap[da + 785] = bitmap[da + 448];
        case 56:
            bitmap[da + 783] = bitmap[da + 447];
            bitmap[da + 781] = bitmap[da + 782] = bitmap[da + 446];
            bitmap[da + 779] = bitmap[da + 780] = bitmap[da + 445];
            bitmap[da + 777] = bitmap[da + 778] = bitmap[da + 444];
            bitmap[da + 776] = bitmap[da + 443];
            bitmap[da + 774] = bitmap[da + 775] = bitmap[da + 442];
            bitmap[da + 772] = bitmap[da + 773] = bitmap[da + 441];
            bitmap[da + 770] = bitmap[da + 771] = bitmap[da + 440];
        case 55:
            bitmap[da + 769] = bitmap[da + 439];
            bitmap[da + 767] = bitmap[da + 768] = bitmap[da + 438];
            bitmap[da + 765] = bitmap[da + 766] = bitmap[da + 437];
            bitmap[da + 763] = bitmap[da + 764] = bitmap[da + 436];
            bitmap[da + 762] = bitmap[da + 435];
            bitmap[da + 760] = bitmap[da + 761] = bitmap[da + 434];
            bitmap[da + 758] = bitmap[da + 759] = bitmap[da + 433];
            bitmap[da + 756] = bitmap[da + 757] = bitmap[da + 432];
        case 54:
            bitmap[da + 755] = bitmap[da + 431];
            bitmap[da + 753] = bitmap[da + 754] = bitmap[da + 430];
            bitmap[da + 751] = bitmap[da + 752] = bitmap[da + 429];
            bitmap[da + 749] = bitmap[da + 750] = bitmap[da + 428];
            bitmap[da + 748] = bitmap[da + 427];
            bitmap[da + 746] = bitmap[da + 747] = bitmap[da + 426];
            bitmap[da + 744] = bitmap[da + 745] = bitmap[da + 425];
            bitmap[da + 742] = bitmap[da + 743] = bitmap[da + 424];
        case 53:
            bitmap[da + 741] = bitmap[da + 423];
            bitmap[da + 739] = bitmap[da + 740] = bitmap[da + 422];
            bitmap[da + 737] = bitmap[da + 738] = bitmap[da + 421];
            bitmap[da + 735] = bitmap[da + 736] = bitmap[da + 420];
            bitmap[da + 734] = bitmap[da + 419];
            bitmap[da + 732] = bitmap[da + 733] = bitmap[da + 418];
            bitmap[da + 730] = bitmap[da + 731] = bitmap[da + 417];
            bitmap[da + 728] = bitmap[da + 729] = bitmap[da + 416];
        case 52:
            bitmap[da + 727] = bitmap[da + 415];
            bitmap[da + 725] = bitmap[da + 726] = bitmap[da + 414];
            bitmap[da + 723] = bitmap[da + 724] = bitmap[da + 413];
            bitmap[da + 721] = bitmap[da + 722] = bitmap[da + 412];
            bitmap[da + 720] = bitmap[da + 411];
            bitmap[da + 718] = bitmap[da + 719] = bitmap[da + 410];
            bitmap[da + 716] = bitmap[da + 717] = bitmap[da + 409];
            bitmap[da + 714] = bitmap[da + 715] = bitmap[da + 408];
        case 51:
            bitmap[da + 713] = bitmap[da + 407];
            bitmap[da + 711] = bitmap[da + 712] = bitmap[da + 406];
            bitmap[da + 709] = bitmap[da + 710] = bitmap[da + 405];
            bitmap[da + 707] = bitmap[da + 708] = bitmap[da + 404];
            bitmap[da + 706] = bitmap[da + 403];
            bitmap[da + 704] = bitmap[da + 705] = bitmap[da + 402];
            bitmap[da + 702] = bitmap[da + 703] = bitmap[da + 401];
            bitmap[da + 700] = bitmap[da + 701] = bitmap[da + 400];
        case 50:
            bitmap[da + 699] = bitmap[da + 399];
            bitmap[da + 697] = bitmap[da + 698] = bitmap[da + 398];
            bitmap[da + 695] = bitmap[da + 696] = bitmap[da + 397];
            bitmap[da + 693] = bitmap[da + 694] = bitmap[da + 396];
            bitmap[da + 692] = bitmap[da + 395];
            bitmap[da + 690] = bitmap[da + 691] = bitmap[da + 394];
            bitmap[da + 688] = bitmap[da + 689] = bitmap[da + 393];
            bitmap[da + 686] = bitmap[da + 687] = bitmap[da + 392];
        case 49:
            bitmap[da + 685] = bitmap[da + 391];
            bitmap[da + 683] = bitmap[da + 684] = bitmap[da + 390];
            bitmap[da + 681] = bitmap[da + 682] = bitmap[da + 389];
            bitmap[da + 679] = bitmap[da + 680] = bitmap[da + 388];
            bitmap[da + 678] = bitmap[da + 387];
            bitmap[da + 676] = bitmap[da + 677] = bitmap[da + 386];
            bitmap[da + 674] = bitmap[da + 675] = bitmap[da + 385];
            bitmap[da + 672] = bitmap[da + 673] = bitmap[da + 384];
        case 48:
            bitmap[da + 671] = bitmap[da + 383];
            bitmap[da + 669] = bitmap[da + 670] = bitmap[da + 382];
            bitmap[da + 667] = bitmap[da + 668] = bitmap[da + 381];
            bitmap[da + 665] = bitmap[da + 666] = bitmap[da + 380];
            bitmap[da + 664] = bitmap[da + 379];
            bitmap[da + 662] = bitmap[da + 663] = bitmap[da + 378];
            bitmap[da + 660] = bitmap[da + 661] = bitmap[da + 377];
            bitmap[da + 658] = bitmap[da + 659] = bitmap[da + 376];
        case 47:
            bitmap[da + 657] = bitmap[da + 375];
            bitmap[da + 655] = bitmap[da + 656] = bitmap[da + 374];
            bitmap[da + 653] = bitmap[da + 654] = bitmap[da + 373];
            bitmap[da + 651] = bitmap[da + 652] = bitmap[da + 372];
            bitmap[da + 650] = bitmap[da + 371];
            bitmap[da + 648] = bitmap[da + 649] = bitmap[da + 370];
            bitmap[da + 646] = bitmap[da + 647] = bitmap[da + 369];
            bitmap[da + 644] = bitmap[da + 645] = bitmap[da + 368];
        case 46:
            bitmap[da + 643] = bitmap[da + 367];
            bitmap[da + 641] = bitmap[da + 642] = bitmap[da + 366];
            bitmap[da + 639] = bitmap[da + 640] = bitmap[da + 365];
            bitmap[da + 637] = bitmap[da + 638] = bitmap[da + 364];
            bitmap[da + 636] = bitmap[da + 363];
            bitmap[da + 634] = bitmap[da + 635] = bitmap[da + 362];
            bitmap[da + 632] = bitmap[da + 633] = bitmap[da + 361];
            bitmap[da + 630] = bitmap[da + 631] = bitmap[da + 360];
        case 45:
            bitmap[da + 629] = bitmap[da + 359];
            bitmap[da + 627] = bitmap[da + 628] = bitmap[da + 358];
            bitmap[da + 625] = bitmap[da + 626] = bitmap[da + 357];
            bitmap[da + 623] = bitmap[da + 624] = bitmap[da + 356];
            bitmap[da + 622] = bitmap[da + 355];
            bitmap[da + 620] = bitmap[da + 621] = bitmap[da + 354];
            bitmap[da + 618] = bitmap[da + 619] = bitmap[da + 353];
            bitmap[da + 616] = bitmap[da + 617] = bitmap[da + 352];
        case 44:
            bitmap[da + 615] = bitmap[da + 351];
            bitmap[da + 613] = bitmap[da + 614] = bitmap[da + 350];
            bitmap[da + 611] = bitmap[da + 612] = bitmap[da + 349];
            bitmap[da + 609] = bitmap[da + 610] = bitmap[da + 348];
            bitmap[da + 608] = bitmap[da + 347];
            bitmap[da + 606] = bitmap[da + 607] = bitmap[da + 346];
            bitmap[da + 604] = bitmap[da + 605] = bitmap[da + 345];
            bitmap[da + 602] = bitmap[da + 603] = bitmap[da + 344];
        case 43:
            bitmap[da + 601] = bitmap[da + 343];
            bitmap[da + 599] = bitmap[da + 600] = bitmap[da + 342];
            bitmap[da + 597] = bitmap[da + 598] = bitmap[da + 341];
            bitmap[da + 595] = bitmap[da + 596] = bitmap[da + 340];
            bitmap[da + 594] = bitmap[da + 339];
            bitmap[da + 592] = bitmap[da + 593] = bitmap[da + 338];
            bitmap[da + 590] = bitmap[da + 591] = bitmap[da + 337];
            bitmap[da + 588] = bitmap[da + 589] = bitmap[da + 336];
        case 42:
            bitmap[da + 587] = bitmap[da + 335];
            bitmap[da + 585] = bitmap[da + 586] = bitmap[da + 334];
            bitmap[da + 583] = bitmap[da + 584] = bitmap[da + 333];
            bitmap[da + 581] = bitmap[da + 582] = bitmap[da + 332];
            bitmap[da + 580] = bitmap[da + 331];
            bitmap[da + 578] = bitmap[da + 579] = bitmap[da + 330];
            bitmap[da + 576] = bitmap[da + 577] = bitmap[da + 329];
            bitmap[da + 574] = bitmap[da + 575] = bitmap[da + 328];
        case 41:
            bitmap[da + 573] = bitmap[da + 327];
            bitmap[da + 571] = bitmap[da + 572] = bitmap[da + 326];
            bitmap[da + 569] = bitmap[da + 570] = bitmap[da + 325];
            bitmap[da + 567] = bitmap[da + 568] = bitmap[da + 324];
            bitmap[da + 566] = bitmap[da + 323];
            bitmap[da + 564] = bitmap[da + 565] = bitmap[da + 322];
            bitmap[da + 562] = bitmap[da + 563] = bitmap[da + 321];
            bitmap[da + 560] = bitmap[da + 561] = bitmap[da + 320];
        case 40:
            bitmap[da + 559] = bitmap[da + 319];
            bitmap[da + 557] = bitmap[da + 558] = bitmap[da + 318];
            bitmap[da + 555] = bitmap[da + 556] = bitmap[da + 317];
            bitmap[da + 553] = bitmap[da + 554] = bitmap[da + 316];
            bitmap[da + 552] = bitmap[da + 315];
            bitmap[da + 550] = bitmap[da + 551] = bitmap[da + 314];
            bitmap[da + 548] = bitmap[da + 549] = bitmap[da + 313];
            bitmap[da + 546] = bitmap[da + 547] = bitmap[da + 312];
        case 39:
            bitmap[da + 545] = bitmap[da + 311];
            bitmap[da + 543] = bitmap[da + 544] = bitmap[da + 310];
            bitmap[da + 541] = bitmap[da + 542] = bitmap[da + 309];
            bitmap[da + 539] = bitmap[da + 540] = bitmap[da + 308];
            bitmap[da + 538] = bitmap[da + 307];
            bitmap[da + 536] = bitmap[da + 537] = bitmap[da + 306];
            bitmap[da + 534] = bitmap[da + 535] = bitmap[da + 305];
            bitmap[da + 532] = bitmap[da + 533] = bitmap[da + 304];
        case 38:
            bitmap[da + 531] = bitmap[da + 303];
            bitmap[da + 529] = bitmap[da + 530] = bitmap[da + 302];
            bitmap[da + 527] = bitmap[da + 528] = bitmap[da + 301];
            bitmap[da + 525] = bitmap[da + 526] = bitmap[da + 300];
            bitmap[da + 524] = bitmap[da + 299];
            bitmap[da + 522] = bitmap[da + 523] = bitmap[da + 298];
            bitmap[da + 520] = bitmap[da + 521] = bitmap[da + 297];
            bitmap[da + 518] = bitmap[da + 519] = bitmap[da + 296];
        case 37:
            bitmap[da + 517] = bitmap[da + 295];
            bitmap[da + 515] = bitmap[da + 516] = bitmap[da + 294];
            bitmap[da + 513] = bitmap[da + 514] = bitmap[da + 293];
            bitmap[da + 511] = bitmap[da + 512] = bitmap[da + 292];
            bitmap[da + 510] = bitmap[da + 291];
            bitmap[da + 508] = bitmap[da + 509] = bitmap[da + 290];
            bitmap[da + 506] = bitmap[da + 507] = bitmap[da + 289];
            bitmap[da + 504] = bitmap[da + 505] = bitmap[da + 288];
        case 36:
            bitmap[da + 503] = bitmap[da + 287];
            bitmap[da + 501] = bitmap[da + 502] = bitmap[da + 286];
            bitmap[da + 499] = bitmap[da + 500] = bitmap[da + 285];
            bitmap[da + 497] = bitmap[da + 498] = bitmap[da + 284];
            bitmap[da + 496] = bitmap[da + 283];
            bitmap[da + 494] = bitmap[da + 495] = bitmap[da + 282];
            bitmap[da + 492] = bitmap[da + 493] = bitmap[da + 281];
            bitmap[da + 490] = bitmap[da + 491] = bitmap[da + 280];
        case 35:
            bitmap[da + 489] = bitmap[da + 279];
            bitmap[da + 487] = bitmap[da + 488] = bitmap[da + 278];
            bitmap[da + 485] = bitmap[da + 486] = bitmap[da + 277];
            bitmap[da + 483] = bitmap[da + 484] = bitmap[da + 276];
            bitmap[da + 482] = bitmap[da + 275];
            bitmap[da + 480] = bitmap[da + 481] = bitmap[da + 274];
            bitmap[da + 478] = bitmap[da + 479] = bitmap[da + 273];
            bitmap[da + 476] = bitmap[da + 477] = bitmap[da + 272];
        case 34:
            bitmap[da + 475] = bitmap[da + 271];
            bitmap[da + 473] = bitmap[da + 474] = bitmap[da + 270];
            bitmap[da + 471] = bitmap[da + 472] = bitmap[da + 269];
            bitmap[da + 469] = bitmap[da + 470] = bitmap[da + 268];
            bitmap[da + 468] = bitmap[da + 267];
            bitmap[da + 466] = bitmap[da + 467] = bitmap[da + 266];
            bitmap[da + 464] = bitmap[da + 465] = bitmap[da + 265];
            bitmap[da + 462] = bitmap[da + 463] = bitmap[da + 264];
        case 33:
            bitmap[da + 461] = bitmap[da + 263];
            bitmap[da + 459] = bitmap[da + 460] = bitmap[da + 262];
            bitmap[da + 457] = bitmap[da + 458] = bitmap[da + 261];
            bitmap[da + 455] = bitmap[da + 456] = bitmap[da + 260];
            bitmap[da + 454] = bitmap[da + 259];
            bitmap[da + 452] = bitmap[da + 453] = bitmap[da + 258];
            bitmap[da + 450] = bitmap[da + 451] = bitmap[da + 257];
            bitmap[da + 448] = bitmap[da + 449] = bitmap[da + 256];
        case 32:
            bitmap[da + 447] = bitmap[da + 255];
            bitmap[da + 445] = bitmap[da + 446] = bitmap[da + 254];
            bitmap[da + 443] = bitmap[da + 444] = bitmap[da + 253];
            bitmap[da + 441] = bitmap[da + 442] = bitmap[da + 252];
            bitmap[da + 440] = bitmap[da + 251];
            bitmap[da + 438] = bitmap[da + 439] = bitmap[da + 250];
            bitmap[da + 436] = bitmap[da + 437] = bitmap[da + 249];
            bitmap[da + 434] = bitmap[da + 435] = bitmap[da + 248];
        case 31:
            bitmap[da + 433] = bitmap[da + 247];
            bitmap[da + 431] = bitmap[da + 432] = bitmap[da + 246];
            bitmap[da + 429] = bitmap[da + 430] = bitmap[da + 245];
            bitmap[da + 427] = bitmap[da + 428] = bitmap[da + 244];
            bitmap[da + 426] = bitmap[da + 243];
            bitmap[da + 424] = bitmap[da + 425] = bitmap[da + 242];
            bitmap[da + 422] = bitmap[da + 423] = bitmap[da + 241];
            bitmap[da + 420] = bitmap[da + 421] = bitmap[da + 240];
        case 30:
            bitmap[da + 419] = bitmap[da + 239];
            bitmap[da + 417] = bitmap[da + 418] = bitmap[da + 238];
            bitmap[da + 415] = bitmap[da + 416] = bitmap[da + 237];
            bitmap[da + 413] = bitmap[da + 414] = bitmap[da + 236];
            bitmap[da + 412] = bitmap[da + 235];
            bitmap[da + 410] = bitmap[da + 411] = bitmap[da + 234];
            bitmap[da + 408] = bitmap[da + 409] = bitmap[da + 233];
            bitmap[da + 406] = bitmap[da + 407] = bitmap[da + 232];
        case 29:
            bitmap[da + 405] = bitmap[da + 231];
            bitmap[da + 403] = bitmap[da + 404] = bitmap[da + 230];
            bitmap[da + 401] = bitmap[da + 402] = bitmap[da + 229];
            bitmap[da + 399] = bitmap[da + 400] = bitmap[da + 228];
            bitmap[da + 398] = bitmap[da + 227];
            bitmap[da + 396] = bitmap[da + 397] = bitmap[da + 226];
            bitmap[da + 394] = bitmap[da + 395] = bitmap[da + 225];
            bitmap[da + 392] = bitmap[da + 393] = bitmap[da + 224];
        case 28:
            bitmap[da + 391] = bitmap[da + 223];
            bitmap[da + 389] = bitmap[da + 390] = bitmap[da + 222];
            bitmap[da + 387] = bitmap[da + 388] = bitmap[da + 221];
            bitmap[da + 385] = bitmap[da + 386] = bitmap[da + 220];
            bitmap[da + 384] = bitmap[da + 219];
            bitmap[da + 382] = bitmap[da + 383] = bitmap[da + 218];
            bitmap[da + 380] = bitmap[da + 381] = bitmap[da + 217];
            bitmap[da + 378] = bitmap[da + 379] = bitmap[da + 216];
        case 27:
            bitmap[da + 377] = bitmap[da + 215];
            bitmap[da + 375] = bitmap[da + 376] = bitmap[da + 214];
            bitmap[da + 373] = bitmap[da + 374] = bitmap[da + 213];
            bitmap[da + 371] = bitmap[da + 372] = bitmap[da + 212];
            bitmap[da + 370] = bitmap[da + 211];
            bitmap[da + 368] = bitmap[da + 369] = bitmap[da + 210];
            bitmap[da + 366] = bitmap[da + 367] = bitmap[da + 209];
            bitmap[da + 364] = bitmap[da + 365] = bitmap[da + 208];
        case 26:
            bitmap[da + 363] = bitmap[da + 207];
            bitmap[da + 361] = bitmap[da + 362] = bitmap[da + 206];
            bitmap[da + 359] = bitmap[da + 360] = bitmap[da + 205];
            bitmap[da + 357] = bitmap[da + 358] = bitmap[da + 204];
            bitmap[da + 356] = bitmap[da + 203];
            bitmap[da + 354] = bitmap[da + 355] = bitmap[da + 202];
            bitmap[da + 352] = bitmap[da + 353] = bitmap[da + 201];
            bitmap[da + 350] = bitmap[da + 351] = bitmap[da + 200];
        case 25:
            bitmap[da + 349] = bitmap[da + 199];
            bitmap[da + 347] = bitmap[da + 348] = bitmap[da + 198];
            bitmap[da + 345] = bitmap[da + 346] = bitmap[da + 197];
            bitmap[da + 343] = bitmap[da + 344] = bitmap[da + 196];
            bitmap[da + 342] = bitmap[da + 195];
            bitmap[da + 340] = bitmap[da + 341] = bitmap[da + 194];
            bitmap[da + 338] = bitmap[da + 339] = bitmap[da + 193];
            bitmap[da + 336] = bitmap[da + 337] = bitmap[da + 192];
        case 24:
            bitmap[da + 335] = bitmap[da + 191];
            bitmap[da + 333] = bitmap[da + 334] = bitmap[da + 190];
            bitmap[da + 331] = bitmap[da + 332] = bitmap[da + 189];
            bitmap[da + 329] = bitmap[da + 330] = bitmap[da + 188];
            bitmap[da + 328] = bitmap[da + 187];
            bitmap[da + 326] = bitmap[da + 327] = bitmap[da + 186];
            bitmap[da + 324] = bitmap[da + 325] = bitmap[da + 185];
            bitmap[da + 322] = bitmap[da + 323] = bitmap[da + 184];
        case 23:
            bitmap[da + 321] = bitmap[da + 183];
            bitmap[da + 319] = bitmap[da + 320] = bitmap[da + 182];
            bitmap[da + 317] = bitmap[da + 318] = bitmap[da + 181];
            bitmap[da + 315] = bitmap[da + 316] = bitmap[da + 180];
            bitmap[da + 314] = bitmap[da + 179];
            bitmap[da + 312] = bitmap[da + 313] = bitmap[da + 178];
            bitmap[da + 310] = bitmap[da + 311] = bitmap[da + 177];
            bitmap[da + 308] = bitmap[da + 309] = bitmap[da + 176];
        case 22:
            bitmap[da + 307] = bitmap[da + 175];
            bitmap[da + 305] = bitmap[da + 306] = bitmap[da + 174];
            bitmap[da + 303] = bitmap[da + 304] = bitmap[da + 173];
            bitmap[da + 301] = bitmap[da + 302] = bitmap[da + 172];
            bitmap[da + 300] = bitmap[da + 171];
            bitmap[da + 298] = bitmap[da + 299] = bitmap[da + 170];
            bitmap[da + 296] = bitmap[da + 297] = bitmap[da + 169];
            bitmap[da + 294] = bitmap[da + 295] = bitmap[da + 168];
        case 21:
            bitmap[da + 293] = bitmap[da + 167];
            bitmap[da + 291] = bitmap[da + 292] = bitmap[da + 166];
            bitmap[da + 289] = bitmap[da + 290] = bitmap[da + 165];
            bitmap[da + 287] = bitmap[da + 288] = bitmap[da + 164];
            bitmap[da + 286] = bitmap[da + 163];
            bitmap[da + 284] = bitmap[da + 285] = bitmap[da + 162];
            bitmap[da + 282] = bitmap[da + 283] = bitmap[da + 161];
            bitmap[da + 280] = bitmap[da + 281] = bitmap[da + 160];
        case 20:
            bitmap[da + 279] = bitmap[da + 159];
            bitmap[da + 277] = bitmap[da + 278] = bitmap[da + 158];
            bitmap[da + 275] = bitmap[da + 276] = bitmap[da + 157];
            bitmap[da + 273] = bitmap[da + 274] = bitmap[da + 156];
            bitmap[da + 272] = bitmap[da + 155];
            bitmap[da + 270] = bitmap[da + 271] = bitmap[da + 154];
            bitmap[da + 268] = bitmap[da + 269] = bitmap[da + 153];
            bitmap[da + 266] = bitmap[da + 267] = bitmap[da + 152];
        case 19:
            bitmap[da + 265] = bitmap[da + 151];
            bitmap[da + 263] = bitmap[da + 264] = bitmap[da + 150];
            bitmap[da + 261] = bitmap[da + 262] = bitmap[da + 149];
            bitmap[da + 259] = bitmap[da + 260] = bitmap[da + 148];
            bitmap[da + 258] = bitmap[da + 147];
            bitmap[da + 256] = bitmap[da + 257] = bitmap[da + 146];
            bitmap[da + 254] = bitmap[da + 255] = bitmap[da + 145];
            bitmap[da + 252] = bitmap[da + 253] = bitmap[da + 144];
        case 18:
            bitmap[da + 251] = bitmap[da + 143];
            bitmap[da + 249] = bitmap[da + 250] = bitmap[da + 142];
            bitmap[da + 247] = bitmap[da + 248] = bitmap[da + 141];
            bitmap[da + 245] = bitmap[da + 246] = bitmap[da + 140];
            bitmap[da + 244] = bitmap[da + 139];
            bitmap[da + 242] = bitmap[da + 243] = bitmap[da + 138];
            bitmap[da + 240] = bitmap[da + 241] = bitmap[da + 137];
            bitmap[da + 238] = bitmap[da + 239] = bitmap[da + 136];
        case 17:
            bitmap[da + 237] = bitmap[da + 135];
            bitmap[da + 235] = bitmap[da + 236] = bitmap[da + 134];
            bitmap[da + 233] = bitmap[da + 234] = bitmap[da + 133];
            bitmap[da + 231] = bitmap[da + 232] = bitmap[da + 132];
            bitmap[da + 230] = bitmap[da + 131];
            bitmap[da + 228] = bitmap[da + 229] = bitmap[da + 130];
            bitmap[da + 226] = bitmap[da + 227] = bitmap[da + 129];
            bitmap[da + 224] = bitmap[da + 225] = bitmap[da + 128];
        case 16:
            bitmap[da + 223] = bitmap[da + 127];
            bitmap[da + 221] = bitmap[da + 222] = bitmap[da + 126];
            bitmap[da + 219] = bitmap[da + 220] = bitmap[da + 125];
            bitmap[da + 217] = bitmap[da + 218] = bitmap[da + 124];
            bitmap[da + 216] = bitmap[da + 123];
            bitmap[da + 214] = bitmap[da + 215] = bitmap[da + 122];
            bitmap[da + 212] = bitmap[da + 213] = bitmap[da + 121];
            bitmap[da + 210] = bitmap[da + 211] = bitmap[da + 120];
        case 15:
            bitmap[da + 209] = bitmap[da + 119];
            bitmap[da + 207] = bitmap[da + 208] = bitmap[da + 118];
            bitmap[da + 205] = bitmap[da + 206] = bitmap[da + 117];
            bitmap[da + 203] = bitmap[da + 204] = bitmap[da + 116];
            bitmap[da + 202] = bitmap[da + 115];
            bitmap[da + 200] = bitmap[da + 201] = bitmap[da + 114];
            bitmap[da + 198] = bitmap[da + 199] = bitmap[da + 113];
            bitmap[da + 196] = bitmap[da + 197] = bitmap[da + 112];
        case 14:
            bitmap[da + 195] = bitmap[da + 111];
            bitmap[da + 193] = bitmap[da + 194] = bitmap[da + 110];
            bitmap[da + 191] = bitmap[da + 192] = bitmap[da + 109];
            bitmap[da + 189] = bitmap[da + 190] = bitmap[da + 108];
            bitmap[da + 188] = bitmap[da + 107];
            bitmap[da + 186] = bitmap[da + 187] = bitmap[da + 106];
            bitmap[da + 184] = bitmap[da + 185] = bitmap[da + 105];
            bitmap[da + 182] = bitmap[da + 183] = bitmap[da + 104];
        case 13:
            bitmap[da + 181] = bitmap[da + 103];
            bitmap[da + 179] = bitmap[da + 180] = bitmap[da + 102];
            bitmap[da + 177] = bitmap[da + 178] = bitmap[da + 101];
            bitmap[da + 175] = bitmap[da + 176] = bitmap[da + 100];
            bitmap[da + 174] = bitmap[da + 99];
            bitmap[da + 172] = bitmap[da + 173] = bitmap[da + 98];
            bitmap[da + 170] = bitmap[da + 171] = bitmap[da + 97];
            bitmap[da + 168] = bitmap[da + 169] = bitmap[da + 96];
        case 12:
            bitmap[da + 167] = bitmap[da + 95];
            bitmap[da + 165] = bitmap[da + 166] = bitmap[da + 94];
            bitmap[da + 163] = bitmap[da + 164] = bitmap[da + 93];
            bitmap[da + 161] = bitmap[da + 162] = bitmap[da + 92];
            bitmap[da + 160] = bitmap[da + 91];
            bitmap[da + 158] = bitmap[da + 159] = bitmap[da + 90];
            bitmap[da + 156] = bitmap[da + 157] = bitmap[da + 89];
            bitmap[da + 154] = bitmap[da + 155] = bitmap[da + 88];
        case 11:
            bitmap[da + 153] = bitmap[da + 87];
            bitmap[da + 151] = bitmap[da + 152] = bitmap[da + 86];
            bitmap[da + 149] = bitmap[da + 150] = bitmap[da + 85];
            bitmap[da + 147] = bitmap[da + 148] = bitmap[da + 84];
            bitmap[da + 146] = bitmap[da + 83];
            bitmap[da + 144] = bitmap[da + 145] = bitmap[da + 82];
            bitmap[da + 142] = bitmap[da + 143] = bitmap[da + 81];
            bitmap[da + 140] = bitmap[da + 141] = bitmap[da + 80];
        case 10:
            bitmap[da + 139] = bitmap[da + 79];
            bitmap[da + 137] = bitmap[da + 138] = bitmap[da + 78];
            bitmap[da + 135] = bitmap[da + 136] = bitmap[da + 77];
            bitmap[da + 133] = bitmap[da + 134] = bitmap[da + 76];
            bitmap[da + 132] = bitmap[da + 75];
            bitmap[da + 130] = bitmap[da + 131] = bitmap[da + 74];
            bitmap[da + 128] = bitmap[da + 129] = bitmap[da + 73];
            bitmap[da + 126] = bitmap[da + 127] = bitmap[da + 72];
        case 9:
            bitmap[da + 125] = bitmap[da + 71];
            bitmap[da + 123] = bitmap[da + 124] = bitmap[da + 70];
            bitmap[da + 121] = bitmap[da + 122] = bitmap[da + 69];
            bitmap[da + 119] = bitmap[da + 120] = bitmap[da + 68];
            bitmap[da + 118] = bitmap[da + 67];
            bitmap[da + 116] = bitmap[da + 117] = bitmap[da + 66];
            bitmap[da + 114] = bitmap[da + 115] = bitmap[da + 65];
            bitmap[da + 112] = bitmap[da + 113] = bitmap[da + 64];
        case 8:
            bitmap[da + 111] = bitmap[da + 63];
            bitmap[da + 109] = bitmap[da + 110] = bitmap[da + 62];
            bitmap[da + 107] = bitmap[da + 108] = bitmap[da + 61];
            bitmap[da + 105] = bitmap[da + 106] = bitmap[da + 60];
            bitmap[da + 104] = bitmap[da + 59];
            bitmap[da + 102] = bitmap[da + 103] = bitmap[da + 58];
            bitmap[da + 100] = bitmap[da + 101] = bitmap[da + 57];
            bitmap[da + 98] = bitmap[da + 99] = bitmap[da + 56];
        case 7:
            bitmap[da + 97] = bitmap[da + 55];
            bitmap[da + 95] = bitmap[da + 96] = bitmap[da + 54];
            bitmap[da + 93] = bitmap[da + 94] = bitmap[da + 53];
            bitmap[da + 91] = bitmap[da + 92] = bitmap[da + 52];
            bitmap[da + 90] = bitmap[da + 51];
            bitmap[da + 88] = bitmap[da + 89] = bitmap[da + 50];
            bitmap[da + 86] = bitmap[da + 87] = bitmap[da + 49];
            bitmap[da + 84] = bitmap[da + 85] = bitmap[da + 48];
        case 6:
            bitmap[da + 83] = bitmap[da + 47];
            bitmap[da + 81] = bitmap[da + 82] = bitmap[da + 46];
            bitmap[da + 79] = bitmap[da + 80] = bitmap[da + 45];
            bitmap[da + 77] = bitmap[da + 78] = bitmap[da + 44];
            bitmap[da + 76] = bitmap[da + 43];
            bitmap[da + 74] = bitmap[da + 75] = bitmap[da + 42];
            bitmap[da + 72] = bitmap[da + 73] = bitmap[da + 41];
            bitmap[da + 70] = bitmap[da + 71] = bitmap[da + 40];
        case 5:
            bitmap[da + 69] = bitmap[da + 39];
            bitmap[da + 67] = bitmap[da + 68] = bitmap[da + 38];
            bitmap[da + 65] = bitmap[da + 66] = bitmap[da + 37];
            bitmap[da + 63] = bitmap[da + 64] = bitmap[da + 36];
            bitmap[da + 62] = bitmap[da + 35];
            bitmap[da + 60] = bitmap[da + 61] = bitmap[da + 34];
            bitmap[da + 58] = bitmap[da + 59] = bitmap[da + 33];
            bitmap[da + 56] = bitmap[da + 57] = bitmap[da + 32];
        case 4:
            bitmap[da + 55] = bitmap[da + 31];
            bitmap[da + 53] = bitmap[da + 54] = bitmap[da + 30];
            bitmap[da + 51] = bitmap[da + 52] = bitmap[da + 29];
            bitmap[da + 49] = bitmap[da + 50] = bitmap[da + 28];
            bitmap[da + 48] = bitmap[da + 27];
            bitmap[da + 46] = bitmap[da + 47] = bitmap[da + 26];
            bitmap[da + 44] = bitmap[da + 45] = bitmap[da + 25];
            bitmap[da + 42] = bitmap[da + 43] = bitmap[da + 24];
        case 3:
            bitmap[da + 41] = bitmap[da + 23];
            bitmap[da + 39] = bitmap[da + 40] = bitmap[da + 22];
            bitmap[da + 37] = bitmap[da + 38] = bitmap[da + 21];
            bitmap[da + 35] = bitmap[da + 36] = bitmap[da + 20];
            bitmap[da + 34] = bitmap[da + 19];
            bitmap[da + 32] = bitmap[da + 33] = bitmap[da + 18];
            bitmap[da + 30] = bitmap[da + 31] = bitmap[da + 17];
            bitmap[da + 28] = bitmap[da + 29] = bitmap[da + 16];
        case 2:
            bitmap[da + 27] = bitmap[da + 15];
            bitmap[da + 25] = bitmap[da + 26] = bitmap[da + 14];
            bitmap[da + 23] = bitmap[da + 24] = bitmap[da + 13];
            bitmap[da + 21] = bitmap[da + 22] = bitmap[da + 12];
            bitmap[da + 20] = bitmap[da + 11];
            bitmap[da + 18] = bitmap[da + 19] = bitmap[da + 10];
            bitmap[da + 16] = bitmap[da + 17] = bitmap[da + 9];
            bitmap[da + 14] = bitmap[da + 15] = bitmap[da + 8];
        case 1:
            bitmap[da + 13] = bitmap[da + 7];
            bitmap[da + 11] = bitmap[da + 12] = bitmap[da + 6];
            bitmap[da + 9] = bitmap[da + 10] = bitmap[da + 5];
            bitmap[da + 7] = bitmap[da + 8] = bitmap[da + 4];
            bitmap[da + 6] = bitmap[da + 3];
            bitmap[da + 4] = bitmap[da + 5] = bitmap[da + 2];
            bitmap[da + 2] = bitmap[da + 3] = bitmap[da + 1];
            bitmap[da] = bitmap[da + 1] = bitmap[da];
        }
    }

    private void stretch21(int dst) {
        int da = iw * dst;
        switch (dw >> 3) {
        case 128:
            bitmap[da + 2686] = bitmap[da + 2687] = bitmap[da + 1023];
            bitmap[da + 2683] = bitmap[da + 2684] = bitmap[da + 2685] = bitmap[da + 1022];
            bitmap[da + 2681] = bitmap[da + 2682] = bitmap[da + 1021];
            bitmap[da + 2678] = bitmap[da + 2679] = bitmap[da + 2680] = bitmap[da + 1020];
            bitmap[da + 2675] = bitmap[da + 2676] = bitmap[da + 2677] = bitmap[da + 1019];
            bitmap[da + 2673] = bitmap[da + 2674] = bitmap[da + 1018];
            bitmap[da + 2670] = bitmap[da + 2671] = bitmap[da + 2672] = bitmap[da + 1017];
            bitmap[da + 2667] = bitmap[da + 2668] = bitmap[da + 2669] = bitmap[da + 1016];
        case 127:
            bitmap[da + 2665] = bitmap[da + 2666] = bitmap[da + 1015];
            bitmap[da + 2662] = bitmap[da + 2663] = bitmap[da + 2664] = bitmap[da + 1014];
            bitmap[da + 2660] = bitmap[da + 2661] = bitmap[da + 1013];
            bitmap[da + 2657] = bitmap[da + 2658] = bitmap[da + 2659] = bitmap[da + 1012];
            bitmap[da + 2654] = bitmap[da + 2655] = bitmap[da + 2656] = bitmap[da + 1011];
            bitmap[da + 2652] = bitmap[da + 2653] = bitmap[da + 1010];
            bitmap[da + 2649] = bitmap[da + 2650] = bitmap[da + 2651] = bitmap[da + 1009];
            bitmap[da + 2646] = bitmap[da + 2647] = bitmap[da + 2648] = bitmap[da + 1008];
        case 126:
            bitmap[da + 2644] = bitmap[da + 2645] = bitmap[da + 1007];
            bitmap[da + 2641] = bitmap[da + 2642] = bitmap[da + 2643] = bitmap[da + 1006];
            bitmap[da + 2639] = bitmap[da + 2640] = bitmap[da + 1005];
            bitmap[da + 2636] = bitmap[da + 2637] = bitmap[da + 2638] = bitmap[da + 1004];
            bitmap[da + 2633] = bitmap[da + 2634] = bitmap[da + 2635] = bitmap[da + 1003];
            bitmap[da + 2631] = bitmap[da + 2632] = bitmap[da + 1002];
            bitmap[da + 2628] = bitmap[da + 2629] = bitmap[da + 2630] = bitmap[da + 1001];
            bitmap[da + 2625] = bitmap[da + 2626] = bitmap[da + 2627] = bitmap[da + 1000];
        case 125:
            bitmap[da + 2623] = bitmap[da + 2624] = bitmap[da + 999];
            bitmap[da + 2620] = bitmap[da + 2621] = bitmap[da + 2622] = bitmap[da + 998];
            bitmap[da + 2618] = bitmap[da + 2619] = bitmap[da + 997];
            bitmap[da + 2615] = bitmap[da + 2616] = bitmap[da + 2617] = bitmap[da + 996];
            bitmap[da + 2612] = bitmap[da + 2613] = bitmap[da + 2614] = bitmap[da + 995];
            bitmap[da + 2610] = bitmap[da + 2611] = bitmap[da + 994];
            bitmap[da + 2607] = bitmap[da + 2608] = bitmap[da + 2609] = bitmap[da + 993];
            bitmap[da + 2604] = bitmap[da + 2605] = bitmap[da + 2606] = bitmap[da + 992];
        case 124:
            bitmap[da + 2602] = bitmap[da + 2603] = bitmap[da + 991];
            bitmap[da + 2599] = bitmap[da + 2600] = bitmap[da + 2601] = bitmap[da + 990];
            bitmap[da + 2597] = bitmap[da + 2598] = bitmap[da + 989];
            bitmap[da + 2594] = bitmap[da + 2595] = bitmap[da + 2596] = bitmap[da + 988];
            bitmap[da + 2591] = bitmap[da + 2592] = bitmap[da + 2593] = bitmap[da + 987];
            bitmap[da + 2589] = bitmap[da + 2590] = bitmap[da + 986];
            bitmap[da + 2586] = bitmap[da + 2587] = bitmap[da + 2588] = bitmap[da + 985];
            bitmap[da + 2583] = bitmap[da + 2584] = bitmap[da + 2585] = bitmap[da + 984];
        case 123:
            bitmap[da + 2581] = bitmap[da + 2582] = bitmap[da + 983];
            bitmap[da + 2578] = bitmap[da + 2579] = bitmap[da + 2580] = bitmap[da + 982];
            bitmap[da + 2576] = bitmap[da + 2577] = bitmap[da + 981];
            bitmap[da + 2573] = bitmap[da + 2574] = bitmap[da + 2575] = bitmap[da + 980];
            bitmap[da + 2570] = bitmap[da + 2571] = bitmap[da + 2572] = bitmap[da + 979];
            bitmap[da + 2568] = bitmap[da + 2569] = bitmap[da + 978];
            bitmap[da + 2565] = bitmap[da + 2566] = bitmap[da + 2567] = bitmap[da + 977];
            bitmap[da + 2562] = bitmap[da + 2563] = bitmap[da + 2564] = bitmap[da + 976];
        case 122:
            bitmap[da + 2560] = bitmap[da + 2561] = bitmap[da + 975];
            bitmap[da + 2557] = bitmap[da + 2558] = bitmap[da + 2559] = bitmap[da + 974];
            bitmap[da + 2555] = bitmap[da + 2556] = bitmap[da + 973];
            bitmap[da + 2552] = bitmap[da + 2553] = bitmap[da + 2554] = bitmap[da + 972];
            bitmap[da + 2549] = bitmap[da + 2550] = bitmap[da + 2551] = bitmap[da + 971];
            bitmap[da + 2547] = bitmap[da + 2548] = bitmap[da + 970];
            bitmap[da + 2544] = bitmap[da + 2545] = bitmap[da + 2546] = bitmap[da + 969];
            bitmap[da + 2541] = bitmap[da + 2542] = bitmap[da + 2543] = bitmap[da + 968];
        case 121:
            bitmap[da + 2539] = bitmap[da + 2540] = bitmap[da + 967];
            bitmap[da + 2536] = bitmap[da + 2537] = bitmap[da + 2538] = bitmap[da + 966];
            bitmap[da + 2534] = bitmap[da + 2535] = bitmap[da + 965];
            bitmap[da + 2531] = bitmap[da + 2532] = bitmap[da + 2533] = bitmap[da + 964];
            bitmap[da + 2528] = bitmap[da + 2529] = bitmap[da + 2530] = bitmap[da + 963];
            bitmap[da + 2526] = bitmap[da + 2527] = bitmap[da + 962];
            bitmap[da + 2523] = bitmap[da + 2524] = bitmap[da + 2525] = bitmap[da + 961];
            bitmap[da + 2520] = bitmap[da + 2521] = bitmap[da + 2522] = bitmap[da + 960];
        case 120:
            bitmap[da + 2518] = bitmap[da + 2519] = bitmap[da + 959];
            bitmap[da + 2515] = bitmap[da + 2516] = bitmap[da + 2517] = bitmap[da + 958];
            bitmap[da + 2513] = bitmap[da + 2514] = bitmap[da + 957];
            bitmap[da + 2510] = bitmap[da + 2511] = bitmap[da + 2512] = bitmap[da + 956];
            bitmap[da + 2507] = bitmap[da + 2508] = bitmap[da + 2509] = bitmap[da + 955];
            bitmap[da + 2505] = bitmap[da + 2506] = bitmap[da + 954];
            bitmap[da + 2502] = bitmap[da + 2503] = bitmap[da + 2504] = bitmap[da + 953];
            bitmap[da + 2499] = bitmap[da + 2500] = bitmap[da + 2501] = bitmap[da + 952];
        case 119:
            bitmap[da + 2497] = bitmap[da + 2498] = bitmap[da + 951];
            bitmap[da + 2494] = bitmap[da + 2495] = bitmap[da + 2496] = bitmap[da + 950];
            bitmap[da + 2492] = bitmap[da + 2493] = bitmap[da + 949];
            bitmap[da + 2489] = bitmap[da + 2490] = bitmap[da + 2491] = bitmap[da + 948];
            bitmap[da + 2486] = bitmap[da + 2487] = bitmap[da + 2488] = bitmap[da + 947];
            bitmap[da + 2484] = bitmap[da + 2485] = bitmap[da + 946];
            bitmap[da + 2481] = bitmap[da + 2482] = bitmap[da + 2483] = bitmap[da + 945];
            bitmap[da + 2478] = bitmap[da + 2479] = bitmap[da + 2480] = bitmap[da + 944];
        case 118:
            bitmap[da + 2476] = bitmap[da + 2477] = bitmap[da + 943];
            bitmap[da + 2473] = bitmap[da + 2474] = bitmap[da + 2475] = bitmap[da + 942];
            bitmap[da + 2471] = bitmap[da + 2472] = bitmap[da + 941];
            bitmap[da + 2468] = bitmap[da + 2469] = bitmap[da + 2470] = bitmap[da + 940];
            bitmap[da + 2465] = bitmap[da + 2466] = bitmap[da + 2467] = bitmap[da + 939];
            bitmap[da + 2463] = bitmap[da + 2464] = bitmap[da + 938];
            bitmap[da + 2460] = bitmap[da + 2461] = bitmap[da + 2462] = bitmap[da + 937];
            bitmap[da + 2457] = bitmap[da + 2458] = bitmap[da + 2459] = bitmap[da + 936];
        case 117:
            bitmap[da + 2455] = bitmap[da + 2456] = bitmap[da + 935];
            bitmap[da + 2452] = bitmap[da + 2453] = bitmap[da + 2454] = bitmap[da + 934];
            bitmap[da + 2450] = bitmap[da + 2451] = bitmap[da + 933];
            bitmap[da + 2447] = bitmap[da + 2448] = bitmap[da + 2449] = bitmap[da + 932];
            bitmap[da + 2444] = bitmap[da + 2445] = bitmap[da + 2446] = bitmap[da + 931];
            bitmap[da + 2442] = bitmap[da + 2443] = bitmap[da + 930];
            bitmap[da + 2439] = bitmap[da + 2440] = bitmap[da + 2441] = bitmap[da + 929];
            bitmap[da + 2436] = bitmap[da + 2437] = bitmap[da + 2438] = bitmap[da + 928];
        case 116:
            bitmap[da + 2434] = bitmap[da + 2435] = bitmap[da + 927];
            bitmap[da + 2431] = bitmap[da + 2432] = bitmap[da + 2433] = bitmap[da + 926];
            bitmap[da + 2429] = bitmap[da + 2430] = bitmap[da + 925];
            bitmap[da + 2426] = bitmap[da + 2427] = bitmap[da + 2428] = bitmap[da + 924];
            bitmap[da + 2423] = bitmap[da + 2424] = bitmap[da + 2425] = bitmap[da + 923];
            bitmap[da + 2421] = bitmap[da + 2422] = bitmap[da + 922];
            bitmap[da + 2418] = bitmap[da + 2419] = bitmap[da + 2420] = bitmap[da + 921];
            bitmap[da + 2415] = bitmap[da + 2416] = bitmap[da + 2417] = bitmap[da + 920];
        case 115:
            bitmap[da + 2413] = bitmap[da + 2414] = bitmap[da + 919];
            bitmap[da + 2410] = bitmap[da + 2411] = bitmap[da + 2412] = bitmap[da + 918];
            bitmap[da + 2408] = bitmap[da + 2409] = bitmap[da + 917];
            bitmap[da + 2405] = bitmap[da + 2406] = bitmap[da + 2407] = bitmap[da + 916];
            bitmap[da + 2402] = bitmap[da + 2403] = bitmap[da + 2404] = bitmap[da + 915];
            bitmap[da + 2400] = bitmap[da + 2401] = bitmap[da + 914];
            bitmap[da + 2397] = bitmap[da + 2398] = bitmap[da + 2399] = bitmap[da + 913];
            bitmap[da + 2394] = bitmap[da + 2395] = bitmap[da + 2396] = bitmap[da + 912];
        case 114:
            bitmap[da + 2392] = bitmap[da + 2393] = bitmap[da + 911];
            bitmap[da + 2389] = bitmap[da + 2390] = bitmap[da + 2391] = bitmap[da + 910];
            bitmap[da + 2387] = bitmap[da + 2388] = bitmap[da + 909];
            bitmap[da + 2384] = bitmap[da + 2385] = bitmap[da + 2386] = bitmap[da + 908];
            bitmap[da + 2381] = bitmap[da + 2382] = bitmap[da + 2383] = bitmap[da + 907];
            bitmap[da + 2379] = bitmap[da + 2380] = bitmap[da + 906];
            bitmap[da + 2376] = bitmap[da + 2377] = bitmap[da + 2378] = bitmap[da + 905];
            bitmap[da + 2373] = bitmap[da + 2374] = bitmap[da + 2375] = bitmap[da + 904];
        case 113:
            bitmap[da + 2371] = bitmap[da + 2372] = bitmap[da + 903];
            bitmap[da + 2368] = bitmap[da + 2369] = bitmap[da + 2370] = bitmap[da + 902];
            bitmap[da + 2366] = bitmap[da + 2367] = bitmap[da + 901];
            bitmap[da + 2363] = bitmap[da + 2364] = bitmap[da + 2365] = bitmap[da + 900];
            bitmap[da + 2360] = bitmap[da + 2361] = bitmap[da + 2362] = bitmap[da + 899];
            bitmap[da + 2358] = bitmap[da + 2359] = bitmap[da + 898];
            bitmap[da + 2355] = bitmap[da + 2356] = bitmap[da + 2357] = bitmap[da + 897];
            bitmap[da + 2352] = bitmap[da + 2353] = bitmap[da + 2354] = bitmap[da + 896];
        case 112:
            bitmap[da + 2350] = bitmap[da + 2351] = bitmap[da + 895];
            bitmap[da + 2347] = bitmap[da + 2348] = bitmap[da + 2349] = bitmap[da + 894];
            bitmap[da + 2345] = bitmap[da + 2346] = bitmap[da + 893];
            bitmap[da + 2342] = bitmap[da + 2343] = bitmap[da + 2344] = bitmap[da + 892];
            bitmap[da + 2339] = bitmap[da + 2340] = bitmap[da + 2341] = bitmap[da + 891];
            bitmap[da + 2337] = bitmap[da + 2338] = bitmap[da + 890];
            bitmap[da + 2334] = bitmap[da + 2335] = bitmap[da + 2336] = bitmap[da + 889];
            bitmap[da + 2331] = bitmap[da + 2332] = bitmap[da + 2333] = bitmap[da + 888];
        case 111:
            bitmap[da + 2329] = bitmap[da + 2330] = bitmap[da + 887];
            bitmap[da + 2326] = bitmap[da + 2327] = bitmap[da + 2328] = bitmap[da + 886];
            bitmap[da + 2324] = bitmap[da + 2325] = bitmap[da + 885];
            bitmap[da + 2321] = bitmap[da + 2322] = bitmap[da + 2323] = bitmap[da + 884];
            bitmap[da + 2318] = bitmap[da + 2319] = bitmap[da + 2320] = bitmap[da + 883];
            bitmap[da + 2316] = bitmap[da + 2317] = bitmap[da + 882];
            bitmap[da + 2313] = bitmap[da + 2314] = bitmap[da + 2315] = bitmap[da + 881];
            bitmap[da + 2310] = bitmap[da + 2311] = bitmap[da + 2312] = bitmap[da + 880];
        case 110:
            bitmap[da + 2308] = bitmap[da + 2309] = bitmap[da + 879];
            bitmap[da + 2305] = bitmap[da + 2306] = bitmap[da + 2307] = bitmap[da + 878];
            bitmap[da + 2303] = bitmap[da + 2304] = bitmap[da + 877];
            bitmap[da + 2300] = bitmap[da + 2301] = bitmap[da + 2302] = bitmap[da + 876];
            bitmap[da + 2297] = bitmap[da + 2298] = bitmap[da + 2299] = bitmap[da + 875];
            bitmap[da + 2295] = bitmap[da + 2296] = bitmap[da + 874];
            bitmap[da + 2292] = bitmap[da + 2293] = bitmap[da + 2294] = bitmap[da + 873];
            bitmap[da + 2289] = bitmap[da + 2290] = bitmap[da + 2291] = bitmap[da + 872];
        case 109:
            bitmap[da + 2287] = bitmap[da + 2288] = bitmap[da + 871];
            bitmap[da + 2284] = bitmap[da + 2285] = bitmap[da + 2286] = bitmap[da + 870];
            bitmap[da + 2282] = bitmap[da + 2283] = bitmap[da + 869];
            bitmap[da + 2279] = bitmap[da + 2280] = bitmap[da + 2281] = bitmap[da + 868];
            bitmap[da + 2276] = bitmap[da + 2277] = bitmap[da + 2278] = bitmap[da + 867];
            bitmap[da + 2274] = bitmap[da + 2275] = bitmap[da + 866];
            bitmap[da + 2271] = bitmap[da + 2272] = bitmap[da + 2273] = bitmap[da + 865];
            bitmap[da + 2268] = bitmap[da + 2269] = bitmap[da + 2270] = bitmap[da + 864];
        case 108:
            bitmap[da + 2266] = bitmap[da + 2267] = bitmap[da + 863];
            bitmap[da + 2263] = bitmap[da + 2264] = bitmap[da + 2265] = bitmap[da + 862];
            bitmap[da + 2261] = bitmap[da + 2262] = bitmap[da + 861];
            bitmap[da + 2258] = bitmap[da + 2259] = bitmap[da + 2260] = bitmap[da + 860];
            bitmap[da + 2255] = bitmap[da + 2256] = bitmap[da + 2257] = bitmap[da + 859];
            bitmap[da + 2253] = bitmap[da + 2254] = bitmap[da + 858];
            bitmap[da + 2250] = bitmap[da + 2251] = bitmap[da + 2252] = bitmap[da + 857];
            bitmap[da + 2247] = bitmap[da + 2248] = bitmap[da + 2249] = bitmap[da + 856];
        case 107:
            bitmap[da + 2245] = bitmap[da + 2246] = bitmap[da + 855];
            bitmap[da + 2242] = bitmap[da + 2243] = bitmap[da + 2244] = bitmap[da + 854];
            bitmap[da + 2240] = bitmap[da + 2241] = bitmap[da + 853];
            bitmap[da + 2237] = bitmap[da + 2238] = bitmap[da + 2239] = bitmap[da + 852];
            bitmap[da + 2234] = bitmap[da + 2235] = bitmap[da + 2236] = bitmap[da + 851];
            bitmap[da + 2232] = bitmap[da + 2233] = bitmap[da + 850];
            bitmap[da + 2229] = bitmap[da + 2230] = bitmap[da + 2231] = bitmap[da + 849];
            bitmap[da + 2226] = bitmap[da + 2227] = bitmap[da + 2228] = bitmap[da + 848];
        case 106:
            bitmap[da + 2224] = bitmap[da + 2225] = bitmap[da + 847];
            bitmap[da + 2221] = bitmap[da + 2222] = bitmap[da + 2223] = bitmap[da + 846];
            bitmap[da + 2219] = bitmap[da + 2220] = bitmap[da + 845];
            bitmap[da + 2216] = bitmap[da + 2217] = bitmap[da + 2218] = bitmap[da + 844];
            bitmap[da + 2213] = bitmap[da + 2214] = bitmap[da + 2215] = bitmap[da + 843];
            bitmap[da + 2211] = bitmap[da + 2212] = bitmap[da + 842];
            bitmap[da + 2208] = bitmap[da + 2209] = bitmap[da + 2210] = bitmap[da + 841];
            bitmap[da + 2205] = bitmap[da + 2206] = bitmap[da + 2207] = bitmap[da + 840];
        case 105:
            bitmap[da + 2203] = bitmap[da + 2204] = bitmap[da + 839];
            bitmap[da + 2200] = bitmap[da + 2201] = bitmap[da + 2202] = bitmap[da + 838];
            bitmap[da + 2198] = bitmap[da + 2199] = bitmap[da + 837];
            bitmap[da + 2195] = bitmap[da + 2196] = bitmap[da + 2197] = bitmap[da + 836];
            bitmap[da + 2192] = bitmap[da + 2193] = bitmap[da + 2194] = bitmap[da + 835];
            bitmap[da + 2190] = bitmap[da + 2191] = bitmap[da + 834];
            bitmap[da + 2187] = bitmap[da + 2188] = bitmap[da + 2189] = bitmap[da + 833];
            bitmap[da + 2184] = bitmap[da + 2185] = bitmap[da + 2186] = bitmap[da + 832];
        case 104:
            bitmap[da + 2182] = bitmap[da + 2183] = bitmap[da + 831];
            bitmap[da + 2179] = bitmap[da + 2180] = bitmap[da + 2181] = bitmap[da + 830];
            bitmap[da + 2177] = bitmap[da + 2178] = bitmap[da + 829];
            bitmap[da + 2174] = bitmap[da + 2175] = bitmap[da + 2176] = bitmap[da + 828];
            bitmap[da + 2171] = bitmap[da + 2172] = bitmap[da + 2173] = bitmap[da + 827];
            bitmap[da + 2169] = bitmap[da + 2170] = bitmap[da + 826];
            bitmap[da + 2166] = bitmap[da + 2167] = bitmap[da + 2168] = bitmap[da + 825];
            bitmap[da + 2163] = bitmap[da + 2164] = bitmap[da + 2165] = bitmap[da + 824];
        case 103:
            bitmap[da + 2161] = bitmap[da + 2162] = bitmap[da + 823];
            bitmap[da + 2158] = bitmap[da + 2159] = bitmap[da + 2160] = bitmap[da + 822];
            bitmap[da + 2156] = bitmap[da + 2157] = bitmap[da + 821];
            bitmap[da + 2153] = bitmap[da + 2154] = bitmap[da + 2155] = bitmap[da + 820];
            bitmap[da + 2150] = bitmap[da + 2151] = bitmap[da + 2152] = bitmap[da + 819];
            bitmap[da + 2148] = bitmap[da + 2149] = bitmap[da + 818];
            bitmap[da + 2145] = bitmap[da + 2146] = bitmap[da + 2147] = bitmap[da + 817];
            bitmap[da + 2142] = bitmap[da + 2143] = bitmap[da + 2144] = bitmap[da + 816];
        case 102:
            bitmap[da + 2140] = bitmap[da + 2141] = bitmap[da + 815];
            bitmap[da + 2137] = bitmap[da + 2138] = bitmap[da + 2139] = bitmap[da + 814];
            bitmap[da + 2135] = bitmap[da + 2136] = bitmap[da + 813];
            bitmap[da + 2132] = bitmap[da + 2133] = bitmap[da + 2134] = bitmap[da + 812];
            bitmap[da + 2129] = bitmap[da + 2130] = bitmap[da + 2131] = bitmap[da + 811];
            bitmap[da + 2127] = bitmap[da + 2128] = bitmap[da + 810];
            bitmap[da + 2124] = bitmap[da + 2125] = bitmap[da + 2126] = bitmap[da + 809];
            bitmap[da + 2121] = bitmap[da + 2122] = bitmap[da + 2123] = bitmap[da + 808];
        case 101:
            bitmap[da + 2119] = bitmap[da + 2120] = bitmap[da + 807];
            bitmap[da + 2116] = bitmap[da + 2117] = bitmap[da + 2118] = bitmap[da + 806];
            bitmap[da + 2114] = bitmap[da + 2115] = bitmap[da + 805];
            bitmap[da + 2111] = bitmap[da + 2112] = bitmap[da + 2113] = bitmap[da + 804];
            bitmap[da + 2108] = bitmap[da + 2109] = bitmap[da + 2110] = bitmap[da + 803];
            bitmap[da + 2106] = bitmap[da + 2107] = bitmap[da + 802];
            bitmap[da + 2103] = bitmap[da + 2104] = bitmap[da + 2105] = bitmap[da + 801];
            bitmap[da + 2100] = bitmap[da + 2101] = bitmap[da + 2102] = bitmap[da + 800];
        case 100:
            bitmap[da + 2098] = bitmap[da + 2099] = bitmap[da + 799];
            bitmap[da + 2095] = bitmap[da + 2096] = bitmap[da + 2097] = bitmap[da + 798];
            bitmap[da + 2093] = bitmap[da + 2094] = bitmap[da + 797];
            bitmap[da + 2090] = bitmap[da + 2091] = bitmap[da + 2092] = bitmap[da + 796];
            bitmap[da + 2087] = bitmap[da + 2088] = bitmap[da + 2089] = bitmap[da + 795];
            bitmap[da + 2085] = bitmap[da + 2086] = bitmap[da + 794];
            bitmap[da + 2082] = bitmap[da + 2083] = bitmap[da + 2084] = bitmap[da + 793];
            bitmap[da + 2079] = bitmap[da + 2080] = bitmap[da + 2081] = bitmap[da + 792];
        case 99:
            bitmap[da + 2077] = bitmap[da + 2078] = bitmap[da + 791];
            bitmap[da + 2074] = bitmap[da + 2075] = bitmap[da + 2076] = bitmap[da + 790];
            bitmap[da + 2072] = bitmap[da + 2073] = bitmap[da + 789];
            bitmap[da + 2069] = bitmap[da + 2070] = bitmap[da + 2071] = bitmap[da + 788];
            bitmap[da + 2066] = bitmap[da + 2067] = bitmap[da + 2068] = bitmap[da + 787];
            bitmap[da + 2064] = bitmap[da + 2065] = bitmap[da + 786];
            bitmap[da + 2061] = bitmap[da + 2062] = bitmap[da + 2063] = bitmap[da + 785];
            bitmap[da + 2058] = bitmap[da + 2059] = bitmap[da + 2060] = bitmap[da + 784];
        case 98:
            bitmap[da + 2056] = bitmap[da + 2057] = bitmap[da + 783];
            bitmap[da + 2053] = bitmap[da + 2054] = bitmap[da + 2055] = bitmap[da + 782];
            bitmap[da + 2051] = bitmap[da + 2052] = bitmap[da + 781];
            bitmap[da + 2048] = bitmap[da + 2049] = bitmap[da + 2050] = bitmap[da + 780];
            bitmap[da + 2045] = bitmap[da + 2046] = bitmap[da + 2047] = bitmap[da + 779];
            bitmap[da + 2043] = bitmap[da + 2044] = bitmap[da + 778];
            bitmap[da + 2040] = bitmap[da + 2041] = bitmap[da + 2042] = bitmap[da + 777];
            bitmap[da + 2037] = bitmap[da + 2038] = bitmap[da + 2039] = bitmap[da + 776];
        case 97:
            bitmap[da + 2035] = bitmap[da + 2036] = bitmap[da + 775];
            bitmap[da + 2032] = bitmap[da + 2033] = bitmap[da + 2034] = bitmap[da + 774];
            bitmap[da + 2030] = bitmap[da + 2031] = bitmap[da + 773];
            bitmap[da + 2027] = bitmap[da + 2028] = bitmap[da + 2029] = bitmap[da + 772];
            bitmap[da + 2024] = bitmap[da + 2025] = bitmap[da + 2026] = bitmap[da + 771];
            bitmap[da + 2022] = bitmap[da + 2023] = bitmap[da + 770];
            bitmap[da + 2019] = bitmap[da + 2020] = bitmap[da + 2021] = bitmap[da + 769];
            bitmap[da + 2016] = bitmap[da + 2017] = bitmap[da + 2018] = bitmap[da + 768];
        case 96:
            bitmap[da + 2014] = bitmap[da + 2015] = bitmap[da + 767];
            bitmap[da + 2011] = bitmap[da + 2012] = bitmap[da + 2013] = bitmap[da + 766];
            bitmap[da + 2009] = bitmap[da + 2010] = bitmap[da + 765];
            bitmap[da + 2006] = bitmap[da + 2007] = bitmap[da + 2008] = bitmap[da + 764];
            bitmap[da + 2003] = bitmap[da + 2004] = bitmap[da + 2005] = bitmap[da + 763];
            bitmap[da + 2001] = bitmap[da + 2002] = bitmap[da + 762];
            bitmap[da + 1998] = bitmap[da + 1999] = bitmap[da + 2000] = bitmap[da + 761];
            bitmap[da + 1995] = bitmap[da + 1996] = bitmap[da + 1997] = bitmap[da + 760];
        case 95:
            bitmap[da + 1993] = bitmap[da + 1994] = bitmap[da + 759];
            bitmap[da + 1990] = bitmap[da + 1991] = bitmap[da + 1992] = bitmap[da + 758];
            bitmap[da + 1988] = bitmap[da + 1989] = bitmap[da + 757];
            bitmap[da + 1985] = bitmap[da + 1986] = bitmap[da + 1987] = bitmap[da + 756];
            bitmap[da + 1982] = bitmap[da + 1983] = bitmap[da + 1984] = bitmap[da + 755];
            bitmap[da + 1980] = bitmap[da + 1981] = bitmap[da + 754];
            bitmap[da + 1977] = bitmap[da + 1978] = bitmap[da + 1979] = bitmap[da + 753];
            bitmap[da + 1974] = bitmap[da + 1975] = bitmap[da + 1976] = bitmap[da + 752];
        case 94:
            bitmap[da + 1972] = bitmap[da + 1973] = bitmap[da + 751];
            bitmap[da + 1969] = bitmap[da + 1970] = bitmap[da + 1971] = bitmap[da + 750];
            bitmap[da + 1967] = bitmap[da + 1968] = bitmap[da + 749];
            bitmap[da + 1964] = bitmap[da + 1965] = bitmap[da + 1966] = bitmap[da + 748];
            bitmap[da + 1961] = bitmap[da + 1962] = bitmap[da + 1963] = bitmap[da + 747];
            bitmap[da + 1959] = bitmap[da + 1960] = bitmap[da + 746];
            bitmap[da + 1956] = bitmap[da + 1957] = bitmap[da + 1958] = bitmap[da + 745];
            bitmap[da + 1953] = bitmap[da + 1954] = bitmap[da + 1955] = bitmap[da + 744];
        case 93:
            bitmap[da + 1951] = bitmap[da + 1952] = bitmap[da + 743];
            bitmap[da + 1948] = bitmap[da + 1949] = bitmap[da + 1950] = bitmap[da + 742];
            bitmap[da + 1946] = bitmap[da + 1947] = bitmap[da + 741];
            bitmap[da + 1943] = bitmap[da + 1944] = bitmap[da + 1945] = bitmap[da + 740];
            bitmap[da + 1940] = bitmap[da + 1941] = bitmap[da + 1942] = bitmap[da + 739];
            bitmap[da + 1938] = bitmap[da + 1939] = bitmap[da + 738];
            bitmap[da + 1935] = bitmap[da + 1936] = bitmap[da + 1937] = bitmap[da + 737];
            bitmap[da + 1932] = bitmap[da + 1933] = bitmap[da + 1934] = bitmap[da + 736];
        case 92:
            bitmap[da + 1930] = bitmap[da + 1931] = bitmap[da + 735];
            bitmap[da + 1927] = bitmap[da + 1928] = bitmap[da + 1929] = bitmap[da + 734];
            bitmap[da + 1925] = bitmap[da + 1926] = bitmap[da + 733];
            bitmap[da + 1922] = bitmap[da + 1923] = bitmap[da + 1924] = bitmap[da + 732];
            bitmap[da + 1919] = bitmap[da + 1920] = bitmap[da + 1921] = bitmap[da + 731];
            bitmap[da + 1917] = bitmap[da + 1918] = bitmap[da + 730];
            bitmap[da + 1914] = bitmap[da + 1915] = bitmap[da + 1916] = bitmap[da + 729];
            bitmap[da + 1911] = bitmap[da + 1912] = bitmap[da + 1913] = bitmap[da + 728];
        case 91:
            bitmap[da + 1909] = bitmap[da + 1910] = bitmap[da + 727];
            bitmap[da + 1906] = bitmap[da + 1907] = bitmap[da + 1908] = bitmap[da + 726];
            bitmap[da + 1904] = bitmap[da + 1905] = bitmap[da + 725];
            bitmap[da + 1901] = bitmap[da + 1902] = bitmap[da + 1903] = bitmap[da + 724];
            bitmap[da + 1898] = bitmap[da + 1899] = bitmap[da + 1900] = bitmap[da + 723];
            bitmap[da + 1896] = bitmap[da + 1897] = bitmap[da + 722];
            bitmap[da + 1893] = bitmap[da + 1894] = bitmap[da + 1895] = bitmap[da + 721];
            bitmap[da + 1890] = bitmap[da + 1891] = bitmap[da + 1892] = bitmap[da + 720];
        case 90:
            bitmap[da + 1888] = bitmap[da + 1889] = bitmap[da + 719];
            bitmap[da + 1885] = bitmap[da + 1886] = bitmap[da + 1887] = bitmap[da + 718];
            bitmap[da + 1883] = bitmap[da + 1884] = bitmap[da + 717];
            bitmap[da + 1880] = bitmap[da + 1881] = bitmap[da + 1882] = bitmap[da + 716];
            bitmap[da + 1877] = bitmap[da + 1878] = bitmap[da + 1879] = bitmap[da + 715];
            bitmap[da + 1875] = bitmap[da + 1876] = bitmap[da + 714];
            bitmap[da + 1872] = bitmap[da + 1873] = bitmap[da + 1874] = bitmap[da + 713];
            bitmap[da + 1869] = bitmap[da + 1870] = bitmap[da + 1871] = bitmap[da + 712];
        case 89:
            bitmap[da + 1867] = bitmap[da + 1868] = bitmap[da + 711];
            bitmap[da + 1864] = bitmap[da + 1865] = bitmap[da + 1866] = bitmap[da + 710];
            bitmap[da + 1862] = bitmap[da + 1863] = bitmap[da + 709];
            bitmap[da + 1859] = bitmap[da + 1860] = bitmap[da + 1861] = bitmap[da + 708];
            bitmap[da + 1856] = bitmap[da + 1857] = bitmap[da + 1858] = bitmap[da + 707];
            bitmap[da + 1854] = bitmap[da + 1855] = bitmap[da + 706];
            bitmap[da + 1851] = bitmap[da + 1852] = bitmap[da + 1853] = bitmap[da + 705];
            bitmap[da + 1848] = bitmap[da + 1849] = bitmap[da + 1850] = bitmap[da + 704];
        case 88:
            bitmap[da + 1846] = bitmap[da + 1847] = bitmap[da + 703];
            bitmap[da + 1843] = bitmap[da + 1844] = bitmap[da + 1845] = bitmap[da + 702];
            bitmap[da + 1841] = bitmap[da + 1842] = bitmap[da + 701];
            bitmap[da + 1838] = bitmap[da + 1839] = bitmap[da + 1840] = bitmap[da + 700];
            bitmap[da + 1835] = bitmap[da + 1836] = bitmap[da + 1837] = bitmap[da + 699];
            bitmap[da + 1833] = bitmap[da + 1834] = bitmap[da + 698];
            bitmap[da + 1830] = bitmap[da + 1831] = bitmap[da + 1832] = bitmap[da + 697];
            bitmap[da + 1827] = bitmap[da + 1828] = bitmap[da + 1829] = bitmap[da + 696];
        case 87:
            bitmap[da + 1825] = bitmap[da + 1826] = bitmap[da + 695];
            bitmap[da + 1822] = bitmap[da + 1823] = bitmap[da + 1824] = bitmap[da + 694];
            bitmap[da + 1820] = bitmap[da + 1821] = bitmap[da + 693];
            bitmap[da + 1817] = bitmap[da + 1818] = bitmap[da + 1819] = bitmap[da + 692];
            bitmap[da + 1814] = bitmap[da + 1815] = bitmap[da + 1816] = bitmap[da + 691];
            bitmap[da + 1812] = bitmap[da + 1813] = bitmap[da + 690];
            bitmap[da + 1809] = bitmap[da + 1810] = bitmap[da + 1811] = bitmap[da + 689];
            bitmap[da + 1806] = bitmap[da + 1807] = bitmap[da + 1808] = bitmap[da + 688];
        case 86:
            bitmap[da + 1804] = bitmap[da + 1805] = bitmap[da + 687];
            bitmap[da + 1801] = bitmap[da + 1802] = bitmap[da + 1803] = bitmap[da + 686];
            bitmap[da + 1799] = bitmap[da + 1800] = bitmap[da + 685];
            bitmap[da + 1796] = bitmap[da + 1797] = bitmap[da + 1798] = bitmap[da + 684];
            bitmap[da + 1793] = bitmap[da + 1794] = bitmap[da + 1795] = bitmap[da + 683];
            bitmap[da + 1791] = bitmap[da + 1792] = bitmap[da + 682];
            bitmap[da + 1788] = bitmap[da + 1789] = bitmap[da + 1790] = bitmap[da + 681];
            bitmap[da + 1785] = bitmap[da + 1786] = bitmap[da + 1787] = bitmap[da + 680];
        case 85:
            bitmap[da + 1783] = bitmap[da + 1784] = bitmap[da + 679];
            bitmap[da + 1780] = bitmap[da + 1781] = bitmap[da + 1782] = bitmap[da + 678];
            bitmap[da + 1778] = bitmap[da + 1779] = bitmap[da + 677];
            bitmap[da + 1775] = bitmap[da + 1776] = bitmap[da + 1777] = bitmap[da + 676];
            bitmap[da + 1772] = bitmap[da + 1773] = bitmap[da + 1774] = bitmap[da + 675];
            bitmap[da + 1770] = bitmap[da + 1771] = bitmap[da + 674];
            bitmap[da + 1767] = bitmap[da + 1768] = bitmap[da + 1769] = bitmap[da + 673];
            bitmap[da + 1764] = bitmap[da + 1765] = bitmap[da + 1766] = bitmap[da + 672];
        case 84:
            bitmap[da + 1762] = bitmap[da + 1763] = bitmap[da + 671];
            bitmap[da + 1759] = bitmap[da + 1760] = bitmap[da + 1761] = bitmap[da + 670];
            bitmap[da + 1757] = bitmap[da + 1758] = bitmap[da + 669];
            bitmap[da + 1754] = bitmap[da + 1755] = bitmap[da + 1756] = bitmap[da + 668];
            bitmap[da + 1751] = bitmap[da + 1752] = bitmap[da + 1753] = bitmap[da + 667];
            bitmap[da + 1749] = bitmap[da + 1750] = bitmap[da + 666];
            bitmap[da + 1746] = bitmap[da + 1747] = bitmap[da + 1748] = bitmap[da + 665];
            bitmap[da + 1743] = bitmap[da + 1744] = bitmap[da + 1745] = bitmap[da + 664];
        case 83:
            bitmap[da + 1741] = bitmap[da + 1742] = bitmap[da + 663];
            bitmap[da + 1738] = bitmap[da + 1739] = bitmap[da + 1740] = bitmap[da + 662];
            bitmap[da + 1736] = bitmap[da + 1737] = bitmap[da + 661];
            bitmap[da + 1733] = bitmap[da + 1734] = bitmap[da + 1735] = bitmap[da + 660];
            bitmap[da + 1730] = bitmap[da + 1731] = bitmap[da + 1732] = bitmap[da + 659];
            bitmap[da + 1728] = bitmap[da + 1729] = bitmap[da + 658];
            bitmap[da + 1725] = bitmap[da + 1726] = bitmap[da + 1727] = bitmap[da + 657];
            bitmap[da + 1722] = bitmap[da + 1723] = bitmap[da + 1724] = bitmap[da + 656];
        case 82:
            bitmap[da + 1720] = bitmap[da + 1721] = bitmap[da + 655];
            bitmap[da + 1717] = bitmap[da + 1718] = bitmap[da + 1719] = bitmap[da + 654];
            bitmap[da + 1715] = bitmap[da + 1716] = bitmap[da + 653];
            bitmap[da + 1712] = bitmap[da + 1713] = bitmap[da + 1714] = bitmap[da + 652];
            bitmap[da + 1709] = bitmap[da + 1710] = bitmap[da + 1711] = bitmap[da + 651];
            bitmap[da + 1707] = bitmap[da + 1708] = bitmap[da + 650];
            bitmap[da + 1704] = bitmap[da + 1705] = bitmap[da + 1706] = bitmap[da + 649];
            bitmap[da + 1701] = bitmap[da + 1702] = bitmap[da + 1703] = bitmap[da + 648];
        case 81:
            bitmap[da + 1699] = bitmap[da + 1700] = bitmap[da + 647];
            bitmap[da + 1696] = bitmap[da + 1697] = bitmap[da + 1698] = bitmap[da + 646];
            bitmap[da + 1694] = bitmap[da + 1695] = bitmap[da + 645];
            bitmap[da + 1691] = bitmap[da + 1692] = bitmap[da + 1693] = bitmap[da + 644];
            bitmap[da + 1688] = bitmap[da + 1689] = bitmap[da + 1690] = bitmap[da + 643];
            bitmap[da + 1686] = bitmap[da + 1687] = bitmap[da + 642];
            bitmap[da + 1683] = bitmap[da + 1684] = bitmap[da + 1685] = bitmap[da + 641];
            bitmap[da + 1680] = bitmap[da + 1681] = bitmap[da + 1682] = bitmap[da + 640];
        case 80:
            bitmap[da + 1678] = bitmap[da + 1679] = bitmap[da + 639];
            bitmap[da + 1675] = bitmap[da + 1676] = bitmap[da + 1677] = bitmap[da + 638];
            bitmap[da + 1673] = bitmap[da + 1674] = bitmap[da + 637];
            bitmap[da + 1670] = bitmap[da + 1671] = bitmap[da + 1672] = bitmap[da + 636];
            bitmap[da + 1667] = bitmap[da + 1668] = bitmap[da + 1669] = bitmap[da + 635];
            bitmap[da + 1665] = bitmap[da + 1666] = bitmap[da + 634];
            bitmap[da + 1662] = bitmap[da + 1663] = bitmap[da + 1664] = bitmap[da + 633];
            bitmap[da + 1659] = bitmap[da + 1660] = bitmap[da + 1661] = bitmap[da + 632];
        case 79:
            bitmap[da + 1657] = bitmap[da + 1658] = bitmap[da + 631];
            bitmap[da + 1654] = bitmap[da + 1655] = bitmap[da + 1656] = bitmap[da + 630];
            bitmap[da + 1652] = bitmap[da + 1653] = bitmap[da + 629];
            bitmap[da + 1649] = bitmap[da + 1650] = bitmap[da + 1651] = bitmap[da + 628];
            bitmap[da + 1646] = bitmap[da + 1647] = bitmap[da + 1648] = bitmap[da + 627];
            bitmap[da + 1644] = bitmap[da + 1645] = bitmap[da + 626];
            bitmap[da + 1641] = bitmap[da + 1642] = bitmap[da + 1643] = bitmap[da + 625];
            bitmap[da + 1638] = bitmap[da + 1639] = bitmap[da + 1640] = bitmap[da + 624];
        case 78:
            bitmap[da + 1636] = bitmap[da + 1637] = bitmap[da + 623];
            bitmap[da + 1633] = bitmap[da + 1634] = bitmap[da + 1635] = bitmap[da + 622];
            bitmap[da + 1631] = bitmap[da + 1632] = bitmap[da + 621];
            bitmap[da + 1628] = bitmap[da + 1629] = bitmap[da + 1630] = bitmap[da + 620];
            bitmap[da + 1625] = bitmap[da + 1626] = bitmap[da + 1627] = bitmap[da + 619];
            bitmap[da + 1623] = bitmap[da + 1624] = bitmap[da + 618];
            bitmap[da + 1620] = bitmap[da + 1621] = bitmap[da + 1622] = bitmap[da + 617];
            bitmap[da + 1617] = bitmap[da + 1618] = bitmap[da + 1619] = bitmap[da + 616];
        case 77:
            bitmap[da + 1615] = bitmap[da + 1616] = bitmap[da + 615];
            bitmap[da + 1612] = bitmap[da + 1613] = bitmap[da + 1614] = bitmap[da + 614];
            bitmap[da + 1610] = bitmap[da + 1611] = bitmap[da + 613];
            bitmap[da + 1607] = bitmap[da + 1608] = bitmap[da + 1609] = bitmap[da + 612];
            bitmap[da + 1604] = bitmap[da + 1605] = bitmap[da + 1606] = bitmap[da + 611];
            bitmap[da + 1602] = bitmap[da + 1603] = bitmap[da + 610];
            bitmap[da + 1599] = bitmap[da + 1600] = bitmap[da + 1601] = bitmap[da + 609];
            bitmap[da + 1596] = bitmap[da + 1597] = bitmap[da + 1598] = bitmap[da + 608];
        case 76:
            bitmap[da + 1594] = bitmap[da + 1595] = bitmap[da + 607];
            bitmap[da + 1591] = bitmap[da + 1592] = bitmap[da + 1593] = bitmap[da + 606];
            bitmap[da + 1589] = bitmap[da + 1590] = bitmap[da + 605];
            bitmap[da + 1586] = bitmap[da + 1587] = bitmap[da + 1588] = bitmap[da + 604];
            bitmap[da + 1583] = bitmap[da + 1584] = bitmap[da + 1585] = bitmap[da + 603];
            bitmap[da + 1581] = bitmap[da + 1582] = bitmap[da + 602];
            bitmap[da + 1578] = bitmap[da + 1579] = bitmap[da + 1580] = bitmap[da + 601];
            bitmap[da + 1575] = bitmap[da + 1576] = bitmap[da + 1577] = bitmap[da + 600];
        case 75:
            bitmap[da + 1573] = bitmap[da + 1574] = bitmap[da + 599];
            bitmap[da + 1570] = bitmap[da + 1571] = bitmap[da + 1572] = bitmap[da + 598];
            bitmap[da + 1568] = bitmap[da + 1569] = bitmap[da + 597];
            bitmap[da + 1565] = bitmap[da + 1566] = bitmap[da + 1567] = bitmap[da + 596];
            bitmap[da + 1562] = bitmap[da + 1563] = bitmap[da + 1564] = bitmap[da + 595];
            bitmap[da + 1560] = bitmap[da + 1561] = bitmap[da + 594];
            bitmap[da + 1557] = bitmap[da + 1558] = bitmap[da + 1559] = bitmap[da + 593];
            bitmap[da + 1554] = bitmap[da + 1555] = bitmap[da + 1556] = bitmap[da + 592];
        case 74:
            bitmap[da + 1552] = bitmap[da + 1553] = bitmap[da + 591];
            bitmap[da + 1549] = bitmap[da + 1550] = bitmap[da + 1551] = bitmap[da + 590];
            bitmap[da + 1547] = bitmap[da + 1548] = bitmap[da + 589];
            bitmap[da + 1544] = bitmap[da + 1545] = bitmap[da + 1546] = bitmap[da + 588];
            bitmap[da + 1541] = bitmap[da + 1542] = bitmap[da + 1543] = bitmap[da + 587];
            bitmap[da + 1539] = bitmap[da + 1540] = bitmap[da + 586];
            bitmap[da + 1536] = bitmap[da + 1537] = bitmap[da + 1538] = bitmap[da + 585];
            bitmap[da + 1533] = bitmap[da + 1534] = bitmap[da + 1535] = bitmap[da + 584];
        case 73:
            bitmap[da + 1531] = bitmap[da + 1532] = bitmap[da + 583];
            bitmap[da + 1528] = bitmap[da + 1529] = bitmap[da + 1530] = bitmap[da + 582];
            bitmap[da + 1526] = bitmap[da + 1527] = bitmap[da + 581];
            bitmap[da + 1523] = bitmap[da + 1524] = bitmap[da + 1525] = bitmap[da + 580];
            bitmap[da + 1520] = bitmap[da + 1521] = bitmap[da + 1522] = bitmap[da + 579];
            bitmap[da + 1518] = bitmap[da + 1519] = bitmap[da + 578];
            bitmap[da + 1515] = bitmap[da + 1516] = bitmap[da + 1517] = bitmap[da + 577];
            bitmap[da + 1512] = bitmap[da + 1513] = bitmap[da + 1514] = bitmap[da + 576];
        case 72:
            bitmap[da + 1510] = bitmap[da + 1511] = bitmap[da + 575];
            bitmap[da + 1507] = bitmap[da + 1508] = bitmap[da + 1509] = bitmap[da + 574];
            bitmap[da + 1505] = bitmap[da + 1506] = bitmap[da + 573];
            bitmap[da + 1502] = bitmap[da + 1503] = bitmap[da + 1504] = bitmap[da + 572];
            bitmap[da + 1499] = bitmap[da + 1500] = bitmap[da + 1501] = bitmap[da + 571];
            bitmap[da + 1497] = bitmap[da + 1498] = bitmap[da + 570];
            bitmap[da + 1494] = bitmap[da + 1495] = bitmap[da + 1496] = bitmap[da + 569];
            bitmap[da + 1491] = bitmap[da + 1492] = bitmap[da + 1493] = bitmap[da + 568];
        case 71:
            bitmap[da + 1489] = bitmap[da + 1490] = bitmap[da + 567];
            bitmap[da + 1486] = bitmap[da + 1487] = bitmap[da + 1488] = bitmap[da + 566];
            bitmap[da + 1484] = bitmap[da + 1485] = bitmap[da + 565];
            bitmap[da + 1481] = bitmap[da + 1482] = bitmap[da + 1483] = bitmap[da + 564];
            bitmap[da + 1478] = bitmap[da + 1479] = bitmap[da + 1480] = bitmap[da + 563];
            bitmap[da + 1476] = bitmap[da + 1477] = bitmap[da + 562];
            bitmap[da + 1473] = bitmap[da + 1474] = bitmap[da + 1475] = bitmap[da + 561];
            bitmap[da + 1470] = bitmap[da + 1471] = bitmap[da + 1472] = bitmap[da + 560];
        case 70:
            bitmap[da + 1468] = bitmap[da + 1469] = bitmap[da + 559];
            bitmap[da + 1465] = bitmap[da + 1466] = bitmap[da + 1467] = bitmap[da + 558];
            bitmap[da + 1463] = bitmap[da + 1464] = bitmap[da + 557];
            bitmap[da + 1460] = bitmap[da + 1461] = bitmap[da + 1462] = bitmap[da + 556];
            bitmap[da + 1457] = bitmap[da + 1458] = bitmap[da + 1459] = bitmap[da + 555];
            bitmap[da + 1455] = bitmap[da + 1456] = bitmap[da + 554];
            bitmap[da + 1452] = bitmap[da + 1453] = bitmap[da + 1454] = bitmap[da + 553];
            bitmap[da + 1449] = bitmap[da + 1450] = bitmap[da + 1451] = bitmap[da + 552];
        case 69:
            bitmap[da + 1447] = bitmap[da + 1448] = bitmap[da + 551];
            bitmap[da + 1444] = bitmap[da + 1445] = bitmap[da + 1446] = bitmap[da + 550];
            bitmap[da + 1442] = bitmap[da + 1443] = bitmap[da + 549];
            bitmap[da + 1439] = bitmap[da + 1440] = bitmap[da + 1441] = bitmap[da + 548];
            bitmap[da + 1436] = bitmap[da + 1437] = bitmap[da + 1438] = bitmap[da + 547];
            bitmap[da + 1434] = bitmap[da + 1435] = bitmap[da + 546];
            bitmap[da + 1431] = bitmap[da + 1432] = bitmap[da + 1433] = bitmap[da + 545];
            bitmap[da + 1428] = bitmap[da + 1429] = bitmap[da + 1430] = bitmap[da + 544];
        case 68:
            bitmap[da + 1426] = bitmap[da + 1427] = bitmap[da + 543];
            bitmap[da + 1423] = bitmap[da + 1424] = bitmap[da + 1425] = bitmap[da + 542];
            bitmap[da + 1421] = bitmap[da + 1422] = bitmap[da + 541];
            bitmap[da + 1418] = bitmap[da + 1419] = bitmap[da + 1420] = bitmap[da + 540];
            bitmap[da + 1415] = bitmap[da + 1416] = bitmap[da + 1417] = bitmap[da + 539];
            bitmap[da + 1413] = bitmap[da + 1414] = bitmap[da + 538];
            bitmap[da + 1410] = bitmap[da + 1411] = bitmap[da + 1412] = bitmap[da + 537];
            bitmap[da + 1407] = bitmap[da + 1408] = bitmap[da + 1409] = bitmap[da + 536];
        case 67:
            bitmap[da + 1405] = bitmap[da + 1406] = bitmap[da + 535];
            bitmap[da + 1402] = bitmap[da + 1403] = bitmap[da + 1404] = bitmap[da + 534];
            bitmap[da + 1400] = bitmap[da + 1401] = bitmap[da + 533];
            bitmap[da + 1397] = bitmap[da + 1398] = bitmap[da + 1399] = bitmap[da + 532];
            bitmap[da + 1394] = bitmap[da + 1395] = bitmap[da + 1396] = bitmap[da + 531];
            bitmap[da + 1392] = bitmap[da + 1393] = bitmap[da + 530];
            bitmap[da + 1389] = bitmap[da + 1390] = bitmap[da + 1391] = bitmap[da + 529];
            bitmap[da + 1386] = bitmap[da + 1387] = bitmap[da + 1388] = bitmap[da + 528];
        case 66:
            bitmap[da + 1384] = bitmap[da + 1385] = bitmap[da + 527];
            bitmap[da + 1381] = bitmap[da + 1382] = bitmap[da + 1383] = bitmap[da + 526];
            bitmap[da + 1379] = bitmap[da + 1380] = bitmap[da + 525];
            bitmap[da + 1376] = bitmap[da + 1377] = bitmap[da + 1378] = bitmap[da + 524];
            bitmap[da + 1373] = bitmap[da + 1374] = bitmap[da + 1375] = bitmap[da + 523];
            bitmap[da + 1371] = bitmap[da + 1372] = bitmap[da + 522];
            bitmap[da + 1368] = bitmap[da + 1369] = bitmap[da + 1370] = bitmap[da + 521];
            bitmap[da + 1365] = bitmap[da + 1366] = bitmap[da + 1367] = bitmap[da + 520];
        case 65:
            bitmap[da + 1363] = bitmap[da + 1364] = bitmap[da + 519];
            bitmap[da + 1360] = bitmap[da + 1361] = bitmap[da + 1362] = bitmap[da + 518];
            bitmap[da + 1358] = bitmap[da + 1359] = bitmap[da + 517];
            bitmap[da + 1355] = bitmap[da + 1356] = bitmap[da + 1357] = bitmap[da + 516];
            bitmap[da + 1352] = bitmap[da + 1353] = bitmap[da + 1354] = bitmap[da + 515];
            bitmap[da + 1350] = bitmap[da + 1351] = bitmap[da + 514];
            bitmap[da + 1347] = bitmap[da + 1348] = bitmap[da + 1349] = bitmap[da + 513];
            bitmap[da + 1344] = bitmap[da + 1345] = bitmap[da + 1346] = bitmap[da + 512];
        case 64:
            bitmap[da + 1342] = bitmap[da + 1343] = bitmap[da + 511];
            bitmap[da + 1339] = bitmap[da + 1340] = bitmap[da + 1341] = bitmap[da + 510];
            bitmap[da + 1337] = bitmap[da + 1338] = bitmap[da + 509];
            bitmap[da + 1334] = bitmap[da + 1335] = bitmap[da + 1336] = bitmap[da + 508];
            bitmap[da + 1331] = bitmap[da + 1332] = bitmap[da + 1333] = bitmap[da + 507];
            bitmap[da + 1329] = bitmap[da + 1330] = bitmap[da + 506];
            bitmap[da + 1326] = bitmap[da + 1327] = bitmap[da + 1328] = bitmap[da + 505];
            bitmap[da + 1323] = bitmap[da + 1324] = bitmap[da + 1325] = bitmap[da + 504];
        case 63:
            bitmap[da + 1321] = bitmap[da + 1322] = bitmap[da + 503];
            bitmap[da + 1318] = bitmap[da + 1319] = bitmap[da + 1320] = bitmap[da + 502];
            bitmap[da + 1316] = bitmap[da + 1317] = bitmap[da + 501];
            bitmap[da + 1313] = bitmap[da + 1314] = bitmap[da + 1315] = bitmap[da + 500];
            bitmap[da + 1310] = bitmap[da + 1311] = bitmap[da + 1312] = bitmap[da + 499];
            bitmap[da + 1308] = bitmap[da + 1309] = bitmap[da + 498];
            bitmap[da + 1305] = bitmap[da + 1306] = bitmap[da + 1307] = bitmap[da + 497];
            bitmap[da + 1302] = bitmap[da + 1303] = bitmap[da + 1304] = bitmap[da + 496];
        case 62:
            bitmap[da + 1300] = bitmap[da + 1301] = bitmap[da + 495];
            bitmap[da + 1297] = bitmap[da + 1298] = bitmap[da + 1299] = bitmap[da + 494];
            bitmap[da + 1295] = bitmap[da + 1296] = bitmap[da + 493];
            bitmap[da + 1292] = bitmap[da + 1293] = bitmap[da + 1294] = bitmap[da + 492];
            bitmap[da + 1289] = bitmap[da + 1290] = bitmap[da + 1291] = bitmap[da + 491];
            bitmap[da + 1287] = bitmap[da + 1288] = bitmap[da + 490];
            bitmap[da + 1284] = bitmap[da + 1285] = bitmap[da + 1286] = bitmap[da + 489];
            bitmap[da + 1281] = bitmap[da + 1282] = bitmap[da + 1283] = bitmap[da + 488];
        case 61:
            bitmap[da + 1279] = bitmap[da + 1280] = bitmap[da + 487];
            bitmap[da + 1276] = bitmap[da + 1277] = bitmap[da + 1278] = bitmap[da + 486];
            bitmap[da + 1274] = bitmap[da + 1275] = bitmap[da + 485];
            bitmap[da + 1271] = bitmap[da + 1272] = bitmap[da + 1273] = bitmap[da + 484];
            bitmap[da + 1268] = bitmap[da + 1269] = bitmap[da + 1270] = bitmap[da + 483];
            bitmap[da + 1266] = bitmap[da + 1267] = bitmap[da + 482];
            bitmap[da + 1263] = bitmap[da + 1264] = bitmap[da + 1265] = bitmap[da + 481];
            bitmap[da + 1260] = bitmap[da + 1261] = bitmap[da + 1262] = bitmap[da + 480];
        case 60:
            bitmap[da + 1258] = bitmap[da + 1259] = bitmap[da + 479];
            bitmap[da + 1255] = bitmap[da + 1256] = bitmap[da + 1257] = bitmap[da + 478];
            bitmap[da + 1253] = bitmap[da + 1254] = bitmap[da + 477];
            bitmap[da + 1250] = bitmap[da + 1251] = bitmap[da + 1252] = bitmap[da + 476];
            bitmap[da + 1247] = bitmap[da + 1248] = bitmap[da + 1249] = bitmap[da + 475];
            bitmap[da + 1245] = bitmap[da + 1246] = bitmap[da + 474];
            bitmap[da + 1242] = bitmap[da + 1243] = bitmap[da + 1244] = bitmap[da + 473];
            bitmap[da + 1239] = bitmap[da + 1240] = bitmap[da + 1241] = bitmap[da + 472];
        case 59:
            bitmap[da + 1237] = bitmap[da + 1238] = bitmap[da + 471];
            bitmap[da + 1234] = bitmap[da + 1235] = bitmap[da + 1236] = bitmap[da + 470];
            bitmap[da + 1232] = bitmap[da + 1233] = bitmap[da + 469];
            bitmap[da + 1229] = bitmap[da + 1230] = bitmap[da + 1231] = bitmap[da + 468];
            bitmap[da + 1226] = bitmap[da + 1227] = bitmap[da + 1228] = bitmap[da + 467];
            bitmap[da + 1224] = bitmap[da + 1225] = bitmap[da + 466];
            bitmap[da + 1221] = bitmap[da + 1222] = bitmap[da + 1223] = bitmap[da + 465];
            bitmap[da + 1218] = bitmap[da + 1219] = bitmap[da + 1220] = bitmap[da + 464];
        case 58:
            bitmap[da + 1216] = bitmap[da + 1217] = bitmap[da + 463];
            bitmap[da + 1213] = bitmap[da + 1214] = bitmap[da + 1215] = bitmap[da + 462];
            bitmap[da + 1211] = bitmap[da + 1212] = bitmap[da + 461];
            bitmap[da + 1208] = bitmap[da + 1209] = bitmap[da + 1210] = bitmap[da + 460];
            bitmap[da + 1205] = bitmap[da + 1206] = bitmap[da + 1207] = bitmap[da + 459];
            bitmap[da + 1203] = bitmap[da + 1204] = bitmap[da + 458];
            bitmap[da + 1200] = bitmap[da + 1201] = bitmap[da + 1202] = bitmap[da + 457];
            bitmap[da + 1197] = bitmap[da + 1198] = bitmap[da + 1199] = bitmap[da + 456];
        case 57:
            bitmap[da + 1195] = bitmap[da + 1196] = bitmap[da + 455];
            bitmap[da + 1192] = bitmap[da + 1193] = bitmap[da + 1194] = bitmap[da + 454];
            bitmap[da + 1190] = bitmap[da + 1191] = bitmap[da + 453];
            bitmap[da + 1187] = bitmap[da + 1188] = bitmap[da + 1189] = bitmap[da + 452];
            bitmap[da + 1184] = bitmap[da + 1185] = bitmap[da + 1186] = bitmap[da + 451];
            bitmap[da + 1182] = bitmap[da + 1183] = bitmap[da + 450];
            bitmap[da + 1179] = bitmap[da + 1180] = bitmap[da + 1181] = bitmap[da + 449];
            bitmap[da + 1176] = bitmap[da + 1177] = bitmap[da + 1178] = bitmap[da + 448];
        case 56:
            bitmap[da + 1174] = bitmap[da + 1175] = bitmap[da + 447];
            bitmap[da + 1171] = bitmap[da + 1172] = bitmap[da + 1173] = bitmap[da + 446];
            bitmap[da + 1169] = bitmap[da + 1170] = bitmap[da + 445];
            bitmap[da + 1166] = bitmap[da + 1167] = bitmap[da + 1168] = bitmap[da + 444];
            bitmap[da + 1163] = bitmap[da + 1164] = bitmap[da + 1165] = bitmap[da + 443];
            bitmap[da + 1161] = bitmap[da + 1162] = bitmap[da + 442];
            bitmap[da + 1158] = bitmap[da + 1159] = bitmap[da + 1160] = bitmap[da + 441];
            bitmap[da + 1155] = bitmap[da + 1156] = bitmap[da + 1157] = bitmap[da + 440];
        case 55:
            bitmap[da + 1153] = bitmap[da + 1154] = bitmap[da + 439];
            bitmap[da + 1150] = bitmap[da + 1151] = bitmap[da + 1152] = bitmap[da + 438];
            bitmap[da + 1148] = bitmap[da + 1149] = bitmap[da + 437];
            bitmap[da + 1145] = bitmap[da + 1146] = bitmap[da + 1147] = bitmap[da + 436];
            bitmap[da + 1142] = bitmap[da + 1143] = bitmap[da + 1144] = bitmap[da + 435];
            bitmap[da + 1140] = bitmap[da + 1141] = bitmap[da + 434];
            bitmap[da + 1137] = bitmap[da + 1138] = bitmap[da + 1139] = bitmap[da + 433];
            bitmap[da + 1134] = bitmap[da + 1135] = bitmap[da + 1136] = bitmap[da + 432];
        case 54:
            bitmap[da + 1132] = bitmap[da + 1133] = bitmap[da + 431];
            bitmap[da + 1129] = bitmap[da + 1130] = bitmap[da + 1131] = bitmap[da + 430];
            bitmap[da + 1127] = bitmap[da + 1128] = bitmap[da + 429];
            bitmap[da + 1124] = bitmap[da + 1125] = bitmap[da + 1126] = bitmap[da + 428];
            bitmap[da + 1121] = bitmap[da + 1122] = bitmap[da + 1123] = bitmap[da + 427];
            bitmap[da + 1119] = bitmap[da + 1120] = bitmap[da + 426];
            bitmap[da + 1116] = bitmap[da + 1117] = bitmap[da + 1118] = bitmap[da + 425];
            bitmap[da + 1113] = bitmap[da + 1114] = bitmap[da + 1115] = bitmap[da + 424];
        case 53:
            bitmap[da + 1111] = bitmap[da + 1112] = bitmap[da + 423];
            bitmap[da + 1108] = bitmap[da + 1109] = bitmap[da + 1110] = bitmap[da + 422];
            bitmap[da + 1106] = bitmap[da + 1107] = bitmap[da + 421];
            bitmap[da + 1103] = bitmap[da + 1104] = bitmap[da + 1105] = bitmap[da + 420];
            bitmap[da + 1100] = bitmap[da + 1101] = bitmap[da + 1102] = bitmap[da + 419];
            bitmap[da + 1098] = bitmap[da + 1099] = bitmap[da + 418];
            bitmap[da + 1095] = bitmap[da + 1096] = bitmap[da + 1097] = bitmap[da + 417];
            bitmap[da + 1092] = bitmap[da + 1093] = bitmap[da + 1094] = bitmap[da + 416];
        case 52:
            bitmap[da + 1090] = bitmap[da + 1091] = bitmap[da + 415];
            bitmap[da + 1087] = bitmap[da + 1088] = bitmap[da + 1089] = bitmap[da + 414];
            bitmap[da + 1085] = bitmap[da + 1086] = bitmap[da + 413];
            bitmap[da + 1082] = bitmap[da + 1083] = bitmap[da + 1084] = bitmap[da + 412];
            bitmap[da + 1079] = bitmap[da + 1080] = bitmap[da + 1081] = bitmap[da + 411];
            bitmap[da + 1077] = bitmap[da + 1078] = bitmap[da + 410];
            bitmap[da + 1074] = bitmap[da + 1075] = bitmap[da + 1076] = bitmap[da + 409];
            bitmap[da + 1071] = bitmap[da + 1072] = bitmap[da + 1073] = bitmap[da + 408];
        case 51:
            bitmap[da + 1069] = bitmap[da + 1070] = bitmap[da + 407];
            bitmap[da + 1066] = bitmap[da + 1067] = bitmap[da + 1068] = bitmap[da + 406];
            bitmap[da + 1064] = bitmap[da + 1065] = bitmap[da + 405];
            bitmap[da + 1061] = bitmap[da + 1062] = bitmap[da + 1063] = bitmap[da + 404];
            bitmap[da + 1058] = bitmap[da + 1059] = bitmap[da + 1060] = bitmap[da + 403];
            bitmap[da + 1056] = bitmap[da + 1057] = bitmap[da + 402];
            bitmap[da + 1053] = bitmap[da + 1054] = bitmap[da + 1055] = bitmap[da + 401];
            bitmap[da + 1050] = bitmap[da + 1051] = bitmap[da + 1052] = bitmap[da + 400];
        case 50:
            bitmap[da + 1048] = bitmap[da + 1049] = bitmap[da + 399];
            bitmap[da + 1045] = bitmap[da + 1046] = bitmap[da + 1047] = bitmap[da + 398];
            bitmap[da + 1043] = bitmap[da + 1044] = bitmap[da + 397];
            bitmap[da + 1040] = bitmap[da + 1041] = bitmap[da + 1042] = bitmap[da + 396];
            bitmap[da + 1037] = bitmap[da + 1038] = bitmap[da + 1039] = bitmap[da + 395];
            bitmap[da + 1035] = bitmap[da + 1036] = bitmap[da + 394];
            bitmap[da + 1032] = bitmap[da + 1033] = bitmap[da + 1034] = bitmap[da + 393];
            bitmap[da + 1029] = bitmap[da + 1030] = bitmap[da + 1031] = bitmap[da + 392];
        case 49:
            bitmap[da + 1027] = bitmap[da + 1028] = bitmap[da + 391];
            bitmap[da + 1024] = bitmap[da + 1025] = bitmap[da + 1026] = bitmap[da + 390];
            bitmap[da + 1022] = bitmap[da + 1023] = bitmap[da + 389];
            bitmap[da + 1019] = bitmap[da + 1020] = bitmap[da + 1021] = bitmap[da + 388];
            bitmap[da + 1016] = bitmap[da + 1017] = bitmap[da + 1018] = bitmap[da + 387];
            bitmap[da + 1014] = bitmap[da + 1015] = bitmap[da + 386];
            bitmap[da + 1011] = bitmap[da + 1012] = bitmap[da + 1013] = bitmap[da + 385];
            bitmap[da + 1008] = bitmap[da + 1009] = bitmap[da + 1010] = bitmap[da + 384];
        case 48:
            bitmap[da + 1006] = bitmap[da + 1007] = bitmap[da + 383];
            bitmap[da + 1003] = bitmap[da + 1004] = bitmap[da + 1005] = bitmap[da + 382];
            bitmap[da + 1001] = bitmap[da + 1002] = bitmap[da + 381];
            bitmap[da + 998] = bitmap[da + 999] = bitmap[da + 1000] = bitmap[da + 380];
            bitmap[da + 995] = bitmap[da + 996] = bitmap[da + 997] = bitmap[da + 379];
            bitmap[da + 993] = bitmap[da + 994] = bitmap[da + 378];
            bitmap[da + 990] = bitmap[da + 991] = bitmap[da + 992] = bitmap[da + 377];
            bitmap[da + 987] = bitmap[da + 988] = bitmap[da + 989] = bitmap[da + 376];
        case 47:
            bitmap[da + 985] = bitmap[da + 986] = bitmap[da + 375];
            bitmap[da + 982] = bitmap[da + 983] = bitmap[da + 984] = bitmap[da + 374];
            bitmap[da + 980] = bitmap[da + 981] = bitmap[da + 373];
            bitmap[da + 977] = bitmap[da + 978] = bitmap[da + 979] = bitmap[da + 372];
            bitmap[da + 974] = bitmap[da + 975] = bitmap[da + 976] = bitmap[da + 371];
            bitmap[da + 972] = bitmap[da + 973] = bitmap[da + 370];
            bitmap[da + 969] = bitmap[da + 970] = bitmap[da + 971] = bitmap[da + 369];
            bitmap[da + 966] = bitmap[da + 967] = bitmap[da + 968] = bitmap[da + 368];
        case 46:
            bitmap[da + 964] = bitmap[da + 965] = bitmap[da + 367];
            bitmap[da + 961] = bitmap[da + 962] = bitmap[da + 963] = bitmap[da + 366];
            bitmap[da + 959] = bitmap[da + 960] = bitmap[da + 365];
            bitmap[da + 956] = bitmap[da + 957] = bitmap[da + 958] = bitmap[da + 364];
            bitmap[da + 953] = bitmap[da + 954] = bitmap[da + 955] = bitmap[da + 363];
            bitmap[da + 951] = bitmap[da + 952] = bitmap[da + 362];
            bitmap[da + 948] = bitmap[da + 949] = bitmap[da + 950] = bitmap[da + 361];
            bitmap[da + 945] = bitmap[da + 946] = bitmap[da + 947] = bitmap[da + 360];
        case 45:
            bitmap[da + 943] = bitmap[da + 944] = bitmap[da + 359];
            bitmap[da + 940] = bitmap[da + 941] = bitmap[da + 942] = bitmap[da + 358];
            bitmap[da + 938] = bitmap[da + 939] = bitmap[da + 357];
            bitmap[da + 935] = bitmap[da + 936] = bitmap[da + 937] = bitmap[da + 356];
            bitmap[da + 932] = bitmap[da + 933] = bitmap[da + 934] = bitmap[da + 355];
            bitmap[da + 930] = bitmap[da + 931] = bitmap[da + 354];
            bitmap[da + 927] = bitmap[da + 928] = bitmap[da + 929] = bitmap[da + 353];
            bitmap[da + 924] = bitmap[da + 925] = bitmap[da + 926] = bitmap[da + 352];
        case 44:
            bitmap[da + 922] = bitmap[da + 923] = bitmap[da + 351];
            bitmap[da + 919] = bitmap[da + 920] = bitmap[da + 921] = bitmap[da + 350];
            bitmap[da + 917] = bitmap[da + 918] = bitmap[da + 349];
            bitmap[da + 914] = bitmap[da + 915] = bitmap[da + 916] = bitmap[da + 348];
            bitmap[da + 911] = bitmap[da + 912] = bitmap[da + 913] = bitmap[da + 347];
            bitmap[da + 909] = bitmap[da + 910] = bitmap[da + 346];
            bitmap[da + 906] = bitmap[da + 907] = bitmap[da + 908] = bitmap[da + 345];
            bitmap[da + 903] = bitmap[da + 904] = bitmap[da + 905] = bitmap[da + 344];
        case 43:
            bitmap[da + 901] = bitmap[da + 902] = bitmap[da + 343];
            bitmap[da + 898] = bitmap[da + 899] = bitmap[da + 900] = bitmap[da + 342];
            bitmap[da + 896] = bitmap[da + 897] = bitmap[da + 341];
            bitmap[da + 893] = bitmap[da + 894] = bitmap[da + 895] = bitmap[da + 340];
            bitmap[da + 890] = bitmap[da + 891] = bitmap[da + 892] = bitmap[da + 339];
            bitmap[da + 888] = bitmap[da + 889] = bitmap[da + 338];
            bitmap[da + 885] = bitmap[da + 886] = bitmap[da + 887] = bitmap[da + 337];
            bitmap[da + 882] = bitmap[da + 883] = bitmap[da + 884] = bitmap[da + 336];
        case 42:
            bitmap[da + 880] = bitmap[da + 881] = bitmap[da + 335];
            bitmap[da + 877] = bitmap[da + 878] = bitmap[da + 879] = bitmap[da + 334];
            bitmap[da + 875] = bitmap[da + 876] = bitmap[da + 333];
            bitmap[da + 872] = bitmap[da + 873] = bitmap[da + 874] = bitmap[da + 332];
            bitmap[da + 869] = bitmap[da + 870] = bitmap[da + 871] = bitmap[da + 331];
            bitmap[da + 867] = bitmap[da + 868] = bitmap[da + 330];
            bitmap[da + 864] = bitmap[da + 865] = bitmap[da + 866] = bitmap[da + 329];
            bitmap[da + 861] = bitmap[da + 862] = bitmap[da + 863] = bitmap[da + 328];
        case 41:
            bitmap[da + 859] = bitmap[da + 860] = bitmap[da + 327];
            bitmap[da + 856] = bitmap[da + 857] = bitmap[da + 858] = bitmap[da + 326];
            bitmap[da + 854] = bitmap[da + 855] = bitmap[da + 325];
            bitmap[da + 851] = bitmap[da + 852] = bitmap[da + 853] = bitmap[da + 324];
            bitmap[da + 848] = bitmap[da + 849] = bitmap[da + 850] = bitmap[da + 323];
            bitmap[da + 846] = bitmap[da + 847] = bitmap[da + 322];
            bitmap[da + 843] = bitmap[da + 844] = bitmap[da + 845] = bitmap[da + 321];
            bitmap[da + 840] = bitmap[da + 841] = bitmap[da + 842] = bitmap[da + 320];
        case 40:
            bitmap[da + 838] = bitmap[da + 839] = bitmap[da + 319];
            bitmap[da + 835] = bitmap[da + 836] = bitmap[da + 837] = bitmap[da + 318];
            bitmap[da + 833] = bitmap[da + 834] = bitmap[da + 317];
            bitmap[da + 830] = bitmap[da + 831] = bitmap[da + 832] = bitmap[da + 316];
            bitmap[da + 827] = bitmap[da + 828] = bitmap[da + 829] = bitmap[da + 315];
            bitmap[da + 825] = bitmap[da + 826] = bitmap[da + 314];
            bitmap[da + 822] = bitmap[da + 823] = bitmap[da + 824] = bitmap[da + 313];
            bitmap[da + 819] = bitmap[da + 820] = bitmap[da + 821] = bitmap[da + 312];
        case 39:
            bitmap[da + 817] = bitmap[da + 818] = bitmap[da + 311];
            bitmap[da + 814] = bitmap[da + 815] = bitmap[da + 816] = bitmap[da + 310];
            bitmap[da + 812] = bitmap[da + 813] = bitmap[da + 309];
            bitmap[da + 809] = bitmap[da + 810] = bitmap[da + 811] = bitmap[da + 308];
            bitmap[da + 806] = bitmap[da + 807] = bitmap[da + 808] = bitmap[da + 307];
            bitmap[da + 804] = bitmap[da + 805] = bitmap[da + 306];
            bitmap[da + 801] = bitmap[da + 802] = bitmap[da + 803] = bitmap[da + 305];
            bitmap[da + 798] = bitmap[da + 799] = bitmap[da + 800] = bitmap[da + 304];
        case 38:
            bitmap[da + 796] = bitmap[da + 797] = bitmap[da + 303];
            bitmap[da + 793] = bitmap[da + 794] = bitmap[da + 795] = bitmap[da + 302];
            bitmap[da + 791] = bitmap[da + 792] = bitmap[da + 301];
            bitmap[da + 788] = bitmap[da + 789] = bitmap[da + 790] = bitmap[da + 300];
            bitmap[da + 785] = bitmap[da + 786] = bitmap[da + 787] = bitmap[da + 299];
            bitmap[da + 783] = bitmap[da + 784] = bitmap[da + 298];
            bitmap[da + 780] = bitmap[da + 781] = bitmap[da + 782] = bitmap[da + 297];
            bitmap[da + 777] = bitmap[da + 778] = bitmap[da + 779] = bitmap[da + 296];
        case 37:
            bitmap[da + 775] = bitmap[da + 776] = bitmap[da + 295];
            bitmap[da + 772] = bitmap[da + 773] = bitmap[da + 774] = bitmap[da + 294];
            bitmap[da + 770] = bitmap[da + 771] = bitmap[da + 293];
            bitmap[da + 767] = bitmap[da + 768] = bitmap[da + 769] = bitmap[da + 292];
            bitmap[da + 764] = bitmap[da + 765] = bitmap[da + 766] = bitmap[da + 291];
            bitmap[da + 762] = bitmap[da + 763] = bitmap[da + 290];
            bitmap[da + 759] = bitmap[da + 760] = bitmap[da + 761] = bitmap[da + 289];
            bitmap[da + 756] = bitmap[da + 757] = bitmap[da + 758] = bitmap[da + 288];
        case 36:
            bitmap[da + 754] = bitmap[da + 755] = bitmap[da + 287];
            bitmap[da + 751] = bitmap[da + 752] = bitmap[da + 753] = bitmap[da + 286];
            bitmap[da + 749] = bitmap[da + 750] = bitmap[da + 285];
            bitmap[da + 746] = bitmap[da + 747] = bitmap[da + 748] = bitmap[da + 284];
            bitmap[da + 743] = bitmap[da + 744] = bitmap[da + 745] = bitmap[da + 283];
            bitmap[da + 741] = bitmap[da + 742] = bitmap[da + 282];
            bitmap[da + 738] = bitmap[da + 739] = bitmap[da + 740] = bitmap[da + 281];
            bitmap[da + 735] = bitmap[da + 736] = bitmap[da + 737] = bitmap[da + 280];
        case 35:
            bitmap[da + 733] = bitmap[da + 734] = bitmap[da + 279];
            bitmap[da + 730] = bitmap[da + 731] = bitmap[da + 732] = bitmap[da + 278];
            bitmap[da + 728] = bitmap[da + 729] = bitmap[da + 277];
            bitmap[da + 725] = bitmap[da + 726] = bitmap[da + 727] = bitmap[da + 276];
            bitmap[da + 722] = bitmap[da + 723] = bitmap[da + 724] = bitmap[da + 275];
            bitmap[da + 720] = bitmap[da + 721] = bitmap[da + 274];
            bitmap[da + 717] = bitmap[da + 718] = bitmap[da + 719] = bitmap[da + 273];
            bitmap[da + 714] = bitmap[da + 715] = bitmap[da + 716] = bitmap[da + 272];
        case 34:
            bitmap[da + 712] = bitmap[da + 713] = bitmap[da + 271];
            bitmap[da + 709] = bitmap[da + 710] = bitmap[da + 711] = bitmap[da + 270];
            bitmap[da + 707] = bitmap[da + 708] = bitmap[da + 269];
            bitmap[da + 704] = bitmap[da + 705] = bitmap[da + 706] = bitmap[da + 268];
            bitmap[da + 701] = bitmap[da + 702] = bitmap[da + 703] = bitmap[da + 267];
            bitmap[da + 699] = bitmap[da + 700] = bitmap[da + 266];
            bitmap[da + 696] = bitmap[da + 697] = bitmap[da + 698] = bitmap[da + 265];
            bitmap[da + 693] = bitmap[da + 694] = bitmap[da + 695] = bitmap[da + 264];
        case 33:
            bitmap[da + 691] = bitmap[da + 692] = bitmap[da + 263];
            bitmap[da + 688] = bitmap[da + 689] = bitmap[da + 690] = bitmap[da + 262];
            bitmap[da + 686] = bitmap[da + 687] = bitmap[da + 261];
            bitmap[da + 683] = bitmap[da + 684] = bitmap[da + 685] = bitmap[da + 260];
            bitmap[da + 680] = bitmap[da + 681] = bitmap[da + 682] = bitmap[da + 259];
            bitmap[da + 678] = bitmap[da + 679] = bitmap[da + 258];
            bitmap[da + 675] = bitmap[da + 676] = bitmap[da + 677] = bitmap[da + 257];
            bitmap[da + 672] = bitmap[da + 673] = bitmap[da + 674] = bitmap[da + 256];
        case 32:
            bitmap[da + 670] = bitmap[da + 671] = bitmap[da + 255];
            bitmap[da + 667] = bitmap[da + 668] = bitmap[da + 669] = bitmap[da + 254];
            bitmap[da + 665] = bitmap[da + 666] = bitmap[da + 253];
            bitmap[da + 662] = bitmap[da + 663] = bitmap[da + 664] = bitmap[da + 252];
            bitmap[da + 659] = bitmap[da + 660] = bitmap[da + 661] = bitmap[da + 251];
            bitmap[da + 657] = bitmap[da + 658] = bitmap[da + 250];
            bitmap[da + 654] = bitmap[da + 655] = bitmap[da + 656] = bitmap[da + 249];
            bitmap[da + 651] = bitmap[da + 652] = bitmap[da + 653] = bitmap[da + 248];
        case 31:
            bitmap[da + 649] = bitmap[da + 650] = bitmap[da + 247];
            bitmap[da + 646] = bitmap[da + 647] = bitmap[da + 648] = bitmap[da + 246];
            bitmap[da + 644] = bitmap[da + 645] = bitmap[da + 245];
            bitmap[da + 641] = bitmap[da + 642] = bitmap[da + 643] = bitmap[da + 244];
            bitmap[da + 638] = bitmap[da + 639] = bitmap[da + 640] = bitmap[da + 243];
            bitmap[da + 636] = bitmap[da + 637] = bitmap[da + 242];
            bitmap[da + 633] = bitmap[da + 634] = bitmap[da + 635] = bitmap[da + 241];
            bitmap[da + 630] = bitmap[da + 631] = bitmap[da + 632] = bitmap[da + 240];
        case 30:
            bitmap[da + 628] = bitmap[da + 629] = bitmap[da + 239];
            bitmap[da + 625] = bitmap[da + 626] = bitmap[da + 627] = bitmap[da + 238];
            bitmap[da + 623] = bitmap[da + 624] = bitmap[da + 237];
            bitmap[da + 620] = bitmap[da + 621] = bitmap[da + 622] = bitmap[da + 236];
            bitmap[da + 617] = bitmap[da + 618] = bitmap[da + 619] = bitmap[da + 235];
            bitmap[da + 615] = bitmap[da + 616] = bitmap[da + 234];
            bitmap[da + 612] = bitmap[da + 613] = bitmap[da + 614] = bitmap[da + 233];
            bitmap[da + 609] = bitmap[da + 610] = bitmap[da + 611] = bitmap[da + 232];
        case 29:
            bitmap[da + 607] = bitmap[da + 608] = bitmap[da + 231];
            bitmap[da + 604] = bitmap[da + 605] = bitmap[da + 606] = bitmap[da + 230];
            bitmap[da + 602] = bitmap[da + 603] = bitmap[da + 229];
            bitmap[da + 599] = bitmap[da + 600] = bitmap[da + 601] = bitmap[da + 228];
            bitmap[da + 596] = bitmap[da + 597] = bitmap[da + 598] = bitmap[da + 227];
            bitmap[da + 594] = bitmap[da + 595] = bitmap[da + 226];
            bitmap[da + 591] = bitmap[da + 592] = bitmap[da + 593] = bitmap[da + 225];
            bitmap[da + 588] = bitmap[da + 589] = bitmap[da + 590] = bitmap[da + 224];
        case 28:
            bitmap[da + 586] = bitmap[da + 587] = bitmap[da + 223];
            bitmap[da + 583] = bitmap[da + 584] = bitmap[da + 585] = bitmap[da + 222];
            bitmap[da + 581] = bitmap[da + 582] = bitmap[da + 221];
            bitmap[da + 578] = bitmap[da + 579] = bitmap[da + 580] = bitmap[da + 220];
            bitmap[da + 575] = bitmap[da + 576] = bitmap[da + 577] = bitmap[da + 219];
            bitmap[da + 573] = bitmap[da + 574] = bitmap[da + 218];
            bitmap[da + 570] = bitmap[da + 571] = bitmap[da + 572] = bitmap[da + 217];
            bitmap[da + 567] = bitmap[da + 568] = bitmap[da + 569] = bitmap[da + 216];
        case 27:
            bitmap[da + 565] = bitmap[da + 566] = bitmap[da + 215];
            bitmap[da + 562] = bitmap[da + 563] = bitmap[da + 564] = bitmap[da + 214];
            bitmap[da + 560] = bitmap[da + 561] = bitmap[da + 213];
            bitmap[da + 557] = bitmap[da + 558] = bitmap[da + 559] = bitmap[da + 212];
            bitmap[da + 554] = bitmap[da + 555] = bitmap[da + 556] = bitmap[da + 211];
            bitmap[da + 552] = bitmap[da + 553] = bitmap[da + 210];
            bitmap[da + 549] = bitmap[da + 550] = bitmap[da + 551] = bitmap[da + 209];
            bitmap[da + 546] = bitmap[da + 547] = bitmap[da + 548] = bitmap[da + 208];
        case 26:
            bitmap[da + 544] = bitmap[da + 545] = bitmap[da + 207];
            bitmap[da + 541] = bitmap[da + 542] = bitmap[da + 543] = bitmap[da + 206];
            bitmap[da + 539] = bitmap[da + 540] = bitmap[da + 205];
            bitmap[da + 536] = bitmap[da + 537] = bitmap[da + 538] = bitmap[da + 204];
            bitmap[da + 533] = bitmap[da + 534] = bitmap[da + 535] = bitmap[da + 203];
            bitmap[da + 531] = bitmap[da + 532] = bitmap[da + 202];
            bitmap[da + 528] = bitmap[da + 529] = bitmap[da + 530] = bitmap[da + 201];
            bitmap[da + 525] = bitmap[da + 526] = bitmap[da + 527] = bitmap[da + 200];
        case 25:
            bitmap[da + 523] = bitmap[da + 524] = bitmap[da + 199];
            bitmap[da + 520] = bitmap[da + 521] = bitmap[da + 522] = bitmap[da + 198];
            bitmap[da + 518] = bitmap[da + 519] = bitmap[da + 197];
            bitmap[da + 515] = bitmap[da + 516] = bitmap[da + 517] = bitmap[da + 196];
            bitmap[da + 512] = bitmap[da + 513] = bitmap[da + 514] = bitmap[da + 195];
            bitmap[da + 510] = bitmap[da + 511] = bitmap[da + 194];
            bitmap[da + 507] = bitmap[da + 508] = bitmap[da + 509] = bitmap[da + 193];
            bitmap[da + 504] = bitmap[da + 505] = bitmap[da + 506] = bitmap[da + 192];
        case 24:
            bitmap[da + 502] = bitmap[da + 503] = bitmap[da + 191];
            bitmap[da + 499] = bitmap[da + 500] = bitmap[da + 501] = bitmap[da + 190];
            bitmap[da + 497] = bitmap[da + 498] = bitmap[da + 189];
            bitmap[da + 494] = bitmap[da + 495] = bitmap[da + 496] = bitmap[da + 188];
            bitmap[da + 491] = bitmap[da + 492] = bitmap[da + 493] = bitmap[da + 187];
            bitmap[da + 489] = bitmap[da + 490] = bitmap[da + 186];
            bitmap[da + 486] = bitmap[da + 487] = bitmap[da + 488] = bitmap[da + 185];
            bitmap[da + 483] = bitmap[da + 484] = bitmap[da + 485] = bitmap[da + 184];
        case 23:
            bitmap[da + 481] = bitmap[da + 482] = bitmap[da + 183];
            bitmap[da + 478] = bitmap[da + 479] = bitmap[da + 480] = bitmap[da + 182];
            bitmap[da + 476] = bitmap[da + 477] = bitmap[da + 181];
            bitmap[da + 473] = bitmap[da + 474] = bitmap[da + 475] = bitmap[da + 180];
            bitmap[da + 470] = bitmap[da + 471] = bitmap[da + 472] = bitmap[da + 179];
            bitmap[da + 468] = bitmap[da + 469] = bitmap[da + 178];
            bitmap[da + 465] = bitmap[da + 466] = bitmap[da + 467] = bitmap[da + 177];
            bitmap[da + 462] = bitmap[da + 463] = bitmap[da + 464] = bitmap[da + 176];
        case 22:
            bitmap[da + 460] = bitmap[da + 461] = bitmap[da + 175];
            bitmap[da + 457] = bitmap[da + 458] = bitmap[da + 459] = bitmap[da + 174];
            bitmap[da + 455] = bitmap[da + 456] = bitmap[da + 173];
            bitmap[da + 452] = bitmap[da + 453] = bitmap[da + 454] = bitmap[da + 172];
            bitmap[da + 449] = bitmap[da + 450] = bitmap[da + 451] = bitmap[da + 171];
            bitmap[da + 447] = bitmap[da + 448] = bitmap[da + 170];
            bitmap[da + 444] = bitmap[da + 445] = bitmap[da + 446] = bitmap[da + 169];
            bitmap[da + 441] = bitmap[da + 442] = bitmap[da + 443] = bitmap[da + 168];
        case 21:
            bitmap[da + 439] = bitmap[da + 440] = bitmap[da + 167];
            bitmap[da + 436] = bitmap[da + 437] = bitmap[da + 438] = bitmap[da + 166];
            bitmap[da + 434] = bitmap[da + 435] = bitmap[da + 165];
            bitmap[da + 431] = bitmap[da + 432] = bitmap[da + 433] = bitmap[da + 164];
            bitmap[da + 428] = bitmap[da + 429] = bitmap[da + 430] = bitmap[da + 163];
            bitmap[da + 426] = bitmap[da + 427] = bitmap[da + 162];
            bitmap[da + 423] = bitmap[da + 424] = bitmap[da + 425] = bitmap[da + 161];
            bitmap[da + 420] = bitmap[da + 421] = bitmap[da + 422] = bitmap[da + 160];
        case 20:
            bitmap[da + 418] = bitmap[da + 419] = bitmap[da + 159];
            bitmap[da + 415] = bitmap[da + 416] = bitmap[da + 417] = bitmap[da + 158];
            bitmap[da + 413] = bitmap[da + 414] = bitmap[da + 157];
            bitmap[da + 410] = bitmap[da + 411] = bitmap[da + 412] = bitmap[da + 156];
            bitmap[da + 407] = bitmap[da + 408] = bitmap[da + 409] = bitmap[da + 155];
            bitmap[da + 405] = bitmap[da + 406] = bitmap[da + 154];
            bitmap[da + 402] = bitmap[da + 403] = bitmap[da + 404] = bitmap[da + 153];
            bitmap[da + 399] = bitmap[da + 400] = bitmap[da + 401] = bitmap[da + 152];
        case 19:
            bitmap[da + 397] = bitmap[da + 398] = bitmap[da + 151];
            bitmap[da + 394] = bitmap[da + 395] = bitmap[da + 396] = bitmap[da + 150];
            bitmap[da + 392] = bitmap[da + 393] = bitmap[da + 149];
            bitmap[da + 389] = bitmap[da + 390] = bitmap[da + 391] = bitmap[da + 148];
            bitmap[da + 386] = bitmap[da + 387] = bitmap[da + 388] = bitmap[da + 147];
            bitmap[da + 384] = bitmap[da + 385] = bitmap[da + 146];
            bitmap[da + 381] = bitmap[da + 382] = bitmap[da + 383] = bitmap[da + 145];
            bitmap[da + 378] = bitmap[da + 379] = bitmap[da + 380] = bitmap[da + 144];
        case 18:
            bitmap[da + 376] = bitmap[da + 377] = bitmap[da + 143];
            bitmap[da + 373] = bitmap[da + 374] = bitmap[da + 375] = bitmap[da + 142];
            bitmap[da + 371] = bitmap[da + 372] = bitmap[da + 141];
            bitmap[da + 368] = bitmap[da + 369] = bitmap[da + 370] = bitmap[da + 140];
            bitmap[da + 365] = bitmap[da + 366] = bitmap[da + 367] = bitmap[da + 139];
            bitmap[da + 363] = bitmap[da + 364] = bitmap[da + 138];
            bitmap[da + 360] = bitmap[da + 361] = bitmap[da + 362] = bitmap[da + 137];
            bitmap[da + 357] = bitmap[da + 358] = bitmap[da + 359] = bitmap[da + 136];
        case 17:
            bitmap[da + 355] = bitmap[da + 356] = bitmap[da + 135];
            bitmap[da + 352] = bitmap[da + 353] = bitmap[da + 354] = bitmap[da + 134];
            bitmap[da + 350] = bitmap[da + 351] = bitmap[da + 133];
            bitmap[da + 347] = bitmap[da + 348] = bitmap[da + 349] = bitmap[da + 132];
            bitmap[da + 344] = bitmap[da + 345] = bitmap[da + 346] = bitmap[da + 131];
            bitmap[da + 342] = bitmap[da + 343] = bitmap[da + 130];
            bitmap[da + 339] = bitmap[da + 340] = bitmap[da + 341] = bitmap[da + 129];
            bitmap[da + 336] = bitmap[da + 337] = bitmap[da + 338] = bitmap[da + 128];
        case 16:
            bitmap[da + 334] = bitmap[da + 335] = bitmap[da + 127];
            bitmap[da + 331] = bitmap[da + 332] = bitmap[da + 333] = bitmap[da + 126];
            bitmap[da + 329] = bitmap[da + 330] = bitmap[da + 125];
            bitmap[da + 326] = bitmap[da + 327] = bitmap[da + 328] = bitmap[da + 124];
            bitmap[da + 323] = bitmap[da + 324] = bitmap[da + 325] = bitmap[da + 123];
            bitmap[da + 321] = bitmap[da + 322] = bitmap[da + 122];
            bitmap[da + 318] = bitmap[da + 319] = bitmap[da + 320] = bitmap[da + 121];
            bitmap[da + 315] = bitmap[da + 316] = bitmap[da + 317] = bitmap[da + 120];
        case 15:
            bitmap[da + 313] = bitmap[da + 314] = bitmap[da + 119];
            bitmap[da + 310] = bitmap[da + 311] = bitmap[da + 312] = bitmap[da + 118];
            bitmap[da + 308] = bitmap[da + 309] = bitmap[da + 117];
            bitmap[da + 305] = bitmap[da + 306] = bitmap[da + 307] = bitmap[da + 116];
            bitmap[da + 302] = bitmap[da + 303] = bitmap[da + 304] = bitmap[da + 115];
            bitmap[da + 300] = bitmap[da + 301] = bitmap[da + 114];
            bitmap[da + 297] = bitmap[da + 298] = bitmap[da + 299] = bitmap[da + 113];
            bitmap[da + 294] = bitmap[da + 295] = bitmap[da + 296] = bitmap[da + 112];
        case 14:
            bitmap[da + 292] = bitmap[da + 293] = bitmap[da + 111];
            bitmap[da + 289] = bitmap[da + 290] = bitmap[da + 291] = bitmap[da + 110];
            bitmap[da + 287] = bitmap[da + 288] = bitmap[da + 109];
            bitmap[da + 284] = bitmap[da + 285] = bitmap[da + 286] = bitmap[da + 108];
            bitmap[da + 281] = bitmap[da + 282] = bitmap[da + 283] = bitmap[da + 107];
            bitmap[da + 279] = bitmap[da + 280] = bitmap[da + 106];
            bitmap[da + 276] = bitmap[da + 277] = bitmap[da + 278] = bitmap[da + 105];
            bitmap[da + 273] = bitmap[da + 274] = bitmap[da + 275] = bitmap[da + 104];
        case 13:
            bitmap[da + 271] = bitmap[da + 272] = bitmap[da + 103];
            bitmap[da + 268] = bitmap[da + 269] = bitmap[da + 270] = bitmap[da + 102];
            bitmap[da + 266] = bitmap[da + 267] = bitmap[da + 101];
            bitmap[da + 263] = bitmap[da + 264] = bitmap[da + 265] = bitmap[da + 100];
            bitmap[da + 260] = bitmap[da + 261] = bitmap[da + 262] = bitmap[da + 99];
            bitmap[da + 258] = bitmap[da + 259] = bitmap[da + 98];
            bitmap[da + 255] = bitmap[da + 256] = bitmap[da + 257] = bitmap[da + 97];
            bitmap[da + 252] = bitmap[da + 253] = bitmap[da + 254] = bitmap[da + 96];
        case 12:
            bitmap[da + 250] = bitmap[da + 251] = bitmap[da + 95];
            bitmap[da + 247] = bitmap[da + 248] = bitmap[da + 249] = bitmap[da + 94];
            bitmap[da + 245] = bitmap[da + 246] = bitmap[da + 93];
            bitmap[da + 242] = bitmap[da + 243] = bitmap[da + 244] = bitmap[da + 92];
            bitmap[da + 239] = bitmap[da + 240] = bitmap[da + 241] = bitmap[da + 91];
            bitmap[da + 237] = bitmap[da + 238] = bitmap[da + 90];
            bitmap[da + 234] = bitmap[da + 235] = bitmap[da + 236] = bitmap[da + 89];
            bitmap[da + 231] = bitmap[da + 232] = bitmap[da + 233] = bitmap[da + 88];
        case 11:
            bitmap[da + 229] = bitmap[da + 230] = bitmap[da + 87];
            bitmap[da + 226] = bitmap[da + 227] = bitmap[da + 228] = bitmap[da + 86];
            bitmap[da + 224] = bitmap[da + 225] = bitmap[da + 85];
            bitmap[da + 221] = bitmap[da + 222] = bitmap[da + 223] = bitmap[da + 84];
            bitmap[da + 218] = bitmap[da + 219] = bitmap[da + 220] = bitmap[da + 83];
            bitmap[da + 216] = bitmap[da + 217] = bitmap[da + 82];
            bitmap[da + 213] = bitmap[da + 214] = bitmap[da + 215] = bitmap[da + 81];
            bitmap[da + 210] = bitmap[da + 211] = bitmap[da + 212] = bitmap[da + 80];
        case 10:
            bitmap[da + 208] = bitmap[da + 209] = bitmap[da + 79];
            bitmap[da + 205] = bitmap[da + 206] = bitmap[da + 207] = bitmap[da + 78];
            bitmap[da + 203] = bitmap[da + 204] = bitmap[da + 77];
            bitmap[da + 200] = bitmap[da + 201] = bitmap[da + 202] = bitmap[da + 76];
            bitmap[da + 197] = bitmap[da + 198] = bitmap[da + 199] = bitmap[da + 75];
            bitmap[da + 195] = bitmap[da + 196] = bitmap[da + 74];
            bitmap[da + 192] = bitmap[da + 193] = bitmap[da + 194] = bitmap[da + 73];
            bitmap[da + 189] = bitmap[da + 190] = bitmap[da + 191] = bitmap[da + 72];
        case 9:
            bitmap[da + 187] = bitmap[da + 188] = bitmap[da + 71];
            bitmap[da + 184] = bitmap[da + 185] = bitmap[da + 186] = bitmap[da + 70];
            bitmap[da + 182] = bitmap[da + 183] = bitmap[da + 69];
            bitmap[da + 179] = bitmap[da + 180] = bitmap[da + 181] = bitmap[da + 68];
            bitmap[da + 176] = bitmap[da + 177] = bitmap[da + 178] = bitmap[da + 67];
            bitmap[da + 174] = bitmap[da + 175] = bitmap[da + 66];
            bitmap[da + 171] = bitmap[da + 172] = bitmap[da + 173] = bitmap[da + 65];
            bitmap[da + 168] = bitmap[da + 169] = bitmap[da + 170] = bitmap[da + 64];
        case 8:
            bitmap[da + 166] = bitmap[da + 167] = bitmap[da + 63];
            bitmap[da + 163] = bitmap[da + 164] = bitmap[da + 165] = bitmap[da + 62];
            bitmap[da + 161] = bitmap[da + 162] = bitmap[da + 61];
            bitmap[da + 158] = bitmap[da + 159] = bitmap[da + 160] = bitmap[da + 60];
            bitmap[da + 155] = bitmap[da + 156] = bitmap[da + 157] = bitmap[da + 59];
            bitmap[da + 153] = bitmap[da + 154] = bitmap[da + 58];
            bitmap[da + 150] = bitmap[da + 151] = bitmap[da + 152] = bitmap[da + 57];
            bitmap[da + 147] = bitmap[da + 148] = bitmap[da + 149] = bitmap[da + 56];
        case 7:
            bitmap[da + 145] = bitmap[da + 146] = bitmap[da + 55];
            bitmap[da + 142] = bitmap[da + 143] = bitmap[da + 144] = bitmap[da + 54];
            bitmap[da + 140] = bitmap[da + 141] = bitmap[da + 53];
            bitmap[da + 137] = bitmap[da + 138] = bitmap[da + 139] = bitmap[da + 52];
            bitmap[da + 134] = bitmap[da + 135] = bitmap[da + 136] = bitmap[da + 51];
            bitmap[da + 132] = bitmap[da + 133] = bitmap[da + 50];
            bitmap[da + 129] = bitmap[da + 130] = bitmap[da + 131] = bitmap[da + 49];
            bitmap[da + 126] = bitmap[da + 127] = bitmap[da + 128] = bitmap[da + 48];
        case 6:
            bitmap[da + 124] = bitmap[da + 125] = bitmap[da + 47];
            bitmap[da + 121] = bitmap[da + 122] = bitmap[da + 123] = bitmap[da + 46];
            bitmap[da + 119] = bitmap[da + 120] = bitmap[da + 45];
            bitmap[da + 116] = bitmap[da + 117] = bitmap[da + 118] = bitmap[da + 44];
            bitmap[da + 113] = bitmap[da + 114] = bitmap[da + 115] = bitmap[da + 43];
            bitmap[da + 111] = bitmap[da + 112] = bitmap[da + 42];
            bitmap[da + 108] = bitmap[da + 109] = bitmap[da + 110] = bitmap[da + 41];
            bitmap[da + 105] = bitmap[da + 106] = bitmap[da + 107] = bitmap[da + 40];
        case 5:
            bitmap[da + 103] = bitmap[da + 104] = bitmap[da + 39];
            bitmap[da + 100] = bitmap[da + 101] = bitmap[da + 102] = bitmap[da + 38];
            bitmap[da + 98] = bitmap[da + 99] = bitmap[da + 37];
            bitmap[da + 95] = bitmap[da + 96] = bitmap[da + 97] = bitmap[da + 36];
            bitmap[da + 92] = bitmap[da + 93] = bitmap[da + 94] = bitmap[da + 35];
            bitmap[da + 90] = bitmap[da + 91] = bitmap[da + 34];
            bitmap[da + 87] = bitmap[da + 88] = bitmap[da + 89] = bitmap[da + 33];
            bitmap[da + 84] = bitmap[da + 85] = bitmap[da + 86] = bitmap[da + 32];
        case 4:
            bitmap[da + 82] = bitmap[da + 83] = bitmap[da + 31];
            bitmap[da + 79] = bitmap[da + 80] = bitmap[da + 81] = bitmap[da + 30];
            bitmap[da + 77] = bitmap[da + 78] = bitmap[da + 29];
            bitmap[da + 74] = bitmap[da + 75] = bitmap[da + 76] = bitmap[da + 28];
            bitmap[da + 71] = bitmap[da + 72] = bitmap[da + 73] = bitmap[da + 27];
            bitmap[da + 69] = bitmap[da + 70] = bitmap[da + 26];
            bitmap[da + 66] = bitmap[da + 67] = bitmap[da + 68] = bitmap[da + 25];
            bitmap[da + 63] = bitmap[da + 64] = bitmap[da + 65] = bitmap[da + 24];
        case 3:
            bitmap[da + 61] = bitmap[da + 62] = bitmap[da + 23];
            bitmap[da + 58] = bitmap[da + 59] = bitmap[da + 60] = bitmap[da + 22];
            bitmap[da + 56] = bitmap[da + 57] = bitmap[da + 21];
            bitmap[da + 53] = bitmap[da + 54] = bitmap[da + 55] = bitmap[da + 20];
            bitmap[da + 50] = bitmap[da + 51] = bitmap[da + 52] = bitmap[da + 19];
            bitmap[da + 48] = bitmap[da + 49] = bitmap[da + 18];
            bitmap[da + 45] = bitmap[da + 46] = bitmap[da + 47] = bitmap[da + 17];
            bitmap[da + 42] = bitmap[da + 43] = bitmap[da + 44] = bitmap[da + 16];
        case 2:
            bitmap[da + 40] = bitmap[da + 41] = bitmap[da + 15];
            bitmap[da + 37] = bitmap[da + 38] = bitmap[da + 39] = bitmap[da + 14];
            bitmap[da + 35] = bitmap[da + 36] = bitmap[da + 13];
            bitmap[da + 32] = bitmap[da + 33] = bitmap[da + 34] = bitmap[da + 12];
            bitmap[da + 29] = bitmap[da + 30] = bitmap[da + 31] = bitmap[da + 11];
            bitmap[da + 27] = bitmap[da + 28] = bitmap[da + 10];
            bitmap[da + 24] = bitmap[da + 25] = bitmap[da + 26] = bitmap[da + 9];
            bitmap[da + 21] = bitmap[da + 22] = bitmap[da + 23] = bitmap[da + 8];
        case 1:
            bitmap[da + 19] = bitmap[da + 20] = bitmap[da + 7];
            bitmap[da + 16] = bitmap[da + 17] = bitmap[da + 18] = bitmap[da + 6];
            bitmap[da + 14] = bitmap[da + 15] = bitmap[da + 5];
            bitmap[da + 11] = bitmap[da + 12] = bitmap[da + 13] = bitmap[da + 4];
            bitmap[da + 8] = bitmap[da + 9] = bitmap[da + 10] = bitmap[da + 3];
            bitmap[da + 6] = bitmap[da + 7] = bitmap[da + 2];
            bitmap[da + 3] = bitmap[da + 4] = bitmap[da + 5] = bitmap[da + 1];
            bitmap[da] = bitmap[da + 1] = bitmap[da + 2] = bitmap[da];
        }
    }

    private void stretch28(int dst) {
        int da = iw * dst;
        switch (dw >> 3) {
        case 128:
            bitmap[da + 3581] = bitmap[da + 3582] = bitmap[da + 3583] = bitmap[da + 1023];
            bitmap[da + 3577] = bitmap[da + 3578] = bitmap[da + 3579] = bitmap[da + 3580] = bitmap[da + 1022];
            bitmap[da + 3574] = bitmap[da + 3575] = bitmap[da + 3576] = bitmap[da + 1021];
            bitmap[da + 3570] = bitmap[da + 3571] = bitmap[da + 3572] = bitmap[da + 3573] = bitmap[da + 1020];
            bitmap[da + 3567] = bitmap[da + 3568] = bitmap[da + 3569] = bitmap[da + 1019];
            bitmap[da + 3563] = bitmap[da + 3564] = bitmap[da + 3565] = bitmap[da + 3566] = bitmap[da + 1018];
            bitmap[da + 3560] = bitmap[da + 3561] = bitmap[da + 3562] = bitmap[da + 1017];
            bitmap[da + 3556] = bitmap[da + 3557] = bitmap[da + 3558] = bitmap[da + 3559] = bitmap[da + 1016];
        case 127:
            bitmap[da + 3553] = bitmap[da + 3554] = bitmap[da + 3555] = bitmap[da + 1015];
            bitmap[da + 3549] = bitmap[da + 3550] = bitmap[da + 3551] = bitmap[da + 3552] = bitmap[da + 1014];
            bitmap[da + 3546] = bitmap[da + 3547] = bitmap[da + 3548] = bitmap[da + 1013];
            bitmap[da + 3542] = bitmap[da + 3543] = bitmap[da + 3544] = bitmap[da + 3545] = bitmap[da + 1012];
            bitmap[da + 3539] = bitmap[da + 3540] = bitmap[da + 3541] = bitmap[da + 1011];
            bitmap[da + 3535] = bitmap[da + 3536] = bitmap[da + 3537] = bitmap[da + 3538] = bitmap[da + 1010];
            bitmap[da + 3532] = bitmap[da + 3533] = bitmap[da + 3534] = bitmap[da + 1009];
            bitmap[da + 3528] = bitmap[da + 3529] = bitmap[da + 3530] = bitmap[da + 3531] = bitmap[da + 1008];
        case 126:
            bitmap[da + 3525] = bitmap[da + 3526] = bitmap[da + 3527] = bitmap[da + 1007];
            bitmap[da + 3521] = bitmap[da + 3522] = bitmap[da + 3523] = bitmap[da + 3524] = bitmap[da + 1006];
            bitmap[da + 3518] = bitmap[da + 3519] = bitmap[da + 3520] = bitmap[da + 1005];
            bitmap[da + 3514] = bitmap[da + 3515] = bitmap[da + 3516] = bitmap[da + 3517] = bitmap[da + 1004];
            bitmap[da + 3511] = bitmap[da + 3512] = bitmap[da + 3513] = bitmap[da + 1003];
            bitmap[da + 3507] = bitmap[da + 3508] = bitmap[da + 3509] = bitmap[da + 3510] = bitmap[da + 1002];
            bitmap[da + 3504] = bitmap[da + 3505] = bitmap[da + 3506] = bitmap[da + 1001];
            bitmap[da + 3500] = bitmap[da + 3501] = bitmap[da + 3502] = bitmap[da + 3503] = bitmap[da + 1000];
        case 125:
            bitmap[da + 3497] = bitmap[da + 3498] = bitmap[da + 3499] = bitmap[da + 999];
            bitmap[da + 3493] = bitmap[da + 3494] = bitmap[da + 3495] = bitmap[da + 3496] = bitmap[da + 998];
            bitmap[da + 3490] = bitmap[da + 3491] = bitmap[da + 3492] = bitmap[da + 997];
            bitmap[da + 3486] = bitmap[da + 3487] = bitmap[da + 3488] = bitmap[da + 3489] = bitmap[da + 996];
            bitmap[da + 3483] = bitmap[da + 3484] = bitmap[da + 3485] = bitmap[da + 995];
            bitmap[da + 3479] = bitmap[da + 3480] = bitmap[da + 3481] = bitmap[da + 3482] = bitmap[da + 994];
            bitmap[da + 3476] = bitmap[da + 3477] = bitmap[da + 3478] = bitmap[da + 993];
            bitmap[da + 3472] = bitmap[da + 3473] = bitmap[da + 3474] = bitmap[da + 3475] = bitmap[da + 992];
        case 124:
            bitmap[da + 3469] = bitmap[da + 3470] = bitmap[da + 3471] = bitmap[da + 991];
            bitmap[da + 3465] = bitmap[da + 3466] = bitmap[da + 3467] = bitmap[da + 3468] = bitmap[da + 990];
            bitmap[da + 3462] = bitmap[da + 3463] = bitmap[da + 3464] = bitmap[da + 989];
            bitmap[da + 3458] = bitmap[da + 3459] = bitmap[da + 3460] = bitmap[da + 3461] = bitmap[da + 988];
            bitmap[da + 3455] = bitmap[da + 3456] = bitmap[da + 3457] = bitmap[da + 987];
            bitmap[da + 3451] = bitmap[da + 3452] = bitmap[da + 3453] = bitmap[da + 3454] = bitmap[da + 986];
            bitmap[da + 3448] = bitmap[da + 3449] = bitmap[da + 3450] = bitmap[da + 985];
            bitmap[da + 3444] = bitmap[da + 3445] = bitmap[da + 3446] = bitmap[da + 3447] = bitmap[da + 984];
        case 123:
            bitmap[da + 3441] = bitmap[da + 3442] = bitmap[da + 3443] = bitmap[da + 983];
            bitmap[da + 3437] = bitmap[da + 3438] = bitmap[da + 3439] = bitmap[da + 3440] = bitmap[da + 982];
            bitmap[da + 3434] = bitmap[da + 3435] = bitmap[da + 3436] = bitmap[da + 981];
            bitmap[da + 3430] = bitmap[da + 3431] = bitmap[da + 3432] = bitmap[da + 3433] = bitmap[da + 980];
            bitmap[da + 3427] = bitmap[da + 3428] = bitmap[da + 3429] = bitmap[da + 979];
            bitmap[da + 3423] = bitmap[da + 3424] = bitmap[da + 3425] = bitmap[da + 3426] = bitmap[da + 978];
            bitmap[da + 3420] = bitmap[da + 3421] = bitmap[da + 3422] = bitmap[da + 977];
            bitmap[da + 3416] = bitmap[da + 3417] = bitmap[da + 3418] = bitmap[da + 3419] = bitmap[da + 976];
        case 122:
            bitmap[da + 3413] = bitmap[da + 3414] = bitmap[da + 3415] = bitmap[da + 975];
            bitmap[da + 3409] = bitmap[da + 3410] = bitmap[da + 3411] = bitmap[da + 3412] = bitmap[da + 974];
            bitmap[da + 3406] = bitmap[da + 3407] = bitmap[da + 3408] = bitmap[da + 973];
            bitmap[da + 3402] = bitmap[da + 3403] = bitmap[da + 3404] = bitmap[da + 3405] = bitmap[da + 972];
            bitmap[da + 3399] = bitmap[da + 3400] = bitmap[da + 3401] = bitmap[da + 971];
            bitmap[da + 3395] = bitmap[da + 3396] = bitmap[da + 3397] = bitmap[da + 3398] = bitmap[da + 970];
            bitmap[da + 3392] = bitmap[da + 3393] = bitmap[da + 3394] = bitmap[da + 969];
            bitmap[da + 3388] = bitmap[da + 3389] = bitmap[da + 3390] = bitmap[da + 3391] = bitmap[da + 968];
        case 121:
            bitmap[da + 3385] = bitmap[da + 3386] = bitmap[da + 3387] = bitmap[da + 967];
            bitmap[da + 3381] = bitmap[da + 3382] = bitmap[da + 3383] = bitmap[da + 3384] = bitmap[da + 966];
            bitmap[da + 3378] = bitmap[da + 3379] = bitmap[da + 3380] = bitmap[da + 965];
            bitmap[da + 3374] = bitmap[da + 3375] = bitmap[da + 3376] = bitmap[da + 3377] = bitmap[da + 964];
            bitmap[da + 3371] = bitmap[da + 3372] = bitmap[da + 3373] = bitmap[da + 963];
            bitmap[da + 3367] = bitmap[da + 3368] = bitmap[da + 3369] = bitmap[da + 3370] = bitmap[da + 962];
            bitmap[da + 3364] = bitmap[da + 3365] = bitmap[da + 3366] = bitmap[da + 961];
            bitmap[da + 3360] = bitmap[da + 3361] = bitmap[da + 3362] = bitmap[da + 3363] = bitmap[da + 960];
        case 120:
            bitmap[da + 3357] = bitmap[da + 3358] = bitmap[da + 3359] = bitmap[da + 959];
            bitmap[da + 3353] = bitmap[da + 3354] = bitmap[da + 3355] = bitmap[da + 3356] = bitmap[da + 958];
            bitmap[da + 3350] = bitmap[da + 3351] = bitmap[da + 3352] = bitmap[da + 957];
            bitmap[da + 3346] = bitmap[da + 3347] = bitmap[da + 3348] = bitmap[da + 3349] = bitmap[da + 956];
            bitmap[da + 3343] = bitmap[da + 3344] = bitmap[da + 3345] = bitmap[da + 955];
            bitmap[da + 3339] = bitmap[da + 3340] = bitmap[da + 3341] = bitmap[da + 3342] = bitmap[da + 954];
            bitmap[da + 3336] = bitmap[da + 3337] = bitmap[da + 3338] = bitmap[da + 953];
            bitmap[da + 3332] = bitmap[da + 3333] = bitmap[da + 3334] = bitmap[da + 3335] = bitmap[da + 952];
        case 119:
            bitmap[da + 3329] = bitmap[da + 3330] = bitmap[da + 3331] = bitmap[da + 951];
            bitmap[da + 3325] = bitmap[da + 3326] = bitmap[da + 3327] = bitmap[da + 3328] = bitmap[da + 950];
            bitmap[da + 3322] = bitmap[da + 3323] = bitmap[da + 3324] = bitmap[da + 949];
            bitmap[da + 3318] = bitmap[da + 3319] = bitmap[da + 3320] = bitmap[da + 3321] = bitmap[da + 948];
            bitmap[da + 3315] = bitmap[da + 3316] = bitmap[da + 3317] = bitmap[da + 947];
            bitmap[da + 3311] = bitmap[da + 3312] = bitmap[da + 3313] = bitmap[da + 3314] = bitmap[da + 946];
            bitmap[da + 3308] = bitmap[da + 3309] = bitmap[da + 3310] = bitmap[da + 945];
            bitmap[da + 3304] = bitmap[da + 3305] = bitmap[da + 3306] = bitmap[da + 3307] = bitmap[da + 944];
        case 118:
            bitmap[da + 3301] = bitmap[da + 3302] = bitmap[da + 3303] = bitmap[da + 943];
            bitmap[da + 3297] = bitmap[da + 3298] = bitmap[da + 3299] = bitmap[da + 3300] = bitmap[da + 942];
            bitmap[da + 3294] = bitmap[da + 3295] = bitmap[da + 3296] = bitmap[da + 941];
            bitmap[da + 3290] = bitmap[da + 3291] = bitmap[da + 3292] = bitmap[da + 3293] = bitmap[da + 940];
            bitmap[da + 3287] = bitmap[da + 3288] = bitmap[da + 3289] = bitmap[da + 939];
            bitmap[da + 3283] = bitmap[da + 3284] = bitmap[da + 3285] = bitmap[da + 3286] = bitmap[da + 938];
            bitmap[da + 3280] = bitmap[da + 3281] = bitmap[da + 3282] = bitmap[da + 937];
            bitmap[da + 3276] = bitmap[da + 3277] = bitmap[da + 3278] = bitmap[da + 3279] = bitmap[da + 936];
        case 117:
            bitmap[da + 3273] = bitmap[da + 3274] = bitmap[da + 3275] = bitmap[da + 935];
            bitmap[da + 3269] = bitmap[da + 3270] = bitmap[da + 3271] = bitmap[da + 3272] = bitmap[da + 934];
            bitmap[da + 3266] = bitmap[da + 3267] = bitmap[da + 3268] = bitmap[da + 933];
            bitmap[da + 3262] = bitmap[da + 3263] = bitmap[da + 3264] = bitmap[da + 3265] = bitmap[da + 932];
            bitmap[da + 3259] = bitmap[da + 3260] = bitmap[da + 3261] = bitmap[da + 931];
            bitmap[da + 3255] = bitmap[da + 3256] = bitmap[da + 3257] = bitmap[da + 3258] = bitmap[da + 930];
            bitmap[da + 3252] = bitmap[da + 3253] = bitmap[da + 3254] = bitmap[da + 929];
            bitmap[da + 3248] = bitmap[da + 3249] = bitmap[da + 3250] = bitmap[da + 3251] = bitmap[da + 928];
        case 116:
            bitmap[da + 3245] = bitmap[da + 3246] = bitmap[da + 3247] = bitmap[da + 927];
            bitmap[da + 3241] = bitmap[da + 3242] = bitmap[da + 3243] = bitmap[da + 3244] = bitmap[da + 926];
            bitmap[da + 3238] = bitmap[da + 3239] = bitmap[da + 3240] = bitmap[da + 925];
            bitmap[da + 3234] = bitmap[da + 3235] = bitmap[da + 3236] = bitmap[da + 3237] = bitmap[da + 924];
            bitmap[da + 3231] = bitmap[da + 3232] = bitmap[da + 3233] = bitmap[da + 923];
            bitmap[da + 3227] = bitmap[da + 3228] = bitmap[da + 3229] = bitmap[da + 3230] = bitmap[da + 922];
            bitmap[da + 3224] = bitmap[da + 3225] = bitmap[da + 3226] = bitmap[da + 921];
            bitmap[da + 3220] = bitmap[da + 3221] = bitmap[da + 3222] = bitmap[da + 3223] = bitmap[da + 920];
        case 115:
            bitmap[da + 3217] = bitmap[da + 3218] = bitmap[da + 3219] = bitmap[da + 919];
            bitmap[da + 3213] = bitmap[da + 3214] = bitmap[da + 3215] = bitmap[da + 3216] = bitmap[da + 918];
            bitmap[da + 3210] = bitmap[da + 3211] = bitmap[da + 3212] = bitmap[da + 917];
            bitmap[da + 3206] = bitmap[da + 3207] = bitmap[da + 3208] = bitmap[da + 3209] = bitmap[da + 916];
            bitmap[da + 3203] = bitmap[da + 3204] = bitmap[da + 3205] = bitmap[da + 915];
            bitmap[da + 3199] = bitmap[da + 3200] = bitmap[da + 3201] = bitmap[da + 3202] = bitmap[da + 914];
            bitmap[da + 3196] = bitmap[da + 3197] = bitmap[da + 3198] = bitmap[da + 913];
            bitmap[da + 3192] = bitmap[da + 3193] = bitmap[da + 3194] = bitmap[da + 3195] = bitmap[da + 912];
        case 114:
            bitmap[da + 3189] = bitmap[da + 3190] = bitmap[da + 3191] = bitmap[da + 911];
            bitmap[da + 3185] = bitmap[da + 3186] = bitmap[da + 3187] = bitmap[da + 3188] = bitmap[da + 910];
            bitmap[da + 3182] = bitmap[da + 3183] = bitmap[da + 3184] = bitmap[da + 909];
            bitmap[da + 3178] = bitmap[da + 3179] = bitmap[da + 3180] = bitmap[da + 3181] = bitmap[da + 908];
            bitmap[da + 3175] = bitmap[da + 3176] = bitmap[da + 3177] = bitmap[da + 907];
            bitmap[da + 3171] = bitmap[da + 3172] = bitmap[da + 3173] = bitmap[da + 3174] = bitmap[da + 906];
            bitmap[da + 3168] = bitmap[da + 3169] = bitmap[da + 3170] = bitmap[da + 905];
            bitmap[da + 3164] = bitmap[da + 3165] = bitmap[da + 3166] = bitmap[da + 3167] = bitmap[da + 904];
        case 113:
            bitmap[da + 3161] = bitmap[da + 3162] = bitmap[da + 3163] = bitmap[da + 903];
            bitmap[da + 3157] = bitmap[da + 3158] = bitmap[da + 3159] = bitmap[da + 3160] = bitmap[da + 902];
            bitmap[da + 3154] = bitmap[da + 3155] = bitmap[da + 3156] = bitmap[da + 901];
            bitmap[da + 3150] = bitmap[da + 3151] = bitmap[da + 3152] = bitmap[da + 3153] = bitmap[da + 900];
            bitmap[da + 3147] = bitmap[da + 3148] = bitmap[da + 3149] = bitmap[da + 899];
            bitmap[da + 3143] = bitmap[da + 3144] = bitmap[da + 3145] = bitmap[da + 3146] = bitmap[da + 898];
            bitmap[da + 3140] = bitmap[da + 3141] = bitmap[da + 3142] = bitmap[da + 897];
            bitmap[da + 3136] = bitmap[da + 3137] = bitmap[da + 3138] = bitmap[da + 3139] = bitmap[da + 896];
        case 112:
            bitmap[da + 3133] = bitmap[da + 3134] = bitmap[da + 3135] = bitmap[da + 895];
            bitmap[da + 3129] = bitmap[da + 3130] = bitmap[da + 3131] = bitmap[da + 3132] = bitmap[da + 894];
            bitmap[da + 3126] = bitmap[da + 3127] = bitmap[da + 3128] = bitmap[da + 893];
            bitmap[da + 3122] = bitmap[da + 3123] = bitmap[da + 3124] = bitmap[da + 3125] = bitmap[da + 892];
            bitmap[da + 3119] = bitmap[da + 3120] = bitmap[da + 3121] = bitmap[da + 891];
            bitmap[da + 3115] = bitmap[da + 3116] = bitmap[da + 3117] = bitmap[da + 3118] = bitmap[da + 890];
            bitmap[da + 3112] = bitmap[da + 3113] = bitmap[da + 3114] = bitmap[da + 889];
            bitmap[da + 3108] = bitmap[da + 3109] = bitmap[da + 3110] = bitmap[da + 3111] = bitmap[da + 888];
        case 111:
            bitmap[da + 3105] = bitmap[da + 3106] = bitmap[da + 3107] = bitmap[da + 887];
            bitmap[da + 3101] = bitmap[da + 3102] = bitmap[da + 3103] = bitmap[da + 3104] = bitmap[da + 886];
            bitmap[da + 3098] = bitmap[da + 3099] = bitmap[da + 3100] = bitmap[da + 885];
            bitmap[da + 3094] = bitmap[da + 3095] = bitmap[da + 3096] = bitmap[da + 3097] = bitmap[da + 884];
            bitmap[da + 3091] = bitmap[da + 3092] = bitmap[da + 3093] = bitmap[da + 883];
            bitmap[da + 3087] = bitmap[da + 3088] = bitmap[da + 3089] = bitmap[da + 3090] = bitmap[da + 882];
            bitmap[da + 3084] = bitmap[da + 3085] = bitmap[da + 3086] = bitmap[da + 881];
            bitmap[da + 3080] = bitmap[da + 3081] = bitmap[da + 3082] = bitmap[da + 3083] = bitmap[da + 880];
        case 110:
            bitmap[da + 3077] = bitmap[da + 3078] = bitmap[da + 3079] = bitmap[da + 879];
            bitmap[da + 3073] = bitmap[da + 3074] = bitmap[da + 3075] = bitmap[da + 3076] = bitmap[da + 878];
            bitmap[da + 3070] = bitmap[da + 3071] = bitmap[da + 3072] = bitmap[da + 877];
            bitmap[da + 3066] = bitmap[da + 3067] = bitmap[da + 3068] = bitmap[da + 3069] = bitmap[da + 876];
            bitmap[da + 3063] = bitmap[da + 3064] = bitmap[da + 3065] = bitmap[da + 875];
            bitmap[da + 3059] = bitmap[da + 3060] = bitmap[da + 3061] = bitmap[da + 3062] = bitmap[da + 874];
            bitmap[da + 3056] = bitmap[da + 3057] = bitmap[da + 3058] = bitmap[da + 873];
            bitmap[da + 3052] = bitmap[da + 3053] = bitmap[da + 3054] = bitmap[da + 3055] = bitmap[da + 872];
        case 109:
            bitmap[da + 3049] = bitmap[da + 3050] = bitmap[da + 3051] = bitmap[da + 871];
            bitmap[da + 3045] = bitmap[da + 3046] = bitmap[da + 3047] = bitmap[da + 3048] = bitmap[da + 870];
            bitmap[da + 3042] = bitmap[da + 3043] = bitmap[da + 3044] = bitmap[da + 869];
            bitmap[da + 3038] = bitmap[da + 3039] = bitmap[da + 3040] = bitmap[da + 3041] = bitmap[da + 868];
            bitmap[da + 3035] = bitmap[da + 3036] = bitmap[da + 3037] = bitmap[da + 867];
            bitmap[da + 3031] = bitmap[da + 3032] = bitmap[da + 3033] = bitmap[da + 3034] = bitmap[da + 866];
            bitmap[da + 3028] = bitmap[da + 3029] = bitmap[da + 3030] = bitmap[da + 865];
            bitmap[da + 3024] = bitmap[da + 3025] = bitmap[da + 3026] = bitmap[da + 3027] = bitmap[da + 864];
        case 108:
            bitmap[da + 3021] = bitmap[da + 3022] = bitmap[da + 3023] = bitmap[da + 863];
            bitmap[da + 3017] = bitmap[da + 3018] = bitmap[da + 3019] = bitmap[da + 3020] = bitmap[da + 862];
            bitmap[da + 3014] = bitmap[da + 3015] = bitmap[da + 3016] = bitmap[da + 861];
            bitmap[da + 3010] = bitmap[da + 3011] = bitmap[da + 3012] = bitmap[da + 3013] = bitmap[da + 860];
            bitmap[da + 3007] = bitmap[da + 3008] = bitmap[da + 3009] = bitmap[da + 859];
            bitmap[da + 3003] = bitmap[da + 3004] = bitmap[da + 3005] = bitmap[da + 3006] = bitmap[da + 858];
            bitmap[da + 3000] = bitmap[da + 3001] = bitmap[da + 3002] = bitmap[da + 857];
            bitmap[da + 2996] = bitmap[da + 2997] = bitmap[da + 2998] = bitmap[da + 2999] = bitmap[da + 856];
        case 107:
            bitmap[da + 2993] = bitmap[da + 2994] = bitmap[da + 2995] = bitmap[da + 855];
            bitmap[da + 2989] = bitmap[da + 2990] = bitmap[da + 2991] = bitmap[da + 2992] = bitmap[da + 854];
            bitmap[da + 2986] = bitmap[da + 2987] = bitmap[da + 2988] = bitmap[da + 853];
            bitmap[da + 2982] = bitmap[da + 2983] = bitmap[da + 2984] = bitmap[da + 2985] = bitmap[da + 852];
            bitmap[da + 2979] = bitmap[da + 2980] = bitmap[da + 2981] = bitmap[da + 851];
            bitmap[da + 2975] = bitmap[da + 2976] = bitmap[da + 2977] = bitmap[da + 2978] = bitmap[da + 850];
            bitmap[da + 2972] = bitmap[da + 2973] = bitmap[da + 2974] = bitmap[da + 849];
            bitmap[da + 2968] = bitmap[da + 2969] = bitmap[da + 2970] = bitmap[da + 2971] = bitmap[da + 848];
        case 106:
            bitmap[da + 2965] = bitmap[da + 2966] = bitmap[da + 2967] = bitmap[da + 847];
            bitmap[da + 2961] = bitmap[da + 2962] = bitmap[da + 2963] = bitmap[da + 2964] = bitmap[da + 846];
            bitmap[da + 2958] = bitmap[da + 2959] = bitmap[da + 2960] = bitmap[da + 845];
            bitmap[da + 2954] = bitmap[da + 2955] = bitmap[da + 2956] = bitmap[da + 2957] = bitmap[da + 844];
            bitmap[da + 2951] = bitmap[da + 2952] = bitmap[da + 2953] = bitmap[da + 843];
            bitmap[da + 2947] = bitmap[da + 2948] = bitmap[da + 2949] = bitmap[da + 2950] = bitmap[da + 842];
            bitmap[da + 2944] = bitmap[da + 2945] = bitmap[da + 2946] = bitmap[da + 841];
            bitmap[da + 2940] = bitmap[da + 2941] = bitmap[da + 2942] = bitmap[da + 2943] = bitmap[da + 840];
        case 105:
            bitmap[da + 2937] = bitmap[da + 2938] = bitmap[da + 2939] = bitmap[da + 839];
            bitmap[da + 2933] = bitmap[da + 2934] = bitmap[da + 2935] = bitmap[da + 2936] = bitmap[da + 838];
            bitmap[da + 2930] = bitmap[da + 2931] = bitmap[da + 2932] = bitmap[da + 837];
            bitmap[da + 2926] = bitmap[da + 2927] = bitmap[da + 2928] = bitmap[da + 2929] = bitmap[da + 836];
            bitmap[da + 2923] = bitmap[da + 2924] = bitmap[da + 2925] = bitmap[da + 835];
            bitmap[da + 2919] = bitmap[da + 2920] = bitmap[da + 2921] = bitmap[da + 2922] = bitmap[da + 834];
            bitmap[da + 2916] = bitmap[da + 2917] = bitmap[da + 2918] = bitmap[da + 833];
            bitmap[da + 2912] = bitmap[da + 2913] = bitmap[da + 2914] = bitmap[da + 2915] = bitmap[da + 832];
        case 104:
            bitmap[da + 2909] = bitmap[da + 2910] = bitmap[da + 2911] = bitmap[da + 831];
            bitmap[da + 2905] = bitmap[da + 2906] = bitmap[da + 2907] = bitmap[da + 2908] = bitmap[da + 830];
            bitmap[da + 2902] = bitmap[da + 2903] = bitmap[da + 2904] = bitmap[da + 829];
            bitmap[da + 2898] = bitmap[da + 2899] = bitmap[da + 2900] = bitmap[da + 2901] = bitmap[da + 828];
            bitmap[da + 2895] = bitmap[da + 2896] = bitmap[da + 2897] = bitmap[da + 827];
            bitmap[da + 2891] = bitmap[da + 2892] = bitmap[da + 2893] = bitmap[da + 2894] = bitmap[da + 826];
            bitmap[da + 2888] = bitmap[da + 2889] = bitmap[da + 2890] = bitmap[da + 825];
            bitmap[da + 2884] = bitmap[da + 2885] = bitmap[da + 2886] = bitmap[da + 2887] = bitmap[da + 824];
        case 103:
            bitmap[da + 2881] = bitmap[da + 2882] = bitmap[da + 2883] = bitmap[da + 823];
            bitmap[da + 2877] = bitmap[da + 2878] = bitmap[da + 2879] = bitmap[da + 2880] = bitmap[da + 822];
            bitmap[da + 2874] = bitmap[da + 2875] = bitmap[da + 2876] = bitmap[da + 821];
            bitmap[da + 2870] = bitmap[da + 2871] = bitmap[da + 2872] = bitmap[da + 2873] = bitmap[da + 820];
            bitmap[da + 2867] = bitmap[da + 2868] = bitmap[da + 2869] = bitmap[da + 819];
            bitmap[da + 2863] = bitmap[da + 2864] = bitmap[da + 2865] = bitmap[da + 2866] = bitmap[da + 818];
            bitmap[da + 2860] = bitmap[da + 2861] = bitmap[da + 2862] = bitmap[da + 817];
            bitmap[da + 2856] = bitmap[da + 2857] = bitmap[da + 2858] = bitmap[da + 2859] = bitmap[da + 816];
        case 102:
            bitmap[da + 2853] = bitmap[da + 2854] = bitmap[da + 2855] = bitmap[da + 815];
            bitmap[da + 2849] = bitmap[da + 2850] = bitmap[da + 2851] = bitmap[da + 2852] = bitmap[da + 814];
            bitmap[da + 2846] = bitmap[da + 2847] = bitmap[da + 2848] = bitmap[da + 813];
            bitmap[da + 2842] = bitmap[da + 2843] = bitmap[da + 2844] = bitmap[da + 2845] = bitmap[da + 812];
            bitmap[da + 2839] = bitmap[da + 2840] = bitmap[da + 2841] = bitmap[da + 811];
            bitmap[da + 2835] = bitmap[da + 2836] = bitmap[da + 2837] = bitmap[da + 2838] = bitmap[da + 810];
            bitmap[da + 2832] = bitmap[da + 2833] = bitmap[da + 2834] = bitmap[da + 809];
            bitmap[da + 2828] = bitmap[da + 2829] = bitmap[da + 2830] = bitmap[da + 2831] = bitmap[da + 808];
        case 101:
            bitmap[da + 2825] = bitmap[da + 2826] = bitmap[da + 2827] = bitmap[da + 807];
            bitmap[da + 2821] = bitmap[da + 2822] = bitmap[da + 2823] = bitmap[da + 2824] = bitmap[da + 806];
            bitmap[da + 2818] = bitmap[da + 2819] = bitmap[da + 2820] = bitmap[da + 805];
            bitmap[da + 2814] = bitmap[da + 2815] = bitmap[da + 2816] = bitmap[da + 2817] = bitmap[da + 804];
            bitmap[da + 2811] = bitmap[da + 2812] = bitmap[da + 2813] = bitmap[da + 803];
            bitmap[da + 2807] = bitmap[da + 2808] = bitmap[da + 2809] = bitmap[da + 2810] = bitmap[da + 802];
            bitmap[da + 2804] = bitmap[da + 2805] = bitmap[da + 2806] = bitmap[da + 801];
            bitmap[da + 2800] = bitmap[da + 2801] = bitmap[da + 2802] = bitmap[da + 2803] = bitmap[da + 800];
        case 100:
            bitmap[da + 2797] = bitmap[da + 2798] = bitmap[da + 2799] = bitmap[da + 799];
            bitmap[da + 2793] = bitmap[da + 2794] = bitmap[da + 2795] = bitmap[da + 2796] = bitmap[da + 798];
            bitmap[da + 2790] = bitmap[da + 2791] = bitmap[da + 2792] = bitmap[da + 797];
            bitmap[da + 2786] = bitmap[da + 2787] = bitmap[da + 2788] = bitmap[da + 2789] = bitmap[da + 796];
            bitmap[da + 2783] = bitmap[da + 2784] = bitmap[da + 2785] = bitmap[da + 795];
            bitmap[da + 2779] = bitmap[da + 2780] = bitmap[da + 2781] = bitmap[da + 2782] = bitmap[da + 794];
            bitmap[da + 2776] = bitmap[da + 2777] = bitmap[da + 2778] = bitmap[da + 793];
            bitmap[da + 2772] = bitmap[da + 2773] = bitmap[da + 2774] = bitmap[da + 2775] = bitmap[da + 792];
        case 99:
            bitmap[da + 2769] = bitmap[da + 2770] = bitmap[da + 2771] = bitmap[da + 791];
            bitmap[da + 2765] = bitmap[da + 2766] = bitmap[da + 2767] = bitmap[da + 2768] = bitmap[da + 790];
            bitmap[da + 2762] = bitmap[da + 2763] = bitmap[da + 2764] = bitmap[da + 789];
            bitmap[da + 2758] = bitmap[da + 2759] = bitmap[da + 2760] = bitmap[da + 2761] = bitmap[da + 788];
            bitmap[da + 2755] = bitmap[da + 2756] = bitmap[da + 2757] = bitmap[da + 787];
            bitmap[da + 2751] = bitmap[da + 2752] = bitmap[da + 2753] = bitmap[da + 2754] = bitmap[da + 786];
            bitmap[da + 2748] = bitmap[da + 2749] = bitmap[da + 2750] = bitmap[da + 785];
            bitmap[da + 2744] = bitmap[da + 2745] = bitmap[da + 2746] = bitmap[da + 2747] = bitmap[da + 784];
        case 98:
            bitmap[da + 2741] = bitmap[da + 2742] = bitmap[da + 2743] = bitmap[da + 783];
            bitmap[da + 2737] = bitmap[da + 2738] = bitmap[da + 2739] = bitmap[da + 2740] = bitmap[da + 782];
            bitmap[da + 2734] = bitmap[da + 2735] = bitmap[da + 2736] = bitmap[da + 781];
            bitmap[da + 2730] = bitmap[da + 2731] = bitmap[da + 2732] = bitmap[da + 2733] = bitmap[da + 780];
            bitmap[da + 2727] = bitmap[da + 2728] = bitmap[da + 2729] = bitmap[da + 779];
            bitmap[da + 2723] = bitmap[da + 2724] = bitmap[da + 2725] = bitmap[da + 2726] = bitmap[da + 778];
            bitmap[da + 2720] = bitmap[da + 2721] = bitmap[da + 2722] = bitmap[da + 777];
            bitmap[da + 2716] = bitmap[da + 2717] = bitmap[da + 2718] = bitmap[da + 2719] = bitmap[da + 776];
        case 97:
            bitmap[da + 2713] = bitmap[da + 2714] = bitmap[da + 2715] = bitmap[da + 775];
            bitmap[da + 2709] = bitmap[da + 2710] = bitmap[da + 2711] = bitmap[da + 2712] = bitmap[da + 774];
            bitmap[da + 2706] = bitmap[da + 2707] = bitmap[da + 2708] = bitmap[da + 773];
            bitmap[da + 2702] = bitmap[da + 2703] = bitmap[da + 2704] = bitmap[da + 2705] = bitmap[da + 772];
            bitmap[da + 2699] = bitmap[da + 2700] = bitmap[da + 2701] = bitmap[da + 771];
            bitmap[da + 2695] = bitmap[da + 2696] = bitmap[da + 2697] = bitmap[da + 2698] = bitmap[da + 770];
            bitmap[da + 2692] = bitmap[da + 2693] = bitmap[da + 2694] = bitmap[da + 769];
            bitmap[da + 2688] = bitmap[da + 2689] = bitmap[da + 2690] = bitmap[da + 2691] = bitmap[da + 768];
        case 96:
            bitmap[da + 2685] = bitmap[da + 2686] = bitmap[da + 2687] = bitmap[da + 767];
            bitmap[da + 2681] = bitmap[da + 2682] = bitmap[da + 2683] = bitmap[da + 2684] = bitmap[da + 766];
            bitmap[da + 2678] = bitmap[da + 2679] = bitmap[da + 2680] = bitmap[da + 765];
            bitmap[da + 2674] = bitmap[da + 2675] = bitmap[da + 2676] = bitmap[da + 2677] = bitmap[da + 764];
            bitmap[da + 2671] = bitmap[da + 2672] = bitmap[da + 2673] = bitmap[da + 763];
            bitmap[da + 2667] = bitmap[da + 2668] = bitmap[da + 2669] = bitmap[da + 2670] = bitmap[da + 762];
            bitmap[da + 2664] = bitmap[da + 2665] = bitmap[da + 2666] = bitmap[da + 761];
            bitmap[da + 2660] = bitmap[da + 2661] = bitmap[da + 2662] = bitmap[da + 2663] = bitmap[da + 760];
        case 95:
            bitmap[da + 2657] = bitmap[da + 2658] = bitmap[da + 2659] = bitmap[da + 759];
            bitmap[da + 2653] = bitmap[da + 2654] = bitmap[da + 2655] = bitmap[da + 2656] = bitmap[da + 758];
            bitmap[da + 2650] = bitmap[da + 2651] = bitmap[da + 2652] = bitmap[da + 757];
            bitmap[da + 2646] = bitmap[da + 2647] = bitmap[da + 2648] = bitmap[da + 2649] = bitmap[da + 756];
            bitmap[da + 2643] = bitmap[da + 2644] = bitmap[da + 2645] = bitmap[da + 755];
            bitmap[da + 2639] = bitmap[da + 2640] = bitmap[da + 2641] = bitmap[da + 2642] = bitmap[da + 754];
            bitmap[da + 2636] = bitmap[da + 2637] = bitmap[da + 2638] = bitmap[da + 753];
            bitmap[da + 2632] = bitmap[da + 2633] = bitmap[da + 2634] = bitmap[da + 2635] = bitmap[da + 752];
        case 94:
            bitmap[da + 2629] = bitmap[da + 2630] = bitmap[da + 2631] = bitmap[da + 751];
            bitmap[da + 2625] = bitmap[da + 2626] = bitmap[da + 2627] = bitmap[da + 2628] = bitmap[da + 750];
            bitmap[da + 2622] = bitmap[da + 2623] = bitmap[da + 2624] = bitmap[da + 749];
            bitmap[da + 2618] = bitmap[da + 2619] = bitmap[da + 2620] = bitmap[da + 2621] = bitmap[da + 748];
            bitmap[da + 2615] = bitmap[da + 2616] = bitmap[da + 2617] = bitmap[da + 747];
            bitmap[da + 2611] = bitmap[da + 2612] = bitmap[da + 2613] = bitmap[da + 2614] = bitmap[da + 746];
            bitmap[da + 2608] = bitmap[da + 2609] = bitmap[da + 2610] = bitmap[da + 745];
            bitmap[da + 2604] = bitmap[da + 2605] = bitmap[da + 2606] = bitmap[da + 2607] = bitmap[da + 744];
        case 93:
            bitmap[da + 2601] = bitmap[da + 2602] = bitmap[da + 2603] = bitmap[da + 743];
            bitmap[da + 2597] = bitmap[da + 2598] = bitmap[da + 2599] = bitmap[da + 2600] = bitmap[da + 742];
            bitmap[da + 2594] = bitmap[da + 2595] = bitmap[da + 2596] = bitmap[da + 741];
            bitmap[da + 2590] = bitmap[da + 2591] = bitmap[da + 2592] = bitmap[da + 2593] = bitmap[da + 740];
            bitmap[da + 2587] = bitmap[da + 2588] = bitmap[da + 2589] = bitmap[da + 739];
            bitmap[da + 2583] = bitmap[da + 2584] = bitmap[da + 2585] = bitmap[da + 2586] = bitmap[da + 738];
            bitmap[da + 2580] = bitmap[da + 2581] = bitmap[da + 2582] = bitmap[da + 737];
            bitmap[da + 2576] = bitmap[da + 2577] = bitmap[da + 2578] = bitmap[da + 2579] = bitmap[da + 736];
        case 92:
            bitmap[da + 2573] = bitmap[da + 2574] = bitmap[da + 2575] = bitmap[da + 735];
            bitmap[da + 2569] = bitmap[da + 2570] = bitmap[da + 2571] = bitmap[da + 2572] = bitmap[da + 734];
            bitmap[da + 2566] = bitmap[da + 2567] = bitmap[da + 2568] = bitmap[da + 733];
            bitmap[da + 2562] = bitmap[da + 2563] = bitmap[da + 2564] = bitmap[da + 2565] = bitmap[da + 732];
            bitmap[da + 2559] = bitmap[da + 2560] = bitmap[da + 2561] = bitmap[da + 731];
            bitmap[da + 2555] = bitmap[da + 2556] = bitmap[da + 2557] = bitmap[da + 2558] = bitmap[da + 730];
            bitmap[da + 2552] = bitmap[da + 2553] = bitmap[da + 2554] = bitmap[da + 729];
            bitmap[da + 2548] = bitmap[da + 2549] = bitmap[da + 2550] = bitmap[da + 2551] = bitmap[da + 728];
        case 91:
            bitmap[da + 2545] = bitmap[da + 2546] = bitmap[da + 2547] = bitmap[da + 727];
            bitmap[da + 2541] = bitmap[da + 2542] = bitmap[da + 2543] = bitmap[da + 2544] = bitmap[da + 726];
            bitmap[da + 2538] = bitmap[da + 2539] = bitmap[da + 2540] = bitmap[da + 725];
            bitmap[da + 2534] = bitmap[da + 2535] = bitmap[da + 2536] = bitmap[da + 2537] = bitmap[da + 724];
            bitmap[da + 2531] = bitmap[da + 2532] = bitmap[da + 2533] = bitmap[da + 723];
            bitmap[da + 2527] = bitmap[da + 2528] = bitmap[da + 2529] = bitmap[da + 2530] = bitmap[da + 722];
            bitmap[da + 2524] = bitmap[da + 2525] = bitmap[da + 2526] = bitmap[da + 721];
            bitmap[da + 2520] = bitmap[da + 2521] = bitmap[da + 2522] = bitmap[da + 2523] = bitmap[da + 720];
        case 90:
            bitmap[da + 2517] = bitmap[da + 2518] = bitmap[da + 2519] = bitmap[da + 719];
            bitmap[da + 2513] = bitmap[da + 2514] = bitmap[da + 2515] = bitmap[da + 2516] = bitmap[da + 718];
            bitmap[da + 2510] = bitmap[da + 2511] = bitmap[da + 2512] = bitmap[da + 717];
            bitmap[da + 2506] = bitmap[da + 2507] = bitmap[da + 2508] = bitmap[da + 2509] = bitmap[da + 716];
            bitmap[da + 2503] = bitmap[da + 2504] = bitmap[da + 2505] = bitmap[da + 715];
            bitmap[da + 2499] = bitmap[da + 2500] = bitmap[da + 2501] = bitmap[da + 2502] = bitmap[da + 714];
            bitmap[da + 2496] = bitmap[da + 2497] = bitmap[da + 2498] = bitmap[da + 713];
            bitmap[da + 2492] = bitmap[da + 2493] = bitmap[da + 2494] = bitmap[da + 2495] = bitmap[da + 712];
        case 89:
            bitmap[da + 2489] = bitmap[da + 2490] = bitmap[da + 2491] = bitmap[da + 711];
            bitmap[da + 2485] = bitmap[da + 2486] = bitmap[da + 2487] = bitmap[da + 2488] = bitmap[da + 710];
            bitmap[da + 2482] = bitmap[da + 2483] = bitmap[da + 2484] = bitmap[da + 709];
            bitmap[da + 2478] = bitmap[da + 2479] = bitmap[da + 2480] = bitmap[da + 2481] = bitmap[da + 708];
            bitmap[da + 2475] = bitmap[da + 2476] = bitmap[da + 2477] = bitmap[da + 707];
            bitmap[da + 2471] = bitmap[da + 2472] = bitmap[da + 2473] = bitmap[da + 2474] = bitmap[da + 706];
            bitmap[da + 2468] = bitmap[da + 2469] = bitmap[da + 2470] = bitmap[da + 705];
            bitmap[da + 2464] = bitmap[da + 2465] = bitmap[da + 2466] = bitmap[da + 2467] = bitmap[da + 704];
        case 88:
            bitmap[da + 2461] = bitmap[da + 2462] = bitmap[da + 2463] = bitmap[da + 703];
            bitmap[da + 2457] = bitmap[da + 2458] = bitmap[da + 2459] = bitmap[da + 2460] = bitmap[da + 702];
            bitmap[da + 2454] = bitmap[da + 2455] = bitmap[da + 2456] = bitmap[da + 701];
            bitmap[da + 2450] = bitmap[da + 2451] = bitmap[da + 2452] = bitmap[da + 2453] = bitmap[da + 700];
            bitmap[da + 2447] = bitmap[da + 2448] = bitmap[da + 2449] = bitmap[da + 699];
            bitmap[da + 2443] = bitmap[da + 2444] = bitmap[da + 2445] = bitmap[da + 2446] = bitmap[da + 698];
            bitmap[da + 2440] = bitmap[da + 2441] = bitmap[da + 2442] = bitmap[da + 697];
            bitmap[da + 2436] = bitmap[da + 2437] = bitmap[da + 2438] = bitmap[da + 2439] = bitmap[da + 696];
        case 87:
            bitmap[da + 2433] = bitmap[da + 2434] = bitmap[da + 2435] = bitmap[da + 695];
            bitmap[da + 2429] = bitmap[da + 2430] = bitmap[da + 2431] = bitmap[da + 2432] = bitmap[da + 694];
            bitmap[da + 2426] = bitmap[da + 2427] = bitmap[da + 2428] = bitmap[da + 693];
            bitmap[da + 2422] = bitmap[da + 2423] = bitmap[da + 2424] = bitmap[da + 2425] = bitmap[da + 692];
            bitmap[da + 2419] = bitmap[da + 2420] = bitmap[da + 2421] = bitmap[da + 691];
            bitmap[da + 2415] = bitmap[da + 2416] = bitmap[da + 2417] = bitmap[da + 2418] = bitmap[da + 690];
            bitmap[da + 2412] = bitmap[da + 2413] = bitmap[da + 2414] = bitmap[da + 689];
            bitmap[da + 2408] = bitmap[da + 2409] = bitmap[da + 2410] = bitmap[da + 2411] = bitmap[da + 688];
        case 86:
            bitmap[da + 2405] = bitmap[da + 2406] = bitmap[da + 2407] = bitmap[da + 687];
            bitmap[da + 2401] = bitmap[da + 2402] = bitmap[da + 2403] = bitmap[da + 2404] = bitmap[da + 686];
            bitmap[da + 2398] = bitmap[da + 2399] = bitmap[da + 2400] = bitmap[da + 685];
            bitmap[da + 2394] = bitmap[da + 2395] = bitmap[da + 2396] = bitmap[da + 2397] = bitmap[da + 684];
            bitmap[da + 2391] = bitmap[da + 2392] = bitmap[da + 2393] = bitmap[da + 683];
            bitmap[da + 2387] = bitmap[da + 2388] = bitmap[da + 2389] = bitmap[da + 2390] = bitmap[da + 682];
            bitmap[da + 2384] = bitmap[da + 2385] = bitmap[da + 2386] = bitmap[da + 681];
            bitmap[da + 2380] = bitmap[da + 2381] = bitmap[da + 2382] = bitmap[da + 2383] = bitmap[da + 680];
        case 85:
            bitmap[da + 2377] = bitmap[da + 2378] = bitmap[da + 2379] = bitmap[da + 679];
            bitmap[da + 2373] = bitmap[da + 2374] = bitmap[da + 2375] = bitmap[da + 2376] = bitmap[da + 678];
            bitmap[da + 2370] = bitmap[da + 2371] = bitmap[da + 2372] = bitmap[da + 677];
            bitmap[da + 2366] = bitmap[da + 2367] = bitmap[da + 2368] = bitmap[da + 2369] = bitmap[da + 676];
            bitmap[da + 2363] = bitmap[da + 2364] = bitmap[da + 2365] = bitmap[da + 675];
            bitmap[da + 2359] = bitmap[da + 2360] = bitmap[da + 2361] = bitmap[da + 2362] = bitmap[da + 674];
            bitmap[da + 2356] = bitmap[da + 2357] = bitmap[da + 2358] = bitmap[da + 673];
            bitmap[da + 2352] = bitmap[da + 2353] = bitmap[da + 2354] = bitmap[da + 2355] = bitmap[da + 672];
        case 84:
            bitmap[da + 2349] = bitmap[da + 2350] = bitmap[da + 2351] = bitmap[da + 671];
            bitmap[da + 2345] = bitmap[da + 2346] = bitmap[da + 2347] = bitmap[da + 2348] = bitmap[da + 670];
            bitmap[da + 2342] = bitmap[da + 2343] = bitmap[da + 2344] = bitmap[da + 669];
            bitmap[da + 2338] = bitmap[da + 2339] = bitmap[da + 2340] = bitmap[da + 2341] = bitmap[da + 668];
            bitmap[da + 2335] = bitmap[da + 2336] = bitmap[da + 2337] = bitmap[da + 667];
            bitmap[da + 2331] = bitmap[da + 2332] = bitmap[da + 2333] = bitmap[da + 2334] = bitmap[da + 666];
            bitmap[da + 2328] = bitmap[da + 2329] = bitmap[da + 2330] = bitmap[da + 665];
            bitmap[da + 2324] = bitmap[da + 2325] = bitmap[da + 2326] = bitmap[da + 2327] = bitmap[da + 664];
        case 83:
            bitmap[da + 2321] = bitmap[da + 2322] = bitmap[da + 2323] = bitmap[da + 663];
            bitmap[da + 2317] = bitmap[da + 2318] = bitmap[da + 2319] = bitmap[da + 2320] = bitmap[da + 662];
            bitmap[da + 2314] = bitmap[da + 2315] = bitmap[da + 2316] = bitmap[da + 661];
            bitmap[da + 2310] = bitmap[da + 2311] = bitmap[da + 2312] = bitmap[da + 2313] = bitmap[da + 660];
            bitmap[da + 2307] = bitmap[da + 2308] = bitmap[da + 2309] = bitmap[da + 659];
            bitmap[da + 2303] = bitmap[da + 2304] = bitmap[da + 2305] = bitmap[da + 2306] = bitmap[da + 658];
            bitmap[da + 2300] = bitmap[da + 2301] = bitmap[da + 2302] = bitmap[da + 657];
            bitmap[da + 2296] = bitmap[da + 2297] = bitmap[da + 2298] = bitmap[da + 2299] = bitmap[da + 656];
        case 82:
            bitmap[da + 2293] = bitmap[da + 2294] = bitmap[da + 2295] = bitmap[da + 655];
            bitmap[da + 2289] = bitmap[da + 2290] = bitmap[da + 2291] = bitmap[da + 2292] = bitmap[da + 654];
            bitmap[da + 2286] = bitmap[da + 2287] = bitmap[da + 2288] = bitmap[da + 653];
            bitmap[da + 2282] = bitmap[da + 2283] = bitmap[da + 2284] = bitmap[da + 2285] = bitmap[da + 652];
            bitmap[da + 2279] = bitmap[da + 2280] = bitmap[da + 2281] = bitmap[da + 651];
            bitmap[da + 2275] = bitmap[da + 2276] = bitmap[da + 2277] = bitmap[da + 2278] = bitmap[da + 650];
            bitmap[da + 2272] = bitmap[da + 2273] = bitmap[da + 2274] = bitmap[da + 649];
            bitmap[da + 2268] = bitmap[da + 2269] = bitmap[da + 2270] = bitmap[da + 2271] = bitmap[da + 648];
        case 81:
            bitmap[da + 2265] = bitmap[da + 2266] = bitmap[da + 2267] = bitmap[da + 647];
            bitmap[da + 2261] = bitmap[da + 2262] = bitmap[da + 2263] = bitmap[da + 2264] = bitmap[da + 646];
            bitmap[da + 2258] = bitmap[da + 2259] = bitmap[da + 2260] = bitmap[da + 645];
            bitmap[da + 2254] = bitmap[da + 2255] = bitmap[da + 2256] = bitmap[da + 2257] = bitmap[da + 644];
            bitmap[da + 2251] = bitmap[da + 2252] = bitmap[da + 2253] = bitmap[da + 643];
            bitmap[da + 2247] = bitmap[da + 2248] = bitmap[da + 2249] = bitmap[da + 2250] = bitmap[da + 642];
            bitmap[da + 2244] = bitmap[da + 2245] = bitmap[da + 2246] = bitmap[da + 641];
            bitmap[da + 2240] = bitmap[da + 2241] = bitmap[da + 2242] = bitmap[da + 2243] = bitmap[da + 640];
        case 80:
            bitmap[da + 2237] = bitmap[da + 2238] = bitmap[da + 2239] = bitmap[da + 639];
            bitmap[da + 2233] = bitmap[da + 2234] = bitmap[da + 2235] = bitmap[da + 2236] = bitmap[da + 638];
            bitmap[da + 2230] = bitmap[da + 2231] = bitmap[da + 2232] = bitmap[da + 637];
            bitmap[da + 2226] = bitmap[da + 2227] = bitmap[da + 2228] = bitmap[da + 2229] = bitmap[da + 636];
            bitmap[da + 2223] = bitmap[da + 2224] = bitmap[da + 2225] = bitmap[da + 635];
            bitmap[da + 2219] = bitmap[da + 2220] = bitmap[da + 2221] = bitmap[da + 2222] = bitmap[da + 634];
            bitmap[da + 2216] = bitmap[da + 2217] = bitmap[da + 2218] = bitmap[da + 633];
            bitmap[da + 2212] = bitmap[da + 2213] = bitmap[da + 2214] = bitmap[da + 2215] = bitmap[da + 632];
        case 79:
            bitmap[da + 2209] = bitmap[da + 2210] = bitmap[da + 2211] = bitmap[da + 631];
            bitmap[da + 2205] = bitmap[da + 2206] = bitmap[da + 2207] = bitmap[da + 2208] = bitmap[da + 630];
            bitmap[da + 2202] = bitmap[da + 2203] = bitmap[da + 2204] = bitmap[da + 629];
            bitmap[da + 2198] = bitmap[da + 2199] = bitmap[da + 2200] = bitmap[da + 2201] = bitmap[da + 628];
            bitmap[da + 2195] = bitmap[da + 2196] = bitmap[da + 2197] = bitmap[da + 627];
            bitmap[da + 2191] = bitmap[da + 2192] = bitmap[da + 2193] = bitmap[da + 2194] = bitmap[da + 626];
            bitmap[da + 2188] = bitmap[da + 2189] = bitmap[da + 2190] = bitmap[da + 625];
            bitmap[da + 2184] = bitmap[da + 2185] = bitmap[da + 2186] = bitmap[da + 2187] = bitmap[da + 624];
        case 78:
            bitmap[da + 2181] = bitmap[da + 2182] = bitmap[da + 2183] = bitmap[da + 623];
            bitmap[da + 2177] = bitmap[da + 2178] = bitmap[da + 2179] = bitmap[da + 2180] = bitmap[da + 622];
            bitmap[da + 2174] = bitmap[da + 2175] = bitmap[da + 2176] = bitmap[da + 621];
            bitmap[da + 2170] = bitmap[da + 2171] = bitmap[da + 2172] = bitmap[da + 2173] = bitmap[da + 620];
            bitmap[da + 2167] = bitmap[da + 2168] = bitmap[da + 2169] = bitmap[da + 619];
            bitmap[da + 2163] = bitmap[da + 2164] = bitmap[da + 2165] = bitmap[da + 2166] = bitmap[da + 618];
            bitmap[da + 2160] = bitmap[da + 2161] = bitmap[da + 2162] = bitmap[da + 617];
            bitmap[da + 2156] = bitmap[da + 2157] = bitmap[da + 2158] = bitmap[da + 2159] = bitmap[da + 616];
        case 77:
            bitmap[da + 2153] = bitmap[da + 2154] = bitmap[da + 2155] = bitmap[da + 615];
            bitmap[da + 2149] = bitmap[da + 2150] = bitmap[da + 2151] = bitmap[da + 2152] = bitmap[da + 614];
            bitmap[da + 2146] = bitmap[da + 2147] = bitmap[da + 2148] = bitmap[da + 613];
            bitmap[da + 2142] = bitmap[da + 2143] = bitmap[da + 2144] = bitmap[da + 2145] = bitmap[da + 612];
            bitmap[da + 2139] = bitmap[da + 2140] = bitmap[da + 2141] = bitmap[da + 611];
            bitmap[da + 2135] = bitmap[da + 2136] = bitmap[da + 2137] = bitmap[da + 2138] = bitmap[da + 610];
            bitmap[da + 2132] = bitmap[da + 2133] = bitmap[da + 2134] = bitmap[da + 609];
            bitmap[da + 2128] = bitmap[da + 2129] = bitmap[da + 2130] = bitmap[da + 2131] = bitmap[da + 608];
        case 76:
            bitmap[da + 2125] = bitmap[da + 2126] = bitmap[da + 2127] = bitmap[da + 607];
            bitmap[da + 2121] = bitmap[da + 2122] = bitmap[da + 2123] = bitmap[da + 2124] = bitmap[da + 606];
            bitmap[da + 2118] = bitmap[da + 2119] = bitmap[da + 2120] = bitmap[da + 605];
            bitmap[da + 2114] = bitmap[da + 2115] = bitmap[da + 2116] = bitmap[da + 2117] = bitmap[da + 604];
            bitmap[da + 2111] = bitmap[da + 2112] = bitmap[da + 2113] = bitmap[da + 603];
            bitmap[da + 2107] = bitmap[da + 2108] = bitmap[da + 2109] = bitmap[da + 2110] = bitmap[da + 602];
            bitmap[da + 2104] = bitmap[da + 2105] = bitmap[da + 2106] = bitmap[da + 601];
            bitmap[da + 2100] = bitmap[da + 2101] = bitmap[da + 2102] = bitmap[da + 2103] = bitmap[da + 600];
        case 75:
            bitmap[da + 2097] = bitmap[da + 2098] = bitmap[da + 2099] = bitmap[da + 599];
            bitmap[da + 2093] = bitmap[da + 2094] = bitmap[da + 2095] = bitmap[da + 2096] = bitmap[da + 598];
            bitmap[da + 2090] = bitmap[da + 2091] = bitmap[da + 2092] = bitmap[da + 597];
            bitmap[da + 2086] = bitmap[da + 2087] = bitmap[da + 2088] = bitmap[da + 2089] = bitmap[da + 596];
            bitmap[da + 2083] = bitmap[da + 2084] = bitmap[da + 2085] = bitmap[da + 595];
            bitmap[da + 2079] = bitmap[da + 2080] = bitmap[da + 2081] = bitmap[da + 2082] = bitmap[da + 594];
            bitmap[da + 2076] = bitmap[da + 2077] = bitmap[da + 2078] = bitmap[da + 593];
            bitmap[da + 2072] = bitmap[da + 2073] = bitmap[da + 2074] = bitmap[da + 2075] = bitmap[da + 592];
        case 74:
            bitmap[da + 2069] = bitmap[da + 2070] = bitmap[da + 2071] = bitmap[da + 591];
            bitmap[da + 2065] = bitmap[da + 2066] = bitmap[da + 2067] = bitmap[da + 2068] = bitmap[da + 590];
            bitmap[da + 2062] = bitmap[da + 2063] = bitmap[da + 2064] = bitmap[da + 589];
            bitmap[da + 2058] = bitmap[da + 2059] = bitmap[da + 2060] = bitmap[da + 2061] = bitmap[da + 588];
            bitmap[da + 2055] = bitmap[da + 2056] = bitmap[da + 2057] = bitmap[da + 587];
            bitmap[da + 2051] = bitmap[da + 2052] = bitmap[da + 2053] = bitmap[da + 2054] = bitmap[da + 586];
            bitmap[da + 2048] = bitmap[da + 2049] = bitmap[da + 2050] = bitmap[da + 585];
            bitmap[da + 2044] = bitmap[da + 2045] = bitmap[da + 2046] = bitmap[da + 2047] = bitmap[da + 584];
        case 73:
            bitmap[da + 2041] = bitmap[da + 2042] = bitmap[da + 2043] = bitmap[da + 583];
            bitmap[da + 2037] = bitmap[da + 2038] = bitmap[da + 2039] = bitmap[da + 2040] = bitmap[da + 582];
            bitmap[da + 2034] = bitmap[da + 2035] = bitmap[da + 2036] = bitmap[da + 581];
            bitmap[da + 2030] = bitmap[da + 2031] = bitmap[da + 2032] = bitmap[da + 2033] = bitmap[da + 580];
            bitmap[da + 2027] = bitmap[da + 2028] = bitmap[da + 2029] = bitmap[da + 579];
            bitmap[da + 2023] = bitmap[da + 2024] = bitmap[da + 2025] = bitmap[da + 2026] = bitmap[da + 578];
            bitmap[da + 2020] = bitmap[da + 2021] = bitmap[da + 2022] = bitmap[da + 577];
            bitmap[da + 2016] = bitmap[da + 2017] = bitmap[da + 2018] = bitmap[da + 2019] = bitmap[da + 576];
        case 72:
            bitmap[da + 2013] = bitmap[da + 2014] = bitmap[da + 2015] = bitmap[da + 575];
            bitmap[da + 2009] = bitmap[da + 2010] = bitmap[da + 2011] = bitmap[da + 2012] = bitmap[da + 574];
            bitmap[da + 2006] = bitmap[da + 2007] = bitmap[da + 2008] = bitmap[da + 573];
            bitmap[da + 2002] = bitmap[da + 2003] = bitmap[da + 2004] = bitmap[da + 2005] = bitmap[da + 572];
            bitmap[da + 1999] = bitmap[da + 2000] = bitmap[da + 2001] = bitmap[da + 571];
            bitmap[da + 1995] = bitmap[da + 1996] = bitmap[da + 1997] = bitmap[da + 1998] = bitmap[da + 570];
            bitmap[da + 1992] = bitmap[da + 1993] = bitmap[da + 1994] = bitmap[da + 569];
            bitmap[da + 1988] = bitmap[da + 1989] = bitmap[da + 1990] = bitmap[da + 1991] = bitmap[da + 568];
        case 71:
            bitmap[da + 1985] = bitmap[da + 1986] = bitmap[da + 1987] = bitmap[da + 567];
            bitmap[da + 1981] = bitmap[da + 1982] = bitmap[da + 1983] = bitmap[da + 1984] = bitmap[da + 566];
            bitmap[da + 1978] = bitmap[da + 1979] = bitmap[da + 1980] = bitmap[da + 565];
            bitmap[da + 1974] = bitmap[da + 1975] = bitmap[da + 1976] = bitmap[da + 1977] = bitmap[da + 564];
            bitmap[da + 1971] = bitmap[da + 1972] = bitmap[da + 1973] = bitmap[da + 563];
            bitmap[da + 1967] = bitmap[da + 1968] = bitmap[da + 1969] = bitmap[da + 1970] = bitmap[da + 562];
            bitmap[da + 1964] = bitmap[da + 1965] = bitmap[da + 1966] = bitmap[da + 561];
            bitmap[da + 1960] = bitmap[da + 1961] = bitmap[da + 1962] = bitmap[da + 1963] = bitmap[da + 560];
        case 70:
            bitmap[da + 1957] = bitmap[da + 1958] = bitmap[da + 1959] = bitmap[da + 559];
            bitmap[da + 1953] = bitmap[da + 1954] = bitmap[da + 1955] = bitmap[da + 1956] = bitmap[da + 558];
            bitmap[da + 1950] = bitmap[da + 1951] = bitmap[da + 1952] = bitmap[da + 557];
            bitmap[da + 1946] = bitmap[da + 1947] = bitmap[da + 1948] = bitmap[da + 1949] = bitmap[da + 556];
            bitmap[da + 1943] = bitmap[da + 1944] = bitmap[da + 1945] = bitmap[da + 555];
            bitmap[da + 1939] = bitmap[da + 1940] = bitmap[da + 1941] = bitmap[da + 1942] = bitmap[da + 554];
            bitmap[da + 1936] = bitmap[da + 1937] = bitmap[da + 1938] = bitmap[da + 553];
            bitmap[da + 1932] = bitmap[da + 1933] = bitmap[da + 1934] = bitmap[da + 1935] = bitmap[da + 552];
        case 69:
            bitmap[da + 1929] = bitmap[da + 1930] = bitmap[da + 1931] = bitmap[da + 551];
            bitmap[da + 1925] = bitmap[da + 1926] = bitmap[da + 1927] = bitmap[da + 1928] = bitmap[da + 550];
            bitmap[da + 1922] = bitmap[da + 1923] = bitmap[da + 1924] = bitmap[da + 549];
            bitmap[da + 1918] = bitmap[da + 1919] = bitmap[da + 1920] = bitmap[da + 1921] = bitmap[da + 548];
            bitmap[da + 1915] = bitmap[da + 1916] = bitmap[da + 1917] = bitmap[da + 547];
            bitmap[da + 1911] = bitmap[da + 1912] = bitmap[da + 1913] = bitmap[da + 1914] = bitmap[da + 546];
            bitmap[da + 1908] = bitmap[da + 1909] = bitmap[da + 1910] = bitmap[da + 545];
            bitmap[da + 1904] = bitmap[da + 1905] = bitmap[da + 1906] = bitmap[da + 1907] = bitmap[da + 544];
        case 68:
            bitmap[da + 1901] = bitmap[da + 1902] = bitmap[da + 1903] = bitmap[da + 543];
            bitmap[da + 1897] = bitmap[da + 1898] = bitmap[da + 1899] = bitmap[da + 1900] = bitmap[da + 542];
            bitmap[da + 1894] = bitmap[da + 1895] = bitmap[da + 1896] = bitmap[da + 541];
            bitmap[da + 1890] = bitmap[da + 1891] = bitmap[da + 1892] = bitmap[da + 1893] = bitmap[da + 540];
            bitmap[da + 1887] = bitmap[da + 1888] = bitmap[da + 1889] = bitmap[da + 539];
            bitmap[da + 1883] = bitmap[da + 1884] = bitmap[da + 1885] = bitmap[da + 1886] = bitmap[da + 538];
            bitmap[da + 1880] = bitmap[da + 1881] = bitmap[da + 1882] = bitmap[da + 537];
            bitmap[da + 1876] = bitmap[da + 1877] = bitmap[da + 1878] = bitmap[da + 1879] = bitmap[da + 536];
        case 67:
            bitmap[da + 1873] = bitmap[da + 1874] = bitmap[da + 1875] = bitmap[da + 535];
            bitmap[da + 1869] = bitmap[da + 1870] = bitmap[da + 1871] = bitmap[da + 1872] = bitmap[da + 534];
            bitmap[da + 1866] = bitmap[da + 1867] = bitmap[da + 1868] = bitmap[da + 533];
            bitmap[da + 1862] = bitmap[da + 1863] = bitmap[da + 1864] = bitmap[da + 1865] = bitmap[da + 532];
            bitmap[da + 1859] = bitmap[da + 1860] = bitmap[da + 1861] = bitmap[da + 531];
            bitmap[da + 1855] = bitmap[da + 1856] = bitmap[da + 1857] = bitmap[da + 1858] = bitmap[da + 530];
            bitmap[da + 1852] = bitmap[da + 1853] = bitmap[da + 1854] = bitmap[da + 529];
            bitmap[da + 1848] = bitmap[da + 1849] = bitmap[da + 1850] = bitmap[da + 1851] = bitmap[da + 528];
        case 66:
            bitmap[da + 1845] = bitmap[da + 1846] = bitmap[da + 1847] = bitmap[da + 527];
            bitmap[da + 1841] = bitmap[da + 1842] = bitmap[da + 1843] = bitmap[da + 1844] = bitmap[da + 526];
            bitmap[da + 1838] = bitmap[da + 1839] = bitmap[da + 1840] = bitmap[da + 525];
            bitmap[da + 1834] = bitmap[da + 1835] = bitmap[da + 1836] = bitmap[da + 1837] = bitmap[da + 524];
            bitmap[da + 1831] = bitmap[da + 1832] = bitmap[da + 1833] = bitmap[da + 523];
            bitmap[da + 1827] = bitmap[da + 1828] = bitmap[da + 1829] = bitmap[da + 1830] = bitmap[da + 522];
            bitmap[da + 1824] = bitmap[da + 1825] = bitmap[da + 1826] = bitmap[da + 521];
            bitmap[da + 1820] = bitmap[da + 1821] = bitmap[da + 1822] = bitmap[da + 1823] = bitmap[da + 520];
        case 65:
            bitmap[da + 1817] = bitmap[da + 1818] = bitmap[da + 1819] = bitmap[da + 519];
            bitmap[da + 1813] = bitmap[da + 1814] = bitmap[da + 1815] = bitmap[da + 1816] = bitmap[da + 518];
            bitmap[da + 1810] = bitmap[da + 1811] = bitmap[da + 1812] = bitmap[da + 517];
            bitmap[da + 1806] = bitmap[da + 1807] = bitmap[da + 1808] = bitmap[da + 1809] = bitmap[da + 516];
            bitmap[da + 1803] = bitmap[da + 1804] = bitmap[da + 1805] = bitmap[da + 515];
            bitmap[da + 1799] = bitmap[da + 1800] = bitmap[da + 1801] = bitmap[da + 1802] = bitmap[da + 514];
            bitmap[da + 1796] = bitmap[da + 1797] = bitmap[da + 1798] = bitmap[da + 513];
            bitmap[da + 1792] = bitmap[da + 1793] = bitmap[da + 1794] = bitmap[da + 1795] = bitmap[da + 512];
        case 64:
            bitmap[da + 1789] = bitmap[da + 1790] = bitmap[da + 1791] = bitmap[da + 511];
            bitmap[da + 1785] = bitmap[da + 1786] = bitmap[da + 1787] = bitmap[da + 1788] = bitmap[da + 510];
            bitmap[da + 1782] = bitmap[da + 1783] = bitmap[da + 1784] = bitmap[da + 509];
            bitmap[da + 1778] = bitmap[da + 1779] = bitmap[da + 1780] = bitmap[da + 1781] = bitmap[da + 508];
            bitmap[da + 1775] = bitmap[da + 1776] = bitmap[da + 1777] = bitmap[da + 507];
            bitmap[da + 1771] = bitmap[da + 1772] = bitmap[da + 1773] = bitmap[da + 1774] = bitmap[da + 506];
            bitmap[da + 1768] = bitmap[da + 1769] = bitmap[da + 1770] = bitmap[da + 505];
            bitmap[da + 1764] = bitmap[da + 1765] = bitmap[da + 1766] = bitmap[da + 1767] = bitmap[da + 504];
        case 63:
            bitmap[da + 1761] = bitmap[da + 1762] = bitmap[da + 1763] = bitmap[da + 503];
            bitmap[da + 1757] = bitmap[da + 1758] = bitmap[da + 1759] = bitmap[da + 1760] = bitmap[da + 502];
            bitmap[da + 1754] = bitmap[da + 1755] = bitmap[da + 1756] = bitmap[da + 501];
            bitmap[da + 1750] = bitmap[da + 1751] = bitmap[da + 1752] = bitmap[da + 1753] = bitmap[da + 500];
            bitmap[da + 1747] = bitmap[da + 1748] = bitmap[da + 1749] = bitmap[da + 499];
            bitmap[da + 1743] = bitmap[da + 1744] = bitmap[da + 1745] = bitmap[da + 1746] = bitmap[da + 498];
            bitmap[da + 1740] = bitmap[da + 1741] = bitmap[da + 1742] = bitmap[da + 497];
            bitmap[da + 1736] = bitmap[da + 1737] = bitmap[da + 1738] = bitmap[da + 1739] = bitmap[da + 496];
        case 62:
            bitmap[da + 1733] = bitmap[da + 1734] = bitmap[da + 1735] = bitmap[da + 495];
            bitmap[da + 1729] = bitmap[da + 1730] = bitmap[da + 1731] = bitmap[da + 1732] = bitmap[da + 494];
            bitmap[da + 1726] = bitmap[da + 1727] = bitmap[da + 1728] = bitmap[da + 493];
            bitmap[da + 1722] = bitmap[da + 1723] = bitmap[da + 1724] = bitmap[da + 1725] = bitmap[da + 492];
            bitmap[da + 1719] = bitmap[da + 1720] = bitmap[da + 1721] = bitmap[da + 491];
            bitmap[da + 1715] = bitmap[da + 1716] = bitmap[da + 1717] = bitmap[da + 1718] = bitmap[da + 490];
            bitmap[da + 1712] = bitmap[da + 1713] = bitmap[da + 1714] = bitmap[da + 489];
            bitmap[da + 1708] = bitmap[da + 1709] = bitmap[da + 1710] = bitmap[da + 1711] = bitmap[da + 488];
        case 61:
            bitmap[da + 1705] = bitmap[da + 1706] = bitmap[da + 1707] = bitmap[da + 487];
            bitmap[da + 1701] = bitmap[da + 1702] = bitmap[da + 1703] = bitmap[da + 1704] = bitmap[da + 486];
            bitmap[da + 1698] = bitmap[da + 1699] = bitmap[da + 1700] = bitmap[da + 485];
            bitmap[da + 1694] = bitmap[da + 1695] = bitmap[da + 1696] = bitmap[da + 1697] = bitmap[da + 484];
            bitmap[da + 1691] = bitmap[da + 1692] = bitmap[da + 1693] = bitmap[da + 483];
            bitmap[da + 1687] = bitmap[da + 1688] = bitmap[da + 1689] = bitmap[da + 1690] = bitmap[da + 482];
            bitmap[da + 1684] = bitmap[da + 1685] = bitmap[da + 1686] = bitmap[da + 481];
            bitmap[da + 1680] = bitmap[da + 1681] = bitmap[da + 1682] = bitmap[da + 1683] = bitmap[da + 480];
        case 60:
            bitmap[da + 1677] = bitmap[da + 1678] = bitmap[da + 1679] = bitmap[da + 479];
            bitmap[da + 1673] = bitmap[da + 1674] = bitmap[da + 1675] = bitmap[da + 1676] = bitmap[da + 478];
            bitmap[da + 1670] = bitmap[da + 1671] = bitmap[da + 1672] = bitmap[da + 477];
            bitmap[da + 1666] = bitmap[da + 1667] = bitmap[da + 1668] = bitmap[da + 1669] = bitmap[da + 476];
            bitmap[da + 1663] = bitmap[da + 1664] = bitmap[da + 1665] = bitmap[da + 475];
            bitmap[da + 1659] = bitmap[da + 1660] = bitmap[da + 1661] = bitmap[da + 1662] = bitmap[da + 474];
            bitmap[da + 1656] = bitmap[da + 1657] = bitmap[da + 1658] = bitmap[da + 473];
            bitmap[da + 1652] = bitmap[da + 1653] = bitmap[da + 1654] = bitmap[da + 1655] = bitmap[da + 472];
        case 59:
            bitmap[da + 1649] = bitmap[da + 1650] = bitmap[da + 1651] = bitmap[da + 471];
            bitmap[da + 1645] = bitmap[da + 1646] = bitmap[da + 1647] = bitmap[da + 1648] = bitmap[da + 470];
            bitmap[da + 1642] = bitmap[da + 1643] = bitmap[da + 1644] = bitmap[da + 469];
            bitmap[da + 1638] = bitmap[da + 1639] = bitmap[da + 1640] = bitmap[da + 1641] = bitmap[da + 468];
            bitmap[da + 1635] = bitmap[da + 1636] = bitmap[da + 1637] = bitmap[da + 467];
            bitmap[da + 1631] = bitmap[da + 1632] = bitmap[da + 1633] = bitmap[da + 1634] = bitmap[da + 466];
            bitmap[da + 1628] = bitmap[da + 1629] = bitmap[da + 1630] = bitmap[da + 465];
            bitmap[da + 1624] = bitmap[da + 1625] = bitmap[da + 1626] = bitmap[da + 1627] = bitmap[da + 464];
        case 58:
            bitmap[da + 1621] = bitmap[da + 1622] = bitmap[da + 1623] = bitmap[da + 463];
            bitmap[da + 1617] = bitmap[da + 1618] = bitmap[da + 1619] = bitmap[da + 1620] = bitmap[da + 462];
            bitmap[da + 1614] = bitmap[da + 1615] = bitmap[da + 1616] = bitmap[da + 461];
            bitmap[da + 1610] = bitmap[da + 1611] = bitmap[da + 1612] = bitmap[da + 1613] = bitmap[da + 460];
            bitmap[da + 1607] = bitmap[da + 1608] = bitmap[da + 1609] = bitmap[da + 459];
            bitmap[da + 1603] = bitmap[da + 1604] = bitmap[da + 1605] = bitmap[da + 1606] = bitmap[da + 458];
            bitmap[da + 1600] = bitmap[da + 1601] = bitmap[da + 1602] = bitmap[da + 457];
            bitmap[da + 1596] = bitmap[da + 1597] = bitmap[da + 1598] = bitmap[da + 1599] = bitmap[da + 456];
        case 57:
            bitmap[da + 1593] = bitmap[da + 1594] = bitmap[da + 1595] = bitmap[da + 455];
            bitmap[da + 1589] = bitmap[da + 1590] = bitmap[da + 1591] = bitmap[da + 1592] = bitmap[da + 454];
            bitmap[da + 1586] = bitmap[da + 1587] = bitmap[da + 1588] = bitmap[da + 453];
            bitmap[da + 1582] = bitmap[da + 1583] = bitmap[da + 1584] = bitmap[da + 1585] = bitmap[da + 452];
            bitmap[da + 1579] = bitmap[da + 1580] = bitmap[da + 1581] = bitmap[da + 451];
            bitmap[da + 1575] = bitmap[da + 1576] = bitmap[da + 1577] = bitmap[da + 1578] = bitmap[da + 450];
            bitmap[da + 1572] = bitmap[da + 1573] = bitmap[da + 1574] = bitmap[da + 449];
            bitmap[da + 1568] = bitmap[da + 1569] = bitmap[da + 1570] = bitmap[da + 1571] = bitmap[da + 448];
        case 56:
            bitmap[da + 1565] = bitmap[da + 1566] = bitmap[da + 1567] = bitmap[da + 447];
            bitmap[da + 1561] = bitmap[da + 1562] = bitmap[da + 1563] = bitmap[da + 1564] = bitmap[da + 446];
            bitmap[da + 1558] = bitmap[da + 1559] = bitmap[da + 1560] = bitmap[da + 445];
            bitmap[da + 1554] = bitmap[da + 1555] = bitmap[da + 1556] = bitmap[da + 1557] = bitmap[da + 444];
            bitmap[da + 1551] = bitmap[da + 1552] = bitmap[da + 1553] = bitmap[da + 443];
            bitmap[da + 1547] = bitmap[da + 1548] = bitmap[da + 1549] = bitmap[da + 1550] = bitmap[da + 442];
            bitmap[da + 1544] = bitmap[da + 1545] = bitmap[da + 1546] = bitmap[da + 441];
            bitmap[da + 1540] = bitmap[da + 1541] = bitmap[da + 1542] = bitmap[da + 1543] = bitmap[da + 440];
        case 55:
            bitmap[da + 1537] = bitmap[da + 1538] = bitmap[da + 1539] = bitmap[da + 439];
            bitmap[da + 1533] = bitmap[da + 1534] = bitmap[da + 1535] = bitmap[da + 1536] = bitmap[da + 438];
            bitmap[da + 1530] = bitmap[da + 1531] = bitmap[da + 1532] = bitmap[da + 437];
            bitmap[da + 1526] = bitmap[da + 1527] = bitmap[da + 1528] = bitmap[da + 1529] = bitmap[da + 436];
            bitmap[da + 1523] = bitmap[da + 1524] = bitmap[da + 1525] = bitmap[da + 435];
            bitmap[da + 1519] = bitmap[da + 1520] = bitmap[da + 1521] = bitmap[da + 1522] = bitmap[da + 434];
            bitmap[da + 1516] = bitmap[da + 1517] = bitmap[da + 1518] = bitmap[da + 433];
            bitmap[da + 1512] = bitmap[da + 1513] = bitmap[da + 1514] = bitmap[da + 1515] = bitmap[da + 432];
        case 54:
            bitmap[da + 1509] = bitmap[da + 1510] = bitmap[da + 1511] = bitmap[da + 431];
            bitmap[da + 1505] = bitmap[da + 1506] = bitmap[da + 1507] = bitmap[da + 1508] = bitmap[da + 430];
            bitmap[da + 1502] = bitmap[da + 1503] = bitmap[da + 1504] = bitmap[da + 429];
            bitmap[da + 1498] = bitmap[da + 1499] = bitmap[da + 1500] = bitmap[da + 1501] = bitmap[da + 428];
            bitmap[da + 1495] = bitmap[da + 1496] = bitmap[da + 1497] = bitmap[da + 427];
            bitmap[da + 1491] = bitmap[da + 1492] = bitmap[da + 1493] = bitmap[da + 1494] = bitmap[da + 426];
            bitmap[da + 1488] = bitmap[da + 1489] = bitmap[da + 1490] = bitmap[da + 425];
            bitmap[da + 1484] = bitmap[da + 1485] = bitmap[da + 1486] = bitmap[da + 1487] = bitmap[da + 424];
        case 53:
            bitmap[da + 1481] = bitmap[da + 1482] = bitmap[da + 1483] = bitmap[da + 423];
            bitmap[da + 1477] = bitmap[da + 1478] = bitmap[da + 1479] = bitmap[da + 1480] = bitmap[da + 422];
            bitmap[da + 1474] = bitmap[da + 1475] = bitmap[da + 1476] = bitmap[da + 421];
            bitmap[da + 1470] = bitmap[da + 1471] = bitmap[da + 1472] = bitmap[da + 1473] = bitmap[da + 420];
            bitmap[da + 1467] = bitmap[da + 1468] = bitmap[da + 1469] = bitmap[da + 419];
            bitmap[da + 1463] = bitmap[da + 1464] = bitmap[da + 1465] = bitmap[da + 1466] = bitmap[da + 418];
            bitmap[da + 1460] = bitmap[da + 1461] = bitmap[da + 1462] = bitmap[da + 417];
            bitmap[da + 1456] = bitmap[da + 1457] = bitmap[da + 1458] = bitmap[da + 1459] = bitmap[da + 416];
        case 52:
            bitmap[da + 1453] = bitmap[da + 1454] = bitmap[da + 1455] = bitmap[da + 415];
            bitmap[da + 1449] = bitmap[da + 1450] = bitmap[da + 1451] = bitmap[da + 1452] = bitmap[da + 414];
            bitmap[da + 1446] = bitmap[da + 1447] = bitmap[da + 1448] = bitmap[da + 413];
            bitmap[da + 1442] = bitmap[da + 1443] = bitmap[da + 1444] = bitmap[da + 1445] = bitmap[da + 412];
            bitmap[da + 1439] = bitmap[da + 1440] = bitmap[da + 1441] = bitmap[da + 411];
            bitmap[da + 1435] = bitmap[da + 1436] = bitmap[da + 1437] = bitmap[da + 1438] = bitmap[da + 410];
            bitmap[da + 1432] = bitmap[da + 1433] = bitmap[da + 1434] = bitmap[da + 409];
            bitmap[da + 1428] = bitmap[da + 1429] = bitmap[da + 1430] = bitmap[da + 1431] = bitmap[da + 408];
        case 51:
            bitmap[da + 1425] = bitmap[da + 1426] = bitmap[da + 1427] = bitmap[da + 407];
            bitmap[da + 1421] = bitmap[da + 1422] = bitmap[da + 1423] = bitmap[da + 1424] = bitmap[da + 406];
            bitmap[da + 1418] = bitmap[da + 1419] = bitmap[da + 1420] = bitmap[da + 405];
            bitmap[da + 1414] = bitmap[da + 1415] = bitmap[da + 1416] = bitmap[da + 1417] = bitmap[da + 404];
            bitmap[da + 1411] = bitmap[da + 1412] = bitmap[da + 1413] = bitmap[da + 403];
            bitmap[da + 1407] = bitmap[da + 1408] = bitmap[da + 1409] = bitmap[da + 1410] = bitmap[da + 402];
            bitmap[da + 1404] = bitmap[da + 1405] = bitmap[da + 1406] = bitmap[da + 401];
            bitmap[da + 1400] = bitmap[da + 1401] = bitmap[da + 1402] = bitmap[da + 1403] = bitmap[da + 400];
        case 50:
            bitmap[da + 1397] = bitmap[da + 1398] = bitmap[da + 1399] = bitmap[da + 399];
            bitmap[da + 1393] = bitmap[da + 1394] = bitmap[da + 1395] = bitmap[da + 1396] = bitmap[da + 398];
            bitmap[da + 1390] = bitmap[da + 1391] = bitmap[da + 1392] = bitmap[da + 397];
            bitmap[da + 1386] = bitmap[da + 1387] = bitmap[da + 1388] = bitmap[da + 1389] = bitmap[da + 396];
            bitmap[da + 1383] = bitmap[da + 1384] = bitmap[da + 1385] = bitmap[da + 395];
            bitmap[da + 1379] = bitmap[da + 1380] = bitmap[da + 1381] = bitmap[da + 1382] = bitmap[da + 394];
            bitmap[da + 1376] = bitmap[da + 1377] = bitmap[da + 1378] = bitmap[da + 393];
            bitmap[da + 1372] = bitmap[da + 1373] = bitmap[da + 1374] = bitmap[da + 1375] = bitmap[da + 392];
        case 49:
            bitmap[da + 1369] = bitmap[da + 1370] = bitmap[da + 1371] = bitmap[da + 391];
            bitmap[da + 1365] = bitmap[da + 1366] = bitmap[da + 1367] = bitmap[da + 1368] = bitmap[da + 390];
            bitmap[da + 1362] = bitmap[da + 1363] = bitmap[da + 1364] = bitmap[da + 389];
            bitmap[da + 1358] = bitmap[da + 1359] = bitmap[da + 1360] = bitmap[da + 1361] = bitmap[da + 388];
            bitmap[da + 1355] = bitmap[da + 1356] = bitmap[da + 1357] = bitmap[da + 387];
            bitmap[da + 1351] = bitmap[da + 1352] = bitmap[da + 1353] = bitmap[da + 1354] = bitmap[da + 386];
            bitmap[da + 1348] = bitmap[da + 1349] = bitmap[da + 1350] = bitmap[da + 385];
            bitmap[da + 1344] = bitmap[da + 1345] = bitmap[da + 1346] = bitmap[da + 1347] = bitmap[da + 384];
        case 48:
            bitmap[da + 1341] = bitmap[da + 1342] = bitmap[da + 1343] = bitmap[da + 383];
            bitmap[da + 1337] = bitmap[da + 1338] = bitmap[da + 1339] = bitmap[da + 1340] = bitmap[da + 382];
            bitmap[da + 1334] = bitmap[da + 1335] = bitmap[da + 1336] = bitmap[da + 381];
            bitmap[da + 1330] = bitmap[da + 1331] = bitmap[da + 1332] = bitmap[da + 1333] = bitmap[da + 380];
            bitmap[da + 1327] = bitmap[da + 1328] = bitmap[da + 1329] = bitmap[da + 379];
            bitmap[da + 1323] = bitmap[da + 1324] = bitmap[da + 1325] = bitmap[da + 1326] = bitmap[da + 378];
            bitmap[da + 1320] = bitmap[da + 1321] = bitmap[da + 1322] = bitmap[da + 377];
            bitmap[da + 1316] = bitmap[da + 1317] = bitmap[da + 1318] = bitmap[da + 1319] = bitmap[da + 376];
        case 47:
            bitmap[da + 1313] = bitmap[da + 1314] = bitmap[da + 1315] = bitmap[da + 375];
            bitmap[da + 1309] = bitmap[da + 1310] = bitmap[da + 1311] = bitmap[da + 1312] = bitmap[da + 374];
            bitmap[da + 1306] = bitmap[da + 1307] = bitmap[da + 1308] = bitmap[da + 373];
            bitmap[da + 1302] = bitmap[da + 1303] = bitmap[da + 1304] = bitmap[da + 1305] = bitmap[da + 372];
            bitmap[da + 1299] = bitmap[da + 1300] = bitmap[da + 1301] = bitmap[da + 371];
            bitmap[da + 1295] = bitmap[da + 1296] = bitmap[da + 1297] = bitmap[da + 1298] = bitmap[da + 370];
            bitmap[da + 1292] = bitmap[da + 1293] = bitmap[da + 1294] = bitmap[da + 369];
            bitmap[da + 1288] = bitmap[da + 1289] = bitmap[da + 1290] = bitmap[da + 1291] = bitmap[da + 368];
        case 46:
            bitmap[da + 1285] = bitmap[da + 1286] = bitmap[da + 1287] = bitmap[da + 367];
            bitmap[da + 1281] = bitmap[da + 1282] = bitmap[da + 1283] = bitmap[da + 1284] = bitmap[da + 366];
            bitmap[da + 1278] = bitmap[da + 1279] = bitmap[da + 1280] = bitmap[da + 365];
            bitmap[da + 1274] = bitmap[da + 1275] = bitmap[da + 1276] = bitmap[da + 1277] = bitmap[da + 364];
            bitmap[da + 1271] = bitmap[da + 1272] = bitmap[da + 1273] = bitmap[da + 363];
            bitmap[da + 1267] = bitmap[da + 1268] = bitmap[da + 1269] = bitmap[da + 1270] = bitmap[da + 362];
            bitmap[da + 1264] = bitmap[da + 1265] = bitmap[da + 1266] = bitmap[da + 361];
            bitmap[da + 1260] = bitmap[da + 1261] = bitmap[da + 1262] = bitmap[da + 1263] = bitmap[da + 360];
        case 45:
            bitmap[da + 1257] = bitmap[da + 1258] = bitmap[da + 1259] = bitmap[da + 359];
            bitmap[da + 1253] = bitmap[da + 1254] = bitmap[da + 1255] = bitmap[da + 1256] = bitmap[da + 358];
            bitmap[da + 1250] = bitmap[da + 1251] = bitmap[da + 1252] = bitmap[da + 357];
            bitmap[da + 1246] = bitmap[da + 1247] = bitmap[da + 1248] = bitmap[da + 1249] = bitmap[da + 356];
            bitmap[da + 1243] = bitmap[da + 1244] = bitmap[da + 1245] = bitmap[da + 355];
            bitmap[da + 1239] = bitmap[da + 1240] = bitmap[da + 1241] = bitmap[da + 1242] = bitmap[da + 354];
            bitmap[da + 1236] = bitmap[da + 1237] = bitmap[da + 1238] = bitmap[da + 353];
            bitmap[da + 1232] = bitmap[da + 1233] = bitmap[da + 1234] = bitmap[da + 1235] = bitmap[da + 352];
        case 44:
            bitmap[da + 1229] = bitmap[da + 1230] = bitmap[da + 1231] = bitmap[da + 351];
            bitmap[da + 1225] = bitmap[da + 1226] = bitmap[da + 1227] = bitmap[da + 1228] = bitmap[da + 350];
            bitmap[da + 1222] = bitmap[da + 1223] = bitmap[da + 1224] = bitmap[da + 349];
            bitmap[da + 1218] = bitmap[da + 1219] = bitmap[da + 1220] = bitmap[da + 1221] = bitmap[da + 348];
            bitmap[da + 1215] = bitmap[da + 1216] = bitmap[da + 1217] = bitmap[da + 347];
            bitmap[da + 1211] = bitmap[da + 1212] = bitmap[da + 1213] = bitmap[da + 1214] = bitmap[da + 346];
            bitmap[da + 1208] = bitmap[da + 1209] = bitmap[da + 1210] = bitmap[da + 345];
            bitmap[da + 1204] = bitmap[da + 1205] = bitmap[da + 1206] = bitmap[da + 1207] = bitmap[da + 344];
        case 43:
            bitmap[da + 1201] = bitmap[da + 1202] = bitmap[da + 1203] = bitmap[da + 343];
            bitmap[da + 1197] = bitmap[da + 1198] = bitmap[da + 1199] = bitmap[da + 1200] = bitmap[da + 342];
            bitmap[da + 1194] = bitmap[da + 1195] = bitmap[da + 1196] = bitmap[da + 341];
            bitmap[da + 1190] = bitmap[da + 1191] = bitmap[da + 1192] = bitmap[da + 1193] = bitmap[da + 340];
            bitmap[da + 1187] = bitmap[da + 1188] = bitmap[da + 1189] = bitmap[da + 339];
            bitmap[da + 1183] = bitmap[da + 1184] = bitmap[da + 1185] = bitmap[da + 1186] = bitmap[da + 338];
            bitmap[da + 1180] = bitmap[da + 1181] = bitmap[da + 1182] = bitmap[da + 337];
            bitmap[da + 1176] = bitmap[da + 1177] = bitmap[da + 1178] = bitmap[da + 1179] = bitmap[da + 336];
        case 42:
            bitmap[da + 1173] = bitmap[da + 1174] = bitmap[da + 1175] = bitmap[da + 335];
            bitmap[da + 1169] = bitmap[da + 1170] = bitmap[da + 1171] = bitmap[da + 1172] = bitmap[da + 334];
            bitmap[da + 1166] = bitmap[da + 1167] = bitmap[da + 1168] = bitmap[da + 333];
            bitmap[da + 1162] = bitmap[da + 1163] = bitmap[da + 1164] = bitmap[da + 1165] = bitmap[da + 332];
            bitmap[da + 1159] = bitmap[da + 1160] = bitmap[da + 1161] = bitmap[da + 331];
            bitmap[da + 1155] = bitmap[da + 1156] = bitmap[da + 1157] = bitmap[da + 1158] = bitmap[da + 330];
            bitmap[da + 1152] = bitmap[da + 1153] = bitmap[da + 1154] = bitmap[da + 329];
            bitmap[da + 1148] = bitmap[da + 1149] = bitmap[da + 1150] = bitmap[da + 1151] = bitmap[da + 328];
        case 41:
            bitmap[da + 1145] = bitmap[da + 1146] = bitmap[da + 1147] = bitmap[da + 327];
            bitmap[da + 1141] = bitmap[da + 1142] = bitmap[da + 1143] = bitmap[da + 1144] = bitmap[da + 326];
            bitmap[da + 1138] = bitmap[da + 1139] = bitmap[da + 1140] = bitmap[da + 325];
            bitmap[da + 1134] = bitmap[da + 1135] = bitmap[da + 1136] = bitmap[da + 1137] = bitmap[da + 324];
            bitmap[da + 1131] = bitmap[da + 1132] = bitmap[da + 1133] = bitmap[da + 323];
            bitmap[da + 1127] = bitmap[da + 1128] = bitmap[da + 1129] = bitmap[da + 1130] = bitmap[da + 322];
            bitmap[da + 1124] = bitmap[da + 1125] = bitmap[da + 1126] = bitmap[da + 321];
            bitmap[da + 1120] = bitmap[da + 1121] = bitmap[da + 1122] = bitmap[da + 1123] = bitmap[da + 320];
        case 40:
            bitmap[da + 1117] = bitmap[da + 1118] = bitmap[da + 1119] = bitmap[da + 319];
            bitmap[da + 1113] = bitmap[da + 1114] = bitmap[da + 1115] = bitmap[da + 1116] = bitmap[da + 318];
            bitmap[da + 1110] = bitmap[da + 1111] = bitmap[da + 1112] = bitmap[da + 317];
            bitmap[da + 1106] = bitmap[da + 1107] = bitmap[da + 1108] = bitmap[da + 1109] = bitmap[da + 316];
            bitmap[da + 1103] = bitmap[da + 1104] = bitmap[da + 1105] = bitmap[da + 315];
            bitmap[da + 1099] = bitmap[da + 1100] = bitmap[da + 1101] = bitmap[da + 1102] = bitmap[da + 314];
            bitmap[da + 1096] = bitmap[da + 1097] = bitmap[da + 1098] = bitmap[da + 313];
            bitmap[da + 1092] = bitmap[da + 1093] = bitmap[da + 1094] = bitmap[da + 1095] = bitmap[da + 312];
        case 39:
            bitmap[da + 1089] = bitmap[da + 1090] = bitmap[da + 1091] = bitmap[da + 311];
            bitmap[da + 1085] = bitmap[da + 1086] = bitmap[da + 1087] = bitmap[da + 1088] = bitmap[da + 310];
            bitmap[da + 1082] = bitmap[da + 1083] = bitmap[da + 1084] = bitmap[da + 309];
            bitmap[da + 1078] = bitmap[da + 1079] = bitmap[da + 1080] = bitmap[da + 1081] = bitmap[da + 308];
            bitmap[da + 1075] = bitmap[da + 1076] = bitmap[da + 1077] = bitmap[da + 307];
            bitmap[da + 1071] = bitmap[da + 1072] = bitmap[da + 1073] = bitmap[da + 1074] = bitmap[da + 306];
            bitmap[da + 1068] = bitmap[da + 1069] = bitmap[da + 1070] = bitmap[da + 305];
            bitmap[da + 1064] = bitmap[da + 1065] = bitmap[da + 1066] = bitmap[da + 1067] = bitmap[da + 304];
        case 38:
            bitmap[da + 1061] = bitmap[da + 1062] = bitmap[da + 1063] = bitmap[da + 303];
            bitmap[da + 1057] = bitmap[da + 1058] = bitmap[da + 1059] = bitmap[da + 1060] = bitmap[da + 302];
            bitmap[da + 1054] = bitmap[da + 1055] = bitmap[da + 1056] = bitmap[da + 301];
            bitmap[da + 1050] = bitmap[da + 1051] = bitmap[da + 1052] = bitmap[da + 1053] = bitmap[da + 300];
            bitmap[da + 1047] = bitmap[da + 1048] = bitmap[da + 1049] = bitmap[da + 299];
            bitmap[da + 1043] = bitmap[da + 1044] = bitmap[da + 1045] = bitmap[da + 1046] = bitmap[da + 298];
            bitmap[da + 1040] = bitmap[da + 1041] = bitmap[da + 1042] = bitmap[da + 297];
            bitmap[da + 1036] = bitmap[da + 1037] = bitmap[da + 1038] = bitmap[da + 1039] = bitmap[da + 296];
        case 37:
            bitmap[da + 1033] = bitmap[da + 1034] = bitmap[da + 1035] = bitmap[da + 295];
            bitmap[da + 1029] = bitmap[da + 1030] = bitmap[da + 1031] = bitmap[da + 1032] = bitmap[da + 294];
            bitmap[da + 1026] = bitmap[da + 1027] = bitmap[da + 1028] = bitmap[da + 293];
            bitmap[da + 1022] = bitmap[da + 1023] = bitmap[da + 1024] = bitmap[da + 1025] = bitmap[da + 292];
            bitmap[da + 1019] = bitmap[da + 1020] = bitmap[da + 1021] = bitmap[da + 291];
            bitmap[da + 1015] = bitmap[da + 1016] = bitmap[da + 1017] = bitmap[da + 1018] = bitmap[da + 290];
            bitmap[da + 1012] = bitmap[da + 1013] = bitmap[da + 1014] = bitmap[da + 289];
            bitmap[da + 1008] = bitmap[da + 1009] = bitmap[da + 1010] = bitmap[da + 1011] = bitmap[da + 288];
        case 36:
            bitmap[da + 1005] = bitmap[da + 1006] = bitmap[da + 1007] = bitmap[da + 287];
            bitmap[da + 1001] = bitmap[da + 1002] = bitmap[da + 1003] = bitmap[da + 1004] = bitmap[da + 286];
            bitmap[da + 998] = bitmap[da + 999] = bitmap[da + 1000] = bitmap[da + 285];
            bitmap[da + 994] = bitmap[da + 995] = bitmap[da + 996] = bitmap[da + 997] = bitmap[da + 284];
            bitmap[da + 991] = bitmap[da + 992] = bitmap[da + 993] = bitmap[da + 283];
            bitmap[da + 987] = bitmap[da + 988] = bitmap[da + 989] = bitmap[da + 990] = bitmap[da + 282];
            bitmap[da + 984] = bitmap[da + 985] = bitmap[da + 986] = bitmap[da + 281];
            bitmap[da + 980] = bitmap[da + 981] = bitmap[da + 982] = bitmap[da + 983] = bitmap[da + 280];
        case 35:
            bitmap[da + 977] = bitmap[da + 978] = bitmap[da + 979] = bitmap[da + 279];
            bitmap[da + 973] = bitmap[da + 974] = bitmap[da + 975] = bitmap[da + 976] = bitmap[da + 278];
            bitmap[da + 970] = bitmap[da + 971] = bitmap[da + 972] = bitmap[da + 277];
            bitmap[da + 966] = bitmap[da + 967] = bitmap[da + 968] = bitmap[da + 969] = bitmap[da + 276];
            bitmap[da + 963] = bitmap[da + 964] = bitmap[da + 965] = bitmap[da + 275];
            bitmap[da + 959] = bitmap[da + 960] = bitmap[da + 961] = bitmap[da + 962] = bitmap[da + 274];
            bitmap[da + 956] = bitmap[da + 957] = bitmap[da + 958] = bitmap[da + 273];
            bitmap[da + 952] = bitmap[da + 953] = bitmap[da + 954] = bitmap[da + 955] = bitmap[da + 272];
        case 34:
            bitmap[da + 949] = bitmap[da + 950] = bitmap[da + 951] = bitmap[da + 271];
            bitmap[da + 945] = bitmap[da + 946] = bitmap[da + 947] = bitmap[da + 948] = bitmap[da + 270];
            bitmap[da + 942] = bitmap[da + 943] = bitmap[da + 944] = bitmap[da + 269];
            bitmap[da + 938] = bitmap[da + 939] = bitmap[da + 940] = bitmap[da + 941] = bitmap[da + 268];
            bitmap[da + 935] = bitmap[da + 936] = bitmap[da + 937] = bitmap[da + 267];
            bitmap[da + 931] = bitmap[da + 932] = bitmap[da + 933] = bitmap[da + 934] = bitmap[da + 266];
            bitmap[da + 928] = bitmap[da + 929] = bitmap[da + 930] = bitmap[da + 265];
            bitmap[da + 924] = bitmap[da + 925] = bitmap[da + 926] = bitmap[da + 927] = bitmap[da + 264];
        case 33:
            bitmap[da + 921] = bitmap[da + 922] = bitmap[da + 923] = bitmap[da + 263];
            bitmap[da + 917] = bitmap[da + 918] = bitmap[da + 919] = bitmap[da + 920] = bitmap[da + 262];
            bitmap[da + 914] = bitmap[da + 915] = bitmap[da + 916] = bitmap[da + 261];
            bitmap[da + 910] = bitmap[da + 911] = bitmap[da + 912] = bitmap[da + 913] = bitmap[da + 260];
            bitmap[da + 907] = bitmap[da + 908] = bitmap[da + 909] = bitmap[da + 259];
            bitmap[da + 903] = bitmap[da + 904] = bitmap[da + 905] = bitmap[da + 906] = bitmap[da + 258];
            bitmap[da + 900] = bitmap[da + 901] = bitmap[da + 902] = bitmap[da + 257];
            bitmap[da + 896] = bitmap[da + 897] = bitmap[da + 898] = bitmap[da + 899] = bitmap[da + 256];
        case 32:
            bitmap[da + 893] = bitmap[da + 894] = bitmap[da + 895] = bitmap[da + 255];
            bitmap[da + 889] = bitmap[da + 890] = bitmap[da + 891] = bitmap[da + 892] = bitmap[da + 254];
            bitmap[da + 886] = bitmap[da + 887] = bitmap[da + 888] = bitmap[da + 253];
            bitmap[da + 882] = bitmap[da + 883] = bitmap[da + 884] = bitmap[da + 885] = bitmap[da + 252];
            bitmap[da + 879] = bitmap[da + 880] = bitmap[da + 881] = bitmap[da + 251];
            bitmap[da + 875] = bitmap[da + 876] = bitmap[da + 877] = bitmap[da + 878] = bitmap[da + 250];
            bitmap[da + 872] = bitmap[da + 873] = bitmap[da + 874] = bitmap[da + 249];
            bitmap[da + 868] = bitmap[da + 869] = bitmap[da + 870] = bitmap[da + 871] = bitmap[da + 248];
        case 31:
            bitmap[da + 865] = bitmap[da + 866] = bitmap[da + 867] = bitmap[da + 247];
            bitmap[da + 861] = bitmap[da + 862] = bitmap[da + 863] = bitmap[da + 864] = bitmap[da + 246];
            bitmap[da + 858] = bitmap[da + 859] = bitmap[da + 860] = bitmap[da + 245];
            bitmap[da + 854] = bitmap[da + 855] = bitmap[da + 856] = bitmap[da + 857] = bitmap[da + 244];
            bitmap[da + 851] = bitmap[da + 852] = bitmap[da + 853] = bitmap[da + 243];
            bitmap[da + 847] = bitmap[da + 848] = bitmap[da + 849] = bitmap[da + 850] = bitmap[da + 242];
            bitmap[da + 844] = bitmap[da + 845] = bitmap[da + 846] = bitmap[da + 241];
            bitmap[da + 840] = bitmap[da + 841] = bitmap[da + 842] = bitmap[da + 843] = bitmap[da + 240];
        case 30:
            bitmap[da + 837] = bitmap[da + 838] = bitmap[da + 839] = bitmap[da + 239];
            bitmap[da + 833] = bitmap[da + 834] = bitmap[da + 835] = bitmap[da + 836] = bitmap[da + 238];
            bitmap[da + 830] = bitmap[da + 831] = bitmap[da + 832] = bitmap[da + 237];
            bitmap[da + 826] = bitmap[da + 827] = bitmap[da + 828] = bitmap[da + 829] = bitmap[da + 236];
            bitmap[da + 823] = bitmap[da + 824] = bitmap[da + 825] = bitmap[da + 235];
            bitmap[da + 819] = bitmap[da + 820] = bitmap[da + 821] = bitmap[da + 822] = bitmap[da + 234];
            bitmap[da + 816] = bitmap[da + 817] = bitmap[da + 818] = bitmap[da + 233];
            bitmap[da + 812] = bitmap[da + 813] = bitmap[da + 814] = bitmap[da + 815] = bitmap[da + 232];
        case 29:
            bitmap[da + 809] = bitmap[da + 810] = bitmap[da + 811] = bitmap[da + 231];
            bitmap[da + 805] = bitmap[da + 806] = bitmap[da + 807] = bitmap[da + 808] = bitmap[da + 230];
            bitmap[da + 802] = bitmap[da + 803] = bitmap[da + 804] = bitmap[da + 229];
            bitmap[da + 798] = bitmap[da + 799] = bitmap[da + 800] = bitmap[da + 801] = bitmap[da + 228];
            bitmap[da + 795] = bitmap[da + 796] = bitmap[da + 797] = bitmap[da + 227];
            bitmap[da + 791] = bitmap[da + 792] = bitmap[da + 793] = bitmap[da + 794] = bitmap[da + 226];
            bitmap[da + 788] = bitmap[da + 789] = bitmap[da + 790] = bitmap[da + 225];
            bitmap[da + 784] = bitmap[da + 785] = bitmap[da + 786] = bitmap[da + 787] = bitmap[da + 224];
        case 28:
            bitmap[da + 781] = bitmap[da + 782] = bitmap[da + 783] = bitmap[da + 223];
            bitmap[da + 777] = bitmap[da + 778] = bitmap[da + 779] = bitmap[da + 780] = bitmap[da + 222];
            bitmap[da + 774] = bitmap[da + 775] = bitmap[da + 776] = bitmap[da + 221];
            bitmap[da + 770] = bitmap[da + 771] = bitmap[da + 772] = bitmap[da + 773] = bitmap[da + 220];
            bitmap[da + 767] = bitmap[da + 768] = bitmap[da + 769] = bitmap[da + 219];
            bitmap[da + 763] = bitmap[da + 764] = bitmap[da + 765] = bitmap[da + 766] = bitmap[da + 218];
            bitmap[da + 760] = bitmap[da + 761] = bitmap[da + 762] = bitmap[da + 217];
            bitmap[da + 756] = bitmap[da + 757] = bitmap[da + 758] = bitmap[da + 759] = bitmap[da + 216];
        case 27:
            bitmap[da + 753] = bitmap[da + 754] = bitmap[da + 755] = bitmap[da + 215];
            bitmap[da + 749] = bitmap[da + 750] = bitmap[da + 751] = bitmap[da + 752] = bitmap[da + 214];
            bitmap[da + 746] = bitmap[da + 747] = bitmap[da + 748] = bitmap[da + 213];
            bitmap[da + 742] = bitmap[da + 743] = bitmap[da + 744] = bitmap[da + 745] = bitmap[da + 212];
            bitmap[da + 739] = bitmap[da + 740] = bitmap[da + 741] = bitmap[da + 211];
            bitmap[da + 735] = bitmap[da + 736] = bitmap[da + 737] = bitmap[da + 738] = bitmap[da + 210];
            bitmap[da + 732] = bitmap[da + 733] = bitmap[da + 734] = bitmap[da + 209];
            bitmap[da + 728] = bitmap[da + 729] = bitmap[da + 730] = bitmap[da + 731] = bitmap[da + 208];
        case 26:
            bitmap[da + 725] = bitmap[da + 726] = bitmap[da + 727] = bitmap[da + 207];
            bitmap[da + 721] = bitmap[da + 722] = bitmap[da + 723] = bitmap[da + 724] = bitmap[da + 206];
            bitmap[da + 718] = bitmap[da + 719] = bitmap[da + 720] = bitmap[da + 205];
            bitmap[da + 714] = bitmap[da + 715] = bitmap[da + 716] = bitmap[da + 717] = bitmap[da + 204];
            bitmap[da + 711] = bitmap[da + 712] = bitmap[da + 713] = bitmap[da + 203];
            bitmap[da + 707] = bitmap[da + 708] = bitmap[da + 709] = bitmap[da + 710] = bitmap[da + 202];
            bitmap[da + 704] = bitmap[da + 705] = bitmap[da + 706] = bitmap[da + 201];
            bitmap[da + 700] = bitmap[da + 701] = bitmap[da + 702] = bitmap[da + 703] = bitmap[da + 200];
        case 25:
            bitmap[da + 697] = bitmap[da + 698] = bitmap[da + 699] = bitmap[da + 199];
            bitmap[da + 693] = bitmap[da + 694] = bitmap[da + 695] = bitmap[da + 696] = bitmap[da + 198];
            bitmap[da + 690] = bitmap[da + 691] = bitmap[da + 692] = bitmap[da + 197];
            bitmap[da + 686] = bitmap[da + 687] = bitmap[da + 688] = bitmap[da + 689] = bitmap[da + 196];
            bitmap[da + 683] = bitmap[da + 684] = bitmap[da + 685] = bitmap[da + 195];
            bitmap[da + 679] = bitmap[da + 680] = bitmap[da + 681] = bitmap[da + 682] = bitmap[da + 194];
            bitmap[da + 676] = bitmap[da + 677] = bitmap[da + 678] = bitmap[da + 193];
            bitmap[da + 672] = bitmap[da + 673] = bitmap[da + 674] = bitmap[da + 675] = bitmap[da + 192];
        case 24:
            bitmap[da + 669] = bitmap[da + 670] = bitmap[da + 671] = bitmap[da + 191];
            bitmap[da + 665] = bitmap[da + 666] = bitmap[da + 667] = bitmap[da + 668] = bitmap[da + 190];
            bitmap[da + 662] = bitmap[da + 663] = bitmap[da + 664] = bitmap[da + 189];
            bitmap[da + 658] = bitmap[da + 659] = bitmap[da + 660] = bitmap[da + 661] = bitmap[da + 188];
            bitmap[da + 655] = bitmap[da + 656] = bitmap[da + 657] = bitmap[da + 187];
            bitmap[da + 651] = bitmap[da + 652] = bitmap[da + 653] = bitmap[da + 654] = bitmap[da + 186];
            bitmap[da + 648] = bitmap[da + 649] = bitmap[da + 650] = bitmap[da + 185];
            bitmap[da + 644] = bitmap[da + 645] = bitmap[da + 646] = bitmap[da + 647] = bitmap[da + 184];
        case 23:
            bitmap[da + 641] = bitmap[da + 642] = bitmap[da + 643] = bitmap[da + 183];
            bitmap[da + 637] = bitmap[da + 638] = bitmap[da + 639] = bitmap[da + 640] = bitmap[da + 182];
            bitmap[da + 634] = bitmap[da + 635] = bitmap[da + 636] = bitmap[da + 181];
            bitmap[da + 630] = bitmap[da + 631] = bitmap[da + 632] = bitmap[da + 633] = bitmap[da + 180];
            bitmap[da + 627] = bitmap[da + 628] = bitmap[da + 629] = bitmap[da + 179];
            bitmap[da + 623] = bitmap[da + 624] = bitmap[da + 625] = bitmap[da + 626] = bitmap[da + 178];
            bitmap[da + 620] = bitmap[da + 621] = bitmap[da + 622] = bitmap[da + 177];
            bitmap[da + 616] = bitmap[da + 617] = bitmap[da + 618] = bitmap[da + 619] = bitmap[da + 176];
        case 22:
            bitmap[da + 613] = bitmap[da + 614] = bitmap[da + 615] = bitmap[da + 175];
            bitmap[da + 609] = bitmap[da + 610] = bitmap[da + 611] = bitmap[da + 612] = bitmap[da + 174];
            bitmap[da + 606] = bitmap[da + 607] = bitmap[da + 608] = bitmap[da + 173];
            bitmap[da + 602] = bitmap[da + 603] = bitmap[da + 604] = bitmap[da + 605] = bitmap[da + 172];
            bitmap[da + 599] = bitmap[da + 600] = bitmap[da + 601] = bitmap[da + 171];
            bitmap[da + 595] = bitmap[da + 596] = bitmap[da + 597] = bitmap[da + 598] = bitmap[da + 170];
            bitmap[da + 592] = bitmap[da + 593] = bitmap[da + 594] = bitmap[da + 169];
            bitmap[da + 588] = bitmap[da + 589] = bitmap[da + 590] = bitmap[da + 591] = bitmap[da + 168];
        case 21:
            bitmap[da + 585] = bitmap[da + 586] = bitmap[da + 587] = bitmap[da + 167];
            bitmap[da + 581] = bitmap[da + 582] = bitmap[da + 583] = bitmap[da + 584] = bitmap[da + 166];
            bitmap[da + 578] = bitmap[da + 579] = bitmap[da + 580] = bitmap[da + 165];
            bitmap[da + 574] = bitmap[da + 575] = bitmap[da + 576] = bitmap[da + 577] = bitmap[da + 164];
            bitmap[da + 571] = bitmap[da + 572] = bitmap[da + 573] = bitmap[da + 163];
            bitmap[da + 567] = bitmap[da + 568] = bitmap[da + 569] = bitmap[da + 570] = bitmap[da + 162];
            bitmap[da + 564] = bitmap[da + 565] = bitmap[da + 566] = bitmap[da + 161];
            bitmap[da + 560] = bitmap[da + 561] = bitmap[da + 562] = bitmap[da + 563] = bitmap[da + 160];
        case 20:
            bitmap[da + 557] = bitmap[da + 558] = bitmap[da + 559] = bitmap[da + 159];
            bitmap[da + 553] = bitmap[da + 554] = bitmap[da + 555] = bitmap[da + 556] = bitmap[da + 158];
            bitmap[da + 550] = bitmap[da + 551] = bitmap[da + 552] = bitmap[da + 157];
            bitmap[da + 546] = bitmap[da + 547] = bitmap[da + 548] = bitmap[da + 549] = bitmap[da + 156];
            bitmap[da + 543] = bitmap[da + 544] = bitmap[da + 545] = bitmap[da + 155];
            bitmap[da + 539] = bitmap[da + 540] = bitmap[da + 541] = bitmap[da + 542] = bitmap[da + 154];
            bitmap[da + 536] = bitmap[da + 537] = bitmap[da + 538] = bitmap[da + 153];
            bitmap[da + 532] = bitmap[da + 533] = bitmap[da + 534] = bitmap[da + 535] = bitmap[da + 152];
        case 19:
            bitmap[da + 529] = bitmap[da + 530] = bitmap[da + 531] = bitmap[da + 151];
            bitmap[da + 525] = bitmap[da + 526] = bitmap[da + 527] = bitmap[da + 528] = bitmap[da + 150];
            bitmap[da + 522] = bitmap[da + 523] = bitmap[da + 524] = bitmap[da + 149];
            bitmap[da + 518] = bitmap[da + 519] = bitmap[da + 520] = bitmap[da + 521] = bitmap[da + 148];
            bitmap[da + 515] = bitmap[da + 516] = bitmap[da + 517] = bitmap[da + 147];
            bitmap[da + 511] = bitmap[da + 512] = bitmap[da + 513] = bitmap[da + 514] = bitmap[da + 146];
            bitmap[da + 508] = bitmap[da + 509] = bitmap[da + 510] = bitmap[da + 145];
            bitmap[da + 504] = bitmap[da + 505] = bitmap[da + 506] = bitmap[da + 507] = bitmap[da + 144];
        case 18:
            bitmap[da + 501] = bitmap[da + 502] = bitmap[da + 503] = bitmap[da + 143];
            bitmap[da + 497] = bitmap[da + 498] = bitmap[da + 499] = bitmap[da + 500] = bitmap[da + 142];
            bitmap[da + 494] = bitmap[da + 495] = bitmap[da + 496] = bitmap[da + 141];
            bitmap[da + 490] = bitmap[da + 491] = bitmap[da + 492] = bitmap[da + 493] = bitmap[da + 140];
            bitmap[da + 487] = bitmap[da + 488] = bitmap[da + 489] = bitmap[da + 139];
            bitmap[da + 483] = bitmap[da + 484] = bitmap[da + 485] = bitmap[da + 486] = bitmap[da + 138];
            bitmap[da + 480] = bitmap[da + 481] = bitmap[da + 482] = bitmap[da + 137];
            bitmap[da + 476] = bitmap[da + 477] = bitmap[da + 478] = bitmap[da + 479] = bitmap[da + 136];
        case 17:
            bitmap[da + 473] = bitmap[da + 474] = bitmap[da + 475] = bitmap[da + 135];
            bitmap[da + 469] = bitmap[da + 470] = bitmap[da + 471] = bitmap[da + 472] = bitmap[da + 134];
            bitmap[da + 466] = bitmap[da + 467] = bitmap[da + 468] = bitmap[da + 133];
            bitmap[da + 462] = bitmap[da + 463] = bitmap[da + 464] = bitmap[da + 465] = bitmap[da + 132];
            bitmap[da + 459] = bitmap[da + 460] = bitmap[da + 461] = bitmap[da + 131];
            bitmap[da + 455] = bitmap[da + 456] = bitmap[da + 457] = bitmap[da + 458] = bitmap[da + 130];
            bitmap[da + 452] = bitmap[da + 453] = bitmap[da + 454] = bitmap[da + 129];
            bitmap[da + 448] = bitmap[da + 449] = bitmap[da + 450] = bitmap[da + 451] = bitmap[da + 128];
        case 16:
            bitmap[da + 445] = bitmap[da + 446] = bitmap[da + 447] = bitmap[da + 127];
            bitmap[da + 441] = bitmap[da + 442] = bitmap[da + 443] = bitmap[da + 444] = bitmap[da + 126];
            bitmap[da + 438] = bitmap[da + 439] = bitmap[da + 440] = bitmap[da + 125];
            bitmap[da + 434] = bitmap[da + 435] = bitmap[da + 436] = bitmap[da + 437] = bitmap[da + 124];
            bitmap[da + 431] = bitmap[da + 432] = bitmap[da + 433] = bitmap[da + 123];
            bitmap[da + 427] = bitmap[da + 428] = bitmap[da + 429] = bitmap[da + 430] = bitmap[da + 122];
            bitmap[da + 424] = bitmap[da + 425] = bitmap[da + 426] = bitmap[da + 121];
            bitmap[da + 420] = bitmap[da + 421] = bitmap[da + 422] = bitmap[da + 423] = bitmap[da + 120];
        case 15:
            bitmap[da + 417] = bitmap[da + 418] = bitmap[da + 419] = bitmap[da + 119];
            bitmap[da + 413] = bitmap[da + 414] = bitmap[da + 415] = bitmap[da + 416] = bitmap[da + 118];
            bitmap[da + 410] = bitmap[da + 411] = bitmap[da + 412] = bitmap[da + 117];
            bitmap[da + 406] = bitmap[da + 407] = bitmap[da + 408] = bitmap[da + 409] = bitmap[da + 116];
            bitmap[da + 403] = bitmap[da + 404] = bitmap[da + 405] = bitmap[da + 115];
            bitmap[da + 399] = bitmap[da + 400] = bitmap[da + 401] = bitmap[da + 402] = bitmap[da + 114];
            bitmap[da + 396] = bitmap[da + 397] = bitmap[da + 398] = bitmap[da + 113];
            bitmap[da + 392] = bitmap[da + 393] = bitmap[da + 394] = bitmap[da + 395] = bitmap[da + 112];
        case 14:
            bitmap[da + 389] = bitmap[da + 390] = bitmap[da + 391] = bitmap[da + 111];
            bitmap[da + 385] = bitmap[da + 386] = bitmap[da + 387] = bitmap[da + 388] = bitmap[da + 110];
            bitmap[da + 382] = bitmap[da + 383] = bitmap[da + 384] = bitmap[da + 109];
            bitmap[da + 378] = bitmap[da + 379] = bitmap[da + 380] = bitmap[da + 381] = bitmap[da + 108];
            bitmap[da + 375] = bitmap[da + 376] = bitmap[da + 377] = bitmap[da + 107];
            bitmap[da + 371] = bitmap[da + 372] = bitmap[da + 373] = bitmap[da + 374] = bitmap[da + 106];
            bitmap[da + 368] = bitmap[da + 369] = bitmap[da + 370] = bitmap[da + 105];
            bitmap[da + 364] = bitmap[da + 365] = bitmap[da + 366] = bitmap[da + 367] = bitmap[da + 104];
        case 13:
            bitmap[da + 361] = bitmap[da + 362] = bitmap[da + 363] = bitmap[da + 103];
            bitmap[da + 357] = bitmap[da + 358] = bitmap[da + 359] = bitmap[da + 360] = bitmap[da + 102];
            bitmap[da + 354] = bitmap[da + 355] = bitmap[da + 356] = bitmap[da + 101];
            bitmap[da + 350] = bitmap[da + 351] = bitmap[da + 352] = bitmap[da + 353] = bitmap[da + 100];
            bitmap[da + 347] = bitmap[da + 348] = bitmap[da + 349] = bitmap[da + 99];
            bitmap[da + 343] = bitmap[da + 344] = bitmap[da + 345] = bitmap[da + 346] = bitmap[da + 98];
            bitmap[da + 340] = bitmap[da + 341] = bitmap[da + 342] = bitmap[da + 97];
            bitmap[da + 336] = bitmap[da + 337] = bitmap[da + 338] = bitmap[da + 339] = bitmap[da + 96];
        case 12:
            bitmap[da + 333] = bitmap[da + 334] = bitmap[da + 335] = bitmap[da + 95];
            bitmap[da + 329] = bitmap[da + 330] = bitmap[da + 331] = bitmap[da + 332] = bitmap[da + 94];
            bitmap[da + 326] = bitmap[da + 327] = bitmap[da + 328] = bitmap[da + 93];
            bitmap[da + 322] = bitmap[da + 323] = bitmap[da + 324] = bitmap[da + 325] = bitmap[da + 92];
            bitmap[da + 319] = bitmap[da + 320] = bitmap[da + 321] = bitmap[da + 91];
            bitmap[da + 315] = bitmap[da + 316] = bitmap[da + 317] = bitmap[da + 318] = bitmap[da + 90];
            bitmap[da + 312] = bitmap[da + 313] = bitmap[da + 314] = bitmap[da + 89];
            bitmap[da + 308] = bitmap[da + 309] = bitmap[da + 310] = bitmap[da + 311] = bitmap[da + 88];
        case 11:
            bitmap[da + 305] = bitmap[da + 306] = bitmap[da + 307] = bitmap[da + 87];
            bitmap[da + 301] = bitmap[da + 302] = bitmap[da + 303] = bitmap[da + 304] = bitmap[da + 86];
            bitmap[da + 298] = bitmap[da + 299] = bitmap[da + 300] = bitmap[da + 85];
            bitmap[da + 294] = bitmap[da + 295] = bitmap[da + 296] = bitmap[da + 297] = bitmap[da + 84];
            bitmap[da + 291] = bitmap[da + 292] = bitmap[da + 293] = bitmap[da + 83];
            bitmap[da + 287] = bitmap[da + 288] = bitmap[da + 289] = bitmap[da + 290] = bitmap[da + 82];
            bitmap[da + 284] = bitmap[da + 285] = bitmap[da + 286] = bitmap[da + 81];
            bitmap[da + 280] = bitmap[da + 281] = bitmap[da + 282] = bitmap[da + 283] = bitmap[da + 80];
        case 10:
            bitmap[da + 277] = bitmap[da + 278] = bitmap[da + 279] = bitmap[da + 79];
            bitmap[da + 273] = bitmap[da + 274] = bitmap[da + 275] = bitmap[da + 276] = bitmap[da + 78];
            bitmap[da + 270] = bitmap[da + 271] = bitmap[da + 272] = bitmap[da + 77];
            bitmap[da + 266] = bitmap[da + 267] = bitmap[da + 268] = bitmap[da + 269] = bitmap[da + 76];
            bitmap[da + 263] = bitmap[da + 264] = bitmap[da + 265] = bitmap[da + 75];
            bitmap[da + 259] = bitmap[da + 260] = bitmap[da + 261] = bitmap[da + 262] = bitmap[da + 74];
            bitmap[da + 256] = bitmap[da + 257] = bitmap[da + 258] = bitmap[da + 73];
            bitmap[da + 252] = bitmap[da + 253] = bitmap[da + 254] = bitmap[da + 255] = bitmap[da + 72];
        case 9:
            bitmap[da + 249] = bitmap[da + 250] = bitmap[da + 251] = bitmap[da + 71];
            bitmap[da + 245] = bitmap[da + 246] = bitmap[da + 247] = bitmap[da + 248] = bitmap[da + 70];
            bitmap[da + 242] = bitmap[da + 243] = bitmap[da + 244] = bitmap[da + 69];
            bitmap[da + 238] = bitmap[da + 239] = bitmap[da + 240] = bitmap[da + 241] = bitmap[da + 68];
            bitmap[da + 235] = bitmap[da + 236] = bitmap[da + 237] = bitmap[da + 67];
            bitmap[da + 231] = bitmap[da + 232] = bitmap[da + 233] = bitmap[da + 234] = bitmap[da + 66];
            bitmap[da + 228] = bitmap[da + 229] = bitmap[da + 230] = bitmap[da + 65];
            bitmap[da + 224] = bitmap[da + 225] = bitmap[da + 226] = bitmap[da + 227] = bitmap[da + 64];
        case 8:
            bitmap[da + 221] = bitmap[da + 222] = bitmap[da + 223] = bitmap[da + 63];
            bitmap[da + 217] = bitmap[da + 218] = bitmap[da + 219] = bitmap[da + 220] = bitmap[da + 62];
            bitmap[da + 214] = bitmap[da + 215] = bitmap[da + 216] = bitmap[da + 61];
            bitmap[da + 210] = bitmap[da + 211] = bitmap[da + 212] = bitmap[da + 213] = bitmap[da + 60];
            bitmap[da + 207] = bitmap[da + 208] = bitmap[da + 209] = bitmap[da + 59];
            bitmap[da + 203] = bitmap[da + 204] = bitmap[da + 205] = bitmap[da + 206] = bitmap[da + 58];
            bitmap[da + 200] = bitmap[da + 201] = bitmap[da + 202] = bitmap[da + 57];
            bitmap[da + 196] = bitmap[da + 197] = bitmap[da + 198] = bitmap[da + 199] = bitmap[da + 56];
        case 7:
            bitmap[da + 193] = bitmap[da + 194] = bitmap[da + 195] = bitmap[da + 55];
            bitmap[da + 189] = bitmap[da + 190] = bitmap[da + 191] = bitmap[da + 192] = bitmap[da + 54];
            bitmap[da + 186] = bitmap[da + 187] = bitmap[da + 188] = bitmap[da + 53];
            bitmap[da + 182] = bitmap[da + 183] = bitmap[da + 184] = bitmap[da + 185] = bitmap[da + 52];
            bitmap[da + 179] = bitmap[da + 180] = bitmap[da + 181] = bitmap[da + 51];
            bitmap[da + 175] = bitmap[da + 176] = bitmap[da + 177] = bitmap[da + 178] = bitmap[da + 50];
            bitmap[da + 172] = bitmap[da + 173] = bitmap[da + 174] = bitmap[da + 49];
            bitmap[da + 168] = bitmap[da + 169] = bitmap[da + 170] = bitmap[da + 171] = bitmap[da + 48];
        case 6:
            bitmap[da + 165] = bitmap[da + 166] = bitmap[da + 167] = bitmap[da + 47];
            bitmap[da + 161] = bitmap[da + 162] = bitmap[da + 163] = bitmap[da + 164] = bitmap[da + 46];
            bitmap[da + 158] = bitmap[da + 159] = bitmap[da + 160] = bitmap[da + 45];
            bitmap[da + 154] = bitmap[da + 155] = bitmap[da + 156] = bitmap[da + 157] = bitmap[da + 44];
            bitmap[da + 151] = bitmap[da + 152] = bitmap[da + 153] = bitmap[da + 43];
            bitmap[da + 147] = bitmap[da + 148] = bitmap[da + 149] = bitmap[da + 150] = bitmap[da + 42];
            bitmap[da + 144] = bitmap[da + 145] = bitmap[da + 146] = bitmap[da + 41];
            bitmap[da + 140] = bitmap[da + 141] = bitmap[da + 142] = bitmap[da + 143] = bitmap[da + 40];
        case 5:
            bitmap[da + 137] = bitmap[da + 138] = bitmap[da + 139] = bitmap[da + 39];
            bitmap[da + 133] = bitmap[da + 134] = bitmap[da + 135] = bitmap[da + 136] = bitmap[da + 38];
            bitmap[da + 130] = bitmap[da + 131] = bitmap[da + 132] = bitmap[da + 37];
            bitmap[da + 126] = bitmap[da + 127] = bitmap[da + 128] = bitmap[da + 129] = bitmap[da + 36];
            bitmap[da + 123] = bitmap[da + 124] = bitmap[da + 125] = bitmap[da + 35];
            bitmap[da + 119] = bitmap[da + 120] = bitmap[da + 121] = bitmap[da + 122] = bitmap[da + 34];
            bitmap[da + 116] = bitmap[da + 117] = bitmap[da + 118] = bitmap[da + 33];
            bitmap[da + 112] = bitmap[da + 113] = bitmap[da + 114] = bitmap[da + 115] = bitmap[da + 32];
        case 4:
            bitmap[da + 109] = bitmap[da + 110] = bitmap[da + 111] = bitmap[da + 31];
            bitmap[da + 105] = bitmap[da + 106] = bitmap[da + 107] = bitmap[da + 108] = bitmap[da + 30];
            bitmap[da + 102] = bitmap[da + 103] = bitmap[da + 104] = bitmap[da + 29];
            bitmap[da + 98] = bitmap[da + 99] = bitmap[da + 100] = bitmap[da + 101] = bitmap[da + 28];
            bitmap[da + 95] = bitmap[da + 96] = bitmap[da + 97] = bitmap[da + 27];
            bitmap[da + 91] = bitmap[da + 92] = bitmap[da + 93] = bitmap[da + 94] = bitmap[da + 26];
            bitmap[da + 88] = bitmap[da + 89] = bitmap[da + 90] = bitmap[da + 25];
            bitmap[da + 84] = bitmap[da + 85] = bitmap[da + 86] = bitmap[da + 87] = bitmap[da + 24];
        case 3:
            bitmap[da + 81] = bitmap[da + 82] = bitmap[da + 83] = bitmap[da + 23];
            bitmap[da + 77] = bitmap[da + 78] = bitmap[da + 79] = bitmap[da + 80] = bitmap[da + 22];
            bitmap[da + 74] = bitmap[da + 75] = bitmap[da + 76] = bitmap[da + 21];
            bitmap[da + 70] = bitmap[da + 71] = bitmap[da + 72] = bitmap[da + 73] = bitmap[da + 20];
            bitmap[da + 67] = bitmap[da + 68] = bitmap[da + 69] = bitmap[da + 19];
            bitmap[da + 63] = bitmap[da + 64] = bitmap[da + 65] = bitmap[da + 66] = bitmap[da + 18];
            bitmap[da + 60] = bitmap[da + 61] = bitmap[da + 62] = bitmap[da + 17];
            bitmap[da + 56] = bitmap[da + 57] = bitmap[da + 58] = bitmap[da + 59] = bitmap[da + 16];
        case 2:
            bitmap[da + 53] = bitmap[da + 54] = bitmap[da + 55] = bitmap[da + 15];
            bitmap[da + 49] = bitmap[da + 50] = bitmap[da + 51] = bitmap[da + 52] = bitmap[da + 14];
            bitmap[da + 46] = bitmap[da + 47] = bitmap[da + 48] = bitmap[da + 13];
            bitmap[da + 42] = bitmap[da + 43] = bitmap[da + 44] = bitmap[da + 45] = bitmap[da + 12];
            bitmap[da + 39] = bitmap[da + 40] = bitmap[da + 41] = bitmap[da + 11];
            bitmap[da + 35] = bitmap[da + 36] = bitmap[da + 37] = bitmap[da + 38] = bitmap[da + 10];
            bitmap[da + 32] = bitmap[da + 33] = bitmap[da + 34] = bitmap[da + 9];
            bitmap[da + 28] = bitmap[da + 29] = bitmap[da + 30] = bitmap[da + 31] = bitmap[da + 8];
        case 1:
            bitmap[da + 25] = bitmap[da + 26] = bitmap[da + 27] = bitmap[da + 7];
            bitmap[da + 21] = bitmap[da + 22] = bitmap[da + 23] = bitmap[da + 24] = bitmap[da + 6];
            bitmap[da + 18] = bitmap[da + 19] = bitmap[da + 20] = bitmap[da + 5];
            bitmap[da + 14] = bitmap[da + 15] = bitmap[da + 16] = bitmap[da + 17] = bitmap[da + 4];
            bitmap[da + 11] = bitmap[da + 12] = bitmap[da + 13] = bitmap[da + 3];
            bitmap[da + 7] = bitmap[da + 8] = bitmap[da + 9] = bitmap[da + 10] = bitmap[da + 2];
            bitmap[da + 4] = bitmap[da + 5] = bitmap[da + 6] = bitmap[da + 1];
            bitmap[da] = bitmap[da + 1] = bitmap[da + 2] = bitmap[da + 3] = bitmap[da];
        }
    }

    private void draw_sprite_buffer(int src) {
        int pat[] = sprite_screen.spat;
        if (sprite_existence) {
            switch (dw >> 3) {
            case 128:
                s_buf[1039] = 0;
                s_prw[1039] = 0;
                s_buf[1038] = 0;
                s_prw[1038] = 0;
                s_buf[1037] = 0;
                s_prw[1037] = 0;
                s_buf[1036] = 0;
                s_prw[1036] = 0;
                s_buf[1035] = 0;
                s_prw[1035] = 0;
                s_buf[1034] = 0;
                s_prw[1034] = 0;
                s_buf[1033] = 0;
                s_prw[1033] = 0;
                s_buf[1032] = 0;
                s_prw[1032] = 0;
            case 127:
                s_buf[1031] = 0;
                s_prw[1031] = 0;
                s_buf[1030] = 0;
                s_prw[1030] = 0;
                s_buf[1029] = 0;
                s_prw[1029] = 0;
                s_buf[1028] = 0;
                s_prw[1028] = 0;
                s_buf[1027] = 0;
                s_prw[1027] = 0;
                s_buf[1026] = 0;
                s_prw[1026] = 0;
                s_buf[1025] = 0;
                s_prw[1025] = 0;
                s_buf[1024] = 0;
                s_prw[1024] = 0;
            case 126:
                s_buf[1023] = 0;
                s_prw[1023] = 0;
                s_buf[1022] = 0;
                s_prw[1022] = 0;
                s_buf[1021] = 0;
                s_prw[1021] = 0;
                s_buf[1020] = 0;
                s_prw[1020] = 0;
                s_buf[1019] = 0;
                s_prw[1019] = 0;
                s_buf[1018] = 0;
                s_prw[1018] = 0;
                s_buf[1017] = 0;
                s_prw[1017] = 0;
                s_buf[1016] = 0;
                s_prw[1016] = 0;
            case 125:
                s_buf[1015] = 0;
                s_prw[1015] = 0;
                s_buf[1014] = 0;
                s_prw[1014] = 0;
                s_buf[1013] = 0;
                s_prw[1013] = 0;
                s_buf[1012] = 0;
                s_prw[1012] = 0;
                s_buf[1011] = 0;
                s_prw[1011] = 0;
                s_buf[1010] = 0;
                s_prw[1010] = 0;
                s_buf[1009] = 0;
                s_prw[1009] = 0;
                s_buf[1008] = 0;
                s_prw[1008] = 0;
            case 124:
                s_buf[1007] = 0;
                s_prw[1007] = 0;
                s_buf[1006] = 0;
                s_prw[1006] = 0;
                s_buf[1005] = 0;
                s_prw[1005] = 0;
                s_buf[1004] = 0;
                s_prw[1004] = 0;
                s_buf[1003] = 0;
                s_prw[1003] = 0;
                s_buf[1002] = 0;
                s_prw[1002] = 0;
                s_buf[1001] = 0;
                s_prw[1001] = 0;
                s_buf[1000] = 0;
                s_prw[1000] = 0;
            case 123:
                s_buf[999] = 0;
                s_prw[999] = 0;
                s_buf[998] = 0;
                s_prw[998] = 0;
                s_buf[997] = 0;
                s_prw[997] = 0;
                s_buf[996] = 0;
                s_prw[996] = 0;
                s_buf[995] = 0;
                s_prw[995] = 0;
                s_buf[994] = 0;
                s_prw[994] = 0;
                s_buf[993] = 0;
                s_prw[993] = 0;
                s_buf[992] = 0;
                s_prw[992] = 0;
            case 122:
                s_buf[991] = 0;
                s_prw[991] = 0;
                s_buf[990] = 0;
                s_prw[990] = 0;
                s_buf[989] = 0;
                s_prw[989] = 0;
                s_buf[988] = 0;
                s_prw[988] = 0;
                s_buf[987] = 0;
                s_prw[987] = 0;
                s_buf[986] = 0;
                s_prw[986] = 0;
                s_buf[985] = 0;
                s_prw[985] = 0;
                s_buf[984] = 0;
                s_prw[984] = 0;
            case 121:
                s_buf[983] = 0;
                s_prw[983] = 0;
                s_buf[982] = 0;
                s_prw[982] = 0;
                s_buf[981] = 0;
                s_prw[981] = 0;
                s_buf[980] = 0;
                s_prw[980] = 0;
                s_buf[979] = 0;
                s_prw[979] = 0;
                s_buf[978] = 0;
                s_prw[978] = 0;
                s_buf[977] = 0;
                s_prw[977] = 0;
                s_buf[976] = 0;
                s_prw[976] = 0;
            case 120:
                s_buf[975] = 0;
                s_prw[975] = 0;
                s_buf[974] = 0;
                s_prw[974] = 0;
                s_buf[973] = 0;
                s_prw[973] = 0;
                s_buf[972] = 0;
                s_prw[972] = 0;
                s_buf[971] = 0;
                s_prw[971] = 0;
                s_buf[970] = 0;
                s_prw[970] = 0;
                s_buf[969] = 0;
                s_prw[969] = 0;
                s_buf[968] = 0;
                s_prw[968] = 0;
            case 119:
                s_buf[967] = 0;
                s_prw[967] = 0;
                s_buf[966] = 0;
                s_prw[966] = 0;
                s_buf[965] = 0;
                s_prw[965] = 0;
                s_buf[964] = 0;
                s_prw[964] = 0;
                s_buf[963] = 0;
                s_prw[963] = 0;
                s_buf[962] = 0;
                s_prw[962] = 0;
                s_buf[961] = 0;
                s_prw[961] = 0;
                s_buf[960] = 0;
                s_prw[960] = 0;
            case 118:
                s_buf[959] = 0;
                s_prw[959] = 0;
                s_buf[958] = 0;
                s_prw[958] = 0;
                s_buf[957] = 0;
                s_prw[957] = 0;
                s_buf[956] = 0;
                s_prw[956] = 0;
                s_buf[955] = 0;
                s_prw[955] = 0;
                s_buf[954] = 0;
                s_prw[954] = 0;
                s_buf[953] = 0;
                s_prw[953] = 0;
                s_buf[952] = 0;
                s_prw[952] = 0;
            case 117:
                s_buf[951] = 0;
                s_prw[951] = 0;
                s_buf[950] = 0;
                s_prw[950] = 0;
                s_buf[949] = 0;
                s_prw[949] = 0;
                s_buf[948] = 0;
                s_prw[948] = 0;
                s_buf[947] = 0;
                s_prw[947] = 0;
                s_buf[946] = 0;
                s_prw[946] = 0;
                s_buf[945] = 0;
                s_prw[945] = 0;
                s_buf[944] = 0;
                s_prw[944] = 0;
            case 116:
                s_buf[943] = 0;
                s_prw[943] = 0;
                s_buf[942] = 0;
                s_prw[942] = 0;
                s_buf[941] = 0;
                s_prw[941] = 0;
                s_buf[940] = 0;
                s_prw[940] = 0;
                s_buf[939] = 0;
                s_prw[939] = 0;
                s_buf[938] = 0;
                s_prw[938] = 0;
                s_buf[937] = 0;
                s_prw[937] = 0;
                s_buf[936] = 0;
                s_prw[936] = 0;
            case 115:
                s_buf[935] = 0;
                s_prw[935] = 0;
                s_buf[934] = 0;
                s_prw[934] = 0;
                s_buf[933] = 0;
                s_prw[933] = 0;
                s_buf[932] = 0;
                s_prw[932] = 0;
                s_buf[931] = 0;
                s_prw[931] = 0;
                s_buf[930] = 0;
                s_prw[930] = 0;
                s_buf[929] = 0;
                s_prw[929] = 0;
                s_buf[928] = 0;
                s_prw[928] = 0;
            case 114:
                s_buf[927] = 0;
                s_prw[927] = 0;
                s_buf[926] = 0;
                s_prw[926] = 0;
                s_buf[925] = 0;
                s_prw[925] = 0;
                s_buf[924] = 0;
                s_prw[924] = 0;
                s_buf[923] = 0;
                s_prw[923] = 0;
                s_buf[922] = 0;
                s_prw[922] = 0;
                s_buf[921] = 0;
                s_prw[921] = 0;
                s_buf[920] = 0;
                s_prw[920] = 0;
            case 113:
                s_buf[919] = 0;
                s_prw[919] = 0;
                s_buf[918] = 0;
                s_prw[918] = 0;
                s_buf[917] = 0;
                s_prw[917] = 0;
                s_buf[916] = 0;
                s_prw[916] = 0;
                s_buf[915] = 0;
                s_prw[915] = 0;
                s_buf[914] = 0;
                s_prw[914] = 0;
                s_buf[913] = 0;
                s_prw[913] = 0;
                s_buf[912] = 0;
                s_prw[912] = 0;
            case 112:
                s_buf[911] = 0;
                s_prw[911] = 0;
                s_buf[910] = 0;
                s_prw[910] = 0;
                s_buf[909] = 0;
                s_prw[909] = 0;
                s_buf[908] = 0;
                s_prw[908] = 0;
                s_buf[907] = 0;
                s_prw[907] = 0;
                s_buf[906] = 0;
                s_prw[906] = 0;
                s_buf[905] = 0;
                s_prw[905] = 0;
                s_buf[904] = 0;
                s_prw[904] = 0;
            case 111:
                s_buf[903] = 0;
                s_prw[903] = 0;
                s_buf[902] = 0;
                s_prw[902] = 0;
                s_buf[901] = 0;
                s_prw[901] = 0;
                s_buf[900] = 0;
                s_prw[900] = 0;
                s_buf[899] = 0;
                s_prw[899] = 0;
                s_buf[898] = 0;
                s_prw[898] = 0;
                s_buf[897] = 0;
                s_prw[897] = 0;
                s_buf[896] = 0;
                s_prw[896] = 0;
            case 110:
                s_buf[895] = 0;
                s_prw[895] = 0;
                s_buf[894] = 0;
                s_prw[894] = 0;
                s_buf[893] = 0;
                s_prw[893] = 0;
                s_buf[892] = 0;
                s_prw[892] = 0;
                s_buf[891] = 0;
                s_prw[891] = 0;
                s_buf[890] = 0;
                s_prw[890] = 0;
                s_buf[889] = 0;
                s_prw[889] = 0;
                s_buf[888] = 0;
                s_prw[888] = 0;
            case 109:
                s_buf[887] = 0;
                s_prw[887] = 0;
                s_buf[886] = 0;
                s_prw[886] = 0;
                s_buf[885] = 0;
                s_prw[885] = 0;
                s_buf[884] = 0;
                s_prw[884] = 0;
                s_buf[883] = 0;
                s_prw[883] = 0;
                s_buf[882] = 0;
                s_prw[882] = 0;
                s_buf[881] = 0;
                s_prw[881] = 0;
                s_buf[880] = 0;
                s_prw[880] = 0;
            case 108:
                s_buf[879] = 0;
                s_prw[879] = 0;
                s_buf[878] = 0;
                s_prw[878] = 0;
                s_buf[877] = 0;
                s_prw[877] = 0;
                s_buf[876] = 0;
                s_prw[876] = 0;
                s_buf[875] = 0;
                s_prw[875] = 0;
                s_buf[874] = 0;
                s_prw[874] = 0;
                s_buf[873] = 0;
                s_prw[873] = 0;
                s_buf[872] = 0;
                s_prw[872] = 0;
            case 107:
                s_buf[871] = 0;
                s_prw[871] = 0;
                s_buf[870] = 0;
                s_prw[870] = 0;
                s_buf[869] = 0;
                s_prw[869] = 0;
                s_buf[868] = 0;
                s_prw[868] = 0;
                s_buf[867] = 0;
                s_prw[867] = 0;
                s_buf[866] = 0;
                s_prw[866] = 0;
                s_buf[865] = 0;
                s_prw[865] = 0;
                s_buf[864] = 0;
                s_prw[864] = 0;
            case 106:
                s_buf[863] = 0;
                s_prw[863] = 0;
                s_buf[862] = 0;
                s_prw[862] = 0;
                s_buf[861] = 0;
                s_prw[861] = 0;
                s_buf[860] = 0;
                s_prw[860] = 0;
                s_buf[859] = 0;
                s_prw[859] = 0;
                s_buf[858] = 0;
                s_prw[858] = 0;
                s_buf[857] = 0;
                s_prw[857] = 0;
                s_buf[856] = 0;
                s_prw[856] = 0;
            case 105:
                s_buf[855] = 0;
                s_prw[855] = 0;
                s_buf[854] = 0;
                s_prw[854] = 0;
                s_buf[853] = 0;
                s_prw[853] = 0;
                s_buf[852] = 0;
                s_prw[852] = 0;
                s_buf[851] = 0;
                s_prw[851] = 0;
                s_buf[850] = 0;
                s_prw[850] = 0;
                s_buf[849] = 0;
                s_prw[849] = 0;
                s_buf[848] = 0;
                s_prw[848] = 0;
            case 104:
                s_buf[847] = 0;
                s_prw[847] = 0;
                s_buf[846] = 0;
                s_prw[846] = 0;
                s_buf[845] = 0;
                s_prw[845] = 0;
                s_buf[844] = 0;
                s_prw[844] = 0;
                s_buf[843] = 0;
                s_prw[843] = 0;
                s_buf[842] = 0;
                s_prw[842] = 0;
                s_buf[841] = 0;
                s_prw[841] = 0;
                s_buf[840] = 0;
                s_prw[840] = 0;
            case 103:
                s_buf[839] = 0;
                s_prw[839] = 0;
                s_buf[838] = 0;
                s_prw[838] = 0;
                s_buf[837] = 0;
                s_prw[837] = 0;
                s_buf[836] = 0;
                s_prw[836] = 0;
                s_buf[835] = 0;
                s_prw[835] = 0;
                s_buf[834] = 0;
                s_prw[834] = 0;
                s_buf[833] = 0;
                s_prw[833] = 0;
                s_buf[832] = 0;
                s_prw[832] = 0;
            case 102:
                s_buf[831] = 0;
                s_prw[831] = 0;
                s_buf[830] = 0;
                s_prw[830] = 0;
                s_buf[829] = 0;
                s_prw[829] = 0;
                s_buf[828] = 0;
                s_prw[828] = 0;
                s_buf[827] = 0;
                s_prw[827] = 0;
                s_buf[826] = 0;
                s_prw[826] = 0;
                s_buf[825] = 0;
                s_prw[825] = 0;
                s_buf[824] = 0;
                s_prw[824] = 0;
            case 101:
                s_buf[823] = 0;
                s_prw[823] = 0;
                s_buf[822] = 0;
                s_prw[822] = 0;
                s_buf[821] = 0;
                s_prw[821] = 0;
                s_buf[820] = 0;
                s_prw[820] = 0;
                s_buf[819] = 0;
                s_prw[819] = 0;
                s_buf[818] = 0;
                s_prw[818] = 0;
                s_buf[817] = 0;
                s_prw[817] = 0;
                s_buf[816] = 0;
                s_prw[816] = 0;
            case 100:
                s_buf[815] = 0;
                s_prw[815] = 0;
                s_buf[814] = 0;
                s_prw[814] = 0;
                s_buf[813] = 0;
                s_prw[813] = 0;
                s_buf[812] = 0;
                s_prw[812] = 0;
                s_buf[811] = 0;
                s_prw[811] = 0;
                s_buf[810] = 0;
                s_prw[810] = 0;
                s_buf[809] = 0;
                s_prw[809] = 0;
                s_buf[808] = 0;
                s_prw[808] = 0;
            case 99:
                s_buf[807] = 0;
                s_prw[807] = 0;
                s_buf[806] = 0;
                s_prw[806] = 0;
                s_buf[805] = 0;
                s_prw[805] = 0;
                s_buf[804] = 0;
                s_prw[804] = 0;
                s_buf[803] = 0;
                s_prw[803] = 0;
                s_buf[802] = 0;
                s_prw[802] = 0;
                s_buf[801] = 0;
                s_prw[801] = 0;
                s_buf[800] = 0;
                s_prw[800] = 0;
            case 98:
                s_buf[799] = 0;
                s_prw[799] = 0;
                s_buf[798] = 0;
                s_prw[798] = 0;
                s_buf[797] = 0;
                s_prw[797] = 0;
                s_buf[796] = 0;
                s_prw[796] = 0;
                s_buf[795] = 0;
                s_prw[795] = 0;
                s_buf[794] = 0;
                s_prw[794] = 0;
                s_buf[793] = 0;
                s_prw[793] = 0;
                s_buf[792] = 0;
                s_prw[792] = 0;
            case 97:
                s_buf[791] = 0;
                s_prw[791] = 0;
                s_buf[790] = 0;
                s_prw[790] = 0;
                s_buf[789] = 0;
                s_prw[789] = 0;
                s_buf[788] = 0;
                s_prw[788] = 0;
                s_buf[787] = 0;
                s_prw[787] = 0;
                s_buf[786] = 0;
                s_prw[786] = 0;
                s_buf[785] = 0;
                s_prw[785] = 0;
                s_buf[784] = 0;
                s_prw[784] = 0;
            case 96:
                s_buf[783] = 0;
                s_prw[783] = 0;
                s_buf[782] = 0;
                s_prw[782] = 0;
                s_buf[781] = 0;
                s_prw[781] = 0;
                s_buf[780] = 0;
                s_prw[780] = 0;
                s_buf[779] = 0;
                s_prw[779] = 0;
                s_buf[778] = 0;
                s_prw[778] = 0;
                s_buf[777] = 0;
                s_prw[777] = 0;
                s_buf[776] = 0;
                s_prw[776] = 0;
            case 95:
                s_buf[775] = 0;
                s_prw[775] = 0;
                s_buf[774] = 0;
                s_prw[774] = 0;
                s_buf[773] = 0;
                s_prw[773] = 0;
                s_buf[772] = 0;
                s_prw[772] = 0;
                s_buf[771] = 0;
                s_prw[771] = 0;
                s_buf[770] = 0;
                s_prw[770] = 0;
                s_buf[769] = 0;
                s_prw[769] = 0;
                s_buf[768] = 0;
                s_prw[768] = 0;
            case 94:
                s_buf[767] = 0;
                s_prw[767] = 0;
                s_buf[766] = 0;
                s_prw[766] = 0;
                s_buf[765] = 0;
                s_prw[765] = 0;
                s_buf[764] = 0;
                s_prw[764] = 0;
                s_buf[763] = 0;
                s_prw[763] = 0;
                s_buf[762] = 0;
                s_prw[762] = 0;
                s_buf[761] = 0;
                s_prw[761] = 0;
                s_buf[760] = 0;
                s_prw[760] = 0;
            case 93:
                s_buf[759] = 0;
                s_prw[759] = 0;
                s_buf[758] = 0;
                s_prw[758] = 0;
                s_buf[757] = 0;
                s_prw[757] = 0;
                s_buf[756] = 0;
                s_prw[756] = 0;
                s_buf[755] = 0;
                s_prw[755] = 0;
                s_buf[754] = 0;
                s_prw[754] = 0;
                s_buf[753] = 0;
                s_prw[753] = 0;
                s_buf[752] = 0;
                s_prw[752] = 0;
            case 92:
                s_buf[751] = 0;
                s_prw[751] = 0;
                s_buf[750] = 0;
                s_prw[750] = 0;
                s_buf[749] = 0;
                s_prw[749] = 0;
                s_buf[748] = 0;
                s_prw[748] = 0;
                s_buf[747] = 0;
                s_prw[747] = 0;
                s_buf[746] = 0;
                s_prw[746] = 0;
                s_buf[745] = 0;
                s_prw[745] = 0;
                s_buf[744] = 0;
                s_prw[744] = 0;
            case 91:
                s_buf[743] = 0;
                s_prw[743] = 0;
                s_buf[742] = 0;
                s_prw[742] = 0;
                s_buf[741] = 0;
                s_prw[741] = 0;
                s_buf[740] = 0;
                s_prw[740] = 0;
                s_buf[739] = 0;
                s_prw[739] = 0;
                s_buf[738] = 0;
                s_prw[738] = 0;
                s_buf[737] = 0;
                s_prw[737] = 0;
                s_buf[736] = 0;
                s_prw[736] = 0;
            case 90:
                s_buf[735] = 0;
                s_prw[735] = 0;
                s_buf[734] = 0;
                s_prw[734] = 0;
                s_buf[733] = 0;
                s_prw[733] = 0;
                s_buf[732] = 0;
                s_prw[732] = 0;
                s_buf[731] = 0;
                s_prw[731] = 0;
                s_buf[730] = 0;
                s_prw[730] = 0;
                s_buf[729] = 0;
                s_prw[729] = 0;
                s_buf[728] = 0;
                s_prw[728] = 0;
            case 89:
                s_buf[727] = 0;
                s_prw[727] = 0;
                s_buf[726] = 0;
                s_prw[726] = 0;
                s_buf[725] = 0;
                s_prw[725] = 0;
                s_buf[724] = 0;
                s_prw[724] = 0;
                s_buf[723] = 0;
                s_prw[723] = 0;
                s_buf[722] = 0;
                s_prw[722] = 0;
                s_buf[721] = 0;
                s_prw[721] = 0;
                s_buf[720] = 0;
                s_prw[720] = 0;
            case 88:
                s_buf[719] = 0;
                s_prw[719] = 0;
                s_buf[718] = 0;
                s_prw[718] = 0;
                s_buf[717] = 0;
                s_prw[717] = 0;
                s_buf[716] = 0;
                s_prw[716] = 0;
                s_buf[715] = 0;
                s_prw[715] = 0;
                s_buf[714] = 0;
                s_prw[714] = 0;
                s_buf[713] = 0;
                s_prw[713] = 0;
                s_buf[712] = 0;
                s_prw[712] = 0;
            case 87:
                s_buf[711] = 0;
                s_prw[711] = 0;
                s_buf[710] = 0;
                s_prw[710] = 0;
                s_buf[709] = 0;
                s_prw[709] = 0;
                s_buf[708] = 0;
                s_prw[708] = 0;
                s_buf[707] = 0;
                s_prw[707] = 0;
                s_buf[706] = 0;
                s_prw[706] = 0;
                s_buf[705] = 0;
                s_prw[705] = 0;
                s_buf[704] = 0;
                s_prw[704] = 0;
            case 86:
                s_buf[703] = 0;
                s_prw[703] = 0;
                s_buf[702] = 0;
                s_prw[702] = 0;
                s_buf[701] = 0;
                s_prw[701] = 0;
                s_buf[700] = 0;
                s_prw[700] = 0;
                s_buf[699] = 0;
                s_prw[699] = 0;
                s_buf[698] = 0;
                s_prw[698] = 0;
                s_buf[697] = 0;
                s_prw[697] = 0;
                s_buf[696] = 0;
                s_prw[696] = 0;
            case 85:
                s_buf[695] = 0;
                s_prw[695] = 0;
                s_buf[694] = 0;
                s_prw[694] = 0;
                s_buf[693] = 0;
                s_prw[693] = 0;
                s_buf[692] = 0;
                s_prw[692] = 0;
                s_buf[691] = 0;
                s_prw[691] = 0;
                s_buf[690] = 0;
                s_prw[690] = 0;
                s_buf[689] = 0;
                s_prw[689] = 0;
                s_buf[688] = 0;
                s_prw[688] = 0;
            case 84:
                s_buf[687] = 0;
                s_prw[687] = 0;
                s_buf[686] = 0;
                s_prw[686] = 0;
                s_buf[685] = 0;
                s_prw[685] = 0;
                s_buf[684] = 0;
                s_prw[684] = 0;
                s_buf[683] = 0;
                s_prw[683] = 0;
                s_buf[682] = 0;
                s_prw[682] = 0;
                s_buf[681] = 0;
                s_prw[681] = 0;
                s_buf[680] = 0;
                s_prw[680] = 0;
            case 83:
                s_buf[679] = 0;
                s_prw[679] = 0;
                s_buf[678] = 0;
                s_prw[678] = 0;
                s_buf[677] = 0;
                s_prw[677] = 0;
                s_buf[676] = 0;
                s_prw[676] = 0;
                s_buf[675] = 0;
                s_prw[675] = 0;
                s_buf[674] = 0;
                s_prw[674] = 0;
                s_buf[673] = 0;
                s_prw[673] = 0;
                s_buf[672] = 0;
                s_prw[672] = 0;
            case 82:
                s_buf[671] = 0;
                s_prw[671] = 0;
                s_buf[670] = 0;
                s_prw[670] = 0;
                s_buf[669] = 0;
                s_prw[669] = 0;
                s_buf[668] = 0;
                s_prw[668] = 0;
                s_buf[667] = 0;
                s_prw[667] = 0;
                s_buf[666] = 0;
                s_prw[666] = 0;
                s_buf[665] = 0;
                s_prw[665] = 0;
                s_buf[664] = 0;
                s_prw[664] = 0;
            case 81:
                s_buf[663] = 0;
                s_prw[663] = 0;
                s_buf[662] = 0;
                s_prw[662] = 0;
                s_buf[661] = 0;
                s_prw[661] = 0;
                s_buf[660] = 0;
                s_prw[660] = 0;
                s_buf[659] = 0;
                s_prw[659] = 0;
                s_buf[658] = 0;
                s_prw[658] = 0;
                s_buf[657] = 0;
                s_prw[657] = 0;
                s_buf[656] = 0;
                s_prw[656] = 0;
            case 80:
                s_buf[655] = 0;
                s_prw[655] = 0;
                s_buf[654] = 0;
                s_prw[654] = 0;
                s_buf[653] = 0;
                s_prw[653] = 0;
                s_buf[652] = 0;
                s_prw[652] = 0;
                s_buf[651] = 0;
                s_prw[651] = 0;
                s_buf[650] = 0;
                s_prw[650] = 0;
                s_buf[649] = 0;
                s_prw[649] = 0;
                s_buf[648] = 0;
                s_prw[648] = 0;
            case 79:
                s_buf[647] = 0;
                s_prw[647] = 0;
                s_buf[646] = 0;
                s_prw[646] = 0;
                s_buf[645] = 0;
                s_prw[645] = 0;
                s_buf[644] = 0;
                s_prw[644] = 0;
                s_buf[643] = 0;
                s_prw[643] = 0;
                s_buf[642] = 0;
                s_prw[642] = 0;
                s_buf[641] = 0;
                s_prw[641] = 0;
                s_buf[640] = 0;
                s_prw[640] = 0;
            case 78:
                s_buf[639] = 0;
                s_prw[639] = 0;
                s_buf[638] = 0;
                s_prw[638] = 0;
                s_buf[637] = 0;
                s_prw[637] = 0;
                s_buf[636] = 0;
                s_prw[636] = 0;
                s_buf[635] = 0;
                s_prw[635] = 0;
                s_buf[634] = 0;
                s_prw[634] = 0;
                s_buf[633] = 0;
                s_prw[633] = 0;
                s_buf[632] = 0;
                s_prw[632] = 0;
            case 77:
                s_buf[631] = 0;
                s_prw[631] = 0;
                s_buf[630] = 0;
                s_prw[630] = 0;
                s_buf[629] = 0;
                s_prw[629] = 0;
                s_buf[628] = 0;
                s_prw[628] = 0;
                s_buf[627] = 0;
                s_prw[627] = 0;
                s_buf[626] = 0;
                s_prw[626] = 0;
                s_buf[625] = 0;
                s_prw[625] = 0;
                s_buf[624] = 0;
                s_prw[624] = 0;
            case 76:
                s_buf[623] = 0;
                s_prw[623] = 0;
                s_buf[622] = 0;
                s_prw[622] = 0;
                s_buf[621] = 0;
                s_prw[621] = 0;
                s_buf[620] = 0;
                s_prw[620] = 0;
                s_buf[619] = 0;
                s_prw[619] = 0;
                s_buf[618] = 0;
                s_prw[618] = 0;
                s_buf[617] = 0;
                s_prw[617] = 0;
                s_buf[616] = 0;
                s_prw[616] = 0;
            case 75:
                s_buf[615] = 0;
                s_prw[615] = 0;
                s_buf[614] = 0;
                s_prw[614] = 0;
                s_buf[613] = 0;
                s_prw[613] = 0;
                s_buf[612] = 0;
                s_prw[612] = 0;
                s_buf[611] = 0;
                s_prw[611] = 0;
                s_buf[610] = 0;
                s_prw[610] = 0;
                s_buf[609] = 0;
                s_prw[609] = 0;
                s_buf[608] = 0;
                s_prw[608] = 0;
            case 74:
                s_buf[607] = 0;
                s_prw[607] = 0;
                s_buf[606] = 0;
                s_prw[606] = 0;
                s_buf[605] = 0;
                s_prw[605] = 0;
                s_buf[604] = 0;
                s_prw[604] = 0;
                s_buf[603] = 0;
                s_prw[603] = 0;
                s_buf[602] = 0;
                s_prw[602] = 0;
                s_buf[601] = 0;
                s_prw[601] = 0;
                s_buf[600] = 0;
                s_prw[600] = 0;
            case 73:
                s_buf[599] = 0;
                s_prw[599] = 0;
                s_buf[598] = 0;
                s_prw[598] = 0;
                s_buf[597] = 0;
                s_prw[597] = 0;
                s_buf[596] = 0;
                s_prw[596] = 0;
                s_buf[595] = 0;
                s_prw[595] = 0;
                s_buf[594] = 0;
                s_prw[594] = 0;
                s_buf[593] = 0;
                s_prw[593] = 0;
                s_buf[592] = 0;
                s_prw[592] = 0;
            case 72:
                s_buf[591] = 0;
                s_prw[591] = 0;
                s_buf[590] = 0;
                s_prw[590] = 0;
                s_buf[589] = 0;
                s_prw[589] = 0;
                s_buf[588] = 0;
                s_prw[588] = 0;
                s_buf[587] = 0;
                s_prw[587] = 0;
                s_buf[586] = 0;
                s_prw[586] = 0;
                s_buf[585] = 0;
                s_prw[585] = 0;
                s_buf[584] = 0;
                s_prw[584] = 0;
            case 71:
                s_buf[583] = 0;
                s_prw[583] = 0;
                s_buf[582] = 0;
                s_prw[582] = 0;
                s_buf[581] = 0;
                s_prw[581] = 0;
                s_buf[580] = 0;
                s_prw[580] = 0;
                s_buf[579] = 0;
                s_prw[579] = 0;
                s_buf[578] = 0;
                s_prw[578] = 0;
                s_buf[577] = 0;
                s_prw[577] = 0;
                s_buf[576] = 0;
                s_prw[576] = 0;
            case 70:
                s_buf[575] = 0;
                s_prw[575] = 0;
                s_buf[574] = 0;
                s_prw[574] = 0;
                s_buf[573] = 0;
                s_prw[573] = 0;
                s_buf[572] = 0;
                s_prw[572] = 0;
                s_buf[571] = 0;
                s_prw[571] = 0;
                s_buf[570] = 0;
                s_prw[570] = 0;
                s_buf[569] = 0;
                s_prw[569] = 0;
                s_buf[568] = 0;
                s_prw[568] = 0;
            case 69:
                s_buf[567] = 0;
                s_prw[567] = 0;
                s_buf[566] = 0;
                s_prw[566] = 0;
                s_buf[565] = 0;
                s_prw[565] = 0;
                s_buf[564] = 0;
                s_prw[564] = 0;
                s_buf[563] = 0;
                s_prw[563] = 0;
                s_buf[562] = 0;
                s_prw[562] = 0;
                s_buf[561] = 0;
                s_prw[561] = 0;
                s_buf[560] = 0;
                s_prw[560] = 0;
            case 68:
                s_buf[559] = 0;
                s_prw[559] = 0;
                s_buf[558] = 0;
                s_prw[558] = 0;
                s_buf[557] = 0;
                s_prw[557] = 0;
                s_buf[556] = 0;
                s_prw[556] = 0;
                s_buf[555] = 0;
                s_prw[555] = 0;
                s_buf[554] = 0;
                s_prw[554] = 0;
                s_buf[553] = 0;
                s_prw[553] = 0;
                s_buf[552] = 0;
                s_prw[552] = 0;
            case 67:
                s_buf[551] = 0;
                s_prw[551] = 0;
                s_buf[550] = 0;
                s_prw[550] = 0;
                s_buf[549] = 0;
                s_prw[549] = 0;
                s_buf[548] = 0;
                s_prw[548] = 0;
                s_buf[547] = 0;
                s_prw[547] = 0;
                s_buf[546] = 0;
                s_prw[546] = 0;
                s_buf[545] = 0;
                s_prw[545] = 0;
                s_buf[544] = 0;
                s_prw[544] = 0;
            case 66:
                s_buf[543] = 0;
                s_prw[543] = 0;
                s_buf[542] = 0;
                s_prw[542] = 0;
                s_buf[541] = 0;
                s_prw[541] = 0;
                s_buf[540] = 0;
                s_prw[540] = 0;
                s_buf[539] = 0;
                s_prw[539] = 0;
                s_buf[538] = 0;
                s_prw[538] = 0;
                s_buf[537] = 0;
                s_prw[537] = 0;
                s_buf[536] = 0;
                s_prw[536] = 0;
            case 65:
                s_buf[535] = 0;
                s_prw[535] = 0;
                s_buf[534] = 0;
                s_prw[534] = 0;
                s_buf[533] = 0;
                s_prw[533] = 0;
                s_buf[532] = 0;
                s_prw[532] = 0;
                s_buf[531] = 0;
                s_prw[531] = 0;
                s_buf[530] = 0;
                s_prw[530] = 0;
                s_buf[529] = 0;
                s_prw[529] = 0;
                s_buf[528] = 0;
                s_prw[528] = 0;
            case 64:
                s_buf[527] = 0;
                s_prw[527] = 0;
                s_buf[526] = 0;
                s_prw[526] = 0;
                s_buf[525] = 0;
                s_prw[525] = 0;
                s_buf[524] = 0;
                s_prw[524] = 0;
                s_buf[523] = 0;
                s_prw[523] = 0;
                s_buf[522] = 0;
                s_prw[522] = 0;
                s_buf[521] = 0;
                s_prw[521] = 0;
                s_buf[520] = 0;
                s_prw[520] = 0;
            case 63:
                s_buf[519] = 0;
                s_prw[519] = 0;
                s_buf[518] = 0;
                s_prw[518] = 0;
                s_buf[517] = 0;
                s_prw[517] = 0;
                s_buf[516] = 0;
                s_prw[516] = 0;
                s_buf[515] = 0;
                s_prw[515] = 0;
                s_buf[514] = 0;
                s_prw[514] = 0;
                s_buf[513] = 0;
                s_prw[513] = 0;
                s_buf[512] = 0;
                s_prw[512] = 0;
            case 62:
                s_buf[511] = 0;
                s_prw[511] = 0;
                s_buf[510] = 0;
                s_prw[510] = 0;
                s_buf[509] = 0;
                s_prw[509] = 0;
                s_buf[508] = 0;
                s_prw[508] = 0;
                s_buf[507] = 0;
                s_prw[507] = 0;
                s_buf[506] = 0;
                s_prw[506] = 0;
                s_buf[505] = 0;
                s_prw[505] = 0;
                s_buf[504] = 0;
                s_prw[504] = 0;
            case 61:
                s_buf[503] = 0;
                s_prw[503] = 0;
                s_buf[502] = 0;
                s_prw[502] = 0;
                s_buf[501] = 0;
                s_prw[501] = 0;
                s_buf[500] = 0;
                s_prw[500] = 0;
                s_buf[499] = 0;
                s_prw[499] = 0;
                s_buf[498] = 0;
                s_prw[498] = 0;
                s_buf[497] = 0;
                s_prw[497] = 0;
                s_buf[496] = 0;
                s_prw[496] = 0;
            case 60:
                s_buf[495] = 0;
                s_prw[495] = 0;
                s_buf[494] = 0;
                s_prw[494] = 0;
                s_buf[493] = 0;
                s_prw[493] = 0;
                s_buf[492] = 0;
                s_prw[492] = 0;
                s_buf[491] = 0;
                s_prw[491] = 0;
                s_buf[490] = 0;
                s_prw[490] = 0;
                s_buf[489] = 0;
                s_prw[489] = 0;
                s_buf[488] = 0;
                s_prw[488] = 0;
            case 59:
                s_buf[487] = 0;
                s_prw[487] = 0;
                s_buf[486] = 0;
                s_prw[486] = 0;
                s_buf[485] = 0;
                s_prw[485] = 0;
                s_buf[484] = 0;
                s_prw[484] = 0;
                s_buf[483] = 0;
                s_prw[483] = 0;
                s_buf[482] = 0;
                s_prw[482] = 0;
                s_buf[481] = 0;
                s_prw[481] = 0;
                s_buf[480] = 0;
                s_prw[480] = 0;
            case 58:
                s_buf[479] = 0;
                s_prw[479] = 0;
                s_buf[478] = 0;
                s_prw[478] = 0;
                s_buf[477] = 0;
                s_prw[477] = 0;
                s_buf[476] = 0;
                s_prw[476] = 0;
                s_buf[475] = 0;
                s_prw[475] = 0;
                s_buf[474] = 0;
                s_prw[474] = 0;
                s_buf[473] = 0;
                s_prw[473] = 0;
                s_buf[472] = 0;
                s_prw[472] = 0;
            case 57:
                s_buf[471] = 0;
                s_prw[471] = 0;
                s_buf[470] = 0;
                s_prw[470] = 0;
                s_buf[469] = 0;
                s_prw[469] = 0;
                s_buf[468] = 0;
                s_prw[468] = 0;
                s_buf[467] = 0;
                s_prw[467] = 0;
                s_buf[466] = 0;
                s_prw[466] = 0;
                s_buf[465] = 0;
                s_prw[465] = 0;
                s_buf[464] = 0;
                s_prw[464] = 0;
            case 56:
                s_buf[463] = 0;
                s_prw[463] = 0;
                s_buf[462] = 0;
                s_prw[462] = 0;
                s_buf[461] = 0;
                s_prw[461] = 0;
                s_buf[460] = 0;
                s_prw[460] = 0;
                s_buf[459] = 0;
                s_prw[459] = 0;
                s_buf[458] = 0;
                s_prw[458] = 0;
                s_buf[457] = 0;
                s_prw[457] = 0;
                s_buf[456] = 0;
                s_prw[456] = 0;
            case 55:
                s_buf[455] = 0;
                s_prw[455] = 0;
                s_buf[454] = 0;
                s_prw[454] = 0;
                s_buf[453] = 0;
                s_prw[453] = 0;
                s_buf[452] = 0;
                s_prw[452] = 0;
                s_buf[451] = 0;
                s_prw[451] = 0;
                s_buf[450] = 0;
                s_prw[450] = 0;
                s_buf[449] = 0;
                s_prw[449] = 0;
                s_buf[448] = 0;
                s_prw[448] = 0;
            case 54:
                s_buf[447] = 0;
                s_prw[447] = 0;
                s_buf[446] = 0;
                s_prw[446] = 0;
                s_buf[445] = 0;
                s_prw[445] = 0;
                s_buf[444] = 0;
                s_prw[444] = 0;
                s_buf[443] = 0;
                s_prw[443] = 0;
                s_buf[442] = 0;
                s_prw[442] = 0;
                s_buf[441] = 0;
                s_prw[441] = 0;
                s_buf[440] = 0;
                s_prw[440] = 0;
            case 53:
                s_buf[439] = 0;
                s_prw[439] = 0;
                s_buf[438] = 0;
                s_prw[438] = 0;
                s_buf[437] = 0;
                s_prw[437] = 0;
                s_buf[436] = 0;
                s_prw[436] = 0;
                s_buf[435] = 0;
                s_prw[435] = 0;
                s_buf[434] = 0;
                s_prw[434] = 0;
                s_buf[433] = 0;
                s_prw[433] = 0;
                s_buf[432] = 0;
                s_prw[432] = 0;
            case 52:
                s_buf[431] = 0;
                s_prw[431] = 0;
                s_buf[430] = 0;
                s_prw[430] = 0;
                s_buf[429] = 0;
                s_prw[429] = 0;
                s_buf[428] = 0;
                s_prw[428] = 0;
                s_buf[427] = 0;
                s_prw[427] = 0;
                s_buf[426] = 0;
                s_prw[426] = 0;
                s_buf[425] = 0;
                s_prw[425] = 0;
                s_buf[424] = 0;
                s_prw[424] = 0;
            case 51:
                s_buf[423] = 0;
                s_prw[423] = 0;
                s_buf[422] = 0;
                s_prw[422] = 0;
                s_buf[421] = 0;
                s_prw[421] = 0;
                s_buf[420] = 0;
                s_prw[420] = 0;
                s_buf[419] = 0;
                s_prw[419] = 0;
                s_buf[418] = 0;
                s_prw[418] = 0;
                s_buf[417] = 0;
                s_prw[417] = 0;
                s_buf[416] = 0;
                s_prw[416] = 0;
            case 50:
                s_buf[415] = 0;
                s_prw[415] = 0;
                s_buf[414] = 0;
                s_prw[414] = 0;
                s_buf[413] = 0;
                s_prw[413] = 0;
                s_buf[412] = 0;
                s_prw[412] = 0;
                s_buf[411] = 0;
                s_prw[411] = 0;
                s_buf[410] = 0;
                s_prw[410] = 0;
                s_buf[409] = 0;
                s_prw[409] = 0;
                s_buf[408] = 0;
                s_prw[408] = 0;
            case 49:
                s_buf[407] = 0;
                s_prw[407] = 0;
                s_buf[406] = 0;
                s_prw[406] = 0;
                s_buf[405] = 0;
                s_prw[405] = 0;
                s_buf[404] = 0;
                s_prw[404] = 0;
                s_buf[403] = 0;
                s_prw[403] = 0;
                s_buf[402] = 0;
                s_prw[402] = 0;
                s_buf[401] = 0;
                s_prw[401] = 0;
                s_buf[400] = 0;
                s_prw[400] = 0;
            case 48:
                s_buf[399] = 0;
                s_prw[399] = 0;
                s_buf[398] = 0;
                s_prw[398] = 0;
                s_buf[397] = 0;
                s_prw[397] = 0;
                s_buf[396] = 0;
                s_prw[396] = 0;
                s_buf[395] = 0;
                s_prw[395] = 0;
                s_buf[394] = 0;
                s_prw[394] = 0;
                s_buf[393] = 0;
                s_prw[393] = 0;
                s_buf[392] = 0;
                s_prw[392] = 0;
            case 47:
                s_buf[391] = 0;
                s_prw[391] = 0;
                s_buf[390] = 0;
                s_prw[390] = 0;
                s_buf[389] = 0;
                s_prw[389] = 0;
                s_buf[388] = 0;
                s_prw[388] = 0;
                s_buf[387] = 0;
                s_prw[387] = 0;
                s_buf[386] = 0;
                s_prw[386] = 0;
                s_buf[385] = 0;
                s_prw[385] = 0;
                s_buf[384] = 0;
                s_prw[384] = 0;
            case 46:
                s_buf[383] = 0;
                s_prw[383] = 0;
                s_buf[382] = 0;
                s_prw[382] = 0;
                s_buf[381] = 0;
                s_prw[381] = 0;
                s_buf[380] = 0;
                s_prw[380] = 0;
                s_buf[379] = 0;
                s_prw[379] = 0;
                s_buf[378] = 0;
                s_prw[378] = 0;
                s_buf[377] = 0;
                s_prw[377] = 0;
                s_buf[376] = 0;
                s_prw[376] = 0;
            case 45:
                s_buf[375] = 0;
                s_prw[375] = 0;
                s_buf[374] = 0;
                s_prw[374] = 0;
                s_buf[373] = 0;
                s_prw[373] = 0;
                s_buf[372] = 0;
                s_prw[372] = 0;
                s_buf[371] = 0;
                s_prw[371] = 0;
                s_buf[370] = 0;
                s_prw[370] = 0;
                s_buf[369] = 0;
                s_prw[369] = 0;
                s_buf[368] = 0;
                s_prw[368] = 0;
            case 44:
                s_buf[367] = 0;
                s_prw[367] = 0;
                s_buf[366] = 0;
                s_prw[366] = 0;
                s_buf[365] = 0;
                s_prw[365] = 0;
                s_buf[364] = 0;
                s_prw[364] = 0;
                s_buf[363] = 0;
                s_prw[363] = 0;
                s_buf[362] = 0;
                s_prw[362] = 0;
                s_buf[361] = 0;
                s_prw[361] = 0;
                s_buf[360] = 0;
                s_prw[360] = 0;
            case 43:
                s_buf[359] = 0;
                s_prw[359] = 0;
                s_buf[358] = 0;
                s_prw[358] = 0;
                s_buf[357] = 0;
                s_prw[357] = 0;
                s_buf[356] = 0;
                s_prw[356] = 0;
                s_buf[355] = 0;
                s_prw[355] = 0;
                s_buf[354] = 0;
                s_prw[354] = 0;
                s_buf[353] = 0;
                s_prw[353] = 0;
                s_buf[352] = 0;
                s_prw[352] = 0;
            case 42:
                s_buf[351] = 0;
                s_prw[351] = 0;
                s_buf[350] = 0;
                s_prw[350] = 0;
                s_buf[349] = 0;
                s_prw[349] = 0;
                s_buf[348] = 0;
                s_prw[348] = 0;
                s_buf[347] = 0;
                s_prw[347] = 0;
                s_buf[346] = 0;
                s_prw[346] = 0;
                s_buf[345] = 0;
                s_prw[345] = 0;
                s_buf[344] = 0;
                s_prw[344] = 0;
            case 41:
                s_buf[343] = 0;
                s_prw[343] = 0;
                s_buf[342] = 0;
                s_prw[342] = 0;
                s_buf[341] = 0;
                s_prw[341] = 0;
                s_buf[340] = 0;
                s_prw[340] = 0;
                s_buf[339] = 0;
                s_prw[339] = 0;
                s_buf[338] = 0;
                s_prw[338] = 0;
                s_buf[337] = 0;
                s_prw[337] = 0;
                s_buf[336] = 0;
                s_prw[336] = 0;
            case 40:
                s_buf[335] = 0;
                s_prw[335] = 0;
                s_buf[334] = 0;
                s_prw[334] = 0;
                s_buf[333] = 0;
                s_prw[333] = 0;
                s_buf[332] = 0;
                s_prw[332] = 0;
                s_buf[331] = 0;
                s_prw[331] = 0;
                s_buf[330] = 0;
                s_prw[330] = 0;
                s_buf[329] = 0;
                s_prw[329] = 0;
                s_buf[328] = 0;
                s_prw[328] = 0;
            case 39:
                s_buf[327] = 0;
                s_prw[327] = 0;
                s_buf[326] = 0;
                s_prw[326] = 0;
                s_buf[325] = 0;
                s_prw[325] = 0;
                s_buf[324] = 0;
                s_prw[324] = 0;
                s_buf[323] = 0;
                s_prw[323] = 0;
                s_buf[322] = 0;
                s_prw[322] = 0;
                s_buf[321] = 0;
                s_prw[321] = 0;
                s_buf[320] = 0;
                s_prw[320] = 0;
            case 38:
                s_buf[319] = 0;
                s_prw[319] = 0;
                s_buf[318] = 0;
                s_prw[318] = 0;
                s_buf[317] = 0;
                s_prw[317] = 0;
                s_buf[316] = 0;
                s_prw[316] = 0;
                s_buf[315] = 0;
                s_prw[315] = 0;
                s_buf[314] = 0;
                s_prw[314] = 0;
                s_buf[313] = 0;
                s_prw[313] = 0;
                s_buf[312] = 0;
                s_prw[312] = 0;
            case 37:
                s_buf[311] = 0;
                s_prw[311] = 0;
                s_buf[310] = 0;
                s_prw[310] = 0;
                s_buf[309] = 0;
                s_prw[309] = 0;
                s_buf[308] = 0;
                s_prw[308] = 0;
                s_buf[307] = 0;
                s_prw[307] = 0;
                s_buf[306] = 0;
                s_prw[306] = 0;
                s_buf[305] = 0;
                s_prw[305] = 0;
                s_buf[304] = 0;
                s_prw[304] = 0;
            case 36:
                s_buf[303] = 0;
                s_prw[303] = 0;
                s_buf[302] = 0;
                s_prw[302] = 0;
                s_buf[301] = 0;
                s_prw[301] = 0;
                s_buf[300] = 0;
                s_prw[300] = 0;
                s_buf[299] = 0;
                s_prw[299] = 0;
                s_buf[298] = 0;
                s_prw[298] = 0;
                s_buf[297] = 0;
                s_prw[297] = 0;
                s_buf[296] = 0;
                s_prw[296] = 0;
            case 35:
                s_buf[295] = 0;
                s_prw[295] = 0;
                s_buf[294] = 0;
                s_prw[294] = 0;
                s_buf[293] = 0;
                s_prw[293] = 0;
                s_buf[292] = 0;
                s_prw[292] = 0;
                s_buf[291] = 0;
                s_prw[291] = 0;
                s_buf[290] = 0;
                s_prw[290] = 0;
                s_buf[289] = 0;
                s_prw[289] = 0;
                s_buf[288] = 0;
                s_prw[288] = 0;
            case 34:
                s_buf[287] = 0;
                s_prw[287] = 0;
                s_buf[286] = 0;
                s_prw[286] = 0;
                s_buf[285] = 0;
                s_prw[285] = 0;
                s_buf[284] = 0;
                s_prw[284] = 0;
                s_buf[283] = 0;
                s_prw[283] = 0;
                s_buf[282] = 0;
                s_prw[282] = 0;
                s_buf[281] = 0;
                s_prw[281] = 0;
                s_buf[280] = 0;
                s_prw[280] = 0;
            case 33:
                s_buf[279] = 0;
                s_prw[279] = 0;
                s_buf[278] = 0;
                s_prw[278] = 0;
                s_buf[277] = 0;
                s_prw[277] = 0;
                s_buf[276] = 0;
                s_prw[276] = 0;
                s_buf[275] = 0;
                s_prw[275] = 0;
                s_buf[274] = 0;
                s_prw[274] = 0;
                s_buf[273] = 0;
                s_prw[273] = 0;
                s_buf[272] = 0;
                s_prw[272] = 0;
            case 32:
                s_buf[271] = 0;
                s_prw[271] = 0;
                s_buf[270] = 0;
                s_prw[270] = 0;
                s_buf[269] = 0;
                s_prw[269] = 0;
                s_buf[268] = 0;
                s_prw[268] = 0;
                s_buf[267] = 0;
                s_prw[267] = 0;
                s_buf[266] = 0;
                s_prw[266] = 0;
                s_buf[265] = 0;
                s_prw[265] = 0;
                s_buf[264] = 0;
                s_prw[264] = 0;
            case 31:
                s_buf[263] = 0;
                s_prw[263] = 0;
                s_buf[262] = 0;
                s_prw[262] = 0;
                s_buf[261] = 0;
                s_prw[261] = 0;
                s_buf[260] = 0;
                s_prw[260] = 0;
                s_buf[259] = 0;
                s_prw[259] = 0;
                s_buf[258] = 0;
                s_prw[258] = 0;
                s_buf[257] = 0;
                s_prw[257] = 0;
                s_buf[256] = 0;
                s_prw[256] = 0;
            case 30:
                s_buf[255] = 0;
                s_prw[255] = 0;
                s_buf[254] = 0;
                s_prw[254] = 0;
                s_buf[253] = 0;
                s_prw[253] = 0;
                s_buf[252] = 0;
                s_prw[252] = 0;
                s_buf[251] = 0;
                s_prw[251] = 0;
                s_buf[250] = 0;
                s_prw[250] = 0;
                s_buf[249] = 0;
                s_prw[249] = 0;
                s_buf[248] = 0;
                s_prw[248] = 0;
            case 29:
                s_buf[247] = 0;
                s_prw[247] = 0;
                s_buf[246] = 0;
                s_prw[246] = 0;
                s_buf[245] = 0;
                s_prw[245] = 0;
                s_buf[244] = 0;
                s_prw[244] = 0;
                s_buf[243] = 0;
                s_prw[243] = 0;
                s_buf[242] = 0;
                s_prw[242] = 0;
                s_buf[241] = 0;
                s_prw[241] = 0;
                s_buf[240] = 0;
                s_prw[240] = 0;
            case 28:
                s_buf[239] = 0;
                s_prw[239] = 0;
                s_buf[238] = 0;
                s_prw[238] = 0;
                s_buf[237] = 0;
                s_prw[237] = 0;
                s_buf[236] = 0;
                s_prw[236] = 0;
                s_buf[235] = 0;
                s_prw[235] = 0;
                s_buf[234] = 0;
                s_prw[234] = 0;
                s_buf[233] = 0;
                s_prw[233] = 0;
                s_buf[232] = 0;
                s_prw[232] = 0;
            case 27:
                s_buf[231] = 0;
                s_prw[231] = 0;
                s_buf[230] = 0;
                s_prw[230] = 0;
                s_buf[229] = 0;
                s_prw[229] = 0;
                s_buf[228] = 0;
                s_prw[228] = 0;
                s_buf[227] = 0;
                s_prw[227] = 0;
                s_buf[226] = 0;
                s_prw[226] = 0;
                s_buf[225] = 0;
                s_prw[225] = 0;
                s_buf[224] = 0;
                s_prw[224] = 0;
            case 26:
                s_buf[223] = 0;
                s_prw[223] = 0;
                s_buf[222] = 0;
                s_prw[222] = 0;
                s_buf[221] = 0;
                s_prw[221] = 0;
                s_buf[220] = 0;
                s_prw[220] = 0;
                s_buf[219] = 0;
                s_prw[219] = 0;
                s_buf[218] = 0;
                s_prw[218] = 0;
                s_buf[217] = 0;
                s_prw[217] = 0;
                s_buf[216] = 0;
                s_prw[216] = 0;
            case 25:
                s_buf[215] = 0;
                s_prw[215] = 0;
                s_buf[214] = 0;
                s_prw[214] = 0;
                s_buf[213] = 0;
                s_prw[213] = 0;
                s_buf[212] = 0;
                s_prw[212] = 0;
                s_buf[211] = 0;
                s_prw[211] = 0;
                s_buf[210] = 0;
                s_prw[210] = 0;
                s_buf[209] = 0;
                s_prw[209] = 0;
                s_buf[208] = 0;
                s_prw[208] = 0;
            case 24:
                s_buf[207] = 0;
                s_prw[207] = 0;
                s_buf[206] = 0;
                s_prw[206] = 0;
                s_buf[205] = 0;
                s_prw[205] = 0;
                s_buf[204] = 0;
                s_prw[204] = 0;
                s_buf[203] = 0;
                s_prw[203] = 0;
                s_buf[202] = 0;
                s_prw[202] = 0;
                s_buf[201] = 0;
                s_prw[201] = 0;
                s_buf[200] = 0;
                s_prw[200] = 0;
            case 23:
                s_buf[199] = 0;
                s_prw[199] = 0;
                s_buf[198] = 0;
                s_prw[198] = 0;
                s_buf[197] = 0;
                s_prw[197] = 0;
                s_buf[196] = 0;
                s_prw[196] = 0;
                s_buf[195] = 0;
                s_prw[195] = 0;
                s_buf[194] = 0;
                s_prw[194] = 0;
                s_buf[193] = 0;
                s_prw[193] = 0;
                s_buf[192] = 0;
                s_prw[192] = 0;
            case 22:
                s_buf[191] = 0;
                s_prw[191] = 0;
                s_buf[190] = 0;
                s_prw[190] = 0;
                s_buf[189] = 0;
                s_prw[189] = 0;
                s_buf[188] = 0;
                s_prw[188] = 0;
                s_buf[187] = 0;
                s_prw[187] = 0;
                s_buf[186] = 0;
                s_prw[186] = 0;
                s_buf[185] = 0;
                s_prw[185] = 0;
                s_buf[184] = 0;
                s_prw[184] = 0;
            case 21:
                s_buf[183] = 0;
                s_prw[183] = 0;
                s_buf[182] = 0;
                s_prw[182] = 0;
                s_buf[181] = 0;
                s_prw[181] = 0;
                s_buf[180] = 0;
                s_prw[180] = 0;
                s_buf[179] = 0;
                s_prw[179] = 0;
                s_buf[178] = 0;
                s_prw[178] = 0;
                s_buf[177] = 0;
                s_prw[177] = 0;
                s_buf[176] = 0;
                s_prw[176] = 0;
            case 20:
                s_buf[175] = 0;
                s_prw[175] = 0;
                s_buf[174] = 0;
                s_prw[174] = 0;
                s_buf[173] = 0;
                s_prw[173] = 0;
                s_buf[172] = 0;
                s_prw[172] = 0;
                s_buf[171] = 0;
                s_prw[171] = 0;
                s_buf[170] = 0;
                s_prw[170] = 0;
                s_buf[169] = 0;
                s_prw[169] = 0;
                s_buf[168] = 0;
                s_prw[168] = 0;
            case 19:
                s_buf[167] = 0;
                s_prw[167] = 0;
                s_buf[166] = 0;
                s_prw[166] = 0;
                s_buf[165] = 0;
                s_prw[165] = 0;
                s_buf[164] = 0;
                s_prw[164] = 0;
                s_buf[163] = 0;
                s_prw[163] = 0;
                s_buf[162] = 0;
                s_prw[162] = 0;
                s_buf[161] = 0;
                s_prw[161] = 0;
                s_buf[160] = 0;
                s_prw[160] = 0;
            case 18:
                s_buf[159] = 0;
                s_prw[159] = 0;
                s_buf[158] = 0;
                s_prw[158] = 0;
                s_buf[157] = 0;
                s_prw[157] = 0;
                s_buf[156] = 0;
                s_prw[156] = 0;
                s_buf[155] = 0;
                s_prw[155] = 0;
                s_buf[154] = 0;
                s_prw[154] = 0;
                s_buf[153] = 0;
                s_prw[153] = 0;
                s_buf[152] = 0;
                s_prw[152] = 0;
            case 17:
                s_buf[151] = 0;
                s_prw[151] = 0;
                s_buf[150] = 0;
                s_prw[150] = 0;
                s_buf[149] = 0;
                s_prw[149] = 0;
                s_buf[148] = 0;
                s_prw[148] = 0;
                s_buf[147] = 0;
                s_prw[147] = 0;
                s_buf[146] = 0;
                s_prw[146] = 0;
                s_buf[145] = 0;
                s_prw[145] = 0;
                s_buf[144] = 0;
                s_prw[144] = 0;
            case 16:
                s_buf[143] = 0;
                s_prw[143] = 0;
                s_buf[142] = 0;
                s_prw[142] = 0;
                s_buf[141] = 0;
                s_prw[141] = 0;
                s_buf[140] = 0;
                s_prw[140] = 0;
                s_buf[139] = 0;
                s_prw[139] = 0;
                s_buf[138] = 0;
                s_prw[138] = 0;
                s_buf[137] = 0;
                s_prw[137] = 0;
                s_buf[136] = 0;
                s_prw[136] = 0;
            case 15:
                s_buf[135] = 0;
                s_prw[135] = 0;
                s_buf[134] = 0;
                s_prw[134] = 0;
                s_buf[133] = 0;
                s_prw[133] = 0;
                s_buf[132] = 0;
                s_prw[132] = 0;
                s_buf[131] = 0;
                s_prw[131] = 0;
                s_buf[130] = 0;
                s_prw[130] = 0;
                s_buf[129] = 0;
                s_prw[129] = 0;
                s_buf[128] = 0;
                s_prw[128] = 0;
            case 14:
                s_buf[127] = 0;
                s_prw[127] = 0;
                s_buf[126] = 0;
                s_prw[126] = 0;
                s_buf[125] = 0;
                s_prw[125] = 0;
                s_buf[124] = 0;
                s_prw[124] = 0;
                s_buf[123] = 0;
                s_prw[123] = 0;
                s_buf[122] = 0;
                s_prw[122] = 0;
                s_buf[121] = 0;
                s_prw[121] = 0;
                s_buf[120] = 0;
                s_prw[120] = 0;
            case 13:
                s_buf[119] = 0;
                s_prw[119] = 0;
                s_buf[118] = 0;
                s_prw[118] = 0;
                s_buf[117] = 0;
                s_prw[117] = 0;
                s_buf[116] = 0;
                s_prw[116] = 0;
                s_buf[115] = 0;
                s_prw[115] = 0;
                s_buf[114] = 0;
                s_prw[114] = 0;
                s_buf[113] = 0;
                s_prw[113] = 0;
                s_buf[112] = 0;
                s_prw[112] = 0;
            case 12:
                s_buf[111] = 0;
                s_prw[111] = 0;
                s_buf[110] = 0;
                s_prw[110] = 0;
                s_buf[109] = 0;
                s_prw[109] = 0;
                s_buf[108] = 0;
                s_prw[108] = 0;
                s_buf[107] = 0;
                s_prw[107] = 0;
                s_buf[106] = 0;
                s_prw[106] = 0;
                s_buf[105] = 0;
                s_prw[105] = 0;
                s_buf[104] = 0;
                s_prw[104] = 0;
            case 11:
                s_buf[103] = 0;
                s_prw[103] = 0;
                s_buf[102] = 0;
                s_prw[102] = 0;
                s_buf[101] = 0;
                s_prw[101] = 0;
                s_buf[100] = 0;
                s_prw[100] = 0;
                s_buf[99] = 0;
                s_prw[99] = 0;
                s_buf[98] = 0;
                s_prw[98] = 0;
                s_buf[97] = 0;
                s_prw[97] = 0;
                s_buf[96] = 0;
                s_prw[96] = 0;
            case 10:
                s_buf[95] = 0;
                s_prw[95] = 0;
                s_buf[94] = 0;
                s_prw[94] = 0;
                s_buf[93] = 0;
                s_prw[93] = 0;
                s_buf[92] = 0;
                s_prw[92] = 0;
                s_buf[91] = 0;
                s_prw[91] = 0;
                s_buf[90] = 0;
                s_prw[90] = 0;
                s_buf[89] = 0;
                s_prw[89] = 0;
                s_buf[88] = 0;
                s_prw[88] = 0;
            case 9:
                s_buf[87] = 0;
                s_prw[87] = 0;
                s_buf[86] = 0;
                s_prw[86] = 0;
                s_buf[85] = 0;
                s_prw[85] = 0;
                s_buf[84] = 0;
                s_prw[84] = 0;
                s_buf[83] = 0;
                s_prw[83] = 0;
                s_buf[82] = 0;
                s_prw[82] = 0;
                s_buf[81] = 0;
                s_prw[81] = 0;
                s_buf[80] = 0;
                s_prw[80] = 0;
            case 8:
                s_buf[79] = 0;
                s_prw[79] = 0;
                s_buf[78] = 0;
                s_prw[78] = 0;
                s_buf[77] = 0;
                s_prw[77] = 0;
                s_buf[76] = 0;
                s_prw[76] = 0;
                s_buf[75] = 0;
                s_prw[75] = 0;
                s_buf[74] = 0;
                s_prw[74] = 0;
                s_buf[73] = 0;
                s_prw[73] = 0;
                s_buf[72] = 0;
                s_prw[72] = 0;
            case 7:
                s_buf[71] = 0;
                s_prw[71] = 0;
                s_buf[70] = 0;
                s_prw[70] = 0;
                s_buf[69] = 0;
                s_prw[69] = 0;
                s_buf[68] = 0;
                s_prw[68] = 0;
                s_buf[67] = 0;
                s_prw[67] = 0;
                s_buf[66] = 0;
                s_prw[66] = 0;
                s_buf[65] = 0;
                s_prw[65] = 0;
                s_buf[64] = 0;
                s_prw[64] = 0;
            case 6:
                s_buf[63] = 0;
                s_prw[63] = 0;
                s_buf[62] = 0;
                s_prw[62] = 0;
                s_buf[61] = 0;
                s_prw[61] = 0;
                s_buf[60] = 0;
                s_prw[60] = 0;
                s_buf[59] = 0;
                s_prw[59] = 0;
                s_buf[58] = 0;
                s_prw[58] = 0;
                s_buf[57] = 0;
                s_prw[57] = 0;
                s_buf[56] = 0;
                s_prw[56] = 0;
            case 5:
                s_buf[55] = 0;
                s_prw[55] = 0;
                s_buf[54] = 0;
                s_prw[54] = 0;
                s_buf[53] = 0;
                s_prw[53] = 0;
                s_buf[52] = 0;
                s_prw[52] = 0;
                s_buf[51] = 0;
                s_prw[51] = 0;
                s_buf[50] = 0;
                s_prw[50] = 0;
                s_buf[49] = 0;
                s_prw[49] = 0;
                s_buf[48] = 0;
                s_prw[48] = 0;
            case 4:
                s_buf[47] = 0;
                s_prw[47] = 0;
                s_buf[46] = 0;
                s_prw[46] = 0;
                s_buf[45] = 0;
                s_prw[45] = 0;
                s_buf[44] = 0;
                s_prw[44] = 0;
                s_buf[43] = 0;
                s_prw[43] = 0;
                s_buf[42] = 0;
                s_prw[42] = 0;
                s_buf[41] = 0;
                s_prw[41] = 0;
                s_buf[40] = 0;
                s_prw[40] = 0;
            case 3:
                s_buf[39] = 0;
                s_prw[39] = 0;
                s_buf[38] = 0;
                s_prw[38] = 0;
                s_buf[37] = 0;
                s_prw[37] = 0;
                s_buf[36] = 0;
                s_prw[36] = 0;
                s_buf[35] = 0;
                s_prw[35] = 0;
                s_buf[34] = 0;
                s_prw[34] = 0;
                s_buf[33] = 0;
                s_prw[33] = 0;
                s_buf[32] = 0;
                s_prw[32] = 0;
            case 2:
                s_buf[31] = 0;
                s_prw[31] = 0;
                s_buf[30] = 0;
                s_prw[30] = 0;
                s_buf[29] = 0;
                s_prw[29] = 0;
                s_buf[28] = 0;
                s_prw[28] = 0;
                s_buf[27] = 0;
                s_prw[27] = 0;
                s_buf[26] = 0;
                s_prw[26] = 0;
                s_buf[25] = 0;
                s_prw[25] = 0;
                s_buf[24] = 0;
                s_prw[24] = 0;
            case 1:
                s_buf[23] = 0;
                s_prw[23] = 0;
                s_buf[22] = 0;
                s_prw[22] = 0;
                s_buf[21] = 0;
                s_prw[21] = 0;
                s_buf[20] = 0;
                s_prw[20] = 0;
                s_buf[19] = 0;
                s_prw[19] = 0;
                s_buf[18] = 0;
                s_prw[18] = 0;
                s_buf[17] = 0;
                s_prw[17] = 0;
                s_buf[16] = 0;
                s_prw[16] = 0;
            }
            sprite_existence = false;
        }
        src -= crtc.r06 - sprite_screen.r7;
        if (src < 0 || src > 1023) {
            return;
        }
        if ((sprite_screen.r8 & 3) == 0) {
            if ((sprite_screen.r4 & 8) != 0) {
                short tnum[], tcol[];
                boolean th[], tv[];
                if ((sprite_screen.r4 & 48) == 0) {
                    tnum = sprite_screen.t0num;
                    tcol = sprite_screen.t0col;
                    th = sprite_screen.t0h;
                    tv = sprite_screen.t0v;
                } else {
                    tnum = sprite_screen.t1num;
                    tcol = sprite_screen.t1col;
                    th = sprite_screen.t1h;
                    tv = sprite_screen.t1v;
                }
                int y = sprite_screen.r3 + src & 511;
                int by = y & 7;
                int sy = y >> 3 << 6;
                int sx = sprite_screen.r2 >> 3 & 63;
                int x = 16 - (sprite_screen.r2 & 7);
                while (x < 16 + dw) {
                    int col = tcol[sy + sx];
                    int t = pat[tnum[sy + sx] + (tv[sy + sx] ? 7 - by : by)];
                    if (t != 0) {
                        sprite_existence = true;
                        if (th[sy + sx]) {
                            if ((t & -268435456) != 0) {
                                s_buf[x + 7] = col + (t >> 28 & 15);
                                s_prw[x + 7] = 1;
                            }
                            if ((t & 251658240) != 0) {
                                s_buf[x + 6] = col + (t >> 24 & 15);
                                s_prw[x + 6] = 1;
                            }
                            if ((t & 15728640) != 0) {
                                s_buf[x + 5] = col + (t >> 20 & 15);
                                s_prw[x + 5] = 1;
                            }
                            if ((t & 983040) != 0) {
                                s_buf[x + 4] = col + (t >> 16 & 15);
                                s_prw[x + 4] = 1;
                            }
                            if ((t & 61440) != 0) {
                                s_buf[x + 3] = col + (t >> 12 & 15);
                                s_prw[x + 3] = 1;
                            }
                            if ((t & 3840) != 0) {
                                s_buf[x + 2] = col + (t >> 8 & 15);
                                s_prw[x + 2] = 1;
                            }
                            if ((t & 240) != 0) {
                                s_buf[x + 1] = col + (t >> 4 & 15);
                                s_prw[x + 1] = 1;
                            }
                            if ((t & 15) != 0) {
                                s_buf[x] = col + (t & 15);
                                s_prw[x] = 1;
                            }
                        } else {
                            if ((t & -268435456) != 0) {
                                s_buf[x] = col + (t >> 28 & 15);
                                s_prw[x] = 1;
                            }
                            if ((t & 251658240) != 0) {
                                s_buf[x + 1] = col + (t >> 24 & 15);
                                s_prw[x + 1] = 1;
                            }
                            if ((t & 15728640) != 0) {
                                s_buf[x + 2] = col + (t >> 20 & 15);
                                s_prw[x + 2] = 1;
                            }
                            if ((t & 983040) != 0) {
                                s_buf[x + 3] = col + (t >> 16 & 15);
                                s_prw[x + 3] = 1;
                            }
                            if ((t & 61440) != 0) {
                                s_buf[x + 4] = col + (t >> 12 & 15);
                                s_prw[x + 4] = 1;
                            }
                            if ((t & 3840) != 0) {
                                s_buf[x + 5] = col + (t >> 8 & 15);
                                s_prw[x + 5] = 1;
                            }
                            if ((t & 240) != 0) {
                                s_buf[x + 6] = col + (t >> 4 & 15);
                                s_prw[x + 6] = 1;
                            }
                            if ((t & 15) != 0) {
                                s_buf[x + 7] = col + (t & 15);
                                s_prw[x + 7] = 1;
                            }
                        }
                    }
                    x += 8;
                    sx = sx + 1 & 63;
                }
            }
            if ((sprite_screen.r4 & 1) != 0) {
                short tnum[], tcol[];
                boolean th[], tv[];
                if ((sprite_screen.r4 & 6) == 0) {
                    tnum = sprite_screen.t0num;
                    tcol = sprite_screen.t0col;
                    th = sprite_screen.t0h;
                    tv = sprite_screen.t0v;
                } else {
                    tnum = sprite_screen.t1num;
                    tcol = sprite_screen.t1col;
                    th = sprite_screen.t1h;
                    tv = sprite_screen.t1v;
                }
                int y = sprite_screen.r1 + src & 511;
                int by = y & 7;
                int sy = y >> 3 << 6;
                int sx = sprite_screen.r0 >> 3 & 63;
                int x = 16 - (sprite_screen.r0 & 7);
                while (x < 16 + dw) {
                    int col = tcol[sy + sx];
                    int t = pat[tnum[sy + sx] + (tv[sy + sx] ? 7 - by : by)];
                    if (t != 0) {
                        sprite_existence = true;
                        if (th[sy + sx]) {
                            if ((t & -268435456) != 0) {
                                s_buf[x + 7] = col + (t >> 28 & 15);
                                s_prw[x + 7] = 2;
                            }
                            if ((t & 251658240) != 0) {
                                s_buf[x + 6] = col + (t >> 24 & 15);
                                s_prw[x + 6] = 2;
                            }
                            if ((t & 15728640) != 0) {
                                s_buf[x + 5] = col + (t >> 20 & 15);
                                s_prw[x + 5] = 2;
                            }
                            if ((t & 983040) != 0) {
                                s_buf[x + 4] = col + (t >> 16 & 15);
                                s_prw[x + 4] = 2;
                            }
                            if ((t & 61440) != 0) {
                                s_buf[x + 3] = col + (t >> 12 & 15);
                                s_prw[x + 3] = 2;
                            }
                            if ((t & 3840) != 0) {
                                s_buf[x + 2] = col + (t >> 8 & 15);
                                s_prw[x + 2] = 2;
                            }
                            if ((t & 240) != 0) {
                                s_buf[x + 1] = col + (t >> 4 & 15);
                                s_prw[x + 1] = 2;
                            }
                            if ((t & 15) != 0) {
                                s_buf[x] = col + (t & 15);
                                s_prw[x] = 2;
                            }
                        } else {
                            if ((t & -268435456) != 0) {
                                s_buf[x] = col + (t >> 28 & 15);
                                s_prw[x] = 2;
                            }
                            if ((t & 251658240) != 0) {
                                s_buf[x + 1] = col + (t >> 24 & 15);
                                s_prw[x + 1] = 2;
                            }
                            if ((t & 15728640) != 0) {
                                s_buf[x + 2] = col + (t >> 20 & 15);
                                s_prw[x + 2] = 2;
                            }
                            if ((t & 983040) != 0) {
                                s_buf[x + 3] = col + (t >> 16 & 15);
                                s_prw[x + 3] = 2;
                            }
                            if ((t & 61440) != 0) {
                                s_buf[x + 4] = col + (t >> 12 & 15);
                                s_prw[x + 4] = 2;
                            }
                            if ((t & 3840) != 0) {
                                s_buf[x + 5] = col + (t >> 8 & 15);
                                s_prw[x + 5] = 2;
                            }
                            if ((t & 240) != 0) {
                                s_buf[x + 6] = col + (t >> 4 & 15);
                                s_prw[x + 6] = 2;
                            }
                            if ((t & 15) != 0) {
                                s_buf[x + 7] = col + (t & 15);
                                s_prw[x + 7] = 2;
                            }
                        }
                    }
                    x += 8;
                    sx = sx + 1 & 63;
                }
            }
        } else {
            if ((sprite_screen.r4 & 1) != 0) {
                short tnum[], tcol[];
                boolean th[], tv[];
                if ((sprite_screen.r4 & 6) == 0) {
                    tnum = sprite_screen.t0num;
                    tcol = sprite_screen.t0col;
                    th = sprite_screen.t0h;
                    tv = sprite_screen.t0v;
                } else {
                    tnum = sprite_screen.t1num;
                    tcol = sprite_screen.t1col;
                    th = sprite_screen.t1h;
                    tv = sprite_screen.t1v;
                }
                int y = sprite_screen.r1 + src & 1023;
                int by = y & 15;
                int sy = y >> 4 << 6;
                int sx = sprite_screen.r0 >> 4 & 63;
                int x = 16 - (sprite_screen.r0 & 15);
                while (x < 16 + dw) {
                    int col = tcol[sy + sx];
                    int a = (tnum[sy + sx] << 2) + (tv[sy + sx] ? 15 - by : by);
                    if (th[sy + sx]) {
                        int t;
                        if ((t = pat[a]) != 0) {
                            sprite_existence = true;
                            if ((t & -268435456) != 0) {
                                s_buf[x + 15] = col + (t >> 28 & 15);
                                s_prw[x + 15] = 2;
                            }
                            if ((t & 251658240) != 0) {
                                s_buf[x + 14] = col + (t >> 24 & 15);
                                s_prw[x + 14] = 2;
                            }
                            if ((t & 15728640) != 0) {
                                s_buf[x + 13] = col + (t >> 20 & 15);
                                s_prw[x + 13] = 2;
                            }
                            if ((t & 983040) != 0) {
                                s_buf[x + 12] = col + (t >> 16 & 15);
                                s_prw[x + 12] = 2;
                            }
                            if ((t & 61440) != 0) {
                                s_buf[x + 11] = col + (t >> 12 & 15);
                                s_prw[x + 11] = 2;
                            }
                            if ((t & 3840) != 0) {
                                s_buf[x + 10] = col + (t >> 8 & 15);
                                s_prw[x + 10] = 2;
                            }
                            if ((t & 240) != 0) {
                                s_buf[x + 9] = col + (t >> 4 & 15);
                                s_prw[x + 9] = 2;
                            }
                            if ((t & 15) != 0) {
                                s_buf[x + 8] = col + (t & 15);
                                s_prw[x + 8] = 2;
                            }
                        }
                        if ((t = pat[a + 16]) != 0) {
                            sprite_existence = true;
                            if ((t & -268435456) != 0) {
                                s_buf[x + 7] = col + (t >> 28 & 15);
                                s_prw[x + 7] = 2;
                            }
                            if ((t & 251658240) != 0) {
                                s_buf[x + 6] = col + (t >> 24 & 15);
                                s_prw[x + 6] = 2;
                            }
                            if ((t & 15728640) != 0) {
                                s_buf[x + 5] = col + (t >> 20 & 15);
                                s_prw[x + 5] = 2;
                            }
                            if ((t & 983040) != 0) {
                                s_buf[x + 4] = col + (t >> 16 & 15);
                                s_prw[x + 4] = 2;
                            }
                            if ((t & 61440) != 0) {
                                s_buf[x + 3] = col + (t >> 12 & 15);
                                s_prw[x + 3] = 2;
                            }
                            if ((t & 3840) != 0) {
                                s_buf[x + 2] = col + (t >> 8 & 15);
                                s_prw[x + 2] = 2;
                            }
                            if ((t & 240) != 0) {
                                s_buf[x + 1] = col + (t >> 4 & 15);
                                s_prw[x + 1] = 2;
                            }
                            if ((t & 15) != 0) {
                                s_buf[x] = col + (t & 15);
                                s_prw[x] = 2;
                            }
                        }
                    } else {
                        int t;
                        if ((t = pat[a]) != 0) {
                            sprite_existence = true;
                            if ((t & -268435456) != 0) {
                                s_buf[x] = col + (t >> 28 & 15);
                                s_prw[x] = 2;
                            }
                            if ((t & 251658240) != 0) {
                                s_buf[x + 1] = col + (t >> 24 & 15);
                                s_prw[x + 1] = 2;
                            }
                            if ((t & 15728640) != 0) {
                                s_buf[x + 2] = col + (t >> 20 & 15);
                                s_prw[x + 2] = 2;
                            }
                            if ((t & 983040) != 0) {
                                s_buf[x + 3] = col + (t >> 16 & 15);
                                s_prw[x + 3] = 2;
                            }
                            if ((t & 61440) != 0) {
                                s_buf[x + 4] = col + (t >> 12 & 15);
                                s_prw[x + 4] = 2;
                            }
                            if ((t & 3840) != 0) {
                                s_buf[x + 5] = col + (t >> 8 & 15);
                                s_prw[x + 5] = 2;
                            }
                            if ((t & 240) != 0) {
                                s_buf[x + 6] = col + (t >> 4 & 15);
                                s_prw[x + 6] = 2;
                            }
                            if ((t & 15) != 0) {
                                s_buf[x + 7] = col + (t & 15);
                                s_prw[x + 7] = 2;
                            }
                        }
                        if ((t = pat[a + 16]) != 0) {
                            sprite_existence = true;
                            if ((t & -268435456) != 0) {
                                s_buf[x + 8] = col + (t >> 28 & 15);
                                s_prw[x + 8] = 2;
                            }
                            if ((t & 251658240) != 0) {
                                s_buf[x + 9] = col + (t >> 24 & 15);
                                s_prw[x + 9] = 2;
                            }
                            if ((t & 15728640) != 0) {
                                s_buf[x + 10] = col + (t >> 20 & 15);
                                s_prw[x + 10] = 2;
                            }
                            if ((t & 983040) != 0) {
                                s_buf[x + 11] = col + (t >> 16 & 15);
                                s_prw[x + 11] = 2;
                            }
                            if ((t & 61440) != 0) {
                                s_buf[x + 12] = col + (t >> 12 & 15);
                                s_prw[x + 12] = 2;
                            }
                            if ((t & 3840) != 0) {
                                s_buf[x + 13] = col + (t >> 8 & 15);
                                s_prw[x + 13] = 2;
                            }
                            if ((t & 240) != 0) {
                                s_buf[x + 14] = col + (t >> 4 & 15);
                                s_prw[x + 14] = 2;
                            }
                            if ((t & 15) != 0) {
                                s_buf[x + 15] = col + (t & 15);
                                s_prw[x + 15] = 2;
                            }
                        }
                    }
                    x += 16;
                    sx = sx + 1 & 63;
                }
            }
        }
        int cnt = 0;
        for (int i = 0; i <= 3; i++) {
            int map = sprite_screen.rmap[i][16 + src];
            if ((map & 1) != 0) {
                s_n[cnt++] = (short) (i << 5);
            }
            if ((map & 2) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 1);
            }
            if ((map & 4) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 2);
            }
            if ((map & 8) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 3);
            }
            if ((map & 16) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 4);
            }
            if ((map & 32) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 5);
            }
            if ((map & 64) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 6);
            }
            if ((map & 128) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 7);
            }
            if ((map & 256) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 8);
            }
            if ((map & 512) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 9);
            }
            if ((map & 1024) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 10);
            }
            if ((map & 2048) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 11);
            }
            if ((map & 4096) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 12);
            }
            if ((map & 8192) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 13);
            }
            if ((map & 16384) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 14);
            }
            if ((map & 32768) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 15);
            }
            if ((map & 65536) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 16);
            }
            if ((map & 131072) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 17);
            }
            if ((map & 262144) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 18);
            }
            if ((map & 524288) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 19);
            }
            if ((map & 1048576) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 20);
            }
            if ((map & 2097152) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 21);
            }
            if ((map & 4194304) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 22);
            }
            if ((map & 8388608) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 23);
            }
            if ((map & 16777216) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 24);
            }
            if ((map & 33554432) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 25);
            }
            if ((map & 67108864) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 26);
            }
            if ((map & 134217728) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 27);
            }
            if ((map & 268435456) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 28);
            }
            if ((map & 536870912) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 29);
            }
            if ((map & 1073741824) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 30);
            }
            if ((map & -2147483648) != 0) {
                s_n[cnt++] = (short) ((i << 5) + 31);
            }
        }
        if (cnt != 0) {
            sprite_existence = true;
            if (cnt > 32) {
                cnt = 32;
            }
            while (cnt > 0) {
                int n = s_n[--cnt];
                short x = sprite_screen.sx[n];
                if (x == 0 || x >= 16 + dw) {
                    continue;
                }
                short col = (short) sprite_screen.scol[n];
                byte prw = sprite_screen.sprw[n];
                int a = (sprite_screen.snum[n] << 5) + (sprite_screen.sv[n] ? sprite_screen.sy[n] - src - 1 : 16 + src - sprite_screen.sy[n]);
                if (sprite_screen.sh[n]) {
                    int t;
                    if ((t = pat[a]) != 0) {
                        if (s_prw[x + 15] < prw && (t & -268435456) != 0) {
                            s_buf[x + 15] = col + (t >> 28 & 15);
                        }
                        if (s_prw[x + 14] < prw && (t & 251658240) != 0) {
                            s_buf[x + 14] = col + (t >> 24 & 15);
                        }
                        if (s_prw[x + 13] < prw && (t & 15728640) != 0) {
                            s_buf[x + 13] = col + (t >> 20 & 15);
                        }
                        if (s_prw[x + 12] < prw && (t & 983040) != 0) {
                            s_buf[x + 12] = col + (t >> 16 & 15);
                        }
                        if (s_prw[x + 11] < prw && (t & 61440) != 0) {
                            s_buf[x + 11] = col + (t >> 12 & 15);
                        }
                        if (s_prw[x + 10] < prw && (t & 3840) != 0) {
                            s_buf[x + 10] = col + (t >> 8 & 15);
                        }
                        if (s_prw[x + 9] < prw && (t & 240) != 0) {
                            s_buf[x + 9] = col + (t >> 4 & 15);
                        }
                        if (s_prw[x + 8] < prw && (t & 15) != 0) {
                            s_buf[x + 8] = col + (t & 15);
                        }
                    }
                    if ((t = pat[a + 16]) != 0) {
                        if (s_prw[x + 7] < prw && (t & -268435456) != 0) {
                            s_buf[x + 7] = col + (t >> 28 & 15);
                        }
                        if (s_prw[x + 6] < prw && (t & 251658240) != 0) {
                            s_buf[x + 6] = col + (t >> 24 & 15);
                        }
                        if (s_prw[x + 5] < prw && (t & 15728640) != 0) {
                            s_buf[x + 5] = col + (t >> 20 & 15);
                        }
                        if (s_prw[x + 4] < prw && (t & 983040) != 0) {
                            s_buf[x + 4] = col + (t >> 16 & 15);
                        }
                        if (s_prw[x + 3] < prw && (t & 61440) != 0) {
                            s_buf[x + 3] = col + (t >> 12 & 15);
                        }
                        if (s_prw[x + 2] < prw && (t & 3840) != 0) {
                            s_buf[x + 2] = col + (t >> 8 & 15);
                        }
                        if (s_prw[x + 1] < prw && (t & 240) != 0) {
                            s_buf[x + 1] = col + (t >> 4 & 15);
                        }
                        if (s_prw[x] < prw && (t & 15) != 0) {
                            s_buf[x] = col + (t & 15);
                        }
                    }
                } else {
                    int t;
                    if ((t = pat[a]) != 0) {
                        if (s_prw[x] < prw && (t & -268435456) != 0) {
                            s_buf[x] = col + (t >> 28 & 15);
                        }
                        if (s_prw[x + 1] < prw && (t & 251658240) != 0) {
                            s_buf[x + 1] = col + (t >> 24 & 15);
                        }
                        if (s_prw[x + 2] < prw && (t & 15728640) != 0) {
                            s_buf[x + 2] = col + (t >> 20 & 15);
                        }
                        if (s_prw[x + 3] < prw && (t & 983040) != 0) {
                            s_buf[x + 3] = col + (t >> 16 & 15);
                        }
                        if (s_prw[x + 4] < prw && (t & 61440) != 0) {
                            s_buf[x + 4] = col + (t >> 12 & 15);
                        }
                        if (s_prw[x + 5] < prw && (t & 3840) != 0) {
                            s_buf[x + 5] = col + (t >> 8 & 15);
                        }
                        if (s_prw[x + 6] < prw && (t & 240) != 0) {
                            s_buf[x + 6] = col + (t >> 4 & 15);
                        }
                        if (s_prw[x + 7] < prw && (t & 15) != 0) {
                            s_buf[x + 7] = col + (t & 15);
                        }
                    }
                    if ((t = pat[a + 16]) != 0) {
                        if (s_prw[x + 8] < prw && (t & -268435456) != 0) {
                            s_buf[x + 8] = col + (t >> 28 & 15);
                        }
                        if (s_prw[x + 9] < prw && (t & 251658240) != 0) {
                            s_buf[x + 9] = col + (t >> 24 & 15);
                        }
                        if (s_prw[x + 10] < prw && (t & 15728640) != 0) {
                            s_buf[x + 10] = col + (t >> 20 & 15);
                        }
                        if (s_prw[x + 11] < prw && (t & 983040) != 0) {
                            s_buf[x + 11] = col + (t >> 16 & 15);
                        }
                        if (s_prw[x + 12] < prw && (t & 61440) != 0) {
                            s_buf[x + 12] = col + (t >> 12 & 15);
                        }
                        if (s_prw[x + 13] < prw && (t & 3840) != 0) {
                            s_buf[x + 13] = col + (t >> 8 & 15);
                        }
                        if (s_prw[x + 14] < prw && (t & 240) != 0) {
                            s_buf[x + 14] = col + (t >> 4 & 15);
                        }
                        if (s_prw[x + 15] < prw && (t & 15) != 0) {
                            s_buf[x + 15] = col + (t & 15);
                        }
                    }
                }
            }
        }
        if (sprite_existence) {
            switch (dw >> 3) {
            case 128:
                s_buf[1039] = palet16ts[s_buf[1039]];
                s_buf[1038] = palet16ts[s_buf[1038]];
                s_buf[1037] = palet16ts[s_buf[1037]];
                s_buf[1036] = palet16ts[s_buf[1036]];
                s_buf[1035] = palet16ts[s_buf[1035]];
                s_buf[1034] = palet16ts[s_buf[1034]];
                s_buf[1033] = palet16ts[s_buf[1033]];
                s_buf[1032] = palet16ts[s_buf[1032]];
            case 127:
                s_buf[1031] = palet16ts[s_buf[1031]];
                s_buf[1030] = palet16ts[s_buf[1030]];
                s_buf[1029] = palet16ts[s_buf[1029]];
                s_buf[1028] = palet16ts[s_buf[1028]];
                s_buf[1027] = palet16ts[s_buf[1027]];
                s_buf[1026] = palet16ts[s_buf[1026]];
                s_buf[1025] = palet16ts[s_buf[1025]];
                s_buf[1024] = palet16ts[s_buf[1024]];
            case 126:
                s_buf[1023] = palet16ts[s_buf[1023]];
                s_buf[1022] = palet16ts[s_buf[1022]];
                s_buf[1021] = palet16ts[s_buf[1021]];
                s_buf[1020] = palet16ts[s_buf[1020]];
                s_buf[1019] = palet16ts[s_buf[1019]];
                s_buf[1018] = palet16ts[s_buf[1018]];
                s_buf[1017] = palet16ts[s_buf[1017]];
                s_buf[1016] = palet16ts[s_buf[1016]];
            case 125:
                s_buf[1015] = palet16ts[s_buf[1015]];
                s_buf[1014] = palet16ts[s_buf[1014]];
                s_buf[1013] = palet16ts[s_buf[1013]];
                s_buf[1012] = palet16ts[s_buf[1012]];
                s_buf[1011] = palet16ts[s_buf[1011]];
                s_buf[1010] = palet16ts[s_buf[1010]];
                s_buf[1009] = palet16ts[s_buf[1009]];
                s_buf[1008] = palet16ts[s_buf[1008]];
            case 124:
                s_buf[1007] = palet16ts[s_buf[1007]];
                s_buf[1006] = palet16ts[s_buf[1006]];
                s_buf[1005] = palet16ts[s_buf[1005]];
                s_buf[1004] = palet16ts[s_buf[1004]];
                s_buf[1003] = palet16ts[s_buf[1003]];
                s_buf[1002] = palet16ts[s_buf[1002]];
                s_buf[1001] = palet16ts[s_buf[1001]];
                s_buf[1000] = palet16ts[s_buf[1000]];
            case 123:
                s_buf[999] = palet16ts[s_buf[999]];
                s_buf[998] = palet16ts[s_buf[998]];
                s_buf[997] = palet16ts[s_buf[997]];
                s_buf[996] = palet16ts[s_buf[996]];
                s_buf[995] = palet16ts[s_buf[995]];
                s_buf[994] = palet16ts[s_buf[994]];
                s_buf[993] = palet16ts[s_buf[993]];
                s_buf[992] = palet16ts[s_buf[992]];
            case 122:
                s_buf[991] = palet16ts[s_buf[991]];
                s_buf[990] = palet16ts[s_buf[990]];
                s_buf[989] = palet16ts[s_buf[989]];
                s_buf[988] = palet16ts[s_buf[988]];
                s_buf[987] = palet16ts[s_buf[987]];
                s_buf[986] = palet16ts[s_buf[986]];
                s_buf[985] = palet16ts[s_buf[985]];
                s_buf[984] = palet16ts[s_buf[984]];
            case 121:
                s_buf[983] = palet16ts[s_buf[983]];
                s_buf[982] = palet16ts[s_buf[982]];
                s_buf[981] = palet16ts[s_buf[981]];
                s_buf[980] = palet16ts[s_buf[980]];
                s_buf[979] = palet16ts[s_buf[979]];
                s_buf[978] = palet16ts[s_buf[978]];
                s_buf[977] = palet16ts[s_buf[977]];
                s_buf[976] = palet16ts[s_buf[976]];
            case 120:
                s_buf[975] = palet16ts[s_buf[975]];
                s_buf[974] = palet16ts[s_buf[974]];
                s_buf[973] = palet16ts[s_buf[973]];
                s_buf[972] = palet16ts[s_buf[972]];
                s_buf[971] = palet16ts[s_buf[971]];
                s_buf[970] = palet16ts[s_buf[970]];
                s_buf[969] = palet16ts[s_buf[969]];
                s_buf[968] = palet16ts[s_buf[968]];
            case 119:
                s_buf[967] = palet16ts[s_buf[967]];
                s_buf[966] = palet16ts[s_buf[966]];
                s_buf[965] = palet16ts[s_buf[965]];
                s_buf[964] = palet16ts[s_buf[964]];
                s_buf[963] = palet16ts[s_buf[963]];
                s_buf[962] = palet16ts[s_buf[962]];
                s_buf[961] = palet16ts[s_buf[961]];
                s_buf[960] = palet16ts[s_buf[960]];
            case 118:
                s_buf[959] = palet16ts[s_buf[959]];
                s_buf[958] = palet16ts[s_buf[958]];
                s_buf[957] = palet16ts[s_buf[957]];
                s_buf[956] = palet16ts[s_buf[956]];
                s_buf[955] = palet16ts[s_buf[955]];
                s_buf[954] = palet16ts[s_buf[954]];
                s_buf[953] = palet16ts[s_buf[953]];
                s_buf[952] = palet16ts[s_buf[952]];
            case 117:
                s_buf[951] = palet16ts[s_buf[951]];
                s_buf[950] = palet16ts[s_buf[950]];
                s_buf[949] = palet16ts[s_buf[949]];
                s_buf[948] = palet16ts[s_buf[948]];
                s_buf[947] = palet16ts[s_buf[947]];
                s_buf[946] = palet16ts[s_buf[946]];
                s_buf[945] = palet16ts[s_buf[945]];
                s_buf[944] = palet16ts[s_buf[944]];
            case 116:
                s_buf[943] = palet16ts[s_buf[943]];
                s_buf[942] = palet16ts[s_buf[942]];
                s_buf[941] = palet16ts[s_buf[941]];
                s_buf[940] = palet16ts[s_buf[940]];
                s_buf[939] = palet16ts[s_buf[939]];
                s_buf[938] = palet16ts[s_buf[938]];
                s_buf[937] = palet16ts[s_buf[937]];
                s_buf[936] = palet16ts[s_buf[936]];
            case 115:
                s_buf[935] = palet16ts[s_buf[935]];
                s_buf[934] = palet16ts[s_buf[934]];
                s_buf[933] = palet16ts[s_buf[933]];
                s_buf[932] = palet16ts[s_buf[932]];
                s_buf[931] = palet16ts[s_buf[931]];
                s_buf[930] = palet16ts[s_buf[930]];
                s_buf[929] = palet16ts[s_buf[929]];
                s_buf[928] = palet16ts[s_buf[928]];
            case 114:
                s_buf[927] = palet16ts[s_buf[927]];
                s_buf[926] = palet16ts[s_buf[926]];
                s_buf[925] = palet16ts[s_buf[925]];
                s_buf[924] = palet16ts[s_buf[924]];
                s_buf[923] = palet16ts[s_buf[923]];
                s_buf[922] = palet16ts[s_buf[922]];
                s_buf[921] = palet16ts[s_buf[921]];
                s_buf[920] = palet16ts[s_buf[920]];
            case 113:
                s_buf[919] = palet16ts[s_buf[919]];
                s_buf[918] = palet16ts[s_buf[918]];
                s_buf[917] = palet16ts[s_buf[917]];
                s_buf[916] = palet16ts[s_buf[916]];
                s_buf[915] = palet16ts[s_buf[915]];
                s_buf[914] = palet16ts[s_buf[914]];
                s_buf[913] = palet16ts[s_buf[913]];
                s_buf[912] = palet16ts[s_buf[912]];
            case 112:
                s_buf[911] = palet16ts[s_buf[911]];
                s_buf[910] = palet16ts[s_buf[910]];
                s_buf[909] = palet16ts[s_buf[909]];
                s_buf[908] = palet16ts[s_buf[908]];
                s_buf[907] = palet16ts[s_buf[907]];
                s_buf[906] = palet16ts[s_buf[906]];
                s_buf[905] = palet16ts[s_buf[905]];
                s_buf[904] = palet16ts[s_buf[904]];
            case 111:
                s_buf[903] = palet16ts[s_buf[903]];
                s_buf[902] = palet16ts[s_buf[902]];
                s_buf[901] = palet16ts[s_buf[901]];
                s_buf[900] = palet16ts[s_buf[900]];
                s_buf[899] = palet16ts[s_buf[899]];
                s_buf[898] = palet16ts[s_buf[898]];
                s_buf[897] = palet16ts[s_buf[897]];
                s_buf[896] = palet16ts[s_buf[896]];
            case 110:
                s_buf[895] = palet16ts[s_buf[895]];
                s_buf[894] = palet16ts[s_buf[894]];
                s_buf[893] = palet16ts[s_buf[893]];
                s_buf[892] = palet16ts[s_buf[892]];
                s_buf[891] = palet16ts[s_buf[891]];
                s_buf[890] = palet16ts[s_buf[890]];
                s_buf[889] = palet16ts[s_buf[889]];
                s_buf[888] = palet16ts[s_buf[888]];
            case 109:
                s_buf[887] = palet16ts[s_buf[887]];
                s_buf[886] = palet16ts[s_buf[886]];
                s_buf[885] = palet16ts[s_buf[885]];
                s_buf[884] = palet16ts[s_buf[884]];
                s_buf[883] = palet16ts[s_buf[883]];
                s_buf[882] = palet16ts[s_buf[882]];
                s_buf[881] = palet16ts[s_buf[881]];
                s_buf[880] = palet16ts[s_buf[880]];
            case 108:
                s_buf[879] = palet16ts[s_buf[879]];
                s_buf[878] = palet16ts[s_buf[878]];
                s_buf[877] = palet16ts[s_buf[877]];
                s_buf[876] = palet16ts[s_buf[876]];
                s_buf[875] = palet16ts[s_buf[875]];
                s_buf[874] = palet16ts[s_buf[874]];
                s_buf[873] = palet16ts[s_buf[873]];
                s_buf[872] = palet16ts[s_buf[872]];
            case 107:
                s_buf[871] = palet16ts[s_buf[871]];
                s_buf[870] = palet16ts[s_buf[870]];
                s_buf[869] = palet16ts[s_buf[869]];
                s_buf[868] = palet16ts[s_buf[868]];
                s_buf[867] = palet16ts[s_buf[867]];
                s_buf[866] = palet16ts[s_buf[866]];
                s_buf[865] = palet16ts[s_buf[865]];
                s_buf[864] = palet16ts[s_buf[864]];
            case 106:
                s_buf[863] = palet16ts[s_buf[863]];
                s_buf[862] = palet16ts[s_buf[862]];
                s_buf[861] = palet16ts[s_buf[861]];
                s_buf[860] = palet16ts[s_buf[860]];
                s_buf[859] = palet16ts[s_buf[859]];
                s_buf[858] = palet16ts[s_buf[858]];
                s_buf[857] = palet16ts[s_buf[857]];
                s_buf[856] = palet16ts[s_buf[856]];
            case 105:
                s_buf[855] = palet16ts[s_buf[855]];
                s_buf[854] = palet16ts[s_buf[854]];
                s_buf[853] = palet16ts[s_buf[853]];
                s_buf[852] = palet16ts[s_buf[852]];
                s_buf[851] = palet16ts[s_buf[851]];
                s_buf[850] = palet16ts[s_buf[850]];
                s_buf[849] = palet16ts[s_buf[849]];
                s_buf[848] = palet16ts[s_buf[848]];
            case 104:
                s_buf[847] = palet16ts[s_buf[847]];
                s_buf[846] = palet16ts[s_buf[846]];
                s_buf[845] = palet16ts[s_buf[845]];
                s_buf[844] = palet16ts[s_buf[844]];
                s_buf[843] = palet16ts[s_buf[843]];
                s_buf[842] = palet16ts[s_buf[842]];
                s_buf[841] = palet16ts[s_buf[841]];
                s_buf[840] = palet16ts[s_buf[840]];
            case 103:
                s_buf[839] = palet16ts[s_buf[839]];
                s_buf[838] = palet16ts[s_buf[838]];
                s_buf[837] = palet16ts[s_buf[837]];
                s_buf[836] = palet16ts[s_buf[836]];
                s_buf[835] = palet16ts[s_buf[835]];
                s_buf[834] = palet16ts[s_buf[834]];
                s_buf[833] = palet16ts[s_buf[833]];
                s_buf[832] = palet16ts[s_buf[832]];
            case 102:
                s_buf[831] = palet16ts[s_buf[831]];
                s_buf[830] = palet16ts[s_buf[830]];
                s_buf[829] = palet16ts[s_buf[829]];
                s_buf[828] = palet16ts[s_buf[828]];
                s_buf[827] = palet16ts[s_buf[827]];
                s_buf[826] = palet16ts[s_buf[826]];
                s_buf[825] = palet16ts[s_buf[825]];
                s_buf[824] = palet16ts[s_buf[824]];
            case 101:
                s_buf[823] = palet16ts[s_buf[823]];
                s_buf[822] = palet16ts[s_buf[822]];
                s_buf[821] = palet16ts[s_buf[821]];
                s_buf[820] = palet16ts[s_buf[820]];
                s_buf[819] = palet16ts[s_buf[819]];
                s_buf[818] = palet16ts[s_buf[818]];
                s_buf[817] = palet16ts[s_buf[817]];
                s_buf[816] = palet16ts[s_buf[816]];
            case 100:
                s_buf[815] = palet16ts[s_buf[815]];
                s_buf[814] = palet16ts[s_buf[814]];
                s_buf[813] = palet16ts[s_buf[813]];
                s_buf[812] = palet16ts[s_buf[812]];
                s_buf[811] = palet16ts[s_buf[811]];
                s_buf[810] = palet16ts[s_buf[810]];
                s_buf[809] = palet16ts[s_buf[809]];
                s_buf[808] = palet16ts[s_buf[808]];
            case 99:
                s_buf[807] = palet16ts[s_buf[807]];
                s_buf[806] = palet16ts[s_buf[806]];
                s_buf[805] = palet16ts[s_buf[805]];
                s_buf[804] = palet16ts[s_buf[804]];
                s_buf[803] = palet16ts[s_buf[803]];
                s_buf[802] = palet16ts[s_buf[802]];
                s_buf[801] = palet16ts[s_buf[801]];
                s_buf[800] = palet16ts[s_buf[800]];
            case 98:
                s_buf[799] = palet16ts[s_buf[799]];
                s_buf[798] = palet16ts[s_buf[798]];
                s_buf[797] = palet16ts[s_buf[797]];
                s_buf[796] = palet16ts[s_buf[796]];
                s_buf[795] = palet16ts[s_buf[795]];
                s_buf[794] = palet16ts[s_buf[794]];
                s_buf[793] = palet16ts[s_buf[793]];
                s_buf[792] = palet16ts[s_buf[792]];
            case 97:
                s_buf[791] = palet16ts[s_buf[791]];
                s_buf[790] = palet16ts[s_buf[790]];
                s_buf[789] = palet16ts[s_buf[789]];
                s_buf[788] = palet16ts[s_buf[788]];
                s_buf[787] = palet16ts[s_buf[787]];
                s_buf[786] = palet16ts[s_buf[786]];
                s_buf[785] = palet16ts[s_buf[785]];
                s_buf[784] = palet16ts[s_buf[784]];
            case 96:
                s_buf[783] = palet16ts[s_buf[783]];
                s_buf[782] = palet16ts[s_buf[782]];
                s_buf[781] = palet16ts[s_buf[781]];
                s_buf[780] = palet16ts[s_buf[780]];
                s_buf[779] = palet16ts[s_buf[779]];
                s_buf[778] = palet16ts[s_buf[778]];
                s_buf[777] = palet16ts[s_buf[777]];
                s_buf[776] = palet16ts[s_buf[776]];
            case 95:
                s_buf[775] = palet16ts[s_buf[775]];
                s_buf[774] = palet16ts[s_buf[774]];
                s_buf[773] = palet16ts[s_buf[773]];
                s_buf[772] = palet16ts[s_buf[772]];
                s_buf[771] = palet16ts[s_buf[771]];
                s_buf[770] = palet16ts[s_buf[770]];
                s_buf[769] = palet16ts[s_buf[769]];
                s_buf[768] = palet16ts[s_buf[768]];
            case 94:
                s_buf[767] = palet16ts[s_buf[767]];
                s_buf[766] = palet16ts[s_buf[766]];
                s_buf[765] = palet16ts[s_buf[765]];
                s_buf[764] = palet16ts[s_buf[764]];
                s_buf[763] = palet16ts[s_buf[763]];
                s_buf[762] = palet16ts[s_buf[762]];
                s_buf[761] = palet16ts[s_buf[761]];
                s_buf[760] = palet16ts[s_buf[760]];
            case 93:
                s_buf[759] = palet16ts[s_buf[759]];
                s_buf[758] = palet16ts[s_buf[758]];
                s_buf[757] = palet16ts[s_buf[757]];
                s_buf[756] = palet16ts[s_buf[756]];
                s_buf[755] = palet16ts[s_buf[755]];
                s_buf[754] = palet16ts[s_buf[754]];
                s_buf[753] = palet16ts[s_buf[753]];
                s_buf[752] = palet16ts[s_buf[752]];
            case 92:
                s_buf[751] = palet16ts[s_buf[751]];
                s_buf[750] = palet16ts[s_buf[750]];
                s_buf[749] = palet16ts[s_buf[749]];
                s_buf[748] = palet16ts[s_buf[748]];
                s_buf[747] = palet16ts[s_buf[747]];
                s_buf[746] = palet16ts[s_buf[746]];
                s_buf[745] = palet16ts[s_buf[745]];
                s_buf[744] = palet16ts[s_buf[744]];
            case 91:
                s_buf[743] = palet16ts[s_buf[743]];
                s_buf[742] = palet16ts[s_buf[742]];
                s_buf[741] = palet16ts[s_buf[741]];
                s_buf[740] = palet16ts[s_buf[740]];
                s_buf[739] = palet16ts[s_buf[739]];
                s_buf[738] = palet16ts[s_buf[738]];
                s_buf[737] = palet16ts[s_buf[737]];
                s_buf[736] = palet16ts[s_buf[736]];
            case 90:
                s_buf[735] = palet16ts[s_buf[735]];
                s_buf[734] = palet16ts[s_buf[734]];
                s_buf[733] = palet16ts[s_buf[733]];
                s_buf[732] = palet16ts[s_buf[732]];
                s_buf[731] = palet16ts[s_buf[731]];
                s_buf[730] = palet16ts[s_buf[730]];
                s_buf[729] = palet16ts[s_buf[729]];
                s_buf[728] = palet16ts[s_buf[728]];
            case 89:
                s_buf[727] = palet16ts[s_buf[727]];
                s_buf[726] = palet16ts[s_buf[726]];
                s_buf[725] = palet16ts[s_buf[725]];
                s_buf[724] = palet16ts[s_buf[724]];
                s_buf[723] = palet16ts[s_buf[723]];
                s_buf[722] = palet16ts[s_buf[722]];
                s_buf[721] = palet16ts[s_buf[721]];
                s_buf[720] = palet16ts[s_buf[720]];
            case 88:
                s_buf[719] = palet16ts[s_buf[719]];
                s_buf[718] = palet16ts[s_buf[718]];
                s_buf[717] = palet16ts[s_buf[717]];
                s_buf[716] = palet16ts[s_buf[716]];
                s_buf[715] = palet16ts[s_buf[715]];
                s_buf[714] = palet16ts[s_buf[714]];
                s_buf[713] = palet16ts[s_buf[713]];
                s_buf[712] = palet16ts[s_buf[712]];
            case 87:
                s_buf[711] = palet16ts[s_buf[711]];
                s_buf[710] = palet16ts[s_buf[710]];
                s_buf[709] = palet16ts[s_buf[709]];
                s_buf[708] = palet16ts[s_buf[708]];
                s_buf[707] = palet16ts[s_buf[707]];
                s_buf[706] = palet16ts[s_buf[706]];
                s_buf[705] = palet16ts[s_buf[705]];
                s_buf[704] = palet16ts[s_buf[704]];
            case 86:
                s_buf[703] = palet16ts[s_buf[703]];
                s_buf[702] = palet16ts[s_buf[702]];
                s_buf[701] = palet16ts[s_buf[701]];
                s_buf[700] = palet16ts[s_buf[700]];
                s_buf[699] = palet16ts[s_buf[699]];
                s_buf[698] = palet16ts[s_buf[698]];
                s_buf[697] = palet16ts[s_buf[697]];
                s_buf[696] = palet16ts[s_buf[696]];
            case 85:
                s_buf[695] = palet16ts[s_buf[695]];
                s_buf[694] = palet16ts[s_buf[694]];
                s_buf[693] = palet16ts[s_buf[693]];
                s_buf[692] = palet16ts[s_buf[692]];
                s_buf[691] = palet16ts[s_buf[691]];
                s_buf[690] = palet16ts[s_buf[690]];
                s_buf[689] = palet16ts[s_buf[689]];
                s_buf[688] = palet16ts[s_buf[688]];
            case 84:
                s_buf[687] = palet16ts[s_buf[687]];
                s_buf[686] = palet16ts[s_buf[686]];
                s_buf[685] = palet16ts[s_buf[685]];
                s_buf[684] = palet16ts[s_buf[684]];
                s_buf[683] = palet16ts[s_buf[683]];
                s_buf[682] = palet16ts[s_buf[682]];
                s_buf[681] = palet16ts[s_buf[681]];
                s_buf[680] = palet16ts[s_buf[680]];
            case 83:
                s_buf[679] = palet16ts[s_buf[679]];
                s_buf[678] = palet16ts[s_buf[678]];
                s_buf[677] = palet16ts[s_buf[677]];
                s_buf[676] = palet16ts[s_buf[676]];
                s_buf[675] = palet16ts[s_buf[675]];
                s_buf[674] = palet16ts[s_buf[674]];
                s_buf[673] = palet16ts[s_buf[673]];
                s_buf[672] = palet16ts[s_buf[672]];
            case 82:
                s_buf[671] = palet16ts[s_buf[671]];
                s_buf[670] = palet16ts[s_buf[670]];
                s_buf[669] = palet16ts[s_buf[669]];
                s_buf[668] = palet16ts[s_buf[668]];
                s_buf[667] = palet16ts[s_buf[667]];
                s_buf[666] = palet16ts[s_buf[666]];
                s_buf[665] = palet16ts[s_buf[665]];
                s_buf[664] = palet16ts[s_buf[664]];
            case 81:
                s_buf[663] = palet16ts[s_buf[663]];
                s_buf[662] = palet16ts[s_buf[662]];
                s_buf[661] = palet16ts[s_buf[661]];
                s_buf[660] = palet16ts[s_buf[660]];
                s_buf[659] = palet16ts[s_buf[659]];
                s_buf[658] = palet16ts[s_buf[658]];
                s_buf[657] = palet16ts[s_buf[657]];
                s_buf[656] = palet16ts[s_buf[656]];
            case 80:
                s_buf[655] = palet16ts[s_buf[655]];
                s_buf[654] = palet16ts[s_buf[654]];
                s_buf[653] = palet16ts[s_buf[653]];
                s_buf[652] = palet16ts[s_buf[652]];
                s_buf[651] = palet16ts[s_buf[651]];
                s_buf[650] = palet16ts[s_buf[650]];
                s_buf[649] = palet16ts[s_buf[649]];
                s_buf[648] = palet16ts[s_buf[648]];
            case 79:
                s_buf[647] = palet16ts[s_buf[647]];
                s_buf[646] = palet16ts[s_buf[646]];
                s_buf[645] = palet16ts[s_buf[645]];
                s_buf[644] = palet16ts[s_buf[644]];
                s_buf[643] = palet16ts[s_buf[643]];
                s_buf[642] = palet16ts[s_buf[642]];
                s_buf[641] = palet16ts[s_buf[641]];
                s_buf[640] = palet16ts[s_buf[640]];
            case 78:
                s_buf[639] = palet16ts[s_buf[639]];
                s_buf[638] = palet16ts[s_buf[638]];
                s_buf[637] = palet16ts[s_buf[637]];
                s_buf[636] = palet16ts[s_buf[636]];
                s_buf[635] = palet16ts[s_buf[635]];
                s_buf[634] = palet16ts[s_buf[634]];
                s_buf[633] = palet16ts[s_buf[633]];
                s_buf[632] = palet16ts[s_buf[632]];
            case 77:
                s_buf[631] = palet16ts[s_buf[631]];
                s_buf[630] = palet16ts[s_buf[630]];
                s_buf[629] = palet16ts[s_buf[629]];
                s_buf[628] = palet16ts[s_buf[628]];
                s_buf[627] = palet16ts[s_buf[627]];
                s_buf[626] = palet16ts[s_buf[626]];
                s_buf[625] = palet16ts[s_buf[625]];
                s_buf[624] = palet16ts[s_buf[624]];
            case 76:
                s_buf[623] = palet16ts[s_buf[623]];
                s_buf[622] = palet16ts[s_buf[622]];
                s_buf[621] = palet16ts[s_buf[621]];
                s_buf[620] = palet16ts[s_buf[620]];
                s_buf[619] = palet16ts[s_buf[619]];
                s_buf[618] = palet16ts[s_buf[618]];
                s_buf[617] = palet16ts[s_buf[617]];
                s_buf[616] = palet16ts[s_buf[616]];
            case 75:
                s_buf[615] = palet16ts[s_buf[615]];
                s_buf[614] = palet16ts[s_buf[614]];
                s_buf[613] = palet16ts[s_buf[613]];
                s_buf[612] = palet16ts[s_buf[612]];
                s_buf[611] = palet16ts[s_buf[611]];
                s_buf[610] = palet16ts[s_buf[610]];
                s_buf[609] = palet16ts[s_buf[609]];
                s_buf[608] = palet16ts[s_buf[608]];
            case 74:
                s_buf[607] = palet16ts[s_buf[607]];
                s_buf[606] = palet16ts[s_buf[606]];
                s_buf[605] = palet16ts[s_buf[605]];
                s_buf[604] = palet16ts[s_buf[604]];
                s_buf[603] = palet16ts[s_buf[603]];
                s_buf[602] = palet16ts[s_buf[602]];
                s_buf[601] = palet16ts[s_buf[601]];
                s_buf[600] = palet16ts[s_buf[600]];
            case 73:
                s_buf[599] = palet16ts[s_buf[599]];
                s_buf[598] = palet16ts[s_buf[598]];
                s_buf[597] = palet16ts[s_buf[597]];
                s_buf[596] = palet16ts[s_buf[596]];
                s_buf[595] = palet16ts[s_buf[595]];
                s_buf[594] = palet16ts[s_buf[594]];
                s_buf[593] = palet16ts[s_buf[593]];
                s_buf[592] = palet16ts[s_buf[592]];
            case 72:
                s_buf[591] = palet16ts[s_buf[591]];
                s_buf[590] = palet16ts[s_buf[590]];
                s_buf[589] = palet16ts[s_buf[589]];
                s_buf[588] = palet16ts[s_buf[588]];
                s_buf[587] = palet16ts[s_buf[587]];
                s_buf[586] = palet16ts[s_buf[586]];
                s_buf[585] = palet16ts[s_buf[585]];
                s_buf[584] = palet16ts[s_buf[584]];
            case 71:
                s_buf[583] = palet16ts[s_buf[583]];
                s_buf[582] = palet16ts[s_buf[582]];
                s_buf[581] = palet16ts[s_buf[581]];
                s_buf[580] = palet16ts[s_buf[580]];
                s_buf[579] = palet16ts[s_buf[579]];
                s_buf[578] = palet16ts[s_buf[578]];
                s_buf[577] = palet16ts[s_buf[577]];
                s_buf[576] = palet16ts[s_buf[576]];
            case 70:
                s_buf[575] = palet16ts[s_buf[575]];
                s_buf[574] = palet16ts[s_buf[574]];
                s_buf[573] = palet16ts[s_buf[573]];
                s_buf[572] = palet16ts[s_buf[572]];
                s_buf[571] = palet16ts[s_buf[571]];
                s_buf[570] = palet16ts[s_buf[570]];
                s_buf[569] = palet16ts[s_buf[569]];
                s_buf[568] = palet16ts[s_buf[568]];
            case 69:
                s_buf[567] = palet16ts[s_buf[567]];
                s_buf[566] = palet16ts[s_buf[566]];
                s_buf[565] = palet16ts[s_buf[565]];
                s_buf[564] = palet16ts[s_buf[564]];
                s_buf[563] = palet16ts[s_buf[563]];
                s_buf[562] = palet16ts[s_buf[562]];
                s_buf[561] = palet16ts[s_buf[561]];
                s_buf[560] = palet16ts[s_buf[560]];
            case 68:
                s_buf[559] = palet16ts[s_buf[559]];
                s_buf[558] = palet16ts[s_buf[558]];
                s_buf[557] = palet16ts[s_buf[557]];
                s_buf[556] = palet16ts[s_buf[556]];
                s_buf[555] = palet16ts[s_buf[555]];
                s_buf[554] = palet16ts[s_buf[554]];
                s_buf[553] = palet16ts[s_buf[553]];
                s_buf[552] = palet16ts[s_buf[552]];
            case 67:
                s_buf[551] = palet16ts[s_buf[551]];
                s_buf[550] = palet16ts[s_buf[550]];
                s_buf[549] = palet16ts[s_buf[549]];
                s_buf[548] = palet16ts[s_buf[548]];
                s_buf[547] = palet16ts[s_buf[547]];
                s_buf[546] = palet16ts[s_buf[546]];
                s_buf[545] = palet16ts[s_buf[545]];
                s_buf[544] = palet16ts[s_buf[544]];
            case 66:
                s_buf[543] = palet16ts[s_buf[543]];
                s_buf[542] = palet16ts[s_buf[542]];
                s_buf[541] = palet16ts[s_buf[541]];
                s_buf[540] = palet16ts[s_buf[540]];
                s_buf[539] = palet16ts[s_buf[539]];
                s_buf[538] = palet16ts[s_buf[538]];
                s_buf[537] = palet16ts[s_buf[537]];
                s_buf[536] = palet16ts[s_buf[536]];
            case 65:
                s_buf[535] = palet16ts[s_buf[535]];
                s_buf[534] = palet16ts[s_buf[534]];
                s_buf[533] = palet16ts[s_buf[533]];
                s_buf[532] = palet16ts[s_buf[532]];
                s_buf[531] = palet16ts[s_buf[531]];
                s_buf[530] = palet16ts[s_buf[530]];
                s_buf[529] = palet16ts[s_buf[529]];
                s_buf[528] = palet16ts[s_buf[528]];
            case 64:
                s_buf[527] = palet16ts[s_buf[527]];
                s_buf[526] = palet16ts[s_buf[526]];
                s_buf[525] = palet16ts[s_buf[525]];
                s_buf[524] = palet16ts[s_buf[524]];
                s_buf[523] = palet16ts[s_buf[523]];
                s_buf[522] = palet16ts[s_buf[522]];
                s_buf[521] = palet16ts[s_buf[521]];
                s_buf[520] = palet16ts[s_buf[520]];
            case 63:
                s_buf[519] = palet16ts[s_buf[519]];
                s_buf[518] = palet16ts[s_buf[518]];
                s_buf[517] = palet16ts[s_buf[517]];
                s_buf[516] = palet16ts[s_buf[516]];
                s_buf[515] = palet16ts[s_buf[515]];
                s_buf[514] = palet16ts[s_buf[514]];
                s_buf[513] = palet16ts[s_buf[513]];
                s_buf[512] = palet16ts[s_buf[512]];
            case 62:
                s_buf[511] = palet16ts[s_buf[511]];
                s_buf[510] = palet16ts[s_buf[510]];
                s_buf[509] = palet16ts[s_buf[509]];
                s_buf[508] = palet16ts[s_buf[508]];
                s_buf[507] = palet16ts[s_buf[507]];
                s_buf[506] = palet16ts[s_buf[506]];
                s_buf[505] = palet16ts[s_buf[505]];
                s_buf[504] = palet16ts[s_buf[504]];
            case 61:
                s_buf[503] = palet16ts[s_buf[503]];
                s_buf[502] = palet16ts[s_buf[502]];
                s_buf[501] = palet16ts[s_buf[501]];
                s_buf[500] = palet16ts[s_buf[500]];
                s_buf[499] = palet16ts[s_buf[499]];
                s_buf[498] = palet16ts[s_buf[498]];
                s_buf[497] = palet16ts[s_buf[497]];
                s_buf[496] = palet16ts[s_buf[496]];
            case 60:
                s_buf[495] = palet16ts[s_buf[495]];
                s_buf[494] = palet16ts[s_buf[494]];
                s_buf[493] = palet16ts[s_buf[493]];
                s_buf[492] = palet16ts[s_buf[492]];
                s_buf[491] = palet16ts[s_buf[491]];
                s_buf[490] = palet16ts[s_buf[490]];
                s_buf[489] = palet16ts[s_buf[489]];
                s_buf[488] = palet16ts[s_buf[488]];
            case 59:
                s_buf[487] = palet16ts[s_buf[487]];
                s_buf[486] = palet16ts[s_buf[486]];
                s_buf[485] = palet16ts[s_buf[485]];
                s_buf[484] = palet16ts[s_buf[484]];
                s_buf[483] = palet16ts[s_buf[483]];
                s_buf[482] = palet16ts[s_buf[482]];
                s_buf[481] = palet16ts[s_buf[481]];
                s_buf[480] = palet16ts[s_buf[480]];
            case 58:
                s_buf[479] = palet16ts[s_buf[479]];
                s_buf[478] = palet16ts[s_buf[478]];
                s_buf[477] = palet16ts[s_buf[477]];
                s_buf[476] = palet16ts[s_buf[476]];
                s_buf[475] = palet16ts[s_buf[475]];
                s_buf[474] = palet16ts[s_buf[474]];
                s_buf[473] = palet16ts[s_buf[473]];
                s_buf[472] = palet16ts[s_buf[472]];
            case 57:
                s_buf[471] = palet16ts[s_buf[471]];
                s_buf[470] = palet16ts[s_buf[470]];
                s_buf[469] = palet16ts[s_buf[469]];
                s_buf[468] = palet16ts[s_buf[468]];
                s_buf[467] = palet16ts[s_buf[467]];
                s_buf[466] = palet16ts[s_buf[466]];
                s_buf[465] = palet16ts[s_buf[465]];
                s_buf[464] = palet16ts[s_buf[464]];
            case 56:
                s_buf[463] = palet16ts[s_buf[463]];
                s_buf[462] = palet16ts[s_buf[462]];
                s_buf[461] = palet16ts[s_buf[461]];
                s_buf[460] = palet16ts[s_buf[460]];
                s_buf[459] = palet16ts[s_buf[459]];
                s_buf[458] = palet16ts[s_buf[458]];
                s_buf[457] = palet16ts[s_buf[457]];
                s_buf[456] = palet16ts[s_buf[456]];
            case 55:
                s_buf[455] = palet16ts[s_buf[455]];
                s_buf[454] = palet16ts[s_buf[454]];
                s_buf[453] = palet16ts[s_buf[453]];
                s_buf[452] = palet16ts[s_buf[452]];
                s_buf[451] = palet16ts[s_buf[451]];
                s_buf[450] = palet16ts[s_buf[450]];
                s_buf[449] = palet16ts[s_buf[449]];
                s_buf[448] = palet16ts[s_buf[448]];
            case 54:
                s_buf[447] = palet16ts[s_buf[447]];
                s_buf[446] = palet16ts[s_buf[446]];
                s_buf[445] = palet16ts[s_buf[445]];
                s_buf[444] = palet16ts[s_buf[444]];
                s_buf[443] = palet16ts[s_buf[443]];
                s_buf[442] = palet16ts[s_buf[442]];
                s_buf[441] = palet16ts[s_buf[441]];
                s_buf[440] = palet16ts[s_buf[440]];
            case 53:
                s_buf[439] = palet16ts[s_buf[439]];
                s_buf[438] = palet16ts[s_buf[438]];
                s_buf[437] = palet16ts[s_buf[437]];
                s_buf[436] = palet16ts[s_buf[436]];
                s_buf[435] = palet16ts[s_buf[435]];
                s_buf[434] = palet16ts[s_buf[434]];
                s_buf[433] = palet16ts[s_buf[433]];
                s_buf[432] = palet16ts[s_buf[432]];
            case 52:
                s_buf[431] = palet16ts[s_buf[431]];
                s_buf[430] = palet16ts[s_buf[430]];
                s_buf[429] = palet16ts[s_buf[429]];
                s_buf[428] = palet16ts[s_buf[428]];
                s_buf[427] = palet16ts[s_buf[427]];
                s_buf[426] = palet16ts[s_buf[426]];
                s_buf[425] = palet16ts[s_buf[425]];
                s_buf[424] = palet16ts[s_buf[424]];
            case 51:
                s_buf[423] = palet16ts[s_buf[423]];
                s_buf[422] = palet16ts[s_buf[422]];
                s_buf[421] = palet16ts[s_buf[421]];
                s_buf[420] = palet16ts[s_buf[420]];
                s_buf[419] = palet16ts[s_buf[419]];
                s_buf[418] = palet16ts[s_buf[418]];
                s_buf[417] = palet16ts[s_buf[417]];
                s_buf[416] = palet16ts[s_buf[416]];
            case 50:
                s_buf[415] = palet16ts[s_buf[415]];
                s_buf[414] = palet16ts[s_buf[414]];
                s_buf[413] = palet16ts[s_buf[413]];
                s_buf[412] = palet16ts[s_buf[412]];
                s_buf[411] = palet16ts[s_buf[411]];
                s_buf[410] = palet16ts[s_buf[410]];
                s_buf[409] = palet16ts[s_buf[409]];
                s_buf[408] = palet16ts[s_buf[408]];
            case 49:
                s_buf[407] = palet16ts[s_buf[407]];
                s_buf[406] = palet16ts[s_buf[406]];
                s_buf[405] = palet16ts[s_buf[405]];
                s_buf[404] = palet16ts[s_buf[404]];
                s_buf[403] = palet16ts[s_buf[403]];
                s_buf[402] = palet16ts[s_buf[402]];
                s_buf[401] = palet16ts[s_buf[401]];
                s_buf[400] = palet16ts[s_buf[400]];
            case 48:
                s_buf[399] = palet16ts[s_buf[399]];
                s_buf[398] = palet16ts[s_buf[398]];
                s_buf[397] = palet16ts[s_buf[397]];
                s_buf[396] = palet16ts[s_buf[396]];
                s_buf[395] = palet16ts[s_buf[395]];
                s_buf[394] = palet16ts[s_buf[394]];
                s_buf[393] = palet16ts[s_buf[393]];
                s_buf[392] = palet16ts[s_buf[392]];
            case 47:
                s_buf[391] = palet16ts[s_buf[391]];
                s_buf[390] = palet16ts[s_buf[390]];
                s_buf[389] = palet16ts[s_buf[389]];
                s_buf[388] = palet16ts[s_buf[388]];
                s_buf[387] = palet16ts[s_buf[387]];
                s_buf[386] = palet16ts[s_buf[386]];
                s_buf[385] = palet16ts[s_buf[385]];
                s_buf[384] = palet16ts[s_buf[384]];
            case 46:
                s_buf[383] = palet16ts[s_buf[383]];
                s_buf[382] = palet16ts[s_buf[382]];
                s_buf[381] = palet16ts[s_buf[381]];
                s_buf[380] = palet16ts[s_buf[380]];
                s_buf[379] = palet16ts[s_buf[379]];
                s_buf[378] = palet16ts[s_buf[378]];
                s_buf[377] = palet16ts[s_buf[377]];
                s_buf[376] = palet16ts[s_buf[376]];
            case 45:
                s_buf[375] = palet16ts[s_buf[375]];
                s_buf[374] = palet16ts[s_buf[374]];
                s_buf[373] = palet16ts[s_buf[373]];
                s_buf[372] = palet16ts[s_buf[372]];
                s_buf[371] = palet16ts[s_buf[371]];
                s_buf[370] = palet16ts[s_buf[370]];
                s_buf[369] = palet16ts[s_buf[369]];
                s_buf[368] = palet16ts[s_buf[368]];
            case 44:
                s_buf[367] = palet16ts[s_buf[367]];
                s_buf[366] = palet16ts[s_buf[366]];
                s_buf[365] = palet16ts[s_buf[365]];
                s_buf[364] = palet16ts[s_buf[364]];
                s_buf[363] = palet16ts[s_buf[363]];
                s_buf[362] = palet16ts[s_buf[362]];
                s_buf[361] = palet16ts[s_buf[361]];
                s_buf[360] = palet16ts[s_buf[360]];
            case 43:
                s_buf[359] = palet16ts[s_buf[359]];
                s_buf[358] = palet16ts[s_buf[358]];
                s_buf[357] = palet16ts[s_buf[357]];
                s_buf[356] = palet16ts[s_buf[356]];
                s_buf[355] = palet16ts[s_buf[355]];
                s_buf[354] = palet16ts[s_buf[354]];
                s_buf[353] = palet16ts[s_buf[353]];
                s_buf[352] = palet16ts[s_buf[352]];
            case 42:
                s_buf[351] = palet16ts[s_buf[351]];
                s_buf[350] = palet16ts[s_buf[350]];
                s_buf[349] = palet16ts[s_buf[349]];
                s_buf[348] = palet16ts[s_buf[348]];
                s_buf[347] = palet16ts[s_buf[347]];
                s_buf[346] = palet16ts[s_buf[346]];
                s_buf[345] = palet16ts[s_buf[345]];
                s_buf[344] = palet16ts[s_buf[344]];
            case 41:
                s_buf[343] = palet16ts[s_buf[343]];
                s_buf[342] = palet16ts[s_buf[342]];
                s_buf[341] = palet16ts[s_buf[341]];
                s_buf[340] = palet16ts[s_buf[340]];
                s_buf[339] = palet16ts[s_buf[339]];
                s_buf[338] = palet16ts[s_buf[338]];
                s_buf[337] = palet16ts[s_buf[337]];
                s_buf[336] = palet16ts[s_buf[336]];
            case 40:
                s_buf[335] = palet16ts[s_buf[335]];
                s_buf[334] = palet16ts[s_buf[334]];
                s_buf[333] = palet16ts[s_buf[333]];
                s_buf[332] = palet16ts[s_buf[332]];
                s_buf[331] = palet16ts[s_buf[331]];
                s_buf[330] = palet16ts[s_buf[330]];
                s_buf[329] = palet16ts[s_buf[329]];
                s_buf[328] = palet16ts[s_buf[328]];
            case 39:
                s_buf[327] = palet16ts[s_buf[327]];
                s_buf[326] = palet16ts[s_buf[326]];
                s_buf[325] = palet16ts[s_buf[325]];
                s_buf[324] = palet16ts[s_buf[324]];
                s_buf[323] = palet16ts[s_buf[323]];
                s_buf[322] = palet16ts[s_buf[322]];
                s_buf[321] = palet16ts[s_buf[321]];
                s_buf[320] = palet16ts[s_buf[320]];
            case 38:
                s_buf[319] = palet16ts[s_buf[319]];
                s_buf[318] = palet16ts[s_buf[318]];
                s_buf[317] = palet16ts[s_buf[317]];
                s_buf[316] = palet16ts[s_buf[316]];
                s_buf[315] = palet16ts[s_buf[315]];
                s_buf[314] = palet16ts[s_buf[314]];
                s_buf[313] = palet16ts[s_buf[313]];
                s_buf[312] = palet16ts[s_buf[312]];
            case 37:
                s_buf[311] = palet16ts[s_buf[311]];
                s_buf[310] = palet16ts[s_buf[310]];
                s_buf[309] = palet16ts[s_buf[309]];
                s_buf[308] = palet16ts[s_buf[308]];
                s_buf[307] = palet16ts[s_buf[307]];
                s_buf[306] = palet16ts[s_buf[306]];
                s_buf[305] = palet16ts[s_buf[305]];
                s_buf[304] = palet16ts[s_buf[304]];
            case 36:
                s_buf[303] = palet16ts[s_buf[303]];
                s_buf[302] = palet16ts[s_buf[302]];
                s_buf[301] = palet16ts[s_buf[301]];
                s_buf[300] = palet16ts[s_buf[300]];
                s_buf[299] = palet16ts[s_buf[299]];
                s_buf[298] = palet16ts[s_buf[298]];
                s_buf[297] = palet16ts[s_buf[297]];
                s_buf[296] = palet16ts[s_buf[296]];
            case 35:
                s_buf[295] = palet16ts[s_buf[295]];
                s_buf[294] = palet16ts[s_buf[294]];
                s_buf[293] = palet16ts[s_buf[293]];
                s_buf[292] = palet16ts[s_buf[292]];
                s_buf[291] = palet16ts[s_buf[291]];
                s_buf[290] = palet16ts[s_buf[290]];
                s_buf[289] = palet16ts[s_buf[289]];
                s_buf[288] = palet16ts[s_buf[288]];
            case 34:
                s_buf[287] = palet16ts[s_buf[287]];
                s_buf[286] = palet16ts[s_buf[286]];
                s_buf[285] = palet16ts[s_buf[285]];
                s_buf[284] = palet16ts[s_buf[284]];
                s_buf[283] = palet16ts[s_buf[283]];
                s_buf[282] = palet16ts[s_buf[282]];
                s_buf[281] = palet16ts[s_buf[281]];
                s_buf[280] = palet16ts[s_buf[280]];
            case 33:
                s_buf[279] = palet16ts[s_buf[279]];
                s_buf[278] = palet16ts[s_buf[278]];
                s_buf[277] = palet16ts[s_buf[277]];
                s_buf[276] = palet16ts[s_buf[276]];
                s_buf[275] = palet16ts[s_buf[275]];
                s_buf[274] = palet16ts[s_buf[274]];
                s_buf[273] = palet16ts[s_buf[273]];
                s_buf[272] = palet16ts[s_buf[272]];
            case 32:
                s_buf[271] = palet16ts[s_buf[271]];
                s_buf[270] = palet16ts[s_buf[270]];
                s_buf[269] = palet16ts[s_buf[269]];
                s_buf[268] = palet16ts[s_buf[268]];
                s_buf[267] = palet16ts[s_buf[267]];
                s_buf[266] = palet16ts[s_buf[266]];
                s_buf[265] = palet16ts[s_buf[265]];
                s_buf[264] = palet16ts[s_buf[264]];
            case 31:
                s_buf[263] = palet16ts[s_buf[263]];
                s_buf[262] = palet16ts[s_buf[262]];
                s_buf[261] = palet16ts[s_buf[261]];
                s_buf[260] = palet16ts[s_buf[260]];
                s_buf[259] = palet16ts[s_buf[259]];
                s_buf[258] = palet16ts[s_buf[258]];
                s_buf[257] = palet16ts[s_buf[257]];
                s_buf[256] = palet16ts[s_buf[256]];
            case 30:
                s_buf[255] = palet16ts[s_buf[255]];
                s_buf[254] = palet16ts[s_buf[254]];
                s_buf[253] = palet16ts[s_buf[253]];
                s_buf[252] = palet16ts[s_buf[252]];
                s_buf[251] = palet16ts[s_buf[251]];
                s_buf[250] = palet16ts[s_buf[250]];
                s_buf[249] = palet16ts[s_buf[249]];
                s_buf[248] = palet16ts[s_buf[248]];
            case 29:
                s_buf[247] = palet16ts[s_buf[247]];
                s_buf[246] = palet16ts[s_buf[246]];
                s_buf[245] = palet16ts[s_buf[245]];
                s_buf[244] = palet16ts[s_buf[244]];
                s_buf[243] = palet16ts[s_buf[243]];
                s_buf[242] = palet16ts[s_buf[242]];
                s_buf[241] = palet16ts[s_buf[241]];
                s_buf[240] = palet16ts[s_buf[240]];
            case 28:
                s_buf[239] = palet16ts[s_buf[239]];
                s_buf[238] = palet16ts[s_buf[238]];
                s_buf[237] = palet16ts[s_buf[237]];
                s_buf[236] = palet16ts[s_buf[236]];
                s_buf[235] = palet16ts[s_buf[235]];
                s_buf[234] = palet16ts[s_buf[234]];
                s_buf[233] = palet16ts[s_buf[233]];
                s_buf[232] = palet16ts[s_buf[232]];
            case 27:
                s_buf[231] = palet16ts[s_buf[231]];
                s_buf[230] = palet16ts[s_buf[230]];
                s_buf[229] = palet16ts[s_buf[229]];
                s_buf[228] = palet16ts[s_buf[228]];
                s_buf[227] = palet16ts[s_buf[227]];
                s_buf[226] = palet16ts[s_buf[226]];
                s_buf[225] = palet16ts[s_buf[225]];
                s_buf[224] = palet16ts[s_buf[224]];
            case 26:
                s_buf[223] = palet16ts[s_buf[223]];
                s_buf[222] = palet16ts[s_buf[222]];
                s_buf[221] = palet16ts[s_buf[221]];
                s_buf[220] = palet16ts[s_buf[220]];
                s_buf[219] = palet16ts[s_buf[219]];
                s_buf[218] = palet16ts[s_buf[218]];
                s_buf[217] = palet16ts[s_buf[217]];
                s_buf[216] = palet16ts[s_buf[216]];
            case 25:
                s_buf[215] = palet16ts[s_buf[215]];
                s_buf[214] = palet16ts[s_buf[214]];
                s_buf[213] = palet16ts[s_buf[213]];
                s_buf[212] = palet16ts[s_buf[212]];
                s_buf[211] = palet16ts[s_buf[211]];
                s_buf[210] = palet16ts[s_buf[210]];
                s_buf[209] = palet16ts[s_buf[209]];
                s_buf[208] = palet16ts[s_buf[208]];
            case 24:
                s_buf[207] = palet16ts[s_buf[207]];
                s_buf[206] = palet16ts[s_buf[206]];
                s_buf[205] = palet16ts[s_buf[205]];
                s_buf[204] = palet16ts[s_buf[204]];
                s_buf[203] = palet16ts[s_buf[203]];
                s_buf[202] = palet16ts[s_buf[202]];
                s_buf[201] = palet16ts[s_buf[201]];
                s_buf[200] = palet16ts[s_buf[200]];
            case 23:
                s_buf[199] = palet16ts[s_buf[199]];
                s_buf[198] = palet16ts[s_buf[198]];
                s_buf[197] = palet16ts[s_buf[197]];
                s_buf[196] = palet16ts[s_buf[196]];
                s_buf[195] = palet16ts[s_buf[195]];
                s_buf[194] = palet16ts[s_buf[194]];
                s_buf[193] = palet16ts[s_buf[193]];
                s_buf[192] = palet16ts[s_buf[192]];
            case 22:
                s_buf[191] = palet16ts[s_buf[191]];
                s_buf[190] = palet16ts[s_buf[190]];
                s_buf[189] = palet16ts[s_buf[189]];
                s_buf[188] = palet16ts[s_buf[188]];
                s_buf[187] = palet16ts[s_buf[187]];
                s_buf[186] = palet16ts[s_buf[186]];
                s_buf[185] = palet16ts[s_buf[185]];
                s_buf[184] = palet16ts[s_buf[184]];
            case 21:
                s_buf[183] = palet16ts[s_buf[183]];
                s_buf[182] = palet16ts[s_buf[182]];
                s_buf[181] = palet16ts[s_buf[181]];
                s_buf[180] = palet16ts[s_buf[180]];
                s_buf[179] = palet16ts[s_buf[179]];
                s_buf[178] = palet16ts[s_buf[178]];
                s_buf[177] = palet16ts[s_buf[177]];
                s_buf[176] = palet16ts[s_buf[176]];
            case 20:
                s_buf[175] = palet16ts[s_buf[175]];
                s_buf[174] = palet16ts[s_buf[174]];
                s_buf[173] = palet16ts[s_buf[173]];
                s_buf[172] = palet16ts[s_buf[172]];
                s_buf[171] = palet16ts[s_buf[171]];
                s_buf[170] = palet16ts[s_buf[170]];
                s_buf[169] = palet16ts[s_buf[169]];
                s_buf[168] = palet16ts[s_buf[168]];
            case 19:
                s_buf[167] = palet16ts[s_buf[167]];
                s_buf[166] = palet16ts[s_buf[166]];
                s_buf[165] = palet16ts[s_buf[165]];
                s_buf[164] = palet16ts[s_buf[164]];
                s_buf[163] = palet16ts[s_buf[163]];
                s_buf[162] = palet16ts[s_buf[162]];
                s_buf[161] = palet16ts[s_buf[161]];
                s_buf[160] = palet16ts[s_buf[160]];
            case 18:
                s_buf[159] = palet16ts[s_buf[159]];
                s_buf[158] = palet16ts[s_buf[158]];
                s_buf[157] = palet16ts[s_buf[157]];
                s_buf[156] = palet16ts[s_buf[156]];
                s_buf[155] = palet16ts[s_buf[155]];
                s_buf[154] = palet16ts[s_buf[154]];
                s_buf[153] = palet16ts[s_buf[153]];
                s_buf[152] = palet16ts[s_buf[152]];
            case 17:
                s_buf[151] = palet16ts[s_buf[151]];
                s_buf[150] = palet16ts[s_buf[150]];
                s_buf[149] = palet16ts[s_buf[149]];
                s_buf[148] = palet16ts[s_buf[148]];
                s_buf[147] = palet16ts[s_buf[147]];
                s_buf[146] = palet16ts[s_buf[146]];
                s_buf[145] = palet16ts[s_buf[145]];
                s_buf[144] = palet16ts[s_buf[144]];
            case 16:
                s_buf[143] = palet16ts[s_buf[143]];
                s_buf[142] = palet16ts[s_buf[142]];
                s_buf[141] = palet16ts[s_buf[141]];
                s_buf[140] = palet16ts[s_buf[140]];
                s_buf[139] = palet16ts[s_buf[139]];
                s_buf[138] = palet16ts[s_buf[138]];
                s_buf[137] = palet16ts[s_buf[137]];
                s_buf[136] = palet16ts[s_buf[136]];
            case 15:
                s_buf[135] = palet16ts[s_buf[135]];
                s_buf[134] = palet16ts[s_buf[134]];
                s_buf[133] = palet16ts[s_buf[133]];
                s_buf[132] = palet16ts[s_buf[132]];
                s_buf[131] = palet16ts[s_buf[131]];
                s_buf[130] = palet16ts[s_buf[130]];
                s_buf[129] = palet16ts[s_buf[129]];
                s_buf[128] = palet16ts[s_buf[128]];
            case 14:
                s_buf[127] = palet16ts[s_buf[127]];
                s_buf[126] = palet16ts[s_buf[126]];
                s_buf[125] = palet16ts[s_buf[125]];
                s_buf[124] = palet16ts[s_buf[124]];
                s_buf[123] = palet16ts[s_buf[123]];
                s_buf[122] = palet16ts[s_buf[122]];
                s_buf[121] = palet16ts[s_buf[121]];
                s_buf[120] = palet16ts[s_buf[120]];
            case 13:
                s_buf[119] = palet16ts[s_buf[119]];
                s_buf[118] = palet16ts[s_buf[118]];
                s_buf[117] = palet16ts[s_buf[117]];
                s_buf[116] = palet16ts[s_buf[116]];
                s_buf[115] = palet16ts[s_buf[115]];
                s_buf[114] = palet16ts[s_buf[114]];
                s_buf[113] = palet16ts[s_buf[113]];
                s_buf[112] = palet16ts[s_buf[112]];
            case 12:
                s_buf[111] = palet16ts[s_buf[111]];
                s_buf[110] = palet16ts[s_buf[110]];
                s_buf[109] = palet16ts[s_buf[109]];
                s_buf[108] = palet16ts[s_buf[108]];
                s_buf[107] = palet16ts[s_buf[107]];
                s_buf[106] = palet16ts[s_buf[106]];
                s_buf[105] = palet16ts[s_buf[105]];
                s_buf[104] = palet16ts[s_buf[104]];
            case 11:
                s_buf[103] = palet16ts[s_buf[103]];
                s_buf[102] = palet16ts[s_buf[102]];
                s_buf[101] = palet16ts[s_buf[101]];
                s_buf[100] = palet16ts[s_buf[100]];
                s_buf[99] = palet16ts[s_buf[99]];
                s_buf[98] = palet16ts[s_buf[98]];
                s_buf[97] = palet16ts[s_buf[97]];
                s_buf[96] = palet16ts[s_buf[96]];
            case 10:
                s_buf[95] = palet16ts[s_buf[95]];
                s_buf[94] = palet16ts[s_buf[94]];
                s_buf[93] = palet16ts[s_buf[93]];
                s_buf[92] = palet16ts[s_buf[92]];
                s_buf[91] = palet16ts[s_buf[91]];
                s_buf[90] = palet16ts[s_buf[90]];
                s_buf[89] = palet16ts[s_buf[89]];
                s_buf[88] = palet16ts[s_buf[88]];
            case 9:
                s_buf[87] = palet16ts[s_buf[87]];
                s_buf[86] = palet16ts[s_buf[86]];
                s_buf[85] = palet16ts[s_buf[85]];
                s_buf[84] = palet16ts[s_buf[84]];
                s_buf[83] = palet16ts[s_buf[83]];
                s_buf[82] = palet16ts[s_buf[82]];
                s_buf[81] = palet16ts[s_buf[81]];
                s_buf[80] = palet16ts[s_buf[80]];
            case 8:
                s_buf[79] = palet16ts[s_buf[79]];
                s_buf[78] = palet16ts[s_buf[78]];
                s_buf[77] = palet16ts[s_buf[77]];
                s_buf[76] = palet16ts[s_buf[76]];
                s_buf[75] = palet16ts[s_buf[75]];
                s_buf[74] = palet16ts[s_buf[74]];
                s_buf[73] = palet16ts[s_buf[73]];
                s_buf[72] = palet16ts[s_buf[72]];
            case 7:
                s_buf[71] = palet16ts[s_buf[71]];
                s_buf[70] = palet16ts[s_buf[70]];
                s_buf[69] = palet16ts[s_buf[69]];
                s_buf[68] = palet16ts[s_buf[68]];
                s_buf[67] = palet16ts[s_buf[67]];
                s_buf[66] = palet16ts[s_buf[66]];
                s_buf[65] = palet16ts[s_buf[65]];
                s_buf[64] = palet16ts[s_buf[64]];
            case 6:
                s_buf[63] = palet16ts[s_buf[63]];
                s_buf[62] = palet16ts[s_buf[62]];
                s_buf[61] = palet16ts[s_buf[61]];
                s_buf[60] = palet16ts[s_buf[60]];
                s_buf[59] = palet16ts[s_buf[59]];
                s_buf[58] = palet16ts[s_buf[58]];
                s_buf[57] = palet16ts[s_buf[57]];
                s_buf[56] = palet16ts[s_buf[56]];
            case 5:
                s_buf[55] = palet16ts[s_buf[55]];
                s_buf[54] = palet16ts[s_buf[54]];
                s_buf[53] = palet16ts[s_buf[53]];
                s_buf[52] = palet16ts[s_buf[52]];
                s_buf[51] = palet16ts[s_buf[51]];
                s_buf[50] = palet16ts[s_buf[50]];
                s_buf[49] = palet16ts[s_buf[49]];
                s_buf[48] = palet16ts[s_buf[48]];
            case 4:
                s_buf[47] = palet16ts[s_buf[47]];
                s_buf[46] = palet16ts[s_buf[46]];
                s_buf[45] = palet16ts[s_buf[45]];
                s_buf[44] = palet16ts[s_buf[44]];
                s_buf[43] = palet16ts[s_buf[43]];
                s_buf[42] = palet16ts[s_buf[42]];
                s_buf[41] = palet16ts[s_buf[41]];
                s_buf[40] = palet16ts[s_buf[40]];
            case 3:
                s_buf[39] = palet16ts[s_buf[39]];
                s_buf[38] = palet16ts[s_buf[38]];
                s_buf[37] = palet16ts[s_buf[37]];
                s_buf[36] = palet16ts[s_buf[36]];
                s_buf[35] = palet16ts[s_buf[35]];
                s_buf[34] = palet16ts[s_buf[34]];
                s_buf[33] = palet16ts[s_buf[33]];
                s_buf[32] = palet16ts[s_buf[32]];
            case 2:
                s_buf[31] = palet16ts[s_buf[31]];
                s_buf[30] = palet16ts[s_buf[30]];
                s_buf[29] = palet16ts[s_buf[29]];
                s_buf[28] = palet16ts[s_buf[28]];
                s_buf[27] = palet16ts[s_buf[27]];
                s_buf[26] = palet16ts[s_buf[26]];
                s_buf[25] = palet16ts[s_buf[25]];
                s_buf[24] = palet16ts[s_buf[24]];
            case 1:
                s_buf[23] = palet16ts[s_buf[23]];
                s_buf[22] = palet16ts[s_buf[22]];
                s_buf[21] = palet16ts[s_buf[21]];
                s_buf[20] = palet16ts[s_buf[20]];
                s_buf[19] = palet16ts[s_buf[19]];
                s_buf[18] = palet16ts[s_buf[18]];
                s_buf[17] = palet16ts[s_buf[17]];
                s_buf[16] = palet16ts[s_buf[16]];
            }
        } else if (palet16ts[0] != 0) {
            int p = palet16ts[0];
            for (int i = 16; i < 16 + dw; i += 8) {
                s_buf[i] = p;
                s_buf[i + 1] = p;
                s_buf[i + 2] = p;
                s_buf[i + 3] = p;
                s_buf[i + 4] = p;
                s_buf[i + 5] = p;
                s_buf[i + 6] = p;
                s_buf[i + 7] = p;
            }
            sprite_existence = true;
        }
    }

    public void duplicateRaster(int dst) {
        int da = iw * dst, db = da + iw;
        switch (stretch_mode) {
        case 8:
            while (da < db) {
                bitmap[da] = bitmap[da - iw];
                bitmap[da + 1] = bitmap[da + 1 - iw];
                bitmap[da + 2] = bitmap[da + 2 - iw];
                bitmap[da + 3] = bitmap[da + 3 - iw];
                bitmap[da + 4] = bitmap[da + 4 - iw];
                bitmap[da + 5] = bitmap[da + 5 - iw];
                bitmap[da + 6] = bitmap[da + 6 - iw];
                bitmap[da + 7] = bitmap[da + 7 - iw];
                da += 8;
            }
            break;
        case 11:
            while (da < db) {
                bitmap[da] = bitmap[da - iw];
                bitmap[da + 1] = bitmap[da + 1 - iw];
                bitmap[da + 2] = bitmap[da + 2 - iw];
                bitmap[da + 3] = bitmap[da + 3 - iw];
                bitmap[da + 4] = bitmap[da + 4 - iw];
                bitmap[da + 5] = bitmap[da + 5 - iw];
                bitmap[da + 6] = bitmap[da + 6 - iw];
                bitmap[da + 7] = bitmap[da + 7 - iw];
                bitmap[da + 8] = bitmap[da + 8 - iw];
                bitmap[da + 9] = bitmap[da + 9 - iw];
                bitmap[da + 10] = bitmap[da + 10 - iw];
                da += 11;
            }
            break;
        case 14:
            while (da < db) {
                bitmap[da] = bitmap[da - iw];
                bitmap[da + 1] = bitmap[da + 1 - iw];
                bitmap[da + 2] = bitmap[da + 2 - iw];
                bitmap[da + 3] = bitmap[da + 3 - iw];
                bitmap[da + 4] = bitmap[da + 4 - iw];
                bitmap[da + 5] = bitmap[da + 5 - iw];
                bitmap[da + 6] = bitmap[da + 6 - iw];
                bitmap[da + 7] = bitmap[da + 7 - iw];
                bitmap[da + 8] = bitmap[da + 8 - iw];
                bitmap[da + 9] = bitmap[da + 9 - iw];
                bitmap[da + 10] = bitmap[da + 10 - iw];
                bitmap[da + 11] = bitmap[da + 11 - iw];
                bitmap[da + 12] = bitmap[da + 12 - iw];
                bitmap[da + 13] = bitmap[da + 13 - iw];
                da += 14;
            }
            break;
        case 21:
            while (da < db) {
                bitmap[da] = bitmap[da - iw];
                bitmap[da + 1] = bitmap[da + 1 - iw];
                bitmap[da + 2] = bitmap[da + 2 - iw];
                bitmap[da + 3] = bitmap[da + 3 - iw];
                bitmap[da + 4] = bitmap[da + 4 - iw];
                bitmap[da + 5] = bitmap[da + 5 - iw];
                bitmap[da + 6] = bitmap[da + 6 - iw];
                bitmap[da + 7] = bitmap[da + 7 - iw];
                bitmap[da + 8] = bitmap[da + 8 - iw];
                bitmap[da + 9] = bitmap[da + 9 - iw];
                bitmap[da + 10] = bitmap[da + 10 - iw];
                bitmap[da + 11] = bitmap[da + 11 - iw];
                bitmap[da + 12] = bitmap[da + 12 - iw];
                bitmap[da + 13] = bitmap[da + 13 - iw];
                bitmap[da + 14] = bitmap[da + 14 - iw];
                bitmap[da + 15] = bitmap[da + 15 - iw];
                bitmap[da + 16] = bitmap[da + 16 - iw];
                bitmap[da + 17] = bitmap[da + 17 - iw];
                bitmap[da + 18] = bitmap[da + 18 - iw];
                bitmap[da + 19] = bitmap[da + 19 - iw];
                bitmap[da + 20] = bitmap[da + 20 - iw];
                da += 21;
            }
            break;
        case 28:
            while (da < db) {
                bitmap[da] = bitmap[da - iw];
                bitmap[da + 1] = bitmap[da + 1 - iw];
                bitmap[da + 2] = bitmap[da + 2 - iw];
                bitmap[da + 3] = bitmap[da + 3 - iw];
                bitmap[da + 4] = bitmap[da + 4 - iw];
                bitmap[da + 5] = bitmap[da + 5 - iw];
                bitmap[da + 6] = bitmap[da + 6 - iw];
                bitmap[da + 7] = bitmap[da + 7 - iw];
                bitmap[da + 8] = bitmap[da + 8 - iw];
                bitmap[da + 9] = bitmap[da + 9 - iw];
                bitmap[da + 10] = bitmap[da + 10 - iw];
                bitmap[da + 11] = bitmap[da + 11 - iw];
                bitmap[da + 12] = bitmap[da + 12 - iw];
                bitmap[da + 13] = bitmap[da + 13 - iw];
                bitmap[da + 14] = bitmap[da + 14 - iw];
                bitmap[da + 15] = bitmap[da + 15 - iw];
                bitmap[da + 16] = bitmap[da + 16 - iw];
                bitmap[da + 17] = bitmap[da + 17 - iw];
                bitmap[da + 18] = bitmap[da + 18 - iw];
                bitmap[da + 19] = bitmap[da + 19 - iw];
                bitmap[da + 20] = bitmap[da + 20 - iw];
                bitmap[da + 21] = bitmap[da + 21 - iw];
                bitmap[da + 22] = bitmap[da + 22 - iw];
                bitmap[da + 23] = bitmap[da + 23 - iw];
                bitmap[da + 24] = bitmap[da + 24 - iw];
                bitmap[da + 25] = bitmap[da + 25 - iw];
                bitmap[da + 26] = bitmap[da + 26 - iw];
                bitmap[da + 27] = bitmap[da + 27 - iw];
                da += 28;
            }
            break;
        }
        if (dst < v1) {
            v1 = dst;
        }
        if (dst > v2) {
            v2 = dst;
        }
    }

    public boolean updateImage() {
        if (v1 > v2) {
            return false;
        }
        source.newPixels(0, v1, iw, v2 - v1 + 1);
        graphics.drawImage(image, offset_x, offset_y, null);
        v1 = ih - 1;
        v2 = 0;
        return true;
    }
}

