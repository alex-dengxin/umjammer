// ITunesDBPlaylistItem
// $Id: ITunesDBPlaylistItem.java,v 1.14 2003/07/20 11:35:38 axelwernicke Exp $
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


/** A PlaylistItem object stores informations from the mhyp tag.<BR>
 *        It represents a single playlist. Some meta information are stored in two mhod records which
 *        this record is followed by. The first playlist item has the name of the ipod attached in the
 *        mhods and contains a list of all songs. For others the name of the playlist is stored in the
 *        mhod records.
 *
 *        <pre>
 *        <BR>mhyp:
 *        <BR>0x0000        'mhyp'        - tag
 *        <BR>0x0004                                        - tag size
 *        <BR>0x0008                                        - size of the complete playlist in bytes
 *        <BR>0x000C        2                                - ?? number of mhod records following ??
 *        <BR>0x0010                                        - number of mhip/mhod pairs in the playlist
 *        <BR>0x0014        0/1                        - 1 for masterplaylist, 0 else
 *        <BR>0x0018                                        // TODO - see dechdoc
 *        <BR>0x0022                                        // TODO - see dechdoc
 *        <BR>0x0026                                        // TODO - see dechdoc
 *        </pre>
 * @see techdoc for more details
 * @author axelwe
 */
public class ITunesDBPlaylistItem {
    /** tag size in bytes */
    private int tagSize = 108;

    /** listSize in bytes */
    private int recordSize = 108;

    /** number of mhod records attached */
    private int contentCount = -1;

    /** number of songs in the playlist */
    private int songCount = -1;

    /** type of list */
    private int listType = -1;

    /** list of songs */
    private List<Object> songItems = null;

    /** Constructor
     */
    ITunesDBPlaylistItem() {
        contentCount = 0;
        songCount = 0;
        songItems = new ArrayList<Object>();
    }

    /** Adds a clip to a playlist
     *
     * @param fileIndex of the clip to add
     * @return record size of the added index + content items
     */
    int addClip(int fileIndex) {
        byte[] tweak = { 0x00, 0x00, 0x00, 0x00 };

        // create new index item for the file index
        ITunesDBPlaylistIndexItem indexItem = new ITunesDBPlaylistIndexItem();

        // create new content item
        ITunesDBContentItem contentItem = new ITunesDBContentItem(ITunesDBContentItem.PLAYLISTENTRY, //this.songCount + 1,
        this.songCount, tweak);

        // for some reason the size must _not_ be corrected ...
        contentItem.setContentSize(0);

        // set Song index
        indexItem.setSongIndex(fileIndex);
        indexItem.addContenItem(contentItem);

        this.songItems.add(indexItem);
        this.songCount++;

        // revalidate size
        // record size of an index item does _not_ contain size of attached content items !
        // indexItem.recordSize += contentItem.getRecordSize();
        this.recordSize += (indexItem.getRecordSize() + contentItem.getRecordSize());

        return (indexItem.getRecordSize() + contentItem.getRecordSize());
    }

    /** Removes a clip from a playlist
     *
     * @param fileIndex index of the clip to remove
     * @return size of the removed index & content records
     */
    long removeClip(long fileIndex) {
        long sizeRemoved = 0;

        ITunesDBContentItem content = null;
        Object indexItem = null;

        // find clip in the playlist
        for (int i = 0; i < songItems.size(); i++) {
            indexItem = songItems.get(i);

            // since at least the first two enties are mhod, we have to check what we got
            if (indexItem instanceof de.axelwernicke.mypod.ipod.ITunesDBPlaylistIndexItem) {
                ITunesDBPlaylistIndexItem playlistIndexItem = (ITunesDBPlaylistIndexItem) indexItem;
                if (playlistIndexItem.getSongIndex() == fileIndex) {
                    // we found the clip, so determine the _complete_ size - the mhod(s) sizes are not counted in the
                    // in the index items size field...
                    for (int cc = 0; cc < playlistIndexItem.getContentCount();
                         cc++) {
                        content = playlistIndexItem.getContentItem(cc);
                        sizeRemoved += content.getRecordSize();
                    }

                    // save item size
                    sizeRemoved += playlistIndexItem.getRecordSize();

                    // remove clip
                    songItems.remove(i);
                }
            }
        }

        // revalidate song order
        validateClips();

        // revalidate playlist size
        this.recordSize -= sizeRemoved;

        return sizeRemoved;
    }

