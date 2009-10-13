// ITunesDBPlaylistIndexItem
// $Id: ITunesDBPlaylistIndexItem.java,v 1.12 2003/07/26 07:06:54 axelwernicke Exp $
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
 * A Playlist Index object stores informations from the mhip tag.
 * 
 * <pre>
 *  mhip:
 *  0x0000  'mhip'      - tag
 *  0x0004  76          - tag size
 *  0x0008              - tag size - _not_ record size ?!?!
 *  0x000C  1           - ?? mhod records that follow ??
 *  0x0010  0           - unknown
 *  0x0014  0           - unknown // TODO - see dechdoc
 *  0x0018              - song index e.g. 2002
 *  0x0022              // TODO - see dechdoc
 *  0x0026              // TODO - see dechdoc
 * </pre>
 * 
 * @see techdoc for more details
 * @author axelwe
 */
class ITunesDBPlaylistIndexItem {
    /** tag size in bytes */
    private int tagSize = 76;

    /** item size - equals tag size ?? */
    private int recordSize = 76;

    private int unknown5 = 0;

    private int unknown6 = 0;

    /** song index */
    private int songIndex = -1;

    /** number of content records that follow */

    // done implicitly private int contentCount = -1;
    private List<ITunesDBContentItem> contentItems = null;

    ITunesDBPlaylistIndexItem() {
        contentItems = new ArrayList<ITunesDBContentItem>();
    }

    /**
     * @return
     */
    public String toString() {
        return new StringBuffer("[tagSize] ").append(tagSize).append('\t').append("[recordSize] ").append(recordSize).append('\t').append("[contentCount] ").append(this.contentItems.size()).append('\t').append("[unknown5] ").append(unknown5).append('\t').append("[unknown6] ").append(unknown6).append('\t').append("[songIndex] ").append(songIndex).toString();
    }

    /**
     * Getter for property tagSize.
     * 
     * @return Value of property tagSize.
     */
    public int getTagSize() {
        return tagSize;
    }

    /**
     * Setter for property tagSize.
     * 
     * @param tagSize New value of property tagSize.
     */
    public void setTagSize(int tagSize) {
        this.tagSize = tagSize;
    }

    /**
     * Getter for property recordSize.
     * 
     * @return Value of property recordSize.
     */
    public int getRecordSize() {
        return recordSize;
    }

    /**
     * Setter for property recordSize.
     * 
     * @param recordSize New value of property recordSize.
     */
    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    /**
     * Getter for property contentCount.
     * 
     * @return Value of property contentCount.
     * 
     */
    public int getContentCount() {
        return this.contentItems.size();
    }

    /**
     * Getter for property unknown5.
     * 
     * @return Value of property unknown5.
     * 
     */
    public int getUnknown5() {
        return unknown5;
    }

    /**
     * Setter for property unknown5.
     * 
     * @param unknown5 New value of property unknown5.
     * 
     */
    public void setUnknown5(int unknown5) {
        this.unknown5 = unknown5;
    }

    /**
     * Getter for property unknown6.
     * 
     * @return Value of property unknown6.
     * 
     */
    public int getUnknown6() {
        return unknown6;
    }

    /**
     * Setter for property unknown6.
     * 
     * @param unknown6 New value of property unknown6.
     * 
     */
    public void setUnknown6(int unknown6) {
        this.unknown6 = unknown6;
    }

    /**
     * Getter for property songIndex.
     * 
     * @return Value of property songIndex.
     */
    public int getSongIndex() {
        return songIndex;
    }

    /**
     * Setter for property songIndex.
     * 
     * @param songIndex New value of property songIndex.
     */
    public void setSongIndex(int songIndex) {
        this.songIndex = songIndex;
    }

    /**
     * Getter for property contentItems.
     * 
     * @return Value of property contentItems.
     */
    @SuppressWarnings("unused")
    private List<ITunesDBContentItem> getContentItems() {
        return contentItems;
    }

    /**
     * Setter for property contentItems.
     * 
     * @param contentItems New value of property contentItems.
     */
    @SuppressWarnings("unused")
    private void setContentItems(List<ITunesDBContentItem> contentItems) {
        this.contentItems = contentItems;
    }

    public ITunesDBContentItem getContentItem(int index) {
        return contentItems.get(index);
    }

    public void addContenItem(ITunesDBContentItem item) {
        this.contentItems.add(item);
    }

    public void removeContentItem(int index) {
        this.contentItems.remove(index);
    }
}

/* */
