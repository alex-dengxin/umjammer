// ITunesDBListHolder
// $Id: ITunesDBListHolder.java,v 1.13 2003/07/25 19:26:15 axelwernicke Exp $
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

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A ListHolder object stores the informations from the 'mhsd' tag. It has
 * references to the lists contained.
 *
 * </PRE>
 *        mhsd:<br>
 *        x0000 'mhsd' - tag<br>
 *        x0004        - tag size 96<br>
 *        x0008        - list size<br>
 *        x0012        - typ: 1 = songlist, 2 = playlist<br>
 * </PRE>
 *
 * @see techdoc for more details
 * @author axelwe
 */
class ITunesDBListHolder {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod.ipod");

    /** songlist */
    public static final int SONGLIST = 1;

    /** Playlist */
    public static final int PLAYLIST = 2;

    /** tag size in bytes */
    private int tagSize = 96;

    /** list size in bytes */
    private int recordSize = 96;

    /** list type */
    private int listType = -1;

    /** Constructor */
    ITunesDBListHolder() {
    }

    /**
     * Calculates the _real_ size of the object.
     * The object is encoded into a byte array to determine size.
     *
     * @return real size of the record
     */
    long calculateRecordSize() {
        long size = 0;

        try {
            ByteArrayOutputStream bau = new ByteArrayOutputStream();

            ITunesDBParser.encodeListHolder(bau, this);
            bau.flush();
            size = bau.size();
            bau.close();
        } catch (Exception e) {
        }

        return size;
    }

    /**
     * puts the listholder to a string.
     * @return string representation
     */
    public String toString() {
        return new StringBuffer("[tag size] ").append(tagSize).append('\t').append("[recordSize] ").append(recordSize).append('\t').append("[listType] ").append(listType).toString();
    }

    /**
     * Getter for property tagSize.
     * @return Value of property tagSize.
     */
    public int getTagSize() {
logger.log(Level.INFO, "tagSize: " + tagSize);
        return tagSize;
    }

    /**
     * Setter for property tagSize.
     * @param tagSize New value of property tagSize.
     */
    public void setTagSize(int tagSize) {
        this.tagSize = tagSize;
    }

    /**
     * Getter for property recordSize.
     * @return Value of property recordSize.
     */
    public int getRecordSize() {
        return recordSize;
    }

    /**
     * Setter for property recordSize.
     * @param recordSize New value of property recordSize.
     */
    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    /**
     * Getter for property listType.
     * @return Value of property listType.
     */
    public int getListType() {
        return listType;
    }

    /** Setter for property listType.
     * @param listType New value of property listType.
     *
     */
    public void setListType(int listType) {
        this.listType = listType;
    }
}
