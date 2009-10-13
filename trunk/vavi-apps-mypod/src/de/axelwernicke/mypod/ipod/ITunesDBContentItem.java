// ITunesDBContentItem
// $Id: ITunesDBContentItem.java,v 1.15 2003/07/25 19:26:15 axelwernicke Exp $
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
import java.util.logging.Logger;


/** A SongContent object stores the informations from the 'mhod' tag.
 *
 * </PRE>
 *        mhod:<br>
 *        0x0000 'mhod' - tag<br>
 *        0x0004 24     - tag size<br>
 *        0x0008        - item size<br>
 *        0x000C        - typ that specifies the content<br>
 *                        0 - ??<br>
 *                        1 - titel ( song or playlist )<br>
 *                        2 - path<br>
 *                        3 - album<br>
 *                        4 - artist<br>
 *                        5 - genre<br>
 *                'MPEG audio file'        6 - filetype<br>
 *                                         7 - equalizer settings<br>
 *                                         8 - comment<br>
 *                                         100 - playlist entry<br>
 *        0x0010        0                                                        - unknown<br>
 *        0x0014        0                                                        - unknown<br>
 *        0x0018        1                                                        - position in playlist ( first is 1 )<br>
 *        0x001C                                                                - content size<br>
 *        0x0020        0                                                        - unknown<br>
 *        0x0024        0                                                        - unknown<br>
 *        0x0028                                                                - content as unicode string<br>
 * </PRE>
 *
 * @see techdoc for more details
 * @author axelwe
 */
public class ITunesDBContentItem {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod.ipod");

    /** content type identifier : content item represents a title */
    public static final int TITEL = 1;

    /** content type identifier : content item represents a file path */
    public static final int PATH = 2;

    /** content type identifier : content item represents an album */
    public static final int ALBUM = 3;

    /** content type identifier : content item represents an artist */
    public static final int ARTIST = 4;

    /** content type identifier : content item represents a genre */
    public static final int GENRE = 5;

    /** content type identifier : content item represents a filetype */
    public static final int FILETYPE = 6;

    /** content type identifier : content item represents an equalizer profile */
    public static final int EQUALIZERPROFILE = 7;

    /** content type identifier : content item represents a comment */
    public static final int COMMENT = 8;

    /** content type identifier : content item represents a playlistentry */
    public static final int PLAYLISTENTRY = 100;

    /** tag size in bytes */
    private int tagSize = 24;

    /** item size in bytes mysteriously, but tag size is in hex, but only for this item typ :? */
    private int recordSize = 40;

    /** content type */
    private int contentTyp = -1;

    /** unknown */
    private int unknown5 = 0;

    /** unknown */
    private int unknown6 = 0;

    /** position in playlist */
    private int listPosition = -1;

    /** content size */
    private int contentSize = 0;

    /** unknown */
    private int unknown9 = 0;

    /** unknown */
    private int unknown10 = 0;

    /** content itself is a UTF 16 String */
    private byte[] content = null;

    /** Default constructor for a content item.         */
    ITunesDBContentItem() {
    }

    /** Constructor for a filled content item.
     * @param _contentTyp of the item
     * @param _listPosition of the item
     * @param _content of the item
     */
    ITunesDBContentItem(int _contentTyp, int _listPosition, byte[] _content) {
        this.setContentTyp(_contentTyp);
        this.setListPosition(_listPosition);
        this.setContent(_content);
    }
    ;

    /** Getter for property tagSize.
     * @return Value of property tagSize.
     */
    public int getTagSize() {
        return tagSize;
    }

    /** Setter for property tagSize.
     * @param tagSize New value of property tagSize.
     */
    @SuppressWarnings("unused")
    private void _setTagSize(int tagSize) {
        this.tagSize = tagSize;
    }

