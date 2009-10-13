/*
 * SetIPodSyncAction.java
 *
 * Created on 5. Juni 2003, 13:27
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.Playlist;
import de.axelwernicke.mypod.gui.GuiUtils;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;


/**
 *
 * @author  axelwe
 */
/** Export Playlist Action class
 */
public class SetIPodSyncAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public SetIPodSyncAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("syncWithIPod"));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("syncWithIPod"));
    }

    /** executes the SetIPodSyncAction
     *
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // determine playlist
            Playlist playlist = myPod.getSelectedPlaylist();

            // toggle state
            playlist.setIPodSync(!playlist.isIPodSync());

            // TODO: inform model about the change
        } catch (Exception e) {
            logger.warning("An action catched an exception : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // lock frontend
            myPod.getFrontend().setLocked(false);
            // cleanup
            System.gc();
        }

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
