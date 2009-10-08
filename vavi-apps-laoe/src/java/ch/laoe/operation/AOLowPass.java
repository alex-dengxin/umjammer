
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * IIR low pass filter
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 09.05.01 first draft oli4
 */
public class AOLowPass extends AOperation {
    public AOLowPass(float dry, float wet, float freq) {
        super();
        this.dry = dry;
        this.wet = wet;
        this.freq = freq;
    }

    private float dry;

    private float wet;

    private float freq;

    /**
     * performs a constant amplification
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float tmp[] = new float[l1];
        float oldRms = AOToolkit.rmsAverage(s1, o1, l1);

        // mark changed channels...
        ch1.getChannel().changeId();

        // copy
        for (int i = 0; i < l1; i++) {
            tmp[i] = s1[i + o1];
        }

        // low pass
        AOToolkit.setIirLowPass(tmp, 0, l1, dry, wet, freq);

        // RMS-calibration
        float newRms = AOToolkit.rmsAverage(tmp, 0, l1);
        AOToolkit.multiply(tmp, 0, l1, (oldRms / newRms));

        // back...
        for (int i = 0; i < l1; i++) {
            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], tmp[i]);
        }

        // zero cross
        AOToolkit.applyZeroCross(s1, o1);
        AOToolkit.applyZeroCross(s1, o1 + l1);
    }
}
