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

package ch.laoe.ui;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.laoe.clip.AClip;


/**
 * graphical component that represents a single channel. Only one track is printed without alpha-information. This
 * component is used for thumbnail-representation of one channel.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 23.10.00 erster Entwurf oli4
 */
public class GChannelViewer extends GClipPanel {
    /**
     * constructor
     */
    public GChannelViewer(AClip clip, int layerIndex, int trackIndex) {
        super(clip);
        this.layerIndex = layerIndex;
        this.trackIndex = trackIndex;
    }

    protected int layerIndex;

    protected int trackIndex;

    protected void paintFullClip(Graphics2D g2d) {
        Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
        clip.getClipPlotter().paintChannelThumbnail(bgGraphics, rect, layerIndex, trackIndex);
    }
}
