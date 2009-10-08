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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipSelection;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOCompressExpand;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChooser;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GControlTextY;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;
import ch.oli4.ui.UiPersistance;


/**
 * plugin compress/expand.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @terget JDK 1.3
 * 
 * @version 22.04.01 first draft oli4 <br>
 *          13.09.01 add variable transferfunction oli4 <br>
 */
public class GPCompressExpand extends GPluginFrame {
    public GPCompressExpand(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "compressExpand";
    }

    // GUI
    private JComboBox effect;

    private UiControlText attack, release;

    private UiControlText x0, y0, x1, y1, x2, y2;

    private JButton applySeg, applyVar;

    private GClipLayerChooser layerChooser;

    // presets
    private String presetsName[];

    private float x0Presets[], y0Presets[], x1Presets[], y1Presets[], x2Presets[], y2Presets[];

    private float attackPresets[], releasePresets[];

    private EventDispatcher eventDispatcher;

    private void initGui() {
        // presets
        UiPersistance persist = new UiPersistance(getName() + ".properties");
        persist.restore();
        int n = persist.getInt("numberOfPresets");
        presetsName = new String[n];
        x0Presets = new float[n];
        y0Presets = new float[n];
        x1Presets = new float[n];
        y1Presets = new float[n];
        x2Presets = new float[n];
        y2Presets = new float[n];
        attackPresets = new float[n];
        releasePresets = new float[n];

        for (int i = 0; i < n; i++) {
            presetsName[i] = GLanguage.translate(persist.getString("name_" + i));
            x0Presets[i] = persist.getFloat("x0_" + i);
            y0Presets[i] = persist.getFloat("y0_" + i);
            x1Presets[i] = persist.getFloat("x1_" + i);
            y1Presets[i] = persist.getFloat("y1_" + i);
            x2Presets[i] = persist.getFloat("x2_" + i);
            y2Presets[i] = persist.getFloat("y2_" + i);
            attackPresets[i] = persist.getFloat("attack_" + i);
            releasePresets[i] = persist.getFloat("release_" + i);
        }

        // GUI
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 10, 9);
        cl.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(cl);

        cl.add(new JLabel(GLanguage.translate("effect")), 0, 0, 4, 1);
        effect = new JComboBox(presetsName);
        cl.add(effect, 4, 0, 6, 1);

        cl.add(new JLabel(GLanguage.translate("attack")), 0, 1, 4, 1);
        attack = new GControlTextX(getMain(), 7, true, true);
        attack.setDataRange(1, 1e9);
        attack.setData(1000);
        cl.add(attack, 4, 1, 6, 1);

        cl.add(new JLabel(GLanguage.translate("release")), 0, 2, 4, 1);
        release = new GControlTextX(getMain(), 7, true, true);
        release.setDataRange(0, 1e9);
        release.setData(1000);
        cl.add(release, 4, 2, 6, 1);

        // segments tab
        JPanel pSeg = new JPanel();
        UiCartesianLayout lSeg = new UiCartesianLayout(pSeg, 10, 5);
        lSeg.setPreferredCellSize(new Dimension(25, 35));
        pSeg.setLayout(lSeg);

        lSeg.add(new JLabel(GLanguage.translate("factorUntil")), 0, 0, 4, 1);
        lSeg.add(new JLabel(GLanguage.translate("amplitude")), 5, 0, 4, 1);

        x0 = new GControlTextY(getMain(), 7, true, true);
        x0.setDataRange(0, 1e9);
        lSeg.add(x0, 5, 1, 5, 1);

        y0 = new GControlTextA(7, true, true);
        y0.setDataRange(-1e9, 1e9);
        lSeg.add(y0, 0, 1, 5, 1);

        x1 = new GControlTextY(getMain(), 7, true, true);
        x1.setDataRange(0, 1e9);
        lSeg.add(x1, 5, 2, 5, 1);

        y1 = new GControlTextA(7, true, true);
        y1.setDataRange(-1e9, 1e9);
        lSeg.add(y1, 0, 2, 5, 1);

        x2 = new GControlTextY(getMain(), 7, true, true);
        x2.setDataRange(0, 1e9);
        lSeg.add(x2, 5, 3, 5, 1);

