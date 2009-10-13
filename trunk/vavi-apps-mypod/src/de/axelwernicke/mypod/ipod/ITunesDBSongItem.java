// ITunesDBSongItem
// $Id: ITunesDBSongItem.java,v 1.13 2003/07/20 11:35:38 axelwernicke Exp $
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A SongItem object stores the informations from the mhit tag. It has
 *        references to the songs content.
 *
 *        mhit:
 *        0x0000        'mhit'                                - tag
 *        0x0004                                                                - tag size
 *        0x0008                                                                - item size
 *        0x000C                                                                - number of song content tags for this song
 *        0x0010                                                                - record index ( smth. like 2001 )
 *        0x0014        1                                                        - unknown6
 *        0x0018                                                                - unknown7
 *        0x001C                                                                - unknown8 - special format !! 0x101 -> VBR, 0x100 ->256k
 *                                                                                                        seems to be mostly 256, and bitrate itself is stored in unknown 15
 *        0x0020                                                                - date (Mac format, sec since 01.01.1904)
 *        0x0024                                                                - filesize
 *        0x0028                                                                - song duration in ms
 *        0x002C                                                                - number of track in album
 *        0x0030                                                                - unknown13
 *        0x0034                                                                - unknown14
 *        0x0038                                                                - bitrate
 *        0x003C                                                                - unknown16 // TODO sample rate shifted 16bit left
 *        0x0040                        0                                        - Volume adjustment between -100 and +100
 *        0x0044                        0                                        - Start time in 1000's of sec (where in the song to start playing)
 *        0x0048                        0                                        - Stop time in 1000's of sec (where in the song to stop playing)
 *        0x004C                        varies                - Number of times song has been played
 *        0x0050                        varies                - same as previous field?
 *        0x0054                        varies                - Date song was last played
 *
 * @see techdoc for more details
 * @author axelwe
 */
public class ITunesDBSongItem {
    /** tag size in bytes */
    private int tagSize = 156;

    /** item size */
    private int recordSize = 156;

    /** number of song content objects attached */
    private int contentCount = -1;

    /** record index */
    private int recordIndex = -1;
    private int unknown6 = 1;
    private int unknown7 = 0;

    /** bitrate */
    private int unknown8 = -1;

    /** date */
    private int date = -1;

    /** filesize */
    private int filesize = -1;

    /** duration */
    private int duration = -1;

    /** songorder */
    private int trackNumber = -1;
    private int unknown13 = 0;
    private int unknown14 = 0;

    /** bitrate of the song - its encoded ??*/
    private int bitrate = 0;

    /** the lower 16bit contain the samplerate of the song */
    private int samplerate = 0;

    /** volume adjustment range: -1000 - 1000 */
    private int emphasis = 0;

    /** start playing - offset in ms */
    private int innerStartTime = 0;

    /** stop playing - offset in ms */
    private int innerStopTime = 0;

    /** playcounter - // TODO is this really set by the iPod ?? */
    private int playcounter = -1;

    /** copy of playcounter ?? */
    private int playcounter2 = -1;

    /** date when the song was played */
    private int lastPlayedDate = -1;
    private int unknown23 = 0;
    private int unknown24 = 0;

    /** date when the song was modified */
    private int lastModifiedDate = 0;

    /** list of song content items */
    private List<ITunesDBContentItem> content = null;

    ITunesDBSongItem() {
        contentCount = 0;
        content = new ArrayList<ITunesDBContentItem>();
    }

    /**
     * @param contentItem
     */
    void attachContent(ITunesDBContentItem contentItem) {
        // add content
        this.content.add(contentItem);

        // correct song Item size
        this.recordSize += contentItem.getRecordSize();
        this.contentCount++;
    }

    /**
     * @return
     */
    java.util.Iterator<ITunesDBContentItem> getContentIterator() {
        return content.iterator();
    }

    /**
     * Calculates the _real_ size of the object.
     * The object is encoded into a byte array to determine size.
     *
     * @return real size of the record
     * @throws IOException
     */
    long calculateRecordSize() throws IOException {
        long size = 0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        ITunesDBParser.encodeSongItem(baos, this);
        baos.flush();
        size = baos.size();
        baos.close();

        return size;
    }

