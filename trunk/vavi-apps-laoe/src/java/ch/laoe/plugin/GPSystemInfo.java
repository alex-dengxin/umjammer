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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import ch.laoe.audio.Audio;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GControlTextMem;
import ch.laoe.ui.GLanguage;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;
import ch.oli4.ui.graph.UiPlot1DLine;
import ch.oli4.ui.graph.UiPlotDefaultModel;
import ch.oli4.ui.graph.UiPlotModel;


/**
 * GPSystemInfo @author olivier gäumann, neuchâtel (switzerland) JDK: 1.4
 * 
 * plugin to display the capability of the memory and os infos.
 * 
 * @version 30.12.01 first draft oli4 12.05.2003 plugin list added oli4
 * 
 */
public class GPSystemInfo extends GPluginFrame implements Runnable {
    public GPSystemInfo(GPluginHandler ph) {
        super(ph);
        initGui();
        init();
    }

    protected String getName() {
        return "systemInfo";
    }

    // GUI
    private UiControlText freeMemory, totalMemory;

    private UiPlot1DLine memoryPlot;

    private UiPlotDefaultModel freeMemoryModel, totalMemoryModel;

    private JTextArea audioInfo, pluginInfo;

    private JButton apply, free;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        JTabbedPane tab = new JTabbedPane();

        // memory tab
        JPanel p1 = new JPanel();
        UiCartesianLayout l1 = new UiCartesianLayout(p1, 20, 7);
        l1.setPreferredCellSize(new Dimension(25, 35));
        p1.setLayout(l1);

        memoryPlot = new UiPlot1DLine();
        memoryPlot.setDataVisible(false);
        memoryPlot.setPointVisible(false);
        memoryPlot.setPScale(0, 200);
        memoryPlot.setPLabelVisible(true);
        p1.add(memoryPlot, new Rectangle(0, 0, 20, 4));

        freeMemoryModel = new UiPlotDefaultModel(memoryPlot, 200);
        freeMemoryModel.setDefaultDataColor(Color.red);
        freeMemoryModel.setLabel(GLanguage.translate("time"), UiPlotModel.P);
        freeMemoryModel.setLabel(GLanguage.translate("free"), UiPlotModel.X);
        memoryPlot.addModel(freeMemoryModel);

        totalMemoryModel = new UiPlotDefaultModel(memoryPlot, 200);
        totalMemoryModel.setDefaultDataColor(Color.blue);
        totalMemoryModel.setLabel(GLanguage.translate("time"), UiPlotModel.P);
        totalMemoryModel.setLabel(GLanguage.translate("total"), UiPlotModel.X);
        memoryPlot.addModel(totalMemoryModel);

        p1.add(new JLabel(GLanguage.translate("freeMemory")), new Rectangle(0, 4, 4, 1));
        freeMemory = new GControlTextMem(7, false, true);
        freeMemory.setDataRange(0, 1e9);
        freeMemory.setEditable(false);
        p1.add(freeMemory, new Rectangle(4, 4, 6, 1));

        p1.add(new JLabel(GLanguage.translate("totalMemory")), new Rectangle(0, 5, 4, 1));
        totalMemory = new GControlTextMem(7, false, true);
        totalMemory.setDataRange(0, 1e9);
        totalMemory.setEditable(false);
        p1.add(totalMemory, new Rectangle(4, 5, 6, 1));

        apply = new JButton(GLanguage.translate("update"));
        p1.add(apply, new Rectangle(4, 6, 5, 1));

        free = new JButton(GLanguage.translate("free"));
        p1.add(free, new Rectangle(11, 6, 5, 1));

        tab.add(p1, GLanguage.translate("memory"));

        // audio tab
        JPanel p2 = new JPanel();
        UiCartesianLayout l2 = new UiCartesianLayout(p2, 20, 7);
        l2.setPreferredCellSize(new Dimension(25, 35));
        p2.setLayout(l2);

        audioInfo = new JTextArea();
        audioInfo.setFont(new Font("Courrier", Font.PLAIN, 10));
        audioInfo.setEditable(false);
        JScrollPane audioScrollPane = new JScrollPane(audioInfo);
        p2.add(audioScrollPane, new Rectangle(0, 0, 20, 7));

        tab.add(p2, GLanguage.translate("audio"));

        // plugin tab
        JPanel p3 = new JPanel();
        UiCartesianLayout l3 = new UiCartesianLayout(p3, 20, 7);
        l3.setPreferredCellSize(new Dimension(25, 35));
        p3.setLayout(l3);

        pluginInfo = new JTextArea();
        pluginInfo.setFont(new Font("Courrier", Font.PLAIN, 10));
        pluginInfo.setEditable(false);
        JScrollPane pluginScrollPane = new JScrollPane(pluginInfo);
        p3.add(pluginScrollPane, new Rectangle(0, 0, 20, 7));

        tab.add(p3, GLanguage.translate("plugins"));

        // panel
        p.add(tab, BorderLayout.CENTER);
        frame.getContentPane().add(p);
        pack();
        frame.setResizable(true);

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
        free.addActionListener(eventDispatcher);
    }

    public void start() {
        super.start();
        audioInfo.setText(Audio.getAudioSystemInfo());

        StringBuffer psb = new StringBuffer();
        Object plugins[] = pluginHandler.getAllPlugins();
        for (int i = 0; i < plugins.length; i++) {
            GPlugin pl = (GPlugin) plugins[i];
            psb.append("" + (i + 1) + "\t");
            psb.append(GLanguage.translate(pl.getName()) + "\n");
        }
        pluginInfo.setText(psb.toString());
    }

    private class EventDispatcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == apply) {
                Debug.println(1, "plugin " + getName() + " [update numeric] clicked");
                updateNumericComponents();
            } else if (e.getSource() == free) {
                Debug.println(1, "plugin " + getName() + " [free] clicked");
                System.gc();
            }
            // autoCloseFrame();
        }
    }

    private synchronized void updateNumericComponents() {
        freeMemory.setData(runtime.freeMemory());
        totalMemory.setData(runtime.totalMemory());
    }

    private void updateGraph() {
        freeMemoryModel.appendData(runtime.freeMemory() >> 20);
        totalMemoryModel.appendData(runtime.totalMemory() >> 20);
        memoryPlot.setXScale(0, totalMemoryModel.getDataMax(UiPlotModel.X, (int) totalMemoryModel.getPMin(), (int) totalMemoryModel.getPMax()));
    }

    // thread

    private Thread thread;

    private Runtime runtime;

    private void init() {
        runtime = Runtime.getRuntime();
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(5000);
                updateNumericComponents();
                updateGraph();
            } catch (Exception e) {
            }
        }
    }

}
