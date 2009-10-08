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
import java.awt.Rectangle;

import ch.laoe.ui.GEditableSegments;


/**
 * the volume-mask of the channel
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 27.02.2002 first draft oli4
 */
public class AChannelMask extends AObject {
    /**
     * constructor
     */
    public AChannelMask(AChannel ch) {
        channel = ch;
        segments = new GEditableSegments();
        segments.setYDomain(GEditableSegments.PERCENT_DOMAIN);
        segments.setYRange(0, 1);
        segments.setChannel(ch);
        segments.setColor(Color.black);
    }

    private GEditableSegments segments;

    private AChannel channel;

    public GEditableSegments getSegments() {
        return segments;
    }

    public void setSegments(GEditableSegments es) {
        segments = es;
        segments.setChannel(channel);
    }

    public boolean isEnabled() {
        return segments.getNumberOfPoints() > 0;
    }

    private static int nameCounter;

    /**
     * set the default name
     */
    public void setDefaultName() {
        setDefaultName("mask", nameCounter++);
    }

    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
        segments.paintOntoClip(g2d, rect);
    }

    public void clear() {
        segments.clear();
    }

    public void setComplementary() {
        segments.setComplementary();
    }

    public void prepareResults() {
        segments.prepareResults();
    }

    /**
     * returns the sample
     */
    public float getSample(int index) {
        return segments.getSample(index);
    }

    public void applyDefinitely() {
        prepareResults();
        for (int i = 0; i < channel.getSampleLength(); i++) {
            channel.setSample(channel.getMaskedSample(i), i);
        }
        clear();
        channel.changeId();
    }
}
