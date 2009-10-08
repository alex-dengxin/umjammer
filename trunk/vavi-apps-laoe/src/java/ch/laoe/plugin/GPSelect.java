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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelMarker;
import ch.laoe.clip.AChannelPlotter;
import ch.laoe.clip.AChannelSelection;
import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;
import ch.laoe.operation.AOToolkit;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextX;
import ch.laoe.ui.GLanguage;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlEvent;
import ch.oli4.ui.UiControlListener;
import ch.oli4.ui.UiControlText;


/**
 * plugin to select the samples. mouse action table:
 * ----------------------------------------------------------------------------
 *  press-drag-release click double-click tripple-click
 * ----------------------------------------------------------------------------
 *  - select range unselect channel
 * select channel select layer shift deep " deep " deep " deep "
 * ----------------------------------------------------------------------------
 * 
 * in addition, if the mouse is pressed near the begin or end of the selection,
 * the "tuning mode" is active. in this mode, the selection can be fine-tuned,
 * the begin or end of a selection can be changed independently.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 01.10.00 erster Entwurf oli4 <br>
 *          11.11.00 deep selection (by shift-key) oli4 <br>
 *          30.01.01 extended select-functions controlled by mouse oli4 <br>
 *          xx.03.01 beautiful cursors added... oli4 <br>
 *          06.05.01 add tuning mode oli4 <br>
 *          08.12.01 add a frame, optional snap to zero-cross oli4 <br>
 *          16.12.01 add changeable unit to selectionplots oli4 <br>
 *          11.01.02 introduce intensity-points oli4 <br>
 *          27.01.02 concept-change: always "multi"-selection oli4 <br>
 *          16.06.02 use channelstack as channel-chooser oli4 <br>
 *          02.07.02 markers manipulation added oli4 <br>
 */
public class GPSelect extends GPluginFrame {
    public GPSelect(GPluginHandler ph) {
        super(ph);
        initCursor();
        initGlobalSelection();
        initIntensitySelection();
        initGui();
    }

    protected String getName() {
        return "select";
    }

    public void start() {
        super.start();
        pluginHandler.setFocussedPlugin(this);
    }

    // cursor

    private Cursor selectionCursor, tuningSelectionCursor, deepSelectionCursor, intensityDrawCursor, intensityEraseCursor, markerDrawCursor, markerEraseCursor;

    private Cursor actualCursor;

    private void initCursor() {
        selectionCursor = createCustomCursor("selectionCursor");
        tuningSelectionCursor = createCustomCursor("tuningSelectionCursor");
        deepSelectionCursor = createCustomCursor("deepSelectionCursor");
        intensityDrawCursor = createCustomCursor("intensityDrawCursor");
        intensityEraseCursor = createCustomCursor("intensityEraseCursor");
        markerDrawCursor = createCustomCursor("markerDrawCursor");
        markerEraseCursor = createCustomCursor("markerEraseCursor");
        actualCursor = null;
    }

    private void setCursor(MouseEvent e, Cursor c) {
        // change detector...
        if (c != actualCursor) {
            actualCursor = c;
            ((Component) e.getSource()).setCursor(actualCursor);
        }
    }

    // global selection manipulating

    // when mouse was pressed
    private Point pressedPoint;

    private boolean deepSelection;

    private int startSample, endSample;

    private int startChannelIndex, endChannelIndex;

    private AClip clip;

    private ALayer layer;

    // tuning
    private int tuningMode;

    private static final int NO_TUNING = 1;

    private static final int START_TUNING = 2;

    private static final int END_TUNING = 3;

    private static final int tuningXTolerance = 5;

    // snap
    private boolean snapEnable;

    private int xSnapRange;

    private void initGlobalSelection() {
    }

