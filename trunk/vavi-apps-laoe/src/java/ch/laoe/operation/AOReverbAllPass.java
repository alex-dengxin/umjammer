
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * reverbation with flat frequency response.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 29.07.00 erster Entwurf: bin nicht sicher, ob der Algorithmus stimmt... oli4 <br>
 *          04.08.00 neuer Stil oli4 <br>
 *          01.11.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          18.04.01 new operation framework oli4 <br>
 *          21.04.01 add dry and wet parts, useage of toolkit oli4 <br>
 */
public class AOReverbAllPass extends AOperation {
    public AOReverbAllPass(int delay, float gain, float dry, float wet, boolean backward) {
        super();
        this.delay = delay;
        this.gain = gain;
        this.dry = dry;
        this.wet = wet;
        this.backward = backward;
    }

    // parameters
    private int delay;

    private float gain;

    private float dry;

    private float wet;

    private boolean backward;

    public void operate(AChannelSelection ch1) {
        float sample[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float oldRms = AOToolkit.rmsAverage(sample, o1, l1);

        // mark changed channels...
        ch1.getChannel().changeId();

        // prepare delay buffer...
        AOFifo delayBuffer = AOToolkit.createFifo(delay + 1);
        float output;
        float tmp[] = new float[l1];

        try {
            // copy to tmp
            if (backward) {
                for (int i = 0; i < l1; i++) {
                    tmp[l1 - 1 - i] = sample[i + o1];
                }
            } else {
                for (int i = 0; i < l1; i++) {
                    tmp[i] = sample[i + o1];
                }
            }

            // reverb...
            for (int i = 0; i < l1; i++) {
                // delay-buffer-output ready ?
                if (delay < delayBuffer.getActualSize()) {
                    output = delayBuffer.pickFromHead(delay - 1) + tmp[i] * (-gain);
                    delayBuffer.put(tmp[i] + output * gain);
                    tmp[i] = output;
                }
                // delay not reached ?
                else {
                    output = tmp[i] * (-gain);
                    delayBuffer.put(tmp[i] + output * gain);
                    tmp[i] = output;
                }
            }

            // RMS-calibration
            float newRms = AOToolkit.rmsAverage(tmp, 0, l1);
            AOToolkit.multiply(tmp, 0, l1, oldRms / newRms);

            // copy back and mix
            if (backward) {
                for (int i = o1; i < o1 + l1; i++) {
                    float s = sample[i] * dry + tmp[l1 - 1 - i - o1] * wet;
                    sample[i] = ch1.mixIntensity(i, sample[i], s);
                }
            } else {
                for (int i = o1; i < o1 + l1; i++) {
                    float s = sample[i] * dry + tmp[i - o1] * wet;
                    sample[i] = ch1.mixIntensity(i, sample[i], s);
                }
            }
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }
}
