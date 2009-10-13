// DataPool
// $Id: DataPool.java,v 1.25 2003/08/03 09:45:28 axelwernicke Exp $
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
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

import de.axelwernicke.mypod.util.FileUtils;


/**
 * holds information about all scanned mp3 files
 * 
 * @author axel wernicke
 */
public class DataPool {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** filename to serialize data */
    private static String DATA_FILENAME = "data" + File.separator + "data";

    /** filename for backup of serialized data */
    private static String DATA_GZ_FILENAME = "data" + File.separator + "data.gz";

    /** filename to backup data file */
    private static String DATA_GZ_BACKUP_FILENAME = "data" + File.separator + "data.gz.bak";

    /**
     * contains all MP3Meta objects for scanned files, access by oid - oid is
     * long!
     */
    private Map<Long, MP3Meta> data = null;

    /** list of filenames and oids to enhance performance */
    private Map<String, Long> filenameCache = null;

    /**
     * Shuts the datapool down
     */
    protected void shutdown() {
        logger.entering("DataPool", "shutdown()");
        try {
            serializeData();
            logger.info("data object serialized");
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }
        logger.exiting("DataPool", "shutdown()");
    }

    /**
     * checks the size of data and filename cache.
     * 
     * @return true, if data and filename cache have the same size
     */
    private boolean isCacheValid() {
        return data.size() == filenameCache.size();
    }

    /**
     * gets the oid of a clip from its filename
     * 
     * @param filename filename of the clip
     * @return oid for the clip
     */
    protected long getOid(String filename) {
        return filenameCache.get(filename);
    }

    /**
     * Checks if an oid is in the data pool
     * 
     * @param oid to check
     * @return true, if oid is in the data pool
     */
    protected boolean contains(long oid) {
        return data.containsKey(oid);
    }

    /**
     * gets the oids for all music in the datapool
     * 
     * @return set of oids
     */
    protected Set<Long> getAllOid() {
        return data.keySet();
    }

    /**
     * gets information about an mp3 from database.
     * 
     * @param file file to get the information for
     * @return information for an mp3 file
     */
    @SuppressWarnings("unused")
    private MP3Meta getMeta(File file) {
        MP3Meta meta = null;
        String path = file.getPath();

        if (filenameCache.containsKey(path)) {
            meta = getMeta(filenameCache.get(path));
        } else {
            logger.warning("tried to get nonexisting meta data for file " + file);
        }
        return meta;
    }

    /**
     * gets all information about a track
     * 
     * @param oid oid of the track to get information for
     * @return meta data for the track
     */
    public MP3Meta getMeta(long oid) {
        MP3Meta meta = null;
        if (data.containsKey(oid)) {
            meta = data.get(oid);
        } else {
            logger.warning("tried to get nonexisting meta data for oid " + oid);
        }

        return meta;
    }

    /**
     * gets from data pool the date when the selected track was modified the
     * last time.
     * 
     * @return date in milliseconds
     * @param file file to get the information for
     */
    protected long getLastModified(File file) {
        long lastModified = -1;
        String path = file.getPath();

        if (filenameCache.containsKey(path)) {
            lastModified = getLastModified(filenameCache.get(path));
        }

        return lastModified;
    }

    /**
     * gets the last modified date for a clip
     * 
     * @param oid oid of the clip
     * @return timestamp when the clip was modified
     */
    private long getLastModified(long oid) {
        long lastModified = -1;
        MP3Meta meta = getMeta(oid);

        if (meta != null) {
            lastModified = meta.getFile().lastModified();
        }

        return lastModified;
    }

    /**
     * checks if the clip specified by path and filename is in the data pool
     * 
     * @param file filename and path of the clip
     * @return true, if the file is known to the data pool
     */
    protected boolean isInPool(File file) {
        return filenameCache.containsKey(file.getPath());
    }

    /**
     * Updates the id3 tags in the clip from the meta data object.
     * 
     * @param mp3Meta to update from
     * @param file to set the meta data for
     */
    protected void updateData(File file, MP3Meta mp3Meta) {
        logger.entering("DataPool", "updateData");

        Long oid = null;

        // find the meta data to update by filename / oid
        if (file != null) {
            oid = this.getOid(file.getPath());
        }

        // set new meta data
        if ((oid != null) && (mp3Meta != null)) {
            data.put(oid, mp3Meta);

            logger.fine("updated clip meta data for file : " + file);
        }

        logger.exiting("DataPool", "updateData");
    }

    /**
     * Updates the id3 tags in the clip from the meta data object.
     * 
     * @param mp3Meta to update from
     * @param file to set the meta data for
     */
    protected void updateData(Long oid, MP3Meta mp3Meta) {
        logger.entering("DataPool", "updateData");

        // set new meta data
        if ((oid != null) && (mp3Meta != null)) {
            data.put(oid, mp3Meta);
        }

        logger.exiting("DataPool", "updateData");
    }