    private void globalSelectionMousePressed(MouseEvent e) {
        Debug.println(6, "mouse pressed: start select functionality");

        // locate pressed layer, channel and sample
        pressedPoint = e.getPoint();
        clip = getFocussedClip();
        layer = clip.getSelectedLayer();
        startChannelIndex = layer.getLayerPlotter().getInsideChannelIndex(pressedPoint);

        if (startChannelIndex >= 0) {
            AChannel c = layer.getChannel(startChannelIndex);
            AChannelSelection cs = c.getChannelSelection();
            AChannelPlotter cp = c.getChannelPlotter();

            // identify tuning mode ?
            int xss = cp.sampleToGraphX(cs.getOffset());
            int xse = cp.sampleToGraphX(cs.getOffset() + cs.getLength());
            if (Math.abs(pressedPoint.x - xss) < tuningXTolerance) {
                tuningMode = START_TUNING;
            } else if (Math.abs(pressedPoint.x - xse) < tuningXTolerance) {
                tuningMode = END_TUNING;
            } else {
                tuningMode = NO_TUNING;
            }

            int x = (int) cp.graphToSampleX(pressedPoint.x);
            if (x < 0)
                x = 0;

            // snap to zero cross
            if (snapEnable) {
                xSnapRange = (int) (cp.graphToSampleX(pressedPoint.x + 20) - x);
                int sx = AOToolkit.getNearestZeroCrossIndex(layer.getChannel(endChannelIndex).sample, x, xSnapRange);
                if (sx != -1) {
                    x = sx;
                }
            }

            // tuning mode - dependent
            switch (tuningMode) {
            case NO_TUNING:
                startSample = x;
                break;

            case START_TUNING:
                endSample = cs.getOffset() + cs.getLength();
                break;

            case END_TUNING:
                startSample = cs.getOffset();
                break;
            }
            Debug.println(6, "tuning mode = " + tuningMode);

            // keys
            // multiSelection = isCtrlKey(e);
            deepSelection = isShiftKey(e);
        }
    }

    private void globalSelectionMouseDragged(MouseEvent e) {
        Debug.println(6, "mouse dragged: create selection");
        // locate channel and sample
        Point draggedPoint = e.getPoint();
        endChannelIndex = layer.getLayerPlotter().getInsideChannelIndex(draggedPoint);
        // channel ?
        if ((startChannelIndex >= 0) && (endChannelIndex >= 0)) {
//          AChannelSelection cs = layer.getChannel(endChannelIndex).getChannelSelection();
            int x = (int) layer.getChannel(endChannelIndex).getChannelPlotter().graphToSampleX(draggedPoint.x);
            if (x < 0)
                x = 0;

            // snap to zero cross
            if (snapEnable) {
                int sx = AOToolkit.getNearestZeroCrossIndex(layer.getChannel(endChannelIndex).sample, x, xSnapRange);
                if (sx != -1) {
                    x = sx;
                }
            }

            // tuning mode - dependent
            switch (tuningMode) {
            case NO_TUNING:
                endSample = x;
                break;

            case START_TUNING:
                startSample = x;
                break;

            case END_TUNING:
                endSample = x;
                break;
            }

            // swap channel order ?
            if (startChannelIndex > endChannelIndex) {
                int s = startChannelIndex;
                startChannelIndex = endChannelIndex;
                endChannelIndex = s;
            }

            // build selections, every channel...
            for (int i = startChannelIndex; i <= endChannelIndex; i++) {
                // deep selection ?
                if (deepSelection) {
                    // every layer...
                    for (int j = 0; j < clip.getNumberOfLayers(); j++) {
                        // create range selection
                        clip.getLayer(j).getChannel(i).modifyChannelSelection(Math.min(startSample, endSample), Math.abs(endSample - startSample));
                    }
                }
                // flat selection ?
                else {
                    // create range selection
                    layer.getChannel(i).modifyChannelSelection(Math.min(startSample, endSample), Math.abs(endSample - startSample));
                }
            }
        }
        repaintFocussedClipEditor();

        // cursor...
        switch (tuningMode) {
        case START_TUNING:
        case END_TUNING:
            setCursor(e, tuningSelectionCursor);
            break;
        }

    }

