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
 * multiplicates a selection n times
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 01.02.02 first draft oli4
 */
public class AOLoopableMultiplicate extends AOperation {
    public AOLoopableMultiplicate(int n) {
        super();
        this.n = n;
    }

    private int n;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        float tmp[] = new float[l1 * n + s1.length - l1];

        // mark changed channels...
        ch1.getChannel().changeId();

        // multiplicate
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < l1; j++) {
                tmp[o1 + i * l1 + j] = s1[o1 + j];
            }
        }

        // before
        for (int i = 0; i < o1; i++) {
            tmp[i] = s1[i];
        }

        // after
        for (int i = o1 + l1; i < s1.length; i++) {
            tmp[i + l1 * (n - 1)] = s1[i];
        }

        // zerocross
        for (int i = 0; i < n; i++) {
            AOToolkit.applyZeroCross(tmp, o1 + l1 * (n + 1));
        }

        // replace original
        ch1.getChannel().sample = tmp;

    }

}
