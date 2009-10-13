/*
 * PreferencesAction.java
 *
 * Created on 5. Juni 2003, 07:12
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.Preferences;
import de.axelwernicke.mypod.gui.GuiUtils;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;


/**
 *
 * @author  axelwe
 */
public class PreferencesAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public PreferencesAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("preferences"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Preferences16.gif")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("myPodPreferences"));
    }

    /** executes the PreferencesAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // make a copy of the preferences
            Preferences prefsCopy = (Preferences) myPod.getBackend().getPreferences().clone();

            // create preferences dialog
            Preferences newPrefs = GuiUtils.showPreferencesDialog(myPod.getFrontend(), prefsCopy);
            myPod.getBackend().setPreferences(newPrefs);
        } catch (Exception e) {
            logger.warning("An action catched an exception : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // unlock frontend
            myPod.getFrontend().setLocked(false);
            // cleanup
            System.gc();
        }

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
