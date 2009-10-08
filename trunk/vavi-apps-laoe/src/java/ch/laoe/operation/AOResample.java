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
 * resamples the whole channels which are (partly) selected.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * TODO resample with interpolation
 * 
 * @version 26.07.00 erster Entwurf oli4 <br>
 *          04.08.00 neuer Stil oli4 <br>
 *          26.10.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          21.04.01 useage of toolkit oli4 <br>
 *          19.09.01 index-calculation double-precision oli4 <br>
 */
public class AOResample extends AOperation {
    public AOResample() {
        super();
        this.sampleRateFactor = 1.f;
        this.order = 2;
    }

    public AOResample(float sampleRateFactor, int order) {
        this();
        this.sampleRateFactor = sampleRateFactor;
        this.order = order;
    }

    public AOResample(int order) {
        this();
        this.order = order;
    }

    // samplerate factor
    private double sampleRateFactor;

    // interpolation order
    private int order;

    /**
     * performs constant resampling
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        int resampledLength = (int) (l1 / sampleRateFactor);

        // mark changed channels...
        ch1.getChannel().changeId();

        // create new samples
        float tmp[] = new float[s1.length - l1 + resampledLength];

        // copy before
        for (int i = 0; i < o1; i++) {
            tmp[i] = s1[i];
        }

        // resampling indexing
        double oldIndex = o1;

        // resample each new point
        progressSupport.entrySubProgress(new ProgressEvent(this));
        for (int i = o1; i < o1 + resampledLength; i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / resampledLength));
            // resample
            if (((int) oldIndex + 1) < s1.length) {
                switch (order) {
                case 0:
                    tmp[i] = AOToolkit.interpolate0(s1, (float) oldIndex);
                    break;
                case 1:
                    tmp[i] = AOToolkit.interpolate1(s1, (float) oldIndex);
                    break;
                case 2:
                    tmp[i] = AOToolkit.interpolate2(s1, (float) oldIndex);
                    break;
                case 3:
                    tmp[i] = AOToolkit.interpolate3(s1, (float) oldIndex);
                    break;
                }
            } else
                break; // end of source

            // calculate next index
            oldIndex += sampleRateFactor;
        }
        progressSupport.exitSubProgress(new ProgressEvent(this));

        // copy after
        for (int i = o1 + resampledLength; i < tmp.length; i++) {
            tmp[i] = s1[i - resampledLength + l1];
        }

        // replace old samples with new samples
        ch1.getChannel().sample = tmp;
    }

    /**
     * performs variable resampling
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float s2[] = ch2.getChannel().sample;
        // int o2 = ch2.getOffset();
        // int l2 = ch2.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        // calculate ressampled length
        double resampledLength = 0;
        for (int i = 0; i < l1; i++) {
            try {
                resampledLength += 1 / s2[o1 + i];
            } catch (ArrayIndexOutOfBoundsException aoobe) {
                resampledLength += 1;
            }
        }

        // create new samples
        float tmp[] = new float[s1.length - l1 + (int) resampledLength];

        // copy before
        for (int i = 0; i < o1; i++) {
            tmp[i] = s1[i];
        }

        // resampling indexing
        double oldIndex = o1;

        // resample each new point
        progressSupport.entrySubProgress(new ProgressEvent(this));
        for (int i = o1; i < o1 + resampledLength; i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / (int) resampledLength));
            // resample
            if (((int) oldIndex + 1) < s1.length) {
                switch (order) {
                case 0:
                    tmp[i] = AOToolkit.interpolate0(s1, (float) oldIndex);
                    break;
                case 1:
                    tmp[i] = AOToolkit.interpolate1(s1, (float) oldIndex);
                    break;
                case 2:
                    tmp[i] = AOToolkit.interpolate2(s1, (float) oldIndex);
                    break;
                case 3:
                    tmp[i] = AOToolkit.interpolate3(s1, (float) oldIndex);
                    break;
                }
            } else
                break; // end of source

            // calculate next index
            // resample curve range ok ?
            try {
                if (s2[(int) oldIndex] > 0)
                    oldIndex += s2[(int) oldIndex];
                else
                    oldIndex += 1;
            } catch (ArrayIndexOutOfBoundsException aoobe) {
                oldIndex += 1;
            }
        }
        progressSupport.exitSubProgress(new ProgressEvent(this));

        // copy after
        for (int i = o1 + (int) resampledLength; i < tmp.length; i++) {
            tmp[i] = s1[i - (int) resampledLength + l1];
        }

        // replace old samples with new samples
        ch1.getChannel().sample = tmp;
    }
}
