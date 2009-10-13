/*
 * HelpAction.java
 *
 * Created on 5. Juni 2003, 19:57
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.SwingWorker;
import de.axelwernicke.mypod.gui.GuiUtils;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;


/** Help Action class
 */
public class HelpAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public HelpAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("help..."));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Help16.gif")));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("help..."));
    }

    /** executes the HelpAction
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
                    String anchor = ""; // show top of the document
                    GuiUtils.showHelpBrowser(anchor);
                } catch (Exception e) {
                    logger.warning("An action catched an exception : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // unlock frontend
                    myPod.getFrontend().setLocked(false);
                    // cleanup
                    System.gc();
                }

                return null;
            } // construct
        };

        worker.start();

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
