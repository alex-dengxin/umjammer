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
 * AOFft.
 * 
 * FFT
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * @version 02.08.01 first draft oli4
 */
public class AOFft extends AOperation {
    public AOFft() {
    }

    /**
     * performs FFT of the whole channel, all channels must have the same size!
     * ch1 is t-domain and will change to magnitude in f-domain ch2 is unused,
     * and will change to phase in f-domain
     */
    public final void operate(AChannelSelection ch1, AChannelSelection ch2) {
        // t-domain
        float td[] = ch1.getChannel().sample;

        // mark changed channels...
        ch1.getChannel().changeId();
        ch2.getChannel().changeId();

        try {
            // prepare f-domain data, with length of 2^n...
            int l = 1 << (int) Math.ceil(Math.log(td.length) / Math.log(2));
            // System.out.println("length of "+td.length+" resized to "+l);
            float re[] = new float[l];
            float im[] = new float[l];

            for (int i = 0; i < l; i++) {
                if (i < td.length) {
                    re[i] = td[i];
                    im[i] = 0;
                } else {
                    re[i] = 0;
                    im[i] = 0;
                }
            }

            // perform FFT...
            AOToolkit.complexFft(re, im);

            // prepare polar result and copy to channels...
            float mag[] = new float[l / 2];
            float phas[] = new float[l / 2];
            for (int i = 0; i < mag.length; i++) {
                mag[i] = AOToolkit.cartesianToMagnitude(re[i], im[i]);
                phas[i] = AOToolkit.cartesianToPhase(re[i], im[i]);
            }
            ch1.getChannel().sample = mag;
            ch2.getChannel().sample = phas;

        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
