// Playlist
// $Id: Playlist.java,v 1.17 2003/07/06 14:00:14 axelwernicke Exp $
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

package de.axelwernicke.mypod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class represents a playlist within myPod.
 * A playlist is a List of clip oid's plus some further information like a name,
 * the total playtime...
 *
 * @author axel wernicke
 */
public class Playlist implements java.io.Serializable {
    /** list of oids that are in the playlist */
    private List<Long> list;

    /** name of the playlist */
    private String name;

    /** total time of this playlist in seconds */
    private int totalTime;

    /** total size of this playlist in bytes */
    private long totalFilesize;

    /** marks a playlist to be synchronized with iPod */
    private boolean iPodSync;

    /** Creates a new instance of Playlist
     */
    public Playlist() {
        name = "";
        list = new ArrayList<Long>();
    }

    /** Creates a new instance of Playlist
     *
     * @param _name name of the new playlist
     */
    public Playlist(String _name) {
        name = _name;
        list = new ArrayList<Long>();
    }

    /** Creates a new playlist from the name given, and adds clips to it
     *
     * @param name of the new playlist
     * @param oids list of clips to put into the new playlist
     */
    public Playlist(String name, List<Long> oids) {
        this.name = name;
        list = new ArrayList<Long>(oids.size());
        this.addClips(oids);
    }

    /** Gets the iterator for a playlist.
     *
     * @return iterator
     */
    Iterator<Long> iterator() {
        return list.iterator();
    }

    /** Removes clips from the playlist
     * @param oids of the clips to remove from the playlist
     */
    public void remove(List<Long> oids) {
        Iterator<Long> iter = oids.iterator();
        while (iter.hasNext()) {
            remove(iter.next());
        }
    }

    /** Removes an item from the playlist, and corrects the total time of the playlist.
     *
     * @param oid of the item to remove
     */
    public void remove(Long oid) {
        MP3Meta mp3meta = null;

        while ((oid != null) && list.contains(oid)) {
            // get meta data for the clip
            mp3meta = myPod.getBackend().getDataPool().getMeta(oid);

            // correct total time and file size for this playlist
            if (mp3meta != null) {
                totalTime -= mp3meta.getDuration();
                totalFilesize -= mp3meta.getFile().length();
            }

            // remove the item from the list
            list.remove(oid);
        }
    }

    /** sets the name of the playlist.
     *
     * @param _name the new name of the playlist
     */
    public void setName(String _name) {
        name = _name;
    }

    /** gets the name of a playlist
     * @return name of playlist
     */
    public String getName() {
        return name;
    }

    /** creates a string representation of a playlist.
     * @return string representing the playlist
     */
    public String toString() {
        return name;
    }

    /** Setter for property totalTime.
     *
     * @param totalTime New value of property totalTime.
     */
    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    /** gets the total playtime for the playlist.
     *
     * @return the total playtime
     */
    public int getTotalTime() {
        return totalTime;
    }

    /** gets the total count of clips in this playlist.
     *
     * @return total count of clips in this playlist
     */
    public int getTotalClips() {
        return list.size();
    }

    /** tue, if the playlist contains the clip specified by the oid.
     *
     * @param oid clip to search for in the playlist
     * @return tue, if the playlist contains the clip specified by the oid.
     */
    public boolean containsClip(Long oid) {
        return list.contains(oid);
    }

    /** Adds a clip to the playlist and corrects the total time.
     *
     * @param oid oid of the clip to add
     */
    public void addClip(Long oid) {
        // add oid if it is not contained yet
        if ((oid != null) && !list.contains(oid)) {
            list.add(oid);

            // update total time and filesize
            totalTime += myPod.getBackend().getDataPool().getMeta(oid).getDuration();
            totalFilesize += myPod.getBackend().getDataPool().getMeta(oid).getFile().length();
        }
    }

    /** Adds a clip to the playlist and corrects the total time.
     * @param oids of the clips to add
     */
    public void addClips(List<Long> oids) {
        if (oids == null) {
            return;
        }

        DataPool dataPool = myPod.getBackend().getDataPool();
        Long oid;
        int oidsSize = oids.size();

        // resize playlists List
//        list.ensureCapacity(list.size() + oidsSize);

        // add clips and update total time
        for (int i = 0; i < oidsSize; i++) {
            oid = oids.get(i);

            // add oid, if not contained already
            if (!list.contains(oid)) {
                list.add(oid);

                // update total time
                totalTime += dataPool.getMeta(oid).getDuration();
                totalFilesize += dataPool.getMeta(oid).getFile().length();
            }
        }
    }

