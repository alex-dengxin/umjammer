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

import ch.laoe.clip.ALayer;


/**
 * GPUnmaskLayer
 * 
 * plugin to remove the mask of a layer
 * 
 * @target JDK 1.3
 * @author olivier gäumann, neuchâtel (switzerland)
 * @version 03.03.02 first draft oli4
 */
public class GPUnmaskLayer extends GPlugin {
    public GPUnmaskLayer(GPluginHandler ph) {
        super(ph);
    }

    protected String getName() {
        return "unmaskLayer";
    }

    public void start() {
        super.start();

        // clear all channels's mask of the actual layer...
        ALayer l = getFocussedClip().getSelectedLayer();

        for (int i = 0; i < l.getNumberOfChannels(); i++) {
            l.getChannel(i).getMask().clear();
        }
        repaintFocussedClipEditor();
    }

}
