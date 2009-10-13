// myPod
// $Id: myPod.java,v 1.52 2003/08/03 09:45:28 axelwernicke Exp $
//
// Copyright (C) 2002-2003 Axel Wernicke <axelwernicke@users.sourceforge.net>
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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import de.axelwernicke.mypod.actions.AboutAction;
import de.axelwernicke.mypod.actions.AddClipsToPlaylistAction;
import de.axelwernicke.mypod.actions.ClipPropertiesAction;
import de.axelwernicke.mypod.actions.DeleteClipsAction;
import de.axelwernicke.mypod.actions.DeletePlaylistAction;
import de.axelwernicke.mypod.actions.ExitAction;
import de.axelwernicke.mypod.actions.ExportPlaylistAction;
import de.axelwernicke.mypod.actions.HelpAction;
import de.axelwernicke.mypod.actions.ImportPlaylistAction;
import de.axelwernicke.mypod.actions.NewAutoPlaylistAction;
import de.axelwernicke.mypod.actions.NewPlaylistAction;
import de.axelwernicke.mypod.actions.PlayClipsAddAction;
import de.axelwernicke.mypod.actions.PlayClipsAppendAction;
import de.axelwernicke.mypod.actions.PlayPlaylistAddAction;
import de.axelwernicke.mypod.actions.PlayPlaylistAppendAction;
import de.axelwernicke.mypod.actions.PlaylistPropertyAction;
import de.axelwernicke.mypod.actions.PreferencesAction;
import de.axelwernicke.mypod.actions.RelocateMissingClipsAction;
import de.axelwernicke.mypod.actions.RemoveClipsMyPodAction;
import de.axelwernicke.mypod.actions.RemoveClipsPlaylistAction;
import de.axelwernicke.mypod.actions.RemoveMissingClipsAction;
import de.axelwernicke.mypod.actions.RenamePlaylistAction;
import de.axelwernicke.mypod.actions.ReorganizeClipsAction;
import de.axelwernicke.mypod.actions.ScanAction;
import de.axelwernicke.mypod.actions.SetIPodSyncAction;
import de.axelwernicke.mypod.actions.SynchronizeIPodAction;
import de.axelwernicke.mypod.actions.UpdateAutoPlaylistAction;
import de.axelwernicke.mypod.actions.WipeIPodAction;
import de.axelwernicke.mypod.gui.Frontend;
import de.axelwernicke.mypod.gui.GuiUtils;
import de.axelwernicke.mypod.gui.ListView;
import de.axelwernicke.mypod.gui.SplashScreen;


/** This is the main class of myPod.
 * It contains all the code for starting and shutting down myPod,
 * as well as the gui logic (actions etc.).
 * It's kind of glue between application logic and gui.
 *
 * @author axel wernicke
 */
public class myPod {
    /** jdk1.4 logger */
    private static Logger logger;

    /** current myPod version */
    public static final String myPodVersion = "0.2.3";

    /** current directory is mypods base diectory */
    public static final String myPodBaseDir = System.getProperty("user.dir");

    /** has accelerator, small icon and text
            // TODO: we should clean them up, but they are contained in the frontend.jave...
     */
    public static final int MENU_ACTION = 1;
    public static final int POPUP_ACTION = 2;
    public static final int TOOLBAR_ACTION = 3;

    /** main window */
    private static Frontend frontend;

    /** application logic */
    private static Backend backend;

    /** myPods splash screen */
    private static SplashScreen splashScreen;

    /** global instance of the about action*/
    private static AboutAction aboutAction = null;

    /** global instance of the clip properties action*/
    private static ClipPropertiesAction clipPropertiesAction = null;

    /** global instance of the delete clips action*/
    private static DeleteClipsAction deleteClipsAction = null;

    /** global instance of the delete playlist action*/
    private static DeletePlaylistAction deletePlaylistAction = null;

    /** global instance of the export playlist action*/
    private static ExportPlaylistAction exportPlaylistAction = null;

    /** global instance of the exit action*/
    private static ExitAction exitAction = null;

    /** global instance of the help action*/
    private static HelpAction helpAction = null;

    /** global instance of the import playlist action*/
    private static ImportPlaylistAction importPlaylistAction = null;

    /** global instance of the new autoplaylist action*/
    private static NewAutoPlaylistAction newAutoPlaylistAction = null;

