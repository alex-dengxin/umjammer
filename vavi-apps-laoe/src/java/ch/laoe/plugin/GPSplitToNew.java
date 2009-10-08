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

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelMarker;
import ch.laoe.clip.AChannelSelection;
import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;
import ch.laoe.ui.GProgressViewer;


/**
 * Class: GPSplit @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to split a clip infunction of the markers.
 * 
 * @version 12.05.2003 first draft oli4
 * 
 */
public class GPSplitToNew extends GPlugin {
    public GPSplitToNew(GPluginHandler ph) {
        super(ph);
    }

    protected String getName() {
        return "splitToNew";
    }

    public void start() {
        super.start();
        // duplicate clip
        GProgressViewer.start(getName());
        GProgressViewer.entrySubProgress();
        GProgressViewer.setProgress(0);
        ALayer l = getSelectedLayer();
        AChannelMarker m = l.getSelectedChannel().getMarker();
        int n = m.getNumberOfMarkers();
        int lastMarker = 0;

        // each marker-range...
        for (int i = 0; i < n + 1; i++) {
            GProgressViewer.setProgress(100 * i / (n + 1));
            int currentMarker;
            if (i < n) {
                currentMarker = m.getMarkerX(i);
            } else {
                currentMarker = l.getSelectedChannel().getSampleLength();
            }
            AClip nc = new AClip(1, 0, 0);
            nc.copyAllAttributes(getFocussedClip());

            // each channel...
            for (int j = 0; j < l.getNumberOfChannels(); j++) {
                AChannelSelection chs = new AChannelSelection(l.getChannel(j), lastMarker, currentMarker - lastMarker);
                nc.getLayer(0).add(new AChannel(chs));
            }
            nc.getAudio().setLoopEndPointer(nc.getLayer(0).getMaxSampleLength());
            nc.setDefaultName();
            getMain().addClipFrame(nc);
            autoScaleFocussedClip();
            lastMarker = currentMarker;
        }
        GProgressViewer.exitSubProgress();
        GProgressViewer.finish();
    }
}
