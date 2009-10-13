/*
 * RemoveClipsMyPodAction.java
 *
 * Created on 5. Juni 2003, 13:35
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.gui.GuiUtils;
import de.axelwernicke.mypod.gui.ListView;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


/** Delete Clips Action class.
 *        This action removes aclip from the data pool and all playlists
 */
public class RemoveClipsMyPodAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public RemoveClipsMyPodAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("removeFromMyPod"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Delete16.gif")));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("removeFromMyPod"));
    }

    /** executes the RemoveClipsMyPodAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // get list view and table model
            ListView clipsTableView = myPod.getFrontend().getClipsTableView();

            // determine the oid of the selected clips
            List<Long> oids = clipsTableView.getSelectedClips();

            //*	show dialog to confirm action						
            if (oids.size() > 0) {
                int result = JOptionPane.showConfirmDialog(null, GuiUtils.getStringLocalized("doYouWantToRemoveClips"), GuiUtils.getStringLocalized("confirmRemoveClips"), JOptionPane.YES_NO_OPTION);

                //*	remove clips from the data pool
                if (result == JOptionPane.YES_OPTION) {
                    // remove clips from data pool and all playlists
                    myPod.getBackend().removeClipsFromMyPod(oids);

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
