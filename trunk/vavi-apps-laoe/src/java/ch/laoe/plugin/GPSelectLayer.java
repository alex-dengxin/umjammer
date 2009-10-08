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

import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;
import ch.laoe.ui.GLanguage;


/**
 * Class: GPSelectLayer @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to select layers. mouse action table: ----------------------------------- release
 * ----------------------------------- - select layer -----------------------------------
 * 
 * 
 * @version 27.07.01 first draft oli4
 * 
 */
public class GPSelectLayer extends GPlugin {
    public GPSelectLayer(GPluginHandler ph) {
        super(ph);
    }

    protected String getName() {
        return "selectLayer";
    }

    public JMenuItem createMenuItem() {
        return super.createMenuItem(KeyEvent.VK_A);
    }

    public void start() {
        super.start();
        AClip clip = getFocussedClip();
        ALayer layer = clip.getSelectedLayer();

        /*
         * //toggle selection... if (layer.getLayerSelection().isSelected()) { layer.setEmptyLayerSelection(); } else {
         * layer.setFullLayerSelection(); } repaintFocussedClipEditor();
         */
        layer.setFullLayerSelection();
        repaintFocussedClipEditor();
        updateHistory(GLanguage.translate(getName()));

    }

}
