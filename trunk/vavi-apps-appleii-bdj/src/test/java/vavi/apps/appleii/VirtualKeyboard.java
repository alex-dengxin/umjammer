/*
 * $LastChangedDate: 2005-11-21 02:11:20 +0900 (ì›? 21 11 2005) $  
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package vavi.apps.appleii;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;


/**
 * This is a popup layer that handles a sub-popup within the text tfContext
 * 
 * @author Amir Uval
 */

class VirtualKeyboard {

    /** indicates whether the virtual keyboard is enabled */
    public static boolean USE_VIRTUAL_KEYBOARD = true;

    /** indicates whether the virtual keypad is enabled */
    public static final boolean USE_VIRTUAL_KEYPAD = false;

    /** indicates whether the virtual keyboard is opened automatically */
    public static boolean USE_VIRTUAL_KEYBOARD_OPEN_AUTO = false;

    /** instance of the virtual keyboard listener */
    VirtualKeyboardListener vkl;

    // keyboard dimensions
    int kbX;
    int kbY;
    int kbWidth;
    int kbHeight;
    int fontW; // width of 'M'
    int fontH; // height of 'M'
    int buttonW; // width of keyboard
    int buttonH; // height of keyboard
    int fontWCenter; // placement of text inside button
    int fontHTop; // placement of text inside button
    int maxRows; // horizontal keyboard rows
    int maxColumns; // vertical keyboard columns
    int fullColumns; // number of columns that are completely full with keys
    int currentChar = 0;
    int currentKeyboard = 1; // abc
    int textfieldHeight = 0; // height of text field area, including adornments
    int candidateFieldHeight = 0; // height of candidate input field
    char itemIndexWhenPressed;

    char PRESS_OUT_OF_BOUNDS = 0;

    /** array of all available keys n the keyboard */
    char[][] keys;

    boolean inMetaKeys = false; // traversal mode
    boolean inShift = false;
    int currentMeta = 0;
    Image[] metaKeys = null;
    boolean textKbd = false;
    Font f;

    static final int PRESSED = 0;
    static final int RELEASED = 1;

    /**
     * Virtual Keyboard constructor.
     * 
     * @param keys array of available keys for the keyboard
     * @param vkl the virtual keyboard listener
     * @param displayTextArea flag to indicate whether to display the text area
     */
    public VirtualKeyboard(char[][] keys,
                           VirtualKeyboardListener vkl,
                           boolean displayTextArea,
                           int neededColumns,
                           int neededRows) throws VirtualKeyboardException {

        textKbd = displayTextArea;
        if (textKbd) {
            PADDING = 1;
        } else {
            PADDING = 2;
        }

        currentKeyboard = 0;
        this.vkl = vkl;

        kbX = PADDING;
        kbY = PADDING;

        kbWidth = vkl.getAvailableWidth() - 2 * PADDING;
        kbHeight = vkl.getAvailableHeight() - 2 * PADDING;

        // f = Font.getFont(Font.MONOSPACED, // or SYSTEM
        // Font.PLAIN,
        // 36);
        fontW = 36;// f.charWidth('M');
        fontH = 36;// f.getHeight();

        if (textKbd) {
            textfieldHeight = fontH + 8 * PADDING;
            buttonW = fontW + 8;
            buttonH = fontH + 8;
            fontHTop = (buttonH - fontH) / 2;
            fontWCenter = buttonW / 2;
        } else {
            buttonW = fontW * 3;
            buttonH = fontH * 3;
            fontHTop = (buttonH - fontH) / 2;
            fontWCenter = buttonW / 2;
        }
        candidateFieldHeight = 0;

        maxRows = (kbHeight - PADDING) / (buttonH + PADDING);

        if (textKbd) {
            if (neededColumns == 0) {
                maxColumns = (kbWidth - PADDING) / (buttonW + PADDING);
            } else {
                maxColumns = neededColumns;
            }
            kbWidth = maxColumns * (buttonW + PADDING) + PADDING + 1;
            kbX = (vkl.getAvailableWidth() - kbWidth) / 2;
        } else {
            maxColumns = 7; // verify
            kbWidth = maxColumns * (buttonW + PADDING) + PADDING + 1;
            kbX = 0;

        }

        if (neededRows == 0) {
            int tmpMax = 0; // will hold the longest keyboard.
            for (int i = 0; i < keys.length; i++) {
                if (tmpMax < keys[i].length)
                    tmpMax = keys[i].length;
            }
            neededRows = (tmpMax + maxColumns - 1) / maxColumns;
        }
        if (neededRows > maxRows) {
System.err.println("Keys list is too long for this size of screen.");
System.err.println("Please split your keyboard array to multiple arrays.");
            // System.exit(0);
            throw new VirtualKeyboardException("Keys list is too long for this size of screen.");
        }
        maxRows = neededRows;
        int neededHeight = 0;
        // do not require to account for meta keys for a canvas keyboard-hk
        if (textKbd) {
            neededHeight = maxRows * (buttonH + PADDING) + 4 * PADDING + // between the keys and the meta keys
                           IMAGE_SIZE + META_PADDING * 4 + textfieldHeight + candidateFieldHeight;
            kbY = kbHeight - neededHeight - 4 * PADDING;
            kbHeight = neededHeight;

        } else {
            neededHeight = maxRows * (buttonH + PADDING) + 3 * PADDING + textfieldHeight;
            kbY = vkl.getAvailableHeight() - neededHeight;
            kbHeight = neededHeight;
        }

        this.keys = keys;

        if (textKbd)
            currentKeyboard = 1; // lower case
        else
            currentKeyboard = 0;
        fullColumns = keys[currentKeyboard].length / maxColumns;
        // need not be displayed in the canvas mode
        if (displayTextArea) { // hk
            metaKeys = new Image[7];
            metaKeys[OK_META_KEY] = createImage("/ok.png");
            metaKeys[CANCEL_META_KEY] = createImage("/cancel.png");
            metaKeys[BACKSPACE_META_KEY] = createImage("/backspace.png");
            metaKeys[SHIFT_META_KEY] = createImage("/shift.png");
            metaKeys[CAPS_META_KEY] = createImage("/caps.png");
            metaKeys[MODE_META_KEY] = createImage("/mode.png");
//            metaKeys[CNINPUT_META_KEY] = createImage("cn.png");
        }
    }

