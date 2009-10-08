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

package ch.laoe.clip;

import java.util.Vector;

import ch.laoe.operation.AOperation;
import ch.laoe.ui.GProgressViewer;


/**
 * groups multiple selections
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 25.07.00 erster Entwurf oli4 <br>
 *          03.08.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          16.05.01 add start/endOperation oli4 <br>
 */
public class ALayerSelection extends ASelection {
    /**
     * constructor
     */
    public ALayerSelection(ALayer l) {
        super(l);
        channelSelection = new Vector<AChannelSelection>();
    }

    /**
     * copy-constructor
     */
    public ALayerSelection(ALayerSelection s) {
        this((ALayer) s.model);
        this.name = s.name;
        // each channel-selection...
        for (int i = 0; i < s.getNumberOfChannelSelections(); i++) {
            addChannelSelection(new AChannelSelection(s.getChannelSelection(i)));
        }
    }

    public ALayer getLayer() {
        return (ALayer) model;
    }

    public void setLayer(ALayer l) {
        model = l;
    }

    private static int nameCounter;

    /**
     * set the default name of the layer
     */
    public void setDefaultName() {
        setDefaultName("layerSelection", nameCounter++);
    }

    // channel selections
    private Vector<AChannelSelection> channelSelection;

    /**
     * add a channel selection
     */
    public void addChannelSelection(AChannelSelection s) {
        channelSelection.add(s);
    }

    public AChannelSelection getChannelSelection(int index) {
        return channelSelection.get(index);
    }

    public int getNumberOfChannelSelections() {
        return channelSelection.size();
    }

    /**
     * returns true if anything is selected
     */
    public boolean isSelected() {
        // each channel-selection...
        for (int i = 0; i < getNumberOfChannelSelections(); i++) {
            if (getChannelSelection(i).isSelected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * return the biggest channel-selection size
     */
    public int getMaxLength() {
        int max = 0;
        // each channel-selection...
        for (int i = 0; i < getNumberOfChannelSelections(); i++) {
            int l = getChannelSelection(i).getLength();
            if (l > max) {
                max = l;
            }
        }
        return max;
    }

    /**
     * return the lowest selected index of all channels
     */
    public int getLowestSelectedIndex() {
        int min = Integer.MAX_VALUE;
        // each channel-selection...
        for (int i = 0; i < getNumberOfChannelSelections(); i++) {
            int l = getChannelSelection(i).getOffset();
            if (l < min) {
                min = l;
            }
        }
        return min;
    }

    /**
     * return the highest selected index of all channels
     */
    public int getHighestSelectedIndex() {
        int max = 0;
        // each channel-selection...
        for (int i = 0; i < getNumberOfChannelSelections(); i++) {
            AChannelSelection chs = getChannelSelection(i);

            int l = chs.getOffset() + chs.getLength();
            if (l > max) {
                max = l;
            }
        }
        return max;
    }

    // intensity

    /**
     * clear the intensityof all channel-selections
     */
    public void clearIntensity() {
        // each channel-selection...
        for (int i = 0; i < getNumberOfChannelSelections(); i++) {
            getChannelSelection(i).clearIntensity();
        }
    }

    // operate

    /**
     * operate one-channel operations, each selected channel. overwrite channels.
     */
    public void operateEachChannel(AOperation o) {
        GProgressViewer.entrySubProgress("layer");
        o.startOperation();
        // each channel...
        for (int j = 0; j < getNumberOfChannelSelections(); j++) {
            if (getChannelSelection(j).isSelected()) {
                GProgressViewer.setProgress((j + 1) * 100 / getNumberOfChannelSelections());
                GProgressViewer.entrySubProgress("channel", " " + j);
                // TODO consider placement
                o.addEditorListener(GProgressViewer.operationProgressListener);
                o.operate(getChannelSelection(j));
                GProgressViewer.exitSubProgress();
            }
        }
        o.endOperation();
        System.gc();
        GProgressViewer.exitSubProgress();
    }

    /**
     * operate channel 0 and 1
     */
    public void operateChannel0WithChannel1(AOperation o) {
        o.startOperation();
        if (getChannelSelection(0).isSelected()) {
            o.operate(getChannelSelection(0), getChannelSelection(1));
        }
        o.endOperation();
        System.gc();
    }

    /**
     * operate channel 0 and 1 and 2
     */
    public void operateChannel0WithChannel1WithChannel2(AOperation o) {
        o.startOperation();
        if (getChannelSelection(0).isSelected()) {
            o.operate(getChannelSelection(0), getChannelSelection(1), getChannelSelection(2));
        }
        o.endOperation();
        System.gc();
    }
}