    /**
     * Adds meta data object to the data pool.<br>
     * A new (unique) oid is generated.<br>
     * data and filename cache are updated
     * 
     * @param mp3Meta set of meta data to add to the pool
     * @param file the set of meta data belongs to
     */
    protected void addData(File file, MP3Meta mp3Meta) {
        logger.entering("DataPool", "addData");

        if (mp3Meta != null) {
            // get a new oid, and make sure its unique
            int cnt = 0;
            Long newOid = null;
            do {
                newOid = new Long(System.currentTimeMillis());
                cnt++;
            } while (data.containsKey(newOid) && (cnt < 10000));

            // put data and update filenamecache
            data.put(newOid, mp3Meta);
            filenameCache.put(mp3Meta.getFile().getPath(), newOid);
        }

        logger.exiting("DataPool", "addData");
    }

    /**
     * Removes a clip from the data pool.<br>
     * data and filename cache are updated.<br>
     * Note: the clip must be removed from all playlists and iPod before calling
     * this!!
     * 
     * @param oid of the clip to remove
     */
    protected void removeClip(Long oid) {
        logger.entering("DataPool", "removeClip");

        // remove from cache if cache is valid
        if (filenameCache.containsValue(oid)) {
            // check if cache is valid
            if (filenameCache.get(getMeta(oid).getFile().getPath()) != null) {
                filenameCache.remove(getMeta(oid).getFile().getPath());

                if (logger.isLoggable(Level.FINE)) {
                    logger.finer("removed from filenameCache :" + oid);
                }
            } else {
                rebuildFilenameCache();
                removeClip(oid);
            }
        }

        // remove from data
        while (data.containsKey(oid)) {
            logger.finer("removed from data pool :" + oid);
            this.data.remove(oid);
        }

        logger.fine("cache is valid after removing: " + this.isCacheValid());

        logger.exiting("DataPool", "removeClip");
    }

    /** Rebuilds the filename cache from scratch. */
    protected void rebuildFilenameCache() {
        logger.entering("de.axelwernicke.mypod.DataPool", "rebuildFilenameCache");
        logger.info("rebuilding filename cache");

        // create new filename cache
        filenameCache = new HashMap<String, Long>(data.size() + 1);

        // iterate over all clips and add them to the cache
        String path;
        for (Long oid : data.keySet()) {
            path = getMeta(oid).getFile().getPath();
            filenameCache.put(path, oid);
        }

        logger.exiting("de.axelwernicke.mypod.DataPool", "rebuildFilenameCache");
    }

