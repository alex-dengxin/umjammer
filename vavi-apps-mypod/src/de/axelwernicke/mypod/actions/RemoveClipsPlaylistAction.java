/*
 * RemoveClipsPlaylistAction.java
 *
 * Created on 5. Juni 2003, 13:31
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.Playlist;
import de.axelwernicke.mypod.gui.GuiUtils;
import de.axelwernicke.mypod.gui.ListView;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;


/** Remove Clips Action class.
 *        removes a clip from the selected playlist. No clips are harmed by this action :)
 *
 *        determine selected clip(s)
 *        determine selected playlist
 *        show dialog to confirm action
 *        remove clips from the playlist
 */
public class RemoveClipsPlaylistAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public RemoveClipsPlaylistAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("removeFromPlaylist"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Remove16.gif")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("removeFromPlaylist"));
    }

    /** executes the RemoveClipsPlaylistAction
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

            //*	show dialog to confirm action						
            if (oids.size() > 0) {
                int result = JOptionPane.showConfirmDialog(null, GuiUtils.getStringLocalized("doYouWantToRemoveClipsFromPlaylist"), GuiUtils.getStringLocalized("confirmRemoveClips"), JOptionPane.YES_NO_OPTION);

                //*	remove clips from the playlist
                if (result == JOptionPane.YES_OPTION) {
                    myPod.getBackend().removeClipsFromPlaylist(playlist, oids);

                    // update current view
                    myPod.updateModels();
                }
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
