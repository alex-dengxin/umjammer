/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;
import ch.laoe.ui.Debug;


/**
 * FFT filter operation
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 19.05.01 first draft oli4 <br>
 *          13.03.02 disturbing clicks removed: overlapping windowed blocks introduced
 *                   (with the cost of more calculations) oli4 <br>
 */
public class AOFftFilter extends AOperation {
    /**
     * filter length must be same as buffer length. neutral value is 1.
     */
    public AOFftFilter(float[] filter) {
        re = new float[convertBufferLength];
        im = new float[convertBufferLength];

        kernelRe = new float[convertBufferLength];
        kernelIm = new float[convertBufferLength];
        for (int i = 0; i < convertBufferLength / 2; i++) {
            kernelRe[i] = filter[i];
        }
    }

    private static final int convertBufferLength = 16384;

    private static final int overlapFactor = 3;

    private float re[], im[];

    public static int getFilterLength() {
        return convertBufferLength / 2;
    }

    // filter-shape
    private float kernelRe[], kernelIm[];

    private void operateFilter() {
        for (int i = 0; i < re.length / 2; i++) {
            re[i] *= kernelRe[i];
            im[i] *= kernelRe[i];
        }
    }

    /**
     * performs a constant amplification
     */
    public final void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float oldRms = AOToolkit.rmsAverage(s1, o1, l1);
        float tmp[] = new float[l1];

        // mark changed channels...
        ch1.getChannel().changeId();

        try {
            // create buffers...
            int bufferOperations = overlapFactor * l1 / convertBufferLength;

            // each buffer-overlap...
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = -1; i < bufferOperations + 1; i++) {
                int ii = o1 + i * convertBufferLength / overlapFactor;
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / bufferOperations));
                // fill buffers...
                for (int j = 0; j < convertBufferLength; j++) {
                    if ((ii + j >= 0) && (ii + j < o1 + l1)) {
                        re[j] = s1[ii + j];
                    } else {
                        re[j] = 0;
                    }
                    im[j] = 0;
                }

                AOToolkit.applyBlackmanWindow(re, convertBufferLength);

                // to frequency-domain...
                AOToolkit.complexFft(re, im);

                // operate the frequency-domain...
                operateFilter();

                // back to time-domain...
                AOToolkit.complexIfft(re, im);
                // get result...
                for (int j = 0; j < convertBufferLength; j++) {
                    if ((ii - o1 + j >= 0) && (ii - o1 + j < l1)) {
                        tmp[ii - o1 + j] += re[j];
                    }
                }
                // zero cross for click-reduction
                // AOToolkit.applyZeroCross(tmp, ii); //not necessary anymore, YEEEAAAHHHH!!!!
            }

            // copy back
            for (int i = 0; i < l1; i++) {
                s1[o1 + i] = ch1.mixIntensity(o1 + i, s1[o1 + i], tmp[i]);
            }

            progressSupport.exitSubProgress(new ProgressEvent(this));

            // RMS-calibration
            float newRms = AOToolkit.rmsAverage(s1, o1, l1);
            AOToolkit.multiply(s1, o1, l1, (oldRms / newRms));
            AOToolkit.applyZeroCross(s1, o1);
            AOToolkit.applyZeroCross(s1, o1 + l1);
        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
