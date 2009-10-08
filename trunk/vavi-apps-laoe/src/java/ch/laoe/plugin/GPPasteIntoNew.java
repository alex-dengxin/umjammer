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

import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;


/**
 * Class: GPPasteIntoNew @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to paste the clipboard into a new clip.
 * 
 * @version 04.11.00 erster Entwurf oli4
 * 
 */
public class GPPasteIntoNew extends GPlugin {
    public GPPasteIntoNew(GPluginHandler ph) {
        super(ph);
    }

    protected String getName() {
        return "pasteIntoNew";
    }

    public void start() {
        super.start();
        // duplicate clip to keep all parameters...
        GProgressViewer.start(getName());
        AClip c = new AClip(getFocussedClip());
        c.removeAll();
        c.add(new ALayer(GPCopy.getClipBoardLayer()));
        getMain().addClipFrame(c);
        autoScaleFocussedClip();
        // update
        updateHistory(GLanguage.translate(getName()));
        GProgressViewer.finish();
    }
}
