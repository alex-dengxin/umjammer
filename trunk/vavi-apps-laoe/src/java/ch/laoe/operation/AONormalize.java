
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * normalizes selections to the defined max-value.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 29.07.00 erster Entwurf oli4 <br>
 *          03.08.00 neuer Stil oli4 <br>
 *          24.10.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          24.01.01 array-based again... oli4 <br>
 *          30.11.01 RMS mode added oli4
 */
public class AONormalize extends AOperation {
    public AONormalize(int mode, float maxValue) {
        this.mode = mode;
        this.maxValue = maxValue;
    }

    private float maxValue;

    private int mode;

    public static final int PEAK = 1;

    public static final int RMS = 2;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        float currentMaxValue = 0;

        // mark changed channels...
        ch1.getChannel().changeId();

        // search current max-value...
        switch (mode) {
        case PEAK:
            currentMaxValue = AOToolkit.max(s1, o1, l1);
            break;

        case RMS:
            currentMaxValue = AOToolkit.rmsAverage(s1, o1, l1);
            break;
        }

        // normalize...
        float scale = maxValue / currentMaxValue;
        progressSupport.entrySubProgress(new ProgressEvent(this));
        for (int i = o1; i < (o1 + l1); i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / l1));
            s1[i] = ch1.mixIntensity(i, s1[i], s1[i] * scale);
        }
        progressSupport.exitSubProgress(new ProgressEvent(this));
    }
}
