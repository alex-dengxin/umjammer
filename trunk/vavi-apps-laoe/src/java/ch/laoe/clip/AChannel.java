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

import ch.laoe.ui.GGraphicObjects;
import ch.laoe.ui.GLanguage;


/**
 * channel model.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 26.07.00 erster Entwurf oli4 <br>
 *          02.08.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          28.12.00 stream-based oli4 <br>
 *          02.01.01 separate operation and view stream oli4 <br>
 *          24.01.01 array-based again... oli4 <br> 
 *          02.03.02 change mask-concept to channel-mask oli4 <br> 
 *          02.07.02 add markers oli4
 */
public class AChannel extends AModel {
    /**
     * constructor
     */
    public AChannel() {
        super();
        changeId();
        audible = true;
        selection = new AChannelSelection(this);
        setPlotType(ALayer.SAMPLE_CURVE_TYPE);
        mask = new AChannelMask(this);
        marker = new AChannelMarker(this);
        createSamples(0.f, 10);

        graphicObjects = new GGraphicObjects();
        graphicObjects.setChannel(this);
    }

    /**
     * constructor
     */
    public AChannel(int length) {
        this();
        createSamples(0.f, length);
    }

    /**
     * constructor
     */
    public AChannel(AChannelSelection chs) {
        this();
        createSamples(0.f, chs.getLength());
        // copy samples from selection
        for (int i = 0; i < sample.length; i++) {
            sample[i] = chs.getChannel().sample[chs.getOffset() + i];
        }
    }

    /**
     * copy-constructor
     */
    public AChannel(AChannel ch) {
        this(ch.getSampleLength());
        this.name = GLanguage.translate("copyOf") + " " + ch.name;
        // copy samples
        for (int i = 0; i < sample.length; i++) {
            sample[i] = ch.sample[i];
        }
        // copy selection
        selection = new AChannelSelection(ch.getChannelSelection());
        ((AChannelSelection) selection).setChannel(this);
        // copy markers
        marker = new AChannelMarker(ch.marker);
    }

    /**
     * returns the layer which contains this channel
     */
    public ALayer getParentLayer() {
        return (ALayer) getParent();
    }

    /**
     * returns the clip which contains this channel
     */
    public AClip getParentClip() {
        return (AClip) getParent().getParent();
    }

    /**
     * unique channel-ID: this ID is not used by the clip.
     */
    private static int uniqueIdIndex = 0;

    private String uniqueId;

    /**
     * change the unique channel-ID, e.g. to give a new reference, when data have been changed (history-tracing... etc.).
     */
    public void changeId() {
        synchronized (this) {
            uniqueIdIndex++;
        }
        uniqueId = "channel" + uniqueIdIndex;
    }

    /**
     * get the current channel-ID
     */
    public String getId() {
        return uniqueId;
    }

    /**
     * set any new channel-ID
     */
    public void setId(String id) {
        uniqueId = id;
    }

    // *************** sample access *****************

    /*
     * samples (will be private and hopefully serialized in file instead of RAM-residing array one day!!!)
     */
    public float sample[];

    /**
     * create a given number of samples
     */
    public void createSamples(int length) {
        sample = new float[length];
    }

    /**
     * create a given number of samples
     */
    public void createSamples(float value, int length) {
        createSamples(length);

        for (int i = 0; i < length; i++)
            sample[i] = value;
    }

    /**
     * get the sample at index, with limitating the index.
     */
    public float getSample(int index) {
        if (isValidIndex(index)) {
            return sample[index];
        } else {
            return 0;
        }
    }

    /**
     * get the sample at index, with volume-mask considered.
     */
    public float getMaskedSample(int index) {
        if (isValidIndex(index) && isAudible()) {
            if (mask.isEnabled()) {
                return sample[index] * mask.getSample(index);
            } else {
                return sample[index];
            }
        } else {
            return 0;
        }
    }

    /**
     * set the sample at index, with limitating the index
     */
    public void setSample(float value, int index) {
        if (isValidIndex(index)) {
            sample[index] = value;
        }
    }

    /*
     * limit the sample-index within the valid x-range
     */
    public int limitIndex(int index) {
        if (index < 0) {
            return 0;
        } else if (index >= sample.length) {
            return sample.length - 1;
        } else {
            return index;
        }
    }

