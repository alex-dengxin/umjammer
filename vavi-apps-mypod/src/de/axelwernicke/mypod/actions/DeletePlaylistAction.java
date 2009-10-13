/*
 * DeletePlaylistAction.java
 *
 * Created on 5. Juni 2003, 12:07
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.gui.GuiUtils;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;


/**
 *
 * @author  axelwe
 */
public class DeletePlaylistAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public DeletePlaylistAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("delete..."));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/DeleteFilter16.gif")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("deletePlaylist..."));
    }

    /** executes the DeletePlaylistAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        try {
            int index = myPod.getSelectedPlaylistIndex();

            if (index >= 0) {
                int result = JOptionPane.showConfirmDialog(null, GuiUtils.getStringLocalized("doYouWantToDeleteThisPlaylist"), GuiUtils.getStringLocalized("confirmRemovePlaylist"), JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    myPod.getBackend().removePlaylist(index);

                    // select next playlist if available
                    int newIndex = ((index < myPod.getPlaylistListModel().getSize()) ? index : (index - 1));
                    if (newIndex >= 0) {
                        myPod.getFrontend().getPlaylistListView().setSelectedIndex(newIndex);
                    }
                }
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
