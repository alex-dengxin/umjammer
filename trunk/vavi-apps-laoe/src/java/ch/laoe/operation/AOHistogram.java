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
 * histogram analysis.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 08.06.02 first draft oli4
 */
public class AOHistogram extends AOperation {
    public AOHistogram() {
        histogram = new float[histogramLength];
    }

    private static final int histogramLength = 0x7FFF;

    private float histogram[];

    public float[] getHistogram() {
        return histogram;
    }

    public static int getHistogramLength() {
        return histogramLength;
    }

    /**
     * performs the histogram of the given channel-selection. histogram-length is constant, begins at zero and ends at samplewidth
     */
    public final void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
//      int sampleWidth = 1 << (ch1.getChannel().getParentClip().getSampleWidth() - 1);
//      double barWidth = (double)histogramLength/(double)sampleWidth;

        try {
            progressSupport.entrySubProgress(new ProgressEvent(this));

            // build histogram...
            for (int i = 0; i < l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                // System.out.println("h["+(Math.abs((int)(s1[o1+i]*barWidth)))+"]++");

                int h = Math.abs((int) (s1[o1 + i] /* *barWidth */));

                if (h >= histogram.length) {
                    h = histogram.length - 1;
                } else if (h < 0) {
                    h = 0;
                }
                histogram[h]++;
            }

            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
