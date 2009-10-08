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

package ch.laoe.plugin;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GMain;
import ch.oli4.ui.UiCartesianLayout;


/**
 * plugin: about-frame
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 08.10.00 erster Entwurf oli4 <br>
 *          27.04.2003 soundclip removed oli4
 * 
 */
public class GPAbout extends GPluginFrame {
    public GPAbout(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "about";
    }

    private void initGui() {

        /*
         * public void start () { super.start();
         */
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

        JLabel copyright = new JLabel(GMain.copyright, SwingConstants.CENTER);
        copyright.setOpaque(false);
        copyright.setForeground(color);
        cl.add(copyright, 0, 8, 10, 1);

        JLabel eMail = new JLabel(GLanguage.translate("homepage") + ": " + GMain.eAddress + "     " + GLanguage.translate("contact") + ": " + GMain.eMail, SwingConstants.CENTER);
        eMail.setOpaque(false);
        eMail.setForeground(color);
        cl.add(eMail, 0, 9, 10, 1);

        // add as last component (to put it to background)
        cl.add(new JLabel(loadIcon("resources/startWindow.gif")), 0, 0, 10, 10);

        frame.getContentPane().add(p);
        pack();

        // sound
        // aboutSound = Applet.newAudioClip(getClass().getResource("about.wav"));
    }

    //
    //
    //
    // //backgroud sound
    // private AudioClip aboutSound;
    //	
    // //window listener
    // public void windowActivated (WindowEvent e)
    // {
    // super.windowActivated(e);
    // if (aboutSound == null)
    // {
    // aboutSound = Applet.newAudioClip(getClass().getResource("about.wav"));
    // //System.out.println(""+getClass().getResource("about.wav"));
    // }
    // aboutSound.loop();
    // }
    //
    // public void windowDeactivated (WindowEvent e)
    // {
    // super.windowDeactivated(e);
    // if (aboutSound != null)
    // aboutSound.stop();
    // }

}