    /**
     * @return
     */
    public String toString() {
        return new StringBuffer("[tagSize] ").append(tagSize).append('\t').append("[recordSize] ").append(recordSize).append('\t').append("[contentCount] ").append(contentCount).append('\t').append("[recordIndex] ").append(recordIndex).append('\t').append("[unknown6] ").append(unknown6).append('\t').append("[unknown7] ").append(unknown7).append('\t').append("[unknown8] ").append(unknown8).append('\t').append("[date] ").append(new java.util.Date(ITunesDBParser.macDateToDate(date)).toString()).append('\t').append("[date] ").append(date).append('\t').append("[filesize] ").append(filesize).append('\t').append("[duration] ").append(duration).append('\t').append("[trackNumber] ").append(trackNumber).append('\t').append("[unknown13] ").append(unknown13).append('\t').append("[unknown14] ").append(unknown14).append('\t').append("[bitrate] ").append(bitrate).append('\t').append("[samplerate] ").append(samplerate).toString();
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
     * Getter for property contentCount.
     * @return Value of property contentCount.
     */
    public int getContentCount() {
        return contentCount;
    }

    /**
     * Setter for property contentCount.
     * @param contentCount New value of property contentCount.
     */
    public void setContentCount(int contentCount) {
        this.contentCount = contentCount;
    }

    /**
     * Getter for property recordIndex.
     * @return Value of property recordIndex.
     */
    public int getRecordIndex() {
        return recordIndex;
    }

    /**
     * Setter for property recordIndex.
     * @param recordIndex New value of property recordIndex.
     */
    public void setRecordIndex(int recordIndex) {
        this.recordIndex = recordIndex;
    }

    /**
     * Getter for property unknown6.
     * @return Value of property unknown6.
     */
    public int getUnknown6() {
        return unknown6;
    }

    /**
     * Setter for property unknown6.
     * @param unknown6 New value of property unknown6.
     */
    public void setUnknown6(int unknown6) {
        this.unknown6 = unknown6;
    }

    /**
     * Getter for property unknown7.
     * @return Value of property unknown7.
     */
    public int getUnknown7() {
        return unknown7;
    }

    /**
     * Setter for property unknown7.
     * @param unknown7 New value of property unknown7.
     */
    public void setUnknown7(int unknown7) {
        this.unknown7 = unknown7;
    }

    /**
     * Getter for property unknown8.
     * @return Value of property unknown8.
     */
    public int getUnknown8() {
        return unknown8;
    }

    /**
     * Setter for property unknown8.
     * @param unknown8 New value of property unknown8.
     */
    public void setUnknown8(int unknown8) {
        this.unknown8 = unknown8;
    }

    /**
     * Getter for property date.
     * @return Value of property date.
     */
    public int getDate() {
        return date;
    }

    /** Setter for property date.
     * @param date New value of property date.
     *
     */
    public void setDate(int date) {
        this.date = date;
    }

    /** Getter for property filesize.
     * @return Value of property filesize.
     *
     */
    public int getFilesize() {
        return filesize;
    }

    /** Setter for property filesize.
     * @param filesize New value of property filesize.
     *
     */
    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    /** Getter for property duration.
     * @return Value of property duration.
     *
     */
    public int getDuration() {
        return duration;
    }

    /** Setter for property duration.
     * @param duration New value of property duration.
     *
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /** Getter for property trackNumber.
     * @return Value of property trackNumber.
     *
     */
    public int getTrackNumber() {
        return trackNumber;
    }

    /** Setter for property trackNumber.
     * @param trackNumber New value of property trackNumber.
     *
     */
    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    /** Getter for property unknown13.
     * @return Value of property unknown13.
     *
     */
    public int getUnknown13() {
        return unknown13;
    }

    /** Setter for property unknown13.
     * @param unknown13 New value of property unknown13.
     *
     */
    public void setUnknown13(int unknown13) {
        this.unknown13 = unknown13;
    }

    /** Getter for property unknown14.
     * @return Value of property unknown14.
     *
     */
    public int getUnknown14() {
        return unknown14;
    }

    /** Setter for property unknown14.
     * @param unknown14 New value of property unknown14.
     *
     */
    public void setUnknown14(int unknown14) {
        this.unknown14 = unknown14;
    }

    /** Getter for property bitrate.
     * @return Value of property bitrate.
     *
     */
    public int getBitrate() {
        return bitrate;
    }

    /** Setter for property bitrate.
     * @param bitrate New value of property bitrate.
     *
     */
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    /** Getter for property samplerate.
     * @return Value of property samplerate.
     *
     */
    public int getSamplerate() {
        return samplerate;
    }

    /** Setter for property samplerate.
     * @param samplerate New value of property samplerate.
     *
     */
    public void setSamplerate(int samplerate) {
        this.samplerate = samplerate;
    }

    /** Getter for property emphasis.
     * @return Value of property emphasis.
     *
     */
    public int getEmphasis() {
        return emphasis;
    }

    /** Setter for property emphasis.
     * @param emphasis New value of property emphasis.
     *
     */
    public void setEmphasis(int emphasis) {
        this.emphasis = emphasis;
    }

    /** Getter for property innerStartTime.
     * @return Value of property innerStartTime.
     *
     */
    public int getInnerStartTime() {
        return innerStartTime;
    }

    /** Setter for property innerStartTime.
     * @param innerStartTime New value of property innerStartTime.
     *
     */
    public void setInnerStartTime(int innerStartTime) {
        this.innerStartTime = innerStartTime;
    }

    /** Getter for property innerStopTime.
     * @return Value of property innerStopTime.
     *
     */
    public int getInnerStopTime() {
        return innerStopTime;
    }

    /** Setter for property innerStopTime.
     * @param innerStopTime New value of property innerStopTime.
     *
     */
    public void setInnerStopTime(int innerStopTime) {
        this.innerStopTime = innerStopTime;
    }

    /** Getter for property playcounter.
     * @return Value of property playcounter.
     *
     */
    public int getPlaycounter() {
        return playcounter;
    }

    /** Setter for property playcounter.
     * @param playcounter New value of property playcounter.
     *
     */
    public void setPlaycounter(int playcounter) {
        this.playcounter = playcounter;
    }

    /** Getter for property playcounter2.
     * @return Value of property playcounter2.
     *
     */
    public int getPlaycounter2() {
        return playcounter2;
    }

    /** Setter for property playcounter2.
     * @param playcounter2 New value of property playcounter2.
     *
     */
    public void setPlaycounter2(int playcounter2) {
        this.playcounter2 = playcounter2;
    }

    /** Getter for property lastPlayedDate.
     * @return Value of property lastPlayedDate.
     *
     */
    public int getLastPlayedDate() {
        return lastPlayedDate;
    }

    /** Setter for property lastPlayedDate.
     * @param lastPlayedDate New value of property lastPlayedDate.
     *
     */
    public void setLastPlayedDate(int lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
    }

    /** Getter for property unknown23.
     * @return Value of property unknown23.
     *
     */
    public int getUnknown23() {
        return unknown23;
    }

    /** Setter for property unknown23.
     * @param unknown23 New value of property unknown23.
     *
     */
    public void setUnknown23(int unknown23) {
        this.unknown23 = unknown23;
    }

    /** Getter for property unknown24.
     * @return Value of property unknown24.
     *
     */
    public int getUnknown24() {
        return unknown24;
    }

    /** Setter for property unknown24.
     * @param unknown24 New value of property unknown24.
     *
     */
    public void setUnknown24(int unknown24) {
        this.unknown24 = unknown24;
    }

    /** Getter for property lastModifiedDate.
     * @return Value of property lastModifiedDate.
     *
     */
    public int getLastModifiedDate() {
        return lastModifiedDate;
    }

    /** Setter for property lastModifiedDate.
     * @param lastModifiedDate New value of property lastModifiedDate.
     *
     */
    public void setLastModifiedDate(int lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /** Getter for property content.
     * @return Value of property content.
     *
     */
    public java.util.List<ITunesDBContentItem> getContent() {
        return content;
    }

    /** Setter for property content.
     * @param content New value of property content.
     *
     */
    public void setContent(java.util.List<ITunesDBContentItem> content) {
        this.content = content;
    }
}
