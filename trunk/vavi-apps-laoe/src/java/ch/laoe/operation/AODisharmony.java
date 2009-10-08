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
 * Class: AODisharmony @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * disharmony operation (f-domain shift)
 * 
 * @version 17.06.01 first draft oli4 02.08.01 divers disharmony-types added oli4 20.09.01 adapt
 * disharmony parameter range to -1 .. 1 oli4
 */
public class AODisharmony extends AOperation {

    /**
     * initializes disharmony-operation.
     * 
     * @param dry dry part 0..1
     * @param wet wet part 0..1
     * @param disharmony -1..1, 0=no disharmony, 1=max. positive d., -1=max. negative d.
     * @param bufferLength length of internally used FFT
     */
    public AODisharmony(int type, float dry, float wet, float disharmony, int bufferLength) {
        this.type = type;
        this.dry = dry;
        this.wet = wet;
        this.disharmony = disharmony;
        this.convertBufferLength = bufferLength;

        re = new float[convertBufferLength];
        im = new float[convertBufferLength];
        shRe = new float[convertBufferLength / 2];
        shIm = new float[convertBufferLength / 2];
    }

    private int convertBufferLength;

    private float re[], im[];

    private float shRe[], shIm[];

    private int type;

    public static final int SHIFT = 0;

    public static final int BLUR = 1;

    public static final int UNPITCH = 2;

    public static final int BASS_DEPHASE = 3;

    public static final int TREBLE_DEPHASE = 4;

    private float dry, wet;

    private float disharmony;

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
            int overlapFactor = 3;
            int bufferOperations = l1 / convertBufferLength * overlapFactor + 1;

            // each buffer...
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = -1; i < bufferOperations + 1; i++) {
                int ii = o1 + i * convertBufferLength / overlapFactor;

                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / bufferOperations));
                // fill buffers...
                for (int j = 0; j < convertBufferLength; j++) {
                    int jj = ii + j;
                    if ((jj >= 0) && (jj < o1 + l1)) {
                        re[j] = s1[jj];
                    } else {
                        re[j] = 0;
                    }
                    im[j] = 0;
                }

                // to frequency-domain...
                AOToolkit.applyBlackmanWindow(re, convertBufferLength);
                AOToolkit.complexFft(re, im);

                // perform disharmony...
                switch (type) {
                case SHIFT: {
                    int d = (int) (disharmony * convertBufferLength / 10);
                    for (int k = 0; k < shRe.length; k++) {
                        if (((k - d) < re.length / 2) && ((k - d) > 0)) {
                            shRe[k] = re[k - d];
                            shIm[k] = im[k - d];
                        } else {
                            shRe[k] = 0;
                            shIm[k] = 0;
                        }
                    }
                }
                    break;

                case BLUR: {
                    int d = (int) (disharmony * convertBufferLength / 5);
                    for (int k = 0; k < shRe.length; k++) {
                        if (((k - d) < re.length / 2) && ((k - d) > 0)) {
                            shRe[k] = re[k - d];
                            shIm[k] = im[k];
                        } else {
                            shRe[k] = 0;
                            shIm[k] = 0;
                        }
                    }
                }
                    break;

                case UNPITCH: {
                    int d = (int) (disharmony * convertBufferLength / 5);
                    for (int k = 0; k < shRe.length; k++) {
                        float rnd = (int) (k + Math.random() * d);
                        if ((rnd >= 0) && (rnd < shRe.length)) {
                            shRe[k] = AOToolkit.interpolate3(re, rnd);
                            shIm[k] = AOToolkit.interpolate3(im, rnd);
                        } else {
                            shRe[k] = 0;
                            shIm[k] = 0;
                        }
                    }
                }
                    break;

                case BASS_DEPHASE: {
                    int d = (int) (disharmony * convertBufferLength * 10);
                    // to polar system...
                    for (int k = 0; k < shRe.length; k++) {
                        float mag = AOToolkit.cartesianToMagnitude(re[k], im[k]);
                        float phas = AOToolkit.cartesianToPhase(re[k], im[k]);
                        re[k] = mag;
                        im[k] = phas;
                    }
                    // dephase...
                    for (int k = 0; k < shRe.length; k++) {
                        float rnd = (int) (k + Math.random() * (d / (k + 1)));
                        if ((rnd >= 0) && (rnd < shRe.length)) {
                            im[k] = AOToolkit.interpolate3(im, rnd);
                        }
                    }
                    // back to cartesian system...
                    for (int k = 0; k < shRe.length; k++) {
                        shRe[k] = AOToolkit.polarToX(re[k], im[k]);
                        shIm[k] = AOToolkit.polarToY(re[k], im[k]);
                    }
                }
                    break;

                case TREBLE_DEPHASE: {
                    int d = (int) (disharmony * convertBufferLength * 10);
                    // to polar system...
                    for (int k = 0; k < shRe.length; k++) {
                        float mag = AOToolkit.cartesianToMagnitude(re[k], im[k]);
                        float phas = AOToolkit.cartesianToPhase(re[k], im[k]);
                        re[k] = mag;
                        im[k] = phas;
                    }
                    // dephase...
                    for (int k = 0; k < shRe.length; k++) {
                        float rnd = (int) (k + Math.random() * (d / (shRe.length - k)));
                        if ((rnd >= 0) && (rnd < shRe.length)) {
                            im[k] = AOToolkit.interpolate3(im, rnd);
                        }
                    }
                    // back to cartesian system...
                    for (int k = 0; k < shRe.length; k++) {
                        shRe[k] = AOToolkit.polarToX(re[k], im[k]);
                        shIm[k] = AOToolkit.polarToY(re[k], im[k]);
                    }
                }
                    break;

                // more types...

                }
                // copy new spectrum...
                for (int k = 0; k < shRe.length; k++) {
                    re[k] = shRe[k];
                    im[k] = shIm[k];
                }

                // offset
                re[0] = 0;
                im[0] = 0;

                // back to time-domain...
                AOToolkit.complexIfft(re, im);

                // get result...
                for (int j = 0; j < convertBufferLength; j++) {
                    if ((ii - o1 + j >= 0) && (ii - o1 + j < l1)) {
                        tmp[ii - o1 + j] += re[j];
                    }
                }
            }

            // copy back
            for (int i = 0; i < l1; i++) {
                s1[o1 + i] = ch1.mixIntensity(o1 + i, s1[o1 + i], tmp[i]);
            }

            // RMS-calibration
            float newRms = AOToolkit.rmsAverage(s1, o1, l1);
            AOToolkit.multiply(s1, o1, l1, (oldRms / newRms));
            AOToolkit.applyZeroCross(s1, o1);
            AOToolkit.applyZeroCross(s1, o1 + l1);

            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
