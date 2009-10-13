/*
 * ImportPlaylistAction.java
 *
 * Created on 5. Juni 2003, 13:18
 */
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.Playlist;
import de.axelwernicke.mypod.gui.GuiUtils;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;


/**
 *
 * @author  axelwe
 */
public class ImportPlaylistAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public ImportPlaylistAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("import"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Import16.gif")));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_P, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("import"));
    }

    /** executes the ImportPlaylistAction
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // lock frontend
        myPod.getFrontend().setLocked(true);

        try {
            // get filename from dialog
            JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView());
            fc.setDialogTitle(GuiUtils.getStringLocalized("selectFileToImportPlaylist"));
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(true);
            fc.setCurrentDirectory(new File(myPod.getBackend().getPreferences().getRecentPlaylistPath()));
            int fcResult = fc.showOpenDialog(myPod.getFrontend());

            if (fcResult == JFileChooser.APPROVE_OPTION) {
                File[] files = fc.getSelectedFiles();

                // update recent directory
                myPod.getBackend().getPreferences().setRecentPlaylistPath(fc.getCurrentDirectory().toString());

                // create playlist(s)
                Playlist playlist;
                for (int i = 0; i < files.length; i++) {
                    // import
                    playlist = myPod.getBackend().loadPlaylistM3U(files[i]);

                    // check if we have a playlist with this name already
                    Playlist existingPlaylist = myPod.getBackend().getPlaylist(playlist.getName());
                    if (existingPlaylist != null) {
                        // ask user whether to add or update the playlist
                        Object[] options = {
                                               GuiUtils.getStringLocalized("replace"),
                                               GuiUtils.getStringLocalized("add")
                        };
                        int result = JOptionPane.showOptionDialog(null, GuiUtils.getStringLocalized("existingPlaylistfound"), GuiUtils.getStringLocalized("importingPlaylist"), JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                        if (result == JOptionPane.YES_OPTION) {
                            // update playlist
                            existingPlaylist.replaceClips(playlist);
                        } else {
                            // add playlist to list of playlists
                            myPod.getBackend().addPlaylist(playlist);
                        }
                    } else {
                        // add playlist to list of playlists
                        myPod.getBackend().addPlaylist(playlist);
                    }
                }
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
