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

import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import ch.laoe.clip.AClip;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipEditor;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GMain;
import ch.oli4.ui.UiPopupMenu;
import ch.oli4.ui.UiStroke;


/**
 * loads all plugins, and integrates them to the ui.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 27.08.00 erster Entwurf oli4
 */
public class GPluginHandler implements GUiListener, MouseListener, MouseMotionListener {
    /**
     * constructor
     */
    public GPluginHandler(GMain g) {
        main = g;

        pluginCounter = 0;
        externalPluginCounter = 0;

        focussedPlugin = new GPlugin[focussedPluginFifoLength];
        focussedPluginHead = 0;
        pluginHistory = new ArrayList<GPlugin>();

        initStrokes();
        initMenus();
        loadPlugins();
        loadExternalPlugins();

        createMainButtons();
        createMainMenuBar();

        visibleFrames = new ArrayList<GPluginFrame>();

        startVsiblePlugins();
    }

    // links
    private GMain main;

    // start window

    private int pluginCounter;

    private void printLoadPlugin(GPlugin p) {
        main.getStartWindow().setWorkInfo(GLanguage.translate("loadPlugin") + " " + ++pluginCounter + ": " + GLanguage.translate(p.getName()));
    }

    private int externalPluginCounter;

    private void printLoadExternalPlugin(GPlugin p) {
        main.getStartWindow().setWorkInfo(GLanguage.translate("loadExternalPlugin") + " " + ++externalPluginCounter + ": " + GLanguage.translate(p.getName()));
    }

    // plugin history
    private List<GPlugin> pluginHistory;

    public void addToPluginHistory(GPlugin p) {
        // add or move to top...
        if (!(p instanceof GPPluginHistory)) {
//          int i;
            while (pluginHistory.indexOf(p) >= 0) {
                pluginHistory.remove(pluginHistory.indexOf(p));
            }
            pluginHistory.add(0, p);
        }

        // cut if too large...
        while (pluginHistory.size() > 15) {
            pluginHistory.remove(15);
        }
    }

    public GPlugin getPluginHistoryEntry(int index) {
        if (index >= 0) {
            return pluginHistory.get(index);
        } else {
            return null;
        }
    }

    public int getPluginHistoryLength() {
        return pluginHistory.size();
    }

    // plugins
    private List<GPlugin> pluginList;

    // some public usable plugins...
    private GPlugin measure;

    public GPlugin fileSave;

    public GPlugin fileSaveAs;

    // focussed plugin fifo
    private static final int focussedPluginFifoLength = 2;

    private GPlugin focussedPlugin[];

    private int focussedPluginHead;

    void setFocussedPlugin(GPlugin fp) {
        // different from old plugin ?
        if (getFocussedPlugin(0) != fp) {
            focussedPlugin[focussedPluginHead] = fp;
            focussedPluginHead = (focussedPluginHead + 1) % focussedPluginFifoLength;
        }
    }

    /**
     * returns the focussed plugin. newest is index 0, the higher the index, the older the plugin
     */
    public GPlugin getFocussedPlugin(int index) {
        return focussedPlugin[(focussedPluginHead + focussedPluginFifoLength - 1 - index) % focussedPluginFifoLength];
    }

    /**
     * returns all loaded plugins including external plugins.
     * 
     * @return
     */
    public Object[] getAllPlugins() {
        return pluginList.toArray();
    }

    private void loadPlugins() {
        pluginList = new ArrayList<GPlugin>();
        GPlugin p;

        // filemenu
        try {
            p = new GPFileNew(this);
            pluginList.add(p);
            fileMenuItem.add(p);
            // mainButton.add(p);
            strokes.add(p, UiStroke.N_RIGHT);
            printLoadPlugin(p);

            p = new GPFileOpen(this);
            pluginList.add(p);
            fileMenuItem.add(p);
            mainButton.add(p);
            printLoadPlugin(p);

            p = new GPFileSaveAs(this);
            fileSaveAs = p;
            pluginList.add(p);
            fileMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPFileSave(this);
            fileSave = p;
            pluginList.add(p);
            fileMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.S_DOWN);
            printLoadPlugin(p);

            p = new GPFileClose(this);
            pluginList.add(p);
            fileMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPFileExit(this);
            pluginList.add(p);
            fileMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPImageSaveAs(this);
            pluginList.add(p);
            fileMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPOptions(this);
            pluginList.add(p);
            fileMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            // clip menu

            p = new GPPlayLoopRec(this);
            pluginList.add(p);
            clipMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.L_DOWN);
            printLoadPlugin(p);

