// Backend
// $Id: Backend.java,v 1.46 2003/08/03 09:45:28 axelwernicke Exp $
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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.axelwernicke.mypod.gui.GuiUtils;
import de.axelwernicke.mypod.gui.ProgressDialog;
import de.axelwernicke.mypod.ipod.IPod;
import de.axelwernicke.mypod.util.ClipsTableUtils;
import de.axelwernicke.mypod.util.FileUtils;
import de.axelwernicke.mypod.util.ID3Utils;


/**
 * Contains all the application logic for myPod
 *
 * @author  axel wernicke
 */
public class Backend {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** name of the file all playlists are serialized to */
    private static final String PLAYLISTS_GZ_FILENAME = "data" + File.separator + "playlists.xml.gz";

    /** name of the file to backup the playlistfile to */
    private static final String PLAYLISTS_GZ_BACKUP_FILENAME = "data" + File.separator + "playlists.xml.gz.bak";

    /** name of the file all playlists are serialized to */
    private static final String PLAYLISTS_FILENAME = "data" + File.separator + "playlists.xml";

    /** name of myPods preferences file */
    private static final String PREFERENCES_GZ_FILENAME = "data" + File.separator + "preferences.xml.gz";

    /** name of the file the playlist file is backed up to */
    private static final String PREFERENCES_GZ_BACKUP_FILENAME = "data" + File.separator + "preferences.xml.bak.gz";

    /** name of the file all playlists are serialized to */
    private static final String PREFERENCES_FILENAME = "data" + File.separator + "preferences.xml";

    /** iPod mapper filename */
    private static final String IPOD_MAPPER_FILENAME = "data/ipodmapper.xml";

    /** data pool */
    private DataPool dataPool = null;

    /** model for the list of all playlists */
    private DefaultListModel playlistListModel;

    /** model for the spreader - a list of all artists / albums / genres of a playlist */
    private DefaultListModel spreaderListModel;

    /** table model for list view */
    private ClipsTableModel clipsListTableModel;

    /** myPod preferences */
    private Preferences preferences = null;

    /** iPod object */
    IPod iPod = null;

    // ------------------------------------- BACKEND STARTUP AND SHUTDOWN HELPERS -------------------------------------- 

    /**
     * Creates a new instance of Backend
     * <li> deserialize preferences
     * <li> initialize datapool & ipod
     * <li> deserialize playlists
     * <li> validate playlists
     * <li> initialize list view table model
     */
    public Backend() {
        logger.entering("Backend", "Backend");

        // try to deserialize preferences
        boolean success = deserializePreferences();
        if (success != true) {
            preferences = new Preferences();
        }

        // try to deserialize playlists
        success = deserializePlaylists();
        if (success != true || this.playlistListModel.size() == 0) {
            logger.info("New Playlist List objects created.");
            this.playlistListModel = new DefaultListModel();

            // add autoplaylist
            createAutoPlaylist(GuiUtils.getStringLocalized("allMyMusic"));
        }

        // initialize spreader list model
        this.spreaderListModel = new DefaultListModel();

        // initialize data pool
        dataPool = new DataPool();

        // deserialize data
        success = dataPool.deserializeData();
        if (success != true) {
            logger.info("New data object created.");
            dataPool.setData(new HashMap<Long, MP3Meta>());
        }

        // create filename cache
        dataPool.rebuildFilenameCache();

        // validate playlists
        validatePlaylists();

        // initialize iPod object
        iPod = new IPod();

        // init list view model from first playlist (which always exist...)
        clipsListTableModel = new ClipsTableModel((Playlist) this.playlistListModel.getElementAt(0));

        logger.exiting("Backend", "Backend");
    }

    /**
     * shuts the backend down.
     * <li> serialzie myPod preferences
     * <li> serialize playlists
     * <li> shut down the data pool
     */
    public void shutdown() {
        serializePreferences();
        serializePlaylists();
        dataPool.shutdown();
    }

    /** Gets the data pool.
     *
     * @return current data pool
     */
    protected DataPool getDataPool() {
        return dataPool;
    }

    /**
     * get list of playlists
     *
     * @return list of playlists
     */
    protected DefaultListModel getPlaylistList() {
        return this.playlistListModel;
    }

    /** get spreader list model
     *
     * @param columnCode for the column to get a model for
     * @param playlistIdx index of playlist to collect list items from
     * @return list model containing a distinct selection of the specified column, ordered
     */
    protected DefaultListModel getSpreaderListModel(int columnCode, int playlistIdx) {
        // get the content for the selected column
        List<String> content;
        switch (ClipsTableUtils.values()[columnCode]) {
        case Artist:
            content = this.getDataPool().getAllValues("Artist", this.getPlaylist(playlistIdx).getAllClips(), true);
            break;
        case Album:
            content = this.getDataPool().getAllValues("Album", this.getPlaylist(playlistIdx).getAllClips(), true);
            break;
        case Genre:
            content = this.getDataPool().getAllValues("Genre", this.getPlaylist(playlistIdx).getAllClips(), true);
            break;
        default:
            content = new ArrayList<String>(1);
        }

        // initialize model for the column
        this.spreaderListModel.removeAllElements();
        this.spreaderListModel.ensureCapacity(content.size());
        Iterator<String> contentIter = content.iterator();
        while (contentIter.hasNext()) {
            this.spreaderListModel.addElement(contentIter.next());
        }

        return this.spreaderListModel;
    }

    /**
     * get the TableModel with data for a specific playlist
     *
     * @param index index of the playlist in the list of playlists
     * @return table model for the playlist
     */
    @SuppressWarnings("unused")
    private ClipsTableModel getClipsListTableModel(int index) {
        // determine playlist
        Playlist playlist = (Playlist) this.playlistListModel.get(index);

        return getClipsListTableModel(playlist);
    }

    /**
     * get the TableModel with data for a specific playlist
     *
     * @param playlist to get the model for
     * @return table model for the playlist
     */
    protected ClipsTableModel getClipsListTableModel(Playlist playlist) {
        // determine playlist
        if ((clipsListTableModel.getPlaylist() == null) || !clipsListTableModel.getPlaylist().equals(playlist)) {
            // set playlist
            clipsListTableModel.setPlaylist(playlist);
        }

        return clipsListTableModel;
    }

    // ---------------------------------------------------- PLAYLIST HELPERS ------------------------------------------- 

    /**
     * Adds a playlist to the list of playlists
     * @param playlist to add
     */
    public void addPlaylist(Playlist playlist) {
        if (playlist != null) {
            this.getPlaylistList().addElement(playlist);
        }
    }

    /**
     * creates a new playlist.
     *
     * @param name name of the new playlist
     * @return new created playlist
     */
    public Playlist createPlaylist(String name) {
        // create new playlist
        Playlist playlist = new Playlist(name);
        this.playlistListModel.addElement(playlist);

        return playlist;
    }

    /** creates a new auto playlist
     *
     * @param name name of the new autoplaylist
     * @return index of the new autoplaylist in the list of playlists
     */
    public Playlist createAutoPlaylist(String name) {
        // create new autoplaylist
        Playlist playlist = new AutoPlaylist(name);
        this.playlistListModel.addElement(playlist);

        return playlist;
    }

