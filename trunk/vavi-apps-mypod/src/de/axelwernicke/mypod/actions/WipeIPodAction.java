/*
 * WipeIPodAction.java
 *
 * Created on 5. Juni 2003, 19:03
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.gui.GuiUtils;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


/** Wipe iPod Action class
 */
public class WipeIPodAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public WipeIPodAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("wipeIPod"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/DeleteIPod16.gif")));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("wipeIPod"));
    }

    /**
     * executes the WipeIPodAction
     * @param event for that action
     */
    @SuppressWarnings("static-access")
    public void actionPerformed(ActionEvent event) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // probe iPod until its found or action is aborted
            boolean iPodFound = false;
            int result = 0;
            do {
                iPodFound = myPod.getBackend().getIPod().isConnected();

                // inform user about missing ipod
                if (!iPodFound) {
                    // ask user to retry			
                    Object[] options = {
                        GuiUtils.getStringLocalized("retry"),
                        GuiUtils.getStringLocalized("abort")
                    };
                    result = JOptionPane.showOptionDialog(null, GuiUtils.getStringLocalized("makeIPodAvailableAsDrive"), GuiUtils.getStringLocalized("iPodNotAvailable"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                }
            } while (!iPodFound && (result == JOptionPane.YES_OPTION));

            // proceed if ipod available
            if (iPodFound) {
                // warn user before deleting anything...
                int confirmation = JOptionPane.showConfirmDialog(null, GuiUtils.getStringLocalized("doYouReallyWantToDeleteAllSongsAndPlaylistsFromYourIPod?"), GuiUtils.getStringLocalized("confirmDeleteAction"), JOptionPane.YES_NO_OPTION);

                // do it, if user confirmed
                if (confirmation == JOptionPane.YES_OPTION) {
                    myPod.getBackend().getIPod().wipe();
                }
            }
        } catch (Exception ex) {
            logger.warning("An action catched an exception : " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // unlock frontend & clean up
            myPod.getFrontend().setLocked(false);
            System.gc();
        }

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