    /** global instance of the new playlist action*/
    private static NewPlaylistAction newPlaylistAction = null;

    /** global instance of the play clips add action*/
    private static PlayClipsAddAction playClipsAddAction = null;

    /** global instance of the play clips append action*/
    private static PlayClipsAppendAction playClipsAppendAction = null;

    /** global instance of the play playlist add action*/
    private static PlayPlaylistAddAction playPlaylistAddAction = null;

    /** global instance of the play playlist append action*/
    private static PlayPlaylistAppendAction playPlaylistAppendAction = null;

    /** global instance of the playlist properties action*/
    private static PlaylistPropertyAction playlistPropertyAction = null;

    /** global instance of the preference action*/
    private static PreferencesAction preferencesAction = null;

    /** global instance of the relocate missing clips action*/
    private static RelocateMissingClipsAction relocateMissingClipsAction = null;

    /** global instance of the remove clips (from) mypod action*/
    private static RemoveClipsMyPodAction removeClipsMyPodAction = null;

    /** global instance of the remove clips (from) playlist action*/
    private static RemoveClipsPlaylistAction removeClipsPlaylistAction = null;

    /** global instance of the remove missing clips action*/
    private static RemoveMissingClipsAction removeMissingClipsAction = null;

    /** global instance of the rename playlist action*/
    private static RenamePlaylistAction renamePlaylistAction = null;

    /** global instance of the reorganize clips action*/
    private static ReorganizeClipsAction reorganizeClipsAction = null;

    /** global instance of the scan action*/
    private static ScanAction scanAction = null;

    /** global instance of the set ipod sync action*/
    private static SetIPodSyncAction setIPodSyncAction = null;

    /** global instance of the synchronize ipod action*/
    private static SynchronizeIPodAction synchronizeIPodAction = null;

    /** global instance of the update autoplaylist action*/
    private static UpdateAutoPlaylistAction updateAutoPlaylistAction = null;

    /** global instance of the wipe ipod action*/
    private static WipeIPodAction wipeIPodAction = null;

    // ------------------------------------ STARTUP & SHUTDOWN -------------------------------------------------------

    /** myPods main class - never call this method directly.
     *        <BR>- show splash screen
     *        <BR>- check jvm version
     *        <BR>-        create backend object
     *        <BR>- create frontend
     *        <BR>-        update all auto playlists
     *        <BR>-        hide splas screen
     *        <BR>- show frontend ( main window )
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // initialize logger
            LogManager.getLogManager().readConfiguration(new FileInputStream("log/logging.properties"));
            logger = Logger.getLogger("de.axelwernicke.mypod");

            // show splash screen
            SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        splashScreen = new SplashScreen();
                        splashScreen.setVisible(true);
                    }
                });

            if (!GuiUtils.checkVMVersion(splashScreen)) {
                System.exit(-1);
            } else {
                // create backend
                splashScreen.splashLabel.setText(GuiUtils.getStringLocalized("initializingBackend..."));
                backend = new Backend();

                // set look and feel
                GuiUtils.setLookAndFeel(getBackend().getPreferences().getLookAndFeelName(), null);

                // create and initialize frontend
                splashScreen.splashLabel.setText(GuiUtils.getStringLocalized("initializingFrontend..."));
                frontend = new Frontend();

                // update all autoplaylists
                splashScreen.splashLabel.setText(GuiUtils.getStringLocalized("updatingAutoplaylists..."));

                // set frontend & divider positions
                splashScreen.splashLabel.setText(GuiUtils.getStringLocalized("preparingMainWindow..."));
                frontend.setPreferences(getBackend().getPreferences());

                // initialize clips table view
                updateClipsTableModel(getSelectedPlaylist());
                GuiUtils.updateStatusLine(frontend);

                // hide splashscreen
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            splashScreen.setVisible(false);
                            splashScreen.dispose();
                            splashScreen = null;
                            System.gc();
                        }
                    });

                // show main window
                frontend.setVisible(true);
            }
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------------------------ GETTER & SETTER -----------------------------------------------------------

    /** Gets a playlist by its index.
     *
     * @param index of the playlist
     * @return playlist at position 'index'
     */
    public static Playlist getPlaylist(int index) {
        return backend.getPlaylist(index);
    }

