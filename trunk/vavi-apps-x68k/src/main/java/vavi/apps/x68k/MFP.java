/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class MFP extends MemoryMappedDevice implements X68000Device, InterruptDevice, KeyboardInputListener {
    private int gpip_alarm;
    private int gpip_expwon;
    private int gpip_power;
    private int gpip_opmirq;
    private int gpip_vdisp;
    private int gpip_rint;
    private int gpip_hsync;
    private int aer;
    private int ier;
    private int interrupt_request[];
    private int interrupt_acknowledged[];
    private boolean interrupt_in_service[];
    private int imr;
    private int vector_high;
    private int ta_prescale, tc_prescale, td_prescale;
    private boolean ta_eventcount;
    private int ta_initial, tc_initial, td_initial;
    private int ta_current, tc_current, td_current;

    final private static long DELTA[] = {
        9223372036854775807L, 10000, 25000, 40000, 125000, 160000, 250000, 500000
    };

    private long ta_start_clock, tc_start_clock, td_start_clock;
    private long ta_delta, tc_delta, td_delta;
    private long ta_interrupt_clock, tc_interrupt_clock, td_interrupt_clock;
    private int udr_queue[];
    private int udr_queue_read_pointer;
    private int udr_queue_write_pointer;
    private int joykey;

    private X68000 x68000;

    private PPI ppi;

    public MFP() {
        interrupt_request = new int[16];
        interrupt_acknowledged = new int[16];
        interrupt_in_service = new boolean[16];
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        ppi = x68000.ppi;
        udr_queue = new int[16];
        for (int i = 0; i <= 15; i++) {
            udr_queue[i] = 0;
        }
        udr_queue_read_pointer = 0;
        udr_queue_write_pointer = 0;
        joykey = x68000.getIntParameter("JOYKEY", 0, 1, 0);
        reset();
        return true;
    }

    public void reset() {
        gpip_alarm = 0;
        gpip_expwon = 0;
        gpip_power = 0;
        gpip_opmirq = 0;
        gpip_vdisp = 0;
        gpip_rint = 0;
        gpip_hsync = 0;
        aer = 0;
        ier = 0;
        for (int i = 0; i <= 15; i++) {
            interrupt_request[i] = 0;
            interrupt_acknowledged[i] = 0;
            interrupt_in_service[i] = false;
        }
        imr = 0;
        vector_high = 0;
        ta_prescale = 0;
        tc_prescale = 0;
        td_prescale = 0;
        ta_eventcount = false;
        ta_initial = 0;
        tc_initial = 0;
        td_initial = 0;
        ta_current = 0;
        tc_current = 0;
        td_current = 0;
        ta_start_clock = 0;
        tc_start_clock = 0;
        td_start_clock = 0;
        ta_interrupt_clock = 9223372036854775807L;
        tc_interrupt_clock = 9223372036854775807L;
        td_interrupt_clock = 9223372036854775807L;
        x68000.mfp_interrupt_clock = 9223372036854775807L;
    }

    public int acknowledge() {
        int request;
        if ((imr & 32768) != 0) {
            request = interrupt_request[15];
            if (request != interrupt_acknowledged[15]) {
                interrupt_acknowledged[15] = request;
                interrupt_in_service[15] = true;
                return vector_high + 15;
            }
        }
        if ((imr & 16384) != 0) {
            request = interrupt_request[14];
            if (request != interrupt_acknowledged[14]) {
                interrupt_acknowledged[14] = request;
                interrupt_in_service[14] = true;
                return vector_high + 14;
            }
        }
        if ((imr & 8192) != 0) {
            request = interrupt_request[13];
            if (request != interrupt_acknowledged[13]) {
                interrupt_acknowledged[13] = request;
                interrupt_in_service[13] = true;
                return vector_high + 13;
            }
        }
        if ((imr & 4096) != 0) {
            request = interrupt_request[12];
            if (request != interrupt_acknowledged[12]) {
                interrupt_acknowledged[12] = request;
                interrupt_in_service[12] = true;
                return vector_high + 12;
            }
        }
        if ((imr & 2048) != 0) {
            request = interrupt_request[11];
            if (request != interrupt_acknowledged[11]) {
                interrupt_acknowledged[11] = request;
                interrupt_in_service[11] = true;
                return vector_high + 11;
            }
        }
        if ((imr & 1024) != 0) {
            request = interrupt_request[10];
            if (request != interrupt_acknowledged[10]) {
                interrupt_acknowledged[10] = request;
                interrupt_in_service[10] = true;
                return vector_high + 10;
            }
        }
        if ((imr & 512) != 0) {
            request = interrupt_request[9];
            if (request != interrupt_acknowledged[9]) {
                interrupt_acknowledged[9] = request;
                interrupt_in_service[9] = true;
                return vector_high + 9;
            }
        }
        if ((imr & 256) != 0) {
            request = interrupt_request[8];
            if (request != interrupt_acknowledged[8]) {
                interrupt_acknowledged[8] = request;
                interrupt_in_service[8] = true;
                return vector_high + 8;
            }
        }
        if ((imr & 128) != 0) {
            request = interrupt_request[7];
            if (request != interrupt_acknowledged[7]) {
                interrupt_acknowledged[7] = request;
                interrupt_in_service[7] = true;
                return vector_high + 7;
            }
        }
        if ((imr & 64) != 0) {
            request = interrupt_request[6];
            if (request != interrupt_acknowledged[6]) {
                interrupt_acknowledged[6] = request;
                interrupt_in_service[6] = true;
                return vector_high + 6;
            }
        }
        if ((imr & 32) != 0) {
            request = interrupt_request[5];
            if (request != interrupt_acknowledged[5]) {
                interrupt_acknowledged[5] = request;
                interrupt_in_service[5] = true;
                return vector_high + 5;
            }
        }
        if ((imr & 16) != 0) {
            request = interrupt_request[4];
            if (request != interrupt_acknowledged[4]) {
                interrupt_acknowledged[4] = request;
                interrupt_in_service[4] = true;
                return vector_high + 4;
            }
        }
        if ((imr & 8) != 0) {
            request = interrupt_request[3];
            if (request != interrupt_acknowledged[3]) {
                interrupt_acknowledged[3] = request;
                interrupt_in_service[3] = true;
                return vector_high + 3;
            }
        }
        if ((imr & 4) != 0) {
            request = interrupt_request[2];
            if (request != interrupt_acknowledged[2]) {
                interrupt_acknowledged[2] = request;
                interrupt_in_service[2] = true;
                return vector_high + 2;
            }
        }
        if ((imr & 2) != 0) {
            request = interrupt_request[1];
            if (request != interrupt_acknowledged[1]) {
                interrupt_acknowledged[1] = request;
                interrupt_in_service[1] = true;
                return vector_high + 1;
            }
        }
        if ((imr & 1) != 0) {
            request = interrupt_request[0];
            if (request != interrupt_acknowledged[0]) {
                interrupt_acknowledged[0] = request;
                interrupt_in_service[0] = true;
                return vector_high;
            }
        }
        return 0;
    }

    public void done(int vector) {
        interrupt_in_service[vector & 15] = false;
        if ((imr & 32768) != 0 && interrupt_request[15] != interrupt_acknowledged[15]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 16384) != 0 && interrupt_request[14] != interrupt_acknowledged[14]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 8192) != 0 && interrupt_request[13] != interrupt_acknowledged[13]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 4096) != 0 && interrupt_request[12] != interrupt_acknowledged[12]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 2048) != 0 && interrupt_request[11] != interrupt_acknowledged[11]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 1024) != 0 && interrupt_request[10] != interrupt_acknowledged[10]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 512) != 0 && interrupt_request[9] != interrupt_acknowledged[9]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 256) != 0 && interrupt_request[8] != interrupt_acknowledged[8]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 128) != 0 && interrupt_request[7] != interrupt_acknowledged[7]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 64) != 0 && interrupt_request[6] != interrupt_acknowledged[6]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 32) != 0 && interrupt_request[5] != interrupt_acknowledged[5]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 16) != 0 && interrupt_request[4] != interrupt_acknowledged[4]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 8) != 0 && interrupt_request[3] != interrupt_acknowledged[3]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 4) != 0 && interrupt_request[2] != interrupt_acknowledged[2]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 2) != 0 && interrupt_request[1] != interrupt_acknowledged[1]) {
            x68000.interrupt_request_mfp++;
            return;
        }
        if ((imr & 1) != 0 && interrupt_request[0] != interrupt_acknowledged[0]) {
            x68000.interrupt_request_mfp++;
            return;
        }
    }

    public void keyboardInput(int keyCode) {
        int temp = udr_queue_write_pointer + 1 & 15;
        if (temp != udr_queue_read_pointer) {
            udr_queue_write_pointer = temp;
            udr_queue[udr_queue_write_pointer] = keyCode;
            if ((ier & 4096) != 0) {
                interrupt_request[12]++;
                if ((imr & 4096) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
        boolean pressed = keyCode < 128;
        if (joykey == 1) {
            switch (keyCode & 127) {
            case 42:
                ppi.triger_a(pressed);
                break;
            case 43:
                ppi.triger_b(pressed);
                break;
            case 24:
            case 60:
            case 68:
                ppi.forward(pressed);
                break;
            case 36:
            case 59:
            case 71:
                ppi.left(pressed);
                break;
            case 38:
            case 61:
            case 73:
                ppi.right(pressed);
                break;
            case 48:
            case 62:
            case 76:
                ppi.back(pressed);
                break;
            }
        }
    }

    public byte read_byte(int a) {
        switch (a) {
        case 15237121:
            return (byte) (gpip_hsync + gpip_rint + gpip_vdisp + gpip_opmirq + gpip_power + gpip_expwon + gpip_alarm);
        case 15237123:
            return (byte) aer;
        case 15237127:
            return (byte) (ier >> 8);
        case 15237129:
            return (byte) ier;
        case 15237131:
            return (byte) ((interrupt_request[15] != interrupt_acknowledged[15] ? 128 : 0) + (interrupt_request[14] != interrupt_acknowledged[14] ? 64 : 0) + (interrupt_request[13] != interrupt_acknowledged[13] ? 32 : 0) + (interrupt_request[12] != interrupt_acknowledged[12] ? 16 : 0) + (interrupt_request[11] != interrupt_acknowledged[11] ? 8 : 0) + (interrupt_request[10] != interrupt_acknowledged[10] ? 4 : 0) + (interrupt_request[9] != interrupt_acknowledged[9] ? 2 : 0) + (interrupt_request[8] != interrupt_acknowledged[8]
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ? 1
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                : 0));
        case 15237133:
            return (byte) ((interrupt_request[7] != interrupt_acknowledged[7] ? 128 : 0) + (interrupt_request[6] != interrupt_acknowledged[6] ? 64 : 0) + (interrupt_request[5] != interrupt_acknowledged[5] ? 32 : 0) + (interrupt_request[4] != interrupt_acknowledged[4] ? 16 : 0) + (interrupt_request[3] != interrupt_acknowledged[3] ? 8 : 0) + (interrupt_request[2] != interrupt_acknowledged[2] ? 4 : 0) + (interrupt_request[1] != interrupt_acknowledged[1] ? 2 : 0) + (interrupt_request[0] != interrupt_acknowledged[0]
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ? 1
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    : 0));
        case 15237135:
            return (byte) ((interrupt_in_service[15] ? 128 : 0) + (interrupt_in_service[14] ? 64 : 0) + (interrupt_in_service[13] ? 32 : 0) + (interrupt_in_service[12] ? 16 : 0) + (interrupt_in_service[11] ? 8 : 0) + (interrupt_in_service[10] ? 4 : 0) + (interrupt_in_service[9] ? 2 : 0) + (interrupt_in_service[8] ? 1 : 0));
        case 15237137:
            return (byte) ((interrupt_in_service[7] ? 128 : 0) + (interrupt_in_service[6] ? 64 : 0) + (interrupt_in_service[5] ? 32 : 0) + (interrupt_in_service[4] ? 16 : 0) + (interrupt_in_service[3] ? 8 : 0) + (interrupt_in_service[2] ? 4 : 0) + (interrupt_in_service[1] ? 2 : 0) + (interrupt_in_service[0] ? 1 : 0));
        case 15237139:
            return (byte) (imr >> 8);
        case 15237141:
            return (byte) imr;
        case 15237143:
            return (byte) vector_high;
        case 15237145:
            return (byte) ((ta_eventcount ? 8 : 0) + ta_prescale);
        case 15237147:
            return 1;
        case 15237149:
            return (byte) ((tc_prescale << 4) + td_prescale);
        case 15237151:
            if (ta_eventcount || ta_prescale == 0) {
                return (byte) ta_current;
            }
            return (byte) (ta_initial - (x68000.clock_count - ta_start_clock) / ta_delta % ta_initial);
        case 15237153:
            return 0;
        case 15237155:
            if (tc_prescale == 0) {
                return (byte) tc_current;
            }
            return (byte) (tc_initial - (x68000.clock_count - tc_start_clock) / tc_delta % tc_initial);
        case 15237157:
            if (td_prescale == 0) {
                return (byte) td_current;
            }
            return (byte) (td_initial - (x68000.clock_count - td_start_clock) / td_delta % td_initial);
        case 15237159:
            return 0;
        case 15237161:
            return 0;
        case 15237163:
            return 0;
        case 15237165:
            return (byte) -128;
        case 15237167:
            if (udr_queue_read_pointer != udr_queue_write_pointer) {
                udr_queue_read_pointer = udr_queue_read_pointer + 1 & 15;
                if (udr_queue_read_pointer != udr_queue_write_pointer) {
                    if ((ier & 4096) != 0) {
                        interrupt_request[12]++;
                        if ((imr & 4096) != 0) {
                            x68000.interrupt_request_mfp++;
                        }
                    }
                }
            }
            return (byte) udr_queue[udr_queue_read_pointer];
        }
        return 0;
    }

    public short read_short_big(int a) {
        return (short) ((read_byte(a) << 8) + (read_byte(a + 1) & 255));
    }

    public int read_int_big(int a) {
        return ((read_byte(a) & 255) << 24) + ((read_byte(a + 1) & 255) << 16) + ((read_byte(a + 2) & 255) << 8) + (read_byte(a + 3) & 255);
    }

    public void write_byte(int a, byte b) {
        switch (a) {
        case 15237121:
            return;
        case 15237123:
            aer = b;
            return;
        case 15237127:
            ier = ((b & 255) << 8) + (ier & 255);
            if ((b & 128) == 0) {
                interrupt_acknowledged[15] = interrupt_request[15];
            }
            if ((b & 64) == 0) {
                interrupt_acknowledged[14] = interrupt_request[14];
            }
            if ((b & 32) == 0) {
                interrupt_acknowledged[13] = interrupt_request[13];
            }
            if ((b & 16) == 0) {
                interrupt_acknowledged[12] = interrupt_request[12];
            }
            if ((b & 8) == 0) {
                interrupt_acknowledged[11] = interrupt_request[11];
            }
            if ((b & 4) == 0) {
                interrupt_acknowledged[10] = interrupt_request[10];
            }
            if ((b & 2) == 0) {
                interrupt_acknowledged[9] = interrupt_request[9];
            }
            if ((b & 1) == 0) {
                interrupt_acknowledged[8] = interrupt_request[8];
            }
            return;
        case 15237129:
            ier = (ier & 65280) + (b & 255);
            if ((b & 128) == 0) {
                interrupt_acknowledged[7] = interrupt_request[7];
            }
            if ((b & 64) == 0) {
                interrupt_acknowledged[6] = interrupt_request[6];
            }
            if ((b & 32) == 0) {
                interrupt_acknowledged[5] = interrupt_request[5];
            }
            if ((b & 16) == 0) {
                interrupt_acknowledged[4] = interrupt_request[4];
            }
            if ((b & 8) == 0) {
                interrupt_acknowledged[3] = interrupt_request[3];
            }
            if ((b & 4) == 0) {
                interrupt_acknowledged[2] = interrupt_request[2];
            }
            if ((b & 2) == 0) {
                interrupt_acknowledged[1] = interrupt_request[1];
            }
            if ((b & 1) == 0) {
                interrupt_acknowledged[0] = interrupt_request[0];
            }
            return;
        case 15237131:
            switch (b) {
            case (byte) 127:
                interrupt_acknowledged[15] = interrupt_request[15];
                break;
            case (byte) -65:
                interrupt_acknowledged[14] = interrupt_request[14];
                break;
            case (byte) -33:
                interrupt_acknowledged[13] = interrupt_request[13];
                break;
            case (byte) -17:
                interrupt_acknowledged[12] = interrupt_request[12];
                break;
            case (byte) -9:
                interrupt_acknowledged[11] = interrupt_request[11];
                break;
            case (byte) -5:
                interrupt_acknowledged[10] = interrupt_request[10];
                break;
            case (byte) -3:
                interrupt_acknowledged[9] = interrupt_request[9];
                break;
            case (byte) -2:
                interrupt_acknowledged[8] = interrupt_request[8];
                break;
            }
            return;
        case 15237133:
            switch (b) {
            case (byte) 127:
                interrupt_acknowledged[7] = interrupt_request[7];
                break;
            case (byte) -65:
                interrupt_acknowledged[6] = interrupt_request[6];
                break;
            case (byte) -33:
                interrupt_acknowledged[5] = interrupt_request[5];
                break;
            case (byte) -17:
                interrupt_acknowledged[4] = interrupt_request[4];
                break;
            case (byte) -9:
                interrupt_acknowledged[3] = interrupt_request[3];
                break;
            case (byte) -5:
                interrupt_acknowledged[2] = interrupt_request[2];
                break;
            case (byte) -3:
                interrupt_acknowledged[1] = interrupt_request[1];
                break;
            case (byte) -2:
                interrupt_acknowledged[0] = interrupt_request[0];
                break;
            }
            return;
        case 15237135:
            switch (b) {
            case (byte) 127:
                interrupt_in_service[15] = false;
                break;
            case (byte) -65:
                interrupt_in_service[14] = false;
                break;
            case (byte) -33:
                interrupt_in_service[13] = false;
                break;
            case (byte) -17:
                interrupt_in_service[12] = false;
                break;
            case (byte) -9:
                interrupt_in_service[11] = false;
                break;
            case (byte) -5:
                interrupt_in_service[10] = false;
                break;
            case (byte) -3:
                interrupt_in_service[9] = false;
                break;
            case (byte) -2:
                interrupt_in_service[8] = false;
                break;
            }
            return;
        case 15237137:
            switch (b) {
            case (byte) 127:
                interrupt_in_service[7] = false;
                break;
            case (byte) -65:
                interrupt_in_service[6] = false;
                break;
            case (byte) -33:
                interrupt_in_service[5] = false;
                break;
            case (byte) -17:
                interrupt_in_service[4] = false;
                break;
            case (byte) -9:
                interrupt_in_service[3] = false;
                break;
            case (byte) -5:
                interrupt_in_service[2] = false;
                break;
            case (byte) -3:
                interrupt_in_service[1] = false;
                break;
            case (byte) -2:
                interrupt_in_service[0] = false;
                break;
            }
            return;
        case 15237139:
            imr = ((b & 255) << 8) + (imr & 255);
            if ((b & 128) != 0 && interrupt_request[15] != interrupt_acknowledged[15]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 64) != 0 && interrupt_request[14] != interrupt_acknowledged[14]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 32) != 0 && interrupt_request[13] != interrupt_acknowledged[13]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 16) != 0 && interrupt_request[12] != interrupt_acknowledged[12]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 8) != 0 && interrupt_request[11] != interrupt_acknowledged[11]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 4) != 0 && interrupt_request[10] != interrupt_acknowledged[10]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 2) != 0 && interrupt_request[9] != interrupt_acknowledged[9]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 1) != 0 && interrupt_request[8] != interrupt_acknowledged[8]) {
                x68000.interrupt_request_mfp++;
            }
            return;
        case 15237141:
            imr = (imr & 65280) + (b & 255);
            if ((b & 128) != 0 && interrupt_request[7] != interrupt_acknowledged[7]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 64) != 0 && interrupt_request[6] != interrupt_acknowledged[6]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 32) != 0 && interrupt_request[5] != interrupt_acknowledged[5]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 16) != 0 && interrupt_request[4] != interrupt_acknowledged[4]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 8) != 0 && interrupt_request[3] != interrupt_acknowledged[3]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 4) != 0 && interrupt_request[2] != interrupt_acknowledged[2]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 2) != 0 && interrupt_request[1] != interrupt_acknowledged[1]) {
                x68000.interrupt_request_mfp++;
            }
            if ((b & 1) != 0 && interrupt_request[0] != interrupt_acknowledged[0]) {
                x68000.interrupt_request_mfp++;
            }
            return;
        case 15237143:
            vector_high = b & 240;
            return;
        case 15237145: {
            int prev_prescale = ta_prescale;
            ta_eventcount = (b & 8) != 0;
            ta_prescale = b & 7;
            if (ta_eventcount && ta_prescale != 0) {
                ta_eventcount = false;
                ta_prescale = 0;
            }
            if (ta_eventcount) {
                if (prev_prescale != 0) {
                    ta_current = (int) (ta_initial - (x68000.clock_count - ta_start_clock) / ta_delta % ta_initial);
                }
                ta_interrupt_clock = 9223372036854775807L;
                x68000.mfp_interrupt_clock = tc_interrupt_clock <= td_interrupt_clock ? tc_interrupt_clock : td_interrupt_clock;
            } else if (ta_prescale != 0) {
                ta_start_clock = x68000.clock_count;
                ta_delta = DELTA[ta_prescale];
                ta_interrupt_clock = ta_start_clock + ta_delta * ta_initial;
                x68000.mfp_interrupt_clock = tc_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock <= tc_interrupt_clock ? ta_interrupt_clock : tc_interrupt_clock : ta_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock : td_interrupt_clock;
            } else {
                if (prev_prescale != 0) {
                    ta_current = (int) (ta_initial - (x68000.clock_count - ta_start_clock) / ta_delta % ta_initial);
                }
                ta_interrupt_clock = 9223372036854775807L;
                x68000.mfp_interrupt_clock = tc_interrupt_clock <= td_interrupt_clock ? tc_interrupt_clock : td_interrupt_clock;
            }
        }
            return;
        case 15237147:
            return;
        case 15237149: {
            int prev_prescale = tc_prescale;
            tc_prescale = b >> 4 & 7;
            if (tc_prescale != 0) {
                tc_start_clock = x68000.clock_count;
                tc_delta = DELTA[tc_prescale];
                tc_interrupt_clock = tc_start_clock + tc_delta * tc_initial;
                x68000.mfp_interrupt_clock = tc_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock <= tc_interrupt_clock ? ta_interrupt_clock : tc_interrupt_clock : ta_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock : td_interrupt_clock;
            } else {
                if (prev_prescale != 0) {
                    tc_current = (int) (tc_initial - (x68000.clock_count - tc_start_clock) / tc_delta % tc_initial);
                }
                tc_interrupt_clock = 9223372036854775807L;
                x68000.mfp_interrupt_clock = ta_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock : td_interrupt_clock;
            }
        }
            {
                int prev_prescale = td_prescale;
                td_prescale = b & 7;
                if (td_prescale != 0) {
                    td_start_clock = x68000.clock_count;
                    td_delta = DELTA[td_prescale];
                    td_interrupt_clock = td_start_clock + td_delta * td_initial;
                    x68000.mfp_interrupt_clock = tc_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock <= tc_interrupt_clock ? ta_interrupt_clock : tc_interrupt_clock : ta_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock : td_interrupt_clock;
                } else {
                    if (prev_prescale != 0) {
                        td_current = (int) (td_initial - (x68000.clock_count - td_start_clock) / td_delta % td_initial);
                    }
                    td_interrupt_clock = 9223372036854775807L;
                    x68000.mfp_interrupt_clock = ta_interrupt_clock <= tc_interrupt_clock ? ta_interrupt_clock : tc_interrupt_clock;
                }
            }
            return;
        case 15237151:
            ta_initial = b & 255;
            if (!ta_eventcount && ta_prescale == 0) {
                ta_current = ta_initial;
            }
            return;
        case 15237153:
            return;
        case 15237155:
            tc_initial = b & 255;
            if (tc_prescale == 0) {
                tc_current = tc_initial;
            }
            return;
        case 15237157:
            td_initial = b & 255;
            if (td_prescale == 0) {
                td_current = td_initial;
            }
            return;
        case 15237159:
            return;
        case 15237161:
            return;
        case 15237163:
            return;
        case 15237165:
            return;
        case 15237167:
            switch (b & 240) {
            case 96:
                x68000.keyboard.setDelayTime(b);
                break;
            case 112:
                x68000.keyboard.setRepeatInterval(b);
                break;
            case 128:
            case 144:
            case 160:
            case 176:
            case 192:
            case 208:
            case 224:
            case 240:
                x68000.keyboard.setLedStatus(b);
                break;
            }
            return;
        }
    }

    public void write_short_big(int a, short s) {
        write_byte(a, (byte) (s >>> 8));
        write_byte(a + 1, (byte) s);
    }

    public void write_int_big(int a, int i) {
        write_byte(a, (byte) (i >>> 24));
        write_byte(a + 1, (byte) (i >>> 16));
        write_byte(a + 2, (byte) (i >>> 8));
        write_byte(a + 3, (byte) i);
    }

    public void tick() {
        long clock = x68000.clock_count;
        if (clock >= ta_interrupt_clock) {
            if ((ier & 8192) != 0) {
                interrupt_request[13]++;
                if ((imr & 8192) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
            ta_interrupt_clock += ta_delta * ta_initial;
            x68000.mfp_interrupt_clock = tc_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock <= tc_interrupt_clock ? ta_interrupt_clock : tc_interrupt_clock : ta_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock : td_interrupt_clock;
        }
        if (clock >= tc_interrupt_clock) {
            if ((ier & 32) != 0) {
                interrupt_request[5]++;
                if ((imr & 32) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
            tc_interrupt_clock += tc_delta * tc_initial;
            x68000.mfp_interrupt_clock = tc_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock <= tc_interrupt_clock ? ta_interrupt_clock : tc_interrupt_clock : ta_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock : td_interrupt_clock;
        }
        if (clock >= td_interrupt_clock) {
            if ((ier & 16) != 0) {
                interrupt_request[4]++;
                if ((imr & 16) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
            td_interrupt_clock += td_delta * td_initial;
            x68000.mfp_interrupt_clock = tc_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock <= tc_interrupt_clock ? ta_interrupt_clock : tc_interrupt_clock : ta_interrupt_clock <= td_interrupt_clock ? ta_interrupt_clock : td_interrupt_clock;
        }
    }

    public void riseAlarm() {
        if (gpip_alarm == 0) {
            gpip_alarm = 1;
            if ((aer & 1) != 0 && (ier & 1) != 0) {
                interrupt_request[0]++;
                if ((imr & 1) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void fallAlarm() {
        if (gpip_alarm != 0) {
            gpip_alarm = 0;
            if ((aer & 1) == 0 && (ier & 1) != 0) {
                interrupt_request[0]++;
                if ((imr & 1) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void riseOpmirq() {
        if (gpip_opmirq == 0) {
            gpip_opmirq = 8;
            if ((aer & 8) != 0 && (ier & 8) != 0) {
                interrupt_request[3]++;
                if ((imr & 8) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void fallOpmirq() {
        if (gpip_opmirq != 0) {
            gpip_opmirq = 0;
            if ((aer & 8) == 0 && (ier & 8) != 0) {
                interrupt_request[3]++;
                if ((imr & 8) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void riseExpwon() {
        if (gpip_expwon == 0) {
            gpip_expwon = 2;
            if ((aer & 2) != 0 && (ier & 2) != 0) {
                interrupt_request[1]++;
                if ((imr & 2) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void fallExpwon() {
        if (gpip_expwon != 0) {
            gpip_expwon = 0;
            if ((aer & 2) == 0 && (ier & 2) != 0) {
                interrupt_request[1]++;
                if ((imr & 2) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void risePower() {
        if (gpip_power == 0) {
            gpip_power = 4;
            if ((aer & 4) != 0 && (ier & 4) != 0) {
                interrupt_request[2]++;
                if ((imr & 4) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void fallPower() {
        if (gpip_power != 0) {
            gpip_power = 0;
            if ((aer & 4) == 0 && (ier & 4) != 0) {
                interrupt_request[2]++;
                if ((imr & 4) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void riseVdisp() {
        if (gpip_vdisp == 0) {
            gpip_vdisp = 16;
            if ((aer & 16) != 0) {
                if ((ier & 64) != 0) {
                    interrupt_request[6]++;
                    if ((imr & 64) != 0) {
                        x68000.interrupt_request_mfp++;
                    }
                }
                if (ta_eventcount && --ta_current <= 0) {
                    ta_current = ta_initial;
                    if ((ier & 8192) != 0) {
                        interrupt_request[13]++;
                        if ((imr & 8192) != 0) {
                            x68000.interrupt_request_mfp++;
                        }
                    }
                }
            }
        }
    }

    public void fallVdisp() {
        if (gpip_vdisp != 0) {
            gpip_vdisp = 0;
            if ((aer & 16) == 0) {
                if ((ier & 64) != 0) {
                    interrupt_request[6]++;
                    if ((imr & 64) != 0) {
                        x68000.interrupt_request_mfp++;
                    }
                }
                if (ta_eventcount && --ta_current <= 0) {
                    ta_current = ta_initial;
                    if ((ier & 8192) != 0) {
                        interrupt_request[13]++;
                        if ((imr & 8192) != 0) {
                            x68000.interrupt_request_mfp++;
                        }
                    }
                }
            }
        }
    }

    public void riseRint() {
        if (gpip_rint == 0) {
            gpip_rint = 64;
            if ((aer & 64) != 0 && (ier & 16384) != 0) {
                interrupt_request[14]++;
                if ((imr & 16384) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void fallRint() {
        if (gpip_rint != 0) {
            gpip_rint = 0;
            if ((aer & 64) == 0 && (ier & 16384) != 0) {
                interrupt_request[14]++;
                if ((imr & 16384) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void riseHsync() {
        if (gpip_hsync == 0) {
            gpip_hsync = 128;
            if ((aer & 128) != 0 && (ier & 32768) != 0) {
                interrupt_request[15]++;
                if ((imr & 32768) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }

    public void fallHsync() {
        if (gpip_hsync != 0) {
            gpip_hsync = 0;
            if ((aer & 128) == 0 && (ier & 32768) != 0) {
                interrupt_request[15]++;
                if ((imr & 32768) != 0) {
                    x68000.interrupt_request_mfp++;
                }
            }
        }
    }
}

