// ListViewTableModel
// $Id: ClipsTableModel.java,v 1.11 2003/07/20 06:46:16 axelwernicke Exp $
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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.event.TableModelEvent;

import de.axelwernicke.mypod.util.ClipsTableUtils;


/**
 * Model for a list view table.
 * The model is mostly a wrapper around a playlist. Additionally all the stuff to sort a
 * table by columns is in here...
 *
 * @author axel wernicke
 */
public class ClipsTableModel extends javax.swing.table.AbstractTableModel implements javax.swing.event.TableModelListener {
    /** */
    private static ResourceBundle rb = ResourceBundle.getBundle("de.axelwernicke.mypod.resource");

    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod");

    /** the playlist currently assigned to this list view model */
    private Playlist playlist;

    /** index the table is sorted by */
    private int[] sortIdx;

    /** counts the compares */
    int compares;

    /** holds the sorting column as Integer */
    private List<Integer> sortingColumns = new ArrayList<Integer>(3);

    /** sorting order */
    private boolean ascending = true;

    /** Locale sensitive string comparator */
    private Collator collator = Collator.getInstance();

    /** Default Constructor */
    public ClipsTableModel() {
        this(null, null);
    }

    /**
     * Creates a new instance of ListViewModel and initializes with playlist.
     * 
     * @param _playlist Playlist to initialize table with.
     */
    public ClipsTableModel(Playlist _playlist) {
        this(_playlist, null);
    }

    /**
     * Creates a new instance of ListViewModel and initializes with playlist and
     * sorting.
     * 
     * @param _sortingColumns columns to define sorting.
     * @param _playlist Playlist to initialize playlist from.
     */
    public ClipsTableModel(Playlist _playlist, List<Integer> _sortingColumns) {
        // set new playlist
        this.playlist = (_playlist != null) ? _playlist : new Playlist();

        // set sorting
        this.sortingColumns = (_sortingColumns != null) ? _sortingColumns : new ArrayList<Integer>(3);

        // reallocate index for column sorting
        reallocateIndexes();

        // kick sorting
        sort();
    }

    /**
     * Sets a playlist to the model.
     * 
     * @param value Playlist to set.
     */
    public void setPlaylist(Playlist value) {
        // set new playlist
        playlist = value;

        // reallocate index for column sorting
        resetSortedBy();
        reallocateIndexes();

        this.fireTableDataChanged();
    }

    /** Gets the playlist behind the model.
     *
     * @return Playlist behind the model.
     */
    public Playlist getPlaylist() {
        return playlist;
    }

    /** Gets the column count of the table.
     *
     * @return column count.
     */
    public int getColumnCount() {
        return ClipsTableUtils.values().length;
    }

    /** gets the column name and adds a mark, if the table is sorted by this row
     *
     * @param column index of the column
     * @return name of the column
     */
    public String getColumnName(int column) {
        String name = ClipsTableUtils.values()[column].columnName();

        if ((sortingColumns.size() > 0) && (column == this.sortingColumns.get(0))) {
            name = rb.getString("sort.tag1") + name;
        }
        if ((sortingColumns.size() > 1) && (column == this.sortingColumns.get(1))) {
            name = rb.getString("sort.tag2") + name;
        }
        if ((sortingColumns.size() > 2) && (column == this.sortingColumns.get(2))) {
            name = rb.getString("sort.tag3") + name;
        }

        return name;
    }

    /** gets the class of a column
     *
     * @param column index of the column
     * @return class of the column
     */
    public Class<?> getColumnClass(int column) {
        switch (ClipsTableUtils.values()[column]) {
        // Integer:
        case Bitrate:
        case Emphasis:
        case Layer:
        case MpegLevel:
        case Mode:
        case PlaylistNR:
        case SampleRate:
        case Track:
        case Year:
            return java.lang.Integer.class;

        // Long:
        case Oid:
        case LastModified:
        case Duration:
        case FileSize:
            return java.lang.Long.class;

        // Boolean:
        case Copyright:
        case Original:
        case Padding:
        case Private:
        case Protection:
//        case UseCRC:
//        case UseCompression:
//        case UsePadding:
//        case UseUnsynchronization:
//        case WriteId3:
//        case WriteId3v2:
            return java.lang.Boolean.class;

        //  anything else:
        default:
            return java.lang.String.class;
        }
    }