    private void globalSelectionMouseClicked(MouseEvent e) {
        // click ?
        if (e.getClickCount() == 1) {
            Debug.println(6, "mouse clicked: unselect");
            // deep selection ?
            if (deepSelection) {
                // every layer...
                for (int i = 0; i < clip.getNumberOfLayers(); i++) {
                    // unselect channel
                    clip.getLayer(i).getChannel(startChannelIndex).setEmptyChannelSelection();
                }
            }
            // flat selection ?
            else {
                // unselect channel
                layer.getChannel(startChannelIndex).setEmptyChannelSelection();
            }
        }
        // double click ?
        else if (e.getClickCount() == 2) {
            Debug.println(6, "mouse double-clicked: create marked selection");
            // deep selection ?
            if (deepSelection) {
                // every layer...
                for (int i = 0; i < clip.getNumberOfLayers(); i++) {
                    // create channel selection
                    clip.getLayer(i).getChannel(startChannelIndex).setMarkedChannelSelection(startSample);
                }
            }
            // flat selection ?
            else {
                // create channel selection
                layer.getChannel(startChannelIndex).setMarkedChannelSelection(startSample);
            }
        }
        // tripple click ?
        else if (e.getClickCount() == 3) {
            Debug.println(6, "mouse tripple-clicked: create channel selection");
            // deep selection ?
            if (deepSelection) {
                // every layer...
                for (int i = 0; i < clip.getNumberOfLayers(); i++) {
                    // create channel selection
                    clip.getLayer(i).getChannel(startChannelIndex).setFullChannelSelection();
                }
            }
            // flat selection ?
            else {
                // create channel selection
                layer.getChannel(startChannelIndex).setFullChannelSelection();
            }
        }
        // quadruple click ?
        else if (e.getClickCount() == 4) {
            Debug.println(6, "mouse quadruple-clicked: create layer selection");
            // deep selection ?
            if (deepSelection) {
                // every layer...
                for (int i = 0; i < clip.getNumberOfLayers(); i++) {
                    // create channel selection
                    clip.getLayer(i).setFullLayerSelection();
                }
            }
            // flat selection ?
            else {
                // create channel selection
                layer.setFullLayerSelection();
            }
        }
        repaintFocussedClipEditor();
        updateHistory(GLanguage.translate(getName()));
    }

    private void globalSelectionMouseReleased(MouseEvent e) {
        updateHistory(GLanguage.translate(getName()));
    }

    private void globalSelectionMouseMoved(MouseEvent e) {
        // choose cursor...
        if (isShiftKey(e)) {
            setCursor(e, deepSelectionCursor);
        } else {
            setCursor(e, selectionCursor);
        }
    }

    // intensity selection

    private int intensityPointIndex;

    private AChannelSelection iChannelSelection;

    private AChannelPlotter iChannelPlotter;

    private void initIntensitySelection() {
    }

    private float toNormalizedX(int x) {
        float f = (iChannelPlotter.graphToSampleX(x) - iChannelSelection.getOffset()) / iChannelSelection.getLength();
        if (f < 0)
            return 0.f;
        else if (f > 1)
            return 1.f;
        else
            return f;
    }

    private float toNormalizedY(int y) {
        float f = 1.f - ((float) ((y - iChannelPlotter.getRectangle().getY()) / iChannelPlotter.getRectangle().getHeight()));
        if (f < 0)
            return 0.f;
        else if (f > 1)
            return 1.f;
        else
            return f;
    }

    private void intensitySelectionMousePressed(MouseEvent e) {
        Debug.println(6, "mouse pressed: find intensity point");

        // locate pressed layer, channel and selection
        AClip c = getFocussedClip();
        ALayer l = c.getSelectedLayer();
        int chi = l.getLayerPlotter().getInsideChannelIndex(e.getPoint());

        if (chi >= 0) {
            AChannel ch = l.getChannel(chi);
            iChannelSelection = ch.getChannelSelection();
            iChannelPlotter = ch.getChannelPlotter();
            intensityPointIndex = iChannelSelection.searchNearestIntensityPointIndex(toNormalizedX(e.getPoint().x));
        }
    }

