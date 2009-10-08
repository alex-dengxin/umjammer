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
 * general amplifier
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 26.07.00 erster Entwurf oli4 <br>
 *          03.08.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          24.01.01 array-based again... oli4 <br>
 */
public class AOAmplify extends AOperation {
    public AOAmplify() {
        super();
        constAmplification = 1.f;
    }

    public AOAmplify(float amplification) {
        super();
        constAmplification = amplification;
    }

    private float constAmplification;

    /**
     * performs a constant amplification
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        try {
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = o1; i < (o1 + l1); i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / l1));
                s1[i] = ch1.mixIntensity(i, s1[i], constAmplification * s1[i]);
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));

            // zero cross
            AOToolkit.applyZeroCross(s1, o1);
            AOToolkit.applyZeroCross(s1, o1 + l1);
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }

    /**
     * performs a variable amplification, where the variable is channel2
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        float s1[] = ch1.getChannel().sample;
        float s2[] = ch2.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
//      int o2 = ch2.getOffset();
//      int l2 = ch2.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        try {
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = o1; i < o1 + l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / l1));
                s1[i] = ch1.mixIntensity(i, s1[i], s2[i] * s1[i]);
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));

            // zero cross
            AOToolkit.applyZeroCross(s1, o1);
            AOToolkit.applyZeroCross(s1, o1 + l1);
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }
}