    /** Gets the index of the currently selected playlist from the list of playlists
     *
     * @return the index of the selected playlist in the playlist list model
     */
    public static int getSelectedPlaylistIndex() {
        return frontend.getPlaylistListView().getSelectedIndex();
    }

    /** Gets the selected playlist from the list of playlists
     * @return the playlist item selected in the playlist list view
     */
    public static Playlist getSelectedPlaylist() {
        return getPlaylist(getSelectedPlaylistIndex());
    }

    /** Gets the oid of a clip.
     *
     * @param filename of the clip
     * @return oid for the clip
     */
    public static Long getOid(String filename) {
        return backend.getDataPool().getOid(filename);
    }

    /** Gets the myPod Backend.
     *
     * @return myPod backend
     */
    public static Backend getBackend() {
        return backend;
    }

    /** Gets the myPod Frontend.
     * @return myPod frontend
     */
    public static Frontend getFrontend() {
        return frontend;
    }

    /** Gets the myPod data pool.
     *
     * @return data pool
     */
    public static DataPool getDataPool() {
        return backend.getDataPool();
    }

    // ------------------------------------ VIEW & MODEL HANDLING -----------------------------------------------------

    /** Sets a column listener to a JTable object.
     * This method is used to add enable sorting to a table.
     *
     * @param clipsTableView to set the column listener for
     */
    public static void setClipsTableColumnListener(final ListView clipsTableView) {
        MouseAdapter listMouseListener = new MouseAdapter() {
            /** Handles mouse clicks to the table header in a list view.
             *        -> Changes the sorting of the table
             **/
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // change mousepointer to wait cursor
                Cursor oldCursor = e.getComponent().getCursor();
                e.getComponent().setCursor(new Cursor(Cursor.WAIT_CURSOR));

                // determine the column the user clicked into
                final javax.swing.table.TableColumnModel columnModel = clipsTableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = clipsTableView.convertColumnIndexToModel(viewColumn);

                if ((e.getClickCount() == 1) && (column != -1)) {
                    ClipsTableModel clipsTableModel = (ClipsTableModel) clipsTableView.getModel();
                    ClipsTableColumnModel clipsTableColumnModel = (ClipsTableColumnModel) clipsTableView.getColumnModel();

                    // sort rows by column
                    logger.fine("Sorting ...");
                    int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                    boolean ascending = (shiftPressed == 0);
                    clipsTableModel.sortByColumn(column, ascending);

                    // fire event to show (re-)sorted columns
                    // seems we loose our ColumnModel changes here, so store and restore column prefs
                    clipsTableColumnModel.storeColumnPreferences();
                    clipsTableModel.fireTableStructureChanged();
                    clipsTableColumnModel.restoreColumnPreferences();
                }

                // change mousepointer back
                e.getComponent().setCursor(oldCursor);
            }
        };

        javax.swing.table.JTableHeader th = clipsTableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    /** gets the list model of all playlists.
     *
     * @return list of playlists
     */
    public static DefaultListModel getPlaylistListModel() {
        return backend.getPlaylistList();
    }

    /** Gets the spreader list model initialized for the current view
     *
     * @param columnCode of the column to get a  model for
     * @return spreader list model for the selected column
     */
    public static DefaultListModel getSpreaderListModel(int columnCode) {
        // get and check index of the current playlist
        int playlistIndex = getSelectedPlaylistIndex();
        if (playlistIndex < 0) {
            playlistIndex = 0;
        }

        // determine spreader list model
        return backend.getSpreaderListModel(columnCode, playlistIndex);
    }

    /** Gets the spreader list model initialized for the current view
     *
     * @param columnCode of the column to create a model for
     * @param playlistIndex to determine the content of the model from
     * @return the initialized spreader list model
     */
    public static DefaultListModel getSpreaderListModel(int columnCode, int playlistIndex) {
        // get and check index of the current playlist
        if (playlistIndex < 0) {
            playlistIndex = 0;
        }

        // determine spreader list model
        return backend.getSpreaderListModel(columnCode, playlistIndex);
    }

