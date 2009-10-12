/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class DMAC extends MemoryMappedDevice implements X68000Device, InterruptDevice {

    private DMACChannel channel[];
    private int bt, br;
    private int interrupt_request[];
    private int interrupt_acknowledged[];

    protected X68000 x68000;

    public DMAC() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        channel = new DMACChannel[4];
        for (int i = 0; i <= 3; i++) {
            channel[i] = new DMACChannel(i);
        }
        interrupt_request = new int[8];
        interrupt_acknowledged = new int[8];
        reset();
        return true;
    }

    public void reset() {
        for (int i = 0; i <= 3; i++) {
            channel[i].reset();
        }
        for (int i = 0; i <= 7; i++) {
            interrupt_request[i] = 0;
            interrupt_acknowledged[i] = 0;
        }
    }

    private void interrupt(int i) {
        interrupt_request[i]++;
        x68000.interrupt_request_dmac++;
    }

    public int acknowledge() {
        for (int i = 0; i <= 7; i++) {
            int request = interrupt_request[i];
            if (request != interrupt_acknowledged[i]) {
                interrupt_acknowledged[i] = request;
                return (i & 1) == 0 ? channel[i >> 1].niv : channel[i >> 1].eiv;
            }
        }
        return 0;
    }

    public void done(int vector) {
        for (int i = 0; i <= 7; i++) {
            if (interrupt_request[i] != interrupt_acknowledged[i]) {
                x68000.interrupt_request_dmac++;
                return;
            }
        }
    }

    public byte read_byte(int a) {
        if ((a & 255) == 255) {
            return (byte) (bt + br);
        }
        return channel[a >> 6 & 3].read_byte(a);
    }

    public short read_short_big(int a) {
        return channel[a >> 6 & 3].read_short_big(a);
    }

    public int read_int_big(int a) {
        return channel[a >> 6 & 3].read_int_big(a);
    }

    public void write_byte(int a, byte b) {
        if ((a & 255) == 255) {
            bt = b & 12;
            br = b & 3;
            return;
        }
        channel[a >> 6 & 3].write_byte(a, b);
    }

    public void write_short_big(int a, short s) {
        channel[a >> 6 & 3].write_short_big(a, s);
    }

    public void write_int_big(int a, int i) {
        channel[a >> 6 & 3].write_int_big(a, i);
    }

    public void fallPCL3() {
        channel[3].fallPCL();
    }

    public void risePCL3() {
        channel[3].risePCL();
    }

    class DMACChannel {
        int ch;
        int coc;
        int blc;
        int ndt;
        int err;
        int act;
        int dit;
        int pct;
        int pcs;
        int error_code;
        int xrm;
        int dtyp;
        int dps;
        int pcl;
        int dir;
        int btd;
        int size;
        int chain;
        int reqg;
        int mac;
        int dac;
        int str;
        int cnt;
        int hlt;
        int sab;
        int ite;
        int niv;
        int eiv;
        int cp;
        int mfc2;
        int mfc1;
        int mfc0;
        int dfc2;
        int dfc1;
        int dfc0;
        int bfc2;
        int bfc1;
        int bfc0;
        int mtc;
        int btc;
        int mar;
        int dar;
        int bar;

        DMACChannel(int ch) {
            this.ch = ch;
            pcs = 0;
        }

        void reset() {
            coc = 0;
            blc = 0;
            ndt = 0;
            err = 0;
            act = 0;
            dit = 0;
            pct = 0;
            error_code = 0;
            xrm = 0;
            dtyp = 0;
            dps = 0;
            pcl = 0;
            dir = 0;
            btd = 0;
            size = 0;
            chain = 0;
            reqg = 0;
            mac = 0;
            dac = 0;
            str = 0;
            cnt = 0;
            hlt = 0;
            sab = 0;
            ite = 0;
            niv = 15;
            eiv = 15;
            cp = 0;
            mfc2 = 0;
            mfc1 = 0;
            mfc0 = 0;
            dfc2 = 0;
            dfc1 = 0;
            dfc0 = 0;
            bfc2 = 0;
            bfc1 = 0;
            bfc0 = 0;
            mtc = 0;
            btc = 0;
            mar = 0;
            dar = 0;
            bar = 0;
        }

        byte read_byte(int a) {
            switch (a & 63) {
            case 0:
                return (byte) (coc + blc + ndt + err + act + dit + pct + pcs);
            case 1:
                return (byte) error_code;
            case 4:
                return (byte) (xrm + dtyp + dps + pcl);
            case 5:
                return (byte) (dir + btd + size + chain + reqg);
            case 6:
                return (byte) (mac + dac);
            case 7:
                return (byte) (str + cnt + hlt + sab + ite);
            case 37:
                return (byte) niv;
            case 39:
                return (byte) eiv;
            case 45:
                return (byte) cp;
            case 41:
                return (byte) (mfc2 + mfc1 + mfc0);
            case 49:
                return (byte) (dfc2 + dfc1 + dfc0);
            case 57:
                return (byte) (bfc2 + bfc1 + bfc0);
            case 10:
                return (byte) (mtc >> 8);
            case 11:
                return (byte) mtc;
            case 26:
                return (byte) (btc >> 8);
            case 27:
                return (byte) btc;
            case 12:
                return (byte) (mar >> 24);
            case 13:
                return (byte) (mar >> 16);
            case 14:
                return (byte) (mar >> 8);
            case 15:
                return (byte) mar;
            case 20:
                return (byte) (dar >> 24);
            case 21:
                return (byte) (dar >> 16);
            case 22:
                return (byte) (dar >> 8);
            case 23:
                return (byte) dar;
            case 28:
                return (byte) (bar >> 24);
            case 29:
                return (byte) (bar >> 16);
            case 30:
                return (byte) (bar >> 8);
            case 31:
                return (byte) bar;
            }
            return 0;
        }

        short read_short_big(int a) {
            switch (a & 63) {
            case 10:
                return (short) mtc;
            case 26:
                return (short) btc;
            case 12:
                return (short) (mar >> 16);
            case 14:
                return (short) mar;
            case 20:
                return (short) (dar >> 16);
            case 22:
                return (short) dar;
            case 28:
                return (short) (bar >> 16);
            case 30:
                return (short) bar;
            }
            return (short) ((read_byte(a) << 8) + (read_byte(a + 1) & 255));
        }

        int read_int_big(int a) {
            switch (a & 63) {
            case 12:
                return mar;
            case 20:
                return dar;
            case 28:
                return bar;
            }
            return (read_byte(a) << 24) + ((read_byte(a + 1) & 255) << 16) + ((read_byte(a + 2) & 255) << 8) + (read_byte(a + 3) & 255);
        }

        void write_byte(int a, byte b) {
            switch (a & 63) {
            case 0:
                if (b == -1) {
                    coc = 0;
                    blc = 0;
                    ndt = 0;
                    err = 0;
                }
                return;
            case 4:
                if (act != 0) {
                    error(2);
                    return;
                }
                xrm = b & 192;
                dtyp = b & 48;
                dps = b & 8;
                pcl = b & 3;
                return;
            case 5:
                dir = b & 128;
                btd = b & 64;
                size = b & 48;
                chain = b & 12;
                reqg = b & 3;
                return;
            case 6:
                if (act != 0) {
                    error(2);
                    return;
                }
                mac = b & 12;
                dac = b & 3;
                return;
            case 7:
                hlt = b & 32;
                ite = b & 8;
                if ((b & 64) != 0) {
                    if (act == 0 && (b & 128) == 0 || blc != 0) {
                        error(2);
                        return;
                    }
                    if (chain != 0) {
                        error(1);
                        return;
                    }
                    cnt = 64;
                }
                if ((b & 16) != 0) {
                    if (act == 0 && (b & 128) == 0) {
                        return;
                    }
                    coc = 0;
                    blc = 0;
                    ndt = 0;
                    hlt = 0;
                    cnt = 0;
                    error(17);
                    return;
                }
                if ((b & 128) != 0) {
                    if (coc + blc + ndt + err + act != 0) {
                        error(2);
                        return;
                    }
                    if ((dtyp == 0 || dtyp == 16) && dps == 8 && size == 0 && (reqg == 2 || reqg == 3) || xrm == 64 || mac == 12 || dac == 3 || chain == 4 || size == 3 && !((dtyp == 0 || dtyp == 16) && dps == 0)) {
                        error(1);
                        return;
                    }
                    act = 8;
                    if (chain == 8) {
                        if (btc == 0) {
                            error(15);
                            return;
                        }
                        if ((bar & 1) != 0) {
                            error(7);
                            return;
                        }
                        try {
                            mar = x68000.read_int_big(bar, bfc2);
                            bar += 4;
                            mtc = x68000.read_short_big(bar, bfc2) & 65535;
                            bar += 2;
                        } catch (MC68000Exception e) {
                            error(11);
                            return;
                        }
                        btc--;
                    } else if (chain == 12) {
                        if ((bar & 1) != 0) {
                            error(7);
                            return;
                        }
                        try {
                            mar = x68000.read_int_big(bar, bfc2);
                            bar += 4;
                            mtc = x68000.read_short_big(bar, bfc2) & 65535;
                            bar += 2;
                            bar = x68000.read_int_big(bar, bfc2);
                        } catch (MC68000Exception e) {
                            error(11);
                            return;
                        }
                    }
                    if (mtc == 0) {
                        error(13);
                        return;
                    }
                    if ((size == 16 || size == 32) && (mar & 1) != 0) {
                        error(5);
                        return;
                    }
                    if ((size == 16 || size == 32) && dps == 8 && (dar & 1) != 0) {
                        error(6);
                        return;
                    }
                    switch (reqg) {
                    case 0:
                    case 1:
                        while (act != 0) {
                            transfer();
                        }
                        break;
                    case 3:
                        transfer();
                        break;
                    }
                }
                return;
            case 37:
                niv = b & 255;
                return;
            case 39:
                eiv = b & 255;
                return;
            case 45:
                cp = b & 3;
                return;
            case 41:
                if (act != 0) {
                    error(2);
                    return;
                }
                mfc2 = b & 4;
                mfc1 = b & 2;
                mfc0 = b & 1;
                return;
            case 49:
                if (act != 0) {
                    error(2);
                    return;
                }
                dfc2 = b & 4;
                dfc1 = b & 2;
                dfc0 = b & 1;
                return;
            case 57:
                bfc2 = b & 4;
                bfc1 = b & 2;
                bfc0 = b & 1;
                return;
            case 10:
                if (act != 0) {
                    error(2);
                    return;
                }
                mtc = (mtc & 255) + ((b & 255) << 8);
                return;
            case 11:
                if (act != 0) {
                    error(2);
                    return;
                }
                mtc = (mtc & 65280) + (b & 255);
                return;
            case 26:
                btc = (btc & 255) + ((b & 255) << 8);
                return;
            case 27:
                btc = (btc & 65280) + (b & 255);
                return;
            case 12:
                if (act != 0) {
                    error(2);
                    return;
                }
                mar = (mar & 16777215) + ((b & 255) << 24);
                return;
            case 13:
                if (act != 0) {
                    error(2);
                    return;
                }
                mar = (mar & -16711681) + ((b & 255) << 16);
                return;
            case 14:
                if (act != 0) {
                    error(2);
                    return;
                }
                mar = (mar & -65281) + ((b & 255) << 8);
                return;
            case 15:
                if (act != 0) {
                    error(2);
                    return;
                }
                mar = (mar & -256) + (b & 255);
                return;
            case 20:
                if (act != 0) {
                    error(2);
                    return;
                }
                dar = (dar & 16777215) + ((b & 255) << 24);
                return;
            case 21:
                if (act != 0) {
                    error(2);
                    return;
                }
                dar = (dar & -16711681) + ((b & 255) << 16);
                return;
            case 22:
                if (act != 0) {
                    error(2);
                    return;
                }
                dar = (dar & -65281) + ((b & 255) << 8);
                return;
            case 23:
                if (act != 0) {
                    error(2);
                    return;
                }
                dar = (dar & -256) + (b & 255);
                return;
            case 28:
                bar = (bar & 16777215) + ((b & 255) << 24);
                return;
            case 29:
                bar = (bar & -16711681) + ((b & 255) << 16);
                return;
            case 30:
                bar = (bar & -65281) + ((b & 255) << 8);
                return;
            case 31:
                bar = (bar & -256) + (b & 255);
                return;
            }
        }

        void write_short_big(int a, short s) {
            switch (a & 63) {
            case 10:
                if (act != 0) {
                    error(2);
                    return;
                }
                mtc = s & 65535;
                return;
            case 26:
                btc = s & 65535;
                return;
            case 12:
                if (act != 0) {
                    error(2);
                    return;
                }
                mar = (mar & 65535) + ((s & 65535) << 16);
                return;
            case 14:
                if (act != 0) {
                    error(2);
                    return;
                }
                mar = (mar & -65536) + (s & 65535);
                return;
            case 20:
                if (act != 0) {
                    error(2);
                    return;
                }
                dar = (dar & 65535) + ((s & 65535) << 16);
                return;
            case 22:
                if (act != 0) {
                    error(2);
                    return;
                }
                dar = (dar & -65536) + (s & 65535);
                return;
            case 28:
                bar = (bar & 65535) + ((s & 65535) << 16);
                return;
            case 30:
                bar = (bar & -65536) + (s & 65535);
                return;
            }
            write_byte(a, (byte) (s >> 8));
            write_byte(a + 1, (byte) s);
        }

        void write_int_big(int a, int i) {
            switch (a & 63) {
            case 12:
                if (act != 0) {
                    error(2);
                    return;
                }
                mar = i;
                return;
            case 20:
                if (act != 0) {
                    error(2);
                    return;
                }
                dar = i;
                return;
            case 28:
                bar = i;
                return;
            }
            write_byte(a, (byte) (i >> 24));
            write_byte(a + 1, (byte) (i >> 16));
            write_byte(a + 2, (byte) (i >> 8));
            write_byte(a + 3, (byte) i);
        }

        void complete() {
            err = 0;
            act = 0;
            str = 0;
            cnt = 0;
            sab = 0;
            error_code = 0;
            if (ite != 0) {
                interrupt(ch << 1);
            }
        }

        private void error(int code) {
            err = 16;
            act = 0;
            str = 0;
            cnt = 0;
            sab = 0;
            error_code = code;
            if (ite != 0) {
                interrupt((ch << 1) + 1);
            }
        }

        void transfer() {
            int code = 0;
            try {
                switch (size) {
                case 0:
                case 48:
                    if (dps == 0) {
                        if (dir == 0) {
                            code = 9;
                            byte data = x68000.read_byte(mar, mfc2);
                            if (mac == 4) {
                                mar += 1;
                            } else if (mac == 8) {
                                mar -= 1;
                            }
                            code = 10;
                            x68000.write_byte(dar, data, dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                        } else {
                            code = 10;
                            byte data = x68000.read_byte(dar, dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            code = 9;
                            x68000.write_byte(mar, data, mfc2);
                            if (mac == 4) {
                                mar += 1;
                            } else if (mac == 8) {
                                mar -= 1;
                            }
                        }
                    } else {
                        if (dir == 0) {
                            code = 9;
                            byte data = x68000.read_byte(mar, mfc2);
                            if (mac == 4) {
                                mar += 1;
                            } else if (mac == 8) {
                                mar -= 1;
                            }
                            code = 10;
                            x68000.write_byte(dar, data, dfc2);
                            if (dac == 1) {
                                dar += 1;
                            } else if (dac == 2) {
                                dar -= 1;
                            }
                        } else {
                            code = 10;
                            byte data = x68000.read_byte(dar, dfc2);
                            if (dac == 1) {
                                dar += 1;
                            } else if (dac == 2) {
                                dar -= 1;
                            }
                            code = 9;
                            x68000.write_byte(mar, (byte) data, mfc2);
                            if (mac == 4) {
                                mar += 1;
                            } else if (mac == 8) {
                                mar -= 1;
                            }
                        }
                    }
                    break;
                case 16:
                    if (dps == 0) {
                        if (dir == 0) {
                            code = 9;
                            short data = x68000.read_short_big(mar, mfc2);
                            if (mac == 4) {
                                mar += 2;
                            } else if (mac == 8) {
                                mar -= 2;
                            }
                            code = 10;
                            x68000.write_byte(dar, (byte) (data >> 8), dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            x68000.write_byte(dar, (byte) data, dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                        } else {
                            code = 10;
                            short data = (short) (x68000.read_byte(dar, dfc2) << 8);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            data += x68000.read_byte(dar, dfc2) & 255;
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            code = 9;
                            x68000.write_short_big(mar, data, mfc2);
                            if (mac == 4) {
                                mar += 2;
                            } else if (mac == 8) {
                                mar -= 2;
                            }
                        }
                    } else {
                        if (dir == 0) {
                            code = 9;
                            short data = x68000.read_short_big(mar, mfc2);
                            if (mac == 4) {
                                mar += 2;
                            } else if (mac == 8) {
                                mar -= 2;
                            }
                            code = 10;
                            x68000.write_short_big(dar, data, dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                        } else {
                            code = 10;
                            short data = x68000.read_short_big(dar, dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            code = 9;
                            x68000.write_short_big(mar, data, mfc2);
                            if (mac == 4) {
                                mar += 2;
                            } else if (mac == 8) {
                                mar -= 2;
                            }
                        }
                    }
                    break;
                case 32:
                    if (dps == 0) {
                        if (dir == 0) {
                            code = 9;
                            int data = x68000.read_int_big(mar, mfc2);
                            if (mac == 4) {
                                mar += 4;
                            } else if (mac == 8) {
                                mar -= 4;
                            }
                            code = 10;
                            x68000.write_byte(dar, (byte) (data >> 24), dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            x68000.write_byte(dar, (byte) (data >> 16), dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            x68000.write_byte(dar, (byte) (data >> 8), dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            x68000.write_byte(dar, (byte) data, dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                        } else {
                            code = 10;
                            int data = x68000.read_byte(dar, dfc2) << 24;
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            data += (x68000.read_byte(dar, dfc2) & 255) << 16;
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            data += (x68000.read_byte(dar, dfc2) & 255) << 8;
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            data += x68000.read_byte(dar, dfc2) & 255;
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            code = 9;
                            x68000.write_int_big(mar, data, mfc2);
                            if (mac == 4) {
                                mar += 4;
                            } else if (mac == 8) {
                                mar -= 4;
                            }
                        }
                    } else {
                        if (dir == 0) {
                            code = 9;
                            int data = x68000.read_int_big(mar, mfc2);
                            if (mac == 4) {
                                mar += 4;
                            } else if (mac == 8) {
                                mar -= 4;
                            }
                            code = 10;
                            x68000.write_short_big(dar, (short) (data >> 16), dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            x68000.write_short_big(dar, (short) data, dfc2);
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                        } else {
                            code = 10;
                            int data = x68000.read_short_big(dar, dfc2) << 16;
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            data += x68000.read_short_big(dar, dfc2) & 65535;
                            if (dac == 1) {
                                dar += 2;
                            } else if (dac == 2) {
                                dar -= 2;
                            }
                            code = 9;
                            x68000.write_int_big(mar, data, mfc2);
                            if (mac == 4) {
                                mar += 4;
                            } else if (mac == 8) {
                                mar -= 4;
                            }
                        }
                    }
                    break;
                }
            } catch (MC68000Exception e) {
                error(code);
                return;
            }
            mtc--;
            if (mtc != 0) {
                return;
            }
            if (chain == 8) {
                if (btc == 0) {
                    coc = 128;
                    blc = 64;
                    ndt = 0;
                    complete();
                    return;
                }
                try {
                    mar = x68000.read_int_big(bar, bfc2);
                    bar += 4;
                    mtc = x68000.read_short_big(bar, bfc2) & 65535;
                    bar += 2;
                } catch (MC68000Exception e) {
                    error(11);
                    return;
                }
                btc--;
                if (mtc == 0) {
                    error(13);
                    return;
                }
                if ((size == 16 || size == 32) && (mar & 1) != 0) {
                    error(5);
                    return;
                }
                return;
            }
            if (chain == 12) {
                if (bar == 0) {
                    coc = 128;
                    blc = 64;
                    ndt = 0;
                    complete();
                    return;
                }
                if ((bar & 1) != 0) {
                    error(7);
                    return;
                }
                try {
                    mar = x68000.read_int_big(bar, bfc2);
                    bar += 4;
                    mtc = x68000.read_short_big(bar, bfc2) & 65535;
                    bar += 2;
                    bar = x68000.read_int_big(bar, bfc2);
                } catch (MC68000Exception e) {
                    error(11);
                    return;
                }
                if (mtc == 0) {
                    error(13);
                    return;
                }
                if ((size == 16 || size == 32) && (mar & 1) != 0) {
                    error(5);
                    return;
                }
                return;
            }
            if (cnt != 0) {
                blc = 64;
                cnt = 0;
                if (ite != 0) {
                    interrupt(ch << 1);
                }
                mtc = btc;
                mar = bar;
                if (mtc == 0) {
                    error(13);
                    return;
                }
                if ((size == 16 || size == 32) && (mar & 1) != 0) {
                    error(5);
                    return;
                }
                return;
            }
            coc = 128;
            blc = 0;
            ndt = 0;
            complete();
            return;
        }

        void fallPCL() {
            pcs = 0;
            pct = 2;
            if (act == 0 || hlt != 0) {
                return;
            }
            transfer();
        }

        void risePCL() {
            pcs = 1;
        }
    }
}

