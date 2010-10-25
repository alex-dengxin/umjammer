package vavi.apps.nes.controller;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import vavi.apps.nes.NES;


/**
 * Class for the Mouse Monitor required by the GUI.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class GUIMouseListener implements OnTouchListener {

    /**
     * The current Graphical User Interface.
     */
    private NES nes;

    /**
     * Create a new Mouse Monitor for the GUI.
     * 
     * @param Nes The current NES Machine.
     * @param Gui The current Graphical User Interface.
     */
    public GUIMouseListener(NES nes) {
        this.nes = nes;
    }

    /**
     * Handler for when Mouse is Pressed
     */
    public boolean onTouch(View v, MotionEvent e) {
        switch (e.getAction()) {
        case MotionEvent.ACTION_DOWN:
            nes.onZapper((int) e.getX(), (int) e.getY());
            break;
        case MotionEvent.ACTION_UP:
            nes.offZapper((int) e.getX(), (int) e.getY());
            break;
        default:
            return true;
        }
        return false;
    }
}
