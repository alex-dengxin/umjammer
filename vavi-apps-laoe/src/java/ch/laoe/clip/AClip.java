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

import java.io.File;

import ch.laoe.audio.Audio;
import ch.laoe.audio.AudioException;
import ch.laoe.operation.AOAmplify;
import ch.laoe.operation.AOMix;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GPersistance;
import ch.laoe.ui.GProgressViewer;


/**
 * clip model.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 04.08.00 erster Entwurf oli4 <br>
 *          10.11.00 attach undostack to AClip oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          28.12.00 stream-based oli4 <br>
 *          24.01.01 array-based again... oli4 <br>
 *          02.03.02 channel mask introduced oli4
 */
public class AClip extends AContainerModel {
    /**
     * default constructor
     */
    public AClip() {
        super();
        changeId();
        sampleRate = DEFAULT_SAMPLE_RATE;
        sampleWidth = DEFAULT_SAMPLE_WIDTH;
        comments = GPersistance.createPersistance().getString("clip.defaultComment");
        selection = new AClipSelection(this);
        plotter = new AClipPlotter(this);
        // audio = new Audio(this);
        // history = new AClipHistory(this);
    }

    /**
     * easy constructor
     */
    public AClip(int layers, int channels) {
        this();
        for (int i = 0; i < layers; i++)
            add(new ALayer(channels));

        audio = new Audio(this);
        history = new AClipHistory(this);
        history.store(GLanguage.translate("initialState"));
    }

    /**
     * easy constructor
     */
    public AClip(int layers, int channels, int samples) {
        this();
        for (int i = 0; i < layers; i++)
            add(new ALayer(channels, samples));

        audio = new Audio(this);
        history = new AClipHistory(this);
        history.store(GLanguage.translate("initialState"));
    }

    /**
     * copy-constructor
     */
    public AClip(AClip c) {
        this();
        this.sampleRate = c.sampleRate;
        this.sampleWidth = c.sampleWidth;
        this.comments = c.comments;
        this.name = c.name;
        this.selection = c.selection;

        for (int i = 0; i < c.getNumberOfLayers(); i++) {
            add(new ALayer(c.getLayer(i)));
        }

        audio = new Audio(this);
        history = new AClipHistory(c.history, this);
    }

    /**
     * open file constructor
     */
    public AClip(File fileName) throws AudioException {
        this();
        GProgressViewer.entrySubProgress();
        GProgressViewer.setProgress(10);
        audio = new Audio(this);
        GProgressViewer.setProgress(90);
        audio.open(fileName);
        GProgressViewer.setProgress(100);
        history = new AClipHistory(this);
        history.store(GLanguage.translate("initialState"));
        GProgressViewer.exitSubProgress();
    }

    /**
     * destroys the clip.
     */
    public void destroy() {
        // System.out.println("free mem = "+Runtime.getRuntime().freeMemory());
        removeAll();
        audio.destroy();
        System.gc();
        // System.out.println("free mem = "+Runtime.getRuntime().freeMemory());
    }

    /**
     * copies all attributes except the samples
     * 
     * @param c
     */
    public void copyAllAttributes(AClip c) {
        this.sampleRate = c.sampleRate;
        this.sampleWidth = c.sampleWidth;
        this.comments = c.comments;
        this.name = c.name;
        this.selection = c.selection;
    }

    private Audio audio;

    public Audio getAudio() {
        return audio;
    }

    /**
     * unique clip-ID: this ID is not used by the clip.
     */
    private static int uniqueIdIndex = 0;

    private String uniqueId;

