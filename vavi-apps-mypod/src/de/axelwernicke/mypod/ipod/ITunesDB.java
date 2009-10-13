// ITuensDB
// $Id: ITunesDB.java,v 1.20 2003/07/26 07:06:54 axelwernicke Exp $
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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


/** An iTunes Database object is related to an database file on an Apple iPod.
 * The database contains references to all music files on an iPod, as well as
 * meta information about the songs. Further all information about playlists on
 * the iPod.
 *
 * All information are stored in a binary file, and is contained in tags.
 * The set of tags contains
 *
 * <PRE>
 * mhbd - root tag of a database<BR>
 * mhsd - list holder, contains song lists or playlists<BR>
 * mhlt - song list header contains song items<BR>
 * mhit - song item, stores song meta information and is followed by one or more<BR>
 * mhod - song item content - stores one strings like artist, filetype, path etc.<BR>
 * mhlp - playlist header, contains one or more playlist items<BR>
 * mhyp - playlist item holds the playlist in paires of<BR>
 * mhip - playlist index and mhod<BR>
 *
 * an ITunesDB object stores the informations from the 'mhbd' tag and has references
 * to the content of all other tags.<BR>
 *
 * @see techdoc for more details
 * @author axelwe
 */
public class ITunesDB {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod.ipod");

    /** tag size for the mhbd tag in bytes */
    private int tagSize = 104;

    /** database size in bytes */
    private int recordSize = 104;

    /** unknown content3 */
    private int unknown3 = 1;

    /** unknown content4 */
    private int unknown4 = 1;

    /** unknown content5 */
    private int unknown5 = 2;

    /** song list holder */
    private ITunesDBListHolder songlistHolder = null;

    /** song list header */
    private ITunesDBSonglistHeader songlistHeader = null;

    /** playlist holder */
    private ITunesDBListHolder playlistHolder = null;

    /** playlist header */
    private ITunesDBPlaylistHeader playlistHeader = null;

    /**
     * Creates a new instance of an iTunesDB.
     * An core object without any content is created.
     */
    public ITunesDB() {
    }

    /**
     * Constructs a new iTunesDB object.
     *
     * An empty, but valid database is created.
     * @param iPodName , used as name of the masterplaylist
     */
    public ITunesDB(String iPodName) {
        this();
        initSonglistHolder();
        initSonglistHeader();
        initPlaylistHolder();
        initPlaylistHeader(iPodName);
    }

    /**
     * Initializes the songlist holder record of the database.
     */
    public void initSonglistHolder() {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "initSonglistHolder");

        songlistHolder = new ITunesDBListHolder();
        songlistHolder.setListType(ITunesDBListHolder.SONGLIST);