    /**
     * Serializes mp3 meta data and filename chache. The current files are moved
     * to .bak
     */
    private void serializeData() {
        logger.entering("DataPool", "serializeData");
        try {
            // move current files to .bak
            File dataFile = new File(DATA_GZ_FILENAME);
            if (dataFile.exists()) {
                FileUtils.copy(dataFile, new File(DATA_GZ_BACKUP_FILENAME));
            }

            // save mp3 meta data
            ObjectOutputStream zos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(DATA_GZ_FILENAME), 65535));
            zos.writeObject(data);
            zos.close();
            // ObjectOutputStream oos = new ObjectOutputStream( new
            // BufferedOutputStream( new FileOutputStream( DATA_FILENAME ),
            // 65535));
            // oos.writeObject(data);
            // oos.close();
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }
        logger.exiting("DataPool", "serializeData");
    }

    /**
     * deserializes clip meta data
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected boolean deserializeData() {
        logger.entering("DataPool", "deserializeData");
        logger.info("deserializing meta data");

        boolean success = false;
        File dataFile = null;

        // try original file
        try {
            dataFile = new File(DATA_GZ_FILENAME);

            if (FileUtils.isWritable(dataFile, 100000)) {
                ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(dataFile), 65535));
                data = (Map) ois.readObject();
                ois.close();
                ois = null;

                success = true;
            }
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        // try backup file
        if (!success) {
            logger.warning("Deserializing data from backup file");
            try {
                dataFile = new File(DATA_GZ_BACKUP_FILENAME);

                if (FileUtils.isWritable(dataFile, 100000)) {
                    ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(dataFile), 65535));
                    data = (Map) ois.readObject();
                    ois.close();
                    ois = null;

                    success = true;
                }
            } catch (Exception e) {
                logger.warning("Exception raised: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // migration to gzip forces us to read old (unzipped files too)
        if (!success) {
            logger.warning("Deserializing data from old unzipped file");
            try {
                dataFile = new File(DATA_FILENAME);

                if (FileUtils.isWritable(dataFile, 100000)) {
                    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(dataFile), 65535));
                    data = (Map) ois.readObject();
                    ois.close();
                    ois = null;

                    success = true;
                }
            } catch (Exception e) {
                logger.warning("Exception raised: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // validate deserialized data hashtable
        validateData();

        logger.exiting("DataPool", "deserializeData");

        return success;
    }

    /**
     * Sets data hashtable
     * 
     * @param data to set
     */
    protected void setData(Map<Long, MP3Meta> data) {
        this.data = data;
    }

    /**
     * Updates meta data and filename cache if file name or path changed.
     * 
     * @param oid of the changed clip
     * @param path of the changed clip
     */
    protected boolean updateFilePath(Long oid, String newPath) {
        boolean success = false;

        try {
            MP3Meta meta = this.getMeta(oid);
            String oldPath = meta.getFile().getPath();

            meta.getFile().renameTo(new File(newPath));
            filenameCache.put(newPath, oid);
            filenameCache.remove(oldPath);

            success = true;
        } catch (Exception e) {
            logger.warning("Exception raised : " + e.getMessage());
            e.printStackTrace();
        }

        return success;
    }

    /**
     * Validates the data object and removes invalid entries<br>
     * Checks if data is null or not,<br>
     * if key is null and<br>
     * if value is null
     */
    private void validateData() {
        logger.entering("DataPool", "validateData");

        long validCnt = 0;
        long invalidCnt = 0;

        if (data != null) {
            // check that there is no key ( oid ) without meta data object...
            Iterator<Long> oidIter = data.keySet().iterator();
            while (oidIter.hasNext()) {
                Long key = oidIter.next();
                if (key == null) {
                    logger.warning("Key (oid) checked was null, removing entry");
                    data.remove(key);
                    invalidCnt++;
                } else if (data.get(key) == null) {
                    logger.warning("value (meta data) for key (oid) " + key + " was null, removing entry");
                    data.remove(key);
                    invalidCnt++;
                } else {
                    validCnt++;
                }
            }
        } else {
            logger.warning("data object is null");
        }

        if (invalidCnt > 0) {
            logger.warning("checking the mypod meta data resulted in " + validCnt + " valid, but " + invalidCnt + " invalid entries");
        }

        logger.exiting("DataPool", "validateData");
    }

    /**
     * Gets the id of all clips.
     * 
     * @param clips to get titles from. If clips is null, all clips in the data
     *            pool are evaluated.
     * @param sort the titles
     * @return a List containing all titles
     */
    public List<String> getAllValues(String id, List<Long> clips, boolean sort) {
        // if we didn't get clips, iterate over all
        List<String> values = (clips != null) ? new ArrayList<String>(clips.size()) : new ArrayList<String>(this.getAllOid().size());
        Iterator<Long> clipIter = (clips != null) ? clips.iterator() : this.getAllOid().iterator();
        String value;

        // get the titles of all clips and add them to the List
        while (clipIter.hasNext()) {
            value = (String) this.getMeta(clipIter.next()).get(id);

            if ((value != null) && (values != null) && !values.contains(value)) {
                values.add(value);
            }
        }

        // sort, if we are forced to
        if (sort) {
            sortList(values);
        }

        return values;
    }

    /**
     * Gets all artists known by the data pool. The content ist sortet.
     * 
     * @return sorted List containing all artits
     */
    public DefaultListModel  getAllArtistValues() {
        List<String> tmp = getAllValues("Artist", null, true);

        // copy sorted list to the model
        DefaultListModel model = new DefaultListModel();
        for (Iterator<String> clipIter = tmp.iterator(); clipIter.hasNext();) {
            model.addElement(clipIter.next());
        }

        return model;
    }

    /**
     * Gets all genres known by the data pool. The content ist sortet.
     * 
     * @return sorted List containing all genres
     */
    public DefaultListModel  getAllGenreValues() {
        List<String> tmp = getAllValues("Genre", null, true);

        // copy sorted list to the model
        DefaultListModel  model = new DefaultListModel ();
        for (Iterator<String> clipIter = tmp.iterator(); clipIter.hasNext();) {
            model.addElement(clipIter.next());
        }

        return model;
    } // get all genres

    /**
     * Gets all years known by the data pool. The content ist sortet.
     * 
     * @return sorted List containing all years
     */
    public DefaultListModel  getAllYearValues() {
        // copy sorted list to the model
        DefaultListModel  model = new DefaultListModel ();
        for (String value : getAllValues("Year", null, true)) {
            model.addElement(value);
        }

        return model;
    }

    /**
     * Sorts a list naturally
     */
    private List<String> sortList(List<String> aCollection) {
        // sometimes there is a null pointer exception while sorting
        try {
            Collections.sort(aCollection, Collator.getInstance());
        } catch (Exception e) {
            logger.warning("exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        return aCollection;
    }
}

/* */
