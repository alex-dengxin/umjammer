/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;

import vavi.apps.nes.NES;


/**
 * Main NESCafe Class.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class NESCafe extends Activity {

    protected NES nes;

    /* */
    @Override
    public void onCreate(Bundle icicle) {
Debug.startMethodTracing("nes");
        super.onCreate(icicle);
        // Create the GUI and NES
        nes = new NES();
        GUI gui = new GUI(this);
        // Initialize the NES Machine and the Graphical User Interface
        nes.init(gui);
        setContentView(gui);

        nes.gui.writeToScreen("Welcome to NESCafe " + 5.6);
        // Load Default ROM from JAR
        nes.cartLoadDefault();
    }

    public void onStop() {
        super.onStop();
Debug.stopMethodTracing();        
    }
}
