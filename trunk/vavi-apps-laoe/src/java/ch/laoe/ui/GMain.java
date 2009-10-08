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

package ch.laoe.ui;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;

import ch.laoe.audio.AudioException;
import ch.laoe.clip.AClip;
import ch.laoe.plugin.GPluginHandler;
import ch.oli4.ui.UiCommandLineParser;
import ch.oli4.ui.UiLFTheme;


/**
 * that frame contains the editable clip.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 27.08.00 erster Entwurf oli4
 */
public class GMain {
    /**
     * constructor, passing the commandline parameterlist
     * Syntax = java GMain [-f audiofile1 audiofile2...]
     * audiofile = audiofiles to open on startup
     */
    public GMain(String arg[]) {
        // install...
        if (UiCommandLineParser.containsFlag(arg, "i")) {
            GUserInstaller.getInstance().forceInstallation();
        } else {
            GUserInstaller.getInstance().maybeInstall();
        }

        // uniform locale...
        Locale.setDefault(Locale.US);

        // interpret command parameters
        String audioFiles[] = UiCommandLineParser.getArguments(arg, "f", ',');
        String languageName[] = UiCommandLineParser.getArguments(arg, "l", ',');
        String debugParams[] = UiCommandLineParser.getArguments(arg, "d", ',');

        // help ?
        if (UiCommandLineParser.containsFlag(arg, "h")) {
            System.out.println("LAoE " + version + ", " + date + ", " + copyright);
            System.out.println("syntax: laoe -hvi [-d verboseLevel[,console]] [-l language] [-f [filename[,filename...]]]");
            System.out.println("  -h            print help information, then exit");
            System.out.println("  -v            print version, then exit");
            System.out.println("  -i            force beginning with installation-dialog, even if already installed");
            System.out.println("  verboseLevel  select the verbose level 0..9, the higher the verbose");
            System.out.println("  console       if the word \"console\" follows the verboseLevel, the debug-text is ");
            System.out.println("                printed to the console instead to the trace-file");
            System.out.println("  -d            verbose for debugging");
            System.out.println("  -l            language setting");
            System.out.println("  language      selected language [en | de | fr], default is english");
            System.out.println("  -f            open audiofiles on start");
            System.out.println("  filename      audiofilenames to open");
            System.out.println("LAoE is a graphic-layer based audiofile editor, implemented in java. ");
            System.out.println("homepage: " + eAddress);
            System.out.println("for questions and bugreport: " + eMail);
            System.out.println("I hope you enjoy it !");
            System.out.println("oli4");
            System.exit(0);
        }
        // version ?
        if (UiCommandLineParser.containsFlag(arg, "v")) {
            System.out.println(version);
            System.exit(0);
        }

        // persistance
        persistance = GPersistance.createPersistance(GToolkit.getLaoeUserHomePath() + "laoe.properties");
        persistance.restore();

        // language
        GLanguage.createLanguage("language");

        if (UiCommandLineParser.containsFlag(arg, "l")) {
            GLanguage.setLanguage(languageName[0], "");
        }

        // start LAoE
        System.out.println("LAoE " + version + ", " + date + ", " + copyright);
        System.out.println(license + ", " + GLanguage.translate("<language>Version") + ", " + os + ", " + java);

        // verbose
        Debug.setTimeStampEnabled(true);
        if (UiCommandLineParser.containsFlag(arg, "d")) {
            Debug.setVerboseLevel(Integer.parseInt(debugParams[0]));
            if (debugParams.length == 2) {
                if (!debugParams[1].trim().equals("console")) {
                    Debug.setOutputFile(GToolkit.getLaoeUserHomePath() + "trace.txt");
                }
            } else {
                Debug.setOutputFile(GToolkit.getLaoeUserHomePath() + "trace.txt");
            }
        } else {
            Debug.setVerboseLevel(persistance.getInt("debug.verboseLevel"));
            Debug.setOutputFile(GToolkit.getLaoeUserHomePath() + "trace.txt");
        }

        Debug.println(3, "restore persistent objects");
        Debug.println(3, "load look and feel");

        // look and feel
        UiLFTheme.load(new GLaoeTheme());

        // start window
        startWindow = new GStartWindow();

        // frames
        mainFrame = new GMainFrame(this);
        clipFrames = new ArrayList<GClipFrame>();
        visibleFrames = new ArrayList<JFrame>();

        // plugins
        pluginHandler = new GPluginHandler(this);

        mainFrame.setVisible(true);

        startWindow.setWorkInfo(GLanguage.translate("loadAudioFiles") + "...");

        // load audiofiles ?
        if (audioFiles != null) {
            for (int i = 0; i < audioFiles.length; i++) {
                addClipFrame(new File(audioFiles[i]));
            }
        }

        startWindow.setWorkInfo(GLanguage.translate("ready") + " !");

        // close start window
        startWindow.onEndOfStart(3000);
    }

