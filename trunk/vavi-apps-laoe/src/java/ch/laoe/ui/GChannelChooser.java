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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.ALayer;
import ch.oli4.ui.UiCartesianLayout;


/**
 * a GUI component to choose one channel from a given layer.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 17.11.00 erster Entwurf oli4
 */
public class GChannelChooser extends JPanel implements ActionListener {
    public GChannelChooser() {
        initGui();
        listenerList = new Vector<ActionListener>();
    }

    // current layer
    private ALayer layer;

    public void setLayer(ALayer l) {
        layer = l;
        reload();
    }

    // GUI
    private JComboBox paramChannel;

    private void initGui() {
        UiCartesianLayout cl = new UiCartesianLayout(this, 10, 1);
        cl.setBorderGap(0);
        cl.setCellGap(0);
        setLayout(cl);
        cl.add(new JLabel(GLanguage.translate("channel") + ":"), 0, 0, 3, 1);
        paramChannel = new JComboBox();
        cl.add(paramChannel, 3, 0, 7, 1);
        reload();

        paramChannel.addActionListener(this);
    }

    /**
     * reloads the tracks, e.g. if a track has been added
     */
    public void reload() {
        if (layer != null) {
            int maxIndex = layer.getNumberOfChannels();
            int actualIndex = paramChannel.getSelectedIndex();

            paramChannel.removeAllItems();
            for (int i = 0; i < maxIndex; i++) {
                paramChannel.addItem(layer.getChannel(i));
            }

            if (actualIndex < maxIndex)
                paramChannel.setSelectedIndex(actualIndex);
        }
    }

    /**
     * returns the actually selected track
     */
    public AChannel getSelectedChannel() {
        return (AChannel) paramChannel.getSelectedItem();
    }

    /**
     * returns the actually selected track index
     */
    public int getSelectedChannelIndex() {
        int i = paramChannel.getSelectedIndex();
        // range check
        if (i < 0)
            return 0;
        else if (i > layer.getNumberOfChannels() - 1)
            return layer.getNumberOfChannels() - 1;
        else
            return i;
    }

    /**
     * set the layerchooser enabled
     */
    public void setEnabled(boolean b) {
        paramChannel.setEnabled(b);
        repaint();
    }

    /**
     * set the layerchooser enabled
     */
    public boolean isEnabled() {
        return paramChannel.isEnabled();
    }

    // event reception

    public void actionPerformed(ActionEvent e) {
        fireActionEvent();
    }

    // event generation
    private Vector<ActionListener> listenerList;

    private void fireActionEvent() {
        ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
        for (int i = 0; i < listenerList.size(); i++) {
            listenerList.get(i).actionPerformed(ae);
        }
    }

    public void addActionListener(ActionListener al) {
        listenerList.add(al);
    }
}
