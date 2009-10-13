// ITunesDBSonglistHeader
// $Id: ITunesDBSonglistHeader.java,v 1.13 2003/07/26 07:06:54 axelwernicke Exp $
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
import java.util.Iterator;
import java.util.List;

import de.axelwernicke.mypod.MP3Meta;


/**
 * A SongListHeader object stores the informations from the mhlt tag. It has
 * references to the songs in the list.
 * 
 * <pre>
 * 	mhlt:
 * 	x00	'mhlt'		- tag
 * 	x04			- tag size
 * 	x08			- total count of songs in the list
 * </pre>
 * 
 * @see techdoc for more details
 * @author axelwe
 */
public class ITunesDBSonglistHeader {
    /** tag size inn bytes */
    private int tagSize = 92;

    /** list of song items - all items stored on the iPod are in here !! */
    private List<ITunesDBSongItem> songItems;

    /** Standard Construcor for a Song list item. */
    ITunesDBSonglistHeader() {
        songItems = new ArrayList<ITunesDBSongItem>();
    }

    /**
     * Adds a Clip to iTunes DB Songlist. a new song item containing all meta
     * data is created and added to the songlist
     * 
     * @param fileIndex of to clip in the iTunes DB
     * @param meta data of the clip
     * @return size of the created song item
     */
    int addClip(int fileIndex, MP3Meta meta) {
        // create song item
        ITunesDBSongItem songItem = new ITunesDBSongItem();

        // set meta data
        songItem.setDate(ITunesDBParser.dateToMacDate(System.currentTimeMillis()));
        songItem.setDuration((int) meta.getDuration() * 1000); // duration is
                                                                // ms in iTunes
                                                                // DB
        songItem.setFilesize((int) meta.getFile().length());
        songItem.setRecordIndex(fileIndex);
        songItem.setTrackNumber((Integer) meta.get("Track"));
        songItem.setUnknown8(256); // TODO: - not sure what that meta data
                                    // means in the db
        // songItem.unknown6 = 0;
        // songItem.unknown7 = 0;
        // songItem.unknown13 = 0;
        // songItem.unknown14 = 0;
        songItem.setBitrate((Integer) meta.getFile().getProperty("Bitrate"));
        // songItem.unknown16 = 0;
        // create song content items
        songItem.attachContent(new ITunesDBContentItem(ITunesDBContentItem.FILETYPE, 1, ITunesDBParser.stringToUTF16LittleEndian("MPEG audio file")));

        /**
         * TODO: take care of this when switching to store files in other
         * directories than F00
         */
        String path = new StringBuffer(":iPod_Control:Music:F00:").append(fileIndex).append(".mp3").toString();
        songItem.attachContent(new ITunesDBContentItem(ITunesDBContentItem.PATH, 1, ITunesDBParser.stringToUTF16LittleEndian(path)));

        String artist = (String) meta.get("Artist");
        if ((artist != null) && (artist.length() != 0)) {
            songItem.attachContent(new ITunesDBContentItem(ITunesDBContentItem.ARTIST, 1, ITunesDBParser.stringToUTF16LittleEndian(artist)));
        }

        String album = (String) meta.get("Album");
        if ((album != null) && (album.length() != 0)) {
            songItem.attachContent(new ITunesDBContentItem(ITunesDBContentItem.ALBUM, 1, ITunesDBParser.stringToUTF16LittleEndian(album)));
        }

        String comment = (String) meta.get("Comment");
        if ((comment != null) && (comment.length() != 0)) {
            songItem.attachContent(new ITunesDBContentItem(ITunesDBContentItem.COMMENT, 1, ITunesDBParser.stringToUTF16LittleEndian(comment)));
        }

        String genre = (String) meta.get("Genre");
        if ((genre != null) && (genre.length() != 0)) {
            songItem.attachContent(new ITunesDBContentItem(ITunesDBContentItem.GENRE, 1, ITunesDBParser.stringToUTF16LittleEndian(genre)));
        }

        if (meta.get("Title") != null && ((String) meta.get("Title")).length() != 0) {
            songItem.attachContent(new ITunesDBContentItem(ITunesDBContentItem.TITEL, 1, ITunesDBParser.stringToUTF16LittleEndian((String) meta.get("Title"))));
        }

        // add song item to the songlist header and correct song count
        songItems.add(songItem);

        return songItem.getRecordSize();
    }

    /**
     * Gets the members of the object as formatted string.
     * 
     * @return formatted string
     */
    public String toString() {
        return new StringBuffer("[tagSize] ").append(tagSize).append('\t').append("[songCount] ").append(songItems.size()).toString();
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
     * Getter for property songCount.
     * 
     * @return Value of property songCount.
     */
    public int getSongCount() {
        return songItems.size();
    }

    /**
     * Getter for property songItems. This is private, cause song items are
     * added and removed by add and remove method...
     * 
     * @return Value of property songItems.
     */
    @SuppressWarnings("unused")
    private List<ITunesDBSongItem> getSongItems() {
        return songItems;
    }

    /**
     * Setter for property songItems.
     * 
     * @param songItems New value of property songItems.
     */
    @SuppressWarnings("unused")
    private void setSongItems(java.util.List<ITunesDBSongItem> songItems) {
        this.songItems = songItems;
    }

    public void addSongItem(ITunesDBSongItem songItem) {
        this.songItems.add(songItem);
    }

    public ITunesDBSongItem getSongItem(int index) {
        return this.songItems.get(index);
    }

    public void removeSongItem(int index) {
        this.songItems.remove(index);
    }

    public void removeSongItem(ITunesDBSongItem songItem) {
        this.songItems.remove(songItem);
    }

    boolean containsClip(int fileIndex) {
        boolean found = false;

        // iterate over all songs until we find the searched one...
        ITunesDBSongItem songItem;
        for (Iterator<ITunesDBSongItem> songIter = this.songItems.iterator(); !found && songIter.hasNext();) {
            songItem = songIter.next();
            if (songItem.getRecordIndex() == fileIndex) {
                found = true;
            }
        }

        return found;
    }
}
