
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;
import ch.laoe.ui.Debug;


/**
 * spectrum analysis.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 24.05.01 first draft oli4
 */
public class AOSpectrum extends AOperation {
    public AOSpectrum(int windowType) {
        this.windowType = windowType;

        re = new float[convertBufferLength];
        im = new float[convertBufferLength];
        spectrum = new float[convertBufferLength / 2];
    }

    // window
    private int windowType;

    public static final int RECTANGULAR_WINDOW = 1;

    public static final int HAMMING_WINDOW = 2;

    public static final int BLACKMAN_WINDOW = 3;

    public static final int FLATTOP_WINDOW = 4;

    // convertBuffer
    private static final int convertBufferLength = 16384;

    private float re[], im[];

    private float spectrum[];

    public float[] getSpectrum() {
        return spectrum;
    }

    public static int getSpectrumLength() {
        return convertBufferLength / 2;
    }

    private void operateTWindow() {
        switch (windowType) {
        case RECTANGULAR_WINDOW:
            AOToolkit.applyRectangularWindow(re, re.length);
            break;

        case HAMMING_WINDOW:
            AOToolkit.applyHammingWindow(re, re.length);
            break;

        case BLACKMAN_WINDOW:
            AOToolkit.applyBlackmanWindow(re, re.length);
            break;

        case FLATTOP_WINDOW:
            AOToolkit.applyFlattopWindow(re, re.length);
            break;
        }
    }

    private void addToSpectrum() {
        for (int i = 0; i < spectrum.length; i++) {
            spectrum[i] += AOToolkit.cartesianToMagnitude(re[i], im[i]);
        }
    }

    private void reduceSpectrum(int n) {
        for (int i = 0; i < spectrum.length; i++) {
            spectrum[i] /= n;
        }
    }

    /**
     * performs a constant amplification
     */
    public final void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

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
                operateTWindow();
                AOToolkit.complexFft(re, im);
                addToSpectrum();
            }
            reduceSpectrum(bufferOperations);
            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
