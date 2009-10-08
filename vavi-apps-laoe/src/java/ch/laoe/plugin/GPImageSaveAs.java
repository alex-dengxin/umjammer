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
import java.io.FileOutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import ch.laoe.ui.Debug;
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
 * @version 29.04.2003 first draft oli4
 */
public class GPImageSaveAs extends GPlugin {
    public GPImageSaveAs(GPluginHandler ph) {
        super(ph);
        fileChooser = new JFileChooser(persistance.getString("plugin." + getName() + ".currentDirectory"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        String s[] = ImageIO.getWriterFormatNames();
        for (int i = 0; i < s.length; i++) {
            fileChooser.addChoosableFileFilter(new GFileFilter("." + s[i]));
        }

        // fileChooser.addChoosableFileFilter(new GFileFilter(".jpg"));
        // fileChooser.addChoosableFileFilter(new GFileFilter(".tiff"));
        // fileChooser.addChoosableFileFilter(new GFileFilter(".gif"));
        // fileChooser.addChoosableFileFilter(new GFileFilter(".png"));

        fileChooser.setFileView(new GPFileView());
    }

    protected String getName() {
        return "imageSaveAs";
    }

    public void start() {
        // open
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fileChooser.getSelectedFile();
                String extension = fileChooser.getFileFilter().getDescription().toLowerCase();

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
                    if (!GDialog.showYesNoQuestionDialog(fileChooser, "fileWarning", new JLabel(GLanguage.translate("overwrite?")))) {
                        return;
                    }
                }

                // add plugin-drawings ? ?
                boolean pluginDrawingEnabled = GDialog.showYesNoQuestionDialog(fileChooser, "paintOptions", new JLabel(GLanguage.translate("pluginDrawings?")));

                // save...
                GProgressViewer.start("saving");
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);

                Debug.println(3, "save clip frame " + getFocussedClipEditor().getClip().getName() + " as image " + f);
                FileOutputStream out = new FileOutputStream(f);
                Iterator ite = ImageIO.getImageWritersByFormatName(extension.substring(1));
                ImageWriter writer = (ImageWriter) ite.next();
                ImageOutputStream ios = ImageIO.createImageOutputStream(out);
                writer.setOutput(ios);
                writer.write(getFocussedClipEditor().createFullImage(pluginDrawingEnabled));
                out.close();

                GProgressViewer.setProgress(100);
                GProgressViewer.exitSubProgress();
            } catch (Exception exc) {
                GDialog.showErrorDialog(null, "ioError", "couldNotSaveImage");
                exc.printStackTrace();
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
