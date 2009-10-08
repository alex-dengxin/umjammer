
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * quantisize a signal
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 01.09.01 first draft oli4
 */
public class AOQuantizationGenerator extends AOperation {
    /**
     * constructor
     */
    public AOQuantizationGenerator(int phase, int period, boolean add) {
        super();
        this.phase = phase;
        this.period = period;
        this.add = add;
    }

    // parameters
    private int phase, period;

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

        // build gaussian
        for (int i = 0; i < l1; i++) {
            if ((i + o1) % period != phase) {
                if (add) {
                    s1[i + o1] += ch1.mixIntensity(i + o1, s1[i + o1], 0);
                } else {
                    s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], 0);
                }
            }
        }
    }
}
