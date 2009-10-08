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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 * installs a user if not existing or corrupt.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 28.04.2003 first draft oli4
 */
public class GUserInstaller implements ActionListener {
    private GUserInstaller() {

    }

    private static GUserInstaller installer = null;

    /**
     * returns the singleton.
     * 
     * @return
     */
    public static GUserInstaller getInstance() {
        if (installer == null) {
            installer = new GUserInstaller();
        }
        return installer;
    }

    private File dotLaoeDir = new File(GToolkit.getLaoeUserHomePath());

    /**
     * perform a modal dialog to install a user. the dialog starts only the very first time of use of LAoE per user.
     */
    public void maybeInstall() {
        // check if installed...
        if (dotLaoeDir.exists() && dotLaoeDir.isDirectory()) {
            return;
        }
        forceInstallation();
    }

    /**
     * perform a modal dialog to install a user. the dialog starts conditionless
     */
    public void forceInstallation() {
        // prepare frame...
        frame = new JFrame();
        URL u = getClass().getResource("resources/laoe.gif");
        if (u != null) {
            frame.setIconImage(new ImageIcon(u).getImage());
        }
        frame.setTitle("LAoE installer");

        // panel
        JPanel p = new JPanel(new BorderLayout());
        frame.getContentPane().add(p);
        p.add(new JLabel(GToolkit.loadIcon(this, "resources/installWindow.gif")), BorderLayout.CENTER);
        JPanel sp = new JPanel(new GridLayout(2, 1));
        p.add(sp, BorderLayout.SOUTH);

        text = new JLabel("", SwingConstants.CENTER);
        text.setText("<html><body>do you want to install <font size=+1><b><i>LAoE</i></b></font> ?</body></html>");
        sp.setBackground(Color.BLACK);
        text.setForeground(Color.WHITE);
        sp.add(text);

        JPanel buttonP = new JPanel();
        FlowLayout buttonL = new FlowLayout(FlowLayout.CENTER);
        buttonP.setLayout(buttonL);
        sp.add(buttonP);
        installB = new JButton("install now!");
        buttonP.add(installB);
        cancelB = new JButton("cancel");
        buttonP.add(cancelB);

        frame.pack();
        GToolkit.setFrameInMiddleOfScreen(frame);
        frame.setVisible(true);

        installB.addActionListener(this);
        cancelB.addActionListener(this);

        // modal processing...
        while (frame.isVisible()) {
            sleep(333);

            switch (action) {
            case CANCEL:
                cancelNow();
                System.exit(0);
                break;

            case INSTALL:
                installNow();
                frame.setVisible(false);
                break;
            }
        }
    }

    private JFrame frame;

    private JButton installB, cancelB;

    private JLabel text;

    private static final int NO = 0;

    private static final int CANCEL = 1;

    private static final int INSTALL = 2;

    private int action = NO;

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == installB) {
            action = INSTALL;
        } else if (e.getSource() == cancelB) {
            action = CANCEL;
        }
    }

    private void installNow() {
        text.setText("create LAoE directory in home directory of " + System.getProperty("user.name") + "...");
        dotLaoeDir.mkdir();
        sleep(2000);

        text.setText("prepare the history...");
        copyDirectory("history");
        copyFile("history/read_me.txt");
        sleep(2000);

        text.setText("prepare the user settings...");
        copyFile("laoe.properties");
        sleep(2000);

        text.setText("successful installation of LAoE!");
        sleep(2000);
    }

    public void cancelNow() {
        text.setText("installation cancelled.");
        sleep(2000);
    }

    private void copyDirectory(String name) {
        File orig = new File(GToolkit.getLaoeInstallationPath() + name);
        File copy = new File(GToolkit.getLaoeUserHomePath() + name);

        if (orig.exists() && orig.isDirectory()) {
            copy.mkdir();
        }
    }

    private void copyFile(String name) {
        File orig = new File(GToolkit.getLaoeInstallationPath() + name);
        File copy = new File(GToolkit.getLaoeUserHomePath() + name);

        InputStream is = null;
        OutputStream os = null;

        if (orig.exists() && orig.isFile()) {
            try {
                is = new FileInputStream(orig);
                os = new FileOutputStream(copy);
                byte b[] = new byte[1024];
                int n = 0;

                while (true) {
                    n = is.read(b);

                    if (n > 0) {
                        os.write(b, 0, n);
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
