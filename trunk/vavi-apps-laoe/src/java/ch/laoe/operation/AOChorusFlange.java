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


/**
 * Class: AOChorusFlange @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * chorus and flange effect.
 * 
 * @version 29.07.00 erster Entwurf: bin nicht sicher, ob der Algorithmus stimmt... oli4 03.08.00 neuer
 * Stil oli4 11.05.01 array-based again... oli4
 */
public class AOChorusFlange extends AOperation {
    public AOChorusFlange(float dry, float wet, float feedback, boolean negFeedback, int baseDelay, int modulationDelay, int modulationPeriod, int modulationShape) {
        super();
        this.dry = dry;
        this.wet = wet;
        this.feedback = feedback;
        this.negFeedback = negFeedback;
        this.baseDelay = baseDelay;
        this.modulationDelay = modulationDelay;
        this.modulationPeriod = modulationPeriod;
        this.modulationShape = modulationShape;
    }

    // parameters
    private float wet, dry, feedback, baseDelay, modulationDelay, modulationPeriod;

    private int modulationShape;

    private boolean negFeedback;

    // shapes
    public static final int SINUS = 1;

    public static final int TRIANGLE = 2;

    public static final int SAW = 3;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        // prepare the delay-buffer...
        AOFifo delayFifo = AOToolkit.createFifo((int) (baseDelay + modulationDelay + 10));

        // all points...
        progressSupport.entrySubProgress(new ProgressEvent(this));
        for (int i = o1; i < (o1 + l1); i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / l1));
            // generate actual modulation...
            float iMod = baseDelay;
            switch (modulationShape) {
            case SINUS:
                iMod += (float) Math.sin((i % (int) modulationPeriod) / modulationPeriod * 2 * Math.PI) * modulationDelay;
                break;

            case TRIANGLE:
                iMod += (Math.abs(((i % (int) modulationPeriod) / modulationPeriod * 4) - 2) - 1) * modulationDelay;
                break;

            case SAW:
                iMod += (((i % (int) modulationPeriod) / modulationPeriod * 2) - 1) * modulationDelay;
                break;

            }

            float s = s1[i];

            // delay-output ready ?
            if ((int) iMod < delayFifo.getActualSize() - 1) {
                // make delay and feedback
                delayFifo.put(s + (delayFifo.pickFromHead(iMod - 1) * feedback));
                // mix dry and wet parts
                if (negFeedback) {
                    s = (s * dry) - (delayFifo.pickFromHead(iMod) * wet);
                } else {
                    s = (s * dry) + (delayFifo.pickFromHead(iMod) * wet);
                }
            } else {
                // make delay and feedback
                delayFifo.put(s);
                // take only dry part
                s = s * dry;
            }
            s1[i] = ch1.mixIntensity(i, s1[i], s);
        }

        // zero cross
        AOToolkit.applyZeroCross(s1, o1);
        AOToolkit.applyZeroCross(s1, o1 + l1);

        progressSupport.exitSubProgress(new ProgressEvent(this));
    }
}
