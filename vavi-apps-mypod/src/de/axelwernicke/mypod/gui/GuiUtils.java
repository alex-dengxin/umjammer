// GuiUtils
// $Id: GuiUtils.java,v 1.35 2003/07/20 06:46:17 axelwernicke Exp $
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

import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

import de.axelwernicke.mypod.AutoPlaylist;
import de.axelwernicke.mypod.Backend;
import de.axelwernicke.mypod.MP3Meta;
import de.axelwernicke.mypod.Playlist;
import de.axelwernicke.mypod.Preferences;
import de.axelwernicke.mypod.myPod;


/** This class provides some helpers to remove code from the core GUI classes.
 * @author axel wernicke
 */
public class GuiUtils {
    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod.gui");

    /** path to the user manual */
    private static final String userManualPath = "doc/user manual/html/index.html";

    /** Base directory where the theme packs for the skin look and feel can be found */
    private static final String SKIN_LF_THEME_DIR = "lib" + File.separator + "skinlfthemes";

    /** instance of this (singleton) class */
    private static GuiUtils INSTANCE = null;

    /** static number format instance. Used to format filesizes. */
    private static NumberFormat numberFormat = NumberFormat.getInstance();

    /** resource bundle, used for all localizations */
    private static ResourceBundle localizeBundle = ResourceBundle.getBundle("resource/language", java.util.Locale.getDefault());

    /** Creates a new instance of GuiUtils.
     *        <BR>Since all methods in here are static you should never be in need to call it...
     */
    private GuiUtils() {
    }

    /** This is a singleton class, so use getInstance to get in instance :)
     *
     * @return GuiUtils instance
     */
    public GuiUtils getInstance() {
        return (INSTANCE != null) ? INSTANCE : new GuiUtils();
    }

