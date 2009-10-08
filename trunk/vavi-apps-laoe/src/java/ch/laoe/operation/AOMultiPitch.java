
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;
import ch.laoe.ui.Debug;


/**
 * multi-pitch operation
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 19.06.01 first draft oli4
 */
public class AOMultiPitch extends AOperation {

    public AOMultiPitch(float[] pitch, float[] amplitude, float dry, float wet, int fftLength) {
        this.convertBufferLength = fftLength;
        this.pitch = pitch;
        this.amplitude = amplitude;

        re = new float[convertBufferLength];
        im = new float[convertBufferLength];
        shRe = new float[convertBufferLength / 2];
        shIm = new float[convertBufferLength / 2];
    }

    private int convertBufferLength;

    private float re[], im[];

    private float shRe[], shIm[];

    private float pitch[], amplitude[];

    /**
     * performs a constant amplification
     */
    public final void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        try {
            // create buffers...
            int bufferOperations = l1 / convertBufferLength + 1;

            // each buffer...
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = 0; i < bufferOperations; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / bufferOperations));

                // fill buffers...
                for (int j = 0; j < convertBufferLength; j++) {
                    int jj = o1 + i * convertBufferLength + j;
                    if (jj < o1 + l1) {
                        re[j] = s1[jj];
                    } else {
                        re[j] = 0;
                    }
                    im[j] = 0;
                }

                // to frequency-domain...
                AOToolkit.complexFft(re, im);

                // perform multi-pitch...

                for (int k = 0; k < shRe.length; k++) {
                    shRe[k] = 0;
                    shIm[k] = 0;
                }

                // each pitch...
                for (int j = 0; j < pitch.length; j++) {
                    if (pitch[j] > 0) {
                        // each sample...
                        for (int k = 0; k < shRe.length; k++) {
                            float shIndex = k / pitch[j];
                            if (shIndex < shRe.length) {
                                shRe[k] += AOToolkit.interpolate3(re, shIndex) * amplitude[j];
                            }
                            if (shIndex < shIm.length) {
                                shIm[k] += AOToolkit.interpolate3(im, shIndex) * amplitude[j];
                            }
                        }
                    }
                }
                for (int k = 0; k < shRe.length; k++) {
                    re[k] = shRe[k];
                    im[k] = shIm[k];
                }

                // back to time-domain...
                AOToolkit.complexIfft(re, im);

                // get result...
                for (int j = 0; j < convertBufferLength; j++) {
                    int jj = o1 + i * convertBufferLength + j;
                    if (jj < o1 + l1) {
                        // s1[jj] = s1[jj] * dry + re[j] * wet;
                        // s1[jj] = re[j];
                        s1[jj] = ch1.mixIntensity(jj, s1[jj], re[j]);
                    }
                }
                // zero cross for click-reduction
                AOToolkit.applyZeroCross(s1, o1 + i * convertBufferLength);
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
