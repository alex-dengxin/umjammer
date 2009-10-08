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
 * generate harmonics signal with superposed sinus.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 30.05.01 first draft oli4
 */
public class AOHarmonicsGenerator extends AOperation {
    /**
     * constructor
     */
    public AOHarmonicsGenerator(float[] amplitude, float offset, int basePeriod, boolean add) {
        super();
        this.amplitude = amplitude;
        this.offset = offset;
        this.basePeriod = basePeriod;
        this.add = add;
    }

    // parameters
    private float amplitude[];

    private float offset;

    private int basePeriod;

    private boolean add;

    /**
     * performs the segments generation
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        // build one period...
        float tmp[] = new float[basePeriod];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = offset;
            for (int j = 0; j < amplitude.length; j++) {
                tmp[i] += amplitude[j] * (float) Math.sin((double) i / (double) basePeriod * 2 * Math.PI * (j + 1));
            }
        }

        // map periodically to sample...
        for (int i = 0; i < l1; i++) {
            float s;
            progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
            if (add) {
                s = s1[i + o1] + tmp[i % tmp.length];
            } else {
                s = tmp[i % tmp.length];
            }
            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], s);
        }
    }
}
