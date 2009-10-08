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
 * equalizer
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 30.04.01 first draft oli4
 */
public class AOEqualizer extends AOperation {
    public AOEqualizer(float[] freq, float[] gain, float q) {
        super();
        this.freq = freq;
        this.gain = gain;
        this.q = q;
    }

    private float freq[];

    private float gain[];

    private float q;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float oldRms = AOToolkit.rmsAverage(s1, o1, l1);

        float tmp[] = new float[l1];

        // mark changed channels...
        ch1.getChannel().changeId();

        // each band...
        progressSupport.entrySubProgress(new ProgressEvent(this));
        for (int i = 0; i < freq.length; i++) {
            if (freq[i] < 0.5) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / freq.length));
                AOToolkit.addIirBandPass(s1, tmp, o1, l1, freq[i], q, gain[i]);
            }
        }
        progressSupport.exitSubProgress(new ProgressEvent(this));

        // RMS-calibration
        float newRms = AOToolkit.rmsAverage(tmp, 0, l1);
        AOToolkit.multiply(tmp, 0, l1, (oldRms / newRms));

        // replace with filtered...
        for (int i = 0; i < l1; i++) {
            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], tmp[i]);
        }

        // zero cross
        AOToolkit.applyZeroCross(s1, o1);
        AOToolkit.applyZeroCross(s1, o1 + l1);
    }
}
