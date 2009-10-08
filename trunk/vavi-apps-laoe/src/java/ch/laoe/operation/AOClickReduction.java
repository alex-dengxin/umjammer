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
 * AOClickReduction
 * 
 * click reduction filter, applying zero-cross.
 * 
 * @target JDK 1.3
 * @author olivier gäumann, neuchâtel (switzerland)
 * @version 05.06.01 first draft oli4
 */
public class AOClickReduction extends AOperation {
    /**
     * @param sense: limit of stepsize, at which smoothing is performed
     * @param smooth: width of zero-cross
     */
    public AOClickReduction(float sense, int smooth) {
        super();
        this.sense = sense;
        this.smooth = smooth;
    }

    // parameters
    private float sense;

    private int smooth;

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
            // copy data to derivation array...
            float d[] = new float[l1];
            for (int i = 0; i < d.length; i++) {
                d[i] = s1[o1 + i];
            }
            // xth order derivation to detect clicks...
            AOToolkit.derivate(d, 0, d.length);
            AOToolkit.derivate(d, 0, d.length);
            AOToolkit.derivate(d, 0, d.length);
            AOToolkit.derivate(d, 0, d.length);
            // detect maximum...
            float max = 0;
            for (int i = 0; i < d.length; i++) {
                if (Math.abs(d[i]) > max) {
                    max = Math.abs(d[i]);
                }
            }
            // remove clicks...
            float minClickPeriod = 200;
            float oldX = 0;
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = 0; i < d.length; i++) {
                // click detected ?
                if (d[i] > max * sense) {
                    progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / d.length));
                    // no click since long enough ?
                    if ((i - oldX) > minClickPeriod) {
                        AOToolkit.applyZeroCross(s1, i + o1, smooth);
                        // System.out.println("click detected at "+(i+o1));
                    }
                    oldX = i;
                }
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