    /** gets the oid for a clip from this playlist.
     *
     * @param index position of the clip
     * @return oid of the clip
     */
    public Long getClipAt(int index) {
        return list.get(index);
    }

    /** true, if the current playlist is of type AutoPlaylist.
     *
     * @return true, if the current playlist is of type AutoPlaylist.
     */
    public boolean isAutoplaylist() {
        return (this instanceof de.axelwernicke.mypod.AutoPlaylist);
    }

    /** Sets playlists iPod sync flag.
     *
     * @param value true to enable synchronization to iPod
     */
    public void setIPodSync(boolean value) {
        iPodSync = value;
    }

    /** Gets the sync to iPod flag for this playlist
     *
     * @return true, if the playlist is synchronized to iPod
     */
    public boolean isIPodSync() {
        return iPodSync;
    }

    /** Gets all artists that are in the playlist sorted by name.
     *
     * @param dataPool to determine artists for clips
     * @return List containing all artists
     */
    List<String> getAllArtists(DataPool dataPool) {
        List<String> result = new ArrayList<String>(101);

        Iterator<Long> clipIter = list.iterator();

        while (clipIter.hasNext()) {
            String artist = (String) dataPool.getMeta(clipIter.next()).get("Artist");

            if (!result.contains(artist)) {
                result.add(artist);
            }
        }

        // sort artists
        java.util.Collections.sort(result);

        return result;
    }

    /**
     * Gets all clips from the playlist.
     *
     * @return List containing the oids of all clips
     */
    @SuppressWarnings("unchecked")
    public List<Long> getAllClips() {
        return List.class.cast(ArrayList.class.cast(list).clone());
    }

    /**
     * Gets path and filename for all clips in the playlist as '"name1" "name2"'.
     *
     * @param dataPool to determine path and filename from.
     * @return path and filename of all clips as string
     */
    String getFilesString(DataPool dataPool) {
        StringBuffer buffer = new StringBuffer();

        Iterator<Long> iter = list.iterator();
        while (iter.hasNext()) {
            buffer.append("\"");
            buffer.append(dataPool.getMeta(iter.next()).getFile().getPath());
            buffer.append("\" ");
        }

        return buffer.toString();
    }

    /**
     * Getter for property list.
     *
     * @return Value of property list.
     */
    public List<Long> getList() {
        return list;
    }

    /**
     * Setter for property list.
     * @param list New value of property list.
     */
    public void setList(List<Long> list) {
        this.list = list;
    }

    /**
     * Recalculates the total time of the playlist
     */
    public void validateTotalTime() {
        int newTotalTime = 0;
        Long oid;
        DataPool dp = myPod.getBackend().getDataPool();
        MP3Meta meta = null;

        // iterate over all clips in the playlist
        Iterator<Long> iter = list.iterator();
        while (iter.hasNext()) {
            oid = iter.next();
            meta = dp.getMeta(oid);

            if ((oid != null) && (meta != null)) {
                newTotalTime += meta.getDuration();
            }
        }

        // set total playlime
        setTotalTime(newTotalTime);
    }

    /**
     * Recalculates the total filesize of the clips in the playlist
     */
    public void validateTotalFilesize() {
        long newTotalFilesize = 0;
        Long oid;
        DataPool dp = myPod.getBackend().getDataPool();
        MP3Meta meta = null;

        // iterate over all clips in the playlist
        Iterator<Long> iter = list.iterator();
        while (iter.hasNext()) {
            oid = iter.next();
            meta = dp.getMeta(oid);

            if ((oid != null) && (meta != null)) {
                newTotalFilesize += meta.getFile().length();
            }
        }

        // set total playlime
        setTotalFilesize(newTotalFilesize);
    }

    /**
     * Getter for property totalFilesize.
     * @return Value of property totalFilesize.
     */
    public long getTotalFilesize() {
        return totalFilesize;
    }

    /**
     * Setter for property totalFilesize.
     * @param totalFilesize New value of property totalFilesize.
     */
    public void setTotalFilesize(long totalFilesize) {
        this.totalFilesize = totalFilesize;
    }

    /**
     * @param srcPlaylist
     */
    public void replaceClips(Playlist srcPlaylist) {
        if (srcPlaylist != null) {
            // copy clips
            this.setList(srcPlaylist.getList());

            // copy statistics
            this.setTotalFilesize(srcPlaylist.getTotalFilesize());
            this.setTotalTime(srcPlaylist.getTotalTime());
        }
    }
}

/* */