    private void intensitySelectionMouseReleased(MouseEvent e) {
    }

    private void intensitySelectionMouseClicked(MouseEvent e) {
        float x = toNormalizedX(e.getPoint().x);
        float y = toNormalizedY(e.getPoint().y);

        if (isShiftKey(e)) {
            iChannelSelection.removeIntensityPoint(x);
        } else {
            iChannelSelection.addIntensityPoint(x, y);
        }
        repaintFocussedClipEditor();
    }

    private void intensitySelectionMouseMoved(MouseEvent e) {
        // locate pressed layer, channel and selection
        AClip c = getFocussedClip();
        ALayer l = c.getSelectedLayer();
        int chi = l.getLayerPlotter().getInsideChannelIndex(e.getPoint());

        if (chi >= 0) {
            AChannel ch = l.getChannel(chi);
            iChannelSelection = ch.getChannelSelection();
            iChannelPlotter = ch.getChannelPlotter();
            intensityPointIndex = iChannelSelection.searchNearestIntensityPointIndex(toNormalizedX(e.getPoint().x));

            float x = toNormalizedX(e.getPoint().x);
            iChannelSelection.setActiveIntensityPoint(x);
            repaintFocussedClipEditor();
        }

        // cursor
        if (isShiftKey(e)) {
            setCursor(e, intensityEraseCursor);
        } else {
            setCursor(e, intensityDrawCursor);
        }
    }

    private void intensitySelectionMouseDragged(MouseEvent e) {
        float x = toNormalizedX(e.getPoint().x);
        float y = toNormalizedY(e.getPoint().y);
        // System.out.println("x="+x+" y="+y);

        iChannelSelection.modifyIntensityPoint(intensityPointIndex, x, y);
        repaintFocussedClipEditor();
    }

    // markers

    private int markerIndex;

    private AChannelMarker markers;

    private final int markerSnapXDistance = 10;

    private AChannelPlotter markerChp;

    private void markersMousePressed(MouseEvent e) {
        Debug.println(6, "mouse pressed: find marker");

        // locate pressed layer, channel and selection
        AClip c = getFocussedClip();
        ALayer l = c.getSelectedLayer();
        int chi = l.getLayerPlotter().getInsideChannelIndex(e.getPoint());

        if (chi >= 0) {
            AChannel ch = l.getChannel(chi);
            markerChp = ch.getChannelPlotter();
            markers = ch.getMarker();
            markerIndex = markers.searchNearestIndex((int) markerChp.graphToSampleX(e.getPoint().x), (int) Math.abs(markerChp.graphToSampleX(e.getPoint().x + markerSnapXDistance) - markerChp.graphToSampleX(e.getPoint().x)));
        }
    }

    private void markersMouseReleased(MouseEvent e) {
    }

    private void markersMouseClicked(MouseEvent e) {
        if (isShiftKey(e)) {
            markers.removeMarker(markerIndex);
        } else {
            markers.addMarker((int) markerChp.graphToSampleX(e.getPoint().x));
        }
        repaintFocussedClipEditor();
    }

    private void markersMouseMoved(MouseEvent e) {
        // cursor
        if (isShiftKey(e)) {
            setCursor(e, markerEraseCursor);
        } else {
            setCursor(e, markerDrawCursor);
        }
    }

    private void markersMouseDragged(MouseEvent e) {
        Debug.println(6, "mouse dragged: move marker");
        if (markerIndex >= 0) {
            markers.moveMarker(markerIndex, (int) markerChp.graphToSampleX(e.getPoint().x));
        }
        repaintFocussedClipEditor();
    }

    // mouse-event dispatching

    public void mousePressed(MouseEvent e) {
        switch (tab.getSelectedIndex()) {
        case 0:
            globalSelectionMousePressed(e);
            break;

        case 1:
            intensitySelectionMousePressed(e);
            break;

        case 2:
            markersMousePressed(e);
            break;
        }
    }

