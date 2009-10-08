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

import ch.laoe.ui.GEditableSegments;


/**
 * plugin to copy the selected mask
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 02.03.02 first draft oli4
 */
public class GPCopyMask extends GPlugin {
    public GPCopyMask(GPluginHandler ph) {
        super(ph);
    }

    protected String getName() {
        return "copyMask";
    }

    // clip board
    private static GEditableSegments clipBoard;

    public static GEditableSegments getClipBoard() {
        return new GEditableSegments(clipBoard);
    }

    public void start() {
        super.start();
        clipBoard = new GEditableSegments(getFocussedClip().getSelectedLayer().getSelectedChannel().getMask().getSegments());

    }
}
