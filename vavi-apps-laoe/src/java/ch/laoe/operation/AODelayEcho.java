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

package ch.laoe.operation;

import java.util.ArrayList;
import java.util.List;

import ch.laoe.clip.AChannelSelection;


/**
 * Class: AODelayEcho @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * delay and echo without feedback and finite number of echoes.
 * 
 * @version 29.07.00 erster Entwurf oli4 03.08.00 neuer Stil oli4 02.11.00 neuer Stil oli4 19.12.00
 * float audio samples oli4 07.05.01 add dry and wet oli4
 */
public class AODelayEcho extends AOperation {
    /**
     * @param delayShape unit = delay
     * @param gainShape unit = gain
     * @param delay
     * @param gain
     * @param dry
     * @param wet
     */
    public AODelayEcho(float delayShape[], float gainShape[], float delay, float gain, float dry, float wet) {
        super();
        this.delayShape = delayShape;
        this.gainShape = gainShape;
        this.delay = delay;
        this.gain = gain;
        this.dry = dry;
        this.wet = wet;
    }

    public AODelayEcho(float dry, float wet) {
        super();
        this.dry = dry;
        this.wet = wet;
    }

    // parameters
    private float delayShape[];

    private float gainShape[];

    private float delay, gain;

    private float dry, wet;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // wet part
        float tmp[] = new float[l1];
        float oldRms = AOToolkit.rmsAverage(s1, o1, l1);

        // mark changed channels...
        ch1.getChannel().changeId();

        // each point...
        progressSupport.entrySubProgress(new ProgressEvent(this));
        for (int i = 0; i < l1; i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
            // all delays...
            for (int j = 0; j < delayShape.length; j++) {
                int d = (int) (delay * delayShape[j]);
                float g = gain * gainShape[j];
                // range-check
                if (i + d < l1) {
                    tmp[i + d] += s1[i + o1] * g;
                }
            }
        }
        progressSupport.exitSubProgress(new ProgressEvent(this));

        // RMS-calibration
        float newRms = AOToolkit.rmsAverage(tmp, 0, l1);
        AOToolkit.multiply(tmp, 0, l1, (oldRms / newRms));

        // mix dry and wet
        for (int i = 0; i < l1; i++) {
            float s = dry * s1[i + o1] + wet * tmp[i];
            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], s);
        }

        // zero cross
        AOToolkit.applyZeroCross(s1, o1);
        AOToolkit.applyZeroCross(s1, o1 + l1);
    }

    /**
     * multi-echo using a user-defined room
     * 
     * @param ch1 samples
     * @param ch2 room
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        float sample[] = ch1.getChannel().sample;
        float room[] = ch2.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float tmp[] = new float[l1];
        float oldRms = AOToolkit.rmsAverage(sample, o1, l1);

        // mark changed channels...
        ch1.getChannel().changeId();

        // prepare room
        class Echo {
            public Echo(int d, float g) {
                delay = d;
                gain = g;
            }

            public int delay;

            public float gain;
        }

        List<Echo> echoList = new ArrayList<Echo>(333);
        for (int i = 0; i < room.length; i++) {
            if (room[i] != 0) {
                echoList.add(new Echo(i, room[i]));
            }
        }

        try {
            // each point...
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = 0; i < l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                // all delays...
                for (int j = 0; j < echoList.size(); j++) {
                    int d = echoList.get(j).delay;
                    float g = echoList.get(j).gain;

                    // range-check
                    if (i + d < l1) {
                        tmp[i + d] += sample[i + o1] * g;
                    }
                }
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));

            // RMS-calibration
            float newRms = AOToolkit.rmsAverage(tmp, 0, l1);
            AOToolkit.multiply(tmp, 0, l1, (oldRms / newRms));

            // mix dry and wet
            for (int i = 0; i < l1; i++) {
                float s = dry * sample[i + o1] + wet * tmp[i];
                sample[i + o1] = ch1.mixIntensity(i + o1, sample[i + o1], s);
            }

            // zero cross
            AOToolkit.applyZeroCross(sample, o1);
            AOToolkit.applyZeroCross(sample, o1 + l1);
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }

}
