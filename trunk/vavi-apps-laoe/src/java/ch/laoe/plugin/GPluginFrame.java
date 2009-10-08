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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import ch.laoe.ui.Debug;
import ch.laoe.ui.GDialog;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GPersistance;
import ch.oli4.ui.UiPersistanceEvent;


/**
 * parent class of all plugins with frames.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 30.08.00 erster Entwurf oli4 <br>
 *          05.01.03 endless plugin-focussing bug fixed oli4 <br>
 */
public abstract class GPluginFrame extends GPlugin implements WindowListener {
    public GPluginFrame(GPluginHandler ph) {
        super(ph);
        frame = new JFrame();

        // load plugin-icon
        ImageIcon i = loadIcon();
        if (i != null) {
            frame.setIconImage(i.getImage());
        } else {
            // if no plugin-icon then try the LAoE icon...
            URL u = getClass().getResource("resources/laoe.gif");
            if (u != null) {
                frame.setIconImage(new ImageIcon(u).getImage());
            }
        }
        frame.addWindowListener(this);
        frame.setTitle(GLanguage.translate(getName()));
        restoreLocation();

        autoCloseManager.addPluginFrame(this);
    }

    public void onBackup(UiPersistanceEvent e) {
        backupLocation();
    }

    protected JFrame frame;

    public boolean isVisible() {
        return frame.isVisible();
    }

    protected void pack() {
        frame.pack();
        frame.setResizable(false);
    }

    // autoclose

    /**
     * global setting of autoclose
     */
    private static final boolean autoCloseFrameEnable = GPersistance.createPersistance().getBoolean("plugin.autoCloseFrame");

    /**
     * not used anymore
     */
    protected void autoCloseFrame() {
        /*
         * if (autoCloseFrameEnable) { frame.setVisible(false); }
         */
    }

    /**
     * individual timestamp of last time activated frame
     */
    private int activateTimeStamp;

    /**
     * central autoclose time manager
     */
    private static AutoCloseManager autoCloseManager;

    static {
        autoCloseManager = new AutoCloseManager();
    }

    static class AutoCloseManager implements Runnable {
        private Thread thread;

        public AutoCloseManager() {
            frames = new ArrayList<GPluginFrame>(100);
            if (autoCloseFrameEnable) {
                thread = new Thread(this);
                thread.start();
            }
        }

        private List<GPluginFrame> frames;

        /**
         * plugins who want to use this autoclose-service, must register here.
         */
        public void addPluginFrame(GPluginFrame pf) {
            frames.add(pf);
        }

        // actual time, the unit varies from 2..20s, depending of population of frames
        private int time = 0;

        /**
         * returns the time of next close, in unit of 10s, given the last seting.
         */
        public int updateAutoCloseTime(int oldValue) {
            // already loaded ?
            if (oldValue > time) {
                // prolong the timeout...
                // System.out.println(""+new Date()+": prolong timeout to "+(oldValue + 6));
                return oldValue + 6;
            } else {
                // set a timeout...
                // System.out.println(""+new Date()+": set initial timeout to "+(time + 20));
                return time + 20;
            }
        }

        public void run() {
            while (true) {
                // count number of visible frames...
                int numberOfVisibleFrames = 0;
                for (int i = 0; i < frames.size(); i++) {
                    GPluginFrame pf = frames.get(i);

                    // visible ?
                    if (pf.frame.isVisible()) {
                        numberOfVisibleFrames++;
                    }
                }

                // close a timeout-frame...
                if (numberOfVisibleFrames > 4) {
                    for (int i = 0; i < frames.size(); i++) {
                        GPluginFrame pf = frames.get(i);

                        // visible ?
                        if (pf.frame.isVisible()) {
                            // is it time to close ?
                            if (pf.activateTimeStamp < time) {
                                pf.frame.setVisible(false);
                                Debug.println(2, "autoclose plugin-frame " + pf.getName());
                                break;
                            }
                        }
                    }
                }

                // update the time, the more frames are open, the faster runs the time...
                time++;
                if (numberOfVisibleFrames > 20) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                    }
                } else if (numberOfVisibleFrames > 10) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                    }
                } else if (numberOfVisibleFrames > 5) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                    }
                } else {
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException ie) {
                    }
                }
                // System.out.println("now "+time);
            }
        }
    }

    // persistent location and visibility

    private boolean visibleOnExit;

    public boolean wasVisibleOnExit() {
        return visibleOnExit;
    }

    /**
     * restore the location
     */
    public void restoreLocation() {
        // reduce location to actual screensize, to avoid invisible frames...
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = Math.min(persistance.getInt("plugin." + getName() + ".frameLocation.x"), d.width - 100);
        int y = Math.min(persistance.getInt("plugin." + getName() + ".frameLocation.y"), d.height - 100);
        frame.setLocation(x, y);

        visibleOnExit = persistance.getBoolean("plugin." + getName() + ".visible");
    }

    /**
     * backup the location
     */
    public void backupLocation() {
        persistance.setInt("plugin." + getName() + ".frameLocation.x", frame.getLocation().x);
        persistance.setInt("plugin." + getName() + ".frameLocation.y", frame.getLocation().y);
        persistance.setBoolean("plugin." + getName() + ".visible", frame.isVisible());
    }

    /**
     * shows a modal dialog on top of the plugin-frame
     */
    public void showErrorDialog(String title, String details) {
        GDialog.showErrorDialog(frame, title, details);
    }

    // interactions with ui

    public void start() {
        super.start();
        if (!frame.isVisible()) {
            frame.setVisible(true);
            frame.setState(Frame.NORMAL);
        }
        repaintFocussedClipEditor();
    }

    // window listener
    public void windowActivated(WindowEvent e) {
        start();

        if (getFocussedClip() != null) {
            reload();
            repaintFocussedClipEditor();
            activateTimeStamp = autoCloseManager.updateAutoCloseTime(activateTimeStamp);
        }
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        // pluginHandler.setFocussedPlugin(null);
        Debug.println(2, "close plugin-frame " + getName());
        frame.setVisible(false);
        repaintFocussedClipEditor();
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void setState(int state) {
        frame.setState(state);
    }

    public int getState() {
        return frame.getState();
    }
}
