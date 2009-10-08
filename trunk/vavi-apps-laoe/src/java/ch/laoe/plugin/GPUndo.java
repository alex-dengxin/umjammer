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

import ch.laoe.ui.GProgressViewer;


/**
 * Class: GPUndo @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to perform one undo-step.
 * 
 * @version 03.01.02 first draft oli4
 * 
 */
public class GPUndo extends GPlugin {
    public GPUndo(GPluginHandler ph) {
        super(ph);
    }

    protected String getName() {
        return "undo";
    }

    public JMenuItem createMenuItem() {
        return super.createMenuItem(KeyEvent.VK_Z);
    }

    public void start() {
        super.start();

        try {
            GProgressViewer.start(getName());
            getFocussedClip().getHistory().undo(getFocussedClip());
            updateFrameTitle();
            reloadFocussedClipEditor();
            GProgressViewer.finish();
        } catch (NullPointerException e) {
        }
    }

}