    public void mouseDragged(MouseEvent e) {
        switch (tab.getSelectedIndex()) {
        case 0:
            globalSelectionMouseDragged(e);
            break;

        case 1:
            intensitySelectionMouseDragged(e);
            break;

        case 2:
            markersMouseDragged(e);
            break;
        }
    }

    public void mouseClicked(MouseEvent e) {
        switch (tab.getSelectedIndex()) {
        case 0:
            globalSelectionMouseClicked(e);
            break;

        case 1:
            intensitySelectionMouseClicked(e);
            break;

        case 2:
            markersMouseClicked(e);
            break;
        }
    }

    public void mouseEntered(MouseEvent e) {
        ((Component) e.getSource()).setCursor(actualCursor);
    }

    public void mouseExited(MouseEvent e) {
        actualCursor = null;
    }

    public void mouseReleased(MouseEvent e) {
        switch (tab.getSelectedIndex()) {
        case 0:
            globalSelectionMouseReleased(e);
            break;

        case 1:
            intensitySelectionMouseReleased(e);
            break;

        case 2:
            markersMouseReleased(e);
            break;
        }
    }

    public void mouseMoved(MouseEvent e) {
        switch (tab.getSelectedIndex()) {
        case 0:
            globalSelectionMouseMoved(e);
            break;

        case 1:
            intensitySelectionMouseMoved(e);
            break;

        case 2:
            markersMouseMoved(e);
            break;
        }
    }

    // ********************* GUI **********************

    private JTabbedPane tab;

    private JCheckBox snapToZeroCross;

    private UiControlText zeroCrossWidth;

    private JButton fullIntensity, clearMarkers, copyMarkers, pasteMarkers, markersFromSelection;

    private JComboBox intensityScale;

    private EventDispatcher eventDispatcher;

    public void initGui() {
        tab = new JTabbedPane();

        // global selection tab
        JPanel p1 = new JPanel();
        UiCartesianLayout l1 = new UiCartesianLayout(p1, 8, 3);
        l1.setPreferredCellSize(new Dimension(30, 35));
        p1.setLayout(l1);

        snapToZeroCross = new JCheckBox(GLanguage.translate("snapToZeroCross"));
        l1.add(snapToZeroCross, 0, 0, 6, 1);

        l1.add(new JLabel(GLanguage.translate("zeroCrossWidth")), 0, 2, 4, 1);

        zeroCrossWidth = new GControlTextX(getMain(), 8, true, true);
        zeroCrossWidth.setData(AOToolkit.getZeroCrossWidth());
        zeroCrossWidth.setDataRange(0, 300);
        l1.add(zeroCrossWidth, 4, 2, 4, 1);

        tab.add(p1, GLanguage.translate("range"));

        // intensity selection tab
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 8, 3);
        l2.setPreferredCellSize(new Dimension(30, 35));
        p2.setLayout(l2);

        l2.add(new JLabel(GLanguage.translate("scale")), 0, 0, 4, 1);
        String intensityScaleItems[] = {
            GLanguage.translate("squareRoot"), GLanguage.translate("linear"), GLanguage.translate("square"), GLanguage.translate("cubic")
        };
        intensityScale = new JComboBox(intensityScaleItems);
        intensityScale.setSelectedIndex(1);
        l2.add(intensityScale, 4, 0, 4, 1);

        fullIntensity = new JButton(GLanguage.translate("clear"));
        l2.add(fullIntensity, 2, 2, 4, 1);

        tab.add(p2, GLanguage.translate("intensity"));

        // marker tab
        JPanel p3 = new JPanel();
        UiCartesianLayout l3 = new UiCartesianLayout(p3, 8, 3);
        l3.setPreferredCellSize(new Dimension(30, 35));
        p3.setLayout(l3);

        copyMarkers = new JButton(GLanguage.translate("copy"));
        l3.add(copyMarkers, 0, 0, 4, 1);

        pasteMarkers = new JButton(GLanguage.translate("paste"));
        l3.add(pasteMarkers, 4, 0, 4, 1);

        markersFromSelection = new JButton(GLanguage.translate("fromSelection"));
        l3.add(markersFromSelection, 0, 1, 4, 1);