    /** Calculates the _real_ size of the object.
     *        The object is encoded into a byte array to determine size.
     *@return real size of the record
     */
    int calculateRecordSize() {
        int size = 0;

        try {
            ByteArrayOutputStream bau = new ByteArrayOutputStream();

            ITunesDBParser.encodeContentItem(bau, this);
            bau.flush();
            size = bau.size();
            bau.close();
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        return size;
    }

    /** Getter for property recordSize.
     * @return Value of property recordSize.
     */
    public int getRecordSize() {
        return recordSize;
    }

    /** Setter for property recordSize.
     * @param recordSize New value of property recordSize.
     */
    @SuppressWarnings("unused")
    private void _setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    /** Getter for property contentTyp.
     * @return Value of property contentTyp.
     */
    public int getContentTyp() {
        return contentTyp;
    }

    /** Setter for property contentTyp.
     * @param contentTyp New value of property contentTyp.
     */
    public void setContentTyp(int contentTyp) {
        this.contentTyp = contentTyp;
    }

    /** Getter for property unknown5.
     * @return Value of property unknown5.
     */
    public int getUnknown5() {
        return unknown5;
    }

    /** Setter for property unknown5.
     * @param unknown5 New value of property unknown5.
     */
    public void setUnknown5(int unknown5) {
        this.unknown5 = unknown5;
    }

    /** Getter for property unknown6.
     * @return Value of property unknown6.
     */
    public int getUnknown6() {
        return unknown6;
    }

    /** Setter for property unknown6.
     * @param unknown6 New value of property unknown6.
     */
    public void setUnknown6(int unknown6) {
        this.unknown6 = unknown6;
    }

    /** Getter for property listPosition.
     * @return Value of property listPosition.
     */
    public int getListPosition() {
        return listPosition;
    }

    /** Setter for property listPosition.
     * @param listPosition New value of property listPosition.
     */
    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    /** Getter for property contentSize.
     * @return Value of property contentSize.
     */
    public int getContentSize() {
        return contentSize;
    }

    /** Setter for property contentSize.
     * @param contentSize New value of property contentSize.
     */
    public void setContentSize(int contentSize) {
        this.contentSize = contentSize;
    }

    /** Getter for property unknown9.
     * @return Value of property unknown9.
     */
    public int getUnknown9() {
        return unknown9;
    }

    /** Setter for property unknown9.
     * @param unknown9 New value of property unknown9.
     */
    public void setUnknown9(int unknown9) {
        this.unknown9 = unknown9;
    }

    /** Getter for property unknown10.
     * @return Value of property unknown10.
     */
    public int getUnknown10() {
        return unknown10;
    }

    /** Setter for property unknown10.
     * @param unknown10 New value of property unknown10.
     */
    public void setUnknown10(int unknown10) {
        this.unknown10 = unknown10;
    }

    /** Getter for property content.
     * @return Value of property content.
     */
    public byte[] getContent() {
        return content;
    }

    /** Setter for property content.
     * @param value as byte array
     */
    public void setContent(byte[] value) {
        try {
            // save old size
            int oldContentSize = contentSize;

            // set new content
            content = value;
            contentSize = content.length;

            // revalidate record size
            recordSize = recordSize - oldContentSize + contentSize;

            // do some logging
            //logger.finest("old content size: " + oldContentSize);
            //logger.finest("new content size: " + contentSize);
        } catch (Exception e) {
            logger.warning("exception raised: " + e.getMessage());
        }
    }

    /** Gets the content of the tag as string.
     * @return content as string
     */
    public String getContentAsString() {
        return ITunesDBParser.uTF16LittleEndianToString(content);
    }

    /** Gets the tag with all its members as formatted string
     * @return formatted string containing all members of the object
     */
    public String toString() {
        return new StringBuffer("[tag size] ").append(tagSize).append('\t').append("[recordSize] ").append(recordSize).append('\t').append("[contentTyp] ").append(contentTyp).append('\t').append("[unknown5] ").append(unknown5).append('\t').append("[unknown6] ").append(unknown6).append('\t').append("[listPosition] ").append(listPosition).append('\t').append("[contentSize] ").append(contentSize).append('\t').append("[unknown9] ").append(unknown9).append('\t').append("[unknown10] ").append(unknown10).append('\t').append("[content] ").append(getContentAsString()).toString();
    }
} //class song content
