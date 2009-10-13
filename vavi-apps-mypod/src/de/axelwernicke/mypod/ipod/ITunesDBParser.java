// ITunesDBParser
// $Id: ITunesDBParser.java,v 1.18 2003/07/26 07:06:54 axelwernicke Exp $
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.axelwernicke.mypod.Backend;
import de.axelwernicke.mypod.myPod;


/**
 * Represents an iTunes Database, which contains clip and playlist information.
 *
 * @author  axelwe
 */
public class ITunesDBParser {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod.ipod");

    /** contains temp database for coding / decoding */
    private static ITunesDB db = null;

    /**
     * path to store the database on an ipod
     */
    public static String ITUNES_DB_PATH = "iPod_Control" + File.separator + "iTunes" + File.separator + "iTunesDB";

    /** iTunes db token */
    private static final String DB_RECORD = "mhbd";

    /** list holder token */
    private static final String LIST_HOLDER = "mhsd";

    /** song list header token */
    private static String SONG_LIST_HEADER = "mhlt";

    /** song item token */
    private static final String SONG_ITEM = "mhit";

    /** song item content token */
    private static final String SONG_ITEM_CONTENT = "mhod";

    /** playlist header token */
    private static final String PLAYLIST_HEADER = "mhlp";

    /** playlist item token */
    private static final String PLAYLIST_ITEM = "mhyp";

    /** song item index token */
    private static final String PLAYLIST_INDEX_ITEM = "mhip";

    /** recent playlist object */
    private ITunesDBPlaylistItem recentPlaylist = null;

    /** recent song object */
    private ITunesDBSongItem recentSong = null;

    /** recent playlistIndexItem object */
    private ITunesDBPlaylistIndexItem recentPlaylistIndexItem = null;

    /**
     * Creates a new instance of a parser
     */
    public ITunesDBParser() {
    }

    /**
     * Creates a new instance of ITunesDB
     *
     * @param _db database to create a parser for
     */
    public ITunesDBParser(ITunesDB _db) {
        db = _db;
    }

    /**
     * Loads the database from an iPod.
     *
     * @return the loaded ITunesDB
     * @throws IOException
     */
    ITunesDB load(InputStream is) throws IOException {
        ITunesDB result = null;

        // parse stream
        decode(is);
        
        // close file
        is.close();
        
        // destroy local tmp data base
        result = db;
        db = null;

        // return created database
        return result;
    }

    /**
     * EDIT 1.7.2005 by GIT: allows for loading without a myPod instance, for
     * stanal one use
     */
    public ITunesDB load(Backend backend) {
        ITunesDB result = null;
        try {
            // open data input stream from file
            FileInputStream fis = new FileInputStream(backend.getPreferences().getIPodPath() + File.separator + ITUNES_DB_PATH);
            BufferedInputStream bis = new BufferedInputStream(fis, 65532);

            // parse stream
            decode(bis);

            // close file
            bis.close();

            // destroy local tmp data base
            result = db;
            db = null;
        } catch (Exception fnf) {
            logger.warning("exception raised: " + fnf.getMessage());
        }

        // return created database
        return result;
    }

    /**
     * saves the database to an iPod
     *
     * @param database ITunesDB to save
     * @throws IOException
     */
    boolean save(ITunesDB database) throws IOException {
        boolean success = false;

        // store database temporarily
        db = database;

        // open data output stream from file
        FileOutputStream fos = new FileOutputStream(myPod.getBackend().getPreferences().getIPodPath() + File.separator + ITUNES_DB_PATH);
        BufferedOutputStream bos = new BufferedOutputStream(fos, 65532);
        
        // encode stream
        encode(bos);
        
        // close file
        bos.close();
        
        success = true;

        return success;
    }