    /** deletes a playlist
     * @param index index of the playlist to delete in the list of playlists
     */
    public void removePlaylist(int index) {
        this.playlistListModel.remove(index);
        clipsListTableModel.setPlaylist(null);
    }

    /** Removes a playlist
     * @param playlist to remove
     */
    public void removePlaylist(Playlist playlist) {
        this.playlistListModel.removeElement(playlist);
        clipsListTableModel.setPlaylist(null);
    }

    /** Gets a playlist by its index
     * @return the playlist
     * @param index index of the playlist in the list of playlists
     */
    protected Playlist getPlaylist(int index) {
        return (Playlist) this.playlistListModel.get(index);
    }

    /** Gets a playlist by its name, or null if it not exists...
     * @param name of the playlist to get
     * @return first playlist found for the name given as argument
     */
    public Playlist getPlaylist(String name) {
        Playlist foundList = null;

        // iterate over all playlists
        Playlist currList;
        boolean found = false;
        for (Enumeration<?> playlistIter = this.getPlaylistList().elements();
             !found && playlistIter.hasMoreElements();) {
            currList = (Playlist) playlistIter.nextElement();
            if (currList.getName().equals(name)) {
                foundList = currList;
                found = true;
            }
        }

        return foundList;
    }

    /** Applies the filters to an autoplaylist
     *        <BR>- get the playlist
     *        <BR>- check if we have an autoplaylist
     *        <BR>- get all enabled filters
     *        <BR>- remove clips from the playlist if they not apply to the filter (anymore)
     *        <BR>- find all clips that apply to the filter
     *        <BR>-        add found clips, if they are not in the playlist yet
     *        <BR>-        update playtime for the playlist
     *
     * @param index index of the playlist in the list of playlists
     */
    public void updateAutoplaylist(int index) {
        Playlist playlist = this.getPlaylist(index);

        if (playlist.isAutoplaylist()) {
            ((AutoPlaylist) playlist).update(null, this.dataPool);
        }
    }

    /** Applies the filters to an autoplaylist
     *         <BR>- get the playlist
     *         <BR>- check if we have an autoplaylist
     *         <BR>- get all enabled filters
     *         <BR>- remove clips from the playlist if they not apply to the filter (anymore)
     *         <BR>- find all clips that apply to the filter
     *         <BR>-        add found clips, if they are not in the playlist yet
     *         <BR>-        update playtime for the playlist
     *
     * @param playlist to update
     * @param oids to update the playlist to
     */
    public void updateAutoplaylist(Playlist playlist, List<Long> oids) {
        if (playlist.isAutoplaylist()) {
            ((AutoPlaylist) playlist).update(oids, this.dataPool);
        }
    }

    /** Update all autoplaylists
     *        <BR>- iterate over all playlists and update the autoplaylists
     *        <BR>- additionally total time and total filesize are validated
     */
    public void updateAllAutoplaylists() {
        Playlist playlist;
        int size = this.playlistListModel.size();

        for (int i = 0; i < size; i++) {
            playlist = this.getPlaylist(i);
            if (playlist.isAutoplaylist()) {
                ((AutoPlaylist) playlist).update(null, this.dataPool);
            }
        }
    }

    /** Validates all playlists.
     *        Each entry in each playlists is checked.
     *        If the entry (oid) is not contained in the data pool, its removed from the playlist.
     *        Attention: This is pretty time consuming!
     */
    public void validatePlaylists() {
        logger.entering("Backend", "validatePlaylists");

        // do it for all playlists
        Playlist playlist;
        List<Long> oids;
        Long oid;

        // check each playlist
        int playlistCnt = this.playlistListModel.size();
        for (int i = 0; i < playlistCnt; i++) {
            // get playlist
            playlist = (Playlist) this.playlistListModel.get(i);
            oids = playlist.getList();

            long validCnt = 0;
            long invalidCnt = 0;

            if (playlist != null) {
                // check that there is no key ( oid ) without meta data object...
                for (int idx = oids.size() - 1; idx >= 0; idx--) {
                    oid = oids.get(idx);
                    if ((oid == null) || (getDataPool().getMeta(oid) == null)) {
                        logger.warning("oid " + oid + " was invalid, removing entry");
                        oids.remove(idx);
                        invalidCnt++;
                    } else {
                        validCnt++;
                    }
                } // for all clips of the playlist
            }

            // log if there was something to correct
            if (invalidCnt > 0) {
                logger.info("checking playlist " + playlist.getName() + " resulted in " + validCnt + " , but " + invalidCnt + " invalid clips.");
            }
        } // for all playlists

        logger.exiting("Backend", "validatePlaylists");
    } // validate playlists

    /** Gets the total time of a couple of clips
     *
     * @param oids of the clips to summarize duration for
     * @return summarized duration of the clips in oids
     */
    public int getTotalTime(List<Long> oids) {
        int totalTime = 0;

        // iterate over all oids to summarize clip duration
        Iterator<Long> oidIter = oids.iterator();
        while (oidIter.hasNext()) {
            totalTime += dataPool.getMeta(oidIter.next()).getDuration();
        }

        return totalTime;
    }

    /** Gets the total filesize of a couple clips.
     *
     * @param oids to summarize filesize
     * @return summarized filesize
     */
    public long getTotalFilesize(List<Long> oids) {
        long totalFileSize = 0;

        // iterate over all oids to summarize clip duration
        Iterator<Long> oidIter = oids.iterator();
        while (oidIter.hasNext()) {
            totalFileSize += dataPool.getMeta(oidIter.next()).getFile().length();
        }

        return totalFileSize;
    }

    // ---------------------------------------------------- ACTION HELPERS ------------------------------------------- 

    /**
     * Scans a list of files and directories for mp3 files.
     * <li> get all selected files and all files in selected direcories
     * <li> if file is not known yet, scan file
     * <li> if not, check if the file was touched since last scan, if so, rescan
     * and update meta data
     * 
     * @param dialog scan progress dialog
     * @param selection list of files and directories
     * @return the number of clips updated or added
     */
    public int scanFiles(File[] selection, de.axelwernicke.mypod.gui.ScanDialog dialog) {
        logger.entering("Backend", "scanFiles");

        List<File> files = new ArrayList<File>();
        int fileCnt = 0;
        int totalScannedCnt = 0;
        int addCnt = 0;
        int updateCnt = 0;
        int errorCnt = 0;
        File currFile = null;

        // do some gui stuff
        dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("determiningFilesToScan"));
        dialog.totalProgressBar.setIndeterminate(true);
        dialog.cancelButton.setEnabled(false);

        // get all files to scan
        for (int i = 0; i < selection.length; i++) {
            currFile = selection[i];
            if (currFile != null) {
                if (currFile.isDirectory()) {
                    files.addAll(FileUtils.getAllFiles(currFile, FileUtils.mp3FilePattern));
                } else {
                    files.add(currFile);
                }
            }

            // check if user aborted scanning
            if (Thread.interrupted()) {
                return addCnt + updateCnt;
            }
        }

        fileCnt = files.size();

