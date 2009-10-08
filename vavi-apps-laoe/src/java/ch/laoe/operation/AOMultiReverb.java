
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;
import ch.laoe.ui.Debug;


/**
 * reverbation with multiple gaussian distributed feedback
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 20.06.01 first draft oli4 <br>
 *          11.08.01 multireverb with user-definable room oli4
 */
public class AOMultiReverb extends AOperation {
    /**
     * @param delay
     * @param gain
     * @param delayShape unit = 1 delay
     * @param gainShape unit = 1 gain
     * @param dry
     * @param wet
     * @param negFeedback
     * @param backward
     */
    public AOMultiReverb(int delay, float gain, float delayShape[], float gainShape[], float dry, float wet, boolean negFeedback, boolean backward) {
        super();

        this.delay = delay;
        this.gain = gain;
        this.delayShape = delayShape;
        this.gainShape = gainShape;
        this.dry = dry;
        this.wet = wet;
        this.negFeedback = negFeedback;
        this.backward = backward;
    }

    /**
     * @param dry
     * @param wet
     * @param backward
     */
    public AOMultiReverb(float dry, float wet, boolean backward) {
        super();
        this.delay = 1;
        this.gain = 1;
        this.dry = dry;
        this.wet = wet;
        this.negFeedback = false;
        this.backward = backward;
    }

    // parameters
    private int delay;

    private float gain;

    private float dry;

    private float wet;

    private boolean negFeedback;

    private boolean backward;

    float delayShape[];

    float gainShape[];

    /**
     * multi-echo using the shapes from constructor
     */
    public void operate(AChannelSelection ch1) {
        float sample[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float sample2[] = new float[l1];
        float sample3[] = new float[l1];
        float oldRms = AOToolkit.rmsAverage(sample, o1, l1);

        // mark changed channels...
        ch1.getChannel().changeId();

        try {
            // each factor...
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int k = 0; k < delayShape.length; k++) {
                progressSupport.setProgress(new ProgressEvent(this, (k + 1) * 100 / delayShape.length));

                // copy to sample2
                if (backward) {
                    for (int i = 0; i < sample2.length; i++) {
                        sample2[l1 - 1 - i] = sample[i + o1];
                    }
                } else {
                    for (int i = 0; i < sample2.length; i++) {
                        sample2[i] = sample[i + o1];
                    }
                }

                // reverb on sample2...
                int d = (int) (delay * delayShape[k]);
                float g = gain * gainShape[k];
                for (int i = 0; i < l1; i++) {
                    // range-check
                    if ((i + d < l1) && (i + d >= 0)) {
                        if (negFeedback) {
                            sample2[i + d] -= sample2[i] * g;
                        } else {
                            sample2[i + d] += sample2[i] * g;
                        }
                    }
                }

                // subtract original
                if (backward) {
                    for (int i = 0; i < sample2.length; i++) {
                        sample2[l1 - 1 - i] -= sample[i + o1];
                    }
                } else {
                    for (int i = 0; i < sample2.length; i++) {
                        sample2[i] -= sample[i + o1];
                    }
                }

                // add to sample3
                for (int i = 0; i < l1; i++) {
                    sample3[i] += sample2[i];
                }
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));

            // RMS-calibration
            float newRms = AOToolkit.rmsAverage(sample3, 0, l1);
            AOToolkit.multiply(sample3, 0, l1, (oldRms / newRms));

            // mix both parts...
            if (backward) {
                for (int i = 0; i < l1; i++) {
                    float s = dry * sample[i + o1] + wet * sample3[l1 - 1 - i];
                    sample[i + o1] = ch1.mixIntensity(i + o1, sample[i + o1], s);
                }
            } else {
                for (int i = 0; i < l1; i++) {
                    float s = dry * sample[i + o1] + wet * sample3[i];
                    sample[i + o1] = ch1.mixIntensity(i + o1, sample[i + o1], s);
                }
            }

        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }

    /**
     * multi-echo using a user-defined room
     * 
     * @param ch1 samples
     * @param ch2 room
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        // build shape
        float shape[] = ch2.getChannel().sample;
        int shapeLength = 0;
        for (int i = 0; i < shape.length; i++) {
            if (shape[i] != 0) {
                shapeLength++;
            }
        }
        this.delayShape = new float[shapeLength];
        this.gainShape = new float[shapeLength];
        int index = 0;
        for (int i = 0; i < shape.length; i++) {
            if (shape[i] != 0) {
                delayShape[index] = i;
                gainShape[index] = shape[i];
                index++;
            }
        }

        // perform operation
        operate(ch1);
    }

}