    /**
     * decodes the binary database file
     *
     * @param is stream to decode
     * @throws IOException
     */
    private void decode(InputStream is) throws IOException {
        logger.entering("ITunesDB", "decode");

        // do all frames
        byte[] tag = new byte[4];
        
        // parse all tags
        while (is.read(tag) != -1) {
            if (new String(tag).equalsIgnoreCase(DB_RECORD)) {
                // ----------- mhbd --------------
                logger.finest("found DB_RECORD tag");
                
                // this is the root record, set temp data base
                db = parseDbRecord(is);
            } else if (new String(tag).equalsIgnoreCase(LIST_HOLDER)) {
                // ----------- mhsd --------------
                logger.finest("found LIST_HOLDER tag");
                ITunesDBListHolder listHolder = parseListHolder(is);
                
                // there are usually two list holder records in a database, add depending on type
                if (listHolder.getListType() == ITunesDBListHolder.SONGLIST) {
                    db.setSonglistHolder(listHolder);
                } else if (listHolder.getListType() == ITunesDBListHolder.PLAYLIST) {
                    db.setPlaylistHolder(listHolder);
                } else {
                    logger.warning("unknown list holder type: " + listHolder.getListType());
                }
            } else if (new String(tag).equalsIgnoreCase(SONG_LIST_HEADER)) {
                // ----------- mhlt --------------
                logger.finest("found SONG_LIST_HEADER tag");
                
                // create & decode
                db.setSonglistHeader(parseSonglistHeader(is));
            } else if (new String(tag).equalsIgnoreCase(SONG_ITEM)) {
                // ----------- mhit --------------
                logger.finest("found SONG_ITEM tag");
                ITunesDBSongItem songItem = parseSongItem(is);
                
                // mhit records always belong to the song list, so add it there and mark as recent
                db.getSonglistHeader().addSongItem(songItem);
                recentSong = songItem;
            } else if (new String(tag).equalsIgnoreCase(SONG_ITEM_CONTENT)) {
                // ----------- mhod --------------
                logger.finest("found SONG_ITEM_CONTENT tag");
                ITunesDBContentItem contentItem = parseContentItem(is);
                
                // mhod records may belong to a song, or to an playlist entry, or to an
                // playlist index item - crazy thing :)
                //
                // first check if we have an playlist index item
                if (recentPlaylistIndexItem != null) {
                    // tweak content
                    byte[] tweak = { 0x00, 0x00, 0x00, 0x00 };
                    contentItem.setContent(tweak);
                    // set Content changes the content size - we don't want this ...
                    contentItem.setContentSize(0);
                    
                    recentPlaylistIndexItem.addContenItem(contentItem);
                } else if (recentPlaylist != null) {
                    // now try if we have at least an playlist header
                    recentPlaylist.getSongItems().add(contentItem);
                } else {
                    // otherwise we have an song list content
                    // add to song list
                    recentSong.getContent().add(contentItem);
                }
            } else if (new String(tag).equalsIgnoreCase(PLAYLIST_HEADER)) {
                // ----------- mhlp --------------
                logger.finest("found PLAYLIST_HEADER tag");
                ITunesDBPlaylistHeader playlistHeader = parsePlaylistHeader(is);
                
                // since there is only one playlist header, simply add it to the data base
                db.setPlaylistHeader(playlistHeader);
            } else if (new String(tag).equalsIgnoreCase(PLAYLIST_ITEM)) {
                // ----------- mhyp --------------
                logger.finest("found PLAYLIST_ITEM tag");
                ITunesDBPlaylistItem playlistItem = parsePlaylistItem(is);
                
                // add to playlist header and mark as recent playlist
                db.getPlaylistHeader().addPlaylist(playlistItem);
                recentPlaylist = playlistItem;
                
                // reset recent index item
                recentPlaylistIndexItem = null;
            } else if (new String(tag).equalsIgnoreCase(PLAYLIST_INDEX_ITEM)) {
                // ----------- mhip --------------
                logger.finest("found PLAYLIST_INDEX_ITEM tag");
                ITunesDBPlaylistIndexItem playlistIndexItem = parsePlaylistIndexItem(is);
                
                // mhip records always belong to a playlist, so append to the most recently added playlist
                recentPlaylist.getSongItems().add(playlistIndexItem);
                
                // make this index item the recent one
                recentPlaylistIndexItem = playlistIndexItem;
            }
        }

        logger.exiting("ITunesDB", "decode");
    }