    // information
    public static final String version = "v0.6.03 beta";

    public static final String date = "27.05.2003";

    public static final String copyright = "(c) 2000-2003 oli4, neuchatel switzerland";

    public static final String eAddress = "www.oli4.ch/laoe";

    public static final String eMail = "laoe@oli4.ch";

    public static final String java = "java " + System.getProperty("java.version");

    public static final String os = System.getProperty("os.name");

    public static final String license = "GNU General Public License";

    // persistance
    private GPersistance persistance;

    // start window
    private GStartWindow startWindow;

    /**
     * returns the start window
     */
    public GStartWindow getStartWindow() {
        return startWindow;
    }

    // plugin handler
    private GPluginHandler pluginHandler;

    /**
     * returns the pluginhandler
     */
    public GPluginHandler getPluginHandler() {
        return pluginHandler;
    }

    // main frame
    private GMainFrame mainFrame;

    /**
     * return the main-frame
     */
    public GMainFrame getMainFrame() {
        return mainFrame;
    }

    // clip frames
    private List<GClipFrame> clipFrames;

    private GClipFrame focussedClipFrame;

    private void createClipFrame(AClip c) {
        Debug.println(3, "create clip frame " + c.getName());
        GClipFrame cf = new GClipFrame(this, c);
        if (getFocussedClipFrame() != null) {
            cf.setLocation(getFocussedClipFrame().getLocation().x + 30, getFocussedClipFrame().getLocation().y + 30);
        }

        clipFrames.add(cf);
        setFocussedClipFrame(cf);
        cf.setVisible(true);
    }

    /**
     * add a new clip-frame
     */
    public void addClipFrame(AClip c) {
        createClipFrame(c);
    }

    /**
     * add a existing clip-frame
     */
    public void addClipFrame(File file) {
        // open a existing clip
        try {
            GProgressViewer.entrySubProgress();
            GProgressViewer.setProgress(90);
            AClip c = new AClip(file);
            GProgressViewer.setProgress(100);
            createClipFrame(c);
            GProgressViewer.exitSubProgress();
        } catch (AudioException ae) {
            GDialog.showErrorDialog(null, "audioError", ae.getMessage());
        }
    }

    /**
     * remove a clip-frame
     */
    public void removeClipFrame(GClipFrame cf) {
        Debug.println(3, "remove clip frame " + cf.getClipEditor().getClip().getName());
        clipFrames.remove(cf);
        setFocussedClipFrame(null);
        System.gc();
    }

    /**
     * returns all clip-frames
     */
    public Object[] getAllClipFrames() {
        return clipFrames.toArray();
    }

    /**
     * set focussed clip-frame
     */
    public void setFocussedClipFrame(GClipFrame f) {
        focussedClipFrame = f;
        if (f != null)
            pluginHandler.reloadAllPluginsAndFocussedClip();
    }

    /**
     * get focussed clip-frame
     */
    public GClipFrame getFocussedClipFrame() {
        if (focussedClipFrame != null)
            return focussedClipFrame;
        else
            return null;
    }

    /**
     * get focussed clip-editor
     */
    public GClipEditor getFocussedClipEditor() {
        if (focussedClipFrame != null)
            return focussedClipFrame.getClipEditor();
        else
            return null;
    }

    // iconify manager

    private List<JFrame> visibleFrames;

    /**
     * iconifies all visible frames
     */
    public void iconifyAllFrames() {
        visibleFrames.clear();

        // clip frames...
        for (int i = 0; i < clipFrames.size(); i++) {
            JFrame f = clipFrames.get(i);
            if (f.getState() == Frame.NORMAL) {
                visibleFrames.add(f);
                f.setState(Frame.ICONIFIED);
            }
        }

        // plugin frames...
        getPluginHandler().iconifyAllFrames();
    }

    /**
     * deiconifies all previously visible frames
     */
    public void deiconifyAllFrames() {
        // clip frames...
        for (int i = 0; i < visibleFrames.size(); i++) {
            JFrame f = visibleFrames.get(i);
            f.setState(Frame.NORMAL);
        }

        // plugin frames...
        getPluginHandler().deiconifyAllFrames();
    }

    // main program

    /**
     * LAoE application entry point
     */
    public static void main(String arg[]) {
        GMain g = new GMain(arg);
Debug.println(5, "g: " + g);
    }
}
