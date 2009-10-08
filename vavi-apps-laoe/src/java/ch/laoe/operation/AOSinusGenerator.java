
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * generate sinus signal
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 28.04.01 first draft oli4
 */
public class AOSinusGenerator extends AOperation {
    /**
     * constructor
     */
    public AOSinusGenerator(float amplitude, float offset, int period, boolean add) {
        super();
        this.amplitude = amplitude;
        this.offset = offset;
        this.period = period;
        this.add = add;
    }

    // parameters
    private float amplitude, offset;

    private int period;

    private boolean add;

    /**
     * performs the segments generation
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        // build period
        float tmp[] = new float[period];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = offset + amplitude * (float) Math.sin((double) i / (double) period * 2 * Math.PI);
        }

        // map periodically to sample
        for (int i = 0; i < l1; i++) {
            float s = s1[o1 + i];
            if (add) {
                s += tmp[i % tmp.length];
            } else {
                s = tmp[i % tmp.length];
            }
            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], s);
        }
    }
}
