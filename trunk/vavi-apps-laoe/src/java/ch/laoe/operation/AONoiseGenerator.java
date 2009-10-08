
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * generate different noise types
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 14.05.01 first draft oli4
 */
public class AONoiseGenerator extends AOperation {
    /**
     * constructor
     */
    public AONoiseGenerator(float amplitude, float offset, int noiseType, boolean add) {
        super();
        this.amplitude = amplitude;
        this.offset = offset;
        this.noiseType = noiseType;
        this.add = add;
    }

    // parameters
    private float amplitude, offset;

    private int noiseType;

    public static final int WHITE = 1;

    public static final int TRIANGLE = 2;

    public static final int GAUSSIAN = 3;

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

        // map periodically to sample
        for (int i = o1; i < o1 + l1; i++) {
            float d = 0;

            switch (noiseType) {
            case WHITE:
                d = (float) (offset + (2 * Math.random() - 1) * amplitude);
                break;

            case TRIANGLE:
                d = (float) (offset + (Math.random() + Math.random() - 1) * amplitude);
                break;

            case GAUSSIAN:
                d = (float) (offset + Math.sqrt(-2 * Math.log(Math.random())) * Math.cos(2 * Math.PI * Math.random()) * amplitude);
                break;

            }

            if (add) {
                s1[i] = ch1.mixIntensity(i, s1[i], s1[i] + d);
            } else {
                s1[i] = ch1.mixIntensity(i, s1[i], d);
            }
        }
    }
}