    /** gets the total count of rows in the model
     *
     * @return total count of rows
     */
    public int getRowCount() {
        if (playlist == null) {
            return 0;
        }

        return playlist.getTotalClips();
    }

    /** gets a value of the model
     *
     * @param row specifies the row
     * @param col specifies the column
     * @return the object from the position (row, col)
     */
    public Object getValueAt(int row, int col) {
        return getUnsortedValueAt(sortIdx[row], col);
    }

    /** gets a value of the model
     *
     * @param row specifies the row
     * @param col specifies the column
     * @return the object from the position (row, col)
     */
    private Object getUnsortedValueAt(int row, int col) {
        MP3Meta mp3Meta = null;
        try {
            mp3Meta = myPod.getDataPool().getMeta(playlist.getClipAt(row));

            switch (ClipsTableUtils.values()[col]) {

            case Oid:
                return myPod.getDataPool().getOid(mp3Meta.getFile().getPath());
            case PlaylistNR:
                return row + 1;

            // meta orig
            case Duration:
                return mp3Meta.getDuration();

            // file
            case LastModified:
                return mp3Meta.getFile().lastModified();
            case Filename:
                return mp3Meta.getFile().getName();
            case FilePath:
                return mp3Meta.getFile().getPath();
            case FileSize:
                return mp3Meta.getFile().length();

            // prop
            case Layer:
            case Bitrate:
            case SampleRate:
            case Mode:
            case Emphasis:
            case Protection:
            case Private:
            case Padding:
            case Copyright:
            case Original:
                return mp3Meta.getFile().getProperty(ClipsTableUtils.values()[col].name());

            // case Length:
            // case VBR:
            
            case Album:
            case Artist:
            case ArtistWebpage:
            case AudioFileWebpage:
            case AudioSourceWebpage:
            case BPM:
            case Band:
            case CDIdentifier:
            case Comment:
            case Commercial:
            case CommercialInfo:
            case Composer:
            case Conductor:
            case ContentGroupSet:
            case CopyrightText:
            case CopyrightWebpage:
            case Date:
            case EncapsulatedObject:
            case EncodedBy:
            case EncryptionMethodRegistration:
            case Equalisation:
            case EventTimingCodes:
            case FileOwner:
            case FileType:
            case Genre:
            case GroupIdentRegistration:
            case ISRC:
            case InitialKey:
            case InternetRadioStationName:
            case InternetRadioStationOwner:
            case InternetRadioWebpage:
            case Language:
            case LengthInTag:
            case LookupTable:
            case Lyricist:
            case MpegLevel:
            case MediaType:
            case Name:
            case OriginalArtist:
            case OriginalFilename:
            case OriginalLyricist:
            case OriginalTitle:
            case OriginalYear:
            case Ownership:
            case PartOfSet:
            case PaymentWebpage:
            case Picture:
            case PlayCounter:
            case PlaylistDelay:
            case Popularimeter:
            case PositionSynchronization:
            case PrivateData:
            case Publisher:
            case PublishersWebpage:
            case RecommendedBufferSize:
            case RecordingDate:
            case RelativeVolumeAdjustment:
            case Remixer:
            case Reverb:
            case Subtitle:
            case SynchronizedLyrics:
            case SynchronizedTempoCodes:
            case TermsOfUse:
            case Time:
            case Title:
            case Track:
            case UniqueFileIdentifier:
            case UnsynchronizedLyrics:
            case UserDefinedText:
            case UserDefinedURL:
            case Year:
                return mp3Meta.get(ClipsTableUtils.values()[col].name());

//            case UseCRC:
//                return new Boolean(mp3Meta.isUseCRC());
//            case UseCompression:
//                return new Boolean(mp3Meta.isUseCompression());
//            case UsePadding:
//                return new Boolean(mp3Meta.isUsePadding());
//            case UseUnsynchronization:
//                return new Boolean(mp3Meta.isUseUnsynchronization());
//
//            case WriteId3:
//                return new Boolean(mp3Meta.isWriteID3());
//            case WriteId3v2:
//                return new Boolean(mp3Meta.isWriteID3v2());

            default:
                return "";
            }
        } catch (Exception ex) {
            logger.warning("Exception raised: " + ex.getMessage());
            logger.warning("mp3Meta was: " + mp3Meta + " row: " + row + " col: " + col);
            ex.printStackTrace();
            return null;
        }
    }

