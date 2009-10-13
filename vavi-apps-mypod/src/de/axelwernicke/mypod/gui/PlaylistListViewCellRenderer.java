// PlaylistListViewCellRenderer
// $Id: PlaylistListViewCellRenderer.java,v 1.8 2003/02/03 19:06:59 axelwernicke Exp $
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

import javax.swing.ImageIcon;

import de.axelwernicke.mypod.Playlist;


/** Cellrenderer for the list of playlist.
 * Icons for filter and iPod sync are added to the name of the playlist.
 * @author axel wernicke
 */
class PlaylistListViewCellRenderer extends javax.swing.JLabel implements javax.swing.ListCellRenderer {
    /** empty icon */
    private final ImageIcon dummyIcon = new ImageIcon(this.getClass().getResource("/resource/dummy10x20.gif"));

    /** iPod sync icon */
    private final ImageIcon iPodIcon = new ImageIcon(this.getClass().getResource("/resource/iPod10x20.gif"));

    /** autoplaylist icon */
    private final ImageIcon filterIcon = new ImageIcon(this.getClass().getResource("/resource/filter10x20.gif"));

    /** iPod sync and autoplaylist icon */
    private final ImageIcon iPodFilterIcon = new ImageIcon(this.getClass().getResource("/resource/iPodFilter10x20.gif"));

    /** Here all the work is done. The status of the playlist is determined and
     * the correct icon is added to the label.
     *
     * @param list list object
     * @param value Playlist object
     * @param index index of value in the playlist
     * @param isSelected status of the list item
     * @param cellHasFocus status of the list item
     * @return label with icon and name of the playlist
     */
    public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String s = value.toString();
        setText(s);

        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

        if (((Playlist) value).isIPodSync() && ((Playlist) value).isAutoplaylist()) {
            setIcon(iPodFilterIcon);
        } else if (((Playlist) value).isIPodSync()) {
            setIcon(iPodIcon);
        } else if (((Playlist) value).isAutoplaylist()) {
            setIcon(filterIcon);
        } else {
            setIcon(dummyIcon);
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);

        return this;
    }
}
