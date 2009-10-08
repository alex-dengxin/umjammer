
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * resamples the whole channels which are (partly) selected,
 * in sweep mode (linear resampling factor variation).
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 29.05.01 first draft oli4 <br>
 *          01.08.01 first stable version oli4 <br>
 *          19.09.01 index-calculation double-precision oli4 <br>
 */
public class AOSweepResample extends AOperation {
    public AOSweepResample(float beginFactor, float endFactor, int order) {
        super();
        this.beginFactor = beginFactor;
        this.endFactor = endFactor;
        this.order = order;
    }

    // samplerate factor
    private float beginFactor, endFactor;

    // interpolation order
    private int order;

    /**
     * performs variable resampling
     */
public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        // calculate new length...
        double resampledLength = 0;
        for (int i = 0; i < l1; i++) {
            double f = (double) i / (double) l1;
            resampledLength += (1 / beginFactor * (1 - f)) + (1 / endFactor * f);
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
        int end = o1 + (int) resampledLength;

        progressSupport.entrySubProgress(new ProgressEvent(this));
        for (int i = o1; i < end; i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / (end - o1)));
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
            } else {
                break; // end of source
            }
            // calculate next index
            double f = (i - o1) / resampledLength;
            oldIndex += 1 / (1 / beginFactor * (1 - f) + 1 / endFactor * f);
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