    /**
     * encodes the binary database file
     *
     * @param os stream to write to
     * @throws IOException
     */
    private void encode(OutputStream os) throws IOException {
        // encode database header
        encodeDbRecord(os);

        // encode all songs
        if (db.getSonglistHolder() != null) {
            encodeListHolder(os, db.getSonglistHolder());
        } else {
            logger.info("song list holder was null");
        }

        if (db.getSonglistHeader() != null) {
            encodeSonglistHeader(os);
        } else {
            logger.info("song list header was null");
        }

        // encode all playlists
        if (db.getPlaylistHolder() != null) {
            encodeListHolder(os, db.getPlaylistHolder());
        } else {
            logger.info("playlist holder was null");
        }

        if (db.getPlaylistHeader() != null) {
            encodePlaylistHeader(os);
        } else {
            logger.info("playlist header was null");
        }
    }

    /**
     * Parses a db record
     *
     * @param is stream to parse the record from
     * @return object for the parsed record
     * @throws IOException
     */
    private static ITunesDB parseDbRecord(InputStream is) throws IOException {
        ITunesDB db = new ITunesDB();

        byte[] dword = new byte[4];
        
        is.read(dword);
        db.setTagSize(littleEndianToInt(dword));
        
        is.read(dword);
        db.setRecordSize(littleEndianToInt(dword));
        
        is.read(dword);
        db.setUnknown3(littleEndianToInt(dword));
        
        is.read(dword);
        db.setUnknown4(littleEndianToInt(dword));
        
        is.read(dword);
        db.setUnknown5(littleEndianToInt(dword));
        
        // log unknown tags
        long tmp;
        int to = db.getTagSize() / 4;
        for (int i = 7; i < to; i++) {
            is.read(dword);
            tmp = littleEndianToInt(dword);
            if (tmp != 0) {
                logger.warning("unknwon: " + i + " = " + tmp);
            }
        }
        
        // log
        logger.finest(db.toString());

        return db;
    }

    /**
     * Encodes a db record.
     *
     * @param os stream to write the db record to
     * @throws IOException
     */
    static void encodeDbRecord(OutputStream os) throws IOException {
        byte[] tag = { 'm', 'h', 'b', 'd' };
        os.write(tag);
        os.write(intToLittleEndian(db.getTagSize()));
        os.write(intToLittleEndian(db.getRecordSize()));
        os.write(intToLittleEndian(db.getUnknown3()));
        os.write(intToLittleEndian(db.getUnknown4()));
        os.write(intToLittleEndian(db.getUnknown5()));
        
        // write padding
        int to = db.getTagSize() - (6 * 4);
        for (int i = 0; i < to; i++) {
            os.write(0x00);
        }
        
        // logging
        logger.finest(db.toString());
    }

    /**
     * Parses a list holder token
     *
     * @param is stream to parse from
     * @return parsed object
     * @throws IOException
     */
    private static ITunesDBListHolder parseListHolder(InputStream is) throws IOException {
        ITunesDBListHolder listHolder = new ITunesDBListHolder();

        byte[] dword = new byte[4];
        
        is.read(dword);
        listHolder.setTagSize(littleEndianToInt(dword));
        
        is.read(dword);
        listHolder.setRecordSize(littleEndianToInt(dword));
        
        is.read(dword);
        listHolder.setListType(littleEndianToInt(dword));
        
        // log unknown tags
        long tmp;
        int to = listHolder.getTagSize() / 4;
        for (int i = 4; i < to; i++) {
            is.read(dword);
            tmp = littleEndianToInt(dword);
            if (tmp != 0) {
                logger.warning("unknown: " + i + " = " + tmp);
            }
        }
        
        // logging
        logger.finest(listHolder.toString());

        return listHolder;
    }

    /**
     * @param os
     * @param lh
     * @throws IOException
     */
    static void encodeListHolder(OutputStream os, ITunesDBListHolder lh) throws IOException {
        byte[] tag = { 'm', 'h', 's', 'd' };
        os.write(tag);
        os.write(intToLittleEndian(lh.getTagSize()));
        os.write(intToLittleEndian(lh.getRecordSize()));
        os.write(intToLittleEndian(lh.getListType()));
        
        // write padding
        int to = lh.getTagSize() - (4 * 4);
        for (int i = 0; i < to; i++) {
            os.write(0x00);
        }
        
        // logging
        logger.finest(lh.toString());
    }