    /** Gets a List containing the menu items for playlists to add clips to.
     *
     * @return List of menu items
     */
    public static List<Component> getPlaylistMenuItems() {
        List<Component> items = new ArrayList<Component>(20);

        // create "new Playlist" menu item
        JMenuItem item = new JMenuItem();
        item.setFont(new Font("Dialog", 1, 10));
        item.setAction(myPod.getAddClipsToPlaylistAction(GuiUtils.getStringLocalized("newPlaylistName")));
        item.setActionCommand(String.valueOf(-1));
        items.add(item);

        // add separator
        items.add(new javax.swing.JSeparator());

        // create a menu item for each existing playlist, except the selected one
        DefaultListModel playlists = getPlaylistListModel();
        Playlist playlist;
        int playlistSize = playlists.size();
        for (int i = 0; i < playlistSize; i++) {
            playlist = (Playlist) playlists.get(i);
            item = new JMenuItem();
            item.setFont(new Font("Dialog", 1, 10));
            item.setAction(myPod.getAddClipsToPlaylistAction(playlist.getName()));
            item.setActionCommand(String.valueOf(i));
            items.add(item);
        }

        return items;
    }

    /** Updates the clips list views model using the selected playlist
     *
     * @param playlist to update the table model with
     */
    public static void updateClipsTableModel(Playlist playlist) {
        if ((frontend != null) && (frontend.getClipsTableView() != null)) {
            ClipsTableColumnModel colModel = (ClipsTableColumnModel) frontend.getClipsTableView().getColumnModel();
            ClipsTableModel tableModel = (ClipsTableModel) frontend.getClipsTableView().getModel();

            // we'about to change the model, so lets save the sorting of the table columns
            List<Integer> sortingColumns = new ArrayList<Integer>(101);
            if (colModel != null) {
                colModel.storeColumnPreferences();
            }
            if (tableModel != null) {
                sortingColumns.addAll(tableModel.getSortingColumns());
            }

            // get model for playlist to show
            tableModel = backend.getClipsListTableModel(playlist);
            frontend.getClipsTableView().setModel(tableModel);

            // restore column preferences
            if (colModel != null) {
                colModel.restoreColumnPreferences();
            }

            // validate model
            tableModel.validate();

            // restore columns sorting
            tableModel.setSortingColumns(sortingColumns);

            // if enabled, optimize column width
            if (getBackend().getPreferences().isOptimizeColumnWidth()) {
                frontend.getClipsTableView().optimizeColumnWidth(getBackend().getPreferences().isHideEmptyColumns());
                frontend.getClipsTableView().hideSpreadViewColumn(frontend.getCurrentViewName(), getBackend().getPreferences());
            }
        }
    }

    /**Updates the models for clips table and spreader list
     */
    public static void updateModels() {
        // update clips table
        ClipsTableModel clipsModel = (ClipsTableModel) frontend.getClipsTableView().getModel();
        clipsModel.tableChanged(new TableModelEvent(clipsModel));

        // update spreader list
        // TODO: axel.wernicke@gmx.de still have to determine how ...
    }

    /*
     * ----------------------------------------- GENERAL Actions & Action Handler ----------------------------
     */

    /** Get the scan action.
     * @return a scan action
     * @param mode the action is for
     */
    public static AbstractAction getScanAction(int mode) {
        if (scanAction == null) {
            scanAction = new ScanAction(mode);
        }

        return scanAction;
    }

    /** Gets an Preferences Action.
     * @return an Preferences Action
     * @param mode the action is for
     */
    public static AbstractAction getPreferencesAction(int mode) {
        if (preferencesAction == null) {
            preferencesAction = new PreferencesAction(mode);
        }
        return preferencesAction;
    }

    public static AbstractAction getExitAction() {
        if (exitAction == null) {
            exitAction = new ExitAction();
        }
        return exitAction;
    }

    /*
     * ----------------------------------------- PLAYLIST Actions & Action Handler -----------------------------------
     */

    /** gets an add playlist action
     * @return an add playlist action
     * @param mode gui object the action is for
     */
    public static AbstractAction getNewPlaylistAction(int mode) {
        if (newPlaylistAction == null) {
            newPlaylistAction = new NewPlaylistAction(mode);
        }

        return newPlaylistAction;
    }

    /** gets an add autoplaylist action
     * @return an add autoplaylist action
     * @param mode defines the kind of gui element the action is instatiated for
     */
    public static AbstractAction getNewAutoPlaylistAction(int mode) {
        if (newAutoPlaylistAction == null) {
            newAutoPlaylistAction = new NewAutoPlaylistAction(mode);
        }

        return newAutoPlaylistAction;
    }

