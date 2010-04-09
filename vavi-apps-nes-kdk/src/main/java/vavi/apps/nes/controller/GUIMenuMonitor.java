package vavi.apps.nes.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import vavi.apps.nes.NES;


/**
 * Class for the Menu Monitor required by the GUI.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class GUIMenuMonitor implements ActionListener {

    /**
     * The current Graphical User Interface.
     */
    private NES.View gui;

    /**
     * Create a new Menu Monitor for the GUI.
     * 
     * @param Nes The current NES Machine.
     * @param Gui The current Graphical User Interface.
     */
    public GUIMenuMonitor(NES.View gui) {
        this.gui = gui;
    }

    /**
     * Handler for when an action is performed on a Menu Item in the GUI.
     */
    public void actionPerformed(ActionEvent e) {
        // Grab the Command Name
        String command = e.getActionCommand();
        // Determine which Command to Perform
        // COMMAND : Close the Current ROM
        if (command == "CLOSE") {
            gui.fireViewEvent(new NES.View.ViewEvent(this, "doClose"));
            return;
        }
        // COMMAND : Exit the Emulator
        if (command == "EXIT") {
            gui.fireViewEvent(new NES.View.ViewEvent(this, "doExit"));
        }
        // COMMAND : Open a new Rom File
        if (command == "OPEN") {
            gui.fireViewEvent(new NES.View.ViewEvent(this, "doOpen"));
            return;
        }
        // COMMAND : Reset the Cart
        if (command == "RESET") {
            gui.fireViewEvent(new NES.View.ViewEvent(this, "doReset"));
            return;
        }
    }
}