        // revalidate size
        this.recordSize += songlistHolder.getRecordSize();

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "initSonglistHolder");
    }

    /**
     * initializes songlist header record.
     */
    public void initSonglistHeader() {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "initSonglistHeader");

        songlistHeader = new ITunesDBSonglistHeader();

        // revalidate size
        songlistHolder.setRecordSize(songlistHolder.getRecordSize() + songlistHeader.getTagSize());
        this.recordSize += songlistHeader.getTagSize();

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "initSonglistHeader");
    }

    /**
     * initializes the playlist holder record.
     */
    public void initPlaylistHolder() {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "initPlaylistHolder");

        playlistHolder = new ITunesDBListHolder();
        playlistHolder.setListType(ITunesDBListHolder.PLAYLIST);

        // revalidate size
        this.recordSize += playlistHolder.getRecordSize();

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "initPlaylistHolder");
    }

    /**
     * Initializes playlist header record.
     * A masterplaylist is created.
     *
     * @param iPodName name of the master playlist (equals name of the iPod)
     */
    public void initPlaylistHeader(String iPodName) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "initPlaylistHeader");

        playlistHeader = new ITunesDBPlaylistHeader();

        // revalidate size
        playlistHolder.setRecordSize(playlistHolder.getRecordSize() + playlistHeader.getTagSize());
        this.recordSize += playlistHeader.getTagSize();

        // create masterplaylist
        this.createPlaylist(iPodName, null);

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "initPlaylistHeader");
    }

    /**
     * Gets the songlist holder record.
     *
     * @return songlist holder record of the database
     */
    public ITunesDBListHolder getSonglistHolder() {
        return songlistHolder;
    }

    /**
     * Gets the songlist header record
     *
     * @return songlist record
     */
    public ITunesDBSonglistHeader getSonglistHeader() {
        return songlistHeader;
    }

    /**
     * Gets the summarized size of all songs on the iPod
     *
     * @return size of all songs in bytes
     */
    public long getTotalFilesize() {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "getTotalFilesize");

        long totalSize = -1;

        // TODO: clean up if( songlistHeader != null && songlistHeader.getSongItems() != null )
        if (songlistHeader != null) {
            totalSize = 0;

            // iterate over all songs
            for (int i = 0; i < songlistHeader.getSongCount(); i++) {
                totalSize += songlistHeader.getSongItem(i).getFilesize();
            }
        }

        logger.info("total size of clips on iPod db: " + totalSize);
        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "getTotalFilesize");

        return totalSize;
    }

    /**
     * Gets the summarized size of all songs.
     *
     * @param fileIdc List containing file indices to summarize size
     * @return summarized size of selected songs
     */
    public long getTotalFilesize(List<Integer> fileIdc) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "getTotalFilesize");
        long totalSize = 0;

        // TODO: clean up		if( songlistHeader != null && songlistHeader.getSongItems() != null )
        if (songlistHeader != null) {
            // iterate over all file indices
            for (Iterator<Integer> iter = fileIdc.iterator(); iter.hasNext();) {
                totalSize += this.getFilesize(iter.next().longValue());
            }
        }

        logger.info("total size of clips : " + de.axelwernicke.mypod.gui.GuiUtils.formatFilesize(totalSize));
        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "getTotalFilesize");

        return totalSize;
    }

    /**
     * Gets the filename for a song from its file index.
     *
     * @param fileIndex of the song
     * @return filename for the song
     */
    public String getFilename(long fileIndex) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "getFilename");

        String filename = null;
        ITunesDBSongItem songItem = null;
        ITunesDBContentItem contentItem = null;
        boolean found = false;

        // iterate over all songs, until we find our file index
        for (int i = 0; (i < songlistHeader.getSongCount()) && !found; i++) {
            // get current song item and check if its our file index
            songItem = songlistHeader.getSongItem(i);
            if (songItem.getRecordIndex() == fileIndex) {
                // search path in an attached content records
                for (Iterator<ITunesDBContentItem> contentIter = songItem.getContentIterator();
                     !found && contentIter.hasNext();) {
                    contentItem = contentIter.next();
                    if (contentItem.getContentTyp() == ITunesDBContentItem.PATH) {
                        // hell, we found it =:)
                        found = true;
                        filename = contentItem.getContentAsString();
                    }
                }
            }
        }

        if (!found) {
            logger.warning("No filename found for file index: " + fileIndex);
        }

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "getFilename");

        return filename;
    }

    /**
     * Gets the filesize for a song from the file index.
     *
     * @param fileIndex fileindex of the song
     * @return size of the song in bytes
     */
    public long getFilesize(long fileIndex) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "getFilesize");

        long size = 0;
        boolean found = false;
        ITunesDBSongItem songItem = null;

        // iterate over all songs
        for (int i = 0; (i < songlistHeader.getSongCount()) && !found; i++) {
            // get the current song item and check the file index
            songItem = songlistHeader.getSongItem(i);
            if (songItem.getRecordIndex() == fileIndex) {
                // yep, thats our boy
                found = true;
                size = songItem.getFilesize();
            }
        }

        if (!found) {
            logger.warning("found no filesize for file index: " + fileIndex);
        }

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "getFilesize");

        return size;
    }

    /**
     * removes a song from the song list and all playlists.
     *
     * <BR>- iterate over all playlists and remove clip by fileindex (filename without extension)
     * <BR>- remove clip from the songlist
     *
     * @param fileIndex of the song to remove
     */
    public void removeClip(long fileIndex) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "removeClip");
        logger.fine("removing " + fileIndex);

        // remove song from all playlists
        long removedSize = 0;
        for (int i = 0; i < playlistHeader.getPlaylistCount(); i++) {
            removedSize = playlistHeader.getPlaylist(i).removeClip(fileIndex);

            // revalidate sizes
            this.playlistHolder.setRecordSize((int) (this.playlistHolder.getRecordSize() - removedSize));
            this.recordSize -= removedSize;

            logger.fine("playlist removed size: " + removedSize);
        }

        // find song in song list
        boolean found = false;
        ITunesDBSongItem songItem = null;
        for (int i = 0; (i < songlistHeader.getSongCount()) && !found; i++) {
            songItem = songlistHeader.getSongItem(i);
            if (songItem.getRecordIndex() == fileIndex) {
                found = true; // song item now contains the song to remove
            }
        }

        // save song item size
        long songRecordSize = songItem.getRecordSize();

        // remove song from song list and correct song list length
        songlistHeader.removeSongItem(songItem);

        // revalidate sizes
        songlistHolder.setRecordSize((int) (songlistHolder.getRecordSize() - songRecordSize));
        this.recordSize -= songRecordSize;

        logger.finer("songlist removed size: " + songRecordSize);
        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "removeClip");
    }

    /**
     * Adds a clip to song list and master playlist
     *
     * @param fileIndex of the song on the iPod
     * @param meta information about the song
     */
    public void addClip(int fileIndex, de.axelwernicke.mypod.MP3Meta meta) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "addClip");

        // add clip to song list
        int songlistAddSize = songlistHeader.addClip(fileIndex, meta);

        // revalidate sizes
        recordSize += songlistAddSize;
        songlistHolder.setRecordSize(songlistHolder.getRecordSize() + songlistAddSize);

        // add clip to master playlist
        ITunesDBPlaylistItem masterPlaylist = playlistHeader.getPlaylist(0);
        int playlistAddSize = masterPlaylist.addClip(fileIndex);

        // revalidate sizes
        recordSize += playlistAddSize;
        playlistHolder.setRecordSize(playlistHolder.getRecordSize() + playlistAddSize);

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "addClip");
    }

    /**
     * Determines the next available file index.
     * This fileindex is used as base filename to store the song on iPod.
     *
     * @param fileIndex recently used file index or 0
     * @return next available fileindex
     */
    public int getNextAvailableSongIndex(int fileIndex) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "getNextAvailableSongIndex");

        // set start value
        int newIndex = fileIndex;

        // increment until we find an unused one
        do {
            newIndex++;
        } while (containsIndex(newIndex));

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "getNextAvailableSongIndex");

        return newIndex;
    }

    /**
     * Checks if an file index is in the database.
     *
     * @param index to check
     * @return true, if the file index was found in the database
     */
    boolean containsIndex(int index) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "containsIndex");

        boolean found = false;
