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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;


/**
 * collection of usefull tools.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 14.02.2002 first draft oli4
 */
public abstract class GToolkit {

    // *************** graphics ******************

    public static ImageIcon loadIcon(Object source, String iconName) {
        URL u = source.getClass().getResource(iconName);
        if (u != null) {
            return new ImageIcon(u);
        }
        return null;
    }

    /**
     * create a custom cursor with a given cursorname, and the fix relative hotspot of (4, 4), and considering the pixelsize
     * supported by os-graphics driver.
     * 
     * the cursorname must not contain any extension and size-information. complete cursor image-filename: <cursorName><size>.gif
     * cursorName e.g. "resources/selectCursor" size e.g. "32" so a complete filename example is "cursorName32.gif" the image should be a
     * gif-file with transparency, and the hotspot must be at x=4, y=4 for all platforms.
     */
    public static Cursor createCustomCursor(Object source, String cursorName) {
        try {

            Dimension d = Toolkit.getDefaultToolkit().getBestCursorSize(32, 32);
//          int colors = Toolkit.getDefaultToolkit().getMaximumCursorColors();

            // System.out.println("best cursor size would be "+d.width+" "+d.height);
            // System.out.println("cursor colors = "+colors);

            Image im = null;
            switch (d.width) {
            case 32:
                im = loadIcon(source, cursorName + "resources/32.gif").getImage();
                break;
            case 64:
                im = loadIcon(source, cursorName + "resources/64.gif").getImage();
                break;
            default:
                return new Cursor(Cursor.DEFAULT_CURSOR);
            }

            if (im == null) {
                return new Cursor(Cursor.DEFAULT_CURSOR);
            }

            Point p = new Point(4, 4);
            return Toolkit.getDefaultToolkit().createCustomCursor(im, p, cursorName);
        } catch (Exception e) {
            return new Cursor(Cursor.DEFAULT_CURSOR);
        }
    }

    // ******************* color *********************

    public static Color mixColors(Color c1, Color c2, float c2Part) {
        float rgba1[] = c1.getRGBComponents(null);
        float rgba2[] = c2.getRGBComponents(null);

        for (int i = 0; i < 4; i++) {
            rgba1[i] = rgba1[i] * (1 - c2Part) + rgba2[i] * c2Part;
        }

        return new Color(rgba1[0], rgba1[1], rgba1[2], rgba1[3]);
    }

    // ******************* file *********************

    public static String getFileExtension(File f) {
        String fileName = f.getName();
        int pointIndex = fileName.lastIndexOf('.');

        if (pointIndex < 0) {
            return null;
        }

        String extension = fileName.substring(pointIndex + 1);
        return extension;
    }

    public static String getFileDotExtension(File f) {
        String fileName = f.getName();
        int pointIndex = fileName.lastIndexOf('.');

        if (pointIndex < 0) {
            return null;
        }

        String extension = fileName.substring(pointIndex);
        return extension;
    }

    // ******************* mouse *********************

    public static boolean isShiftKey(MouseEvent e) {
        return (e.getModifiers() & InputEvent.SHIFT_MASK) != 0;
    }

    public static boolean isCtrlKey(MouseEvent e) {
        return (e.getModifiers() & InputEvent.CTRL_MASK) != 0;
    }

    public static boolean isAltKey(MouseEvent e) {
        return (e.getModifiers() & InputEvent.ALT_MASK) != 0;
    }

    public static boolean isButton1(MouseEvent e) {
        return (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0;
    }

    public static boolean isButton2(MouseEvent e) {
        return (e.getModifiers() & InputEvent.BUTTON2_MASK) != 0;
    }

    public static boolean isButton3(MouseEvent e) {
        return (e.getModifiers() & InputEvent.BUTTON3_MASK) != 0;
    }

    public static String getLaoeInstallationPath() {
        return System.getProperty("user.dir") + System.getProperty("file.separator");
    }

    public static String getLaoeUserHomePath() {
        return System.getProperty("user.home") + System.getProperty("file.separator") + ".laoe" + System.getProperty("file.separator");
    }

    public static void setFrameInMiddleOfScreen(JFrame frame) {
        Dimension size = frame.getSize();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
    }

}