    /** gets an update autoplaylist action
     * @return an update autoplaylist action
     * @param mode defines the kind of gui element the action is instatiated for
     */
    public static AbstractAction getUpdateAutoPlaylistAction(int mode) {
        if (updateAutoPlaylistAction == null) {
            updateAutoPlaylistAction = new UpdateAutoPlaylistAction(mode);
        }

        return updateAutoPlaylistAction;
    }

    /** gets an delete playlist action
     * @return an delete playlist action
     * @param mode defines the kind of gui element the action is instatiated for
     */
    public static AbstractAction getDeletePlaylistAction(int mode) {
        if (deletePlaylistAction == null) {
            deletePlaylistAction = new DeletePlaylistAction(mode);
        }

        return deletePlaylistAction;
    }

    /** gets an delete playlist action.
     * @return an delete playlist action
     * @param mode defines the kind of gui element the action is instatiated for
     */
    public static AbstractAction getRenamePlaylistAction(int mode) {
        if (renamePlaylistAction == null) {
            renamePlaylistAction = new RenamePlaylistAction(mode);
        }

        return renamePlaylistAction;
    }

    /** gets an delete playlist action
     * @return an delete playlist action
     * @param mode defines the kind of gui element the action is instatiated for
     */
    public static AbstractAction getPlaylistPropertyAction(int mode) {
        if (playlistPropertyAction == null) {
            playlistPropertyAction = new PlaylistPropertyAction(mode);
        }

        return playlistPropertyAction;
    }

    /** gets an  Add Play External Action
     * @return an Play External Action
     * @param mode defines the kind of gui element the action is instatiated for
     */
    public static AbstractAction getPlayClipsAddAction(int mode) {
        if (playClipsAddAction == null) {
            playClipsAddAction = new PlayClipsAddAction(mode);
        }

        return playClipsAddAction;
    }

    /** gets an  Add Play External Action
     *
     * @return an Play External Action
     * @param mode defines the kind of gui element the action is instatiated for
     */
    public static AbstractAction getPlayPlaylistAddAction(int mode) {
        if (playPlaylistAddAction == null) {
            playPlaylistAddAction = new PlayPlaylistAddAction(mode);
        }

        return playPlaylistAddAction;
    }

    /** gets an  Add Play External Action
     * @return an Play External Action
     * @param mode defines the kind of gui element the action is instatiated for
     */
    public static AbstractAction getPlayClipsAppendAction(int mode) {
        if (playClipsAppendAction == null) {
            playClipsAppendAction = new PlayClipsAppendAction(mode);
        }

        return playClipsAppendAction;
    }

    /** gets an  Add Play External Action
     *
     * @return an Play External Action
     * @param mode defines the kind of gui element the action is instatiated for
     */
    public static AbstractAction getPlayPlaylistAppendAction(int mode) {
        if (playPlaylistAppendAction == null) {
            playPlaylistAppendAction = new PlayPlaylistAppendAction(mode);
        }

        return playPlaylistAppendAction;
    }

    /** gets an  Export Playlist Action.
     *
     * @return an Export Playlist Action
     * @param mode defines the kind of gui element the action is instatiated for
     */
    public static AbstractAction getImportPlaylistAction(int mode) {
        if (importPlaylistAction == null) {
            importPlaylistAction = new ImportPlaylistAction(mode);
        }

        return importPlaylistAction;
    }

    /** gets an  Export Playlist Action
     * @return an Export Playlist Action
     * @param mode gui object the action is for
     */
    public static AbstractAction getExportPlaylistAction(int mode) {
        if (exportPlaylistAction == null) {
            exportPlaylistAction = new ExportPlaylistAction(mode);
        }

        return exportPlaylistAction;
    }

    /** gets an  Export Playlist Action
     *
     * @return an Export Playlist Action
     * @param mode gui object the action is for
     */
    public static AbstractAction getSetIPodSyncAction(int mode) {
        if (setIPodSyncAction == null) {
            setIPodSyncAction = new SetIPodSyncAction(mode);
        }

        return setIPodSyncAction;
    }

    /*
     * ----------------------------------------- CLIPS Actions & Action Handler -----------------------------------
     */

    /** Gets an Remove Clips Action.
     * @return an Remove Clips Action
     * @param mode that describes action properties
     */
    public static AbstractAction getRemoveClipsPlaylistAction(int mode) {
        if (removeClipsPlaylistAction == null) {
            removeClipsPlaylistAction = new RemoveClipsPlaylistAction(mode);
        }

        return removeClipsPlaylistAction;
    }

