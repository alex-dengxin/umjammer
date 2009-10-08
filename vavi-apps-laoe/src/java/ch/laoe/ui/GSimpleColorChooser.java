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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * widget for simple color-choosing
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 21.03.02 first draft oli4
 */
public class GSimpleColorChooser extends JComboBox {
    public GSimpleColorChooser(Color colors[]) {
        super();
        ListCellRenderer r = new SimpleColorChooserRenderer();
        setRenderer(r);
        fillColorItems(colors);
    }

    public GSimpleColorChooser() {
        this(colorList);
    }

    private static final Color colorList[] = {
        Color.blue, Color.blue.darker(), Color.blue.darker().darker(), Color.cyan, Color.cyan.darker(), Color.cyan.darker().darker(), Color.green, Color.green.darker(), Color.green.darker().darker(), Color.orange, Color.orange.darker(), Color.orange.darker().darker(), Color.magenta, Color.magenta.darker(), Color.magenta.darker().darker(), Color.pink, Color.pink.darker(), Color.pink.darker().darker(), Color.red.darker(), Color.red.darker().darker(), Color.yellow.darker(), Color.yellow.darker().darker(),
        Color.darkGray, Color.gray
    };

    public static Color[] getDefaultColorList() {
        return colorList;
    }

    private class ColorPanel extends JLabel {
        public ColorPanel() {
            setPreferredSize(new Dimension(30, 20));
            color = Color.lightGray;
        }

        private Color color;

        public void setColor(Color c) {
            color = c;
        }

        public void paintComponent(Graphics g) {
            g.setColor(color);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private class SimpleColorChooserRenderer extends ColorPanel implements ListCellRenderer {
        public SimpleColorChooserRenderer() {
            setColor(Color.lightGray);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setColor((Color) value);
            return this;
        }
    }

    private void fillColorItems(Color c[]) {
        for (int i = 0; i < c.length; i++) {
            addItem(c[i]);
        }
    }

    public Color getSelectedColor() {
        return (Color) getSelectedItem();
    }

    public static void main(String arg[]) {
        JFrame f = new JFrame();
        JComboBox c = new GSimpleColorChooser();
        f.getContentPane().add(c);
        f.pack();
        f.setVisible(true);
    }
}
