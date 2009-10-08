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

import javax.swing.JMenuItem;

import ch.laoe.audio.AudioException;
import ch.laoe.ui.GDialog;
import ch.laoe.ui.GProgressViewer;


/**
 * Class: GPFileSave @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to save a clip-file.
 * 
 * @version 03.12.00 first draft oli4
 * 
 */
public class GPFileSave extends GPlugin {
    public GPFileSave(GPluginHandler ph) {
        super(ph);
    }

    protected String getName() {
        return "save";
    }

    public JMenuItem createMenuItem() {
        return super.createMenuItem(KeyEvent.VK_S);
    }

    public void start() {
        try {
            // no file attributed yet ?
            GProgressViewer.start("saving");
            if (!getFocussedClip().getAudio().save()) {
                GProgressViewer.finish();

                // start "save as"
                pluginHandler.fileSaveAs.start();
            }
            // simple save ?
            else {
                getFocussedClip().getHistory().onSave();
                updateFrameTitle();
            }

        } catch (AudioException ae) {
            GDialog.showErrorDialog(null, "audioError", ae.getMessage());
        } finally {
            GProgressViewer.finish();
        }
    }
}
