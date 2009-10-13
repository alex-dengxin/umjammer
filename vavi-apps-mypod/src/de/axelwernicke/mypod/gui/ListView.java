// ListView
// $Id: ListView.java,v 1.17 2003/07/20 06:46:17 axelwernicke Exp $
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
package de.axelwernicke.mypod.gui;

import java.awt.FontMetrics;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import de.axelwernicke.mypod.ClipsTableColumnModel;
import de.axelwernicke.mypod.util.ClipsTableUtils;


/** Class that extends JTable and adds methods for handling clips table
 *
 * @author  axel wernicke
 */
public class ListView extends javax.swing.JTable {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod.gui");

    /** dummy object for DnD */
    List<Long> clips;

    /** Creates new form BeanForm */
    public ListView() {
        super();
        setTransferHandler(new javax.swing.TransferHandler("clips"));
        this.setName(GuiUtils.getStringLocalized("resource/language", "listView"));
    }

    /** Gets the with of values for a column of the table as List
     *
     * @param columnIndex index of the column to get width values for
     * @return array containing the width values for all cells of the column
     */
    int[] getValuesWidthForOptimization(int columnIndex) {
        int rowCount = this.getRowCount();
        int rowInkrement = (rowCount / 1000);
        rowInkrement = (rowInkrement == 0) ? 1 : rowInkrement;
        int[] result = new int[(rowCount / rowInkrement) + 1];
        int resultCnt = 0;

        int staticWidth = 0;
        int variableWidth = 0;
        Object value = null;
        FontMetrics fontMetrics = this.getFontMetrics(this.getFont());

        javax.swing.table.TableCellRenderer cellRenderer = this.getCellRenderer(0, columnIndex);
        String cellRendererClassName = cellRenderer.getClass().getName();

        // boolean values have a static width
        if (cellRendererClassName.equals("javax.swing.JTable$BooleanRenderer")) {
            for (int row = 0; row < rowCount; row += rowInkrement) {
                result[resultCnt++] = 20;
            }
        }
        // playlist number value
        else if (cellRendererClassName.equals("de.axelwernicke.mypod.ClipsTableColumnModel$PlaylistIndexNumberRenderer")) {
            staticWidth = fontMetrics.stringWidth(". ");
            for (int row = 0; row < rowCount; row += rowInkrement) {
                value = this.getValueAt(row, columnIndex);
                variableWidth = (value != null) ? fontMetrics.stringWidth(value.toString()) : 0;
                result[resultCnt++] = staticWidth + variableWidth;
            }
        }
        // date value
        else if (cellRendererClassName.equals("de.axelwernicke.mypod.ClipsTableColumnModel$DateRenderer")) {
            DateFormat df = DateFormat.getDateTimeInstance();
            Date date = new Date();
            for (int row = 0; row < rowCount; row += rowInkrement) {
                value = this.getValueAt(row, columnIndex);
                if (value != null) {
                    date.setTime(((Long) value).longValue());
                }
                result[resultCnt++] = (value != null) ? fontMetrics.stringWidth(df.format(date)) : 0;
            }
        }
        // duration value
        else if (cellRendererClassName.equals("de.axelwernicke.mypod.ClipsTableColumnModel$DurationRenderer")) {
            for (int row = 0; row < rowCount; row += rowInkrement) {
                value = this.getValueAt(row, columnIndex);
                result[resultCnt++] = (value != null) ? fontMetrics.stringWidth(GuiUtils.formatTime(((Long) value).longValue())) : 0;
            }
        }
        // filesize value
        else if (cellRendererClassName.equals("de.axelwernicke.mypod.ClipsTableColumnModel$FilesizeRenderer")) {
            staticWidth = fontMetrics.stringWidth(" ");
            for (int row = 0; row < rowCount; row += rowInkrement) {
                value = this.getValueAt(row, columnIndex);
                variableWidth = (value != null) ? fontMetrics.stringWidth(GuiUtils.formatFilesizeKB(((Long) value).longValue())) : 0;
                result[resultCnt++] = staticWidth + variableWidth;
            }
        }
        // emphasis value
        else if (cellRendererClassName.equals("de.axelwernicke.mypod.ClipsTableColumnModel$EmphasisRenderer")) {
            staticWidth = fontMetrics.stringWidth(" 5015MS ");
            for (int row = 0; row < rowCount; row += rowInkrement) {
                result[resultCnt++] = staticWidth;
            }
        } else if (cellRendererClassName.equals("de.axelwernicke.mypod.ClipsTableColumnModel$ModeRenderer")) {
            staticWidth = fontMetrics.stringWidth(" Joint Stereo ");
            for (int row = 0; row < rowCount; row += rowInkrement) {
                result[resultCnt++] = staticWidth;
            }
        }
        // MPEG layer value
        else if (cellRendererClassName.equals("de.axelwernicke.mypod.ClipsTableColumnModel$LayerRenderer")) {
            staticWidth = fontMetrics.stringWidth(" III ");
            for (int row = 0; row < rowCount; row += rowInkrement) {
                result[resultCnt++] = staticWidth;
            }
        }
        // MPEG level value
        else if (cellRendererClassName.equals("de.axelwernicke.mypod.ClipsTableColumnModel$LevelRenderer")) {
            staticWidth = fontMetrics.stringWidth(" MPEG 2 ");
            for (int row = 0; row < rowCount; row += rowInkrement) {
                result[resultCnt++] = staticWidth;
            }
        }
        // samplerate value
        else if (cellRendererClassName.equals("de.axelwernicke.mypod.ClipsTableColumnModel$SamplerateRenderer")) {
            staticWidth = fontMetrics.stringWidth(" Hz ");
            for (int row = 0; row < rowCount; row += rowInkrement) {
                value = this.getValueAt(row, columnIndex);
                variableWidth = (value != null) ? fontMetrics.stringWidth(value.toString()) : 0;
                result[resultCnt++] = staticWidth + variableWidth;
            }
        }
        // bitrate value
        else if (cellRendererClassName.equals("de.axelwernicke.mypod.ClipsTableColumnModel$BitrateRenderer")) {
            staticWidth = fontMetrics.stringWidth("[v] kbit ");
            for (int row = 0; row < rowCount; row += rowInkrement) {
                value = this.getValueAt(row, columnIndex);
                variableWidth = (value != null) ? fontMetrics.stringWidth(value.toString()) : 0;
                result[resultCnt++] = staticWidth + variableWidth;
            }
        }
        // for all other renderers
        else {
            for (int row = 0; row < rowCount; row += rowInkrement) {
                value = this.getValueAt(row, columnIndex);
                result[resultCnt++] = (value != null) ? fontMetrics.stringWidth(value.toString()) : 0;
            }
        }

        return result;
    }

