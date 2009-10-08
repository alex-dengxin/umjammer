
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * generate sinus sweep signal
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 28.04.01 first draft oli4
 */
public class AOSinusSweepGenerator extends AOperation {
    /**
     * constructor
     */
    public AOSinusSweepGenerator(float amplitude, float offset, int startPeriod, int endPeriod, boolean add) {
        super();
        this.amplitude = amplitude;
        this.offset = offset;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.add = add;
    }

    // parameters
    private float amplitude, offset;

    private int startPeriod, endPeriod;

    private boolean add;

    /**
     * performs the segments generation
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float p = startPeriod;
        double rad = 0;
        double exp = Math.pow((double) endPeriod / startPeriod, 1. / l1);

        // mark changed channels...
        ch1.getChannel().changeId();

        // expponential sweep
        for (int i = 0; i < l1; i++) {
            p *= exp;
            rad += 1. / p * 2 * Math.PI;
            float s = 0;

            if (add) {
                s = s1[o1 + i];
            }
            s += offset + amplitude * (float) Math.sin(rad);
            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], s);
        }
    }
}
