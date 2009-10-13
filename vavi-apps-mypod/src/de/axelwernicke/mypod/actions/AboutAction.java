// myPod
// $Id: AboutAction.java,v 1.3 2003/08/03 09:45:28 axelwernicke Exp $
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

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import de.axelwernicke.mypod.SwingWorker;
import de.axelwernicke.mypod.myPod;
import de.axelwernicke.mypod.gui.AboutDialog;
import de.axelwernicke.mypod.gui.GuiUtils;
import de.axelwernicke.mypod.ipod.IPod;


/** About Action class
 *        <br>Action to show an about dialog.
 *        <br>type:        asynchroneously / blocking
 *
 * @author  axelwernicke@users.sourceforge.net
 */
public class AboutAction extends AbstractAction {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** constructs an action object
     * @param mode gui object the action is for
     */
    public AboutAction(int mode) {
        putValue(Action.NAME, GuiUtils.getStringLocalized("about..."));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resource/About16.gif")));
        putValue(Action.SHORT_DESCRIPTION, GuiUtils.getStringLocalized("about..."));
    }

    /** executes the AboutAction
     *        <br>open the dialog
     *        <br>fill in all the values determined from myPod and, if connected, iPod
     *
     * @param evt event
     */
    public void actionPerformed(ActionEvent evt) {
        logger.entering(this.getClass().getName(), "actionPerformed");

        myPod.getFrontend().setLocked(true);

        // do work in an separate thread
        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                AboutDialog aboutDialog = null;
                try {
                    // get information about the iPod
                    String iPodNotAvailable = GuiUtils.getStringLocalized("iPodNotAvailable");
                    de.axelwernicke.mypod.ipod.IPod iPod = myPod.getBackend().getIPod();
                    boolean iPodConnected = (iPod != null) && IPod.isConnected();

                    String iPodName = iPodConnected ? iPod.getName() : iPodNotAvailable;
                    String iPodVersion = iPodConnected ? iPod.getVersion() : iPodNotAvailable;
                    String iPodSerialNumber = iPodConnected ? iPod.getSerialNumber() : iPodNotAvailable;
                    String iPodDiscSpace = iPodConnected ? GuiUtils.formatFilesize(iPod.getDiscSpace()) : iPodNotAvailable;
                    String iPodFreeDiscSpace = iPodConnected ? GuiUtils.formatFilesize(iPod.getDiscSpaceFree()) : iPodNotAvailable;

                    // create about dialog an set bounds
                    aboutDialog = new AboutDialog(myPod.getFrontend(), true);
                    aboutDialog.setBounds(GuiUtils.calcCenteredBounds(myPod.getFrontend().getBounds(), aboutDialog.getBounds()));

                    // set myPod data
                    aboutDialog.versionContentLabel.setText(myPod.myPodVersion);
                    aboutDialog.totalClipsContentLabel.setText(String.valueOf(myPod.getBackend().getClipsTotalCount()));
                    aboutDialog.totalDurationContentLabel.setText(GuiUtils.formatTime(myPod.getBackend().getClipsTotalTime()) + " (hh:mm:ss)");
                    aboutDialog.totalSizeContentLabel.setText(GuiUtils.formatFilesize(myPod.getBackend().getClipsTotalSize()));

                    // set iPod data
                    aboutDialog.nameContentLabel.setText(iPodName);
                    aboutDialog.firmwareContentLabel.setText(iPodVersion);
                    aboutDialog.serialNumberContentLabel.setText(iPodSerialNumber);
                    aboutDialog.discSizeContentLabel.setText(iPodDiscSpace);
                    aboutDialog.spaceLeftContentLabel.setText(iPodFreeDiscSpace);

                    // show dialog
                    aboutDialog.setVisible(true);
                } catch (Exception e) {
                    logger.warning("An action catched an exception : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // unlock frontend
                    myPod.getFrontend().setLocked(false);

                    // dispose dialog and clean up 
                    if (aboutDialog != null) {
                        aboutDialog.dispose();
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
