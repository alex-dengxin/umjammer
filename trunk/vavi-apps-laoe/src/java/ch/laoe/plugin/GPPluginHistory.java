/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ch.laoe.ui.Debug;
import ch.laoe.ui.GLanguage;


/**
 * Class: GPPluginHistory @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to visualize the last used plugins, and start them again.
 * 
 * @version 14.03.02 first draft oli4
 * 
 */
public class GPPluginHistory extends GPluginFrame {
    public GPPluginHistory(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "pluginHistory";
    }

    public void reload() {
        tableModel.update();
        frame.validate();
        // frame.repaint();
    }

    // GUI

    private JTable table;

    private HistoryTableModel tableModel;

    private ArrayList clipPanels;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        // main structure
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        // pScroll.setLayout(new BoxLayout(pScroll, BoxLayout.Y_AXIS));

        // top components
        // JPanel pTop = new JPanel();
        // p.add(pTop, BorderLayout.NORTH);

        // layer stack
        tableModel = new HistoryTableModel();
        table = new JTable(tableModel);
        // table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableColumn iconColumn = table.getColumnModel().getColumn(0);
        iconColumn.setCellRenderer(new HistoryTableIconRenderer());
        iconColumn.setMaxWidth(30);

        // tableModel.addTableModelListener(table);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(new Dimension(200, 70));
        p.add(scrollPane, BorderLayout.CENTER);

        // bottom
        JPanel pBottom = new JPanel();
        p.add(pBottom, BorderLayout.SOUTH);

        // storeButton = new JButton(GLanguage.translate("manualSave"));
        // pBottom.add(storeButton);

        frame.getContentPane().add(p);
        frame.setSize(new Dimension(200, 200));
        // pack();

        eventDispatcher = new EventDispatcher();
        table.addMouseListener(eventDispatcher);
    }

    /**
     * loads all data from the selected clip
     */
    /*
     * public void onClipChange () { try { AClipHistory h = getFocussedClip().getHistory(); tableModel.setHistory(h);
     * 
     * memory.setData(AClipHistory.getMemorySize()); } catch (NullPointerException e){}
     * 
     * updateThisAndFocussedClip(); }
     */

    private class EventDispatcher implements ActionListener, MouseListener {
        public void actionPerformed(ActionEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            if (isButton1(e)) {
                // GProgressViewer.start(getName());
                Debug.println(1, "plugin " + getName() + " [history entry] clicked");
                pluginHandler.getPluginHistoryEntry(table.getSelectedRow()).start();
                // GProgressViewer.finish();
            }
        }

        public void mouseClicked(MouseEvent e) {
        }
    }

    private class HistoryTableIconRenderer implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                return new JLabel((ImageIcon) value);
            } else {
                return new JLabel();
            }
        }
    }

    private class HistoryTableModel extends AbstractTableModel {
        final String columnNames[] = {
            "", GLanguage.translate("plugin")
        };

        public void update() {
            fireTableDataChanged();
        }

        public String getColumnName(int col) {
            return columnNames[col].toString();
        }

        public int getRowCount() {
            return pluginHandler.getPluginHistoryLength();
        }

        public int getColumnCount() {
            return 2;
        }

        /*
         * public Class getColumnClass(int c) { try { switch (c) { case 0: return getValueAt(0, c).getClass();
         * 
         * default: return getValueAt(0, c).getClass(); } } catch (Exception e) { return super.getColumnClass(c); } }
         */

        public Object getValueAt(int row, int col) {
            try {
                switch (col) {
                case 0:
                    ImageIcon i = pluginHandler.getPluginHistoryEntry(row).loadIcon();
                    if (i != null) {
                        return i;
                    } else {
                        return loadIcon();
                    }

                case 1:
                    return GLanguage.translate(pluginHandler.getPluginHistoryEntry(row).getName());

                default:
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void setValueAt(Object value, int row, int col) {
            fireTableCellUpdated(row, col);
        }
    }

}
