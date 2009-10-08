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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOClickReduction;
import ch.laoe.operation.AOClippReparing;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to browse the manual.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 05.06.01 first draft oli4 <br>
 *          08.07.02 add clipp removing oli4
 */
public class GPNoiseReduction extends GPluginFrame {
    public GPNoiseReduction(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "noiseReduction";
    }

    // GUI
    private UiControlText sense, smooth;

    private UiControlText clippMaxWidth, clippMinDerivation;

    private JButton applyClickReduction;

    private JButton applyClippRemoving;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // click reduction tab
        JPanel p1 = new JPanel();
        UiCartesianLayout cl1 = new UiCartesianLayout(p1, 10, 5);
        cl1.setPreferredCellSize(new Dimension(25, 35));
        p1.setLayout(cl1);

        cl1.add(new JLabel(GLanguage.translate("sense")), 0, 0, 4, 1);
        sense = new GControlTextA(10, true, true);
        sense.setDataRange(.1, 1);
        sense.setData(.7);
        cl1.add(sense, 4, 0, 6, 1);

        cl1.add(new JLabel(GLanguage.translate("smooth")), 0, 1, 4, 1);
        smooth = new GControlTextX(getMain(), 10, true, true);
        smooth.setDataRange(0, 100);
        smooth.setData(40);
        cl1.add(smooth, 4, 1, 6, 1);

        applyClickReduction = new JButton(GLanguage.translate("apply"));
        cl1.add(applyClickReduction, 3, 4, 4, 1);

        tabbedPane.add(GLanguage.translate("clickReduction"), p1);

        // clipp removing tab
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 10, 5);
        l2.setPreferredCellSize(new Dimension(25, 35));
        p2.setLayout(l2);

        l2.add(new JLabel(GLanguage.translate("maxWidth")), 0, 0, 4, 1);
        clippMaxWidth = new GControlTextX(getMain(), 10, true, true);
        clippMaxWidth.setDataRange(1, 10000);
        clippMaxWidth.setData(1000);
        l2.add(clippMaxWidth, 4, 0, 6, 1);

        l2.add(new JLabel(GLanguage.translate("minYStep")), 0, 1, 4, 1);
        clippMinDerivation = new GControlTextA(10, true, true);
        clippMinDerivation.setDataRange(.1, 1);
        clippMinDerivation.setData(.9);
        l2.add(clippMinDerivation, 4, 1, 6, 1);

        applyClippRemoving = new JButton(GLanguage.translate("apply"));
        l2.add(applyClippRemoving, 3, 4, 4, 1);

        tabbedPane.add(GLanguage.translate("clippRemoving"), p2);

        // future tabs...

        frame.getContentPane().add(tabbedPane);
        pack();

        eventDispatcher = new EventDispatcher();
        applyClickReduction.addActionListener(eventDispatcher);
        applyClippRemoving.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == applyClickReduction) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply click reduction] clicked");
                onApplyClickReduction();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == applyClippRemoving) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply clipp removing] clicked");
                onApplyClippRemoving();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            }

        }
    }

    private void onApplyClickReduction() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOClickReduction((float) sense.getData(), (int) smooth.getData()));
    }

    private void onApplyClippRemoving() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOClippReparing((int) clippMaxWidth.getData(), (float) clippMinDerivation.getData()));
    }
}
