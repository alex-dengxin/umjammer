/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class CRTC extends MemoryMappedDevice implements X68000Device {
    final private static int LOW_CHAR_CLOCK[] = {
        16468, 8234, 5489, 4117
    };

    final private static int HIGH_CHAR_CLOCK[] = {
        6900, 3450, 2300, 1725
    };

    final private static int H_STRETCH_0[] = {
        21, 11, 8, 8
    };

    final private static int H_STRETCH_1[] = {
        28, 14, 11, 11
    };

    public int r00, r01, r02, r03, r04, r05, r06, r07;
    public int r08, r09, r10, r11, r20, r21, r22, r23;
    public int r24;
    public int g_scroll_x[], g_scroll_y[];
    private int rint_number;
    private int stage;
    private int hrl_mode;
    private int h_resolution_mode;
    private int v_resolution_mode;
    private boolean high_resolution;
    public int h_stretch_mode;
    public boolean duplicate_raster;
    public boolean interlace;
    public boolean slit;
    private int even_odd;
    private int char_clock;
    private int h_disp_length;
    private int h_blank_length;
    private int h_sync_length;
    private int raster_number;
    private int data_y;
    private int bitmap_y;
    public int frame_skip_initial;
    private int frame_skip_counter;
    public int sc[], dc[];
    private int asc, adc, tmp_asc;
    private boolean force_update_frame;
    private long contrast_clock;
    private int current_contrast;
    private int target_contrast;
    private boolean clear_standby;
    private boolean clear_in_execution;

    private X68000 x68000;
    private TextScreen text_screen;
    private GraphicScreen graphic_screen;
    private Video video;
    private MFP mfp;
    private SpriteScreen sprite_screen;

    public CRTC() {
        g_scroll_x = new int[4];
        g_scroll_y = new int[4];
        sc = new int[1039];
        dc = new int[1039];
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        text_screen = x68000.text_screen;
        graphic_screen = x68000.graphic_screen;
        video = x68000.video;
        mfp = x68000.mfp;
        sprite_screen = x68000.sprite_screen;
        clear_standby = false;
        clear_in_execution = false;
        reset();
        return true;
    }

    public void reset() {
        r00 = 0;
        r01 = 0;
        r02 = 0;
        r03 = 0;
        r04 = 0;
        r05 = 0;
        r06 = 0;
        r07 = 0;
        r08 = 0;
        r09 = 0;
        r10 = 0;
        r11 = 0;
        for (int i = 0; i <= 3; i++) {
            g_scroll_x[i] = 0;
            g_scroll_y[i] = 0;
        }
        r20 = 0;
        r21 = 0;
        r22 = 0;
        r23 = 0;
        r24 = 0;
        rint_number = -1;
        x68000.crtc_interrupt_clock = 9223372036854775807L;
        stage = 0;
        hrl_mode = 0;
        frame_skip_initial = 2;
        asc = 1;
        adc = 0;
        h_stretch_mode = 8;
        duplicate_raster = false;
        contrast_clock = 9223372036854775807L;
        current_contrast = 15;
    }

    public void setContrast(int contrast) {
        target_contrast = contrast & 15;
        contrast_clock = x68000.clock_count;
    }

    public void updateAll() {
        asc++;
    }

    public byte read_byte(int a) throws MC68000Exception {
        switch (a) {
        case 15204352:
            return 0;
        case 15204353:
            return (byte) r00;
        case 15204354:
            return 0;
        case 15204355:
            return (byte) r01;
        case 15204356:
            return 0;
        case 15204357:
            return (byte) r02;
        case 15204358:
            return 0;
        case 15204359:
            return (byte) r03;
        case 15204360:
            return (byte) (r04 >> 8);
        case 15204361:
            return (byte) r04;
        case 15204362:
            return (byte) (r05 >> 8);
        case 15204363:
            return (byte) r05;
        case 15204364:
            return (byte) (r06 >> 8);
        case 15204365:
            return (byte) r06;
        case 15204366:
            return (byte) (r07 >> 8);
        case 15204367:
            return (byte) r07;
        case 15204368:
            return 0;
        case 15204369:
            return (byte) r08;
        case 15204370:
            return (byte) (r09 >> 8);
        case 15204371:
            return (byte) r09;
        case 15204372:
            return (byte) (r10 >> 8);
        case 15204373:
            return (byte) r10;
        case 15204374:
            return (byte) (r11 >> 8);
        case 15204375:
            return (byte) r11;
        case 15204376:
        case 15204380:
        case 15204384:
        case 15204388:
            return (byte) (g_scroll_x[a - 15204376 >> 2] >> 8);
        case 15204377:
        case 15204381:
        case 15204385:
        case 15204389:
            return (byte) g_scroll_x[a - 15204376 >> 2];
        case 15204378:
        case 15204382:
        case 15204386:
        case 15204390:
            return (byte) (g_scroll_y[a - 15204378 >> 2] >> 8);
        case 15204379:
        case 15204383:
        case 15204387:
        case 15204391:
            return (byte) g_scroll_y[a - 15204378 >> 2];
        case 15204392:
            return (byte) (r20 >> 8);
        case 15204393:
            return (byte) r20;
        case 15204394:
            return (byte) (r21 >> 8);
        case 15204395:
            return (byte) r21;
        case 15204396:
            return (byte) (r22 >> 8);
        case 15204397:
            return (byte) r22;
        case 15204398:
            return (byte) (r23 >> 8);
        case 15204399:
            return (byte) r23;
        case 15204400:
            return (byte) (r24 >> 8);
        case 15204401:
            return (byte) r24;
        case 15205504:
            return 0;
        case 15205505:
            return (byte) (clear_in_execution ? 2 : 0);
        }
        return 0;
    }

    public short read_short_big(int a) throws MC68000Exception {
        switch (a) {
        case 15204352:
            return (short) r00;
        case 15204354:
            return (short) r01;
        case 15204356:
            return (short) r02;
        case 15204358:
            return (short) r03;
        case 15204360:
            return (short) r04;
        case 15204362:
            return (short) r05;
        case 15204364:
            return (short) r06;
        case 15204366:
            return (short) r07;
        case 15204368:
            return (short) r08;
        case 15204370:
            return (short) r09;
        case 15204372:
            return (short) r10;
        case 15204374:
            return (short) r11;
        case 15204376:
        case 15204380:
        case 15204384:
        case 15204388:
            return (short) g_scroll_x[a - 15204376 >> 2];
        case 15204378:
        case 15204382:
        case 15204386:
        case 15204390:
            return (short) g_scroll_y[a - 15204378 >> 2];
        case 15204392:
            return (short) r20;
        case 15204394:
            return (short) r21;
        case 15204396:
            return (short) r22;
        case 15204398:
            return (short) r23;
        case 15204400:
            return (short) r24;
        case 15205504:
            return (short) (clear_in_execution ? 2 : 0);
        }
        return 0;
    }

    public int read_int_big(int a) throws MC68000Exception {
        return (read_short_big(a) << 16) + (read_short_big(a + 2) & 65535);
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        int t;
        switch (a) {
        case 15204352:
            return;
        case 15204353:
            t = b & 255;
            if (t != r00) {
                r00 = t;
                restart_from_stage0();
            }
            return;
        case 15204354:
            return;
        case 15204355:
            t = b & 255;
            if (t != r01) {
                r01 = t;
                restart_from_stage0();
            }
            return;
        case 15204356:
            return;
        case 15204357:
            t = b & 255;
            if (t != r02) {
                r02 = t;
                restart_from_stage0();
            }
            return;
        case 15204358:
            return;
        case 15204359:
            t = b & 255;
            if (t != r03) {
                r03 = t;
                restart_from_stage0();
            }
            return;
        case 15204360:
            t = ((b & 3) << 8) + (r04 & 255);
            if (t != r04) {
                r04 = t;
                restart_from_stage0();
            }
            return;
        case 15204361:
            t = (r04 & 768) + (b & 255);
            if (t != r04) {
                r04 = t;
                restart_from_stage0();
            }
            return;
        case 15204362:
            t = ((b & 3) << 8) + (r05 & 255);
            if (t != r05) {
                r05 = t;
                restart_from_stage0();
            }
            return;
        case 15204363:
            t = (r05 & 768) + (b & 255);
            if (t != r05) {
                r05 = t;
                restart_from_stage0();
            }
            return;
        case 15204364:
            t = ((b & 3) << 8) + (r06 & 255);
            if (t != r06) {
                r06 = t;
                restart_from_stage0();
                calc_rint_number();
            }
            return;
        case 15204365:
            t = (r06 & 768) + (b & 255);
            if (t != r06) {
                r06 = t;
                restart_from_stage0();
                calc_rint_number();
            }
            return;
        case 15204366:
            t = ((b & 3) << 8) + (r07 & 255);
            if (t != r07) {
                r07 = t;
                restart_from_stage0();
            }
            return;
        case 15204367:
            t = (r07 & 768) + (b & 255);
            if (t != r07) {
                r07 = t;
                restart_from_stage0();
            }
            return;
        case 15204368:
            return;
        case 15204369:
            r08 = b & 255;
            return;
        case 15204370:
            r09 = ((b & 3) << 8) + (r09 & 255);
            calc_rint_number();
            return;
        case 15204371:
            r09 = (r09 & 768) + (b & 255);
            calc_rint_number();
            return;
        case 15204372:
            r10 = ((b & 3) << 8) + (r10 & 255);
            text_screen.setScrollX(r10);
            return;
        case 15204373:
            r10 = (r10 & 768) + (b & 255);
            text_screen.setScrollX(r10);
            return;
        case 15204374:
            r11 = ((b & 3) << 8) + (r11 & 255);
            text_screen.setScrollX(r11);
            return;
        case 15204375:
            r11 = (r11 & 768) + (b & 255);
            text_screen.setScrollX(r11);
            return;
        case 15204376:
        case 15204380:
        case 15204384:
        case 15204388:
            g_scroll_x[a - 15204376 >> 2] = ((b & 3) << 8) + (g_scroll_x[a - 15204376 >> 2] & 255);
            updateAll();
            return;
        case 15204377:
        case 15204381:
        case 15204385:
        case 15204389:
            g_scroll_x[a - 15204376 >> 2] = (g_scroll_x[a - 15204376 >> 2] & 768) + (b & 255);
            updateAll();
            return;
        case 15204378:
        case 15204382:
        case 15204386:
        case 15204390:
            g_scroll_y[a - 15204378 >> 2] = ((b & 3) << 8) + (g_scroll_y[a - 15204378 >> 2] & 255);
            updateAll();
            return;
        case 15204379:
        case 15204383:
        case 15204387:
        case 15204391:
            g_scroll_y[a - 15204378 >> 2] = (g_scroll_y[a - 15204378 >> 2] & 768) + (b & 255);
            updateAll();
            return;
        case 15204392:
            graphic_screen.changeMode(b & 7);
            r20 = ((b & 31) << 8) + (r20 & 31);
            return;
        case 15204393:
            t = (r20 & 7936) + (b & 31);
            if (t != r20) {
                r20 = t;
                restart_from_stage0();
                calc_rint_number();
            }
            return;
        case 15204394:
            r21 = ((b & 3) << 8) + (r21 & 255);
            text_screen.setMode(b);
            return;
        case 15204395:
            r21 = (r21 & 768) + (b & 255);
            text_screen.setPlane(b);
            graphic_screen.setPlane(b);
            return;
        case 15204396:
            r22 = ((b & 255) << 8) + (r22 & 255);
            text_screen.setSourceRaster(b);
            return;
        case 15204397:
            r22 = (r22 & 65280) + (b & 255);
            text_screen.setDestinationRaster(b);
            return;
        case 15204398:
            r23 = ((b & 255) << 8) + (r23 & 255);
            text_screen.setMaskHigh(b);
            return;
        case 15204399:
            r23 = (r23 & 65280) + (b & 255);
            text_screen.setMaskLow(b);
            return;
        case 15204400:
            r24 = ((b & 255) << 8) + (r24 & 255);
            return;
        case 15204401:
            r24 = (r24 & 65280) + (b & 255);
            return;
        case 15205504:
            return;
        case 15205505:
            if ((b & 2) != 0) {
                clear_standby = true;
            }
            if ((b & 8) != 0) {
                text_screen.rasterCopy();
            }
            return;
        }
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        int t;
        switch (a) {
        case 15204352:
            t = s & 255;
            if (t != r00) {
                r00 = t;
                restart_from_stage0();
            }
            return;
        case 15204354:
            t = s & 255;
            if (t != r01) {
                r01 = t;
                restart_from_stage0();
            }
            return;
        case 15204356:
            t = s & 255;
            if (t != r02) {
                r02 = t;
                restart_from_stage0();
            }
            return;
        case 15204358:
            t = s & 255;
            if (t != r03) {
                r03 = t;
                restart_from_stage0();
            }
            return;
        case 15204360:
            t = s & 1023;
            if (t != r04) {
                r04 = t;
                restart_from_stage0();
            }
            return;
        case 15204362:
            t = s & 1023;
            if (t != r05) {
                r05 = t;
                restart_from_stage0();
            }
            return;
        case 15204364:
            t = s & 1023;
            if (t != r06) {
                r06 = t;
                restart_from_stage0();
                calc_rint_number();
            }
            return;
        case 15204366:
            t = s & 1023;
            if (t != r07) {
                r07 = t;
                restart_from_stage0();
            }
            return;
        case 15204368:
            r08 = s & 255;
            return;
        case 15204370:
            r09 = s & 1023;
            calc_rint_number();
            return;
        case 15204372:
            r10 = s & 1023;
            text_screen.setScrollX(r10);
            return;
        case 15204374:
            r11 = s & 1023;
            text_screen.setScrollY(r11);
            return;
        case 15204376:
            g_scroll_x[0] = s & 1023;
            updateAll();
            return;
        case 15204378:
            g_scroll_y[0] = s & 1023;
            updateAll();
            return;
        case 15204380:
        case 15204384:
        case 15204388:
            g_scroll_x[a - 15204376 >> 2] = s & 511;
            updateAll();
            return;
        case 15204382:
        case 15204386:
        case 15204390:
            g_scroll_y[a - 15204378 >> 2] = s & 511;
            updateAll();
            return;
        case 15204392:
            graphic_screen.changeMode(s >> 8 & 7);
            t = s & 7967;
            if (t != r20) {
                r20 = t;
                restart_from_stage0();
                calc_rint_number();
            }
            return;
        case 15204394:
            r21 = s & 1023;
            text_screen.setMode(s >> 8);
            text_screen.setPlane(s);
            graphic_screen.setPlane(s);
            return;
        case 15204396:
            r22 = s & 65535;
            text_screen.setSourceRaster(s >> 8);
            text_screen.setDestinationRaster(s);
            return;
        case 15204398:
            r23 = s & 65535;
            text_screen.setMaskHigh(s >> 8);
            text_screen.setMaskLow(s);
            return;
        case 15204400:
            r24 = s & 65535;
            return;
        case 15205504:
            if ((s & 2) != 0) {
                clear_standby = true;
            }
            if ((s & 8) != 0) {
                text_screen.rasterCopy();
            }
            return;
        }
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        write_short_big(a, (short) (i >> 16));
        write_short_big(a + 2, (short) i);
    }

    private void calc_rint_number() {
        rint_number = r09 + r05 + 2;
    }

    private void restart_from_stage0() {
        mfp.fallVdisp();
        mfp.fallHsync();
        updateAll();
        stage = 0;
        x68000.crtc_interrupt_clock = x68000.clock_count + 5000000000L;
    }

    public void setHRL(int mode) {
        mode &= 1;
        if (mode != hrl_mode) {
            hrl_mode = mode;
            restart_from_stage0();
        }
    }

    public int getHRL() {
        return hrl_mode;
    }

    public void tick() {
        int sc_tmp;
        switch (stage) {
        case 0:
            if (r01 > r02) {
                r01 = r02;
            }
            if (r01 > r02 || r02 >= r03 || r03 > r00 || r05 > r06 || r06 >= r07 || r07 > r04) {
                reset();
                return;
            }
            h_resolution_mode = r20 & 3;
            v_resolution_mode = r20 >> 2 & 3;
            high_resolution = (r20 & 16) != 0;
            h_stretch_mode = hrl_mode == 0 ? H_STRETCH_0[h_resolution_mode] : H_STRETCH_1[h_resolution_mode];
            duplicate_raster = high_resolution && v_resolution_mode == 0;
            interlace = !high_resolution && v_resolution_mode >= 1 || high_resolution && v_resolution_mode >= 2;
            slit = !high_resolution && v_resolution_mode == 0;
            char_clock = high_resolution ? HIGH_CHAR_CLOCK[h_resolution_mode] : LOW_CHAR_CLOCK[h_resolution_mode];
            if (hrl_mode == 1) {
                char_clock = ((char_clock << 2) + 1) / 3;
            }
            h_blank_length = r01 * char_clock;
            h_disp_length = (r00 - r01) * char_clock;
            h_sync_length = r00 * char_clock;
            video.initScreen(r03 - r02 << 3, interlace || slit ? r07 - r06 << 1 : r07 - r06, h_stretch_mode);
            for (int i = 0; i <= 1038; i++) {
                sc[i] = 0;
                dc[i] = 0;
            }
            if (interlace) {
                even_odd = 0;
            }
            frame_skip_counter = 0;
            stage = duplicate_raster ? 18 : interlace ? 41 : slit ? 58 : 1;
            return;
        case 1:
            if (clear_in_execution) {
                clear_in_execution = false;
            }
            raster_number = 0;
            mfp.fallVdisp();
            mfp.riseRint();
        case 2:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 4;
            } else {
                stage = 3;
            }
            return;
        case 3:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            stage = raster_number < r05 ? 2 : 5;
            return;
        case 4:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            stage = raster_number < r05 ? 2 : 5;
            return;
        case 5:
            if (clear_standby) {
                clear_standby = false;
                clear_in_execution = true;
                graphic_screen.clear();
            }
            if (x68000.clock_count >= contrast_clock) {
                if (current_contrast == target_contrast) {
                    contrast_clock = 9223372036854775807L;
                } else {
                    current_contrast += current_contrast < target_contrast ? 1 : -1;
                    video.setContrast(current_contrast);
                    contrast_clock += 1000000000L;
                }
            }
            mfp.riseVdisp();
        case 6:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 8;
            } else {
                stage = 7;
            }
            return;
        case 7:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (raster_number < r06) {
                stage = 6;
            } else if (frame_skip_counter == 0) {
                tmp_asc = asc;
                force_update_frame = tmp_asc != adc;
                data_y = 0;
                bitmap_y = 0;
                stage = 9;
            } else {
                stage = 12;
            }
            return;
        case 8:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (raster_number < r06) {
                stage = 6;
            } else if (frame_skip_counter == 0) {
                tmp_asc = asc;
                force_update_frame = tmp_asc != adc;
                data_y = 0;
                bitmap_y = 0;
                stage = 9;
            } else {
                stage = 12;
            }
            return;
        case 9:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 11;
            } else {
                stage = 10;
            }
            return;
        case 10:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            sc_tmp = sc[data_y];
            if (force_update_frame || sc_tmp != dc[data_y]) {
                video.drawRaster(data_y, bitmap_y);
                dc[data_y] = sc_tmp;
            }
            data_y++;
            bitmap_y++;
            if (raster_number < r07) {
                stage = 9;
            } else {
                if (video.updateImage()) {
                    frame_skip_counter = frame_skip_initial;
                }
                if (force_update_frame) {
                    adc = tmp_asc;
                }
                stage = 15;
            }
            return;
        case 11:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            sc_tmp = sc[data_y];
            if (force_update_frame || sc_tmp != dc[data_y]) {
                video.drawRaster(data_y, bitmap_y);
                dc[data_y] = sc_tmp;
            }
            data_y++;
            bitmap_y++;
            if (raster_number < r07) {
                stage = 9;
            } else {
                if (video.updateImage()) {
                    frame_skip_counter = frame_skip_initial;
                }
                if (force_update_frame) {
                    adc = tmp_asc;
                }
                stage = 15;
            }
            return;
        case 12:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 14;
            } else {
                stage = 13;
            }
            return;
        case 13:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (raster_number < r07) {
                stage = 12;
            } else {
                frame_skip_counter--;
                stage = 15;
            }
            return;
        case 14:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (raster_number < r07) {
                stage = 12;
            } else {
                frame_skip_counter--;
                stage = 15;
            }
            return;
        case 15:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 17;
            } else {
                stage = 16;
            }
            return;
        case 16:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            stage = raster_number < r04 ? 15 : 1;
            return;
        case 17:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            stage = raster_number < r04 ? 15 : 1;
            return;
        case 18:
            if (clear_in_execution) {
                clear_in_execution = false;
            }
            raster_number = 0;
            mfp.fallVdisp();
            mfp.riseRint();
        case 19:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 21;
            } else {
                stage = 20;
            }
            return;
        case 20:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            stage = raster_number < r05 ? 19 : 22;
            return;
        case 21:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            stage = raster_number < r05 ? 19 : 22;
            return;
        case 22:
            if (clear_standby) {
                clear_standby = false;
                clear_in_execution = true;
                graphic_screen.clear();
            }
            if (x68000.clock_count >= contrast_clock) {
                if (current_contrast == target_contrast) {
                    contrast_clock = 9223372036854775807L;
                } else {
                    current_contrast += current_contrast < target_contrast ? 1 : -1;
                    video.setContrast(current_contrast);
                    contrast_clock += 1000000000L;
                }
            }
            mfp.riseVdisp();
        case 23:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 25;
            } else {
                stage = 24;
            }
            return;
        case 24:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (raster_number < r06) {
                stage = 23;
            } else if (frame_skip_counter == 0) {
                tmp_asc = asc;
                force_update_frame = tmp_asc != adc;
                data_y = 0;
                bitmap_y = 0;
                stage = 26;
            } else {
                stage = 32;
            }
            return;
        case 25:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (raster_number < r06) {
                stage = 23;
            } else if (frame_skip_counter == 0) {
                tmp_asc = asc;
                force_update_frame = tmp_asc != adc;
                data_y = 0;
                bitmap_y = 0;
                stage = 26;
            } else {
                stage = 32;
            }
            return;
        case 26:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 28;
            } else {
                stage = 27;
            }
            return;
        case 27:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (force_update_frame || sc[data_y] != dc[data_y]) {
                video.drawRaster(data_y, bitmap_y);
            }
            bitmap_y++;
            if (raster_number < r07) {
                stage = 29;
            } else {
                if (video.updateImage()) {
                    frame_skip_counter = frame_skip_initial;
                }
                if (force_update_frame) {
                    adc = tmp_asc;
                }
                stage = 38;
            }
            return;
        case 28:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (force_update_frame || sc[data_y] != dc[data_y]) {
                video.drawRaster(data_y, bitmap_y);
            }
            bitmap_y++;
            if (raster_number < r07) {
                stage = 29;
            } else {
                if (video.updateImage()) {
                    frame_skip_counter = frame_skip_initial;
                }
                if (force_update_frame) {
                    adc = tmp_asc;
                }
                stage = 38;
            }
            return;
        case 29:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 31;
            } else {
                stage = 30;
            }
            return;
        case 30:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            sc_tmp = sc[data_y];
            if (force_update_frame || sc_tmp != dc[data_y]) {
                video.duplicateRaster(bitmap_y);
                dc[data_y] = sc_tmp;
            }
            data_y++;
            bitmap_y++;
            if (raster_number < r07) {
                stage = 26;
            } else {
                if (video.updateImage()) {
                    frame_skip_counter = frame_skip_initial;
                }
                if (force_update_frame) {
                    adc = tmp_asc;
                }
                stage = 38;
            }
            return;
        case 31:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            sc_tmp = sc[data_y];
            if (force_update_frame || sc_tmp != dc[data_y]) {
                video.duplicateRaster(bitmap_y);
                dc[data_y] = sc_tmp;
            }
            data_y++;
            bitmap_y++;
            if (raster_number < r07) {
                stage = 26;
            } else {
                if (video.updateImage()) {
                    frame_skip_counter = frame_skip_initial;
                }
                if (force_update_frame) {
                    adc = tmp_asc;
                }
                stage = 38;
            }
            return;
        case 32:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 34;
            } else {
                stage = 33;
            }
            return;
        case 33:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (raster_number < r07) {
                stage = 35;
            } else {
                frame_skip_counter--;
                stage = 38;
            }
            return;
        case 34:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (raster_number < r07) {
                stage = 35;
            } else {
                frame_skip_counter--;
                stage = 38;
            }
            return;
        case 35:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 37;
            } else {
                stage = 36;
            }
            return;
        case 36:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (raster_number < r07) {
                stage = 32;
            } else {
                frame_skip_counter--;
                stage = 38;
            }
            return;
        case 37:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (raster_number < r07) {
                stage = 32;
            } else {
                frame_skip_counter--;
                stage = 38;
            }
            return;
        case 38:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 40;
            } else {
                stage = 39;
            }
            return;
        case 39:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            stage = raster_number < r04 ? 38 : 18;
            return;
        case 40:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            stage = raster_number < r04 ? 38 : 18;
            return;
        case 41:
            if (clear_in_execution) {
                clear_in_execution = false;
            }
            raster_number = 0;
            mfp.fallVdisp();
            mfp.riseRint();
        case 42:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 44;
            } else {
                stage = 43;
            }
            return;
        case 43:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            stage = raster_number < r05 ? 42 : 45;
            return;
        case 44:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            stage = raster_number < r05 ? 42 : 45;
            return;
        case 45:
            if (clear_standby) {
                clear_standby = false;
                clear_in_execution = true;
                graphic_screen.clear();
            }
            if (x68000.clock_count >= contrast_clock) {
                if (current_contrast == target_contrast) {
                    contrast_clock = 9223372036854775807L;
                } else {
                    current_contrast += current_contrast < target_contrast ? 1 : -1;
                    video.setContrast(current_contrast);
                    contrast_clock += 1000000000L;
                }
            }
            mfp.riseVdisp();
        case 46:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 48;
            } else {
                stage = 47;
            }
            return;
        case 47:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (raster_number < r06) {
                stage = 46;
            } else if (frame_skip_counter == 0) {
                tmp_asc = asc;
                force_update_frame = tmp_asc != adc;
                data_y = even_odd;
                bitmap_y = even_odd;
                stage = 49;
            } else {
                stage = 52;
            }
            return;
        case 48:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (raster_number < r06) {
                stage = 46;
            } else if (frame_skip_counter == 0) {
                tmp_asc = asc;
                force_update_frame = tmp_asc != adc;
                data_y = even_odd;
                bitmap_y = even_odd;
                stage = 49;
            } else {
                stage = 52;
            }
            return;
        case 49:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 51;
            } else {
                stage = 50;
            }
            return;
        case 50:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            sc_tmp = sc[data_y];
            if (force_update_frame || sc_tmp != dc[data_y]) {
                video.drawRaster(data_y, bitmap_y);
                dc[data_y] = sc_tmp;
            }
            data_y += 2;
            bitmap_y += 2;
            if (raster_number < r07) {
                stage = 49;
            } else {
                if (video.updateImage()) {
                    frame_skip_counter = frame_skip_initial & -2;
                }
                if (force_update_frame) {
                    adc = tmp_asc;
                }
                stage = 55;
            }
            return;
        case 51:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            sc_tmp = sc[data_y];
            if (force_update_frame || sc_tmp != dc[data_y]) {
                video.drawRaster(data_y, bitmap_y);
                dc[data_y] = sc_tmp;
            }
            data_y += 2;
            bitmap_y += 2;
            if (raster_number < r07) {
                stage = 49;
            } else {
                if (video.updateImage()) {
                    frame_skip_counter = frame_skip_initial & -2;
                }
                if (force_update_frame) {
                    adc = tmp_asc;
                }
                stage = 55;
            }
            return;
        case 52:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 54;
            } else {
                stage = 53;
            }
            return;
        case 53:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (raster_number < r07) {
                stage = 52;
            } else {
                frame_skip_counter--;
                stage = 55;
            }
            return;
        case 54:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (raster_number < r07) {
                stage = 52;
            } else {
                frame_skip_counter--;
                stage = 55;
            }
            return;
        case 55:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 57;
            } else {
                stage = 56;
            }
            return;
        case 56:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (raster_number < r04) {
                stage = 55;
            } else {
                even_odd = 1 - even_odd;
                stage = 41;
            }
            return;
        case 57:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (raster_number < r04) {
                stage = 55;
            } else {
                even_odd = 1 - even_odd;
                stage = 41;
            }
            return;
        case 58:
            if (clear_in_execution) {
                clear_in_execution = false;
            }
            raster_number = 0;
            mfp.fallVdisp();
            mfp.riseRint();
        case 59:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 61;
            } else {
                stage = 60;
            }
            return;
        case 60:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            stage = raster_number < r05 ? 59 : 62;
            return;
        case 61:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            stage = raster_number < r05 ? 59 : 62;
            return;
        case 62:
            if (clear_standby) {
                clear_standby = false;
                clear_in_execution = true;
                graphic_screen.clear();
            }
            if (x68000.clock_count >= contrast_clock) {
                if (current_contrast == target_contrast) {
                    contrast_clock = 9223372036854775807L;
                } else {
                    current_contrast += current_contrast < target_contrast ? 1 : -1;
                    video.setContrast(current_contrast);
                    contrast_clock += 1000000000L;
                }
            }
            mfp.riseVdisp();
        case 63:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 65;
            } else {
                stage = 64;
            }
            return;
        case 64:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (raster_number < r06) {
                stage = 63;
            } else if (frame_skip_counter == 0) {
                tmp_asc = asc;
                force_update_frame = tmp_asc != adc;
                data_y = 0;
                bitmap_y = 0;
                stage = 66;
            } else {
                stage = 69;
            }
            return;
        case 65:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (raster_number < r06) {
                stage = 63;
            } else if (frame_skip_counter == 0) {
                tmp_asc = asc;
                force_update_frame = tmp_asc != adc;
                data_y = 0;
                bitmap_y = 0;
                stage = 66;
            } else {
                stage = 69;
            }
            return;
        case 66:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 68;
            } else {
                stage = 67;
            }
            return;
        case 67:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            sc_tmp = sc[data_y];
            if (force_update_frame || sc_tmp != dc[data_y]) {
                video.drawRaster(data_y, bitmap_y);
                dc[data_y] = sc_tmp;
            }
            data_y++;
            bitmap_y += 2;
            if (raster_number < r07) {
                stage = 66;
            } else {
                if (video.updateImage()) {
                    frame_skip_counter = frame_skip_initial;
                }
                if (force_update_frame) {
                    adc = tmp_asc;
                }
                stage = 72;
            }
            return;
        case 68:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            sc_tmp = sc[data_y];
            if (force_update_frame || sc_tmp != dc[data_y]) {
                video.drawRaster(data_y, bitmap_y);
                dc[data_y] = sc_tmp;
            }
            data_y++;
            bitmap_y += 2;
            if (raster_number < r07) {
                stage = 66;
            } else {
                if (video.updateImage()) {
                    frame_skip_counter = frame_skip_initial;
                }
                if (force_update_frame) {
                    adc = tmp_asc;
                }
                stage = 72;
            }
            return;
        case 69:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 71;
            } else {
                stage = 70;
            }
            return;
        case 70:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            if (raster_number < r07) {
                stage = 69;
            } else {
                frame_skip_counter--;
                stage = 72;
            }
            return;
        case 71:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            if (raster_number < r07) {
                stage = 69;
            } else {
                frame_skip_counter--;
                stage = 72;
            }
            return;
        case 72:
            x68000.crtc_interrupt_clock += h_blank_length;
            mfp.fallHsync();
            if (raster_number == rint_number) {
                mfp.fallRint();
                stage = 74;
            } else {
                stage = 73;
            }
            return;
        case 73:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            stage = raster_number < r04 ? 72 : 58;
            return;
        case 74:
            x68000.crtc_interrupt_clock += h_disp_length;
            raster_number++;
            mfp.riseHsync();
            mfp.riseRint();
            stage = raster_number < r04 ? 72 : 58;
            return;
        }
    }
}