    /** Optimizes the width of all colums
     *
     * @param hideEmptyColumns empty columns width is set to 0 if this is true
     */
    public void optimizeColumnWidth(boolean hideEmptyColumns) {
        // get the column model
        ClipsTableColumnModel columnModel = (ClipsTableColumnModel) this.getColumnModel();

        FontMetrics fm = this.getTableHeader().getFontMetrics(this.getTableHeader().getFont());

        // optimize each column
        int colCount = this.getColumnCount();
        for (int col = 0; col < colCount; col++) {
            columnModel.optimizeColumnWidth(getValuesWidthForOptimization(col), col, fm.stringWidth(getColumnName(col)) + 5, hideEmptyColumns);
        }
    }

    /** Hides the column the current view is spread by
     *
     * @param viewName name of the current view
     * @param preferences object
     */
    public void hideSpreadViewColumn(String viewName, de.axelwernicke.mypod.Preferences preferences) {
        int hideColumnIndex = -1;

        if ((viewName != null) && (preferences != null)) {
            // determine view and appropriate column
            if (viewName.equals("artistView")) {
                hideColumnIndex = preferences.getListViewColumnInfo()[ClipsTableUtils.Artist.ordinal()][2];
            } else if (viewName.equals("albumView")) {
                hideColumnIndex = preferences.getListViewColumnInfo()[ClipsTableUtils.Album.ordinal()][2];
            } else if (viewName.equals("genreView")) {
                hideColumnIndex = preferences.getListViewColumnInfo()[ClipsTableUtils.Genre.ordinal()][2];
            }

            // hide column the view is spread by
            if (hideColumnIndex > -1) {
                this.getColumnModel().getColumn(hideColumnIndex).setPreferredWidth(0);
            }
        }
    }

    /** gets tracks from ListView for DnD - drag from list view.
     * oids are determined from filename via data pool
     *
     * @return  List of oid's
     */
    public List<Long> getSelectedClips() {
        logger.entering("ListView", "getSelectedClips");

        String filename;
        Long oid = null;
        javax.swing.table.TableModel model = getModel();

        // get selected rows
        int[] selected = getSelectedRows();
        int selectedCnt = getSelectedRowCount();
        List<Long> result = new ArrayList<Long>(selectedCnt);

        // determine rows selected in view ( rows to drag )
        for (int i = 0; i < selectedCnt; i++) {
            try {
                // since we can't get oids directly, get files path & name
                filename = (String) model.getValueAt(selected[i], ClipsTableUtils.FilePath.ordinal());

                // determine oid for the selected clip and add it
                oid = de.axelwernicke.mypod.myPod.getOid(filename);
                if (oid != null) {
                    result.add(oid);
                }
            } catch (Exception e) {
                logger.warning(e.getClass().getName() + " raised: " + e.getMessage());
            }
        }
        logger.exiting("ListView", "getSelectedClips");

        return result;
    }

    /** for DnD purposes only
     * @return
     */
    public List<Long> getClips() {
        return getSelectedClips();
    }

    /** sets clips to the list view.
     * Actually this method is called if a clip is dropped to the list view.
     * Please note, that this method is not implemented yet.
     *
     * @param oids List of oids dropped
     */
    public void setClips(List<Long> oids) {
        logger.entering("ListView", "getClips()");
        logger.info("DnD - got " + oids.size() + " clips to drop");
        logger.exiting("ListView", "getClips()");
    }

    @SuppressWarnings("unused")
    private void initComponents() //GEN-BEGIN:initComponents
     {
        setModel(null);
        setColumnModel(new de.axelwernicke.mypod.ClipsTableColumnModel());
        setDragEnabled(true);
        setGridColor(new java.awt.Color(204, 204, 204));
    } //GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
