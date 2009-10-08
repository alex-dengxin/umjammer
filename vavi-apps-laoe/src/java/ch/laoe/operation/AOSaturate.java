
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * saturates selections to the defined max-value.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 29.07.00 erster Entwurf oli4 <br>
 *          04.08.00 neuer Stil oli4 <br>
 *          24.10.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          26.01.01 array-based again... oli4
 */
public class AOSaturate extends AOperation {
    public AOSaturate(float maxValue) {
        this.maxValue = maxValue;
    }

    private float maxValue;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        progressSupport.entrySubProgress(new ProgressEvent(this));
        for (int i = o1; i < (o1 + l1); i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / l1));
            float s = s1[i];

            // positive saturation range ?
            if (s > maxValue)
                s = maxValue;
            // negative saturation range ?
            if (s < -maxValue)
                s = -maxValue;

            s1[i] = ch1.mixIntensity(i, s1[i], s);
        }

        progressSupport.exitSubProgress(new ProgressEvent(this));
    }
}
