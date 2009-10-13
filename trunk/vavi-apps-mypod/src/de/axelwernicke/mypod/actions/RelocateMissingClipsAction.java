/*
 * RelocateMissingClipsAction.java
 *
 * Created on 5. Juni 2003, 19:24
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.SwingWorker;
import de.axelwernicke.mypod.gui.GuiUtils;
import de.axelwernicke.mypod.gui.ProgressDialog;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


/** Relocate Missing Clips Action class
 */
public class RelocateMissingClipsAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public RelocateMissingClipsAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("relocateMediaFiles"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Dummy16.gif")));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("relocateMediaFiles"));
    }

    /** executes the RelocateMissingClipsAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        // do work in an separate thread
        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                // create a new progress dialog
                ProgressDialog progressDialog = null;
                try {
                    progressDialog = new ProgressDialog(this, myPod.getFrontend(), false);
                    progressDialog.setBounds(GuiUtils.calcCenteredBounds(myPod.getFrontend().getBounds(), progressDialog.getBounds()));
                    progressDialog.setVisible(true);

                    // search for missing media files
                    List<Long> missingClips = myPod.getBackend().scanForMissingMediaFiles(progressDialog);

                    if ((missingClips != null) && !missingClips.isEmpty()) {
                        // get user confirmation for deletion
                        int result = JOptionPane.showConfirmDialog(progressDialog, GuiUtils.getStringLocalized("missingMediaFilesFound1") + " " + missingClips.size() + " " + GuiUtils.getStringLocalized("missingMediaFilesFound2") + '\n' + GuiUtils.getStringLocalized("doYouWantToRelocateClips"), GuiUtils.getStringLocalized("confirmRelocateClips"), JOptionPane.YES_NO_OPTION);

                        //	proceed, if user confirmed
                        if (result == JOptionPane.YES_OPTION) {
                            // relocate clips
                            myPod.getBackend().relocateClips(progressDialog, missingClips);

                            // inform the list views model about the changes
                            myPod.updateModels();
                        }
                    } else {
                        // inform user that no files are missing
                        JOptionPane.showMessageDialog(null, GuiUtils.getStringLocalized("noMissingClipsFound") + ".");
                    }

                    // hide and destroy progress dialog
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                } catch (Exception e) {
                    logger.warning("An action catched an exception : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // unlock frontend
                    myPod.getFrontend().setLocked(false);
                    // dispose dialog and clean up
                    if (progressDialog != null) {
                        progressDialog.dispose();
                    }
                    System.gc();
                }

                return null;
            } // construct
        };

        worker.start();

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
