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

import java.util.ArrayList;
import java.util.List;

import ch.laoe.operation.AOperation;
import ch.laoe.ui.GProgressViewer;


/**
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * selection model.
 * 
 * @version 10.01.01 first draft oli4 <br>
 *          16.05.01 add start/endOperation oli4
 */
public class AClipSelection extends ASelection {
    /**
     * constructor
     */
    public AClipSelection(AClip c) {
        super(c);
        layerSelection = new ArrayList<ALayerSelection>();
    }

    /**
     * copy-constructor
     */
    public AClipSelection(AClipSelection s) {
        this((AClip) s.model);
        this.name = s.name;
    }

    public AClip getClip() {
        return (AClip) model;
    }

    private static int nameCounter;

    /**
     * set the default name of the layer
     */
    public void setDefaultName() {
        setDefaultName("clipSelection", nameCounter++);
    }

    // channel selections
    private List<ALayerSelection> layerSelection;

    /**
     * add a channel selection
     */
    public void addLayerSelection(ALayerSelection s) {
        layerSelection.add(s);
    }

    public ALayerSelection getLayerSelection(int index) {
        return layerSelection.get(index);
    }

    public int getNumberOfLayerSelections() {
        return layerSelection.size();
    }

    /**
     * returns true if anything is selected
     */
    public boolean isSelected() {
        // each layer-selection...
        for (int i = 0; i < getNumberOfLayerSelections(); i++) {
            if (getLayerSelection(i).isSelected()) {
                return true;
            }
        }
        return false;
    }

    // operate

    /**
     * operate one-channel operations, each selected channel of each selected layer. overwrite channels.
     */
    public void operateEachChannel(AOperation o) {
        GProgressViewer.entrySubProgress("clip");
        o.startOperation();
        // each layer...
        for (int i = 0; i < getNumberOfLayerSelections(); i++) {
            GProgressViewer.setProgress((i + 1) * 100 / getNumberOfLayerSelections());
            GProgressViewer.entrySubProgress("layer", " " + i);
            // each channel...
            for (int j = 0; j < getLayerSelection(i).getNumberOfChannelSelections(); j++) {
                GProgressViewer.setProgress((j + 1) * 100 / getLayerSelection(i).getNumberOfChannelSelections());
                GProgressViewer.entrySubProgress("channel", " " + j);
                if (getLayerSelection(i).getChannelSelection(j).isSelected()) {
                    // TODO consider placement
                    o.addEditorListener(GProgressViewer.operationProgressListener);
                    o.operate(getLayerSelection(i).getChannelSelection(j));
                }
                GProgressViewer.exitSubProgress();
            }
            GProgressViewer.exitSubProgress();
        }
        o.endOperation();
        System.gc();
        GProgressViewer.exitSubProgress();
    }

    /**
     * operate two-channel operations, layer0 with layer1, channel by channel, write result to layer0
     */
    public void operateLayer0WithLayer1(AOperation o) {
        int n0 = getLayerSelection(0).getNumberOfChannelSelections();
        int n1 = getLayerSelection(1).getNumberOfChannelSelections();
        int n = Math.min(n0, n1);

        GProgressViewer.entrySubProgress("clip");
        o.startOperation();
        // each channel...
        for (int i = 0; i < n; i++) {
            GProgressViewer.setProgress((i + 1) * 100 / n);
            if (getLayerSelection(0).getChannelSelection(i).isSelected())
                o.operate(getLayerSelection(0).getChannelSelection(i), getLayerSelection(1).getChannelSelection(i));
        }
        o.endOperation();
        System.gc();
        GProgressViewer.exitSubProgress();
    }

    /**
     * operate three-channel operations, layer0 with layer1 and layer2, channel by channel, write result to layer0
     */
    public void operateLayer0WithLayer1And2(AOperation o) {
        int n0 = getLayerSelection(0).getNumberOfChannelSelections();
        int n1 = getLayerSelection(1).getNumberOfChannelSelections();
        int n2 = getLayerSelection(1).getNumberOfChannelSelections();

        int n = Math.min(Math.min(n0, n1), n2);
        GProgressViewer.entrySubProgress("clip");
        o.startOperation();
        // each channel...
        for (int i = 0; i < n; i++) {
            GProgressViewer.setProgress((i + 1) * 100 / n);
            if (getLayerSelection(0).getChannelSelection(i).isSelected()) {
                o.operate(getLayerSelection(0).getChannelSelection(i), getLayerSelection(1).getChannelSelection(i), getLayerSelection(2).getChannelSelection(i));
            }
        }
        o.endOperation();
        System.gc();
        GProgressViewer.exitSubProgress();
    }

}
