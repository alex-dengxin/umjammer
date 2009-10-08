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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ch.laoe.ui.Debug;
import ch.laoe.ui.GLanguage;
import ch.oli4.ui.UiPersistance;
import ch.oli4.ui.UiPersistanceEvent;


/**
 * Class: GPTipOfTheDay @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * plugin to show the "tip of the day".
 * 
 * @version 18.10.00 erster Entwurf oli4 19.08.01 source in property-file oli4
 * 
 */
public class GPTipOfTheDay extends GPluginFrame {
    public GPTipOfTheDay(GPluginHandler ph) {
        super(ph);
        try {
            initGui();

            // start now ?
            if (showNextTime.isSelected()) {
                start();
            }
        } catch (Exception e) {
        }
    }

    protected String getName() {
        return "tipOfTheDay";
    }

    public void start() {
        super.start();
    }

    // GUI
    private JEditorPane pTip;

    private JButton bNextTip, bPreviousTip;

    private JCheckBox showNextTime;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        tipTexts = new UiPersistance("tipOfTheDay_en.properties", "tipOfTheDay_" + GLanguage.getActualLanguage() + ".properties");
        tipTexts.restore();
        pageTop = tipTexts.getString("pageTop");
        pageBottom = tipTexts.getString("pageBottom");
        maxNumberOfTips = tipTexts.getInt("numberOfTips");

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        pTip = new JEditorPane();
        pTip.setEditable(false);
        pTip.setContentType("text/html");
        JScrollPane pScroll = new JScrollPane(pTip);
        pScroll.setPreferredSize(new Dimension(450, 160));
        p.add(pScroll, BorderLayout.CENTER);

        JPanel pButtons = new JPanel();

        showNextTime = new JCheckBox(GLanguage.translate("showNextTime"));
        showNextTime.setSelected(persistance.getBoolean("plugin." + getName() + ".showNextTime"));
        pButtons.add(showNextTime);

        bNextTip = new JButton(loadIcon("resources/up.gif"));
        bNextTip.setToolTipText(GLanguage.translate("nextTip"));
        bNextTip.setPreferredSize(new Dimension(26, 26));
        pButtons.add(bNextTip);

        bPreviousTip = new JButton(loadIcon("resources/down.gif"));
        bPreviousTip.setToolTipText(GLanguage.translate("previousTip"));
        bPreviousTip.setPreferredSize(new Dimension(26, 26));
        pButtons.add(bPreviousTip);

        p.add(pButtons, BorderLayout.SOUTH);
        frame.getContentPane().add(p);
        pack();
        frame.setResizable(true);

        eventDispatcher = new EventDispatcher();
        bNextTip.addActionListener(eventDispatcher);
        bPreviousTip.addActionListener(eventDispatcher);

        loadNextTip();
    }

    // tip management

    private int tipNumber = persistance.getInt("plugin." + getName() + ".currentTip");

    private UiPersistance tipTexts;

    private String pageTop;

    private String pageBottom;

    private int maxNumberOfTips;

    private String getFormattedTipNumber() {
        return "<p align=center> (" + tipNumber + ")";
    }

    private void loadPage() {
        try {
            String s = tipTexts.getString("tip" + tipNumber);

            // out of range ?
            if (s == null) {
                tipNumber = 0;
                s = tipTexts.getString("tip0");
            }

            pTip.setText(pageTop + " " + s + getFormattedTipNumber() + " " + pageBottom);
            pTip.setCaretPosition(0);

        } catch (Exception e) {
            Debug.printStackTrace(5, e);
        }
    }

    private void loadNextTip() {
        tipNumber = (tipNumber + 1) % maxNumberOfTips;
        loadPage();
    }

    private void loadPreviousTip() {
        if (--tipNumber < 0)
            tipNumber = maxNumberOfTips - 1;

        loadPage();
    }

    public void onBackup(UiPersistanceEvent e) {
        super.onBackup(e);
        persistance.setInt("plugin." + getName() + ".currentTip", tipNumber);
        persistance.setBoolean("plugin." + getName() + ".showNextTime", showNextTime.isSelected());
    }

    private class EventDispatcher implements ActionListener {
        // action events
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == bNextTip) {
                Debug.println(1, "plugin " + getName() + " [load next] clicked");
                loadNextTip();
            }
            if (e.getSource() == bPreviousTip) {
                Debug.println(1, "plugin " + getName() + " [load previous] clicked");
                loadPreviousTip();
            }
        }
    }

}
