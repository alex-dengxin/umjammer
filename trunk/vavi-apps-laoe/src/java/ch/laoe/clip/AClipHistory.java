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

package ch.laoe.clip;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.ImageIcon;

import ch.laoe.ui.Debug;
import ch.laoe.ui.GPersistance;
import ch.laoe.ui.GProgressViewer;
import ch.laoe.ui.GToolkit;


/**
 * history of old states of this clip.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 31.08.00 erster Entwurf oli4 <br>
 *          07.11.00 attach to AClip oli4 <br>
 *          20.07.01 unlimited file-based history oli4 <br>
 *          08.12.01 file-modified handling (title and star) oli4
 */
public class AClipHistory {
    /**
     * constructor
     */
    public AClipHistory(AClip c) {
        clip = c;
        history = new ArrayList<HistoryElement>();
        lastSaveIndex = 0;
    }

    /**
     * copy-constructor
     */
    public AClipHistory(AClipHistory ch, AClip c) {
        this(c);
        for (int i = 0; i < ch.history.size(); i++) {
            this.history.add(ch.history.get(i));
        }
    }

    // settings
    private static final boolean historyEnable = GPersistance.createPersistance().getBoolean("history.enable");

    public static boolean isEnabled() {
        return historyEnable;
    }

    private static String historyPath = GToolkit.getLaoeUserHomePath() + "history/";

    private static String historyExtension = ".laoe.tmp";

    private static class HistoryFileFilter implements FileFilter {
        public boolean accept(File file) {
            return file.getName().endsWith(historyExtension);
        }
    }

    // initial erasure...
    static {
        // delete all existing temp-files...
        File garbage[] = new File(historyPath).listFiles(new HistoryFileFilter());
        if (garbage != null) {
            for (int i = 0; i < garbage.length; i++) {
                garbage[i].delete();
            }
        }
    }

