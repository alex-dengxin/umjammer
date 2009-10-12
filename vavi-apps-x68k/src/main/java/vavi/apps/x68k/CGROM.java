/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.InputStream;


class CGROM extends MemoryMappedDevice implements X68000Device {
    private X68000 x68000;

    public CGROM() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        return load_cgrom() || load_f16_f24() || create_cgrom();
    }

    public void reset() {
    }

    private boolean load_cgrom() {
        InputStream inputStream = x68000.openFile(x68000.getStringParameter("CGROM", "CGROM.DAT.gz"));
        if (inputStream == null) {
            return false;
        }
        return x68000.readFile(inputStream, m, 15728640, 786432, 131072, true) >= 0;
    }

    private boolean load_f16_f24() {
        InputStream inputStream = x68000.openFile(x68000.getStringParameter("F16", "GOL80.FON.gz"));
        if (inputStream == null) {
            return false;
        }
        if (x68000.readFile(inputStream, m, 15968256, 4096, 131072, false) < 0) {
            return false;
        }
        if (x68000.readFile(inputStream, m, 15728640, 45120, 131072, false) < 0) {
            return false;
        }
        if (x68000.readFile(inputStream, m, 15752704, 207552, 131072, true) < 0) {
            return false;
        }
        create_quarter_font(15966208, 15968256, 8, 256);
        inputStream = x68000.openFile(x68000.getStringParameter("F24", "MIN.F24.gz"));
        if (inputStream == null) {
            return false;
        }
        if (x68000.readFile(inputStream, m, 15978496, 12288, 131072, false) < 0) {
            return false;
        }
        if (x68000.readFile(inputStream, m, 15990784, 101520, 131072, false) < 0) {
            return false;
        }
        if (x68000.readFile(inputStream, m, 16044928, 466992, 131072, true) < 0) {
            return false;
        }
        create_quarter_font(15972352, 15978496, 12, 256);
        return true;
    }

    Image image;

    Graphics graphics;

    int bitmap[];

    int cnt;

    private boolean create_cgrom() {
        image = x68000.createImage(2256, 1848);
        graphics = image.getGraphics();
        bitmap = new int[379008];
        cnt = 0;
        x68000.monitor.outputString("フォント生成中...");
        if (!create_bitmap_font(15972352, 12, 12, ShiftJIS.asc2) || !create_bitmap_font(15728640, 16, 16, ShiftJIS.jis2) || !create_bitmap_font(15968256, 8, 16, ShiftJIS.asc1) || !create_hira_font(15968256, 15728640, 16) || !create_quarter_font(15966208, 15968256, 8, 256) || !create_bitmap_font(15990784, 24, 24, ShiftJIS.jis2) || !create_bitmap_font(15978496, 12, 24, ShiftJIS.asc1) || !create_hira_font(15978496, 15990784, 24)) {
            x68000.monitor.outputString("エラー\n");
            cnt = -1;
            return false;
        }
        x68000.monitor.outputString("完了\n");
        return true;
    }

    private boolean create_bitmap_font(int a, int u, int v, char table[]) {
        int l = table.length;
        int unx = u * 94, vny = v * 77;
        graphics.setClip(0, 0, unx, vny);
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, unx, vny);
        graphics.setFont(new Font(u < 24 ? "Monospaced" : "Serif", Font.PLAIN, v));
        for (int i = 0; i < l; i++) {
            if ((cnt & 255) == 0) {
                x68000.monitor.outputChar('.');
            }
            if (table[i] == 0) {
                continue;
            }
            int x = i % 94 * u;
            int y = i / 94 * v;
            graphics.setClip(x, y, u, v);
            graphics.setColor(Color.white);
            graphics.drawChars(table, i, 1, x, y + v - (v >>> 3));
            cnt++;
        }
        int y_block = -1;
        PixelGrabber pixelgrabber;
        switch (u) {
        case 8:
            for (int p = a, i = 0; i < l; i++) {
                if ((cnt & 255) == 0) {
                    x68000.monitor.outputChar('.');
                }
                if (table[i] == 0) {
                    p += v;
                    continue;
                }
                int x = i % 94 * 8;
                int y = i / 94;
                if (y / 7 != y_block) {
                    y_block = y / 7;
                    pixelgrabber = new PixelGrabber(image, 0, y_block * v * 7, unx, v * 7, bitmap, 0, unx);
                    try {
                        pixelgrabber.grabPixels();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
                y = (y - y_block * 7) * v;
                for (int j = 0; j < v; j++) {
                    int base = unx * (y + j) + x;
                    m[p++] = (byte) (((bitmap[base] & 16777215) != 0 ? 128 : 0) + ((bitmap[base + 1] & 16777215) != 0 ? 64 : 0) + ((bitmap[base + 2] & 16777215) != 0 ? 32 : 0) + ((bitmap[base + 3] & 16777215) != 0 ? 16 : 0) + ((bitmap[base + 4] & 16777215) != 0 ? 8 : 0) + ((bitmap[base + 5] & 16777215) != 0 ? 4 : 0) + ((bitmap[base + 6] & 16777215) != 0 ? 2 : 0) + ((bitmap[base + 7] & 16777215) != 0 ? 1 : 0));
                }
                cnt++;
            }
            break;
        case 12:
            for (int p = a, i = 0; i < l; i++) {
                if ((cnt & 255) == 0) {
                    x68000.monitor.outputChar('.');
                }
                if (table[i] == 0) {
                    p += 2 * v;
                    continue;
                }
                int x = i % 94 * 12;
                int y = i / 94;
                if (y / 7 != y_block) {
                    y_block = y / 7;
                    pixelgrabber = new PixelGrabber(image, 0, y_block * v * 7, unx, v * 7, bitmap, 0, unx);
                    try {
                        pixelgrabber.grabPixels();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
                y = (y - y_block * 7) * v;
                for (int j = 0; j < v; j++) {
                    int base = unx * (y + j) + x;
                    m[p++] = (byte) (((bitmap[base] & 16777215) != 0 ? 128 : 0) + ((bitmap[base + 1] & 16777215) != 0 ? 64 : 0) + ((bitmap[base + 2] & 16777215) != 0 ? 32 : 0) + ((bitmap[base + 3] & 16777215) != 0 ? 16 : 0) + ((bitmap[base + 4] & 16777215) != 0 ? 8 : 0) + ((bitmap[base + 5] & 16777215) != 0 ? 4 : 0) + ((bitmap[base + 6] & 16777215) != 0 ? 2 : 0) + ((bitmap[base + 7] & 16777215) != 0 ? 1 : 0));
                    m[p++] = (byte) (((bitmap[base + 8] & 16777215) != 0 ? 128 : 0) + ((bitmap[base + 9] & 16777215) != 0 ? 64 : 0) + ((bitmap[base + 10] & 16777215) != 0 ? 32 : 0) + ((bitmap[base + 11] & 16777215) != 0 ? 16 : 0));
                }
                cnt++;
            }
            break;
        case 16:
            for (int p = a, i = 0; i < l; i++) {
                if ((cnt & 255) == 0) {
                    x68000.monitor.outputChar('.');
                }
                if (table[i] == 0) {
                    p += 2 * v;
                    continue;
                }
                int x = i % 94 * 16;
                int y = i / 94;
                if (y / 7 != y_block) {
                    y_block = y / 7;
                    pixelgrabber = new PixelGrabber(image, 0, y_block * v * 7, unx, v * 7, bitmap, 0, unx);
                    try {
                        pixelgrabber.grabPixels();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
                y = (y - y_block * 7) * v;
                for (int j = 0; j < v; j++) {
                    int base = unx * (y + j) + x;
                    m[p++] = (byte) (((bitmap[base] & 16777215) != 0 ? 128 : 0) + ((bitmap[base + 1] & 16777215) != 0 ? 64 : 0) + ((bitmap[base + 2] & 16777215) != 0 ? 32 : 0) + ((bitmap[base + 3] & 16777215) != 0 ? 16 : 0) + ((bitmap[base + 4] & 16777215) != 0 ? 8 : 0) + ((bitmap[base + 5] & 16777215) != 0 ? 4 : 0) + ((bitmap[base + 6] & 16777215) != 0 ? 2 : 0) + ((bitmap[base + 7] & 16777215) != 0 ? 1 : 0));
                    m[p++] = (byte) (((bitmap[base + 8] & 16777215) != 0 ? 128 : 0) + ((bitmap[base + 9] & 16777215) != 0 ? 64 : 0) + ((bitmap[base + 10] & 16777215) != 0 ? 32 : 0) + ((bitmap[base + 11] & 16777215) != 0 ? 16 : 0) + ((bitmap[base + 12] & 16777215) != 0 ? 8 : 0) + ((bitmap[base + 13] & 16777215) != 0 ? 4 : 0) + ((bitmap[base + 14] & 16777215) != 0 ? 2 : 0) + ((bitmap[base + 15] & 16777215) != 0 ? 1 : 0));
                }
                cnt++;
            }
            break;
        case 24:
            for (int p = a, i = 0; i < l; i++) {
                if ((cnt & 255) == 0) {
                    x68000.monitor.outputChar('.');
                }
                if (table[i] == 0) {
                    p += 3 * v;
                    continue;
                }
                int x = i % 94 * 24;
                int y = i / 94;
                if (y / 7 != y_block) {
                    y_block = y / 7;
                    pixelgrabber = new PixelGrabber(image, 0, y_block * v * 7, unx, v * 7, bitmap, 0, unx);
                    try {
                        pixelgrabber.grabPixels();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
                y = (y - y_block * 7) * v;
                for (int j = 0; j < v; j++) {
                    int base = unx * (y + j) + x;
                    m[p++] = (byte) (((bitmap[base] & 16777215) != 0 ? 128 : 0) + ((bitmap[base + 1] & 16777215) != 0 ? 64 : 0) + ((bitmap[base + 2] & 16777215) != 0 ? 32 : 0) + ((bitmap[base + 3] & 16777215) != 0 ? 16 : 0) + ((bitmap[base + 4] & 16777215) != 0 ? 8 : 0) + ((bitmap[base + 5] & 16777215) != 0 ? 4 : 0) + ((bitmap[base + 6] & 16777215) != 0 ? 2 : 0) + ((bitmap[base + 7] & 16777215) != 0 ? 1 : 0));
                    m[p++] = (byte) (((bitmap[base + 8] & 16777215) != 0 ? 128 : 0) + ((bitmap[base + 9] & 16777215) != 0 ? 64 : 0) + ((bitmap[base + 10] & 16777215) != 0 ? 32 : 0) + ((bitmap[base + 11] & 16777215) != 0 ? 16 : 0) + ((bitmap[base + 12] & 16777215) != 0 ? 8 : 0) + ((bitmap[base + 13] & 16777215) != 0 ? 4 : 0) + ((bitmap[base + 14] & 16777215) != 0 ? 2 : 0) + ((bitmap[base + 15] & 16777215) != 0 ? 1 : 0));
                    m[p++] = (byte) (((bitmap[base + 16] & 16777215) != 0 ? 128 : 0) + ((bitmap[base + 17] & 16777215) != 0 ? 64 : 0) + ((bitmap[base + 18] & 16777215) != 0 ? 32 : 0) + ((bitmap[base + 19] & 16777215) != 0 ? 16 : 0) + ((bitmap[base + 20] & 16777215) != 0 ? 8 : 0) + ((bitmap[base + 21] & 16777215) != 0 ? 4 : 0) + ((bitmap[base + 22] & 16777215) != 0 ? 2 : 0) + ((bitmap[base + 23] & 16777215) != 0 ? 1 : 0));
                }
                cnt++;
            }
            break;
        }
        return true;
    }

    final private static int hira[] = {
        31, 0, 0, 0, 0, 0, 363, 282, 284, 286, 288, 290, 348, 350, 352, 316, 0, 283, 285, 287, 289, 291, 292, 294, 296, 298, 300, 302, 304, 306, 308, 310, 312, 314, 317, 319, 321, 323, 324, 325, 326, 327, 328, 331, 334, 337, 340, 343, 344, 345, 346, 347, 349, 351, 353, 354, 355, 356, 357, 358, 360, 364
    };

    private boolean create_hira_font(int a1, int a2, int v) {
        int u1 = v >>> 1, u2 = v;
        int i, j, k;
        int p1, p2;
        for (i = 160; i <= 221; i++) {
            if ((cnt & 255) == 0) {
                x68000.monitor.outputChar('.');
            }
            if (hira[i - 160] == 0) {
                continue;
            }
            p1 = a1 + (u1 + 7 >>> 3) * v * (i ^ 32);
            p2 = a2 + (u2 + 7 >>> 3) * v * hira[i - 160];
            for (j = 0; j < v; j++) {
                int src = 0, dst = 0;
                for (k = 0; k < u1; k++) {
                    if ((k & 3) == 0) {
                        src = m[p2++];
                    }
                    if ((src & 192 >> ((k & 3) << 1)) != 0) {
                        dst += 128 >> (k & 7);
                    }
                    if (k == u1 - 1 || (k & 7) == 7) {
                        m[p1++] = (byte) dst;
                        dst = 0;
                    }
                }
            }
            cnt++;
        }
        return true;
    }

    private boolean create_quarter_font(int a1, int a2, int v, int l) {
        int u = v, w = u + 7 >>> 3;
        int i, j, k;
        int p1 = a1;
        int p2 = a2;
        for (i = 0; i < l; i++) {
            for (j = 0; j < v; j++) {
                for (k = 0; k < w; k++) {
                    m[p1] = (byte) (m[p2] | m[p2 + w]);
                    p1++;
                    p2++;
                }
                p2 += w;
            }
            cnt++;
        }
        return true;
    }
}

