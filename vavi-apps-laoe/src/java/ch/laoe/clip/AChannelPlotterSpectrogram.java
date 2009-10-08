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

package ch.laoe.clip;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.laoe.operation.AOToolkit;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GToolkit;


/**
 * channel view, x-axis = time, y-axis = frequency color-darkness = magnitude, spectrogramm-view.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 07.03.02 first draft oli4
 */
public class AChannelPlotterSpectrogram extends AChannelPlotter {
    /**
     * constructor
     */
    public AChannelPlotterSpectrogram(AModel m, AChannelPlotter p) {
        super(m, p);
    }

    public float getAutoscaleXOffset() {
        return 0;
    }

    public float getAutoscaleXLength() {
        return getChannelModel().getSampleLength();
    }

    public float getAutoscaleYOffset(int xOffset, int xLength) {
        return 0;
    }

    public float getAutoscaleYLength(int xOffset, int xLength) {
        return ((AClip) getChannelModel().getParent().getParent()).getSampleRate() / 2;
    }

    protected float getValidYOffset() {
        return 0;
    }

    protected float getValidYLength() {
        return ((AClip) getChannelModel().getParent().getParent()).getSampleRate() / 2;
    }

    // ************* configuration *************

    private static int fftLength = 512;

    public static void setFftLength(int l) {
        switch (l) {
        case 32:
        case 64:
        case 128:
        case 256:
        case 512:
        case 1024:
        case 2048:
        case 4096:
        case 8192:
            fftLength = l;
            break;

        default:
            fftLength = 512;
            break;
        }
    }

    public static int getFftLength() {
        return fftLength;
    }

    public static final int HAMMING_WINDOW = 1;

    public static final int RECTANGULAR_WINDOW = 2;

    public static final int BLACKMAN_WINDOW = 3;

    public static final int FLATTOP_WINDOW = 4;

    private static int windowType = HAMMING_WINDOW;

    public static void setWindowType(int w) {
        switch (w) {
        case HAMMING_WINDOW:
        case RECTANGULAR_WINDOW:
        case BLACKMAN_WINDOW:
        case FLATTOP_WINDOW:
            windowType = w;
            break;

        default:
            windowType = HAMMING_WINDOW;
            break;
        }
    }

    public static int getWindowType() {
        return windowType;
    }

    // ************* color *************

    private static int colorGradeLength = 256;

    private static Color colorGrade[] = new Color[colorGradeLength];

    /**
     * set the gamma (range 0..1, neutral at 0.5)
     */
    public static void setColorGamma(float g) {
        float x[] = {
            0, 1 - g, 1
        };
        float y[] = {
            0, g, 1
        };
        setColorTransferFunction(x, y);
    }

    /**
     * set the transferfunction-segments of value to darkness curve
     */
    public static void setColorTransferFunction(float x[], float y[]) {
        Color color = GToolkit.mixColors(Color.black, Color.blue, .2f);
        for (int i = 0; i < colorGradeLength; i++) {
            float c = (float) (i) / colorGradeLength;
            c = AOToolkit.interpolate1(x, y, c);
            colorGrade[i] = GToolkit.mixColors(Color.lightGray, color, c);
        }
    }

    static {
        colorGrade = new Color[colorGradeLength];
        float x[] = {
            0, .2f, 1
        };
        float y[] = {
            0, .8f, 1
        };
        setColorTransferFunction(x, y);
    }

    /**
     * factor range 0..1
     */
    private Color getColorGrade(float factor) {
        int index = (int) (Math.abs(factor) * colorGradeLength);

        if (index > colorGradeLength - 1) {
            index = colorGradeLength - 1;
        }
        return colorGrade[index];
    }

    public void paintSamples(Graphics2D g2d, Color color) {
        try {
            // color, clip
            g2d.setColor(color);
            g2d.setClip(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

            AChannel ch = getChannelModel();
            AClip clip = (AClip) ch.getParent().getParent();
            int sampleWidth = 1 << (clip.getSampleWidth() - 1);
            int width = rectangle.width;
            int height = rectangle.height;
            int x = rectangle.x;
            int y = rectangle.y;
            float sample[] = ch.sample;
            float samplerate = clip.getSampleRate();

            int xSample;
            int xGraphFftWidth;

            // short time FFT
            float re[] = new float[fftLength];
            float im[] = new float[fftLength];

            // graphic rectangles...
            int yGraph[] = new int[fftLength / 2 + 1];
            for (int i = 0; i < fftLength / 2 + 1; i++) {
                yGraph[i] = sampleToGraphY(i * samplerate / fftLength);
            }

            // draw reduced sample, each pixel...
            for (int i = x; i < x + width; i += xGraphFftWidth) {
                // x scaling, sample range represented by pixel i
                xSample = (int) graphToSampleX(i);
                xGraphFftWidth = Math.max((sampleToGraphX(xSample + fftLength) - i) / 8, 1);

                // x range ok ?
                if (xSample >= sample.length)
                    break;

                if (xSample < 0)
                    continue;

                // short time fft
                for (int j = 0; j < fftLength; j++) {
                    if (xSample + j < sample.length)
                        re[j] = sample[xSample + j];
                }
                for (int j = 0; j < fftLength; j++) {
                    im[j] = 0;
                }
                switch (windowType) {
                case HAMMING_WINDOW:
                    AOToolkit.applyHammingWindow(re, fftLength);
                    break;

                case BLACKMAN_WINDOW:
                    AOToolkit.applyBlackmanWindow(re, fftLength);
                    break;

                case FLATTOP_WINDOW:
                    AOToolkit.applyFlattopWindow(re, fftLength);
                    break;

                default:
                    AOToolkit.applyRectangularWindow(re, fftLength);
                    break;
                }
                AOToolkit.complexFft(re, im);
                for (int j = 0; j < fftLength / 2; j++) {
                    re[j] = AOToolkit.cartesianToMagnitude(re[j], im[j]);
                }

                // paint a colored row
                for (int j = 0; j < fftLength / 2; j++) {
                    if (yGraph[j] > y + height)
                        continue;

                    if (yGraph[j] < y)
                        break;

                    g2d.setColor(getColorGrade(re[j] / sampleWidth));
                    g2d.fillRect(i, yGraph[j + 1], xGraphFftWidth, yGraph[j] - yGraph[j + 1]);
                }
            }
        } catch (Exception e) {
            Debug.printStackTrace(5, e);
        }
    }

}