            p = new GPClipProperties(this);
            pluginList.add(p);
            clipMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            // edit menu

            p = new GPUndo(this);
            pluginList.add(p);
            editMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPRedo(this);
            pluginList.add(p);
            editMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPUndoStack(this);
            pluginList.add(p);
            editMenuItem.add(p);
            // mainButton.add(p);
            strokes.add(p, UiStroke.U_RIGHT);
            printLoadPlugin(p);

            p = new GPFind(this);
            pluginList.add(p);
            editMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPCopy(this);
            pluginList.add(p);
            editMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.C_DOWN);
            printLoadPlugin(p);

            p = new GPPasteInsert(this);
            pluginList.add(p);
            editMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.P_UP);
            printLoadPlugin(p);

            p = new GPPasteReplace(this);
            pluginList.add(p);
            editMenuItem.add(p);
            // mainButton.add(p);
            // strokes.add(p, UiStroke.P_UP);
            printLoadPlugin(p);

            p = new GPPasteIntoNew(this);
            pluginList.add(p);
            editMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPCut(this);
            pluginList.add(p);
            editMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.C_UP);
            printLoadPlugin(p);

            p = new GPMove(this);
            pluginList.add(p);
            editMenuItem.add(p);
            mainButton.add(p);
            printLoadPlugin(p);

            p = new GPDuplicate(this);
            pluginList.add(p);
            editMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.D_DOWN);
            printLoadPlugin(p);

            p = new GPSplitToNew(this);
            pluginList.add(p);
            editMenuItem.add(p);
            printLoadPlugin(p);

            p = new GPCrop(this);
            pluginList.add(p);
            editMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPSampleEditor(this);
            pluginList.add(p);
            editMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPInsertSilence(this);
            pluginList.add(p);
            editMenuItem.add(p);
            mainButton.add(p);
            printLoadPlugin(p);

            p = new GPGraphicDocumentation(this);
            pluginList.add(p);
            editMenuItem.add(p);
            printLoadPlugin(p);

            // select menu

            p = new GPSelect(this);
            pluginList.add(p);
            selectMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.I_RIGHT);
            printLoadPlugin(p);

            p = new GPSelectLayer(this);
            pluginList.add(p);
            selectMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPSelectNumeric(this);
            pluginList.add(p);
            selectMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPSelectBefore(this);
            pluginList.add(p);
            selectMenuItem.add(p);
            printLoadPlugin(p);

            p = new GPSelectAfter(this);
            pluginList.add(p);
            selectMenuItem.add(p);
            printLoadPlugin(p);

            p = new GPUnselectLayer(this);
            pluginList.add(p);
            selectMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.I_LEFT);
            printLoadPlugin(p);

            p = new GPSelectExpander(this);
            pluginList.add(p);
            selectMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPCopySelection(this);
            pluginList.add(p);
            selectMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPPasteSelection(this);
            pluginList.add(p);
            selectMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            // mask menu

            p = new GPMask(this);
            pluginList.add(p);
            maskMenuItem.add(p);
            mainButton.add(p);
            printLoadPlugin(p);

            p = new GPUnmaskLayer(this);
            pluginList.add(p);
            maskMenuItem.add(p);
            printLoadPlugin(p);

            p = new GPCopyMask(this);
            pluginList.add(p);
            maskMenuItem.add(p);
            printLoadPlugin(p);

            p = new GPPasteMask(this);
            pluginList.add(p);
            maskMenuItem.add(p);
            printLoadPlugin(p);

            // view menu

            p = new GPLayerStack(this);
            pluginList.add(p);
            viewMenuItem.add(p);
            mainButton.add(p);
            printLoadPlugin(p);

            p = new GPChannelStack(this);
            pluginList.add(p);
            viewMenuItem.add(p);
            mainButton.add(p);
            printLoadPlugin(p);

            p = new GPZoom(this);
            pluginList.add(p);
            viewMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.I_UP);
            printLoadPlugin(p);

            p = new GPViewOptions(this);
            pluginList.add(p);
            viewMenuItem.add(p);
            printLoadPlugin(p);

            p = new GPVuMeter(this);
            pluginList.add(p);
            viewMenuItem.add(p);
            mainButton.add(p);
            printLoadPlugin(p);

            p = new GPPluginHistory(this);
            pluginList.add(p);
            viewMenuItem.add(p);
            printLoadPlugin(p);

            // analysis menu

            p = new GPCalculator(this);
            pluginList.add(p);
            analysisMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPMeasure(this);
            pluginList.add(p);
            analysisMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.I_DOWN);
            printLoadPlugin(p);
            measure = p;

            p = new GPSpectrum(this);
            pluginList.add(p);
            analysisMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.S_UP);
            printLoadPlugin(p);

            p = new GPHistogram(this);
            pluginList.add(p);
            analysisMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            // generator menu

            p = new GPFreeGenerator(this);
            pluginList.add(p);
            generatorMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.F_UP);
            printLoadPlugin(p);

            p = new GPSignalGenerator(this);
            pluginList.add(p);
            generatorMenuItem.add(p);
            mainButton.add(p);
            printLoadPlugin(p);

            p = new GPHarmonicsGenerator(this);
            pluginList.add(p);
            generatorMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPPitchGenerator(this);
            pluginList.add(p);
            generatorMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            // amplitude menu

            p = new GPAmplify(this);
            pluginList.add(p);
            amplitudeMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.N_LEFT);
            printLoadPlugin(p);

            p = new GPPan(this);
            pluginList.add(p);
            amplitudeMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPFade(this);
            pluginList.add(p);
            amplitudeMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.F_DOWN);
            printLoadPlugin(p);

            p = new GPCompressExpand(this);
            pluginList.add(p);
            amplitudeMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPDistortion(this);
            pluginList.add(p);
            amplitudeMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPAutoVolume(this);
            pluginList.add(p);
            amplitudeMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            // samples menu

            p = new GPResample(this);
            pluginList.add(p);
            samplesMenuItem.add(p);
            mainButton.add(p);
            printLoadPlugin(p);

            p = new GPMath(this);
            pluginList.add(p);
            samplesMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPConvolution(this);
            pluginList.add(p);
            samplesMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPFft(this);
            pluginList.add(p);
            samplesMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPReverse(this);
            pluginList.add(p);
            samplesMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPLoopable(this);
            pluginList.add(p);
            samplesMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPFlipBytes(this);
            pluginList.add(p);
            samplesMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            // help menu

            p = new GPTipOfTheDay(this);
            pluginList.add(p);
            helpMenuItem.add(p);
            // mainButton.add(p);
            strokes.add(p, UiStroke.S_MIRROR_DOWN);
            printLoadPlugin(p);

            p = new GPSystemInfo(this);
            pluginList.add(p);
            helpMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPAbout(this);
            pluginList.add(p);
            helpMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            // effects menu

            p = new GPReverb(this);
            pluginList.add(p);
            effectsMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPDelayEcho(this);
            pluginList.add(p);
            effectsMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPChorusFlange(this);
            pluginList.add(p);
            effectsMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPVibrato(this);
            pluginList.add(p);
            effectsMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPPitchShiftTimeStretch(this);
            pluginList.add(p);
            effectsMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPMultiPitch(this);
            pluginList.add(p);
            effectsMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPDisharmony(this);
            pluginList.add(p);
            effectsMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPNarrowWide(this);
            pluginList.add(p);
            effectsMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPNoiseReduction(this);
            pluginList.add(p);
            effectsMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            // filter menu

            p = new GPEqualizer(this);
            pluginList.add(p);
            filterMenuItem.add(p);
            mainButton.add(p);
            strokes.add(p, UiStroke.F_MIRROR_DOWN);
            printLoadPlugin(p);

            p = new GPParameterFilter(this);
            pluginList.add(p);
            filterMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPMultiNotch(this);
            pluginList.add(p);
            filterMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPFftFilter(this);
            pluginList.add(p);
            filterMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);

            p = new GPSpectrogramFilter(this);
            pluginList.add(p);
            filterMenuItem.add(p);
            // mainButton.add(p);
            printLoadPlugin(p);
        } catch (Exception e) {
            Debug.println(1, "could not load all plugins");
            e.printStackTrace();
        }

    }

    private void loadExternalPlugins() {
        class ClassFileFilter implements FileFilter {
            public boolean accept(File file) {
                return file.getName().endsWith(".class");
            }
        }

        // read directory content...
        File file[] = new File("ch/laoe/plugin/extern").listFiles(new ClassFileFilter());
        String className = "";

        // directory existing ?
        if (file != null) {
            // each file of directory...
            for (int i = 0; i < file.length; i++) {
                try {
                    // if plugin exists, then preload it...
                    className = "ch.laoe.plugin.extern." + file[i].getName();
                    className = className.substring(0, className.lastIndexOf('.'));
                    GPlugin p = (GPlugin) Class.forName(className).newInstance();

                    // is valid external plugin ?
                    if ((p instanceof GExternalPlugin)) {
                        // load and add to LAoE...
                        ((GExternalPlugin) p).init(this);
                        pluginList.add(p);
                        externalPluginMenuItem.add(p);
                        printLoadExternalPlugin(p);
                        Debug.println(1, "external plugin found: " + className);
                    }
                } catch (ClassCastException e) {
                } catch (IllegalAccessException e) {
                } catch (ClassNotFoundException e) {
                } catch (InstantiationException e) {
                }
            }
        }
    }

    public void reload() {
        if (getFocussedPlugin(0) != null) {
            getFocussedPlugin(0).reload();
        }
    }

    /**
     * menus
     */

    private void initMenus() {
        fileMenuItem = new ArrayList<GPlugin>();
        editMenuItem = new ArrayList<GPlugin>();
        maskMenuItem = new ArrayList<GPlugin>();
        selectMenuItem = new ArrayList<GPlugin>();
        viewMenuItem = new ArrayList<GPlugin>();
        clipMenuItem = new ArrayList<GPlugin>();
        analysisMenuItem = new ArrayList<GPlugin>();
        penMenuItem = new ArrayList<GPlugin>();
        generatorMenuItem = new ArrayList<GPlugin>();
        amplitudeMenuItem = new ArrayList<GPlugin>();
        samplesMenuItem = new ArrayList<GPlugin>();
        filterMenuItem = new ArrayList<GPlugin>();
        effectsMenuItem = new ArrayList<GPlugin>();
        externalPluginMenuItem = new ArrayList<GPlugin>();
        helpMenuItem = new ArrayList<GPlugin>();

        mainButton = new ArrayList<GPlugin>();
    }

    private List<GPlugin> fileMenuItem;

    private JMenu createFileMenu() {
        JMenu m = new JMenu(GLanguage.translate("file"));
        m.add(fileMenuItem.get(0).createMenuItem());
        m.add(fileMenuItem.get(1).createMenuItem());
        m.addSeparator();
        m.add(fileMenuItem.get(2).createMenuItem());
        m.add(fileMenuItem.get(3).createMenuItem());
        m.addSeparator();
        m.add(fileMenuItem.get(4).createMenuItem());
        m.add(fileMenuItem.get(5).createMenuItem());
        m.addSeparator();
        m.add(fileMenuItem.get(6).createMenuItem());
        m.addSeparator();
        m.add(fileMenuItem.get(7).createMenuItem());
        return m;
    }

    private List<GPlugin> editMenuItem;

    private JMenu createEditMenu() {
        JMenu m = new JMenu(GLanguage.translate("edit"));
        m.add(editMenuItem.get(0).createMenuItem());
        m.add(editMenuItem.get(1).createMenuItem());
        m.add(editMenuItem.get(2).createMenuItem());
        m.addSeparator();
        m.add(editMenuItem.get(3).createMenuItem());
        m.addSeparator();
        m.add(editMenuItem.get(4).createMenuItem());
        m.add(editMenuItem.get(5).createMenuItem());
        m.add(editMenuItem.get(6).createMenuItem());
        m.add(editMenuItem.get(7).createMenuItem());
        m.add(editMenuItem.get(8).createMenuItem());
        m.addSeparator();
        m.add(editMenuItem.get(9).createMenuItem());
        m.add(editMenuItem.get(10).createMenuItem());
        m.add(editMenuItem.get(11).createMenuItem());
        m.addSeparator();
        m.add(editMenuItem.get(12).createMenuItem());
        m.add(editMenuItem.get(13).createMenuItem());
        m.add(editMenuItem.get(14).createMenuItem());
        m.addSeparator();
        m.add(editMenuItem.get(15).createMenuItem());
        return m;
    }

    private List<GPlugin> selectMenuItem;

    private JMenu createSelectMenu() {
        JMenu m = new JMenu(GLanguage.translate("select"));
        m.add(selectMenuItem.get(0).createMenuItem());
        m.add(selectMenuItem.get(1).createMenuItem());
        m.add(selectMenuItem.get(2).createMenuItem());
        m.add(selectMenuItem.get(3).createMenuItem());
        m.add(selectMenuItem.get(4).createMenuItem());
        m.add(selectMenuItem.get(5).createMenuItem());
        m.addSeparator();
        m.add(selectMenuItem.get(6).createMenuItem());
        m.addSeparator();
        m.add(selectMenuItem.get(7).createMenuItem());
        m.add(selectMenuItem.get(8).createMenuItem());
        return m;
    }

    private List<GPlugin> maskMenuItem;

    private JMenu createMaskMenu() {
        JMenu m = new JMenu(GLanguage.translate("mask"));
        m.add(maskMenuItem.get(0).createMenuItem());
        m.add(maskMenuItem.get(1).createMenuItem());
        m.addSeparator();
        m.add(maskMenuItem.get(2).createMenuItem());
        m.add(maskMenuItem.get(3).createMenuItem());
        return m;
    }

    private List<GPlugin> viewMenuItem;

    private JMenu createViewMenu() {
        JMenu m = new JMenu(GLanguage.translate("view"));
        m.add(viewMenuItem.get(0).createMenuItem());
        m.add(viewMenuItem.get(1).createMenuItem());
        m.addSeparator();
        m.add(viewMenuItem.get(2).createMenuItem());
        m.add(viewMenuItem.get(3).createMenuItem());
        m.add(viewMenuItem.get(4).createMenuItem());
        m.addSeparator();
        m.add(viewMenuItem.get(5).createMenuItem());
        return m;
    }

    private List<GPlugin> clipMenuItem;

    private JMenu createClipMenu() {
        JMenu m = new JMenu(GLanguage.translate("clip"));
        m.add(clipMenuItem.get(0).createMenuItem());
        m.addSeparator();
        m.add(createAnalysisMenu());
        m.addSeparator();
        m.add(createGeneratorMenu());
        m.add(createAmplitudeMenu());
        m.add(createSamplesMenu());
        m.add(createFilterMenu());
        m.add(createEffectsMenu());
        m.addSeparator();
        // m.add(createPenMenu());
        // m.addSeparator();
        m.add(clipMenuItem.get(1).createMenuItem());
        return m;
    }

    private List<GPlugin> analysisMenuItem;

    private JMenu createAnalysisMenu() {
        JMenu m = new JMenu(GLanguage.translate("analysis"));
        m.add(analysisMenuItem.get(0).createMenuItem());
        m.add(analysisMenuItem.get(1).createMenuItem());
        m.add(analysisMenuItem.get(2).createMenuItem());
        m.add(analysisMenuItem.get(3).createMenuItem());
        return m;
    }

    private List<GPlugin> penMenuItem;

    private JMenu createPenMenu() {
        JMenu m = new JMenu(GLanguage.translate("pen"));
        return m;
    }

    private List<GPlugin> generatorMenuItem;

    private JMenu createGeneratorMenu() {
        JMenu m = new JMenu(GLanguage.translate("generator"));
        m.add(generatorMenuItem.get(0).createMenuItem());
        m.add(generatorMenuItem.get(1).createMenuItem());
        m.add(generatorMenuItem.get(2).createMenuItem());
        m.add(generatorMenuItem.get(3).createMenuItem());
        return m;
    }

    private List<GPlugin> amplitudeMenuItem;

    private JMenu createAmplitudeMenu() {
        JMenu m = new JMenu(GLanguage.translate("amplitude"));
        m.add(amplitudeMenuItem.get(0).createMenuItem());
        m.add(amplitudeMenuItem.get(1).createMenuItem());
        m.add(amplitudeMenuItem.get(2).createMenuItem());
        m.addSeparator();
        m.add(amplitudeMenuItem.get(3).createMenuItem());
        m.add(amplitudeMenuItem.get(4).createMenuItem());
        m.add(amplitudeMenuItem.get(5).createMenuItem());
        return m;
    }

    private List<GPlugin> samplesMenuItem;

    private JMenu createSamplesMenu() {
        JMenu m = new JMenu(GLanguage.translate("samples"));
        m.add(samplesMenuItem.get(0).createMenuItem());
        m.addSeparator();
        m.add(samplesMenuItem.get(1).createMenuItem());
        m.add(samplesMenuItem.get(2).createMenuItem());
        m.add(samplesMenuItem.get(3).createMenuItem());
        m.addSeparator();
        m.add(samplesMenuItem.get(4).createMenuItem());
        m.add(samplesMenuItem.get(5).createMenuItem());
        m.add(samplesMenuItem.get(6).createMenuItem());
        return m;
    }

    private List<GPlugin> filterMenuItem;

    private JMenu createFilterMenu() {
        JMenu m = new JMenu(GLanguage.translate("filter"));
        m.add(filterMenuItem.get(0).createMenuItem());
        m.add(filterMenuItem.get(1).createMenuItem());
        m.add(filterMenuItem.get(2).createMenuItem());
        m.addSeparator();
        m.add(filterMenuItem.get(3).createMenuItem());
        m.add(filterMenuItem.get(4).createMenuItem());
        return m;
    }

    private List<GPlugin> effectsMenuItem;

    private JMenu createEffectsMenu() {
        JMenu m = new JMenu(GLanguage.translate("effects"));
        m.add(effectsMenuItem.get(0).createMenuItem());
        m.add(effectsMenuItem.get(1).createMenuItem());
        m.add(effectsMenuItem.get(2).createMenuItem());
        m.add(effectsMenuItem.get(3).createMenuItem());
        m.addSeparator();
        m.add(effectsMenuItem.get(4).createMenuItem());
        m.add(effectsMenuItem.get(5).createMenuItem());
        m.add(effectsMenuItem.get(6).createMenuItem());
        m.add(effectsMenuItem.get(7).createMenuItem());
        m.addSeparator();
        m.add(effectsMenuItem.get(8).createMenuItem());
        return m;
    }

    private List<GPlugin> externalPluginMenuItem;

    private JMenu createExternalPluginMenu() {
        JMenu m = new JMenu(GLanguage.translate("externalPlugins"));
        for (int i = 0; i < externalPluginMenuItem.size(); i++) {
            m.add(externalPluginMenuItem.get(i).createMenuItem());
        }
        return m;
    }

    private List<GPlugin> helpMenuItem;

    private JMenu createHelpMenu() {
        JMenu m = new JMenu(GLanguage.translate("help"));
        m.add(helpMenuItem.get(0).createMenuItem());
//      m.add(helpMenuItem.get(1).createMenuItem());
        m.addSeparator();
        m.add(helpMenuItem.get(1).createMenuItem());
        m.add(helpMenuItem.get(2).createMenuItem());
        return m;
    }

    public UiPopupMenu createFullPopupMenu() {
        UiPopupMenu p = new UiPopupMenu();
        p.add(createFileMenu());
        p.add(createEditMenu());
        p.add(createSelectMenu());
        p.add(createMaskMenu());
        p.add(createViewMenu());
        p.add(createClipMenu());
        p.add(createExternalPluginMenu());
        p.add(createHelpMenu());
        return p;
    }

    public void createMainMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(createHelpMenu());
        main.getMainFrame().setJMenuBar(menuBar);
    }

    private List<GPlugin> mainButton;

    private void createMainButtons() {
        for (int i = 0; i < mainButton.size(); i++) {
            main.getMainFrame().addButton(mainButton.get(i).createButton());
        }
    }

    // starts visible plugins
    private void startVsiblePlugins() {
        for (int i = 0; i < pluginList.size(); i++) {
            GPlugin p = pluginList.get(i);

            if (p instanceof GPluginFrame) {
                // reload all visible plugins...
                if (((GPluginFrame) p).wasVisibleOnExit()) {
                    try {
                        ((GPluginFrame) p).start();
                    } catch (Exception e) {
                    }
                }
            }
        }

    }

    // strokes

    private GPStrokeHandler strokes;

    private void initStrokes() {
        strokes = new GPStrokeHandler();
    }

    public GPStrokeHandler getStrokeHandler() {
        return strokes;
    }

    /**
     * environment
     */

    public GClipEditor getFocussedClipEditor() {
        return main.getFocussedClipEditor();
    }

    public AClip getFocussedClip() {
        return main.getFocussedClipEditor().getClip();
    }

    public GMain getMain() {
        return main;
    }

    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
        try {
            // delegate to all visible plugins...
            for (int i = 0; i < pluginList.size(); i++) {
                GPlugin p = pluginList.get(i);

                if (p != null) {
                    // reload all visible plugins...
                    if (p.isVisible() && getFocussedClip().getSelectedLayer().getLayerPlotter().isVisible()) {
                        g2d.setClip(rect);
                        p.paintOntoClip(g2d, rect);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void reloadAllPluginsAndFocussedClip() {
        // delegate to all plugins...
        for (int i = 0; i < pluginList.size(); i++) {
            GPlugin p = pluginList.get(i);

            if (p != null) {
                // reload all visible plugins...
                if (p.isVisible()) {
                    p.reload();
                    p.repaintFocussedClipEditor();
                }
            }
        }
    }

    // mouse-events come from clip-editor

    public void mouseClicked(MouseEvent e) {
        try {
            if (getFocussedPlugin(0) != null) {
                if (!e.isPopupTrigger()) {
                    Debug.println(4, "plugin " + getFocussedPlugin(0).getName() + " mouse clicked");
                    getFocussedPlugin(0).mouseClicked(e);
                }
            }
        } catch (Exception exc) {
        }
    }

    public void mouseEntered(MouseEvent e) {
        try {
            if (getFocussedPlugin(0) != null) {
                if (!e.isPopupTrigger()) {
                    Debug.println(4, "plugin " + getFocussedPlugin(0).getName() + " mouse entered");
                    getFocussedPlugin(0).mouseEntered(e);
                }
            }
        } catch (Exception exc) {
        }
    }

    public void mouseExited(MouseEvent e) {
        try {
            if (getFocussedPlugin(0) != null) {
                if (!e.isPopupTrigger()) {
                    Debug.println(4, "plugin " + getFocussedPlugin(0).getName() + " mouse exited");
                    getFocussedPlugin(0).mouseExited(e);
                }
            }
        } catch (Exception exc) {
        }
    }

    public void mousePressed(MouseEvent e) {
        try {
            if (getFocussedPlugin(0) != null) {
                if (!e.isPopupTrigger()) {
                    Debug.println(4, "plugin " + getFocussedPlugin(0).getName() + " mouse pressed");
                    getFocussedPlugin(0).mousePressed(e);
                }
            }
        } catch (Exception exc) {
        }
    }

    public void mouseReleased(MouseEvent e) {
        try {
            if (getFocussedPlugin(0) != null) {
                if (!e.isPopupTrigger()) {
                    Debug.println(4, "plugin " + getFocussedPlugin(0).getName() + " mouse released");
                    getFocussedPlugin(0).mouseReleased(e);
                }
            }
        } catch (Exception exc) {
        }
    }

    public void mouseMoved(MouseEvent e) {
        try {
            if (getFocussedPlugin(0) != null) {
                if (!e.isPopupTrigger()) {
                    Debug.println(9, "plugin " + getFocussedPlugin(0).getName() + " mouse moved");
                    getFocussedPlugin(0).mouseMoved(e);
                }
            }
        } catch (Exception exc) {
        }
    }

    public void mouseDragged(MouseEvent e) {
        try {
            if (getFocussedPlugin(0) != null) {
                if (!e.isPopupTrigger()) {
                    Debug.println(9, "plugin " + getFocussedPlugin(0).getName() + " mouse dragged");
                    getFocussedPlugin(0).mouseDragged(e);
                }
            }
        } catch (Exception exc) {
        }
    }

    // iconify manager

    private List<GPluginFrame> visibleFrames;

    /**
     * iconifies all visible frames
     */
    public void iconifyAllFrames() {
        visibleFrames.clear();

        // clip frames...
        for (int i = 0; i < pluginList.size(); i++) {
            if (pluginList.get(i) instanceof GPluginFrame) {
                GPluginFrame f = (GPluginFrame) pluginList.get(i);
                if (f.getState() == Frame.NORMAL) {
                    Debug.println(4, "plugin " + getFocussedPlugin(0).getName() + " iconify");
                    visibleFrames.add(f);
                    f.setState(Frame.ICONIFIED);
                }
            }
        }
    }

    /**
     * deiconifies all previously visible frames
     */
    public void deiconifyAllFrames() {
        // clip frames...
        for (int i = 0; i < visibleFrames.size(); i++) {
            Debug.println(4, "plugin " + getFocussedPlugin(0).getName() + " deiconify");
            GPluginFrame f = visibleFrames.get(i);
            f.setState(Frame.NORMAL);
        }
    }

}
