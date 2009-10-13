/*
 * RenamePlaylistActions.java
 *
 * Created on 5. Juni 2003, 12:12
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
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;


/** Rename Playlist Action class
 *
 * @author  axelwe
 */
public class RenamePlaylistAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public RenamePlaylistAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("rename..."));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Dummy16.gif")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("rename..."));
    }

    /** executes the RenamePlaylistAction
     *        <BR>- determine selected playlist
     *        <BR>- show input dialog
     *        <BR>- set new playlist name
     *
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        try {
            int index = myPod.getSelectedPlaylistIndex();
            Playlist playlist = myPod.getPlaylist(index);

            // ask user for new name...
            String newName = null;
            newName = JOptionPane.showInputDialog(myPod.getFrontend(), GuiUtils.getStringLocalized("enterNewPlaylistName"), playlist.getName());

            // check result
            if ((newName != null) && (newName.length() != 0)) {
                // ... and do it.
                playlist.setName(newName);
            }
        } catch (Exception e) {
            logger.warning("An action catched an exception : " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.gc();
        }

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
