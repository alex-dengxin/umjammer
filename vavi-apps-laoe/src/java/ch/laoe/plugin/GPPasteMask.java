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

import ch.laoe.clip.AChannelMask;
import ch.laoe.ui.GEditableSegments;


/**
 * Class: GPPasteMask @author olivier g�umann, neuch�tel (switzerland) @target JDK 1.3
 * 
 * plugin to paste the selected mask.
 * 
 * @version 02.03.02 first draft oli4
 * 
 */
public class GPPasteMask extends GPlugin {
    public GPPasteMask(GPluginHandler ph) {
        super(ph);
    }

    protected String getName() {
        return "pasteMask";
    }

    public void start() {
        super.start();
        // paste selection
        GEditableSegments ps = GPCopyMask.getClipBoard();
        if (ps != null) {
            AChannelMask m = getFocussedClip().getSelectedLayer().getSelectedChannel().getMask();
            m.setSegments(ps);
            repaintFocussedClipEditor();
        }
    }
}
