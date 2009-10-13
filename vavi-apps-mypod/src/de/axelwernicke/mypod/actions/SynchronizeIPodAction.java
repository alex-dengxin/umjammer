/*
 * SynchronizeIPodAction.java
 *
 * Created on 5. Juni 2003, 18:50
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.gui.IPodSyncDialog;
import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.SwingWorker;
import de.axelwernicke.mypod.gui.GuiUtils;
import de.axelwernicke.mypod.ipod.IPod;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


/** Synchronize iPod Action class<br><br>
 *
 *        This action should be executed synchroneously.<br>
 *        @author Axel Wernicke [axel.wernicke@gmx.de]
 */
public class SynchronizeIPodAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public SynchronizeIPodAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("synchronizeIPod"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/RefreshIPod16.gif")));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("synchronizeIPod"));
    }

    /** executes the SynchronizeIPodAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        // do work in an separate thread
        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                IPodSyncDialog dialog = null;
                try {
                    // show dialog, (synchronously) non modal
                    dialog = new IPodSyncDialog(this, myPod.getFrontend(), false);
                    dialog.setBounds(GuiUtils.calcCenteredBounds(myPod.getFrontend().getBounds(), dialog.getBounds()));
                    dialog.canelButton.setEnabled(false);
                    dialog.setVisible(true);

                    // probe iPod, until found or aborted
                    boolean iPodFound = false;
                    int result = 0;
                    do {
                        iPodFound = IPod.isConnected();

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

                    // proceed if iPod found
                    if (iPodFound) {
                        // disable frontend, enable cancel button
                        dialog.canelButton.setEnabled(true);

                        // do it with high priority
                        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                        long spaceLeft = myPod.getBackend().synchronizeIPod(dialog);
                        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

                        // enable frontend, disable cancel button
                        dialog.canelButton.setEnabled(false);

                        // log space on iPod
                        logger.info("space left on iPod : " + GuiUtils.formatFilesize(spaceLeft));

                        // inform user that not enough space on iPod
                        if (spaceLeft < 0) {
                            JOptionPane.showMessageDialog(null, GuiUtils.getStringLocalized("notEnoughSpaceOnIPod") + " ( " + GuiUtils.formatFilesize(spaceLeft) + " )", GuiUtils.getStringLocalized("couldNotSynchronize"), JOptionPane.ERROR_MESSAGE);
                        }
                    } // iPod found
                } catch (Exception e) {
                    logger.warning("An action catched an exception : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // unlock frontend
                    myPod.getFrontend().setLocked(false);

                    // hide dialog
                    dialog.dispose();
                    System.gc();
                }

                return null;
            } // construct
        };

        worker.start();

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
