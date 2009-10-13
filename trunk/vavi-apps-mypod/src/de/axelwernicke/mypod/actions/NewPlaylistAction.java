/*
 * NewPlaylistAction.java
 *
 * Created on 5. Juni 2003, 07:19
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.Playlist;
import de.axelwernicke.mypod.gui.GuiUtils;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;


/**
 *
 * @author  axelwe
 */
public class NewPlaylistAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public NewPlaylistAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("new"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/New16.gif")));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("newPlaylist..."));
    }

    /** executes the NewPlaylistAction
     * @param evt executes the action
     */
    public void actionPerformed(ActionEvent evt) {
        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // create new playlist
            Playlist playlist = myPod.getBackend().createPlaylist(GuiUtils.getStringLocalized("newPlaylistName"));

            // select new playlist
            myPod.getFrontend().getPlaylistListView().setSelectedValue(playlist, true);
        } catch (Exception e) {
            logger.warning("An action catched an exception : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // unlock frontend
            myPod.getFrontend().setLocked(false);
            // cleanup
            System.gc();
        }
    }
}
