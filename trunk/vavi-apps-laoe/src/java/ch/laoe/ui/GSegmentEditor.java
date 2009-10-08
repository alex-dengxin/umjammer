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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import ch.laoe.clip.AClip;


/**
 * graphical component that allows to edit a universal segment-curve.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 09.03.02 first draft oli4
 */
public class GSegmentEditor extends JPanel implements MouseListener, MouseMotionListener {
    /**
     * constructor
     */
    public GSegmentEditor() {
        clip = new AClip(1, 1, 100);
        segments = new GEditableSegments();
        segments.setChannel(clip.getLayer(0).getChannel(0));

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    private AClip clip;

    public void setXRange(float offset, float length) {
    }

    public void setYRange(float offset, float length) {
    }

    private GEditableSegments segments;

    public void clear() {
        segments.clear();
    }

    public void paintComponent(Graphics g) {
        segments.paintOntoClip((Graphics2D) g, g.getClipBounds());
    }

    public void mousePressed(MouseEvent e) {
        segments.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        segments.mouseEntered(e);
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        segments.mouseClicked(e);
    }

    public void mouseMoved(MouseEvent e) {
        segments.mouseMoved(e);
    }

    public void mouseDragged(MouseEvent e) {
        segments.mouseDragged(e);
    }
}
