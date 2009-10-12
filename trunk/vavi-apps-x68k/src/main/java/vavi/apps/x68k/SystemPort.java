/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class SystemPort extends MemoryMappedDevice implements X68000Device, InterruptDevice {
    private int contrast;

    private X68000 x68000;

    public SystemPort() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        return true;
    }

    public void reset() {
        x68000.writeProtect(15532032, 16384, true);
        contrast = 15;
    }

    public int acknowledge() {
        return 31;
    }

    public void done(int vector) {
    }

    public void nmi() {
        m[5972] = -1;
        m[5973] = -1;
        m[124] = 0;
        m[125] = -2;
        m[126] = 0;
        m[127] = 90;
        x68000.interrupt_request_system_port++;
    }

    public byte read_byte(int a) throws MC68000Exception {
        switch (a) {
        case 15261697:
            return (byte) (240 + contrast);
        case 15261699:
            return (byte) -9;
        case 15261701:
            return (byte) -1;
        case 15261703:
            return (byte) (253 + (x68000.crtc.getHRL() << 1));
        case 15261705:
            return (byte) -1;
        case 15261707:
            return (byte) -1;
        case 15261709:
            return (byte) -1;
        case 15261711:
            return (byte) -1;
        }
        return (byte) -1;
    }

    public short read_short_big(int a) throws MC68000Exception {
        return (short) ((read_byte(a) << 8) + (read_byte(a + 1) & 255));
    }

    public int read_int_big(int a) throws MC68000Exception {
        return (read_short_big(a) << 16) + (read_short_big(a + 2) & 65535);
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        switch (a) {
        case 15261697:
            contrast = b & 15;
            x68000.crtc.setContrast(contrast);
            return;
        case 15261699:
            return;
        case 15261701:
            return;
        case 15261703:
            x68000.crtc.setHRL(b >> 1 & 1);
            return;
        case 15261705:
            return;
        case 15261707:
            return;
        case 15261709:
            x68000.writeProtect(15532032, 16384, b != 49);
            return;
        case 15261711:
            return;
        }
        return;
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        write_byte(a, (byte) (s >> 8));
        write_byte(a + 1, (byte) s);
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        write_short_big(a, (short) (i >> 16));
        write_short_big(a + 2, (short) i);
    }
}

