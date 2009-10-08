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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.operation.AOMove;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GProgressViewer;


/**
 * plugin to move the selection.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 13.04.01 first draft oli4
 */
public class GPMove extends GPlugin {
    public GPMove(GPluginHandler ph) {
        super(ph);
        initGraphics();
        initCursors();
    }

    protected String getName() {
        return "move";
    }

    public JMenuItem createMenuItem() {
        return super.createMenuItem(KeyEvent.VK_M);
    }

    public void start() {
        super.start();
        pluginHandler.setFocussedPlugin(this);
    }

    // cursors

    private Cursor moveCursor, moveSelectionCursor;

    private Cursor actualCursor;

    private void initCursors() {
        moveCursor = createCustomCursor("moveCursor");
        moveSelectionCursor = createCustomCursor("moveSelectionCursor");
        actualCursor = null;
    }

    private void setCursor(MouseEvent e, Cursor c) {
        // change detector...
        if (c != actualCursor) {
            actualCursor = c;
            ((Component) e.getSource()).setCursor(actualCursor);
        }
    }

    // mouse

    private boolean ctrlActive;

    private boolean mouseDown;

    private AChannel channel;

    private int channelIndex;

    private int pressedX, draggedX;

    private int draggedGraphX;

    private int pressedSelectionOffset;

    public void mousePressed(MouseEvent e) {
        mouseDown = true;
        // start move track
        channelIndex = getFocussedClip().getSelectedLayer().getLayerPlotter().getInsideChannelIndex(e.getPoint());
        channel = getFocussedClip().getSelectedLayer().getChannel(channelIndex);
        pressedX = (int) channel.getChannelPlotter().graphToSampleX(e.getPoint().x);
        pressedSelectionOffset = channel.getChannelSelection().getOffset();
        // store keys
        ctrlActive = isCtrlKey(e);
    }

    public void mouseReleased(MouseEvent e) {
        // end move track
        if (ctrlActive) {
        } else {
            if (channelIndex >= 0) {
                // move samples...
                int newIndex = (int) channel.getChannelPlotter().graphToSampleX(e.getPoint().x);
                GProgressViewer.start(getName());
                ALayerSelection ls = getFocussedClip().getSelectedLayer().getLayerSelection();
                ls.operateEachChannel(new AOMove(newIndex));
                reloadFocussedClipEditor();
                getFocussedClip().getSelectedLayer().setEmptyLayerSelection();
                updateHistory(GLanguage.translate(getName()));
                GProgressViewer.finish();
            }
        }

        mouseDown = false;
        ctrlActive = false;
    }

    public void mouseMoved(MouseEvent e) {
        if (isCtrlKey(e)) {
            setCursor(e, moveSelectionCursor);
        } else {
            setCursor(e, moveCursor);
        }
    }

    public void mouseDragged(MouseEvent e) {
        draggedGraphX = e.getPoint().x;
        if (ctrlActive) {
            if (channelIndex >= 0) {
                // move selection...
                draggedX = (int) channel.getChannelPlotter().graphToSampleX(e.getPoint().x);
                channel.getChannelSelection().setOffset(pressedSelectionOffset + draggedX - pressedX);
            }
        }

        repaintFocussedClipEditor();
    }

    public void mouseEntered(MouseEvent e) {
        ((Component) e.getSource()).setCursor(actualCursor);
    }

    private Stroke stroke;

    private void initGraphics() {
        stroke = new BasicStroke();
    }

    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
        if (mouseDown && (!ctrlActive)) {
            int x1 = draggedGraphX;
            int y1 = (int) rect.getY() + 1;
            int x2 = x1;
            int y2 = (int) rect.getY() + (int) rect.getHeight() - 1;
            int w = 3;

            // draw "I" insert-pointer
            g2d.setColor(Color.black);
            g2d.setStroke(stroke);
            g2d.drawLine(x1, y1, x2, y2);
            g2d.drawLine(x1 - w, y1, x1 + w, y1);
            g2d.drawLine(x2 - w, y2, x2 + w, y2);
        }
    }

}