    private Image createImage(String name) {
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(name));
        return image;
    }

    /**
     * Checks if the virtual keyboard is enabled.
     * 
     * @return <code>true</code> if the virtual keyboard is enabled,
     *         <code>false</code> otherwise.
     */
    static boolean isKeyboardEnabled() {
        return USE_VIRTUAL_KEYBOARD;
    }

    /**
     * Checks if the virtual keyboard is enabled.
     * 
     * @return <code>true</code> if the virtual keyboard is enabled,
     *         <code>false</code> otherwise.
     */
    static boolean isKeypadEnabled() {
        return USE_VIRTUAL_KEYPAD;
    }

    /**
     * Checks if the virtual keyboard is opened automatically.
     * 
     * @return <code>true</code> if the virtual keyboard is opened
     *         automatically, <code>false</code> otherwise.
     */
    static boolean isAutoOpen() {
        return USE_VIRTUAL_KEYBOARD_OPEN_AUTO;
    }

    /**
     * traverse the virtual keyboard according to key pressed.
     * 
     * @param type type of keypress
     * @param keyCode key code of key pressed
     */
    void traverse(int type, int keyCode) {
System.err.println("VirtualK: keyCode=" + keyCode);

        // Soft button means dismiss to the virtual keyboard
        if (type == RELEASED && keyCode == 461) {
            vkl.virtualKeyEntered(type, (char) 0);
            return;
        }

        if (!inMetaKeys) {
            if (type == RELEASED &&
                keyCode != KeyEvent.VK_ENTER) {
                // in this case we don't want to traverse on key release

            } else {
                switch (keyCode) {
                case KeyEvent.VK_RIGHT:
                    currentChar++;
                    if (currentChar > keys[currentKeyboard].length - 1) {
                        currentChar = 0;
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    currentChar--;
                    if (currentChar < 0) {
                        currentChar = keys[currentKeyboard].length - 1;
                    }
                    break;
                case KeyEvent.VK_UP:
                    currentChar = (currentChar - maxColumns);
                    if (currentChar < 0) {
                        currentChar = currentChar + (fullColumns + 1) * maxColumns;
                        if (currentChar > keys[currentKeyboard].length - 1) {
                            currentChar -= maxColumns;
                        }
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    currentChar = (currentChar + maxColumns);
                    if (currentChar > keys[currentKeyboard].length - 1) {
                        currentChar = (currentChar - maxColumns);
                        inMetaKeys = true;
                    }
                    break;
                case KeyEvent.VK_ENTER:
                    // System.out.println("Key Selected - type :" + type + ", "
                    // + keys[currentKeyboard][currentChar]);
                    vkl.virtualKeyEntered(type, keys[currentKeyboard][currentChar]);
                    if (inShift && type == PRESSED) {
                        // shift is a one-shot upper case
                        inShift = false;
                        if (textKbd) {
                            currentKeyboard = 1;
                            vkl.virtualMetaKeyEntered(IM_CHANGED_KEY);
                        } // hk : still need a keyboard displayed
                        else {
                            currentKeyboard = 0;
                        }
                    }
                    break;
                }
            }
        } else {

            if (type != RELEASED) {

                // meta keys
                switch (keyCode) {
                case KeyEvent.VK_RIGHT:
                    currentMeta++;
                    if (currentMeta > metaKeys.length - 1) {
                        currentMeta = 0;
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    currentMeta--;
                    if (currentMeta < 0) {
                        currentMeta = metaKeys.length - 1;
                    }
                    break;
                case KeyEvent.VK_UP:
                    inMetaKeys = false;
                    break;
                case KeyEvent.VK_DOWN:
                    inMetaKeys = false;
                    currentChar = (currentChar + maxColumns);
                    if (currentChar > keys[currentKeyboard].length - 1) {
                        currentChar = currentChar % maxColumns;
                    }
                    break;
                case KeyEvent.VK_ENTER:

                    switch (currentMeta) {
                    case SHIFT_META_KEY: // "Shift" - one shot upper case
                        if (currentKeyboard == 1) { // lower case
                            currentKeyboard = 2;
                            vkl.virtualMetaKeyEntered(IM_CHANGED_KEY);
                        }
                        inShift = true;
                        inMetaKeys = false;
                        break;
                    case CAPS_META_KEY: // "CapsL" (caps lock)
                        if (currentKeyboard == 1) { // lower case
                            currentKeyboard = 2;
                        } else if (currentKeyboard == 2) { // upper case
                            currentKeyboard = 1;
                        }
                        vkl.virtualMetaKeyEntered(IM_CHANGED_KEY);
                        break;
                    case MODE_META_KEY: // "Mode"
                        currentKeyboard = (currentKeyboard + 1) % keys.length;
                        vkl.virtualMetaKeyEntered(IM_CHANGED_KEY);
                        break;
                    case BACKSPACE_META_KEY: // "backspace"
                        vkl.virtualMetaKeyEntered(BACKSPACE_META_KEY);
                        break;
                    case OK_META_KEY: // "ok"
                        vkl.virtualMetaKeyEntered(OK_META_KEY);
                        break;
                    case CANCEL_META_KEY: // "cancel"
                        vkl.virtualMetaKeyEntered(CANCEL_META_KEY);
                        break;
                    case CNINPUT_META_KEY: // "cn_input"
                        vkl.virtualMetaKeyEntered(CNINPUT_META_KEY);
                        break;
                    }
                }
            }
        }

        if (type != RELEASED) {
            if (461 == keyCode) {
                vkl.virtualMetaKeyEntered(BACKSPACE_META_KEY);
            } else {

                switch (keyCode) {
                // Short cuts by number keys
                case 461:
                    vkl.virtualMetaKeyEntered(CNINPUT_META_KEY);
                    break;
                case KeyEvent.VK_ENTER:
                    currentKeyboard = (currentKeyboard + 1) % keys.length;
                    vkl.virtualMetaKeyEntered(IM_CHANGED_KEY);
                    break;
                case KeyEvent.VK_NUMPAD2:
                    vkl.virtualMetaKeyEntered(CURSOR_UP_META_KEY);
                    break;
                case KeyEvent.VK_NUMPAD4:
                    vkl.virtualMetaKeyEntered(CURSOR_LEFT_META_KEY);
                    break;
                case KeyEvent.VK_NUMPAD6:
                    vkl.virtualMetaKeyEntered(CURSOR_RIGHT_META_KEY);
                    break;
                case KeyEvent.VK_NUMPAD8:
                    vkl.virtualMetaKeyEntered(CURSOR_DOWN_META_KEY);
                    break;
                case KeyEvent.VK_NUMPAD5:
                    vkl.virtualMetaKeyEntered(CNINPUT_SELECT_META_KEY);
                    break;
                }
            }
        }
        // triggers paint()
        vkl.repaintVK();
    }

    /**
     * paint the virtual keyboard on the screen
     * 
     * @param g The graphics context to paint to
     */
    protected void paint(Graphics g) {
        int actualHeight = kbHeight + candidateFieldHeight;
        g.setFont(f);
        g.setColor(Color.lightGray);

        g.fillRect(0, 0, kbWidth, actualHeight);
        drawBorder(g, 0, 0, kbWidth - 1, actualHeight - 1);

        if (candidateFieldHeight > 0) {
            drawCandidateBar(g);
        }

        g.translate(0, candidateFieldHeight);

        if (textfieldHeight > 0) {
            drawTextField(g);
        }

        g.translate(0, textfieldHeight);
        drawKeys(g);

        g.translate(0, actualHeight - (IMAGE_SIZE + 4 * PADDING + 2 * META_PADDING) - textfieldHeight - candidateFieldHeight);
        if (textKbd) {
            drawMetaKeys(g);
        }
    }

    /**
     * Draw the text field of the virtual keyboard.
     * 
     * @param g The graphics context to paint to
     */
    void drawTextField(Graphics g) {
        drawSunkedBorder(g, PADDING, PADDING, kbWidth - 2 * PADDING, textfieldHeight);

        g.setClip(0, 0, kbWidth - 2 * PADDING, textfieldHeight);

        g.translate(PADDING + 1, 0);

        vkl.paintTextOnly(g, kbWidth, textfieldHeight);

        g.translate(-PADDING - 1, 0);
        g.setClip(0, 0, kbWidth, kbHeight);
    }

    void drawCandidateBar(Graphics g) {

        g.setClip(0, 0, kbWidth - 2 * PADDING, candidateFieldHeight);

        g.translate(PADDING + 1, 2 * PADDING);

        vkl.paintCandidateBar(g, kbWidth - 3 * PADDING, candidateFieldHeight - 2 * PADDING);

        g.translate(-PADDING - 1, -PADDING);
        g.setClip(0, 0, kbWidth, kbHeight);
    }

    /**
     * draw keyboard keys
     * 
     * @param g The graphics context to paint to
     */
    void drawKeys(Graphics g) {

        int tmp;

        if (!textKbd) {
            currentKeyboard = 0;
        }

        for (int i = 0; i < maxRows; i++) {
            for (int j = 0; j < maxColumns; j++) {
                tmp = i * maxColumns + j;
                if (tmp >= keys[currentKeyboard].length) {
                    // no more chars to draw
                    break;
                }

                if (currentChar == tmp && inMetaKeys == false) {

                    drawButton(g, j * (PADDING + buttonW) + PADDING, i * (PADDING + buttonH) + PADDING, buttonW, buttonH);
                } else {
                    drawBeveledButton(g, j * (PADDING + buttonW) + PADDING, i * (PADDING + buttonH) + PADDING, buttonW, buttonH);
                }

                // g.setColor(DARK_GRAY);
                g.setColor(TEXT_COLOR);
                g.drawString("" + keys[currentKeyboard][tmp], j * (PADDING + buttonW) + PADDING + fontWCenter, i * (PADDING + buttonH) + PADDING + fontHTop);
            }
        }
    }

    /**
     * draw keyboard meta keys
     * 
     * @param g The graphics context to paint to
     */
    void drawMetaKeys(Graphics g) {

        int mkWidth = metaKeys.length * (IMAGE_SIZE + 3 * META_PADDING) + META_PADDING;
        int currX = (kbWidth - mkWidth) / 2 + 2 * META_PADDING;
        int currY = 0;

        if (inMetaKeys) {
            drawBorder(g, currX - 2 * META_PADDING, // x1
                       currY - 2 * META_PADDING, // y1
                       currX + mkWidth,
                       currY + IMAGE_SIZE + 2 * META_PADDING);
        }

        for (int i = 0; i < metaKeys.length; i++) {
            if (currX + IMAGE_SIZE > kbWidth) {

                currX = PADDING;
                currY -= (IMAGE_SIZE + META_PADDING);
            }
            if (inMetaKeys && i == currentMeta) {
                drawButton(g, currX, currY, IMAGE_SIZE + 2 * META_PADDING, IMAGE_SIZE + 2 * META_PADDING);
            } else {
                drawBeveledButton(g, currX, currY, IMAGE_SIZE + 2 * META_PADDING, IMAGE_SIZE + 2 * META_PADDING);
            }
            g.drawImage(metaKeys[i], currX + META_PADDING, currY + META_PADDING, null);
            currX += (IMAGE_SIZE + 2 * META_PADDING + 2);
            if (currX > kbWidth) {
                currX = META_PADDING;
                currY -= (IMAGE_SIZE + META_PADDING);
            }
        }
    }

    /**
     * draw a border
     * 
     * @param g The graphics context to paint to
     * @param x1 x-coordinate of the button's location
     * @param y1 y-coordinate of the button's location
     * @param x2 the x-coordinate at the width of the border
     * @param y2 the y-coordinate at the height of the border
     */
    private void drawBorder(Graphics g, int x1, int y1, int x2, int y2) {

        g.setColor(Color.gray);
        g.drawLine(x1 + 2, y1 + 2, x1 + 2, y2 - 3); // left
        g.drawLine(x1 + 2, y1 + 2, x2 - 2, y1 + 2); // top
        g.drawLine(x1 + 2, y2 - 1, x2 - 1, y2 - 1); // bottom
        g.drawLine(x2 - 1, y1 + 2, x2 - 1, y2 - 1); // right
        g.setColor(Color.white);
        g.drawRect(x1 + 1, y1 + 1, x2 - x1 - 3, y2 - y1 - 3);
    }

    /**
     * draw a sunken border
     * 
     * @param g The graphics context to paint to
     * @param x1 x-coordinate of the button's location
     * @param y1 y-coordinate of the button's location
     * @param x2 the x-coordinate at the width of the border
     * @param y2 the y-coordinate at the height of the border
     */
    private void drawSunkedBorder(Graphics g, int x1, int y1, int x2, int y2) {

        g.setColor(Color.white);
        g.fillRect(x1 + 2, y1 + 2, x2 - x1 - 2, y2 - y1 - 2);

        g.setColor(Color.gray);
        g.drawLine(x1 + 2, y1 + 2, x1 + 2, y2 - 2); // left
        g.drawLine(x1 + 2, y1 + 2, x2 - 2, y1 + 2); // top
        g.setColor(Color.darkGray);
        g.drawLine(x1 + 3, y1 + 3, x1 + 3, y2 - 3); // left
        g.drawLine(x1 + 3, y1 + 3, x2 - 3, y1 + 3); // top

        g.setColor(Color.lightGray);
        g.drawLine(x1 + 3, y2 - 2, x2 - 2, y2 - 2); // bottom
        g.drawLine(x2 - 2, y1 + 3, x2 - 2, y2 - 2); // right
    }

    /**
     * draw a button
     * 
     * @param g The graphics context to paint to
     * @param x x-coordinate of the button's location
     * @param y y-coordinate of the button's location
     * @param w the width of the button
     * @param h the height of the button
     */
    private void drawButton(Graphics g, int x, int y, int w, int h) {
        g.setColor(Color.gray);
        g.drawLine(x + 1, y + h - 1, x + w, y + h - 1); // bottom
        g.drawLine(x + w - 1, y + 1, x + w - 1, y + h); // right

        g.setColor(Color.darkGray);
        g.drawLine(x, y + h, x + w, y + h); // bottom
        g.drawLine(x + w, y, x + w, y + h); // right

        g.setColor(Color.white);
        g.drawLine(x, y, x + w - 1, y);
        g.drawLine(x, y, x, y + h - 1);

    }

    /**
     * draw a beveled button
     * 
     * @param g The graphics context to paint to
     * @param x x-coordinate of the button's location
     * @param y y-coordinate of the button's location
     * @param w the width of the button
     * @param h the height of the button
     */
    private void drawBeveledButton(Graphics g, int x, int y, int w, int h) {
        g.setColor(Color.gray);
        g.drawLine(x + 1, y + h - 1, x + w, y + h - 1); // bottom
        g.drawLine(x + w - 1, y + 1, x + w - 1, y + h); // right

        g.setColor(Color.white);
        g.drawLine(x, y + h, x + w, y + h); // bottom
        g.drawLine(x + w, y, x + w, y + h); // right

        g.setColor(Color.gray);
        g.drawLine(x, y, x + w - 1, y);
        g.drawLine(x, y, x, y + h - 1);

        g.setColor(Color.white);
        g.drawLine(x + 1, y + 1, x + w - 2, y + 1);
        g.drawLine(x + 1, y + 1, x + 1, y + h - 2);

    }

    /**
     * Helper function to determine the itemIndex at the x,y position
     * 
     * @param x,y pointer coordinates in menuLayer's space (0,0 means left-top
     *            corner) both value can be negative as menuLayer handles the
     *            pointer event outside its bounds
     * @return menuItem's index since 0, or PRESS_OUT_OF_BOUNDS, PRESS_ON_TITLE
     * 
     */
    private boolean isKeyAtPointerPosition(int x, int y) {
        int tmpX, tmpY, tmp;
        for (int i = 0; i < maxRows; i++) {
            for (int j = 0; j < maxColumns; j++) {
                tmp = i * maxColumns + j;
                if (tmp >= keys[currentKeyboard].length) {
                    // no more chars to draw
                    break;
                }

                tmpX = x - (j * (PADDING + buttonW) + PADDING);
                tmpY = y - (i * (PADDING + buttonH) + PADDING) - textfieldHeight;

                if ((tmpX >= 0) && (tmpY >= 0) && (tmpX < buttonW) && (tmpY < buttonH)) {
                    currentChar = tmp;
                    inMetaKeys = false;
                    return true;
                }

            }
        }

        if (metaKeys == null) {
            return false;
        }

        // Check for meta chars
        int mkWidth = metaKeys.length * (IMAGE_SIZE + 3 * META_PADDING) + META_PADDING;
        int currX = (kbWidth - mkWidth) / 2 + 2 * META_PADDING;
        int currY = kbHeight - (IMAGE_SIZE + 6 * META_PADDING);

        for (int i = 0; i < metaKeys.length; i++) {
            if (currX + IMAGE_SIZE > kbWidth) {

                currX = PADDING;
                currY -= (IMAGE_SIZE + META_PADDING);
            }

            tmpX = x - currX;
            tmpY = y - currY;

            if ((tmpX >= 0) && (tmpY >= 0) && (tmpX < (IMAGE_SIZE + 2 * META_PADDING)) && (tmpY < (IMAGE_SIZE + 2 * META_PADDING))) {
                currentMeta = i;
                inMetaKeys = true;
                return true;
            }

            currX += (IMAGE_SIZE + 2 * META_PADDING + 2);
            if (currX > kbWidth) {
                currX = META_PADDING;
                currY -= (IMAGE_SIZE + META_PADDING);
            }
        }

        return false;
    }

    /**
     * Handle input from a pen tap. Parameters describe the type of pen event
     * and the x,y location in the layer at which the event occurred. Important
     * : the x,y location of the pen tap will already be translated into the
     * coordinate space of the layer.
     * 
     * @param type the type of pen event
     * @param x the x coordinate of the event
     * @param y the y coordinate of the event
     */
    public boolean pointerInput(int type, int x, int y) {
        switch (type) {
        case PRESSED:

            // dismiss the menu layer if the user pressed outside the menu
            if (isKeyAtPointerPosition(x, y)) {
                // press on valid key
                traverse(type, KeyEvent.VK_ENTER);
                vkl.repaintVK();

            }
            break;
        case RELEASED:
            if (isKeyAtPointerPosition(x, y)) {
                traverse(type, KeyEvent.VK_ENTER);
                vkl.repaintVK();

            }

            break;
        }
        // return true always as menuLayer will capture all of the pointer
        // inputs
        return true;
    }

    // ********* attributes ********* //

    private final static Color TEXT_COLOR = Color.black;

    /** padding between rows of buttons */
    private int PADDING;

    /** padding used by the meta keys */
    private final static int META_PADDING = 2;

    /** size of meta icons */
    private final static int IMAGE_SIZE = 13;

    // If you want to change the order of the buttons, just
    // change the serial numbers here:
    final static int OK_META_KEY = 0;
    final static int CANCEL_META_KEY = 1;
    final static int MODE_META_KEY = 2;
    final static int BACKSPACE_META_KEY = 3;
    final static int SHIFT_META_KEY = 4;
    final static int CAPS_META_KEY = 5;
    final static int CNINPUT_META_KEY = 6;
    final static int CURSOR_UP_META_KEY = 7;
    final static int CURSOR_DOWN_META_KEY = 8;
    final static int CURSOR_LEFT_META_KEY = 9;
    final static int CURSOR_RIGHT_META_KEY = 10;
    final static int CNINPUT_SELECT_META_KEY = 11;

    // When input method is changed, process this key to update UI
    final static int IM_CHANGED_KEY = 99;

}

class VirtualKeyboardException extends Exception {
    /**
     * Constructs an <code>IOException</code> with <code>null</code> as its
     * error detail message.
     */
    public VirtualKeyboardException() {
        super();
    }

    /**
     * Constructs an <code>IOException</code> with the specified detail message.
     * The error message string <code>s</code> can later be retrieved by the
     * <code>{@link java.lang.Throwable#getMessage}</code> method of class
     * <code>java.lang.Throwable</code>.
     * 
     * @param s the detail message.
     */
    public VirtualKeyboardException(String s) {
        super(s);
    }
}