    /**
     * returns the memory-size used for all histories
     */
    public static long getMemorySize() {
        long mem = 0;
        File files[] = new File(historyPath).listFiles(new HistoryFileFilter());
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                mem += files[i].length();
            }
        }
        return mem;
    }

    // links
    private AClip clip;

    // stack
    private List<HistoryElement> history;

    // save-pointer
    private int lastSaveIndex;

    private int actualIndex;

    /**
     * returns true if the actual history-index corresponds to the last saved version.
     */
    public boolean hasUnsavedModifications() {
        // System.out.println("lastSaveIndex="+lastSaveIndex+" actualIndex="+actualIndex);
        return (lastSaveIndex != actualIndex);
    }

    /**
     * if a clip is saved, call this method.
     */
    public void onSave() {
        lastSaveIndex = actualIndex;
    }

    // element
    private class HistoryElement {
        private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.GERMAN);

        public HistoryElement(String d) {
            this(null, d);
        }

        public HistoryElement(ImageIcon icon, String d) {
            this.icon = icon;
            time = dateFormat.format(new Date());
            description = d;

            // store clip...
            if (historyEnable) {
                clip.changeId();
                file = new File(historyPath + clip.getId() + historyExtension);
                file.deleteOnExit();
                try {
                    GProgressViewer.entrySubProgress("storeToHistory");
                    Debug.println(3, "history save clip " + clip.getName() + " to file " + file.getName());
                    // save clip...
                    AClipStorage.saveWithoutSamples(clip, file);
                    // save all new channels...
                    for (int i = 0; i < clip.getNumberOfLayers(); i++) {
                        GProgressViewer.setProgress((i + 1) * 100 / clip.getNumberOfLayers());
                        ALayer l = clip.getLayer(i);
                        GProgressViewer.entrySubProgress();
                        for (int j = 0; j < l.getNumberOfChannels(); j++) {
                            GProgressViewer.setProgress((j + 1) * 100 / l.getNumberOfChannels());
                            AChannel ch = l.getChannel(j);
                            File chFile = new File(historyPath + ch.getId() + historyExtension);
                            if (!chFile.exists()) {
                                // System.out.println(" history save "+chFile.getName());
                                chFile.deleteOnExit();
                                AClipStorage.saveSamples(ch.sample, chFile);
                            }
                        }
                        GProgressViewer.exitSubProgress();
                    }
                    GProgressViewer.exitSubProgress();
                } catch (IOException ioe) {
                    Debug.printStackTrace(5, ioe);
                }
            }
        }

        private File file;

        public AClip reloadClip(AClip c) {
            if (historyEnable && (c != null)) {
                try {
                    GProgressViewer.entrySubProgress("reloadFromHistory");
                    // empty current clip...
                    c.removeAll();

                    // load clip...
                    AClipStorage.load(c, file);
                    Debug.println(3, "history load clip" + clip.getName() + " from file " + file.getName());
                    // load all channels...
                    for (int i = 0; i < c.getNumberOfLayers(); i++) {
                        GProgressViewer.setProgress((i + 1) * 100 / c.getNumberOfLayers());
                        ALayer l = c.getLayer(i);
                        GProgressViewer.entrySubProgress();
                        for (int j = 0; j < l.getNumberOfChannels(); j++) {
                            GProgressViewer.setProgress((j + 1) * 100 / l.getNumberOfChannels());
                            AChannel ch = l.getChannel(j);
                            File chFile = new File(historyPath + ch.getId() + historyExtension);
                            if (chFile.exists()) {
                                // System.out.println(" history load "+chFile.getName());
                                ch.sample = AClipStorage.loadSamples(chFile);
                            }
                        }
                        GProgressViewer.exitSubProgress();
                    }
                    GProgressViewer.exitSubProgress();
                } catch (IOException ioe) {
                    Debug.printStackTrace(5, ioe);
                }
            }
            return c;
        }

        public AClip reloadClip() {
            return reloadClip(new AClip(0, 0));
        }

        private String time;

        private String description;

        public String getTime() {
            return time;
        }

        public String getDescription() {
            return description;
        }

        private ImageIcon icon;

        public ImageIcon getIcon() {
            return icon;
        }
    }

    /**
     * call after doing an operation which changes the data of the AClip
     */
    public void store(String description) {
        history.add(new HistoryElement(description));
        actualIndex = history.size() - 1;
    }

    /**
     * call after doing an operation which changes the data of the AClip
     */
    public void store(ImageIcon icon, String description) {
        history.add(new HistoryElement(icon, description));
        actualIndex = history.size() - 1;
    }

    private int limitIndex(int index) {
        // range check...
        if (index >= history.size()) {
            return history.size() - 1;
        } else if (index < 0) {
            return 0;
        }
        return index;
    }

    /**
     * reload an old version of this clip, giving the clip
     */
    public AClip reloadClip(int index, AClip c) {
        int i = limitIndex(index);
        actualIndex = i;
        return history.get(i).reloadClip(c);
    }

    /**
     * undo one step into the given clip
     */
    public AClip undo(AClip c) {
        return reloadClip(--actualIndex, c);
    }

    /**
     * redo one step into the given clip
     */
    public AClip redo(AClip c) {
        return reloadClip(++actualIndex, c);
    }

    /**
     * reload an old version of this clip into a new clip
     */
    public AClip reloadClip(int index) {
        int i = limitIndex(index);
        actualIndex = i;
        return history.get(i).reloadClip();
    }

    /**
     * get the element at index
     */
    public String getDescription(int index) {
        if (index < history.size()) {
            return history.get(index).getDescription();
        }
        return null;
    }

    /**
     * get the element at index
     */
    public String getTime(int index) {
        if (index < history.size()) {
            return history.get(index).getTime();
        }
        return null;
    }

    /**
     * get the element at index
     */
    public ImageIcon getIcon(int index) {
        if (index < history.size()) {
            return history.get(index).getIcon();
        }
        return null;
    }

    /**
     * get the stack size
     */
    public int getStackSize() {
        return history.size();
    }
}
