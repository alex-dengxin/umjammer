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
import ch.laoe.operation.AOMath;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipLayerChooser;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to directly calculate with samples
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 11.06.01 first draft oli4 <br>
 *          20.08.01 add two-channel-operations oli4
 */
public class GPMath extends GPluginFrame {
    public GPMath(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "math";
    }

    // GUI
    private String operationItemConst[] = {
        GLanguage.translate("add"), GLanguage.translate("subtract"), GLanguage.translate("multiply"), GLanguage.translate("divide"), GLanguage.translate("invers"), GLanguage.translate("neg"), GLanguage.translate("pow"), GLanguage.translate("sqrt"), GLanguage.translate("derivate"), GLanguage.translate("integral"), GLanguage.translate("exp"), GLanguage.translate("log"), GLanguage.translate("toDb"), GLanguage.translate("fromDb"), GLanguage.translate("mean"), GLanguage.translate("RMS"),
        GLanguage.translate("complement")
    };

    private String operationItemVar[] = {
        GLanguage.translate("add"), GLanguage.translate("subtract"), GLanguage.translate("multiply"), GLanguage.translate("divide")
    };

    private JComboBox operationConst, operationVar;

    private UiControlText operand1, operand2;

    private GClipLayerChooser layerChooser;

    private JButton applyConst, applyVar;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JTabbedPane tab = new JTabbedPane();

        // const tab...
        JPanel pConst = new JPanel();
        UiCartesianLayout lConst = new UiCartesianLayout(pConst, 10, 5);
        lConst.setPreferredCellSize(new Dimension(25, 35));
        pConst.setLayout(lConst);

        lConst.add(new JLabel(GLanguage.translate("operation")), 0, 0, 4, 1);
        operationConst = new JComboBox(operationItemConst);
        lConst.add(operationConst, 4, 0, 6, 1);

        lConst.add(new JLabel(GLanguage.translate("operand") + " 1"), 0, 1, 4, 1);
        operand1 = new GControlTextA(10, true, true);
        operand1.setDataRange(-1e9, 1e9);
        operand1.setData(0);
        lConst.add(operand1, 4, 1, 6, 1);

        lConst.add(new JLabel(GLanguage.translate("operand") + " 2"), 0, 2, 4, 1);
        operand2 = new GControlTextA(10, true, true);
        operand2.setDataRange(-1e9, 1e9);
        operand2.setData(0);
        lConst.add(operand2, 4, 2, 6, 1);

        applyConst = new JButton(GLanguage.translate("apply"));
        lConst.add(applyConst, 3, 4, 4, 1);

        tab.add(GLanguage.translate("simple"), pConst);

        // variable tab...
        JPanel pVar = new JPanel();
        UiCartesianLayout lVar = new UiCartesianLayout(pVar, 10, 5);
        lVar.setPreferredCellSize(new Dimension(25, 35));
        pVar.setLayout(lVar);

        lVar.add(new JLabel(GLanguage.translate("operation")), 0, 0, 4, 1);
        operationVar = new JComboBox(operationItemVar);
        lVar.add(operationVar, 4, 0, 6, 1);

        layerChooser = new GClipLayerChooser(getMain(), "operandCurve");
        lVar.add(layerChooser, 0, 1, 10, 3);

        applyVar = new JButton(GLanguage.translate("apply"));
        lVar.add(applyVar, 3, 4, 4, 1);

        tab.add(GLanguage.translate("f(time)"), pVar);

        frame.getContentPane().add(tab);
        pack();

