
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * general amplifier
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 16.05.01 first draft oli4
 */
public class AOMeasure extends AOperation {
    public AOMeasure(int sampleWidth) {
        super();
        clippedThreshold = 1 << (sampleWidth - 1);
    }

    private float min, max, mean, rms, stdDev;

    private float sum, sumOfSquares;

    private int samples, clippedSamples, clippedThreshold;

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getAbsoluteMax() {
        if (Math.abs(max) > Math.abs(min)) {
            return Math.abs(max);
        } else {
            return Math.abs(min);
        }
    }

    public float getMean() {
        return mean;
    }

    public float getRms() {
        return rms;
    }

    public float getStandardDeviation() {
        return stdDev;
    }

    public int getNumberOfClippedSamples() {
        return clippedSamples;
    }

    public void startOperation() {
        min = Float.MAX_VALUE;
        max = Float.MIN_VALUE;
        sum = 0;
        sumOfSquares = 0;
        samples = 0;
        clippedSamples = 0;
    }

    public void endOperation() {
        mean = sum / samples;
        rms = (float) Math.sqrt(sumOfSquares / samples);
        stdDev = (float) Math.sqrt((sumOfSquares - (sum * sum / samples)) / (samples - 1));
    }

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        try {
            for (int i = o1; i < (o1 + l1); i++) {
                samples++;

                sum += s1[i];
                sumOfSquares += s1[i] * s1[i];

                if (s1[i] > max) {
                    max = s1[i];
                }
                if (s1[i] < min) {
                    min = s1[i];
                }
                if (s1[i] > clippedThreshold) {
                    clippedSamples++;
                }
            }
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }

}