    /** Validates clip related data in a playlist.
     *        positions of a clip are set in the mhod record
     *        total song count is set correctly
     */
    void validateClips() {
        int cnt = 0;
//      boolean found;

        ITunesDBContentItem content = null;
        Object indexItem = null;

        // determine clip count and correct songs playlist position
        int songItemCount = songItems.size();
        for (int i = 0; i < songItemCount; i++) {
            indexItem = songItems.get(i);

            // since at least the first two enties are hod, we have to check what we got
            if (indexItem instanceof de.axelwernicke.mypod.ipod.ITunesDBPlaylistIndexItem) {
                // increment song count
                cnt++;

                // find content entry with playlist position
//              found = false;
                int contentItemCount = ((ITunesDBPlaylistIndexItem) indexItem).getContentCount();
                for (int index = 0; index < contentItemCount; index++) {
                    content = ((ITunesDBPlaylistIndexItem) indexItem).getContentItem(index);
                    if (content.getContentTyp() == ITunesDBContentItem.PLAYLISTENTRY) {
//                      found = true;

                        // set position of the song in the playlist
                        content.setListPosition(cnt);
                    }
                }
            }
        }

        // correct clip count
        this.songCount = cnt;
    }

    /**
     * Gets the name of the playlist
     *
     * @return name as string
     */
    String getName() {
        String name = null;

        try {
            if (this.contentCount > 0) {
                // the first songItems element contains the name of the playlist 
                name = ITunesDBParser.uTF16LittleEndianToString(((ITunesDBContentItem) songItems.get(0)).getContent());
            }
        } catch (Exception e) {
            // TODO logging !
            e.printStackTrace();
        }

        return name;
    }

    /**
     * Sets the name of a playlist.
     *
     * @param name to set
     */
    void setName(String name) {
        if (this.contentCount == 0) { // TODO what happens if playlist contains already some items ( and a name )
        
            // create content item for playlist name, and add it twice
            ITunesDBContentItem content = new ITunesDBContentItem(ITunesDBContentItem.TITEL, 1, ITunesDBParser.stringToUTF16LittleEndian(name));
            this.songItems.add(content);
            this.songItems.add(content);

            // revalidate size
            this.contentCount += 2;
            this.recordSize += (2 * content.getRecordSize());
        }
    }

    /**
     * Visualizes the content of an playlist item object
     *
     * @return  content of the object
     */
    public String toString() {
        return new StringBuffer("[tagSize] ").append(tagSize).append('\t').append("[recordSize] ").append(recordSize).append('\t').append("[contentCount] ").append(contentCount).append('\t').append("[songCount] ").append(songCount).append('\t').append("[listType] ").append(listType).toString();
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

    /** Getter for property recordSize.
     * @return Value of property recordSize.
     *
     */
    public int getRecordSize() {
        return recordSize;
    }

    /** Setter for property recordSize.
     * @param recordSize New value of property recordSize.
     *
     */
    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    /** Getter for property contentCount.
     * @return Value of property contentCount.
     *
     */
    public int getContentCount() {
        return contentCount;
    }

    /** Setter for property contentCount.
     * @param contentCount New value of property contentCount.
     *
     */
    public void setContentCount(int contentCount) {
        this.contentCount = contentCount;
    }

    /** Getter for property songCount.
     * @return Value of property songCount.
     *
     */
    public int getSongCount() {
        return songCount;
    }

    /** Setter for property songCount.
     * @param songCount New value of property songCount.
     *
     */
    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    /** Getter for property listType.
     * @return Value of property listType.
     *
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

    /** Getter for property songItems.
     * @return Value of property songItems.
     *
     */
    public List<Object> getSongItems() {
        return songItems;
    }

    /** Setter for property songItems.
     * @param songItems New value of property songItems.
     *
     */
    public void setSongItems(java.util.List<Object> songItems) {
        this.songItems = songItems;
    }
}
