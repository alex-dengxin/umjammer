
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * reduces a spectrumrange to zero with sharp edges.
 * 
 * @version 07.06.02 first draft oli4
 */
public class AOStopSpectrumRange extends AOperation {
    /**
     * constructor
     */
    public AOStopSpectrumRange() {
        super();
    }

    /**
     * performs the segments generation
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        // write samples
        for (int i = 0; i < l1; i++) {
            s1[o1 + i] = 0;
        }
    }
}