    /**
     * Parses a songlist header record.
     * @param is stream to parse the record from
     * @return object for the parsed record
     * @throws IOException
     */
    private static ITunesDBSonglistHeader parseSonglistHeader(InputStream is) throws IOException {
        ITunesDBSonglistHeader songlistHeader = new ITunesDBSonglistHeader();

        byte[] dword = new byte[4];
        
        is.read(dword);
        songlistHeader.setTagSize(littleEndianToInt(dword));
        
        is.read(dword);
        // this is done implicitly  by adding song items
        // songlistHeader.setSongCount(littleEndianToInt(dword) );
        // log unknown tags
        long tmp;
        int to = songlistHeader.getTagSize() / 4;
        for (int i = 4; i < to; i++) {
            is.read(dword);
            tmp = littleEndianToInt(dword);
            if (tmp != 0) {
                logger.warning("unknwon: " + i + " = " + tmp);
            }
        }
        
        // logging
        logger.finest(songlistHeader.toString());

        return songlistHeader;
    }

    /**
     * @param os
     * @throws IOException
     */
    static void encodeSonglistHeader(OutputStream os) throws IOException {
        byte[] tag = { 'm', 'h', 'l', 't' };
        os.write(tag);
        os.write(intToLittleEndian(db.getSonglistHeader().getTagSize()));
        os.write(intToLittleEndian(db.getSonglistHeader().getSongCount()));
        
        // write padding
        int to = db.getSonglistHeader().getTagSize() - (3 * 4);
        for (int i = 0; i < to; i++) {
            os.write(0x00);
        }
        
        // encode all song list items
        int songCount = db.getSonglistHeader().getSongCount();
        for (int i = 0; i < songCount; i++) {
            encodeSongItem(os, db.getSonglistHeader().getSongItem(i));
        }
        
        // logging
        logger.finest(db.getSonglistHeader().toString());
    }

    /**
     * Parses a song item record.
     *
     * @param is stream to parse the record from
     * @return object for the parsed record
     * @throws IOException
     */
    private static ITunesDBSongItem parseSongItem(InputStream is) throws IOException {
        ITunesDBSongItem songItem = new ITunesDBSongItem();

        byte[] dword = new byte[4];
        
        is.read(dword);
        songItem.setTagSize(littleEndianToInt(dword));
        is.read(dword);
        songItem.setRecordSize(littleEndianToInt(dword));
        is.read(dword);
        songItem.setContentCount(littleEndianToInt(dword));
        is.read(dword);
        songItem.setRecordIndex(littleEndianToInt(dword));
        is.read(dword);
        songItem.setUnknown6(littleEndianToInt(dword));
        is.read(dword);
        songItem.setUnknown7(littleEndianToInt(dword));
        is.read(dword);
        songItem.setUnknown8(littleEndianToInt(dword));
        is.read(dword);
        songItem.setDate(littleEndianToInt(dword));
        is.read(dword);
        songItem.setFilesize(littleEndianToInt(dword));
        is.read(dword);
        songItem.setDuration(littleEndianToInt(dword));
        is.read(dword);
        songItem.setTrackNumber(littleEndianToInt(dword));
        is.read(dword);
        songItem.setUnknown13(littleEndianToInt(dword));
        is.read(dword);
        songItem.setUnknown14(littleEndianToInt(dword));
        is.read(dword);
        songItem.setBitrate(littleEndianToInt(dword));
        is.read(dword);
        songItem.setSamplerate(littleEndianToInt(dword));
        
        // log unknown tags
        long tmp;
        int to = songItem.getTagSize() / 4;
        for (int i = 17; i < to; i++) {
            is.read(dword);
            tmp = littleEndianToInt(dword);
            if (tmp != 0) {
                logger.warning("unknwon: " + i + " = " + tmp);
            }
        }
        
        // logging
        logger.finest(songItem.toString());

        return songItem;
    }

