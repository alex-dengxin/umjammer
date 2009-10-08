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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.laoe.clip.AChannelMask;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GEditableSegments;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to edit the mask.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 27.02.02 first draft oli4
 */
public class GPMask extends GPluginFrame {
    public GPMask(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "mask";
    }

    public void start() {
        super.start();
        pluginHandler.setFocussedPlugin(this);
    }

    private GEditableSegments actualSegments;

    private AChannelMask actualMask;

    // mouse-event dispatching

    public void mousePressed(MouseEvent e) {
        actualMask = getSelectedLayer().getChannel(e.getPoint()).getMask();
        actualSegments = actualMask.getSegments();
        actualSegments.mousePressed(e);
        repaintFocussedClipEditor();
    }

    public void mouseClicked(MouseEvent e) {
        actualSegments.mouseClicked(e);
        repaintFocussedClipEditor();
    }

    public void mouseEntered(MouseEvent e) {
        if (actualSegments != null) {
            actualSegments.mouseEntered(e);
        }
        repaintFocussedClipEditor();
    }

    public void mouseMoved(MouseEvent e) {
        try {
            actualMask = getSelectedLayer().getChannel(e.getPoint()).getMask();
            actualSegments = actualMask.getSegments();

            if (actualSegments != null) {
                actualSegments.mouseMoved(e);
                updateActivePoint();
            }
            repaintFocussedClipEditor();
        } catch (Exception exc) {
        }
    }

    public void mouseDragged(MouseEvent e) {
        actualSegments.mouseDragged(e);
        updateActivePoint();
        repaintFocussedClipEditor();
    }

    // ********************* GUI **********************

    private UiControlText activeX, activeY;

    private JButton clear, complementary, applyDefinitely;

    private EventDispatcher eventDispatcher;

    public void initGui() {

        // mask editing
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 8, 4);
        l2.setPreferredCellSize(new Dimension(30, 35));
        p2.setLayout(l2);

        l2.add(new JLabel(GLanguage.translate("x")), 0, 0, 4, 1);
        activeX = new GControlTextX(getMain(), 9, false, true);
        activeX.setDataRange(0, 1e10);
        activeX.setData(0);
        activeX.setEditable(false);
        l2.add(activeX, 0, 1, 4, 1);

        l2.add(new JLabel(GLanguage.translate("y")), 4, 0, 4, 1);
        activeY = new GControlTextA(9, false, true);
        activeY.setDataRange(0, 1);
        activeY.setData(0);
        activeY.setEditable(false);
        l2.add(activeY, 4, 1, 4, 1);

        clear = new JButton(GLanguage.translate("clear"));
        l2.add(clear, 0, 2, 4, 1);

        complementary = new JButton(GLanguage.translate("complement"));
        l2.add(complementary, 4, 2, 4, 1);

        applyDefinitely = new JButton(GLanguage.translate("applyDefinitely"));
        l2.add(applyDefinitely, 0, 3, 4, 1);

        frame.getContentPane().add(p2);
        pack();

        eventDispatcher = new EventDispatcher();
        clear.addActionListener(eventDispatcher);
        complementary.addActionListener(eventDispatcher);
        applyDefinitely.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            AChannelMask m = getFocussedClip().getSelectedLayer().getSelectedChannel().getMask();
            if (e.getSource() == clear) {
                Debug.println(1, "plugin " + getName() + " [clear] clicked");
                m.clear();
                repaintFocussedClipEditor();
            } else if (e.getSource() == complementary) {
                Debug.println(1, "plugin " + getName() + " [complementary] clicked");
                m.setComplementary();
                repaintFocussedClipEditor();
            } else if (e.getSource() == applyDefinitely) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply definitely] clicked");
                m.applyDefinitely();
                reloadFocussedClipEditor();
                GProgressViewer.setProgress(100);
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            }
        }
    }

    private void updateActivePoint() {
        activeX.setData(actualSegments.getActivePointX());
        activeY.setData(actualSegments.getActivePointY());
    }

}