        eventDispatcher = new EventDispatcher();
        applyConst.addActionListener(eventDispatcher);
        applyVar.addActionListener(eventDispatcher);
        operationConst.addActionListener(eventDispatcher);
        updateActiveComponents();
    }

    private void updateActiveComponents() {
        switch (operationConst.getSelectedIndex()) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 6:
        case 14:
        case 15:
            operand1.setEnabled(true);
            operand2.setEnabled(false);
            break;

        default:
            operand1.setEnabled(false);
            operand2.setEnabled(false);
            break;
        }
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == operationConst) {
                Debug.println(1, "plugin " + getName() + " [operation const] clicked");
                updateActiveComponents();
            } else if (e.getSource() == applyConst) {
                GProgressViewer.start(getName());
                Debug.println(1, "plugin " + getName() + " [apply const] clicked");
                onApplyConst();
                GProgressViewer.finish();
            } else if (e.getSource() == applyVar) {
                GProgressViewer.start(getName());
                Debug.println(1, "plugin " + getName() + " [apply var] clicked");
                onApplyVar();
                GProgressViewer.finish();
            }
        }
    }

    public void reload() {
        layerChooser.reload();
    }

    private void onApplyConst() {
        ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
        float o[] = {
            (float) operand1.getData(), (float) operand2.getData()
        };
        // operation ?
        switch (operationConst.getSelectedIndex()) {
        case 0:
            ls.operateEachChannel(new AOMath(AOMath.ADD, o));
            break;

        case 1:
            ls.operateEachChannel(new AOMath(AOMath.SUBTRACT, o));
            break;

        case 2:
            ls.operateEachChannel(new AOMath(AOMath.MULTIPLY, o));
            break;

        case 3:
            ls.operateEachChannel(new AOMath(AOMath.DIVIDE, o));
            break;

        case 4:
            ls.operateEachChannel(new AOMath(AOMath.INVERS, o));
            break;

        case 5:
            ls.operateEachChannel(new AOMath(AOMath.NEG, o));
            break;

        case 6:
            ls.operateEachChannel(new AOMath(AOMath.POW, o));
            break;

        case 7:
            ls.operateEachChannel(new AOMath(AOMath.SQRT, o));
            break;

        case 8:
            ls.operateEachChannel(new AOMath(AOMath.DERIVATE, o));
            break;

        case 9:
            ls.operateEachChannel(new AOMath(AOMath.INTEGRATE, o));
            break;

        case 10:
            ls.operateEachChannel(new AOMath(AOMath.EXP, o));
            break;

        case 11:
            ls.operateEachChannel(new AOMath(AOMath.LOG, o));
            break;

        case 12:
            ls.operateEachChannel(new AOMath(AOMath.TO_dB, o));
            break;

        case 13:
            ls.operateEachChannel(new AOMath(AOMath.FROM_dB, o));
            break;

        case 14:
            ls.operateEachChannel(new AOMath(AOMath.MEAN, o));
            break;

        case 15:
            ls.operateEachChannel(new AOMath(AOMath.RMS, o));
            break;

        case 16:
            o[0] = -1;
            o[1] = 0;
            ls.operateEachChannel(new AOMath(AOMath.MULTIPLY, o));
            o[0] = 1;
            ls.operateEachChannel(new AOMath(AOMath.ADD, o));
            break;

        }

        // reload clip
        updateHistory(GLanguage.translate(getName()));
        reloadFocussedClipEditor();
        autoCloseFrame();
    }

    private void onApplyVar() {
        AClipSelection cs = new AClipSelection(new AClip());
        cs.addLayerSelection(getFocussedClip().getSelectedLayer().getLayerSelection());
        cs.addLayerSelection(layerChooser.getSelectedLayer().createLayerSelection());

        // operation ?
        switch (operationVar.getSelectedIndex()) {
        case 0:
            cs.operateLayer0WithLayer1(new AOMath(AOMath.ADD));
            break;

        case 1:
            cs.operateLayer0WithLayer1(new AOMath(AOMath.SUBTRACT));
            break;

        case 2:
            cs.operateLayer0WithLayer1(new AOMath(AOMath.MULTIPLY));
            break;

        case 3:
            cs.operateLayer0WithLayer1(new AOMath(AOMath.DIVIDE));
            break;

        }

        // reload clip
        updateHistory(GLanguage.translate(getName()));
        reloadFocussedClipEditor();
        autoCloseFrame();
    }

}
