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
 * general insertion, null-samples, another channel
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 04.08.00 erster Entwurf oli4 <br>
 *          16.10.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          24.01.01 array-based again... oli4 <br>
 */
public class AOInsert extends AOperation {
    public AOInsert() {
        super();
        insertedLength = 0;
    }

    /**
     * inserts 0's ("length" times)
     */
    public AOInsert(int length) {
        super();
        insertedLength = length;
    }

    private int insertedLength;

    /**
     * performs a 0-samples insertion
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
//      int l1 = ch1.getLength();

        float tmp[] = new float[s1.length + insertedLength];

        // mark changed channels...
        ch1.getChannel().changeId();

        // before
        for (int i = 0; i < o1; i++)
            tmp[i] = s1[i];

        // insertion
        for (int i = o1; i < o1 + insertedLength; i++) {
            tmp[i] = 0;
        }

        // after
        for (int i = o1 + insertedLength; i < tmp.length; i++)
            tmp[i] = s1[i - insertedLength];

        // zero cross
        AOToolkit.applyZeroCross(tmp, o1);
        AOToolkit.applyZeroCross(tmp, o1 + insertedLength);

        // replace original
        ch1.getChannel().sample = tmp;
    }

    /**
     * performs an insertion of channel2 to channel1
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        float s1[] = ch1.getChannel().sample;
        float s2[] = ch2.getChannel().sample;
        int o1 = ch1.getOffset();
//      int l1 = ch1.getLength();
        int o2 = ch2.getOffset();
        int l2 = ch2.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        // insertion length is l2
        float tmp[] = new float[s1.length + l2];

        // before
        for (int i = 0; i < o1; i++)
            tmp[i] = s1[i];

        // insertion
        for (int i = 0; i < l2; i++) {
            tmp[o1 + i] = s2[o2 + i];
        }

        // after
        for (int i = o1 + l2; i < tmp.length; i++)
            tmp[i] = s1[i - l2];

        // zero cross
        AOToolkit.applyZeroCross(tmp, o1);
        AOToolkit.applyZeroCross(tmp, o1 + l2);

        // replace original
        ch1.getChannel().sample = tmp;
    }
}