    /**
     * @param os
     * @param si
     * @throws IOException
     */
    static void encodeSongItem(OutputStream os, ITunesDBSongItem si) throws IOException {
        byte[] tag = { 'm', 'h', 'i', 't' };
        os.write(tag);
        os.write(intToLittleEndian(si.getTagSize()));
        os.write(intToLittleEndian(si.getRecordSize()));
        os.write(intToLittleEndian(si.getContentCount()));
        os.write(intToLittleEndian(si.getRecordIndex()));
        os.write(intToLittleEndian(si.getUnknown6()));
        os.write(intToLittleEndian(si.getUnknown7()));
        os.write(intToLittleEndian(si.getUnknown8()));
        os.write(intToLittleEndian(si.getDate()));
        os.write(intToLittleEndian(si.getFilesize()));
        os.write(intToLittleEndian(si.getDuration()));
        os.write(intToLittleEndian(si.getTrackNumber()));
        os.write(intToLittleEndian(si.getUnknown13()));
        os.write(intToLittleEndian(si.getUnknown14()));
        os.write(intToLittleEndian(si.getBitrate()));
        os.write(intToLittleEndian(si.getSamplerate()));
        
        // write padding
        int to = si.getTagSize() - (16 * 4);
        for (int i = 0; i < to; i++) {
            os.write(0x00);
        }
        
        // encode all content items
        to = si.getContent().size();
        for (int i = 0; i < to; i++) {
            encodeContentItem(os, si.getContent().get(i));
        }
        
        // logging
        logger.finest(si.toString());
    }

    /**
     * Parses a content item record.
     *
     * @param is stream to parse the record from
     * @return object for the parsed record
     * @throws IOException
     */
    private static ITunesDBContentItem parseContentItem(InputStream is) throws IOException {
        ITunesDBContentItem contentItem = new ITunesDBContentItem();

        byte[] dword = new byte[4];
        
        is.read(dword);
        //songItemContent.tagSize = littleEndianToInt(dword);
        long ts = littleEndianToInt(dword);
        if (ts != contentItem.getTagSize()) {
            logger.warning("parsed tag size does not equal default" + ts + " vs. " + contentItem.getTagSize());
        }
        
        is.read(dword);
        // record size is set when setting content
        //long rs = littleEndianToInt(dword);
        is.read(dword);
        contentItem.setContentTyp(littleEndianToInt(dword));
        
        is.read(dword);
        contentItem.setUnknown5(littleEndianToInt(dword));
        
        is.read(dword);
        contentItem.setUnknown6(littleEndianToInt(dword));
        
        is.read(dword);
        contentItem.setListPosition(littleEndianToInt(dword));
        
        is.read(dword);
        // content size is set when setting content
        long cs = littleEndianToInt(dword);
        
        is.read(dword);
        contentItem.setUnknown9(littleEndianToInt(dword));
        
        is.read(dword);
        contentItem.setUnknown10(littleEndianToInt(dword));
        
        // read til end of tag as content
        byte[] tmp = new byte[(int) cs];
        is.read(tmp);
        
        // make default endian encoded and set content
        contentItem.setContent(tmp);
        
        // logging
        logger.finest(contentItem.toString());

        return contentItem;
    }

    /**
     * @param os
     * @param sic
     * @throws IOException
     */
    static void encodeContentItem(OutputStream os, ITunesDBContentItem sic) throws IOException {
        byte[] tag = { 'm', 'h', 'o', 'd' };
        os.write(tag);
        os.write(intToLittleEndian(sic.getTagSize()));
        os.write(intToLittleEndian(sic.getRecordSize()));
        os.write(intToLittleEndian(sic.getContentTyp()));
        os.write(intToLittleEndian(sic.getUnknown5()));
        os.write(intToLittleEndian(sic.getUnknown6()));
        os.write(intToLittleEndian(sic.getListPosition()));
        os.write(intToLittleEndian(sic.getContentSize()));
        os.write(intToLittleEndian(sic.getUnknown9()));
        os.write(intToLittleEndian(sic.getUnknown10()));
        
        os.write(sic.getContent());
        
        // logging
        logger.finest(sic.toString());
    }

