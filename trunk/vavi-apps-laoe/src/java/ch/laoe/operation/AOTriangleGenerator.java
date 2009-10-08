
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * generate triangle signal
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 28.04.01 first draft oli4
 */
public class AOTriangleGenerator extends AOperation {
    /**
     * constructor
     */
    public AOTriangleGenerator(float amplitude, float offset, int period, float dutyCycle, boolean add) {
        super();
        this.amplitude = amplitude;
        this.offset = offset;
        this.period = period;
        this.dutyCycle = dutyCycle;
        this.add = add;
    }

    // parameters
    private float amplitude, offset, dutyCycle;

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

        // build segments
        float x[] = new float[3];
        float y[] = new float[3];
        x[0] = 0;
        x[1] = period * dutyCycle;
        x[2] = period;
        y[0] = offset - amplitude;
        y[1] = offset + amplitude;
        y[2] = offset - amplitude;

        // build period
        float tmp[] = new float[period];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = AOToolkit.interpolate1(x, y, i);
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
