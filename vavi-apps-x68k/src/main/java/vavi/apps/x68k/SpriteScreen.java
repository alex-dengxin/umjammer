/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class SpriteScreen extends MemoryMappedDevice implements X68000Device {
    public short sx[];
    public short sy[];
    public short snum[];
    public short scol[];
    public byte sprw[];
    public boolean sh[];
    public boolean sv[];
    public int rmap0[], rmap1[], rmap2[], rmap3[], rmap[][];
    public int spat[];
    public int pmap0[], pmap1[], pmap2[], pmap3[], pmap[][];
    public short r0, r1, r2, r3, r4, r5, r6, r7, r8;
    public short t0num[];
    public short t0col[];
    public boolean t0h[];
    public boolean t0v[];
    public short t1num[];
    public short t1col[];
    public boolean t1h[];
    public boolean t1v[];

    private X68000 x68000;
    private CRTC crtc;
    private Video video;

    public SpriteScreen() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        crtc = x68000.crtc;
        video = x68000.video;
        sx = new short[128];
        sy = new short[128];
        snum = new short[128];
        scol = new short[128];
        sprw = new byte[128];
        sh = new boolean[128];
        sv = new boolean[128];
        for (int i = 0; i <= 127; i++) {
            sx[i] = 0;
            sy[i] = 0;
            snum[i] = 0;
            scol[i] = 0;
            sprw[i] = 0;
            sh[i] = false;
            sv[i] = false;
        }
        rmap = new int[4][];
        rmap[0] = rmap0 = new int[1039];
        rmap[1] = rmap1 = new int[1039];
        rmap[2] = rmap2 = new int[1039];
        rmap[3] = rmap3 = new int[1039];
        for (int i = 0; i <= 1038; i++) {
            rmap0[i] = 0;
            rmap1[i] = 0;
            rmap2[i] = 0;
            rmap3[i] = 0;
        }
        spat = new int[8192];
        for (int i = 0; i <= 8191; i++) {
            spat[i] = 0;
        }
        pmap = new int[4][];
        pmap[0] = pmap0 = new int[256];
        pmap[1] = pmap1 = new int[256];
        pmap[2] = pmap2 = new int[256];
        pmap[3] = pmap3 = new int[256];
        for (int i = 0; i <= 255; i++) {
            pmap0[i] = 0;
            pmap1[i] = 0;
            pmap2[i] = 0;
            pmap3[i] = 0;
        }
        t0num = new short[4096];
        t0col = new short[4096];
        t0h = new boolean[4096];
        t0v = new boolean[4096];
        t1num = new short[4096];
        t1col = new short[4096];
        t1h = new boolean[4096];
        t1v = new boolean[4096];
        return true;
    }

    public void reset() {
    }

    public byte read_byte(int a) throws MC68000Exception {
        short s = read_short_big(a & -2);
        return (byte) ((a & 1) == 0 ? s >> 8 : s);
    }

    public short read_short_big(int a) throws MC68000Exception {
        if (a < 15401984) {
            short n = (short) (a >> 3 & 127);
            switch (a & 6) {
            case 0:
                return sx[n];
            case 2:
                return sy[n];
            case 4:
                return (short) ((sv[n] ? 32768 : 0) + (sh[n] ? 16384 : 0) + (scol[n] << 4) + snum[n]);
            case 6:
                return sprw[n];
            }
        } else if (a < 15433728) {
            switch (a) {
            case 15403008:
                return r0;
            case 15403010:
                return r1;
            case 15403012:
                return r2;
            case 15403014:
                return r3;
            case 15403016:
                return r4;
            case 15403018:
                return r5;
            case 15403020:
                return r6;
            case 15403022:
                return r7;
            case 15403024:
                return r8;
            }
            return 0;
        } else {
            int t = a >> 2 & 8191;
            return (short) ((a & 2) == 0 ? spat[t] >> 16 : spat[t]);
        }
        return 0;
    }

    public int read_int_big(int a) throws MC68000Exception {
        return (read_short_big(a) << 16) + (read_short_big(a + 2) & 65535);
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        short s = read_short_big(a & -2);
        if ((a & 1) == 0) {
            write_short_big(a, (short) ((b << 8) + (s & 255)));
        } else {
            write_short_big(a - 1, (short) ((s & 65280) + (b & 255)));
        }
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        if (a < 15401984) {
            short n = (short) (a >> 3 & 127);
            switch (a & 6) {
            case 0:
                sx[n] = (short) (s & 1023);
                if (sprw[n] != 0 && (video.r3 & 64) != 0) {
                    short y = sy[n];
                    int sc[] = crtc.sc;
                    if (y < 16) {
                        for (int i = 0; i < y; i++) {
                            sc[i]++;
                        }
                    } else {
                        sc[y - 16]++;
                        sc[y - 15]++;
                        sc[y - 14]++;
                        sc[y - 13]++;
                        sc[y - 12]++;
                        sc[y - 11]++;
                        sc[y - 10]++;
                        sc[y - 9]++;
                        sc[y - 8]++;
                        sc[y - 7]++;
                        sc[y - 6]++;
                        sc[y - 5]++;
                        sc[y - 4]++;
                        sc[y - 3]++;
                        sc[y - 2]++;
                        sc[y - 1]++;
                    }
                }
                break;
            case 2:
                s &= 1023;
                if (sy[n] != s) {
                    short y = sy[n];
                    sy[n] = s;
                    if (sprw[n] != 0) {
                        int map[] = rmap[n >> 5];
                        int mask = 1 << (n & 31);
                        map[y] -= mask;
                        map[y + 1] -= mask;
                        map[y + 2] -= mask;
                        map[y + 3] -= mask;
                        map[y + 4] -= mask;
                        map[y + 5] -= mask;
                        map[y + 6] -= mask;
                        map[y + 7] -= mask;
                        map[y + 8] -= mask;
                        map[y + 9] -= mask;
                        map[y + 10] -= mask;
                        map[y + 11] -= mask;
                        map[y + 12] -= mask;
                        map[y + 13] -= mask;
                        map[y + 14] -= mask;
                        map[y + 15] -= mask;
                        map[s] += mask;
                        map[s + 1] += mask;
                        map[s + 2] += mask;
                        map[s + 3] += mask;
                        map[s + 4] += mask;
                        map[s + 5] += mask;
                        map[s + 6] += mask;
                        map[s + 7] += mask;
                        map[s + 8] += mask;
                        map[s + 9] += mask;
                        map[s + 10] += mask;
                        map[s + 11] += mask;
                        map[s + 12] += mask;
                        map[s + 13] += mask;
                        map[s + 14] += mask;
                        map[s + 15] += mask;
                        if ((video.r3 & 64) != 0) {
                            int sc[] = crtc.sc;
                            if (y < 16) {
                                for (int i = 0; i < y; i++) {
                                    sc[i]++;
                                }
                            } else {
                                sc[y - 16]++;
                                sc[y - 15]++;
                                sc[y - 14]++;
                                sc[y - 13]++;
                                sc[y - 12]++;
                                sc[y - 11]++;
                                sc[y - 10]++;
                                sc[y - 9]++;
                                sc[y - 8]++;
                                sc[y - 7]++;
                                sc[y - 6]++;
                                sc[y - 5]++;
                                sc[y - 4]++;
                                sc[y - 3]++;
                                sc[y - 2]++;
                                sc[y - 1]++;
                            }
                            if (s < 16) {
                                for (int i = 0; i < s; i++) {
                                    sc[i]++;
                                }
                            } else {
                                sc[s - 16]++;
                                sc[s - 15]++;
                                sc[s - 14]++;
                                sc[s - 13]++;
                                sc[s - 12]++;
                                sc[s - 11]++;
                                sc[s - 10]++;
                                sc[s - 9]++;
                                sc[s - 8]++;
                                sc[s - 7]++;
                                sc[s - 6]++;
                                sc[s - 5]++;
                                sc[s - 4]++;
                                sc[s - 3]++;
                                sc[s - 2]++;
                                sc[s - 1]++;
                            }
                        }
                    }
                }
                break;
            case 4: {
                int num = snum[n];
                snum[n] = (short) (s & 255);
                scol[n] = (short) (s >> 4 & 240);
                sh[n] = (short) (s << 1) < 0;
                sv[n] = (short) s < 0;
                if (sprw[n] != 0) {
                    int map[] = pmap[n >> 5];
                    int mask = 1 << (n & 31);
                    map[num] -= mask;
                    map[snum[n]] += mask;
                    if ((video.r3 & 64) != 0) {
                        short y = sy[n];
                        int sc[] = crtc.sc;
                        if (y < 16) {
                            for (int i = 0; i < y; i++) {
                                sc[i]++;
                            }
                        } else {
                            sc[y - 16]++;
                            sc[y - 15]++;
                            sc[y - 14]++;
                            sc[y - 13]++;
                            sc[y - 12]++;
                            sc[y - 11]++;
                            sc[y - 10]++;
                            sc[y - 9]++;
                            sc[y - 8]++;
                            sc[y - 7]++;
                            sc[y - 6]++;
                            sc[y - 5]++;
                            sc[y - 4]++;
                            sc[y - 3]++;
                            sc[y - 2]++;
                            sc[y - 1]++;
                        }
                    }
                }
            }
                break;
            case 6:
                s &= 3;
                short prw = sprw[n];
                sprw[n] = (byte) s;
                if (prw != s) {
                    if (prw == 0) {
                        short y = sy[n];
                        int map[] = rmap[n >> 5];
                        int mask = 1 << (n & 31);
                        map[y] += mask;
                        map[y + 1] += mask;
                        map[y + 2] += mask;
                        map[y + 3] += mask;
                        map[y + 4] += mask;
                        map[y + 5] += mask;
                        map[y + 6] += mask;
                        map[y + 7] += mask;
                        map[y + 8] += mask;
                        map[y + 9] += mask;
                        map[y + 10] += mask;
                        map[y + 11] += mask;
                        map[y + 12] += mask;
                        map[y + 13] += mask;
                        map[y + 14] += mask;
                        map[y + 15] += mask;
                        pmap[n >> 5][snum[n]] += mask;
                    } else if (s == 0) {
                        short y = sy[n];
                        int map[] = rmap[n >> 5];
                        int mask = 1 << (n & 31);
                        map[y] -= mask;
                        map[y + 1] -= mask;
                        map[y + 2] -= mask;
                        map[y + 3] -= mask;
                        map[y + 4] -= mask;
                        map[y + 5] -= mask;
                        map[y + 6] -= mask;
                        map[y + 7] -= mask;
                        map[y + 8] -= mask;
                        map[y + 9] -= mask;
                        map[y + 10] -= mask;
                        map[y + 11] -= mask;
                        map[y + 12] -= mask;
                        map[y + 13] -= mask;
                        map[y + 14] -= mask;
                        map[y + 15] -= mask;
                        pmap[n >> 5][snum[n]] -= mask;
                    }
                    if ((video.r3 & 64) != 0) {
                        short y = sy[n];
                        int sc[] = crtc.sc;
                        if (y < 16) {
                            for (int i = 0; i < y; i++) {
                                sc[i]++;
                            }
                        } else {
                            sc[y - 16]++;
                            sc[y - 15]++;
                            sc[y - 14]++;
                            sc[y - 13]++;
                            sc[y - 12]++;
                            sc[y - 11]++;
                            sc[y - 10]++;
                            sc[y - 9]++;
                            sc[y - 8]++;
                            sc[y - 7]++;
                            sc[y - 6]++;
                            sc[y - 5]++;
                            sc[y - 4]++;
                            sc[y - 3]++;
                            sc[y - 2]++;
                            sc[y - 1]++;
                        }
                    }
                }
                break;
            }
            return;
        } else if (a < 15433728) {
            switch (a) {
            case 15403008:
                r0 = (short) (s & 1023);
                if ((video.r3 & 64) != 0 && (r4 & 1) != 0) {
                    crtc.updateAll();
                }
                return;
            case 15403010:
                r1 = (short) (s & 1023);
                if ((video.r3 & 64) != 0 && (r4 & 1) != 0) {
                    crtc.updateAll();
                }
                return;
            case 15403012:
                r2 = (short) (s & 1023);
                if ((video.r3 & 64) != 0 && (r4 & 8) != 0) {
                    crtc.updateAll();
                }
                return;
            case 15403014:
                r3 = (short) (s & 1023);
                if ((video.r3 & 64) != 0 && (r4 & 8) != 0) {
                    crtc.updateAll();
                }
                return;
            case 15403016:
                r4 = (short) (s & 2047);
                if ((video.r3 & 64) != 0) {
                    crtc.updateAll();
                }
                return;
            case 15403018:
                r5 = (short) (s & 255);
                return;
            case 15403020:
                r6 = (short) (s & 63);
                return;
            case 15403022:
                r7 = (short) (s & 255);
                return;
            case 15403024:
                r8 = (short) (s & 31);
                if ((video.r3 & 64) != 0) {
                    crtc.updateAll();
                }
                return;
            }
            return;
        } else {
            if (a >= 15458304) {
                int n = a >> 1 & 4095;
                t1num[n] = (short) ((s & 255) << 3);
                t1col[n] = (short) ((s & 3840) >> 4);
                t1h[n] = (short) (s << 1) < 0;
                t1v[n] = s < 0;
                if ((video.r3 & 64) != 0) {
                    if ((r4 & 7) == 3) {
                        int sc[] = crtc.sc;
                        if ((r8 & 3) == 0) {
                            int y = (n >> 6 << 3) - r1;
                            sc[y & 511]++;
                            sc[y + 1 & 511]++;
                            sc[y + 2 & 511]++;
                            sc[y + 3 & 511]++;
                            sc[y + 4 & 511]++;
                            sc[y + 5 & 511]++;
                            sc[y + 6 & 511]++;
                            sc[y + 7 & 511]++;
                        } else {
                            int y = (n >> 6 << 4) - r1;
                            sc[y & 1023]++;
                            sc[y + 1 & 1023]++;
                            sc[y + 2 & 1023]++;
                            sc[y + 3 & 1023]++;
                            sc[y + 4 & 1023]++;
                            sc[y + 5 & 1023]++;
                            sc[y + 6 & 1023]++;
                            sc[y + 7 & 1023]++;
                            sc[y + 8 & 1023]++;
                            sc[y + 9 & 1023]++;
                            sc[y + 10 & 1023]++;
                            sc[y + 11 & 1023]++;
                            sc[y + 12 & 1023]++;
                            sc[y + 13 & 1023]++;
                            sc[y + 14 & 1023]++;
                            sc[y + 15 & 1023]++;
                        }
                    }
                    if ((r4 & 56) == 24) {
                        int sc[] = crtc.sc;
                        int y = (n >> 6 << 3) - r3;
                        sc[y & 511]++;
                        sc[y + 1 & 511]++;
                        sc[y + 2 & 511]++;
                        sc[y + 3 & 511]++;
                        sc[y + 4 & 511]++;
                        sc[y + 5 & 511]++;
                        sc[y + 6 & 511]++;
                        sc[y + 7 & 511]++;
                    }
                }
            } else if (a >= 15450112) {
                int n = a >> 1 & 4095;
                t0num[n] = (short) ((s & 255) << 3);
                t0col[n] = (short) ((s & 3840) >> 4);
                t0h[n] = (short) (s << 1) < 0;
                t0v[n] = s < 0;
                if ((video.r3 & 64) != 0) {
                    if ((r4 & 7) == 1) {
                        int sc[] = crtc.sc;
                        if ((r8 & 3) == 0) {
                            int y = (n >> 6 << 3) - r1;
                            sc[y & 511]++;
                            sc[y + 1 & 511]++;
                            sc[y + 2 & 511]++;
                            sc[y + 3 & 511]++;
                            sc[y + 4 & 511]++;
                            sc[y + 5 & 511]++;
                            sc[y + 6 & 511]++;
                            sc[y + 7 & 511]++;
                        } else {
                            int y = (n >> 6 << 4) - r1;
                            sc[y & 1023]++;
                            sc[y + 1 & 1023]++;
                            sc[y + 2 & 1023]++;
                            sc[y + 3 & 1023]++;
                            sc[y + 4 & 1023]++;
                            sc[y + 5 & 1023]++;
                            sc[y + 6 & 1023]++;
                            sc[y + 7 & 1023]++;
                            sc[y + 8 & 1023]++;
                            sc[y + 9 & 1023]++;
                            sc[y + 10 & 1023]++;
                            sc[y + 11 & 1023]++;
                            sc[y + 12 & 1023]++;
                            sc[y + 13 & 1023]++;
                            sc[y + 14 & 1023]++;
                            sc[y + 15 & 1023]++;
                        }
                    }
                    if ((r4 & 56) == 8) {
                        int sc[] = crtc.sc;
                        int y = (n >> 6 << 3) - r3;
                        sc[y & 511]++;
                        sc[y + 1 & 511]++;
                        sc[y + 2 & 511]++;
                        sc[y + 3 & 511]++;
                        sc[y + 4 & 511]++;
                        sc[y + 5 & 511]++;
                        sc[y + 6 & 511]++;
                        sc[y + 7 & 511]++;
                    }
                }
            }
            int t = a >> 2 & 8191;
            spat[t] = (a & 2) == 0 ? (s << 16) + (spat[t] & 65535) : (spat[t] & -65536) + (s & 65535);
            if ((video.r3 & 64) != 0) {
                short num = (short) (a >> 7 & 255);
                for (int i = 96; i >= 0; i -= 32) {
                    int map = pmap[i >> 5][num];
                    if (map == 0) {
                        continue;
                    }
                    for (int n = i + 31; n >= i; n--) {
                        if (map < 0) {
                            short y = sy[n];
                            int sc[] = crtc.sc;
                            if (y < 16) {
                                for (int k = 0; k < y; k++) {
                                    sc[k]++;
                                }
                            } else {
                                sc[y - 16]++;
                                sc[y - 15]++;
                                sc[y - 14]++;
                                sc[y - 13]++;
                                sc[y - 12]++;
                                sc[y - 11]++;
                                sc[y - 10]++;
                                sc[y - 9]++;
                                sc[y - 8]++;
                                sc[y - 7]++;
                                sc[y - 6]++;
                                sc[y - 5]++;
                                sc[y - 4]++;
                                sc[y - 3]++;
                                sc[y - 2]++;
                                sc[y - 1]++;
                            }
                        }
                        map <<= 1;
                    }
                }
            }
            return;
        }
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        write_short_big(a, (short) (i >> 16));
        write_short_big(a + 2, (short) i);
    }
}