//      ITunesDBSongItem songItem;

        // iterate over all songs in the song list
        for (int i = 0; (i < songlistHeader.getSongCount()) && !found; i++) {
            if (songlistHeader.getSongItem(i).getRecordIndex() == index) {
                found = true;
            }
        }

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "containsIndex");

        return found;
    }

    /**
     * iterates over all playlists and removes them, if not a masterplaylist, but empty.
     */
    public void removeEmptyPlaylists() {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "removeEmptyPlaylists");

        ITunesDBPlaylistItem playlist = null;
        int size = 0;
        String name = null;

        // iterate over all, but leave the first one
        for (int i = playlistHeader.getPlaylistCount() - 1; i > 0; i--) {
            playlist = this.playlistHeader.getPlaylist(i);

            // remove if empty
            if (playlist.getSongCount() == 0) {
                name = playlist.getName();

                // revalidate sizes
                size = playlist.getRecordSize();
                this.playlistHolder.setRecordSize(this.playlistHolder.getRecordSize() - size);
                this.recordSize -= size;

                // remove playlist
                this.playlistHeader.removePlaylist(i);

                logger.info(new StringBuffer("removed playlist ").append(name).append(" from iPod.").toString());
            }
        }
        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "removeEmptyPlaylists");
    }

    /**
     * Removes a playlist from database.
     *
     * @param playlist playlist to remove
     * @return true, if removing was sucessful
     */
    boolean removePlaylist(ITunesDBPlaylistItem playlist) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "removePlaylist");

        boolean result = false;
        String name = "";
        long size = 0;

        // check that the playlist exists
        if (this.playlistHeader.containsPlaylist(playlist)) {
            name = playlist.getName();

            // revalidate sizes
            size = playlist.getRecordSize();
            this.playlistHolder.setRecordSize((int) (this.playlistHolder.getRecordSize() - size));
            this.recordSize -= size;

            // remove playlist
            this.playlistHeader.removePlaylist(playlist);

            result = true;

            logger.info(new StringBuffer("removed playlist ").append(name).append(" from iPod.").toString());
        } else {
            result = false;
            logger.warning("playlist to remove (" + name + ") was not fount in the playlist header");
        }

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "removePlaylist");

        return result;
    }

    /**
     * Validates the database.
     * Record sizes and content count is checked.
     *
     * <BR>- check db record size
     * <BR>- check songlist holder size
     * <BR>- check all song items size
     * <BR>- check song item count
     * <BR>- check playlist holder size
     * <BR>- check all playlist items
     * <BR>- check playlist count
     * <BR>- check song count for each playlist
     *
     * @return true, if the database is valid
     * @throws IOException
     */
    boolean isValid() throws IOException {
        // delegate
        return isValid(false);
    }

    /**
     * Validates the database.
     * Record sizes and content count is checked.
     *
     * <BR>- check db record size
     * <BR>- check songlist holder size
     * <BR>- check all song items size
     * <BR>- check song item count
     * <BR>- check playlist holder size
     * <BR>- check all playlist items
     * <BR>- check playlist count
     * <BR>- check song count for each playlist
     *
     * @param correct if true, the offsets are corrected
     * @return true, if the database is valid
     * @throws IOException
     */
    boolean isValid(boolean correct) throws IOException {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "isValid");
        boolean valid = true;

        // check tag size, item counts and references
        valid = !(!tagSizesValid(correct) || !countsValid(correct) || !referencesValid(correct));

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "isValid");

        return valid;
    }

    /**
     * Checks the size of the records of the data base.
     * @throws IOException
     */
    private boolean tagSizesValid(boolean correct) throws IOException {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "tagSizesValid");

        boolean valid = true;
        int size;

        // instantiate a parser to check the database
        ITunesDBParser parser = new ITunesDBParser(this);

        // check db record
        size = parser.calculateRecordSize(this);
        if (size != this.recordSize) {
            valid = false;
            logger.warning("db record size invalid (record size is: " + this.recordSize + ", but was calculated as:" + size + ")");

            if (correct) {
                this.recordSize = size;
            }
        }

        // check songlist holder record
        size = parser.calculateRecordSize(this.songlistHolder) - this.songlistHolder.getTagSize();
        if (size != this.songlistHolder.getRecordSize()) {
            valid = false;
            logger.warning("songlist holder size invalid ( record size is: " + this.songlistHolder.getRecordSize() + ", but was calculated as:" + size + ")");

            if (correct) {
                this.songlistHolder.setRecordSize(size);
            }
        }

        // check all song items size
        ITunesDBSongItem songItem;
        for (int i = 0; i < songlistHeader.getSongCount(); i++) {
            songItem = songlistHeader.getSongItem(i);
            size = parser.calculateRecordSize(songItem);
            if (size != songItem.getRecordSize()) {
                valid = false;
                logger.warning("song item invalid ( record size is: " + songItem.getRecordSize() + ", but was calculated as:" + size + ")");

                if (correct) {
                    songItem.setRecordSize(size);
                }
            }
        } // for all songs

        // check playlist holder size
        size = parser.calculateRecordSize(this.playlistHolder) - this.songlistHolder.getTagSize();
        if (size != this.playlistHolder.getRecordSize()) {
            valid = false;
            logger.warning("playlist holder size invalid ( record size is:" + this.playlistHolder.getRecordSize() + ", but was calculated as:" + size + ")");

            if (correct) {
                this.playlistHolder.setRecordSize(size);
            }
        }

        // check all playlist items size
        ITunesDBPlaylistItem playlistItem;
        for (int i = 0; i < this.playlistHeader.getPlaylistCount(); i++) {
            playlistItem = this.playlistHeader.getPlaylist(i);
            size = parser.calculateRecordSize(playlistItem);
            if (size != playlistItem.getRecordSize()) {
                valid = false;
                logger.warning("playlistItem.recordSize invalid ( record size is:" + playlistItem.getRecordSize() + ", but was calculated as:" + size + ")");

                if (correct) {
                    playlistItem.setRecordSize(size);
                }
            }
        } // for all playlists

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "tagSizesValid");

        return valid;
    }

    /** Creates a new playlist on iPod containing all the songs in the List.
     *
     * @param name of the playlist
     * @param fileIdc List of fileindices to include in the playlist
     */
    public void createPlaylist(String name, List<Integer> fileIdc) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "createPlaylist");

        int fileIndex = 0;
        boolean result = false;

        // check if we have that playlist on iPod already
        ITunesDBPlaylistItem playlist = getPlaylist(name);

        // remove that iPod playlist
        if (playlist != null) {
            result = removePlaylist(playlist);
            logger.info("removed playlist " + name + " from db : " + result);
        } else {
            logger.info("added new playlist to db " + name);
        }

        // create new playlist
        playlist = new ITunesDBPlaylistItem();
        playlist.setListType(0);
        playlist.setName(name);

        // add playlist to header
        playlistHeader.addPlaylist(playlist);

        // revalidate size
        playlistHolder.setRecordSize(playlistHolder.getRecordSize() + playlist.getRecordSize());
        recordSize += playlist.getRecordSize();

        // add songs to playlist
        int addSize = 0;
        if (fileIdc != null) {
            for (Iterator<Integer> fileIter = fileIdc.iterator(); fileIter.hasNext();) {
                fileIndex = fileIter.next().intValue();
                addSize += playlist.addClip(fileIndex);

                logger.finest("added clip to playlist : " + fileIndex + " size " + addSize);
            }
        }

        // revalidate size
        this.playlistHolder.setRecordSize(this.playlistHolder.getRecordSize() + addSize);
        this.recordSize += addSize;

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "createPlaylist");
    }

    /**
     * Gets a playlist from the database by name.
     *
     * @param name of the playlist
     * @return playlist, if found, null else
     */
    ITunesDBPlaylistItem getPlaylist(String name) {
        logger.entering("de.axelwernicke.mypod.ipod.ITunesDB", "getPlaylist");

        ITunesDBPlaylistItem currPlaylist = null;
        ITunesDBPlaylistItem foundPlaylist = null;
        boolean found = false;

        // iterate over all playlists
        for (int i = 0; (i < this.playlistHeader.getPlaylistCount()) && !found;
             i++) {
            currPlaylist = this.playlistHeader.getPlaylist(i);

            logger.finer("searching for playlist: " + name + ", found " + currPlaylist.getName());

            if (name.equals(currPlaylist.getName())) {
                found = true;
                foundPlaylist = currPlaylist;
            }
        }

        if (!found) {
            logger.info("searching for playlist: " + name + " failed.");
        }

        logger.exiting("de.axelwernicke.mypod.ipod.ITunesDB", "getPlaylist");

        return foundPlaylist;
    }

    /**
     * generates a string that represents the current object
     *
     * @return string representation of the database
     */
    public String toString() {
        return new StringBuffer("[tagSize] ").append(tagSize).append('\t').append("[recordSize] ").append(recordSize).append('\t').append("[unknown3] ").append(unknown3).append('\t').append("[unknown4] ").append(unknown4).append('\t').append("[unknown5] ").append(unknown5).toString();
    }

    /**
     */
    private boolean countsValid(boolean correct) {
        logger.entering("ITunesDB", "checkCounts");
        boolean valid = true;

        // check item count of the playlists.
        ITunesDBPlaylistItem playlistItem;
        for (int i = 0; i < this.playlistHeader.getPlaylistCount(); i++) {
            playlistItem = this.playlistHeader.getPlaylist(i);
            // check item count of the playlist. Its normal that playlist count is less that items attached, 
            // because two items are used for the playlist name
            if ((playlistItem.getSongItems().size() - 2) != playlistItem.getSongCount()) {
                valid = false;
                logger.warning("playlist " + playlistItem.getName() + " song count invalid ( playlist song count is : " + playlistItem.getSongCount() + ", but playlist has :" + (playlistItem.getSongItems().size() - 2) + " song items attached)");

                if (correct) {
                    playlistItem.setSongCount(playlistItem.getSongItems().size() - 2);
                }
            }
        } // for all playlists

        logger.exiting("ITunesDB", "checkCounts");
        return valid;
    }

    /**
     */
    private boolean referencesValid(boolean correct) {
        logger.entering("ITunesDB", "checkReferences");

        boolean valid = true; // in german we call this "Unschuldsvermutung" =:)

        // check references song items playlist -> songlist
        ITunesDBPlaylistItem playlistItem;
        Object indexItem;
        for (int i = 0; i < this.playlistHeader.getPlaylistCount(); i++) {
            // iterate over all song index items
            playlistItem = this.playlistHeader.getPlaylist(i);
            for (Iterator<?> indexIter = playlistItem.getSongItems().iterator();
                 indexIter.hasNext();) {
                // take care, the playlist contains content items as well as index items...
                indexItem = indexIter.next();
                if (indexItem instanceof de.axelwernicke.mypod.ipod.ITunesDBPlaylistIndexItem && !this.songlistHeader.containsClip(((ITunesDBPlaylistIndexItem) indexItem).getSongIndex())) {
                    valid = false;
                    logger.warning("playlist " + playlistItem.getName() + " contains a clip [index]: " + ((ITunesDBPlaylistIndexItem) indexItem).getSongIndex() + " that is not contained in the global song list.");
                }
            } // for all songs in the playlist
        } // for all playlists

        // check references song items -> file system
        logger.exiting("ITunesDB", "checkReferences");

        return valid;
    }

    /**
     * Getter for property tagSize.
     * @return Value of property tagSize.
     */
    public int getTagSize() {
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
     * Getter for property unknown3.
     * @return Value of property unknown3.
     */
    public int getUnknown3() {
        return unknown3;
    }

    /**
     * Setter for property unknown3.
     * @param unknown3 New value of property unknown3.
     */
    public void setUnknown3(int unknown3) {
        this.unknown3 = unknown3;
    }

    /**
     * Getter for property unknown4.
     * @return Value of property unknown4.
     */
    public int getUnknown4() {
        return unknown4;
    }

    /**
     * Setter for property unknown4.
     * @param unknown4 New value of property unknown4.
     */
    public void setUnknown4(int unknown4) {
        this.unknown4 = unknown4;
    }

    /**
     * Getter for property unknown5.
     * @return Value of property unknown5.
     */
    public int getUnknown5() {
        return unknown5;
    }

    /**
     * Setter for property unknown5.
     * @param unknown5 New value of property unknown5.
     */
    public void setUnknown5(int unknown5) {
        this.unknown5 = unknown5;
    }

    /**
     * Setter for property songlistHolder.
     * @param songlistHolder New value of property songlistHolder.
     */
    public void setSonglistHolder(de.axelwernicke.mypod.ipod.ITunesDBListHolder songlistHolder) {
        this.songlistHolder = songlistHolder;
    }

    /**
     * Setter for property songlistHeader.
     * @param songlistHeader New value of property songlistHeader.
     */
    public void setSonglistHeader(de.axelwernicke.mypod.ipod.ITunesDBSonglistHeader songlistHeader) {
        this.songlistHeader = songlistHeader;
    }

    /**
     * Getter for property playlistHolder.
     * @return Value of property playlistHolder.
     */
    public de.axelwernicke.mypod.ipod.ITunesDBListHolder getPlaylistHolder() {
        return playlistHolder;
    }

    /**
     * Setter for property playlistHolder.
     * @param playlistHolder New value of property playlistHolder.
     */
    public void setPlaylistHolder(de.axelwernicke.mypod.ipod.ITunesDBListHolder playlistHolder) {
        this.playlistHolder = playlistHolder;
    }

    /**
     * Getter for property playlistHeader.
     * @return Value of property playlistHeader.
     */
    public de.axelwernicke.mypod.ipod.ITunesDBPlaylistHeader getPlaylistHeader() {
        return playlistHeader;
    }

    /**
     * Setter for property playlistHeader.
     * @param playlistHeader New value of property playlistHeader.
     */
    public void setPlaylistHeader(de.axelwernicke.mypod.ipod.ITunesDBPlaylistHeader playlistHeader) {
        this.playlistHeader = playlistHeader;
    }
}
