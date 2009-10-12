/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.MediaTracker;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.Collections;


class Keyboard extends Canvas implements KeyListener, MouseListener, MouseMotionListener, Runnable {

    private static final boolean variableKeyMap[] = {
        false, false, true, true, true, true, true, true,
        true, true, true, true, true, true, true, false,
        false, true, true, true, true, true, true, true,
        true, true, true, true, true, false, true, true,
        true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true,
        true, true, true, true, true, false, false, false,
        false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false,
        false, false, false, false, false, false
    };

    private static final int ctrlMap[] = {
        -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 28, -1, -1, 17, 23, 5, 18, 20, 25, 21, 9, 15, 16, 0, 27, -1, 1, 19, 4, 6, 7, 8, 10, 11, 12, 0, 0, 29, 26, 24, 3, 22, 2, 14, 13, 0, 0, 0, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    private static final int normalMap[] = {
        -1, -1, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 45, 94, 92, -1, -1, 113, 119, 101, 114, 116, 121, 117, 105, 111, 112, 64, 91, -1, 97, 115, 100, 102, 103, 104, 106, 107, 108, 59, 58, 93, 122, 120, 99, 118, 98, 110, 109, 44, 46, 47, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    private static final int normalShiftMap[] = {
        -1, -1, 33, 34, 35, 36, 37, 38, 39, 40, 41, 0, 61, 126, 124, -1, -1, 81, 87, 69, 82, 84, 89, 85, 73, 79, 80, 96, 123, -1, 65, 83, 68, 70, 71, 72, 74, 75, 76, 43, 42, 125, 90, 88, 67, 86, 66, 78, 77, 60, 62, 63, 95, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    private static final int capsMap[] = {
        -1, -1, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 45, 94, 92, -1, -1, 81, 87, 69, 82, 84, 89, 85, 73, 79, 80, 64, 91, -1, 65, 83, 68, 70, 71, 72, 74, 75, 76, 59, 58, 93, 90, 88, 67, 86, 66, 78, 77, 44, 46, 47, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    private static final int capsShiftMap[] = {
        -1, -1, 33, 34, 35, 36, 37, 38, 39, 40, 41, 0, 61, 126, 124, -1, -1, 113, 119, 101, 114, 116, 121, 117, 105, 111, 112, 96, 123, -1, 97, 115, 100, 102, 103, 104, 106, 107, 108, 43, 42, 125, 122, 120, 99, 118, 98, 110, 109, 60, 62, 63, 95, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    private static final int romaMap[] = {
        -1, -1, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 45, 94, 92, -1, -1, 81, 87, 69, 82, 84, 89, 85, 73, 79, 80, 64, 91, -1, 65, 83, 68, 70, 71, 72, 74, 75, 76, 59, 58, 93, 90, 88, 67, 86, 66, 78, 77, 44, 46, 47, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    private static final int romaShiftMap[] = {
        -1, -1, 33, 34, 35, 36, 37, 38, 39, 40, 41, 0, 61, 126, 124, -1, -1, 113, 119, 101, 114, 116, 121, 117, 105, 111, 112, 96, 123, -1, 97, 115, 100, 102, 103, 104, 106, 107, 108, 43, 42, 125, 122, 120, 99, 118, 98, 110, 109, 60, 62, 63, 95, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    private static final int kataJisMap[] = {
        -1, -1, 199, 204, 177, 179, 180, 181, 212, 213, 214, 220, 206, 205, 176, -1, -1, 192, 195, 178, 189, 182, 221, 197, 198, 215, 190, 222, 223, -1, 193, 196, 188, 202, 183, 184, 207, 201, 216, 218, 185, 209, 194, 187, 191, 203, 186, 208, 211, 200, 217, 210, 219, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1
    };

    private static final int kataJisShiftMap[] = {
        -1, -1, 199, 204, 167, 169, 170, 171, 172, 173, 174, 166, 206, 205, 176, -1, -1, 192, 195, 168, 189, 182, 221, 197, 198, 215, 190, 222, 162, -1, 193, 196, 188, 202, 183, 184, 207, 201, 216, 218, 185, 163, 175, 187, 191, 203, 186, 208, 211, 164, 161, 165, 160, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1
    };

    private static final int kataAiuMap[] = {
        -1, -1, 177, 178, 179, 180, 181, 197, 198, 199, 200, 201, 215, 216, 217, -1, -1, 182, 183, 184, 185, 186, 202, 203, 204, 205, 206, 218, 219, -1, 187, 188, 189, 190, 191, 207, 208, 209, 210, 211, 222, 223, 192, 193, 194, 195, 196, 212, 213, 214, 220, 166, 221, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1
    };

    private static final int kataAiuShiftMap[] = {
        -1, -1, 167, 168, 169, 170, 171, 197, 198, 199, 200, 166, 215, 216, 217, -1, -1, 182, 183, 184, 185, 186, 202, 203, 204, 205, 206, 218, 162, -1, 160, 188, 189, 190, 191, 207, 208, 209, 210, 211, 176, 163, 192, 193, 175, 195, 196, 172, 173, 174, 164, 161, 165, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1
    };

    private static final int hiraJisMap[] = {
        -1, -1, 231, 236, 145, 147, 148, 149, 244, 245, 246, 252, 238, 237, 176, -1, -1, 224, 227, 146, 157, 150, 253, 229, 230, 247, 158, 222, 223, -1, 225, 228, 156, 234, 151, 152, 239, 233, 248, 250, 153, 241, 226, 155, 159, 235, 154, 240, 243, 232, 249, 242, 251, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1
    };

    private static final int hiraJisShiftMap[] = {
        -1, -1, 231, 236, 135, 137, 138, 139, 140, 141, 142, 134, 238, 237, 176, -1, -1, 224, 227, 136, 157, 150, 253, 229, 230, 247, 158, 222, 162, -1, 225, 228, 156, 234, 151, 152, 239, 233, 248, 250, 153, 163, 143, 155, 159, 235, 154, 240, 243, 164, 161, 165, 160, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1
    };

    private static final int hiraAiuMap[] = {
        -1, -1, 145, 146, 147, 148, 149, 229, 230, 231, 232, 233, 247, 248, 249, -1, -1, 150, 151, 152, 153, 154, 234, 235, 236, 237, 238, 250, 251, -1, 155, 156, 157, 158, 159, 239, 240, 241, 242, 243, 222, 223, 224, 225, 226, 227, 228, 244, 245, 246, 252, 134, 253, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1
    };

    private static final int hiraAiuShiftMap[] = {
        -1, -1, 135, 136, 137, 138, 139, 229, 230, 231, 232, 134, 247, 248, 249, -1, -1, 150, 151, 152, 153, 154, 234, 235, 236, 237, 238, 250, 162, -1, 160, 156, 157, 158, 159, 239, 240, 241, 242, 243, 176, 163, 224, 225, 143, 227, 228, 140, 141, 142, 164, 161, 165, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1
    };

    private Graphics graphics;

    private int bm[];

    int keyLeft[];

    int keyRight[];

    int keyTop[];

    int keyBottom[];

    int keyWidth[];

    int keyHeight[];

    private int pointMap[];

    private int mousePointMap[];

    private boolean availableKey[];

    private int availableKeysStart;

    private int availableKeysEnd;

    private int vp[];

    private MemoryImageSource vm;

    private Image vi;

    private int kanaMode;

    private int xchgMode;

    private int mousePointedKey;

    private boolean transientShift;

    private boolean transientCtrl;

    private boolean pressedKey[];

    private boolean lockedKey[];

    private boolean lightedKey[];

    private int drawVariableKeysRequest;

    private int drawVariableKeysAcknowledged;

    private int drawKeyRequest[];

    private int drawKeyAcknowledged[];

    private KeyboardInputListener keyboardInputListener = null;

    private int lastPressedKey;

    private int repeatingKey;

    private int delayTime;

    private int repeatInterval;

    private int timeout;

    public Keyboard(Image im) {
        super();
        setBackground(Color.black);
        graphics = null;
        bm = new int[915936];
        if (im != null) {
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(im, 0);
            while (!mt.checkID(0, true)) {
            }
            if (mt.isErrorID(0)) {
                return;
            }
            if (im.getWidth(this) != 564 || im.getHeight(this) != 1624) {
                return;
            }
            PixelGrabber pg = new PixelGrabber(im, 0, 0, 564, 1624, bm, 0, 564);
            pg.startGrabbing();
            while ((pg.getStatus() & ImageObserver.ALLBITS) == 0) {
                if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                    return;
                }
            }
            for (int i = 0; i < 915936; i++) {
                bm[i] |= -16777216;
            }
        }
        keyLeft = new int[117];
        keyRight = new int[117];
        keyTop = new int[117];
        keyBottom = new int[117];
        keyWidth = new int[117];
        keyHeight = new int[117];
        for (int k = 0; k < 117; k++) {
            keyLeft[k] = 564;
            keyRight[k] = 0;
            keyTop[k] = 100;
            keyBottom[k] = 0;
            keyWidth[k] = 0;
            keyHeight[k] = 0;
        }
        pointMap = new int[56400];
        mousePointMap = new int[56400];
        for (int y = 0; y < 100; y++) {
            for (int x = 0; x < 564; x++) {
                int i = x + 564 * y;
                int c = bm[i];
                int r = c >> 16 & 255;
                int g = c >> 8 & 255;
                int b = c & 255;
                int k = 25 * (r == 0 ? 0 : r / 51 - 1) + 5 * (g == 0 ? 0 : g / 51 - 1) + (b == 0 ? 0 : b / 51 - 1);
                if (k == 0) {
                    k = -1;
                }
                pointMap[i] = k;
                mousePointMap[i] = bm[56400 + i] != c ? -1 : k;
                if (k < 0) {
                    continue;
                }
                if (x < keyLeft[k]) {
                    keyLeft[k] = x;
                }
                if (x > keyRight[k]) {
                    keyRight[k] = x;
                }
                if (y < keyTop[k]) {
                    keyTop[k] = y;
                }
                if (y > keyBottom[k]) {
                    keyBottom[k] = y;
                }
            }
        }
        availableKey = new boolean[117];
        availableKeysStart = 116;
        availableKeysEnd = 0;
        for (int k = 0; k < 117; k++) {
            if (availableKey[k] = keyLeft[k] < keyRight[k]) {
                keyWidth[k] = keyRight[k] - keyLeft[k] + 1;
                keyHeight[k] = keyBottom[k] - keyTop[k] + 1;
                if (k < availableKeysStart) {
                    availableKeysStart = k;
                } else if (k > availableKeysEnd) {
                    availableKeysEnd = k;
                }
            }
        }
        vp = new int[56400];
        for (int i = 0; i < 56400; i++) {
            vp[i] = -16777216;
        }
        vm = new MemoryImageSource(564, 100, vp, 0, 564);
        vm.setAnimated(true);
        vm.setFullBufferUpdates(false);
        vi = createImage(vm);
        kanaMode = 0;
        xchgMode = 0;
        mousePointedKey = -1;
        transientShift = false;
        transientShift = false;
        pressedKey = new boolean[117];
        lockedKey = new boolean[117];
        for (int k = availableKeysStart; k <= availableKeysEnd; k++) {
            pressedKey[k] = false;
            lockedKey[k] = false;
        }
        lightedKey = new boolean[117];
        for (int l = 0; l <= 6; l++) {
            lightedKey[l] = false;
        }
        drawVariableKeysRequest = 0;
        drawVariableKeysAcknowledged = 0;
        drawKeyRequest = new int[117];
        drawKeyAcknowledged = new int[117];
        for (int k = availableKeysStart; k <= availableKeysEnd; k++) {
            drawKeyRequest[k] = 1;
            drawKeyAcknowledged[k] = 0;
        }
        draw();
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        lastPressedKey = -1;
        repeatingKey = -1;
        delayTime = 200;
        repeatInterval = 30;
        timeout = 0;
        start();
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setKeyboardInputListener(KeyboardInputListener keyboardInputListener) {
        this.keyboardInputListener = keyboardInputListener;
    }

    private static int keyboardMap106[][] = {
        {
            1, KeyEvent.VK_ESCAPE
        }, {
            2, KeyEvent.VK_1
        }, {
            3, KeyEvent.VK_2
        }, {
            4, KeyEvent.VK_3
        }, {
            5, KeyEvent.VK_4
        }, {
            6, KeyEvent.VK_5
        }, {
            7, KeyEvent.VK_6
        }, {
            8, KeyEvent.VK_7
        }, {
            9, KeyEvent.VK_8
        }, {
            10, KeyEvent.VK_9
        }, {
            11, KeyEvent.VK_0
        }, {
            12, KeyEvent.VK_MINUS
        }, {
            13, KeyEvent.VK_CIRCUMFLEX
        }, {
            14, KeyEvent.VK_BACK_SLASH, -1, 0, 92
        }, {
            14, KeyEvent.VK_BACK_SLASH, -1, InputEvent.SHIFT_DOWN_MASK, 124
        }, {
            14, KeyEvent.VK_BACK_SLASH, -1, InputEvent.CTRL_DOWN_MASK, 28
        }, {
            14, KeyEvent.VK_BACK_SLASH, -1, InputEvent.SHIFT_DOWN_MASK + InputEvent.CTRL_DOWN_MASK, 65535
        }, {
            15, KeyEvent.VK_BACK_SPACE
        }, {
            16, KeyEvent.VK_TAB
        }, {
            17, KeyEvent.VK_Q
        }, {
            18, KeyEvent.VK_W
        }, {
            19, KeyEvent.VK_E
        }, {
            20, KeyEvent.VK_R
        }, {
            21, KeyEvent.VK_T
        }, {
            22, KeyEvent.VK_Y
        }, {
            23, KeyEvent.VK_U
        }, {
            24, KeyEvent.VK_I
        }, {
            25, KeyEvent.VK_O
        }, {
            26, KeyEvent.VK_P
        }, {
            27, KeyEvent.VK_AT
        }, {
            28, KeyEvent.VK_OPEN_BRACKET
        }, {
            29, KeyEvent.VK_ENTER, KeyEvent.KEY_LOCATION_STANDARD
        }, {
            30, KeyEvent.VK_A
        }, {
            31, KeyEvent.VK_S
        }, {
            32, KeyEvent.VK_D
        }, {
            33, KeyEvent.VK_F
        }, {
            34, KeyEvent.VK_G
        }, {
            35, KeyEvent.VK_H
        }, {
            36, KeyEvent.VK_J
        }, {
            37, KeyEvent.VK_K
        }, {
            38, KeyEvent.VK_L
        }, {
            39, KeyEvent.VK_SEMICOLON
        }, {
            40, KeyEvent.VK_COLON
        }, {
            41, KeyEvent.VK_CLOSE_BRACKET
        }, {
            42, KeyEvent.VK_Z
        }, {
            43, KeyEvent.VK_X
        }, {
            44, KeyEvent.VK_C
        }, {
            45, KeyEvent.VK_V
        }, {
            46, KeyEvent.VK_B
        }, {
            47, KeyEvent.VK_N
        }, {
            48, KeyEvent.VK_M
        }, {
            49, KeyEvent.VK_COMMA
        }, {
            50, KeyEvent.VK_PERIOD
        }, {
            51, KeyEvent.VK_SLASH
        }, {
            52, KeyEvent.VK_BACK_SLASH, -1, InputEvent.SHIFT_DOWN_MASK, 95
        }, {
            52, KeyEvent.VK_BACK_SLASH, -1, InputEvent.CTRL_DOWN_MASK, 92
        }, {
            52, KeyEvent.VK_BACK_SLASH, -1, InputEvent.SHIFT_DOWN_MASK + InputEvent.CTRL_DOWN_MASK, 95
        }, {
            53, KeyEvent.VK_SPACE
        }, {
            54, KeyEvent.VK_HOME
        }, {
            55, KeyEvent.VK_DELETE
        }, {
            56, KeyEvent.VK_PAGE_UP
        }, {
            57, KeyEvent.VK_PAGE_DOWN
        }, {
            58, KeyEvent.VK_END
        }, {
            59, KeyEvent.VK_LEFT
        }, {
            60, KeyEvent.VK_UP
        }, {
            61, KeyEvent.VK_RIGHT
        }, {
            62, KeyEvent.VK_DOWN
        }, {
            63
        }, {
            64, KeyEvent.VK_DIVIDE
        }, {
            65, KeyEvent.VK_MULTIPLY
        }, {
            66, KeyEvent.VK_SUBTRACT
        }, {
            67, KeyEvent.VK_NUMPAD7
        }, {
            68, KeyEvent.VK_NUMPAD8
        }, {
            69, KeyEvent.VK_NUMPAD9
        }, {
            70, KeyEvent.VK_ADD
        }, {
            71, KeyEvent.VK_NUMPAD4
        }, {
            72, KeyEvent.VK_NUMPAD5
        }, {
            73, KeyEvent.VK_NUMPAD6
        }, {
            74
        }, {
            75, KeyEvent.VK_NUMPAD1
        }, {
            76, KeyEvent.VK_NUMPAD2
        }, {
            77, KeyEvent.VK_NUMPAD3
        }, {
            78, KeyEvent.VK_ENTER, KeyEvent.KEY_LOCATION_NUMPAD
        }, {
            79, KeyEvent.VK_NUMPAD0
        }, {
            80, KeyEvent.VK_SEPARATOR
        }, {
            81, KeyEvent.VK_DECIMAL
        }, {
            82
        }, {
            83
        }, {
            84, KeyEvent.VK_HELP
        }, {
            85, KeyEvent.VK_ALT, KeyEvent.KEY_LOCATION_LEFT
        }, {
            86
        }, {
            87, KeyEvent.VK_ALT, KeyEvent.KEY_LOCATION_RIGHT
        }, {
            88
        }, {
            89, KeyEvent.VK_CONTROL, KeyEvent.KEY_LOCATION_RIGHT
        }, {
            90
        }, {
            91
        }, {
            92
        }, {
            93
        }, {
            94, KeyEvent.VK_INSERT
        }, {
            95
        }, {
            96
        }, {
            97
        }, {
            98
        }, {
            99, KeyEvent.VK_F1
        }, {
            100, KeyEvent.VK_F2
        }, {
            101, KeyEvent.VK_F3
        }, {
            102, KeyEvent.VK_F4
        }, {
            103, KeyEvent.VK_F5
        }, {
            104, KeyEvent.VK_F6
        }, {
            105, KeyEvent.VK_F7
        }, {
            106, KeyEvent.VK_F8
        }, {
            107, KeyEvent.VK_F9
        }, {
            108, KeyEvent.VK_F10
        }, {
            112, KeyEvent.VK_SHIFT
        }, {
            113, KeyEvent.VK_CONTROL, KeyEvent.KEY_LOCATION_LEFT
        }, {
            114
        }, {
            115
        }
    };

    private static final int keyboardMapRules[][];
    private static final int keyboardMapTable[];

    static {
        int keyboardMap106TableSize = 0;
        for (int i = 0; i < keyboardMap106.length; i++) {
            int rule[] = keyboardMap106[i];
            if (rule.length >= 2 && rule[1] >= keyboardMap106TableSize) {
                keyboardMap106TableSize = rule[1] + 1;
            }
        }
        int keyboardMap106Table[] = new int[keyboardMap106TableSize];
        for (int i = 0; i < keyboardMap106TableSize; i++) {
            keyboardMap106Table[i] = -1;
        }
        for (int i = 0; i < keyboardMap106.length; i++) {
            int rule[] = keyboardMap106[i];
            if (rule.length >= 2) {
                int keyCode = rule[1];
                rule[1] = keyboardMap106Table[keyCode];
                keyboardMap106Table[keyCode] = i;
            }
        }
        keyboardMapRules = keyboardMap106;
        keyboardMapTable = keyboardMap106Table;
    }

    public void keyPressed(KeyEvent e) {
        key(e, true);
        e.consume();
    }

    public void keyReleased(KeyEvent e) {
        key(e, false);
        e.consume();
    }

    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    private void key(KeyEvent e, boolean pressed) {
        char keyChar = e.getKeyChar();
        int keyCode = e.getKeyCode();
        int keyLocation = e.getKeyLocation();
        int modifiersEx = e.getModifiersEx();
        int k = -1;
        if (keyCode >= 0 && keyCode < keyboardMapTable.length) {
            int rule[];
            for (int i = keyboardMapTable[keyCode]; i >= 0; i = rule[1]) {
                rule = keyboardMapRules[i];
                if (rule.length >= 3 && rule[2] >= 0) {
                    if (rule[2] != keyLocation) {
                        continue;
                    }
                }
                if (rule.length >= 4 && rule[3] >= 0) {
                    if (rule[3] != (modifiersEx & 192)) {
                        continue;
                    }
                }
                if (rule.length >= 5 && rule[4] >= 0) {
                    if (rule[4] != keyChar) {
                        continue;
                    }
                }
                k = rule[0];
                break;
            }
        }
        if (k < availableKeysStart || k > availableKeysEnd || !availableKey[k]) {
            return;
        }
        if (pressed) {
            keyDown(k, -1);
        } else {
            keyUp(k, -1);
        }
    }

    public void mouseClicked(MouseEvent e) {
        e.consume();
    }

    public void mouseEntered(MouseEvent e) {
        e.consume();
    }

    public void mouseExited(MouseEvent e) {
        if (mousePointedKey >= 0) {
            int k = mousePointedKey;
            mousePointedKey = -1;
            if (pressedKey[k] && !lockedKey[k]) {
                pressedKey[k] = false;
            }
            drawKeyRequest[k]++;
            draw();
            if (graphics != null) {
                graphics.drawImage(vi, 2, 2, null);
            }
        }
        e.consume();
    }

    public void mousePressed(MouseEvent e) {
        mouseButton(e, true);
        e.consume();
    }

    public void mouseReleased(MouseEvent e) {
        mouseButton(e, false);
        e.consume();
    }

    public void mouseDragged(MouseEvent e) {
        mouseMove(e);
        e.consume();
    }

    public void mouseMoved(MouseEvent e) {
        mouseMove(e);
        e.consume();
    }

    public void mouseButton(MouseEvent e, boolean pressed) {
        if (!isFocusOwner()) {
            requestFocusInWindow();
        }
        int x = e.getX() - 2;
        int y = e.getY() - 2;
        int button = e.getButton();
        if (x < 0 || x >= 564 || y < 0 || y >= 100) {
            return;
        }
        int k = mousePointMap[x + 564 * y];
        if (k < availableKeysStart || k > availableKeysEnd || !availableKey[k]) {
            return;
        }
        if (pressed) {
            keyDown(k, button);
        } else {
            keyUp(k, button);
        }
    }

    public void mouseMove(MouseEvent e) {
        int x = e.getX() - 2;
        int y = e.getY() - 2;
        if (x < 0 || x >= 564 || y < 0 || y >= 100) {
            return;
        }
        int k = mousePointMap[x + 564 * y];
        if (k == mousePointedKey) {
            return;
        }
        if (mousePointedKey >= 0) {
            if (pressedKey[mousePointedKey] && !lockedKey[mousePointedKey]) {
                pressedKey[mousePointedKey] = false;
            }
            drawKeyRequest[mousePointedKey]++;
            if (mousePointedKey == 112 || mousePointedKey == 113) {
                drawVariableKeysRequest++;
            }
        }
        mousePointedKey = k;
        if (mousePointedKey >= 0) {
            drawKeyRequest[mousePointedKey]++;
            if (mousePointedKey == 112 || mousePointedKey == 113) {
                drawVariableKeysRequest++;
            }
        }
        draw();
        if (graphics != null) {
            graphics.drawImage(vi, 2, 2, null);
        }
    }

    private synchronized void keyDown(int k, int button) {
        if (lockedKey[k]) {
            lastPressedKey = -1;
            repeatingKey = -1;
            lockedKey[k] = false;
            pressedKey[k] = false;
            if (keyboardInputListener != null) {
                keyboardInputListener.keyboardInput(128 + k);
            }
            if (k == 112 && transientShift) {
                transientShift = false;
                drawVariableKeysRequest++;
            } else if (k == 113 && transientCtrl) {
                transientCtrl = false;
                drawVariableKeysRequest++;
            }
            drawKeyRequest[k]++;
            draw();
            if (graphics != null) {
                graphics.drawImage(vi, 2, 2, null);
            }
            return;
        }
        if (pressedKey[k]) {
            return;
        }
        if (button == MouseEvent.BUTTON3) {
            lockedKey[k] = true;
        }
        pressedKey[k] = true;
        lastPressedKey = k;
        notify();
        if (keyboardInputListener != null) {
            keyboardInputListener.keyboardInput(k);
        }
        if (k == 112 || k == 113) {
            drawVariableKeysRequest++;
            if (button == MouseEvent.BUTTON1) {
                if (k == 112) {
                    transientShift = true;
                } else {
                    transientCtrl = true;
                }
                lockedKey[k] = true;
            }
        }
        drawKeyRequest[k]++;
        draw();
        if (graphics != null) {
            graphics.drawImage(vi, 2, 2, null);
        }
    }

    private void keyUp(int k, int button) {
        if (lockedKey[k]) {
            return;
        }
        if (!pressedKey[k]) {
            return;
        }
        lastPressedKey = -1;
        repeatingKey = -1;
        pressedKey[k] = false;
        if (keyboardInputListener != null) {
            keyboardInputListener.keyboardInput(128 + k);
        }
        if (k == 112) {
            drawVariableKeysRequest++;
        } else if (transientShift) {
            transientShift = false;
            if (lockedKey[112]) {
                lockedKey[112] = false;
                pressedKey[112] = false;
                drawKeyRequest[112]++;
            }
            if (keyboardInputListener != null) {
                keyboardInputListener.keyboardInput(240);
            }
            drawVariableKeysRequest++;
        }
        if (k == 113) {
            drawVariableKeysRequest++;
        } else if (transientCtrl) {
            transientCtrl = false;
            if (lockedKey[113]) {
                lockedKey[113] = false;
                pressedKey[113] = false;
                drawKeyRequest[113]++;
            }
            if (keyboardInputListener != null) {
                keyboardInputListener.keyboardInput(241);
            }
            drawVariableKeysRequest++;
        }
        drawKeyRequest[k]++;
        draw();
        if (graphics != null) {
            graphics.drawImage(vi, 2, 2, null);
        }
    }

    Thread thread, currentthread;

    public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    public synchronized void stop() {
        thread = null;
        notify();
    }

    public void run() {
        currentthread = Thread.currentThread();
        while (thread == currentthread) {
            try {
                synchronized (this) {
                    wait(timeout);
                }
            } catch (InterruptedException e) {
            }
            int k = lastPressedKey;
            if (k > 0 && pressedKey[k]) {
                if (k != repeatingKey) {
                    repeatingKey = k;
                    timeout = delayTime;
                } else {
                    if (keyboardInputListener != null) {
                        keyboardInputListener.keyboardInput(k);
                    }
                    timeout = repeatInterval;
                }
            } else {
                timeout = 0;
            }
        }
    }

    public void setLedStatus(int ledStatus) {
        for (int l = 0; l <= 6; l++) {
            if ((ledStatus & 1 << l) != 0) {
                if (lightedKey[l]) {
                    lightedKey[l] = false;
                    drawKeyRequest[90 + l]++;
                }
            } else {
                if (!lightedKey[l]) {
                    lightedKey[l] = true;
                    drawKeyRequest[90 + l]++;
                }
            }
        }
        drawVariableKeysRequest++;
        draw();
        if (graphics != null) {
            graphics.drawImage(vi, 2, 2, null);
        }
    }

    public void setDelayTime(int n) {
        n &= 15;
        delayTime = 200 + n * 100;
    }

    public void setRepeatInterval(int n) {
        n &= 15;
        repeatInterval = 30 + n * n * 5;
    }

    public void setKana(int mode) {
        kanaMode = mode & 1;
        drawVariableKeysRequest++;
        draw();
        if (graphics != null) {
            graphics.drawImage(vi, 2, 2, null);
        }
    }

    public void setXchg(int mode) {
        xchgMode = mode & 7;
        drawVariableKeysRequest++;
        draw();
        if (graphics != null) {
            graphics.drawImage(vi, 2, 2, null);
        }
    }

    public void draw() {
        int variableKeysCounter = drawVariableKeysRequest;
        boolean variableKeysFlag = variableKeysCounter != drawVariableKeysAcknowledged;
        int map[];
        if (transientCtrl || pressedKey[113] || mousePointedKey == 113) {
            map = ctrlMap;
        } else if (transientShift || pressedKey[112] || mousePointedKey == 112) {
            if (lightedKey[0]) {
                if (lightedKey[5]) {
                    map = kanaMode != 0 ? hiraAiuShiftMap : hiraJisShiftMap;
                } else {
                    map = kanaMode != 0 ? kataAiuShiftMap : kataJisShiftMap;
                }
            } else if (lightedKey[1]) {
                map = romaShiftMap;
            } else if (lightedKey[3]) {
                map = capsShiftMap;
            } else {
                map = normalShiftMap;
            }
        } else {
            if (lightedKey[0]) {
                if (lightedKey[5]) {
                    map = kanaMode != 0 ? hiraAiuMap : hiraJisMap;
                } else {
                    map = kanaMode != 0 ? kataAiuMap : kataJisMap;
                }
            } else if (lightedKey[1]) {
                map = romaMap;
            } else if (lightedKey[3]) {
                map = capsMap;
            } else {
                map = normalMap;
            }
        }
        for (int k = availableKeysStart; k <= availableKeysEnd; k++) {
            if (!availableKey[k]) {
                continue;
            }
            int dx = keyLeft[k];
            int dy = keyTop[k];
            int sx, sy;
            if (variableKeyMap[k]) {
                int counter = drawKeyRequest[k];
                if (counter == drawKeyAcknowledged[k] && !variableKeysFlag) {
                    continue;
                }
                drawKeyAcknowledged[k] = counter;
                int c = map[k];
                if (c == '\\' && (xchgMode & 1) != 0) {
                    c = 128;
                }
                if (c == '~' && (xchgMode & 2) != 0) {
                    c = 129;
                }
                if (c == '|' && (xchgMode & 4) != 0) {
                    c = 130;
                }
                if (c == 28 && (xchgMode & 1) != 0) {
                    c = 131;
                }
                sx = 24 * (c % 16);
                sy = 300 + 16 * (c / 16);
            } else {
                int counter = drawKeyRequest[k];
                if (counter == drawKeyAcknowledged[k]) {
                    continue;
                }
                drawKeyAcknowledged[k] = counter;
                sx = dx;
                sy = 200 + dy;
                if (k >= 90 && k <= 96) {
                    if (lightedKey[k - 90]) {
                        sx = 24 * (-74 + k);
                        sy = 300;
                    }
                }
            }
            if (k == mousePointedKey) {
                sy += 356;
            }
            if (pressedKey[k]) {
                sy += 712;
            }
            int di = dx + 564 * dy;
            int si = sx + 564 * sy;
            for (int y = 0; y < keyHeight[k]; y++) {
                for (int x = 0; x < keyWidth[k]; x++) {
                    int i = x + 564 * y;
                    if (pointMap[di + i] == k) {
                        vp[di + i] = bm[si + i];
                    }
                }
            }
        }
        drawVariableKeysAcknowledged = variableKeysCounter;
        vm.newPixels(0, 0, 564, 100);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        g.setColor(Color.white);
        g.draw3DRect(0, 0, 567, 103, false);
        g.drawImage(vi, 2, 2, this);
        graphics = g;
    }
}

