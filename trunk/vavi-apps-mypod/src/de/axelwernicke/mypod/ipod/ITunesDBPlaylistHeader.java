// ITunesDBPlaylistHeader
// $Id: ITunesDBPlaylistHeader.java,v 1.11 2003/07/26 07:06:54 axelwernicke Exp $
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

package de.axelwernicke.mypod.ipod;

import java.util.ArrayList;
import java.util.List;


/**
 * A PlaylistHeader object stores informations from the mhlp tag.
 *
 * </pre>
 *  mhlp:
 *  0x0000  'mhlp'      - tag
 *  0x0004              - tag size
 *  0x0008              - number of playlist items in there
 * </pre>
 *
 * @see techdoc for more details
 * @author axelwe
 */
public class ITunesDBPlaylistHeader {
    /** tag size in bytes */
    private int tagSize = 92;

    /** number of playlsit items */
//  private int playlistCount = -1;

    /** list of song items */
    private List<ITunesDBPlaylistItem> playlistItems = null;

    ITunesDBPlaylistHeader() {
        playlistItems = new ArrayList<ITunesDBPlaylistItem>();
    }

    /**
     * @return
     */
    public String toString() {
        return new StringBuffer("[tag size] ").append(tagSize).append('\t').append("[playlistCount] ").append(getPlaylistCount()).toString();
    }

    /** Getter for property tagSize.
     * @return Value of property tagSize.
     *
     */
    public int getTagSize() {
        return tagSize;
    }

    /** Setter for property tagSize.
     * @param tagSize New value of property tagSize.
     *
     */
    public void setTagSize(int tagSize) {
        this.tagSize = tagSize;
    }

    /** Getter for property playlistCount.
     * @return Value of property playlistCount.
     *
     */
    public int getPlaylistCount() {
        return this.playlistItems.size();
    }

    /** Getter for property playlistItems.
     * @return Value of property playlistItems.
     *
     */
    @SuppressWarnings("unused")
    private java.util.List<ITunesDBPlaylistItem> getPlaylistItems() {
        return playlistItems;
    }

    /** Setter for property playlistItems.
     * @param playlistItems New value of property playlistItems.
     *
     */
    @SuppressWarnings("unused")
    private void setPlaylistItems(java.util.List<ITunesDBPlaylistItem> playlistItems) {
        this.playlistItems = playlistItems;
    }

    public void addPlaylist(ITunesDBPlaylistItem playlistItem) {
        this.playlistItems.add(playlistItem);
    }

    public ITunesDBPlaylistItem getPlaylist(int index) {
        return this.playlistItems.get(index);
    }

    public void removePlaylist(int index) {
        this.playlistItems.remove(index);
    }

    public void removePlaylist(ITunesDBPlaylistItem playlistItem) {
        this.playlistItems.remove(playlistItem);
    }

    public boolean containsPlaylist(ITunesDBPlaylistItem playlistItem) {
        return this.playlistItems.contains(playlistItem);
    }
}

/* */
