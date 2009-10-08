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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import ch.laoe.audio.AFileOptions;
import ch.laoe.audio.Audio;
import ch.laoe.audio.AudioException;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOMeasure;
import ch.laoe.ui.GDialog;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiPersistanceEvent;


/**
 * plugin to open a clip-file.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 29.11.00 first draft oli4
 */
public class GPFileSaveAs extends GPlugin {
    public GPFileSaveAs(GPluginHandler ph) {
        super(ph);
        fileChooser = new JFileChooser(persistance.getString("plugin." + getName() + ".currentDirectory"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new GFileFilter(".laoe"));
        fileChooser.addChoosableFileFilter(new GFileFilter(".wav"));
        fileChooser.addChoosableFileFilter(new GFileFilter(".au"));
        // fileChooser.addChoosableFileFilter(new GFileFilter(".mp3"));
        // fileChooser.addChoosableFileFilter(new GFileFilter(".aifc"));
        fileChooser.addChoosableFileFilter(new GFileFilter(".aiff"));
        fileChooser.addChoosableFileFilter(new GFileFilter(".snd"));
        // fileChooser.addChoosableFileFilter(new GFileFilter(".gsm"));

        fileChooser.setFileView(new GPFileView());
    }

    protected String getName() {
        return "saveAs";
    }

    private boolean hasClippedSamples() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        AOMeasure o = new AOMeasure(getFocussedClip().getSampleWidth());
        ls.operateEachChannel(o);

        return o.getNumberOfClippedSamples() > 0;
    }

    public void start() {
        // open
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fileChooser.getSelectedFile();
                String extension = fileChooser.getFileFilter().getDescription().toLowerCase();
                Audio audio = getFocussedClip().getAudio();

                // no extension ?
                if (f.getName().indexOf('.') < 0) {
                    // add extension
                    f = new File(f.getPath() + extension);
                }

                // is directory ?
                if (f.isDirectory()) {
                    GDialog.showErrorDialog(fileChooser, "fileError", "isDirectory");
                    return;
                }

                // is existing file ?
                if (f.isFile()) {
                    // overwrite ?
                    if (!GDialog.showYesNoQuestionDialog(fileChooser, "fileWarning", "overwrite?")) {
                        return;
                    }
                }

                AFileOptions fileOptions = Audio.createFileOptions(extension);

                // set options...
                JPanel p = fileOptions.getPanel();
                if (p != null) {
                    if (!GDialog.showCustomOkCancelDialog(null, p, GLanguage.translate("options"))) {
                        return;
                    }
                }

                // make some checks...
                if (hasClippedSamples()) {
                    GDialog.showWarningDialog(fileChooser, "fileWarning", "containsClippedSamples");
                }

                // save...
                GProgressViewer.start("saving");
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                audio.setFileType(extension);
                audio.setFileOptions(fileOptions);
                audio.saveAs(f);
                getFocussedClip().setName(f.getName());
                GProgressViewer.setProgress(100);
                getFocussedClip().getHistory().onSave();
                GProgressViewer.exitSubProgress();
                updateFrameTitle();
            } catch (AudioException ae) {
                GDialog.showErrorDialog(null, "audioError", ae.getMessage());
            } finally {
                GProgressViewer.finish();
            }
        }
    }

    public void onBackup(UiPersistanceEvent e) {
        persistance.setString("plugin." + getName() + ".currentDirectory", fileChooser.getCurrentDirectory().getPath());
    }

    // file open dialog

    private JFileChooser fileChooser;

}
