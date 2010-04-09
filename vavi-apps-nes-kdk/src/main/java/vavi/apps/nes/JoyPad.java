/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Class for the JoyPad Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class JoyPad {
    /**
     * Status of the Joypad
     */
    private int joypad = 0;

    /**
     * Declare variable for current button being read
     */
    private int joypadBit = 0;

    /**
     * Button A on the JoyPad 1.
     */
    protected static final int BUTTON1_A = 'X';

    /**
     * Button B on the JoyPad 1.
     */
    protected static final int BUTTON1_B = 'Z';

    /**
     * The START button on the JoyPad 1.
     */
    protected static final int BUTTON1_START = '\n';

    /**
     * The SELECT button on the JoyPad 1.
     */
    protected static final int BUTTON1_SELECT = 0x20;

    /**
     * The UP button on the JoyPad 1.
     */
    protected static final int BUTTON1_UP = 38;

    /**
     * The DOWN button on the JoyPad 1.
     */
    protected static final int BUTTON1_DOWN = 40;

    /**
     * The LEFT button on the JoyPad1 .
     */
    protected static final int BUTTON1_LEFT = 37;

    /**
     * The RIGHT button on the JoyPad 1.
     */
    protected static final int BUTTON1_RIGHT = 39;

    /**
     * Button A on the JoyPad 2.
     */
    protected static final int BUTTON2_A = 'T';

    /**
     * Button B on the JoyPad 2.
     */
    protected static final int BUTTON2_B = 'R';

    /**
     * The START button on the JoyPad 2.
     */
    protected static final int BUTTON2_START = 'E';

    /**
     * The SELECT button on the JoyPad 2.
     */
    protected static final int BUTTON2_SELECT = 'Q';

    /**
     * The UP button on the JoyPad 2.
     */
    protected static final int BUTTON2_UP = 'W';

    /**
     * The DOWN button on the JoyPad 2.
     */
    protected static final int BUTTON2_DOWN = 'S';

    /**
     * The LEFT button on the JoyPad2.
     */
    protected static final int BUTTON2_LEFT = 'A';

    /**
     * The RIGHT button on the JoyPad 2.
     */
    protected static final int BUTTON2_RIGHT = 'D';

    /**
     * JoyPad One.
     */
    protected static final int JOYPAD_1 = 0x01;

    /**
     * JoyPad Two.
     */
    protected static final int JOYPAD_2 = 0x02;

    /**
     * The current Joypad Number.
     */
    private int joyPadNumber = JOYPAD_1;

    /**
     * Creates a new JoyPad object.
     */
    public JoyPad(int controllerNumber) {
        joyPadNumber = controllerNumber;
    }

    /**
     * Method to hold down a button on the JoyPad
     * 
     * @param button The button to be pressed.
     */
    public final void buttonDown(int button) {
        if (joyPadNumber == JoyPad.JOYPAD_1) {
            switch (button) {
            case JoyPad.BUTTON1_A: // Button A is Down (X)
                joypad |= 0x1;
                return;
            case JoyPad.BUTTON1_B: // Button B is Down (Z)
                joypad |= 0x2;
                return;
            case JoyPad.BUTTON1_SELECT: // Select is Down
                joypad |= 0x4;
                return;
            case JoyPad.BUTTON1_START: // Start is Down
                joypad |= 0x8;
                return;
            case JoyPad.BUTTON1_UP: // Up
                joypad |= 0x10;
                return;
            case JoyPad.BUTTON1_DOWN: // Down
                joypad |= 0x20;
                return;
            case JoyPad.BUTTON1_LEFT: // Left
                joypad |= 0x40;
                return;
            case JoyPad.BUTTON1_RIGHT: // Right
                joypad |= 0x80;
                return;
            default:
                return;
            }
        } else if (joyPadNumber == JoyPad.JOYPAD_2) {
            switch (button) {
            case JoyPad.BUTTON2_A: // Button A is Down
                joypad |= 0x1;
                return;
            case JoyPad.BUTTON2_B: // Button B is Down
                joypad |= 0x2;
                return;
            case JoyPad.BUTTON2_SELECT: // Select is Down
                joypad |= 0x4;
                return;
            case JoyPad.BUTTON2_START: // Start is Down
                joypad |= 0x8;
                return;
            case JoyPad.BUTTON2_UP: // Up
                joypad |= 0x10;
                return;
            case JoyPad.BUTTON2_DOWN: // Down
                joypad |= 0x20;
                return;
            case JoyPad.BUTTON2_LEFT: // Left
                joypad |= 0x40;
                return;
            case JoyPad.BUTTON2_RIGHT: // Right
                joypad |= 0x80;
                return;
            default:
                return;
            }
        }
    }

    /**
     * Method to release a button on the JoyPad.
     * 
     * @param button The button to be released.
     */
    public final void buttonUp(int button) {
        if (joyPadNumber == JoyPad.JOYPAD_1) {
            switch (button) {
            case JoyPad.BUTTON1_A: // Button A is Up (X)
                joypad &= 0xFE;
                return;
            case JoyPad.BUTTON1_B: // Button B is Up (Z)
                joypad &= 0xFD;
                return;
            case JoyPad.BUTTON1_SELECT: // Select is Up
                joypad &= 0xFB;
                return;
            case JoyPad.BUTTON1_START: // Start is Up
                joypad &= 0xF7;
                return;
            case JoyPad.BUTTON1_UP: // Up
                joypad &= 0xEF;
                return;
            case JoyPad.BUTTON1_DOWN: // Down
                joypad &= 0xDF;
                return;
            case JoyPad.BUTTON1_LEFT: // Left
                joypad &= 0xBF;
                return;
            case JoyPad.BUTTON1_RIGHT: // Right
                joypad &= 0x7F;
                return;
            default:
                return;
            }
        } else if (joyPadNumber == JoyPad.JOYPAD_2) {
            switch (button) {
            case JoyPad.BUTTON2_A: // Button A is Up (X)
                joypad &= 0xFE;
                return;
            case JoyPad.BUTTON2_B: // Button B is Up (Z)
                joypad &= 0xFD;
                return;
            case JoyPad.BUTTON2_SELECT: // Select is Up
                joypad &= 0xFB;
                return;
            case JoyPad.BUTTON2_START: // Start is Up
                joypad &= 0xF7;
                return;
            case JoyPad.BUTTON2_UP: // Up
                joypad &= 0xEF;
                return;
            case JoyPad.BUTTON2_DOWN: // Down
                joypad &= 0xDF;
                return;
            case JoyPad.BUTTON2_LEFT: // Left
                joypad &= 0xBF;
                return;
            case JoyPad.BUTTON2_RIGHT: // Right
                joypad &= 0x7F;
                return;
            default:
                return;
            }
        }
    }

    /**
     * Method to return the status of the JoyPad.
     * 
     * @return Status value of the JoyPad
     */
    public final char getStatus() {
        return (char) joypadCorrection();
    }

    /**
     * Method to correct for when Both Directions are Pressed Together. Thanks
     * to SmashManiac for this function, which allows better handling of the
     * JoyPad when both UP+DOWN or LEFT+RIGHT are held together
     * 
     * @return Status value of the JoyPad
     */
    private int joypadCorrection() {
        int correction = joypad;
        if ((correction & 0x30) == 0x30) // Up and Down are pressed
            correction &= 0xCF;
        if ((correction & 0xC0) == 0xC0) // Left and Right are pressed
            correction &= 0x3F;
        return correction;
    }

    /**
     * Read value from the JoyPad for the currently indexed button.
     * 
     * @return Returns 1 if current indexed button is pressed, else 0.
     */
    public final int readJoyPadBit() {
        // Read the Indexed Bit
        int retVal = joypadCorrection() >> joypadBit;
        // Roll Index
        joypadBit = (joypadBit + 1) & 0x7;
        // Return Least Significant Bit
        return retVal & 0x1;
    }

    /**
     * Reset the JoyPad so that the next read is for button A.
     */
    public final void resetJoyPad() {
        joypadBit = 0;
    }

    /**
     * Set the value of the JoyPad.
     * 
     * @param value The desired value of the JoyPad.
     */
    public final void setStatus(int value) {
        joypad = value;
    }

    /**
     * Loads the State of the JoyPad from an InputStream.
     */
    public final void stateLoad(InputStream input) throws IOException {
        // Load JoyPad Information
        joypad = input.read() & 0xFF;
        joypadBit = input.read() & 0xFF;
    }

    /**
     * Saves the State of the JoyPad to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        // Save JoyPad Information
        output.write(joypad & 0xFF);
        output.write(joypadBit & 0xFF);
    }

    /**
     * String representation of the JoyPad object.
     * 
     * @return String value of JoyPad.
     */
    public final String toString() {
        return "NESCafe JoyPad " + joyPadNumber;
    }
}
