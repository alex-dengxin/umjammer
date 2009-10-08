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
 * convolution operation
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 24.06.01 first draft oli4 <br>
 *          17.09.01 layer-kernel added oli4 <br>
 */
public class AOConvolution extends AOperation {
    public AOConvolution(float[] kernel) {
        this.kernel = kernel;
    }

    public AOConvolution() {
    }

    private float[] kernel;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float tmp[] = new float[l1];

        // mark changed channels...
        ch1.getChannel().changeId();

        // convolve...
        progressSupport.entrySubProgress(new ProgressEvent(this));
        for (int i = 0; i < l1; i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
            tmp[i] = AOToolkit.convolve(s1, i + o1, kernel, kernel.length);
        }
        progressSupport.exitSubProgress(new ProgressEvent(this));

        // copy
        for (int i = 0; i < l1; i++) {
            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], tmp[i]);
        }

        // zero cross
        AOToolkit.applyZeroCross(s1, o1);
        AOToolkit.applyZeroCross(s1, o1 + l1);
    }

    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        kernel = ch2.getChannel().sample;
        operate(ch1);
    }

}
