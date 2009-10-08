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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GDialog;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GLayerViewer;
import ch.laoe.ui.GProgressViewer;
import ch.laoe.ui.GSimpleColorChooser;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * plugin to manage the layers by the layer stack.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 05.10.00 erster Entwurf oli4 <br>
 *          13.09.00 add copy/paste layer oli4 <br>
 *          23.09.00 add dialog on new layer oli4 <br>
 *          21.03.02 color-settings introduced oli4
 * 
 */
public class GPLayerStack extends GPluginFrame {
    public GPLayerStack(GPluginHandler ph) {
        super(ph);
        initGui();
        frame.setResizable(true);
    }

    protected String getName() {
        return "layerStack";
    }

    public JMenuItem createMenuItem() {
        return super.createMenuItem(KeyEvent.VK_L);
    }

    public void start() {
        super.start();
    }

    public void reload() {
        if (frame.isVisible() && (getFocussedClip() != null))
            onClipChange();
    }

    // GUI

    private JComboBox layerType, plotType;

    private GSimpleColorChooser layerColor;

    private JScrollPane scrollPane;

    private JPanel pScroll;

    private Vector layerPanels;

    private JButton newButton, upButton, downButton, copyButton, pasteButton, duplicateButton, deleteButton, mergeButton, mergeAllButton;

    private EventDispatcher eventDispatcher;

    private class EventDispatcher implements ActionListener, ChangeListener {
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() == layerType) {
                    Debug.println(1, "plugin " + getName() + " [layer type] clicked");
                    onLayerTypeChange(); // bug: event comes here infinitely!!!!!!!!!
                    return;
                }
                if (e.getSource() == plotType) {
                    Debug.println(1, "plugin " + getName() + " [plot type] clicked");
                    onPlotTypeChange(); // bug: event comes here infinitely!!!!!!!!!
                    return;
                }
                if (e.getSource() == layerColor) {
                    Debug.println(1, "plugin " + getName() + " [layer color] clicked");
                    onLayerColorChange(); // bug: event comes here infinitely!!!!!!!!!
                    return;
                }

                GProgressViewer.start(getName());
                if (e.getSource() == newButton) {
                    Debug.println(1, "plugin " + getName() + " [new] clicked");
                    onNewButton();
                } else if (e.getSource() == upButton) {
                    Debug.println(1, "plugin " + getName() + " [up] clicked");
                    onUpButton();
                } else if (e.getSource() == downButton) {
                    Debug.println(1, "plugin " + getName() + " [down] clicked");
                    onDownButton();
                } else if (e.getSource() == copyButton) {
                    Debug.println(1, "plugin " + getName() + " [copy] clicked");
                    onCopyButton();
                } else if (e.getSource() == pasteButton) {
                    Debug.println(1, "plugin " + getName() + " [paste] clicked");
                    onPasteButton();
                } else if (e.getSource() == duplicateButton) {
                    Debug.println(1, "plugin " + getName() + " [duplicate] clicked");
                    onDuplicateButton();
                } else if (e.getSource() == deleteButton) {
                    Debug.println(1, "plugin " + getName() + " [delete] clicked");
                    onDeleteButton();
                } else if (e.getSource() == mergeButton) {
                    Debug.println(1, "plugin " + getName() + " [merge] clicked");
                    onMergeButton();
                } else if (e.getSource() == mergeAllButton) {
                    Debug.println(1, "plugin " + getName() + " [merge all] clicked");
                    onMergeAllButton();
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            GProgressViewer.finish();
        }

