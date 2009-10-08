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
 * makes a selection loopable, different modes are possible: -
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 14.05.01 first draft oli4
 *          21.06.01 add loopcount oli4
 *          15.09.01 completely redefined oli4
 */
public class AOLoopable extends AOperation {
    public AOLoopable(int order) {
        super();
        this.order = order;
    }

    private int order;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        float tmp[] = new float[l1];

        // mark changed channels...
        ch1.getChannel().changeId();

        // copy center
        for (int i = 0; i < l1; i++) {
            tmp[i] = s1[i + o1];
        }

        float oldRms = AOToolkit.rmsAverage(tmp, 0, tmp.length);

        // fade in left part
        AChannelSelection ch2 = new AChannelSelection(ch1.getChannel(), 0, o1);
        ch2.operateChannel(new AOFade(AOFade.IN, order, 0, false));

        // fade out right part
        AChannelSelection ch3 = new AChannelSelection(ch1.getChannel(), o1 + l1, s1.length - o1 - l1);
        ch3.operateChannel(new AOFade(AOFade.OUT, order, 0, false));

        // copy left part
        for (int i = 0; i < l1; i++) {
            if (o1 - l1 + i >= 0) {
                tmp[i] += s1[o1 - l1 + i];
            }
        }

        // copy right part
        for (int i = 0; i < l1; i++) {
            if (o1 + l1 + i < s1.length) {
                tmp[i] += s1[o1 + l1 + i];
            }
        }

        // RMS-calibration
        float newRms = AOToolkit.rmsAverage(tmp, 0, tmp.length);
        AOToolkit.multiply(tmp, 0, tmp.length, (oldRms / newRms));

        // replace old samples with new samples
        ch1.getChannel().sample = tmp;

    }

}
