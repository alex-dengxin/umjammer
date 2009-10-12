/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;


class Sound extends MemoryMappedDevice implements X68000Device {
    private long ta_interrupt_clock;
    private long tb_interrupt_clock;
    private long adpcmout_interrupt_clock;
    private long lineout_interrupt_clock;
    private int address;
    private int clka;
    private int clkb;
    private boolean irqena;
    private boolean irqenb;
    private boolean ista;
    private boolean istb;
    private int adpcmout_clock;
    private int adpcmout_divide;

    final private int adpcmout_sample_repeats[] = {
        4, 3, 2, 2, 8, 6, 4, 4
    };

    private int adpcmout_sample_repeat;

    final private long adpcmout_interrupt_intervals[] = {
        2560000L, 1920000L, 1280000L, 1280000L, 5120000L, 3840000L, 2560000L, 2560000L
    };

    private long adpcmout_interrupt_interval;
    private int adpcmout_pan;
    private boolean adpcmout_active;
    private int adpcmout_data;
    private int adpcm2pcm_pointer;
    private int decoded_13bits_pcm_data;
    private int current_adpcmout_block[], next_adpcmout_block[];
    private int adpcmout_pointer;

    final private static int quantization_table[] = {
        16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552
    };

    final private static int adpcm2pcm_table[];
    static {
        adpcm2pcm_table = new int[37632];
        for (int level1 = 0; level1 < 49; level1++) {
            for (int data2 = 0; data2 < 16; data2++) {
                for (int data1 = 0; data1 < 16; data1++) {
                    int level2 = calc_nextlevel(level1, data1);
                    int pos = 768 * level1 + (data2 << 4) + data1;
                    adpcm2pcm_table[pos] = calc_delta(level1, data1);
                    adpcm2pcm_table[pos + 256] = calc_delta(level2, data2);
                    adpcm2pcm_table[pos + 512] = 768 * calc_nextlevel(level2, data2);
                }
            }
        }
    }

    private static int calc_delta(int level, int data) {
        int quantization = quantization_table[level];
        int delta = ((data & 4) != 0 ? quantization : 0) + ((data & 2) != 0 ? quantization >> 1 : 0) + ((data & 1) != 0 ? quantization >> 2 : 0) + (quantization >> 3);
        if ((data & 8) != 0) {
            delta = -delta;
        }
        return delta;
    }

    private static int calc_nextlevel(int level, int data) {
        data &= 7;
        if (data < 4) {
            if (level > 0) {
                level--;
            }
        } else {
            level += data - 3 << 1;
            if (level > 48) {
                level = 48;
            }
        }
        return level;
    }

    AudioFormat audio_format;
    DataLine.Info info;
    SourceDataLine lineout;
    byte lineout_block[];

    private X68000 x68000;
    private DMAC dmac;
    private MFP mfp;

