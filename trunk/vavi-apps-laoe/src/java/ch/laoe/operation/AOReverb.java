
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;
import ch.laoe.ui.Debug;


/**
 * reverbation with feedback and infinite number of echoes.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 29.07.00 erster Entwurf oli4 <br>
 *          04.08.00 neuer Stil oli4 <br>
 *          01.11.00 neuer Stil oli4 <br>
 *          12.11.00 add dry and wet parts oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          18.04.01 new operation framework oli4
 * 
 */
public class AOReverb extends AOperation {
    public AOReverb(int delay, float gain, float dry, float wet, boolean negFeedback, boolean backward) {
        super();
        this.delay = delay;
        this.gain = gain;
        this.dry = dry;
        this.wet = wet;
        this.negFeedback = negFeedback;
        this.backward = backward;
    }

    // parameters
    private int delay;

    private float gain;

    private float dry;

    private float wet;

    private boolean negFeedback;

    private boolean backward;

    public void operate(AChannelSelection ch1) {
        float sample[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float oldRms = AOToolkit.rmsAverage(sample, o1, l1);

        float tmp[] = new float[l1];

        // mark changed channels...
        ch1.getChannel().changeId();

        try {
            // copy to tmp
            if (backward) {
                for (int i = 0; i < l1; i++) {
                    tmp[l1 - 1 - i] = sample[i + o1];
                }
            } else {
                for (int i = 0; i < l1; i++) {
                    tmp[i] = sample[i + o1];
                }
            }

            // reverb...
            for (int i = 0; i < l1; i++) {
                // range-check
                if (i + delay < l1) {
                    if (negFeedback) {
                        tmp[i + delay] -= tmp[i] * gain;
                    } else {
                        tmp[i + delay] += tmp[i] * gain;
                    }
                }
            }

            // RMS-calibration
            float newRms = AOToolkit.rmsAverage(tmp, 0, l1);
            AOToolkit.multiply(tmp, 0, l1, (oldRms / newRms));

            // copy back to channel
            if (backward) {
                for (int i = o1; i < o1 + l1; i++) {
                    float s = sample[i] * dry + tmp[l1 - 1 - i - o1] * wet;
                    sample[i] = ch1.mixIntensity(i, sample[i], s);
                }
            } else {
                for (int i = o1; i < o1 + l1; i++) {
                    float s = sample[i] * dry + tmp[i - o1] * wet;
                    sample[i] = ch1.mixIntensity(i, sample[i], s);
                }
            }

            // zero cross
            AOToolkit.applyZeroCross(sample, o1);
            AOToolkit.applyZeroCross(sample, o1 + l1);
        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