        // update gui
        dialog.totalProgressBar.setIndeterminate(false);
        dialog.cancelButton.setEnabled(true);
        dialog.totalProgressBar.setMaximum(files.size());

        // scan files
        MP3Meta mp3meta = null;
        while (!files.isEmpty()) {
            totalScannedCnt++;

            // get file from the end of the List
            currFile = files.remove(files.size() - 1);

            // do some gui stuff
            dialog.statusContentLabel.setText(new StringBuffer(GuiUtils.getStringLocalized("scanningFiles")).append(" (").append(totalScannedCnt).append("/").append(fileCnt).append(")").toString());
            dialog.fileNameContentLabel.setText(currFile.getName());
            dialog.totalProgressBar.setValue(totalScannedCnt);

            // check if the file is scanned already
            if (dataPool.isInPool(currFile)) {
                // we need only to analyze the file, if it is touched
                if (currFile.lastModified() != dataPool.getLastModified(currFile)) {
                    mp3meta = ID3Utils.scanMp3VdHeide(currFile);
                    if (mp3meta != null) {
                        dataPool.updateData(currFile, mp3meta);
                        updateCnt++;
                    } else {
                        errorCnt++;
                    }
                }
            } else {
                mp3meta = ID3Utils.scanMp3VdHeide(currFile);
                if (mp3meta != null) {
                    dataPool.addData(currFile, mp3meta);
                    addCnt++;
                } else {
                    errorCnt++;
                }
            }

            // update gui
            dialog.clipsAddedContentLabel.setText(String.valueOf(addCnt));
            dialog.clipsUpdatedContentLabel.setText(String.valueOf(updateCnt));

            // check if user aborted scanning
            if (Thread.interrupted()) {
                return addCnt + updateCnt;
            }
        } // for all files to scann

        logger.info("Scanning failed for " + errorCnt + " files");
        logger.info("Scanning added " + addCnt + " files");
        logger.info("Scanning updated " + updateCnt + " files");
        logger.exiting("Backend", "scanFiles");