    public Sound() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        dmac = x68000.dmac;
        mfp = x68000.mfp;
        lineout_interrupt_clock = 9223372036854775807L;
        current_adpcmout_block = new int[1250];
        next_adpcmout_block = new int[1250];
        lineout_block = new byte[2646];
        try {
            audio_format = new AudioFormat(11025F, 16, 1, true, false);
            info = new DataLine.Info(SourceDataLine.class, audio_format);
            lineout = (SourceDataLine) AudioSystem.getLine(info);
            lineout.open(audio_format, 2656);
            lineout.start();
        } catch (Exception e) {
            x68000.monitor.outputString(e.toString() + "\n");
            return false;
        }
        if (lineout.available() < 2646) {
            x68000.monitor.outputString("ライン出力バッファを確保できませんでした\n");
            return false;
        }
        for (int i = 0; i < 2646; i++) {
            lineout_block[i] = 0;
        }
        try {
            lineout.write(lineout_block, 0, 2646);
        } catch (Exception e) {
        }
        lineout_interrupt_clock = 400000000;
        reset();
        return true;
    }

    public void reset() {
        ta_interrupt_clock = 9223372036854775807L;
        tb_interrupt_clock = 9223372036854775807L;
        adpcmout_interrupt_clock = 9223372036854775807L;
        x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                           : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
        address = -1;
        clka = 0;
        clkb = 0;
        irqena = false;
        irqenb = false;
        ista = false;
        istb = false;
        adpcmout_clock = 0;
        adpcmout_divide = 2;
        update_adpcmout_sample_rate();
        set_pan(0);
        adpcmout_active = false;
        adpcmout_data = -1;
        adpcm2pcm_pointer = 0;
        decoded_13bits_pcm_data = 0;
        for (int i = 0; i < 1250; i++) {
            current_adpcmout_block[i] = 0;
            next_adpcmout_block[i] = 0;
        }
        adpcmout_pointer = 0;
    }

    public byte read_byte(int a) throws MC68000Exception {
        switch (a) {
        case 15269891:
            return (byte) ((ista ? 2 : 0) + (istb ? 1 : 0));
        case 15278081:
            return 64;
        }
        return 0;
    }

    public short read_short_big(int a) throws MC68000Exception {
        return (short) ((read_byte(a) << 8) + (read_byte(a + 1) & 255));
    }

    public int read_int_big(int a) throws MC68000Exception {
        return (read_byte(a) << 24) + ((read_byte(a + 1) & 255) << 16) + ((read_byte(a + 2) & 255) << 8) + (read_byte(a + 3) & 255);
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        switch (a) {
        case 15269889:
            address = b & 255;
            break;
        case 15269891:
            switch (address) {
            case 16:
                clka = ((b & 255) << 2) + (clka & 3);
                if (ta_interrupt_clock != 9223372036854775807L) {
                    ta_interrupt_clock = x68000.clock_count + (1024 - clka << 4) * 10000L;
                    x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                                       : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                }
                break;
            case 17:
                clka = (clka & 1020) + (b & 3);
                if (ta_interrupt_clock != 9223372036854775807L) {
                    ta_interrupt_clock = x68000.clock_count + (1024 - clka << 4) * 10000L;
                    x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                                       : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                }
                break;
            case 18:
                clkb = b & 255;
                if (tb_interrupt_clock != 9223372036854775807L) {
                    tb_interrupt_clock = x68000.clock_count + (256 - clkb << 8) * 10000L;
                    x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                                       : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                }
                break;
            case 20:
                if ((b & 1) != 0) {
                    if (!(ta_interrupt_clock != 9223372036854775807L)) {
                        if (ista || istb) {
                            mfp.fallOpmirq();
                        } else {
                            mfp.riseOpmirq();
                        }
                        ta_interrupt_clock = x68000.clock_count + (1024 - clka << 4) * 10000L;
                        x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                                           : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                    }
                } else {
                    if (ta_interrupt_clock != 9223372036854775807L) {
                        if (ista || istb) {
                            mfp.fallOpmirq();
                        } else {
                            mfp.riseOpmirq();
                        }
                        ta_interrupt_clock = 9223372036854775807L;
                        x68000.sound_interrupt_clock = tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                    }
                }
                if ((b & 2) != 0) {
                    if (!(tb_interrupt_clock != 9223372036854775807L)) {
                        if (ista || istb) {
                            mfp.fallOpmirq();
                        } else {
                            mfp.riseOpmirq();
                        }
                        tb_interrupt_clock = x68000.clock_count + (256 - clkb << 8) * 10000L;
                        x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                                           : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                    }
                } else {
                    if (tb_interrupt_clock != 9223372036854775807L) {
                        if (ista || istb) {
                            mfp.fallOpmirq();
                        } else {
                            mfp.riseOpmirq();
                        }
                        tb_interrupt_clock = 9223372036854775807L;
                        x68000.sound_interrupt_clock = ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                    }
                }
                irqena = (b & 4) != 0;
                irqenb = (b & 8) != 0;
                if ((b & 16) != 0) {
                    ista = false;
                    if (istb) {
                        mfp.fallOpmirq();
                    } else {
                        mfp.riseOpmirq();
                    }
                }
                if ((b & 32) != 0) {
                    istb = false;
                    if (ista) {
                        mfp.fallOpmirq();
                    } else {
                        mfp.riseOpmirq();
                    }
                }
                break;
            case 27:
                set_clock(b >> 7);
                break;
            }
            break;
        case 15278081:
            if ((b & 1) != 0) {
                adpcmout_interrupt_clock = 9223372036854775807L;
                x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock;
                adpcmout_active = false;
            } else if ((b & 2) != 0) {
                adpcmout_interrupt_clock = x68000.clock_count - (x68000.clock_count - 1) % 320000L + 319999L;
                x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                                   : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                adpcmout_data = -1;
                adpcmout_pointer = (int) (adpcmout_interrupt_clock % 400000000 / 320000L);
                adpcmout_active = true;
                dmac.risePCL3();
                dmac.fallPCL3();
            }
            break;
        case 15278083:
            adpcmout_data = b & 255;
            dmac.risePCL3();
            break;
        }
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        write_byte(a, (byte) (s >> 8));
        write_byte(a + 1, (byte) s);
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        write_byte(a, (byte) (i >> 24));
        write_byte(a + 1, (byte) (i >> 16));
        write_byte(a + 2, (byte) (i >> 8));
        write_byte(a + 3, (byte) i);
    }

    public void set_clock(int clock) {
        adpcmout_clock = clock & 1;
        update_adpcmout_sample_rate();
    }

    public void set_divide(int divide) {
        adpcmout_divide = divide & 3;
        update_adpcmout_sample_rate();
    }

    private void update_adpcmout_sample_rate() {
        adpcmout_sample_repeat = adpcmout_sample_repeats[(adpcmout_clock << 2) + adpcmout_divide];
        adpcmout_interrupt_interval = adpcmout_interrupt_intervals[(adpcmout_clock << 2) + adpcmout_divide];
    }

    public void set_pan(int pan) {
        adpcmout_pan = pan & 3;
    }

    int required_size_list[] = {
        882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882, 882
    };

    int required_size_sum = 56448;

    int required_size_pointer = 0;

    public void tick() {
        do {
            if (x68000.sound_interrupt_clock == lineout_interrupt_clock) {
                lineout_interrupt_clock += 400000000;
                x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                                   : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                int required_size = lineout.available();
                required_size_sum = required_size_sum - required_size_list[required_size_pointer] + required_size;
                required_size_list[required_size_pointer] = required_size;
                required_size_pointer = required_size_pointer + 1 < 64 ? required_size_pointer + 1 : 0;
                required_size = required_size_sum >> 6;
                required_size = (required_size < 882 ? 882 : required_size > 2646 ? 2646 : required_size) & -2;
                int required_samples = required_size / 2;
                int balance = 0, input_pointer = 0, output_pointer = 0;
                construct_lineout_block: while (output_pointer < required_size) {
                    balance += required_samples;
                    while (balance > 0) {
                        if (output_pointer >= required_size) {
                            break construct_lineout_block;
                        }
                        int data;
                        data = current_adpcmout_block[input_pointer];
                        lineout_block[output_pointer] = (byte) data;
                        lineout_block[output_pointer + 1] = (byte) (data >> 8);
                        output_pointer += 2;
                        balance -= 1250;
                    }
                    input_pointer += 1;
                }
                try {
                    lineout.write(lineout_block, 0, required_size);
                } catch (Exception e) {
                }
                current_adpcmout_block[0] = 0;
                current_adpcmout_block[1] = 0;
                current_adpcmout_block[2] = 0;
                current_adpcmout_block[3] = 0;
                current_adpcmout_block[4] = 0;
                current_adpcmout_block[5] = 0;
                current_adpcmout_block[6] = 0;
                current_adpcmout_block[7] = 0;
                current_adpcmout_block[8] = 0;
                current_adpcmout_block[9] = 0;
                current_adpcmout_block[10] = 0;
                current_adpcmout_block[11] = 0;
                current_adpcmout_block[12] = 0;
                current_adpcmout_block[13] = 0;
                current_adpcmout_block[14] = 0;
                current_adpcmout_block[15] = 0;
                current_adpcmout_block[16] = 0;
                current_adpcmout_block[17] = 0;
                current_adpcmout_block[18] = 0;
                current_adpcmout_block[19] = 0;
                current_adpcmout_block[20] = 0;
                current_adpcmout_block[21] = 0;
                current_adpcmout_block[22] = 0;
                current_adpcmout_block[23] = 0;
                current_adpcmout_block[24] = 0;
                current_adpcmout_block[25] = 0;
                current_adpcmout_block[26] = 0;
                current_adpcmout_block[27] = 0;
                current_adpcmout_block[28] = 0;
                current_adpcmout_block[29] = 0;
                current_adpcmout_block[30] = 0;
                current_adpcmout_block[31] = 0;
                current_adpcmout_block[32] = 0;
                current_adpcmout_block[33] = 0;
                current_adpcmout_block[34] = 0;
                current_adpcmout_block[35] = 0;
                current_adpcmout_block[36] = 0;
                current_adpcmout_block[37] = 0;
                current_adpcmout_block[38] = 0;
                current_adpcmout_block[39] = 0;
                current_adpcmout_block[40] = 0;
                current_adpcmout_block[41] = 0;
                current_adpcmout_block[42] = 0;
                current_adpcmout_block[43] = 0;
                current_adpcmout_block[44] = 0;
                current_adpcmout_block[45] = 0;
                current_adpcmout_block[46] = 0;
                current_adpcmout_block[47] = 0;
                current_adpcmout_block[48] = 0;
                current_adpcmout_block[49] = 0;
                current_adpcmout_block[50] = 0;
                current_adpcmout_block[51] = 0;
                current_adpcmout_block[52] = 0;
                current_adpcmout_block[53] = 0;
                current_adpcmout_block[54] = 0;
                current_adpcmout_block[55] = 0;
                current_adpcmout_block[56] = 0;
                current_adpcmout_block[57] = 0;
                current_adpcmout_block[58] = 0;
                current_adpcmout_block[59] = 0;
                current_adpcmout_block[60] = 0;
                current_adpcmout_block[61] = 0;
                current_adpcmout_block[62] = 0;
                current_adpcmout_block[63] = 0;
                current_adpcmout_block[64] = 0;
                current_adpcmout_block[65] = 0;
                current_adpcmout_block[66] = 0;
                current_adpcmout_block[67] = 0;
                current_adpcmout_block[68] = 0;
                current_adpcmout_block[69] = 0;
                current_adpcmout_block[70] = 0;
                current_adpcmout_block[71] = 0;
                current_adpcmout_block[72] = 0;
                current_adpcmout_block[73] = 0;
                current_adpcmout_block[74] = 0;
                current_adpcmout_block[75] = 0;
                current_adpcmout_block[76] = 0;
                current_adpcmout_block[77] = 0;
                current_adpcmout_block[78] = 0;
                current_adpcmout_block[79] = 0;
                current_adpcmout_block[80] = 0;
                current_adpcmout_block[81] = 0;
                current_adpcmout_block[82] = 0;
                current_adpcmout_block[83] = 0;
                current_adpcmout_block[84] = 0;
                current_adpcmout_block[85] = 0;
                current_adpcmout_block[86] = 0;
                current_adpcmout_block[87] = 0;
                current_adpcmout_block[88] = 0;
                current_adpcmout_block[89] = 0;
                current_adpcmout_block[90] = 0;
                current_adpcmout_block[91] = 0;
                current_adpcmout_block[92] = 0;
                current_adpcmout_block[93] = 0;
                current_adpcmout_block[94] = 0;
                current_adpcmout_block[95] = 0;
                current_adpcmout_block[96] = 0;
                current_adpcmout_block[97] = 0;
                current_adpcmout_block[98] = 0;
                current_adpcmout_block[99] = 0;
                current_adpcmout_block[100] = 0;
                current_adpcmout_block[101] = 0;
                current_adpcmout_block[102] = 0;
                current_adpcmout_block[103] = 0;
                current_adpcmout_block[104] = 0;
                current_adpcmout_block[105] = 0;
                current_adpcmout_block[106] = 0;
                current_adpcmout_block[107] = 0;
                current_adpcmout_block[108] = 0;
                current_adpcmout_block[109] = 0;
                current_adpcmout_block[110] = 0;
                current_adpcmout_block[111] = 0;
                current_adpcmout_block[112] = 0;
                current_adpcmout_block[113] = 0;
                current_adpcmout_block[114] = 0;
                current_adpcmout_block[115] = 0;
                current_adpcmout_block[116] = 0;
                current_adpcmout_block[117] = 0;
                current_adpcmout_block[118] = 0;
                current_adpcmout_block[119] = 0;
                current_adpcmout_block[120] = 0;
                current_adpcmout_block[121] = 0;
                current_adpcmout_block[122] = 0;
                current_adpcmout_block[123] = 0;
                current_adpcmout_block[124] = 0;
                current_adpcmout_block[125] = 0;
                current_adpcmout_block[126] = 0;
                current_adpcmout_block[127] = 0;
                current_adpcmout_block[128] = 0;
                current_adpcmout_block[129] = 0;
                current_adpcmout_block[130] = 0;
                current_adpcmout_block[131] = 0;
                current_adpcmout_block[132] = 0;
                current_adpcmout_block[133] = 0;
                current_adpcmout_block[134] = 0;
                current_adpcmout_block[135] = 0;
                current_adpcmout_block[136] = 0;
                current_adpcmout_block[137] = 0;
                current_adpcmout_block[138] = 0;
                current_adpcmout_block[139] = 0;
                current_adpcmout_block[140] = 0;
                current_adpcmout_block[141] = 0;
                current_adpcmout_block[142] = 0;
                current_adpcmout_block[143] = 0;
                current_adpcmout_block[144] = 0;
                current_adpcmout_block[145] = 0;
                current_adpcmout_block[146] = 0;
                current_adpcmout_block[147] = 0;
                current_adpcmout_block[148] = 0;
                current_adpcmout_block[149] = 0;
                current_adpcmout_block[150] = 0;
                current_adpcmout_block[151] = 0;
                current_adpcmout_block[152] = 0;
                current_adpcmout_block[153] = 0;
                current_adpcmout_block[154] = 0;
                current_adpcmout_block[155] = 0;
                current_adpcmout_block[156] = 0;
                current_adpcmout_block[157] = 0;
                current_adpcmout_block[158] = 0;
                current_adpcmout_block[159] = 0;
                current_adpcmout_block[160] = 0;
                current_adpcmout_block[161] = 0;
                current_adpcmout_block[162] = 0;
                current_adpcmout_block[163] = 0;
                current_adpcmout_block[164] = 0;
                current_adpcmout_block[165] = 0;
                current_adpcmout_block[166] = 0;
                current_adpcmout_block[167] = 0;
                current_adpcmout_block[168] = 0;
                current_adpcmout_block[169] = 0;
                current_adpcmout_block[170] = 0;
                current_adpcmout_block[171] = 0;
                current_adpcmout_block[172] = 0;
                current_adpcmout_block[173] = 0;
                current_adpcmout_block[174] = 0;
                current_adpcmout_block[175] = 0;
                current_adpcmout_block[176] = 0;
                current_adpcmout_block[177] = 0;
                current_adpcmout_block[178] = 0;
                current_adpcmout_block[179] = 0;
                current_adpcmout_block[180] = 0;
                current_adpcmout_block[181] = 0;
                current_adpcmout_block[182] = 0;
                current_adpcmout_block[183] = 0;
                current_adpcmout_block[184] = 0;
                current_adpcmout_block[185] = 0;
                current_adpcmout_block[186] = 0;
                current_adpcmout_block[187] = 0;
                current_adpcmout_block[188] = 0;
                current_adpcmout_block[189] = 0;
                current_adpcmout_block[190] = 0;
                current_adpcmout_block[191] = 0;
                current_adpcmout_block[192] = 0;
                current_adpcmout_block[193] = 0;
                current_adpcmout_block[194] = 0;
                current_adpcmout_block[195] = 0;
                current_adpcmout_block[196] = 0;
                current_adpcmout_block[197] = 0;
                current_adpcmout_block[198] = 0;
                current_adpcmout_block[199] = 0;
                current_adpcmout_block[200] = 0;
                current_adpcmout_block[201] = 0;
                current_adpcmout_block[202] = 0;
                current_adpcmout_block[203] = 0;
                current_adpcmout_block[204] = 0;
                current_adpcmout_block[205] = 0;
                current_adpcmout_block[206] = 0;
                current_adpcmout_block[207] = 0;
                current_adpcmout_block[208] = 0;
                current_adpcmout_block[209] = 0;
                current_adpcmout_block[210] = 0;
                current_adpcmout_block[211] = 0;
                current_adpcmout_block[212] = 0;
                current_adpcmout_block[213] = 0;
                current_adpcmout_block[214] = 0;
                current_adpcmout_block[215] = 0;
                current_adpcmout_block[216] = 0;
                current_adpcmout_block[217] = 0;
                current_adpcmout_block[218] = 0;
                current_adpcmout_block[219] = 0;
                current_adpcmout_block[220] = 0;
                current_adpcmout_block[221] = 0;
                current_adpcmout_block[222] = 0;
                current_adpcmout_block[223] = 0;
                current_adpcmout_block[224] = 0;
                current_adpcmout_block[225] = 0;
                current_adpcmout_block[226] = 0;
                current_adpcmout_block[227] = 0;
                current_adpcmout_block[228] = 0;
                current_adpcmout_block[229] = 0;
                current_adpcmout_block[230] = 0;
                current_adpcmout_block[231] = 0;
                current_adpcmout_block[232] = 0;
                current_adpcmout_block[233] = 0;
                current_adpcmout_block[234] = 0;
                current_adpcmout_block[235] = 0;
                current_adpcmout_block[236] = 0;
                current_adpcmout_block[237] = 0;
                current_adpcmout_block[238] = 0;
                current_adpcmout_block[239] = 0;
                current_adpcmout_block[240] = 0;
                current_adpcmout_block[241] = 0;
                current_adpcmout_block[242] = 0;
                current_adpcmout_block[243] = 0;
                current_adpcmout_block[244] = 0;
                current_adpcmout_block[245] = 0;
                current_adpcmout_block[246] = 0;
                current_adpcmout_block[247] = 0;
                current_adpcmout_block[248] = 0;
                current_adpcmout_block[249] = 0;
                current_adpcmout_block[250] = 0;
                current_adpcmout_block[251] = 0;
                current_adpcmout_block[252] = 0;
                current_adpcmout_block[253] = 0;
                current_adpcmout_block[254] = 0;
                current_adpcmout_block[255] = 0;
                current_adpcmout_block[256] = 0;
                current_adpcmout_block[257] = 0;
                current_adpcmout_block[258] = 0;
                current_adpcmout_block[259] = 0;
                current_adpcmout_block[260] = 0;
                current_adpcmout_block[261] = 0;
                current_adpcmout_block[262] = 0;
                current_adpcmout_block[263] = 0;
                current_adpcmout_block[264] = 0;
                current_adpcmout_block[265] = 0;
                current_adpcmout_block[266] = 0;
                current_adpcmout_block[267] = 0;
                current_adpcmout_block[268] = 0;
                current_adpcmout_block[269] = 0;
                current_adpcmout_block[270] = 0;
                current_adpcmout_block[271] = 0;
                current_adpcmout_block[272] = 0;
                current_adpcmout_block[273] = 0;
                current_adpcmout_block[274] = 0;
                current_adpcmout_block[275] = 0;
                current_adpcmout_block[276] = 0;
                current_adpcmout_block[277] = 0;
                current_adpcmout_block[278] = 0;
                current_adpcmout_block[279] = 0;
                current_adpcmout_block[280] = 0;
                current_adpcmout_block[281] = 0;
                current_adpcmout_block[282] = 0;
                current_adpcmout_block[283] = 0;
                current_adpcmout_block[284] = 0;
                current_adpcmout_block[285] = 0;
                current_adpcmout_block[286] = 0;
                current_adpcmout_block[287] = 0;
                current_adpcmout_block[288] = 0;
                current_adpcmout_block[289] = 0;
                current_adpcmout_block[290] = 0;
                current_adpcmout_block[291] = 0;
                current_adpcmout_block[292] = 0;
                current_adpcmout_block[293] = 0;
                current_adpcmout_block[294] = 0;
                current_adpcmout_block[295] = 0;
                current_adpcmout_block[296] = 0;
                current_adpcmout_block[297] = 0;
                current_adpcmout_block[298] = 0;
                current_adpcmout_block[299] = 0;
                current_adpcmout_block[300] = 0;
                current_adpcmout_block[301] = 0;
                current_adpcmout_block[302] = 0;
                current_adpcmout_block[303] = 0;
                current_adpcmout_block[304] = 0;
                current_adpcmout_block[305] = 0;
                current_adpcmout_block[306] = 0;
                current_adpcmout_block[307] = 0;
                current_adpcmout_block[308] = 0;
                current_adpcmout_block[309] = 0;
                current_adpcmout_block[310] = 0;
                current_adpcmout_block[311] = 0;
                current_adpcmout_block[312] = 0;
                current_adpcmout_block[313] = 0;
                current_adpcmout_block[314] = 0;
                current_adpcmout_block[315] = 0;
                current_adpcmout_block[316] = 0;
                current_adpcmout_block[317] = 0;
                current_adpcmout_block[318] = 0;
                current_adpcmout_block[319] = 0;
                current_adpcmout_block[320] = 0;
                current_adpcmout_block[321] = 0;
                current_adpcmout_block[322] = 0;
                current_adpcmout_block[323] = 0;
                current_adpcmout_block[324] = 0;
                current_adpcmout_block[325] = 0;
                current_adpcmout_block[326] = 0;
                current_adpcmout_block[327] = 0;
                current_adpcmout_block[328] = 0;
                current_adpcmout_block[329] = 0;
                current_adpcmout_block[330] = 0;
                current_adpcmout_block[331] = 0;
                current_adpcmout_block[332] = 0;
                current_adpcmout_block[333] = 0;
                current_adpcmout_block[334] = 0;
                current_adpcmout_block[335] = 0;
                current_adpcmout_block[336] = 0;
                current_adpcmout_block[337] = 0;
                current_adpcmout_block[338] = 0;
                current_adpcmout_block[339] = 0;
                current_adpcmout_block[340] = 0;
                current_adpcmout_block[341] = 0;
                current_adpcmout_block[342] = 0;
                current_adpcmout_block[343] = 0;
                current_adpcmout_block[344] = 0;
                current_adpcmout_block[345] = 0;
                current_adpcmout_block[346] = 0;
                current_adpcmout_block[347] = 0;
                current_adpcmout_block[348] = 0;
                current_adpcmout_block[349] = 0;
                current_adpcmout_block[350] = 0;
                current_adpcmout_block[351] = 0;
                current_adpcmout_block[352] = 0;
                current_adpcmout_block[353] = 0;
                current_adpcmout_block[354] = 0;
                current_adpcmout_block[355] = 0;
                current_adpcmout_block[356] = 0;
                current_adpcmout_block[357] = 0;
                current_adpcmout_block[358] = 0;
                current_adpcmout_block[359] = 0;
                current_adpcmout_block[360] = 0;
                current_adpcmout_block[361] = 0;
                current_adpcmout_block[362] = 0;
                current_adpcmout_block[363] = 0;
                current_adpcmout_block[364] = 0;
                current_adpcmout_block[365] = 0;
                current_adpcmout_block[366] = 0;
                current_adpcmout_block[367] = 0;
                current_adpcmout_block[368] = 0;
                current_adpcmout_block[369] = 0;
                current_adpcmout_block[370] = 0;
                current_adpcmout_block[371] = 0;
                current_adpcmout_block[372] = 0;
                current_adpcmout_block[373] = 0;
                current_adpcmout_block[374] = 0;
                current_adpcmout_block[375] = 0;
                current_adpcmout_block[376] = 0;
                current_adpcmout_block[377] = 0;
                current_adpcmout_block[378] = 0;
                current_adpcmout_block[379] = 0;
                current_adpcmout_block[380] = 0;
                current_adpcmout_block[381] = 0;
                current_adpcmout_block[382] = 0;
                current_adpcmout_block[383] = 0;
                current_adpcmout_block[384] = 0;
                current_adpcmout_block[385] = 0;
                current_adpcmout_block[386] = 0;
                current_adpcmout_block[387] = 0;
                current_adpcmout_block[388] = 0;
                current_adpcmout_block[389] = 0;
                current_adpcmout_block[390] = 0;
                current_adpcmout_block[391] = 0;
                current_adpcmout_block[392] = 0;
                current_adpcmout_block[393] = 0;
                current_adpcmout_block[394] = 0;
                current_adpcmout_block[395] = 0;
                current_adpcmout_block[396] = 0;
                current_adpcmout_block[397] = 0;
                current_adpcmout_block[398] = 0;
                current_adpcmout_block[399] = 0;
                current_adpcmout_block[400] = 0;
                current_adpcmout_block[401] = 0;
                current_adpcmout_block[402] = 0;
                current_adpcmout_block[403] = 0;
                current_adpcmout_block[404] = 0;
                current_adpcmout_block[405] = 0;
                current_adpcmout_block[406] = 0;
                current_adpcmout_block[407] = 0;
                current_adpcmout_block[408] = 0;
                current_adpcmout_block[409] = 0;
                current_adpcmout_block[410] = 0;
                current_adpcmout_block[411] = 0;
                current_adpcmout_block[412] = 0;
                current_adpcmout_block[413] = 0;
                current_adpcmout_block[414] = 0;
                current_adpcmout_block[415] = 0;
                current_adpcmout_block[416] = 0;
                current_adpcmout_block[417] = 0;
                current_adpcmout_block[418] = 0;
                current_adpcmout_block[419] = 0;
                current_adpcmout_block[420] = 0;
                current_adpcmout_block[421] = 0;
                current_adpcmout_block[422] = 0;
                current_adpcmout_block[423] = 0;
                current_adpcmout_block[424] = 0;
                current_adpcmout_block[425] = 0;
                current_adpcmout_block[426] = 0;
                current_adpcmout_block[427] = 0;
                current_adpcmout_block[428] = 0;
                current_adpcmout_block[429] = 0;
                current_adpcmout_block[430] = 0;
                current_adpcmout_block[431] = 0;
                current_adpcmout_block[432] = 0;
                current_adpcmout_block[433] = 0;
                current_adpcmout_block[434] = 0;
                current_adpcmout_block[435] = 0;
                current_adpcmout_block[436] = 0;
                current_adpcmout_block[437] = 0;
                current_adpcmout_block[438] = 0;
                current_adpcmout_block[439] = 0;
                current_adpcmout_block[440] = 0;
                current_adpcmout_block[441] = 0;
                current_adpcmout_block[442] = 0;
                current_adpcmout_block[443] = 0;
                current_adpcmout_block[444] = 0;
                current_adpcmout_block[445] = 0;
                current_adpcmout_block[446] = 0;
                current_adpcmout_block[447] = 0;
                current_adpcmout_block[448] = 0;
                current_adpcmout_block[449] = 0;
                current_adpcmout_block[450] = 0;
                current_adpcmout_block[451] = 0;
                current_adpcmout_block[452] = 0;
                current_adpcmout_block[453] = 0;
                current_adpcmout_block[454] = 0;
                current_adpcmout_block[455] = 0;
                current_adpcmout_block[456] = 0;
                current_adpcmout_block[457] = 0;
                current_adpcmout_block[458] = 0;
                current_adpcmout_block[459] = 0;
                current_adpcmout_block[460] = 0;
                current_adpcmout_block[461] = 0;
                current_adpcmout_block[462] = 0;
                current_adpcmout_block[463] = 0;
                current_adpcmout_block[464] = 0;
                current_adpcmout_block[465] = 0;
                current_adpcmout_block[466] = 0;
                current_adpcmout_block[467] = 0;
                current_adpcmout_block[468] = 0;
                current_adpcmout_block[469] = 0;
                current_adpcmout_block[470] = 0;
                current_adpcmout_block[471] = 0;
                current_adpcmout_block[472] = 0;
                current_adpcmout_block[473] = 0;
                current_adpcmout_block[474] = 0;
                current_adpcmout_block[475] = 0;
                current_adpcmout_block[476] = 0;
                current_adpcmout_block[477] = 0;
                current_adpcmout_block[478] = 0;
                current_adpcmout_block[479] = 0;
                current_adpcmout_block[480] = 0;
                current_adpcmout_block[481] = 0;
                current_adpcmout_block[482] = 0;
                current_adpcmout_block[483] = 0;
                current_adpcmout_block[484] = 0;
                current_adpcmout_block[485] = 0;
                current_adpcmout_block[486] = 0;
                current_adpcmout_block[487] = 0;
                current_adpcmout_block[488] = 0;
                current_adpcmout_block[489] = 0;
                current_adpcmout_block[490] = 0;
                current_adpcmout_block[491] = 0;
                current_adpcmout_block[492] = 0;
                current_adpcmout_block[493] = 0;
                current_adpcmout_block[494] = 0;
                current_adpcmout_block[495] = 0;
                current_adpcmout_block[496] = 0;
                current_adpcmout_block[497] = 0;
                current_adpcmout_block[498] = 0;
                current_adpcmout_block[499] = 0;
                current_adpcmout_block[500] = 0;
                current_adpcmout_block[501] = 0;
                current_adpcmout_block[502] = 0;
                current_adpcmout_block[503] = 0;
                current_adpcmout_block[504] = 0;
                current_adpcmout_block[505] = 0;
                current_adpcmout_block[506] = 0;
                current_adpcmout_block[507] = 0;
                current_adpcmout_block[508] = 0;
                current_adpcmout_block[509] = 0;
                current_adpcmout_block[510] = 0;
                current_adpcmout_block[511] = 0;
                current_adpcmout_block[512] = 0;
                current_adpcmout_block[513] = 0;
                current_adpcmout_block[514] = 0;
                current_adpcmout_block[515] = 0;
                current_adpcmout_block[516] = 0;
                current_adpcmout_block[517] = 0;
                current_adpcmout_block[518] = 0;
                current_adpcmout_block[519] = 0;
                current_adpcmout_block[520] = 0;
                current_adpcmout_block[521] = 0;
                current_adpcmout_block[522] = 0;
                current_adpcmout_block[523] = 0;
                current_adpcmout_block[524] = 0;
                current_adpcmout_block[525] = 0;
                current_adpcmout_block[526] = 0;
                current_adpcmout_block[527] = 0;
                current_adpcmout_block[528] = 0;
                current_adpcmout_block[529] = 0;
                current_adpcmout_block[530] = 0;
                current_adpcmout_block[531] = 0;
                current_adpcmout_block[532] = 0;
                current_adpcmout_block[533] = 0;
                current_adpcmout_block[534] = 0;
                current_adpcmout_block[535] = 0;
                current_adpcmout_block[536] = 0;
                current_adpcmout_block[537] = 0;
                current_adpcmout_block[538] = 0;
                current_adpcmout_block[539] = 0;
                current_adpcmout_block[540] = 0;
                current_adpcmout_block[541] = 0;
                current_adpcmout_block[542] = 0;
                current_adpcmout_block[543] = 0;
                current_adpcmout_block[544] = 0;
                current_adpcmout_block[545] = 0;
                current_adpcmout_block[546] = 0;
                current_adpcmout_block[547] = 0;
                current_adpcmout_block[548] = 0;
                current_adpcmout_block[549] = 0;
                current_adpcmout_block[550] = 0;
                current_adpcmout_block[551] = 0;
                current_adpcmout_block[552] = 0;
                current_adpcmout_block[553] = 0;
                current_adpcmout_block[554] = 0;
                current_adpcmout_block[555] = 0;
                current_adpcmout_block[556] = 0;
                current_adpcmout_block[557] = 0;
                current_adpcmout_block[558] = 0;
                current_adpcmout_block[559] = 0;
                current_adpcmout_block[560] = 0;
                current_adpcmout_block[561] = 0;
                current_adpcmout_block[562] = 0;
                current_adpcmout_block[563] = 0;
                current_adpcmout_block[564] = 0;
                current_adpcmout_block[565] = 0;
                current_adpcmout_block[566] = 0;
                current_adpcmout_block[567] = 0;
                current_adpcmout_block[568] = 0;
                current_adpcmout_block[569] = 0;
                current_adpcmout_block[570] = 0;
                current_adpcmout_block[571] = 0;
                current_adpcmout_block[572] = 0;
                current_adpcmout_block[573] = 0;
                current_adpcmout_block[574] = 0;
                current_adpcmout_block[575] = 0;
                current_adpcmout_block[576] = 0;
                current_adpcmout_block[577] = 0;
                current_adpcmout_block[578] = 0;
                current_adpcmout_block[579] = 0;
                current_adpcmout_block[580] = 0;
                current_adpcmout_block[581] = 0;
                current_adpcmout_block[582] = 0;
                current_adpcmout_block[583] = 0;
                current_adpcmout_block[584] = 0;
                current_adpcmout_block[585] = 0;
                current_adpcmout_block[586] = 0;
                current_adpcmout_block[587] = 0;
                current_adpcmout_block[588] = 0;
                current_adpcmout_block[589] = 0;
                current_adpcmout_block[590] = 0;
                current_adpcmout_block[591] = 0;
                current_adpcmout_block[592] = 0;
                current_adpcmout_block[593] = 0;
                current_adpcmout_block[594] = 0;
                current_adpcmout_block[595] = 0;
                current_adpcmout_block[596] = 0;
                current_adpcmout_block[597] = 0;
                current_adpcmout_block[598] = 0;
                current_adpcmout_block[599] = 0;
                current_adpcmout_block[600] = 0;
                current_adpcmout_block[601] = 0;
                current_adpcmout_block[602] = 0;
                current_adpcmout_block[603] = 0;
                current_adpcmout_block[604] = 0;
                current_adpcmout_block[605] = 0;
                current_adpcmout_block[606] = 0;
                current_adpcmout_block[607] = 0;
                current_adpcmout_block[608] = 0;
                current_adpcmout_block[609] = 0;
                current_adpcmout_block[610] = 0;
                current_adpcmout_block[611] = 0;
                current_adpcmout_block[612] = 0;
                current_adpcmout_block[613] = 0;
                current_adpcmout_block[614] = 0;
                current_adpcmout_block[615] = 0;
                current_adpcmout_block[616] = 0;
                current_adpcmout_block[617] = 0;
                current_adpcmout_block[618] = 0;
                current_adpcmout_block[619] = 0;
                current_adpcmout_block[620] = 0;
                current_adpcmout_block[621] = 0;
                current_adpcmout_block[622] = 0;
                current_adpcmout_block[623] = 0;
                current_adpcmout_block[624] = 0;
                current_adpcmout_block[625] = 0;
                current_adpcmout_block[626] = 0;
                current_adpcmout_block[627] = 0;
                current_adpcmout_block[628] = 0;
                current_adpcmout_block[629] = 0;
                current_adpcmout_block[630] = 0;
                current_adpcmout_block[631] = 0;
                current_adpcmout_block[632] = 0;
                current_adpcmout_block[633] = 0;
                current_adpcmout_block[634] = 0;
                current_adpcmout_block[635] = 0;
                current_adpcmout_block[636] = 0;
                current_adpcmout_block[637] = 0;
                current_adpcmout_block[638] = 0;
                current_adpcmout_block[639] = 0;
                current_adpcmout_block[640] = 0;
                current_adpcmout_block[641] = 0;
                current_adpcmout_block[642] = 0;
                current_adpcmout_block[643] = 0;
                current_adpcmout_block[644] = 0;
                current_adpcmout_block[645] = 0;
                current_adpcmout_block[646] = 0;
                current_adpcmout_block[647] = 0;
                current_adpcmout_block[648] = 0;
                current_adpcmout_block[649] = 0;
                current_adpcmout_block[650] = 0;
                current_adpcmout_block[651] = 0;
                current_adpcmout_block[652] = 0;
                current_adpcmout_block[653] = 0;
                current_adpcmout_block[654] = 0;
                current_adpcmout_block[655] = 0;
                current_adpcmout_block[656] = 0;
                current_adpcmout_block[657] = 0;
                current_adpcmout_block[658] = 0;
                current_adpcmout_block[659] = 0;
                current_adpcmout_block[660] = 0;
                current_adpcmout_block[661] = 0;
                current_adpcmout_block[662] = 0;
                current_adpcmout_block[663] = 0;
                current_adpcmout_block[664] = 0;
                current_adpcmout_block[665] = 0;
                current_adpcmout_block[666] = 0;
                current_adpcmout_block[667] = 0;
                current_adpcmout_block[668] = 0;
                current_adpcmout_block[669] = 0;
                current_adpcmout_block[670] = 0;
                current_adpcmout_block[671] = 0;
                current_adpcmout_block[672] = 0;
                current_adpcmout_block[673] = 0;
                current_adpcmout_block[674] = 0;
                current_adpcmout_block[675] = 0;
                current_adpcmout_block[676] = 0;
                current_adpcmout_block[677] = 0;
                current_adpcmout_block[678] = 0;
                current_adpcmout_block[679] = 0;
                current_adpcmout_block[680] = 0;
                current_adpcmout_block[681] = 0;
                current_adpcmout_block[682] = 0;
                current_adpcmout_block[683] = 0;
                current_adpcmout_block[684] = 0;
                current_adpcmout_block[685] = 0;
                current_adpcmout_block[686] = 0;
                current_adpcmout_block[687] = 0;
                current_adpcmout_block[688] = 0;
                current_adpcmout_block[689] = 0;
                current_adpcmout_block[690] = 0;
                current_adpcmout_block[691] = 0;
                current_adpcmout_block[692] = 0;
                current_adpcmout_block[693] = 0;
                current_adpcmout_block[694] = 0;
                current_adpcmout_block[695] = 0;
                current_adpcmout_block[696] = 0;
                current_adpcmout_block[697] = 0;
                current_adpcmout_block[698] = 0;
                current_adpcmout_block[699] = 0;
                current_adpcmout_block[700] = 0;
                current_adpcmout_block[701] = 0;
                current_adpcmout_block[702] = 0;
                current_adpcmout_block[703] = 0;
                current_adpcmout_block[704] = 0;
                current_adpcmout_block[705] = 0;
                current_adpcmout_block[706] = 0;
                current_adpcmout_block[707] = 0;
                current_adpcmout_block[708] = 0;
                current_adpcmout_block[709] = 0;
                current_adpcmout_block[710] = 0;
                current_adpcmout_block[711] = 0;
                current_adpcmout_block[712] = 0;
                current_adpcmout_block[713] = 0;
                current_adpcmout_block[714] = 0;
                current_adpcmout_block[715] = 0;
                current_adpcmout_block[716] = 0;
                current_adpcmout_block[717] = 0;
                current_adpcmout_block[718] = 0;
                current_adpcmout_block[719] = 0;
                current_adpcmout_block[720] = 0;
                current_adpcmout_block[721] = 0;
                current_adpcmout_block[722] = 0;
                current_adpcmout_block[723] = 0;
                current_adpcmout_block[724] = 0;
                current_adpcmout_block[725] = 0;
                current_adpcmout_block[726] = 0;
                current_adpcmout_block[727] = 0;
                current_adpcmout_block[728] = 0;
                current_adpcmout_block[729] = 0;
                current_adpcmout_block[730] = 0;
                current_adpcmout_block[731] = 0;
                current_adpcmout_block[732] = 0;
                current_adpcmout_block[733] = 0;
                current_adpcmout_block[734] = 0;
                current_adpcmout_block[735] = 0;
                current_adpcmout_block[736] = 0;
                current_adpcmout_block[737] = 0;
                current_adpcmout_block[738] = 0;
                current_adpcmout_block[739] = 0;
                current_adpcmout_block[740] = 0;
                current_adpcmout_block[741] = 0;
                current_adpcmout_block[742] = 0;
                current_adpcmout_block[743] = 0;
                current_adpcmout_block[744] = 0;
                current_adpcmout_block[745] = 0;
                current_adpcmout_block[746] = 0;
                current_adpcmout_block[747] = 0;
                current_adpcmout_block[748] = 0;
                current_adpcmout_block[749] = 0;
                current_adpcmout_block[750] = 0;
                current_adpcmout_block[751] = 0;
                current_adpcmout_block[752] = 0;
                current_adpcmout_block[753] = 0;
                current_adpcmout_block[754] = 0;
                current_adpcmout_block[755] = 0;
                current_adpcmout_block[756] = 0;
                current_adpcmout_block[757] = 0;
                current_adpcmout_block[758] = 0;
                current_adpcmout_block[759] = 0;
                current_adpcmout_block[760] = 0;
                current_adpcmout_block[761] = 0;
                current_adpcmout_block[762] = 0;
                current_adpcmout_block[763] = 0;
                current_adpcmout_block[764] = 0;
                current_adpcmout_block[765] = 0;
                current_adpcmout_block[766] = 0;
                current_adpcmout_block[767] = 0;
                current_adpcmout_block[768] = 0;
                current_adpcmout_block[769] = 0;
                current_adpcmout_block[770] = 0;
                current_adpcmout_block[771] = 0;
                current_adpcmout_block[772] = 0;
                current_adpcmout_block[773] = 0;
                current_adpcmout_block[774] = 0;
                current_adpcmout_block[775] = 0;
                current_adpcmout_block[776] = 0;
                current_adpcmout_block[777] = 0;
                current_adpcmout_block[778] = 0;
                current_adpcmout_block[779] = 0;
                current_adpcmout_block[780] = 0;
                current_adpcmout_block[781] = 0;
                current_adpcmout_block[782] = 0;
                current_adpcmout_block[783] = 0;
                current_adpcmout_block[784] = 0;
                current_adpcmout_block[785] = 0;
                current_adpcmout_block[786] = 0;
                current_adpcmout_block[787] = 0;
                current_adpcmout_block[788] = 0;
                current_adpcmout_block[789] = 0;
                current_adpcmout_block[790] = 0;
                current_adpcmout_block[791] = 0;
                current_adpcmout_block[792] = 0;
                current_adpcmout_block[793] = 0;
                current_adpcmout_block[794] = 0;
                current_adpcmout_block[795] = 0;
                current_adpcmout_block[796] = 0;
                current_adpcmout_block[797] = 0;
                current_adpcmout_block[798] = 0;
                current_adpcmout_block[799] = 0;
                current_adpcmout_block[800] = 0;
                current_adpcmout_block[801] = 0;
                current_adpcmout_block[802] = 0;
                current_adpcmout_block[803] = 0;
                current_adpcmout_block[804] = 0;
                current_adpcmout_block[805] = 0;
                current_adpcmout_block[806] = 0;
                current_adpcmout_block[807] = 0;
                current_adpcmout_block[808] = 0;
                current_adpcmout_block[809] = 0;
                current_adpcmout_block[810] = 0;
                current_adpcmout_block[811] = 0;
                current_adpcmout_block[812] = 0;
                current_adpcmout_block[813] = 0;
                current_adpcmout_block[814] = 0;
                current_adpcmout_block[815] = 0;
                current_adpcmout_block[816] = 0;
                current_adpcmout_block[817] = 0;
                current_adpcmout_block[818] = 0;
                current_adpcmout_block[819] = 0;
                current_adpcmout_block[820] = 0;
                current_adpcmout_block[821] = 0;
                current_adpcmout_block[822] = 0;
                current_adpcmout_block[823] = 0;
                current_adpcmout_block[824] = 0;
                current_adpcmout_block[825] = 0;
                current_adpcmout_block[826] = 0;
                current_adpcmout_block[827] = 0;
                current_adpcmout_block[828] = 0;
                current_adpcmout_block[829] = 0;
                current_adpcmout_block[830] = 0;
                current_adpcmout_block[831] = 0;
                current_adpcmout_block[832] = 0;
                current_adpcmout_block[833] = 0;
                current_adpcmout_block[834] = 0;
                current_adpcmout_block[835] = 0;
                current_adpcmout_block[836] = 0;
                current_adpcmout_block[837] = 0;
                current_adpcmout_block[838] = 0;
                current_adpcmout_block[839] = 0;
                current_adpcmout_block[840] = 0;
                current_adpcmout_block[841] = 0;
                current_adpcmout_block[842] = 0;
                current_adpcmout_block[843] = 0;
                current_adpcmout_block[844] = 0;
                current_adpcmout_block[845] = 0;
                current_adpcmout_block[846] = 0;
                current_adpcmout_block[847] = 0;
                current_adpcmout_block[848] = 0;
                current_adpcmout_block[849] = 0;
                current_adpcmout_block[850] = 0;
                current_adpcmout_block[851] = 0;
                current_adpcmout_block[852] = 0;
                current_adpcmout_block[853] = 0;
                current_adpcmout_block[854] = 0;
                current_adpcmout_block[855] = 0;
                current_adpcmout_block[856] = 0;
                current_adpcmout_block[857] = 0;
                current_adpcmout_block[858] = 0;
                current_adpcmout_block[859] = 0;
                current_adpcmout_block[860] = 0;
                current_adpcmout_block[861] = 0;
                current_adpcmout_block[862] = 0;
                current_adpcmout_block[863] = 0;
                current_adpcmout_block[864] = 0;
                current_adpcmout_block[865] = 0;
                current_adpcmout_block[866] = 0;
                current_adpcmout_block[867] = 0;
                current_adpcmout_block[868] = 0;
                current_adpcmout_block[869] = 0;
                current_adpcmout_block[870] = 0;
                current_adpcmout_block[871] = 0;
                current_adpcmout_block[872] = 0;
                current_adpcmout_block[873] = 0;
                current_adpcmout_block[874] = 0;
                current_adpcmout_block[875] = 0;
                current_adpcmout_block[876] = 0;
                current_adpcmout_block[877] = 0;
                current_adpcmout_block[878] = 0;
                current_adpcmout_block[879] = 0;
                current_adpcmout_block[880] = 0;
                current_adpcmout_block[881] = 0;
                current_adpcmout_block[882] = 0;
                current_adpcmout_block[883] = 0;
                current_adpcmout_block[884] = 0;
                current_adpcmout_block[885] = 0;
                current_adpcmout_block[886] = 0;
                current_adpcmout_block[887] = 0;
                current_adpcmout_block[888] = 0;
                current_adpcmout_block[889] = 0;
                current_adpcmout_block[890] = 0;
                current_adpcmout_block[891] = 0;
                current_adpcmout_block[892] = 0;
                current_adpcmout_block[893] = 0;
                current_adpcmout_block[894] = 0;
                current_adpcmout_block[895] = 0;
                current_adpcmout_block[896] = 0;
                current_adpcmout_block[897] = 0;
                current_adpcmout_block[898] = 0;
                current_adpcmout_block[899] = 0;
                current_adpcmout_block[900] = 0;
                current_adpcmout_block[901] = 0;
                current_adpcmout_block[902] = 0;
                current_adpcmout_block[903] = 0;
                current_adpcmout_block[904] = 0;
                current_adpcmout_block[905] = 0;
                current_adpcmout_block[906] = 0;
                current_adpcmout_block[907] = 0;
                current_adpcmout_block[908] = 0;
                current_adpcmout_block[909] = 0;
                current_adpcmout_block[910] = 0;
                current_adpcmout_block[911] = 0;
                current_adpcmout_block[912] = 0;
                current_adpcmout_block[913] = 0;
                current_adpcmout_block[914] = 0;
                current_adpcmout_block[915] = 0;
                current_adpcmout_block[916] = 0;
                current_adpcmout_block[917] = 0;
                current_adpcmout_block[918] = 0;
                current_adpcmout_block[919] = 0;
                current_adpcmout_block[920] = 0;
                current_adpcmout_block[921] = 0;
                current_adpcmout_block[922] = 0;
                current_adpcmout_block[923] = 0;
                current_adpcmout_block[924] = 0;
                current_adpcmout_block[925] = 0;
                current_adpcmout_block[926] = 0;
                current_adpcmout_block[927] = 0;
                current_adpcmout_block[928] = 0;
                current_adpcmout_block[929] = 0;
                current_adpcmout_block[930] = 0;
                current_adpcmout_block[931] = 0;
                current_adpcmout_block[932] = 0;
                current_adpcmout_block[933] = 0;
                current_adpcmout_block[934] = 0;
                current_adpcmout_block[935] = 0;
                current_adpcmout_block[936] = 0;
                current_adpcmout_block[937] = 0;
                current_adpcmout_block[938] = 0;
                current_adpcmout_block[939] = 0;
                current_adpcmout_block[940] = 0;
                current_adpcmout_block[941] = 0;
                current_adpcmout_block[942] = 0;
                current_adpcmout_block[943] = 0;
                current_adpcmout_block[944] = 0;
                current_adpcmout_block[945] = 0;
                current_adpcmout_block[946] = 0;
                current_adpcmout_block[947] = 0;
                current_adpcmout_block[948] = 0;
                current_adpcmout_block[949] = 0;
                current_adpcmout_block[950] = 0;
                current_adpcmout_block[951] = 0;
                current_adpcmout_block[952] = 0;
                current_adpcmout_block[953] = 0;
                current_adpcmout_block[954] = 0;
                current_adpcmout_block[955] = 0;
                current_adpcmout_block[956] = 0;
                current_adpcmout_block[957] = 0;
                current_adpcmout_block[958] = 0;
                current_adpcmout_block[959] = 0;
                current_adpcmout_block[960] = 0;
                current_adpcmout_block[961] = 0;
                current_adpcmout_block[962] = 0;
                current_adpcmout_block[963] = 0;
                current_adpcmout_block[964] = 0;
                current_adpcmout_block[965] = 0;
                current_adpcmout_block[966] = 0;
                current_adpcmout_block[967] = 0;
                current_adpcmout_block[968] = 0;
                current_adpcmout_block[969] = 0;
                current_adpcmout_block[970] = 0;
                current_adpcmout_block[971] = 0;
                current_adpcmout_block[972] = 0;
                current_adpcmout_block[973] = 0;
                current_adpcmout_block[974] = 0;
                current_adpcmout_block[975] = 0;
                current_adpcmout_block[976] = 0;
                current_adpcmout_block[977] = 0;
                current_adpcmout_block[978] = 0;
                current_adpcmout_block[979] = 0;
                current_adpcmout_block[980] = 0;
                current_adpcmout_block[981] = 0;
                current_adpcmout_block[982] = 0;
                current_adpcmout_block[983] = 0;
                current_adpcmout_block[984] = 0;
                current_adpcmout_block[985] = 0;
                current_adpcmout_block[986] = 0;
                current_adpcmout_block[987] = 0;
                current_adpcmout_block[988] = 0;
                current_adpcmout_block[989] = 0;
                current_adpcmout_block[990] = 0;
                current_adpcmout_block[991] = 0;
                current_adpcmout_block[992] = 0;
                current_adpcmout_block[993] = 0;
                current_adpcmout_block[994] = 0;
                current_adpcmout_block[995] = 0;
                current_adpcmout_block[996] = 0;
                current_adpcmout_block[997] = 0;
                current_adpcmout_block[998] = 0;
                current_adpcmout_block[999] = 0;
                current_adpcmout_block[1000] = 0;
                current_adpcmout_block[1001] = 0;
                current_adpcmout_block[1002] = 0;
                current_adpcmout_block[1003] = 0;
                current_adpcmout_block[1004] = 0;
                current_adpcmout_block[1005] = 0;
                current_adpcmout_block[1006] = 0;
                current_adpcmout_block[1007] = 0;
                current_adpcmout_block[1008] = 0;
                current_adpcmout_block[1009] = 0;
                current_adpcmout_block[1010] = 0;
                current_adpcmout_block[1011] = 0;
                current_adpcmout_block[1012] = 0;
                current_adpcmout_block[1013] = 0;
                current_adpcmout_block[1014] = 0;
                current_adpcmout_block[1015] = 0;
                current_adpcmout_block[1016] = 0;
                current_adpcmout_block[1017] = 0;
                current_adpcmout_block[1018] = 0;
                current_adpcmout_block[1019] = 0;
                current_adpcmout_block[1020] = 0;
                current_adpcmout_block[1021] = 0;
                current_adpcmout_block[1022] = 0;
                current_adpcmout_block[1023] = 0;
                current_adpcmout_block[1024] = 0;
                current_adpcmout_block[1025] = 0;
                current_adpcmout_block[1026] = 0;
                current_adpcmout_block[1027] = 0;
                current_adpcmout_block[1028] = 0;
                current_adpcmout_block[1029] = 0;
                current_adpcmout_block[1030] = 0;
                current_adpcmout_block[1031] = 0;
                current_adpcmout_block[1032] = 0;
                current_adpcmout_block[1033] = 0;
                current_adpcmout_block[1034] = 0;
                current_adpcmout_block[1035] = 0;
                current_adpcmout_block[1036] = 0;
                current_adpcmout_block[1037] = 0;
                current_adpcmout_block[1038] = 0;
                current_adpcmout_block[1039] = 0;
                current_adpcmout_block[1040] = 0;
                current_adpcmout_block[1041] = 0;
                current_adpcmout_block[1042] = 0;
                current_adpcmout_block[1043] = 0;
                current_adpcmout_block[1044] = 0;
                current_adpcmout_block[1045] = 0;
                current_adpcmout_block[1046] = 0;
                current_adpcmout_block[1047] = 0;
                current_adpcmout_block[1048] = 0;
                current_adpcmout_block[1049] = 0;
                current_adpcmout_block[1050] = 0;
                current_adpcmout_block[1051] = 0;
                current_adpcmout_block[1052] = 0;
                current_adpcmout_block[1053] = 0;
                current_adpcmout_block[1054] = 0;
                current_adpcmout_block[1055] = 0;
                current_adpcmout_block[1056] = 0;
                current_adpcmout_block[1057] = 0;
                current_adpcmout_block[1058] = 0;
                current_adpcmout_block[1059] = 0;
                current_adpcmout_block[1060] = 0;
                current_adpcmout_block[1061] = 0;
                current_adpcmout_block[1062] = 0;
                current_adpcmout_block[1063] = 0;
                current_adpcmout_block[1064] = 0;
                current_adpcmout_block[1065] = 0;
                current_adpcmout_block[1066] = 0;
                current_adpcmout_block[1067] = 0;
                current_adpcmout_block[1068] = 0;
                current_adpcmout_block[1069] = 0;
                current_adpcmout_block[1070] = 0;
                current_adpcmout_block[1071] = 0;
                current_adpcmout_block[1072] = 0;
                current_adpcmout_block[1073] = 0;
                current_adpcmout_block[1074] = 0;
                current_adpcmout_block[1075] = 0;
                current_adpcmout_block[1076] = 0;
                current_adpcmout_block[1077] = 0;
                current_adpcmout_block[1078] = 0;
                current_adpcmout_block[1079] = 0;
                current_adpcmout_block[1080] = 0;
                current_adpcmout_block[1081] = 0;
                current_adpcmout_block[1082] = 0;
                current_adpcmout_block[1083] = 0;
                current_adpcmout_block[1084] = 0;
                current_adpcmout_block[1085] = 0;
                current_adpcmout_block[1086] = 0;
                current_adpcmout_block[1087] = 0;
                current_adpcmout_block[1088] = 0;
                current_adpcmout_block[1089] = 0;
                current_adpcmout_block[1090] = 0;
                current_adpcmout_block[1091] = 0;
                current_adpcmout_block[1092] = 0;
                current_adpcmout_block[1093] = 0;
                current_adpcmout_block[1094] = 0;
                current_adpcmout_block[1095] = 0;
                current_adpcmout_block[1096] = 0;
                current_adpcmout_block[1097] = 0;
                current_adpcmout_block[1098] = 0;
                current_adpcmout_block[1099] = 0;
                current_adpcmout_block[1100] = 0;
                current_adpcmout_block[1101] = 0;
                current_adpcmout_block[1102] = 0;
                current_adpcmout_block[1103] = 0;
                current_adpcmout_block[1104] = 0;
                current_adpcmout_block[1105] = 0;
                current_adpcmout_block[1106] = 0;
                current_adpcmout_block[1107] = 0;
                current_adpcmout_block[1108] = 0;
                current_adpcmout_block[1109] = 0;
                current_adpcmout_block[1110] = 0;
                current_adpcmout_block[1111] = 0;
                current_adpcmout_block[1112] = 0;
                current_adpcmout_block[1113] = 0;
                current_adpcmout_block[1114] = 0;
                current_adpcmout_block[1115] = 0;
                current_adpcmout_block[1116] = 0;
                current_adpcmout_block[1117] = 0;
                current_adpcmout_block[1118] = 0;
                current_adpcmout_block[1119] = 0;
                current_adpcmout_block[1120] = 0;
                current_adpcmout_block[1121] = 0;
                current_adpcmout_block[1122] = 0;
                current_adpcmout_block[1123] = 0;
                current_adpcmout_block[1124] = 0;
                current_adpcmout_block[1125] = 0;
                current_adpcmout_block[1126] = 0;
                current_adpcmout_block[1127] = 0;
                current_adpcmout_block[1128] = 0;
                current_adpcmout_block[1129] = 0;
                current_adpcmout_block[1130] = 0;
                current_adpcmout_block[1131] = 0;
                current_adpcmout_block[1132] = 0;
                current_adpcmout_block[1133] = 0;
                current_adpcmout_block[1134] = 0;
                current_adpcmout_block[1135] = 0;
                current_adpcmout_block[1136] = 0;
                current_adpcmout_block[1137] = 0;
                current_adpcmout_block[1138] = 0;
                current_adpcmout_block[1139] = 0;
                current_adpcmout_block[1140] = 0;
                current_adpcmout_block[1141] = 0;
                current_adpcmout_block[1142] = 0;
                current_adpcmout_block[1143] = 0;
                current_adpcmout_block[1144] = 0;
                current_adpcmout_block[1145] = 0;
                current_adpcmout_block[1146] = 0;
                current_adpcmout_block[1147] = 0;
                current_adpcmout_block[1148] = 0;
                current_adpcmout_block[1149] = 0;
                current_adpcmout_block[1150] = 0;
                current_adpcmout_block[1151] = 0;
                current_adpcmout_block[1152] = 0;
                current_adpcmout_block[1153] = 0;
                current_adpcmout_block[1154] = 0;
                current_adpcmout_block[1155] = 0;
                current_adpcmout_block[1156] = 0;
                current_adpcmout_block[1157] = 0;
                current_adpcmout_block[1158] = 0;
                current_adpcmout_block[1159] = 0;
                current_adpcmout_block[1160] = 0;
                current_adpcmout_block[1161] = 0;
                current_adpcmout_block[1162] = 0;
                current_adpcmout_block[1163] = 0;
                current_adpcmout_block[1164] = 0;
                current_adpcmout_block[1165] = 0;
                current_adpcmout_block[1166] = 0;
                current_adpcmout_block[1167] = 0;
                current_adpcmout_block[1168] = 0;
                current_adpcmout_block[1169] = 0;
                current_adpcmout_block[1170] = 0;
                current_adpcmout_block[1171] = 0;
                current_adpcmout_block[1172] = 0;
                current_adpcmout_block[1173] = 0;
                current_adpcmout_block[1174] = 0;
                current_adpcmout_block[1175] = 0;
                current_adpcmout_block[1176] = 0;
                current_adpcmout_block[1177] = 0;
                current_adpcmout_block[1178] = 0;
                current_adpcmout_block[1179] = 0;
                current_adpcmout_block[1180] = 0;
                current_adpcmout_block[1181] = 0;
                current_adpcmout_block[1182] = 0;
                current_adpcmout_block[1183] = 0;
                current_adpcmout_block[1184] = 0;
                current_adpcmout_block[1185] = 0;
                current_adpcmout_block[1186] = 0;
                current_adpcmout_block[1187] = 0;
                current_adpcmout_block[1188] = 0;
                current_adpcmout_block[1189] = 0;
                current_adpcmout_block[1190] = 0;
                current_adpcmout_block[1191] = 0;
                current_adpcmout_block[1192] = 0;
                current_adpcmout_block[1193] = 0;
                current_adpcmout_block[1194] = 0;
                current_adpcmout_block[1195] = 0;
                current_adpcmout_block[1196] = 0;
                current_adpcmout_block[1197] = 0;
                current_adpcmout_block[1198] = 0;
                current_adpcmout_block[1199] = 0;
                current_adpcmout_block[1200] = 0;
                current_adpcmout_block[1201] = 0;
                current_adpcmout_block[1202] = 0;
                current_adpcmout_block[1203] = 0;
                current_adpcmout_block[1204] = 0;
                current_adpcmout_block[1205] = 0;
                current_adpcmout_block[1206] = 0;
                current_adpcmout_block[1207] = 0;
                current_adpcmout_block[1208] = 0;
                current_adpcmout_block[1209] = 0;
                current_adpcmout_block[1210] = 0;
                current_adpcmout_block[1211] = 0;
                current_adpcmout_block[1212] = 0;
                current_adpcmout_block[1213] = 0;
                current_adpcmout_block[1214] = 0;
                current_adpcmout_block[1215] = 0;
                current_adpcmout_block[1216] = 0;
                current_adpcmout_block[1217] = 0;
                current_adpcmout_block[1218] = 0;
                current_adpcmout_block[1219] = 0;
                current_adpcmout_block[1220] = 0;
                current_adpcmout_block[1221] = 0;
                current_adpcmout_block[1222] = 0;
                current_adpcmout_block[1223] = 0;
                current_adpcmout_block[1224] = 0;
                current_adpcmout_block[1225] = 0;
                current_adpcmout_block[1226] = 0;
                current_adpcmout_block[1227] = 0;
                current_adpcmout_block[1228] = 0;
                current_adpcmout_block[1229] = 0;
                current_adpcmout_block[1230] = 0;
                current_adpcmout_block[1231] = 0;
                current_adpcmout_block[1232] = 0;
                current_adpcmout_block[1233] = 0;
                current_adpcmout_block[1234] = 0;
                current_adpcmout_block[1235] = 0;
                current_adpcmout_block[1236] = 0;
                current_adpcmout_block[1237] = 0;
                current_adpcmout_block[1238] = 0;
                current_adpcmout_block[1239] = 0;
                current_adpcmout_block[1240] = 0;
                current_adpcmout_block[1241] = 0;
                current_adpcmout_block[1242] = 0;
                current_adpcmout_block[1243] = 0;
                current_adpcmout_block[1244] = 0;
                current_adpcmout_block[1245] = 0;
                current_adpcmout_block[1246] = 0;
                current_adpcmout_block[1247] = 0;
                current_adpcmout_block[1248] = 0;
                current_adpcmout_block[1249] = 0;
                {
                    int temp[] = current_adpcmout_block;
                    current_adpcmout_block = next_adpcmout_block;
                    next_adpcmout_block = temp;
                }
            }
            if (x68000.sound_interrupt_clock == ta_interrupt_clock) {
                ta_interrupt_clock += (1024 - clka << 4) * 10000L;
                x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                                   : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                if (irqena) {
                    ista = true;
                    mfp.fallOpmirq();
                }
            }
            if (x68000.sound_interrupt_clock == tb_interrupt_clock) {
                tb_interrupt_clock += (256 - clkb << 8) * 10000L;
                x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                                   : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                if (irqenb) {
                    istb = true;
                    mfp.fallOpmirq();
                }
            }
            if (x68000.sound_interrupt_clock == adpcmout_interrupt_clock) {
                adpcmout_interrupt_clock += adpcmout_interrupt_interval;
                x68000.sound_interrupt_clock = ta_interrupt_clock <= tb_interrupt_clock ? ta_interrupt_clock <= adpcmout_interrupt_clock ? ta_interrupt_clock <= lineout_interrupt_clock ? ta_interrupt_clock : lineout_interrupt_clock : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock : tb_interrupt_clock <= adpcmout_interrupt_clock ? tb_interrupt_clock <= lineout_interrupt_clock ? tb_interrupt_clock : lineout_interrupt_clock
                                                                                                                                                                                                                                                                                                                                                                                                   : adpcmout_interrupt_clock <= lineout_interrupt_clock ? adpcmout_interrupt_clock : lineout_interrupt_clock;
                if (adpcmout_data >= 0) {
                    int block[] = current_adpcmout_block;
                    adpcm2pcm_pointer += adpcmout_data & 255;
                    decoded_13bits_pcm_data += adpcm2pcm_table[adpcm2pcm_pointer];
                    decoded_13bits_pcm_data = decoded_13bits_pcm_data < -4096 ? -4096 : decoded_13bits_pcm_data > 4095 ? 4095 : decoded_13bits_pcm_data;
                    switch (adpcmout_sample_repeat) {
                    case 8:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 7:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 6:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 5:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 4:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 3:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 2:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 1:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    }
                    decoded_13bits_pcm_data += adpcm2pcm_table[adpcm2pcm_pointer + 256];
                    adpcm2pcm_pointer = adpcm2pcm_table[adpcm2pcm_pointer + 512];
                    decoded_13bits_pcm_data = decoded_13bits_pcm_data < -4096 ? -4096 : decoded_13bits_pcm_data > 4095 ? 4095 : decoded_13bits_pcm_data;
                    switch (adpcmout_sample_repeat) {
                    case 8:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 7:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 6:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 5:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 4:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 3:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 2:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 1:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    }
                    adpcmout_data = -1;
                } else {
                    int block[] = current_adpcmout_block;
                    switch (adpcmout_sample_repeat * 2) {
                    case 16:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 15:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 14:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 13:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 12:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 11:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 10:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 9:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 8:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 7:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 6:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 5:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 4:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 3:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 2:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    case 1:
                        block[adpcmout_pointer++] = adpcmout_pan != 3 ? decoded_13bits_pcm_data << 3 : 0;
                        if (adpcmout_pointer == 1250) {
                            block = next_adpcmout_block;
                            adpcmout_pointer = 0;
                        }
                    }
                }
                dmac.fallPCL3();
            }
        } while (x68000.sound_interrupt_clock <= x68000.clock_count);
    }
}

