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
 * Class: AOCanon @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * folds the selection in two (or more), like a canon-song
 * 
 * @version 15.09.01 first draft oli4
 * 
 */
public class AOCanon extends AOperation {
    public AOCanon(int voices) {
        super();
        this.voices = voices;
    }

    private int voices;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        int l2 = l1 / voices;
        float tmp[] = new float[s1.length - l1 + l2];

        // mark changed channels...
        ch1.getChannel().changeId();

        float oldRms = AOToolkit.rmsAverage(s1, o1, l1);

        // copy before
        for (int i = 0; i < o1; i++) {
            tmp[i] = s1[i];
        }

        // canonial part
        for (int i = o1; i < l1; i++) {
            tmp[o1 + (i % l2)] += s1[i];
        }

        // copy after
        for (int i = o1 + l1; i < s1.length; i++) {
            tmp[i + l2 - l1] = s1[i];
        }

        // RMS-calibration
        float newRms = AOToolkit.rmsAverage(tmp, o1, l2);
        AOToolkit.multiply(tmp, 0, tmp.length, oldRms / newRms);

        // zero cross
        AOToolkit.applyZeroCross(tmp, o1);
        AOToolkit.applyZeroCross(tmp, o1 + l2);

        // replace old samples with new samples
        ch1.getChannel().sample = tmp;

    }

}