        clearMarkers = new JButton(GLanguage.translate("clear"));
        l3.add(clearMarkers, 2, 2, 4, 1);

        tab.add(p3, GLanguage.translate("markers"));

        frame.getContentPane().add(tab);
        pack();

        eventDispatcher = new EventDispatcher();
        snapToZeroCross.addActionListener(eventDispatcher);
        fullIntensity.addActionListener(eventDispatcher);
        intensityScale.addActionListener(eventDispatcher);
        zeroCrossWidth.addControlListener(eventDispatcher);
        copyMarkers.addActionListener(eventDispatcher);
        pasteMarkers.addActionListener(eventDispatcher);
        markersFromSelection.addActionListener(eventDispatcher);
        clearMarkers.addActionListener(eventDispatcher);

        onIntensityScale();
    }

    private class EventDispatcher implements ActionListener, UiControlListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == snapToZeroCross) {
                Debug.println(1, "plugin " + getName() + " [snap to zerocross] clicked");
                snapEnable = snapToZeroCross.isSelected();
            } else if (e.getSource() == fullIntensity) {
                Debug.println(1, "plugin " + getName() + " [full intensity] clicked");
                onFullIntensity();
            } else if (e.getSource() == intensityScale) {
                Debug.println(1, "plugin " + getName() + " [intensity scale] clicked");
                onIntensityScale();
            } else if (e.getSource() == copyMarkers) {
                Debug.println(1, "plugin " + getName() + " [copy markers] clicked");
                onCopyMarkers();
            } else if (e.getSource() == pasteMarkers) {
                Debug.println(1, "plugin " + getName() + " [paste markers] clicked");
                onPasteMarkers();
            } else if (e.getSource() == markersFromSelection) {
                Debug.println(1, "plugin " + getName() + " [from selection] clicked");
                onMarkersFromSelection();
            } else if (e.getSource() == clearMarkers) {
                Debug.println(1, "plugin " + getName() + " [clear markers] clicked");
                onClearMarkers();
            }
        }

        public void onDataChanged(UiControlEvent e) {
            if (e.getSource() == zeroCrossWidth) {
                Debug.println(1, "plugin " + getName() + " [zerocross width] changed");
                AOToolkit.setZeroCrossWidth((int) zeroCrossWidth.getData());
            }
        }

        public void onDataChanging(UiControlEvent e) {
        }

        public void onValidate(UiControlEvent e) {
        }
    }

    private void onIntensityScale() {
        int s;

        switch (intensityScale.getSelectedIndex()) {
        case 0:
            s = AChannelSelection.SQUARE_ROOT_INTENSITY_SCALE;
            break;

        case 2:
            s = AChannelSelection.SQUARE_INTENSITY_SCALE;
            break;

        case 3:
            s = AChannelSelection.CUBIC_INTENSITY_SCALE;
            break;

        default:
            s = AChannelSelection.LINEAR_INTENSITY_SCALE;
            break;
        }
        AChannelSelection.setIntensityScale(s);
    }

    private void onFullIntensity() {
        getFocussedClip().getSelectedLayer().getSelectedChannel().getChannelSelection().clearIntensity();
        repaintFocussedClipEditor();
    }

    private AChannelMarker markerClipBoard;

    private void onCopyMarkers() {
        markerClipBoard = new AChannelMarker(getFocussedClip().getSelectedLayer().getSelectedChannel().getMarker());
    }

    private void onPasteMarkers() {
        getFocussedClip().getSelectedLayer().getSelectedChannel().setMarker(new AChannelMarker(markerClipBoard));
        repaintFocussedClipEditor();
    }

    private void onMarkersFromSelection() {
        AChannel ch = getFocussedClip().getSelectedLayer().getSelectedChannel();
        ch.getMarker().addMarkerFromSelection(ch.getChannelSelection());
        repaintFocussedClipEditor();
    }

    private void onClearMarkers() {
        getFocussedClip().getSelectedLayer().getSelectedChannel().getMarker().clear();
        repaintFocussedClipEditor();
    }

}
