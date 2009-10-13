// PlaylistListView
// $Id: PlaylistListView.java,v 1.10 2003/07/20 06:46:17 axelwernicke Exp $
//
// Copyright (C) 2002-2003 Axel Wernicke <axel.wernicke@gmx.de>
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
package de.axelwernicke.mypod.gui;

import java.util.List;
import java.util.logging.Logger;

import de.axelwernicke.mypod.Playlist;


/**
 *
 * @author  axel wernicke
 */
public class PlaylistListView extends javax.swing.JList {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod.gui");
    List<Long> clips;

    /** Creates new form PlaylistListView */
    public PlaylistListView() {
        super();
        initComponents();
        this.setTransferHandler(new javax.swing.TransferHandler("clips"));
    }

    /**
     * @param oids
     */
    public void setClips(List<Long> oids) {
        logger.entering("PlaylistListView", "setClips()");

        logger.finer("DnD - got " + oids.size() + " clips to drop");

        // determine destination playlist
        Playlist playlist = (Playlist) this.getSelectedValue();

        while (!oids.isEmpty()) {
            Long oid = oids.remove(0);

            logger.finer("curr oid: " + oid + " is in " + playlist.getName() + " : " + playlist.containsClip(oid));

            if (!playlist.containsClip(oid)) {
                playlist.addClip(oid);
                logger.finer("DnD - set a clip");
            }
        }

        logger.exiting("PlaylistListView", "setClips()");
    }

    /**
     * @return  */
    public List<Long> getClips() {
        logger.entering("PlaylistListView", "getClips()");

        logger.exiting("PlaylistListView", "getClips()");

        return null;
    }

    private void initComponents() //GEN-BEGIN:initComponents
     {
        setCellRenderer(new de.axelwernicke.mypod.gui.PlaylistListViewCellRenderer());
    } //GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
