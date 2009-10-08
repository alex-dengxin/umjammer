
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * generate a ramp signal
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 13.09.01 first draft oli4
 */
public class AORampGenerator extends AOperation {
    /**
     * constructor
     */
    public AORampGenerator(float startAmplitude, float endAmplitude, boolean add, boolean normalized) {
        super();
        this.startAmplitude = startAmplitude;
        this.endAmplitude = endAmplitude;
        this.add = add;
        this.normalized = normalized;
    }

    // parameters
    private float startAmplitude, endAmplitude;

    private boolean add, normalized;

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
        float x[] = new float[2];
        float y[] = new float[2];
        x[0] = 0;
        x[1] = l1;

        if (normalized) {
            y[0] = o1;
            y[1] = o1 + l1;
        } else {
            y[0] = startAmplitude;
            y[1] = endAmplitude;
        }

        // build ramp
        if (add) {
            for (int i = 0; i < l1; i++) {
                s1[o1 + i] = ch1.mixIntensity(i + o1, s1[o1 + i], s1[o1 + i] + AOToolkit.interpolate1(x, y, i));
            }
        } else {
            for (int i = 0; i < l1; i++) {
                s1[o1 + i] = ch1.mixIntensity(i + o1, s1[o1 + i], AOToolkit.interpolate1(x, y, i));
            }
        }

    }
}
