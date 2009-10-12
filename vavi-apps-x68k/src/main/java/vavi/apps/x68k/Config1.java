/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


class Config1 extends Panel {
    int mpuClockData[] = {
        5000, 2500, 1667, 1250, 1000, 833, 714, 625, 556, 500, 455, 417
    };

    String mpuClockName[] = {
        "2MHz", "4MHz", "6MHz", "8MHz", "10MHz", "12MHz", "14MHz", "16MHz", "18MHz", "20MHz", "22MHz", "24MHz"
    };

    Choice choiceMpuClock;

    int frameSkipData[] = {
        9, 8, 7, 6, 5, 4, 3, 2, 1, 0
    };

    String frameSkipName[] = {
        "1 / 10", "1 / 9", "1 / 8", "1 / 7", "1 / 6", "1 / 5", "1 / 4", "1 / 3", "1 / 2", "Full"
    };

    Choice choiceFrameSkip;

    public Config1() {
        setForeground(Color.white);
        setBackground(Color.black);
        setLayout(new GridLayout(2, 2));
        setFont(new Font("SansSerif", Font.PLAIN, 16));
        add(new Label("MPU clock:"));
        choiceMpuClock = new Choice();
        choiceMpuClock.setForeground(Color.white);
        choiceMpuClock.setBackground(Color.black);
        for (int i = 0; i < 12; i++) {
            choiceMpuClock.addItem(mpuClockName[i]);
        }
        choiceMpuClock.select(9);
        choiceMpuClock.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int index = choiceMpuClock.getSelectedIndex();
                int clockUnit = mpuClockData[index];
                x68000.clock_unit = clockUnit;
                x68000.monitor.outputString("clock unit changed to ");
                x68000.monitor.outputDec(clockUnit);
                x68000.monitor.outputString(" (" + mpuClockName[index] + ")\n");
                x68000.requestFocusInWindow();
            }
        });
        add(choiceMpuClock);
        add(new Label("Frame skip:"));
        choiceFrameSkip = new Choice();
        choiceFrameSkip.setForeground(Color.white);
        choiceFrameSkip.setBackground(Color.black);
        for (int i = 0; i < 10; i++) {
            choiceFrameSkip.addItem(frameSkipName[i]);
        }
        choiceFrameSkip.select(7);
        choiceFrameSkip.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int index = choiceFrameSkip.getSelectedIndex();
                int frameSkipInitial = frameSkipData[index];
                x68000.crtc.frame_skip_initial = frameSkipInitial;
                x68000.monitor.outputString("frame skip changed to ");
                x68000.monitor.outputDec(frameSkipInitial);
                x68000.monitor.outputString(" (" + frameSkipName[index] + ")\n");
                x68000.requestFocusInWindow();
            }
        });
        add(choiceFrameSkip);
    }

    private X68000 x68000;

    public void init(X68000 x68000) {
        this.x68000 = x68000;
    }
}

