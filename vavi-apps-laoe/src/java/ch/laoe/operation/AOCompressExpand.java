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

import ch.laoe.clip.AChannelSelection;


/**
 * Class: AOCompressExpand @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * compressor / expander
 * 
 * @version 22.04.01 first draft oli4 29.07.01 first really debugged version!!! oli4 13.09.01 add
 * variable transferfunction oli4
 */
public class AOCompressExpand extends AOperation {
    /**
     * fx and fy describes a transfer-function, they must begin with 0, and each position (x,y) is the entry- and exit value, e.g.
     * (30, 60) where this point doubles the sample-value.
     */
    public AOCompressExpand(int sampleRate, int tAttack, int tRelease, float[] fx, float[] fy) {
        super();
        this.sampleRate = sampleRate;
        this.tAttack = tAttack;
        this.tRelease = tRelease;
        this.fx = fx;
        this.fy = fy;
    }

    private int sampleRate;

    private int tAttack;

    private int tRelease;

    private float fx[];

    private float fy[];

    // actual amplitude

    private float meanAmplitude;

    private void initAmplitude() {
        meanAmplitude = 1;
    }

    private void updateAmplitude(float sample) {
        // attack ?
        sample = Math.abs(sample);
        if (sample > meanAmplitude) {
            meanAmplitude = AOToolkit.movingRmsAverage(meanAmplitude, sample, tAttack);
        }
        // release ?
        else {
            meanAmplitude = AOToolkit.movingRmsAverage(meanAmplitude, sample, tRelease);
        }
    }

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        initAmplitude();

        try {
            // compress/expand...
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = o1; i < o1 + l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / l1));
                updateAmplitude(s1[i]);

                float f = AOToolkit.interpolate1(fx, fy, meanAmplitude /** 0.707f */
                ) / meanAmplitude;
                s1[i] = ch1.mixIntensity(i, s1[i], s1[i] * f);
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));

            // zero cross
            AOToolkit.applyZeroCross(s1, o1);
            AOToolkit.applyZeroCross(s1, o1 + l1);
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }

    /*
     * x unit = 1, y unit = 1, neutral curve: value[index] = index
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float t[] = ch2.getChannel().sample;

        // mark changed channels...
        ch1.getChannel().changeId();

        initAmplitude();

        try {
            // compress/expand...
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = o1; i < o1 + l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / l1));
                updateAmplitude(s1[i]);

                float f = t[ch2.getChannel().limitIndex((int) meanAmplitude)] / meanAmplitude;
                s1[i] = ch1.mixIntensity(i, s1[i], s1[i] * f);
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));

            // zero cross
            AOToolkit.applyZeroCross(s1, o1);
            AOToolkit.applyZeroCross(s1, o1 + l1);
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }
}