    /** Checks if the current java vm version is > 1.4
     *
     * @return true, if version is > 1.4
     * @param component to show the message dialog at
     */
    public static boolean checkVMVersion(java.awt.Component component) {
        boolean appropriate = true;

        // check that we run with a jvm >= 1.4
        String jvmVersion = System.getProperty("java.version");
        int majorVersion = Integer.parseInt(jvmVersion.substring(0, 1));
        int minorVersion = Integer.parseInt(jvmVersion.substring(2, 3));
        if ((majorVersion < 2) && (minorVersion < 4)) {
            // jvm version not appropriate - warn user!
            appropriate = false;
            logger.warning("myPod needs a java runtime version 1.4 or higher. You have: " + jvmVersion);
            javax.swing.JOptionPane.showMessageDialog(component, GuiUtils.getStringLocalized("wrongJavaVersionMessage") + jvmVersion, GuiUtils.getStringLocalized("wrongJavaVersionTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
        }

        return appropriate;
    }

    /**
     * Gets the selected clips
     * <li> get current clips table view
     * <li> get oids for selected clips from the view
     * 
     * @param frontend to get the clips table view from
     * @return List, containing the oids of the selected clips
     */
    public static List<Long> getSelectedClips(Frontend frontend) {
        // get list view and table model
        ListView clipsTableView = frontend.getClipsTableView();

        // determine selected clips
        return clipsTableView.getSelectedClips();
    }

    /**        Gets a string from the resource bundle - localized for the current default language
     *
     * @param bundle name of the resource bundle
     * @param key        in the bundle
     * @return value for the key
     */
    public static String getStringLocalized(String bundle, String key) {
        return ResourceBundle.getBundle(bundle, java.util.Locale.getDefault()).getString(key);
    }

    /**        Gets a string from the resource bundle - localized for the current default language
     *
     * @param key        in the bundle
     * @return value for the key
     */
    public static String getStringLocalized(String key) {
        return localizeBundle.getString(key);
    }

    /** Gets the tree model for autoplaylist filter properties.
     *
     * @return tree model to be used in the property dialog
     */
    public static DefaultMutableTreeNode getAutoPlaylistPropertyTreeModel() {
        DefaultMutableTreeNode root = getPlaylistPropertyTreeModel();

        // add the auto playlist nodes
        DefaultMutableTreeNode filter = new DefaultMutableTreeNode(GuiUtils.getStringLocalized("filter"));
        DefaultMutableTreeNode artist = new DefaultMutableTreeNode(GuiUtils.getStringLocalized("Artist"));
        DefaultMutableTreeNode genre = new DefaultMutableTreeNode(GuiUtils.getStringLocalized("Genre"));
        DefaultMutableTreeNode year = new DefaultMutableTreeNode(GuiUtils.getStringLocalized("Year"));

        filter.add(artist);
        filter.add(genre);
        filter.add(year);
        root.add(filter);

        return root;
    }

    /** Gets the tree model for playlist properties.
     *
     * @return tree model to be used in the property dialog
     */
    public static DefaultMutableTreeNode getPlaylistPropertyTreeModel() {
        // create root object
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(GuiUtils.getStringLocalized("playlistProperties"));
        root.add(new DefaultMutableTreeNode(GuiUtils.getStringLocalized("general")));

        return root;
    }

    /** Gets the tree model for myPod preferences.
     *
     * @return tree model to be used in the preferences dialog
     */
    private static DefaultMutableTreeNode getPreferencesTreeModel() {
        // create root object
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(GuiUtils.getStringLocalized("preferences"));
        root.add(new DefaultMutableTreeNode(GuiUtils.getStringLocalized("general")));
        root.add(new DefaultMutableTreeNode(GuiUtils.getStringLocalized("ipod")));
        root.add(new DefaultMutableTreeNode(GuiUtils.getStringLocalized("externalPlayer")));

        return root;
    }

    /** Gets the tree model for playlist properties.
     *
     * @return tree model to be used in the property dialog
     */
    private static DefaultMutableTreeNode getClipPropertiesTreeModel() {
        // create root object
        DefaultMutableTreeNode root;
        root = new DefaultMutableTreeNode(GuiUtils.getStringLocalized("clipProperties"));
        root.add(new DefaultMutableTreeNode(GuiUtils.getStringLocalized("Track")));
        root.add(new DefaultMutableTreeNode(GuiUtils.getStringLocalized("Album")));
        root.add(new DefaultMutableTreeNode(GuiUtils.getStringLocalized("credits")));
        root.add(new DefaultMutableTreeNode(GuiUtils.getStringLocalized("Original")));

        return root;
    }

    /** Creates and shows the playlist property dialog.
     *        <BR>- create property dialog
     *        <BR>- set tree model
     *        <BR>- set data from playlist to the dialog
     *        <BR>- show dialog
     *
     * @param playlist to show a property dialog for
     * @param frontend myPods main window
     * @param backend        myPods application logic
     * @return true, if the dialog was confirmed by the user
     */
    public static boolean showPlaylistPropertyDialog(Playlist playlist, Frontend frontend, Backend backend) {
        PlaylistPropertyDialog propDialog = new PlaylistPropertyDialog(frontend, true);

        // set tree
        DefaultTreeModel tree = playlist.isAutoplaylist() ? new DefaultTreeModel(GuiUtils.getAutoPlaylistPropertyTreeModel()) : new DefaultTreeModel(GuiUtils.getPlaylistPropertyTreeModel());

        propDialog.optionTree.setModel(tree);

        // set models and data
        propDialog.setPlaylistName(playlist.getName());
        propDialog.setIPodSync(playlist.isIPodSync());

        if (playlist.isAutoplaylist()) {
            // get all known artists from data pool
            propDialog.setArtistFilterEnabled(((AutoPlaylist) playlist).isArtistFilterEnabled());
            propDialog.setArtistListModel(myPod.getDataPool().getAllArtistValues(), ((AutoPlaylist) playlist).getArtistFilter());

            // get all known genres from data pool
            propDialog.setGenreFilterEnabled(((AutoPlaylist) playlist).isGenreFilterEnabled());
            propDialog.setGenreListModel(myPod.getDataPool().getAllGenreValues(), ((AutoPlaylist) playlist).getGenreFilter());

            // get all known years from data pool
            propDialog.setYearFilterEnabled(((AutoPlaylist) playlist).isYearFilterEnabled());
            propDialog.setYearListModel(myPod.getDataPool().getAllYearValues(), ((AutoPlaylist) playlist).getYearFilter());
        }

        // do some gui stuff
        propDialog.setBounds(calcCenteredBounds(frontend.getBounds(), propDialog.getBounds()));
        propDialog.optionTree.setSelectionRow(0);

        // open dialog
        propDialog.setVisible(true);

        // check if dialog was canceled
        boolean confirmed = propDialog.isConfirmed();
        if (confirmed == true) {
            // set changed data
            playlist.setName(propDialog.getPlaylistName());
            playlist.setIPodSync(propDialog.isIPodSync());

            if (playlist.isAutoplaylist()) {
                // set filter from selected artists, genres
                ((AutoPlaylist) playlist).setArtistFilterEnabled(propDialog.isArtistFilterEnabled());
                ((AutoPlaylist) playlist).setGenreFilterEnabled(propDialog.isGenreFilterEnabled());
                ((AutoPlaylist) playlist).setYearFilterEnabled(propDialog.isYearFilterEnabled());

                ((AutoPlaylist) playlist).setArtistFilter(propDialog.getSelectedArtists());
                ((AutoPlaylist) playlist).setGenreFilter(propDialog.getSelectedGenres());
                ((AutoPlaylist) playlist).setYearFilter(propDialog.getSelectedYears());
            }
        }

        // destroy dialog
        propDialog.dispose();
        propDialog = null;

        return confirmed;
    }

    /** Prepares and shows preferences dialog and evaulates result.
     *        <BR>- create preferences dialog
     *        <BR>- set tree model
     *        <BR>- set data from prefs to the dialog
     *        <BR>- show dialog
     *        <BR>- copy data from dialog to prefs
     *
     * @return true, if the dialog was confirmed by the user
     * @param prefs for myPod
     * @param frontend of myPod
     */
    public static Preferences showPreferencesDialog(Frontend frontend, Preferences prefs) {
        PreferencesDialog prefsDialog = new PreferencesDialog(frontend, true);

        // set tree
        DefaultTreeModel tree = new DefaultTreeModel(GuiUtils.getPreferencesTreeModel());
        prefsDialog.optionTree.setModel(tree);

        // set models and data
        prefsDialog.setPreferences(prefs);

        // set position and select first item
        prefsDialog.setBounds(calcCenteredBounds(frontend.getBounds(), prefsDialog.getBounds()));
        prefsDialog.optionTree.setSelectionRow(0);

        // open dialog
        prefsDialog.setVisible(true);

        // check if dialog was canceled
        boolean confirmed = prefsDialog.isConfirmed();

        // set changed data
        if (confirmed == true) {
            prefs = prefsDialog.getPrefs();
        }

        // destroy dialog
        prefsDialog.dispose();
        prefsDialog = null;

        return prefs;
    }

    /** Prepares and shows clip properties dialog and evaulates result.
     *
     * @return set of mp3 meta data, changed by the user
     * @param clips clips to change properties for
     * @param frontend used to calculate the dialogs position
     */
    public static MP3Meta showClipPropertiesDialog(Frontend frontend, List<Long> clips) {
        ClipPropertiesDialog clipPropertiesDialog = new ClipPropertiesDialog(frontend, true);

        // set tree
        DefaultTreeModel tree = new DefaultTreeModel(GuiUtils.getClipPropertiesTreeModel());
        clipPropertiesDialog.optionTree.setModel(tree);

        // determine and set data
        clipPropertiesDialog.setValues(myPod.getDataPool(), clips);

        // set position and select first item
        clipPropertiesDialog.setBounds(calcCenteredBounds(frontend.getBounds(), clipPropertiesDialog.getBounds()));
        clipPropertiesDialog.optionTree.setSelectionRow(0);

        // open dialog
        clipPropertiesDialog.setVisible(true);

        // set changed data if dialog was confirmed
        MP3Meta meta = null;
        if (clipPropertiesDialog.isConfirmed()) {
            meta = clipPropertiesDialog.getChangedValues();
        }

        // destroy dialog
        clipPropertiesDialog.setVisible(false);
        clipPropertiesDialog.dispose();

        return meta;
    }

    /** sets the status line
     *
     * @param frontend where the status line is placed
     */
    public static void updateStatusLine(Frontend frontend) {
        // get selected playlist and clips
        Playlist playlist = ((de.axelwernicke.mypod.ClipsTableModel) frontend.getClipsTableView().getModel()).getPlaylist();
        List<Long> oids = getSelectedClips(frontend);
        String textToSet = "";
        if (playlist != null) {
            textToSet = ((oids != null) && !oids.isEmpty()) ? (oids.size() + " of " + playlist.getTotalClips() + " " + GuiUtils.getStringLocalized("clips") + " | " + formatTime(myPod.getBackend().getTotalTime(oids)) + " | " + formatFilesizeMB(myPod.getBackend().getTotalFilesize(oids), 2) + " ") : (playlist.getTotalClips() + " " + GuiUtils.getStringLocalized("clips") + " | " + formatTime(playlist.getTotalTime()) + " | " + formatFilesizeMB(playlist.getTotalFilesize(), 2) + " ");
        }
        frontend.setClipsInfo(textToSet);
    }

    /** calculates bound to show dialog in the center of mainFrame
     *
     * @param mainFrame bounds
     * @param dialog bounds
     * @return  bounds of dialog in the center of mainFrame
     */
    public static Rectangle calcCenteredBounds(Rectangle mainFrame, Rectangle dialog) {
        Rectangle rect;
        rect = new Rectangle((int) ((mainFrame.getX() + (mainFrame.getWidth() / 2)) - (dialog.getWidth() / 2)), (int) ((mainFrame.getY() + (mainFrame.getHeight() / 2)) - (dialog.getHeight() / 2)), (int) dialog.getWidth(), (int) dialog.getHeight());
        return rect;
    }

    /** Formats filesize in bytes as appropriate to Bytes, KB, MB or GB
     *
     * @param filesize in bytes
     * @return formatted filesize
     **/
    public static String formatFilesize(long filesize) {
        String result;

        if (Math.abs(filesize) < 1024) {
            result = "" + filesize + " Bytes";
        } else if (Math.abs(filesize) < 1048576) {
            result = formatFilesizeKB(filesize, 2);
        } else if (Math.abs(filesize) < 1073741824) {
            result = formatFilesizeMB(filesize, 2);
        } else {
            result = formatFilesizeGB(filesize, 2);
        }

        return result;
    }

    /** Formats filesize in bytes to KB
     *
     * @param filesize in bytes
     * @param fractionDigits ...
     * @return formatted filesize
     */
    private static String formatFilesizeKB(long filesize, int fractionDigits) {
        numberFormat.setMaximumFractionDigits(fractionDigits);

        return new StringBuffer(numberFormat.format(filesize / 1024.0)).append(" KB").toString();
    } // format filesize KB

    /** Formats filesize in bytes to KB
     *
     * @param filesize in bytes
     * @return formatted filesize
     */
    public static String formatFilesizeKB(long filesize) {
        return new StringBuffer().append(filesize / 1024).append(" KB").toString();
    } // format filesize KB

    /** Formats filesize in bytes to MB
     *
     * @param filesize in bytes
     * @param fractionDigits ...
     * @return formatted filesize
     */
    private static String formatFilesizeMB(long filesize, int fractionDigits) {
        numberFormat.setMaximumFractionDigits(fractionDigits);

        // 1048576 = 1024.0 * 1024.0
        return new StringBuffer(numberFormat.format(filesize / 1048576.0)).append(" MB").toString();
    } // format filesize MB

    /** Formats filesize in bytes to GB
     *
     * @param filesize in bytes
     * @param fractionDigits ...
     * @return formatted filesize
     */
    private static String formatFilesizeGB(long filesize, int fractionDigits) {
        numberFormat.setMaximumFractionDigits(fractionDigits);

        // 1048576 = 1024.0 * 1024.0
        return new StringBuffer(numberFormat.format(filesize / 1073741824.0)).append(" GB").toString();
    } // format filesize GB

    /** Formats transfer speed in bytes/s to KB/s
     *
     * @return formatted transfer speed
     * @param filesize to format in bytes
     * @param fractionDigits ...
     */
    public static String formatTransferSpeedKB(long filesize, int fractionDigits) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(fractionDigits);

        return new StringBuffer(nf.format(filesize / 1024.0)).append(" KB/s").toString();
    } // format transfer speed KB

    /** Formats transfer speed in bytes/s to MB/s
     *
     * @return formatted transfer speed
     * @param filesize to format in bytes
     * @param fractionDigits ...
     */
    public static String formatTransferSpeedMB(long filesize, int fractionDigits) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(fractionDigits);

        // 1048576 = 1024.0 * 1024.0
        return new StringBuffer(nf.format(filesize / 1048576.0)).append(" MB/s").toString();
    } // format transfer speed MB

