package vavi.apps.nes.controller;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import vavi.apps.nes.NES;


/**
 * Class for the Mouse Monitor required by the GUI.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class GUIMouseListener implements MouseMotionListener, MouseListener {

    /**
     * The current Graphical User Interface.
     */
    private NES.View gui;

    /**
     * Create a new Mouse Monitor for the GUI.
     * 
     * @param Nes The current NES Machine.
     * @param Gui The current Graphical User Interface.
     */
    public GUIMouseListener(NES.View gui) {
        this.gui = gui;
    }

    /**
     * Handler for when Mouse is Pressed
     */
    public void mousePressed(MouseEvent e) {
        gui.onZapper(e.getX(), e.getY());
    }

    /**
     * Handler for when Mouse is Released
     */
    public void mouseReleased(MouseEvent e) {
        gui.offZapper(e.getX(), e.getY());
    }

    /**
     * Handler for when Mouse is Dragged
     */
    public void mouseDragged(MouseEvent e) {
    }

    /**
     * Handler for when Mouse is Moved
     */
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * Handler for when Mouse is Clicked
     */
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Handler for when Mouse Enters
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Handler for when Mouse Exits
     */
    public void mouseExited(MouseEvent e) {
    }
}