    /** Resets the sorting. */
    void resetSortedBy() {
        sortingColumns.clear();
    }

    /** Checks if the data in the model has the same count of rows as the sorting index array.
     *
     * @return true, if sorter index an playlist have the sampe size
     */
    public boolean isValid() {
        return sortIdx.length == getRowCount();
    }

    /** validates list view table model.
     *
     * check playlist.size() == sortIdx.length
     * check playlist.totalClipsTime()
     */
    public void validate() {
        logger.entering("ListViewTableModel", "validate");

        if (!isValid()) {
            logger.info("table index not valid");

            // reallocate sort index
            reallocateIndexes();

            // restore sorting
            sort();
        }

        logger.exiting("ListViewTableModel", "validate");
    }

    /** Inform everbody listening that the model changed.
     *
     * @param tableModelEvent event to post
     */
    public void tableChanged(TableModelEvent tableModelEvent) {
        logger.entering("ListViewTableModel", "tableChanged");

        // revalidate the model if neccessary
        validate();

        // inform anybody
        fireTableChanged(tableModelEvent);

        logger.exiting("ListViewTableModel", "tableChanged");
    }

    // -----------------  sorting stuff -------------------------------

    /** Reallocates sorting index.
     */
    public void reallocateIndexes() {
        logger.entering("ListViewTableModel", "reallocateIndexes");

        int rowCount = getRowCount();

        // Set up a new array of indexes with the right number of elements
        sortIdx = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++) {
            sortIdx[row] = row;
        }

        logger.exiting("ListViewTableModel", "reallocateIndexes");
    } // reallocate indexes

    /** Compares two elements of a column.
     *
     * @param row1 to compare
     * @param row2 to compare
     * @param column to compare
     * @return result to the comparison.
     */
    public int compareRowsByColumn(int row1, int row2, int column) {
        // get object type for the column
        Class<?> type = getColumnClass(column);

        // get the objects to compare
        Object o1 = getUnsortedValueAt(row1, column);
        Object o2 = getUnsortedValueAt(row2, column);

        // If both values are null return 0
        if ((o1 == null) && (o2 == null)) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } // Define null less than everything.
        else if (o2 == null) {
            return 1;
        }

        // We copy all returned values from the getValue call in case
        // an optimised model is reusing one object to return many values.
        // The Number subclasses in the JDK are immutable and so will not be used in
        // this way but other subclasses of Number might want to do this to save
        // space and avoid unnecessary heap allocation.
        if (type.getSuperclass() == java.lang.Number.class) {
            double d1 = ((Number) o1).doubleValue();
            double d2 = ((Number) o2).doubleValue();

            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == Date.class) {
            long n1 = ((Date) o1).getTime();
            long n2 = ((Date) o2).getTime();

            if (n1 < n2) {
                return -1;
            } else if (n1 > n2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == String.class) {
            return collator.compare((String) o1, (String) o2);
        } else if (type == Boolean.class) {
            boolean b1 = ((Boolean) o1).booleanValue();
            boolean b2 = ((Boolean) o2).booleanValue();

            if (b1 == b2) {
                return 0;
            } else if (b1) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return collator.compare(o1.toString(), o2.toString());
        }
    }

    /** Compares two values of a column to sort by
     *
     * @param row1 value1
     * @param row2 value2
     * @return result
     */
    public int compare(int row1, int row2) {
        //logger.entering("ListViewTableModel", "compare");
        int result;
        compares++;

        // do it for all columns to compare
        int sortingColumnSize = sortingColumns.size();
        for (int level = 0; level < sortingColumnSize; level++) {
            result = compareRowsByColumn(row1, row2, sortingColumns.get(level));

            if (result != 0) {
                return ascending ? result : (-result);
            }
        }

        //logger.exiting("ListViewTableModel", "compare");
        return 0;
    }

    /** Sort the model.
     *        <br- check the integrity of the model
     *        <br- kick (shuttle-)sorting
     *        <br- fire table changed event
     */
    public void sort() {
        // check integrity
        validate();

        // reset statistics
        long start = System.currentTimeMillis();
        compares = 0;

        // do sorting
        shuttlesort(sortIdx.clone(), sortIdx, 0, sortIdx.length);

        // log statistics
        double duration = System.currentTimeMillis() - start;
        logger.info("Compares: " + compares + " time: " + (duration / 1000.0) + "s, equals to " + (compares / (duration / 1000.0)) + " compares per second.");

        // inform all about changes
        tableChanged(new TableModelEvent(this));
    } // sort

