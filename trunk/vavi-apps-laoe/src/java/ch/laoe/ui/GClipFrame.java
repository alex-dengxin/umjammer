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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import ch.laoe.clip.AClip;
import ch.laoe.plugin.GPluginHandler;
import ch.oli4.ui.UiStroke;
import ch.oli4.ui.UiStrokeGlass;


/**
 * that frame contains the editable clip.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 27.08.00 erster Entwurf oli4 <br>
 */
public class GClipFrame extends JFrame implements WindowListener {
    /**
     * constructor
     */
    public GClipFrame(GMain g, AClip c) {
        GPersistance p = GPersistance.createPersistance();
        main = g;
        clipEditor = new GClipEditor(g, this, c);
        getContentPane().add(clipEditor);
        Dimension size = new Dimension(p.getInt("clip.frameSize.width"), p.getInt("clip.frameSize.height"));
        setSize(size);
        GToolkit.setFrameInMiddleOfScreen(this);
        URL u = getClass().getResource("resources/laoe.gif");
        if (u != null) {
            setIconImage(new ImageIcon(u).getImage());
        }

        // title
        if (c.getName() == null)
            c.setName("<" + GLanguage.translate("clip") + " " + clipFrameCounter + ">");

        setTitle(c.getName());
        clipFrameCounter++;

        // close
        addWindowListener(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // stroke
        stroke = new UiStrokeGlass(this);
        stroke.setButtonMask(InputEvent.BUTTON2_MASK);
        clipEditor.addMouseListener(stroke);
        stroke.addStrokeListener(main.getPluginHandler().getStrokeHandler());
    }

    // stroke
    protected UiStroke stroke;

    /**
     * ask the user by dialog to close this frame, returns true if it could close.
     */
    public boolean tryToClose() {
        Debug.println(3, "try to close " + getClipEditor().getClip().getName());
        // really close ?
        setState(Frame.NORMAL);
        if (GDialog.showYesNoQuestionDialog(this, GLanguage.translate("close"), new JLabel(GLanguage.translate("wantToCloseAudioClip?")))) {
            // save modifications ?
            if (getClipEditor().getClip().getHistory().hasUnsavedModifications()) {
                if (GDialog.showYesNoQuestionDialog(this, GLanguage.translate("save"), GLanguage.translate("wantToSaveModifications?"))) {
                    // save...
                    getPluginHandler().fileSave.start();
                }
            }

            // close...
            Debug.println(3, "close " + getClipEditor().getClip().getName());
            setVisible(false);
            clipEditor.destroy();
            main.removeClipFrame(this);
            return true;
        } else {
            return false;
        }
    }

    // links
    private GMain main;

    protected GPluginHandler getPluginHandler() {
        return main.getPluginHandler();
    }

    // frame counter
    private static int clipFrameCounter = 0;

    // clip editor
    protected GClipEditor clipEditor;

    /**
     * returns the clip-editor
     */
    public GClipEditor getClipEditor() {
        return clipEditor;
    }

    // window listener
    public void windowActivated(WindowEvent e) {
        main.setFocussedClipFrame(this);
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        tryToClose();
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

}
