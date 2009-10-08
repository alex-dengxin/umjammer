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
import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiStartWindow;


/**
 * start window of LAoE.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 02.09.00 erster Entwurf oli4
 */
public class GStartWindow extends UiStartWindow {
    private ImageIcon loadIcon(String iconName) {
        URL u = getClass().getResource(iconName);
        if (u != null) {
            return new ImageIcon(u);
        }
        return null;
    }

    /**
     * constructor
     */
    public GStartWindow() {
        super();

        // build window
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 10);
        cl.setPreferredCellSize(new Dimension(35, 20));
        cl.setBorderGap(0);

        p.setLayout(cl);

        Color color = new Color(0xDDDDDD);

        JLabel version = new JLabel("LAoE " + GMain.version + ", " + GMain.date, SwingConstants.RIGHT);
        version.setOpaque(false);
        version.setForeground(color);
        cl.add(version, 0, 0, 10, 1);

        JLabel jvm = new JLabel(GMain.java, SwingConstants.RIGHT);
        jvm.setOpaque(false);
        jvm.setForeground(color);
        cl.add(jvm, 0, 1, 10, 1);

        JLabel copyright = new JLabel(GMain.copyright, SwingConstants.LEFT);
        copyright.setOpaque(false);
        copyright.setForeground(color);
        cl.add(copyright, 0, 8, 10, 1);

        workInfo = new JLabel(" ", SwingConstants.LEFT);
        setWorkInfo(GLanguage.translate("startLaoe") + "...");
        workInfo.setOpaque(false);
        workInfo.setForeground(color);
        cl.add(workInfo, 0, 9, 10, 1);

        // add as last component (to put it to background)
        cl.add(new JLabel(loadIcon("resources/startWindow.gif")), 0, 0, 10, 10);

        getContentPane().add(p);
        // center on screen
        pack();
        setLocation((int) (getToolkit().getScreenSize().getWidth() - getWidth()) / 2, (int) (getToolkit().getScreenSize().getHeight() - getHeight()) / 2);

        onBeginOfStart();
        setName(GLanguage.translate("startLaoe"));
        pack();
        Debug.println(3, "show start-window");
    }

    private JLabel workInfo;

    /**
     * set the work information text
     */
    public void setWorkInfo(String text) {
        workInfo.setText(text);
        Debug.println(1, text);
    }

}