        return addCnt + updateCnt;
    } // scan files

    /** serializes all playlists
     */
    private void serializePlaylists() {
        logger.entering("Backend", "serializePlaylists()");

        // create backup file
        File playlistFile = new File(PLAYLISTS_GZ_FILENAME);
        if (playlistFile.exists()) {
            FileUtils.copy(playlistFile, new File(PLAYLISTS_GZ_BACKUP_FILENAME));
        }

        // since jdk 1.4 has a problem to serialize javax.swing.DefaultListModel to xml, lets copy the content in
        // a simple List
        List<Object> playlists = new ArrayList<Object>(this.playlistListModel.size());
        int modelSize = this.playlistListModel.size();
        for (int i = 0; i < modelSize; i++) {
            playlists.add(this.playlistListModel.get(i));
        }

        try {
            XMLEncoder xe = new XMLEncoder(new GZIPOutputStream(new FileOutputStream(playlistFile), 65535));
            xe.writeObject(playlists);
            xe.close();
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        logger.exiting("Backend", "serializePlaylists()");
    }

    /** deserializes all playlists
     *        <BR>- check if file exists
     *        <BR>- heck if file is writable, if not wait and retry
     *        <BR>- deserialize List of playlists
     *        <BR>- copy playlists into a default list model
     * @return success
     */
    private boolean deserializePlaylists() {
        logger.entering("Backend", "deserializePlaylists()");
        logger.info("deserializing playlists");

        boolean success = false;
        File playlistFile = null;
        List<?> playlists = null;

        // try original playlist file
        try {
            playlistFile = new File(PLAYLISTS_GZ_FILENAME);

            if (FileUtils.isWritable(playlistFile, 100000)) {
                // deserialize List of playlists
                XMLDecoder xd = new XMLDecoder(new GZIPInputStream(new FileInputStream(playlistFile), 65535));
                playlists = (List<?>) xd.readObject();
                xd.close();

                success = true;
            }
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
        }

        // try backup playlist file
        if (!success) {
            logger.warning("Deserializing Playlists from backup file");
            try {
                playlistFile = new File(PLAYLISTS_GZ_BACKUP_FILENAME);

                if (FileUtils.isWritable(playlistFile, 100000)) {
                    // deserialize List of playlists
                    XMLDecoder xd = new XMLDecoder(new GZIPInputStream(new FileInputStream(playlistFile), 65535));
                    playlists = (List<?>) xd.readObject();
                    xd.close();

                    success = true;
                }
            } catch (Exception e) {
                logger.warning("Exception raised: " + e.getMessage());
            }
        }

        // try old unzipped playlist file
        if (!success) {
            logger.warning("Deserializing Playlists from old unzipped file");
            try {
                playlistFile = new File(PLAYLISTS_FILENAME);

                if (FileUtils.isWritable(playlistFile, 100000)) {
                    // deserialize List of playlists
                    XMLDecoder xd = new XMLDecoder(new BufferedInputStream(new FileInputStream(playlistFile), 65535));
                    playlists = (List<?>) xd.readObject();
                    xd.close();

                    success = true;
                }
            } catch (Exception e) {
                logger.warning("Exception raised: " + e.getMessage());
            }
        }

        if (success) {
            // copy playlists into a default list model
            this.playlistListModel = new DefaultListModel();
            int size = playlists.size();
            for (int i = 0; i < size; i++) {
                this.playlistListModel.addElement(playlists.get(i));
            }
        }
        logger.exiting("Backend", "deserializePlaylists()");

        return success;
    }

    /** Gets the myPod preferences
     * @return myPod preferences
     */
    public Preferences getPreferences() {
        return preferences;
    }

    /** Sets myPod preferences
     * @param prefs to set
     */
    public void setPreferences(Preferences prefs) {
        preferences = prefs;
    }

    /** serializes all preferences
     *        <BR>- encode and save prefs to file
     *
     * @return success
     */
    private boolean serializePreferences() {
        logger.entering("Backend", "serializePreferences()");

        boolean result = false;
        File preferencesFile = new File(Backend.PREFERENCES_GZ_FILENAME);

        if (preferencesFile.exists()) {
            FileUtils.copy(preferencesFile, new File(PREFERENCES_GZ_BACKUP_FILENAME));
        }

        try {
            XMLEncoder xe = new XMLEncoder(new GZIPOutputStream(new FileOutputStream(preferencesFile), 65535));
            xe.writeObject(this.preferences);
            xe.close();
            result = true;
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }
        logger.exiting("Backend", "serializePreferences()");

        return result;
    }

    /** Deserializes myPods preferences
     *        <BR>- check if the file exists
     *        <BR>-        check if the file is writable
     *        <BR>- load and decode preferences file
     *
     * @return success
     */
    private boolean deserializePreferences() {
        logger.entering("Backend", "deserializePreferences()");
        logger.info("deserializing preferences");

        boolean success = false;
        File prefsFile = null;

        // try original preferences file
        try {
            prefsFile = new File(PREFERENCES_GZ_FILENAME);

            if (FileUtils.isWritable(prefsFile, 100000)) {
                // load and decode prefs file
                XMLDecoder xd = new XMLDecoder(new GZIPInputStream(new FileInputStream(prefsFile), 65535));
                this.preferences = (Preferences) xd.readObject();
                xd.close();

                success = true;
            }
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
        }

        // try backup preferences file
        if (!success) {
            logger.warning("Deserializing preferences from backup file");
            try {
                prefsFile = new File(PREFERENCES_GZ_BACKUP_FILENAME);

                if (FileUtils.isWritable(prefsFile, 100000)) {
                    // load and decode prefs file
                    XMLDecoder xd = new XMLDecoder(new GZIPInputStream(new FileInputStream(prefsFile), 65535));
                    this.preferences = (Preferences) xd.readObject();
                    xd.close();

                    success = true;
                }
            } catch (Exception e) {
                logger.warning("Exception raised: " + e.getMessage());
            }
        }

        // try old, unzipped preferences file
        if (!success) {
            logger.warning("Deserializing preferences from old unzipped file");
            try {
                prefsFile = new File(PREFERENCES_FILENAME);

                if (FileUtils.isWritable(prefsFile, 100000)) {
                    // load and decode prefs file
                    XMLDecoder xd = new XMLDecoder(new BufferedInputStream(new FileInputStream(prefsFile), 65535));
                    this.preferences = (Preferences) xd.readObject();
                    xd.close();

                    success = true;
                }
            } catch (Exception e) {
                logger.warning("Exception raised: " + e.getMessage());
            }
        }

        logger.exiting("Backend", "deserializePreferences()");

        return success;
    }

    /** creates a new playlist from an m3u file.
     *         <BR>- determine name of the playlist from filename
     *
     * @param file m3u playlist
     * @return playlist loaded from file
     */
    public Playlist loadPlaylistM3U(File file) {
        logger.entering("Backend", "loadPlaylistM3U");

        Playlist playlist = null;

        try {
            // determine name of the playlist from filename
            String name = file.getName();
            if (name.toLowerCase().endsWith(".m3u")) {
                name = name.substring(0, name.length() - 4);
            }

            // open file and get a  buffered reader
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(new FileInputStream(file)));

            List<Long> oids = new ArrayList<Long>();
            Long oid = null;
            List<String> artistFilter = new ArrayList<String>();
            List<String> genreFilter = new ArrayList<String>();
            List<String> yearFilter = new ArrayList<String>();

            // read the file line by line and parse m3u playlist
            String line = br.readLine();
            while (line != null) {
                try {
                    line.trim();

                    // check if we have a line with path and filename
                    if (!line.startsWith("#")) {
                        // try to find file in the data pool			
                        oid = getDataPool().getOid(line);

                        // add clip to playlist
                        if (oid != null) {
                            oids.add(oid);
                        } else {
                            logger.info("unable to find clip " + line + " in the data pool.");
                        }
                    } else {
                        // check for filter definition
                        if (line.startsWith("#mypod artist")) {
                            artistFilter.add(line.substring(13).trim());
                        } else if (line.startsWith("#mypod genre")) {
                            genreFilter.add(line.substring(12).trim());
                        } else if (line.startsWith("#mypod year")) {
                            yearFilter.add(line.substring(11).trim());
                        }
                    }

                    // read next line
                    line = br.readLine();
                } catch (Exception e) {
                    ;
                }
            }

            // close file
            br.close();

            // create a new playlist object
            if ((artistFilter.size() > 0) || (genreFilter.size() > 0) || (yearFilter.size() > 0)) {
                // create autoplaylist and add filter
                playlist = new AutoPlaylist(name);

                if (artistFilter.size() > 0) {
                    ((AutoPlaylist) playlist).setArtistFilter(artistFilter);
                    ((AutoPlaylist) playlist).setArtistFilterEnabled(true);
                }
                if (genreFilter.size() > 0) {
                    ((AutoPlaylist) playlist).setGenreFilter(genreFilter);
                    ((AutoPlaylist) playlist).setGenreFilterEnabled(true);
                }
                if (yearFilter.size() > 0) {
                    ((AutoPlaylist) playlist).setYearFilter(yearFilter);
                    ((AutoPlaylist) playlist).setYearFilterEnabled(true);
                }
            } else {
                // create playlist
                playlist = new Playlist(name);
            }

            // add clips
            playlist.addClips(oids);
        } catch (Exception e) {
            logger.warning("exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        logger.exiting("Backend", "loadPlaylistM3U");

        return playlist;
    } // import playlist

    /** Saves a playlist as m3u file.
     *        <br>- make sure, that the filename ends to .m3u
     *        <br>- create file
     *        <br>- write file header
     *        <br>- write filter information for autoplaylists
     *        <br>-
     *
     * @param filename of the playlist
     * @param playlist to save
     */
    public void savePlaylistM3U(String filename, Playlist playlist) {
        try {
            // make sure, that the filename ends to .m3u
            if (!filename.endsWith(".m3u") && !filename.endsWith(".M3U") && !filename.endsWith(".M3u") && !filename.endsWith(".m3U")) {
                filename += ".m3u";
            }

            // open file
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(filename)));

            // write file header
            fos.write(("#EXTM3U" + "\n").getBytes());

            // write filter infos
            if (playlist.isAutoplaylist()) {
                Iterator<?> filterIter;
                List<?> filter = ((AutoPlaylist) playlist).getArtistFilter();
                if (filter != null) {
                    filterIter = filter.iterator();
                    while (filterIter.hasNext()) {
                        fos.write(("#mypod artist " + filterIter.next() + "\n").getBytes());
                    }
                }

                filter = ((AutoPlaylist) playlist).getGenreFilter();
                if (filter != null) {
                    filterIter = filter.iterator();
                    while (filterIter.hasNext()) {
                        fos.write(("#mypod genre " + filterIter.next() + "\n").getBytes());
                    }
                }

                filter = ((AutoPlaylist) playlist).getYearFilter();
                if (filter != null) {
                    filterIter = filter.iterator();
                    while (filterIter.hasNext()) {
                        fos.write(("#mypod year " + filterIter.next() + "\n").getBytes());
                    }
                }
            }

            // append all tracks
            MP3Meta meta;
            List<Long> oids = playlist.getList();
            Iterator<Long> oidIter = oids.iterator();
            while (oidIter.hasNext()) {
                meta = getDataPool().getMeta(oidIter.next());

                fos.write(new StringBuffer("#EXTINF:").append(meta.getDuration()).append(",").append(meta.get("Artist")).append(" - ").append(meta.get("Title")).append("\n").toString().getBytes());
                fos.write(new StringBuffer(meta.getFile().getPath()).append("\n").toString().getBytes());
            }

            // close file
            fos.close();
        } catch (java.io.FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, GuiUtils.getStringLocalized("resource/language", "couldNotWritePlaylistTo") + ": \n" + filename + "\n" + GuiUtils.getStringLocalized("makeSureAllDirsExistAndYouAreAllowedToWriteInto") + ".", GuiUtils.getStringLocalized("anErrorOccured"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Removes clips from myPod
     * First the clip is removed from all playlist, the from data pool.
     *
     * @param oids of the clips to delete
     */
    public void removeClipsFromMyPod(List<Long> oids) {
        logger.entering("Backend", "removeClipsFromMyPod");

        // do it for all clips
        Iterator<Long> iter = oids.iterator();

        while (iter.hasNext()) {
            // remove clip from myPod
            this.removeClipFromMyPod(iter.next());
        }

        logger.exiting("Backend", "removeClipsFromMyPod");
    }

    /**
     * Removes a clip from myPod
     * First the clip is removed from all playlist, the from data pool.
     *
     * @param oid of the clip to remove
     */
    private void removeClipFromMyPod(Long oid) {
        logger.entering("Backend", "removeClipFromMyPod");

        logger.fine("deleting oid:" + oid);

        // remove clip from all playlists - DO THIS BEFORE REMOVING THE CLIP FROM DATA POOL
        int listCnt = this.playlistListModel.size();
        for (int i = 0; i < listCnt; i++) {
            getPlaylist(i).remove(oid);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("removed oid: " + oid + " from playlist at " + i);
            }
        }

        // if we are in an spreader view, the playlist displayed must be handled here ...
        if (!this.playlistListModel.contains(this.clipsListTableModel.getPlaylist()) && this.clipsListTableModel.getPlaylist().containsClip(oid)) {
            this.clipsListTableModel.getPlaylist().remove(oid);
        }

        // remove clip from data pool
        dataPool.removeClip(oid);

        logger.exiting("Backend", "removeClipFromMyPod");
    }

    /** Deletes clips from myPod and filesystem.
     *
     * @param oids to delete media files for
     */
    public void deleteClips(List<Long> oids) {
        logger.entering("Backend", "deleteClips");

        // do it for all clips
        Iterator<Long> iter = oids.iterator();
        Long oid = null;
        boolean deleted = false;

        while (iter.hasNext()) {
            oid = iter.next();

            logger.fine("deleting oid:" + oid);

            // remove clips media file
//            deleted = FileUtils.delete(new File(this.dataPool.getMeta(oid).getFile().getPath()));

            // remove clip from all myPod
            if (deleted) {
                this.removeClipFromMyPod(oid);
            }
        }

        logger.exiting("Backend", "deleteClips");
    }

    /** Removes a list of clips vom a playlist
     * @param playlist to remove clips from
     * @param oids of the clips to remove
     */
    public void removeClipsFromPlaylist(Playlist playlist, List<Long> oids) {
        Long oid = null;

        // remove clips from playlist
        playlist.remove(oids);

        // if we are in an spreader view, the playlist displayed must be handled here ...
        if (!this.playlistListModel.contains(this.clipsListTableModel.getPlaylist())) {
            for (Iterator<Long> clipIter = oids.iterator(); clipIter.hasNext();) {
                oid = clipIter.next();
                if (this.clipsListTableModel.getPlaylist().containsClip(oid)) {
                    this.clipsListTableModel.getPlaylist().remove(oid);
                }
            }
        }
    }

    /** Gets all clips that are missing.
     *         all clips in the data pool are checked for their files.
     *         If the file can not be opended, the oid is added to the
     *         List of missing clips
     *
     * @return List containing oids of missing clips or null, if action was canceled
     * @param dialog to show the progress of the action
     */
    public List<Long> scanForMissingMediaFiles(ProgressDialog dialog) {
        logger.entering("Backend", "scanForMissingMediaFiles");

        List<Long> missingClips = new ArrayList<Long>();
        Set<Long> oids = getDataPool().getAllOid();
        String path = null;
        DataPool dp = this.getDataPool();

        // do some gui stuff
        dialog.setStatusText(GuiUtils.getStringLocalized("searchingForMissingMediaFiles"));
        dialog.setProgressMax(oids.size());

        // iterate over all clips in the data pool
        for (Long oid : oids) {
            path = dp.getMeta(oid).getFile().getPath();

            // do some gui stuff
            dialog.setProgressValue(dialog.getProgressValue() + 1);
            dialog.setClipText(dp.getMeta(oid).getFile().getName());

            // check if the file exists
            if (!new File(path).exists()) {
                missingClips.add(oid);
            }

            // check if user aborted reorganizing
            if (Thread.interrupted()) {
                return null;
            }
        }

        logger.fine(missingClips.size() + " missing clips found");

        logger.exiting("Backend", "scanForMissingMediaFiles");

        return missingClips;
    }

    /** serializes iPod mapper.
     *
     * @param mapper to serialize
     */
    public void serializeIPodMapper(Map<Long, Integer> mapper) {
        logger.entering("Backend", "serializeIPodMapper()");
        try {
            XMLEncoder xe = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(Backend.IPOD_MAPPER_FILENAME)));
            xe.writeObject(mapper);
            xe.close();
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }
        logger.exiting("Backend", "serializeIPodMapper()");
    }

    /**
     * deserializes iPod mapper.
     * 
     * @return mapper object
     */
    @SuppressWarnings("unchecked")
    private Map<Long, Integer> deserializeIPodMapper() {
        logger.entering("Backend", "deserializeIPodMapper()");

        Map<Long, Integer> mapper = null;

        try {
            XMLDecoder xd = new XMLDecoder(new BufferedInputStream(new FileInputStream(Backend.IPOD_MAPPER_FILENAME)));
            mapper = (Map) xd.readObject();
            xd.close();
        } catch (Exception e) {
            logger.info("impapper not found");
        }
        logger.exiting("Backend", "deserializeIPodMapper()");

        if (mapper == null) {
            mapper = new HashMap<Long, Integer>();
            logger.info("new iPod Mapper object created.");
        }

        return mapper;
    }

    /**
     * Synchronizes myPod playlists with iPod.
     * <li> load iTunes database from iPod
     * <li> load iMapper
     * <li> determine all clips to synchronize
     * <li> determine clips to remove from iPod
     * <li> determine clips to move to iPod
     * <li> check space left on iPod
     * <li> remove clips from iPod
     * <li> remove empty playlists from iPod
     * <li> copy clips to iPod
     * <li> create playlists on iPod
     * <li> save iTunes database to iPod
     * <li> serialize iMapper
     * 
     * @param dialog to show progress
     * @return space left on iPod after sync in bytes
     * @throws IOException
     */
    public long synchronizeIPod(de.axelwernicke.mypod.gui.IPodSyncDialog dialog) throws IOException {
        // get iTunes DB
        dialog.totalProgressBar.setIndeterminate(true);
        dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("loadingDababase"));
        iPod.getITunesDB();
        dialog.totalProgressBar.setIndeterminate(false);

        // deserialize iPod Mapper, this hashmap contains oids as key and iPod fileindizes as content.
        Map<Long, Integer> iMapper = deserializeIPodMapper();

        // set dialog
        dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("determiningFilesToSync"));
        dialog.totalProgressBar.setValue(5);

        // get a list of all oids to synchronize to the iPod
        List<Long> syncOids = getClipsToSync();

        // check clips to remove from iPod, clipsToRemove contains oids
        List<Long> clipsToRemove = getClipsToRemoveFromIPod(syncOids, iMapper);
        long removeSize = iPod.getClipsSize(clipsToRemove, iMapper);

        // check clips to move to iPod
        List<Long> clipsToMove = getClipsToMoveToIPod(syncOids, iMapper);
        long moveSize = getClipsTotalSize(clipsToMove);

        // check free space on iPod
        long spaceLeft = iPod.getDiscSpaceFree() - moveSize + removeSize;
        if (spaceLeft > 0) {
            // update dialog
            dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("removingClips"));
            dialog.totalProgressBar.setValue(10);

            // remove clips from iPod
            iPod.removeClips(clipsToRemove, iMapper, dialog);

            // remove empty playlists
            iPod.removeEmptyPlaylists();

            // copy clips to iPod
            dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("copyingFiles"));
            dialog.totalProgressBar.setValue(15);
            dialog.totalSizeContent.setText(GuiUtils.formatFilesize(moveSize));
            iPod.addClips(dataPool, clipsToMove, iMapper, dialog);

            // create playlists
            dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("creatingPlaylists"));
            dialog.totalProgressBar.setValue(90);
            iPod.createPlaylists(this.playlistListModel, iMapper, dialog);

            // update dialog
            dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("savingDatabase"));
            dialog.totalProgressBar.setValue(95);

            // save iTunes DB
            dialog.totalProgressBar.setIndeterminate(true);
            iPod.saveITunesDB();
            dialog.totalProgressBar.setIndeterminate(false);

            // update dialog
            dialog.totalProgressBar.setValue(100);

            // serialize iMapper
            serializeIPodMapper(iMapper);
        }

        return spaceLeft;
    }

    /**
     * Gets all clips to synchronize with iPod
     * 
     * @return List of all clips to synchronize
     */
    private List<Long> getClipsToSync() {
        List<Long> syncOids = new ArrayList<Long>();
        Playlist playlist = null;
        Long clip = null;

        // iterate over all playlists
        int size = this.playlistListModel.getSize();
        for (int i = 0; i < size; i++) {
            // get playlist
            playlist = getPlaylist(i);

            // check if playlist is synchronized to iPod
            if (playlist.isIPodSync()) {
                // iterate over all clips in the playist
                int listSize = playlist.getTotalClips();
                for (int y = 0; y < listSize; y++) {
                    // get current clip
                    clip = playlist.getClipAt(y);

                    // check if clip is in result set already
                    if (!syncOids.contains(clip)) {
                        syncOids.add(clip);
                    }
                } // for all clips
            } // if playlist is ipod sync
        } // for all playlists

        return syncOids;
    }

    /**
     * check clips to remove from iPod ->clips are to be removed if clip is on
     * the iPod but not on a playlist to synchronize
     * 
     * @param clipsToSync List of oids - clips to synchronize
     * @param iMapper mapping from oids to iPod song id
     * @return fileIndex of the clips to remove
     */
    private List<Long> getClipsToRemoveFromIPod(List<Long> clipsToSync, Map<Long, Integer> iMapper) {
        List<Long> clips2Remove = new ArrayList<Long>();

        // iterate over all oids on the iPod
        for (Long oid : iMapper.keySet()) {

            // clip must be removed, if it isn't in a playlist to sync
            if (!clipsToSync.contains(oid)) {
                clips2Remove.add(oid);
            }
        }

        logger.fine("found " + clips2Remove.size() + " clips to remove from iPod");

        return clips2Remove;
    } // getClipsToRemoveFromIPod

    /**
     * Gets clips to move to iPod ->clips are moved if clip is on a playlist,
     * but not on the iPod
     * 
     * @param syncOids oids of the clips to synchronize
     * @param iMapper hashtable that defines which songs are synchronized yet
     * @return List of oids to move to iPod
     */
    private List<Long> getClipsToMoveToIPod(List<Long> syncOids, Map<Long, Integer> iMapper) {
        List<Long> clips2move = new ArrayList<Long>(syncOids.size());

        // iterate over the playlist
        for (Long oid : syncOids) {
            if (!iMapper.containsKey(oid)) {
                clips2move.add(oid);
            }
        }

        logger.fine("found " + clips2move.size() + " clips to move to iPod");

        return clips2move;
    }

    /**
     * Gets the total count of clips known by myPods data pool
     * <li> get the size of a list of oids for all clips
     * 
     * @return total count of clips known managed by myPod
     */
    public long getClipsTotalCount() {
        long result = -1;
        try {
            result = getDataPool().getAllOid().size();
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Gets the total size of all clips known by myPods data pool - get a list
     * of oids for all clips
     * <li> summarize the size of each clip
     * 
     * @return total size of clips known managed by myPod in bytes
     */
    public long getClipsTotalSize() {
        long result = 0;

        try {
            for (Long oid : getDataPool().getAllOid()) {
                result += dataPool.getMeta(oid).getFile().length();
            }
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
            result = -1;
        }

        return result;
    }

    /**
     * Gets the total duration of all clips known by myPods data pool - get a
     * list of oids for all clips <BR>
     * <li> summarize the duration of each clip
     * 
     * @return total duration of clips known managed by myPod
     */
    public long getClipsTotalTime() {
        long result = 0;

        try {
            for (Long oid : getDataPool().getAllOid()) {
                result += dataPool.getMeta(oid).getDuration();
            }
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
            result = -1;
        }

        return result;
    }

    /**
     * Gets the summarized filesize for all clips.
     * It tries to determine clip size from local db via oid. The oid _must_ be known by myPod!
     *
     * @param oids List of oids to summarize filesizes
     * @return  filesize of all specified oids
     */
    public long getClipsTotalSize(List<Long> oids) {
        long size = 0;
        DataPool dp = this.getDataPool();

        for (Long oid : oids) {
            MP3Meta meta = dp.getMeta(oid);
            if (meta != null) {
                size += meta.getFile().length();
            }
        }

        logger.fine("summarized size of " + oids.size() + " clips was " + size);

        return size;
    }

    /**
     * Sends a playlist to an external player
     * <li> determine parameter (add or append)
     * <li> check that tmp directory exists to store playlist in
     * <li> check that configured player can be found
     * <li> store playlist
     * <li> call player
     *
     * @param playlist to play
     * @param append if true, playlist is appended to the clips on the player
     * @throws IOException
     */
    public void playExtern(Playlist playlist, boolean append) throws IOException {
        if (playlist != null) {
            String parameter = (append) ? preferences.getAppendParameter() : preferences.getAddParameter();
            String fs = File.separator;
            
            // check external player
            String playerPath = preferences.getPlayerPath();
            if (!new File(playerPath).exists()) {
                JOptionPane.showMessageDialog(null, GuiUtils.getStringLocalized("couldNotFindExternalPlayer") + ": \n" + playerPath, GuiUtils.getStringLocalized("checkConfiguration"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // create tmp playlist
            String playlistName = System.getProperty("user.dir") + fs + "tmp" + fs + "play.m3u";
            savePlaylistM3U(playlistName, playlist);
            
            // create and execute command
            String command = "\"" + playerPath + "\" " + parameter + " \"" + playlistName + "\"";
            java.lang.Runtime.getRuntime().exec(command);
        }
    }

    /** Getter for property iPod.
     *
     * @return Value of property iPod.
     */
    public IPod getIPod() {
        return iPod;
    }

    /**
     * Setter for property iPod.
     *
     * @param iPod New value of property iPod.
     */
    public void setIPod(IPod iPod) {
        this.iPod = iPod;
    }

    /**
     * Gets a List containing all clips that are below a given directory
     * @param baseDirectory to determine clips below
     * @return List of oids
     */
    private List<Long> getClipsBelowDirectory(String baseDirectory) {
        List<Long> oids = new ArrayList<Long>();
        for (Long oid : this.getDataPool().getAllOid()) {
            if (getDataPool().getMeta(oid).getFile().getPath().startsWith(baseDirectory + File.separator)) {
                oids.add(oid);
            }
        }

        return oids;
    }

    /**
     * Reorganizes filestructure and renames files based on id3 tags
     *
     * <BR>determine clips that are below base directory
     * <BR>for all clips to reorganize: determine new pathname
     * <BR>for all clips to reorganize: determine new filename
     * <BR>for all clips to reorganize: create new file
     * <BR>
     *
     * @param dialog reorganizing progress dialog
     * @param removeEmptyDirectories flag, if true - empty directories are removed
     * @param baseDirectory all files below are reorganized
     * @param dirStructureIndex index that determines how the filestructure is reorganized
     * @param filenameStructureIndex index that determines how the files are renamed
     */
    public void reorganizeClips(de.axelwernicke.mypod.gui.ReorganizeClipsProgressDialog dialog, String baseDirectory, int dirStructureIndex, int filenameStructureIndex, boolean removeEmptyDirectories) {
        logger.entering("de.axelwernicke.mypod.Backend", "reorganizeClips");

        // statistics for gui
        int filesMoved = 0;
        int filesRenamed = 0;
        boolean dirChanged;
        boolean filenameChanged;

        // do some gui stuff
        dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("determiningClipsToReorganize"));
        dialog.totalProgressBar.setIndeterminate(true);

        // determine clips that are below base directory
        logger.fine("determining clips");
        Long oid = null;
        List<Long> oids = null;
        if ((dirStructureIndex != 0) || (filenameStructureIndex != 0)) {
            oids = getClipsBelowDirectory(baseDirectory);
        }

        // check if user aborted reorganizing
        if (Thread.interrupted()) {
            return;
        }

        // do some gui stuff
        dialog.totalProgressBar.setIndeterminate(false);
        dialog.totalProgressBar.setMaximum(oids.size());

        MP3Meta meta = null;
        String album;
        String artist;
        String genre;
        String title;
        String track = null;
        String oldDirectoryName;
        String newDirectoryName;
        String newFilename = null;
        File newFile;
        File newDir = null;

        // iterate over clips to reorganize
        int size = oids.size();
        for (int i = 0; i < size; i++) {
            newFilename = null;
            newFile = null;
            newDir = null;

            // get meta data for the current clip
            oid = oids.get(i);
            meta = getDataPool().getMeta(oid);

            // get valid meta data values to create new directory and filenames from
            album = FileUtils.validateDirectoryName((String) meta.get("Album"));
            if ((album == null) || album.equals("")) {
                album = GuiUtils.getStringLocalized("unknown");
            }

            artist = FileUtils.validateDirectoryName((String) meta.get("Artist"));
            if ((artist == null) || artist.equals("")) {
                artist = GuiUtils.getStringLocalized("unknown");
            }

            genre = FileUtils.validateDirectoryName((String) meta.get("Genre"));
            if ((genre == null) || genre.equals("")) {
                genre = GuiUtils.getStringLocalized("unknown");
            }

            title = FileUtils.validateDirectoryName((String) meta.get("Title"));
            if ((title == null) || title.equals("")) {
                title = GuiUtils.getStringLocalized("unknown");
            }

            int tmpTrack = Integer.parseInt((String) meta.get("Track"));
            if (tmpTrack >= 10) {
                track = String.valueOf(tmpTrack);
            } else if (tmpTrack < 0) {
                track = "00";
            } else {
                track = "0" + String.valueOf(tmpTrack);
            }

            // do some gui stuff
            dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("reorganizingClips") + " (" + i + "/" + oids.size() + ")");
            dialog.totalProgressBar.setValue(i);
            dialog.clipContentLabel.setText(artist + " - " + title);
            dialog.clipsMovedContentLabel.setText(String.valueOf(filesMoved));
            dialog.clipsRenamedContentLabel.setText(String.valueOf(filesRenamed));
            dialog.directoriesRemovedContentLabel.setText(String.valueOf(0));

            try {
                // determine new directory name
                oldDirectoryName = meta.getFile().getPath().substring(0, meta.getFile().getPath().lastIndexOf(File.separator));
                switch (dirStructureIndex) {
                case 1:
                    newDirectoryName = baseDirectory + File.separator + artist;
                    break;
                case 2:
                    newDirectoryName = baseDirectory + File.separator + artist + File.separator + album;
                    break;
                case 3:
                    newDirectoryName = baseDirectory + File.separator + genre;
                    break;
                case 4:
                    newDirectoryName = baseDirectory + File.separator + genre + File.separator + album;
                    break;
                default:
                    newDirectoryName = oldDirectoryName;
                    break;
                }

                // determine file name
                switch (filenameStructureIndex) {
                case 1:
                    newFilename = track + " - " + title + ".mp3";
                    break;
                default:
                    newFilename = meta.getFile().getName();
                    break;
                }

                // create new file
                newFile = new File(newDirectoryName + File.separator + newFilename);

                // check if directory change
                dirChanged = !(new File(newDirectoryName).equals(new File(oldDirectoryName)));
                if (dirChanged) {
                    filesMoved++;

                    logger.finer("dir changed from " + oldDirectoryName + " to " + newDirectoryName);

                    // check if the directory exists, create if not
                    newDir = new File(newDirectoryName);
                    if (!newDir.exists()) {
                        newDir.mkdirs();
                    }

                    if (!newDir.exists() || !newDir.canWrite()) {
                        // if the dir is not there or not readable yet, we are in serious trouble...
                        throw new Exception("could not get directory " + newDir + " writable");
                    }
                }

                // check if filename changed
                filenameChanged = !(new File(newFilename).equals(new File(meta.getFile().getName())));
                if (filenameChanged) {
                    filesRenamed++;

                    logger.finer("filename changed from " + meta.getFile().getName() + " to " + newFilename);

                    // check if the file exists, change filename if so
                    if (newFile.exists()) {
                        String tmpFileName = newFilename.substring(0, newFilename.lastIndexOf("."));
                        int cnt = 0;
                        do {
                            // check file exteded by counter
                            newFile = new File(newDirectoryName + File.separator + tmpFileName + " (" + cnt + ").mp3");
                            cnt++;
                        } while (newFile.exists() && (cnt < 100000));
                    }

                    if (newFile.exists()) // if the dir is not there or not readable yet, we are in serious trouble...
                     {
                        throw new Exception("could not get new filename last try was: " + newFile);
                    }
                }

                // move file to new location
                if (dirChanged || filenameChanged) {
                    boolean success = FileUtils.move(new File(meta.getFile().getPath()), newFile);

                    // change meta data
                    if (success) {
                        dataPool.updateFilePath(oid, newFile.getPath());
                    } else {
                        logger.info("moving from " + new File(meta.getFile().getPath()) + " to " + newFile + " failed");
                    }
                }
            } catch (Exception e) {
                logger.warning("exception raised: " + e.getMessage());
            }

            // check if user aborted reorganizing
            if (Thread.interrupted()) {
                return;
            }
        } // for all files to reorganize

        // delete empty directories
        if (removeEmptyDirectories) {
            int totalDirsDeleted = 0;
            int dirsDeleted = 0;

            // do some gui stuff
            dialog.statusContentLabel.setText(GuiUtils.getStringLocalized("determiningDirectoriesToRemove"));
            dialog.totalProgressBar.setIndeterminate(true);

            // get all directories recursively
            List<File> dirs = FileUtils.getAllDirectories(new File(baseDirectory));

            // while empty directories found
            int cnt = 0;
            do {
                dirsDeleted = FileUtils.deleteEmptyDirectories(dirs);
                totalDirsDeleted += dirsDeleted;
                cnt++;

                // update gui
                dialog.directoriesRemovedContentLabel.setText(String.valueOf(totalDirsDeleted));

                // check if user aborted reorganizing
                if (Thread.interrupted()) {
                    return;
                }
            } while ((dirsDeleted != 0) && (cnt < 50));

            logger.info("" + totalDirsDeleted + " directories deleted in " + cnt + " loops");
        }

        logger.exiting("de.axelwernicke.mypod.Backend", "reorganizeClips");
    }

    /**
     * Set new id3 tags for a couple of clips
     *
     * @param dialog to visualize progress
     * @param newMeta mp3 meta data to set
     * @param oids of the clips to update id3 tags
     */
    public void setClipsProperties(ProgressDialog dialog, MP3Meta newMeta, List<Long> oids) {
        MP3Meta origMeta;
        Long oid;
        int clipCnt = oids.size();

        // do some gui stuff
        dialog.setProgressBounds(0, clipCnt);

        // set new meta data for all clips in oids
        for (int cnt = 0; cnt < clipCnt; cnt++) {
            try {
                oid = oids.get(cnt);
                origMeta = dataPool.getMeta(oid);

                if (origMeta != null) {
                    // update gui
                    dialog.setStatusText(GuiUtils.getStringLocalized("updatingID3Tags") + " ( " + (cnt + 1) + " / " + clipCnt + " )");
                    dialog.setProgressValue(cnt);
                    dialog.setClipText(origMeta.getFile().getName());

                    // do tag updating
                    ID3Utils.updateMp3VdHeide(origMeta, newMeta);

                    // update clip from file
                    // TODO if we can be sure that tag writing works, we should avoid scanning and set data directly
                    getDataPool().updateData(oid, ID3Utils.scanMp3VdHeide(new File(origMeta.getFile().getPath())));
                }
            } catch (Exception e) {
                logger.warning("Exception raised: " + e.getMessage());
                e.printStackTrace();
            }

            // check if user canceled action
            if (Thread.interrupted()) {
                return;
            }

            // reset orig meta
            origMeta = null;
        }
    }

    /** Relocates clips if their media files where moved.
     *
     *         e.g: a:\bbb\ccc\ddd\fool.mp3 --> k:\lll\mmm\nnn\ddd\fool.mp3
     *                  - baseSrc - | -- file --            -- baseDest -- | -- file --
     *
     * @param dialog to show progress
     * @param missingClips clips where media file is missing
     */
    public void relocateClips(ProgressDialog dialog, Collection<Long> missingClips) {
        Long oid;
        MP3Meta meta;
        Map<String, String> moves = new HashMap<String, String>();
        Iterator<String> movesIter;
        boolean found;
        String oldLocation;
        String newLocation;
        String recentMoveSrc;
        File recentPath = new File(this.getPreferences().getRecentScanPath());

        // prepare dialog
        dialog.setStatusText(GuiUtils.getStringLocalized("relocateMediaFiles"));
        dialog.setProgressBounds(0, missingClips.size());
        dialog.setProgressValue(0);

        // do it for all clips with missing media files
        Iterator<Long> clipsIter = missingClips.iterator();
        while (clipsIter.hasNext()) {
            try {
                //do some preparations for the clip
                oid = clipsIter.next();
                meta = this.getDataPool().getMeta(oid);
                oldLocation = meta.getFile().getPath();
                newLocation = "";
                found = false;

                // update dialog
                dialog.setProgressValue(dialog.getProgressValue() + 1);
                dialog.setClipText(oldLocation);

                // check if a known move matches
                movesIter = moves.keySet().iterator();
                while (!found && movesIter.hasNext()) {
                    recentMoveSrc = movesIter.next();
                    if (meta.getFile().getPath().startsWith(recentMoveSrc)) {
                        // build potential new location and check if the new location exists
                        newLocation = moves.get(recentMoveSrc) + oldLocation.substring(recentMoveSrc.length());
                        if ((new File(newLocation)).exists()) {
                            found = true;
                        }

                        logger.fine("Tried " + newLocation + " as new location (recent move), result : " + found);
                    }
                } // for all recent moves

                if (!found) {
                    // ask the user
                    JFileChooser fc = new JFileChooser();
                    fc.setDialogTitle(GuiUtils.getStringLocalized("findNewLocationFor") + " " + oldLocation);
                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fc.setMultiSelectionEnabled(false);
                    fc.setCurrentDirectory(recentPath);
                    int res = fc.showOpenDialog(dialog);
                    if (res == JFileChooser.APPROVE_OPTION) {
                        if (fc.getSelectedFile().exists()) {
                            newLocation = fc.getSelectedFile().toString();
                            recentPath = fc.getSelectedFile();
                            found = true;
                        }

                        logger.fine("Tried " + newLocation + " as new location (user selection), result : " + found);
                    }
                }

                if (!found) {
                    // if still not found the user gave us nonsense ... ask for skip or abort
                    int result = JOptionPane.showInternalConfirmDialog(null, GuiUtils.getStringLocalized("doYouWantToCancelTheAction"), GuiUtils.getStringLocalized("pleaseConfirm"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        dialog.interrupt();
                    }
                } else {
                    // split base and extension to remember move
                    // first we need to know how far meta.getFilename and newLocation equal _from the end_ (!!)
                    int oldLength = oldLocation.length();
                    int newLength = newLocation.length();
                    int minLen = (oldLength < newLength) ? oldLength : newLength;
                    int equalChars = 0;
                    boolean equal;

                    do {
                        equal = (oldLocation.charAt(oldLength - 1 - equalChars) == newLocation.charAt(newLength - 1 - equalChars));
                    } while (equal && (equalChars++ < minLen));

                    // extract and store base path
                    moves.put(oldLocation.substring(0, oldLength - equalChars), newLocation.substring(0, newLength - equalChars));

                    logger.fine("put new move: " + oldLocation.substring(0, oldLength - equalChars) + " : " + newLocation.substring(0, newLength - equalChars));

                    // propagate changes to the data pool
                    dataPool.updateFilePath(oid, newLocation);
                }
            } catch (Exception ex) {
                logger.warning("Exception raised: " + ex.getMessage());
                ex.printStackTrace();
            }

            // return if user cancelled the action
            if (Thread.interrupted()) {
                return;
            }
        } // do for all clips with missing media files
    }
}