    /** Gets an  Delete Clips Action.
     * @return an Delete Clips Action
     * @param mode that describes action properties
     */
    public static AbstractAction getRemoveClipsMyPodAction(int mode) {
        if (removeClipsMyPodAction == null) {
            removeClipsMyPodAction = new RemoveClipsMyPodAction(mode);
        }

        return removeClipsMyPodAction;
    }

    /** Gets an  Delete Clips Action.
     * @return an Delete Clips Action
     * @param mode that describes action properties
     */
    public static AbstractAction getDeleteClipsAction(int mode) {
        if (deleteClipsAction == null) {
            deleteClipsAction = new DeleteClipsAction(mode);
        }

        return deleteClipsAction;
    }

    /** Gets an AddClipsToPlaylistAction.
     * @return an Remove Clips Action
     * @param name  of the action
     * @param mode that describes action properties
     */
    public static AbstractAction getAddClipsToPlaylistAction(String name) {
        // since they have different names, this action is _not_ singleton
        return new AddClipsToPlaylistAction(name);
    }

    /** Gets an ClipPropertiesAction.
     * @return an ClipPropertiesAction
     * @param mode that describes action properties
     */
    public static AbstractAction getClipPropertiesAction(int mode) {
        if (clipPropertiesAction == null) {
            clipPropertiesAction = new ClipPropertiesAction(mode);
        }

        return clipPropertiesAction;
    }

    /*
     * ----------------------------------------- IPOD Actions & Action Handler -----------------------------------
     */

    /** Gets an  SynchronizeIPodAction.
     *
     * @return an SynchronizeIPodAction
     * @param mode describes action properties
     */
    public static AbstractAction getSynchronizeIPodAction(int mode) {
        if (synchronizeIPodAction == null) {
            synchronizeIPodAction = new SynchronizeIPodAction(mode);
        }

        return synchronizeIPodAction;
    }

    /** Gets an  WipeIPodAction.
     *
     * @return an WipeIPodAction
     * @param mode describes action properties
     */
    public static AbstractAction getWipeIPodAction(int mode) {
        if (wipeIPodAction == null) {
            wipeIPodAction = new WipeIPodAction(mode);
        }

        return wipeIPodAction;
    }

    /*
     * ----------------------------------------- TOOLS Actions & Action Handler -----------------------------------
     */

    /** Gets an  RemoveMissingClipsAction.
     *
     * @return an RemoveMissingClipsAction
     * @param mode that describes action properties
     */
    public static AbstractAction getRemoveMissingClipsAction(int mode) {
        if (removeMissingClipsAction == null) {
            removeMissingClipsAction = new RemoveMissingClipsAction(mode);
        }

        return removeMissingClipsAction;
    }

    /** Gets an  RemoveMissingClipsAction.
     *
     * @return an RemoveMissingClipsAction
     * @param mode that describes action properties
     */
    public static AbstractAction getRelocateMissingClipsAction(int mode) {
        if (relocateMissingClipsAction == null) {
            relocateMissingClipsAction = new RelocateMissingClipsAction(mode);
        }

        return relocateMissingClipsAction;
    }

    /** Gets an  ReorganizeClipsAction.
     *
     * @return an ReorganizeClipsAction
     * @param mode that describes action properties
     */
    public static AbstractAction getReorganizeClipsAction(int mode) {
        if (reorganizeClipsAction == null) {
            reorganizeClipsAction = new ReorganizeClipsAction(mode);
        }

        return reorganizeClipsAction;
    }

    /*
     * ----------------------------------------- HELP Actions & Action Handler -----------------------------------
     */

    /** Gets an  ReorganizeClipsAction.
     * @return an ReorganizeClipsAction
     * @param mode that describes action properties
     */
    public static AbstractAction getAboutAction(int mode) {
        if (aboutAction == null) {
            aboutAction = new AboutAction(mode);
        }

        return aboutAction;
    }

    /** Gets an  ReorganizeClipsAction.
     * @return an ReorganizeClipsAction
     * @param mode that describes action properties
     */
    public static AbstractAction getHelpAction(int mode) {
        if (helpAction == null) {
            helpAction = new HelpAction(mode);
        }

        return helpAction;
    }
} // MyPod
