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

package ch.laoe.audio;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;


/**
 * AudioPlotter.
 *
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * @version 02.12.00 new stream-technique oli4 <br>
 *          28.07.01 graphics dirty region management for play pointer oli4 <br>
 */
public class AudioPlotter {
    /**
     * constructor
     */
    protected AudioPlotter(Audio a) {
        this.audio = a;
    }

    // audio
    protected Audio audio;

    /**
     * returns the audio, which this plotter represents.
     */
    public Audio getAudio() {
        return audio;
    }

    // play plotter
    protected int xPlayPointer;

    private Rectangle paintRect; // for dirty-region

    /**
     * paints the play pointers
     */
    public void paintPlayPointer(Graphics2D g2d, Rectangle rect, Color color) {
        paintRect = rect;
//      int maxSampleLength = audio.getClip().getMaxSampleLength();
        xPlayPointer = audio.getClip().getSelectedLayer().getChannel(0).getChannelPlotter().sampleToGraphX(audio.getPlayPointer());
        int y0 = (int) rect.getX();
        int y1 = (int) (rect.getX() + rect.getHeight());

        g2d.setClip(rect);
        g2d.setStroke(new BasicStroke());
        g2d.setColor(color);
        g2d.drawLine(xPlayPointer, y0, xPlayPointer, y1);
    }

    /**
     * get the area which has to be repainted because of play-pointer movement
     */
    public Rectangle getDirtyRegion() {
        int x = audio.getClip().getSelectedLayer().getChannel(0).getChannelPlotter().sampleToGraphX(audio.getPlayPointer());
        paintRect.x = Math.min(x, xPlayPointer) - 30;
        paintRect.width = Math.abs(x - xPlayPointer) + 60;

        return paintRect;
    }

    /**
     * returns the x coordinate of the play-pointer
     */
    public int getXPlayPointer() {
        return xPlayPointer;
    }

    // loop pointers
    protected int xLoopStartPointer;

    protected int xLoopEndPointer;

    /**
     * paints the loop pointers
     */
    public void paintLoopPointer(Graphics2D g2d, Rectangle rect, Color color) {
        paintRect = rect;
//      int maxSampleLength = audio.getClip().getMaxSampleLength();
        int y0 = (int) rect.getX();
        int y1 = (int) (rect.getX() + rect.getHeight());

        g2d.setClip(rect);
        g2d.setStroke(new BasicStroke());
        g2d.setColor(color);
        xLoopStartPointer = audio.getClip().getSelectedLayer().getChannel(0).getChannelPlotter().sampleToGraphX(audio.getLoopStartPointer());
        g2d.drawLine(xLoopStartPointer, y0, xLoopStartPointer, y1);
        xLoopEndPointer = audio.getClip().getSelectedLayer().getChannel(0).getChannelPlotter().sampleToGraphX(audio.getLoopEndPointer());
        g2d.drawLine(xLoopEndPointer, y0, xLoopEndPointer, y1);
    }

    /**
     * returns the x coordinate of the loop start pointer
     */
    public int getXLoopStartPointer() {
        return xLoopStartPointer;
    }

    /**
     * returns the x coordinate of the loop end pointer
     */
    public int getXLoopEndPointer() {
        return xLoopEndPointer;
    }

    /**
     * return true if something would change when plotting again public boolean hasChanged () { return audio.isActive(); }
     */

}
