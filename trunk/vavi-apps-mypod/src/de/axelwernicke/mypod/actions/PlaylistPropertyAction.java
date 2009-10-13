/*
 * PlaylistPropertyAction.java
 *
 * Created on 5. Juni 2003, 13:00
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.Playlist;
import de.axelwernicke.mypod.gui.GuiUtils;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;


/**
 *
 * @author  axelwe
 */
public class PlaylistPropertyAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public PlaylistPropertyAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("properties"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Properties16.gif")));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_F2, 0));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("properties"));
    }

    /** executes the PlaylistPropertyAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // get playlist
            Playlist playlist = myPod.getSelectedPlaylist();

            // create property dialog
            boolean confirmed = GuiUtils.showPlaylistPropertyDialog(playlist, myPod.getFrontend(), myPod.getBackend());

            // update playlist, filter using all clips
            if (confirmed & playlist.isAutoplaylist()) {
                myPod.getBackend().updateAutoplaylist(playlist, null);
            }
        } catch (Exception e) {
            logger.warning("An action catched an exception : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // unlock frontend
            myPod.getFrontend().setLocked(false);
            //cleanup
            System.gc();
        }

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
