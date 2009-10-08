/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.ui;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import ch.laoe.audio.Audio;
import ch.laoe.audio.AudioException;
import ch.laoe.clip.AClip;
import ch.oli4.ui.UiPopupMenu;


/**
 * graphical component that represents a layered clip. All layers are mixed together with alpha.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 17.08.00 erster Entwurf oli4 <br>
 *          10.11.00 attach undostack to AClip, remove scrollable part oli4 <br>
 *          30.03.01 exit from thread on destroy oli4 <br>
 *          28.07.01 repaints only thin area on playing oli4 <br> 
 *          10.07.02 key-actions added oli4 <br>
 */
public class GClipEditor extends GClipPanel implements Runnable, MouseListener, MouseMotionListener, KeyListener {
    /**
     * constructor
     */
    public GClipEditor(GMain main, GClipFrame frame, AClip clip) {
        super(clip);
        setLayout(new BorderLayout());

        // mouse handler
        addMouseListener(this);
        addMouseMotionListener(this);

        this.main = main;
        this.frame = frame;

        // popup menu
        popupMenu = main.getPluginHandler().createFullPopupMenu();
        addMouseListener(popupMenu);
        addKeyListener(popupMenu);

        thread = new Thread(this);
        running = true;
        thread.start();

        // key
        addKeyListener(this);
    }

    private GClipFrame frame;

    private GMain main;

    // popup menu
    private UiPopupMenu popupMenu;

    // thread
    protected Thread thread;

    private boolean running;

    private int whenInactiveCounter;

    private static final int numberOfRepaintWhenInactive = 40;

    public void run() {
        while (running) {
            try {
                // repaint counter...
                if (clip.getAudio().isActive()) {
                    whenInactiveCounter = numberOfRepaintWhenInactive;
                } else {
                    whenInactiveCounter--;
                }

                // repaint...
                if (whenInactiveCounter >= 0) {
                    repaint(clip.getAudio().getPlotter().getDirtyRegion());
                    // repaint();
                }

                Thread.sleep(100);
            } catch (Exception e) {
                Debug.printStackTrace(6, e);
            }
        }
    }

    /**
     * destroys the running tasks. call when not using anymore.
     */
    public void destroy() {
        running = false;
        clip.destroy();
    }

    protected void paintFullClip(Graphics2D g2d) {
        Debug.println(7, "clip editor " + getClip().getName() + ", paint when clip changed ");
        Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
        clipPlotter.paintFullClip(bgGraphics, rect);
    }

    protected void paintTopComponentsOnly(Graphics2D g2d) {
        Debug.println(7, "clip editor " + getClip().getName() + ", paint clip always ");
        Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
        clipPlotter.paintDetailsOfSelectedLayer(g2d, rect);

        // plugin-specific painting...
        g2d.setClip(rect);
        main.getPluginHandler().paintOntoClip(g2d, rect);
    }

    // mouse listener and mouse motion listener

    public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger() && !isButton2Or3(e))
            main.getPluginHandler().mouseClicked(e);
    }

    public void mouseEntered(MouseEvent e) {
        if (!e.isPopupTrigger() && !isButton2Or3(e))
            main.getPluginHandler().mouseEntered(e);
    }

    public void mouseExited(MouseEvent e) {
        if (!e.isPopupTrigger() && !isButton2Or3(e))
            main.getPluginHandler().mouseExited(e);
    }

    public void mousePressed(MouseEvent e) {
        if (!e.isPopupTrigger() && !isButton2Or3(e))
            main.getPluginHandler().mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        if (!e.isPopupTrigger() && !isButton2Or3(e))
            main.getPluginHandler().mouseReleased(e);
    }

    public void mouseMoved(MouseEvent e) {
        if (!e.isPopupTrigger() && !isButton2Or3(e))
            main.getPluginHandler().mouseMoved(e);
    }

    public void mouseDragged(MouseEvent e) {
        if (!e.isPopupTrigger() && !isButton2Or3(e))
            main.getPluginHandler().mouseDragged(e);
    }

    private boolean isButton2Or3(MouseEvent e) {
        return (e.getModifiers() & (InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0;
    }

    // key listener
    public void keyPressed(KeyEvent e) {
        Audio a = clip.getAudio();
        switch (e.getKeyCode()) {
        case KeyEvent.VK_S:
            Debug.println(1, "plugin " + getName() + " s key pressed");
            a.setLoopStartPointer(a.getPlayPointer());
            repaint();
            break;

        case KeyEvent.VK_E:
            Debug.println(1, "plugin " + getName() + " e key pressed");
            a.setLoopEndPointer(a.getPlayPointer());
            repaint();
            break;

        case KeyEvent.VK_M:
            Debug.println(1, "plugin " + getName() + " m key pressed");
            clip.getSelectedLayer().getSelectedChannel().getMarker().addMarker(a.getPlayPointer());
            repaint();
            break;

        case KeyEvent.VK_SPACE:
            Debug.println(1, "plugin " + getName() + " space key pressed");
            a.pause();
            break;

        case KeyEvent.VK_LEFT:
            Debug.println(1, "plugin " + getName() + " left arrow key pressed");
            a.rewind();
            break;

        case KeyEvent.VK_RIGHT:
            Debug.println(1, "plugin " + getName() + " right arrow key pressed");
            a.forwind();
            break;

        case KeyEvent.VK_P:
            Debug.println(1, "plugin " + getName() + " p key pressed");
            try {
                a.play();
            } catch (AudioException ae) {
                Debug.printStackTrace(1, ae);
            }
            break;

        case KeyEvent.VK_T:
            Debug.println(1, "plugin " + getName() + " t key pressed");
            a.stop();
            break;
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

}
