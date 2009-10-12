/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class GraphicScreen extends MemoryMappedDevice implements X68000Device {

    private static GraphicScreen g4_0, g4_1, g4_2, g4_3;
    private static GraphicScreen g8_0, g8_1;
    private static GraphicScreen g16_0;
    private static GraphicScreen g4_4;

    static {
        g4_0 = new GraphicScreen4_0();
        g4_1 = new GraphicScreen4_1();
        g4_2 = new GraphicScreen4_2();
        g4_3 = new GraphicScreen4_3();
        g8_0 = new GraphicScreen8_0();
        g8_1 = new GraphicScreen8_1();
        g16_0 = new GraphicScreen16_0();
        g4_4 = new GraphicScreen4_4();
    }

    private int clear_plane;

    private static X68000 x68000;
    protected static CRTC crtc;
    protected static Video video;

    public GraphicScreen() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        crtc = x68000.crtc;
        video = x68000.video;
        reset();
        return true;
    }

    public void reset() {
        changeMode(0);
        clear_plane = 0;
    }

    public void setPlane(int plane) {
        clear_plane = plane & 15;
    }

    public void clear() {
        if ((clear_plane & 1) != 0) {
            for (int i = 12582912; i < 13107200; i += 8) {
                m[i + 1] = 0;
                m[i + 3] = 0;
                m[i + 5] = 0;
                m[i + 7] = 0;
            }
        }
        if ((clear_plane & 2) != 0) {
            for (int i = 13107200; i < 13631488; i += 8) {
                m[i + 1] = 0;
                m[i + 3] = 0;
                m[i + 5] = 0;
                m[i + 7] = 0;
            }
        }
        if ((clear_plane & 4) != 0) {
            for (int i = 13631488; i < 14155776; i += 8) {
                m[i + 1] = 0;
                m[i + 3] = 0;
                m[i + 5] = 0;
                m[i + 7] = 0;
            }
        }
        if ((clear_plane & 8) != 0) {
            for (int i = 14155776; i < 14680064; i += 8) {
                m[i + 1] = 0;
                m[i + 3] = 0;
                m[i + 5] = 0;
                m[i + 7] = 0;
            }
        }
    }

    public void changeMode(int mode) {
        if ((mode & 4) != 0) {
            x68000.map(g4_4, 12582912, 2097152, true, false);
        } else if ((mode & 2) != 0) {
            x68000.map(g16_0, 12582912, 524288, true, false);
            x68000.map(null, 13107200, 1572864, true, false);
        } else if ((mode & 1) != 0) {
            x68000.map(g8_0, 12582912, 524288, true, false);
            x68000.map(g8_1, 13107200, 524288, true, false);
            x68000.map(null, 13631488, 1048576, true, false);
        } else {
            x68000.map(g4_0, 12582912, 524288, true, false);
            x68000.map(g4_1, 13107200, 524288, true, false);
            x68000.map(g4_2, 13631488, 524288, true, false);
            x68000.map(g4_3, 14155776, 524288, true, false);
        }
    }
}

