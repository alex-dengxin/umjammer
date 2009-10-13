// myPod
// $Id: ScanAction.java,v 1.4 2003/08/03 08:48:54 axelwernicke Exp $
//
// Copyright (C) 2002-2003 Axel Wernicke <axelwernicke@users.sourceforge.net>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
package de.axelwernicke.mypod.actions;

import de.axelwernicke.mypod.SwingWorker;
import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.gui.GuiUtils;
import de.axelwernicke.mypod.gui.ScanDialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;


/** Scan Action class.
 *        <br>Action to scan media files and extract lots of information.
 *        <br>type:        asynchroneously / blocking
 *
 * @author  axelwernicke@users.sourceforge.net
 */
public class ScanAction extends AbstractAction {
    /** jdk 1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** default constructor
     *
     * @param mode gui object the action is for
     */
    public ScanAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("ScanDiscForMedia..."));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("ScanDiscForMedia..."));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Eject16.gif")));
    }

    /** executes the ScanAction.
     *
     *        <br>open filechooser
     *        <br>get choosen files and dirs
     *        <br>scan them for meta data
     *        <br>update all playlists and models
     *
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        // do work in an separate thread
        SwingWorker worker = new SwingWorker() {
            JFileChooser fc = new JFileChooser();

            public Object construct() {
                // lock frontend
                myPod.getFrontend().setLocked(true);

                try {
                    // open file dialog
                    fc.setDialogTitle(GuiUtils.getStringLocalized("selectDirectory2Scan"));
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fc.setMultiSelectionEnabled(true);
                    fc.setCurrentDirectory(new File(myPod.getBackend().getPreferences().getRecentScanPath()));
                    int result = fc.showOpenDialog(myPod.getFrontend());

                    // unlock frontend
                    //myPod.getFrontend().setLocked(false);
                    // get the files and directories
                    File[] files;
                    if (result == JFileChooser.APPROVE_OPTION) {
                        // set recent path
                        myPod.getBackend().getPreferences().setRecentScanPath(fc.getCurrentDirectory().toString());

                        // get selected files & dirs
                        files = fc.getSelectedFiles();
                        if (files != null) {
                            // show non modal progress dialog
                            ScanDialog dialog = new ScanDialog(this, myPod.getFrontend(), false);
                            dialog.pack();
                            dialog.setBounds(GuiUtils.calcCenteredBounds(myPod.getFrontend().getBounds(), dialog.getBounds()));
                            dialog.setVisible(true);

                            // just do it
                            int cnt = myPod.getBackend().scanFiles(files, dialog);

                            // update all autoplaylists if some clips were added or updated
                            if (cnt > 0) {
                                // do some gui stuff
                                dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("resource/language", "updatingAutoplaylists..."));
                                dialog.totalProgressBar.setIndeterminate(true);
                                dialog.fileNameContentLabel.setText("");
                                myPod.getBackend().updateAllAutoplaylists();

                                // tell the models about added items, this helps even for non auto-playlists
                                myPod.updateModels();
                            }

                            // hide progress dialog
                            dialog.dispose();
                            dialog = null;
                        }
                    }
                } catch (Exception e) {
                    logger.warning("An action catched an exception : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // unlock frontend
                    myPod.getFrontend().setLocked(false);
                    // initialize memory clean up
                    System.gc();
                }

                return null;
            } // construct
        };

        worker.start();

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
