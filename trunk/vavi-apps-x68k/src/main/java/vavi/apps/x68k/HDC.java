/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class HDC extends MemoryMappedDevice implements X68000Device {
    private String fileNames[];

    private byte diskImages[][];

    private int sectorCount[];

    private X68000 x68000;

    public HDC() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        fileNames = new String[1];
        diskImages = new byte[1][];
        sectorCount = new int[1];
        for (int unitNumber = 0; unitNumber < 1; unitNumber++) {
            String unitName = "HD" + unitNumber;
            String fileName = x68000.getStringParameter(unitName, null);
            if (fileName == null) {
                continue;
            }
            byte diskImage[];
            try {
                diskImage = new byte[20748288];
            } catch (Exception e) {
                x68000.monitor.outputString(e.toString() + "\n");
                return false;
            }
            if (x68000.getFile(fileName, diskImage, 0, 4194304, 20748288, 131072) < 0) {
                return false;
            }
            diskImages[unitNumber] = diskImage;
            sectorCount[unitNumber] = 81048;
            if (readInt(diskImage, 1024) != 1479948363) {
                continue;
            }
            sectorCount[unitNumber] = readInt(diskImage, 1028);
            if (!(readInt(diskImage, 1040) == 1215655265 && readInt(diskImage, 1044) == 1849047147 && readInt(diskImage, 1048) == 33 && readInt(diskImage, 1052) == 162040)) {
                continue;
            }
            x68000.monitor.outputString("40MB‚©‚ç20MB‚Ö•ÏŠ·‚µ‚Ü‚·...");
            writeInt(diskImage, 1028, 81048);
            writeInt(diskImage, 1032, 81048);
            writeInt(diskImage, 1036, 87648);
            writeInt(diskImage, 1052, 81000);
            writeInt(diskImage, 8466, 67109122);
            writeInt(diskImage, 8470, 66048);
            writeInt(diskImage, 8474, 1327167528);
            boolean error = false;
            for (int i = 9476; i < 49782; i += 2) {
                int fat = ((diskImage[i] & 255) << 8) + (diskImage[i + 1] & 255);
                error |= fat >= 20155 && fat < 40335;
            }
            for (int i = 49782; i < 90142; i += 2) {
                int fat = ((diskImage[i] & 255) << 8) + (diskImage[i + 1] & 255);
                error |= fat != 0;
                diskImage[i] = 0;
                diskImage[i + 1] = 0;
            }
            for (int i = 173312; i < 20744448; i++) {
                diskImage[i - 81920] = diskImage[i];
            }
            sectorCount[unitNumber] = 81048;
            if (error) {
                x68000.monitor.outputString("ƒGƒ‰[\n");
                return false;
            } else {
                x68000.monitor.outputString("Š®—¹\n");
            }
        }
        return true;
    }

    public void reset() {
    }

    private int readInt(byte array[], int offset) {
        return ((array[offset] & 255) << 24) + ((array[offset + 1] & 255) << 16) + ((array[offset + 2] & 255) << 8) + (array[offset + 3] & 255);
    }

    private void writeInt(byte array[], int offset, int data) {
        array[offset] = (byte) (data >> 24);
        array[offset + 1] = (byte) (data >> 16);
        array[offset + 2] = (byte) (data >> 8);
        array[offset + 3] = (byte) data;
    }

    public int iocs(int r[]) throws MC68000Exception {
        int d0 = r[0], d1 = r[1], d2 = r[2], d3 = r[3], a1 = r[9];
        int unitNumber = d1 >> 8 & 15;
        if (unitNumber >= 1) {
            return -1;
        }
        byte diskImage[] = diskImages[unitNumber];
        if (diskImage == null) {
            return -1;
        }
        switch ((byte) d0) {
        case 64:
            if (d2 >= 81048) {
                return -1;
            }
            return 0;
        case 65:
            if (d2 >= 81048) {
                return -1;
            }
            for (int a = 256 * d2, i = 0; i < d3; i++) {
                if (diskImage[a + i] != x68000.read_byte(a1 + i)) {
                    return -1;
                }
            }
            return 0;
        case 66:
            return 0;
        case 67:
            return 0;
        case 68:
            return 0;
        case 69:
            if (d2 >= 81048) {
                return -1;
            }
            for (int a = 256 * d2, i = 0; i < d3; i++) {
                diskImage[a + i] = x68000.read_byte(a1 + i);
            }
            return 0;
        case 70:
            if (d2 >= 81048) {
                return -1;
            }
            for (int a = 256 * d2, i = 0; i < d3; i++) {
                x68000.write_byte(a1 + i, diskImage[a + i]);
            }
            return 0;
        case 71:
            return 0;
        case 72:
            return 0;
        case 73:
            return 0;
        case 74:
            return 0;
        case 75:
            return 0;
        case 76:
            return 0;
        case 77:
            if (d2 >= 81048) {
                return -1;
            }
            for (int a = 256 * d2, i = 0; i < 8448; i++) {
                diskImage[a + i] = 0;
            }
            return 0;
        case 78:
            return 0;
        case 79:
            return 0;
        }
        return -1;
    }
}

