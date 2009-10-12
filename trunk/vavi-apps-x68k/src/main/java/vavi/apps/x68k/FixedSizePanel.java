/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;


class FixedSizePanel extends Panel {
    int width, height;

    Image image;

    public FixedSizePanel(int width, int height, Image image) {
        super();
        this.width = width;
        this.height = height;
        this.image = image;
        setSize(width, height);
    }

    public Dimension getMinimumSize() {
        return new Dimension(width, height);
    }

    public Dimension getMaximumSize() {
        return new Dimension(width, height);
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }
}
