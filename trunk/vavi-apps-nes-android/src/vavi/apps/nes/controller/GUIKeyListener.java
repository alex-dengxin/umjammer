package vavi.apps.nes.controller;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

import vavi.apps.nes.NES;
import vavi.apps.nes.Utils;


/**
 * Class for the GUI key Listener required by the GUI Window.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class GUIKeyListener {
    /**
     * The current NES Machine.
     */
    private NES nes;

    /**
     * Create a new GUI Key Listener.
     * 
     * @param Nes The current NES Machine.
     * @param Gui The current Graphical User Interface.
     */
    public GUIKeyListener(NES nes) {
        this.nes = nes;
    }

    /**
     * Handler for when a key is pressed in the GUI Window.
     * 
     * @param keyevent The KeyEvent.
     */
    public final void keyPressed(int userPressed) {
        // System.out.println(userPressed);
        // Inform JoyPads of KeyPress
        nes.joyPad1.buttonDown(userPressed);
        nes.joyPad2.buttonDown(userPressed);
        // KEYPRESS 0-9 : Change FrameSkip
        if ((userPressed >= 49) && (userPressed <= 56)) {
            nes.gui.setFrameSkip(userPressed - 49);
            nes.gui.writeToScreen("Frame Skip = " + (userPressed - 49));
            return;
        }
        // Dump Memory to Disk
        if (userPressed == 'M') {
            if (!nes.regdebug) {
                nes.memory.debugOpen();
                nes.memory.debugDump();
                nes.memory.debugClose();
                nes.gui.writeToScreen("Dumped Memory to Disk");
                return;
            }
        }
        // Enable Debugging for Developer
        if (userPressed == 'E') {
            nes.regdebug = !nes.regdebug;
            nes.gui.writeToScreen("Registry Debug : " + nes.regdebug);
            if (nes.regdebug) {
                nes.memory.debugOpen();
            } else {
                nes.memory.debugClose();
            }
            return;
        }
        // Provide Scanning of Memory
        if (userPressed == 'W') {
            try {
                // Inform User
                System.out.println("Please enter value:");
                LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
                // Grab Input
                int a = Integer.parseInt(lnr.readLine());
                System.out.println("finding " + a);
                // Search Memory
                for (int i = 0; i < nes.memory.memory.length; i++) {
                    if (nes.memory.memory[i] == a)
                        System.out.println(Utils.hex(i, 4));
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return;
        }
        // KEYPRESS B : Black and White Toggle
        if (userPressed == 'B') {
            nes.gui.toggleBlackWhite();
            return;
        }
        // Adjust the Tint
        if (userPressed == '-') {
            nes.gui.decTint();
            return;
        }
        if (userPressed == '=') {
            nes.gui.incTint();
            return;
        }
        // Adjust the Hue
        if (userPressed == 57) {
            nes.gui.decHue();
            return;
        }
        if (userPressed == 48) {
            nes.gui.incHue();
            return;
        }
        // KEYPRESS C : Commit Point
        if (userPressed == 67) {
            nes.gui.setSaveStateRequest(true);
            nes.gui.setLoadStateRequest(false);
            nes.gui.writeToScreen("Attempting to Save State...");
            return;
        }
        // Debug Enter or Exit
        if (userPressed == 116) {
            nes.gui.fireViewEvent(new NES.View.ViewEvent(this, "doToggleDebug"));
            return;
        }
        // Debug Step
        if (userPressed == 117) {
            nes.cpu.debugStep(1);
            return;
        }
        // Debug Step x 20
        if (userPressed == 118) {
            nes.cpu.debugStep(20);
            return;
        }
        // Debug to Interrupt
        if (userPressed == 119) {
            nes.cpu.debugToInterrupt(true);
            return;
        }
        // KEYPRESS P : Pause the Current Cart
        if (userPressed == 80 && nes.isCartRunning()) {
            nes.gui.fireViewEvent(new NES.View.ViewEvent(this, "doPauseCart"));
            return;
        }
        // KEYPRESS R : Reset the Current Cart
        if (userPressed == 82 && nes.isCartRunning()) {
            nes.gui.fireViewEvent(new NES.View.ViewEvent(this, "doResetCart"));
            return;
        }
        // KEYPRESS L : Rollback to Commit Point
        if (userPressed == 'L') {
            nes.gui.setSaveStateRequest(false);
            nes.gui.setLoadStateRequest(true);
            nes.gui.writeToScreen("Attempting to Load State...");
            return;
        }
    }

    /**
     * Handler for when a key is released in the GUI Window.
     * 
     * @param keyevent The KeyEvent.
     */
    public final void keyReleased(int keyCode) {
        // Inform the JoyPad
        nes.joyPad1.buttonUp(keyCode);
        nes.joyPad2.buttonUp(keyCode);
    }

    /**
     * Handler for when a key is typed in the GUI Window.
     * 
     * @param keyevent The KeyEvent.
     */
    public final void keyTyped(int keyCode) {
    }
}
