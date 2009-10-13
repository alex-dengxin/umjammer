/*
 * ExportPlaylstAction.java
 *
 * Created on 5. Juni 2003, 13:22
 */
package de.axelwernicke.mypod.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import de.axelwernicke.mypod.Playlist;
import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.gui.GuiUtils;


/**
 *
 * @author  axelwe
 */
/** Export Playlist Action class
 */
public class ExportPlaylistAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public ExportPlaylistAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("export"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Export16.gif")));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("export"));
    }

    /** executes the ExportPlaylistAction
     *        <br>- determine selected playlist
     *        <br>- get playlist filename from dialog
     *        <br>- save playlist
     *
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // determine playlist
            Playlist playlist = myPod.getSelectedPlaylist();

            // get filename from dialog
            JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView());
            fc.setDialogTitle(GuiUtils.getStringLocalized("selectFileToExportPlaylist"));
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(false);
            fc.setCurrentDirectory(new File(myPod.getBackend().getPreferences().getRecentPlaylistPath()));
            int result = fc.showSaveDialog(myPod.getFrontend());

            if (result == JFileChooser.APPROVE_OPTION) {
                // save recent directory
                myPod.getBackend().getPreferences().setRecentPlaylistPath(fc.getCurrentDirectory().toString());

                // create playlist
                myPod.getBackend().savePlaylistM3U(fc.getSelectedFile().getPath(), playlist);
            }
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
