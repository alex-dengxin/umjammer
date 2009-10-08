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

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelSelection;
import ch.laoe.clip.ALayer;
import ch.laoe.clip.ALayerSelection;


/**
 * plugin to copy the actual selection.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 03.10.00 erster Entwurf oli4
 */
public class GPCopy extends GPlugin {
    public GPCopy(GPluginHandler ph) {
        super(ph);
    }

    protected String getName() {
        return "copy";
    }

    public JMenuItem createMenuItem() {
        return super.createMenuItem(KeyEvent.VK_C);
    }

    // clip board
    private static ALayer clipBoardLayer;

    public static ALayerSelection getClipBoardMultiSelection() {
        return clipBoardLayer.createLayerSelection();
    }

    public static ALayer getClipBoardLayer() {
        return clipBoardLayer;
    }

    public void start() {
        super.start();
        // copy data
        clipBoardLayer = new ALayer();
        ALayer originalLayer = getFocussedClip().getSelectedLayer();

        // each selected channel...
        for (int i = 0; i < originalLayer.getNumberOfChannels(); i++) {
            AChannelSelection chs = originalLayer.getChannel(i).getChannelSelection();

            if (chs.isSelected()) {
                clipBoardLayer.add(new AChannel(chs));
            }
        }
    }
}
