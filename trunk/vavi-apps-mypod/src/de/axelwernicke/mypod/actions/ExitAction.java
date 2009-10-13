/*
 * ExportPlaylstAction.java
 *
 * Created on 5. Juni 2003, 13:22
 */
package de.axelwernicke.mypod.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.axelwernicke.mypod.ClipsTableColumnModel;
import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.gui.GuiUtils;


/**
 *
 * @author  axelwe
 */
/** ExitAction class
 */
public class ExitAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object */
    public ExitAction() {
        putValue(Action.NAME, GuiUtils.getStringLocalized("exit"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Dummy16.gif")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("exit"));
    }

    /** executes the Exit Action
     *
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // save gui configuration
        ((ClipsTableColumnModel) myPod.getFrontend().getClipsTableView().getColumnModel()).storeColumnPreferences();
        myPod.getBackend().setPreferences(myPod.getFrontend().getPreferences(myPod.getBackend().getPreferences()));

        // hide frontend
        myPod.getFrontend().setVisible(false);

        // updating playlists is pretty time consuming - we moved it out of the startup to this place
        myPod.getBackend().updateAllAutoplaylists();

        // make all data persistent
        myPod.getBackend().shutdown();

        // destroy frontend
        myPod.getFrontend().dispose();

        // exit
        System.exit(0);

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
