
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * pitch generator.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 25.05.01 first draft oli4
 */
public class AOPitchGenerator extends AOperation {
    public AOPitchGenerator(float[] baseSignal) {
        super();
        this.baseSignal = baseSignal;
        smoothBaseSignal();
    }

    public AOPitchGenerator(float[] baseSignal, float constPitch) {
        this(baseSignal);
        this.constPitch = constPitch;
    }

    private float baseSignal[];

    private float constPitch;

    private void smoothBaseSignal() {
        if (baseSignal.length > 50) {
            AOToolkit.applyZeroCross(baseSignal, 0);
        }
    }

    /**
     * constant pitch generation
     * 
     * @param ch1 channel, where the pitch signal is applied
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float pitchIndex = 0;

        // mark changed channels...
        ch1.getChannel().changeId();

        try {
            for (int i = 0; i < l1; i++) {
                float s = AOToolkit.interpolate3(baseSignal, pitchIndex);
                s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], s);
                pitchIndex += constPitch;
            }
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }

    /**
     * variable pitch generation
     * 
     * @param ch1 channel, where the pitch signal is applied
     * @param ch2 channel, where the pitch value is defined
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float s2[] = ch2.getChannel().sample;
        float pitchIndex = 0;

        // mark changed channels...
        ch1.getChannel().changeId();

        try {
            for (int i = 0; i < l1; i++) {
                float s = AOToolkit.interpolate3(baseSignal, pitchIndex);
                s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], s);
                pitchIndex += s2[i + o1];
            }
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }

}
