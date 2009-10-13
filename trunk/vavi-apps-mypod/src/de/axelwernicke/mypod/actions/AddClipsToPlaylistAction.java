/*
 * AddClipsToPlaylistAction.java
 *
 * Created on 5. Juni 2003, 13:45
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.Playlist;
import de.axelwernicke.mypod.gui.GuiUtils;
import de.axelwernicke.mypod.gui.ListView;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;


/**
 *
 * @author  axelwe
 */
/**  Add Clips To Playlist Action
 */
public class AddClipsToPlaylistAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     * @param name of the action
     */
    public AddClipsToPlaylistAction(String name) {
        putValue(Action.NAME, name);
    }

    /** executes the AddClipsToPlaylistAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // get selected playlist
            Playlist playlist = myPod.getSelectedPlaylist();

            // get list view and table model
            ListView clipsTableView = myPod.getFrontend().getClipsTableView();

            // determine selected clips
            List<Long> oids = clipsTableView.getSelectedClips();

            //	show dialog to confirm action						
            if (oids.size() > 0) {
                //Playlist desPlaylist = null;
                int actionCommand = new Integer(evt.getActionCommand()).intValue();

                // get playlist to add clips to, or create a new plylist
                if (actionCommand < 0) {
                    // create new playlist
                    playlist = myPod.getBackend().createPlaylist(GuiUtils.getStringLocalized("newPlaylistName"));
                } else {
                    // get existing playlist
                    // TODO: take care, this was called in the backend before...
                    playlist = myPod.getPlaylist(actionCommand);
                }

                // add clips to the playlist
                playlist.addClips(oids);

                // clean up
                oids = null;
            }
        } catch (Exception e) {
            logger.warning("An action catched an exception : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // unlock frontend
            myPod.getFrontend().setLocked(false);
            // clean up
            System.gc();
        }

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
