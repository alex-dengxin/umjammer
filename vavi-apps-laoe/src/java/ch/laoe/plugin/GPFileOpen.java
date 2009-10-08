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

import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;

import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiPersistanceEvent;


/**
 * GPFileOpen.
 *
 * plugin to open a clip-file.
 * 
 * @target JDK 1.3 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @version 10.09.00 erster Entwurf oli4
 */
public class GPFileOpen extends GPlugin {
    public GPFileOpen(GPluginHandler ph) {
        super(ph);
        fileChooser = new JFileChooser(persistance.getString("plugin." + getName() + ".currentDirectory"));
        String ext1[] = {
            ".wav", ".wave", ".riff"
        };
        fileChooser.addChoosableFileFilter(new GFileFilterGroup(ext1));
        String ext2[] = {
            ".aif", ".aiff", ".aifc"
        };
        fileChooser.addChoosableFileFilter(new GFileFilterGroup(ext2));
        String ext3[] = {
            ".mp1", ".mp2", ".mp3"
        };
        fileChooser.addChoosableFileFilter(new GFileFilterGroup(ext3));
        String ext4[] = {
            ".au", ".snd"
        };
        fileChooser.addChoosableFileFilter(new GFileFilterGroup(ext4));
        String ext5[] = {
            ".gsm"
        };
        fileChooser.addChoosableFileFilter(new GFileFilterGroup(ext5));
        String ext6[] = {
            ".ogg", ".OGG"
        };
        fileChooser.addChoosableFileFilter(new GFileFilterGroup(ext6));
        String ext7[] = {
            ".laoe", ".LAoE"
        };
        fileChooser.addChoosableFileFilter(new GFileFilterGroup(ext7));

        fileChooser.setFileView(new GPFileView());
    }

    protected String getName() {
        return "open";
    }

    public JMenuItem createMenuItem() {
        return super.createMenuItem(KeyEvent.VK_O);
    }

    public void start() {
        super.start();
        // open
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            GProgressViewer.start("loading");
            pluginHandler.getMain().addClipFrame(f);
            GProgressViewer.finish();
        }
    }

    public void onBackup(UiPersistanceEvent e) {
        persistance.setString("plugin." + getName() + ".currentDirectory", fileChooser.getCurrentDirectory().getPath());
    }

    // file open dialog

    private JFileChooser fileChooser;

}
