// MP3Meta
// $Id: MP3Meta.java,v 1.16 2003/07/20 06:46:16 axelwernicke Exp $
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

import java.io.IOException;
import java.util.logging.Logger;

import vavi.util.tag.Tag;
import vavi.util.tag.TagException;
import vavi.util.tag.id3.ID3Tag;
import vavi.util.tag.id3.ID3TagException;
import vavi.util.tag.id3.MP3File;
import vavi.util.tag.id3.v2.ID3v2;



/**
 * holds all information about a single mp3 file
 *
 * @author  axel wernicke
 */
public class MP3Meta implements java.io.Serializable {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** */
    private MP3File mp3File;

    /**
     * Creates a new instance of MP3Meta
     */
    public MP3Meta() {
    }

    /**
     * Creates a new instance of MP3Meta
     */
    public MP3Meta(MP3File mp3File) {
        this.mp3File = mp3File;
    }

    /** */
    public MP3File getFile() {
        if (mp3File == null) {
            try {
                this.mp3File = new MP3File("");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ID3TagException e) {
                e.printStackTrace();
            }
            // TODO copy into mp3file
        }

        return mp3File;
    }

    /** */
    public Object get(String key) {
        Object value = null;
        if (mp3File.hasTag(ID3Tag.Type.ID3v2)) {
            Tag tag = mp3File.getTag(ID3Tag.Type.ID3v2);

            try {
                value = tag.getTag(key);
            } catch (TagException e) {
//logger.info("v2 no value for: " + key);
                return "";
            } 

            //
            if ("Track".equals(key)) {
                String track = (String) value;
                if (value != null && !value.equals("")) {
                    // sometimes we get a tracknumber like 3/15 - check this!
                    if (track.indexOf("/") != -1) {
                        track = track.substring(0, track.indexOf("/"));
                    }
                    return new Integer(track);
                }
            } else if ("Picture".equals(key)) {
                return ((byte[]) value).length + " bytes";
            }

        } else if (mp3File.hasTag(ID3Tag.Type.ID3v1)) {
            Tag tag = mp3File.getTag(ID3Tag.Type.ID3v1);
            try {
                value = tag.getTag(key);
            } catch (TagException e) {
//logger.info("v1 no value for: " + key);
                return "";
            } 
        } else {
logger.info("file has no tags: " + mp3File);
            if ("Track".equals(key) ||
                "Year".equals(key)) {
                return 0;
            } else {
                return "";
            }
        }

        return value;
    }

    /** */
    public void set(String key, Object value) {
        // TODO
    }

    /**
     */
    public long getDuration() {
        long lengthInTag = -1;
        // get length from tag
        if (mp3File.hasTag(ID3Tag.Type.ID3v2)) {
            try {
                lengthInTag = Long.parseLong((String) ((ID3v2) mp3File.getTag(ID3Tag.Type.ID3v2)).getTag("LengthInTag"));
            } catch (Exception e) {
                // there can be various uncritical exception, so don't care
            }
        }
        
        // return length from tag, if it seems to be valid, calculated length (more sensible and inaccurate) otherwise
        return (lengthInTag > 0) ? (lengthInTag / 1000) : (Long) mp3File.getProperty("Length");
    }
}

/* */
