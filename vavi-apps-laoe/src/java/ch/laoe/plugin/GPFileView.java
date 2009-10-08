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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

import ch.laoe.ui.GToolkit;


/**
 * custom LAoE-fileview for the filechoosers.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 22.03.02 first draft oli4
 */
public class GPFileView extends FileView {
    ImageIcon laoeIcon = GToolkit.loadIcon(this, "resources/laoeFileIcon.gif");

    ImageIcon wavIcon = GToolkit.loadIcon(this, "resources/wavFileIcon.gif");

    ImageIcon auIcon = GToolkit.loadIcon(this, "resources/auFileIcon.gif");

    ImageIcon aiffIcon = GToolkit.loadIcon(this, "resources/aiffFileIcon.gif");

    ImageIcon aifcIcon = GToolkit.loadIcon(this, "resources/aifcFileIcon.gif");

    ImageIcon sndIcon = GToolkit.loadIcon(this, "resources/sndFileIcon.gif");

    ImageIcon mp3Icon = GToolkit.loadIcon(this, "resources/mp3FileIcon.gif");

    ImageIcon gsmIcon = GToolkit.loadIcon(this, "resources/gsmFileIcon.gif");

    ImageIcon oggIcon = GToolkit.loadIcon(this, "resources/oggFileIcon.gif");

    public String getName(File f) {
        return null; // let the L&F FileView figure this out
    }

    public String getDescription(File f) {
        return null; // let the L&F FileView figure this out
    }

    public Boolean isTraversable(File f) {
        return null; // let the L&F FileView figure this out
    }

    /*
     * public String getTypeDescription (File f) { String extension = GToolkit.getFileExtension(f); String type = null;
     * 
     * if (extension != null) { if (extension.equals("wav")) { type = "WAVE file"; } else if (extension.equals("au")) { type = "AU
     * audiofile"; } else if (extension.equals(Utils.tiff) || extension.equals(Utils.tif)) { type = "TIFF Image"; } } return type; }
     */

    public Icon getIcon(File f) {
        String extension = GToolkit.getFileExtension(f);
        // System.out.println("extension="+extension);
        Icon icon = null;

        if (extension != null) {
            if (extension.equals("laoe")) {
                icon = laoeIcon;
            } else if (extension.equals("wav")) {
                icon = wavIcon;
            } else if (extension.equals("au")) {
                icon = auIcon;
            } else if (extension.equals("aiff")) {
                icon = aiffIcon;
            } else if (extension.equals("aifc")) {
                icon = aifcIcon;
            } else if (extension.equals("snd")) {
                icon = sndIcon;
            } else if (extension.equals("mp2") || extension.equals("mp3")) {
                icon = mp3Icon;
            } else if (extension.equals("gsm")) {
                icon = gsmIcon;
            } else if (extension.equals("ogg")) {
                icon = oggIcon;
            }
        }
        return icon;
    }
}
