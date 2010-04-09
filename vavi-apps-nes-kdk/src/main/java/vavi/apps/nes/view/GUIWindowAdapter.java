/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.view;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import vavi.apps.nes.NES;


/**
 * Class for the Window Adapter required by GUI.
 * 
 * @author David de Niese
 * @version 0.56f
 */
final class GUIWindowAdapter implements WindowListener {
    /**
     * The current Graphical User Interface.
     */
    private GUI gui;

    /**
     * The current NES Engine.
     */
    private NES nes;

    /**
     * Create a new GUI Window Adaptor.
     * 
     * @param Gui The current Graphical User Interface.
     */
    public GUIWindowAdapter(GUI gui, NES nes) {
        this.gui = gui;
        this.nes = nes;
    }

    /**
     * Handler for when the GUI Window closes.
     * 
     * @param windowEvent The WindowEvent.
     */
    public final void windowClosing(WindowEvent windowEvent) {
        gui.setVisible(false);
        nes.cartUnload();
        System.exit(0);
    }

    /**
     * Handler for when the GUI Window is activated.
     * 
     * @param windowEvent The WindowEvent.
     */
    public final void windowActivated(WindowEvent windowEvent) {
        gui.pauseSystem(false);
    }

    /**
     * Handler for when the GUI Window is deactivated.
     * 
     * @param windowEvent The WindowEvent.
     */
    public final void windowDeactivated(WindowEvent windowEvent) {
        gui.pauseSystem(true);
    }

    /**
     * Handler for when the GUI Window is opened.
     * 
     * @param windowEvent The WindowEvent.
     */
    public final void windowOpened(WindowEvent windowEvent) {
    }

    /**
     * Handler for when the GUI Window is closed.
     * 
     * @param windowEvent The WindowEvent.
     */
    public final void windowClosed(WindowEvent windowEvent) {
    }

    /**
     * Handler for when the GUI Window is iconified.
     * 
     * @param windowEvent The WindowEvent.
     */
    public final void windowIconified(WindowEvent windowEvent) {
        gui.pauseSystem(true);
    }

    /**
     * Handler for when the GUI Window is deiconified.
     * 
     * @param windowEvent The WindowEvent.
     */
    public final void windowDeiconified(WindowEvent windowEvent) {
        gui.pauseSystem(false);
    }
}
