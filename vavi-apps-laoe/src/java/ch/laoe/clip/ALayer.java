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

import java.awt.Point;

import ch.laoe.operation.AOMix;
import ch.laoe.ui.GLanguage;


/**
 * layer model.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 25.07.00 erster Entwurf oli4 <br>
 *          02.08.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          28.12.00 stream-based oli4 <br>
 *          24.01.01 array-based again... oli4
 * 
 */
public class ALayer extends AContainerModel {
    /**
     * constructor
     */
    public ALayer() {
        super();
        type = AUDIO_LAYER;
        plotType = SAMPLE_CURVE_TYPE;
        selection = new ALayerSelection(this);
        plotter = new ALayerPlotter(this);
    }

    /**
     * easy constructor
     */
    public ALayer(int channels) {
        this();
        for (int i = 0; i < channels; i++)
            add(new AChannel());

        setStandardChannelNames();
    }

    /**
     * easy constructor
     */
    public ALayer(int channels, int samples) {
        this();
        for (int i = 0; i < channels; i++) {
            add(new AChannel(samples));
        }

        setStandardChannelNames();
    }

    /**
     * copy-constructor
     */
    public ALayer(ALayer l) {
        this();
        this.type = l.type;
        this.getLayerPlotter().setColor(l.getLayerPlotter().getColor());
        this.name = GLanguage.translate("copyOf") + " " + l.name;

        for (int i = 0; i < l.getNumberOfChannels(); i++) {
            add(new AChannel(l.getChannel(i)));
        }
    }

    private void setStandardChannelNames() {
        int channels = getNumberOfChannels();
        // default channel names...
        if (channels == 1) {
            getChannel(0).setName(GLanguage.translate("mono"));
        } else if (channels == 2) {
            getChannel(0).setName(GLanguage.translate("left"));
            getChannel(1).setName(GLanguage.translate("right"));
        }
    }

    /**
     * returns the clip which contains this layer
     */
    public AClip getParentClip() {
        return (AClip) getParent();
    }

    // add type to subelement...

    public void add(AModel e) {
        super.add(e);
        setType(type);
        ((AChannel) e).setPlotType(plotType);
    }

    public void insert(AModel e, int index) {
        super.insert(e, index);
        setType(type);
        ((AChannel) e).setPlotType(plotType);
    }

    public void replace(AModel e, int index) {
        super.replace(e, index);
        setType(type);
        ((AChannel) e).setPlotType(plotType);
    }

    /**
     * get view
     */
    public ALayerPlotter getLayerPlotter() {
        return (ALayerPlotter) getPlotter();
    }

    // plottype

    private int plotType;

    public static final int SAMPLE_CURVE_TYPE = 1;

    public static final int SPECTROGRAM_TYPE = 2;

    public void setPlotType(int type) {
        plotType = type;
        for (int i = 0; i < getNumberOfChannels(); i++) {
            getChannel(i).setPlotType(type);
        }
    }

    public int getPlotType() {
        return plotType;
    }

    private static int nameCounter;

    /**
     * set the default name of the track
     */
    public void setDefaultName() {
        setDefaultName("layer", nameCounter++);
    }

    // type
    private int type;

    public static final int AUDIO_LAYER = 1;

    public static final int PARAMETER_LAYER = 2;

    /**
     * set the layer type
     */
    public void setType(int t) {
        // type
        type = t;
    }

    /**
     * returns the layer type
     */
    public int getType() {
        return type;
    }

    // channel manipulation

    public AChannel getChannel(int index) {
        return (AChannel) get(index);
    }

    public void mergeDownChannel(int index) {
        // more than one channel ?
        if ((getNumberOfElements() > 1) && (index > 0)) {
            // mix
            AChannelSelection ch1 = getChannel(index - 1).createChannelSelection();
            AChannelSelection ch2 = getChannel(index).createChannelSelection();
            ALayerSelection l = new ALayerSelection(this);
            l.addChannelSelection(ch1);
            l.addChannelSelection(ch2);
            l.operateChannel0WithChannel1(new AOMix());
            // remove
            remove(index);
        }
    }

