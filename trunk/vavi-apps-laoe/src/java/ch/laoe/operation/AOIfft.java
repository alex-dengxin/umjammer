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
import ch.laoe.ui.Debug;


/**
 * IFFT
 * 
 * @target JDK 1.3
 * @author olivier gäumann, neuchâtel (switzerland)
 * @version 02.08.01 first draft oli4
 */
public class AOIfft extends AOperation {
    public AOIfft() {
    }

    /**
     * performs FFT of the whole channel, all channels must have the same size!
     * ch1 is f-domain magnitude and will change to t-domain ch2 is f-domain phase
     * and will change to unused
     */
    public final void operate(AChannelSelection ch1, AChannelSelection ch2) {
        // f-domain...
        float mag[] = ch1.getChannel().sample;
        float phas[] = ch2.getChannel().sample;

        // mark changed channels...
        ch1.getChannel().changeId();

        // prepare t-domain data with length of 2^n...
        int l = 1 << (int) Math.ceil(Math.log(mag.length) / Math.log(2));
        // System.out.println("length of "+mag.length+" resized to "+l);
        float re[] = new float[2 * l];
        float im[] = new float[2 * l];

        try {
            // to cartesian system...
            for (int i = 0; i < l; i++) {
                float m = 0;
                float p = 0;
                if (i < mag.length) {
                    m = mag[i];
                }
                if (i < phas.length) {
                    p = phas[i];
                }

                re[i] = AOToolkit.polarToX(m, p);
                im[i] = AOToolkit.polarToY(m, p);
            }

            // perform IFFT...
            AOToolkit.complexIfft(re, im);

            // copy result t-domain
            ch1.getChannel().sample = re;

        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
