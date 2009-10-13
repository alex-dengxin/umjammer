/*
 * NewAutoplaylistAction.java
 *
 * Created on 5. Juni 2003, 07:26
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
public class NewAutoPlaylistAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public NewAutoPlaylistAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("newAutoplaylist"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/NewFilter16.gif")));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("newAutoplaylist"));
    }

    /** executes the NewAutoPlaylistAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // create new default playlist
            Playlist playlist = myPod.getBackend().createAutoPlaylist(GuiUtils.getStringLocalized("newPlaylistName"));

            // create property dialog
            boolean confirmed = GuiUtils.showPlaylistPropertyDialog(playlist, myPod.getFrontend(), myPod.getBackend());

            if (confirmed) {
                // user confirmed dialog, so lets update the new playlist
                myPod.getBackend().updateAutoplaylist(playlist, null);

                // select new playlist
                myPod.getFrontend().getPlaylistListView().setSelectedValue(playlist, true);
            } else {
                // user cancelled dialog, so remove that playlist...
                myPod.getBackend().removePlaylist(playlist);
            }
        } catch (Exception e) {
            logger.warning("An action catched an exception : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // unlock frontend
            myPod.getFrontend().setLocked(false);
            // cleanup
            System.gc();
        }

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
