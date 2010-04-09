/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes;



/**
 * Class for the Message Displayer required by the NESCafe NES Emulator.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class Messenger extends Thread {
    /**
     * The current Graphical User Interface.
     */
    private NES.View gui;

    /**
     * The message to be displayed.
     */
    private String message = "";

    /**
     * Create a new Messenger Object.
     * 
     * @param Gui The GUI to receive the message.
     * @param Message The message that you wish to display.
     */
    public Messenger(NES.View gui, String message) {
        this.message = message;
        this.gui = gui;
    }

    /**
     * The run method for the Messenger Thread.
     */
    public final void run() {
        gui.sendMessage(message);
    }
}
