// myPod
// $Id: ReorganizeClipsAction.java,v 1.3 2003/08/03 08:53:46 axelwernicke Exp $
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

import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.SwingWorker;
import de.axelwernicke.mypod.gui.GuiUtils;
import de.axelwernicke.mypod.gui.ReorganizeClipsPrefsDialog;
import de.axelwernicke.mypod.gui.ReorganizeClipsProgressDialog;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;


/** Reorganize Clips Action class
 *        <br>Action to change directory structure and filenames according to some meta data of the files.
 *        <br>type:        asynchroneously / blocking
 *
 * @author  axelwernicke@users.sourceforge.net
 */
public class ReorganizeClipsAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public ReorganizeClipsAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("reorganizeClips..."));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/Dummy16.gif")));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("reorganizeClips..."));
    }

    /** executes the ReorganizeClipsAction.
     *        <br>open the action preferences dialog
     *        <br>reorganize clips
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        myPod.getFrontend().setLocked(true);

        // do work in an separate thread
        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                ReorganizeClipsPrefsDialog prefsDialog = null;
                ReorganizeClipsProgressDialog progressDialog = null;
                try {
                    prefsDialog = new ReorganizeClipsPrefsDialog(myPod.getFrontend(), true);
                    prefsDialog.setBounds(GuiUtils.calcCenteredBounds(myPod.getFrontend().getBounds(), prefsDialog.getBounds()));
                    prefsDialog.setVisible(true);

                    // check if confirmed
                    if (prefsDialog.isConfirmed()) {
                        prefsDialog.setVisible(false);

                        // show progress dialog
                        progressDialog = new ReorganizeClipsProgressDialog(this, myPod.getFrontend(), false);
                        progressDialog.setBounds(GuiUtils.calcCenteredBounds(myPod.getFrontend().getBounds(), progressDialog.getBounds()));
                        progressDialog.setVisible(true);

                        // reorganize clips
                        myPod.getBackend().reorganizeClips(progressDialog, prefsDialog.baseDirTextField.getText(), prefsDialog.dirStructComboBox.getSelectedIndex(), prefsDialog.filenameStructComboBox.getSelectedIndex(), prefsDialog.isRemoveEmptyDirectories());
                    }
                } catch (Exception e) {
                    logger.warning("An action catched an exception : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // dispose dialog and clean up 
                    if (progressDialog != null) {
                        progressDialog.dispose();
                    }
                    if (prefsDialog != null) {
                        prefsDialog.dispose();
                    }
                    myPod.getFrontend().setLocked(false);
                    System.gc();
                }

                return null;
            } // construct
        };

        worker.start();

        logger.exiting(this.getClass().getName(), "actionPerformed");
    }
}
