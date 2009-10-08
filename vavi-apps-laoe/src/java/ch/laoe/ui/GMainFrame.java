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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import ch.oli4.ui.UiPersistanceEvent;
import ch.oli4.ui.UiPersistanceListener;


/**
 * frame with the main commands.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 27.08.00 erster Entwurf oli4
 */
public class GMainFrame extends JFrame implements WindowListener, UiPersistanceListener {
    /**
     * constructor
     */
    public GMainFrame(GMain g) {
        Debug.println(3, "create main-frame");
        main = g;
        setSize(new Dimension(145, 245));
        URL u = getClass().getResource("resources/laoe.gif");
        if (u != null) {
            setIconImage(new ImageIcon(u).getImage());
        }
        setTitle(GLanguage.translate("laoe"));
        buttonPanel = new JPanel();
        getContentPane().add(buttonPanel);

        persistance = GPersistance.createPersistance();
        persistance.addPersistanceListener(this);

        setLocation(persistance.getInt("mainFrame.frameLocation.x"), persistance.getInt("mainFrame.frameLocation.y"));
        setVisible(false);

        // close
        addWindowListener(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    // persistance listener

    protected GPersistance persistance;

    /**
     * do not use "on restore", each class is responsible of its own restoring, e.g. at construction time.
     */
    public final void onRestore(UiPersistanceEvent e) {
    }

    public void onBackup(UiPersistanceEvent e) {
        persistance.setInt("mainFrame.frameLocation.x", getLocation().x);
        persistance.setInt("mainFrame.frameLocation.y", getLocation().y);
    }

    /**
     * ask the user by dialog to close the whole program, returns true if it could close.
     */
    public void tryToExit() {
        Debug.println(3, "try to exit");
        // really close ?
        /*
         * if ( GDialog.showYesNoQuestionDialog( this, GLanguage.translate("exit"), GLanguage.translate("wantToExit?")) )
         */
        {
            boolean exitAllowed = true;

            // first close all clip-frames...
            Object o[] = main.getAllClipFrames();
            for (int i = 0; i < o.length; i++) {
                // close NOT allowed by user ?
                if (((GClipFrame) o[i]).tryToClose() == false) {
                    exitAllowed = false;
                    break;
                }
            }

            // all clip-frames closed ?
            if (exitAllowed) {
                // exit...
                Debug.println(3, "backup persistent objects");
                GPersistance.createPersistance().backup();
                setVisible(false);
                Debug.println(3, "exit LAoE now");
                System.exit(0);
            }
        }
    }

    // links
    private GMain main;

    /**
     * set the menubar
     */
    public void setMenuBar(JMenuBar m) {
        setJMenuBar(m);
        validate();
    }

    // button manager
    private JPanel buttonPanel;

    /**
     * adds a button
     */
    public void addButton(JButton b) {
        buttonPanel.add(b);
        validate();
    }

    // window listener
    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        tryToExit();
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
        main.deiconifyAllFrames();
    }

    public void windowIconified(WindowEvent e) {
        main.iconifyAllFrames();
    }

    public void windowOpened(WindowEvent e) {
    }

}
