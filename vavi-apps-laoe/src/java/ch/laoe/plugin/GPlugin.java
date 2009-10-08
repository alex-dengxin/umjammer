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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipEditor;
import ch.laoe.ui.GClipFrame;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GMain;
import ch.laoe.ui.GPersistance;
import ch.laoe.ui.GToolkit;
import ch.oli4.ui.UiPersistanceEvent;
import ch.oli4.ui.UiPersistanceListener;


/**
 * parent class of all plugins.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 30.08.00 erster Entwurf oli4 <br>
 *          28.03.01 set focussed plugin individually oli4
 */
public abstract class GPlugin implements ActionListener, MouseListener, MouseMotionListener, UiPersistanceListener {
    /**
     * constructor
     */
    public GPlugin(GPluginHandler ph) {
        setPluginHandler(ph);
        persistance = GPersistance.createPersistance();
        persistance.addPersistanceListener(this);
    }

    // name

    /**
     * returns the normal one-word name of the plugin.
     * 
     * @return
     */
    protected abstract String getName();

    /**
     * returns the name of the plugin and a short description. the format has no restriction, typically html format is used. if
     * not used, return the one-word name of the plugin.
     * 
     * @return
     */
    protected final String getDescription() {
        return getName() + "Description";
    }

    // environement
    protected GPluginHandler pluginHandler;

    protected GPersistance persistance;

    protected void setPluginHandler(GPluginHandler ph) {
        pluginHandler = ph;
    }

    public boolean isVisible() {
        return false;
    }

    // clip and clipeditor access

    protected AClip getFocussedClip() {
        try {
            return pluginHandler.getFocussedClipEditor().getClip();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    protected ALayer getSelectedLayer() {
        try {
            return pluginHandler.getFocussedClipEditor().getClip().getSelectedLayer();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    protected GClipEditor getFocussedClipEditor() {
        return pluginHandler.getFocussedClipEditor();
    }

    protected GClipFrame getFocussedClipFrame() {
        return getMain().getFocussedClipFrame();
    }

    protected void autoScaleFocussedClip() {
        try {
            pluginHandler.getFocussedClipEditor().getClip().getClipPlotter().autoScale();
        } catch (NullPointerException npe) {
        }
    }

    protected final void repaintFocussedClipEditor() {
        try {
            pluginHandler.getFocussedClipEditor().repaint();
        } catch (NullPointerException npe) {
        }
    }

    protected final void reloadFocussedClipEditor() {
        try {
            pluginHandler.getFocussedClipEditor().reload();
            // pluginHandler.reloadAllPluginsAndFocussedClip();
        } catch (NullPointerException npe) {
        }
    }

    protected void updateHistory(String s) {
        try {
            getFocussedClip().getHistory().store(loadIcon(), s);
            updateFrameTitle();
        } catch (NullPointerException npe) {
        }
    }

    protected void updateFrameTitle() {
        // update frame title...
        if (getFocussedClip().getHistory().hasUnsavedModifications()) {
            getFocussedClipFrame().setTitle(getFocussedClip().getName() + " *");
        } else {
            getFocussedClipFrame().setTitle(getFocussedClip().getName());
        }
    }

    protected GMain getMain() {
        return pluginHandler.getMain();
    }

    protected ImageIcon loadIcon(String iconName) {
        return GToolkit.loadIcon(this, iconName);
    }

    private ImageIcon icon;

    protected ImageIcon loadIcon() {
        if (icon == null) {
            icon = GToolkit.loadIcon(this, getName() + ".gif");
        }
        return icon;
    }

    // button and menuItem to start that plugin

    /**
     * helps the children to create their buttons
     */
    protected JButton createButton(String iconName, String toolTip) {
        // JButton b = new JButton(new ImageIcon(iconName));
        JButton b = new JButton(loadIcon(iconName));

        b.setToolTipText(GLanguage.translate(toolTip));
        b.setPreferredSize(new Dimension(26, 26));
        b.addActionListener(this);
        return b;
    }

    public JButton createButton() {
        return createButton(getName() + ".gif", getDescription());
    }

    /**
     * helps the children to create their menuitems. if no icon should be used, set it to null. if no key accelerator should be
     * used, set it to 0.
     */
    protected JMenuItem createMenuItem(String iconName, String text, int key) {
        JMenuItem mi;
        String translatedText = GLanguage.translate(text);

        if (this instanceof GPluginFrame)
            translatedText = new String(translatedText + "...");

        if (iconName != null) {
            mi = new JMenuItem(translatedText, loadIcon(iconName));
        } else {
            mi = new JMenuItem(translatedText);
        }

        if (key != 0)
            mi.setAccelerator(KeyStroke.getKeyStroke(key, ActionEvent.CTRL_MASK));

        mi.addActionListener(this);
        return mi;
    }

    public JMenuItem createMenuItem() {
        return createMenuItem(getName() + ".gif", getName(), 0);
    }

    public JMenuItem createMenuItem(int key) {
        return createMenuItem(getName() + ".gif", getName(), key);
    }

    // interactions with ui

    /**
     * called when this plugin gets active and focussed
     */
    public void start() {
        Debug.println(5, "plugin " + getClass().getName() + " starts");
        pluginHandler.addToPluginHistory(this);
    }

    /**
     * called by ui if a plugin should reload its values
     */
    public void reload() {
        Debug.println(5, "plugin " + getClass().getName() + " reloads");
    }

    /**
     * called by ui when this plugin needs to print onto the clip
     */
    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
    }

    // mouse-events come from clip-editor

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        ((Component) e.getSource()).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    // cursors

    protected Cursor createCustomCursor(String cursorName) {
        return GToolkit.createCustomCursor(this, cursorName);
    }

    // persistance listener

    /**
     * do not use "on restore", each class is responsible of its own restoring, e.g. at construction time.
     */
    public final void onRestore(UiPersistanceEvent e) {
    }

    public void onBackup(UiPersistanceEvent e) {
    }

    // ActionListener

    /**
     * called from created button or menu-item
     */
    public final void actionPerformed(ActionEvent e) {
        start();
    }

    // shift, alt, ctrl, mouse - keys

    protected static boolean isShiftKey(MouseEvent e) {
        return GToolkit.isShiftKey(e);
    }

    protected static boolean isCtrlKey(MouseEvent e) {
        return GToolkit.isCtrlKey(e);
    }

    protected static boolean isAltKey(MouseEvent e) {
        return GToolkit.isAltKey(e);
    }

    protected static boolean isButton1(MouseEvent e) {
        return GToolkit.isButton1(e);
    }

    protected static boolean isButton2(MouseEvent e) {
        return GToolkit.isButton2(e);
    }

    protected static boolean isButton3(MouseEvent e) {
        return GToolkit.isButton3(e);
    }

    protected static String getLaoeInstallationPath() {
        return GToolkit.getLaoeInstallationPath();
    }

}