    public void mergeAllChannels() {
        while (getNumberOfElements() > 1) {
            mergeDownChannel(1);
        }
    }

    public AChannel getSelectedChannel() {
        return (AChannel) getSelected();
    }

    public int getNumberOfChannels() {
        return getNumberOfElements();
    }

    public AChannel getChannel(Point p) {
        try {
            return getChannel(getLayerPlotter().getInsideChannelIndex(p));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * return the biggest channel size of the whole clip
     */
    public int getMaxSampleLength() {
        int m = 0;
        for (int i = 0; i < getNumberOfElements(); i++) {
            int s = getChannel(i).getSampleLength();
            if (s > m)
                m = s;
        }
        return m;
    }

    /**
     * returns the biggest sample value (absolute value) of this layer
     */
    public float getMaxSampleValue() {
        float m = 0;
        for (int i = 0; i < getNumberOfElements(); i++) {
            float s = getChannel(i).getMaxSampleValue();
            if (s > m)
                m = s;
        }
        return m;
    }

    /**
     * returns the biggest sample value (absolute value) of this layer in the given range
     */
    public float getMaxSampleValue(int offset, int length) {
        float m = 0;
        for (int i = 0; i < getNumberOfElements(); i++) {
            float s = getChannel(i).getMaxSampleValue(offset, length);
            if (s > m)
                m = s;
        }
        return m;
    }

    /**
     * return true, if masking is used in one of the channels
     */
    public boolean isMaskEnabled() {
        for (int i = 0; i < getNumberOfChannels(); i++) {
            if (getChannel(i).isMaskEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * set a selection
     */
    public void setLayerSelection(ALayerSelection s) {
        int n = Math.min(getNumberOfChannels(), s.getNumberOfChannelSelections());

        // set bidirectional links...
        s.setLayer(this);
        for (int i = 0; i < n; i++) {
            getChannel(i).setChannelSelection(s.getChannelSelection(i));
            s.getChannelSelection(i).setChannel(getChannel(i));
        }
        selection = s;
    }

    /**
     * builds dynamically a layer-selection containing only selected channels
     */
    public ALayerSelection getLayerSelection() {
        ALayerSelection s = new ALayerSelection(this);
        for (int i = 0; i < getNumberOfChannels(); i++) {
            AChannelSelection chS = getChannel(i).getChannelSelection();
            if (chS.isSelected()) {
                s.addChannelSelection(chS);
            }
        }

        // if empty-selection then set FULL selection!!!
        if (!s.isSelected()) {
            return createLayerSelection();
        }

        return s;
    }

    /**
     * create a selection which selects the whole layer, and all channels
     */
    public ALayerSelection createLayerSelection() {
        ALayerSelection s = new ALayerSelection(this);
        for (int i = 0; i < getNumberOfChannels(); i++) {
            s.addChannelSelection(getChannel(i).createChannelSelection());
        }
        return s;
    }

    /**
     * modify all channel-selections
     */
    public void modifyLayerSelection(int offset, int length) {
        for (int i = 0; i < getNumberOfChannels(); i++) {
            getChannel(i).modifyChannelSelection(offset, length);
        }
    }

    /**
     * set the selection to select the full layer, and all channels
     */
    public void setFullLayerSelection() {
        ALayerSelection s = new ALayerSelection(this);
        for (int i = 0; i < getNumberOfChannels(); i++) {
            getChannel(i).setFullChannelSelection();
            s.addChannelSelection(getChannel(i).getChannelSelection());
        }
        selection = s;
    }

    /**
     * set the selection to select nothing of the layer
     */
    public void setEmptyLayerSelection() {
        ALayerSelection s = new ALayerSelection(this);
        for (int i = 0; i < getNumberOfChannels(); i++) {
            getChannel(i).setEmptyChannelSelection();
            s.addChannelSelection(getChannel(i).getChannelSelection());
        }
        selection = s;
    }

}
