// ListViewUtils
// $Id: ClipsTableUtils.java,v 1.5 2003/02/03 19:07:13 axelwernicke Exp $
//
// Copyright (C) 2002 Axel Wernicke <axel.wernicke@gmx.de>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package de.axelwernicke.mypod.util;

import de.axelwernicke.mypod.gui.GuiUtils;


/**
 * This class defines some helpers for the list view table. For each mp3 meta
 * data an abstract name and a number are defined. Furthermore a name for each
 * column of the list view table is defined.
 * 
 * @author axel wernicke
 */
public enum ClipsTableUtils {
    /**
     * list of static variables that declare which mp3meta information is
     * assigned to which column of the list view table model
     */
    PlaylistNR
    (1, 50, -1),

    Duration
    (1, 50, -1),
    Title
    (1, 150, -1),
    Album
    (1, 200, -1),
    Artist
    (1, 150, -1),
    Track
    (1, 25, -1),
    PartOfSet
    (1, 50, -1),
    Year
    (1, 50, -1),
    Genre
    (1, 50, -1),
    Comment
    (1, 50, -1),

    Composer
    (1, 50, -1),

    Bitrate
    (1, 50, -1),
    SampleRate
    (1, 50, -1),

    FileSize
    (1, 50, -1),
    FilePath
    (1, 250, -1),
    Filename
    (1, 250, -1),
    LastModified
    (1, 50, -1),
    FileType
    (1, 50, -1),

    EncodedBy
    (1, 50, -1),
    PlayCounter
    (1, 50, -1),
    Popularimeter
    (1, 50, -1),
    Picture
    (1, 50, -1),

    Mode
    (1, 50, -1),
    BPM
    (1, 50, -1),
    Emphasis
    (1, 50, -1),
    CDIdentifier
    (0, 50, -1),
    Layer
    (1, 50, -1),
    LengthInTag
    (1, 50, -1),
    MpegLevel
    (1, 50, -1),

    Date
    (0, 50, -1),
    Time
    (0, 50, -1),

    ArtistWebpage
    (1, 10, -1),
    AudioFileWebpage
    (1, 50, -1),
    AudioSourceWebpage
    (1, 50, -1),
    Band
    (1, 50, -1),
    Commercial
    (1, 50, -1),
    CommercialInfo
    (1, 50, -1),
    Conductor
    (1, 50, -1),
    ContentGroupSet
    (1, 50, -1),
    Copyright
    (1, 50, -1),
    CopyrightText
    (1, 50, -1),
    CopyrightWebpage
    (1, 50, -1),
    EncapsulatedObject
    (0, 50, -1),
    EncryptionMethodRegistration
    (0, 50, -1),
    Equalisation
    (0, 50, -1),
    EventTimingCodes
    (0, 50, -1),
    FileOwner
    (0, 50, -1),
    GroupIdentRegistration
    (0, 50, -1),
    ISRC
    (0, 50, -1),
    InitialKey
    (0, 50, -1),
    InternetRadioStationName
    (1, 50, -1),
    InternetRadioStationOwner
    (1, 50, -1),
    InternetRadioWebpage
    (1, 50, -1),
    Language
    (1, 50, -1),
    LookupTable
    (0, 50, -1),
    Lyricist
    (1, 50, -1),
    MediaType
    (1, 50, -1),
    Name
    (0, 50, -1),
    Original
    (1, 50, -1),
    OriginalArtist
    (1, 50, -1),
    OriginalFilename
    (0, 50, -1),
    OriginalLyricist
    (1, 50, -1),
    OriginalTitle
    (1, 50, -1),
    OriginalYear
    (1, 50, -1),
    Ownership
    (1, 50, -1),
    Padding
    (0, 50, -1),
    PaymentWebpage
    (1, 50, -1),
    PlaylistDelay
    (0, 50, -1),
    PositionSynchronization
    (0, 50, -1),
    Private
    (1, 50, -1),
    PrivateData
    (0, 50, -1),
    Protection
    (1, 50, -1),
    Publisher
    (1, 50, -1),
    PublishersWebpage
    (1, 50, -1),
    RecommendedBufferSize
    (0, 50, -1),
    RecordingDate
    (1, 50, -1),
    RelativeVolumeAdjustment
    (1, 50, -1),
    Remixer
    (0, 50, -1),
    Reverb
    (0, 50, -1),
    Subtitle
    (1, 50, -1),
    SynchronizedLyrics
    (1, 50, -1),
    SynchronizedTempoCodes
    (0, 50, -1),
    TermsOfUse
    (1, 50, -1),
    UniqueFileIdentifier
    (0, 50, -1),
    UnsynchronizedLyrics
    (1, 50, -1),
//    UseCRC
//    (0, 50, -1),
//    UseCompression
//    (0, 10, -1),
//    UsePadding
//    (0, 10, -1),
//    UseUnsynchronization
//    (0, 10, -1),
    UserDefinedText
    (1, 10, -1),
    UserDefinedURL
    (1, 10, -1),
//    WriteId3
//    (0, 10, -1),
//    WriteId3v2
//    (0, 10, -1),
    Oid
    (1, 50, -1);
    /** */
    int visibility;
    /** */
    int width;
    /** */
    int position;
    /** */
    public int getPosition() {
        return position;
    }
    /** */
    public int getVisibility() {
        return visibility;
    }
    /** */
    public int getWidth() {
        return width;
    }
    /** */
    ClipsTableUtils(int visibility, int width, int position) {
        this.visibility = visibility;
        this.width = width;
        this.position = position;
    }
    /** containing the names of each column in the list view */
    public String columnName() {
        return GuiUtils.getStringLocalized("resource/language", toString());
    }
}

/* */
