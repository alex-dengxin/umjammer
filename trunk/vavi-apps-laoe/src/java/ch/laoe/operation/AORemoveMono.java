
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * removes the mono-part of a stereo layer, and writes the result to channel1
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 11.07.01 first draft oli4
 */
public class AORemoveMono extends AOperation {
    /**
     * @param pan pan, value range 1..2, usually 1.5 (middle), but may vary if mono source is not centered.
     */
    public AORemoveMono(float pan) {
        super();
        this.pan = pan;
        l = Math.max(1, 2 * pan - 2);
        r = Math.max(1, 4 - 2 * pan);
    }

    private float pan;

    private float l;

    private float r;

    public void operate(AChannelSelection channel1, AChannelSelection channel2) {
        float s1[] = channel1.getChannel().sample;
        float s2[] = channel2.getChannel().sample;
        int o1 = channel1.getOffset();
        int l1 = channel1.getLength();
        float ch1, ch2;

        // mark changed channels...
        channel1.getChannel().changeId();

        try {
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = 0; i < l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));

                ch1 = s1[i + o1];
                ch2 = s2[i + o1];

                // compensate concentric mono
                ch1 = ch1 * r - ch2 * l;

                s1[i + o1] = channel1.mixIntensity(i + o1, s1[i + o1], ch1);
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }

}