    /**
     * Parses a playlist index item record.
     *
     * @param is stream to parse from
     * @return object for the parsed record
     * @throws IOException
     */
    private static ITunesDBPlaylistIndexItem parsePlaylistIndexItem(InputStream is) throws IOException {
        ITunesDBPlaylistIndexItem playlistIndexItem = new ITunesDBPlaylistIndexItem();

        byte[] dword = new byte[4];
        
        is.read(dword);
        playlistIndexItem.setTagSize(littleEndianToInt(dword));
        
        is.read(dword);
        playlistIndexItem.setRecordSize(littleEndianToInt(dword));
        
        is.read(dword);
        // set implicitly by adding content items
        // playlistIndexItem.setContentCount(littleEndianToInt(dword));
        is.read(dword);
        playlistIndexItem.setUnknown5(littleEndianToInt(dword));
        
        is.read(dword);
        playlistIndexItem.setUnknown6(littleEndianToInt(dword));
        
        is.read(dword);
        playlistIndexItem.setSongIndex(littleEndianToInt(dword));
        
        // log unknown tags
        long tmp;
        int to = playlistIndexItem.getTagSize() / 4;
        for (int i = 8; i < to; i++) {
            is.read(dword);
            tmp = littleEndianToInt(dword);
            if (tmp != 0) {
                logger.warning("unknwon: " + i + " = " + tmp);
            }
        }
        
        // logging
        logger.finest(playlistIndexItem.toString());

        return playlistIndexItem;
    }

    /**
     * Encodes mhip item
     *
     * @param fos stream to write to
     * @param pii playlist index item
     * @throws IOException
     */
    private static void encodePlaylistIndexItem(OutputStream fos, ITunesDBPlaylistIndexItem pii) throws IOException {
        byte[] tag = { 'm', 'h', 'i', 'p' };
        fos.write(tag);
        fos.write(intToLittleEndian(pii.getTagSize()));
        fos.write(intToLittleEndian(pii.getRecordSize()));
        fos.write(intToLittleEndian(pii.getContentCount()));
        fos.write(intToLittleEndian(pii.getUnknown5()));
        fos.write(intToLittleEndian(pii.getUnknown6()));
        fos.write(intToLittleEndian(pii.getSongIndex()));
        
        // write padding
        int to = pii.getTagSize() - (7 * 4);
        for (int i = 0; i < to; i++) {
            fos.write(0x00);
        }
        
        // write attached content items
        for (int i = 0; i < pii.getContentCount(); i++) {
            encodeContentItem(fos, pii.getContentItem(i));
        }
        
        // logging
        logger.finest(pii.toString());
    }

    /**
     * Parses a playlist header record.
     *
     * @param is stream to parse the record from
     * @return object for the parsed record
     * @throws IOException
     */
    private static ITunesDBPlaylistHeader parsePlaylistHeader(InputStream is) throws IOException {
        ITunesDBPlaylistHeader playlistHeader = new ITunesDBPlaylistHeader();

        byte[] dword = new byte[4];
        
        is.read(dword);
        playlistHeader.setTagSize(littleEndianToInt(dword));
        
        is.read(dword);
        // done implicitly playlistHeader.playlistCount = littleEndianToInt(dword);
        // log unknown tags
        long tmp;
        int to = playlistHeader.getTagSize() / 4;
        for (int i = 4; i < to; i++) {
            is.read(dword);
            tmp = littleEndianToInt(dword);
            if (tmp != 0) {
                logger.warning("unknwon: " + i + " = " + tmp);
            }
        }
        
        // logging
        logger.finest(playlistHeader.toString());

        return playlistHeader;
    }

    /**
     * Encodes a playlist header item of an iTunes DB
     *
     * @param os Stream to encode into
     * @throws IOException
     */
    void encodePlaylistHeader(OutputStream os) throws IOException {
        byte[] tag = { 'm', 'h', 'l', 'p' };
        os.write(tag);
        os.write(intToLittleEndian(db.getPlaylistHeader().getTagSize()));
        os.write(intToLittleEndian(db.getPlaylistHeader().getPlaylistCount()));
        
        // write padding
        for (int i = 0;
        i < (db.getPlaylistHeader().getTagSize() - (3 * 4)); i++) {
            os.write(0x00);
        }
        
        // encode all attached playlists
        int playlistCount = db.getPlaylistHeader().getPlaylistCount();
        for (int i = 0; i < playlistCount; i++) {
            encodePlaylistItem(os, db.getPlaylistHeader().getPlaylist(i));
        }
        
        // logging
        logger.finest(db.getPlaylistHeader().toString());
    }