    /*
     * returns true if the index is inside the valid x-range
     */
    public boolean isValidIndex(int index) {
        if (index < 0) {
            return false;
        } else if (index >= sample.length) {
            return false;
        }
        return true;
    }

    public void setPlotType(int type) {
        switch (type) {
        case ALayer.SAMPLE_CURVE_TYPE:
            plotter = new AChannelPlotterSampleCurve(this, getChannelPlotter());
            break;

        case ALayer.SPECTROGRAM_TYPE:
            plotter = new AChannelPlotterSpectrogram(this, getChannelPlotter());
            break;
        }
    }

    /**
     * get plotter
     */
    public AChannelPlotter getChannelPlotter() {
        return (AChannelPlotter) getPlotter();
    }

    private static int nameCounter;

    /**
     * set the default name of the layer
     */
    public void setDefaultName() {
        setDefaultName("channel", nameCounter++);
    }

    // audible
    private boolean audible;

    /**
     * set audible
     */
    public void setAudible(boolean a) {
        audible = a;
    }

    /**
     * returns audible
     */
    public boolean isAudible() {
        return audible;
    }

    /**
     * return the sample length
     */
    public int getSampleLength() {
        return sample.length;
    }

    /**
     * return the maximum absolute sample value
     */
    public float getMaxSampleValue() {
        return getMaxSampleValue(0, getSampleLength());
    }

    /**
     * return the maximum absolute sample value in the given range
     */
    public float getMaxSampleValue(int offset, int length) {
        float m = 0.f;
        int start = limitIndex(offset);
        int end = limitIndex(offset + length);
        for (int i = start; i < end; i++) {
            float s = Math.abs(sample[i]);
            // bigger ?
            if (s > m)
                m = s;
        }
        return m;
    }

    private AChannelMask mask;

    /**
     * return the mask of this channel
     */
    public AChannelMask getMask() {
        return mask;
    }

    /**
     * return true, if masking is used (if mask-points are defined)
     */
    public boolean isMaskEnabled() {
        return mask.isEnabled();
    }

    private AChannelMarker marker;

    /**
     * return the markers of this channel
     */
    public AChannelMarker getMarker() {
        return marker;
    }

    public void setMarker(AChannelMarker m) {
        marker = m;
        marker.setChannel(this);
    }

    /**
     * set a selection
     */
    public void setChannelSelection(AChannelSelection s) {
        selection = s;
    }

    /**
     * modify a selection
     */
    public void modifyChannelSelection(int offset, int length) {
        // modify ?
        if (selection != null) {
            ((AChannelSelection) selection).setOffset(offset);
            ((AChannelSelection) selection).setLength(length);
        }
        // create ?
        else {
            selection = new AChannelSelection(this, offset, length);
        }
    }

    /**
     * get a Selection
     */
    public AChannelSelection getChannelSelection() {
        return (AChannelSelection) selection;
    }

    /**
     * get a Selection, return full selection if not selected
     */
    public AChannelSelection getNonEmptyChannelSelection() {
        if (!selection.isSelected()) {
            return createChannelSelection();
        } else {
            return getChannelSelection();
        }
    }

    /**
     * create an independent selection which selects the whole channel
     */
    public AChannelSelection createChannelSelection() {
        AChannelSelection s = new AChannelSelection(this, 0, getSampleLength());
        return s;
    }

    /**
     * set the selection to select the full channel
     */
    public void setFullChannelSelection() {
        setChannelSelection(createChannelSelection());
    }

    /**
     * set the selection to select the range from left to right marker relative to x.
     */
    public void setMarkedChannelSelection(int x) {
        AChannelMarker m = getMarker();
        int o = m.searchLeftMarker(x);
        int l = m.searchRightMarker(x) - o;
        modifyChannelSelection(o, l);
    }

    /**
     * set the selection to select nothing of the channel
     */
    public void setEmptyChannelSelection() {
        setChannelSelection(new AChannelSelection(this, 0, 0));
    }

    // ************** graphic objects ***************

    private GGraphicObjects graphicObjects;

    public GGraphicObjects getGraphicObjects() {
        return graphicObjects;
    }

}
