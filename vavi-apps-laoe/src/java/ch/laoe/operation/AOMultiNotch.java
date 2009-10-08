
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * IIR multiple notch filter
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 09.06.01 first draft oli4
 */
public class AOMultiNotch extends AOperation {
    public AOMultiNotch(float[] freq, float q) {
        super();
        this.freq = freq;
        this.q = q;
    }

    private float freq[];

    private float q;

    /**
     * performs a constant amplification
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        float tmp[] = new float[l1];

        // mark changed channels...
        ch1.getChannel().changeId();

        progressSupport.entrySubProgress(new ProgressEvent(this));

        // copy
        for (int i = 0; i < l1; i++) {
            tmp[i] = s1[i + o1];
        }

        for (int i = 0; i < freq.length; i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / freq.length));
            // System.out.println("f="+freq[i]);
            if (freq[i] != 0) {
                AOToolkit.setIirNotch(tmp, 0, l1, freq[i], q, 1);
            }
        }

        // back...
        for (int i = 0; i < l1; i++) {
            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], tmp[i]);
        }

        progressSupport.exitSubProgress(new ProgressEvent(this));
    }
}