    /**
     * change the unique clip-ID, e.g. to give a new reference, when data have been changed (history-tracing... etc.).
     */
    public void changeId() {
        synchronized (this) {
            uniqueIdIndex++;
        }
        uniqueId = "clip" + uniqueIdIndex;
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

    private AClipHistory history;

    /**
     * get history
     */
    public AClipHistory getHistory() {
        return history;
    }

    /**
     * get selection
     */
    public AClipSelection getClipSelection() {
        return (AClipSelection) selection;
    }

    /**
     * get view
     */
    public AClipPlotter getClipPlotter() {
        return (AClipPlotter) getPlotter();
    }

    public void setPlotType(int type) {
        for (int i = 0; i < getNumberOfLayers(); i++) {
            getLayer(i).setPlotType(type);
        }
    }

    private static int nameCounter;

    /**
     * set the default name of the layer
     */
    public void setDefaultName() {
        setDefaultName("clip", nameCounter++);
    }

    // comment
    private String comments;

    /**
     * get the comment of this clip
     */
    public String getComments() {
        return comments;
    }

    /**
     * set the comment of this clip
     */
    public void setComments(String n) {
        comments = n;
    }

    // layer manipulation

    public ALayer getLayer(int index) {
        return (ALayer) get(index);
    }

    public void mergeDownLayer(int index) {
        // more than one layer ?
        if ((getNumberOfElements() > 1) && (index > 0)) {
            ALayer l0 = getLayer(index - 1);
            ALayer l1 = getLayer(index);

            // make selections
            ALayerSelection s0 = l0.createLayerSelection();
            ALayerSelection s1 = l1.createLayerSelection();
            AClipSelection c = new AClipSelection(this);
            c.addLayerSelection(s0);
            c.addLayerSelection(s1);
            // mix
            c.operateLayer0WithLayer1(new AOMix());
            remove(index);
            // clear masks
            for (int i = 0; i < l0.getNumberOfChannels(); i++) {
                l0.getChannel(i).getMask().clear();
            }
        }
    }

    public void mergeAllLayers() {
        while (getNumberOfElements() > 1) {
            mergeDownLayer(1);
        }
    }

    public ALayer getSelectedLayer() {
        return (ALayer) getSelected();
    }

    public int getNumberOfLayers() {
        return getNumberOfElements();
    }

    public int getMaxNumberOfChannels() {
        return getMaxNumberOfSubElements();
    }

    /**
     * return the biggest channel size of the whole clip
     */
    public int getMaxSampleLength() {
        int m = 0;
        for (int i = 0; i < getNumberOfElements(); i++) {
            int s = getLayer(i).getMaxSampleLength();
            if (s > m)
                m = s;
        }
        return m;
    }

    /**
     * returns the biggest sample value (absolute value) of this clip
     */
    public float getMaxSampleValue() {
        float m = 0;
        for (int i = 0; i < getNumberOfElements(); i++) {
            float s = getLayer(i).getMaxSampleValue();
            if (s > m)
                m = s;
        }
        return m;
    }

    /**
     * returns the biggest sample value (absolute value) of this clip in the given x-range
     */
    public float getMaxSampleValue(int offset, int length) {
        float m = 0;
        for (int i = 0; i < getNumberOfElements(); i++) {
            float s = getLayer(i).getMaxSampleValue(offset, length);
            if (s > m)
                m = s;
        }
        return m;
    }

    /**
     * create a flattened layer, respecting layer superposing, layer types. if the given layer has the required size, no new
     * memory needs to be allocated. masking and audible is considered. a maximum of optimisations has been introduced here, to as
     * fast as possible and memory-safe when possible.
     */
    public ALayer createFlattenedLayer(ALayer layer) {
        try {
            // check if single-audiolayer...
            int audioLayerCounter = 0;
            int firstAudioLayerIndex = 0;
            for (int i = 0; i < getNumberOfLayers(); i++) {
                if (getLayer(i).getType() == ALayer.AUDIO_LAYER) {
                    audioLayerCounter++;
                    firstAudioLayerIndex = i;
                }
            }

            // check if single-audiolayer and maskless...
            if (audioLayerCounter == 1) {
                if (!getLayer(firstAudioLayerIndex).isMaskEnabled()) {
                    // if so, then return the original channels instead of costly creating them...
                    ALayer ol = getLayer(firstAudioLayerIndex);
                    layer = new ALayer();
                    for (int i = 0; i < ol.getNumberOfChannels(); i++) {
                        if (ol.getChannel(i).isAudible()) {
                            layer.link(ol.getChannel(i)); // don't touch the original!
                        } else {
                            layer.add(new AChannel());
                        }
                        layer.createLayerSelection();
                    }
                    return layer;
                }
            }

            // what about the given layer ?
            if ((layer == null) || (layer.getMaxSampleLength() != getMaxSampleLength()) || (layer.getNumberOfChannels() != getMaxNumberOfChannels()) || (contains(layer))) {
                // so create a new one...
                layer = new ALayer(getMaxNumberOfChannels(), getMaxSampleLength());
            }

            // create selection
            ALayerSelection ls = layer.createLayerSelection();
            // clear all data
            ls.operateEachChannel(new AOAmplify(0.f));

            // each layer...
            for (int i = 0; i < getNumberOfLayers(); i++) {
                ALayer l = getLayer(i);
                // create selection
                ALayerSelection ps = l.createLayerSelection();
                AClipSelection cs = new AClipSelection(this);
                cs.addLayerSelection(ls);
                cs.addLayerSelection(ps);

                // audio layer ?
                if (l.getType() == ALayer.AUDIO_LAYER) {
                    cs.operateLayer0WithLayer1(new AOMix());
                }
            }
        } catch (Exception e) {
            Debug.printStackTrace(5, e);
        }

        return layer;
    }

    // sample rate of the layer

    private float sampleRate;

    private final static float DEFAULT_SAMPLE_RATE = 8000.f;

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float s) {
        if (s > 48000.f)
            sampleRate = 48000.f;
        else if (s < 1000.f)
            sampleRate = 1000.f;
        else
            sampleRate = s;

        audio.changeSampleRate(s);
    }

    // sample width
    private int sampleWidth;

    private final static int DEFAULT_SAMPLE_WIDTH = 16;

    public int getSampleWidth() {
        return sampleWidth;
    }

    public void setSampleWidth(int sw) {
        if (sw < 1)
            sampleWidth = 1;
        else if (sw > 32)
            sampleWidth = 32;
        else
            sampleWidth = sw;
    }

    // big endian
    private boolean bigEndian;

    public boolean isBigEndian() {
        return false;
    }

    public void setBigEndian(boolean b) {
        // some comments from moustique: iztitlot.t..lovggggg8 6¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢
        // ¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢
        // ¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢¢
        // ¢¢¢¢¢¢88b g c
        bigEndian = b;
    }

}