        y2 = new GControlTextA(7, true, true);
        y2.setDataRange(-1e9, 1e9);
        lSeg.add(y2, 0, 3, 5, 1);

        applySeg = new JButton(GLanguage.translate("apply"));
        lSeg.add(applySeg, 3, 4, 4, 1);

        // variable tab
        JPanel pVar = new JPanel();
        UiCartesianLayout lVar = new UiCartesianLayout(pVar, 10, 5);
        lVar.setPreferredCellSize(new Dimension(25, 35));
        pVar.setLayout(lVar);

        layerChooser = new GClipLayerChooser(getMain(), "transferFunction");
        lVar.add(layerChooser, 0, 0, 10, 3);

        applyVar = new JButton(GLanguage.translate("apply"));
        lVar.add(applyVar, 3, 4, 4, 1);

        JTabbedPane tab = new JTabbedPane();
        tab.add(pSeg, GLanguage.translate("segments"));
        tab.add(pVar, GLanguage.translate("f(amplitude)"));
        cl.add(tab, 0, 3, 10, 6);

        frame.getContentPane().add(p);
        pack();

        eventDispatcher = new EventDispatcher();
        applySeg.addActionListener(eventDispatcher);
        applyVar.addActionListener(eventDispatcher);
        effect.addActionListener(eventDispatcher);
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == applySeg) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply segments] clicked");
                onApplySeg();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();
                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == applyVar) {
                GProgressViewer.start(getName());
                GProgressViewer.entrySubProgress();
                GProgressViewer.setProgress(70);
                Debug.println(1, "plugin " + getName() + " [apply var] clicked");
                onApplyVar();
                GProgressViewer.setProgress(100);
                reloadFocussedClipEditor();

                updateHistory(GLanguage.translate(getName()));
                autoCloseFrame();
                GProgressViewer.exitSubProgress();
                GProgressViewer.finish();
            } else if (e.getSource() == effect) {
                Debug.println(1, "plugin " + getName() + " [effect] clicked");
                onEffectChange();
            }

        }
    }

    public void reload() {
        layerChooser.reload();
    }

    private void onEffectChange() {
        float sr = getFocussedClip().getSampleRate();
//      float sw = (float) Math.pow(2, getFocussedClip().getSampleWidth() - 1);
//      float sm = getFocussedClip().getMaxSampleValue();

        int i = effect.getSelectedIndex();
        attack.setData(attackPresets[i] * sr);
        release.setData(releasePresets[i] * sr);
        x0.setData(x0Presets[i]);
        y0.setData(y0Presets[i]);
        x1.setData(x1Presets[i]);
        y1.setData(y1Presets[i]);
        x2.setData(x2Presets[i]);
        y2.setData(y2Presets[i]);
    }

    private void onApplySeg() {
        float fx[] = new float[4];
        fx[0] = 0;
        fx[1] = (float) x0.getData();
        fx[2] = (float) x1.getData();
        fx[3] = (float) x2.getData();

        float fy[] = new float[4];
        fy[0] = 0;
        fy[1] = (float) (y0.getData() * fx[1]);
        fy[2] = (float) (fy[1] + (fx[2] - fx[1]) * y1.getData());
        fy[3] = (float) (fy[2] + (fx[3] - fx[2]) * y2.getData());
        // System.out.println("fx="+fx[0]+" "+fx[1]+" "+fx[2]+" "+fx[3]);
        // System.out.println("fy="+fy[0]+" "+fy[1]+" "+fy[2]+" "+fy[3]);

        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ls.operateEachChannel(new AOCompressExpand((int) getFocussedClip().getSampleRate(), (int) attack.getData(), (int) release.getData(), fx, fy));
    }

    private void onApplyVar() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        ALayerSelection ps = layerChooser.getSelectedLayer().createLayerSelection();
        AClipSelection cs = new AClipSelection(new AClip());
        cs.addLayerSelection(ls);
        cs.addLayerSelection(ps);

        cs.operateLayer0WithLayer1(new AOCompressExpand((int) getFocussedClip().getSampleRate(), (int) attack.getData(), (int) release.getData(), null, null));

    }

}
