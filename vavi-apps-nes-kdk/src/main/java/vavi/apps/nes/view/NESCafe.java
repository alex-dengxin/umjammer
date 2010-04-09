/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.view;

import com.amazon.kindle.kindlet.Kindlet;
import com.amazon.kindle.kindlet.KindletContext;

import vavi.apps.nes.NES;


/**
 * Main NESCafe Class.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class NESCafe implements Kindlet {

    NES nes;

    /**
     * Startup Method.
     */
    public void create(KindletContext context) {
        // Create the GUI and NES
        nes = new NES();
        GUI gui = new GUI(nes);
        // Initialize the NES Machine and the Graphical User Interface
        nes.init(gui);
        context.getRootContainer().add(gui);
    }

    public void destroy() {
    }

    public void start() {
        nes.gui.writeToScreen("Welcome to NESCafe " + 5.6);
        // Load Default ROM from JAR
        nes.cartLoadDefault();
    }

    public void stop() {
    }
}
