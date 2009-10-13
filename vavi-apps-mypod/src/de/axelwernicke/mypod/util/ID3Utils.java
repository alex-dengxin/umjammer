/*
 * ID3Utils
 * $Id: ID3Utils.java,v 1.16 2003/07/20 06:46:17 axelwernicke Exp $
 *
 * Copyright (C) 2002-2003 Axel Wernicke <axel.wernicke@gmx.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.axelwernicke.mypod.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import vavi.util.tag.Tag;
import vavi.util.tag.id3.ID3Tag;
import vavi.util.tag.id3.MP3File;
import vavi.util.tag.id3.v2.FrameContent;
import vavi.util.tag.id3.v2.ID3v2;
import vavi.util.tag.id3.v2.di.RawFrameContent;

import de.axelwernicke.mypod.MP3Meta;


/**
 * This class provides some helper methods to read and write id3 tags
 *
 * @author  axel wernicke
 */
public class ID3Utils {
    /** Holds an instance of this singleton designed class */
    private static ID3Utils INSTANCE = null;

    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger(ID3Utils.class.getName());

    /**
     * Creates a new instance of ID3Utils.
     * The constructor is private, cause this class is singleton. Please use getInstance() instead.
     */
    private ID3Utils() {
    }

    /**
     * Gets an instance of the ID3Utils class.
     *
     * @return instance of ID3Utils class
     */
    public ID3Utils getInstance() {
        return (INSTANCE != null) ? INSTANCE : new ID3Utils();
    }

    /**
     * <li> file is opened as de.vdheide.MP3File
     *
     * @param file to scan
     * @return mp3 meta data of the file
     */
    public static MP3Meta scanMp3VdHeide(java.io.File file) {
        logger.entering("de.axelwernicke.mypod.util.ID3Utils", "scanMp3VdHeide");

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("now scanning: " + file.getName());
        }

        MP3Meta mp3Meta = null;

        try {
            MP3File mp3File = new MP3File(file.getPath());
            mp3Meta = new MP3Meta(mp3File);
            mp3File = null;
        } catch (vavi.util.tag.id3.ID3TagException e) {
            logger.warning(e + ": raised for file: " + file);
            logger.warning(e.getStackTrace()[0].toString());
        } catch (Exception e) {
            logger.warning("Exception: " + e.getMessage() + ": raised for file: " + file);
            e.printStackTrace();
        } catch (Error e) {
            logger.warning("ERROR: " + file);
            throw e;
        }

        logger.exiting("de.axelwernicke.mypod.util.ID3Utils", "scanMp3VdHeide");

        return mp3Meta;
    }

    /** Updates the id3 tags ( v1 & v2.3 ) of an mp3 clip
     *
     * @param origMeta        original meta data of the clip
     * @param metaToSet        meta data to set, values are set if they are not null (strings) or -1 (numbers)
     */
    public static void updateMp3VdHeide(MP3Meta origMeta, MP3Meta metaToSet) {
        logger.entering("de.axelwernicke.mypod.util.ID3Utils", "updateMp3VdHeide");

        MP3File mp3File = null;

        try {
            // open mp3 file and read tags - this is neccessary, cause only tags read at least once, are written correctly
            mp3File = new MP3File(origMeta.getFile().getPath());
            prepareForUpdate(mp3File);

            // update file
            // TODO removed checking if something changed - this should help to hold id3v1 and id3v2 in sync
            for (Tag tag : mp3File.getTags()) {
                tag.update();
            }
        } catch (Exception e) {
            logger.warning("Exception raised for " + ((mp3File != null) ? mp3File.getName() : ("unknown file (file == null)" + " : " + e.getMessage())));
e.printStackTrace(System.err);
        }

        logger.exiting("de.axelwernicke.mypod.util.ID3Utils", "updateMp3VdHeide");
    }

    /**
     * Create a id3 text content tag from string <br>
     * The tag is created only, if new value is not null and different from the
     * original one. <br>
     * null is returned otherwise
     * 
     * @param origValue old value
     * @param newValue new value
     * @return tag containing the new value
     */
    @SuppressWarnings("unused")
    private static FrameContent createTag(String origValue, String newValue) {
        logger.entering("de.axelwernicke.mypod.util.ID3Utils", "createTag");

        FrameContent tag = null;

        if ((newValue != null) && !newValue.equals(origValue)) {
            tag = new RawFrameContent();
            tag.setContent(newValue);
        }

        logger.exiting("de.axelwernicke.mypod.util.ID3Utils", "createTag");

        return tag;
    }

    /**
     * Create a id3 binary content tag from byte array <br>
     * The tag is created only, if new value is not null and different from the
     * original one. <br>
     * null is returned otherwise
     * 
     * @param origValue old value
     * @param newValue new value
     * @return tag containing the new value
     */
    @SuppressWarnings("unused")
    private static FrameContent createTag(byte[] origValue, byte[] newValue) {
        logger.entering("de.axelwernicke.mypod.util.ID3Utils", "createTag");

        FrameContent tag = null;

        if ((newValue != null) && !java.util.Arrays.equals(newValue, origValue)) {
            tag = new RawFrameContent();
            tag.setContent(newValue);
        }

        logger.exiting("de.axelwernicke.mypod.util.ID3Utils", "createTag");

        return tag;
    }

    /**
     * Create a id3 binary content tag from int <br>
     * The tag is created only, if new value is not null and different from the
     * original one. <br>
     * null is returned otherwise
     * 
     * @param origValue old value
     * @param newValue new value
     * @return tag containing the new value
     */
    @SuppressWarnings("unused")
    private static FrameContent createTag(int origValue, int newValue) {
        logger.entering("de.axelwernicke.mypod.util.ID3Utils", "createTag");

        FrameContent tag = null;

        if ((newValue != -1) && (newValue != origValue)) {
            tag = new RawFrameContent();
            tag.setContent(String.valueOf(newValue));
        }

        logger.exiting("de.axelwernicke.mypod.util.ID3Utils", "createTag");

        return tag;
    }

    /**
     * Prepares an mp3 file to be updated.
     * All tags are read once and id3v1 tags are written
     *
     * @param mp3File to prepare
     */
    private static void prepareForUpdate(MP3File mp3File) {
        logger.entering("de.axelwernicke.mypod.util.ID3Utils", "prepareForUpdate");

        try {
            // read all tags once, otherwise id3v2 tags are not written correctly
            scanMp3VdHeide(mp3File);

            // unsynchronization makes id3v2 unicode tags unreadable ...
            if (mp3File.hasTag(ID3Tag.Type.ID3v2)) {
                ((ID3v2) mp3File.getTag(ID3Tag.Type.ID3v2)).setUseUnsynchronization(false);
            }
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        logger.exiting("de.axelwernicke.mypod.util.ID3Utils", "prepareForUpdate");
    }
}