    /**
     * Parses a playlist item record.
     *
     * @param fis stream to parse the record from
     * @return object for the parsed record
     * @throws IOException
     */
    private static ITunesDBPlaylistItem parsePlaylistItem(InputStream fis) throws IOException {
        ITunesDBPlaylistItem playlistItem = new ITunesDBPlaylistItem();

        byte[] dword = new byte[4];
        
        fis.read(dword);
        playlistItem.setTagSize(littleEndianToInt(dword));
        
        fis.read(dword);
        playlistItem.setRecordSize(littleEndianToInt(dword));
        
        fis.read(dword);
        playlistItem.setContentCount(littleEndianToInt(dword));
        
        fis.read(dword);
        playlistItem.setSongCount(littleEndianToInt(dword));
        
        fis.read(dword);
        playlistItem.setListType(littleEndianToInt(dword));
        
        // log unknown tags
        long tmp;
        int to = playlistItem.getTagSize() / 4;
        for (int i = 7; i < to; i++) {
            fis.read(dword);
            tmp = littleEndianToInt(dword);
            if (tmp != 0) {
                logger.warning("unknwon: " + i + " = " + tmp);
            }
        }
        
        // logging
        logger.finest(playlistItem.toString());

        return playlistItem;
    }

    /**
     * @param os
     * @param pi
     * @throws IOException
     */
    void encodePlaylistItem(OutputStream os, ITunesDBPlaylistItem pi) throws IOException {
        byte[] tag = { 'm', 'h', 'y', 'p' };
        os.write(tag);
        os.write(intToLittleEndian(pi.getTagSize()));
        os.write(intToLittleEndian(pi.getRecordSize()));
        os.write(intToLittleEndian(pi.getContentCount()));
        os.write(intToLittleEndian(pi.getSongCount()));
        os.write(intToLittleEndian(pi.getListType()));
        
        // write padding
        int to = pi.getTagSize() - (6 * 4);
        for (int i = 0; i < to; i++) {
            os.write(0x00);
        }
        
        // encode all attached playlist entries
        int songCnt = pi.getSongItems().size();
        for (int i = 0; i < songCnt; i++) {
            Object tmp = pi.getSongItems().get(i);
            if (tmp instanceof de.axelwernicke.mypod.ipod.ITunesDBPlaylistIndexItem) {
                // encode all index entries
                encodePlaylistIndexItem(os, (ITunesDBPlaylistIndexItem) tmp);
            } else {
                // encode content item
                ITunesDBParser.encodeContentItem(os, (ITunesDBContentItem) tmp);
            }
        }
        
        // logging
        logger.finest(pi.toString());
    }

    /**
     * Calculates the _real_ size of the object.
     * The object is encoded into a byte array to determine size.
     *
     * @return real size of the record
     * @param obj
     * @throws IOException
     */
    int calculateRecordSize(Object obj) throws IOException {
        int size = 0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        if (obj instanceof de.axelwernicke.mypod.ipod.ITunesDB) {
            ITunesDBParser.encodeDbRecord(baos);
            ITunesDBParser.encodeListHolder(baos, db.getSonglistHolder());
            ITunesDBParser.encodeSonglistHeader(baos);
            ITunesDBParser.encodeListHolder(baos, db.getPlaylistHolder());
            this.encodePlaylistHeader(baos);
        } else if (obj instanceof de.axelwernicke.mypod.ipod.ITunesDBContentItem) {
            ITunesDBParser.encodeContentItem(baos, (ITunesDBContentItem) obj);
        } else if (obj instanceof de.axelwernicke.mypod.ipod.ITunesDBListHolder) {
            ITunesDBParser.encodeListHolder(baos, (ITunesDBListHolder) obj);
            if (((ITunesDBListHolder) obj).getListType() == ITunesDBListHolder.SONGLIST) {
                ITunesDBParser.encodeListHolder(baos, db.getSonglistHolder());
                ITunesDBParser.encodeSonglistHeader(baos);
            } else {
                ITunesDBParser.encodeListHolder(baos, db.getPlaylistHolder());
                this.encodePlaylistHeader(baos);
            }
        } else if (obj instanceof de.axelwernicke.mypod.ipod.ITunesDBPlaylistHeader) {
            this.encodePlaylistHeader(baos);
        } else if (obj instanceof de.axelwernicke.mypod.ipod.ITunesDBPlaylistIndexItem) {
            ITunesDBParser.encodePlaylistIndexItem(baos, (ITunesDBPlaylistIndexItem) obj);
        } else if (obj instanceof de.axelwernicke.mypod.ipod.ITunesDBPlaylistItem) {
            this.encodePlaylistItem(baos, (ITunesDBPlaylistItem) obj);
        } else if (obj instanceof de.axelwernicke.mypod.ipod.ITunesDBSongItem) {
            ITunesDBParser.encodeSongItem(baos, (ITunesDBSongItem) obj);
        } else if (obj instanceof de.axelwernicke.mypod.ipod.ITunesDBSonglistHeader) {
            ITunesDBParser.encodeSonglistHeader(baos);
        }
        
        baos.flush();
        size = baos.size();
        baos.close();

        return size;
    }

