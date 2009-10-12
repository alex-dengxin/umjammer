/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;

import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.TextArea;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;


class Monitor extends TextArea implements KeyListener {
    private MonitorInputListener monitorInputListener = null;

    public Monitor() {
        super("X68000 Emulator in Java version 54VII\nCopyright (C) 2003,2004 by M.Kamada\n\n", 8, 90, TextArea.SCROLLBARS_VERTICAL_ONLY);
        setEditable(true);
        setForeground(Color.white);
        setBackground(Color.black);
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        addKeyListener(this);
    }

    public void setMonitorInputListener(MonitorInputListener monitorInputListener) {
        this.monitorInputListener = monitorInputListener;
    }

    public void keyPressed(KeyEvent e) {
        if (monitorInputListener != null) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE:
                monitorInputListener.monitorInput((byte) 8);
                break;
            case KeyEvent.VK_TAB:
                monitorInputListener.monitorInput((byte) 9);
                break;
            case KeyEvent.VK_ENTER:
                monitorInputListener.monitorInput((byte) 13);
                break;
            }
        }
        e.consume();
    }

    public void keyReleased(KeyEvent e) {
        e.consume();
    }

    public void keyTyped(KeyEvent e) {
        if (monitorInputListener != null) {
            int keyChar = e.getKeyChar();
            if (keyChar >= 32 && keyChar <= 126) {
                monitorInputListener.monitorInput(keyChar);
            }
        }
        e.consume();
    }

    private boolean silent = false;

    private char temp[] = {
        0
    };

    public void toggleSilent() {
        silent = !silent;
    }

    public void outputString(String string) {
        int i, length;
        length = string.length();
        for (i = 0; i < length; i++) {
            outputChar((int) string.charAt(i));
        }
    }

    public void outputDec(int number) {
        if (number < 0) {
            outputChar('-');
            number = -number;
        }
        int base;
        for (base = 1000000000; base > number && base >= 10; base /= 10) {
        }
        for (; base >= 1; base /= 10) {
            outputChar(48 + number / base);
            number %= base;
        }
    }

    public void outputHex(int number, int digits) {
        for (digits--; digits >= 0; digits--) {
            int h = number >> (digits << 2) & 15;
            outputChar(h < 10 ? 48 + h : 87 + h);
        }
    }

    public void outputLF() {
        outputChar(10);
    }

    public void outputChar(int c) {
        if (silent) {
            return;
        }
        if (c == 8) {
            String string = getText();
            int length = string.length();
            if (length > 1) {
                setText(string.substring(0, length - 1));
                select(length - 1, length - 1);
            }
            return;
        }
        if (c == 9 || c == 10 || c >= 32) {
            temp[0] = (char) c;
            append(new String(temp));
        }
    }

    private int wideCharacterBuffer = 0;

    public void outputCharShiftJIS(int c) {
        c &= 255;
        if (wideCharacterBuffer != 0) {
            c = ShiftJIS.convertShiftJIStoUnicode((wideCharacterBuffer << 8) + c);
            wideCharacterBuffer = 0;
        } else if (ShiftJIS.isShiftJIS_2_1(c)) {
            wideCharacterBuffer = c;
            return;
        }
        outputChar(c);
    }
}

