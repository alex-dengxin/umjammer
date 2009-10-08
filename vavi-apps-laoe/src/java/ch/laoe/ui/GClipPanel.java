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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipPlotter;


/**
 * parent-class of all double buffered graphical components which represents a layered clip.
 * The scrollable interface is also implemented here.
 * The use of this class makes no sense, use its children.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 17.08.00 erster Entwurf oli4
 */
public abstract class GClipPanel extends JPanel // implements MouseListener
{
    /**
     * constructor
     */
    public GClipPanel(AClip clip) {
        this.clip = clip;
        clipPlotter = this.clip.getClipPlotter();

        setFocusable(true);

        // size change detection
        oldSize = new Dimension(0, 0);
    }

    // source
    protected AClip clip;

    protected AClipPlotter clipPlotter;

    /**
     * returns the clip
     */
    public AClip getClip() {
        return clip;
    }

    /**
     * returns the clip-plotter
     */
    public AClipPlotter getClipPlotter() {
        return clipPlotter;
    }

    // double buffer with size change detection
    protected BufferedImage bgImage;

    protected Graphics2D bgGraphics;

    protected Dimension oldSize;

    /**
     * returns a new created image containing the clip-drawing with optionaly the plugin-drawings. this is usefull if you want to
     * use the image elsewhere, e.g. to store it as image file.
     * 
     * @return
     */
    public BufferedImage createFullImage(boolean pluginDrawingIncluded) {
        BufferedImage im = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_BGR);

        if (im != null) {
            // build background image
            Graphics2D gr = im.createGraphics();
            gr.drawImage(bgImage, 0, 0, this);
            if (pluginDrawingIncluded) {
                paintTopComponentsOnly(gr);
            }
        }
        return im;
    }

    /**
     * reloads the clip data and recalculates the graphical re- presentation.
     */
    public final void reload() {
        try {
            if ((getSize().width > 0) && (getSize().height > 0)) {
                // create background image
                bgImage = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_BGR);
                if (bgImage != null) {
                    // build background image
                    bgGraphics = bgImage.createGraphics();
                    bgGraphics.setColor(getBackground());
                    bgGraphics.fillRect(0, 0, getWidth(), getHeight());
                    paintFullClip(bgGraphics);
                    repaint();
                }
            }
        } catch (Exception e) {
            Debug.printStackTrace(5, e);
        }
    }

    /**
     * implemented by children. paints time-consuming part of the graphic, when clip data has changed.
     */
    protected void paintFullClip(Graphics2D g2d) {
    }

    /**
     * implemented by children. paints non-time-consuming parts of the graphic over the time-consuming part, when clip data
     * remains.
     */
    protected void paintTopComponentsOnly(Graphics2D g2d) {
    }

    /**
     * called by swing
     */
    protected final void paintComponent(Graphics g) {
        try {
            // System.out.println("GClipPanel.paintComponent...");
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            // size changed ?
            if ((oldSize.getWidth() != getSize().getWidth()) || (oldSize.getHeight() != getSize().getHeight())) {
                oldSize = new Dimension((int) getSize().getWidth(), (int) getSize().getHeight());
                // rebuild graphical representation...
                reload();
            }

            // copy background to foreground
            if (bgImage != null)
                g2d.drawImage(bgImage, 0, 0, this);
            // selections
            paintTopComponentsOnly(g2d);
        } catch (Exception e) {
            Debug.printStackTrace(5, e);
        }
    }

    /*
     * public void mousePressed(MouseEvent e) { }
     * 
     * public void mouseReleased(MouseEvent e) { }
     * 
     * public void mouseClicked(MouseEvent e) { }
     * 
     * public void mouseEntered(MouseEvent e) { }
     * 
     * public void mouseExited(MouseEvent e) { }
     */
}
