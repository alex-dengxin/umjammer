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


/*********************************************************************************************************************************
 * 
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with LAoE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * Class: AOBandPass @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * IIR band pass filter
 * 
 * @version 09.05.01 first draft oli4
 * 
 */
public class AOBandPass extends AOperation {
    public AOBandPass(float dry, float wet, float freq, float q) {
        super();
        this.dry = dry;
        this.wet = wet;
        this.freq = freq;
        this.q = q;
    }

    private float dry;

    private float wet;

    private float freq;

    private float q;

    /**
     * performs a constant amplification
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float oldRms = AOToolkit.rmsAverage(s1, o1, l1);

        float tmp[] = new float[l1];

        // mark changed channels...
        ch1.getChannel().changeId();

        AOToolkit.addIirBandPass(s1, tmp, o1, l1, freq, q, wet);

        // RMS-calibration
        float newRms = AOToolkit.rmsAverage(tmp, 0, l1);
        AOToolkit.multiply(tmp, 0, l1, oldRms / newRms);

        // mix dry and wet
        for (int i = 0; i < l1; i++) {
            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], s1[i + o1] * dry + tmp[i]);
        }

        // zero cross
        AOToolkit.applyZeroCross(s1, o1);
        AOToolkit.applyZeroCross(s1, o1 + l1);
    }
}