    /* --------------------------------------- some endian conversion stuff ---------------------------------- */

    // first of all, JVM, Mac, and others are big endian machines
    // PC and iPod are little endians.

    /**
     * Decodes a byte array to long
     *
     * @return long representation of value
     * @param value byte array to decode
     */
    static long littleEndianToLong(byte[] value) {
        long result = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getLong();

        return result;
    }

    /**
     * Decodes a byte array to int
     *
     * @return int representation of value
     * @param value byte array to decode
     */
    static int littleEndianToInt(byte[] value) {
        int result = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getInt();

        return result;
    }

    /**
     * Encodes a long to an little endianded byte array.
     *
     * @param value value to encode
     * @return byte array containing the value little endianded
     */
    static byte[] longToLittleEndian(long value) {
        ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(value);

        return bb.array();
    }

    /**
     * Encodes a long to an little endianded byte array.
     *
     * @param value value to encode
     * @return byte array containing the value little endianded
     */
    static byte[] intToLittleEndian(int value) {
        ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(value);

        return bb.array();
    }

    /**
     * Converts a little endianed UTF16 byte array into a java string.
     *
     * @param value byte array to decode
     * @return string representing the decoded value
     */
    public static String uTF16LittleEndianToString(byte[] value) {
        // wrap the byte array
        java.nio.CharBuffer cb = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer();

        // get the string
        return cb.toString();
    }

    /**
     * Converts a java String to an UTF16 little endianed byte array
     *
     * @param value string to convert
     * @return byte array containing the encoded value
     */
    static byte[] stringToUTF16LittleEndian(String value) {
        // allocate the byte array
        ByteBuffer bb = ByteBuffer.allocate(value.getBytes().length * 2).order(ByteOrder.LITTLE_ENDIAN);

        // set the value
        bb.asCharBuffer().put(value);
        bb.compact();

        // get the encoded array
        return bb.array();
    }

    /**
     * Converts a date as long from iPod to a java date as long
     *
     * <PRE>
     * mac date is seconds since 01/01/1904
     * java date is milliseconds since 01/01/1970
     * </PRE>
     *
     * @return date in java format
     * @param macDate date on iPod
     */
    static public long macDateToDate(int macDate) {
        long javaDate = 0;

        // offset between java & iPod (Mac) & seconds to milliseconds
        javaDate = (macDate - 2082844800) * 1000;

        return javaDate;
    }

    /**
     * Converts a java date as long to a mac date as long
     *
     * <PRE>
     * mac date is seconds since 01/01/1904
     * java date is milliseconds since 01/01/1970
     * </PRE>
     *
     * @return date in mac format
     * @param javaDate date in java format
     */
    static public int dateToMacDate(long javaDate) {
        int macDate = 0;

        // milliseconds -> seconds & offset between java & iPod (Mac)
        macDate = (int) (javaDate / 1000) + 2082844800;

        return macDate;
    }

    //----
    
    /** */
    public static void main(String[] args) throws Exception {
        ITunesDB db = new ITunesDBParser().load(new BufferedInputStream(new FileInputStream(args[0])));
        logger.log(Level.INFO, String.valueOf(db));
    }
}
