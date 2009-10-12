/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class FDC extends MemoryMappedDevice implements X68000Device {

    private String fileNames[];
    private byte diskImages[][];

    private X68000 x68000;

    public FDC() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        fileNames = new String[2];
        diskImages = new byte[2][];
        for (int unitNumber = 0; unitNumber < 2; unitNumber++) {
            String unitName = "FD" + unitNumber;
            String fileName = x68000.getStringParameter(unitName, null);
            if (fileName == null) {
                continue;
            }
            diskImages[unitNumber] = new byte[1261568];
            if (x68000.getFile(fileName, diskImages[unitNumber], 0, 1261568, 131072) < 0) {
                return false;
            }
        }
        return true;
    }

    public void reset() {
    }

    public void iocs(int r[]) throws MC68000Exception {
        int unitNumber = r[1] >> 8 & 15;
        if (unitNumber >= 2) {
            r[0] = -1;
            return;
        }
        switch ((byte) r[0]) {
        case 64:
            r[0] = sector(r[2]) >= 0 ? 0 : -1;
            return;
        case 66:
        case 70: {
            int pos = 1024 * sector(r[2]);
            int length = r[3];
            int a = r[9];
            for (int i = 0; i < length; i++) {
                x68000.write_byte(a + i, diskImages[unitNumber][pos + i]);
            }
        }
        case 65:
        case 67:
        case 71:
        case 79:
            r[0] = 0;
            return;
        case 69: {
            int pos = 1024 * sector(r[2]);
            int length = r[3];
            int a = r[9];
            for (int i = 0; i < length; i++) {
                diskImages[unitNumber][pos + i] = x68000.read_byte(a + i);
            }
        }
            r[0] = 0;
            return;
        case 68:
            r[0] = 1610612736;
            return;
        case 74:
            x68000.write_byte(3222 + (unitNumber << 3), (byte) 3);
            r[0] = 0;
            return;
        case 78:
            if ((r[2] & 65535) < 8) {
                r[0] = 66;
                return;
            } else if ((r[2] & 65535) == 8) {
                r[0] = 1;
                return;
            }
        case 72:
        case 73:
        case 75:
        case 76:
        case 77:
            r[0] = -1;
            return;
        }
        r[0] = -1;
        return;
    }

    private int sector(int pos) {
        int mode = pos >> 24 & 255;
        int track = pos >> 16 & 255;
        int side = pos >> 8 & 255;
        int sector = pos - 1 & 255;
        if (mode != 3 || track >= 77 || side >= 2 || sector >= 8) {
            return -1;
        }
        return (track * 2 + side) * 8 + sector;
    }
}