//	/** n2sorting algorithm - not in use
//	 */
//	public void n2sort()
//	{
//		for(int i = 0; i < getRowCount(); i++)
//		{
//			for(int j = i+1; j < getRowCount(); j++)
//			{
//				if (compare(sortIdx[i], sortIdx[j]) == -1)
//				{
//					swap(i, j);
//				}
//			}
//		}
//	} // n2sort
//	

    /** This is a home-grown implementation which we have not had time
     * to research - it may perform poorly in some circumstances. It
     * requires twice the space of an in-place algorithm and makes
     * NlogN assigments shuttling the values between the two
     * arrays. The number of compares appears to vary between N-1 and
     * NlogN depending on the initial order but the main reason for
     * using it here is that, unlike qsort, it is stable.
     *
     * @param from source array
     * @param to destination array
     * @param low index
     * @param high index
     */
    public void shuttlesort(int[] from, int[] to, int low, int high) {
        // there is nothing to do, break recursion
        if ((high - low) < 2) {
            return;
        }

        // split
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        // This is an optional short-cut; at each recursive call,
        // check to see if the elements in this subset are already
        // ordered.  If so, no further comparisons are needed; the
        // sub-array can just be copied.  The array must be copied rather
        // than assigned otherwise sister calls in the recursion might
        // get out of sinc.  When the number of elements is three they
        // are partitioned so that the first set, [low, mid), has one
        // element and and the second, [mid, high), has two. We skip the
        // optimisation when the number of elements is three or less as
        // the first compare in the normal merge will produce the same
        // sequence of steps. This optimisation seems to be worthwhile
        // for partially ordered lists but some analysis is needed to
        // find out how the performance drops to Nlog(N) as the initial
        // order diminishes - it may drop very quickly.
        if (((high - low) >= 4) && (compare(from[middle - 1], from[middle]) <= 0)) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }

        // A normal merge.
        for (int i = low; i < high; i++) {
            if ((q >= high) || ((p < middle) && (compare(from[p], from[q]) <= 0))) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    } // shuttlesort

    /** Swaps two elements.
     * @param i element to swap.
     * @param j element to swap.
     */
    public void swap(int i, int j) {
        int tmp = sortIdx[i];
        sortIdx[i] = sortIdx[j];
        sortIdx[j] = tmp;
    }

    /** Sorts a a table model by column ascending.
     *
     * @param column to sort by
     */
    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    /** Sorts a a table model by column.
     *
     * @param column to sort by
     * @param ascending sorting oder
     */
    public void sortByColumn(int column, boolean ascending) {
        // set order
        this.ascending = ascending;

        // FIFO indices, but only if column changed
        if ((sortingColumns.size() > 0 && column != sortingColumns.get(0)) || sortingColumns.size() == 0) {
            sortingColumns.add(0, new Integer(column));
        }

        // we want max 3 sorted columns
        if (sortingColumns.size() > 3) {
            sortingColumns.remove(3);
        }

        // make sure that first and third sorting column are not the same
        if (sortingColumns.size() > 2 && sortingColumns.get(0) == sortingColumns.get(2)) {
            sortingColumns.remove(2);
        }

        // just do it
        sort();
    }

    /**
     * Getter for property sortingColumns.
     * @return Value of property sortingColumns.
     */
    public List<Integer> getSortingColumns() {
        return sortingColumns;
    }

    /**
     * Setter for property sortingColumns.
     * @param _sortingColumns List containing the columns to sort the table by.
     */
    public void setSortingColumns(List<Integer> _sortingColumns) {
        this.sortingColumns = _sortingColumns;

        // kick sorting
        sort();
    }
}

/* */

