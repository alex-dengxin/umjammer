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
 * Class: AOClippReparing @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * repairs clipping, tries to reproduce original curve, but without performing normalization.
 * 
 * @version 08.07.02 first draft oli4
 */
public class AOClippReparing extends AOperation {
    /**
     * @param maxWidth: maximum clipp width which will be repaired
     * @param minDerivation: minimum derivation which identifies a clipping border, unit in [2^samplewidth], 0.1 .. 1
     */
    public AOClippReparing(int maxWidth, float minDerivationFactor) {
        super();
        this.maxWidth = maxWidth;
        this.minDerivationFactor = minDerivationFactor;
    }

    // parameters
    private float minDerivationFactor;

    private int maxWidth;

    /**
     * performs a constant amplification
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        int sampleWidth = 1 << ch1.getChannel().getParentClip().getSampleWidth();
        int minDerivation = (int) (minDerivationFactor * sampleWidth);

        try {
            progressSupport.entrySubProgress(new ProgressEvent(this));

            for (int i = 1; i < l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                // derivation
                float dBegin = s1[o1 + i] - s1[o1 + i - 1];
                float dEnd;

                // clipp-begin ?
                if (Math.abs(dBegin) > minDerivation) {
                    // search clipp-end...
                    for (int j = 1; j < maxWidth; j++) {
                        dEnd = s1[o1 + i + j] - s1[o1 + i + j - 1];

                        // clipp end ?
                        if (Math.abs(dEnd) > minDerivation) {
                            // System.out.println("aaaahhh !"+dBegin+" "+dEnd);
                            // negative clipp ?
                            if ((dBegin > 0) && (dEnd < 0)) {
                                // System.out.println("negative clipp fond at"+i+".."+(i+j));
                                // remove negative clipping...
                                for (int k = 0; k < j; k++) {
                                    s1[o1 + i + k] -= sampleWidth;
                                }
                            }
                            // positive clipp ?
                            else if ((dBegin < 0) && (dEnd > 0)) {
                                // System.out.println("positive clipp fond at"+i+".."+(i+j));
                                // remove positive clipping...
                                for (int k = 0; k < j; k++) {
                                    s1[o1 + i + k] += sampleWidth;
                                }
                            }
                            i += j + 1;
                            break;
                        }
                    }
                }
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