    /** Formats time string from seconds.
     *
     * @param time to format in seconds
     * @return time formatted as hh:mm:ss
     */
    public static String formatTime(long time) {
        StringBuffer result = new StringBuffer();

        // determine hours and minutes from seconds
        long hour = time / 3600;
        time -= (hour * 3600);
        long min = time / 60;
        time -= (min * 60);
        long sec = time;

        // format to string
        // append hour
        if (hour > 0) {
            result.append(hour).append(":");
            if (min < 10) {
                result.append("0");
            }
        }

        // append min
        result.append(min).append(":");

        // append sec
        if (sec < 10) {
            result.append("0");
        }
        result.append(sec);

        return result.toString();
    }

    /** Gets all accasseble look and feels.
     *
     * @return hashtable containing name and info string for all available l&f
     */
    public static Map<String, String> getLookAndFeelInfo() {
        Map<String, String> lookAndFeelInfo = new HashMap<String, String>(20);

        try {
            // add system look and feels
            UIManager.LookAndFeelInfo[] lfInfo = UIManager.getInstalledLookAndFeels();
            for (int i = 0; (lfInfo != null) && (i < lfInfo.length); i++) {
                lookAndFeelInfo.put(lfInfo[i].getName(), lfInfo[i].getClassName());
            }

            // add additional look and feels
            lookAndFeelInfo.put("Kunststoff", "com.incors.plaf.kunststoff.KunststoffLookAndFeel");
            lookAndFeelInfo.put("NEXT", "nextlf.plaf.NextLookAndFeel");

            // add all themes for the skin look and feel
            File themeBase = new File(SKIN_LF_THEME_DIR);
            File[] themes = themeBase.listFiles();
            for (int i = 0; (themes != null) && (i < themes.length); i++) {
                lookAndFeelInfo.put("Skin Look and Feel (" + themes[i].getName() + ")", themes[i].getName() + "@com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
            }
        } catch (Exception ex) {
            logger.warning("Eception raised: " + ex.getMessage());
            ex.printStackTrace();
        }

        return lookAndFeelInfo;
    }

    /** Sets the look and feel from an info string as used in the preferences dialog class.
     *
     * @param lookAndFeelInfo describing the look and feel to set
     * @param frame to set the look and feel for
     */
    public static void setLookAndFeel(String lookAndFeelInfo, Frame frame) {
        try {
            // skin look and feel need special handling ...
            if (lookAndFeelInfo.endsWith("@com.l2fprod.gui.plaf.skin.SkinLookAndFeel")) {
                // determine theme package name from look and feel info string
                String themeName = lookAndFeelInfo.substring(0, lookAndFeelInfo.indexOf("@"));

                // set skin
                SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(SKIN_LF_THEME_DIR + File.separator + themeName));

                // set lf class name
                lookAndFeelInfo = "com.l2fprod.gui.plaf.skin.SkinLookAndFeel";
            }

            // set look and feel from class name
            setLookAndFeel((LookAndFeel) Class.forName(lookAndFeelInfo).newInstance(), frame);
        } catch (Exception ex) {
            logger.warning("exception raised: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Sets the look and feel for myPod.
     *
     * @param lfClass the look and feel class to activate
     * @param frame root frame that is reinitialized after changing the l&f
     */
    public static void setLookAndFeel(LookAndFeel lfClass, Frame frame) {
        try {
            UIManager.setLookAndFeel(lfClass);

            if (frame != null) {
                SwingUtilities.updateComponentTreeUI(frame);
            }
        } catch (Exception e) {
            logger.warning("exception raised: " + e.getMessage());
        }
    }

    /** Shows a swing browser for a given URL
     * @param anchor in the help file to show in the browser
     */
    public static void showHelpBrowser(String anchor) {
        try {
            String url = "file:///" + myPod.myPodBaseDir + File.separator + userManualPath + anchor;
            logger.info("Trying to open help file: " + url);
            JFrame browser = new HelpBrowser(url);
            browser.setVisible(true);
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
        }
    }
}
