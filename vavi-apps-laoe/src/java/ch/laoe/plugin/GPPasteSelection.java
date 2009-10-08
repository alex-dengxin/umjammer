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

import ch.laoe.clip.AChannelSelection;


/**
 * Class: GPPasteSelection @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to paste the copied selection to the actual layer.
 * 
 * @version 04.11.00 erster Entwurf oli4
 * 
 * 17.06.02 changed from layerselection- to channelselection- clipboard oli4
 * 
 */
public class GPPasteSelection extends GPlugin {
    public GPPasteSelection(GPluginHandler ph) {
        super(ph);
    }

    protected String getName() {
        return "pasteSelection";
    }

    public void start() {
        super.start();
        // paste selection
        AChannelSelection ps = GPCopySelection.getClipBoard();
        if (ps != null) {
            getFocussedClip().getSelectedLayer().getSelectedChannel().setChannelSelection(ps);
            repaintFocussedClipEditor();
        }
    }
}