        public void stateChanged(ChangeEvent e) {
        }

    }

    private void initGui() {
        // main structure
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel pTop = new JPanel();
        pTop.setLayout(new FlowLayout(FlowLayout.LEFT));
        pScroll = new JPanel();
        // pScroll.setLayout(new BoxLayout(pScroll, BoxLayout.Y_AXIS));
        JPanel pBottom = new JPanel();

        // top components
        // pTop.add(new JLabel(GLanguage.translate("layerType")));
        String layerTypeNames[] = {
            GLanguage.translate("audioLayer"), GLanguage.translate("parameterLayer")
        };
        layerType = new JComboBox(layerTypeNames);
        layerType.setToolTipText(GLanguage.translate("layerType"));
        pTop.add(layerType);

        // pTop.add(new JLabel(GLanguage.translate("plotType")));
        String plotTypeNames[] = {
            GLanguage.translate("sampleCurve"), GLanguage.translate("spectrogram")
        };
        plotType = new JComboBox(plotTypeNames);
        plotType.setToolTipText(GLanguage.translate("plotType"));
        pTop.add(plotType);

        // pTop.add(new JLabel(GLanguage.translate("layerColor")));
        layerColor = new GSimpleColorChooser();
        layerColor.setToolTipText(GLanguage.translate("layerColor"));
        pTop.add(layerColor);

        p.add(pTop);

        // layer stack
        scrollPane = new JScrollPane(pScroll, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(200, 200));
        p.add(scrollPane);

        // bottom buttons
        newButton = new JButton(loadIcon("resources/new.gif"));
        newButton.setToolTipText(GLanguage.translate("newLayer"));
        newButton.setPreferredSize(new Dimension(26, 26));
        pBottom.add(newButton);
        upButton = new JButton(loadIcon("resources/up.gif"));
        upButton.setToolTipText(GLanguage.translate("moveLayerUp"));
        upButton.setPreferredSize(new Dimension(26, 26));
        pBottom.add(upButton);
        downButton = new JButton(loadIcon("resources/down.gif"));
        downButton.setToolTipText(GLanguage.translate("moveLayerDown"));
        downButton.setPreferredSize(new Dimension(26, 26));
        pBottom.add(downButton);
        copyButton = new JButton(loadIcon("resources/copy.gif"));
        copyButton.setToolTipText(GLanguage.translate("copyLayer"));
        copyButton.setPreferredSize(new Dimension(26, 26));
        pBottom.add(copyButton);
        pasteButton = new JButton(loadIcon("resources/paste.gif"));
        pasteButton.setToolTipText(GLanguage.translate("pasteLayer"));
        pasteButton.setPreferredSize(new Dimension(26, 26));
        pBottom.add(pasteButton);
        duplicateButton = new JButton(loadIcon("resources/duplicate.gif"));
        duplicateButton.setToolTipText(GLanguage.translate("duplicateLayer"));
        duplicateButton.setPreferredSize(new Dimension(26, 26));
        pBottom.add(duplicateButton);
        deleteButton = new JButton(loadIcon("resources/delete.gif"));
        deleteButton.setToolTipText(GLanguage.translate("deleteLayer"));
        deleteButton.setPreferredSize(new Dimension(26, 26));
        pBottom.add(deleteButton);
        mergeButton = new JButton(loadIcon("resources/mergeUpLayer.gif"));
        mergeButton.setToolTipText(GLanguage.translate("mergeUpLayer"));
        mergeButton.setPreferredSize(new Dimension(26, 26));
        pBottom.add(mergeButton);
        mergeAllButton = new JButton(loadIcon("resources/mergeAllLayers.gif"));
        mergeAllButton.setToolTipText(GLanguage.translate("mergeAllLayers"));
        mergeAllButton.setPreferredSize(new Dimension(26, 26));
        pBottom.add(mergeAllButton);
        p.add(pBottom);

        frame.getContentPane().add(p);
        frame.setSize(new Dimension(400, 300));
        // frame.pack();

        eventDispatcher = new EventDispatcher();
        layerType.addActionListener(eventDispatcher);
        plotType.addActionListener(eventDispatcher);
        layerColor.addActionListener(eventDispatcher);
        newButton.addActionListener(eventDispatcher);
        upButton.addActionListener(eventDispatcher);
        downButton.addActionListener(eventDispatcher);
        copyButton.addActionListener(eventDispatcher);
        pasteButton.addActionListener(eventDispatcher);
        duplicateButton.addActionListener(eventDispatcher);
        deleteButton.addActionListener(eventDispatcher);
        mergeButton.addActionListener(eventDispatcher);
        mergeAllButton.addActionListener(eventDispatcher);
    }

    // update methods

    private void updateThisAndFocussedClip() {
        frame.validate();
        frame.repaint();
        pluginHandler.getFocussedClipEditor().repaint();
    }

    private void reloadFocussedClip() {
        pluginHandler.getFocussedClipEditor().reload();
    }

    // reactions

    private void onLayerTypeChange() {
        switch (layerType.getSelectedIndex()) {
        case 0:
            pluginHandler.getFocussedClip().getSelectedLayer().setType(ALayer.AUDIO_LAYER);
            break;

        case 1:
            pluginHandler.getFocussedClip().getSelectedLayer().setType(ALayer.PARAMETER_LAYER);
            break;
        }

        onClipChange();
        // updateHistory(GLanguage.translate("layerType")); //is called too much...
        updateThisAndFocussedClip();
        reloadFocussedClip();
    }

    private void onPlotTypeChange() {
        switch (plotType.getSelectedIndex()) {
        case 0:
            pluginHandler.getFocussedClip().getSelectedLayer().setPlotType(ALayer.SAMPLE_CURVE_TYPE);
            break;

        case 1:
            pluginHandler.getFocussedClip().getSelectedLayer().setPlotType(ALayer.SPECTROGRAM_TYPE);
            break;
        }

        pluginHandler.getFocussedClip().getSelectedLayer().getLayerPlotter().autoScaleY();
        onClipChange();
        // updateHistory(GLanguage.translate("layerType")); //is called too much...
        updateThisAndFocussedClip();
        reloadFocussedClip();
    }

    private void onLayerColorChange() {
        pluginHandler.getFocussedClip().getSelectedLayer().getLayerPlotter().setColor(layerColor.getSelectedColor());

        onClipChange();
        updateThisAndFocussedClip();
        reloadFocussedClip();
    }

    /**
     * new layer dialog
     */
    private UiControlText channels, samples;

    private JPanel createNewLayerDialogContent(int ch, int s) {
        JPanel p = new JPanel();
        UiCartesianLayout l = new UiCartesianLayout(p, 10, 2);
        l.setPreferredCellSize(new Dimension(25, 35));
        p.setLayout(l);

        l.add(new JLabel(GLanguage.translate("channels")), 0, 0, 4, 1);
        channels = new UiControlText(10, true, false);
        channels.setDataRange(1, 100);
        channels.setData(ch);
        l.add(channels, 4, 0, 6, 1);

        l.add(new JLabel(GLanguage.translate("samples")), 0, 1, 4, 1);
        samples = new GControlTextX(getMain(), 10, true, true);
        samples.setDataRange(1, 1e9);
        samples.setData(s);
        l.add(samples, 4, 1, 6, 1);

        return p;
    }

    private void onNewButton() {
        AClip c = pluginHandler.getFocussedClip();
        if (GDialog.showCustomOkCancelDialog(frame, createNewLayerDialogContent(c.getMaxNumberOfChannels(), c.getMaxSampleLength()), GLanguage.translate("newLayer"))) {
            // add a new layer "like the others", at the position of the currently selected layer...
            c.insert(new ALayer((int) channels.getData(), (int) samples.getData()), c.getSelectedIndex() + 1);
            pluginHandler.getFocussedClipEditor().reload();

            updateHistory(GLanguage.translate("newLayer"));
            onClipChange();
            autoScaleFocussedClip();
            reloadFocussedClip();
        }
    }

    private void onUpButton() {
        AClip c = pluginHandler.getFocussedClip();
        c.moveDown(c.getSelectedIndex());

        updateHistory(GLanguage.translate("moveLayerUp"));
        onClipChange();
        autoScaleFocussedClip();
        reloadFocussedClip();
    }

    private void onDownButton() {
        AClip c = pluginHandler.getFocussedClip();
        c.moveUp(c.getSelectedIndex());

        updateHistory(GLanguage.translate("moveLayerDown"));
        onClipChange();
        autoScaleFocussedClip();
        reloadFocussedClip();
    }

    private static ALayer clipBoardLayer;

    private void onCopyButton() {
        AClip c = pluginHandler.getFocussedClip();
        clipBoardLayer = new ALayer(c.getSelectedLayer());
    }

    private void onPasteButton() {
        if (clipBoardLayer != null) {
            AClip c = pluginHandler.getFocussedClip();
            c.insert(new ALayer(clipBoardLayer), c.getSelectedIndex());

            updateHistory(GLanguage.translate("pasteLayer"));
            onClipChange();
            autoScaleFocussedClip();
            reloadFocussedClip();
        }
    }

    private void onDuplicateButton() {
        AClip c = pluginHandler.getFocussedClip();
        c.insert(new ALayer(c.getSelectedLayer()), c.getSelectedIndex());

        updateHistory(GLanguage.translate("duplicateLayer"));
        onClipChange();
        autoScaleFocussedClip();
        reloadFocussedClip();
    }

    private void onDeleteButton() {
        AClip c = pluginHandler.getFocussedClip();
        c.remove(c.getSelectedIndex());

        updateHistory(GLanguage.translate("deleteLayer"));
        onClipChange();
        autoScaleFocussedClip();
        reloadFocussedClip();
    }

    private void onMergeButton() {
        AClip c = pluginHandler.getFocussedClip();
        c.mergeDownLayer(c.getSelectedIndex());

        updateHistory(GLanguage.translate("mergeUpLayer"));
        onClipChange();
        autoScaleFocussedClip();
        reloadFocussedClip();
    }

    private void onMergeAllButton() {
        AClip c = pluginHandler.getFocussedClip();
        c.mergeAllLayers();

        updateHistory(GLanguage.translate("mergeAllLayers"));
        onClipChange();
        autoScaleFocussedClip();
        reloadFocussedClip();
    }

    /**
     * loads all data from the selected clip
     */
    public void onClipChange() {
        AClip c = pluginHandler.getFocussedClip();

        // update top
        switch (c.getSelectedLayer().getType()) {
        case ALayer.AUDIO_LAYER:
            if (layerType.getSelectedIndex() != 0)
                layerType.setSelectedIndex(0);
            break;

        case ALayer.PARAMETER_LAYER:
            if (layerType.getSelectedIndex() != 1)
                layerType.setSelectedIndex(1);
            break;

        }

        // update layer stack...
        pScroll.removeAll();
        pScroll.setPreferredSize(new Dimension(300, 60 * pluginHandler.getFocussedClip().getNumberOfLayers()));
        for (int i = 0; i < c.getNumberOfLayers(); i++) {
            pScroll.add(new LayerPanel(this, c, i));
        }

        updateThisAndFocussedClip();
    }

    /**
     * A panel representing a layer in the layerstack
     */
    private class LayerPanel extends JPanel implements ActionListener, MouseListener {
        private JCheckBox visible, audible;

        private GLayerViewer layerView;

        private JTextField layerName;

        private boolean selected;

        private GPLayerStack layerStack;

        private AClip clip;

        private ALayer layer;

        private int layerIndex;

        public LayerPanel(GPLayerStack ls, AClip c, int layerIndex) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            layerStack = ls;
            clip = c;
            this.layerIndex = layerIndex;
            layer = c.getLayer(layerIndex);
            visible = new JCheckBox();
            visible.setToolTipText(GLanguage.translate("visible"));
            visible.setSelected(layer.getLayerPlotter().isVisible());
            visible.addActionListener(this);
            add(visible);

            audible = new JCheckBox();
            audible.setToolTipText(GLanguage.translate("audible"));
            audible.setSelected(layer.getType() == ALayer.AUDIO_LAYER);
            audible.addActionListener(this);
            add(audible);

            layerView = new GLayerViewer(c, layerIndex);
            layerView.setPreferredSize(new Dimension(100, 40));
            layerView.addMouseListener(this);
            add(layerView);

            layerName = new JTextField(30);
            layerName.setToolTipText(GLanguage.translate("layerName"));
            layerName.setText(layer.getName());
            layerName.addActionListener(this);
            add(layerName);
            setBorder(BorderFactory.createEtchedBorder());

            setSelected(c.getSelectedLayer() == layer);
            addMouseListener(this);
            setPreferredSize(new Dimension(380, 50));
        }

        public void setSelected(boolean b) {
            selected = b;

            if (selected) {
                setBackground(Color.blue);
                visible.setBackground(Color.blue);
                audible.setBackground(Color.blue);
                // layerColor.setBackground(Color.blue);
                layerName.setBackground(Color.blue);
            }
            repaint();
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == visible) {
                Debug.println(1, "plugin " + this.getName() + " [visible] clicked");
                layer.getLayerPlotter().setVisible(visible.isSelected());
                layerStack.pluginHandler.getFocussedClipEditor().reload();
            } else if (e.getSource() == audible) {
                Debug.println(1, "plugin " + this.getName() + " [audible] clicked");
                if (audible.isSelected())
                    layer.setType(ALayer.AUDIO_LAYER);
                else
                    layer.setType(ALayer.PARAMETER_LAYER);

                layerStack.pluginHandler.getFocussedClipEditor().reload();
            } else if (e.getSource() == layerName) {
                Debug.println(1, "plugin " + this.getName() + " [layer name] clicked");
                layer.setName(layerName.getText());
            }
            layerStack.onClipChange();
            layerStack.pluginHandler.getFocussedClipEditor().reload();
        }

        // mouse listener

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            Debug.println(1, "plugin " + this.getName() + " [select layer] mouse-click");
            clip.setSelectedIndex(layerIndex);
            layerStack.onClipChange();
            layerStack.pluginHandler.getFocussedClipEditor().reload();
        }
    }

}
