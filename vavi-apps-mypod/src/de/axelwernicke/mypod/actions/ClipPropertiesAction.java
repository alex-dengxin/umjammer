/*
 * ClipPropertiesAction.java
 *
 * Created on 5. Juni 2003, 13:59
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.MP3Meta;
import de.axelwernicke.mypod.gui.ProgressDialog;
import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.SwingWorker;
import de.axelwernicke.mypod.gui.GuiUtils;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;


/** Clips Property Action class.
 *
 *        determine selected clip(s)
 *        determine selected playlist
 *        show dialog to confirm action
 *        remove clips from the playlist
 */
public class ClipPropertiesAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public ClipPropertiesAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("properties..."));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Properties16.gif")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F9, InputEvent.CTRL_MASK));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("properties..."));
    }

    /** executes the ClipPropertiesAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        // do work in an separate thread
        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                try {
                    // get selected clips
                    List<Long> oids = GuiUtils.getSelectedClips(myPod.getFrontend());

                    //	show properties dialog for selected clips						
                    if (oids.size() > 0) {
                        MP3Meta meta = GuiUtils.showClipPropertiesDialog(myPod.getFrontend(), oids);

                        // if meta is not null, the dialog was confirmed by the user
                        if (meta != null) {
                            ProgressDialog progressDialog = new ProgressDialog(this, myPod.getFrontend(), false);
                            progressDialog.setBounds(GuiUtils.calcCenteredBounds(myPod.getFrontend().getBounds(), progressDialog.getBounds()));
                            progressDialog.setStatusText(GuiUtils.getStringLocalized("updatingID3Tags"));
                            progressDialog.setVisible(true);

                            // lower thread priority, this helps to end the application in the case the update hangs
                            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

                            // just do it
                            myPod.getBackend().setClipsProperties(progressDialog, meta, oids);

                            // set thread priority back to normal
                            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

                            progressDialog.setVisible(false);
                            progressDialog.dispose();

                            // tell the model about added items, this helps even for non auto-playlists
                            myPod.updateModels();
                        }
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

                return null;
            } // construct
        };

        worker.start();

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
