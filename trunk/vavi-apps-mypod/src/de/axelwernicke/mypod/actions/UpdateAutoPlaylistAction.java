/*
 * UpdateAutoPlaylistAction.java
 *
 * Created on 5. Juni 2003, 12:01
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.Playlist;
import de.axelwernicke.mypod.gui.GuiUtils;
import java.awt.event.ActionEvent;
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
public class UpdateAutoPlaylistAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public UpdateAutoPlaylistAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("update"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Refresh16.gif")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("update"));
    }

    /** executes the UpdateAutoPlaylistAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // determine index of the playlist to update
            Playlist playlist = myPod.getSelectedPlaylist();

            // just do it ...
            myPod.getBackend().updateAutoplaylist(playlist, null);

            // tell the model about added items, this helps even for non auto-playlists
            myPod.updateModels();

            // update status line
            if (myPod.getFrontend() != null) {
                GuiUtils.updateStatusLine(myPod.getFrontend());
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
